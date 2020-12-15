package com.matchi

import com.matchi.enums.BookingGroupType
import com.matchi.enums.RedeemType
import com.matchi.enums.RefundOption
import com.matchi.membership.MembershipType
import grails.validation.Validateable
import org.joda.time.LocalDate

class GenericBookingController extends GenericController {

    /**
     * Generates BookingSlot
     */
    protected def getAvailableSlots(def date, def startHour, def endHour) {
        def slots = []

        Calendar startTime = Calendar.getInstance()
        startTime.setTime(date)

        startTime.set(Calendar.MINUTE, 0)
        startTime.set(Calendar.SECOND, 0)
        startTime.set(Calendar.MILLISECOND, 0)

        Calendar endTime = Calendar.getInstance()
        endTime.setTime(startTime.getTime())

        startTime.set(Calendar.HOUR_OF_DAY, startHour)
        endTime.set(Calendar.HOUR_OF_DAY, endHour)

        while (startTime.getTime().before(endTime.getTime())) {

            def slot = new BookingSlot()
            slot.startTime = startTime.getTime()

            def slotEndTime = Calendar.getInstance()
            slotEndTime.setTime(startTime.getTime())
            slotEndTime.set(Calendar.HOUR_OF_DAY, startTime.get(Calendar.HOUR_OF_DAY) + 1)
            slot.endTime = slotEndTime.getTime()

            slots << slot

            startTime.roll(Calendar.HOUR_OF_DAY, true)
        }

        return slots
    }

    static class BookingSlot {
        def startTime
        def endTime
    }
}

@Validateable(nullable = true)
class CreateBookingCommand {
    Long customerId
    Long userId
    String comments
    String email
    String firstname
    String lastname
    String telephone
    Long start
    Long end
    Long customerCouponId
    Boolean paid
    Boolean useCoupon
    Boolean showComment
    Boolean newMember
    Boolean newUser
    Boolean sendInvite
    boolean online = false
    Boolean activateMpc = false
    String number
    String slotId
    Payment payment
    MembershipType memberType
    LocalDate startDate
    LocalDate endDate
    LocalDate gracePeriodEndDate
    Integer startingGracePeriodDays
    Boolean membershipPaid
    Boolean membershipCancel
    Boolean hideBookingHolder
    BookingGroupType bookingType
    Long trainerId

    static constraints = {
        email(email: true, nullable: false, blank: false)
        telephone(nullable: false, blank: false)
        payment(nullable: true)
        customerCouponId(nullable: true)
        number(nullable: true)
        newMember(nullable: true)
        newUser(nullable: true)
        sendInvite(nullable: true)
        hideBookingHolder(nullable: true)
        bookingType(nullable: true)
        trainerId(nullable: true)
        memberType(nullable: true, validator: { val, obj -> val || !obj.newMember })
        startDate(nullable: true, validator: { val, obj -> val || !obj.newMember })
        endDate(nullable: true, validator: { val, obj ->
            (val && val >= new LocalDate() && val >= obj.startDate) || !obj.newMember
        })
        gracePeriodEndDate(nullable: true, validator: { val, obj ->
            (val && val >= obj.endDate) || !obj.newMember
        })
        startingGracePeriodDays nullable: true, min: 1
        membershipPaid(nullable: true)
        membershipCancel(nullable: true)
    }
}

@Validateable(nullable = true)
class CancelBookingCommand {
    String cancelSlotsData
    String message
    Boolean removeRecurrence = false
    Boolean sendNotification
    RedeemType redeemType
    RefundOption refundOption
}
