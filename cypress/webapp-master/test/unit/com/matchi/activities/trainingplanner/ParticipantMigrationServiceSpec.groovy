package com.matchi.activities.trainingplanner

import com.matchi.Customer
import com.matchi.Facility
import com.matchi.Municipality
import com.matchi.Region
import com.matchi.User
import com.matchi.activities.ActivityOccasion
import com.matchi.activities.Participant
import com.matchi.dynamicforms.Form
import com.matchi.dynamicforms.Submission
import grails.test.mixin.*
import static com.matchi.TestUtils.*
import org.junit.*

@TestFor(ParticipantMigrationService)
@Mock([Region, Municipality, Facility, Customer, CourseActivity, User, Form, Participant, Submission, ActivityOccasion])
class ParticipantMigrationServiceSpec {

    void testNoSubmissionNoOccasions() {
        Participant participant = createCourseParticipant()
        participant.status = Participant.Status.QUEUED

        assert !service.isRemovable(participant)
        assert service.isReservable(participant)
        assert !service.isActivateable(participant)
    }

    void testNoSubmissionEmptyOccasionList() {
        Participant participant = createCourseParticipant()
        participant.status = Participant.Status.QUEUED
        participant.occasions = [].toSet()

        assert !service.isRemovable(participant)
        assert service.isReservable(participant)
        assert !service.isActivateable(participant)
    }

    void testHasSubmissionAndOccasions() {
        Facility facility = createFacility()
        Customer customer = createCustomer(facility)
        Form form = createForm(facility)
        Submission submission = createSubmission(customer, form)
        CourseActivity courseActivity = createCourse(facility, new Date(), new Date() + 10, form)
        Participant participant = createCourseParticipant(customer, courseActivity)
        participant.status = Participant.Status.QUEUED

        ActivityOccasion activityOccasion = createActivityOccasion(courseActivity)
        activityOccasion.addToParticipants(participant)
        activityOccasion.save(flush: true, failOnError: true)

        assert !service.isRemovable(participant)
        assert !service.isReservable(participant)
        assert service.isActivateable(participant)
    }

    void testNoSubmissionHasOccasions() {
        Facility facility = createFacility()
        Customer customer = createCustomer(facility)
        Form form = createForm(facility)
        CourseActivity courseActivity = createCourse(facility, new Date(), new Date() + 10, form)
        Participant participant = createCourseParticipant(customer, courseActivity)
        participant.status = Participant.Status.QUEUED

        ActivityOccasion activityOccasion = createActivityOccasion(courseActivity)
        activityOccasion.addToParticipants(participant)
        activityOccasion.save(flush: true, failOnError: true)

        assert !service.isRemovable(participant)
        assert !service.isReservable(participant)
        assert service.isActivateable(participant)
    }

    void testIsRemovableTrue() {
        Facility facility = createFacility()
        Customer customer = createCustomer(facility)
        Form form = createForm(facility)
        Submission submission = createSubmission(customer, form)
        CourseActivity courseActivity = createCourse(facility, new Date(), new Date() + 10, form)
        Participant participant = createCourseParticipant(customer, courseActivity)
        participant.submission = submission
        participant.status = Participant.Status.QUEUED
        participant.save(flush: true, failOnError: true)

        assert service.isRemovable(participant)
        assert !service.isReservable(participant)
        assert !service.isActivateable(participant)
    }
}
