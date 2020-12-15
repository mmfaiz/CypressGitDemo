package com.matchi.api_ext.model

import com.matchi.activities.EventActivity
import org.joda.time.LocalDate

class APIExtEvent {
    Long id
    String name
    String description
    LocalDate startDate
    LocalDate endDate
    Integer maxParticipants
    Integer registeredParticipants
    Boolean membershipRequired
    Boolean paymentRequired
    LocalDate activeFrom
    LocalDate activeTo

    APIExtEvent(EventActivity eventActivity) {
        this.id = eventActivity.id
        this.name = eventActivity.name
        this.description = eventActivity.description
        this.startDate = new LocalDate(eventActivity.startDate)
        this.endDate = new LocalDate(eventActivity.endDate)
        this.maxParticipants = eventActivity.form?.maxSubmissions
        this.registeredParticipants = eventActivity.form?.acceptedSubmissionsAmount
        this.membershipRequired = eventActivity.form?.membershipRequired
        this.paymentRequired = eventActivity.form?.paymentRequired
        this.activeFrom = new LocalDate(eventActivity.form?.activeFrom)
        this.activeTo = new LocalDate(eventActivity.form?.activeTo)
    }
}
