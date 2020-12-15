package com.matchi

import com.matchi.coupon.CustomerCoupon
import com.matchi.coupon.Offer
import com.matchi.orders.AdyenOrderPayment
import com.matchi.orders.CashOrderPayment
import com.matchi.orders.CouponOrderPayment
import com.matchi.orders.InvoiceOrderPayment
import com.matchi.orders.Order
import com.matchi.payment.PaymentMethod
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.junit.Test

@TestFor(PaymentService)
@Mock([Customer, Offer])
class PaymentServiceTests {

    @Test
    void testMakePayment() {
        def cashServiceControl = mockFor(CashService)
        cashServiceControl.demand.createCashOrderPayment { o ->
            new CashOrderPayment()
        }
        service.cashService = cashServiceControl.createMock()

        assert service.makePayment(new Order(), [:])
        assert !service.makePayment(null, [:])

        cashServiceControl.verify()
    }

    @Test
    void testMakePayment_UseInvoice() {
        def invoiceServiceControl = mockFor(InvoiceService)
        invoiceServiceControl.demand.createInvoiceOrderPayment { o ->
            new InvoiceOrderPayment()
        }
        service.invoiceService = invoiceServiceControl.createMock()

        assert service.makePayment(new Order(), [useInvoice: true])

        invoiceServiceControl.verify()
    }

    @Test
    void testMakePayment_UseCouponOrGiftCard() {
        def couponServiceControl = mockFor(CouponService)
        couponServiceControl.demand.getExpiresFirstCustomerCoupon(2..2) { c, o, p ->
            new CustomerCoupon()
        }
        service.couponService = couponServiceControl.createMock()
        def couponPaymentServiceControl = mockFor(CouponPaymentService)
        couponPaymentServiceControl.demand.createCouponOrderPayment(2..2) { o, cid, pm ->
            new CouponOrderPayment()
        }
        service.couponPaymentService = couponPaymentServiceControl.createMock()

        assert service.makePayment(new Order(), [useCoupon: true])
        assert service.makePayment(new Order(), [useGiftCard: true])

        couponServiceControl.verify()
        couponPaymentServiceControl.verify()
    }

    void testHandleCreditCardPaymentUseAdyenWithSavedAdyenPaymentInfo() {
        def mockFacility = mockFor(Facility)
        Facility facility = mockFacility.createMock()

        def mockOrder = mockFor(Order)
        Order order = mockOrder.createMock()

        def mockAdyenOrderPayment = mockFor(AdyenOrderPayment)
        def mockStaticAdyenOrderPayment = mockFor(AdyenOrderPayment)
        AdyenOrderPayment adyenOrderPayment = mockAdyenOrderPayment.createMock()

        def mockPaymentInfo = mockFor(PaymentInfo)
        PaymentInfo paymentInfo = mockPaymentInfo.createMock()

        def mockUser = mockFor(User)
        User user = mockUser.createMock()

        mockFor(PaymentInfo).demand.static.findByUser(1) { User u, def args ->
            return paymentInfo
        }

        mockPaymentInfo.demand.getProvider(1..1) { ->
            return PaymentInfo.PaymentProvider.ADYEN
        }

        mockStaticAdyenOrderPayment.demand.static.create(1..1) { Order o, PaymentMethod pm ->
            return adyenOrderPayment
        }

        mockAdyenOrderPayment.demand.authorise(1..1) { m, o -> }

        service.handleCreditCardPayment(order, null)

        mockOrder.verify()
        mockFacility.verify()
        mockStaticAdyenOrderPayment.verify()
        mockAdyenOrderPayment.verify()
        mockPaymentInfo.verify()
        mockUser.verify()
    }

    @Test
    void testRecordingPrices(){
        assert service.getRecordingPrice('SEK') == 99
        assert service.getRecordingPrice('NOK') == 99
        assert service.getRecordingPrice('DKK') == 79
        assert TestUtils.npeHappens{service.getRecordingPrice('FOOBAR')}
    }
}