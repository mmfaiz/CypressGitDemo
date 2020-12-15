package com.matchi.payment

import com.matchi.*
import com.matchi.async.ScheduledTaskService
import com.matchi.orders.AdyenOrderPayment
import com.matchi.orders.Order
import grails.plugin.springsecurity.SpringSecurityService
import grails.test.GrailsMock
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.web.GroovyPageUnitTestMixin
import org.joda.time.LocalDateTime
import org.junit.Before
import org.springframework.http.HttpStatus

import static com.matchi.TestUtils.*

/**
 * See the API for {@link grails.test.mixin.web.ControllerUnitTestMixin} for usage instructions
 */
@TestFor(RemotePaymentController)
@TestMixin(GroovyPageUnitTestMixin)
@Mock([Order, User, Region, Facility, Municipality, AdyenOrderPayment])
class RemotePaymentControllerTests extends GenericPaymentControllerTests {

    GrailsMock springSecurityService
    GrailsMock notificationService
    GrailsMock paymentService
    GrailsMock orderService
    GrailsMock scheduledTaskService

    @Before
    void setUp() {
        springSecurityService = mockFor(SpringSecurityService)
        controller.springSecurityService = springSecurityService.createMock()

        notificationService = mockFor(NotificationService)
        controller.notificationService = notificationService.createMock()

        paymentService = mockFor(PaymentService)
        controller.paymentService = paymentService.createMock()

        orderService = mockFor(OrderService)
        controller.orderService = orderService.createMock()

        scheduledTaskService = mockFor(ScheduledTaskService)
        controller.scheduledTaskService = scheduledTaskService.createMock()
    }

    void testConfirmRequiresLoggedInUser() {
        springSecurityService.demand.getCurrentUser(1) { ->
            return null
        }

        controller.confirm()

        assert controller.modelAndView.viewName == "/remotePayment/error"

        springSecurityService.verify()
    }

    void testPayRequiresLoggedInUser() {
        springSecurityService.demand.getCurrentUser(1) { ->
            return null
        }

        controller.pay(null)

        assert controller.response.status == HttpStatus.UNAUTHORIZED.value()

        springSecurityService.verify()
    }

    void testConfirmRequiresValidOrder() {
        User user = new User()
        springSecurityService.demand.getCurrentUser(1) { ->
            return user
        }

        controller.params.id = 1l

        GrailsMock mockOrder = mockFor(Order)
        mockOrder.demand.static.get() { Long id ->
            assert id == params.id
            return null
        }

        shouldFail(IllegalStateException) {
            controller.confirm()
        }

        springSecurityService.verify()
        mockOrder.verify()
    }

    void testConfirmRequiresOrderBeingRemotePayable() {
        User user = new User()
        springSecurityService.demand.getCurrentUser(1) { ->
            return user
        }

        controller.params.id = 1l

        GrailsMock mockOrder = mockFor(Order)
        mockOrder.demand.asBoolean(1) { -> return true }
        mockOrder.demand.isRemotePayable(1) { -> return false }

        GrailsMock mockStaticOrder = mockFor(Order)
        mockStaticOrder.demand.static.get(1) { Long id ->
            assert id == params.id
            return mockOrder.createMock()
        }

        controller.confirm()

        assert controller.modelAndView.viewName == "/remotePayment/error"

        springSecurityService.verify()
        mockStaticOrder.verify()
        mockOrder.verify()
    }

    void testConfirmRequiresCustomer() {
        User user = new User()
        Facility facility = new Facility()

        springSecurityService.demand.getCurrentUser(1) { ->
            return user
        }

        controller.params.id = 1l

        GrailsMock mockOrder = mockFor(Order)
        mockOrder.demand.asBoolean(1) { -> return true }
        mockOrder.demand.isRemotePayable(1) { -> return true }
        mockOrder.demand.getFacility(1) { -> return facility }

        GrailsMock mockStaticCustomer = mockFor(Customer)
        mockStaticCustomer.demand.static.findByUserAndFacility(1) { User u, Facility f ->
            assert u == user
            assert f == facility
            return null
        }

        GrailsMock mockStaticOrder = mockFor(Order)
        mockStaticOrder.demand.static.get(1) { Long id ->
            assert id == params.id
            return mockOrder.createMock()
        }

        controller.confirm()

        assert controller.response.status == HttpStatus.UNAUTHORIZED.value()

        springSecurityService.verify()
        mockStaticOrder.verify()
        mockOrder.verify()
        mockStaticCustomer.verify()
    }

    void testConfirmRequiresCustomerSameAsOrders() {
        User user = new User()
        Facility facility = new Facility()

        springSecurityService.demand.getCurrentUser(1) { ->
            return user
        }

        controller.params.id = 1l

        GrailsMock mockOrder = mockFor(Order)
        mockOrder.demand.asBoolean(1) { -> return true }
        mockOrder.demand.isRemotePayable(1) { -> return true }
        mockOrder.demand.getFacility(1) { -> return facility }
        mockOrder.demand.getCustomer(1) { -> return new Customer() }

        GrailsMock mockCustomer = mockFor(Customer)
        mockCustomer.demand.asBoolean(1) { -> return true }

        GrailsMock mockStaticCustomer = mockFor(Customer)
        mockStaticCustomer.demand.static.findByUserAndFacility(1) { User u, Facility f ->
            assert u == user
            assert f == facility
            return mockCustomer.createMock()
        }

        GrailsMock mockStaticOrder = mockFor(Order)
        mockStaticOrder.demand.static.get(1) { Long id ->
            assert id == params.id
            return mockOrder.createMock()
        }

        controller.confirm()

        assert controller.response.status == HttpStatus.UNAUTHORIZED.value()

        springSecurityService.verify()
        mockStaticOrder.verify()
        mockOrder.verify()
        mockStaticCustomer.verify()
        mockCustomer.verify()
    }

    void testConfirmCancelledOrderRefreshed() {
        User user = new User()

        springSecurityService.demand.getCurrentUser(1) { ->
            return user
        }

        controller.params.id = 1l

        PaymentInfo paymentInfo = new PaymentInfo()
        paymentService.demand.getAnyPaymentInfoByUser(1) { User u ->
            assert u == user
            return paymentInfo
        }

        GrailsMock mockFacility = mockFor(Facility)
        Facility facility = mockFacility.createMock()

        GrailsMock mockCustomer = mockFor(Customer)
        mockCustomer.demand.asBoolean(1) { -> return true }
        Customer customer = mockCustomer.createMock()

        BigDecimal orderPrice = 100

        GrailsMock mockOrder = mockFor(Order)
        mockOrder.demand.asBoolean(1) { -> return true }
        mockOrder.demand.isRemotePayable(1) { -> return true }
        mockOrder.demand.getFacility(1) { -> return facility }
        mockOrder.demand.getCustomer(1) { -> return customer }
        mockOrder.demand.getStatus(1) { -> return Order.Status.CANCELLED }
        Order order = mockOrder.createMock()

        GrailsMock newMockOrder = mockFor(Order)
        newMockOrder.demand.getPrice(2) { -> return orderPrice }
        Order newOrder = newMockOrder.createMock()

        orderService.demand.replaceOrderWithFreshCopy(1) { Order o ->
            assert o == order
            return newOrder
        }

        GrailsMock mockStaticCustomer = mockFor(Customer)
        mockStaticCustomer.demand.static.findByUserAndFacility(1) { User u, Facility f ->
            assert u == user
            assert f == facility
            return customer
        }

        GrailsMock mockStaticOrder = mockFor(Order)
        mockStaticOrder.demand.static.get(1) { Long id ->
            assert id == params.id
            return order
        }

        Map result = controller.confirm()

        assert result.totalPrice == orderPrice
        assert result.order == newOrder
        assert result.facility == facility
        assert result.paymentMethodsModel.methods.contains(PaymentMethod.CREDIT_CARD_RECUR)

        springSecurityService.verify()
        paymentService.verify()
        orderService.verify()
        mockStaticOrder.verify()
        mockOrder.verify()
        newMockOrder.verify()
        mockStaticCustomer.verify()
        mockCustomer.verify()
        mockFacility.verify()
    }

    void testConfirmConfirmedOrderRefreshed() {
        User user = new User()

        springSecurityService.demand.getCurrentUser(1) { ->
            return user
        }

        controller.params.id = 1l

        PaymentInfo paymentInfo = new PaymentInfo()
        paymentService.demand.getAnyPaymentInfoByUser(1) { User u ->
            assert u == user
            return paymentInfo
        }

        GrailsMock mockFacility = mockFor(Facility)
        Facility facility = mockFacility.createMock()

        GrailsMock mockCustomer = mockFor(Customer)
        mockCustomer.demand.asBoolean(1) { -> return true }
        Customer customer = mockCustomer.createMock()

        BigDecimal orderPrice = 100

        GrailsMock mockOrder = mockFor(Order)
        mockOrder.demand.asBoolean(1) { -> return true }
        mockOrder.demand.isRemotePayable(1) { -> return true }
        mockOrder.demand.getFacility(1) { -> return facility }
        mockOrder.demand.getCustomer(1) { -> return customer }
        mockOrder.demand.getStatus(1) { -> return Order.Status.CONFIRMED }
        Order order = mockOrder.createMock()

        GrailsMock newMockOrder = mockFor(Order)
        newMockOrder.demand.getPrice(2) { -> return orderPrice }
        Order newOrder = newMockOrder.createMock()

        orderService.demand.replaceOrderWithFreshCopy(1) { Order o ->
            assert o == order
            return newOrder
        }

        GrailsMock mockStaticCustomer = mockFor(Customer)
        mockStaticCustomer.demand.static.findByUserAndFacility(1) { User u, Facility f ->
            assert u == user
            assert f == facility
            return customer
        }

        GrailsMock mockStaticOrder = mockFor(Order)
        mockStaticOrder.demand.static.get(1) { Long id ->
            assert id == params.id
            return order
        }

        Map result = controller.confirm()

        assert result.totalPrice == orderPrice
        assert result.order == newOrder
        assert result.facility == facility
        assert result.paymentMethodsModel.methods.contains(PaymentMethod.CREDIT_CARD_RECUR)

        springSecurityService.verify()
        paymentService.verify()
        orderService.verify()
        mockStaticOrder.verify()
        mockOrder.verify()
        newMockOrder.verify()
        mockStaticCustomer.verify()
        mockCustomer.verify()
        mockFacility.verify()
    }

    void testConfirmEverythingIsNormal() {
        User user = new User()

        springSecurityService.demand.getCurrentUser(1) { ->
            return user
        }

        controller.params.id = 1l

        PaymentInfo paymentInfo = new PaymentInfo()
        paymentService.demand.getAnyPaymentInfoByUser(1) { User u ->
            assert u == user
            return paymentInfo
        }

        GrailsMock mockFacility = mockFor(Facility)
        Facility facility = mockFacility.createMock()

        GrailsMock mockCustomer = mockFor(Customer)
        mockCustomer.demand.asBoolean(1) { -> return true }
        Customer customer = mockCustomer.createMock()

        BigDecimal orderPrice = 100

        GrailsMock mockOrder = mockFor(Order)
        mockOrder.demand.asBoolean(1) { -> return true }
        mockOrder.demand.isRemotePayable(1) { -> return true }
        mockOrder.demand.getFacility(1) { -> return facility }
        mockOrder.demand.getCustomer(1) { -> return customer }
        mockOrder.demand.getStatus(1) { -> return Order.Status.NEW }
        mockOrder.demand.getPrice(2) { -> return orderPrice }
        Order order = mockOrder.createMock()

        GrailsMock mockStaticCustomer = mockFor(Customer)
        mockStaticCustomer.demand.static.findByUserAndFacility(1) { User u, Facility f ->
            assert u == user
            assert f == facility
            return customer
        }

        GrailsMock mockStaticOrder = mockFor(Order)
        mockStaticOrder.demand.static.get(1) { Long id ->
            assert id == params.id
            return order
        }

        Map result = controller.confirm()

        assert result.totalPrice == orderPrice
        assert result.order == order
        assert result.facility == facility
        assert result.paymentMethodsModel.methods.contains(PaymentMethod.CREDIT_CARD_RECUR)

        springSecurityService.verify()
        paymentService.verify()
        mockStaticOrder.verify()
        mockOrder.verify()
        mockStaticCustomer.verify()
        mockCustomer.verify()
        mockFacility.verify()
    }

    void testPayRequiresValidOrder() {
        User user = new User()
        springSecurityService.demand.getCurrentUser(1) { ->
            return user
        }

        RemotePaymentCommand cmd = new RemotePaymentCommand(orderId: 1l)

        GrailsMock mockOrder = mockFor(Order)
        mockOrder.demand.static.get(1) { Long id ->
            assert id == cmd.orderId
            return null
        }

        shouldFail(IllegalStateException) {
            controller.pay(cmd)
        }

        springSecurityService.verify()
        mockOrder.verify()
    }

    void testPayRequiresOrderBeingRemotePayable() {
        User user = new User()
        springSecurityService.demand.getCurrentUser(1) { ->
            return user
        }

        RemotePaymentCommand cmd = new RemotePaymentCommand(orderId: 1l)

        GrailsMock mockOrder = mockFor(Order)
        mockOrder.demand.asBoolean(1) { -> return true }
        mockOrder.demand.isRemotePayable(1) { -> return false }

        GrailsMock mockStaticOrder = mockFor(Order)
        mockStaticOrder.demand.static.get(1) { Long id ->
            assert id == cmd.orderId
            return mockOrder.createMock()
        }

        controller.pay(cmd)

        assert controller.modelAndView.viewName == "/remotePayment/error"

        springSecurityService.verify()
        mockStaticOrder.verify()
        mockOrder.verify()
    }

    void testPayRequiresCustomer() {
        User user = new User()
        Facility facility = new Facility()

        springSecurityService.demand.getCurrentUser(1) { ->
            return user
        }

        RemotePaymentCommand cmd = new RemotePaymentCommand(orderId: 1l)

        GrailsMock mockOrder = mockFor(Order)
        mockOrder.demand.asBoolean(1) { -> return true }
        mockOrder.demand.isRemotePayable(1) { -> return true }
        mockOrder.demand.getFacility(1) { -> return facility }
        Order order = mockOrder.createMock()

        GrailsMock mockStaticCustomer = mockFor(Customer)
        mockStaticCustomer.demand.static.findByUserAndFacility(1) { User u, Facility f ->
            assert u == user
            assert f == facility
            return null
        }

        GrailsMock mockStaticOrder = mockFor(Order)
        mockStaticOrder.demand.static.get(1) { Long id ->
            assert id == cmd.orderId
            return order
        }

        controller.pay(cmd)

        assert controller.response.status == HttpStatus.UNAUTHORIZED.value()

        springSecurityService.verify()
        mockStaticOrder.verify()
        mockOrder.verify()
        mockStaticCustomer.verify()
    }

    void testPayRequiresCustomerSameAsOrders() {
        User user = new User()
        Facility facility = new Facility()

        springSecurityService.demand.getCurrentUser(1) { ->
            return user
        }

        RemotePaymentCommand cmd = new RemotePaymentCommand(orderId: 1l)

        GrailsMock mockOrder = mockFor(Order)
        mockOrder.demand.asBoolean(1) { -> return true }
        mockOrder.demand.isRemotePayable(1) { -> return true }
        mockOrder.demand.getFacility(1) { -> return facility }
        mockOrder.demand.getCustomer(1) { -> return new Customer() }
        Order order = mockOrder.createMock()

        GrailsMock mockCustomer = mockFor(Customer)
        mockCustomer.demand.asBoolean(1) { -> return true }

        GrailsMock mockStaticCustomer = mockFor(Customer)
        mockStaticCustomer.demand.static.findByUserAndFacility(1) { User u, Facility f ->
            assert u == user
            assert f == facility
            return mockCustomer.createMock()
        }

        GrailsMock mockStaticOrder = mockFor(Order)
        mockStaticOrder.demand.static.get(1) { Long id ->
            assert id == cmd.orderId
            return order
        }

        controller.pay(cmd)

        assert controller.response.status == HttpStatus.UNAUTHORIZED.value()

        springSecurityService.verify()
        mockStaticOrder.verify()
        mockOrder.verify()
        mockStaticCustomer.verify()
        mockCustomer.verify()
    }

    void testPayRequiresValidCommandObject() {
        User user = new User()
        Facility facility = new Facility()

        springSecurityService.demand.getCurrentUser(1) { ->
            return user
        }

        RemotePaymentCommand cmd = new RemotePaymentCommand(orderId: 1l)

        GrailsMock mockCustomer = mockFor(Customer)
        mockCustomer.demand.asBoolean(1) { -> return true }
        Customer customer = mockCustomer.createMock()

        GrailsMock mockOrder = mockFor(Order)
        mockOrder.demand.asBoolean(1) { -> return true }
        mockOrder.demand.isRemotePayable(1) { -> return true }
        mockOrder.demand.getFacility(1) { -> return facility }
        mockOrder.demand.getCustomer(1) { -> return customer }
        Order order = mockOrder.createMock()

        GrailsMock mockStaticCustomer = mockFor(Customer)
        mockStaticCustomer.demand.static.findByUserAndFacility(1) { User u, Facility f ->
            assert u == user
            assert f == facility
            return customer
        }

        GrailsMock mockStaticOrder = mockFor(Order)
        mockStaticOrder.demand.static.get(1) { Long id ->
            assert id == cmd.orderId
            return order
        }

        controller.pay(cmd)

        assert controller.modelAndView.viewName == '/remotePayment/showError'

        springSecurityService.verify()
        mockStaticOrder.verify()
        mockOrder.verify()
        mockStaticCustomer.verify()
        mockCustomer.verify()
    }

    void testPayRequiresGatewayMethod() {
        User user = new User()
        Facility facility = new Facility()

        springSecurityService.demand.getCurrentUser(1) { ->
            return user
        }

        RemotePaymentCommand cmd = new RemotePaymentCommand(
                orderId: 1l,
                method: "COUPON" // Not a gateway method
        )

        GrailsMock mockCustomer = mockFor(Customer)
        mockCustomer.demand.asBoolean(1) { -> return true }
        Customer customer = mockCustomer.createMock()

        GrailsMock mockOrder = mockFor(Order)
        mockOrder.demand.asBoolean(1) { -> return true }
        mockOrder.demand.isRemotePayable(1) { -> return true }
        mockOrder.demand.getFacility(1) { -> return facility }
        mockOrder.demand.getCustomer(1) { -> return customer }
        Order order = mockOrder.createMock()

        GrailsMock mockStaticCustomer = mockFor(Customer)
        mockStaticCustomer.demand.static.findByUserAndFacility(1) { User u, Facility f ->
            assert u == user
            assert f == facility
            return customer
        }

        GrailsMock mockStaticOrder = mockFor(Order)
        mockStaticOrder.demand.static.get(1) { Long id ->
            assert id == cmd.orderId
            return order
        }

        controller.pay(cmd)
        assert controller.modelAndView.viewName == '/remotePayment/showError'

        springSecurityService.verify()
        mockStaticOrder.verify()
        mockOrder.verify()
        mockStaticCustomer.verify()
        mockCustomer.verify()
    }

    void testPayAllGood() {
        User user = new User()
        Facility facility = new Facility()

        springSecurityService.demand.getCurrentUser(1) { ->
            return user
        }

        RemotePaymentCommand cmd = new RemotePaymentCommand(
                orderId: 1l,
                method: "CREDIT_CARD"
        )

        GrailsMock mockCustomer = mockFor(Customer)
        mockCustomer.demand.asBoolean(1) { -> return true }
        Customer customer = mockCustomer.createMock()

        GrailsMock mockOrder = mockFor(Order)
        mockOrder.demand.asBoolean(1) { -> return true }
        mockOrder.demand.isRemotePayable(1) { -> return true }
        mockOrder.demand.getFacility(1) { -> return facility }
        mockOrder.demand.getCustomer(1) { -> return customer }
        Order order = mockOrder.createMock()

        GrailsMock mockStaticCustomer = mockFor(Customer)
        mockStaticCustomer.demand.static.findByUserAndFacility(1) { User u, Facility f ->
            assert u == user
            assert f == facility
            return customer
        }

        GrailsMock mockStaticOrder = mockFor(Order)
        mockStaticOrder.demand.static.get(1) { Long id ->
            assert id == cmd.orderId
            return order
        }

        controller.metaClass.getPaymentProviderParameters = { PaymentMethod method, Order o, User u ->
            assert method.equals(PaymentMethod.CREDIT_CARD)
            assert o == order
            assert u == user
        }

        controller.pay(cmd)

        springSecurityService.verify()
        mockStaticOrder.verify()
        mockOrder.verify()
        mockStaticCustomer.verify()
        mockCustomer.verify()
    }

    void testOrderDateDeliveryIsUpdatedToPaymentDateCreated() {
        Date twoDaysAgo = new LocalDateTime().minusDays(2).toDate() // Delivery was two days ago

        User user          = createUser()
        Facility facility  = createFacility()
        Order order        = createOrder(user, facility)
        order.id           = 1L
        order.dateDelivery = twoDaysAgo

        AdyenOrderPayment payment = createAdyenOrderPayment(user, order, "123")
        payment.dateCreated = new Date()
        order.payments << payment

        springSecurityService.demand.getCurrentUser(1) { -> return user }
        scheduledTaskService.demand.scheduleTask(1) { -> }

        //controller.session[PaymentFlow.PAYMENT_FLOW] = [:].put(order.id, new PaymentFlow(order.id))
        controller.startPaymentFlow(order.id, 'http://localhost:8080/facilities/gltk')

        controller.params.orderId = 1l
        controller.processArticle(order)

        assert order.dateDelivery != twoDaysAgo
        assert order.dateDelivery == payment.dateCreated
    }
}
