package com.matchi

import com.matchi.activities.ActivityOccasion
import com.matchi.activities.ClassActivity
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.joda.time.DateTime
import org.joda.time.LocalDate

import static com.matchi.TestUtils.*
import static plastic.criteria.PlasticCriteria.mockCriteria

@TestFor(ScheduleTagLib)
@Mock([Facility, Region, Municipality, Customer, Slot, Court, Sport, Booking, ClassActivity, ActivityOccasion, Player])
class ScheduleTagLibTests {

    void testScheduleActivityName() {
        mockCriteria([ActivityOccasion])

        def scheduleTagLib = new ScheduleTagLib()
        def facility = createFacility()
        def booking = createBooking(createCustomer(facility))
        def activity = new ClassActivity(facility: facility, name: "name").save(flush: true)
        new ActivityOccasion(date: new LocalDate(), startTime: new DateTime(), endTime: new DateTime().plusDays(1).toDate(),
                bookings: [booking], activity: activity).save(flush: true)

        assert activity.name == scheduleTagLib.scheduleActivityName(bookingId: booking.id).toString()
        assert "" == scheduleTagLib.scheduleActivityName(bookingId: null).toString()
    }

    void testSchedulePlayers() {
        mockCriteria([Player])

        def scheduleTagLib = new ScheduleTagLib()
        def facility = createFacility()
        def customer1 = createCustomer(facility)
        def customer2 = createCustomer(facility)
        def booking = createBooking(customer1)
        booking.addPlayers([customer1, customer2, new Customer()])

        def result = render(template: "/templates/schedule/players", model: [players: booking.players.findAll { it.customer != customer1 }])
        assert result == scheduleTagLib.schedulePlayers(bookingId: booking.id, customerId: customer1.id).toString()
        assert "" == scheduleTagLib.schedulePlayers(bookingId: null, customerId: customer1.id).toString()
        assert "" == scheduleTagLib.schedulePlayers(bookingId: 123, customerId: customer1.id).toString()
    }
}
