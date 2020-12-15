package com.matchi.season

import grails.validation.Validateable
import org.joda.time.DateTime

@Validateable(nullable = true)
class CreateSeasonCommand {
    String name
    String description
    String startTime
    String endTime

    static constraints = {
        name(blank:false, nullable: false)
        description(blank:true, nullable: true)
        startTime(blank:false, nullable: false)
        endTime(blank:false, nullable: false, validator: {endTime, obj ->
            def startTime = obj.properties['startTime']
            new DateTime(startTime).isAfter(new DateTime(endTime)) ? ['invalid.datemismatch'] : true
        })
    }
}
