package com.matchi

import grails.test.GrailsUnitTestCase

class ResetPasswordTicketTests extends GrailsUnitTestCase {
    protected void setUp() {
        super.setUp()
        mockDomain(ResetPasswordTicket, [])
    }

    protected void tearDown() {
        super.tearDown()
    }

    void testSimpleSave() {
        ResetPasswordTicket ticket = new ResetPasswordTicket(expires: new Date(), key: "test", user: new User())
        assertNotNull(ticket.save())
    }

    void testUserIsRequired() {
        ResetPasswordTicket ticket = new ResetPasswordTicket(expires: new Date(), key: "test", user: null)
        assertFalse(ticket.validate())
    }
}
