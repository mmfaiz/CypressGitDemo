package com.matchi.facility

import com.matchi.Court
import com.matchi.CourtGroup
import com.matchi.Facility
import com.matchi.Municipality
import com.matchi.RedeemService
import com.matchi.Region
import com.matchi.SecurityService
import com.matchi.Sport
import grails.test.mixin.Mock
import grails.test.mixin.TestFor

import static com.matchi.TestUtils.createFacility

/**
 * @author Sergei Shushkevich
 */
@TestFor(FacilitySlotRedeemController)
@Mock([Court, CourtGroup, Facility, Municipality, Region, Sport])
class FacilitySlotRedeemControllerTests {

    void testIndex() {
        def facility = createFacility()
        def securityService = mockSecurityService(facility)
        def redeemServiceControl = mockFor(RedeemService)
        redeemServiceControl.demand.getSlotRedeems { f -> [] }
        controller.redeemService = redeemServiceControl.createMock()

        def model = controller.index()

        assert model.containsKey("redeems")
        assert model.containsKey("filter")
        redeemServiceControl.verify()
        securityService.verify()
    }

    private mockSecurityService(facility = null) {
        def serviceControl = mockFor(SecurityService)
        serviceControl.demand.getUserFacility() { -> facility ?: createFacility() }
        controller.securityService = serviceControl.createMock()
        serviceControl
    }
}
