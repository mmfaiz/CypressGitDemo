package com.matchi.schedule

import com.matchi.ColorFetcher
import com.matchi.Court
import com.matchi.payment.PaymentStatus
import org.joda.time.DateTime
import org.joda.time.Interval

class Schedule {
	
	public static enum Status {
		NOT_AVAILABLE, PARTLY_BOOKED, FULL, FREE, OWN_BOOKING, OWN_UNPAYED, PAST
	}

    def facility
    def user

    def startDate
    def endDate

    def items = []
    def itemsPerCourtId = [:]
    def itemsFree = []
    def itemsBooked = []
    def itemsSelfBooked = []

    Schedule(startDate, endDate, facility, slotItems, user) {
        this.startDate = startDate
        this.endDate   = endDate
        this.facility  = facility
        this.user = user
        addItems(slotItems)
    }

    def getSlots(TimeSpan timeSpan, Court court = null) {
        def slots = (court ? itemsPerCourtId.get(court.id) : items)
        def result = []

        if(slots) {
            slots.each {
                if(timeSpan.toInterval().contains(new DateTime(it.start))) {
                    result << it
                }
            }
        }

        return result
    }

    def firstSlot() {
        items.min { it.start.millis }
    }

    def lastSlot() {
        items.max { it.end.millis }
    }

    def getAllSlots() {
        return items;
    }

    def isEmpty() {
        return items.isEmpty()
    }
    
    def getSlot(TimeSpan timeSpan, Court court) {

        def slots = itemsPerCourtId.get(court.id)

        if(!slots || slots.isEmpty()) {
            return null
        }
        def mc = [
                compare: { a, b ->
                    if(a.timeSpan.overlap(b.toInterval())) {
                        return 0
                    };

                    return a.start.compareTo(b.start)
                }
        ] as Comparator

        def slotIndex = Collections.binarySearch(slots, timeSpan, mc)

        if(slotIndex > -1) {
            return slots.get(slotIndex)
        }

        return null
    }

    def getSlots(Interval interval) {
        getSlots(interval, items)
    }

    def getFreeSlots(Interval interval) {
        getSlots(interval, itemsFree)
    }

    def getSlotsWhereOwnBookings(Interval interval) {
        getSlots(interval, itemsSelfBooked)
    }

    def getSlots(Interval interval, def slotLists) {
        return slotLists.findAll { interval.contains(it.start) }
    }

    def color(Interval interval, int bookingLimit) {
        ColorFetcher.color(status(interval, bookingLimit))
    }

    def color(Map slot, int bookingLimit) {
        ColorFetcher.color(status(slot, bookingLimit))
    }

    def status(Interval interval, int bookingLimit) {

        // not able to book in past
        if(interval.end.isBefore(new DateTime())) {
            return [Status.PAST]
        }

        if (!facility.isBookableForLimit(interval.start, bookingLimit) || isEmpty()) {
            return [Status.NOT_AVAILABLE]
        }

        def result = []


        // look for free slots
        if(getFreeSlots(interval).isEmpty()) {
            result << Status.FULL
        } else {
            result << Status.FREE
        }

        // Slots are actually ScheduleItems check structure in ScheduleService:createScheduleItem where paidStatus is assigned.
        ArrayList slots = getSlotsWhereOwnBookings(interval)

        if(!slots.isEmpty()) {
            result << Status.OWN_BOOKING
            if(slots.any{ it?.booking?.paidStatus != PaymentStatus.OK }) {
                result << Status.OWN_UNPAYED
            }
        }

        return result
    }

    def status(Map slot, int bookingLimit) {
        // not able to book in past
        if(new DateTime(slot.end).isBefore(new DateTime())) {
            return [Status.PAST]
        }

        if (!facility.isBookableForLimit(new DateTime(slot.start), bookingLimit) || isEmpty()) {
            return [Status.NOT_AVAILABLE]
        }

        def result = []

        if (itemsFree.find {it.id == slot.id}) {
            result << Status.FREE
        } else {
            result << Status.FULL
        }

        if (slot.booking?.owned) {
            result << Status.OWN_BOOKING

            if (slot?.booking?.paidStatus == PaymentStatus.PENDING) {

                result << Status.OWN_UNPAYED
            }
        }

        return result
    }

    def status(TimeSpan timeSpan, int bookingLimit) {
        return status(timeSpan.toInterval(), bookingLimit)
    }

    public void addItems(def items) {
        this.items = items
        this.itemsPerCourtId = items.groupBy { it.court.id }
        this.itemsFree = items.findAll { !it.booking }
        this.itemsBooked = items.findAll { it.booking }
        this.itemsSelfBooked = this.itemsBooked.findAll { it?.booking?.owned }
    }
}
