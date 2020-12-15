package com.matchi.slots

import org.joda.time.LocalDateTime
import org.joda.time.ReadableInstant
import org.joda.time.LocalTime

class SlotFilter {
    ReadableInstant from
    ReadableInstant to
    LocalTime fromTime
    LocalTime toTime
    LocalTime fixedStartTime
    def courts = []
    def onlyFreeSlots = false
    List<Integer> onWeekDays = []

    String toString() {
        return "${this.class.getName()}: [${from}, ${to}, ${fromTime}, ${toTime}, ${courts}, ${onWeekDays}]"
    }
}
