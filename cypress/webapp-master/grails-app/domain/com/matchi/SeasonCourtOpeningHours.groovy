package com.matchi

import org.joda.time.LocalTime
import org.joda.time.Period

class SeasonCourtOpeningHours implements Serializable {
    static belongsTo = [ season : Season, court : Court ]
    int weekDay
    LocalTime opens
    LocalTime closes
    Period timeBetween
    Period bookingLength
}
