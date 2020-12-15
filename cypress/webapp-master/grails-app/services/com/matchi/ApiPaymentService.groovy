package com.matchi

import com.matchi.payment.PaymentException
import com.matchi.protocol.ApiPaymentRequest
import com.matchi.protocol.ApiPaymentRequestException
import com.matchi.protocol.ApiRequestError
import com.matchi.protocol.ApiRequestedPayment
import grails.transaction.Transactional
import org.springframework.util.StopWatch
/* Created: 2012-11-28 Mattias (mattias@tdag.se) */
class ApiPaymentService {

    static transactional = false
    def paymentService
    def bookingService
    def boxnetManager

    @Transactional
    def processApiPaymentRequest(ApiPaymentRequest apiPaymentRequest) {
        log.debug("Processing boxnet payment request")
        log.debug("Payments for orders ${apiPaymentRequest.orderIds.split(":")}")
        def apiPayment = new ApiRequestedPayment()
        def watch = new StopWatch("Payments for orders ${apiPaymentRequest.orderIds}")
        watch.start()

        def orderAndPricesPaid = retrievePaymentOrdersAndPrices(apiPaymentRequest)

        if( !boxnetManager.validateResponse(apiPaymentRequest) ) {
            log.debug("VERIFICATION_ERROR")
            throw new ApiPaymentRequestException(ApiRequestError.Code.VERIFICATION_ERROR)
        } else if( orderAndPricesPaid.size() < 1 ) {
            log.debug("PAYMENT_ORDER_NOT_FOUND")
            throw new ApiPaymentRequestException(ApiRequestError.Code.PAYMENT_ORDER_NOT_FOUND)
        } else if( !apiPaymentRequest.confirmed ) {
            log.debug("CONFIRMATION_ERROR")
            throw new ApiPaymentRequestException(ApiRequestError.Code.CONFIRMATION_ERROR)
        } else if( !apiPaymentRequest.cashRegisterTransactionId ) {
            log.debug("NO_TRANSACTION_ID")
            throw new ApiPaymentRequestException(ApiRequestError.Code.NO_TRANSACTION_ID)
        }

        try {
            makePaymentTransactions(apiPaymentRequest, orderAndPricesPaid)
            log.debug("Verification of request successful, register payment as successful")
        } catch (PaymentException e) {
            log.error("GENERIC_ERROR")
            throw new ApiPaymentRequestException(ApiRequestError.Code.GENERIC_ERROR)
        }

        watch.stop();
        apiPayment.executionTime = watch.lastTaskTimeMillis

        return apiPayment
    }

    @Transactional
    def makePaymentTransactions(ApiPaymentRequest apiPaymentRequest, def orderAndPricesPaid) {

        orderAndPricesPaid.each {
            PaymentOrder order = it.order
            String pricePaid = it.pricePaid
            Booking booking
            Payment payment
            if ( order.orderParameters.containsKey("slotId") ) {
                Slot slot = Slot.findById(order.orderParameters.get("slotId").toString())
                booking = slot.booking
            }
            if (!booking?.payment) {
                payment = paymentService.registerPayment(order)
                booking.payment = payment
                booking.save(failOnError: true)
            } else {
                payment = booking.payment
            }

            paymentService.registerPaymentTransaction(payment, pricePaid, apiPaymentRequest.cashRegisterTransactionId)
        }

        return apiPaymentRequest
    }

    def retrievePaymentOrdersAndPrices(def apiPaymentRequest) {
        def orderPrices = []
        def orderIds = apiPaymentRequest.orderIds.split(":")
        def paidAmounts = apiPaymentRequest.prices.split(":")

        def i = 0
        orderIds.each { String s ->
            PaymentOrder order = PaymentOrder.findById(s)
            def pricePaid = paidAmounts[i]
            if ( order && pricePaid != null) {
                orderPrices << [order:order, pricePaid:pricePaid]
            }
            i++
        }

        return orderPrices
    }
}
