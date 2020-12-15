package com.matchi

import grails.test.GrailsUnitTestCase
import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.domain.DomainClassUnitTestMixin
import org.joda.time.DateTime
import org.junit.Before

import static com.matchi.TestUtils.*

@TestFor(TicketService)
@TestMixin(DomainClassUnitTestMixin)
class TicketServiceTests {

    def ticketService
    def testUser
    def testUser2
    def expiresInDays = 2

    @Before
    void setUp() {
        ticketService = new TicketService(grailsApplication: grailsApplication)

        testUser = this.createUser("test@test.com")
        testUser2 = this.createUser("test2@test.com")
        mockDomain(User, [testUser])
        mockDomain(Ticket)
        mockDomain(ResetPasswordTicket, [])
        mockDomain(BookingCancelTicket)
        mockDomain(CustomerDisableMessagesTicket)
        mockDomain(Customer)
        mockDomain(Municipality)
        mockDomain(Region)
        mockDomain(Facility)

        config.ticket.resetPassword.expiresInDays = expiresInDays
    }

    void testPasswordTicketIsCreated() {
        ResetPasswordTicket ticket = ticketService.createResetPasswordTicket(testUser)
        assertNotNull(ticket)
    }

    void testPasswordTicketHasUserSet() {
        ResetPasswordTicket ticket = ticketService.createResetPasswordTicket(testUser)
        assertNotNull(ticket.user)
    }

    void testPasswordTicketHasExpiredDate() {
        ResetPasswordTicket ticket = ticketService.createResetPasswordTicket(testUser)
        assertNotNull(ticket.expires)
    }

    void testPasswordTicketHasExpiredDateSetToConfigValue() {
        def dateBeforeExpired = new DateTime().plusDays(expiresInDays).minusMinutes(10)
        ResetPasswordTicket ticket = ticketService.createResetPasswordTicket(testUser)
        assertTrue(dateBeforeExpired.isBefore(ticket.expires.time))
    }

    void testPasswordTicketHasExpiredDateSetToConfigValueAfter() {
        def dateAfterExpired = new DateTime().plusDays(expiresInDays).plusMinutes(10)
        ResetPasswordTicket ticket = ticketService.createResetPasswordTicket(testUser)
        assertTrue(dateAfterExpired.isAfter(ticket.expires.time))
    }

    void testPasswordTicketHasGeneratedKey() {
        ResetPasswordTicket ticket = ticketService.createResetPasswordTicket(testUser)
        assertNotNull(ticket.key)
    }

    void testPasswordTicketHasGeneratedKeyOfReasonableLength() {
        ResetPasswordTicket ticket = ticketService.createResetPasswordTicket(testUser)
        assertTrue(ticket.key.length() > 20)
    }

    void testPasswordTicketIsSaved() {
        ResetPasswordTicket ticket = ticketService.createResetPasswordTicket(testUser)
        assertNotNull(ticket.id)
    }

    void testExpectIllegalArgumentExpceptionIfUserIsNull() {
        try {
            ticketService.createResetPasswordTicket(null)
        } catch (IllegalArgumentException e) {
            return
        }
        fail("IllegalArguementException should be thrown if user is null")
    }

    void testExpectIllegalStateExpceptionIfExpiresConfigVarsIsNotSet() {
        config.ticket.resetPassword.expiresInDays = null
        try {
            ticketService.createResetPasswordTicket(testUser)
        } catch (IllegalStateException e) {
            return
        }
        fail("IllegalStateException should be thrown if expires config var is not set")
    }

    void testExpectIllegalStateExpceptionIfExpiresConfigVarsIsNotParsable() {
        config.ticket.resetPassword.expiresInDays = "strangevalue"
        try {
            ticketService.createResetPasswordTicket(testUser)
        } catch (IllegalStateException e) {
            return
        }
        fail("IllegalStateException should be thrown if expires config var is not set")
    }

    void testExpectIllegalStateExpceptionIfExpiresConfigVarsIsZero() {
        config.ticket.resetPassword.expiresInDays = 0
        try {
            ticketService.createResetPasswordTicket(testUser)
        } catch (IllegalStateException e) {
            return
        }
        fail("IllegalStateException should be thrown if expires config var is not set")
    }

    void testUserCanHaveMultipleResetpasswordTickets() {
        ticketService.createResetPasswordTicket(testUser)
        ticketService.createResetPasswordTicket(testUser)
        assertEquals(2, ResetPasswordTicket.count())
    }

    void testPreviousTicketsMarkedConsumedIfNewIsGenerated() {
        ticketService.createResetPasswordTicket(testUser)
        ticketService.createResetPasswordTicket(testUser)

        assertNotNull(ResetPasswordTicket.findByConsumedIsNotNull())
    }

    void testUseKeyReturnsNullIfKeyNotFound() {
        assertNull(ticketService.useResetPasswordTicket("non_existing_key"))
    }

    void testGeneratedTicketCanBeUsedOnce() {
        def ticket = ticketService.createResetPasswordTicket(testUser)
        assertNotNull(ticketService.useResetPasswordTicket(ticket.key))
    }

    void testGeneratedTicketCannotBeUsedTwice() {
        def ticket = ticketService.createResetPasswordTicket(testUser)

        ticketService.useResetPasswordTicket(ticket.key)
        assertNull(ticketService.useResetPasswordTicket(ticket.key))
    }

    void testExpiredTicketCannotBeUsed() {
        def key = "secret_key"
        // expired ticket
        new ResetPasswordTicket(user: testUser,
                expires: new DateTime().minusDays(1).toDate(), key: key).save()

        assertNull(ticketService.useResetPasswordTicket(key))
    }

    void testGeneratedTicketValid() {
        def ticket = ticketService.createResetPasswordTicket(testUser)
        assertTrue(ticketService.isTicketValid(ticket.key))
    }

    void testGeneratedTicketNotValidIfNotExists() {
        assertFalse(ticketService.isTicketValid("notexists"))
    }

    void testGeneratedTicketIsNotValidIfExpired() {
        def key = "secret_key"
        // expired ticket
        new ResetPasswordTicket(user: testUser,
                expires: new DateTime().minusDays(1).toDate(), key: key).save()

        assertFalse(ticketService.isTicketValid(key))
    }

    void testGenerateRandomKey() {
        def keys = []
        (1..1000).each { i ->
            def key = ticketService.generateKey()
            if (keys.contains(key)) {
                fail("Ticket service generated same key twice")
            }
            keys << key
        }
    }

    void testCreateBookingCancelTicket() {
        def booking = new Booking(customer: new Customer())
        booking.id = 1L
        booking.customer.id = 2L

        def ticket = ticketService.createBookingCancelTicket(booking)

        assert ticket
        assert booking.id == ticket.bookingId
        assert booking.customer.id == ticket.bookingCustomerId
        assert ticket.expires > new Date()
        assert ticket.key
    }

    void testConsumeBookingCancelTicket() {
        def ticket = createBookingCancelTicket(1L, 2L)
        ticketService.consumeBookingCancelTicket(ticket)
        assert ticket.consumed
    }

    void testCreateCustomerDisableMessagesTicket() {
        def customer = createCustomer()

        def ticket = ticketService.createCustomerDisableMessagesTicket(customer)

        assert ticket
        assert customer == ticket.customer
        assert ticket.expires > (new Date() + 27)
        assert ticket.key
    }

    void testConsumeCustomerDisableMessagesTicket() {
        def ticket = createCustomerDisableMessagesTicket()
        ticketService.consumeCustomerDisableMessagesTicket(ticket)
        assert ticket.consumed
    }

    private User createUser(def email) {
        return new User(email: email, password: "test")
    }
}
