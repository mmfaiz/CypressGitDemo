package com.matchi

import com.matchi.activities.Activity
import com.matchi.activities.ActivityOccasion
import com.matchi.activities.ClassActivity
import com.matchi.activities.Participation
import com.matchi.dynamicforms.Form
import com.matchi.orders.Order
import com.matchi.payment.ActivityPaymentController
import com.matchi.payment.PaymentFlow
import com.matchi.watch.ObjectWatchNotificationService
import grails.plugin.springsecurity.SpringSecurityService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.junit.Before
import org.junit.Test

import static com.matchi.TestUtils.createUser

/**
 * See the API for {@link grails.test.mixin.web.ControllerUnitTestMixin} for usage instructions
 */
@TestFor(ActivityPaymentController)
@Mock([User, ActivityOccasion, Participation, Customer, ClassActivity, Activity, Facility, Region, Municipality, Form, Order])
class ActivityPaymentControllerTests extends GenericPaymentControllerTests {

    def mockSpringSecurityService
    def mockActivityService

    User user

    @Before
    void setup() {
        mockSpringSecurityService = mockFor(SpringSecurityService)
        mockActivityService = mockFor(ActivityService)

        controller.springSecurityService = mockSpringSecurityService.createMock()
        controller.activityService = mockActivityService.createMock()

        user = createUser()
    }

    @Test
    void testCancelWithNotRefundableActivity() {
        def objectWatchNotificationService = mockFor(ObjectWatchNotificationService)
        objectWatchNotificationService.demand.sendActivityNotificationsFor { id -> }
        controller.objectWatchNotificationService = objectWatchNotificationService.createMock()

        Participation participation
        def mockStaticActivityOccasion = mockFor(ActivityOccasion)
        def mockActivityOccasion = mockFor(ActivityOccasion)
        def mockParticipation = mockFor(Participation)
        def mockOrder = mockFor(Order)
        def mockActivity = mockFor(ClassActivity)

        mockSpringSecurityService.demand.getCurrentUser(1..1) { ->
            return user
        }

        mockActivityService.demand.cancelAndRefundParticipant(1..1) { ->
            return true
        }

        mockStaticActivityOccasion.demand.static.get(1..1) { Long id ->
            assert id == 1l
            return mockActivityOccasion.createMock()
        }

        mockActivityOccasion.demand.getParticipation(1..1){ User user ->
            assert user == this.user
            participation = mockParticipation.createMock()
            return participation
        }

        mockParticipation.demand.asBoolean(1..1) { ->
            return true
        }

        mockActivityOccasion.demand.getActivity(1..1) { ->
            return mockActivity.createMock()
        }

        mockActivity.demand.cancelByUser(1..1) { ->
            return false
        }

        mockParticipation.demand.getOrder(1..1) { ->
            return mockOrder.createMock()
        }

        mockParticipation.demand.isRefundable(0..1) { ->
            return false
        }

        mockOrder.demand.isStillRefundable(0..1) { ->
            return false
        }

        controller.params.id = 1l
        controller.cancel()

        mockSpringSecurityService.verify()
        mockActivityService.verify()
        mockOrder.verify()
    }

    @Test
    void testSuccessfulActivityOrder() {
        def mockOrder = mockFor(Order)
        def mockStaticOrder = mockFor(Order)
        def mockStaticActivityOccasion = mockFor(ActivityOccasion)
        def mockActivityOccasion = mockFor(ActivityOccasion)

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
            return Order.Article.ACTIVITY
        }

        mockOrder.demand.getMetadata(1..1) { ->
            return [activityOccasionId: 1l]
        }

        mockStaticActivityOccasion.demand.static.get(1..1) { Long id ->
            assert id == 1l
            return mockActivityOccasion.createMock()
        }

        mockActivityOccasion.demand.isFull(1..1) { ->
            return false
        }

        mockActivityService.demand.book(1..1) { ->

        }

        mockOrder.demand.getId(1..1) { ->
            return params.orderId
        }

        controller.startPaymentFlow(params.orderId, getFinishUrl())
        controller.process()

        PaymentFlow paymentFlow = PaymentFlow.getInstance(session, params.orderId)

        assert paymentFlow != null
        assert paymentFlow.state.equals(PaymentFlow.State.RECEIPT)

        mockOrder.verify()
        mockActivityService.verify()
        mockActivityOccasion.verify()
        mockStaticActivityOccasion.verify()
    }

    @Test
    void testUnsuccessfulActivityBooking() {
        def mockOrder = mockFor(Order)
        def mockStaticOrder = mockFor(Order)
        def mockStaticActivityOccasion = mockFor(ActivityOccasion)
        def mockActivityOccasion = mockFor(ActivityOccasion)

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
            return Order.Article.ACTIVITY
        }

        mockOrder.demand.getMetadata(1..1) { ->
            return [activityOccasionId: 1l]
        }

        mockStaticActivityOccasion.demand.static.get(1..1) { Long id ->
            assert id == 1l
            return mockActivityOccasion.createMock()
        }

        mockActivityOccasion.demand.isFull(1..1) { ->
            return false
        }

        mockActivityService.demand.book(1..1) {
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
            return Order.Article.ACTIVITY
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
        String expectedErrorMessageCode = "activityPaymentController.process.errors.couldNotProcess"

        assert paymentFlow != null
        assert paymentFlow.state.equals(PaymentFlow.State.ERROR)
        assert paymentFlow.errorMessage.equals(expectedErrorMessageCode)

        mockActivityOccasion.verify()
        mockStaticActivityOccasion.verify()
    }
}
