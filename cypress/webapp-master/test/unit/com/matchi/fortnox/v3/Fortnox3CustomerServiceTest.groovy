package com.matchi.fortnox.v3

import com.matchi.Facility
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.web.ControllerUnitTestMixin
import com.matchi.FacilityProperty

/**
 * @author Michael Astreiko
 */
@TestFor(Fortnox3CustomerService)
//Using ControllerUnitTestMixin to enable JSON converters
@TestMixin(ControllerUnitTestMixin)
@Mock([FacilityProperty, Facility])
class Fortnox3CustomerServiceTest extends Fortnox3CommonTest {
    void setUp() {
        super.setUp()
        service.fortnox3Service = new Fortnox3Service()
        service.fortnox3Service.permitsPerSecond = 3.0d
    }

    void testIsFortnoxEnabledForFacility() {
        assert service.isFortnoxEnabledForFacility(facility)
    }

    void testList() {
        def customers = service.list(facility)

        assert customers
        assert customers.size() > 50
    }

    void testSetAndGet() {
        def name = "Test Customer"
        FortnoxCustomer customer = new FortnoxCustomer(Name: name)
        assert !customer.CustomerNumber

        //Saving customer
        customer = service.set(facility, customer)

        assert customer.CustomerNumber
        assert customer.Currency
        assert name == customer.Name

        //Getting customer
        customer = service.get(facility, customer.customerNumber)

        assert customer.CustomerNumber
        assert customer.Currency
        assert name == customer.Name
    }

    void testUpdateWithNullValues() {
        def name = "Test Customer"
        def city = "Stockholm"
        FortnoxCustomer customer = new FortnoxCustomer(Name: name, City: city)
        assert !customer.CustomerNumber

        //Saving customer
        customer = service.set(facility, customer)

        assert customer.CustomerNumber
        assert customer.Currency
        assert name == customer.Name
        assert city == customer.City

        //Updating customer
        customer.City = null
        customer = service.set(facility, customer)

        assert customer.CustomerNumber
        assert name == customer.Name
        assert !customer.City
    }
}
