package com.matchi.schedule

import com.matchi.*
import grails.test.GrailsUnitTestCase
import org.joda.time.DateTime
import org.joda.time.Interval
import org.junit.Before
import org.junit.Test

class ScheduleTests {

	Schedule schedule;

    @Before
	public void setUp() {
		DateTime fromDate = new DateTime(2011,1,1,12,0,0,0);
		DateTime toDate =   new DateTime(2011,1,10,12,0,0,0);
		
		Facility facility = new Facility()
		
		schedule = new Schedule(fromDate, toDate, facility, [], new User())
	}

    @Test
	void testFreeSlotsOfDayNoSlots() {
		DateTime date =   new DateTime(2011,1,12,12,0,0,0);
		assertEquals 0, schedule.getSlots(new Interval(date.withTime(0,0,0,0),date.withTime(23,59,59,999))).size()
	}

    @Test
	void testFreeSlotsOfDay() {
		// query date
		DateTime date =   new DateTime(2011,1,12,12,0,0,0);
		
		// slot date (same date)
		DateTime fromSlot =   new DateTime(2011,1,12,10,0,0,0);
		DateTime toSlot =   new DateTime(2011,1,12,11,0,0,0);

		schedule.addItems([createSlot(1, fromSlot, toSlot)])
		
		// should have 1 match
        assertEquals 1, schedule.getSlots(new Interval(date.withTime(10,0,0,0), date.withTime(11,0,0,0))).size()
	}

    @Test
	void testFreeSlotsOfDayNoMatch() {
		// query date
		DateTime date =   new DateTime(2011,1,11,0,0,0,0);
		
		// slot date (1 day before query date)
		DateTime fromSlot = new DateTime(2011,1,11,12,8,0,0);
		DateTime toSlot =   new DateTime(2011,1,11,12,9,0,0);
		
		schedule.addItems([createSlot(1, fromSlot, toSlot)])
		
		// should have no match
		assertEquals 0, schedule.getSlots(new Interval(date.withTime(7,0,0,0), date.withTime(8,0,0,0))).size()
	}

    @Test
    void testGetSlots() {
		// query date
		DateTime date =   new DateTime(2011,1,11,12,0,0,0);

		// slot date (1 day before query date)
		DateTime fromSlot = new DateTime(2011,1,12,12,8,0,0);
		DateTime toSlot =   new DateTime(2011,1,12,12,9,0,0);

        def items = []
        3.times { items << createSlot(1, fromSlot, toSlot) }

        schedule.addItems(items)

		// should have no match
		assertEquals 3, schedule.getSlots(new TimeSpan(fromSlot, toSlot)).size()
	}

    @Test
    void testGetSlotsNoMatch() {
		// query date
		DateTime date =   new DateTime(2011,1,11,12,0,0,0);

		// slot date (1 day before query date)
		DateTime fromSlot = new DateTime(2011,1,12,12,8,0,0);
		DateTime toSlot =   new DateTime(2011,1,12,12,9,0,0);

		schedule.addItems([createSlot(1, fromSlot, toSlot)])

		// should have no match
		assertEquals 0, schedule.getSlots(new TimeSpan(fromSlot.plusHours(2), toSlot.plusHours(2))).size()
	}

    @Test
    void testGetSlotsWithStartTimeWithingInterval() {
        // query date
        DateTime date =   new DateTime(2011,1,12,12,0,0,0);

        // slot date (1 day before query date)
        DateTime fromSlot = new DateTime(2011,1,12,8,30,0,0);
        DateTime toSlot =   new DateTime(2011,1,12,9,15,0,0);

        schedule.addItems([createSlot(1, fromSlot, toSlot)])

        // should have no match
        assertEquals 1, schedule.getSlots(new TimeSpan(date.withTime(8,0,0,0), date.withTime(9,0,0,0))).size()
    }

    @Test
    void testStatusReturnNotAvailableIfNum() {
        schedule.facility.bookingRuleNumDaysBookable = 5

        schedule.addItems([createSlot(1, new DateTime().plusDays(12), new DateTime().plusDays(12))])
        assert schedule.status(new TimeSpan(new DateTime().plusDays(10), new DateTime().plusDays(15)), schedule.facility.bookingRuleNumDaysBookable)
            .contains(Schedule.Status.NOT_AVAILABLE)
    }

    @Test
    void testStatusFreeIfNumDaysBookableIsZero() {
        schedule.facility.bookingRuleNumDaysBookable = 0

        schedule.addItems([createSlot(1, new DateTime().plusDays(12), new DateTime().plusDays(12))])
        assert schedule.status(new TimeSpan(new DateTime().plusDays(10), new DateTime().plusDays(15)), schedule.facility.bookingRuleNumDaysBookable)
                .contains(Schedule.Status.FREE)
    }

    @Test
    void testStatusReturnFreeIfBookable() {
        schedule.facility.bookingRuleNumDaysBookable = 10
        schedule.addItems([createSlot(1, new DateTime().plusDays(2), new DateTime().plusDays(2))])
        assert schedule.status(new TimeSpan(new DateTime(), new DateTime().plusDays(5)), schedule.facility.bookingRuleNumDaysBookable).contains(Schedule.Status.FREE)
    }

	
	private def createSlot(def courtId, def fromDate, def toDate) {
        Facility fac = new Facility()
		Court court = new Court(id: courtId, facility: fac); court.id = courtId;
		return [start: fromDate, end: toDate, court: court]
	}
}
