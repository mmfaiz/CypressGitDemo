package com.matchi.orders

import com.matchi.*
import com.matchi.api.ListOrderCommand
import com.matchi.membership.Membership
import org.junit.Test

import static com.matchi.TestUtils.*

class OrderServiceIntegrationTests extends GroovyTestCase {

    @Test
    void testGetOrders() {
        OrderService service = new OrderService()
        service.dateUtil = new DateUtil()

        Facility facility = createFacility()
        Customer customer = createCustomer(facility, "sune@matchi.se", "Sune", "Andersson")
        customer.number = 123456
        customer.save(flush: true, failOnError: true)

        Slot slot = createSlot(createCourt(facility))
        Booking booking = createBooking(customer, slot)

        Order order = createOrder(createUser(), facility)
        order.origin = Order.ORIGIN_FACILITY
        order.price = new BigDecimal(100)
        order.save(flush: true, failOnError: true)

        booking.order = order

        ListOrderCommand cmd = new ListOrderCommand(q: "une ander")
        List<Order> orders = service.getOrders(cmd, facility.id)

        assert orders.size() == 1
        assert orders[0] == order

        cmd = new ListOrderCommand(q: "34")
        orders = service.getOrders(cmd, facility.id)

        assert orders.size() == 1
        assert orders[0] == order
    }

    @Test
    void testGetOrdersWithEmail() {
        OrderService service = new OrderService()
        service.dateUtil = new DateUtil()

        Facility facility = createFacility()
        Customer customer = createCustomer(facility, "sune@matchi.se", "Sune", "Andersson")
        customer.number = 123456
        customer.save(flush: true, failOnError: true)

        User user = createUser(customer.email)

        Slot slot = createSlot(createCourt(facility))
        Booking booking = createBooking(customer, slot)

        Order order = createOrder(createUser(), facility)
        order.origin = Order.ORIGIN_FACILITY
        order.price = new BigDecimal(100)
        order.save(flush: true, failOnError: true)

        booking.order = order

        ListOrderCommand cmd = new ListOrderCommand(email: "sune@matchi.se")
        List<Order> orders = service.getOrders(cmd, facility.id)

        assert orders.size() == 1
        assert orders[0] == order

        cmd = new ListOrderCommand(q: "34")
        orders = service.getOrders(cmd, facility.id)

        assert orders.size() == 1
        assert orders[0] == order

        cmd = new ListOrderCommand(email: "sune@matchi.s")
        orders = service.getOrders(cmd, facility.id)

        assert !orders

        // Email has precedence if supplied
        cmd = new ListOrderCommand(email: "sune@matchi.s", q: "sune@matchi.se")
        orders = service.getOrders(cmd, facility.id)

        assert !orders

        cmd = new ListOrderCommand(q: "une@matchi.s")
        orders = service.getOrders(cmd, facility.id)

        assert orders.size() == 1
        assert orders[0] == order

        order.price = new BigDecimal(0)
        order.save(flush: true, failOnError: true)

        cmd = new ListOrderCommand(q: "une@matchi.s")
        orders = service.getOrders(cmd, facility.id)
        assert !orders

        OrderPayment orderPayment = createAdyenOrderPayment(user, order, "", OrderPayment.Status.CAPTURED)
        orderPayment.amount = 100
        orderPayment.save(flush: true, failOnError: true)
        order.addToPayments(orderPayment)
        order.save(flush: true, failOnError: true)

        order.price = new BigDecimal(100)
        order.save(flush: true, failOnError: true)

        cmd = new ListOrderCommand(q: "une@matchi.s")
        orders = service.getOrders(cmd, facility.id)
        assert orders.size() == 1
        assert orders[0] == order

        orderPayment.amount = 50 // Now it will not be completely paid
        orderPayment.save(flush: true, failOnError: true)

        cmd = new ListOrderCommand(email: "sune@matchi.se")
        orders = service.getOrders(cmd, facility.id)
        assert orders.size() == 1
        assert orders[0] == order
    }

    @Test
    void testGetOrdersWithPaidFalse() {
        OrderService service = new OrderService()
        service.dateUtil = new DateUtil()

        Facility facility = createFacility()
        Customer customer = createCustomer(facility, "sune@matchi.se", "Sune", "Andersson")
        customer.number = 123456
        customer.save(flush: true, failOnError: true)

        User user = createUser(customer.email)

        Slot slot = createSlot(createCourt(facility))
        Booking booking = createBooking(customer, slot)

        Order order = createOrder(createUser(), facility)
        order.origin = Order.ORIGIN_FACILITY
        order.price = new BigDecimal(100)
        order.save(flush: true, failOnError: true)

        booking.order = order

        ListOrderCommand cmd = new ListOrderCommand(email: "sune@matchi.se", paid: false)
        List<Order> orders = service.getOrders(cmd, facility.id)

        assert orders.size() == 1
        assert orders[0] == order

        cmd = new ListOrderCommand(q: "34", paid: false)
        orders = service.getOrders(cmd, facility.id)

        assert orders.size() == 1
        assert orders[0] == order

        cmd = new ListOrderCommand(email: "sune@matchi.s", paid: false)
        orders = service.getOrders(cmd, facility.id)

        assert !orders

        // Email has precedence if supplied
        cmd = new ListOrderCommand(email: "sune@matchi.s", q: "sune@matchi.se", paid: false)
        orders = service.getOrders(cmd, facility.id)

        assert !orders

        cmd = new ListOrderCommand(q: "une@matchi.s", paid: false)
        orders = service.getOrders(cmd, facility.id)

        assert orders.size() == 1
        assert orders[0] == order

        order.price = new BigDecimal(0)
        order.save(flush: true, failOnError: true)

        cmd = new ListOrderCommand(q: "une@matchi.s", paid: false)
        orders = service.getOrders(cmd, facility.id)
        assert !orders // Not visible in both TRUE and FALSE

        OrderPayment orderPayment = createAdyenOrderPayment(user, order, "", OrderPayment.Status.CAPTURED)
        orderPayment.amount = 100
        orderPayment.save(flush: true, failOnError: true)
        order.addToPayments(orderPayment)
        order.save(flush: true, failOnError: true)

        order.price = new BigDecimal(100)
        order.save(flush: true, failOnError: true)

        cmd = new ListOrderCommand(q: "une@matchi.s", paid: false)
        orders = service.getOrders(cmd, facility.id)
        assert !orders // Since order is paid

        orderPayment.amount = 50 // Now it will not be completely paid
        orderPayment.save(flush: true, failOnError: true)

        cmd = new ListOrderCommand(email: "sune@matchi.se", paid: false)
        orders = service.getOrders(cmd, facility.id)

        assert orders.size() == 1
        assert orders[0] == order
    }

    @Test
    void testGetOrdersWithPaidTrue() {
        OrderService service = new OrderService()
        service.dateUtil = new DateUtil()

        Facility facility = createFacility()
        Customer customer = createCustomer(facility, "sune@matchi.se", "Sune", "Andersson")
        customer.number = 123456
        customer.save(flush: true, failOnError: true)

        User user = createUser(customer.email)

        Slot slot = createSlot(createCourt(facility))
        Booking booking = createBooking(customer, slot)

        Order order = createOrder(createUser(), facility)
        order.origin = Order.ORIGIN_FACILITY
        order.price = new BigDecimal(100)
        order.save(flush: true, failOnError: true)

        booking.order = order

        ListOrderCommand cmd = new ListOrderCommand(email: "sune@matchi.se", paid: true)
        List<Order> orders = service.getOrders(cmd, facility.id)
        assert !orders

        cmd = new ListOrderCommand(q: "34", paid: true)
        orders = service.getOrders(cmd, facility.id)
        assert !orders

        cmd = new ListOrderCommand(email: "sune@matchi.s", paid: true)
        orders = service.getOrders(cmd, facility.id)
        assert !orders

        // Email has precedence if supplied
        cmd = new ListOrderCommand(email: "sune@matchi.s", q: "sune@matchi.se", paid: true)
        orders = service.getOrders(cmd, facility.id)
        assert !orders

        cmd = new ListOrderCommand(q: "une@matchi.s", paid: true)
        orders = service.getOrders(cmd, facility.id)
        assert !orders

        order.price = new BigDecimal(0)
        order.save(flush: true, failOnError: true)

        cmd = new ListOrderCommand(q: "une@matchi.s", paid: true)
        orders = service.getOrders(cmd, facility.id)
        assert !orders

        OrderPayment orderPayment = createAdyenOrderPayment(user, order, "", OrderPayment.Status.CAPTURED)
        orderPayment.amount = 100
        orderPayment.save(flush: true, failOnError: true)
        order.addToPayments(orderPayment)
        order.save(flush: true, failOnError: true)

        order.price = new BigDecimal(100)
        order.save(flush: true, failOnError: true)

        cmd = new ListOrderCommand(q: "une@matchi.s", paid: true)
        orders = service.getOrders(cmd, facility.id)
        assert orders.size() == 1
        assert orders[0] == order

        orderPayment.amount = 50 // Now it will not be completely paid
        orderPayment.save(flush: true, failOnError: true)

        cmd = new ListOrderCommand(email: "sune@matchi.se", paid: true)
        orders = service.getOrders(cmd, facility.id)
        assert !orders // Since order is paid
    }

    @Test
    void testReplaceOrderWithFreshCopy() {
        OrderService service = new OrderService()

        User user = createUser()
        Facility facility = createFacility()
        Customer customer = createCustomer(facility, user.email)
        customer.user = user
        customer.save(flush: true, failOnError: true)

        Order order = createOrder(user, facility)
        order.customer = customer
        order.origin = Order.ORIGIN_FACILITY
        order.status = Order.Status.CANCELLED

        OrderPayment orderPayment = createAdyenOrderPayment(user, order, "", OrderPayment.Status.FAILED)
        order.addToPayments(orderPayment)
        order.save(flush: true, failOnError: true)

        assert order.isRemotePayable()

        Membership membership = createMembership(customer)
        membership.order = order
        membership.save(flush: true, failOnError: true)

        Order newOrder = service.replaceOrderWithFreshCopy(membership.order)

        assert membership.order.id != order.id

        assert newOrder.id != order.id
        assert newOrder.isRemotePayable()

        assert newOrder.article == order.article
        assert newOrder.origin == Order.ORIGIN_FACILITY
        assert newOrder.price == order.price
        assert newOrder.vat == order.vat
        assert newOrder.status == Order.Status.NEW
        assert newOrder.facility == order.facility && newOrder.facility == facility
        assert newOrder.customer == order.customer && newOrder.customer == customer
        assert newOrder.issuer == order.issuer
        assert newOrder.user == order.user
        assert newOrder.description == order.description
        assert newOrder.dateDelivery == order.dateDelivery
        assert !newOrder.payments

        assert Order.get(newOrder.id) == newOrder
    }

}
