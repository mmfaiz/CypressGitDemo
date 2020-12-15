package com.matchi

import grails.test.mixin.TestFor

/**
 * @author Sergei Shushkevich
 */
@TestFor(CustomerDisableMessagesTicket)
class CustomerDisableMessagesTicketTests {

    void testConstraints() {
        mockForConstraintsTests(CustomerDisableMessagesTicket)

        def obj = new CustomerDisableMessagesTicket()
        assert !obj.validate()
        assert 3 == obj.errors.errorCount
        assert "nullable" == obj.errors.customer
        assert "nullable" == obj.errors.key
        assert "nullable" == obj.errors.expires


        obj = new CustomerDisableMessagesTicket(customer: new Customer(), key: "abc", expires: new Date())
        assert obj.validate()
    }
}
