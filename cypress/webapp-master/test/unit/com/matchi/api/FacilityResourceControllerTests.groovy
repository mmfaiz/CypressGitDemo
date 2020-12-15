package com.matchi.api

import com.matchi.Facility
import com.matchi.FacilityService
import com.matchi.api.v2.FacilityResourceController
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.junit.Before

/**
 * See the API for {@link grails.test.mixin.web.ControllerUnitTestMixin} for usage instructions
 */
@TestFor(FacilityResourceController)
@Mock(Facility)
class FacilityResourceControllerTests {

    @Before
    void setUp() {

        def facilityServiceMock = mockFor(FacilityService)

        new Facility(name: "Test 1", bookable: false).save(validate: false)
        new Facility(name: "Test 2", bookable: true).save(validate: false)
        new Facility(name: "Test 3", bookable: true, active: true).save(validate: false)
        new Facility(name: "Test 4", bookable: true, active: true).save(validate: false)
        new Facility(name: "Test 5", bookable: false, active: true).save(validate: false)

        facilityServiceMock.demand.getUsersAvailableFacilities { def user ->
            return [new Facility()]
        }

        controller.facilityService = facilityServiceMock.createMock()

    }

    void testNumberOfFacilities() {
        controller.list()
        assert response.json.facilities.size() == 2
    }
}
