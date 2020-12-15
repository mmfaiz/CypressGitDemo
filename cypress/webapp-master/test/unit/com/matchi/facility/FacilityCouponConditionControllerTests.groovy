package com.matchi.facility

import com.matchi.conditions.*
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.junit.Before

/**
 * See the API for {@link grails.test.mixin.web.ControllerUnitTestMixin} for usage instructions
 */
@TestFor(FacilityCouponConditionController)
@Mock([DateSlotCondition, TimeSlotCondition, WeekdaySlotCondition, CourtSlotCondition, SlotConditionSet])
class FacilityCouponConditionControllerTests {

    @Before
    public void setUp() {

    }

    void test() {
    }

}
