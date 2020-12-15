package com.matchi

import com.matchi.membership.Membership
import com.matchi.orders.Order
import org.joda.time.LocalDate
import org.junit.Test

import static com.matchi.TestUtils.*

class MembershipIntegrationTests extends GroovyTestCase {

    @Test
    void testGetRemotePayablesForOrders() {
        User user = createUser()
        Facility facility = createFacility()
        Customer customer = createCustomer(facility, user.email)

        LocalDate startDate = new LocalDate().minusMonths(2)
        LocalDate endDate = startDate.plusYears(1)
        Membership membership = createMembership(customer, startDate, endDate, endDate)

        LocalDate startDate2 = new LocalDate().minusYears(1)
        LocalDate endDate2 = startDate2.plusMonths(11)
        Membership membership2 = createMembership(customer, startDate2, endDate2, endDate2)

        Order order = membership.order
        order.price = 100
        order.origin = Order.ORIGIN_FACILITY
        order.status = Order.Status.NEW
        order.save(flush: true, failOnError: true)

        Order order2 = membership2.order
        order2.price = 100
        order2.origin = Order.ORIGIN_FACILITY
        order2.status = Order.Status.NEW
        order2.save(flush: true, failOnError: true)

        List<Order> result = Membership.getRemotePayablesForOrders([order, order2])
        assert result == [order] // Since the second membership was too old
    }

    @Test
    void testGetRemotePayablesForOrdersOneNotRemotePayable() {
        User user = createUser()
        Facility facility = createFacility()
        Customer customer = createCustomer(facility, user.email)

        LocalDate startDate = new LocalDate().minusMonths(2)
        LocalDate endDate = startDate.plusYears(1)
        Membership membership = createMembership(customer, startDate, endDate, endDate)

        LocalDate startDate2 = new LocalDate().minusYears(1)
        LocalDate endDate2 = startDate2.plusYears(2)
        Membership membership2 = createMembership(customer, startDate2, endDate2, endDate2)

        Order order = membership.order
        order.price = 100
        order.origin = Order.ORIGIN_FACILITY
        order.status = Order.Status.NEW
        order.save(flush: true, failOnError: true)

        Order order2 = membership2.order
        order2.price = 100
        order2.origin = Order.ORIGIN_FACILITY
        order2.status = Order.Status.NEW
        order2.save(flush: true, failOnError: true)

        List<Order> result = Membership.getRemotePayablesForOrders([order, order2])
        assert result == [order, order2]

        // But now!!
        order2.status = Order.Status.COMPLETED
        order2.save(flush: true, failOnError: true)

        result = Membership.getRemotePayablesForOrders([order, order2])
        assert result == [order]
    }

    @Test
    void testGetRemotePayablesForOrdersOneNotActivated() {
        User user = createUser()
        Facility facility = createFacility()
        Customer customer = createCustomer(facility, user.email)

        LocalDate startDate = new LocalDate().minusMonths(2)
        LocalDate endDate = startDate.plusYears(1)
        Membership membership = createMembership(customer, startDate, endDate, endDate)

        LocalDate startDate2 = new LocalDate().minusYears(1)
        LocalDate endDate2 = startDate2.plusYears(2)
        Membership membership2 = createMembership(customer, startDate2, endDate2, endDate2)

        Order order = membership.order
        order.price = 100
        order.origin = Order.ORIGIN_FACILITY
        order.status = Order.Status.NEW
        order.save(flush: true, failOnError: true)

        Order order2 = membership2.order
        order2.price = 100
        order2.origin = Order.ORIGIN_WEB
        order2.status = Order.Status.NEW
        order2.save(flush: true, failOnError: true)

        List<Order> result = Membership.getRemotePayablesForOrders([order, order2])
        assert result == [order, order2]

        // But now!!
        membership2.activated = false
        membership2.save(flush: true, failOnError: true)

        result = Membership.getRemotePayablesForOrders([order, order2])
        assert result == [order]
    }

}
