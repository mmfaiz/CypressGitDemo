package com.matchi.admin

import com.matchi.Facility
import com.matchi.Municipality
import com.matchi.Region
import grails.test.mixin.Mock
import grails.test.mixin.TestFor

import static com.matchi.TestUtils.createFacility

@TestFor(AdminFacilityController)
@Mock([Facility, Municipality, Region])
class AdminFacilityControllerTests {

    void testIndex() {
        def facility = createFacility()
        facility.name = "Testclub1"
        facility.save(flush: true)
        def facility2 = createFacility()
        facility2.enabled = false
        facility2.save(flush: true)
        AdminFacilityCommand cmd = new AdminFacilityCommand()
        cmd.facilityName = "Test"
        def model = controller.index(cmd)

        assert 1 == model.facilityInstanceTotal
        assert 1 == model.facilities.size()
        assert facility == model.facilities[0]
    }

    void testArchive() {
        createFacility()
        def facility2 = createFacility()
        facility2.enabled = false
        facility2.name = "Testclub1"
        facility2.save(flush: true)
        AdminFacilityCommand cmd = new AdminFacilityCommand()
        cmd.facilityName = "Test"
        controller.archivedFacilities(cmd)

        assert 1 == model.facilityInstanceTotal
        assert view == "/adminFacility/index"
        assert 1 == model.facilities.size()
        assert facility2 == model.facilities[0]
    }

}