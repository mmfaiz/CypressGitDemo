package com.matchi.facility

import com.matchi.*
import com.matchi.activities.ClassActivity
import grails.test.mixin.Mock
import grails.test.mixin.TestFor

import static com.matchi.TestUtils.*

@TestFor(FacilityActivityController)
@Mock([Facility, Municipality, Region])
class FacilityActivityControllerTests {

    void testIndex() {
        def securityServiceControl = mockSecurityService()
        def activityServiceControl = mockActivityService()

        def model = controller.index()

        assert 1 == model.activities.size()
        securityServiceControl.verify()
        activityServiceControl.verify()
    }

    void testArchive() {
        def securityServiceControl = mockSecurityService()
        def activityServiceControl = mockActivityService()

        controller.archive()

        assert "/facilityActivity/index" == view
        assert !model.activities
        securityServiceControl.verify()
        activityServiceControl.verify()
    }

    private mockSecurityService() {
        def serviceControl = mockFor(SecurityService)
        serviceControl.demand.getUserFacility() { -> createFacility() }
        controller.securityService = serviceControl.createMock()
        serviceControl
    }

    private mockActivityService() {
        def serviceControl = mockFor(ActivityService)
        serviceControl.demand.getActivitiesByFacility { f, a -> a ? [] : [new ClassActivity()] }
        controller.activityService = serviceControl.createMock()
        serviceControl
    }
}
