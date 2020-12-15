package com.matchi.marshallers

import com.matchi.Booking
import com.matchi.FacilityAccessCode
import com.matchi.mpc.CodeRequest
import com.matchi.payment.PaymentMethod
import grails.converters.JSON
import org.joda.time.format.DateTimeFormatter
import org.joda.time.format.ISODateTimeFormat

import javax.annotation.PostConstruct

class BookingMarshaller {

    def paymentService
    def slotService
    def grailsApplication

    private DateTimeFormatter formatter = ISODateTimeFormat.dateTime()

    @PostConstruct
    void register() {
        // Standard Node marshall
        JSON.registerObjectMarshaller(Booking) { Booking booking ->
            marshallBooking(booking)
        }
    }

    def marshallBooking(Booking booking) {
        def serviceFee = paymentService.getServiceFee(booking.slot.court.facility.currency)
        def bookingFeeAmount = serviceFee.getAmount()
        def bookingFeeVAT = serviceFee.getVAT()
        def bookingAccessCode

        if (booking.slot.court.facility.hasMPC()) {
            bookingAccessCode = CodeRequest.findByBooking(booking)?.code
        } else {
            bookingAccessCode = FacilityAccessCode.validAccessCodeContentFor(booking.slot)
        }

        def payment = null

        if(booking.payment) {
            payment = booking.payment
        } else if(booking.order) {
            if(booking.order.payments.size() > 0) {
                if(booking.order.isPaidByCreditCard()) {
                    payment = [
                            id: booking.order.id,
                            created: booking.order.dateCreated,
                            amount: booking.order.total(),
                            vat: booking.order.vat(),
                            status: "OK",
                            method: booking.order.isPaidByCreditCard()
                                    ?PaymentMethod.CREDIT_CARD_RECUR.toString():
                                    PaymentMethod.COUPON.toString()
                    ]
                }

            }
        }


        [
                id: booking.id,
                slot: booking.slot,
                refundableUntil: formatter.print(booking.slot.isRefundableUntil()),
                refundPolicy: slotService.getSlotRefundPolicy(booking.slot),
                bookingFee: [amount: bookingFeeAmount, VAT: bookingFeeVAT],
                accessCode: bookingAccessCode,
                payment: payment
        ]
    }
}