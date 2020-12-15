package com.matchi.orders

import grails.test.mixin.TestFor
import org.junit.Test

/**
 * @author Sergei Shushkevich
 */
@TestFor(CashOrderPayment)
class CashOrderPaymentTests {

    void testRefund() {
        def payment = new CashOrderPayment(status: OrderPayment.Status.CAPTURED)

        payment.refund(100.0)

        assert OrderPayment.Status.CREDITED == payment.status
        assert 100.0 == payment.credited
    }

    @Test
    void testAllowLateRefund() {
        def payment = new CashOrderPayment(status: OrderPayment.Status.CAPTURED)
        assert payment.allowLateRefund()
    }
}
