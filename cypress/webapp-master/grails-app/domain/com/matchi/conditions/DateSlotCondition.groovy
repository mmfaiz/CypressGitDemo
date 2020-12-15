package com.matchi.conditions

import org.joda.time.LocalDate
import org.joda.time.DateTime
import com.matchi.Slot
import org.joda.time.Interval
import javax.persistence.Transient

class DateSlotCondition extends SlotCondition {
    LocalDate startDate
    LocalDate endDate

    /**
     * From inclusive and to inclusive
     */
    @Override
    public boolean accept(Slot slot) {
        DateTime fromTime = new DateTime(startDate.toDateMidnight())
        DateTime toTime = new DateTime(endDate.toDateMidnight()).plusDays(1) // midnight day after (= to inclusive)
        DateTime slotTime = new DateTime(slot.startTime)

        return new Interval(fromTime, toTime).contains(slotTime)
    }

    String getType() {
        return "DATE"
    }

    static transients = ['type']

    static constraints = {
        startDate(nullable: false)
        endDate (nullable: false, validator: { val, obj ->
            val >= obj.startDate
        })
    }

}
