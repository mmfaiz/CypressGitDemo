package com.matchi
import com.matchi.enums.BookingGroupType
import com.matchi.payment.PaymentStatus
import com.matchi.schedule.Schedule

class ColorFetcher {
    static def blue       = "blue"
    static def lightBlue  = "lightBlue"
    static def grey       = "grey"
    static def lightGrey  = "lightGrey"
    static def green      = "green"
    static def lightGreen = "lightGreen"
    static def red        = "red"
    static def yellow     = "yellow"
    static def purple     = "purple"

    static def getFacilityColor(def slot) {
        def isLeasedSubscription = (slot.booking && slot.subscription
                && !slot.booking.customer.equals(slot.subscription.customer))

        return slotColor(
                slot?.booking,
                slot?.booking?.group?.type,
                slot?.booking?.payment?.status,
                slot?.subscription,
                isLeasedSubscription,
                slot?.booking?.isFinalPaid())
    }

    static def slotColor(def hasBooking,
                         def bookingGroupType,
                         def paymentStatus,
                         def isSubscription,
                         def isLeasedSubscription, // if slot is subscription but is leased to another customer
                         def markedPaid) {

        // subscription
        if(isSubscription) {
            if(!isLeasedSubscription) {
                return (hasBooking ? blue : lightBlue)
            }
        }

        if(!hasBooking) {
            return grey
        }

        // group
        if(bookingGroupType) {
            switch (bookingGroupType) {
                case BookingGroupType.ACTIVITY:
                    return purple;
                case BookingGroupType.PRIVATE_LESSON:
                case BookingGroupType.DEFAULT:
                    return getSlotPaymentStatusColor(paymentStatus, markedPaid)
                case BookingGroupType.TRAINING:
                    return red;
                case BookingGroupType.COMPETITION:
                    return red;
                default:
                    return lightGrey;
            }
        }

        // payment
        return getSlotPaymentStatusColor(paymentStatus, markedPaid)
    }

    static def getSlotPaymentStatusColor(def paymentStatus, def markedPaid) {
        if(PaymentStatus.OK.equals(paymentStatus)) {
            return green
        }

        if(PaymentStatus.PARTLY.equals(paymentStatus)) {
            return lightGreen
        }

        return markedPaid ? green : yellow
    }

    static def color(List statuses) {
        if(statuses.contains(Schedule.Status.PAST)) {
            return "grey"
        }

        if(statuses.contains(Schedule.Status.NOT_AVAILABLE)) {
            return "grey"
        }

        if(statuses.contains(Schedule.Status.OWN_UNPAYED)) {
            return "yellow"
        }

        if(statuses.contains(Schedule.Status.OWN_BOOKING)) {
            return "green"
        }

        if(statuses.contains(Schedule.Status.FULL)) {
            return "red"
        }

        if(statuses.contains(Schedule.Status.FREE)) {
            return ""
        }
    }
}
