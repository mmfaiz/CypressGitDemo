package com.matchi.integration.events

class Facility {
    final String id
    final String name
    final String shortName

    Facility(com.matchi.Facility facility) {
        this.id = facility.id
        this.name = facility.name
        this.shortName = facility.shortname
    }
}
