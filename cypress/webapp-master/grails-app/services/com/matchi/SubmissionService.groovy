package com.matchi

import com.matchi.activities.Activity
import com.matchi.activities.Participant
import com.matchi.activities.trainingplanner.CourseActivity
import com.matchi.async.ScheduledTask
import com.matchi.dynamicforms.Form
import com.matchi.dynamicforms.FormField
import com.matchi.dynamicforms.Submission
import com.matchi.facility.FilterCourseParticipantCommand
import com.matchi.facility.FilterSubmissionCommand
import com.matchi.membership.Membership
import grails.gorm.DetachedCriteria
import grails.transaction.Transactional
import org.hibernate.criterion.CriteriaSpecification
import org.hibernate.criterion.Projections
import org.hibernate.type.StandardBasicTypes
import org.hibernate.type.Type
import org.joda.time.LocalDate

class SubmissionService {

    static transactional = false

    def courseParticipantService
    def customerService
    def editParticipantService
    def fileArchiveService
    def scheduledTaskService
    def excelExportManager

    /**
     * Returns submissions based on facility and a filter
     * @param facility
     * @return
     */
    List<Submission> getSubmissions(Facility facility, FilterSubmissionCommand cmd = null) {
        Map params = [
            max: cmd?.max ?: null,
            offset: cmd?.offset ?: null
        ]

        return getSubmissionsUsingParams(facility, params, cmd)
    }

    /**
     * Returns submissions based on facility, only looking at processed filter.
     * Used for total count.
     * @param facility
     * @return
     */
    List<Submission> getAllSubmissions(Facility facility, FilterSubmissionCommand cmd = null) {
        Map params = [:]

        return getSubmissionsUsingParams(facility, params, cmd)
    }

    private List<Submission> getSubmissionsUsingParams(Facility facility, Map params, FilterSubmissionCommand cmd = null) {
        List<CourseActivity> courses = CourseActivity.createCriteria().list {
            eq('facility', facility)
        }

        List<Form> forms = courses.collect { CourseActivity ca ->
            return ca.form
        }

        if(forms.size() == 0) return []

        List<Long> formIds = []
        if(cmd?.courses?.size() > 0) {
            formIds = cmd.courses.collect {
                CourseActivity.get(it).form.id
            }
        }

        def dc = new DetachedCriteria(Submission).build {
            projections {
                distinct("id")
            }
            customer {
                eq("facility", facility)
            }
        }

        def pickupSubmissions = dc.build {
            values {
                eq("fieldType", FormField.Type.TEXT_CHECKBOX_PICKUP.name())
            }
        }
        def allergiesSubmissions = dc.build {
            values {
                eq("fieldType", FormField.Type.TEXT_CHECKBOX_ALLERGIES.name())
            }
        }

        def ids = Submission.withCriteria {
            projections {
                distinct("id")
            }

            inList('form', forms)
            inList('status', cmd?.processed ? [Submission.Status.ACCEPTED, Submission.Status.DISCARDED] : [Submission.Status.WAITING])

            createAlias('customer', 'c')
            createAlias('form', 'f')
            createAlias('f.course', 'crs', CriteriaSpecification.LEFT_JOIN)

            isNull("c.deleted")
            eq("c.archived", false)

            if(cmd?.q) {
                or {
                    ilike("c.email", "%${cmd.q}%")
                    ilike("c.firstname", "%${cmd.q}%")
                    ilike("c.lastname", "%${cmd.q}%")
                    ilike("c.companyname", "%${cmd.q}%")
                    ilike("c.telephone", "%${cmd.q}%")
                    ilike("c.cellphone", "%${cmd.q}%")
                    ilike("c.contact", "%${cmd.q}%")
                    ilike("c.notes", "%${cmd.q}%")
                    ilike("c.invoiceAddress1", "%${cmd.q}%")
                    ilike("c.invoiceContact", "%${cmd.q}%")
                    sqlRestriction("number like ?", ["%${cmd.q}%" as String])
                    sqlRestriction("concat(firstname,' ',lastname) like ?", ["%${cmd.q}%" as String])
                }
            }

            if (cmd?.courses && formIds.size() > 0) {
                inList("f.id", formIds)
            }

            if (cmd?.genders) {
                inList("c.type", cmd?.genders)
            }

            if(cmd?.processed && cmd?.submissionStatus) {
                inList("status", cmd?.submissionStatus)
            }

            if (cmd?.pickup != null || cmd?.allergies != null) {
                createAlias("values", "sv")
                or {
                    if (cmd.pickup != null) {
                        if (cmd.pickup) {
                            and {
                                eq("sv.fieldType", FormField.Type.TEXT_CHECKBOX_PICKUP.name())
                                isNotNull("sv.value")
                            }
                        } else {
                            not {
                                inList("id", pickupSubmissions.list() ?: [-1L])
                            }
                        }
                    }
                    if (cmd.allergies != null) {
                        if (cmd.allergies) {
                            and {
                                eq("sv.fieldType", FormField.Type.TEXT_CHECKBOX_ALLERGIES.name())
                                isNotNull("sv.value")
                            }
                        } else {
                            not {
                                inList("id", allergiesSubmissions.list() ?: [-1L])
                            }
                        }
                    }
                }
            }

            if (cmd?.memberStatuses && cmd?.memberStatuses.size() == 1) {
                createAlias("c.memberships", "m", CriteriaSpecification.LEFT_JOIN)
                def today = new LocalDate()
                if (cmd.memberStatuses.contains(FilterSubmissionCommand.MemberStatus.MEMBER)) {
                    and {
                        le("m.startDate", today)
                        ge("m.gracePeriodEndDate", today)
                    }
                } else {
                    not {
                        inList("c.id", customerService.getMembersIds(facility))
                    }
                }
            }
        }

        Submission.createCriteria().list(params) {
            if (cmd?.sort?.startsWith("c.")) {
                createAlias('customer', 'c')
            } else if (cmd?.sort?.startsWith("crs.")) {
                createAlias('form', 'f')
                createAlias('f.course', 'crs', CriteriaSpecification.LEFT_JOIN)
            }

            inList("id", ids ?: [-1L])

            if(cmd?.sort && cmd?.order) {
                order(cmd?.sort, cmd?.order)
            }
        }
    }

    /**
     * Returns submission that already exists for a customer and course
     * Prioritizes WAITING submission
     * @param customer
     * @param course
     * @return
     */
    Submission findExistingActivitySubmission(Customer customer, Activity activity) {
        Submission waitingSubmission = Submission.findByCustomerAndFormAndStatus(customer, activity?.form, Submission.Status.WAITING)

        if(waitingSubmission) return waitingSubmission

        return Submission.findByCustomerAndForm(customer, activity?.form)
    }

    /**
     * Accepts a
     * @param submissionsToCourses
     * @return
     */
    @Transactional
    def acceptSubmissionsToCourse(Map<Long, Long> submissionsToCourses) {
        submissionsToCourses.each { Long submissionId, Long courseId ->
            Submission submission = Submission.get(submissionId)
            CourseActivity course = CourseActivity.get(courseId)
            Submission finalSubmission
            Submission previousSubmission = this.findExistingActivitySubmission(submission.customer,
                    course.form.getActivity())

            //the submission is to the same course, accept it
            if(submission.form == course.form) {
                submission.status = Submission.Status.ACCEPTED
                submission.save(flush:true)
                finalSubmission = submission
            } else { //the submission is changed to a different course
                if(previousSubmission) { //there's already a waiting submission for the new course, accept it and remove the old submission
                    previousSubmission.status = Submission.Status.ACCEPTED
                    finalSubmission = previousSubmission
                    submission.delete(flush:true)
                } else {// no previous submission, copy the current submission into the new course, accept it and remove the old submission
                    finalSubmission = editParticipantService.copySubmission(submission, course)
                    finalSubmission.status = Submission.Status.ACCEPTED
                    //cleanup the participant before removal
                    def participant = Participant.findByCustomerAndSubmission(submission.customer, submission)
                    participant?.delete(flush:true)
                    submission.delete(flush:true)
                }
            }

            if(course.isAlreadyParticipant(submission.customer.id)) {
                Participant participant = Participant.findByCustomerAndActivity(finalSubmission.customer, course)
                participant.submission = finalSubmission
                participant.save()
            } else {
                Participant participant = new Participant(customer: finalSubmission.customer, activity: course, submission: finalSubmission)
                participant.save()
            }
        }
    }

    /**
     * Exports submissions to Excel file
     */
    void exportSubmissions(List<Long> submissionIds, Facility facility, String taskName) {
        scheduledTaskService.scheduleTask(taskName,
                facility.id, facility) { taskId ->
            Submission.withNewSession {
                List<Submission> submissions = Submission.createCriteria().listDistinct {
                    createAlias('customer', 'c')

                    inList("id", submissionIds)

                // Sorting to maintain exact same order as the submission ids
                // Complexity log(O^3) which is not optimal...  :-)
                // Still making the calculation in the grails app, rather than making log(O) fetchings from database
                }.sort { Submission s1, Submission s2 ->
                    if(s1.id == s2.id) return 0
                    return submissionIds.indexOf(s1.id) > submissionIds.indexOf(s2.id) ? 1 : -1
                }

                def exportFile = File.createTempFile("submission_export-", ".xls")

                exportFile.withOutputStream { out ->
                    excelExportManager.exportSubmissions(submissions, out)
                }

                def task = ScheduledTask.get(taskId)
                task.resultFileName = "submission_export-${new Date().format(DateUtil.DEFAULT_DATE_FORMAT)}.xls"
                task.resultFilePath = fileArchiveService.storeExportedFile(exportFile)
                task.save(flush: true)

                exportFile.delete()
            }
        }
    }
}
