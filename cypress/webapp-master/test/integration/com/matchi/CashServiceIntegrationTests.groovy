package com.matchi

import com.matchi.orders.CashOrderPayment
import com.matchi.orders.Order
import com.matchi.orders.OrderPayment

import static com.matchi.TestUtils.*

/**
 * @author Sergei Shushkevich
 */
class CashServiceIntegrationTests extends GroovyTestCase {

    def cashService
    def springSecurityService

    void testCreateCashOrderPayment() {
        def user = createUser()
        springSecurityService.reauthenticate user.email
        def facility = createFacility()
        def order = createOrder(user, facility)

        def payment = cashService.createCashOrderPayment(order)

        assert payment
        assert 1 == CashOrderPayment.countByIssuer(user)
        order.refresh()
        assert 1 == order.payments.size()
        assert Order.Status.COMPLETED == order.status
        assert payment.issuer.id == user.id
        assert payment.amount == order.price
        assert payment.vat == order.vat
        assert payment.status == OrderPayment.Status.CAPTURED
    }
}