package com.matchi.sportprofile

import com.matchi.Sport

class SportAttribute {

    static belongsTo = [ sport:Sport ]

    String name
    String description

    static constraints = {
        name(nullable: false)
        description(nullable: true)
    }

    String toString() { "$name" }
}
