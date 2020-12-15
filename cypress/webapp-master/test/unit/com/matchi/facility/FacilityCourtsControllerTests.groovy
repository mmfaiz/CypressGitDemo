package com.matchi.facility

import com.matchi.*
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.junit.Before
import spock.lang.Specification

import static com.matchi.TestUtils.*

@TestFor(FacilityCourtsController)
@Mock([Region, Municipality, Facility, Court, Sport])
class FacilityCourtsControllerTests {

    def courtService

    @Before
    void setup() {
        courtService = mockFor(CourtService)
        controller.courtService = courtService.createMock()

        courtService.demand.updateCourtInstanceWithCourtTypeAttributes{a,b -> null}
    }

    void testPutToArchive() {
        def facility = createFacility()
        def sport = createSport()
        new Court(name: "qwe", indoor: true, surface: Court.Surface.HARD, offlineOnly: false, sport: sport, facility: facility).save(failOnError: true)
        controller.securityService = [getUserFacility: { -> facility }]
        assert !Court.get(1)?.archived
        controller.params.id = 1

        def courtService = mockFor(CourtService)
        courtService.demand.updateCourt() { c -> c}
        controller.courtService =  courtService.createMock()

        controller.putToArchive()

        assert Court.get(1)?.archived
    }

    void testGetFromArchive() {
        def facility = createFacility()
        def sport = createSport()
        new Court(name: "qwe", indoor: true, surface: Court.Surface.HARD, offlineOnly: false, archived: true, sport: sport, facility: facility).save(failOnError: true)
        controller.securityService = [getUserFacility: { -> facility }]
        assert Court.get(1)?.archived
        controller.params.id = 1

        def courtService = mockFor(CourtService)
        courtService.demand.updateCourt() { c -> c}
        controller.courtService =  courtService.createMock()

        controller.getFromArchive()

        assert !Court.get(1)?.archived
    }

    void testSave() {
        def facility = createFacility()
        def sport = createSport()
        sport.id = 10
        sport.save()
        def securityService = mockSecurityService(facility)
        def courtService = mockFor(CourtService)
        courtService.demand.updateCourtInstanceWithCourtTypeAttributes() {}
        courtService.demand.createCourt() { c -> c.save()}
        controller.courtService =  courtService.createMock()

        params.listPosition = 1
        params."sport.id" = sport.id
        params.name = "new court"

        controller.save()

        assert response.redirectedUrl == "/facility/courts/index"
        assert Court.count() == 1
        assert Court.first().facility == facility
        assert Court.first().name == "new court"
        securityService.verify()
    }

    void testSaveFailedValidation() {
        def sport = createSport()
        def securityService = mockSecurityService(createFacility(), 2)
        def courtService = mockFor(CourtService)
        courtService.demand.updateCourtInstanceWithCourtTypeAttributes() {}
        courtService.demand.createCourt() { c -> c.save(flush: true)}
        controller.courtService =  courtService.createMock()

        params.listPosition = 1
        params."sport.id" = sport.id

        controller.save()

        assert view == "/facilityCourts/create"
        assert model.courtInstance
        assert model.facility
        securityService.verify()
    }

    void testUpdate() {
        def facility = createFacility()
        def court = createCourt(facility, new Sport(id: 10))
        def securityService = mockSecurityService(facility)
        def courtService = mockFor(CourtService)
        courtService.demand.updateCourtInstanceWithCourtTypeAttributes() {}
        courtService.demand.updateCourt() { c -> c}
        controller.courtService =  courtService.createMock()

        params.id = court.id
        params.name = "new court name"

        controller.update()

        assert response.redirectedUrl == "/facility/courts/index"
        assert Court.count() == 1
        assert Court.first().name == "new court name"
        securityService.verify()
    }

    void testUpdateFailedValidation() {
        def facility = createFacility()
        def court = createCourt(facility)
        def securityService = mockSecurityService(facility)
        def courtService = mockFor(CourtService)
        courtService.demand.updateCourtInstanceWithCourtTypeAttributes() {}
        courtService.demand.updateCourt() { c -> c.save(flush: true)}
        controller.courtService =  courtService.createMock()
        params.id = court.id
        params.name = ""

        controller.update()

        assert view == "/facilityCourts/edit"
        assert model.courtInstance
        securityService.verify()
    }

    private mockSecurityService(facility = null, maxCalls = 1) {
        def serviceControl = mockFor(SecurityService)
        serviceControl.demand.getUserFacility(1..maxCalls) { -> facility ?: createFacility() }
        controller.securityService = serviceControl.createMock()
        serviceControl
    }
}
