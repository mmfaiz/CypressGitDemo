package com.matchi.idrottonline.commands

import com.fasterxml.jackson.annotation.JsonIgnore
import com.matchi.idrottonline.IdrottOnlineSportMapper
import grails.validation.Validateable
import groovy.transform.Immutable
import org.springframework.validation.ObjectError

@Validateable()
@Immutable class ActivityCommand {

    String activityId
    String typeId
    String optionType
    String startTime
    String endTime
    String name
    String organisationNumber
    VenueCommand venue
    String description
    String sportID
    List<ParticipantCommand> participants

    @JsonIgnore // Only used for validation messages.
    int dayOfWeek

    @JsonIgnore // Only used for validation messages.
    String prettyName

    @JsonIgnore // Only used for sync result information.
    Long activityOccassionId

    static constraints = {
        activityId blank: false
        typeId blank: false, inList: [TypeId.TRAINING.toString(),
                                      TypeId.COMPETITION.toString(),
                                      TypeId.MEETING.toString(),
                                      TypeId.EDUCATION.toString(),
                                      TypeId.CAMP.toString(),
                                      TypeId.ANNUAL_MEETING.toString(),
                                      TypeId.BOARD_MEETING.toString(),
                                      TypeId.COMMITTEE_MEETING.toString()]
        optionType blank: false, inList: [OptionType.INSERT.toString(),
                                          OptionType.DELETE.toString()]

        startTime blank: false
        endTime blank: false
        name blank: false
        organisationNumber blank: false
        venue validator: { it.validate() }
        sportID blank:false, inList: [IdrottOnlineSportMapper.SportId.TENNIS.toString(),
                                      IdrottOnlineSportMapper.SportId.BADMINTON.toString(),
                                      IdrottOnlineSportMapper.SportId.SQUASH.toString(),
                                      IdrottOnlineSportMapper.SportId.TABLETENNIS.toString()]
        participants minSize: 1, validator: { val, obj, errors ->
            if (!(val.every{it.validate()})) errors.rejectValue('participants', 'facilityCustomer.iosync.allparticipantsnotvalidated')
        }
        description nullable: true
    }

    @JsonIgnore
    List<Map.Entry<ParticipantCommand, List<ObjectError>>> getErrorsCascading(String missingCustomerReferenceErrorMessage){
        List<Map.Entry<ParticipantCommand, List<ObjectError>>> errorsList = new ArrayList<Map.Entry<ParticipantCommand, List<ObjectError>>>()

        // Instance
        List<ObjectError> instanceErrors = new ArrayList<ObjectError>()
        this.errors.allErrors.each { instanceErrors.add( it ) }
        errorsList.add(new AbstractMap.SimpleEntry<ParticipantCommand, List<ObjectError>>(null, instanceErrors))

        // Participants
        participants.each { ParticipantCommand participantCommand ->

            if(!participantCommand.validate()){
                List<ObjectError> participantErrors = new ArrayList<ObjectError>()
                if(!participantCommand.personsCommand)

                    participantErrors.add(new ObjectError("Customer", missingCustomerReferenceErrorMessage))
                else
                {
                    participantCommand.personsCommand.person.errors.allErrors.each { participantErrors.add( it )  }
                    participantCommand.errors.allErrors.each { participantErrors.add( it )  }
                }

                errorsList.add(new AbstractMap.SimpleEntry<ParticipantCommand, List<ObjectError>>(participantCommand, participantErrors))
            }
        }
        errorsList
    }


    enum TypeId {
        TRAINING(1), // 1 Träning
        COMPETITION(2), // 2 Match/Tävling/Cup
        MEETING(3), // 3 Möte
        EDUCATION(4), // 4 Utbildning
        CAMP(5), // 5 Läger
        OTHER(6), // 6 Övrigt
        ANNUAL_MEETING(7), // 7 Årsmöte
        BOARD_MEETING(8), // 8 Styrelsemöte
        COMMITTEE_MEETING(9) // 9 Kommittémöte

        TypeId(int value) {
            this.value = value
        }
        private final int value

        public String toString() {
            return value
        }
    }

    enum OptionType {
        INSERT("Insert"),
        DELETE("Delete")

        OptionType(String value) { this.value = value }
        private final String value
        public String toString() { return value }
    }
}
