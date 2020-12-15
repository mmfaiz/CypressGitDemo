package com.matchi
import com.matchi.enums.BookingGroupType
import com.matchi.orders.AdyenOrderPayment

import com.matchi.orders.Order
import com.matchi.orders.OrderPayment
import com.matchi.payment.PaymentStatus
import grails.test.GrailsUnitTestCase
import org.junit.Before
import org.junit.Test

class ColorFetcherTests extends GrailsUnitTestCase {

    def customer

    @Before
    void setUp() {
        customer = new Customer(number: 1)
    }

    @Test
    public void testSubscriptionIsBlue() {
        BookingGroup group = new BookingGroup([type: BookingGroupType.SUBSCRIPTION])
        Subscription subscription = new Subscription()
        subscription.id = 1
        subscription.customer = customer

        group.subscription = subscription

        Booking booking = new Booking()
        booking.customer = customer
        booking.group = group

        Slot slot = new Slot()
        slot.booking = booking
        slot.subscription = subscription

        assert ColorFetcher.blue == ColorFetcher.getFacilityColor(slot)
    }

    @Test
    public void testActivityIsPurple() {
        BookingGroup group = new BookingGroup([type: BookingGroupType.ACTIVITY])

        Booking booking = new Booking()
        booking.customer = customer
        booking.group = group

        Slot slot = new Slot()
        slot.booking = booking

        assert ColorFetcher.purple == ColorFetcher.getFacilityColor(slot)
    }
    @Test
    void testCanceledSubscriptionBookingIsLightBlue() {

        Slot slot = new Slot()
        slot.subscription = new Subscription()
        slot.subscription.id = 1

        assert ColorFetcher.lightBlue == ColorFetcher.getFacilityColor(slot)
    }

    @Test
    public void testNewCustomerBookingOnCanceledSubscriptionBookingIsGreen() {
        BookingGroup group = new BookingGroup([type: BookingGroupType.SUBSCRIPTION])
        Subscription subscription = new Subscription()
        subscription.customer = customer

        group.subscription = subscription

        Booking booking = new Booking()
        booking.customer = new Customer(number: 2)
        booking.order = new Order(price: 100, payments: [new AdyenOrderPayment(amount: 100, status: OrderPayment.Status.CAPTURED)])
        //booking.group = group

        Slot slot = new Slot()
        slot.booking = booking
        slot.subscription = subscription

        assert subscription.customer != booking.customer
        assert ColorFetcher.green == ColorFetcher.getFacilityColor(slot)
    }
    @Test
    void testNewUserBookingOnWithDefaultGroupOnCanceledSubscriptionBookingIsGreenWhenPaid() {
        BookingGroup group = new BookingGroup([type: BookingGroupType.SUBSCRIPTION])
        Subscription subscription = new Subscription()
        subscription.customer = new Customer(id: 1)

        group.subscription = subscription

        Booking booking = new Booking()
        booking.customer = new Customer(id: 2)
        booking.order = new Order(price: 100, payments: [new AdyenOrderPayment(amount: 100, status: OrderPayment.Status.CAPTURED)])
        BookingGroup g2 = new BookingGroup([type: BookingGroupType.DEFAULT])
        booking.group = g2

        Slot slot = new Slot()
        slot.booking = booking
        slot.subscription = subscription

        assertNotSame(subscription.customer, booking.customer)
        assertEquals(ColorFetcher.green, ColorFetcher.getFacilityColor(slot))
    }
    @Test
    void testNewUserBookingOnWithDefaultGroupOnCanceledSubscriptionBookingIsGreenWhenPaymentOK() {
        BookingGroup group = new BookingGroup([type: BookingGroupType.SUBSCRIPTION])
        Subscription subscription = new Subscription()
        subscription.customer = new Customer(id: 1)

        group.subscription = subscription

        Booking booking = new Booking()
        booking.customer = new Customer(id: 2)
        booking.payment = new Payment(status: PaymentStatus.OK)
        BookingGroup g2 = new BookingGroup([type: BookingGroupType.DEFAULT])
        booking.group = g2

        Slot slot = new Slot()
        slot.booking = booking
        slot.subscription = subscription

        assertNotSame(subscription.customer, booking.customer)
        assertEquals(ColorFetcher.green, ColorFetcher.getFacilityColor(slot))
    }
    @Test
    void testNewUserBookingOnWithDefaultGroupOnCanceledSubscriptionBookingIsLightGreenWhenPaymentPARTLY() {
        BookingGroup group = new BookingGroup([type: BookingGroupType.SUBSCRIPTION])
        Subscription subscription = new Subscription()
        subscription.customer = new Customer(id: 1)

        group.subscription = subscription

        Booking booking = new Booking()
        booking.customer = new Customer(id: 2)
        booking.payment = new Payment(status: PaymentStatus.PARTLY)
        BookingGroup g2 = new BookingGroup([type: BookingGroupType.DEFAULT])
        booking.group = g2

        Slot slot = new Slot()
        slot.booking = booking
        slot.subscription = subscription

        assertNotSame(subscription.customer, booking.customer)
        assertEquals(ColorFetcher.lightGreen, ColorFetcher.getFacilityColor(slot))
    }

    @Test
    void testNotAvailableIsRed() {
        BookingGroup group = new BookingGroup([type: BookingGroupType.NOT_AVAILABLE])

        Booking booking = new Booking()
        booking.customer = customer
        booking.group = group

        Slot slot = new Slot()
        slot.booking = booking

        assert ColorFetcher.lightGrey == ColorFetcher.getFacilityColor(slot)
    }

    @Test
    void testBookingsInDefaultBookingGroupGetsBookingColors() {
        BookingGroup group = new BookingGroup([type: BookingGroupType.DEFAULT])

        Booking booking = new Booking()
        booking.customer = customer
        booking.order = new Order(price: 100, payments: [])
        booking.group = group

        Slot slot = new Slot()
        slot.booking = booking

        assert ColorFetcher.yellow == ColorFetcher.getFacilityColor(slot)

        booking.order = new Order(price: 100, payments: [new AdyenOrderPayment(amount: 100, status: OrderPayment.Status.CAPTURED)])
        assert ColorFetcher.green == ColorFetcher.getFacilityColor(slot)
    }

    @Test
    void testNotPayedIsYellow() {
        Booking booking = new Booking()
        booking.customer = customer
        booking.order = new Order(price: 100, payments: [])

        Slot slot = new Slot()
        slot.booking = booking

        assert ColorFetcher.yellow == ColorFetcher.getFacilityColor(slot)
    }

    @Test
    void testPayedIsGreen() {
        Booking booking = new Booking()
        booking.customer = customer
        booking.order = new Order(price: 100, payments: [new AdyenOrderPayment(amount: 100, status: OrderPayment.Status.CAPTURED)])

        Slot slot = new Slot()
        slot.booking = booking

        assert ColorFetcher.green == ColorFetcher.getFacilityColor(slot)
    }
    @Test
    void testPaymentNotStartedIsYellow() {
        Booking booking = new Booking()
        booking.customer = new Customer()

        Payment payment = new Payment()
        payment.status = PaymentStatus.PENDING
        booking.payment = payment

        Slot slot = new Slot()
        slot.booking = booking

        assert ColorFetcher.yellow == ColorFetcher.getFacilityColor(slot)
    }
    @Test
    void testPaymentCompletedIsGreen() {
        Booking booking = new Booking()
        customer.user = new User()
        booking.customer = customer

        Payment payment = new Payment()
        payment.status = PaymentStatus.OK
        booking.payment = payment

        Slot slot = new Slot()
        slot.booking = booking

        assert ColorFetcher.green == ColorFetcher.getFacilityColor(slot)
    }
    @Test
    void testPaymentPartlyPaidIsLightGreen() {
        Booking booking = new Booking()
        customer.user = new User()
        booking.customer = customer

        Payment payment = new Payment()
        payment.status = PaymentStatus.PARTLY
        booking.payment = payment

        Slot slot = new Slot()
        slot.booking = booking

        assert ColorFetcher.lightGreen == ColorFetcher.getFacilityColor(slot)
    }
}
