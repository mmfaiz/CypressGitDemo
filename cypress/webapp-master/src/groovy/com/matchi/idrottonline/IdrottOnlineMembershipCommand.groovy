package com.matchi.idrottonline

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.matchi.ErrorsMixIn
import com.matchi.idrottonline.commands.OrganisationCommand
import com.matchi.idrottonline.commands.OrganisationsCommand
import com.matchi.idrottonline.commands.PersonCommand
import com.matchi.idrottonline.commands.PersonsCommand
import com.matchi.idrottonline.commands.SportCommand
import com.matchi.idrottonline.commands.SportsCommand
import grails.validation.Validateable

@Validateable()
class IdrottOnlineMembershipCommand extends IdrottOnlineCommand{

    String applicationId
    List<PersonsCommand> persons

    static constraints = {
        applicationId blank: false
        persons minSize: 1, validator: { val -> val.every { it.validate() } }
    }

    IdrottOnlineMembershipCommand(String appId, List<PersonsCommand> persons) {
        applicationId = appId
        this.persons = persons
    }

    void addSyncResultInfo(IdrottOnlineSyncResult syncResult){
        syncResult.customerIds = persons.collect { PersonsCommand personsCommand ->
            personsCommand.person.customerId as Long
        }
    }

    @JsonIgnore
    List<PersonCommand> getValidPersons() {
        persons.collect{it.person}.findAll{it.validate()}
    }

    @JsonIgnore
    List<PersonCommand> getNotValidPersons(){
        persons.collect{it.person}.findAll{!it.validate()}
    }

    void removeNotValidated(){
        persons.removeAll { !it.validate() }
    }

    String toJSON() {
        def mapper = new ObjectMapper()
        mapper.addMixInAnnotations(IdrottOnlineMembershipCommand, ErrorsMixIn)
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




