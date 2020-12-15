package com.matchi.jobs

import com.matchi.Booking
import com.matchi.BookingCancelTicket
import com.matchi.BookingGroup
import com.matchi.Court
import com.matchi.Customer
import com.matchi.Facility
import com.matchi.Municipality
import com.matchi.Slot
import com.matchi.Sport
import com.matchi.Subscription
import com.matchi.TestUtils
import com.matchi.enums.BookingGroupType
import com.matchi.FacilityProperty
import org.apache.commons.lang.RandomStringUtils
import org.joda.time.DateTime
import org.joda.time.LocalTime

/**
 * @author Michael Astreiko
 */
class BookingReminderJobTest extends GroovyTestCase {
    def bookingService
    def subscriptionService
    def ticketService


    void testExecute() {
        BookingReminderJob bookingReminderJob = new BookingReminderJob()
        bookingReminderJob.bookingService = bookingService
        bookingReminderJob.subscriptionService = subscriptionService
        bookingReminderJob.ticketService = ticketService
        bookingReminderJob.notificationService = [
                sendBookingReminder: { customer, slots, tickets -> }
        ]

        //prepare data
        def startTime1 = new DateTime().toDate()
        def endTime1 = new DateTime().plusHours(1).toDate()
        def startTime2 = new DateTime().toDate()
        def endTime2 = new DateTime().plusHours(2).toDate()
        def subscription1 = createSubscription("1", startTime1 + 1, endTime1 + 1)
        createSubscription("0", startTime1, endTime1)
        def subscription3 = createSubscription("1", startTime2 + 2, endTime2 + 2, "48")
        def extraCourt = new Court(name: RandomStringUtils.randomAlphabetic(10),
                facility: subscription3.customer.facility, sport: TestUtils.createSport())
                .save(failOnError: true, flush: true)
        def extraSlot = new Slot(court: extraCourt, startTime: startTime2 + 3, endTime: endTime2 + 3,
                booking: new Booking(customer: subscription3.customer))
                .save(failOnError: true, flush: true)
        subscription3.addToSlots(extraSlot).save(failOnError: true, flush: true)

        //verify initial state
        assert !BookingCancelTicket.count()
        Booking booking1 = subscription1.slots.iterator().next().booking
        def slotsIterator = subscription3.slots.iterator()
        Booking booking2 = slotsIterator.next().booking
        Booking booking3 = slotsIterator.next().booking
        assert !booking1.dateReminded
        assert !booking2.dateReminded
        assert !booking3.dateReminded

        bookingReminderJob.execute()

        //Verify that there are two Booking tickets and two Booking of three became reminded
        assert 2 == BookingCancelTicket.count()
        booking1 = Booking.get(booking1.id)
        booking2 = Booking.get(booking2.id)
        booking3 = Booking.get(booking3.id)
        assert booking1.dateReminded
        assert booking2.dateReminded
        assert !booking3.dateReminded
    }

    private Subscription createSubscription(String reminderValue, Date start, Date end, String reminderHoursBefore = null) {
        def facility = new Facility(name: RandomStringUtils.randomAlphabetic(10),
                shortname: RandomStringUtils.randomAlphabetic(10), active: true, bookable: true,
                bookingRuleNumDaysBookable: 10, boxnet: false, lat: 0.0d, lng: 0.0d, vat: 0,
                municipality: Municipality.first(), country: "SV", email: "facility@matchi.se")
        facility.setFacilityProperty(FacilityProperty.FacilityPropertyKey.FEATURE_SUBSCRIPTION_REMINDER, reminderValue)
        if(reminderHoursBefore){
            facility.setFacilityProperty(FacilityProperty.FacilityPropertyKey.SUBSCRIPTION_REMINDER_HOURS, reminderHoursBefore)
        }
        facility.save(failOnError: true, flush: true)
        def customer = new Customer(facility: facility, number: Customer.count() + 1)
                .save(failOnError: true, flush: true)
        def court = new Court(name: RandomStringUtils.randomAlphabetic(10), facility: facility, sport: TestUtils.createSport())
                .save(failOnError: true, flush: true)
        def slot = new Slot(court: court, startTime: start, endTime: end,
                booking: new Booking(customer: customer))
                .save(failOnError: true, flush: true)
        def bookingGroup = new BookingGroup(type: BookingGroupType.SUBSCRIPTION)
                .save(failOnError: true, flush: true)
        new Subscription(customer: customer, bookingGroup: bookingGroup, court: court,
                weekday: 1, time: new LocalTime(slot.startTime)).addToSlots(slot)
                .save(failOnError: true, flush: true)
    }
}
