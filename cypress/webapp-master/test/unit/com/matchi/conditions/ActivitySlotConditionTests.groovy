package com.matchi.conditions

import com.matchi.*
import com.matchi.activities.ActivityOccasion
import com.matchi.activities.ClassActivity
import com.matchi.enums.BookingGroupType
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import org.codehaus.groovy.grails.commons.InstanceFactoryBean
import org.junit.Before
import org.junit.Test

import static com.matchi.TestUtils.createBokingGroup
import static com.matchi.TestUtils.createBooking
/**
 * See the API for {@link grails.test.mixin.domain.DomainClassUnitTestMixin} for usage instructions
 */
@TestMixin(GrailsUnitTestMixin)
@TestFor(ActivitySlotCondition)
@Mock([Slot, Booking, ClassActivity, ActivityOccasion, Region, Municipality, Facility, Customer, Sport, Court, BookingGroup])
class ActivitySlotConditionTests {

    final static Long ID = 1
    ActivitySlotCondition condition

    def mockActivityService

    @Before
    void setUp() {
        mockActivityService = mockFor(ActivityService)
        defineBeans {
            activityService(InstanceFactoryBean, mockActivityService.createMock(), ActivityService)
        }
        condition = new ActivitySlotCondition()
        condition.activities = []
    }

    @Test
    void testNotValidConstraints() {
        condition.activities = []
        assert !condition.validate()
    }

    @Test
    void testValidConstraintsWhenNotForActivitiesIsTrue() {
        condition.notValidForActivities = true
        assert condition.validate()
    }

    @Test
    void testValidConstraintsWhenNotForActivitiesIsFalse() {
        condition.notValidForActivities = false
        condition.activities = [new ClassActivity()]
        assert condition.validate()
    }

    @Test
    void testOneActivityShouldReturnTrue() {
        ClassActivity activity = new ClassActivity(id: 1L).save(validate: false)
        condition.addToActivities(activity)

        Booking booking = createBooking()
        BookingGroup group = createBokingGroup(BookingGroupType.ACTIVITY)
        group.addToBookings(booking)

        Slot slot = new Slot(booking: booking)
        ActivityOccasion occasion = new ActivityOccasion()
        activity.addToOccasions(occasion)

        Map<Slot, ActivityOccasion> map = [:]
        map.put(slot, occasion)

        mockActivityService.demand.getOccasionsBySlots(1..1) { List<Slot> slots ->
            return map
        }

        assert condition.accept(slot)
    }

    @Test
    void testOneActivityShouldReturnFalse() {
        ClassActivity activity = new ClassActivity(id: 1L).save(validate: false)
        ClassActivity activity1 = new ClassActivity(id: 2L).save(validate: false)
        condition.addToActivities(activity1)

        Booking booking = createBooking()
        BookingGroup group = createBokingGroup(BookingGroupType.ACTIVITY)
        group.addToBookings(booking)

        Slot slot = new Slot(booking: booking)
        ActivityOccasion occasion = new ActivityOccasion()
        activity.addToOccasions(occasion)

        Map<Slot, ActivityOccasion> map = [:]
        map.put(slot, occasion)

        mockActivityService.demand.getOccasionsBySlots(1..1) { List<Slot> slots ->
            return map
        }

        assert !condition.accept(slot)
    }

    @Test
    void testNonActivityBookingShouldReturnTrue() {
        Booking booking = createBooking()
        Slot slot = new Slot(booking: booking)

        assert condition.accept(slot)
    }

    @Test
    void testNonActivityBookingShouldReturnFalse() {
        ClassActivity activity = new ClassActivity(id: 1L).save(validate: false)
        condition.addToActivities(activity)

        Booking booking = createBooking()
        Slot slot = new Slot(booking: booking)

        mockActivityService.demand.getOccasionsBySlots(0) { List<Slot> slots -> }

        assert !condition.accept(slot)
    }

    @Test
    void testNonActivityBookingWithGroupShouldReturnTrue() {
        Booking booking = createBooking()
        BookingGroup group = createBokingGroup(BookingGroupType.DEFAULT)
        group.addToBookings(booking)

        Slot slot = new Slot(booking: booking)

        assert condition.accept(slot)
    }

    @Test
    void testSlotWithoutBookingReturnsFalse() {
        Slot slot = new Slot()
        mockActivityService.demand.getOccasionsBySlots(0) { List<Slot> slots -> }
        assert !condition.accept(slot)
    }

    @Test
    void testSlotWithBookingReturnsTrue() {
        condition.notValidForActivities = true

        Slot slot = new Slot()
        mockActivityService.demand.getOccasionsBySlots(0) { List<Slot> slots -> }
        assert condition.accept(slot)
    }

    @Test
    void testNotValidForActivitiesSetTrueReturnsFalse() {
        condition.notValidForActivities = true

        Booking booking = createBooking()
        BookingGroup group = createBokingGroup(BookingGroupType.ACTIVITY)
        group.addToBookings(booking)

        Slot slot = new Slot(booking: booking)
        mockActivityService.demand.getOccasionsBySlots(0) { List<Slot> slots -> }
        assert !condition.accept(slot)
    }
}
