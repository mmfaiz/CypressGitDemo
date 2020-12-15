package com.matchi.api

import com.matchi.*
import com.matchi.api.CancelBookingCommand
import com.matchi.orders.Order
import grails.test.GrailsMock
import grails.test.mixin.Mock
import grails.test.mixin.TestFor

import static com.matchi.TestUtils.*
/**
 * See the API for {@link grails.test.mixin.web.ControllerUnitTestMixin} for usage instructions
 */
@TestFor(BookingResourceController)
@Mock([Region, Municipality, Facility, User, Customer, Booking, Sport, Slot, Court, Order, Player])
class BookingResourceControllerTests {

    void testCancelBookingCorrectUser() {
        GrailsMock mockBookingService = mockFor(BookingService)
        controller.bookingService = mockBookingService.createMock()

        Facility facility = createFacility()
        User user = createUser()

        def springSecurityService = [getCurrentUser: {  ->
            return user
        }]
        controller.springSecurityService = springSecurityService


        Customer customer = createCustomer(facility)
        customer.user = user
        customer.save(flush: true)

        Booking booking = createBooking(customer)
        Long bookingId = booking.id

        mockBookingService.demand.cancelBooking(1) { a,b,c,d,e,f ->
            assert a.id == bookingId
        }

        params.id = bookingId
        controller.cancel(new CancelBookingCommand())

        mockBookingService.verify()
    }

    void testCancelBookingIncorrectUser() {
        GrailsMock mockBookingService = mockFor(BookingService)
        controller.bookingService = mockBookingService.createMock()

        Facility facility = createFacility()
        User user = createUser()
        User sessionUser = createUser('anothermail@matchi.se')

        def springSecurityService = [getCurrentUser: {  ->
            return sessionUser
        }]
        controller.springSecurityService = springSecurityService


        Customer customer = createCustomer(facility)
        customer.user = user
        customer.save(flush: true)

        Booking booking = createBooking(customer)
        Long bookingId = booking.id

        mockBookingService.demand.cancelBooking(0) { a,b,c,d,e,f ->
            assert a.id == bookingId
        }

        params.id = bookingId
        controller.cancel(new CancelBookingCommand())

        mockBookingService.verify()
    }

    void testCancelBookingNoUser() {
        GrailsMock mockBookingService = mockFor(BookingService)
        controller.bookingService = mockBookingService.createMock()

        Facility facility = createFacility()
        User sessionUser = createUser('anothermail@matchi.se')

        def springSecurityService = [getCurrentUser: {  ->
            return sessionUser
        }]
        controller.springSecurityService = springSecurityService


        Customer customer = createCustomer(facility)

        Booking booking = createBooking(customer)
        Long bookingId = booking.id

        mockBookingService.demand.cancelBooking(0) { a,b,c,d,e,f ->
            assert a.id == bookingId
        }

        params.id = bookingId
        controller.cancel(new CancelBookingCommand())

        mockBookingService.verify()
    }

    void testNotifyPlayer_onePlayer() {
        GrailsMock mockNotificationService = mockFor(NotificationService)
        controller.notificationService = mockNotificationService.createMock()

        mockNotificationService.demand.sendNewBookingNotification(1) { booking, payment ->
            //nop
        }

        Booking booking = new Booking()
        booking.customer = createCustomer()

        controller.notifyPlayers(booking)
    }

    void testNotifyPlayer_severalPlayers() {
        GrailsMock mockNotificationService = mockFor(NotificationService)
        controller.notificationService = mockNotificationService.createMock()

        mockNotificationService.demand.sendNewBookingNotification(1) { booking, payment ->
            assert booking.customer.email == "first@player.se"
        }

        mockNotificationService.demand.sendNewBookingPlayerNotification(1) { booking, player ->
            assert player.email == "second@player.se"
        }

        Facility facility = createFacility()
        Booking booking = new Booking()
        Player playerAndCustomer = createPlayer(facility, "first@player.se", booking)
        booking.customer = playerAndCustomer.customer

        booking.addToPlayers(createPlayer(facility, "first@player.se", booking))
        booking.addToPlayers(createPlayer(facility, "second@player.se", booking))

        controller.notifyPlayers(booking)
    }
}
