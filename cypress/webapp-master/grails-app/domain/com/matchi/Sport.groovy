package com.matchi

import com.matchi.sportprofile.SportAttribute
import com.matchi.sportprofile.SportProfile

class Sport implements Comparable<Sport>, Serializable {
    private static final long serialVersionUID = 12L

    public static final long TENNIS_ID = 1L

    static hasMany = [courts: Court, sportProfiles: SportProfile, sportAttributes: SportAttribute]

    String name
    Integer position
    Boolean coreSport

    static constraints = {
        name unique: true, nullable: false, blank: false
        coreSport nullable: true, default: false
    }

    static mapping = {
        sort "position": "asc"
        sportAttributes sort: 'name', order: 'asc'
        cache true
    }

    String toString() { "$name" }

    int compareTo(Sport other) {
        return other ? this.position?.compareTo(other.position) : 1
    }

    // Ugly hack to actually filter non core sports that has been added.
    def isReallyCoreSport() {
        return coreSport && id != 6 && id != 14
    }

    static namedQueries = {
        realCoreSports {
            eq "coreSport", Boolean.TRUE
            notEqual "id", 6L
            notEqual "id", 14L
            order "position", "asc"
            cache true
        }
        realNonCoreSports {
            eq "coreSport", Boolean.FALSE
            inList "id", [6L, 14L]
            order "position", "asc"
            cache true
        }
        coreSportAndOther {
            eq "coreSport", Boolean.TRUE
            notEqual "id", 14L
            order "position", "asc"
            cache true
        }
    }
}
