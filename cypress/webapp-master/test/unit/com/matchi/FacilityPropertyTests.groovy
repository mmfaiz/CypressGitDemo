package com.matchi

import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.junit.Test

/**
 * See the API for {@link grails.test.mixin.domain.DomainClassUnitTestMixin} for usage instructions
 */
@TestFor(FacilityProperty)
@Mock([FacilityProperty, Customer, Facility, Municipality, Region])
class FacilityPropertyTests {

    @Test
    void testUnknownKeyDoesNotValidate() {
        domain.key = "test"
        domain.value = "0"
        domain.facility = new Facility()

        assert !domain.validate()
    }

    @Test
    void testKnownKeyDoesNotValidate() {
        domain.key = FacilityProperty.FacilityPropertyKey.INVOICE_NUMBER_START.toString()
        domain.value = "0"
        domain.facility = new Facility()

        assert domain.validate()
    }

    @Test
    void testGetBackhandsmashCustomers() {
        initDomainsForTest()
        FacilityProperty property = FacilityProperty.findByKey(FacilityProperty.FacilityPropertyKey.BACKHANDSMASH_CUSTOMER_ID)
        assert property
        assert property.backhandsmashCustomers.size() == 2
    }

    private void initDomainsForTest() {
        Region region = new Region(name: '1', lat: 12, lng: 12).save(flush: true,failOnError: true)
        Municipality municipality = new Municipality(name: "1", lat: 12, lng: 12, region:region).save(flush: true, failOnError: true)
        Facility facility = new Facility(name: "1", shortname: "1", lat: 123, lng: 123, vat: 12, municipality: municipality, country: "sv", email: "facility@matchi.se").save(flush: true, failOnError: true)
        new FacilityProperty(key: FacilityProperty.FacilityPropertyKey.BACKHANDSMASH_CUSTOMER_ID, value: "1,2", facility: facility).save(flush: true, failOnError: true)
        new Customer(number: "1", facility: facility).save(flush: true, failOnError: true)
        new Customer(number: "2", facility: facility).save(flush: true, failOnError: true)
    }

}
