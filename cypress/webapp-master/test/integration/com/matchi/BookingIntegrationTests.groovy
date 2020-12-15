package com.matchi

import com.matchi.enums.BookingGroupType
import com.matchi.orders.Order
import org.joda.time.DateTime
import org.junit.Test

import static com.matchi.TestUtils.*

class BookingIntegrationTests extends GroovyTestCase {

    @Test
    void testRemotePayableNamedQueryNoOrdersInvolved() {
        Facility facility = createFacility()
        User user = createUser()
        Customer customer = createCustomer(facility, user.email)

        Court court = createCourt(facility)

        // Is currently playing
        Date startTime = new DateTime().minusMinutes(30).toDate()
        Date endTime = new DateTime().plusMinutes(30).toDate()
        Slot slot = createSlot(court, startTime, endTime)

        Booking booking = createBooking(customer, slot)

        shouldFail {
            Booking.remotePayables().list()
        }

        Order order = createOrder(user, facility, Order.Article.BOOKING)
        order.origin = Order.ORIGIN_FACILITY
        order.save(flush: true, failOnError: true)

        booking.order = order
        booking.save(flush: true, failOnError: true)

        shouldFail {
            Booking.remotePayables().list()
        }

        // But now it should work
        List<Booking> result = Booking.remotePayables([order]).list()
        assert result.size() == 1
        assert result == [booking]

        List<Order> orders = booking.getRemotePayablesForOrders([order])
        assert orders.size() == 1
        assert orders == [order]
    }

    @Test
    void testRemotePayableNamedQueryPastSlot() {
        Facility facility = createFacility()
        User user = createUser()
        Customer customer = createCustomer(facility, user.email)

        Court court = createCourt(facility)

        // Past slot
        Date startTime = new DateTime().minusMinutes(60).toDate()
        Date endTime = new DateTime().minusMinutes(30).toDate()
        Slot slot = createSlot(court, startTime, endTime)

        Booking booking = createBooking(customer, slot)

        Order order = createOrder(user, facility, Order.Article.BOOKING)
        order.origin = Order.ORIGIN_FACILITY
        order.save(flush: true, failOnError: true)

        booking.order = order
        booking.save(flush: true, failOnError: true)

        // So no :(
        assert !Booking.remotePayables([order]).list()
        assert !booking.getRemotePayablesForOrders([order])
    }

    @Test
    void testRemotePayableNamedQueryNotDefault() {
        Facility facility = createFacility()
        User user = createUser()
        Customer customer = createCustomer(facility, user.email)

        Court court = createCourt(facility)

        // Currently playing
        Date startTime = new DateTime().minusMinutes(60).toDate()
        Date endTime = new DateTime().plusMinutes(30).toDate()
        Slot slot = createSlot(court, startTime, endTime)

        Booking booking = createBooking(customer, slot)

        Order order = createOrder(user, facility, Order.Article.BOOKING)
        order.origin = Order.ORIGIN_FACILITY
        order.save(flush: true, failOnError: true)

        booking.order = order
        booking.save(flush: true, failOnError: true)

        // Without a group
        assert Booking.remotePayables([order]).list()
        assert booking.getRemotePayablesForOrders([order])

        // Testing different types
        BookingGroupType.list().each { BookingGroupType bgt ->
            BookingGroup bg = new BookingGroup(type: bgt)
            bg.addToBookings(booking)
            bg.save(flush: true, failOnError: true)

            booking.group = bg
            booking.save(flush: true, failOnError: true)

            assert bgt == BookingGroupType.DEFAULT ? Booking.remotePayables([order]).list() : !Booking.remotePayables([order]).list()
            assert bgt == BookingGroupType.DEFAULT ? booking.getRemotePayablesForOrders([order]) : !booking.getRemotePayablesForOrders([order])

            booking.group = null
            booking.save(flush: true, failOnError: true)

            bg.removeFromBookings(booking)
            bg.delete(flush: true, failOnError: true)
        }
    }

    @Test
    void testRemotePayableNamedQueryOrderFiltering() {
        Facility facility = createFacility()
        User user = createUser()
        Customer customer = createCustomer(facility, user.email)

        Court court = createCourt(facility)

        Date startTime = new DateTime().minusMinutes(60).toDate()
        Date endTime = new DateTime().plusMinutes(60).toDate()
        Slot slot = createSlot(court, startTime, endTime)

        Booking booking = createBooking(customer, slot)

        Order order = createOrder(user, facility, Order.Article.BOOKING)
        order.origin = Order.ORIGIN_FACILITY
        order.save(flush: true, failOnError: true)
        assert order.isRemotePayable()

        booking.order = order
        booking.save(flush: true, failOnError: true)

        Order order2 = createOrder(user, facility, Order.Article.BOOKING)
        order2.origin = Order.ORIGIN_FACILITY
        order2.save(flush: true, failOnError: true)
        assert order2.isRemotePayable()

        // Without the article existing, they should not return
        assert Booking.remotePayables([order, order2]).list() == [booking]
        assert booking.getRemotePayablesForOrders([order, order2]) == [order]

        Date startTime2 = new DateTime().plusMinutes(61).toDate()
        Date endTime2 = new DateTime().plusMinutes(121).toDate()
        Slot slot2 = createSlot(court, startTime2, endTime2)

        Booking booking2 = createBooking(customer, slot2)
        booking2.order = order2
        booking2.save(flush: true, failOnError: true)

        assert Booking.remotePayables([order]).list() == [booking]
        assert booking.getRemotePayablesForOrders([order]) == [order]

        assert Booking.remotePayables([order2]).list() == [booking2]
        assert booking.getRemotePayablesForOrders([order2]) == [order2]

        assert Booking.remotePayables([order, order2]).list() == [booking, booking2]
        assert booking.getRemotePayablesForOrders([order, order2]) == [order, order2]
    }
}
