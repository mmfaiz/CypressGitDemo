package com.matchi.facility

import com.matchi.*
import com.matchi.requirements.RequirementProfile
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.junit.Before
import org.junit.Test

import static com.matchi.TestUtils.*

@TestFor( FacilityBookingRestrictionsController )
@Mock([ BookingRestriction, RequirementProfile, Facility, Region, Municipality, Slot, Sport, Court ])
class FacilityBookingRestrictionsControllerTests {

    Slot slot
    Slot slot1
    RequirementProfile requirementProfile

    @Before
    void setUp() {
        slot               = createSlot()
        slot1              = createSlot()
        requirementProfile = createRequirementProfile()
    }

    @Test
    void testCreateBookingRestriction() {
        SaveBookingRestrictionCommand cmd = new SaveBookingRestrictionCommand()
        cmd.restrictionSlots = slot.id.toString()
        cmd.requirementProfiles = [requirementProfile.id]
        controller.save(cmd)

        assert BookingRestriction.get(1)
    }

    @Test
    void testDeleteBookingRestrictionDoesntDeleteIfSlotsRemain() {
        BookingRestriction restriction = new BookingRestriction()
        restriction.addToSlots(slot)
        restriction.addToSlots(slot1)
        restriction.addToRequirementProfiles(requirementProfile)
        restriction.save(failOnError: true, flush: true)

        DeleteBookingRestrictionCommand cmd = new DeleteBookingRestrictionCommand()
        cmd.delRestrictionSlotData = slot.id.toString()
        cmd.restrictionIds = [ restriction.id ]
        controller.delete(cmd)

        assert restriction
        assert BookingRestriction.get(1)
    }
}
