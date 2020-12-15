package com.matchi

import com.matchi.invoice.InvoiceRow
import com.matchi.price.Price
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.joda.time.DateTime
import org.junit.Before

@TestFor(Subscription)
@Mock([Subscription, Booking, BookingGroup, Slot])
class SubscriptionTests {

    Subscription subscription
    def mockPriceList

    @Before
    public void setUp() {
        mockDomain(BookingGroup)
        mockForConstraintsTests(InvoiceRow)
        subscription = new Subscription()
        subscription.slots = new TreeSet()
        mockPriceList = mockFor(PriceList)
        subscription.bookingGroup = new BookingGroup()
    }

    void testGetPriceReturnsRightAmount() {
        def numberOfBookings = 3
        def pricePerBooking = new BigDecimal(100)
        def expectedSubscriptionPrice = 300

        // mock price list
        mockPriceList.demand.getBookingPrice(1..3) { def user, def slot ->
            return new Price(price: pricePerBooking)
        }

        // add bookings to subscription
        numberOfBookings.times {
            def booking = createBooking(new Date() + it, new Date() + it + 1)
            subscription.bookingGroup.addToBookings(booking)
            subscription.addToSlots(booking.slot)
        }

        assert subscription.getPrice(mockPriceList.createMock()) == expectedSubscriptionPrice
    }

    void testGetSubscriptionDescription() {
        subscription.slots << new Slot(startTime: new DateTime(2013,1,1,12,0).toDate(),
                endTime: new DateTime(2013,2,1,12,0).toDate())
        subscription.slots << new Slot(startTime: new DateTime(2013,3,18,13,0).toDate(),
                endTime: new DateTime(2013,4,18,13,0).toDate())
        assert "Abonnemang (2013-01-01 - 2013-03-18)" == subscription.createInvoiceDescription()
    }

    void testGetSubscriptionDescriptionWithTemplate() {
        subscription.slots << new Slot(startTime: new DateTime(2013,1,1,12,0).toDate(),
                endTime: new DateTime(2013,2,1,12,0).toDate())
        subscription.slots << new Slot(startTime: new DateTime(2013,3,18,13,0).toDate(),
                endTime: new DateTime(2013,4,18,13,0).toDate())
        assert "Subscription 2013-01-01 - 2013-03-18" == subscription.createInvoiceDescription("Subscription %s")
    }

    void testCreateInvoiceRow() {
        def numberOfBookings = 3
        def pricePerBooking = new BigDecimal(100)
        def expectedSubscriptionPrice = 300

        User createdBy = new User(email: "admin@matchi.se")
        Customer subscriptionOwner = new Customer(user: new User(email: "user@matchi.se"));

        subscription.customer = subscriptionOwner
        // mock price list
        mockPriceList.demand.getBookingPrice(1..3) { def user, def slot ->
            return new Price(price: pricePerBooking)
        }

        // add bookings to subscription
        numberOfBookings.times { subscription.bookingGroup.addToBookings(createBooking()) }

        subscription.slots << new Slot(startTime: new DateTime(2013,1,1,12,0).toDate(),
                endTime: new DateTime(2013,1,2,12,0).toDate())
        subscription.slots << new Slot(startTime: new DateTime(2013,1,2,12,0).toDate(),
                endTime: new DateTime(2013,1,3,12,0).toDate())
        subscription.slots << new Slot(startTime: new DateTime(2013,3,18,13,0).toDate(),
                endTime: new DateTime(2013,4,18,13,0).toDate())

        def row = subscription.createInvoiceRow(mockPriceList.createMock(), createdBy,
                subscription.createInvoiceDescription(), new BigDecimal(0))

        assert row != null
        assert row.validate()
    }

    def createBooking(start = new Date(), end = new Date()) {
        return new Booking(slot: new Slot(startTime: start, endTime: end))
    }
}
