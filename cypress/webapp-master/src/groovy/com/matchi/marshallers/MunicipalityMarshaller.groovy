package com.matchi.marshallers

import com.matchi.Municipality
import grails.converters.JSON
import javax.annotation.PostConstruct

/**
 * @author Sergei Shushkevich
 */
class MunicipalityMarshaller {

    @PostConstruct
    void register() {
        JSON.registerObjectMarshaller(Municipality) { Municipality m ->
            marshallMunicipality(m)
        }
    }

    def marshallMunicipality(Municipality m) {
        [
            id: m.id,
            lat: m.lat,
            lng: m.lng,
            name: m.name,
            region: m.region,
            zoomlv: m.zoomlv
        ]
    }
}