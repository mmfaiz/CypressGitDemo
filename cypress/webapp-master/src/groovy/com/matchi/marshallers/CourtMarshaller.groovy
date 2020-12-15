package com.matchi.marshallers

import com.matchi.Court
import com.matchi.CourtTypeAttribute
import com.matchi.CourtTypeEnum
import grails.converters.JSON

import javax.annotation.PostConstruct

class CourtMarshaller {

    List<String> courtTypeEnumArray = []

    @PostConstruct
    void register() {
        // Standard Node marshall
        JSON.registerObjectMarshaller(Court) { Court court ->
            marshallCourt(court)
        }
    }

    def marshallCourt(Court court) {
        def response = [
                id: court.id,
                name: court.name,
                sport: court.sport,
                surface: court.surface.toString(),
                indoor: court.indoor,
                facility: [
                        id: court.facility.id,
                        name: court.facility.name,
                ],
                cameras: court.facility.hasCameraFeature() ? court.cameras : [],
                courtTypes: [:]
        ]

        CourtTypeEnum.getBySport(court.sport).each { CourtTypeEnum courtTypeEnum ->
            response.courtTypes[courtTypeEnum.name()] = court.courtTypeAttributes.find { CourtTypeAttribute it ->
                it.courtTypeEnum == courtTypeEnum
            }?.value ?: ""
        }

        response
    }
}
