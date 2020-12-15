package com.matchi.price

import com.matchi.Customer
import com.matchi.Slot
import com.matchi.User
import grails.test.GrailsUnitTestCase
import org.joda.time.DateTime

class TimePriceConditionTests extends GrailsUnitTestCase {

    TimePriceCondition condition
    Customer customer
    User user

    protected void setUp() {
        super.setUp()

        condition = new TimePriceCondition()
        condition.fromHour = 9
        condition.fromMinute = 30
        condition.toHour = 12
        condition.toMinute = 45

        user = new User()
        customer = new Customer()
    }

    protected void tearDown() {
        super.tearDown()
    }

    void testNotAcceptOutsideOurs() {
        assertFalse(condition.accept(createSlot(13,14), customer))
    }

    void testNotAcceptOutsideOursInMinutes() {
        assertFalse(condition.accept(createSlot(12,50, 13,0), customer))
    }

    void testAccept() {
        assertTrue(condition.accept(createSlot(10,11), customer))
    }

    void testAcceptPartialBetween() {
        assertTrue(condition.accept(createSlot(12,30,13,30), customer))
    }

    void testNotAcceptStartDateAsToTime() {
        assertFalse(condition.accept(createSlot(12,45,13,30), customer))
    }

    void testNotAcceptEndTimePartialBetween() {
        assertFalse(condition.accept(createSlot(9,0,10,0), customer))
    }


    Date createTime(int hour, int minute) {
        return new DateTime(2020, 1,1, hour, minute, 0, 0).toDate()
    }

    Slot createSlot(int startHour, int endHour) {
        Slot slot = new Slot()
        slot.startTime = createTime(startHour, 0)
        slot.endTime = createTime(endHour, 0)
        return slot
    }

    Slot createSlot(int startHour, int startMinute, int endHour, int endMinute) {
        Slot slot = new Slot()
        slot.startTime = createTime(startHour, startMinute)
        slot.endTime = createTime(endHour, endMinute)
        return slot
    }
}
