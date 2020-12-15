package com.matchi.activities

import com.matchi.Facility
import com.matchi.Sport

/**
 * @author Michael Astreiko
 */
abstract class Activity implements Serializable {
    String name
    String description
    boolean deleted = false

    public static final LEVEL_RANGE_MIN = 1
    public static final LEVEL_RANGE_MAX = 10

    Integer levelMin
    Integer levelMax

    static belongsTo = [facility: Facility]

    static hasMany = [participants: Participant, occasions: ActivityOccasion]

    static constraints = {
        name(nullable: false, blank: false, matches: "[0-9\\p{L}\\s\\-+_:.,!'´/()]+")
        description(nullable: true)
        deleted(nullable: true)
        levelMin(nullable: true, min: LEVEL_RANGE_MIN, max: LEVEL_RANGE_MAX)
        levelMax(nullable: true, min: LEVEL_RANGE_MIN, max: LEVEL_RANGE_MAX)
    }

    static mapping = {
        description type: 'text'
        occasions sort:'date', desc: false
    }

    static hibernateFilters = {
        nonDeletedFilter(collection: 'occasions', condition: 'deletecol=0', default: true)
    }

    Integer getCancelLimitWithFallback() {
        facility.getBookingCancellationLimit()
    }

    Sport guessSport() {
        return null
    }

    abstract String[] getToMails()

}
