package com.matchi.facility

import com.matchi.Facility
import com.matchi.Group
import com.matchi.GroupService
import com.matchi.User
import com.matchi.SecurityService
import grails.plugin.springsecurity.SpringSecurityService
import grails.test.GrailsMock
import grails.test.mixin.TestFor
import org.junit.Before
import org.junit.Test

/**
 * See the API for {@link grails.test.mixin.web.ControllerUnitTestMixin} for usage instructions
 */
@TestFor(FacilityGroupController)
class FacilityGroupControllerTests {

    User user, userNoFacility
    Facility facility, facility2
    GrailsMock mockSecurityService
    GrailsMock mockGroupService

    @Before
    public void setUp() {
        mockSecurityService = mockFor(SecurityService)
        controller.securityService = mockSecurityService.createMock()

        mockGroupService = mockFor(GroupService)
        controller.groupService = mockGroupService.createMock()

        facility = new Facility(id: 1, name: "Test", shortname: "test", lat: "1", lng: "1", openingHours: "07:00", closingHours: "10:00")
        facility.id = 1
        facility2 = new Facility(id: 2, name: "Test", shortname: "test", lat: "1", lng: "1", openingHours: "07:00", closingHours: "10:00")
        facility2.id = 2
        user = new User(id: 1, facility: facility, email: 'user@mail.com', firstname: "Test", lastname: "User")
        userNoFacility = new User(id: 2, facility: null, email: 'user@mail.com', firstname: "Test", lastname: "User")

        mockSecurityService.demand.getUserFacility(1..2) {  -> return facility }
    }

    void testIndexRenders() {
        mockGroupService.demand.getFacilityGroups() { def facility -> return [new Group()]}

        controller.index()
        assert view == "/facilityGroup/index"
    }

    @Test(expected = IllegalStateException)
    void testIndexNoFacilityRendersError() {
        mockSecurityService.demand.getUserFacility(1..2) {  -> return null }
        controller.index()
    }

    void testEditRenders() {
        mockGroupService.demand.getGroup() { id ->
            def group = new Group()
            group.id = 1
            group.facility = facility
            return group}

        controller.params.id = 1
        controller.edit()

        assert model.group?.id == 1
        assert view == "/facilityGroup/edit"
    }

    @Test(expected = com.matchi.SecurityException)
    void testEditThrowsSecurityExceptionOnWrongFacility() {
        def mockSpringSecurityService = mockFor(SpringSecurityService)
        mockSpringSecurityService.demand.getCurrentUser {  -> return user }
        controller.springSecurityService = mockSpringSecurityService.createMock()
        mockSecurityService.demand.getUserFacility(1..3) {  -> return facility }
        mockGroupService.demand.getGroup() { id ->
            def group = new Group()
            group.id = 1
            group.facility = facility2
            return group}

        controller.params.id = 1
        controller.edit()
    }

    void testRemoveGroup() {

    }
}
