package com.matchi.watch

import com.matchi.Slot

class SlotWatchEvent {

    Slot slot
    Date dateCreated
    Type type

    static constraints = {
    }

    public static enum Type {
        CANCEL
    }
}
