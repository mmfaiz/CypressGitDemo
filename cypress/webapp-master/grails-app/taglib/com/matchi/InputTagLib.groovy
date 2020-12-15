package com.matchi

import grails.converters.JSON

class InputTagLib {
    def facilityService

    def searchFacilityInput = { attrs, body ->
        def name        = attrs.name ?: ''
        def placeholder = attrs.placeholder ?: ''
        def classes     = attrs.class ?: ''
        def value       = attrs.value ?: ''

        def facilites = Facility.findAllByActive(true, [cache: true])
        def regions = Region.list( [cache: true])
        def municipalities = Municipality.list( [cache: true])

        def source = []

        facilites.each {
            source << it.name
        }
        municipalities.each {
            source << it.name
        }
        regions.each {
            source << it.name
        }

        out << render(template: "/templates/inputs/searchFacilityInput", model: [name:name, placeholder:placeholder, classes:classes, value:value, source:source as JSON])
    }
}
