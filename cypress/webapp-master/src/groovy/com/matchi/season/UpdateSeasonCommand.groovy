package com.matchi.season

import com.matchi.CreateCourtSeason
import grails.validation.Validateable

@Validateable(nullable = true)
class UpdateSeasonCommand {
    Long id
    String name
    String description
    Date startDate
    Date endDate

    List<CreateCourtSeason> courts = []

    static constraints = {
        name(blank:false, nullable: false)
        description(blank:true, nullable: true)
    }
}
