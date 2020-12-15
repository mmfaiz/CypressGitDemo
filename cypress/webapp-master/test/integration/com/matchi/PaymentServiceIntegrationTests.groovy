package com.matchi

import com.matchi.orders.Order

import static com.matchi.TestUtils.*

/**
 * @author Sergei Shushkevich
 */
class PaymentServiceIntegrationTests extends GroovyTestCase {

    def paymentService
    def springSecurityService

    void testCreateBookingOrder() {
        def user = createUser()
        springSecurityService.reauthenticate user.email
        def facility = createFacility()
        def slot = createSlot(createCourt(facility))
        def customer = createCustomer(facility)
        def customerUser = createUser("customer@local.net")
        customer.user = customerUser
        customer.save(failOnError: true, flush: true)

        def order = paymentService.createBookingOrder(slot, customer, Order.ORIGIN_FACILITY)

        assert order
        assert 1 == Order.countByFacility(facility)
        assert Order.Status.NEW == order.status
        assert user.id == order.issuer.id
        assert customerUser.id == order.user.id
        assert customer.id == order.customer.id
        assert Order.ORIGIN_FACILITY == order.origin
        assert Order.Article.BOOKING == order.article
    }
}