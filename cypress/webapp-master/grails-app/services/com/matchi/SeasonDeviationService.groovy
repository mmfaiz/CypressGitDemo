package com.matchi

import com.matchi.slots.SlotDelta

class SeasonDeviationService {

    static transactional = true
    def slotService

    def apply(SeasonDeviation deviation) {
        if(deviation.open) {
            applyOpen(deviation)
        } else {
            applyClose(deviation)
        }
    }

    def applyOpen(SeasonDeviation deviation) {

        def slotsToOpen = slotService.generateSlots(deviation.toCreateSeasonCommand())
        def slotsExisting = slotService.getSlots(deviation.toSlotFilter())

        SlotDelta delta = new SlotDelta(slotsToOpen, slotsExisting)
        slotService.removeSlots(delta.rightOverlaps(delta.leftOnly())
                .values().flatten()*.slot.unique())

        slotService.createSlots(slotsToOpen)
    }

    def applyClose(SeasonDeviation deviation) {
        def slotsToDelete = slotService.getSlots(deviation.toSlotFilter())
        slotService.removeSlots(slotsToDelete)
    }

    def remove(SeasonDeviation deviation) {
        deviation.delete()
    }

    def save(SeasonDeviation deviation) {
        deviation.save(failOnError: true)
    }

    def saveAndApply(SeasonDeviation deviation) {
        save(deviation)
        apply(deviation)
    }
}
