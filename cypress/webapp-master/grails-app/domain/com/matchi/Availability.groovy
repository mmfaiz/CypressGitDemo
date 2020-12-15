package com.matchi

import org.codehaus.groovy.grails.web.servlet.mvc.GrailsParameterMap
import org.joda.time.DateTime
import org.joda.time.LocalDate
import org.joda.time.LocalTime

class Availability implements Comparable<Availability>, Serializable {

    int weekday
    LocalTime begin
    LocalTime end

    LocalDate validStart
    LocalDate validEnd

    Boolean active = true

    static constraints = {
        weekday(nullable: false)
        begin(nullable: false)
        end(nullable: false)
        validStart(nullable: true)
        validEnd(nullable: true)
    }

    static mapping = {
        sort "weekday"
    }

    String toString() {
        return "weekday: ${weekday}, begin: ${begin}, end: ${end}, validStart: ${validStart}, validEnd: ${validEnd}"
    }

    int compareTo(Availability other) {
        return other ? this.weekday?.compareTo(other.weekday) : 1
    }

    static Availability buildFromParams(GrailsParameterMap params, int index) {
        List<String> fromTime = params.get("fromTime_" + index).split(":")
        List<String> toTime = params.get("toTime_" + index).split(":")

        Availability a = new Availability()
        a.weekday = params.int("weekDay_${index}")
        a.begin = new LocalTime(Integer.parseInt(fromTime[0]), Integer.parseInt(fromTime[1]))
        a.end = new LocalTime(Integer.parseInt(toTime[0]), Integer.parseInt(toTime[1]))

        String startDateString = params.get("startDate_" + index)
        String endDateString = params.get("endDate_" + index)

        if(startDateString && endDateString) {
            a.validStart = new DateTime(startDateString).toLocalDate()
            a.validEnd = new DateTime(endDateString).toLocalDate()
        } else {
            a.validStart = null
            a.validEnd = null
        }

        return a
    }
}
