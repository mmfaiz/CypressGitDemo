package com.matchi

import grails.plugin.springsecurity.SpringSecurityService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor

import javax.servlet.http.HttpServletResponse

import static com.matchi.TestUtils.*

@TestFor(UserBookingController)
@Mock([Booking, BookingCancelTicket, Court, Customer, Facility, Municipality, Region, Slot, Sport])
class UserBookingControllerTests {

    void testShowByTicket() {
        def booking = createBooking()
        def ticket = createBookingCancelTicket(booking.id, booking.customer.id)
        params.ticket = ticket.key

        def model = controller.showByTicket()

        assert ticket == model.ticket
        assert booking == model.booking
    }

    void testShowByTicket_InvalidTicketKey() {
        params.ticket = "invalid"
        controller.showByTicket()
        assert HttpServletResponse.SC_NOT_FOUND == response.status
    }

    void testShowByTicket_InvalidTicket() {
        def booking = createBooking()
        def ticket = createBookingCancelTicket(booking.id, booking.customer.id)
        ticket.consumed = new Date()
        ticket.save()
        params.ticket = ticket.key

        controller.showByTicket()

        assert HttpServletResponse.SC_NOT_FOUND == response.status
    }

    void testShowByTicket_ChangedCustomer() {
        def booking = createBooking()
        def ticket = createBookingCancelTicket(booking.id, createCustomer(booking.customer.facility).id)
        params.ticket = ticket.key

        controller.showByTicket()

        assert HttpServletResponse.SC_NOT_FOUND == response.status
    }

    void testCancelByTicket() {
        User user = new User()
        def booking = createBooking()
        def ticket = createBookingCancelTicket(booking.id, booking.customer.id)
        def bookingServiceControl = mockFor(BookingService)
        bookingServiceControl.demand.cancelBooking { b, c -> }
        controller.bookingService = bookingServiceControl.createMock()
        def ticketServiceControl = mockFor(TicketService)
        ticketServiceControl.demand.consumeBookingCancelTicket { t -> }
        controller.ticketService = ticketServiceControl.createMock()
        def springSecurityServiceControl = mockFor(SpringSecurityService)
        controller.springSecurityService = springSecurityServiceControl.createMock()
        springSecurityServiceControl.demand.getCurrentUser(1) { ->
            return user
        }

        params.ticket = ticket.key

        def model = controller.cancelByTicket()

        assert HttpServletResponse.SC_OK == response.status
        assert booking.slot.startTime == model.date
        assert booking.slot.court == model.court
        bookingServiceControl.verify()
        ticketServiceControl.verify()
    }

    void testCancelByTicket_InvalidTicketKey() {
        params.ticket = "invalid"
        controller.cancelByTicket()
        assert HttpServletResponse.SC_NOT_FOUND == response.status
    }

    void testCancelByTicket_InvalidTicket() {
        def booking = createBooking()
        def ticket = createBookingCancelTicket(booking.id, booking.customer.id)
        ticket.consumed = new Date()
        ticket.save()
        params.ticket = ticket.key

        controller.cancelByTicket()

        assert HttpServletResponse.SC_NOT_FOUND == response.status
    }

    void testCancelByTicket_ChangedCustomer() {
        def booking = createBooking()
        def ticket = createBookingCancelTicket(booking.id, createCustomer(booking.customer.facility).id)
        params.ticket = ticket.key

        controller.cancelByTicket()

        assert HttpServletResponse.SC_NOT_FOUND == response.status
    }
}
