package com.matchi.facility

import com.matchi.*
import com.matchi.activities.*
import com.matchi.watch.*
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.joda.time.LocalDate
import org.joda.time.LocalTime
import spock.lang.Specification

import static com.matchi.TestUtils.*

@TestFor(FacilityActivityOccasionController)
@Mock([ActivityOccasion, ClassActivity, ClassActivityWatch, Customer, Facility, Municipality, Region, User])
class FacilityActivityOccasionControllerTests extends Specification {

    def objectWatchNotificationService = Mock(ObjectWatchNotificationService)
    def securityService = Mock(SecurityService)

    def setup() {
        controller.objectWatchNotificationService = objectWatchNotificationService
        controller.securityService = securityService
    }

    void testCreateActivityWatch() {
        def user = createUser()
        def facility = createFacility()
        def customer = createCustomer(facility)
        customer.user = user
        customer.save(failOnError: true)
        def activity = new ClassActivity(name: "a", facility: facility).save(failOnError: true)
        def occasion = new ActivityOccasion(activity: activity, date: LocalDate.now(),
                startTime: LocalTime.now(), endTime: LocalTime.now()).save(failOnError: true)

        when:
        controller.createActivityWatch(12345L, 54321L)

        then:
        response.redirectedUrl == "/facility/activities/index"

        when:
        response.reset()
        controller.createActivityWatch(occasion.id, customer.id)

        then:
        2 * securityService.getUserFacility() >> facility
        1 * objectWatchNotificationService.addNotificationFor(user, activity, _, false)
        response.redirectedUrl == "/facility/activities/occasions/edit/$occasion.id"
    }

    void testRemoveActivityWatch() {
        def user = createUser()
        def facility = createFacility()
        def activity = new ClassActivity(name: "a", facility: facility).save(failOnError: true)
        def occasion = new ActivityOccasion(activity: activity, date: LocalDate.now(),
                startTime: LocalTime.now(), endTime: LocalTime.now()).save(failOnError: true)
        new ClassActivityWatch(user: user, facility: facility, classActivity: activity,
                fromDate: occasion.date.toDateTime(occasion.startTime).toDate(),
                toDate: occasion.date.toDateTime(occasion.endTime).toDate()).save(failOnError: true)

        when:
        controller.removeActivityWatch(12345L, 54321L)

        then:
        response.redirectedUrl == "/facility/activities/index"

        when:
        response.reset()
        controller.removeActivityWatch(occasion.id, user.id)

        then:
        1 * securityService.getUserFacility() >> facility
        !ClassActivityWatch.countByClassActivity(activity)
        response.redirectedUrl == "/facility/activities/occasions/edit/$occasion.id"
    }
}
