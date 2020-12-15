package com.matchi

import com.matchi.orders.AdyenOrderPayment
import com.matchi.orders.AdyenOrderPaymentError
import com.matchi.payment.PaymentFlow
import grails.test.mixin.TestMixin
import grails.test.mixin.web.ControllerUnitTestMixin
import org.codehaus.groovy.grails.plugins.testing.GrailsMockHttpSession
import org.junit.Before
import org.junit.Test

import javax.servlet.http.HttpSession

@TestMixin(ControllerUnitTestMixin)
class PaymentFlowTest extends GroovyTestCase {

    final static Long ORDER_ID = 1L
    HttpSession session

    @Before
    void setUp() {
        session = new GrailsMockHttpSession()
    }

    @Test
    void testIsFinished() {
        PaymentFlow paymentFlow = new PaymentFlow(ORDER_ID)
        assert !paymentFlow.isFinished()

        paymentFlow.state = PaymentFlow.State.RECEIPT
        assert paymentFlow.isFinished()

        paymentFlow.state = PaymentFlow.State.ERROR
        assert paymentFlow.isFinished()
    }

    @Test
    void testGetInstance() {
        PaymentFlow paymentFlow = new PaymentFlow(ORDER_ID)
        PaymentFlow.addInstance(session, paymentFlow)

        assert PaymentFlow.getInstance(session, ORDER_ID).equals(paymentFlow)
    }

    @Test
    void testPopInstance() {
        PaymentFlow paymentFlow = new PaymentFlow(ORDER_ID)
        PaymentFlow.addInstance(session, paymentFlow)

        assert PaymentFlow.popInstance(session, ORDER_ID).equals(paymentFlow)
        assert PaymentFlow.getInstance(session, ORDER_ID) == null
    }

    @Test
    void testGetFinishedReceipt() {
        PaymentFlow paymentFlow = new PaymentFlow(ORDER_ID)
        paymentFlow.state = PaymentFlow.State.RECEIPT
        PaymentFlow.addInstance(session, paymentFlow)

        assert PaymentFlow.getFinished(session, ORDER_ID).equals(paymentFlow)
        assert PaymentFlow.getInstance(session, ORDER_ID).equals(paymentFlow)
    }

    @Test
    void testErrorWithAdyenOrderPayment() {
        PaymentFlow paymentFlow = new PaymentFlow(ORDER_ID)

        AdyenOrderPayment adyenOrderPayment = new AdyenOrderPayment(error: null)
        String suppliedDefaultError = "Some default error text"

        paymentFlow.error(adyenOrderPayment)
        assert paymentFlow.errorMessage == PaymentFlow.DEFAULT_ERROR_MESSAGE

        paymentFlow.error(adyenOrderPayment, suppliedDefaultError)
        assert paymentFlow.errorMessage == suppliedDefaultError

        String errorReason = "Very serious reason for error"
        adyenOrderPayment.error = new AdyenOrderPaymentError(reason: errorReason)

        paymentFlow.error(adyenOrderPayment, suppliedDefaultError)
        assert paymentFlow.errorMessage == errorReason
    }

    @Test
    void testGetFinishedUnfinished() {
        PaymentFlow paymentFlow = new PaymentFlow(ORDER_ID)
        PaymentFlow.addInstance(session, paymentFlow)

        assert PaymentFlow.getFinished(session, ORDER_ID) == null
        assert PaymentFlow.getInstance(session, ORDER_ID) == null
    }

    @Test
    void testPopInstanceRemovesItFromExistingPaymentFlows() {
        PaymentFlow paymentFlow = new PaymentFlow(ORDER_ID)
        PaymentFlow.addInstance(session, paymentFlow)

        assert PaymentFlow.popInstance(session, ORDER_ID) == paymentFlow
        assert PaymentFlow.getPaymentFlows(session).isEmpty()
    }

    @Test
    void testGetPaymentFlowsContainsAddedInstances() {
        PaymentFlow.addInstance(session, new PaymentFlow(ORDER_ID))
        PaymentFlow.addInstance(session, new PaymentFlow(ORDER_ID + 1))

        assert !PaymentFlow.getPaymentFlows(session).isEmpty()
        assert PaymentFlow.getPaymentFlows(session).size() == 2

        assert PaymentFlow.popInstance(session, ORDER_ID)
        assert PaymentFlow.getPaymentFlows(session).size() == 1
        assert PaymentFlow.popInstance(session, (ORDER_ID + 1))
        assert PaymentFlow.getPaymentFlows(session).isEmpty()
    }

    @Test
    void testCreateFinishUrlReturnsCorrectUrl() {
        String baseUrl = "http://localhost"
        String params1 = "?test=1"
        assert PaymentFlow.createFinishUrl(baseUrl+params1, ORDER_ID) == baseUrl + params1 + "&orderId=${ORDER_ID}"
        String params2 = "?test=1&orderId=${ORDER_ID + 2}"
        assert PaymentFlow.createFinishUrl(baseUrl+params2, ORDER_ID) == baseUrl + params1 + "&orderId=${ORDER_ID}"
        String params3 = "?orderId=${ORDER_ID + 2}"
        assert PaymentFlow.createFinishUrl(baseUrl+params3, ORDER_ID) == baseUrl + "?orderId=${ORDER_ID}"
        assert PaymentFlow.createFinishUrl(baseUrl, ORDER_ID) == baseUrl + "?orderId=${ORDER_ID}"
    }

    @Test
    void testGetModalParamsReturnsOrderIdOnReceiptOrErrorState() {
        PaymentFlow paymentFlow = new PaymentFlow(ORDER_ID)

        paymentFlow.state = PaymentFlow.State.RECEIPT
        assert !paymentFlow.getModalParams().isEmpty()

        paymentFlow.state = PaymentFlow.State.UNFINISHED
        assert paymentFlow.getModalParams().isEmpty()

        paymentFlow.state = PaymentFlow.State.ERROR
        assert !paymentFlow.getModalParams().isEmpty()
    }
}
