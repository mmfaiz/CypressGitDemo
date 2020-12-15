package com.matchi.integration.events

class Slot {
    final String id
    final Date startTime
    final Date endTime
    final int hourStart
    final int hourEnd
    final Court court

    Slot(com.matchi.Slot slot) {
        this.id = slot.id
        this.startTime = slot.startTime
        this.endTime = slot.endTime
        this.hourStart = slot.hourStart
        this.hourEnd = slot.hourEnd
        this.court = new Court(slot.court)
    }
}
