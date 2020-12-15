package com.matchi.boxnet
import com.matchi.PaymentOrder
import com.matchi.Slot
import com.matchi.external.ExternalSynchronizationEntity
import com.matchi.protocol.ApiPaymentRequest
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.joda.time.DateTime
/* Created: 2012-11-27 Mattias (mattias@tdag.se) */
class BoxnetManager {
    private static final Log log = LogFactory.getLog(BoxnetManager)

    def grailsApplication
    def grailsLinkGenerator
    def externalSynchronizationService

    def validateResponse(ApiPaymentRequest boxnetPaymentRequest) {
        def hash =  createVerificationHash([boxnetPaymentRequest.orderIds?:"", boxnetPaymentRequest.prices?:"", boxnetPaymentRequest.confirmed?"true":"false",
                boxnetPaymentRequest.errorMessage?:"", boxnetPaymentRequest.paymentMethod, boxnetPaymentRequest.cashRegisterTransactionId])

        return hash.equals(boxnetPaymentRequest.hash)
    }

    def createRequest(def orders) {
        def createdParamsLists = createParamsList(orders)

        def orderIds = createdParamsLists.orderIds
        def prices = createdParamsLists.prices
        def paymentMeta = createdParamsLists.paymentMeta
        def customerIds = createdParamsLists.customerIds
        def responseUrl = boxnetResponseUrl()
        def hash = createVerificationHash([orderIds, prices, responseUrl])

        def linkParams = [ paymentId:orderIds, price:prices, customerId: customerIds, paymentMeta:paymentMeta, responseUrl:responseUrl, hash:hash ]

        return buildRequestUrl(linkParams)
    }

    def buildRequestUrl(linkParams) {
        StringBuilder sb = new StringBuilder()
        sb.append(boxnetRequestUrl())
        linkParams.each {
            def value = isoUrlEncode(it.value)
            sb.append("&${it.key}=${value}")
        }

        return sb.toString()
    }

    def createVerificationHash(def items) {
        StringBuilder sb = new StringBuilder()
        items.each {
            sb.append(it)
        }
        return sb.append(boxnetSecret()).toString().encodeAsMD5()
    }

    def createParamsList(def orders) {
        def orderIds = ""
        def prices = ""
        def customerIds = ""
        def paymentMeta = ""
        def i = 1
        orders.each { PaymentOrder order ->
            orderIds += order.id
            prices += order.price

            if ( order.orderParameters.containsKey("slotId") ) {
                Slot slot = Slot.findById(order.orderParameters.get("slotId").toString())
                def customerId = ExternalSynchronizationEntity.findByEntityIdAndEntity(slot.booking.customer.id, ExternalSynchronizationEntity.LocalEntity.CUSTOMER)?.externalEntityId
                customerIds += customerId ?: ""
            } else {
                customerIds += ""
            }
            paymentMeta += assemblePaymentMeta(order)

            if(i < orders.size()) {
                orderIds += ":"
                prices += ":"
                customerIds += ":"
                paymentMeta += ":"
            }

            i++
        }
        return [orderIds:orderIds, prices:prices, customerIds: customerIds, paymentMeta: paymentMeta]
    }

    def assemblePaymentMeta(PaymentOrder order) {
        def paymentMeta = ""
        paymentMeta += order.priceDescription ? order.priceDescription + "<br/>" : ""
        if ( order.orderParameters.containsKey("slotId") ) {
            Slot slot = Slot.findById(order.orderParameters.get("slotId").toString())
            paymentMeta += slot.court.name + " " + new DateTime(slot.startTime).toString("HH.mm") + "-" + new DateTime(slot.endTime).toString("HH.mm")
        }

        return paymentMeta
    }
    def isoUrlEncode(String url) {
        return URLEncoder.encode(url, "ISO-8859-1")
    }
    def boxnetResponseUrl() {
        return grailsLinkGenerator.link([controller: "apiPayment", action: "payment", absolute: "true"])
    }
    def boxnetRequestUrl() {
        return grailsApplication.config.boxnet.requestUrl
    }
    def boxnetSecret() {
        return grailsApplication.config.boxnet.secret
    }
    def boxnetWindowExtras() {
        return grailsApplication.config.boxnet.windowExtras
    }
    def boxnetWindowTitle() {
        return grailsApplication.config.boxnet.windowTitle
    }
}
