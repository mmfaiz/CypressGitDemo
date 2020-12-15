package com.matchi.marshallers

import com.matchi.Slot
import grails.converters.JSON
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat

import javax.annotation.PostConstruct

class SlotMarshaller {
    @PostConstruct
    void register() {
        // Standard Node marshall
        JSON.registerObjectMarshaller(Slot) { Slot slot ->
            marshallSlot(slot)
        }
    }

    def marshallSlot(Slot slot) {
        [
                id: slot.id,
                start: ISODateTimeFormat.dateTime().print(new DateTime(slot.startTime)),
                end: ISODateTimeFormat.dateTime().print(new DateTime(slot.endTime)),
                court: slot.court
        ]
    }
}
