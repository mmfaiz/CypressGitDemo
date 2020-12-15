package com.matchi

class Municipality implements Serializable {

    private static final long serialVersionUID = 12L

    static belongsTo = [ region:Region ]
    static hasMany = [ facilities:Facility, users:User ]

    String name
    Double lat
    Double lng
    int zoomlv

    static constraints = {
        name(nullable: false)
        lat(nullable: false)
        lng(nullable: false)
        zoomlv(nullable: true)
    }

    static mapping = {
        sort name: "asc"
        facilities cache: true
        cache true
    }

    String toString() { "$name" }
}
