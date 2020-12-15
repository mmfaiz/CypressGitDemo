package com.matchi.facility

import com.matchi.Facility
import com.matchi.Municipality
import com.matchi.Region
import com.matchi.SecurityService
import com.matchi.coupon.Coupon
import com.matchi.facility.offers.FacilityOfferController
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.junit.Before
import org.junit.Test

import static com.matchi.TestUtils.createFacility

@TestFor(FacilityOfferController)
@Mock([Region, Municipality, Facility, Coupon])
class FacilityCouponControllerTests {

    def facility

    @Before
    void setUp() {
        facility = createFacility()
        new Coupon(name: "123", nrOfTickets: 1, facility: facility).save(flush: true, failOnError: true)
        new Coupon(name: "123", nrOfTickets: 1, facility: facility).save(flush: true, failOnError: true)
    }

    @Test
    void testPutToArchive() {
        def securityServiceControl = mockFor(SecurityService)
        securityServiceControl.demand.getUserFacility { -> facility }
        controller.securityService = securityServiceControl.createMock()
        assert !Coupon.get(1)?.archived
        controller.params.id = 1

        controller.putToArchive("Coupon")

        assert Coupon.get(1)?.archived
        assert !Coupon.get(2)?.archived
        securityServiceControl.verify()
    }
}
