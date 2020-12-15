package com.matchi.facility


import com.matchi.*
import com.matchi.membership.Membership
import com.matchi.membership.MembershipType
import com.matchi.orders.AdyenOrderPayment
import com.matchi.orders.Order
import com.matchi.orders.OrderPayment
import com.matchi.payment.MembershipPaymentCommand
import com.matchi.payment.MembershipPaymentController
import com.matchi.payment.PaymentFlow
import com.matchi.payment.PaymentMethod
import grails.plugin.springsecurity.SpringSecurityService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.codehaus.groovy.grails.commons.InstanceFactoryBean
import org.junit.Test

import static com.matchi.TestUtils.*
/**
 * @author Sergei Shushkevich
 */
@TestFor(MembershipPaymentController)
@Mock([Customer, Facility, Membership, MembershipType, Municipality, Order, Region, User])
class MembershipPaymentControllerTests extends GenericPaymentControllerTests {

    @Test
    void testConfirm() {
        views['/membershipPayment/_confirm.gsp'] = 'ok'
        def mt = createMembershipType()
        def order = new Order(id: 1L, metadata: [:])

        def userServiceControl = mockUserService(new User())
        def membershipPaymentServiceControl = mockFor(MembershipPaymentService)
        def mockStaticMembershipType = mockFor(MembershipType)
        def mockCustomerService = mockFor(CustomerService)
        controller.customerService = mockCustomerService.createMock()
        mockCustomerService.demand.getOrCreateUserCustomer(1) { u,f ->
            return new Customer()
        }

        membershipPaymentServiceControl.demand.createMembershipPaymentOrder { u, m -> order }
        mockStaticMembershipType.demand.static.get(1..1) { Long id ->
            return mt
        }

        controller.membershipPaymentService = membershipPaymentServiceControl.createMock()

        controller.confirm(mt.id, "message", false, null)

        assert "ok" == response.text
        userServiceControl.verify()
        membershipPaymentServiceControl.verify()
    }

    @Test
    void testPay() {
        def user = createUser()
        def mt = createMembershipType()
        def order = createOrder(user, mt.facility, Order.Article.MEMBERSHIP, [:])

        def userServiceControl = mockUserService(user)

        def cmd = mockCommandObject(MembershipPaymentCommand)
        cmd.id = mt.id
        cmd.orderId = order.id.toString()
        cmd.method = PaymentMethod.CREDIT_CARD.name()
        cmd.allowRecurring = true
        cmd.validate()

        controller.pay(cmd)

        assert "/adyen/index?orderId=${order.id}&method=CREDIT_CARD&issuerId=&savePaymentInfo=false&ignoreAssert=false&promoCodeId=" == response.redirectedUrl
        assert order.metadata[Order.META_ALLOW_RECURRING] == "true"
        userServiceControl.verify()
    }

    @Test
    void testProcess() {
        def user = createUser()
        def mt = createMembershipType()
        def customer = createCustomer(mt.facility)
        def order = createOrder(user, mt.facility, Order.Article.MEMBERSHIP,
                [membershipTypeId: mt.id.toString(), customerId: customer.id.toString()])

        order.status = Order.Status.CONFIRMED
        order.payments = [new AdyenOrderPayment(status: OrderPayment.Status.AUTHED)]

        def mockSpringSecurityService = mockFor(SpringSecurityService)

        defineBeans {
            springSecurityService(InstanceFactoryBean, mockSpringSecurityService.createMock(), SpringSecurityService)
        }

        def mockStaticOrder = mockFor(Order)

        mockSpringSecurityService.demand.getCurrentUser(2..2) { ->
            return user
        }

        mockStaticOrder.demand.static.get(1..1) { Long id ->
            return order
        }

        params.orderId = order.id.toString()
        controller.startPaymentFlow(params.orderId as Long, getFinishUrl())

        def memberServiceControl = mockFor(MemberService)
        controller.memberService = memberServiceControl.createMock()
        memberServiceControl.demand.requestMembership { c, t, o, d ->
            def membership = createMembership(customer)
            membership.order = order
            return membership
        }
        memberServiceControl.demand.sendMembershipRequestNotification { m, c, msg -> null }

        controller.process()

        assert customer.membership
        assert order == customer.membership.order
        assert "/facilities/membership/payment/receipt?orderId=${order.id}&membershipId=${customer.membership.id}" == response.redirectedUrl
        memberServiceControl.verify()
    }

    @Test
    void testNoMembershipType() {
        def mockOrder = mockFor(Order)
        def mockStaticOrder = mockFor(Order)

        def mockStaticMembershipType = mockFor(MembershipType)

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
            return Order.Article.MEMBERSHIP
        }

        mockOrder.demand.getMetadata(1..1) { ->
            return [membershipTypeId: 1L]
        }

        mockStaticMembershipType.demand.static.get(1..1) { Long id ->
            return null
        }

        mockOrder.demand.getId(1..2) { ->
            return params.orderId
        }

        controller.startPaymentFlow(params.orderId, getFinishUrl())
        controller.process()

        PaymentFlow paymentFlow = PaymentFlow.getInstance(session, 1L)

        assert paymentFlow != null
        assert paymentFlow.state.equals(PaymentFlow.State.ERROR)
        assert paymentFlow.errorMessage.equals("Could not process order with id ${params.orderId}".toString())

        mockOrder.verify()
        mockStaticOrder.verify()
        mockStaticMembershipType.verify()
    }

    @Test
    void testNoCustomerId() {
        def mockOrder = mockFor(Order)
        def mockStaticOrder = mockFor(Order)

        def mockStaticMembershipType = mockFor(MembershipType)
        def mockMembershipType = mockFor(MembershipType)

        Order order = mockOrder.createMock()
        MembershipType membershipType = mockMembershipType.createMock()
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
            return Order.Article.MEMBERSHIP
        }

        mockOrder.demand.getMetadata(2..2) { ->
            return [membershipTypeId: 1L, customerId: null]
        }

        mockStaticMembershipType.demand.static.get(1..1) { Long id ->
            return membershipType
        }

        mockMembershipType.demand.asBoolean(1..1) { ->
            return true
        }

        mockOrder.demand.getId(1..2) { ->
            return params.orderId
        }

        controller.startPaymentFlow(params.orderId, getFinishUrl())
        controller.process()

        PaymentFlow paymentFlow = PaymentFlow.getInstance(session, 1L)

        assert paymentFlow != null
        assert paymentFlow.state.equals(PaymentFlow.State.ERROR)
        assert paymentFlow.errorMessage.equals("Could not process order with id ${params.orderId}".toString())

        mockOrder.verify()
        mockStaticOrder.verify()
        mockStaticMembershipType.verify()
    }

    /**
     * Just tests an exception thrown in createOrUpdateMembership
     */
    @Test
    void testErrorWhenCreating() {
        def mockOrder = mockFor(Order)
        def mockStaticOrder = mockFor(Order)

        def mockStaticMembershipType = mockFor(MembershipType)
        def mockMembershipType = mockFor(MembershipType)
        def mockStaticCustomer = mockFor(Customer)

        def mockMemberService = mockFor(MemberService)
        controller.memberService = mockMemberService.createMock()

        Order order = mockOrder.createMock()
        MembershipType membershipType = mockMembershipType.createMock()
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
            return Order.Article.MEMBERSHIP
        }

        mockOrder.demand.getMetadata(5..5) { ->
            return [membershipTypeId: 1L, customerId: 1L]
        }

        mockStaticMembershipType.demand.static.get(1..1) { Long id ->
            return membershipType
        }

        mockMembershipType.demand.asBoolean(1..1) { ->
            return true
        }

        mockStaticCustomer.demand.static.get(1..1) { Long id ->
            return new Customer(id: 1L)
        }

        mockMemberService.demand.isUpcomingMembershipAvailableForPurchase { m -> false }
        mockMemberService.demand.createMembership(1..1) { c, m, o ->
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
            return Order.Article.MEMBERSHIP
        }

        mockOrder.demand.refund(1..1) { ->

        }

        mockOrder.demand.assertCustomer(1..1) { ->

        }

        mockOrder.demand.setStatus(1..1) { def s ->

        }

        mockOrder.demand.save(1..1) { def args ->

        }

        mockOrder.demand.getId(1..2) { ->
            return params.orderId
        }

        controller.startPaymentFlow(params.orderId, getFinishUrl())
        controller.process()

        PaymentFlow paymentFlow = PaymentFlow.getInstance(session, 1L)

        assert paymentFlow != null
        assert paymentFlow.state.equals(PaymentFlow.State.ERROR)
        assert paymentFlow.errorMessage.equals('membershipPaymentController.process.errors.creationError')

        mockOrder.verify()
        mockStaticOrder.verify()
        mockStaticMembershipType.verify()
    }

    @Test
    void testReceipt() {
        def user = createUser()
        def mt = createMembershipType()
        def customer = createCustomer(mt.facility)
        def membership = createMembership(customer, null, null, null, mt)
        def order = createOrder(user, mt.facility)

        membership.order = order
        membership.save(flush: true, failOnError: true)

        def memberServiceControl = mockFor(MemberService)
        memberServiceControl.demand.getMembership { o -> membership }
        controller.memberService = memberServiceControl.createMock()

        params.orderId = order.id.toString()

        controller.startPaymentFlow(params.orderId as Long, getFinishUrl())
        controller.receipt()

        assert "/membershipPayment/receipt" == view
        assert membership == model.membership
        assert order == model.order
        memberServiceControl.verify()
    }

    private mockUserService(User user) {
        def userServiceControl = mockFor(UserService)
        userServiceControl.demand.getLoggedInUser { -> user }
        controller.userService = userServiceControl.createMock()
        userServiceControl
    }
}
