package com.matchi.marshallers

import com.matchi.Region
import grails.converters.JSON
import javax.annotation.PostConstruct

/**
 * @author Sergei Shushkevich
 */
class RegionMarshaller {

    @PostConstruct
    void register() {
        JSON.registerObjectMarshaller(Region) { Region r ->
            marshallRegion(r)
        }
    }

    def marshallRegion(Region r) {
        [
            id: r.id,
            lat: r.lat,
            lng: r.lng,
            name: r.name,
            zoomlv: r.zoomlv
        ]
    }
}