package com.matchi

import com.matchi.enums.BookingGroupType
import com.matchi.mpc.MpcService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.joda.time.DateTime
import org.joda.time.LocalTime
import org.junit.After
import org.junit.Before
import org.junit.Test

@TestFor(BookingAvailabilityService)
@Mock([Booking, BookingGroup, MpcService, Slot, Court, Customer, Facility, Subscription])
class BookingAvailabilityServiceTests {

    def mockSlotService
    def mockCourtRelationsBookingService
    def mockMpcService

    Customer customer
    Slot slot1
    Slot slot2
    Slot slot3
    Facility facility

    @Before
    public void setUp() {

        def startTime = new DateTime()

        facility = new Facility(name: "Test", shortname: "test").save(validate: false)
        Court court = new Court(name: "bana", facility: facility, listPosition: 1).save(validate: false)
        customer = new Customer(number: 123, firstname: "First", lastname: "Last", email: "customer@test.com", telephone: "031-212121", facility: facility).save(validate: false)
        slot1 = new Slot(startTime: startTime.toDate(), endTime: startTime.plusHours(1).toDate()).save(validate: false)
        slot2 = new Slot(startTime: startTime.plusWeeks(1).toDate(), endTime: startTime.plusWeeks(1).plusHours(1).toDate()).save(validate: false)
        slot3 = new Slot(startTime: startTime.plusWeeks(2).toDate(), endTime: startTime.plusWeeks(2).plusHours(1).toDate()).save(validate: false)

        court.addToSlots(slot1)
        court.addToSlots(slot2)
        court.addToSlots(slot3)

        slot1.save(validate: false)
        slot2.save(validate: false)
        slot3.save(validate: false)

        mockSlotService = mockFor(SlotService)
        mockCourtRelationsBookingService = mockFor(CourtRelationsBookingService)
        mockMpcService = mockFor(MpcService)

        service.slotService = mockSlotService.createMock()
        service.courtRelationsBookingService = mockCourtRelationsBookingService.createMock()
        service.mpcService = mockMpcService.createMock()

        mockSlotService.demand.getRecurrenceSlots(1..1) { DateTime fromDate, DateTime toDate, List<String> weekDays,
                                                          int frequency, int interval, List<Slot> recurrenceSlots, def onlyFreeSlots ->
            return [ freeSlots: [slot1, slot2, slot3], unavailableSlots: [slot1, slot2, slot3] ]
        }
    }

    @After
    public void tearDown() { }


    @Test
    public void testCreateBookingGroupCreatedBookingGroup() {
        Slot slot = new Slot(startTime: new DateTime().toDate(), court: new Court(id: 1l))

        BookingGroup bookingGroup = service.createSubscriptionBookingGroup(new Subscription(), new DateTime(), new DateTime().plusHours(6),
                slot, [new DateTime().dayOfWeek.toString()], 1, customer, false, "")

        assert bookingGroup != null
    }

    @Test
    public void testCreateBookingGroupThrowsExceptionOnDateToEarlierThanDateFrom() {
        Slot slot = new Slot(startTime: new DateTime().toDate(), court: new Court(id: 1l))

        shouldFail(IllegalArgumentException) {
            service.createSubscriptionBookingGroup(new Subscription(), new DateTime(), new DateTime().minusDays(6),
                    slot, [new DateTime().dayOfWeek.toString()], 1, customer, false, "")
        }
    }

    @Test
    public void testAddBookingGroupSlotsAddsSlots() {
        Subscription subscription = new Subscription()
        BookingGroup bookingGroup = new BookingGroup(type: BookingGroupType.TRAINING).save(validate: false)

        service.addBookingGroupBookings(subscription, bookingGroup, [slot1, slot2, slot3], customer, true, "Lorem")

        assert bookingGroup != null
        assert bookingGroup.bookings != null
        assert bookingGroup.bookings?.size() == 3
    }

    @Test
    public void testUpdateBookingGroupSlotsUpdatesSlots() {
        def subscription = new Subscription(customer: customer).save(validate: false)
        Slot slot = new Slot(startTime: new DateTime().toDate(), court: new Court(id: 1l))

        BookingGroup bookingGroup = service.createSubscriptionBookingGroup(new Subscription(), new DateTime(), new DateTime().plusWeeks(2),
                        slot, [new DateTime().dayOfWeek.toString()], 1, customer, false, "").save(failOnError: false)
        bookingGroup.subscription = subscription
        bookingGroup.save(validate: false)

        assert bookingGroup.bookings.size() == 3
        assert !bookingGroup.bookings.first().showComment
        assert !bookingGroup.bookings.first().comments

        service.updateBookingGroupBookings(subscription, bookingGroup, [slot1, slot2, slot3], customer, true, "Lorem")

        assert bookingGroup != null
        assert bookingGroup.bookings.size() == 3
    }

    @Test
    public void testUpdateBookingGroupSlotsOnlyUpdatesSlotsWithBookingInSubscription() {
        Subscription subscription = new Subscription(customer: customer).save(validate: false)
        Slot slot = new Slot(startTime: new DateTime().toDate(), court: new Court(id: 1l))

        BookingGroup bookingGroup = service.createSubscriptionBookingGroup(new Subscription(), new DateTime(), new DateTime().plusWeeks(2),
                        slot, [new DateTime().dayOfWeek.toString()], 1, customer, false, "").save(failOnError: false)
        bookingGroup.subscription = subscription
        bookingGroup.save(validate: false)

        bookingGroup.removeFromBookings(slot1.booking)
        bookingGroup.save(validate: false)

        slot1.booking = null
        slot1.save(validate: false)

        service.updateBookingGroupBookings(subscription, bookingGroup, [slot1, slot2, slot3], customer, true, "Lorem")

        assert bookingGroup != null
        assert bookingGroup.bookings.size() == 2
    }

    @Test
    public void testRemoveBookingGroupSlotsRemovesSlots() {
        mockMpcService.demand.tryDelete(0..3) { def booking -> }
        mockCourtRelationsBookingService.demand.tryCancelRelatedCourts(0..3) { def booking -> }

        def court = new Court(id: 1l)
        def subscription = new Subscription(customer: customer, time: new LocalTime(), court: court).save(validate: false)
        Slot slot = new Slot(startTime: new DateTime().toDate(), court: court)

        BookingGroup bookingGroup = service.createSubscriptionBookingGroup(new Subscription(), new DateTime(), new DateTime().plusWeeks(2),
                        slot, [new DateTime().dayOfWeek.toString()], 1, customer, false, "").save(failOnError: false)
        bookingGroup.subscription = subscription
        bookingGroup.save(validate: false)

        service.removeBookingGroupBookings(subscription, bookingGroup, [slot1, slot2, slot3])

        assert bookingGroup != null
        assert !bookingGroup.bookings
        assert !subscription.slots
    }
}
