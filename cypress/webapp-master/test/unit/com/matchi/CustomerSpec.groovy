package com.matchi

import com.matchi.api.RegisterUserCommand
import grails.test.mixin.TestFor
import spock.lang.Specification
import spock.lang.Unroll

@TestFor(Customer)
class CustomerSpec extends Specification {

    @Unroll
    def "Customers date of birth to birthyear `3101#date`-> #year"() {
        given:
        Customer customer = new Customer()
        customer.setDateOfBirth(Date.parse("ddMMyyyy", "3101"+date))

        expect:
        assert customer.dateOfBirthToBirthYear() == year

        where:
        date  || year
        "1930"|| 1930
        "1929"|| null
        "1111"|| null
        "2020"|| 2020
        "2001"|| 2001
    }
}