package com.matchi

import grails.plugin.springsecurity.SpringSecurityService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.junit.Before
import org.junit.Test

@TestFor(GenericController)
@Mock([Facility, User])
class GenericControllerTests {

    def facility, facility2, user

    @Before
    void setUp() {
        def mockSpringSecurityService = mockFor(SpringSecurityService)
        controller.springSecurityService = mockSpringSecurityService.createMock()

        facility = new Facility(id: 1, name: "Test", shortname: "test", lat: "1", lng: "1", openingHours: "07:00", closingHours: "10:00")
        facility2 = new Facility(id: 2, name: "Test 2", shortname: "test", lat: "1", lng: "1", openingHours: "07:00", closingHours: "10:00")

        facility.id = 1
        facility2.id = 2
        user = new User(id: 1, facility: facility, email: 'user@mail.com', firstname: "Test", lastname: "User")

        mockSpringSecurityService.demand.getCurrentUser(1..2) {  -> return user }

    }

    @Test(expected = SecurityException)
    void testAssertFacilityAccessThrowsException() {
        def securityServiceControl = mockSecurityService(facility)
        Court court = new Court(facility: facility2)
        controller.assertFacilityAccessTo(court)
    }

    @Test
    void testAssertFacilityAccess() {
        def securityServiceControl = mockSecurityService(facility)
        Court court = new Court(facility: facility)
        controller.assertFacilityAccessTo(court)
        securityServiceControl.verify()
    }

    @Test
    void testAssertFacilityAccessOnNullObjects() {
        controller.assertFacilityAccessTo(null)
    }

    @Test(expected = SecurityException)
    void testAssertFacilityAccessNotFacilityResourceThrowsException() {
        def securityServiceControl = mockSecurityService(facility)
        def o = new Booking()
        controller.assertFacilityAccessTo(o)
        securityServiceControl.verify()
    }

    void testRaiseFacilityExceptionNoFacility() {
        try {
            controller.raiseFacilityAccessException(null)
        } catch(SecurityException se) {
            return
        }

        fail("Should throw exception")
    }

    private mockSecurityService(facility) {
        def serviceControl = mockFor(SecurityService)
        serviceControl.demand.getUserFacility { -> facility }
        controller.securityService = serviceControl.createMock()
        serviceControl
    }
}
