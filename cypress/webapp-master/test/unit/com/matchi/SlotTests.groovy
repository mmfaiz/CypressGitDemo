package com.matchi

import com.matchi.requirements.RequirementProfile
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.joda.time.DateTime
import org.junit.Before
import org.junit.Test
import static com.matchi.TestUtils.*

@TestFor(Slot)
@Mock([Customer, Subscription, Booking, Region, Facility, Municipality, RequirementProfile, BookingRestriction, Sport, Court])
class SlotTests {

    def mockedConfig
    def mockedFacility

    @Before
    void setUp() {
        mockForConstraintsTests(Slot)

        mockedConfig = new ConfigObject()

        mockedFacility = mockFor(Facility)
    }

    void testEndTimeBeforeStartTimeNotValid() {
        def slot = createValidSlot()
        slot.endTime = new DateTime(slot.startTime).minusDays(1).toDate()

        def result = slot.validate()

        assertEquals("beforeStartTime", slot.errors["endTime"])
        assertFalse(result)
    }

    void testValidSlot() {
        Slot slot = createValidSlot()
        assertTrue(slot.validate())
    }

    void testIsNotRefundable() {
        assert !createValidSlot().isRefundable()
    }

    void testIsRefundable() {
        def slot = createValidSlot()
        slot.startTime = new DateTime().plusDays(10).toDate()
        slot.endTime = new DateTime().plusDays(10).plusHours(1).toDate()

        mockedFacility.demand.getRefundPercentage(1..1) { def start ->
            return 10
        }

        assert slot.isRefundable()
    }

    void testSlotIsBookableIfInFuture() {
        Slot slot = new Slot()
        slot.startTime = new DateTime().plusHours(1).toDate()
        slot.endTime = slot.startTime = new DateTime().plusHours(2).toDate()

        assert slot.isBookable()
    }

    void testSlotIsNotBookableIfInPast() {
        Slot slot = new Slot()
        slot.startTime = new DateTime().minusHours(2).toDate()
        slot.endTime = slot.startTime = new DateTime().minusHours(1).toDate()

        assert !slot.isBookable()
    }

    @Test
    void testSlotRefundableWhenCancelRefundIsLargerThanZero() {
        Slot slot = createValidSlot()
        slot.startTime = new DateTime().minusHours(3).toDate()
        slot.endTime = new DateTime().minusHours(2).toDate()

        mockedFacility.demand.getRefundPercentage(1..1) { def start ->
            return 50
        }

        assert slot.isRefundable()
    }

    void testSlotNotRefundableWhenCancelRefundIsLesserThanZero() {
        Slot slot = createValidSlot()
        slot.startTime = new DateTime().minusHours(3).toDate()
        slot.endTime = new DateTime().minusHours(2).toDate()

        mockedFacility.demand.getRefundPercentage(1..1) { def start ->
            return 0
        }

        assert !slot.isRefundable()
    }

    void testSlotBelongsToSubscription() {
        Slot slot = createValidSlot()

        Subscription subscription = new Subscription()
        subscription.addToSlots(slot)

        assert slot.belongsToSubscription()
    }

    void testSlotNotBelongsToSubscription() {
        Slot slot = createValidSlot()

        assert !slot.belongsToSubscription()
    }

    void testIsEquallyRestrictedTo() {
        Facility facility = createFacility()
        Court court = createCourt(facility)
        Slot slot = createSlot(court)
        Slot slot2 = createSlot(court)

        RequirementProfile rp1 = createRequirementProfile(facility)
        RequirementProfile rp2 = createRequirementProfile(facility)
        RequirementProfile rp3 = createRequirementProfile(facility)

        slot.bookingRestriction = new BookingRestriction(requirementProfiles: [rp1, rp2, rp3], slots: [slot]).save(flush: true, failOnError: true)
        slot2.bookingRestriction = new BookingRestriction(requirementProfiles: [rp1, rp3, rp2], slots: [slot2]).save(flush: true, failOnError: true)

        assert slot.isEquallyRestrictedAs(slot2)
        assert slot.getRequirementProfiles().size() == 3
        assert slot2.isEquallyRestrictedAs(slot)
        assert slot2.getRequirementProfiles().size() == 3

        Slot slot3 = createSlot(court)
        Slot slot4 = createSlot(court)

        assert slot3.getRequirementProfiles() == null
        assert slot4.getRequirementProfiles() == null
        assert slot3.isEquallyRestrictedAs(slot4)
        assert slot4.isEquallyRestrictedAs(slot3)
    }

    void testIsNotEquallyRestrictedTo() {
        Facility facility = createFacility()
        Court court = createCourt(facility)
        Slot slot = createSlot(court)
        Slot slot2 = createSlot(court)
        Slot slot3 = createSlot(court)
        Slot slot4 = createSlot(court)

        RequirementProfile rp1 = createRequirementProfile(facility)
        RequirementProfile rp2 = createRequirementProfile(facility)
        RequirementProfile rp3 = createRequirementProfile(facility)
        RequirementProfile rp4 = createRequirementProfile(facility)

        slot.bookingRestriction = new BookingRestriction(requirementProfiles: [rp1, rp2, rp3], slots: [slot]).save(flush: true, failOnError: true)
        slot2.bookingRestriction = new BookingRestriction(requirementProfiles: [rp1, rp2], slots: [slot2]).save(flush: true, failOnError: true)
        slot3.bookingRestriction = new BookingRestriction(requirementProfiles: [rp1, rp2, rp4], slots: [slot3]).save(flush: true, failOnError: true)

        assert !slot.isEquallyRestrictedAs(slot2)
        assert !slot.isEquallyRestrictedAs(slot3)
        assert !slot.isEquallyRestrictedAs(slot4)

        assert !slot2.isEquallyRestrictedAs(slot)
        assert !slot2.isEquallyRestrictedAs(slot3)
        assert !slot2.isEquallyRestrictedAs(slot4)

        assert !slot3.isEquallyRestrictedAs(slot2)
        assert !slot3.isEquallyRestrictedAs(slot)
        assert !slot3.isEquallyRestrictedAs(slot4)

        assert !slot4.isEquallyRestrictedAs(slot2)
        assert !slot4.isEquallyRestrictedAs(slot)
        assert !slot4.isEquallyRestrictedAs(slot3)

        assert slot4.getRequirementProfiles() == null
    }

    private Slot createValidSlot() {
        def slot = new Slot()
        slot.court = new Court()
        slot.court.id = 123
        slot.startTime = new DateTime().toDate()
        slot.endTime = new DateTime().plusHours(1).toDate()

        slot.grailsApplication = [ config: mockedConfig ]

        mockedFacility.demand.getRefundPercentage(1..1) { def start ->
            return 0
        }

        slot.court.facility = mockedFacility.createMock()

        return slot
    }
}
