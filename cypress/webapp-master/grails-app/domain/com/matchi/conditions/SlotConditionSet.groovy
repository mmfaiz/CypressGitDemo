package com.matchi.conditions

import com.matchi.Slot

/**
 * Represents a set of slot conditions. This set is accepts slot only if ALL of the
 * conditions accepts the slot.
 */
class SlotConditionSet implements Serializable {
    static hasMany = [slotConditions: SlotCondition]

    String unsavedIdentifier

    static transients = ['unsavedIdentifier']

    def identifier() {
        if(id) {return id} else {return "H" + unsavedIdentifier}
    }

    def accept(Slot slot) {
        if(slotConditions && !slotConditions.isEmpty()) {
            def failedConditions = slotConditions.any() { !it.accept(slot) }
            return !failedConditions
        } else {
            false
        }
    }

    static constraints = {
    }
}
