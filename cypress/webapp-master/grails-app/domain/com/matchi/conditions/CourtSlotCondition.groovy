package com.matchi.conditions

import com.matchi.Court
import com.matchi.Slot

class CourtSlotCondition extends SlotCondition {
    static belongsTo = Court
    static hasMany = [courts: Court]

    @Override
    boolean accept(Slot slot) {
        if (slot && slot.court) {
            def courtIds = courts.collect { it.id }
            return courtIds.contains(slot.court.id)
        } else {
            false
        }
    }

    @Override
    void populate(def params) {
        params.list("courts").each {
            addToCourts(Court.get(it))
        }
    }

    String getType() {
        return "COURTS"
    }

    static transients = ['type']

    static constraints = {
        courts(nullable: false, minSize: 1)
    }

    static mapping = {
        courts joinTable: [name: "court_slot_condition_court", key: 'court_slot_condition_courts_id']
    }
}