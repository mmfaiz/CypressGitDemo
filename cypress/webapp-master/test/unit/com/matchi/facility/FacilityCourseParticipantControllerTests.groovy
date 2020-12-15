package com.matchi.facility

import com.matchi.ActivityService
import com.matchi.Facility
import com.matchi.Group
import com.matchi.Municipality
import com.matchi.Region
import com.matchi.Season
import com.matchi.User
import com.matchi.activities.trainingplanner.CourseActivity
import com.matchi.activities.trainingplanner.CourseParticipantService
import com.matchi.SecurityService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor

import static com.matchi.TestUtils.*

/**
 * @author Sergei Shushkevich
 */
@TestFor(FacilityCourseParticipantController)
@Mock([CourseActivity, Facility, Group, Municipality, Region, Season])
class FacilityCourseParticipantControllerTests {

    void testIndex() {
        def facility = createFacility()
        def securityServiceControl = mockSecurityService(facility)
        def courseParticipantServiceControl = mockFor(CourseParticipantService)
        courseParticipantServiceControl.demand.findParticipantsWithOccasions { f, ftr -> [participants: [], totalCount: 0] }
        controller.courseParticipantService = courseParticipantServiceControl.createMock()
        def activityServiceControl = mockFor(ActivityService)
        controller.activityService = activityServiceControl.createMock()

        def model = controller.index(new FilterCourseParticipantCommand())

        assert model.containsKey("participants")
        assert model.containsKey("totalCount")
        assert model.containsKey("seasons")
        assert model.containsKey("courses")
        assert model.containsKey("filter")
        securityServiceControl.verify()
        courseParticipantServiceControl.verify()
        activityServiceControl.verify()
    }

    private mockSecurityService(facility = null) {
        def serviceControl = mockFor(SecurityService)
        serviceControl.demand.getUserFacility { -> facility ?: createFacility() }
        controller.securityService = serviceControl.createMock()
        serviceControl
    }
}
