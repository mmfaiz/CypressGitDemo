package com.matchi.marshallers

import com.matchi.Sport
import grails.converters.JSON

import javax.annotation.PostConstruct

class SportMarshaller {

    @PostConstruct
    void register() {
        // Standard Node marshall
        JSON.registerObjectMarshaller(Sport) { Sport sport ->
            marshallSport(sport)
        }
    }

    def marshallSport(Sport sport) {
        [
                id: sport.id,
                name: sport.name
        ]
    }
}