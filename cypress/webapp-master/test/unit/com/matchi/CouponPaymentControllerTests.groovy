package com.matchi

import com.matchi.coupon.Coupon
import com.matchi.coupon.CustomerCoupon
import com.matchi.coupon.Offer
import com.matchi.orders.Order
import com.matchi.payment.CouponPaymentController
import com.matchi.payment.PaymentFlow
import grails.test.GrailsMock
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.junit.Before
import org.junit.Test

/**
 * See the API for {@link grails.test.mixin.web.ControllerUnitTestMixin} for usage instructions
 */
@TestFor(CouponPaymentController)
@Mock([CustomerCoupon, Offer, Order])
class CouponPaymentControllerTests extends GenericPaymentControllerTests {
    GrailsMock couponPaymentService

    @Before
    void setUp() {
        couponPaymentService = mockFor(CouponPaymentService)
        controller.couponPaymentService = couponPaymentService.createMock()
    }

    @Test
    void testNoCouponFound() {
        def mockOrder = mockFor(Order)
        def mockStaticOrder = mockFor(Order)

        def mockStaticOffer = mockFor(Offer)

        Order order = mockOrder.createMock()
        params.orderId = 1L

        mockStaticOrder.demand.static.get(1..1) { Long id ->
            return order
        }

        mockOrder.demand.getId(1..1) { ->
            return params.orderId
        }

        mockOrder.demand.asBoolean(1..1) { ->
            return true
        }

        mockOrder.demand.isProcessable(1..1) { ->
            return true
        }

        mockOrder.demand.getArticle(1..1) { ->
            return Order.Article.COUPON
        }

        mockOrder.demand.getMetadata(1..1) { ->
            return [couponId: 1L]
        }

        mockStaticOffer.demand.static.get(1..1) { Long id ->
            return null
        }

        mockStaticOrder.demand.static.withTransaction { Closure callable ->
            callable.call(null)
        }

        mockStaticOrder.demand.static.get(1..1) { Long id ->
            return order
        }

        mockOrder.demand.getId(1..1) { ->
            return params.orderId
        }

        mockOrder.demand.getArticle(1..1) { ->
            return Order.Article.COUPON
        }

        mockOrder.demand.refund(1..1) { ->

        }

        mockOrder.demand.assertCustomer(1..1) { ->

        }

        mockOrder.demand.setStatus(1..1) { def s ->

        }

        mockOrder.demand.save(1..1) { def args ->

        }

        mockOrder.demand.getId(1..1) { ->
            return params.orderId
        }

        controller.startPaymentFlow(params.orderId, getFinishUrl())
        controller.process()

        PaymentFlow paymentFlow = PaymentFlow.getInstance(session, params.orderId)

        assert paymentFlow != null
        assert paymentFlow.state.equals(PaymentFlow.State.ERROR)
        assert paymentFlow.errorMessage.equals('couponPaymentController.process.errors.couponNotFound')

        mockOrder.verify()
        mockStaticOrder.verify()
        mockStaticOffer.verify()
    }

    @Test
    void testSuccessfulCoupon() {
        def mockOrder = mockFor(Order)
        def mockStaticOrder = mockFor(Order)

        def mockCoupon = mockFor(Coupon)
        def mockStaticOffer = mockFor(Offer)

        def mockCustomerCoupon = mockFor(CustomerCoupon)

        Order order = mockOrder.createMock()
        Coupon coupon = mockCoupon.createMock()
        CustomerCoupon customerCoupon = mockCustomerCoupon.createMock()

        params.orderId = 1L

        mockStaticOrder.demand.static.get(1..1) { Long id ->
            return order
        }

        mockOrder.demand.asBoolean(1..1) { ->
            return true
        }

        mockOrder.demand.isProcessable(1..1) { ->
            return true
        }

        mockOrder.demand.getArticle(1..1) { ->
            return Order.Article.COUPON
        }

        mockOrder.demand.getMetadata(1..1) { ->
            return [couponId: 1L]
        }

        mockStaticOffer.demand.static.get(1..1) { Long id ->
            return coupon
        }

        mockCoupon.demand.asBoolean(1..1) { ->
            return true
        }

        couponPaymentService.demand.registerCouponPurchase(1..1) { u1, c, t, u2, o ->
            return customerCoupon
        }

        mockCustomerCoupon.demand.asBoolean(1..1) { ->
            return true
        }

        controller.startPaymentFlow(params.orderId, getFinishUrl())
        controller.process()

        PaymentFlow paymentFlow = PaymentFlow.getInstance(session, params.orderId)

        assert paymentFlow != null
        assert paymentFlow.state.equals(PaymentFlow.State.RECEIPT)

        mockOrder.verify()
        mockStaticOrder.verify()
        mockCoupon.verify()
        mockStaticOffer.verify()
        mockCustomerCoupon.verify()
        couponPaymentService.verify()
    }

    @Test
    void testExceptionThrownFromService() {
        def mockOrder = mockFor(Order)
        def mockStaticOrder = mockFor(Order)

        def mockCoupon = mockFor(Coupon)
        def mockStaticOffer = mockFor(Offer)

        Order order = mockOrder.createMock()
        Coupon coupon = mockCoupon.createMock()

        params.orderId = 1L

        mockStaticOrder.demand.static.get(1..1) { Long id ->
            return order
        }

        mockOrder.demand.getId(1..1) { ->
            return params.orderId
        }

        mockOrder.demand.asBoolean(1..1) { ->
            return true
        }

        mockOrder.demand.isProcessable(1..1) { ->
            return true
        }

        mockOrder.demand.getArticle(1..1) { ->
            return Order.Article.COUPON
        }

        mockOrder.demand.getMetadata(1..1) { ->
            return [couponId: 1L]
        }

        mockStaticOffer.demand.static.get(1..1) { Long id ->
            return coupon
        }

        mockCoupon.demand.asBoolean(1..1) { ->
            return true
        }

        couponPaymentService.demand.registerCouponPurchase(1..1) {
            throw new Exception("error")
        }

        mockStaticOrder.demand.static.withTransaction { Closure callable ->
            callable.call(null)
        }

        mockStaticOrder.demand.static.get(1..1) { Long id ->
            return order
        }

        mockOrder.demand.getId(1..1) { ->
            return params.orderId
        }

        mockOrder.demand.getArticle(1..1) { ->
            return Order.Article.COUPON
        }

        mockOrder.demand.refund(1..1) { ->

        }

        mockOrder.demand.assertCustomer(1..1) { ->

        }

        mockOrder.demand.setStatus(1..1) { def s ->

        }

        mockOrder.demand.save(1..1) { def args ->

        }

        mockOrder.demand.getId(1..1) { ->
            return params.orderId
        }

        controller.startPaymentFlow(params.orderId, getFinishUrl())
        controller.process()

        PaymentFlow paymentFlow = PaymentFlow.getInstance(session, params.orderId)

        assert paymentFlow != null
        assert paymentFlow.state.equals(PaymentFlow.State.ERROR)
        assert paymentFlow.errorMessage.equals('couponPaymentController.process.errors.creationError')

        mockOrder.verify()
        mockStaticOrder.verify()
        mockCoupon.verify()
        mockStaticOffer.verify()
    }

    @Test
    void testNullCustomerCouponReturnedFromService() {
        def mockOrder = mockFor(Order)
        def mockStaticOrder = mockFor(Order)

        def mockCoupon = mockFor(Coupon)
        def mockStaticOffer = mockFor(Offer)

        Order order = mockOrder.createMock()
        Coupon coupon = mockCoupon.createMock()

        params.orderId = 1L

        mockStaticOrder.demand.static.get(1..1) { Long id ->
            return order
        }

        mockOrder.demand.getId(1..1) { ->
            return params.orderId
        }

        mockOrder.demand.asBoolean(1..1) { ->
            return true
        }

        mockOrder.demand.isProcessable(1..1) { ->
            return true
        }

        mockOrder.demand.getArticle(1..1) { ->
            return Order.Article.COUPON
        }

        mockOrder.demand.getMetadata(1..1) { ->
            return [couponId: 1L]
        }

        mockStaticOffer.demand.static.get(1..1) { Long id ->
            return coupon
        }

        mockCoupon.demand.asBoolean(1..1) { ->
            return true
        }

        couponPaymentService.demand.registerCouponPurchase(1..1) { u1, c, t, u2, o ->
            return null
        }

        mockStaticOrder.demand.static.withTransaction { Closure callable ->
            callable.call(null)
        }

        mockStaticOrder.demand.static.get(1..1) { Long id ->
            return order
        }

        mockOrder.demand.getId(1..1) { ->
            return params.orderId
        }

        mockOrder.demand.getArticle(1..1) { ->
            return Order.Article.COUPON
        }

        mockOrder.demand.refund(1..1) { ->

        }

        mockOrder.demand.assertCustomer(1..1) { ->

        }

        mockOrder.demand.setStatus(1..1) { def s ->

        }

        mockOrder.demand.save(1..1) { def args ->

        }

        mockOrder.demand.getId(1..1) { ->
            return params.orderId
        }

        controller.startPaymentFlow(params.orderId, getFinishUrl())
        controller.process()

        PaymentFlow paymentFlow = PaymentFlow.getInstance(session, params.orderId)

        assert paymentFlow != null
        assert paymentFlow.state.equals(PaymentFlow.State.ERROR)
        assert paymentFlow.errorMessage.equals('couponPaymentController.process.errors.creationError')

        mockOrder.verify()
        mockStaticOrder.verify()
        mockCoupon.verify()
        mockStaticOffer.verify()
    }
}
