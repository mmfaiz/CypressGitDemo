package com.matchi

/* Created: 2012-09-18 Mattias (mattias@tdag.se) */
class RecurringSlotsContainer {
    List<Slot> freeSlots = []
    List<Slot> unavailableSlots = []

    public String listFreeSlots() {
        def slots = ""

        for(int i=0; i<this.freeSlots.size();i++) {
            if(i < this.freeSlots.size() - 1) {
                slots += this.freeSlots[i].id + ","
            } else {
                slots += this.freeSlots[i].id
            }
        }

        return slots
    }
}
