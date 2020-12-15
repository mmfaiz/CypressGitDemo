package com.matchi.facility

import com.matchi.*
import com.matchi.activities.Participant
import com.matchi.activities.trainingplanner.CourseActivity
import com.matchi.async.ScheduledTask
import com.matchi.dynamicforms.FormField
import grails.validation.Validateable
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsParameterMap

class FacilityCourseParticipantController extends GenericController {

    def activityService
    def courseParticipantService
    def excelExportManager
    def notificationService
    def scheduledTaskService
    def editParticipantService
    def customerService
    def submissionService

    def index(FilterCourseParticipantCommand filter) {
        def facility = getUserFacility()

        def model = [filter: filter, facility: facility,
                facilityGroups: Group.findAllByFacility(facility, [sort: "name"]),
                seasons: Season.findAllByFacility(facility, [sort: "startTime", order: "desc"]),
                courses: CourseActivity.findAllByFacility(facility, [sort: "name", order: "asc"]).sort{it.isArchived()}]
        model.putAll(courseParticipantService.findParticipantsWithOccasions(facility, filter))

        if (model.courses) {
            model.pickupCourse = model.courses.find {
                it.form.fields.find { ff ->
                    ff.type == FormField.Type.TEXT_CHECKBOX_PICKUP.name() && ff.isActive
                }
            }
            model.allergyCourse = model.courses.find {
                it.form.fields.find { ff ->
                    ff.type == FormField.Type.TEXT_CHECKBOX_ALLERGIES.name() && ff.isActive
                }
            }
        }

        return model
    }

    def remove(FilterCourseParticipantCommand filter) {
        def facility = getUserFacility()

        def participantIds
        if (filter.allselected) {
            participantIds = courseParticipantService.findParticipants(facility, filter)*.id
        } else if(params.participantId) {
            participantIds = params.list("participantId").collect { Long.parseLong(it) }
        } else {
            flash.error = message(code: 'facilityCourseParticipant.index.noneSelected')
            redirect(action: "index", params: params)
            return
        }

        def participants = Participant.createCriteria().listDistinct { inList("id", participantIds) }

        participants.each {
            it.remove()
        }

        if(params.returnUrl) {
            redirect(url: params.returnUrl)
        } else {
            flash.info = message(code: 'courseParticipant.remove.action.success')
            redirect(action: "index", params: params)
        }
    }

    def changeStatus(FilterCourseParticipantCommand filter) {
        def facility = getUserFacility()

        def participantIds
        if (filter.allselected) {
            participantIds = courseParticipantService.findParticipants(facility, filter)*.id
        } else if(params.participantId) {
            participantIds = params.list("participantId").collect { Long.parseLong(it) }
        } else {
            flash.error = message(code: 'facilityCourseParticipant.index.noneSelected')
            redirect(action: "index", params: params)
            return
        }

        def participants = Participant.createCriteria().listDistinct { inList("id", participantIds) }

        participants.each {
            it.status = params.status
            it.save()
        }

        if (params.returnUrl) {
            redirect(url: params.returnUrl)
        } else {
            flash.message = message(code: 'courseParticipant.status.action.success')
            redirect(action: "index", params: params)
        }
    }

    def customerAction(FilterCourseParticipantCommand filter) {
        def customerIds = getSelectedParticipants(filter).collect { it.customer.id }.unique()

        if (customerIds) {
            session[CUSTOMER_IDS_KEY] = customerIds

            redirect(controller: params.targetController, action: params.targetAction,
                    params: [exportType: params.exportType, returnUrl: params.returnUrl, originTitle: 'courseParticipant.label'])
        }
    }

    def exportSubmissions(FilterCourseParticipantCommand filter) {
        def facility = getUserFacility()

        def participantIds
        if (filter.allselected) {
            participantIds = courseParticipantService.findParticipants(facility, filter)*.id
        } else if(params.participantId) {
            participantIds = params.list("participantId").collect { Long.parseLong(it) }
        } else {
            flash.error = message(code: 'facilityCourseParticipant.index.noneSelected')
            redirect(action: "index", params: params)
            return
        }

        List<Long> submissionIds = []
        participantIds.each { Long id ->
            Participant p = Participant.get(id)
            if(p.submission) submissionIds.push(p.submission.id)
        }

        if(submissionIds.size() > 0) {
            submissionService.exportSubmissions(submissionIds, facility, message(code: 'scheduledTask.exportSubmissions.taskName'))
        }

        if(params.returnUrl) {
            redirect(url: params.returnUrl)
        } else {
            redirect(action: "index", params: params)
        }
    }

    private List getSelectedParticipants(FilterCourseParticipantCommand filter) {
        def participantIds
        if (filter.allselected) {
            participantIds = courseParticipantService.findParticipants(getUserFacility(), filter)*.id
        } else if(params.participantId) {
            participantIds = params.list("participantId").collect { Long.parseLong(it) }
        } else {
            flash.error = message(code: 'facilityCourseParticipant.index.noneSelected')
            redirect(action: "index", params: params)
        }

        participantIds ? Participant.findAllByIdInList(participantIds) : null
    }
}

@Validateable(nullable = true)
class FilterCourseParticipantCommand {

    String q
    List<Long> seasons = []
    List<Long> courses = []
    List<Long> groups = []
    List<Customer.CustomerType> genders = []
    List<Participant.Status> statuses = []
    List<Integer> occasions = []
    List<Integer> wantedOccasions = []
    List<MemberStatus> memberStatuses = []
    Boolean pickup
    Boolean allergies
    Boolean hasSubmission

    String order = "asc"
    String sort = "c.firstname"
    int max = 50
    int offset = 0

    boolean allselected = false

    /**
     * To enable this command to be built later than directly as a parameter
     * @param params
     * @return
     */
    static FilterCourseParticipantCommand buildFromParameters(GrailsParameterMap params) {
        return new FilterCourseParticipantCommand(
                q: params.q,
                seasons: params.list("seasons").collect {Long.valueOf(it)},
                courses: params.list("courses").collect {Long.valueOf(it)},
                groups: params.list("groups").collect {Long.valueOf(it)},
                genders: params.list("genders").collect { Enum.valueOf(Customer.CustomerType, it) },
                statuses: params.list("statuses").collect { Enum.valueOf(Participant.Status, it) },
                occasions: params.list("occasions").collect {Integer.valueOf(it)},
                wantedOccasions: params.list("wantedOccasions").collect {Integer.valueOf(it)},
                memberStatuses: params.list("memberStatuses").collect { Enum.valueOf(MemberStatus, it) },
                allselected: params.boolean("allselected", false)
        )
    }

    String toString() {
        return "[q: ${q}, seasons: ${seasons}, courses: ${courses}, groups: ${groups}, genders: ${genders}, statuses: ${statuses}, occasions: ${occasions}´, wantedOccasions: ${wantedOccasions}´]"
    }

    static enum MemberStatus {
        MEMBER, NO_MEMBER

        static list() {
            return [MEMBER, NO_MEMBER]
        }
    }
}
