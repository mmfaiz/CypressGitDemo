package com.matchi

import com.matchi.activities.EventActivity
import com.matchi.activities.Participant
import com.matchi.activities.trainingplanner.CourseParticipantService
import com.matchi.dynamicforms.Form
import com.matchi.dynamicforms.FormPaymentService
import com.matchi.dynamicforms.Submission
import com.matchi.orders.AdyenOrderPayment
import com.matchi.orders.Order
import com.matchi.orders.OrderPayment
import com.matchi.payment.FormPaymentCommand
import com.matchi.payment.FormPaymentController
import com.matchi.payment.PaymentFlow
import com.matchi.payment.PaymentMethod
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.junit.Test

import static com.matchi.TestUtils.*
import static plastic.criteria.PlasticCriteria.mockCriteria

/**
 * @author Sergei Shushkevich
 */
@TestFor(FormPaymentController)
@Mock([Facility, Form, Municipality, Order, Region, Submission, User, FacilityUserRole, Customer])
class FormPaymentControllerTests extends GenericPaymentControllerTests {

    @Test
    void testConfirm() {
        views['/formPayment/_confirm.gsp'] = 'ok'
        def form = createForm()
        def order = new Order(metadata: [:])
        def pi = new PaymentInfo()

        def userServiceControl = mockUserService(new User())
        def formPaymentServiceControl = mockFor(FormPaymentService)
        formPaymentServiceControl.demand.createFormPaymentOrder { u, f -> order }
        controller.formPaymentService = formPaymentServiceControl.createMock()
        mockCriteria([FacilityUserRole])

        def securityServiceControl = mockFor(SecurityService)
        securityServiceControl.demand.hasFacilityAccessTo(1) { f ->
            assert form.facility.id == f.id
            return false
        }

        controller.securityService = securityServiceControl.createMock()

        controller.confirm(form.id)

        assert "ok" == response.text
        /*assert order == model.order
        assert model.order.metadata.cancelUrl
        assert model.order.metadata.processUrl
        assert form == model.form
        assert pi == model.paymentInfo*/
        userServiceControl.verify()
        formPaymentServiceControl.verify()
    }

    @Test
    void testPay() {
        def user = createUser()
        def form = createForm()
        def order = createOrder(user, form.facility)

        def userServiceControl = mockUserService(user)

        def cmd = mockCommandObject(FormPaymentCommand)
        cmd.id = form.id
        cmd.orderId = order.id.toString()
        cmd.method = PaymentMethod.CREDIT_CARD.name()
        cmd.validate()

        controller.pay(cmd)

        assert "/adyen/index?orderId=${order.id}&method=CREDIT_CARD&issuerId=&savePaymentInfo=false&ignoreAssert=false&promoCodeId=" == response.redirectedUrl
        userServiceControl.verify()
    }

    void testProcess() {
        def user = createUser()
        def form = createForm()
        form.event = new EventActivity()
        form.save(flush: true, failOnError: true)

        def customer = createCustomer()
        def submission = new Submission(form: form, user: user, customer: customer)
        session[FormController.SUBMISSION_SESSION_KEY] = submission
        def order = createOrder(user, form.facility, Order.Article.FORM_SUBMISSION,
                [formId: form.id.toString()])

        order.status = Order.Status.CONFIRMED
        order.payments = [new AdyenOrderPayment(status: OrderPayment.Status.AUTHED)]

        def mockCourseParticipantService = mockFor(CourseParticipantService)
        controller.courseParticipantService = mockCourseParticipantService.createMock()

        mockCourseParticipantService.demand.saveParticipant(1..1) { Submission s ->
            return new Participant()
        }

        def notificationServiceControl = mockFor(NotificationService)
        notificationServiceControl.demand.sendFormSubmissionReceipt { s -> }
        notificationServiceControl.demand.sendActivitySubmissionNotification { c, activity -> }
        controller.notificationService = notificationServiceControl.createMock()
        params.orderId = order.id

        controller.startPaymentFlow(order.id, getFinishUrl())
        controller.process()

        assert "/forms/payment/receipt?orderId=${order.id}" == response.redirectedUrl
        assert !session[FormController.SUBMISSION_SESSION_KEY]
        notificationServiceControl.verify()
    }

    void testProcessMaxSumbissionsError() {
        def user = createUser()
        def form = createForm()
        form.event = new EventActivity()
        form.maxSubmissions = 1
        form.save(flush: true, failOnError: true)

        def customer = createCustomer()
        def submission = new Submission(form: form, user: user, customer: customer)
        session[FormController.SUBMISSION_SESSION_KEY] = submission
        createSubmission(createCustomer(), form, user)

        def mockOrder = mockFor(Order)
        mockOrder.demand.asBoolean(1..1) { ->
            return true
        }
        mockOrder.demand.isProcessable(1..1) { ->
            return true
        }
        def order = mockOrder.createMock()
        def mockStaticOrder = mockFor(Order)
        mockStaticOrder.demand.static.get(1..1) { Long id ->
            return order
        }

        params.orderId = 1L

        controller.startPaymentFlow(order.id, getFinishUrl())
        controller.process()

        assert "/forms/payment/error?orderId=" == response.redirectedUrl
        mockStaticOrder.verify()
        mockOrder.verify()
    }

    @Test
    void testReceipt() {
        def user = createUser()
        def order = createOrder(user, createFacility())
        params.orderId = order.id.toString()

        controller.startPaymentFlow(order.id, getFinishUrl())
        def model = controller.receipt()

        assert order == model.order
    }

    @Test
    void testNoSubmission() {
        def mockOrder = mockFor(Order)
        def mockStaticOrder = mockFor(Order)

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
            return Order.Article.FORM_SUBMISSION
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
            return Order.Article.FORM_SUBMISSION
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

        PaymentFlow paymentFlow = PaymentFlow.getInstance(session, 1L)

        assert paymentFlow != null
        assert paymentFlow.state.equals(PaymentFlow.State.ERROR)
        assert paymentFlow.errorMessage.equals('formPaymentController.process.errors.noSubmission')

        mockOrder.verify()
        mockStaticOrder.verify()
    }

    private mockUserService(User user) {
        def userServiceControl = mockFor(UserService)
        userServiceControl.demand.getLoggedInUser { -> user }
        controller.userService = userServiceControl.createMock()
        userServiceControl
    }
}
