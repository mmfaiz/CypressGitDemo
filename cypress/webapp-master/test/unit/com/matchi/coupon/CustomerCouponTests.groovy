package com.matchi.coupon

import com.matchi.Booking
import com.matchi.BookingGroup
import com.matchi.Customer
import com.matchi.Facility
import com.matchi.Slot
import com.matchi.activities.ActivityOccasion
import com.matchi.activities.ClassActivity
import com.matchi.enums.BookingGroupType
import com.matchi.orders.CouponOrderPayment
import grails.test.MockUtils
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import org.joda.time.DateTime
import org.joda.time.LocalDate
import org.junit.Before
import org.junit.Test

@TestFor(CustomerCoupon)
@TestMixin(GrailsUnitTestMixin)
@Mock([CustomerCoupon, Coupon, Slot, Facility, Customer, CustomerCouponTicket,
        Booking, BookingGroup, CouponOrderPayment, ClassActivity, ActivityOccasion])
class CustomerCouponTests {

    Coupon coupon
    Facility facility

    @Before
    void setUp() {
        MockUtils.mockLogging(CustomerCoupon)

        facility = new Facility()
        coupon = new Coupon(facility: facility)
    }

    @Test
    void testIsExpiredReturnsTrueIfExpirationDatePassed() {
        def customerCoupon = new CustomerCoupon(expireDate: new LocalDate().minusDays(1)) //expired yesterday
        assert customerCoupon.isExpired()
    }

    @Test
    void testIsExpiredReturnsFalseIfExpirationDateNotPassed() {
        assert !new CustomerCoupon(expireDate: new LocalDate()).isExpired()  // expires tomorrow
        assert !new CustomerCoupon(expireDate: new LocalDate().plusDays(1)).isExpired() // expires day after tomorrow
        assert !new CustomerCoupon().isExpired()  // never expires
    }

    @Test
    void testIsExpiredReturnsFalseIfNoExpirationDate() {
        def customerCoupon = new CustomerCoupon()
        assert !customerCoupon.isExpired()
    }

    @Test
    void testIsValidWithinExpirationDateAndTicketsNoSlots() {
        def customerCoupon = new CustomerCoupon(expireDate: new LocalDate().plusDays(1)) //expires tomorrow
        customerCoupon.addToCouponTickets(new CustomerCouponTicket())

        assert customerCoupon.isValid()
    }
    @Test
    void testIsValidNoTicketsAndUnlimitedCoupon() {
        coupon.unlimited = true
        def customerCoupon = new CustomerCoupon(coupon: coupon)

        assert customerCoupon.isValid()
    }
    @Test
    void testNotValidBeforeExpirationDateAndTicketsNoSlots() {
        def customerCoupon = new CustomerCoupon(expireDate: new DateTime().minusDays(1)) //expired yesterday
        customerCoupon.addToCouponTickets(new CustomerCouponTicket())

        assert !customerCoupon.isValid()
    }
    @Test
    void testNotValidBeforeExpirationDateAndTicketsAndSlots() {
        def customerCoupon = new CustomerCoupon(expireDate: new DateTime().minusDays(1)) //expired yesterday
        customerCoupon.addToCouponTickets(new CustomerCouponTicket())

        assert !customerCoupon.isValid()
    }
    @Test
    void testGetLastDayOfPeriodReturnsLastDay() {
        def customerCoupon = new CustomerCoupon(coupon: coupon)
        DateTime pivot = new DateTime().withMonthOfYear(1).withDayOfMonth(10)

        coupon.conditionPeriod = Coupon.ConditionPeriod.DAILY
        assert pivot.plusDays(1).toDateMidnight().toDateTime() == customerCoupon.getLastDayOfPeriod(pivot)
        coupon.conditionPeriod = Coupon.ConditionPeriod.WEEKLY
        assert pivot.plusWeeks(1).withDayOfWeek(1).toDateMidnight().toDateTime() == customerCoupon.getLastDayOfPeriod(pivot)
        coupon.conditionPeriod = Coupon.ConditionPeriod.MONTHLY
        assert pivot.withDayOfMonth(1).withMonthOfYear(2).toDateMidnight().toDateTime() == customerCoupon.getLastDayOfPeriod(pivot)
        coupon.conditionPeriod = Coupon.ConditionPeriod.YEARLY
        assert pivot.plusYears(1).withDayOfYear(1).toDateMidnight().toDateTime() == customerCoupon.getLastDayOfPeriod(pivot)
    }

    @Test
    void testGetNextPivotIteratesCorrectly() {
        def customerCoupon = new CustomerCoupon(coupon: coupon)
        DateTime pivot = new DateTime().withMonthOfYear(1).withDayOfMonth(10)

        coupon.conditionPeriod = Coupon.ConditionPeriod.WEEKLY
        def firstPivot = pivot.plusWeeks(1).withDayOfWeek(1).toDateMidnight().toDateTime()
        assert firstPivot == customerCoupon.getLastDayOfPeriod(pivot)
        assert firstPivot == customerCoupon.getNextPivot(firstPivot, 0)
        assert firstPivot.plusWeeks(1) == customerCoupon.getNextPivot(firstPivot, 1)
        assert firstPivot.plusWeeks(2) == customerCoupon.getNextPivot(firstPivot, 2)
    }

    @Test
    void testGetSurroundingIntervalsReturnsCorrectNumberOfIntervals() {
        def customerCoupon = new CustomerCoupon(coupon: coupon)
        DateTime pivot = new DateTime().withMonthOfYear(1).withDayOfMonth(10)

        coupon.conditionPeriod = Coupon.ConditionPeriod.WEEKLY
        coupon.nrOfPeriods = 2
        assert coupon.nrOfPeriods == customerCoupon.getSurroundingIntervals(pivot)?.size()

        coupon.nrOfPeriods = 3
        assert coupon.nrOfPeriods == customerCoupon.getSurroundingIntervals(pivot)?.size()
    }

    @Test
    void testValidatePeriodConditionReturnsTrueIfNotSet() {
        coupon.conditionPeriod = null
        def customerCoupon = new CustomerCoupon(coupon: coupon)
        def lookupSlot = new Slot(startTime: new DateTime().toDate()) //today
        assert customerCoupon.validatePeriodCondition( lookupSlot )
    }

    @Test
    void testValidatePeriodCondition() {
        CustomerCoupon customerCoupon = new CustomerCoupon(coupon: coupon)
        coupon.conditionPeriod = Coupon.ConditionPeriod.DAILY
        coupon.nrOfPeriods = 1
        coupon.nrOfBookingsInPeriod = 1
        coupon.totalBookingsInPeriod = true

        Slot slot1 = new Slot(startTime: new DateTime().withTime(12, 0, 0, 0).toDate(), endTime: new DateTime().withTime(12, 0, 0, 0).plusHours(1).toDate())
        Slot slot2 = new Slot(startTime: new DateTime().withTime(12, 0, 0, 0).plusHours(2).toDate(), endTime: new DateTime().withTime(12, 0, 0, 0).plusHours(3).toDate())

        assert !customerCoupon.validatePeriodCondition(slot1, [slot2])
        coupon.nrOfBookingsInPeriod = 2
        assert customerCoupon.validatePeriodCondition(slot1, [slot2])

        Slot slot3 = new Slot(startTime: new DateTime().plusHours(1).toDate(), endTime: new DateTime().plusHours(2).toDate())
        Booking booking = new Booking(id: 100L, slot: slot3).save(validate: false)
        customerCoupon.couponTickets = [new CustomerCouponTicket(purchasedObjectId: booking.id,
                type: CustomerCouponTicket.Type.BOOKING)].toSet()

        assert !customerCoupon.validatePeriodCondition(slot2, [slot1])
        coupon.nrOfBookingsInPeriod = 3
        assert customerCoupon.validatePeriodCondition(slot2, [slot1])
    }

    @Test
    void testValidateForUnlimitedPunchCardPeriodCondition() {

        def customerCouponUnlimited = new CustomerCoupon(coupon: coupon)
        coupon.conditionPeriod = Coupon.ConditionPeriod.DAILY
        coupon.nrOfPeriods = 1
        coupon.nrOfBookingsInPeriod = 1
        coupon.totalBookingsInPeriod = true
        coupon.unlimited = true

        Slot slot1 = new Slot(startTime: new DateTime().withTime(12, 0, 0, 0).toDate(), endTime: new DateTime().withTime(12, 0, 0, 0).plusHours(1).toDate())
        Slot slot2 = new Slot(startTime: new DateTime().withTime(12, 0, 0, 0).plusHours(2).toDate(), endTime: new DateTime().withTime(12, 0, 0, 0).plusHours(3).toDate())
        BookingGroup group = new BookingGroup([type: BookingGroupType.ACTIVITY])
        Booking bookingSlot1 = new Booking(id: 100L, slot: slot1)
        Booking bookingSlot2 = new Booking(id: 101L, slot: slot2)
        bookingSlot1.group = group
        bookingSlot2.group = group
        ActivityOccasion occasion = new ActivityOccasion()
        occasion.bookings=[bookingSlot1, bookingSlot2].toSet()

        ClassActivity activity = new ClassActivity()
        activity.occasions = [occasion].toSet()
        facility.activities = [activity].toSet()
        slot1.booking = bookingSlot1

        assert customerCouponUnlimited.validatePeriodCondition(slot1, [slot2])
        coupon.unlimited = false
        assert !customerCouponUnlimited.validatePeriodCondition(slot1, [slot2])
    }
}
