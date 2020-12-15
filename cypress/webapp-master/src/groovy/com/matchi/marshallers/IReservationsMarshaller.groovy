package com.matchi.marshallers

import com.matchi.*
import com.matchi.activities.Participation
import com.matchi.mpc.CodeRequest
import com.matchi.payment.ArticleType
import grails.util.Holders
import org.joda.time.LocalDateTime
import org.joda.time.format.DateTimeFormatter
import org.joda.time.format.ISODateTimeFormat

class IReservationsMarshaller {

    static PaymentService paymentService
    static SlotService slotService
    static ActivityService activityService
    static FacilityService facilityService

    private static DateTimeFormatter formatter = ISODateTimeFormat.dateTime()

    static register(def json) {
        // Standard Node marshall
        json.registerObjectMarshaller(IReservation) { IReservation reservation ->
            slotService = Holders.applicationContext.getBean("slotService")
            paymentService = Holders.applicationContext.getBean("paymentService")
            facilityService = Holders.applicationContext.getBean("facilityService")
            marshallIReservation(reservation)
        }
    }

    static marshallIReservation(IReservation reservation) {

        Facility facility = null
        Participation participation = null
        Booking booking = null

        if (reservation.articleType == ArticleType.ACTIVITY) {
            participation = (Participation)reservation
            facility = participation.occasion.activity.facility
        }
        else if (reservation.articleType == ArticleType.BOOKING) {
            booking = (Booking)reservation
            facility = booking.slot.court.facility
        }

        def serviceFee = paymentService.getServiceFee(facility.currency)
        def bookingFeeAmount = serviceFee.getAmount()
        def bookingFeeVAT = serviceFee.getVAT()

        def result = [
            id: reservation.id,
            type: reservation.articleType.toString(),
            date: new LocalDateTime(reservation.date),
            bookingCancellationLimit: facility?.getBookingCancellationLimit(),
            bookingFee: [amount: bookingFeeAmount, VAT: bookingFeeVAT],
            order: reservation.order,
            payment: reservation.payment,
            facility: reservation.facility,
        ]

        if (reservation.articleType == ArticleType.ACTIVITY) {
//            ClassActivity.JSONoptions = new GetActivitiesCommand(exposeOccasions: true)
            result += [
                activity: participation.occasion.activity,
                occasion: participation.occasion,
                refundPolicy: [
                    code: "cancellation.rule.with.service.fee",
                    args: [activityService.getServiceFeeWithCurrency(participation.occasion)]
                ],
                refundableUntil: participation.occasion.refundableUntil,
                isRefundable: participation.isRefundable()
            ]
        }
        else if (reservation.articleType == ArticleType.BOOKING) {
            def bookingAccessCode = booking.getAccessCode()


            result += [
                slot: booking.slot,
                refundableUntil: formatter.print(booking.slot.isRefundableUntil()),
                refundPolicy: slotService.getSlotRefundPolicy(booking.slot),
                accessCode: bookingAccessCode,
            ]
        }

        return result
    }
}
