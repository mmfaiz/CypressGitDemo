package com.matchi.orders

import com.matchi.Customer
import com.matchi.Facility
import com.matchi.User
import org.junit.Test

import static com.matchi.TestUtils.*
import static com.matchi.TestUtils.createCustomer
import static com.matchi.TestUtils.createOrder
import static com.matchi.TestUtils.createOrder

/**
 * Created by victorlindhe on 2018-05-14.
 */
class OrderIntegrationTests extends GroovyTestCase {

    @Test
    void testCannotDeleteWithPayments() {
        User user = createUser()
        Facility facility = createFacility()

        Order order = createOrder(user, facility)
        order.status = Order.Status.COMPLETED
        order.save(flush: true, failOnError: true)

        OrderPayment orderPayment = createAdyenOrderPayment(user, order, "", OrderPayment.Status.CAPTURED)
        order.addToPayments(orderPayment)

        assert order.payments?.size() == 1

        Long orderId = order.id
        Long paymentId = orderPayment.id

        assert AdyenOrderPayment.get(paymentId) != null

        // Should not happen since not deletable
        assert !order.isDeletable()
        order.deleteWithPayments(true)
        assert AdyenOrderPayment.get(paymentId) != null
        assert Order.get(orderId) != null
    }

    @Test
    void testCanDeleteWithPayments() {
        User user = createUser()
        Facility facility = createFacility()

        Order order = createOrder(user, facility)
        order.status = Order.Status.NEW

        OrderPayment orderPayment = createAdyenOrderPayment(user, order, "", OrderPayment.Status.NEW)
        order.addToPayments(orderPayment)

        OrderPayment orderPayment2 = createAdyenOrderPayment(user, order, "", OrderPayment.Status.NEW)
        order.addToPayments(orderPayment2)

        order.save(flush: true, failOnError: true)

        assert order.payments?.size() == 2

        Long orderId = order.id
        Long paymentId = orderPayment.id
        Long paymentId2 = orderPayment2.id

        assert AdyenOrderPayment.get(paymentId) != null
        assert AdyenOrderPayment.get(paymentId2) != null

        assert order.isDeletable()
        order.deleteWithPayments(true)
        assert AdyenOrderPayment.get(paymentId) == null
        assert AdyenOrderPayment.get(paymentId2) == null
        assert Order.get(orderId) == null
    }

    @Test
    void testAssertCustomer() {
        User user = createUser()

        // First with no facility, should not break
        Order order = createOrder(user, null)
        order.customer = null
        order.save(flush: true, failOnError: true)

        order.assertCustomer()
        assert !order.customer
        assert !order.facility

        // Add facility to order making it find one
        Facility facility = createFacility()
        Customer customer = createCustomer(facility)
        customer.firstname = user.firstname
        customer.lastname = user.lastname
        customer.email = user.email
        customer.save(flush: true, failOnError: true)

        order.facility = facility
        order.save(flush: true, failOnError: true)
        assert !order.customer

        order.assertCustomer()
        assert order.customer == customer
        assert Customer.findAllByFacility(facility).size() == 1

        // If no customer on exists on that facility, create new one
        Facility facility2 = createFacility()
        order.facility = facility2
        order.customer = null
        order.save(flush: true, failOnError: true)

        order.assertCustomer()
        assert order.customer != customer
        assert Customer.findAllByFacility(facility).size() == 1
        assert Customer.findAllByFacility(facility2).size() == 1

    }

    @Test
    void testCreateCopyForRemotePayment() {
        User user = createUser()
        Facility facility = createFacility()
        Order order = createOrder(user, facility)
        order.origin = Order.ORIGIN_WEB
        order.status = Order.Status.CANCELLED
        order.save(flush: true, failOnError: true)

        assert order.isRemotePayable()

        Order newOrder = order.createCopyForRemotePayment()

        assert newOrder.isRemotePayable()

        assert newOrder.article == order.article
        assert newOrder.origin == order.origin
        assert newOrder.price == order.price
        assert newOrder.vat == order.vat
        assert newOrder.status == Order.Status.NEW
        assert newOrder.facility == order.facility
        assert newOrder.customer == order.customer
        assert newOrder.issuer == order.issuer
        assert newOrder.user == order.user
        assert newOrder.description == order.description
        assert newOrder.dateDelivery == order.dateDelivery
        assert !newOrder.metadata

        assert Order.get(newOrder.id) == newOrder
    }

    @Test
    void testCreateCopyForRemotePaymentWithMetaData() {
        String metadataString = "hello"

        User user = createUser()
        Facility facility = createFacility()
        Order order = createOrder(user, facility)
        order.origin = Order.ORIGIN_WEB
        order.status = Order.Status.CANCELLED
        order.metadata = [greetings: metadataString, somethingNull: null, somethingEmpty: ""]
        order.save(flush: true, failOnError: true)

        assert order.isRemotePayable()

        Order newOrder = order.createCopyForRemotePayment()

        assert newOrder.isRemotePayable()

        assert newOrder.article == order.article
        assert newOrder.origin == order.origin
        assert newOrder.price == order.price
        assert newOrder.vat == order.vat
        assert newOrder.status == Order.Status.NEW
        assert newOrder.facility == order.facility
        assert newOrder.customer == order.customer
        assert newOrder.issuer == order.issuer
        assert newOrder.user == order.user
        assert newOrder.description == order.description
        assert newOrder.dateDelivery == order.dateDelivery
        assert newOrder.metadata.greetings == metadataString
        assert newOrder.metadata.somethingNull == null
        assert newOrder.metadata.somethingEmpty == ""

        System.out.println newOrder.metadata.keySet().dump()

        assert Order.get(newOrder.id) == newOrder
    }
}
