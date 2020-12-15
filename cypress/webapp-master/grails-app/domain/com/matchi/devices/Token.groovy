package com.matchi.devices

import org.apache.commons.lang.RandomStringUtils

class Token {

    static belongsTo = [ device: Device ]

    String identifier
    Date blocked
    Date dateCreated
    Date lastUpdated

    def isValid() {
        return blocked == null && device.blocked == null
    }

    static constraints = {
        identifier unique: true
        blocked nullable: true
    }

    static mapping = {
        autoTimestamp true
    }

    static def generateIdentifier() {
        RandomStringUtils.random(30, true, true)
    }
}
