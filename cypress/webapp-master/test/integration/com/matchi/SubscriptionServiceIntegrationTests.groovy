package com.matchi

import com.matchi.enums.BookingGroupType
import com.matchi.orders.Order
import grails.test.mixin.Mock
import org.apache.commons.lang.RandomStringUtils
import org.joda.time.DateTime
import org.joda.time.LocalTime

import static com.matchi.TestUtils.createUser

/**
 * @author Sergei Shushkevich
 */
class SubscriptionServiceIntegrationTests extends GroovyTestCase {

    def subscriptionService
    def springSecurityService

    void testGetSubscriptionsToRemind() {
        def startTime1 = new DateTime().withHourOfDay(9).toDate()
        def endTime1 = new DateTime().withHourOfDay(10).toDate()
        def startTime2 = new DateTime().withHourOfDay(11).toDate()
        def endTime2 = new DateTime().withHourOfDay((12)).toDate()
        def subscription1 = createSubscription("1", startTime1, endTime1)
        createSubscription("0", startTime1, endTime1)
        def subscription3 = createSubscription("1", startTime2, endTime2)
        def extraCourt = new Court(name: RandomStringUtils.randomAlphabetic(10),
                facility: subscription3.customer.facility, sport: TestUtils.createSport())
                .save(failOnError: true, flush: true)
        def extraSlot = new Slot(court: extraCourt, startTime: startTime2, endTime: endTime2,
                booking: new Booking(customer: subscription3.customer))
                .save(failOnError: true, flush: true)
        subscription3.addToSlots(extraSlot).save(failOnError: true, flush: true)

        def result = subscriptionService.getSubscriptionsToRemind(startTime1, endTime1, subscription1.customer.facility)

        assert 1 == result.size()
        assert subscription1 == result[0]

        result = subscriptionService.getSubscriptionsToRemind(startTime2, endTime2, subscription3.customer.facility)

        assert 1 == result.size()
        assert subscription3 == result[0]

        def booking = subscription1.slots.iterator().next().booking
        booking.dateReminded = true
        booking.save(failOnError: true, flush: true)

        result = subscriptionService.getSubscriptionsToRemind(startTime1, endTime1, subscription1.customer.facility)

        assert !result
    }

    void testSave() {
        def user = createUser()
        springSecurityService.reauthenticate user.email
        def subscription = createSubscription("0", new Date(), new Date() + 1, false)
        def slot2 = new Slot(court: subscription.court, startTime: new Date() + 1, endTime: new Date() + 2,
                booking: new Booking(customer: subscription.customer)).save(failOnError: true, flush: true)
        subscription.addToSlots(slot2)
        subscription.save(failOnError: true, flush: true)

        subscriptionService.save(subscription)

        assert 1 == Subscription.count()
        subscription = Subscription.first()
        assert 2 == subscription.slots.size()
        assert 1 == subscription.slots.count { it.booking.order }
        assert 1 == subscription.bookingGroup.bookings.size()
        def order = subscription.bookingGroup.bookings.iterator().next().order
        assert order
        assert Order.Article.SUBSCRIPTION_BOOKING == order.article
    }

    void testCreateSubscriptionOrder() {
        def user = createUser()
        springSecurityService.reauthenticate user.email
        def subscription = createSubscription("0", new Date(), new Date() + 1)

        def order = subscriptionService.createSubscriptionOrder(
                subscription, subscription.customer)

        assert order
        assert 1 == Order.countByIssuer(user)
        assert Order.Status.NEW == order.status
        assert user.id == order.issuer.id
        assert subscription.customer.id == order.customer.id
        assert subscription.customer.facility.id == order.facility.id
        assert Order.ORIGIN_FACILITY == order.origin
        assert Order.Article.SUBSCRIPTION == order.article
        assert subscription.id.toString() == order.metadata.subscriptionId
    }

    private Subscription createSubscription(String reminderValue, Date start, Date end, boolean save = true) {
        def facility = new Facility(name: RandomStringUtils.randomAlphabetic(10),
                shortname: RandomStringUtils.randomAlphabetic(10), active: true, bookable: true,
                bookingRuleNumDaysBookable: 10, boxnet: false, lat: 0.0d, lng: 0.0d, vat: 0,
                municipality: Municipality.first(), country: "SV", email: 'facility@matchi.se')
        facility.setFacilityProperty(FacilityProperty.FacilityPropertyKey.FEATURE_SUBSCRIPTION_REMINDER, reminderValue)
        facility.save(failOnError: true, flush: true)
        def customer = new Customer(facility: facility, number: Customer.count() + 1)
                .save(failOnError: true, flush: true)
        def court = new Court(name: RandomStringUtils.randomAlphabetic(10), facility: facility, sport: TestUtils.createSport())
                .save(failOnError: true, flush: true)
        def slot = new Slot(court: court, startTime: start, endTime: end)
                .save(failOnError: true, flush: true)
        def bookingGroup = new BookingGroup(type: BookingGroupType.SUBSCRIPTION)
                .addToBookings(new Booking(slot: slot, customer: customer))
                .save(failOnError: true, flush: true)
        def subscription = new Subscription(customer: customer, bookingGroup: bookingGroup, court: court,
                weekday: 1, time: new LocalTime(slot.startTime)).addToSlots(slot)
        if (save) {
            subscription.save(failOnError: true, flush: true)
        }
        subscription
    }
}
