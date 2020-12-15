package com.matchi.admin

import com.matchi.*
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.junit.Before
import org.junit.Test

/**
 * See the API for {@link grails.test.mixin.web.ControllerUnitTestMixin} for usage instructions
 */
@TestFor(AdminFacilityPropertiesController)
@Mock([FacilityProperty, Customer, Facility, Municipality, Region])
class AdminFacilityPropertiesControllerTests {

    @Before
    void setUp() {
        Region region = new Region(name: '1', lat: 12, lng: 12).save(flush: true, failOnError: true)
        Municipality municipality = new Municipality(name: "1", lat: 12, lng: 12, region: region).save(flush: true, failOnError: true)
        Facility facility = new Facility(name: "1", shortname: "1", lat: 123, lng: 123, vat: 12, municipality: municipality, country: "sv", email: "facility@matchi.se").save(flush: true, failOnError: true)
        FacilityProperty.FacilityPropertyKey.values().each {
            new FacilityProperty(key: it, value: "1", facility: facility).save(flush: true, failOnError: true)
        }
        new Customer(number: "1", facility: facility).save(flush: true, failOnError: true)
        new Customer(number: "2", facility: facility).save(flush: true, failOnError: true)
    }

    @Test
    void testSaveBackhandsmashPropertyWthEmptyEntry() {
        params.id = "1"
        params.BACKHANDSMASH_CUSTOMER_ID = ""
        controller.update(1)
        Facility facility = Facility.get(1)
        assert facility
        assert !Facility.get(1).getFacilityProperty(FacilityProperty.FacilityPropertyKey.BACKHANDSMASH_CUSTOMER_ID)
    }

    @Test
    void testSaveBackhandsmashProperty() {
        params.id = "1"
        params.BACKHANDSMASH_CUSTOMER_ID = "1,2,3,as"
        controller.update(1)
        assert Facility.get(1).getFacilityProperty(FacilityProperty.FacilityPropertyKey.BACKHANDSMASH_CUSTOMER_ID).value == "1,2"
    }

    @Test
    void testSaveBackhandsmashPropertyWthSomeNullEntry() {
        params.id = "1"
        params.BACKHANDSMASH_CUSTOMER_ID = ",, 3, 0, 2"
        controller.update(1)
        assert Facility.get(1).getFacilityProperty(FacilityProperty.FacilityPropertyKey.BACKHANDSMASH_CUSTOMER_ID).value == "2"
    }

    @Test
    void testSaveBackhandsmashPropertyWthOneEntry() {
        params.id = "1"
        params.BACKHANDSMASH_CUSTOMER_ID = "2"
        controller.update(1)
        assert Facility.get(1).getFacilityProperty(FacilityProperty.FacilityPropertyKey.BACKHANDSMASH_CUSTOMER_ID).value == "2"
    }

    @Test
    void testSaveMultiplePlayersNumberProperty() {
        params.id = "1"
        params.MULTIPLE_PLAYERS_NUMBER = ['1': '2', '3': '4']
        controller.update(1)
        assert Facility.get(1).getFacilityProperty(
                FacilityProperty.FacilityPropertyKey.MULTIPLE_PLAYERS_NUMBER).value == "['1':'2', '3':'4']"
    }
}
