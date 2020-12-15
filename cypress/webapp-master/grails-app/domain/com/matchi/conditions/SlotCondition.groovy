package com.matchi.conditions

import com.matchi.Slot

abstract class SlotCondition implements Serializable {

    static constraints = {
    }

    abstract boolean accept(Slot slot);

    void populate(def params) {
        this.properties = params
    }
}
