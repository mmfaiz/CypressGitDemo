package com.matchi

import grails.test.mixin.TestFor

/**
 * @author Sergei Shushkevich
 */
@TestFor(BookingCancelTicket)
class BookingCancelTicketTests {

    void testConstraints() {
        mockForConstraintsTests(BookingCancelTicket)

        def obj = new BookingCancelTicket()
        assert !obj.validate()
        assert 4 == obj.errors.errorCount
        assert "nullable" == obj.errors.bookingId
        assert "nullable" == obj.errors.bookingCustomerId
        assert "nullable" == obj.errors.key
        assert "nullable" == obj.errors.expires


        obj = new BookingCancelTicket(bookingId: 1L, bookingCustomerId: 2L, key: "abc", expires: new Date())
        assert obj.validate()
    }
}
