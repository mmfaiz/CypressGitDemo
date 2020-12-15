package com.matchi
import com.matchi.enums.BookingGroupType
import com.matchi.mpc.CodeRequest
import com.matchi.slots.SlotDelta
import org.joda.time.DateTime

class BookingAvailabilityService {

    static transactional = true
    def dateUtil
    def slotService
    def mpcService
    def courtRelationsBookingService

    def createSubscriptionBookingGroup(Subscription subscription, DateTime fromDate, DateTime toDate, Slot slot,
                           List<String> weekDays, int interval, Customer customer, boolean showComment, def comment) {

        if( toDate.isBefore(fromDate) ) {
            throw new IllegalArgumentException("To can not be before from!")
        }

        BookingGroup group = new BookingGroup()
        group.type = BookingGroupType.SUBSCRIPTION

        def slots = slotService.getRecurrenceSlots(fromDate, toDate, weekDays, 1, interval, [slot], true)

        addBookingGroupBookings(subscription, group, slots.freeSlots, customer, showComment, comment, true)

        return group
    }

    def updateSubscriptionBookingGroup(Subscription subscription, DateTime fromDate, DateTime toDate, Slot slot,
                                       int interval, Customer customer, def showComment, def comment) {

        def weekDays = [new DateTime(slot.startTime).dayOfWeek.toString()]
        def slots = slotService.getRecurrenceSlots(fromDate, toDate, weekDays, 1, interval, [slot], false)
        def intervalSlots = slots.freeSlots + slots.unavailableSlots

        if(intervalSlots.size() > 0) {
            def toBeAdded   = new SlotDelta(intervalSlots, subscription.slots).leftOnly().collect { it.slot }
            def toBeUpdated = []
            def toBeRemoved = []

            subscription.slots.each { Slot s ->
                if(intervalSlots.contains(s)) {
                    toBeUpdated << s
                } else {
                    toBeRemoved << s
                }
            }
            addBookingGroupBookings(subscription, subscription.bookingGroup, toBeAdded, customer, showComment, comment)
            updateBookingGroupBookings(subscription, subscription.bookingGroup, toBeUpdated, customer, showComment, comment, false)
            removeBookingGroupBookings(subscription, subscription.bookingGroup, toBeRemoved)
        } else {
            return null
        }

        return subscription.bookingGroup
    }

    def addBookingGroupBookings(def subscription, def group, def slots, def customer, def showComment, def comment, boolean queue = false, def playerCustomers = null) {
        log.debug("Adding ${slots.size()} bookings to group")

        if(slots?.size() > 0) {
            slots.each { Slot slot ->
                if(!slot.booking) {
                    addBookingToGroup(subscription, group, slot, customer, showComment, comment, queue, playerCustomers)
                }
            }
        }
    }

    private def updateBookingGroupBookings(def subscription, def group, def slots, def customer, def showComment, def comment, boolean queue = false) {
        log.debug("Updating ${slots.size()} bookings in group")

        if(slots?.size() > 0) {
            slots.each { Slot slot ->
                if( slot.subscription == subscription &&
                        (slot.booking?.customer == group.subscription.customer)) {
                    updateBookingInGroup(subscription, group, slot, customer, showComment, comment, queue)
                }
            }
        }
    }

    private def removeBookingGroupBookings(Subscription subscription, BookingGroup group, List<Slot> slots) {
        log.debug("Removing ${slots.size()} bookings from group")

        def tmp = []
        tmp.addAll slots

        tmp.each { Slot slot ->
            removeBookingFromGroup(subscription, group, slot)
        }
    }

    private def addBookingToGroup(def subscription, def group, def slot, def customer, def showComment, def comment, boolean queue = false, def playerCustomers = null) {
        log.debug("Adding booking to group and slot to subscription")

        Booking booking = new Booking()
        booking.customer = customer
        booking.telephone = customer.telephone
        booking.slot = slot
        booking.showComment = showComment
        booking.comments = comment

        if (playerCustomers) {
            booking.addPlayers(playerCustomers)
        }

        booking.save(failOnError: true)
        slot.booking = booking
        slot.save(failOnError: true)

        // Extra for creation of bookingNumber when making subscription as well
        booking.bookingNumber = "M-" + booking.id
        booking.save(failOnError: true)

        group.addToBookings(booking)
        group.save(failOnError: true)

        if (customer?.facility?.hasMPC()) {
            if(queue) {
                mpcService.queue(booking)
            } else {
                mpcService.add(booking)
            }
        }

        subscription.addToSlots(slot)
    }

    private def updateBookingInGroup(Subscription subscription, def group, Slot slot, def customer, def showComment, def comment, boolean queue = false) {
        log.debug("Updating booking in group")

        if(slot.booking) {
            slot.booking.showComment = showComment
            slot.booking.comments = comment
            slot.booking.save(failOnError: true)
        } else {
            addBookingToGroup(subscription, group, slot, customer, showComment, comment, queue)
        }
    }

    void removeBookingFromGroup(Subscription subscription, BookingGroup group, Slot slot) {
        log.debug("Removing booking from group and slot from subscription")

        subscription.removeFromSlots(slot)
        subscription.save(failOnError: true)

        if( slot.booking != null ) {
            group.removeFromBookings(slot.booking)
            group.save(failOnError: true)

            if (slot.booking.customer == subscription.customer) {
                def booking = slot.booking
                slot.booking = null

                mpcService.tryDelete(booking)
                courtRelationsBookingService.tryCancelRelatedCourts(booking)
                booking.customer.removeFromBookings(booking)
                booking.delete(flush: true)
            }
        }
    }
}
