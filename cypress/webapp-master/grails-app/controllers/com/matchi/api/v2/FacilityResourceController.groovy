package com.matchi.api.v2

import com.matchi.Facility
import com.matchi.api.Code
import com.matchi.api.GenericAPIController
import grails.converters.JSON
import grails.plugin.cache.Cacheable
import grails.validation.Validateable
import groovy.transform.ToString

class FacilityResourceController extends GenericAPIController {

    Object renderJson(Object o) {
        render o as JSON
    }

    def fileArchiveService

    // NOTE! Controller methods annotated with @Cacheable are no longer cached when using namespaces in the controllers.
    // Do not use namespaces in this controller and see history of this file for example.
    @Cacheable(value = "facilities")
    def list() {
        renderJson(Facility.bookableAndActiveFacilities.listDistinct())
    }

    def show(Long id) {
        def facility = Facility.get(id)

        if(facility) {
            renderJson(facility)
        } else {
            error(404, Code.RESOURCE_NOT_FOUND, "Facility not found")
        }
    }

    def courts(Long id) {
        def facility = Facility.get(id)

        if(facility) {
            renderJson(facility.courts)
        } else {
            error(404, Code.RESOURCE_NOT_FOUND, "Facility not found")
        }

    }

    def buildFacilityCourtResources(Facility facility) {
        def result = []

        facility.courts.each { def court ->
            def c = [
                    id: court.id,
                    name: court.name
            ]
            result << c
        }

        return result
    }
}

@ToString
@Validateable(nullable = true)
class QueryFacilitiesCommand {
    String country
    List<Long> sportIds
    Boolean hasCamera

    static constraints = {
        country  nullable: true
        sportIds nullable: true
        hasCamera nullable: true
    }
}

