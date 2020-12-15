package com.matchi

import com.matchi.requirements.RequirementProfile
import org.joda.time.LocalDateTime
import org.joda.time.Minutes

class BookingRestriction {

    static belongsTo = [ Slot, RequirementProfile ]
    static hasMany   = [ slots: Slot, requirementProfiles: RequirementProfile ]

    int validUntilMinBeforeStart = 0 // Indicates how long before a slot start this restriction should be considered

    static constraints = {
        slots nullable: false
        requirementProfiles nullable: false, minSize: 1
        validUntilMinBeforeStart nullable: false
    }

    static mapping = {
        requirementProfiles joinTable: [name: "booking_restriction_requirement_profiles", key: "restriction_id"]
    }

    boolean accept(Customer customer, Slot slot) {
        // Always accept if slot has started
        if(slot.startTime.before(new Date())) {
            return true
        }

        Minutes timeDelta = Minutes.minutesBetween(new LocalDateTime(), new LocalDateTime(slot.startTime))
        if (timeDelta.getMinutes() < validUntilMinBeforeStart) {
            return true
        }

        return requirementProfiles?.any { it.validate(customer) }
    }
}
