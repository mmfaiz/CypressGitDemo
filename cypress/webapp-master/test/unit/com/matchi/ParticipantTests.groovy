package com.matchi

import com.matchi.activities.Participant
import com.matchi.activities.trainingplanner.CourseActivity
import com.matchi.dynamicforms.Form
import com.matchi.dynamicforms.Submission
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import static com.matchi.TestUtils.*

@TestFor(Participant)
@Mock([Region, Municipality, Facility, Customer, User, Form, Submission, CourseActivity])
class ParticipantTests {

    void testGetSubmission() {
        Facility facility = createFacility()
        Customer customer = createCustomer(facility)
        Form form = createForm(facility)
        Submission submission = createSubmission(customer, form)
        CourseActivity courseActivity = createCourse(facility, new Date(), new Date() + 10, form)
        Participant participant = createCourseParticipant(customer, courseActivity)
        participant.submission = submission
        participant.save(flush: true, failOnError: true)

        assert participant.getSubmission() == submission
        assert participant.submission == submission
    }

    void testGetSubmissionNull() {
        Facility facility = createFacility()
        Customer customer = createCustomer(facility)
        Form form = createForm(facility)
        Submission submission = createSubmission(customer, form)
        Participant participant = createCourseParticipant(customer)

        assert participant.getSubmission() == null
        assert participant.submission == null
    }

}
