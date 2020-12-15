package com.matchi

import com.matchi.membership.Membership
import com.matchi.orders.AdyenOrderPayment
import com.matchi.orders.Order
import com.matchi.orders.OrderPayment
import org.joda.time.DateTime
import org.joda.time.LocalDate
import org.junit.Test
import static com.matchi.TestUtils.*
import static com.matchi.TestUtils.createAdyenOrderPayment
import static com.matchi.TestUtils.createCourt

class RemotePaymentServiceIntegrationTests extends GroovyTestCase {

    def remotePaymentService

    @Test
    void testGetRemotePayableCustomersForUser() {
        Facility facility = createFacility()
        User user = createUser()
        Customer customer = createCustomer(facility, user.email)
        customer.user = user
        customer.save(flush: true, failOnError: true)

        assert !facility.hasEnabledRemotePayments()
        assert !remotePaymentService.getRemotePayableCustomersFor(user)

        FacilityProperty facilityProperty = new FacilityProperty(key: FacilityProperty.FacilityPropertyKey.FEATURE_REMOTE_PAYMENT_MEMBERSHIP.toString(), value: "1")
        facilityProperty.facility = facility
        facilityProperty.save(flush: true, failOnError: true)

        facility.facilityProperties = [facilityProperty]
        facility.save(flush: true, failOnError: true)

        assert facility.hasEnabledRemotePayments()
        assert remotePaymentService.getRemotePayableCustomersFor(user) == [customer]
    }

    @Test
    void testGetRemotePayableOrdersFor() {
        Facility facility = createFacility()
        FacilityProperty facilityProperty = new FacilityProperty(key: FacilityProperty.FacilityPropertyKey.FEATURE_REMOTE_PAYMENT_MEMBERSHIP.toString(), value: "1")
        facilityProperty.facility = facility
        facilityProperty.save(flush: true, failOnError: true)

        facility.facilityProperties = [facilityProperty]
        facility.save(flush: true, failOnError: true)

        assert facility.hasEnabledRemotePayments()
        assert facility.getRemotePaymentArticles() == [Order.Article.MEMBERSHIP]
        assert facility.hasEnabledRemotePaymentsFor(Order.Article.MEMBERSHIP)
        assert !facility.hasEnabledRemotePaymentsFor(Order.Article.BOOKING)
        assert !facility.hasEnabledRemotePaymentsFor(null)

        User user = createUser()
        User admin = createUser('admin@matchi.se')
        Customer customer = createCustomer(facility, user.email)
        customer.user = user
        customer.save(flush: true, failOnError: true)

        Order order = createOrder(admin, facility, Order.Article.MEMBERSHIP)
        order.customer = customer
        order.origin = Order.ORIGIN_FACILITY
        order.save(flush: true, failOnError: true)

        assert !remotePaymentService.getRemotePayableOrdersFor(user)

        LocalDate membershipStartDate = new LocalDate()
        LocalDate membershipEndDate = membershipStartDate.plusYears(1)
        LocalDate gracetimeEndDate = membershipEndDate.plusMonths(1)
        Membership membership = createMembership(customer, membershipStartDate, membershipEndDate, gracetimeEndDate)
        membership.order = order
        membership.save(flush: true, failOnError: true)

        assert membership.isRemotePayable()

        List<Order> result = remotePaymentService.getRemotePayableOrdersFor(user)
        assert result.size() == 1
        assert order in result

        Court court = createCourt(facility)
        Slot slot = createSlot(court, new Date(), new DateTime().plusHours(1).toDate())
        Booking booking = createBooking(customer, slot)

        assert booking.isRemotePayable()

        Order orderBooking = createOrder(user, facility, Order.Article.BOOKING)
        orderBooking.customer = customer
        orderBooking.origin = Order.ORIGIN_FACILITY
        orderBooking.save(flush: true, failOnError: true)

        booking.order = orderBooking
        booking.save(flush: true, failOnError: true)

        // Not yet activated booking for that facility
        result = remotePaymentService.getRemotePayableOrdersFor(user)
        assert result.size() == 1
        assert order in result

        FacilityProperty facilityProperty2 = new FacilityProperty(key: FacilityProperty.FacilityPropertyKey.FEATURE_REMOTE_PAYMENT_BOOKING.toString(), value: "1")
        facilityProperty2.facility = facility
        facilityProperty2.save(flush: true, failOnError: true)

        facility.facilityProperties = [facilityProperty, facilityProperty2]
        facility.save(flush: true, failOnError: true)

        result = remotePaymentService.getRemotePayableOrdersFor(user)
        assert result.size() == 2
        assert order in result
        assert orderBooking in result
    }

    @Test
    void testGetPotentialOrdersMembership() {
        Facility facility = createFacility()
        FacilityProperty facilityProperty = new FacilityProperty(key: FacilityProperty.FacilityPropertyKey.FEATURE_REMOTE_PAYMENT_MEMBERSHIP.toString(), value: "1")
        facilityProperty.facility = facility
        facilityProperty.save(flush: true, failOnError: true)

        facility.facilityProperties = [facilityProperty]
        facility.save(flush: true, failOnError: true)

        assert facility.hasEnabledRemotePayments()
        assert facility.getRemotePaymentArticles() == [Order.Article.MEMBERSHIP]
        assert facility.hasEnabledRemotePaymentsFor(Order.Article.MEMBERSHIP)
        assert !facility.hasEnabledRemotePaymentsFor(Order.Article.BOOKING)
        assert !facility.hasEnabledRemotePaymentsFor(null)

        User user = createUser()
        User admin = createUser('admin@matchi.se')
        Customer customer = createCustomer(facility, user.email)
        customer.user = user
        customer.save(flush: true, failOnError: true)

        Order order = createOrder(admin, facility, Order.Article.MEMBERSHIP)
        order.customer = customer
        order.origin = Order.ORIGIN_FACILITY
        order.save(flush: true, failOnError: true)

        List<Order> result = remotePaymentService.getPotentialOrders([customer])
        assert result.size() == 1
        assert order in result

        Customer customer2 = createCustomer(facility, admin.email)
        assert !remotePaymentService.getPotentialOrders([customer2])
    }

    @Test
    void testGetPotentialOrdersBooking() {
        Facility facility = createFacility()
        FacilityProperty facilityProperty = new FacilityProperty(key: FacilityProperty.FacilityPropertyKey.FEATURE_REMOTE_PAYMENT_BOOKING.toString(), value: "1")
        facilityProperty.facility = facility
        facilityProperty.save(flush: true, failOnError: true)

        facility.facilityProperties = [facilityProperty]
        facility.save(flush: true, failOnError: true)

        User user = createUser()
        Customer customer = createCustomer(facility, user.email)
        customer.user = user
        customer.save(flush: true, failOnError: true)

        Order orderBooking = createOrder(user, facility, Order.Article.BOOKING)
        orderBooking.customer = customer
        orderBooking.origin = Order.ORIGIN_FACILITY
        orderBooking.save(flush: true, failOnError: true)

        List<Order> result = remotePaymentService.getPotentialOrders([customer])
        assert result.size() == 1
        assert orderBooking in result

        Customer customer2 = createCustomer(facility, "anotherSune@matchi.se")
        assert !remotePaymentService.getPotentialOrders([customer2])
    }

    @Test
    void testGetPotentialOrdersNoCustomers() {
        Facility facility = createFacility()
        FacilityProperty facilityProperty = new FacilityProperty(key: FacilityProperty.FacilityPropertyKey.FEATURE_REMOTE_PAYMENT_BOOKING.toString(), value: "1")
        facilityProperty.facility = facility
        facilityProperty.save(flush: true, failOnError: true)

        facility.facilityProperties = [facilityProperty]
        facility.save(flush: true, failOnError: true)

        User user = createUser()
        Customer customer = createCustomer(facility, user.email)
        customer.user = user
        customer.save(flush: true, failOnError: true)

        Order orderBooking = createOrder(user, facility, Order.Article.BOOKING)
        orderBooking.customer = customer
        orderBooking.origin = Order.ORIGIN_FACILITY
        orderBooking.save(flush: true, failOnError: true)

        assert !remotePaymentService.getPotentialOrders([])
    }

    @Test
    void testGetPotentialOrdersFree() {
        Facility facility = createFacility()
        FacilityProperty facilityProperty = new FacilityProperty(key: FacilityProperty.FacilityPropertyKey.FEATURE_REMOTE_PAYMENT_BOOKING.toString(), value: "1")
        facilityProperty.facility = facility
        facilityProperty.save(flush: true, failOnError: true)

        facility.facilityProperties = [facilityProperty]
        facility.save(flush: true, failOnError: true)

        User user = createUser()
        Customer customer = createCustomer(facility, user.email)
        customer.user = user
        customer.save(flush: true, failOnError: true)

        Order orderBooking = createOrder(user, facility, Order.Article.BOOKING)
        orderBooking.customer = customer
        orderBooking.origin = Order.ORIGIN_FACILITY
        orderBooking.save(flush: true, failOnError: true)

        assert remotePaymentService.getPotentialOrders([customer]) == [orderBooking]

        orderBooking.price = 0
        orderBooking.save(flush: true, failOnError: true)
        assert !remotePaymentService.getPotentialOrders([customer])
    }

    @Test
    void testGetPotentialOrdersNoCustomersDifferentCustomers() {
        Facility facility = createFacility()
        FacilityProperty facilityProperty = new FacilityProperty(key: FacilityProperty.FacilityPropertyKey.FEATURE_REMOTE_PAYMENT_BOOKING.toString(), value: "1")
        facilityProperty.facility = facility
        facilityProperty.save(flush: true, failOnError: true)

        facility.facilityProperties = [facilityProperty]
        facility.save(flush: true, failOnError: true)

        Facility facility2 = createFacility()
        FacilityProperty facilityProperty2 = new FacilityProperty(key: FacilityProperty.FacilityPropertyKey.FEATURE_REMOTE_PAYMENT_MEMBERSHIP.toString(), value: "1")
        facilityProperty2.facility = facility2
        facilityProperty2.save(flush: true, failOnError: true)

        facility2.facilityProperties = [facilityProperty2]
        facility2.save(flush: true, failOnError: true)

        User user = createUser()
        Customer customer = createCustomer(facility, user.email)
        customer.user = user
        customer.save(flush: true, failOnError: true)

        Customer customer2 = createCustomer(facility2, user.email)
        customer2.user = user
        customer2.save(flush: true, failOnError: true)

        Order order = createOrder(user, facility, Order.Article.MEMBERSHIP)
        order.customer = customer
        order.origin = Order.ORIGIN_FACILITY
        order.save(flush: true, failOnError: true)

        Order orderBooking = createOrder(user, facility, Order.Article.BOOKING)
        orderBooking.customer = customer
        orderBooking.origin = Order.ORIGIN_FACILITY
        orderBooking.save(flush: true, failOnError: true)

        Order order2 = createOrder(user, facility2, Order.Article.MEMBERSHIP)
        order2.customer = customer2
        order2.origin = Order.ORIGIN_FACILITY
        order2.save(flush: true, failOnError: true)

        Order orderBooking2 = createOrder(user, facility2, Order.Article.BOOKING)
        orderBooking2.customer = customer2
        orderBooking2.origin = Order.ORIGIN_FACILITY
        orderBooking2.save(flush: true, failOnError: true)

        List<Order> result = remotePaymentService.getPotentialOrders([customer, customer2])
        assert result.size() == 2
        assert orderBooking in result
        assert order2 in result

        // Testing so that method can handle customers where remote articles ain't stated

        facility.facilityProperties = []
        facility.save(flush: true, failOnError: true)

        List<Customer> inputList = [customer, customer2]
        result = remotePaymentService.getPotentialOrders(inputList)
        assert result == [order2]
        assert inputList == [customer, customer2]
    }

    @Test
    void testGetRemotePayableOrders() {
        Facility facility = createFacility()

        User user = createUser()
        User admin = createUser('admin@matchi.se')
        Customer customer = createCustomer(facility, user.email)
        customer.user = user
        customer.save(flush: true, failOnError: true)

        Order order = createOrder(admin, facility, Order.Article.MEMBERSHIP)
        order.customer = customer
        order.origin = Order.ORIGIN_FACILITY
        order.save(flush: true, failOnError: true)

        LocalDate membershipStartDate = new LocalDate()
        LocalDate membershipEndDate = membershipStartDate.plusYears(1)
        LocalDate gracetimeEndDate = membershipEndDate.plusMonths(1)
        Membership membership = createMembership(customer, membershipStartDate, membershipEndDate, gracetimeEndDate)
        membership.order = order
        membership.save(flush: true, failOnError: true)

        Court court = createCourt(facility)
        Slot slot = createSlot(court, new Date(), new DateTime().plusHours(1).toDate())
        Booking booking = createBooking(customer, slot)

        Order orderBooking = createOrder(user, facility, Order.Article.BOOKING)
        orderBooking.customer = customer
        orderBooking.origin = Order.ORIGIN_FACILITY
        orderBooking.save(flush: true, failOnError: true)

        booking.order = orderBooking
        booking.save(flush: true, failOnError: true)

        assert remotePaymentService.getRemotePayableOrders(Order.Article.BOOKING, [order, orderBooking]) == [orderBooking]
        assert remotePaymentService.getRemotePayableOrders(Order.Article.MEMBERSHIP, [order, orderBooking]) == [order]
        assert !remotePaymentService.getRemotePayableOrders(Order.Article.BOOKING, [order])
        assert !remotePaymentService.getRemotePayableOrders(Order.Article.MEMBERSHIP, [orderBooking])
        assert !remotePaymentService.getRemotePayableOrders(Order.Article.MEMBERSHIP, [])
        assert !remotePaymentService.getRemotePayableOrders(Order.Article.SUBSCRIPTION, [])

        shouldFail {
            remotePaymentService.getRemotePayableOrders(Order.Article.SUBSCRIPTION, [order])
        }
    }

    @Test
    void testGetRemotePayableOrdersMoreCombos() {
        Facility facility = createFacility()

        User user = createUser()
        User admin = createUser('admin@matchi.se')
        Customer customer = createCustomer(facility, user.email)
        customer.user = user
        customer.save(flush: true, failOnError: true)

        Order order = createOrder(admin, facility, Order.Article.MEMBERSHIP)
        order.status = Order.Status.CONFIRMED
        order.customer = customer
        order.origin = Order.ORIGIN_FACILITY
        order.save(flush: true, failOnError: true)

        LocalDate membershipStartDate = new LocalDate()
        LocalDate membershipEndDate = membershipStartDate.plusYears(1)
        LocalDate gracetimeEndDate = membershipEndDate.plusMonths(1)
        Membership membership = createMembership(customer, membershipStartDate, membershipEndDate, gracetimeEndDate)
        membership.order = order
        membership.save(flush: true, failOnError: true)

        assert remotePaymentService.getRemotePayableOrders(Order.Article.MEMBERSHIP, [order])

        AdyenOrderPayment payment = createAdyenOrderPayment(user, order, "1234")
        payment.status = OrderPayment.Status.NEW
        payment.save(flush: true, failOnError: true)
        order.payments = [payment]
        order.save(flush: true, failOnError: true)

        assert remotePaymentService.getRemotePayableOrders(Order.Article.MEMBERSHIP, [order])

        payment.status = OrderPayment.Status.AUTHED
        payment.save(flush: true, failOnError: true)

        assert !remotePaymentService.getRemotePayableOrders(Order.Article.MEMBERSHIP, [order])
    }

    @Test
    void testGetPotentialOrdersNoCustomersDifferentCustomersOriginWeb() {
        Facility facility = createFacility()
        FacilityProperty facilityProperty = new FacilityProperty(key: FacilityProperty.FacilityPropertyKey.FEATURE_REMOTE_PAYMENT_BOOKING.toString(), value: "1")
        facilityProperty.facility = facility
        facilityProperty.save(flush: true, failOnError: true)

        facility.facilityProperties = [facilityProperty]
        facility.save(flush: true, failOnError: true)

        Facility facility2 = createFacility()
        FacilityProperty facilityProperty2 = new FacilityProperty(key: FacilityProperty.FacilityPropertyKey.FEATURE_REMOTE_PAYMENT_MEMBERSHIP.toString(), value: "1")
        facilityProperty2.facility = facility2
        facilityProperty2.save(flush: true, failOnError: true)

        facility2.facilityProperties = [facilityProperty2]
        facility2.save(flush: true, failOnError: true)

        User user = createUser()
        Customer customer = createCustomer(facility, user.email)
        customer.user = user
        customer.save(flush: true, failOnError: true)

        Customer customer2 = createCustomer(facility2, user.email)
        customer2.user = user
        customer2.save(flush: true, failOnError: true)

        Order order = createOrder(user, facility, Order.Article.MEMBERSHIP)
        order.customer = customer
        order.origin = Order.ORIGIN_WEB
        order.save(flush: true, failOnError: true)

        Order orderBooking = createOrder(user, facility, Order.Article.BOOKING)
        orderBooking.customer = customer
        orderBooking.origin = Order.ORIGIN_WEB
        orderBooking.save(flush: true, failOnError: true)

        Order order2 = createOrder(user, facility2, Order.Article.MEMBERSHIP)
        order2.customer = customer2
        order2.origin = Order.ORIGIN_WEB
        order2.save(flush: true, failOnError: true)

        Order orderBooking2 = createOrder(user, facility2, Order.Article.BOOKING)
        orderBooking2.customer = customer2
        orderBooking2.origin = Order.ORIGIN_WEB
        orderBooking2.save(flush: true, failOnError: true)

        List<Order> result = remotePaymentService.getPotentialOrders([customer, customer2])
        assert result.size() == 2
        assert orderBooking in result
        assert order2 in result

        // Testing so that method can handle customers where remote articles ain't stated

        facility.facilityProperties = []
        facility.save(flush: true, failOnError: true)

        List<Customer> inputList = [customer, customer2]
        result = remotePaymentService.getPotentialOrders(inputList)
        assert result == [order2]
        assert inputList == [customer, customer2]
    }

    @Test
    void testGetRemotePayableOrdersOriginWeb() {
        Facility facility = createFacility()

        User user = createUser()
        User admin = createUser('admin@matchi.se')
        Customer customer = createCustomer(facility, user.email)
        customer.user = user
        customer.save(flush: true, failOnError: true)

        Order order = createOrder(admin, facility, Order.Article.MEMBERSHIP)
        order.customer = customer
        order.origin = Order.ORIGIN_WEB
        order.save(flush: true, failOnError: true)

        LocalDate membershipStartDate = new LocalDate()
        LocalDate membershipEndDate = membershipStartDate.plusYears(1)
        LocalDate gracetimeEndDate = membershipEndDate.plusMonths(1)
        Membership membership = createMembership(customer, membershipStartDate, membershipEndDate, gracetimeEndDate)
        membership.order = order
        membership.save(flush: true, failOnError: true)

        Court court = createCourt(facility)
        Slot slot = createSlot(court, new Date(), new DateTime().plusHours(1).toDate())
        Booking booking = createBooking(customer, slot)

        Order orderBooking = createOrder(user, facility, Order.Article.BOOKING)
        orderBooking.customer = customer
        orderBooking.origin = Order.ORIGIN_WEB
        orderBooking.save(flush: true, failOnError: true)

        booking.order = orderBooking
        booking.save(flush: true, failOnError: true)

        assert remotePaymentService.getRemotePayableOrders(Order.Article.BOOKING, [order, orderBooking]) == [orderBooking]
        assert remotePaymentService.getRemotePayableOrders(Order.Article.MEMBERSHIP, [order, orderBooking]) == [order]
        assert !remotePaymentService.getRemotePayableOrders(Order.Article.BOOKING, [order])
        assert !remotePaymentService.getRemotePayableOrders(Order.Article.MEMBERSHIP, [orderBooking])
        assert !remotePaymentService.getRemotePayableOrders(Order.Article.MEMBERSHIP, [])
        assert !remotePaymentService.getRemotePayableOrders(Order.Article.SUBSCRIPTION, [])

        shouldFail {
            remotePaymentService.getRemotePayableOrders(Order.Article.SUBSCRIPTION, [order])
        }
    }

}
