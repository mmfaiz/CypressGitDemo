package com.matchi.facility

import com.matchi.Customer
import com.matchi.Facility
import com.matchi.GenericController
import com.matchi.User
import com.matchi.activities.ActivityOccasion
import com.matchi.activities.Participant
import com.matchi.activities.trainingplanner.CourseActivity
import com.matchi.viewhelpers.ParticipantViewHelper
import com.matchi.viewhelpers.ParticipantViewHelperFactory
import grails.validation.Validateable

class FacilityCourseParticipantFlowController extends GenericController {

    static scope = "prototype"

    def courseParticipantService
    def notificationService
    def customerService

    def sendScheduleFlow = {
        entry {
            action { FilterCourseParticipantCommand cmd ->
                log.info("Flow action entry")
                flow.returnUrl = params.returnUrl
                log.debug("returnUrl: ${flow.returnUrl}")

                flow.user = getCurrentUser()
                flow.facility = flow.user.facility

                def participants
                if (cmd.allselected) {
                    participants = courseParticipantService.findParticipants(flow.facility, cmd)
                } else if (params.participantId) {
                    participants = params.list("participantId").collect {
                        Participant.findById(it as Long, [fetch: [customer: "join"]])
                    }
                } else {
                    flow.returnUrl += addParam(flow.returnUrl, "error",
                            message(code: "facilityCourseParticipant.index.noneSelected"))
                    return error()
                }


                def participantInfo = []
                def guardianInfo = []

                flow.participantIds = []
                flow.cantRecieve = []

                participants.each { Participant p ->
                    if (p.customer.isEmailReceivable()) {
                        flow.participantIds << p.id
                        participantInfo << p.customer.getEmailCustomerInfo()

                        if (p.customer.hasGuardianEmails()) {
                            guardianInfo.addAll(p.customer.getGuardianMessageInfo())
                        }
                    } else {
                        flow.cantRecieve << p.id
                    }
                }

                flow.nMails = participantInfo.collect { it.email }.unique().size()
                flow.nMailsAll = (participantInfo + guardianInfo).collect { it.email }.unique().size()

                flow.participantInfo = participantInfo.unique()
                flow.participantInfoAll = (participantInfo + guardianInfo).unique()

                flow.persistenceContext.clear()
            }
            on("success").to "createMessage"
            on("error").to "finish"
        }
        createMessage {
            log.info("Flow view create message")
            on("cancel").to "finish"
            on("next").to "sendNotification"
        }
        sendNotification {
            action {
                log.info("Flow action send")

                Facility facility = getCurrentUser().facility

                String messageText = params.message
                String fromMail = params.fromMail
                boolean includeGuardian = params.boolean('includeGuardian', false)
                boolean includeTrainers = params.boolean('includeTrainers', false)
                boolean includeParticipants = params.boolean('includeParticipants', false)


                List<Long> ids = flow.participantIds

                String taskName = message(code: "facilityCourseParticipation.sendSchedule.taskName") as String

                notificationService.executeSending(ids, taskName, facility) { List<Long> participantIds ->
                    Participant.withTransaction {
                        List<Participant> participantsList = Participant.createCriteria().list {
                            inList("id", participantIds)
                        }
                        participantsList.each { p ->
                            List<ActivityOccasion> occasions = ActivityOccasion.withCriteria {
                                participants {
                                    eq("id", p.id)
                                }
                                join "activity"
                                join "court"
                            }
                            if (occasions) {
                                notificationService.sendCourseOccasionInfo(p.customer, occasions, messageText, fromMail, includeGuardian, includeTrainers, includeParticipants)
                            }
                        }
                    }
                }

                flow.returnUrl += addParam(flow.returnUrl, "message",
                        message(code: "facilityCourseParticipation.sendSchedule.success"))

                flow.persistenceContext.clear()
                success()
            }
            on("success").to "finish"
            on(Exception).to "finish"
        }
        finish {
            redirect(url: flow.returnUrl)
        }
    }

    def editParticipantFlow = {
        entry {
            action {
                log.info("Flow action entry")

                User user = getCurrentUser()
                ParticipantViewHelper participantViewHelper = buildParticipantViewHelper(params.actionTitle)
                participantViewHelper.setup(user, flow, params)

                try {
                    participantViewHelper.entryAction(params, user, flow)
                } catch (IllegalArgumentException e) {
                    flow.returnUrl += addParam(flow.returnUrl, "error", message(code: e.getMessage()))
                    return error()
                }

                flow.persistenceContext.clear()
                success()
            }
            on("success").to "chooseCourse"
            on("error").to "finish"
        }
        chooseCourse {
            on("cancel").to "finish"
            on("next").to "assignCourse"
        }
        assignCourse {
            action { AssignCourseCommand cmd ->
                flow.error = null
                CourseActivity chosenCourse = CourseActivity.findById(cmd.toCourse)


                if (chosenCourse?.form?.maxSubmissions && (chosenCourse?.participants?.size() + flow.customers?.size()) > chosenCourse?.form?.maxSubmissions) {
                    flow.error = message(code: "submission.accept.course.error", args: [(chosenCourse?.name ?: chosenCourse?.form?.name), chosenCourse.form?.maxSubmissions])
                }
                if (flow.error) {
                    error()
                } else {
                    ParticipantViewHelper participantViewHelper = buildParticipantViewHelper(flow.actionTitle)
                    participantViewHelper.checkAlreadyParticipants(flow, chosenCourse)

                    flow.chosenCourse = [id: chosenCourse.id, name: chosenCourse.name]
                    flow.persistenceContext.clear()
                    success()
                }
            }
            on("error").to "chooseCourse"
            on("success").to "confirm"
        }
        confirm {
            on("cancel").to "finish"
            on("next").to "execute"
        }
        execute {
            action {
                ParticipantViewHelper participantViewHelper = buildParticipantViewHelper(flow.actionTitle)
                CourseActivity course = CourseActivity.get(flow.chosenCourse.id)

                flow.nTransferred = participantViewHelper.execute(flow, course)
                flow.persistenceContext.clear()
                success()
            }
            on("error").to "finish"
            on("success").to "complete"
        }
        complete {
            on("next").to "finish"
        }
        finish {
            redirect(url: flow.returnUrl)
        }
    }

    private ParticipantViewHelper buildParticipantViewHelper(String actionTitle) {
        return ParticipantViewHelperFactory.build(actionTitle)
    }
}

@Validateable(nullable = true)
class AssignCourseCommand {
    Long toCourse

    static constraints = {
        toCourse(nullable: false)
    }
}
