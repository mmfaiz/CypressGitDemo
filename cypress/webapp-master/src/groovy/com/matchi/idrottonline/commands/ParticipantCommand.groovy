package com.matchi.idrottonline.commands

import com.fasterxml.jackson.annotation.JsonIgnore
import grails.validation.Validateable

@Validateable()
class ParticipantCommand {

    final String attendanceStatusString = "Attendant"

    String personId // References PersonCommand.externalIdentification and must match/exist otherwise the activity will not be processed by IdrottOnline.
    String disabled = false
    String leader
    String attendanceStatus = attendanceStatusString


    @JsonIgnore // Only used for validation and should not be sent to IdrottOnline.
    PersonsCommand personsCommand

    @JsonIgnore // Only used for better validation message when missing customer (PersonCommand) reference.
    String name

    @JsonIgnore // A participant must be a member to be able to participate in an activity.
    boolean isMember

    @JsonIgnore // If the participant is a trainer without customer reference
    String trainerId

    static constraints = {
        personId blank: false
        disabled blank: false, inList: ["true", "false"]
        leader blank: false, inList: ["true", "false"]
        attendanceStatus blank: false
        personsCommand validator: { val, obj, errors ->
            if (!(val.every{it.validate()})) errors.rejectValue('personsCommand', 'participantCommand.personsCommand.invalid')
        }
        isMember validator: { val, obj, errors ->
            if (obj.personsCommand?.person?.organisations?.every{
                it.organisation.every {
                    it.sports.every {
                        it.sport.isActive != "true"}
                }
            }) errors.rejectValue('isMember', 'facilityCustomer.iosync.missingmembership')
        }
        name nullable: true
        trainerId nullable: true
    }
}
