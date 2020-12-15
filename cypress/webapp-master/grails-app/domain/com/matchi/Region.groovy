package com.matchi

class Region implements Serializable {

    static hasMany = [ municipalities:Municipality ]

    String country = "SE"

    String name
    Double lat
    Double lng
    int zoomlv

    static constraints = {
        country(nullable: false, blank: false)
		name(nullable: false, blank: false)
        lat(nullable: false)
        lng(nullable: false)
        zoomlv(nullable: true)
    }

    static mapping = {
        sort name: "asc"
        municipalities sort: "name", order: "asc", cache: true
        cache  true
    }

    String toString() { "$name" }
}
