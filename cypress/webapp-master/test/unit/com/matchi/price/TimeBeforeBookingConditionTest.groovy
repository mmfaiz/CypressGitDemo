package com.matchi.price

import com.matchi.Customer
import com.matchi.Slot
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.joda.time.DateTime

/**
 * @author Michael Astreiko
 */
@TestFor(TimeBeforeBookingCondition)
class TimeBeforeBookingConditionTest {
    Customer customer
    TimeBeforeBookingCondition domain

    void setUp() {
        customer = new Customer()
        domain = new TimeBeforeBookingCondition()
    }

    void testAccept() {
        domain.hours = 5
        domain.minutes = 15
        assert domain.accept(createSlot(new DateTime().plusHours(3).toDate(), new DateTime().plusHours(5).toDate()), customer)
        domain.hours = 0
        assert domain.accept(createSlot(new DateTime().plusMinutes(10).toDate(), new DateTime().plusMinutes(70).toDate()), customer)
        domain.hours = 1
        domain.minutes = 0
        assert domain.accept(createSlot(new DateTime().plusMinutes(2).toDate(), new DateTime().plusMinutes(62).toDate()), customer)
    }

    void testAcceptFailsIfConditionTimeWillFireInFuture() {
        domain.hours = 4
        domain.minutes = 0
        assert !domain.accept(createSlot(new DateTime().plusHours(5).toDate(), new DateTime().plusHours(7).toDate()), customer)
    }

    void testConstrains(){
        mockForConstraintsTests(TimeBeforeBookingCondition)

        def obj = new TimeBeforeBookingCondition()
        assert !obj.validate()
        assert 1 == obj.errors.errorCount
        assert 'timeBeforeBooking.condition.anythingShouldBeSpecified' == obj.errors.hours

        obj.minutes = 80
        assert !obj.validate()
        assert 1 == obj.errors.errorCount
        assert "max" == obj.errors.minutes

        obj.minutes = 60
        assert obj.validate()

        obj.minutes = null
        obj.hours = 1
        assert obj.validate()
    }

    private Slot createSlot(Date startDate, Date endDate) {
        new Slot(startTime: startDate, endTime: endDate)
    }

}
