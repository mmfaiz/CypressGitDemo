package com.matchi.facility

import com.matchi.Customer
import com.matchi.Facility
import com.matchi.GenericController
import com.matchi.activities.trainingplanner.CourseActivity
import com.matchi.dynamicforms.FormField
import com.matchi.dynamicforms.Submission
import com.matchi.dynamicforms.SubmissionValue
import grails.validation.Validateable

import javax.servlet.http.HttpServletResponse

class FacilityCourseSubmissionController extends GenericController {

    def submissionService

    static final int PAGINATION_STEP_SIZE = 50

    def index(FilterSubmissionCommand cmd) {
        Facility facility = getUserFacility()
        if(!cmd) cmd = new FilterCustomerCommand()

        List<Submission> allSubmissions = submissionService.getAllSubmissions(facility, cmd)
        List<Submission> submissions = submissionService.getSubmissions(facility, cmd)

        def model = [ facility: facility,
                      submissions: submissions,
                      totalCount: allSubmissions.size(),
                      paginationStepSize: PAGINATION_STEP_SIZE,
                      courses: CourseActivity.findAllByFacility(facility, [sort: "name", order: "asc"]).sort{it.isArchived()},
                      cmd: cmd ]

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

        model
    }

    def show(Long id) {
        def submission = Submission.byFacility(getUserFacility()).get(id)

        if (submission) {
            def values = SubmissionValue.findAllBySubmission(submission).groupBy {
                it.label
            }.sort {
                it.value[0].fieldId
            }
            return [submission: submission, submissionValues: values, course: submission.form.course]
        } else {
            response.sendError HttpServletResponse.SC_NOT_FOUND
        }
    }

    def processed(FilterSubmissionCommand cmd) {
        Facility facility = getUserFacility()
        if(!cmd) cmd = new FilterCustomerCommand()

        cmd.processed = true
        List<Submission> allSubmissions = submissionService.getAllSubmissions(facility, cmd)
        List<Submission> submissions = submissionService.getSubmissions(facility, cmd)

        def model = [ facility: facility,
                      submissions: submissions,
                      totalCount: allSubmissions.size(),
                      paginationStepSize: PAGINATION_STEP_SIZE,
                      courses: CourseActivity.findAllByFacility(facility, [sort: "name", order: "asc"]).sort{it.isArchived()},
                      cmd: cmd ]

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

        model
    }

    def discard() {
        Submission submission = Submission.get(params.id)
        assertFacilityAccessTo(submission.form)

        submission.status = Submission.Status.DISCARDED
        submission.save()

        redirect controller: "facilityCourseSubmission", action: "index"
    }

    def readdDiscarded() {
        Submission submission = Submission.get(params.id)
        assertFacilityAccessTo(submission.form)

        submission.status = Submission.Status.WAITING
        submission.save()

        redirect controller: "facilityCourseSubmission", action: "processed"
    }

    def exportSubmissions(FilterSubmissionCommand cmd) {
        List<Long> submissionIds
        Facility facility = getUserFacility()

        if (cmd.allselected) {
            submissionIds = submissionService.getAllSubmissions(facility,cmd)*.id
        } else if(params.submissionIds) {
            submissionIds = params.list('submissionIds').collect { it as Long }
        } else {
            flash.error = message(code: 'facilityCourseSubmission.index.noneSelected')
            redirectBack()
            return
        }

        if (submissionIds) {
            submissionService.exportSubmissions(submissionIds, facility, message(code: 'scheduledTask.exportSubmissions.taskName'))
        }

        redirectBack()
    }

    def customerAction(FilterSubmissionCommand cmd) {
        List<Submission> submissions
        Facility facility = getUserFacility()

        if (cmd.allselected) {
            submissions = submissionService.getAllSubmissions(facility,cmd)
        } else if(params.submissionIds) {
            submissions = Submission.findAllByIdInList(params.list('submissionIds').collect { it as Long })
        } else {
            flash.error = message(code: 'facilityCourseSubmission.index.noneSelected')
            redirectBack()
            return
        }

        List<Long> customerIds = submissions.findAll { Submission submission ->
            submission.customer
        }.collect { Submission submission -> submission.customer.id }.unique()

        if (customerIds) {
            session[CUSTOMER_IDS_KEY] = customerIds

            redirect(controller: params.targetController, action: params.targetAction,
                    params: [exportType: params.exportType, returnUrl: params.returnUrl, originTitle: 'courseSubmission.label'])
        }
    }

    /**
     * Redirect to returnUrl or back to index
     */
    void redirectBack() {
        if(params.returnUrl) {
            redirect(url: params.returnUrl)
        } else {
            redirect(action: "index", params: params)
        }
    }
}

@Validateable(nullable = true)
class FilterSubmissionCommand {
    int max = FacilityCourseSubmissionController.PAGINATION_STEP_SIZE
    int offset = 0
    String sort = "dateCreated"
    String order = "asc"
    boolean processed = false
    boolean allselected = false

    String q
    List<Long> courses = []
    List<Long> groups = []
    List<Customer.CustomerType> genders = []
    List<MemberStatus> memberStatuses = []
    Boolean pickup
    Boolean allergies
    List<Submission.Status> submissionStatus

    static constraints = {
        q(nullable: true)
        courses(nullable: true)
        genders(nullable: true)
        memberStatuses(nullable: true)
        pickup(nullable: true)
        allergies(nullable: true)
        submissionStatus(nullable: true)

        groups(nullable: true)
        max(nullable: false)
        offset(nullable: false)
        sort(nullable: false)
        order(nullable: false)
        processed(nullable: false)
        allselected(nullable: false)
    }

    static enum MemberStatus {
        MEMBER, NO_MEMBER

        static list() {
            return [MEMBER, NO_MEMBER]
        }
    }
}
