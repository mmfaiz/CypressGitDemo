package com.matchi.orders

import com.matchi.Booking
import com.matchi.Facility
import com.matchi.Municipality
import com.matchi.Region
import com.matchi.Sport
import com.matchi.User
import com.matchi.coupon.CustomerCoupon
import com.matchi.coupon.CustomerCouponTicket
import com.matchi.orders.CouponOrderPayment
import com.matchi.orders.OrderPayment
import com.matchi.payment.PaymentMethod
import grails.test.mixin.Mock

import static com.matchi.TestUtils.*

/**
 * @author Sergei Shushkevich
 */
class CouponOrderPaymentIntegrationTests extends GroovyTestCase {

    void testRefund() {
        def user = createUser()
        def facility = createFacility()
        def customer = createCustomer(facility)
        def booking = createBooking(customer, createSlot(createCourt(facility)))
        def coupon = createCoupon(facility)
        def customerCoupon = new CustomerCoupon(customer: customer, coupon: coupon,
                createdBy: user, nrOfTickets: 5).save(failOnError: true, flush: true)
        def payment = new CouponOrderPayment(issuer: user, status: OrderPayment.Status.CAPTURED, method: PaymentMethod.COUPON)
        payment.ticket = customerCoupon.consumeTicket(
                new Order(price: 100.0, article: Order.Article.BOOKING), user, null, booking.id)
        payment.ticket.save(failOnError: true, flush: true)
        payment.save(failOnError: true, flush: true)

        payment.refund(100.0)

        assert payment.ticket.purchasedObjectId
        assert OrderPayment.Status.CREDITED == payment.status
        assert 100.0 == payment.credited
        customerCoupon.refresh()
        assert 5 == customerCoupon.nrOfTickets
        assert CustomerCouponTicket.countByPurchasedObjectId(booking.id) == 2
        def refundTicket = CustomerCouponTicket.findByPurchasedObjectIdAndType(
                booking.id, CustomerCouponTicket.Type.BOOKING_REFUND)
        assert refundTicket
        assert refundTicket.nrOfTickets == 1
    }
}
