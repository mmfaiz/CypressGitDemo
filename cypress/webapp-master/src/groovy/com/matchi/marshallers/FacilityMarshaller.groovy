package com.matchi.marshallers


import com.matchi.Facility
import grails.converters.JSON

import javax.annotation.PostConstruct

class FacilityMarshaller {

    def grailsLinkGenerator

    @PostConstruct
    void register() {
        // Standard Node marshall
        JSON.registerObjectMarshaller(Facility) { Facility facility -> marshallFacility(facility) }
    }

    def marshallFacility(Facility facility) {
        [
                id          : facility.id,
                name        : facility.name,
                shortname   : facility.shortname,
                description : facility.description,
                address     : facility.address,
                zipcode     : facility.zipcode,
                telephone   : facility.telephone,
                fax         : facility.fax,
                city        : facility.city,
                country     : facility.country,
                email       : facility.email,
                webUrl      : grailsLinkGenerator.link(controller: 'facility', action: 'show', absolute: true, params: [name: facility.shortname]),
                imageUrl    : facility.facilityWelcomeImage ? facility.facilityWelcomeImage.getAbsoluteFileURL() :
                        grailsLinkGenerator.serverBaseURL + "/images/fac_sport_${facility.sports?.findAll { it.isReallyCoreSport() }.sort { Math.random() }?.getAt(0)?.id}.jpg",

                position    : [latitude: facility.lat, longitude: facility.lng],

                logoUrl     : facility.facilityLogotypeImage ? facility.facilityLogotypeImage.getAbsoluteFileURL() : "",

                openHours   : facility.availabilities?.sort { it.weekday },
                courts      : facility.courts.findAll { !it.offlineOnly && !it.archived && facility.sports.contains(it.sport) },
                municipality: facility.municipalityId,
                region      : facility.municipality?.regionId
        ]
    }
}
