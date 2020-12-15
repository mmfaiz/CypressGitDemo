package com.matchi.invoice
import com.matchi.Customer
import com.matchi.LuhnValidator
import com.matchi.FacilityProperty
import com.matchi.facility.Organization
import grails.util.Holders
import org.hibernate.criterion.CriteriaSpecification
import org.joda.time.DateTime
import org.joda.time.Days
import org.joda.time.LocalDate

import java.math.RoundingMode

class Invoice implements Serializable {

    static final enum InvoiceStatus {
        READY("badge-info"),
        PAID("badge-success"),
        OVERDUE("badge-important"),
        POSTED("badge-warning"),
        CANCELLED(""),
        INCORRECT("badge-important"),
        CREDITED("")

        final String badgeClass

        InvoiceStatus(String badgeClass) {
            this.badgeClass = badgeClass
        }

        static List preselectedInvoiceFilter() {
            [READY, PAID, OVERDUE, POSTED, INCORRECT].collect() { it.toString() }
        }
    }
    /**
     * Status SENT saying that Invoice was SENT or PRINT but we has no info about exact action
     */
    static final enum InvoiceSentStatus {
        NOT_SENT, EMAIL, PRINT, SENT
    }


    static hasMany = [rows : InvoiceRow, invoicePayments : InvoicePayment]

    static belongsTo = [customer: Customer]

    String text
    LocalDate invoiceDate
    LocalDate expirationDate
    LocalDate paidDate

    Long number

    DateTime dateCreated
    DateTime lastUpdated

    InvoiceStatus status   = InvoiceStatus.READY
    InvoiceSentStatus sent = InvoiceSentStatus.NOT_SENT

    Date lastSent

    Organization organization

    BigDecimal getTotalIncludingVAT() {
        return rows.sum(0) { it.price == null ? 0 : it.getTotalIncludingVAT() }
    }

    BigDecimal getTotalExcludingVAT() {
        return rows.sum(0) { it.getTotalExcludingVAT() }
    }

    BigDecimal getTotalIncludingVATRounded() {
        return getTotalIncludingVAT().setScale(0, RoundingMode.HALF_UP);
    }

    BigDecimal getRoundedAmount() {
        return getTotalIncludingVATRounded().minus(getTotalIncludingVAT())
    }

    boolean isRounded() {
        return getRoundedAmount() != 0
    }

    BigDecimal getTotalVAT() {
        return rows.sum(0) { it.getTotalVAT() }
    }

    int getExpirationDays() {
        Days.daysBetween(invoiceDate.toDateMidnight(), expirationDate.toDateMidnight() ).getDays()
    }

    def isEditable() {
        return (Invoice.InvoiceStatus.READY.equals(this.status) || Invoice.InvoiceStatus.INCORRECT.equals(this.status))
    }

    def getOCR() {
        return numberToOCR(number)
    }

    def getTotalAmountPayments() {
        def totalAmountPaid = new BigDecimal(0)

        invoicePayments.each {
            totalAmountPaid = totalAmountPaid.plus(it.amount)
        }

        totalAmountPaid
    }

    def getTotalAmountPaymentRemaining() {
        getTotalIncludingVATRounded().minus(getTotalAmountPayments())
    }

    def addPayment(LocalDate paymentDate, BigDecimal amount) {

        log.info("Registering payment on invoice ${id} with date ${paymentDate} and amount ${amount}")

        def payment = new InvoicePayment(paymentDate: paymentDate, amount: amount, invoice: this)

        if(payment.save()) {
            addToInvoicePayments(payment)
            verifyPaymentStatus()
            return payment
        } else {
            log.error("Could not save payment ${payment}")
            return null
        }

    }

    def removePayment(InvoicePayment payment) {
        removeFromInvoicePayments(payment)
        verifyPaymentStatus()
    }

    private def verifyPaymentStatus() {
        def totalPaymentAmount = invoicePayments.sum(0) { it.amount }

        if((status.equals(InvoiceStatus.POSTED)
                || status.equals(InvoiceStatus.READY)
                || status.equals(InvoiceStatus.OVERDUE))
                && getTotalIncludingVATRounded() <= totalPaymentAmount) {
            status = InvoiceStatus.PAID
            paidDate = invoicePayments.collect { it.paymentDate }.max()
        }

        if(status.equals(InvoiceStatus.PAID)
                && getTotalIncludingVATRounded() > totalPaymentAmount) {
            status = InvoiceStatus.POSTED
            paidDate = null
        }

    }

    static constraints = {
        text(nullable: true, maxSize: 1000)
        status(nullable: false)
        sent(nullable: false)
        paidDate(nullable: true)
        expirationDate(nullable: true)
        lastSent(nullable: true)
        organization(nullable: true)
        number(nullable: true)
    }

    static mapping = {
        autoTimestamp true
        rows sort:'description', order: 'asc'
    }

    static namedQueries = {
        findByOCR { ocr ->
            eq("number", Invoice.ocrToNumber(ocr))
            uniqueResult = true
        }

        findAllByOCRs { ocrs ->
            inList("number", Invoice.ocrsToNumbers(ocrs))
        }


    }

    static def nextInvoiceNumber(def facility) {
        def maxInvoiceNumber = Invoice.createCriteria().get() {
            createAlias("customer", "c", CriteriaSpecification.LEFT_JOIN)
            eq("c.facility", facility)
            projections {
                max("number")
            }

        }

        if (!maxInvoiceNumber) {
            maxInvoiceNumber = 1;
        } else {
            maxInvoiceNumber = maxInvoiceNumber + 1
        }

        def facilityInvoiceNumberStart = Long.parseLong(facility
                .getFacilityPropertyValue(FacilityProperty.FacilityPropertyKey.INVOICE_NUMBER_START.toString()))

        return Math.max(maxInvoiceNumber, facilityInvoiceNumberStart)
    }

    static def ocrToNumber(def ocr) {
        def ocrValue = String.valueOf(ocr)

        def invoiceNumber = -1l
        try {
            invoiceNumber = Long.parseLong(ocrValue.substring(0, ocrValue.length()-2))
        } catch (NumberFormatException e) {
            // could not parse
        }
        return invoiceNumber;
    }

    static def numberToOCR(def number) {
        if(number == null) return null

        def invoiceNumber = String.valueOf(number)

        def length = String.valueOf(invoiceNumber.length()+2)
        length = length.substring(length.size()-1,length.size())

        // add length of invoice number, check digits length
        invoiceNumber += length

        return "${invoiceNumber}${LuhnValidator.generate(invoiceNumber)}"
    }

    static def numbersToOCRs(def ids) {
        return ids.collect { numberToOCR(it) }
    }

    static def ocrsToNumbers(def ocrs) {
        return ocrs.collect { ocrToNumber(it) }
    }

    def afterUpdate() {
        def applicationContext = Holders.applicationContext
        def memberService = applicationContext.getBean("memberService")

        if (status in [InvoiceStatus.CANCELLED, InvoiceStatus.CREDITED,
                InvoiceStatus.PAID, InvoiceStatus.POSTED]) {
            Invoice.withNewSession {
                memberService?.handleMembershipInvoicePayment(this)
            }
        }
    }
}
