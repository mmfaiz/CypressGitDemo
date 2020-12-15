package com.matchi

import org.joda.time.DateTime
import org.joda.time.LocalTime
import org.joda.time.Period

class CreateSeason {
    DateTime startTime
    DateTime endTime
    String name
    List<CreateCourtSeason> courts = []
}

class CreateSeasonDeviation extends CreateSeason {
    boolean open
    def weekDays = []
}

/**
 * int dayOfWeek = org.joda.time.DateTimeConstants.MONDAY/TUESDAY etc
 */
class CreateCourtSeason {
    Court court
    Period timeBetween // eg. 15 minutes
    Period bookingLength   // eg. 45 minutes slots

    def openHoursPerWeekDay = [:]

    public void addOpenHours(int dayOfWeek, OpenHours openHours) {
        openHoursPerWeekDay.put(dayOfWeek, openHours)
    }

    public OpenHours getOpenHours(int dayOfWeek) {
        return openHoursPerWeekDay.get(dayOfWeek)
    }
}

class OpenHours {
    LocalTime opening
    LocalTime closing
}
