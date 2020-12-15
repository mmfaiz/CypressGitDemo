package com.matchi

import grails.test.GrailsUnitTestCase
import org.joda.time.DateTime

class TicketTests extends GrailsUnitTestCase {
    protected void setUp() {
        super.setUp()
        mockDomain(Ticket)
    }

    protected void tearDown() {
        super.tearDown()
    }

    void testIsValidWhenNotExpired() {
        Ticket ticket = new Ticket(expires: new DateTime().plusDays(1).toDate())
        assertTrue(ticket.isValid())
    }

    void testIsNotValidWhenExpired() {
        Ticket ticket = new Ticket(expires: new DateTime().minusHours(1).toDate())
        assertFalse(ticket.isValid())
    }

    void testIsNotValidWhenNotExpiredButConsumed() {
        Ticket ticket = new Ticket(expires: new DateTime().minusHours(1).toDate(), consumed: new Date())
        assertFalse(ticket.isValid())
    }

    void testTicketKeyMustBeUnique() {
        new Ticket(key: "test", expires: new Date()).save()
        assertFalse(new Ticket(key: "test", expires: new Date()).validate())
    }
}
