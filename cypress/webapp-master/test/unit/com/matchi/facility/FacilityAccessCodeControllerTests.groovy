package com.matchi.facility

import com.matchi.Facility
import com.matchi.FacilityAccessCode
import com.matchi.Municipality
import com.matchi.Region
import com.matchi.Sport
import com.matchi.User
import com.matchi.Court
import com.matchi.SecurityService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor

import static com.matchi.TestUtils.*

/**
 * @author Sergei Shushkevich
 */
@TestFor(FacilityAccessCodeController)
@Mock([Facility, Court, Sport, FacilityAccessCode, Municipality, Region])
class FacilityAccessCodeControllerTests {

    void testDelete() {
        def facility = createFacility()
        def court = createCourt()
        def securityServiceControl = mockSecurityService(facility)

        def c1 = new FacilityAccessCode(facility: facility, content: "1",
                validFrom: new Date(), validTo: new Date(), courts: [court])
                .save(failOnError: true)
        def c2 = new FacilityAccessCode(facility: facility, content: "2",
                validFrom: new Date(), validTo: new Date(), courts: [court])
                .save(failOnError: true)
        def c3 = new FacilityAccessCode(facility: facility, content: "3",
                validFrom: new Date(), validTo: new Date(), courts: [court])
                .save(failOnError: true)
        params.ids = [c1.id.toString(), c3.id.toString()]

        controller.delete()

        assert "/facility/accesscodes/index" == response.redirectedUrl
        assert 1 == FacilityAccessCode.count()
        assert c2 == FacilityAccessCode.first()
        securityServiceControl.verify()
    }

    void testDeleteUsed() {
        def facility = createFacility()
        def court = createCourt()

        def securityServiceControl = mockSecurityService(facility)
        def c1 = new FacilityAccessCode(facility: facility, content: "1",
                validFrom: new Date(), validTo: new Date() + 10, courts: [court])
                .save(failOnError: true)
        new FacilityAccessCode(facility: facility, content: "2",
                validFrom: new Date() - 20, validTo: new Date() - 10, courts: [court])
                .save(failOnError: true)
        new FacilityAccessCode(facility: facility, content: "3",
                validFrom: new Date() - 30, validTo: new Date() - 20, courts: [court])
                .save(failOnError: true)

        controller.deleteUsed()

        assert "/facility/accesscodes/index" == response.redirectedUrl
        assert 1 == FacilityAccessCode.count()
        assert c1 == FacilityAccessCode.first()
        securityServiceControl.verify()
    }

    private mockSecurityService(facility) {
        def serviceControl = mockFor(SecurityService)
        serviceControl.demand.getUserFacility { -> facility }
        controller.securityService = serviceControl.createMock()
        serviceControl
    }
}
