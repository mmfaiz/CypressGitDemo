package com.matchi.conditions

import org.joda.time.LocalTime
import org.joda.time.DateTime
import com.matchi.Slot
import org.joda.time.Interval
import org.joda.time.Period

class TimeSlotCondition extends SlotCondition {
    LocalTime startTime
    LocalTime endTime

    /**
     * From inclusive and end exclusive (see @TimeSlotConditionTests)
     */
    @Override
    public boolean accept(Slot slot) {
        DateTime slotStartTime = new DateTime(slot.startTime).toLocalTime().toDateTimeToday()
        DateTime slotEndTime = new DateTime(slot.startTime).toLocalTime().toDateTimeToday()

        DateTime conditionStartTime = startTime.toDateTimeToday()
        DateTime conditionEndTime = endTime.toDateTimeToday()

        return ((conditionStartTime.isBefore(slotStartTime) || conditionStartTime.isEqual(slotStartTime))
                && (conditionEndTime.isAfter(slotEndTime)))
    }

    String getType() {
        return "TIME"
    }

    static transients = ['type']

    static constraints = {
        startTime(nullable: false)
        endTime (nullable: false, validator: { val, obj ->
            val >= obj.startTime
        })
    }

}