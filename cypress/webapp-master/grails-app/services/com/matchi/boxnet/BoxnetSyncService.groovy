package com.matchi.boxnet

import com.matchi.CashRegisterTransaction
import com.matchi.Customer
import com.matchi.Facility
import com.matchi.external.BoxnetTransaction
import com.matchi.invoice.InvoiceRow
import com.matchi.payment.PaymentMethod
import grails.util.Holders
import groovyx.net.http.ContentType
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.Method
import org.apache.commons.lang.StringEscapeUtils
import org.joda.time.DateTime

import java.text.DecimalFormat
import java.text.SimpleDateFormat

class BoxnetSyncService {
    static transactional = true

    def grailsApplication
    def springSecurityService
    def userService
    def externalSynchronizationService

    def BASE_URL = Holders.config.boxnet.transactions.url
    def dateFormat = "yyyy-MM-dd HH:mm:ss" // 6/25/2013 12:35:05 PM
    def numberFormat = new DecimalFormat("###.##")
    def facilitiesShortnameSyncList = Holders.config.boxnet.transactions.facilities

    def syncTransactions() {
        return syncTransactions(null, null)
    }

    def syncTransactions(def start, def stop) {

        def startDate = start ?: new DateTime().minusDays(1)
        def stopDate = stop ?: new DateTime().minusDays(1)

        def facilities = getSyncFacilities()

        log.debug("Got facilities to sync ${facilities}")

        facilities.each { Facility facility ->
            def items = null

            getTransactionsForFacilityAndDate(facility, startDate, stopDate) { def response ->
                //log.debug("Got response: ${response}")
                items = response
            }

            log.info("Got: ${items.Transaktion.size()} transactions for ${facility.name}")
            def kvittonr = BoxnetTransaction.findAll().collect { it.kvittonr }.unique()

            // iterating through XML blocks
            items.Transaktion.each {
                def transaction = it
                if (!kvittonr.contains(transaction.@Kvittonr)) {
                    def boxnetTransaction = createBoxnetTransaction(transaction)
                    createCashRegisterTransaction(boxnetTransaction, facility)
                } else {
                    log.debug("Receiptnumber already exists, skipping...")
                }
            }
        }
    }

    def getTransactionsForFacilityAndDate(Facility facility, DateTime start, DateTime stop, Closure closure) {

        def httpUrl = getUrl(facility, start, stop)
        log.info("Fetching Boxnet transactions from ${httpUrl} for: ${facility.name} between: ${start.toString("yyyy-MM-dd")} and ${stop.toString("yyyy-MM-dd")}")

        new HTTPBuilder( httpUrl ).request( Method.GET, ContentType.TEXT ) {
            requestContentType = 'application/x-www-form-urlencoded; charset=utf-8'

            response.failure = { resp ->
                throw new IllegalStateException("Error when connecting to Boxnet: ${httpUrl}")
            }

            response.success = { resp, xmlData ->
                def xml = xmlData.getText()
                log.info("Received transactions for ${facility.name} between ${start.toString("yyyy-MM-dd")} and ${stop.toString("yyyy-MM-dd")}: ${xml}")

                def objects = new XmlParser().parseText( xml )
                closure.call(objects)
            }
        }
    }

    private def createBoxnetTransaction(def transaction) {
        log.info("${transaction}")
        def boxnetTransaction = new BoxnetTransaction()

        boxnetTransaction.with {
            tid         = transaction.@Tid
            kundId      = transaction.@KundId
            produktnr   = transaction.@Produktnr
            titel       = StringEscapeUtils.unescapeHtml(transaction.@Titel)
            betalsatt   = StringEscapeUtils.unescapeHtml(transaction.@Betalsatt)
            debPris     = transaction.@DebPris
            momssats    = transaction.@Momssats
            kassa       = StringEscapeUtils.unescapeHtml(transaction.@Kassa)
            kassakod    = transaction.@Kassakod
            kvittonr    = transaction.@Kvittonr
        }

        if (!boxnetTransaction.validate()) {
            log.error("${boxnetTransaction.errors}")
            return
        }
        boxnetTransaction.save()

        return boxnetTransaction
    }

    private def createCashRegisterTransaction(BoxnetTransaction boxnetTransaction, Facility facility) {
        def customerId = externalSynchronizationService.getLocalCustomerIdFromFortnoxCustomerNumber(boxnetTransaction.getKundId(), facility.fortnoxAuthentication.db)

        log.info("Got customerid ${customerId}")

        def customer   = Customer.findByIdAndFacility(customerId, facility)
        def vat = getVat(boxnetTransaction.momssats)

        if (!customer) {
            log.error("Could not find a customer with ExternalEntityId:${boxnetTransaction.kundId}")
            return
        }

        def paymentMethod = getPaymentMethod(boxnetTransaction.betalsatt)
        if (!paymentMethod) {
            log.error("Could not find matching paymentMethod for ${boxnetTransaction.betalsatt}")
            return
        }

        def cashRegisterTransaction           = new CashRegisterTransaction()
        cashRegisterTransaction.customer      = customer
        cashRegisterTransaction.date          = new SimpleDateFormat(dateFormat).parse(boxnetTransaction.tid)
        cashRegisterTransaction.title         = boxnetTransaction.titel
        cashRegisterTransaction.method        = paymentMethod
        cashRegisterTransaction.paidAmount    = boxnetTransaction.debPris.replace(",",".").toBigDecimal()
        cashRegisterTransaction.vat           = vat
        cashRegisterTransaction.receiptNumber = boxnetTransaction.kvittonr

        if (!cashRegisterTransaction.validate()) {
            log.error("${cashRegisterTransaction.errors}")
            return
        }

        cashRegisterTransaction.save()
        boxnetTransaction.syncedDate = new Date()
        boxnetTransaction.save()

        if (facility.hasApplicationInvoice() && cashRegisterTransaction.method.equals(PaymentMethod.INVOICE)) {
            InvoiceRow row  = new InvoiceRow()
            row.createdBy   = userService.getLoggedInOrSystemUser()
            row.customer    = cashRegisterTransaction.customer
            row.description = cashRegisterTransaction.title
            row.price       = cashRegisterTransaction.paidAmount.toInteger()
            row.amount      = 1
            row.vat         = new BigDecimal(vat.replace(",",".")) * 100

            row.save()
        }

        return cashRegisterTransaction
    }

    String getVat(String s) {
        def vatNumber = new BigDecimal(s.replace(",","."))
        return numberFormat.format((1/vatNumber) - 1)
    }

    def getUrl(Facility facility, DateTime start, DateTime stop) {
        return "${BASE_URL}?${appendParameters(facility, start, stop)}"
    }

    def appendParameters(Facility facility, DateTime start, DateTime stop) {
        def user = grailsApplication.config.boxnet.transactions.user
        def key  = grailsApplication.config.boxnet.transactions.key

        def startDateFormatted = new DateTime(start).toString("yyyy-MM-dd")
        def stopDateFormatted = new DateTime(stop).toString("yyyy-MM-dd")

        def authParams = "user=${user}&key=${key}"
        def metaParams = "shortname=${facility.shortname}&start=${startDateFormatted}&stop=${stopDateFormatted}"

        return "${authParams}&${metaParams}"
    }

    def getPaymentMethod(String s) {
        switch (s) {
            case "K":
                return PaymentMethod.CASH
                break
            case "B":
                return PaymentMethod.CREDIT_CARD
                break
            case "I":
                return PaymentMethod.INVOICE
                break
            case "S":
                return PaymentMethod.SWISH
                break
            default:
                break
        }
    }

    def getSyncFacilities() {
        return Facility.where {
            shortname in facilitiesShortnameSyncList
        }.list()
    }
}
