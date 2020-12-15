package com.matchi.price

import com.matchi.Customer
import com.matchi.Slot
import com.matchi.User
import grails.test.GrailsUnitTestCase
import org.joda.time.DateTime
import org.joda.time.DateTimeConstants

class WeekDayPriceConditionTests extends GrailsUnitTestCase {

    WeekDayPriceCondition condition
    User user
    Customer customer

    protected void setUp() {
        super.setUp()

        condition = new WeekDayPriceCondition()
        user = new User()
        customer = new Customer()
    }

    protected void tearDown() {
        super.tearDown()
    }

    void testAcceptsOnRightDay() {
        condition.weekDays = [String.valueOf(DateTimeConstants.MONDAY)]
        assertTrue(condition.accept(createSlot(5), customer))
    }

    void testAcceptsOnRightDays() {
        condition.weekDays = [String.valueOf(DateTimeConstants.MONDAY),String.valueOf(DateTimeConstants.TUESDAY)]
        assertTrue(condition.accept(createSlot(6), customer))
    }

    void testDoNotAccepts() {
        condition.weekDays = [String.valueOf(DateTimeConstants.MONDAY)]
        assertFalse(condition.accept(createSlot(4), customer)) // sunday
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
