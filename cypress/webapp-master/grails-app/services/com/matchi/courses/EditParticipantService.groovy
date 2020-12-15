package com.matchi.courses

import com.matchi.Customer
import com.matchi.activities.Participant
import com.matchi.activities.trainingplanner.CourseActivity
import com.matchi.dynamicforms.FormField
import com.matchi.dynamicforms.Submission
import com.matchi.dynamicforms.SubmissionValue
import grails.transaction.Transactional
/**
 * Service for different actions that can be done on participants
 */
class EditParticipantService {

    static transactional = false

    def courseParticipantService

    final static String COPY_PARTICIPANTS_TITLE = 'copyParticipants'
    final static String MOVE_PARTICIPANTS_TITLE = 'moveParticipants'
    final static String ADD_PARTICIPANTS_TITLE = 'addParticipants'

    /**
     *
     * @param customerIds
     * @param courseId
     * @return
     */
    List<Participant> getParticipantsFromCustomersAndCourse(List<Customer> customers, CourseActivity course) {
        return customers.collect { Customer customer ->
            return customer.courseParticipants?.find { Participant p ->
                return p.activity.id == course.id
            }
        }.findAll {
            it != null
        }
    }

    /**
     * Adds a list of customers to a course
     * @param customers
     * @param course
     */
    @Transactional
    void addCustomersToCourse(List<Customer> customers, CourseActivity course) {
        customers.each { Customer customer ->
            addCustomerToCourse(customer, course)
        }
    }

    /**
     * Adds specific customer to course
     * @param customer
     * @param course
     */
    void addCustomerToCourse(Customer customer, CourseActivity course) {
        Participant participant = new Participant(customer: customer, activity: course)
        participant.save()
    }

    /**
     * Copies a list of participants together with submissions to specific course
     * @param participants
     * @param targetCourse
     * @param copyStatus
     * @return
     */
    @Transactional
    List<Participant> copyParticipantsWithSubmissions(List<Participant> participants, CourseActivity targetCourse, boolean copyStatus = false) {
        participants.collect { Participant participant ->
            return copyParticipantWithSubmission(participant, targetCourse, copyStatus)
        }
    }

    Submission copySubmission(Submission originalSubmission, CourseActivity targetCourse, Boolean moved = false) {
        Submission newSubmission = new Submission()
        newSubmission.form = targetCourse.form
        newSubmission.customer = originalSubmission.customer
        newSubmission.submissionIssuer = originalSubmission.submissionIssuer
        newSubmission.status = originalSubmission.status
        if(moved) newSubmission.order = originalSubmission.order

        newSubmission.save(failOnError: true)

        List<FormField> newFormFields = FormField.findAllByForm(targetCourse.form)

        originalSubmission.values.asList().each { SubmissionValue sv ->

            /**
             * We need to check for a corresponding form field when copying
             */
            FormField correspondingField = newFormFields.find { FormField formField ->
                return formField.type.equals(sv.fieldType)
            }

            if(correspondingField != null) {
                SubmissionValue newValue = new SubmissionValue()

                newValue.label = sv.label
                newValue.input = sv.input
                newValue.inputGroup = sv.inputGroup
                newValue.value = sv.value
                newValue.valueIndex = sv.valueIndex
                newValue.fieldId = correspondingField.id
                newValue.fieldType = sv.fieldType
                newValue.submission = newSubmission

                newSubmission.addToValues(newValue)
                newValue.save(failOnError: true)
            }
        }

        return newSubmission
    }

    /**
     * Copies a participant with its submission
     * @param participant
     * @param targetCourse
     */
    Participant copyParticipantWithSubmission(Participant participant, CourseActivity targetCourse, boolean copyStatus = false) {
        Participant newParticipant = new Participant(customer: participant.customer, activity: targetCourse)
        Submission originalSubmission = participant.submission

        if(copyStatus) {
            newParticipant.status = participant.status
        }

        if(originalSubmission) {
            Submission newSubmission = copySubmission(originalSubmission, targetCourse, false)
            newParticipant.submission = newSubmission
        }

        newParticipant.save(failOnError: true)
        return newParticipant
    }

    @Transactional
    void moveParticipants(List<Participant> participants, CourseActivity course) {
        participants.each { Participant participant ->
            moveParticipant(participant, course)
        }
    }

    /**
     * Moves participant and submission if any
     * @param participant
     * @param course
     */
    void moveParticipant(Participant participant, CourseActivity course) {
        Submission submission = participant.submission
        Participant newParticipant = new Participant(customer: participant.customer, activity: course)

        if(submission) {
            newParticipant.submission = copySubmission(submission, course, true)
            submission.delete()
        }

        newParticipant.save()
        participant.remove()
    }
}