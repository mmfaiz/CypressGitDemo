package com.matchi.facility

import com.matchi.GenericController
import com.matchi.User
import com.matchi.activities.trainingplanner.CourseActivity
import com.matchi.dynamicforms.FormField
import com.matchi.dynamicforms.Submission
import grails.validation.Validateable

class FacilityCourseSubmissionFlowController extends GenericController {

    static scope = "prototype"

    def activityService
    def submissionService

    def acceptFlow = {
        entry {
            action { FilterSubmissionCommand cmd ->
                User user = getCurrentUser()
                def facility = getCurrentUser().facility
                flow.submissionIds = params.list('submissionIds')
                flow.returnUrl = params.returnUrl

                List<Submission> submissions = []
                if (params.allselected) {
                    submissions = submissionService.getAllSubmissions(facility, cmd)
                    flow.submissionIds = submissions.collect { it.id }
                } else if (params.submissionIds) {
                    submissions = params.list('submissionIds').collect { String id ->
                        return Submission.get(Long.parseLong(id))
                    }
                } else {
                    flow.returnUrl += addParam(flow.returnUrl, "error",
                            message(code: 'facilityCourseSubmission.index.noneSelected'))
                    return error()
                }

                flow.submissions = submissions.collect { Submission submission ->
                    return [id          : submission.id,
                            customerName: submission.customer.fullName(),
                            customerId  : submission.customer.id,
                            courseName  : submission.form.getActivity().name,
                            courseId    : submission.form.getActivity().id]
                }

                flow.coursesToSelect = activityService.getCurrentAndUpcomingActivities(
                        CourseActivity, user.getFacility(), FormField.Type.PERSONAL_INFORMATION
                ).collect { CourseActivity ca ->
                    return [id: ca.id, name: ca.name]
                }

                flow.persistenceContext.clear()
                success()
            }
            on("success").to "setCourses"
            on("error").to "finish"
        }
        setCourses {
            on("cancel").to "finish"
            on("next").to "assignCourses"
        }
        assignCourses {
            action { CourseAssignmentCommand cmd ->
                StringBuilder sb = new StringBuilder()
                int i = 0
                if (!cmd.hasErrors()) {
                    flow.submissions.each {
                        it.courseId = cmd.coursePerSubmission[it.id as String]
                        CourseActivity course = CourseActivity.get(it.courseId)
                        if (course.name.equals(it.courseName)) i++
                        if (course?.form?.maxSubmissions && (course?.participants?.size() + i) > course?.form?.maxSubmissions) {
                            if (!sb.contains(course.name)) {
                                sb.append(message(code: "submission.accept.course.error", args: [(course?.name ?: course?.form?.name), course.form?.maxSubmissions]))
                            }
                        } else {
                            it.courseName = course.name
                            it.isAlreadyParticipant = course.isAlreadyParticipant(it.customerId as Long)
                        }
                    }
                }

                if (sb.size() >= 1 ) {
                    flow.error = sb.toString()
                    error()
                } else {
                    flow.persistenceContext.clear()
                    success()
                }
            }
            on("error").to "setCourses"
            on("success").to "confirm"
        }
        confirm {
            on("cancel").to "finish"
            on("next").to "execute"
        }
        execute {
            action {

                Map<Long, Long> submissionsToCourses = flow.submissions.collectEntries {
                    [(it.id): it.courseId as Long]
                }

                submissionService.acceptSubmissionsToCourse(submissionsToCourses)
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

    def denyFlow = {
        entry {
            action { FilterSubmissionCommand cmd ->
                User user = getCurrentUser()
                flow.returnUrl = params.returnUrl

                List<Submission> submissions
                if (params.allselected) {
                    submissions = submissionService.getAllSubmissions(user.facility, cmd)
                } else if (params.submissionIds) {
                    submissions = params.list('submissionIds').collect { String id ->
                        return Submission.get(Long.parseLong(id))
                    }
                } else {
                    flow.returnUrl += addParam(flow.returnUrl, "error",
                            message(code: 'facilityCourseSubmission.index.noneSelected'))
                    return error()
                }

                flow.submissions = submissions.collect { Submission submission ->
                    return [id          : submission.id,
                            customerName: submission.customer.fullName(),
                            customerId  : submission.customer.id,
                            courseName  : submission.form.getActivity().name,
                            courseId    : submission.form.getActivity().id]
                }

                flow.coursesToSelect = activityService.getCurrentAndUpcomingActivities(
                        CourseActivity, user.getFacility(), FormField.Type.PERSONAL_INFORMATION
                ).collect { CourseActivity ca ->
                    return [id: ca.id, name: ca.name]
                }

                flow.persistenceContext.clear()
                success()
            }
            on("error").to "finish"
            on("success").to "confirm"
        }
        confirm {
            on("cancel").to "finish"
            on("next").to "execute"
        }
        execute {
            action {
                List<Submission> submissions = flow.submissions

                submissions.each { submission ->
                    Submission sub = Submission.get(submission.id)
                    sub.status = Submission.Status.DISCARDED
                    sub.save()
                }
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
}


@Validateable(nullable = true)
class CourseAssignmentCommand {
    Map<String, String> coursePerSubmission

    static constraints = {
        coursePerSubmission(nullable: false)
    }
}