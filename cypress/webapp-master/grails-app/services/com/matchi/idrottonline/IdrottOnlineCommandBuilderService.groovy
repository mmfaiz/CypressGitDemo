package com.matchi.idrottonline

import com.matchi.Customer
import com.matchi.Facility
import com.matchi.Sport
import com.matchi.activities.ActivityOccasion
import com.matchi.idrottonline.commands.ActivityCommand
import com.matchi.idrottonline.commands.OrganisationCommand
import com.matchi.idrottonline.commands.OrganisationsCommand
import com.matchi.idrottonline.commands.ParticipantCommand
import com.matchi.idrottonline.commands.PersonCommand
import com.matchi.idrottonline.commands.PersonsCommand
import com.matchi.idrottonline.commands.SportCommand
import com.matchi.idrottonline.commands.SportsCommand
import com.matchi.idrottonline.commands.VenueCommand

class IdrottOnlineCommandBuilderService {

    public IdrottOnlineMembershipCommand buildMembershipCommand(Facility facility, IdrottOnlineSettings settings, List<Customer> customers, boolean skipInvalid = false) {
        List<PersonsCommand> personsCommandList = buildPersonsCommand(facility, customers, settings)

        IdrottOnlineMembershipCommand idrottOnlineMembershipCommand = new IdrottOnlineMembershipCommand(settings.appId, personsCommandList)
        if (skipInvalid) idrottOnlineMembershipCommand.removeNotValidated()
        idrottOnlineMembershipCommand
    }

    public IdrottOnlineActivitiesCommand buildActivitiesCommand(Facility facility, IdrottOnlineSettings settings, List<ActivityOccasionOccurence> activityOccasions, boolean skipInvalid = false) {
        List<ActivityCommand> activitiesCommandList = buildActivitiesCommandList(facility, activityOccasions, settings)
        IdrottOnlineActivitiesCommand idrottOnlineActivitiesCommand = new IdrottOnlineActivitiesCommand(settings.appId, activitiesCommandList)
        if (skipInvalid) idrottOnlineActivitiesCommand.removeNotValidated()
        idrottOnlineActivitiesCommand
    }

    private static List<PersonsCommand> buildPersonsCommand(Facility facility, List<Customer> customers, IdrottOnlineSettings settings) {
        String idrottOnlineOrganisationNumber = facility?.getIdrottOnlineOrganisationNumber()
        List<Sport> membershipSports = facility?.getIdrottOnlineMembershipSports()

        customers.collect { Customer customer -> buildPersonsCommand(facility, idrottOnlineOrganisationNumber, membershipSports, customer, settings) }
    }

    private static PersonsCommand buildPersonsCommand(Facility facility, String idrottOnlineOrganisationNumber, List<Sport> membershipSports, Customer customer, IdrottOnlineSettings settings){
        def sports = []
        membershipSports?.each { Sport sport ->
            def id = new IdrottOnlineSportMapper().getIdrottOnlineSportId(sport)
            if (id) {
                SportCommand sportCommand = new SportCommand(
                        optionType: SportCommand.OptionType.UPDATE.toString(),
                        isActive: (customer?.hasActiveIdrottOnlineMembership() || customer?.hasActiveIdrottOnlineMembershipAtFacility(facility)) ? "true" : "false",
                        sportID: id)

                sports << new SportsCommand(sport: sportCommand)
            }
        }

        def person = new PersonCommand(
                createDate: customer?.dateCreated?.format(settings.DATE_TIME_FORMAT),
                changeDate: customer?.lastUpdated?.format(settings.DATE_TIME_FORMAT),
                optionType: PersonCommand.OptionType.UPDATE.toString(),
                birthDate: customer?.dateOfBirth ? customer?.dateOfBirth?.format(settings.DATE_OF_BIRTH_FORMAT) : "",
                personalIdentityNumber: customer?.getPersonalNumberForIdrottOnline(),
                firstName: customer?.firstname ? customer?.firstname : "",
                lastName: customer?.lastname ? customer?.lastname : "",
                gender: customer?.type ? customer?.type?.name()?.toLowerCase() : "",
                customerId: customer?.id?.toString(),
                organisations: [new OrganisationsCommand(organisation:
                        new OrganisationCommand(
                                optionType: OrganisationCommand.OptionType.UPDATE.toString(),
                                externalOrganisationIdentification: facility.id,
                                organisationIdentityNumber: idrottOnlineOrganisationNumber,
                                sports: sports
                        ))],
                externalIdentification: customer?.getPersonalNumberForIdrottOnline()
        )

        new PersonsCommand(person: person)
    }

    private List<ActivityCommand> buildActivitiesCommandList(Facility facility, List<ActivityOccasionOccurence> activityOccasions, IdrottOnlineSettings settings) {
        String idrottOnlineOrganisationNumber = facility?.getIdrottOnlineOrganisationNumber()
        List<Sport> membershipSports = facility?.getIdrottOnlineMembershipSports()
        Sport idrottOnlineActivitiesport = facility.getIdrottOnlineActivitiesSport()

        activityOccasions.collect { buildActivityCommand(facility, idrottOnlineActivitiesport, idrottOnlineOrganisationNumber, membershipSports, it, settings) }
    }

    private ActivityCommand buildActivityCommand(Facility facility, Sport idrottOnlineActivitiesSport, String idrottOnlineOrganisationNumber, List<Sport> membershipSports, ActivityOccasionOccurence activityOccasionOccurence, IdrottOnlineSettings settings) {
        List<ParticipantCommand> participantCommandList = new ArrayList<ParticipantCommand>()
        participantCommandList.addAll(buildParticipantCommandForTrainer(facility, idrottOnlineOrganisationNumber, membershipSports, activityOccasionOccurence.activityOccasion, settings))
        participantCommandList.addAll(buildParticipantCommand(facility, idrottOnlineOrganisationNumber, membershipSports, activityOccasionOccurence.activityOccasion, settings))


        new ActivityCommand(
                activityId: activityOccasionOccurence.getUniqueIdentifier(),
                name: activityOccasionOccurence.activityOccasion.activity.name,
                description: activityOccasionOccurence.activityOccasion.message,
                typeId: ActivityCommand.TypeId.TRAINING.toString(),
                optionType: ActivityCommand.OptionType.INSERT.toString(),
                startTime: activityOccasionOccurence.getStartDateTime().format(settings.DATE_TIME_FORMAT),
                endTime: activityOccasionOccurence.getEndDateTime().format(settings.DATE_TIME_FORMAT),
                organisationNumber: idrottOnlineOrganisationNumber,
                sportID: idrottOnlineActivitiesSport ? new IdrottOnlineSportMapper().getIdrottOnlineSportId(idrottOnlineActivitiesSport) : "",
                venue: new VenueCommand(venueName: activityOccasionOccurence.activityOccasion.court?.name),
                participants: participantCommandList,
                dayOfWeek: activityOccasionOccurence.date.dayOfWeek,
                prettyName: activityOccasionOccurence.toString(),
                activityOccassionId: activityOccasionOccurence.activityOccasion.id

        )
    }

    private List<ParticipantCommand> buildParticipantCommandForTrainer(Facility facility, String idrottOnlineOrganisationNumber, List<Sport> membershipSports, ActivityOccasion activityOccasion, IdrottOnlineSettings settings) {
        List<ParticipantCommand> participantCommandList = new ArrayList<ParticipantCommand>()
        activityOccasion.trainers.each {
            PersonsCommand personsCommand = it.customer ? buildPersonsCommand(facility, idrottOnlineOrganisationNumber, membershipSports, it.customer, settings) : null
            ParticipantCommand participantCommand = new ParticipantCommand(
                    personId: personsCommand?.person?.externalIdentification,
                    leader: true,
                    personsCommand: personsCommand,
                    name: it.toString(),
                    trainerId: it.id
            )
            participantCommandList.add(participantCommand)
        }
        participantCommandList
    }

    private List<ParticipantCommand> buildParticipantCommand(Facility facility, String idrottOnlineOrganisationNumber, List<Sport> membershipSports, ActivityOccasion activityOccasion, IdrottOnlineSettings settings) {
        List<ParticipantCommand> participantCommandList = new ArrayList<ParticipantCommand>()
        activityOccasion.participants.each {
            PersonsCommand personsCommand = buildPersonsCommand(facility, idrottOnlineOrganisationNumber, membershipSports, it.customer, settings)
            ParticipantCommand participantCommand = new ParticipantCommand(
                    personId: personsCommand.person.externalIdentification,
                    leader: false,
                    personsCommand: personsCommand,
                    name: it.toString()
            )
            participantCommandList.add(participantCommand)
        }
        participantCommandList
    }
}
