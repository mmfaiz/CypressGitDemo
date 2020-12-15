package com.matchi

import com.matchi.payment.GenericPaymentController
import com.matchi.payment.PaymentFlow
import org.junit.Test

abstract class GenericPaymentControllerTests {

    @Test
    void testOrderIdAndControllerName() {
        long orderId = 1L
        String finishUrl = getFinishUrl()
        controller.startPaymentFlow(orderId, finishUrl)

        PaymentFlow paymentFlow = PaymentFlow.getInstance(session, orderId)

        assert paymentFlow != null
        assert paymentFlow.orderId == orderId
        assert paymentFlow.paymentController?.equals(controller.controllerName)
    }

    @Test
    void testFinishUrlSupplied() {
        long orderId = 1L
        String finishUrl = getFinishUrl()
        controller.startPaymentFlow(orderId, finishUrl)

        PaymentFlow paymentFlow = PaymentFlow.getInstance(session, orderId)

        assert paymentFlow?.finishUrl?.equals(finishUrl)
    }

    @Test
    void testRefererHeader() {
        long orderId = 1L
        String refererUrl = "http://localhost:8080/facilities/salk?orderId=${orderId}"

        controller.request.addHeader(GenericPaymentController.REFERER_KEY, refererUrl)
        assert controller.hasRefererHeader()
        assert controller.request.getHeader(GenericPaymentController.REFERER_KEY) == refererUrl
    }

    protected String getFinishUrl() {
        return 'http://localhost:8080/facilities/gltk'
    }
}
