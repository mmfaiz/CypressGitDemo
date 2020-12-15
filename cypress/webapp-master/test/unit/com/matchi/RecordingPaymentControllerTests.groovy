package com.matchi

import com.matchi.async.ScheduledTaskService
import com.matchi.play.PlayService
import com.matchi.play.Recording
import com.matchi.dynamicforms.Form
import com.matchi.orders.Order
import com.matchi.payment.RecordingPaymentController
import com.matchi.payment.PaymentFlow
import grails.plugin.springsecurity.SpringSecurityService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.junit.Before
import org.junit.Test

/**
 * See the API for {@link grails.test.mixin.web.ControllerUnitTestMixin} for usage instructions
 */
@TestFor(RecordingPaymentController)
@Mock([User, Booking, RecordingPurchase, Customer, Facility, Region, Municipality, Form, Order])
class RecordingPaymentControllerTests extends GenericPaymentControllerTests {

    def mockUserService
    def mockPlayService
    def mockRecordingPaymentService
    def mockSpringSecurityService
    def mockScheduledTaskService

    def mockUser
    def mockCustomer
    def mockFacility
    def mockOrder
    def mockStaticOrder
    def mockStaticRecording
    def mockRecording
    def mockRecordingPurchase
    def mockBooking

    Order order
    User user
    Recording recording
    RecordingPurchase recordingPurchase

    @Before
    void setup() {
        mockUserService = mockFor(UserService)
        mockPlayService = mockFor(PlayService)
        mockRecordingPaymentService = mockFor(RecordingPaymentService)
        mockSpringSecurityService = mockFor(SpringSecurityService)
        mockScheduledTaskService = mockFor(ScheduledTaskService)

        controller.userService = mockUserService.createMock()
        controller.playService = mockPlayService.createMock()
        controller.recordingPaymentService = mockRecordingPaymentService.createMock()
        controller.springSecurityService = mockSpringSecurityService.createMock()
        controller.scheduledTaskService = mockScheduledTaskService.createMock()

        mockUser = mockFor(User)
        mockCustomer = mockFor(Customer)
        mockFacility = mockFor(Facility)
        mockOrder = mockFor(Order)
        mockStaticOrder = mockFor(Order)
        mockStaticRecording = mockFor(Recording)
        mockRecording = mockFor(Recording)
        mockRecordingPurchase = mockFor(RecordingPurchase)
        mockBooking = mockFor(Booking)

        order = mockOrder.createMock()
        user = mockUser.createMock()
        user.id = 1L
        recording = mockRecording.createMock()
        recordingPurchase = mockRecordingPurchase.createMock()
        order.id = 1L
        order.booking = mockBooking.createMock()
        order.booking.id = 1L
        order.issuer = user
        order.customer = mockCustomer.createMock()
        order.customer.facility = mockFacility.createMock()

        params.orderId = 1L

        mockStaticOrder.demand.static.get(1..5) { Long id ->
            return order
        }

        mockOrder.demand.getId(1..1) { ->
            return 1L
        }

        mockOrder.demand.asBoolean(1..1) { ->
            return true
        }

        mockOrder.demand.isProcessable(1..1) { ->
            return true
        }

        mockOrder.demand.getArticle(1..1) { ->
            return Order.Article.RECORDING
        }

        mockOrder.demand.getMetadata(1..2) { ->
            return ["recording.bookingId" : 1L]
        }

        mockSpringSecurityService.demand.getCurrentUser(1..2) { ->
            return user
        }

        mockPlayService.demand.getRecordingFromBooking(1..1) {
            return recording
        }

        mockRecordingPaymentService.demand.getRecordingPurchaseByOrder(1..1) { recording, order ->
            return null
        }

        mockRecordingPaymentService.demand.createRecordingPurchase(1..1) { recording, order ->
            return recordingPurchase
        }

        mockScheduledTaskService.demand.scheduleTask(1) { a, b, c, d ->

        }

        mockOrder.demand.getId(1..7) { ->
            return 1L
        }
    }

    @Test
    void testSuccessfulRecordingOrder() {
        controller.startPaymentFlow(order.booking.id, getFinishUrl())
        controller.process()

        PaymentFlow paymentFlow = PaymentFlow.getInstance(session, order.booking.id)

        assert paymentFlow != null
        assert paymentFlow.state.equals(PaymentFlow.State.RECEIPT)

        mockUser.verify()
        mockCustomer.verify()
        mockFacility.verify()
        mockOrder.verify()
        mockStaticOrder.verify()
        mockStaticRecording.verify()
        mockRecording.verify()
        mockRecordingPurchase.verify()
        mockBooking.verify()
    }

    @Test
    void testUnsuccessfulActivityBooking1() {
        mockRecordingPaymentService.demand.getRecordingPurchaseByOrder(1..1) { recording1, order2 ->
            return recordingPurchase
        }

        controller.startPaymentFlow(order.booking.id, getFinishUrl())
        controller.process()

        PaymentFlow paymentFlow = PaymentFlow.getInstance(session, params.orderId)
        String expectedErrorMessageCode = "recordingPaymentController.process.errors.alreadyProcessed"

        assert paymentFlow != null
        assert paymentFlow.state.equals(PaymentFlow.State.ERROR)
        assert paymentFlow.errorMessage.equals(expectedErrorMessageCode)

        mockRecording.verify()
        mockStaticRecording.verify()
    }

    void testUnsuccessfulActivityBooking2() {
        mockSpringSecurityService.demand.getCurrentUser(1..2) { ->
            User user = mockUser.createMock()
            user.id = 9L
            return user
        }

        mockOrder.demand.getArticle(1..2) { ->
            return Order.Article.RECORDING
        }

        mockOrder.demand.refund(1..1) { -> }

        mockOrder.demand.assertCustomer(1..1) { ->
            return true
        }
        mockOrder.demand.save(1..1) { ->  }

        mockOrder.demand.getId(1..1) { ->
            return 1L
        }

        controller.startPaymentFlow(order.booking.id, getFinishUrl())
        controller.process()

        PaymentFlow paymentFlow = PaymentFlow.getInstance(session, params.orderId)
        String expectedErrorMessageCode = "recordingPaymentController.process.errors.authError"

        assert paymentFlow != null
        assert paymentFlow.state.equals(PaymentFlow.State.ERROR)
        assert paymentFlow.errorMessage.equals(expectedErrorMessageCode)

        mockRecording.verify()
        mockStaticRecording.verify()
    }

    void testUnsuccessfulActivityBooking3() {
        mockOrder.demand.getArticle(1..2) { ->
            return Order.Article.RECORDING
        }

        mockRecordingPaymentService.demand.createRecordingPurchase(1..1) { recording, order ->
            throw new Exception("Any")
        }

        mockOrder.demand.refund(1..1) { -> }

        mockOrder.demand.assertCustomer(1..1) { ->
            return true
        }
        mockOrder.demand.save(1..1) { ->  }

        mockOrder.demand.getId(1..1) { ->
            return 1L
        }


        controller.startPaymentFlow(order.booking.id, getFinishUrl())
        controller.process()

        PaymentFlow paymentFlow = PaymentFlow.getInstance(session, params.orderId)
        String expectedErrorMessageCode = "recordingPaymentController.process.errors.couldNotProcess"

        assert paymentFlow != null
        assert paymentFlow.state.equals(PaymentFlow.State.ERROR)
        assert paymentFlow.errorMessage.equals(expectedErrorMessageCode)

        mockRecording.verify()
        mockStaticRecording.verify()
    }
}
