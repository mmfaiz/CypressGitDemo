package com.matchi

import com.matchi.enums.BookingGroupType
import com.matchi.membership.MembershipType
import grails.validation.Validateable
import org.joda.time.LocalDate

@Validateable(nullable = true)
class FacilityBookingCommand {
    Long customerId
    Long userId

    String email
    String firstname
    String lastname
    String comments
    String telephone
    String slotId
    Boolean paid
    Boolean useCoupon = false
    Boolean useGiftCard = false
    Boolean useInvoice = false
    Boolean activateMpc = false
    Boolean showComment
    Boolean useRecurrence
    Boolean updateGroup
    BookingGroupType type

    Long customerCouponId
    Long trainerId

    // Recurrence
    String recurrenceStart
    String recurrenceEnd
    List<Integer> weekDays
    int frequency
    int interval

    Boolean newMember
    Boolean newCustomer = false
    Boolean sendNotification
    MembershipType memberType
    LocalDate startDate
    LocalDate endDate
    LocalDate gracePeriodEndDate
    Integer startingGracePeriodDays
    Boolean membershipPaid
    Boolean membershipCancel

    static constraints = {
        customerId(nullable: true, blank: true, validator: { customerId, obj ->
            if(obj.properties['newCustomer'] || customerId) {
                return true
            }

            return ["com.matchi.FacilityBookingCommand.customerId.nullable.error"]
        })
        userId(nullable: true, blank: true)
        email(nullable:true, blank: true, email: true)
        comments(nullable:true, maxSize: 1000)
        slotId(nullable: false)
        trainerId(nullable: true)
        useRecurrence(nullable: true)
        updateGroup(nullable: true)
        recurrenceStart(nullable: true)
        recurrenceEnd(nullable: true)
        weekDays(nullable: true)
        frequency(nullable: true)
        interval(nullable: true)
        newMember(nullable: true)
        newCustomer(nullable: true)
        sendNotification(nullable: true)

        memberType(nullable: true, validator: { val, obj -> val || !obj.newMember})
        startDate(nullable: true, validator: { val, obj -> val || !obj.newMember})
        endDate(nullable: true, validator: { val, obj ->
            (val && val >= new LocalDate() && val >= obj.startDate) || !obj.newMember
        })
        gracePeriodEndDate(nullable: true, validator: { val, obj ->
            (val && val >= obj.endDate) || !obj.newMember
        })
        startingGracePeriodDays(nullable: true, min: 1)
        membershipPaid(nullable: true)
        membershipCancel(nullable: true)
    }

    String getCustomerInfo() {
        return email?: firstname + ' ' + lastname
    }

    String toString() {
        return "[slotId: ${slotId}, customerId: ${customerId}, email: ${email}, comment: ${comments}, showComment: ${showComment}, type: ${type}, telephone: ${telephone}, useRecurrence: ${useRecurrence}, recurrenceStart: ${recurrenceStart}, recurrenceEnd: ${recurrenceEnd}, weekDays: ${weekDays}, frequency: ${frequency}, interval: ${interval}, customerCouponId: ${customerCouponId}, paid: ${paid}, useInvoice: ${useInvoice}, sendNotification: ${sendNotification}, updateGroup: ${updateGroup}, activateMpc: ${activateMpc}]"
    }

    def slotIds() {
        return Slot.parseAll(slotId)
    }
}

