package com.matchi.idrottonline.commands

import grails.validation.Validateable
import groovy.transform.Immutable

@Validateable()
@Immutable class VenueCommand {
    String venueName

    static constraints = {
        venueName blank: false
    }
}

