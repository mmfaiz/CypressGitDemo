package com.matchi.conditions

import com.matchi.Slot
import org.joda.time.DateTime

/**
 * @author Sergei Shushkevich
 */
class HoursInAdvanceBookableSlotCondition extends SlotCondition {

    Integer nrOfHours

    @Override
    boolean accept(Slot slot) {
        return !(new DateTime().plusHours(nrOfHours).isBefore(new DateTime(slot.startTime)))
    }

    String getType() {
        return "HOURSINADVANCE"
    }

    static transients = ['type']

    static constraints = {
        nrOfHours(nullable: false, min: 1)
    }
}