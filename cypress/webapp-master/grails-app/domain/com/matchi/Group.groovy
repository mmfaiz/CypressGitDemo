package com.matchi

class Group implements Serializable {

    static belongsTo = [ facility: Facility ]
    static hasMany = [ customerGroups: CustomerGroup ]

    String name
    String description
    Date dateCreated
    Date lastUpdated

    static constraints = {
        name (blank: false, nullable: false)
        description(nullable: true, maxSize: 1000)
    }

    static mapping = {
        sort "name"
        table 'facility_group'
        autoTimestamp true
        customerGroups cascade: 'all-delete-orphan'
    }

    String toString() { "$name" }
}
