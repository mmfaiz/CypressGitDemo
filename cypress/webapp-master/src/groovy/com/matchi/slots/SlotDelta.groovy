package com.matchi.slots

import com.matchi.Slot
import org.apache.commons.collections.CollectionUtils
import org.joda.time.DateTime
import org.joda.time.LocalDate

class SlotDelta {
    def left = []
    def right = []

    public SlotDelta(l, r) {

        l.each {
            this.left << new SlotItem(it)
        }

        r.each {
            this.right << new SlotItem(it)
        }

    }

    def leftOnly() {
        return (left - right)
    }

    def rightOnly() {
        return (right - left)
    }

    def rightOverlaps(def slots) {
        return overlaps(slots, right)
    }

    def leftOverlaps(def slots) {
        return overlaps(slots, left)
    }

    def intersection() {
        return (left.intersect(right))
    }

    def union() {
        return (left + right).unique()
    }

    private def overlaps(def slots, def base) {
        def overlaps = [:]

        def baseSlots = [:]
        base.each { SlotItem other ->
            if (!baseSlots.containsKey(other.slot.court.id)) {
                baseSlots[other.slot.court.id] = [:]
            }
            def date = new LocalDate(other.slot.startTime)
            if (!baseSlots[other.slot.court.id].containsKey(date)) {
                baseSlots[other.slot.court.id][date] = []
            }
            baseSlots[other.slot.court.id][date] << other
        }

        slots.each { SlotItem item ->
            baseSlots[item.slot.court.id]?.get(new LocalDate(item.slot.startTime)).each { SlotItem other ->
                if (other.slot.timeSpan.intersects(item.slot.timeSpan)) {
                    def slotsOverlaps = overlaps.get(item.slot)
                    if(!slotsOverlaps) {
                        slotsOverlaps = []
                    }
                    slotsOverlaps << other
                    overlaps.put(item.slot, slotsOverlaps)
                }
            }
        }

        return overlaps
    }

    class SlotItem implements Comparable<SlotItem> {
        Slot slot

        SlotItem(Slot slot) {
            this.slot = slot
        }

        @Override
        boolean equals(Object o) {
            return o.slot.equals(slot)
        }

        @Override
        int compareTo(SlotItem t) {
            return this.slot.compareTo(t.slot)
        }

        @Override
        String toString() {
            return "${slot.court?.id}-${new DateTime(slot.startTime)}-${new DateTime(slot.endTime)}"
        }
    }
}
