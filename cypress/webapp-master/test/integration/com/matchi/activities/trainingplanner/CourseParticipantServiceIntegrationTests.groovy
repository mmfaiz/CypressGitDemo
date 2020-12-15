package com.matchi.activities.trainingplanner

import com.matchi.Customer
import com.matchi.Facility
import com.matchi.activities.Participant
import com.matchi.activities.Participation
import com.matchi.facility.FilterCourseParticipantCommand

import static com.matchi.TestUtils.*

/**
 * @author Sergei Shushkevich
 */
class CourseParticipantServiceIntegrationTests extends GroovyTestCase {

    def courseParticipantService

    void testFindParticipants() {
        def facility1 = createFacility()
        def season1 = createSeason(facility1, new Date().clearTime(), (new Date() + 10).clearTime())
        def season2 = createSeason(facility1, (new Date() + 11).clearTime(), (new Date() + 20).clearTime())
        def course1 = createCourse(facility1, season1.startTime, season1.endTime, createForm(facility1))
        def course2 = createCourse(facility1, season2.startTime, season2.endTime, createForm(facility1))
        def customer1 = createCustomer(facility1, "john.doe@local.net", "John", "Doe")
        def customer2 = createCustomer(facility1, "jane.doe@local.net", "Jane", "Doe", Customer.CustomerType.FEMALE)
        def customer3 = createCustomer(facility1, "paul.smith@local.net", "Paul", "Smith")
        def cp1 = createCourseParticipant(customer1, course1)
        def cp2 = createCourseParticipant(customer2, course2)
        def cp3 = createCourseParticipant(customer3, course1)
        def cp4 = createCourseParticipant(customer3, course2, Participant.Status.CANCELLED)
        def facility2 = createFacility()
        createCourseParticipant(createCustomer(facility2), createCourse(facility2))

        def result = courseParticipantService.findParticipants(facility1, new FilterCourseParticipantCommand())
        assert 4 == result.size()
        assert [cp1, cp2, cp3, cp4].every { result.contains(it) }

        result = courseParticipantService.findParticipants(facility1,
                new FilterCourseParticipantCommand(q: "doe"))
        assert 2 == result.size()
        assert [cp1, cp2].every { result.contains(it) }

        result = courseParticipantService.findParticipants(facility1,
                new FilterCourseParticipantCommand(seasons: [season1.id]))
        assert 2 == result.size()
        assert [cp1, cp3].every { result.contains(it) }

        result = courseParticipantService.findParticipants(facility1,
                new FilterCourseParticipantCommand(courses: [course2.id]))
        assert 2 == result.size()
        assert [cp2, cp4].every { result.contains(it) }

        result = courseParticipantService.findParticipants(facility1,
                new FilterCourseParticipantCommand(genders: [Customer.CustomerType.FEMALE]))
        assert 1 == result.size()
        assert cp2 == result[0]

        result = courseParticipantService.findParticipants(facility1,
                new FilterCourseParticipantCommand(statuses: [Participant.Status.CANCELLED]))
        assert 1 == result.size()
        assert cp4 == result[0]

        result = courseParticipantService.findParticipants(facility1,
                new FilterCourseParticipantCommand(seasons: [season1.id, season2.id],
                        courses: [course1.id, course2.id], statuses: Participant.Status.values()))
        assert 4 == result.size()
        assert [cp1, cp2, cp3, cp4].every { result.contains(it) }
    }

    void testAddCustomerAsParticipantToCourse() {
        Facility facility = createFacility()
        CourseActivity courseActivity = createCourse(facility)
        Customer customer = createCustomer(facility)

        Participant participant = new Participant(customer: customer, activity: courseActivity)
        participant.save(flush: true)

        courseActivity.refresh()
        Set participants = courseActivity.getParticipants()

        assert participants.size() == 1
        assert participants[0].customer == customer
        assert participants[0].status == Participant.Status.RESERVED
    }
}
