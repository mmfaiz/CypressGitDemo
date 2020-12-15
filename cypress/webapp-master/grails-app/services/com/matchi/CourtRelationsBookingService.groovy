package com.matchi

import com.matchi.enums.BookingGroupType
import grails.transaction.NotTransactional
import org.joda.time.Interval
import org.springframework.dao.DataIntegrityViolationException

class CourtRelationsBookingService {
    static transactional = true
    def slotService

    def tryBookRelatedCourts(Booking booking) {
        bookRelatedSlots(booking.slot)
    }

    def tryCancelRelatedCourts(Booking booking) {
        Booking.withNewSession {
            cancelRelatedSlots(booking.slot)
        }
    }

    def tryBookRelatedCourts(Slot slot) {
        bookRelatedSlots(slot)
    }

    def tryCancelRelatedCourts(Slot slot) {
        cancelRelatedSlots(slot)
    }

    def bookRelatedSlots(Slot slot) {
        def slots = findRelatedSlots(slot)

        if(!slots.isEmpty()) {
            log.debug("Found ${slots.size()} related slots, booking them...")

            slots.each {
                log.info("Booking court ${it.court.name} on ${it.startTime}")
                blockSlot(slot, it)
            }
        } else {
            log.debug("Found no related slots on court, skipping.")
        }
    }

    def cancelRelatedSlots(Slot slot) {
        def slots = findRelatedSlots(slot)

        if(!slots.isEmpty()) {
            log.debug("Found ${slots.size()} related slots, trying to cancel...")

            slots.each {
                unblockSlot(slot, it)
            }
        } else {
            log.debug("Found no related slots on court, skipping cancelling related")
        }
    }

    void blockSlot(Slot originalSlot, Slot slot) {
        def customer = slot.court.facility.relatedBookingsCustomer

        slot = slot.refresh()

        if(!slot.booking && customer) {
            Booking.withNewSession {
                Booking.withTransaction {
                    try {
                        Booking booking  = new Booking()
                        booking.customer = customer
                        booking.slot     = slot
                        booking.comments = "Blockerad"
                        booking.showComment = true
                        booking.save()

                        BookingGroup group = new BookingGroup()
                        group.type = BookingGroupType.BLOCKED
                        group.addToBookings(booking)
                        group.save(flush: true)
                    } catch (DataIntegrityViolationException e) {
                        // ignore it; it occurs when slot has been already blocked by another concurrent transaction
                    }
                }
            }
        }
    }

    def unblockSlot(Slot originalSlot, Slot slot) {

        def hasOtherBlockingBookings = false

        if(!slot.court.childs.isEmpty()) {
            // check to prevent that a parent slot is unblocked even if the slot
            // has more childs that are blocking the parent

            def childSlots = findRelatedChildSlots(slot)
            def otherBookings = childSlots.findAll { it.id != originalSlot.id && it.booking }

            hasOtherBlockingBookings = !otherBookings.isEmpty()
        } else if(slot.court.parent) {
            // check to prevent that a child that is blocking multiple parents
            // eg. at half hour times are not unblocked

            def parentSlots = findRelatedParentSlots(slot)
            def otherBookings = parentSlots.findAll { it.id != originalSlot.id && it.booking }

            hasOtherBlockingBookings = !otherBookings.isEmpty()
        }

        if(!hasOtherBlockingBookings &&
                slot.booking && slot.booking.group && slot.booking.group.isType(BookingGroupType.BLOCKED)) {

            // Remove group if empty
            def group = slot.booking.group
            group.removeFromBookings(slot.booking)
            if(group.bookings.isEmpty()) {
                group.delete()
            }

            def booking = slot.booking
            slot.booking = null

            booking.delete(flush: true)

        }

    }

    @NotTransactional
    def findRelatedSlots(Slot slot) {
        def court = slot.court

        if(court.parent) {
            return findRelatedParentSlots(slot)
        } else if(court.childs && !court.childs.isEmpty()) {
            return findRelatedChildSlots(slot)
        } else {
            return []
        }

    }

    @NotTransactional
    def findRelatedChildSlots(Slot slot) {
        def slots = []

        log.debug "Court has ${slot.court.childs.size()} childs..."

        slot.court.childs.each {
            slots.addAll findSlotsByCourtAndInterval(it, slot.toInterval())
        }

        return slots
    }

    @NotTransactional
    def findRelatedParentSlots(Slot slot) {
        return findSlotsByCourtAndInterval(slot.court.parent, slot.toInterval())
    }

    @NotTransactional
    def findSlotsByCourtAndInterval(Court court, Interval interval) {

        Slot.createCriteria().listDistinct {
            eq("court", court)
            lt("startTime", interval.end.toDate())
            gt("endTime", interval.start.toDate())
        }

    }
}
