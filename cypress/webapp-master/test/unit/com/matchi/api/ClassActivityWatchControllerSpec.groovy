package com.matchi.api

import com.matchi.*
import com.matchi.activities.ActivityOccasion
import com.matchi.activities.ClassActivity
import com.matchi.watch.ClassActivityWatch
import com.matchi.watch.ObjectWatchNotificationService
import grails.plugin.springsecurity.SpringSecurityService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.joda.time.LocalDate
import org.joda.time.LocalTime
import spock.lang.Specification

import static com.matchi.TestUtils.*

/**
 * @author Sergei Shushkevich
 */
@TestFor(ClassActivityWatchController)
@Mock([ActivityOccasion, ClassActivity, ClassActivityWatch, Facility, Municipality, Region, User])
class ClassActivityWatchControllerSpec extends Specification {

    def customerService = Mock(CustomerService)
    def objectWatchNotificationService = Mock(ObjectWatchNotificationService)
    def springSecurityService = Mock(SpringSecurityService)
    def userService = Mock(UserService)

    def setup() {
        controller.customerService = customerService
        controller.objectWatchNotificationService = objectWatchNotificationService
        controller.springSecurityService = springSecurityService
        controller.userService = userService
    }

    void testList() {
        def user = createUser()
        def facility1 = createFacility()
        def facility2 = createFacility()
        def activity1 = new ClassActivity(name: "a1", facility: facility1).save(failOnError: true)
        def activity2 = new ClassActivity(name: "a2", facility: facility2).save(failOnError: true)
        new ClassActivityWatch(user: user, facility: facility1, classActivity: activity1,
                fromDate: new Date() - 1, toDate: new Date() - 1).save(failOnError: true)
        def w1 = new ClassActivityWatch(user: user, facility: facility1, classActivity: activity1,
                fromDate: new Date() + 1, toDate: new Date() + 1).save(failOnError: true)
        def w2 = new ClassActivityWatch(user: user, facility: facility2, classActivity: activity2,
                fromDate: new Date() + 1, toDate: new Date() + 1).save(failOnError: true)

        when:
        params.facility = facility1.id
        controller.list()

        then:
        1 * springSecurityService.getCurrentUser() >> user
        response.status == 200
        response.json
        response.json.size() == 1
        response.json[0].id == w1.id

        when:
        response.reset()
        params.facility = facility2.id
        controller.list()

        then:
        1 * springSecurityService.getCurrentUser() >> user
        response.json
        response.json.size() == 1
        response.json[0].id == w2.id

        when:
        response.reset()
        params.facility = null
        controller.list()

        then:
        1 * springSecurityService.getCurrentUser() >> user
        response.json
        response.json.size() == 2
        response.json.find {it.id == w1.id}
        response.json.find {it.id == w2.id}
    }

    void testConfirm() {
        def facility = createFacility()
        def activity = new ClassActivity(name: "a", facility: facility).save(failOnError: true)
        def occasion = new ActivityOccasion(activity: activity, date: LocalDate.now(),
                startTime: LocalTime.now(), endTime: LocalTime.now()).save(failOnError: true)

        when:
        controller.confirm(12345L)

        then:
        response.status == 404

        when:
        response.reset()
        controller.confirm(occasion.id)

        then:
        1 * userService.getLoggedInUser()
        response.status == 401

        when:
        response.reset()
        def model = controller.confirm(occasion.id)

        then:
        1 * userService.getLoggedInUser() >> new User()
        response.status == 200
        model.occasion == occasion
        model.user
    }

    void testAdd() {
        def user = createUser()
        def facility1 = createFacility()
        def activity1 = new ClassActivity(name: "a1", facility: facility1).save(failOnError: true)
        def cmd = new AddActivityWatchCommand(activityId: activity1.id, fromDateTime: new Date())

        when:
        cmd.validate()
        controller.add(cmd)

        then:
        1 * userService.getLoggedInUser() >> user
        1 * objectWatchNotificationService.addNotificationFor(_, _, _, _) >> new ClassActivityWatch()
        1 * customerService.getOrCreateUserCustomer(user, facility1)
        response.status == 200

        when: "user is not logged in"
        response.reset()
        controller.add(cmd)

        then: "unauthorized status is returned"
        1 * userService.getLoggedInUser() >> null
        response.status == 401

        when: "wrong data is submitted"
        response.reset()
        cmd = new AddActivityWatchCommand()
        cmd.validate()
        controller.add(cmd)

        then: "bad request status is returned"
        1 * userService.getLoggedInUser() >> user
        response.status == 400
    }

    void testRemove() {
        def user = createUser()
        def facility1 = createFacility()
        def activity1 = new ClassActivity(name: "a1", facility: facility1).save(failOnError: true)
        def w1 = new ClassActivityWatch(user: user, facility: facility1, classActivity: activity1,
                fromDate: new Date() + 1, toDate: new Date() + 1).save(failOnError: true)

        when:
        params.id = w1.id
        controller.remove()

        then:
        1 * springSecurityService.getCurrentUser() >> user
        response.status == 204
        !ClassActivityWatch.count()

        when: "wrong id is used"
        response.reset()
        params.id = 12345L
        controller.remove()

        then: "not found status is returned"
        1 * springSecurityService.getCurrentUser() >> user
        response.status == 404
    }
}