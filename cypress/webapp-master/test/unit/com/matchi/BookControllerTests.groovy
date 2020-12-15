package com.matchi

import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import org.junit.Before
import org.junit.Test

import static com.matchi.TestUtils.createFacility

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@TestMixin(GrailsUnitTestMixin)
@TestFor(BookController)
@Mock([Facility, Municipality, Region])
class BookControllerTests {

    def facility

    @Before
    void setUp() {
        facility = createFacility()
    }

    @Test
    void testScheduleWithoutDate() {
        controller.params << [facilityId: "1", week: '20', year: '2015']
        controller.schedule()
    }

    @Test
    void testScheduleWithoutAnyDateOrWeekParams() {
        controller.params << [facilityId: "1"]
        controller.schedule()
    }
}
