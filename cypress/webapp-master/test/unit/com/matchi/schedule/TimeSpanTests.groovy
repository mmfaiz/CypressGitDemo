package com.matchi.schedule

import grails.test.GrailsUnitTestCase
import org.joda.time.DateTime

class TimeSpanTests extends GrailsUnitTestCase {
	

	public void setUp() {
		super.setUp();
		
		DateTime fromDate = new DateTime(2011,1,1,12,0,0,0);
	}
	
	public void tearDown() {
		super.tearDown();
	}
	
	public void testSpanIsWithinReturnsTrue() {
		TimeSpan span1 = new TimeSpan(new DateTime(2011,1,1,12,0,0,0), new DateTime(2011,1,1,13,0,0,0))
		TimeSpan span2 = new TimeSpan(new DateTime(2011,1,1,12,10,0,0), new DateTime(2011,1,1,12,40,0,0))
		
		assertTrue "span2 should be within span1",span2.isWithin(span1)
		
	}
	
	public void testSpanIsWithinReturnsTrueWhenEqual() {
		TimeSpan span1 = new TimeSpan(new DateTime(2011,1,1,12,0,0,0), new DateTime(2011,1,1,13,0,0,0))
		TimeSpan span2 = new TimeSpan(new DateTime(2011,1,1,12,0,0,0), new DateTime(2011,1,1,13,0,0,0))
		
		assertTrue "span2 should be within span1",span2.isWithin(span1)
		
	}
	
	public void testPartlyWithinStartDate() {
		TimeSpan span1 = new TimeSpan(new DateTime(2011,1,1,12,0,0,0), new DateTime(2011,1,1,13,0,0,0))
		TimeSpan span2 = new TimeSpan(new DateTime(2011,1,1,12,10,0,0), new DateTime(2011,1,1,13,40,0,0))
		
		assertFalse "span2 should NOT be within span1",span2.isWithin(span1)
	}
	
	public void testPartlyWithinEndDate() {
		TimeSpan span1 = new TimeSpan(new DateTime(2011,1,1,12,0,0,0), new DateTime(2011,1,1,13,0,0,0))
		TimeSpan span2 = new TimeSpan(new DateTime(2011,1,1,11,10,0,0), new DateTime(2011,1,1,12,40,0,0))
		
		assertFalse "span2 should NOT be within span1",span2.isWithin(span1)
	}

    public void testTwoOfTheSame() {
		TimeSpan span1 = new TimeSpan(new DateTime(2011,1,1,12,0,0,0), new DateTime(2011,1,1,13,0,0,0))

		assertTrue "span1 should be within itself",span1.isWithin(span1)
	}
	
	public void testWrongYearReturnsFails() {
		TimeSpan span1 = new TimeSpan(new DateTime(2011,1,1,12,0,0,0), new DateTime(2011,1,1,13,0,0,0))
		TimeSpan span2 = new TimeSpan(new DateTime(2010,2,1,11,10,0,0), new DateTime(2010,3,1,12,40,0,0))
		
		assertFalse "span2 should NOT be within span1",span2.isWithin(span1)
	}
    public void testHourlyFormatted() {
		TimeSpan span = new TimeSpan(new DateTime(2011,1,1,12,0,0,0), new DateTime(2011,1,1,13,59,59,999))

		assertEquals "12-13", span.hourlyFormatted
	}
    public void testHourlyFormattedLeadingZero() {
		TimeSpan span = new TimeSpan(new DateTime(2011,1,1,6,0,0,0), new DateTime(2011,1,1,6,59,59,999))

		assertEquals "06-06", span.hourlyFormatted
	}
	
}
