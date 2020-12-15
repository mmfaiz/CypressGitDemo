package com.matchi

import com.matchi.coupon.CustomerCoupon
import com.matchi.orders.CouponOrderPayment
import com.matchi.orders.Order
import com.matchi.orders.OrderPayment
import com.matchi.payment.PaymentMethod

import static com.matchi.TestUtils.*

/**
 * @author Sergei Shushkevich
 */
class CouponPaymentServiceIntegrationTests extends GroovyTestCase {

    def couponPaymentService
    def springSecurityService

    void testCreateCouponOrderPayment() {
        def user = createUser()
        springSecurityService.reauthenticate user.email
        def facility = createFacility()
        def order = createOrder(user, facility, Order.Article.BOOKING)
        def customer = createCustomer(facility)
        def coupon = createCoupon(facility)
        def customerCoupon = new CustomerCoupon(customer: customer, coupon: coupon,
                createdBy: user, nrOfTickets: 5).save(failOnError: true, flush: true)

        def payment = couponPaymentService.createCouponOrderPayment(order, customerCoupon.id, PaymentMethod.COUPON)

        assert payment
        assert 1 == CouponOrderPayment.count()
        order.refresh()
        assert 1 == order.payments.size()
        assert Order.Status.COMPLETED == order.status
        assert payment.issuer.id == user.id
        assert payment.amount == order.price
        assert payment.vat == order.vat
        assert payment.status == OrderPayment.Status.CAPTURED
        assert payment.ticket
        assert payment.method.equals(PaymentMethod.COUPON)
        customerCoupon.refresh()
        assert 4 == customerCoupon.nrOfTickets
    }
}
