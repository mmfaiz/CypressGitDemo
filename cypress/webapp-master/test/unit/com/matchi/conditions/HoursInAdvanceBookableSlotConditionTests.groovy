package com.matchi.conditions
import com.matchi.Slot
import grails.test.MockUtils
import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import org.joda.time.DateTime
import org.junit.Before
import org.junit.Test

@TestFor(HoursInAdvanceBookableSlotCondition)
@TestMixin(GrailsUnitTestMixin)
class HoursInAdvanceBookableSlotConditionTests {

    @Before
    void setup() {
        MockUtils.mockLogging(HoursInAdvanceBookableSlotCondition)
    }

    @Test
    void testConditionFailIfBookingIsToEarly() {
        def nrOfHours = 2
        def condition = new HoursInAdvanceBookableSlotCondition()
        condition.nrOfHours = nrOfHours

        assert !condition.accept(slotWithStartTime(new DateTime().plusHours(nrOfHours+1)))
        assert !condition.accept(slotWithStartTime(new DateTime().plusHours(nrOfHours+2)))
    }

    @Test
    void testConditionAcceptedIfBookingIsInRange() {
        def nrOfHours = 2
        def condition = new HoursInAdvanceBookableSlotCondition()
        condition.nrOfHours = nrOfHours

        assert condition.accept(slotWithStartTime(new DateTime().plusHours(nrOfHours)))
        assert condition.accept(slotWithStartTime(new DateTime().plusHours(nrOfHours-1)))
    }

    @Test
    void testExactLimit() {
        def nrOfHours = 2
        def condition = new HoursInAdvanceBookableSlotCondition()
        condition.nrOfHours = nrOfHours

        DateTime now = new DateTime()

        assert condition.accept(slotWithStartTime(now.plusHours(nrOfHours)))
        assert condition.accept(slotWithStartTime(now.plusHours(nrOfHours-1)))

        // Need to wait another minute
        assert !condition.accept(slotWithStartTime(now.plusHours(nrOfHours).plusMinutes(1)))
    }

    private static def slotWithStartTime(DateTime time) {
        def slot = new Slot()
        slot.startTime = time.toDate()
        slot
    }
}
