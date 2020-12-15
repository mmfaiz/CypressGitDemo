package com.matchi.conditions

import com.matchi.Slot
import grails.test.mixin.TestFor
import org.joda.time.DateTime
import org.joda.time.DateTimeConstants
import org.junit.Before

/**
 * See the API for {@link grails.test.mixin.domain.DomainClassUnitTestMixin} for usage instructions
 */
@TestFor(WeekdaySlotCondition)
class WeekdaySlotConditionTests {

    WeekdaySlotCondition condition

    @Before
    public void setUp() {
        condition = new WeekdaySlotCondition()
        mockForConstraintsTests(WeekdaySlotCondition)
    }

    void testMinSizeWeekdays() {
        condition.weekdays = []
        assert !condition.validate()
    }

    void testWeekdaysSuccess() {
        condition.weekdays = [0, 1, 2]
        assert condition.validate()
    }

    void testMaxSizeWeekdays() {
        condition.weekdays = [0, 1, 2, 3, 4, 5, 6 ,7, 8, 9]
        assert !condition.validate()
    }

    void testAcceptsOnRightDay() {
        condition.weekdays = [DateTimeConstants.MONDAY]
        assert condition.accept(createSlot(5))
    }

    void testAcceptsOnRightDays() {
        condition.weekdays = [DateTimeConstants.MONDAY,DateTimeConstants.TUESDAY]
        assert condition.accept(createSlot(6))
    }

    void testDoNotAccepts() {
        condition.weekdays = [DateTimeConstants.MONDAY]
        assert !condition.accept(createSlot(4)) // sunday
    }

    Date createTime(int day) {
        return new DateTime(2012, 3,day, 10, 0, 0, 0).toDate()
    }

    Slot createSlot(int day) {
        Slot slot = new Slot()
        slot.startTime = createTime(day)
        slot.endTime = new DateTime(createTime(day)).plusHours(1).toDate()
        return slot
    }
}
