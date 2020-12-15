package com.matchi.idrottonline

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.matchi.ErrorsMixIn
import com.matchi.idrottonline.commands.ActivityCommand
import com.matchi.idrottonline.commands.OrganisationCommand
import com.matchi.idrottonline.commands.OrganisationsCommand
import com.matchi.idrottonline.commands.ParticipantCommand
import com.matchi.idrottonline.commands.PersonCommand
import com.matchi.idrottonline.commands.PersonsCommand
import com.matchi.idrottonline.commands.SportCommand
import com.matchi.idrottonline.commands.SportsCommand
import com.matchi.idrottonline.commands.VenueCommand
import grails.validation.Validateable

@Validateable()
class IdrottOnlineActivitiesCommand extends IdrottOnlineCommand{

    String applicationId
    List<PersonsCommand> persons
    List<ActivityCommand> activities

    static constraints = {
        applicationId blank: false
        persons minSize: 1, validator: { val -> val.every { it.validate() } }
        activities minSize: 1, validator: { val -> val.every { it.validate() } }
    }

    IdrottOnlineActivitiesCommand(String appId, List<ActivityCommand> activities) {
        applicationId = appId
        this.activities = activities

        setPersonsFromActivities(activities)
    }

    void addSyncResultInfo(IdrottOnlineSyncResult syncResult){
        syncResult.customerIds = persons.collect { PersonsCommand personsCommand ->
            personsCommand.person.customerId as Long
        }
        syncResult.activityOccasionIds = activities.collect { ActivityCommand activityCommand ->
            activityCommand.activityOccassionId as Long
        }
    }

    @JsonIgnore
    List<ActivityCommand> getValidActivities() {
        activities.findAll{it.validate()}
    }

    @JsonIgnore
    List<ActivityCommand> getNotValidActivities(){
        activities.findAll{!it.validate()}
    }

    void removeNotValidated() {
        activities.removeAll { !it.validate() }
        setPersonsFromActivities(activities)
    }

    private void setPersonsFromActivities(List<ActivityCommand> activities) {
        List<PersonsCommand> personsList = new ArrayList<PersonsCommand>()
        activities.each { ActivityCommand activityCommand ->
            personsList.addAll(activityCommand.participants*.personsCommand)
        }
        persons = personsList
    }

    String toJSON() {
        def mapper = new ObjectMapper()
        mapper.addMixInAnnotations(IdrottOnlineActivitiesCommand, ErrorsMixIn)
        mapper.addMixInAnnotations(ActivityCommand, ErrorsMixIn)
        mapper.addMixInAnnotations(VenueCommand, ErrorsMixIn)
        mapper.addMixInAnnotations(ParticipantCommand, ErrorsMixIn)
        mapper.addMixInAnnotations(PersonsCommand, ErrorsMixIn)
        mapper.addMixInAnnotations(PersonCommand, ErrorsMixIn)
        mapper.addMixInAnnotations(OrganisationsCommand, ErrorsMixIn)
        mapper.addMixInAnnotations(OrganisationCommand, ErrorsMixIn)
        mapper.addMixInAnnotations(SportsCommand, ErrorsMixIn)
        mapper.addMixInAnnotations(SportCommand, ErrorsMixIn)
        mapper.setPropertyNamingStrategy(PropertyNamingStrategy.PASCAL_CASE_TO_CAMEL_CASE)
        mapper.writeValueAsString(this)
    }
}