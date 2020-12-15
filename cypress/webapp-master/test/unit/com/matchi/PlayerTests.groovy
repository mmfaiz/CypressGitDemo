package com.matchi

import grails.test.mixin.TestFor
import org.junit.Test

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@TestFor(Player)
class PlayerTests {

    @Test
    void testIsBookingCustomer() {
        Player player = domain

        String email = "sune@matchi.se"

        assert !player.isBookingCustomer(email)

        Customer customer = new Customer()
        player.customer = customer

        assert !player.isBookingCustomer(email)

        customer.email = email
        assert player.isBookingCustomer(email)

        customer.email = null
        player.email = email

        assert player.isBookingCustomer(email)

        customer.email = email
        assert player.isBookingCustomer(email)

        customer.email = "anotheremail@matchi.se"
        assert !player.isBookingCustomer(email)
    }
}
