package com.matchi

import org.joda.time.DateTime


class Ticket implements Serializable {
    String key
    Date expires
    Date dateCreated
    Date lastUpdated
    Date consumed

    def isValid() {
        return new DateTime(expires.time).isAfterNow() && !consumed
    }

    static constraints = {
        key nullable: false, unique: true
        expires nullable: false
        consumed nullable: true
    }

    static mapping = {
        autoTimestamp true
        key column:'ticketKey'
    }
}
