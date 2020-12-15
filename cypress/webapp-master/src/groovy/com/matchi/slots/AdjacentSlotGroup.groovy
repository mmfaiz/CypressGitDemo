package com.matchi.slots

import com.matchi.Slot

/**
 * A group of adjacent slots on the same court
 */
class AdjacentSlotGroup {

    // Always ordered, as guaranteed by constructor and getter
    final List<Slot> selectedSlots

    // List of all selected and could-be-selected slots
    List<Slot> subsequentSlots

    AdjacentSlotGroup(List<Slot> slots) {
        if(slots && slots*.court*.id.unique().size() > 1) {
            throw new IllegalStateException("AdjacentSlotGroup must be on same court")
        }

        // Use collect to copy the incoming list for immutabiliy
        this.selectedSlots = slots.collect().sort { Slot slot ->
            return slot.startTime
        }
    }

    void setSubsequentSlots(List<Slot> slots) {
        this.subsequentSlots = slots.collect().sort { Slot slot ->
            return slot.startTime
        }
    }

    List<Slot> getSubsequentSlots() {
        return this.subsequentSlots.collect() as List<Slot>
    }

    List<Slot> getSelectedSlots() {
        return this.selectedSlots.collect() as List<Slot>
    }

    Slot getFirstSlot() {
        return this.selectedSlots.first()
    }

    Slot getLastSlot() {
        return this.selectedSlots.last()
    }
}
