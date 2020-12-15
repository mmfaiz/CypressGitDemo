package com.matchi.activities

import com.matchi.Customer
import com.matchi.Facility
import com.matchi.Municipality
import com.matchi.Region
import com.matchi.User
import com.matchi.activities.trainingplanner.CourseActivity
import com.matchi.dynamicforms.Form
import grails.test.mixin.Mock
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import org.joda.time.LocalDate

import static com.matchi.TestUtils.*

@TestMixin(GrailsUnitTestMixin)
@Mock([Region, Municipality, Facility, Customer, CourseActivity, User, Form, Participant])
class CourseActivityTests {

    void testIsAlreadyParticipant() {
        Facility facility = createFacility()
        Customer customer = createCustomer(facility)
        CourseActivity courseActivity = createCourse(facility)
        Participant participant = createCourseParticipant(customer, courseActivity)
        courseActivity.addToParticipants(participant)

        assert courseActivity.isAlreadyParticipant(customer.id)
    }

    void testIsAlreadyParticipantFalse() {
        Facility facility = createFacility()
        Customer customer = createCustomer(facility)
        Customer customer2 = createCustomer(facility)
        CourseActivity courseActivity = createCourse(facility)
        Participant participant = createCourseParticipant(customer, courseActivity)
        courseActivity.addToParticipants(participant)

        assert !courseActivity.isAlreadyParticipant(customer2.id)
    }

    void testFunctionalityOfLocalDate() {
        final LocalDate localDateFromDate = new LocalDate(new Date())
        final LocalDate nowLocalDate = new LocalDate()

        assert localDateFromDate.equals(nowLocalDate)
    }

    void testIsArchived() {
        Facility facility = createFacility()
        CourseActivity courseActivity = createCourse(facility, new Date() - 2, new Date() - 1)

        assert courseActivity.isArchived()
    }

    void testIsArchivedFalse() {
        Facility facility = createFacility()
        CourseActivity courseActivity = createCourse(facility)

        assert !courseActivity.isArchived()
    }

}
