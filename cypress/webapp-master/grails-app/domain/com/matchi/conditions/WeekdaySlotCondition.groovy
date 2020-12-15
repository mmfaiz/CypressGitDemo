package com.matchi.conditions

import com.matchi.Slot
import org.joda.time.DateTime

class WeekdaySlotCondition extends SlotCondition {

    static hasMany = [weekdays:Integer]

    /**
     * From inclusive and end exclusive (see @TimeSlotConditionTests)
     */
    @Override
    public boolean accept(Slot slot) {
        DateTime slotTime = new DateTime(slot.startTime)
        def accept = false

        weekdays.each {
            if(it.equals(slotTime.dayOfWeek)) {
                accept = true
            }
        }
        return accept
    }

    @Override
    void populate(def params) {
        params.list("weekdays").each {
            addToWeekdays(Integer.parseInt(it))
        }
    }

    String getType() {
        return "WEEKDAYS"
    }

    static transients = ['type']

    static constraints = {
        weekdays(nullable: false, minSize: 1, maxSize: 7)
    }

}