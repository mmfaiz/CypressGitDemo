package com.matchi.facility

import com.matchi.Court
import com.matchi.CourtGroup
import com.matchi.Facility
import com.matchi.Municipality
import com.matchi.Region
import com.matchi.SecurityService
import com.matchi.Sport
import grails.test.mixin.Mock
import grails.test.mixin.TestFor

import static com.matchi.TestUtils.*

/**
 * @author Sergei Shushkevich
 */
@TestFor(FacilityCourtsGroupsController)
@Mock([Court, CourtGroup, Facility, Municipality, Region, Sport])
class FacilityCourtsGroupsControllerTests {

    void testIndex() {
        def facility1 = createFacility()
        def facility2 = createFacility()
        def group1 = new CourtGroup(facility: facility1, name: "gr1", tabPosition: 1, visible: true)
                .save(failOnError: true)
        new CourtGroup(facility: facility2, name: "gr2", tabPosition: 1, visible: true).save(failOnError: true)
        def securityService = mockSecurityService(facility1)

        def model = controller.index()

        assert model.facility == facility1
        assert model.groups.size() == 1
        assert model.groups[0] == group1
        securityService.verify()
    }

    void testCreate() {
        def facility1 = createFacility()
        def securityService = mockSecurityService(facility1)
        def model = controller.create()
        assert model.courtGroupInstance
        securityService.verify()
    }

    void testSave() {
        def facility = createFacility()
        def court1 = createCourt(facility)
        def court2 = createCourt(facility)
        params.name = "test"
        params.tabPosition = 1
        params.visible = true
        params.courts = [court1.id, court2.id]
        def securityService = mockSecurityService(facility)

        controller.save()

        assert response.redirectedUrl == "/facility/courtsgroups/index"
        assert CourtGroup.count() == 1
        def cg = CourtGroup.first()
        assert cg.facility == facility
        assert cg.name == "test"
        assert cg.courts.size() == 2
        assert cg.courts.contains(court1)
        assert cg.courts.contains(court2)
        securityService.verify()
    }

    void testEdit() {
        def facility = createFacility()
        def cg = new CourtGroup(facility: facility, name: "gr1", tabPosition: 1, visible: true)
                .save(failOnError: true)
        def securityService = mockSecurityService(facility)

        def model = controller.edit(cg.id)

        assert model.courtGroupInstance == cg
        securityService.verify()
    }

    void testEditNoAccess() {
        def facility = createFacility()
        def cg = new CourtGroup(facility: facility, name: "gr1", tabPosition: 1, visible: true)
                .save(failOnError: true)
        def securityService = mockSecurityService()

        controller.edit(cg.id)

        assert response.redirectedUrl == "/facility/courtsgroups/index"
        securityService.verify()
    }

    void testUpdate() {
        def facility = createFacility()
        def court1 = createCourt(facility)
        def court2 = createCourt(facility)
        def cg = new CourtGroup(facility: facility, name: "gr1", tabPosition: 1, visible: true)
                .addToCourts(court1)
        cg.version = 0
        cg.save(failOnError: true)
        def securityService = mockSecurityService(facility)
        params.name = "new name"
        params.courts = [court2.id]

        controller.update(cg.id, cg.version)

        assert response.redirectedUrl == "/facility/courtsgroups/index"
        assert CourtGroup.count() == 1
        assert cg.name == "new name"
        assert cg.courts.size() == 1
        assert cg.courts.iterator().next() == court2
        securityService.verify()
    }

    void testUpdateNoAccess() {
        def facility = createFacility()
        def court1 = createCourt(facility)
        def court2 = createCourt(facility)
        def cg = new CourtGroup(facility: facility, name: "gr1", tabPosition: 1, visible: true)
                .addToCourts(court1)
        cg.version = 0
        cg.save(failOnError: true)
        def securityService = mockSecurityService()
        params.name = "new name"
        params.courts = [court2.id]

        controller.update(cg.id, cg.version)

        assert response.redirectedUrl == "/facility/courtsgroups/index"
        assert CourtGroup.count() == 1
        assert cg.name == "gr1"
        assert cg.courts.size() == 1
        assert cg.courts.iterator().next() == court1
        securityService.verify()
    }

    void testUpdateWrongVersion() {
        def facility = createFacility()
        def court1 = createCourt(facility)
        def court2 = createCourt(facility)
        def cg = new CourtGroup(facility: facility, name: "gr1", tabPosition: 1, visible: true)
                .addToCourts(court1)
        cg.version = 0
        cg.save(failOnError: true)
        def securityService = mockSecurityService(facility)
        params.name = "new name"
        params.courts = [court2.id]

        controller.update(cg.id, cg.version - 1)

        assert view == "/facilityCourtsGroups/edit"
        securityService.verify()
    }

    void testDelete() {
        def facility = createFacility()
        def court = createCourt(facility)
        def cg = new CourtGroup(facility: facility, name: "gr1", tabPosition: 1, visible: true)
                .addToCourts(court).save(failOnError: true)
        def securityService = mockSecurityService(facility)

        controller.delete(cg.id)

        assert response.redirectedUrl == "/facility/courtsgroups/index"
        assert !CourtGroup.count()
        assert Court.count() == 1
        securityService.verify()
    }

    void testDeleteNoAccess() {
        def facility = createFacility()
        def court = createCourt(facility)
        def cg = new CourtGroup(facility: facility, name: "gr1", tabPosition: 1, visible: true)
                .addToCourts(court).save(failOnError: true)
        def securityService = mockSecurityService()

        controller.delete(cg.id)

        assert response.redirectedUrl == "/facility/courtsgroups/index"
        assert CourtGroup.count() == 1
        assert Court.count() == 1
        securityService.verify()
    }

    void testSwapListPosition() {
        def facility = createFacility()
        def cg1 = new CourtGroup(facility: facility, name: "gr1", tabPosition: 1, visible: true)
                .save(failOnError: true)
        def cg2 = new CourtGroup(facility: facility, name: "gr2", tabPosition: 2, visible: true)
                .save(failOnError: true)
        def securityService = mockSecurityService(facility)

        controller.swapListPosition(cg1.id, cg2.id)

        assert response.status == 200
        assert cg1.tabPosition == 2
        assert cg2.tabPosition == 1
        securityService.verify()
    }

    void testSwapListPositionNoAccess() {
        def facility = createFacility()
        def cg1 = new CourtGroup(facility: facility, name: "gr1", tabPosition: 1, visible: true)
                .save(failOnError: true)
        def cg2 = new CourtGroup(facility: facility, name: "gr2", tabPosition: 2, visible: true)
                .save(failOnError: true)
        def securityService = mockSecurityService()

        controller.swapListPosition(cg1.id, cg2.id)

        assert response.status == 400
        securityService.verify()
    }

    private mockSecurityService(facility = null) {
        def serviceControl = mockFor(SecurityService)
        serviceControl.demand.getUserFacility() { -> facility ?: createFacility() }
        controller.securityService = serviceControl.createMock()
        serviceControl
    }
}
