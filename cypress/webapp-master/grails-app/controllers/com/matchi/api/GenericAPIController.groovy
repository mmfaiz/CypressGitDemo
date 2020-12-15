package com.matchi.api

import com.matchi.Facility
import com.matchi.FacilityProperty
import com.matchi.Payment
import com.matchi.PaymentInfo
import com.matchi.Slot
import com.matchi.User
import com.matchi.coupon.GiftCard
import com.matchi.payment.PaymentMethod
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONArray
import org.springframework.validation.Errors

class GenericAPIController {

    def facilityService
    def springSecurityService
    def couponService
    def paymentService

    void error(int status, Code code, String message) {
        def error = [status: status, code: code?.toString(), message: message]
        response.setStatus(status)

        render error as JSON
    }

    protected User getCurrentUser() {
        return springSecurityService.getCurrentUser()
    }

    protected void renderValidationErrors(Errors errors) {
        response.setStatus(422)

        def respErrors = errors.fieldErrors.collect {
            [message: message(error: it), path: "/$it.field"]
        }

        render([status: 422, code: Code.INPUT_ERROR.toString(),
                message: "Validation failed", errors: respErrors] as JSON)
    }

    protected void renderValidationErrors(List<Errors> errors) {
        response.setStatus(422)

        def respErrors = []
        errors.eachWithIndex { err, idx ->
            err.fieldErrors.each {
                respErrors << [message: message(error: it), path: "/$idx/$it.field"]
            }
        }

        render([status: 422, code: Code.INPUT_ERROR.toString(),
                message: "Validation failed", errors: respErrors] as JSON)
    }

    protected Facility getRequestFacility() {
        facilityService.getFacility(request.facilityId)
    }

    protected JSONArray getRequestJSONArray() {
        if (!(request.JSON instanceof JSONArray)) {
            throw new APIException(400, Code.INPUT_ERROR, "Invalid request")
        }
        request.JSON
    }

    protected Map getPaymentMethods(User user, List<Slot> slots, Long totalPrice) {
        Map paymentMethods = [:]
        Facility facility = slots.first().court?.facility

        // valid coupons
        def validCoupons = couponService.getValidCouponsByUserAndSlots(getCurrentUser(), slots, totalPrice).collect {
            [id: it.id, name: it.coupon.name, remaining: it.nrOfTickets]
        }
        // valid giftCards
        // def validGiftCards = customer ? couponService.getValidCouponsByCustomerAndSlots(customer, slots, totalPrice, GiftCard.class) : []
        def validGiftCards = couponService.getValidCouponsByUserAndSlots(getCurrentUser(), slots, totalPrice, GiftCard.class).collect {
            [id: it.id, name: it.coupon.name, remaining: it.nrOfTickets]
        }

        // free courts
        if(totalPrice == 0l) {
            paymentMethods.put(PaymentMethod.FREE, true)
        }

        def recurringPaymentInfo = getRecurringPaymentInfo()
        if(recurringPaymentInfo) {
            paymentMethods.put(PaymentMethod.CREDIT_CARD_RECUR, recurringPaymentInfo)
        }

        if(!validCoupons.isEmpty()) {
            paymentMethods.put(PaymentMethod.COUPON, validCoupons)
        }

        if(!validGiftCards.isEmpty()) {
            paymentMethods.put(PaymentMethod.GIFT_CARD, validGiftCards)
        }

        if (facility.isFacilityPropertyEnabled(FacilityProperty.FacilityPropertyKey.FEATURE_PROMO_CODES.name()) &&
                !couponService.getActivePromoCodes(facility).isEmpty() && recurringPaymentInfo) {
            paymentMethods.put("PROMO_CODE", true)
        }

        return paymentMethods
    }

    private def getRecurringPaymentInfo() {
        def paymentInfo = PaymentInfo.findByUser(getCurrentUser())

        if(paymentInfo) {
            Payment payment

            if(paymentInfo.transactionId) {
                payment = Payment.findByTransactionId(paymentInfo.transactionId)
            }

            if(payment) {
                return ["cardNumber":payment.cardNumber, "cardType": payment.cardType]
            } else {
                return ["cardNumber":paymentInfo.maskedPan, "cardType": paymentInfo.issuer]
            }
        }

        return null
    }

    protected def pay(def orders, def paymentCommand) {

        log.info("Handling payments for ${orders.size()} booking orders")

        orders.each { order ->
            switch(paymentCommand.getPaymentMethod()) {
                case PaymentMethod.COUPON:
                case PaymentMethod.GIFT_CARD:
                    paymentService.handleCouponPayment(order, paymentCommand.couponId as Long, paymentCommand.getPaymentMethod(), getCurrentUser())
                    break;
                case PaymentMethod.CREDIT_CARD_RECUR:
                    paymentService.handleCreditCardPayment(order, getCurrentUser())
                    break;
            }

            if(!order.isFinalPaid()) {
                throw new Exception("Could not process payment on order ${order.id}")
            }
        }
    }
}
