package com.matchi.viewhelpers

import com.matchi.Customer
import com.matchi.activities.Participant
import com.matchi.activities.trainingplanner.CourseActivity
import com.matchi.facility.FilterCourseParticipantCommand
import grails.util.Holders
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsParameterMap

class CopyParticipantViewHelper extends ParticipantViewHelper {

    def courseParticipantService = Holders.grailsApplication.mainContext.getBean('courseParticipantService')

    @Override
    void entryAction(GrailsParameterMap params, def user, def flow) {
        List<Long> participantIds
        FilterCourseParticipantCommand cmd = FilterCourseParticipantCommand.buildFromParameters(params)

        if (cmd.allselected) {
            participantIds = courseParticipantService.findParticipantsIds(user.facility, cmd)
        }
        else if(params.participantId) {
            participantIds = params.list("participantId")
        } else {
            throw new IllegalArgumentException("facilityCourseParticipant.index.noneSelected")
        }

        List<Customer> customers = participantIds.collect {
            Participant p = Participant.findById(it as Long, [fetch: [customer: "join", activity: "join"]])
            [name: p.customer.fullName(),
             id: p.id,
             customerId: p.customer.id,
             courseOriginName: p.activity.name]
        }

        flow.customers = customers
    }

    @Override
    int execute(def flow, CourseActivity course) {
        List<Participant> actionableParticipants = getParticipantsToChange(flow.customers)
        editParticipantService.copyParticipantsWithSubmissions(actionableParticipants, course, false)

        return actionableParticipants.size()
    }
}
