package com.matchi.adyen

import com.matchi.*
import com.matchi.events.EventInitiator
import com.matchi.orders.AdyenOrderPayment
import com.matchi.orders.Order
import com.matchi.orders.OrderPayment
import com.matchi.payment.PaymentException
import com.matchi.payment.PaymentFlow
import com.matchi.payment.PaymentMethod
import grails.plugin.springsecurity.SpringSecurityService
import grails.test.GrailsMock
import grails.test.mixin.Mock
import grails.test.mixin.TestFor

import org.springframework.context.MessageSource
import org.springframework.http.HttpStatus
import spock.lang.Specification

import static com.matchi.TestUtils.*
/**
 * See the API for {@link grails.test.mixin.web.ControllerUnitTestMixin} for usage instructions
 */
@TestFor(AdyenPaymentController)
@Mock([User, Facility, Order, Municipality, Region, Sport, AdyenOrderPayment])
class AdyenPaymentControllerSpec extends Specification {
    GrailsMock springSecurityService
    GrailsMock userService

    void setup() {
        springSecurityService = mockFor(SpringSecurityService)
        userService = mockFor(UserService)
        controller.springSecurityService = springSecurityService.createMock()
        controller.userService = userService.createMock()
    }

    void testConfirmNotificationResponse() {
        when:
        controller.confirm()

        then:
        response.text == '{"notificationResponse":"[accepted]"}'
    }

    void testCheckPendingPaymentNoPaymentFlow() {
        Order order = createOrder(createUser(), createFacility())
        params.orderId = order.id

        when:
        controller.checkPendingPayment()

        then:
        response.status == HttpStatus.NOT_FOUND.value()
    }

    void testPayDoublePayment() {
        User user = createUser()
        Order order = createOrder(user, createFacility())
        AdyenOrderPayment payment = createAdyenOrderPayment(user, order, "123")
        order.payments = [payment]
        params.method = PaymentMethod.CREDIT_CARD_RECUR
        params.orderId = order.id

        springSecurityService.demand.getCurrentUser(1) { ->
            return null
        }

        when:
        controller.pay()

        then:
        thrown PaymentException

        springSecurityService.verify()
    }

    void testCheckPendingPaymentStillPending() {
        Order order = createOrder(createUser(), createFacility())
        AdyenOrderPayment orderPayment = new AdyenOrderPayment()
        orderPayment.issuer = order.issuer
        orderPayment.amount = order.total()
        orderPayment.vat    = order.vat()
        orderPayment.method = PaymentMethod.SWISH
        orderPayment.status = OrderPayment.Status.PENDING

        orderPayment.orders = [order]
        orderPayment.save(flush: true, failOnError: true)

        order.payments = [orderPayment]
        order.save(flush: true, failOnError: true)

        PaymentFlow paymentFlow = new PaymentFlow(order.id)
        paymentFlow.paymentController = "PaymentControllerName"
        paymentFlow.finishUrl = "someUrl"
        PaymentFlow.addInstance(session, paymentFlow)

        def mockAdyenService = mockFor(AdyenService)
        mockAdyenService.demand.processOrTimeOutPending(1) { ap ->
            assert ap == orderPayment
        }
        userService.demand.getCurrentUser(1) { ->
            return null
        }

        controller.adyenService = mockAdyenService.createMock()
        params.orderId = order.id

        when:
        controller.checkPendingPayment()

        then:
        response.json.toString() == "{}"

        mockAdyenService.verify()
    }

    void testCheckPendingPaymentCompleted() {
        Order order = createOrder(createUser(), createFacility())
        AdyenOrderPayment orderPayment = new AdyenOrderPayment()
        orderPayment.issuer = order.issuer
        orderPayment.amount = order.total()
        orderPayment.vat    = order.vat()
        orderPayment.method = PaymentMethod.SWISH
        orderPayment.status = OrderPayment.Status.PENDING

        orderPayment.orders = [order]
        orderPayment.save(flush: true, failOnError: true)

        order.payments = [orderPayment]
        order.save(flush: true, failOnError: true)

        PaymentFlow paymentFlow = new PaymentFlow(order.id)
        paymentFlow.paymentController = "bookingPayment"
        paymentFlow.finishUrl = "someUrl"
        PaymentFlow.addInstance(session, paymentFlow)

        def mockAdyenService = mockFor(AdyenService)
        mockAdyenService.demand.processOrTimeOutPending(1) { AdyenOrderPayment ap, EventInitiator eventInitiator ->
            assert ap == orderPayment
            ap.orders[0].status = Order.Status.COMPLETED
            ap.status = OrderPayment.Status.CAPTURED
        }
        userService.demand.getCurrentUser(1) { ->
            return null
        }

        controller.adyenService = mockAdyenService.createMock()
        params.orderId = order.id

        String expectedResponse = "{\"url\":\"${controller.createLink(controller: paymentFlow.paymentController, action: "process", params: [orderId: order.id], absolute: true)}\"}"

        when:
        controller.checkPendingPayment()

        then:
        response.json.toString() == expectedResponse

        mockAdyenService.verify()
    }

    void testCheckPendingPaymentFailed() {
        Order order = createOrder(createUser(), createFacility())

        AdyenOrderPayment orderPayment = new AdyenOrderPayment()
        orderPayment.issuer = order.issuer
        orderPayment.amount = order.total()
        orderPayment.vat    = order.vat()
        orderPayment.method = PaymentMethod.SWISH
        orderPayment.status = OrderPayment.Status.PENDING

        orderPayment.orders = [order]
        orderPayment.save(flush: true, failOnError: true)

        order.payments = [orderPayment]
        order.save(flush: true, failOnError: true)

        PaymentFlow paymentFlow = new PaymentFlow(order.id)
        paymentFlow.paymentController = "bookingPayment"
        paymentFlow.finishUrl = "someUrl"
        PaymentFlow.addInstance(session, paymentFlow)

        def mockAdyenService = mockFor(AdyenService)
        mockAdyenService.demand.processOrTimeOutPending(1) { AdyenOrderPayment ap, EventInitiator eventInitiator ->
            assert ap == orderPayment
            ap.orders[0].status = Order.Status.CANCELLED
            ap.status = OrderPayment.Status.FAILED
        }
        userService.demand.getCurrentUser(1) { ->
            return null
        }

        def mockMessageSource = mockFor(MessageSource)
        mockMessageSource.demand.getMessage { m, a, l ->
            return "errorMessage"
        }
        controller.messageSource = mockMessageSource.createMock()

        controller.adyenService = mockAdyenService.createMock()
        params.orderId = order.id

        when:
        controller.checkPendingPayment()

        then:
        response.json.toString() == "{\"url\":\"${paymentFlow.finishUrl}\"}"

        mockAdyenService.verify()
    }

}
