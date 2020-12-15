package com.matchi.marshallers

import com.matchi.Availability
import grails.converters.JSON

import javax.annotation.PostConstruct

class AvailabilityMarshaller {

    @PostConstruct
    void register() {
        // Standard Node marshall
        JSON.registerObjectMarshaller(Availability) { Availability availability ->
            marshallAvailability(availability)
        }
    }

    def marshallAvailability(Availability availability) {
        def result = [
                weekday: availability.weekday
        ]

        if(availability.active) {
            result.open  = availability.begin
            result.close = availability.end
        }

        return result
    }
}
