package com.matchi.conditions

import com.matchi.Slot
import grails.test.mixin.TestFor
import org.joda.time.DateTime
import org.joda.time.LocalDate

/**
 * See the API for {@link grails.test.mixin.domain.DomainClassUnitTestMixin} for usage instructions
 */
@TestFor(DateSlotCondition)
class DateSlotConditionTests {

    public void setUp() {
    }

    void testAcceptIfWithin() {
        def condition = new DateSlotCondition()
        condition.startDate = new LocalDate("2012-01-01")
        condition.endDate = new LocalDate("2012-01-03")

        assert condition.accept(slotWithStartTime(new DateTime(2012, 1, 2, 16, 0, 0)))
    }

    void testNotAcceptIfNotWithin() {
        def condition = new DateSlotCondition()
        condition.startDate = new LocalDate("2012-01-01")
        condition.endDate = new LocalDate("2012-01-03")

        assert !condition.accept(slotWithStartTime(new DateTime(2012, 1, 4, 16, 0, 0)))
    }

    void testAcceptIfOnStartDate() {
        def condition = new DateSlotCondition()
        condition.startDate = new LocalDate("2012-01-01")
        condition.endDate = new LocalDate("2012-01-03")

        assert condition.accept(slotWithStartTime(new DateTime(2012, 1, 1, 9, 0, 0)))
    }

    void testAcceptIfOnEndDate() {
        def condition = new DateSlotCondition()
        condition.startDate = new LocalDate("2012-01-01")
        condition.endDate = new LocalDate("2012-01-03")

        assert condition.accept(slotWithStartTime(new DateTime(2012, 1, 3, 9, 0, 0)))
    }

    void testAcceptIfOnStartAndEndDate() {
        def condition = new DateSlotCondition()
        condition.startDate = new LocalDate("2012-01-01")
        condition.endDate = new LocalDate("2012-01-01")

        assert condition.accept(slotWithStartTime(new DateTime(2012, 1, 1, 9, 0, 0)))
    }


    private def slotWithStartTime(DateTime time) {
        def slot = new Slot()
        slot.startTime = time.toDate()
        slot
    }
}
