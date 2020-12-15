package com.matchi.facility

import com.matchi.FacilityBookingCommand
import grails.test.GrailsUnitTestCase

class FacilityBookingCommandTest extends GrailsUnitTestCase {

    def facilityBookingCommand

    public void setUp() {
		super.setUp();
        facilityBookingCommand = new FacilityBookingCommand()
	}

	public void tearDown() {
		super.tearDown();
	}

    public void testParseSlotIdsEmpty() {
        facilityBookingCommand.slotId = null
        def slotIds = facilityBookingCommand.slotIds()
        assertNotNull(slotIds)
        assertEquals(0, slotIds.size())
    }

    public void testParseSlotIdsSingleId() {
        facilityBookingCommand.slotId = "slotId"
        def slotIds = facilityBookingCommand.slotIds()
        assertNotNull(slotIds)
        assertEquals(1, slotIds.size())
    }

    public void testParseSlotIdsMultipleIds() {
        facilityBookingCommand.slotId = "slotId,sloitId2,sltawd"
        def slotIds = facilityBookingCommand.slotIds()
        assertNotNull(slotIds)
        assertEquals(3, slotIds.size())
    }

    public void testParseSlotIdsMultipleEmptyLastIds() {
        facilityBookingCommand.slotId = "slotId,sloitId2,sltawd,"
        def slotIds = facilityBookingCommand.slotIds()
        assertNotNull(slotIds)
        assertEquals(3, slotIds.size())
    }
}
