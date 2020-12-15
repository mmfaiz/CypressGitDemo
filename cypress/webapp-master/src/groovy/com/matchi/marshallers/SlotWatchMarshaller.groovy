package com.matchi.marshallers

import com.matchi.watch.SlotWatch
import grails.converters.JSON
import org.joda.time.DateTime

import javax.annotation.PostConstruct

class SlotWatchMarshaller {

    def messageSource

    @PostConstruct
    void register() {
        // Standard Node marshall
        JSON.registerObjectMarshaller(SlotWatch) { SlotWatch slotWatch ->
            marshallSlotWatch(slotWatch)
        }
    }

    def marshallSlotWatch(SlotWatch slotWatch) {
        def sportData
        if (slotWatch.sport) {
            sportData = [
                    id: slotWatch.sport.id,
                    name: messageSource.getMessage("sport.name.${slotWatch.sport.id}",
                            null, slotWatch.sport.name, new Locale(slotWatch.user.language))
            ]
        }

        [
                id: slotWatch.id,
                from: new DateTime(slotWatch.fromDate).toString("HH:mm"),
                to: new DateTime(slotWatch.toDate).toString("HH:mm"),
                court: slotWatch.court,
                facility: slotWatch.facility,
                sport: sportData,
                sms: slotWatch.smsNotify
        ]
    }
}
