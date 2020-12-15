package com.matchi.schedule

import com.matchi.Court
import org.joda.time.DateTime
import org.joda.time.Interval

class PerformanceSchedule extends Schedule {

    def items = []
    def itemsPerCourtId = [:]
    def itemsFree = []
    def itemsBooked = []
    def itemsSelfBooked = []

    PerformanceSchedule(startDate, endDate, facility, slotItems, user) {
        super(startDate, endDate, facility)
        this.user = user
        addItems(slotItems)
    }

    def getSlots(TimeSpan timeSpan, Court court = null) {
        def slots = (court ? itemsPerCourtId.get(court.id) : slots)
        def result = []

        if(slots) {
            slots.each {
                if(timeSpan.toInterval().overlap(it.interval)) {
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

    @Override
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
        getSlot(interval, items)
    }

    def getFreeSlots(Interval interval) {
        getSlots(interval, itemsFree)
    }

    def getSlotsWhereOwnBookings(Interval interval) {
        getSlots(interval, itemsSelfBooked)
    }

    def getSlots(Interval interval, def list) {
        return list.findAll { interval.contains(it.start) }
    }

    def color(def statuses) {
        if(statuses.contains(Schedule.Status.PAST)) {
            return "grey"
        }

        if(statuses.contains(Schedule.Status.NOT_AVAILABLE)) {
            return "grey"
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

    def color(Interval interval, int bookingLimit) {
        color(status(interval, bookingLimit))
    }

    def status(Interval interval, int bookingLimit) {

        // not able to book in past
        if(interval.end.isBefore(new DateTime())) {
            return [Schedule.Status.PAST]
        }

        if (!facility.isBookableForLimit(interval.start, bookingLimit) || isEmpty()) {
            return [Status.NOT_AVAILABLE]
        }

        def result = []

        // look for free slots
        if(getFreeSlots(interval).isEmpty()) {
            result << Schedule.Status.FULL
        } else {
            result << Schedule.Status.FREE
        }

        if(!getSlotsWhereOwnBookings(interval).isEmpty()) {
            result << Schedule.Status.OWN_BOOKING
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
