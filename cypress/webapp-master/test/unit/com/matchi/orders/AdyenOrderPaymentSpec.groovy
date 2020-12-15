package com.matchi.orders

import com.matchi.*
import com.matchi.adyen.AdyenNotification
import com.matchi.adyen.AdyenService
import com.matchi.adyen.authorization.*
import com.matchi.integration.IntegrationService
import com.matchi.payment.PaymentMethod
import grails.plugin.springsecurity.SpringSecurityService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.codehaus.groovy.grails.commons.InstanceFactoryBean
import org.joda.time.DateTime
import org.joda.time.LocalDateTime
import org.junit.Before
import org.junit.Test

/**
 * See the API for {@link grails.test.mixin.domain.DomainClassUnitTestMixin} for usage instructions
 */
@TestFor(AdyenOrderPayment)
@Mock([Order, OrderRefund, User, Facility, AdyenOrderPaymentError, Booking, Customer])
class AdyenOrderPaymentSpec {

    Order order
    AdyenOrderPayment payment

    LocalDateTime now
    LocalDateTime tomorrow
    LocalDateTime sevenDaysAgo
    LocalDateTime twelveHoursAgo

    def mockCustomerService
    def mockAdyenService
    def mockedConfig

    OrderStatusService orderStatusService

    private static final def FULL_ORDER_SUM = 100
    private static final def HALF_ORDER_SUM = 50
    private static final def FACILITY_CURRENCY = "SEK"
    private static final def GLOBAL_ID = 1l

    @Before
    void setup() {
        now = new LocalDateTime()
        tomorrow = new LocalDateTime().plusDays(1)

        twelveHoursAgo = new LocalDateTime().minusHours(12)
        sevenDaysAgo = new LocalDateTime().minusDays(7)

        order = createOrder()
        payment = createAdyenOrderPayment()

        mockAdyenService = mockFor(AdyenService)
        mockCustomerService = mockFor(CustomerService)

        mockedConfig = new ConfigObject()
        mockedConfig.adyen.merchant = "test"

        orderStatusService = new OrderStatusService()
        def mockIntegrationService = mockFor(IntegrationService)
        orderStatusService.integrationService = mockIntegrationService.createMock()
        mockIntegrationService.demand.send(1..2) {  }

        defineBeans {
            customerService(InstanceFactoryBean, mockCustomerService.createMock(), CustomerService)
            adyenService(InstanceFactoryBean, mockAdyenService.createMock(), AdyenService)
            orderStatusService(InstanceFactoryBean, orderStatusService, OrderStatusService)
            grailsApplication = [config: mockedConfig]
            dateUtil(DateUtil)
        }
    }

    @Test
    void testCancelPaymentIsUsedWhenNotCapturedAndFullRefundIsMade() {
        payment.status = OrderPayment.Status.AUTHED

        mockAdyenService.demand.cancel(1) {}
        payment.refund(FULL_ORDER_SUM)
    }

    @Test
    void testRefundPaymentIsUsedWhenPaymentIsCapturedAndCreditIsMade() {
        payment.status = OrderPayment.Status.CAPTURED

        mockAdyenService.demand.amountToAdyenFormat(1) { amount -> }
        mockAdyenService.demand.refund(1) {}
        payment.refund(FULL_ORDER_SUM)
    }

    @Test
    void testNothingIsMadeIfPaymentIsNotCapturedAndPartialRefundIsMade() {
        payment.status = OrderPayment.Status.AUTHED

        payment.refund(HALF_ORDER_SUM)
    }

    @Test
    void createModificationRequestContainsMandatoryFields() {
        payment.orders << order

        mockAdyenService.demand.amountToAdyenFormat(1) { amount -> FULL_ORDER_SUM }
        Map request = payment.createModificationRequest()

        assert request.merchantAccount && request.merchantAccount != ""
        assert request.originalReference && request.originalReference != ""
        assert request.modificationAmount && request.modificationAmount.value
        assert request.modificationAmount && request.modificationAmount.currency == FACILITY_CURRENCY
    }

    @Test
    void createModificationRequestUsesCorrectModificationsAmountOnCapture() {
        payment.orders << order

        mockAdyenService.demand.amountToAdyenFormat(1) { amount, currency -> FULL_ORDER_SUM }
        Map request = payment.createModificationRequest()

        assert request.modificationAmount.value == payment.amount
    }

    @Test
    void createModificationRequestUsesCorrectModificationsAmountOnRefund() {
        payment.orders << order
        payment.credited = new BigDecimal(HALF_ORDER_SUM)

        mockAdyenService.demand.amountToAdyenFormat(1) { amount, currency -> HALF_ORDER_SUM }
        Map request = payment.createModificationRequest()

        assert request.modificationAmount.value == HALF_ORDER_SUM
    }

    @Test
    void createAuthoriseRequestContainsMandatoryFieldsDefault() {
        Map params = [:]
        params.put("adyen-encrypted-data", "adyen-encrypted-data")

        payment.orders << order
        mockAdyenService.demand.amountToAdyenFormat(1) { amount -> FULL_ORDER_SUM }
        AdyenAuthorizationBuilder adyenAuthorizationBuilder = AdyenAuthorizationBuilderFactory.createNewDetailsAuthorization()

        assert adyenAuthorizationBuilder instanceof AdyenNewCardOneTimeAuthorizationBuilder

        Map request = payment.createAuthorizeRequest(params, order, adyenAuthorizationBuilder)

        assert request.additionalData && request.additionalData != ""
        assert request.browserInfo && request.browserInfo != ""
        assert request.merchantAccount && request.merchantAccount != ""
        assert !request.shopperEmail && !request.shopperReference
        validateReference(order, request.reference)
    }

    @Test
    void createAuthoriseRequestContainsMandatoryFieldsWhenSaveCard() {
        Map params = [:]
        params.put("adyen-encrypted-data", "adyen-encrypted-data")

        payment.orders << order
        mockAdyenService.demand.amountToAdyenFormat(1) { amount -> FULL_ORDER_SUM }

        AdyenAuthorizationBuilder adyenAuthorizationBuilder = AdyenAuthorizationBuilderFactory.createNewDetailsAuthorization(true)

        assert adyenAuthorizationBuilder instanceof AdyenNewCardStoreDetailsAuthorizationBuilder

        Map request = payment.createAuthorizeRequest(params, order, adyenAuthorizationBuilder)

        assert request.additionalData && request.additionalData != ""
        assert request.browserInfo && request.browserInfo != ""
        assert request.merchantAccount && request.merchantAccount != ""
        assert request.shopperEmail && request.recurring != ""
        validateReference(order, request.reference)
    }

    @Test
    void createAuthoriseRequestContainsMandatoryFieldsWhenUseCard() {
        payment.orders << order
        mockAdyenService.demand.amountToAdyenFormat(1) { amount -> FULL_ORDER_SUM }

        AdyenAuthorizationBuilder adyenAuthorizationBuilder = AdyenAuthorizationBuilderFactory.createStoredDetailsAuthorization()

        assert adyenAuthorizationBuilder instanceof AdyenCardOnFileAuthorizationBuilder

        Map request = payment.createAuthorizeRequest([:], order, adyenAuthorizationBuilder)

        assert request.browserInfo && request.browserInfo != ""
        assert request.merchantAccount && request.merchantAccount != ""
        assert request.shopperEmail && request.recurring != "" &&
                request.shopperInteraction != "" && request.selectedRecurringDetailReference != ""
        validateReference(order, request.reference)

    }

    boolean validateReference(Order order, String reference) {
        String[] parts = reference.split("-")

        assert reference && reference != ""
        assert order.id == Long.valueOf(parts[0])
        assert order.dateDelivery.format(DateUtil.DEFAULT_DATE_SHORT_FORMAT, TimeZone.getTimeZone('UTC')) == parts[1]
    }

    @Test
    void testAddErrorCancelsOrder() {
        order.status = Order.Status.NEW
        payment.orders << order

        def customer = new Customer()
        customer.facility = new Facility()
        mockCustomerService.demand.getOrCreateUserCustomer(1) { user, facility -> customer}

        payment.addError(new AdyenNotification([eventCode: AdyenNotification.EventCode.CAPTURE]), TestUtils.createSystemEventInitiator())

        assert payment.status == OrderPayment.Status.FAILED
        assert order.status == Order.Status.CANCELLED
    }

    @Test
    void testRetryFailedPaymentsRetriesCapture() {
        def customer = new Customer()
        customer.facility = new Facility()
        mockCustomerService.demand.getOrCreateUserCustomer(2) { user, facility -> customer }
        mockAdyenService.demand.amountToAdyenFormat(1) { amount -> return 100 }
        mockAdyenService.demand.capture(1) {}

        payment.addError(new AdyenNotification([eventCode: AdyenNotification.EventCode.CAPTURE]), TestUtils.createSystemEventInitiator())

        assert payment.error != null

        payment.retry()
    }

    @Test
    void testRetryFailedPaymentsRetriesRefund() {
        def customer = new Customer()
        customer.facility = new Facility()
        mockCustomerService.demand.getOrCreateUserCustomer(1) { user, facility -> customer}
        mockAdyenService.demand.amountToAdyenFormat(1) { amount -> }
        mockAdyenService.demand.cancel(1) { amount -> }

        payment.addError(new AdyenNotification([eventCode: AdyenNotification.EventCode.CANCEL_OR_REFUND]), TestUtils.createSystemEventInitiator())

        assert payment.error != null
        payment.retry()
    }

    @Test
    void testPaymentIsNotRefundedIfNoArticleIsVerified() {
        order.article = Order.Article.BOOKING
        new Booking(order: order).save(validate: false)
        payment.orders << order

        def verified = payment.refundOrderIfNoArticleCreated()

        assert verified
        assert payment.total() == FULL_ORDER_SUM
    }

    @Test
    void testPaymentIsRefundedIfNoArticleIsVerified() {
        def mockSpringSecurityService = mockFor(SpringSecurityService)
        defineBeans {
            springSecurityService(InstanceFactoryBean, mockSpringSecurityService.createMock(), SpringSecurityService)
        }

        mockSpringSecurityService.demand.getCurrentUser(1) { ->
            return new User(id: GLOBAL_ID, email: "test@email.com")
        }

        mockAdyenService.demand.cancel(1) { amount -> }

        order.article = Order.Article.BOOKING
        payment.orders << order
        order.payments << payment

        def verified = payment.refundOrderIfNoArticleCreated()

        assert !verified
        assert payment.total() == 0
    }

    @Test
    void testGetPredictedDateOfCapture() {
        AdyenOrderPayment payment = createAdyenOrderPayment()
        payment.dateCreated = new Date()
        payment.lastUpdated = new Date()
        payment.method = PaymentMethod.CREDIT_CARD
        payment.orders = null
        payment.save(flush: true, failOnError: true)

        assert !payment.getPredictedDateOfCapture()

        Order order = createOrder()
        order.issuer = payment.issuer
        order.article = Order.Article.BOOKING
        order.description = "Hellooo"
        order.save(flush: true, failOnError: true)

        payment.orders = [order]
        payment.save(flush: true, failOnError: true)

        assert payment.getPredictedDateOfCapture() == new DateTime(order.dateDelivery).plusHours(AdyenOrderPayment.CAPTURE_DELAY).toDate()

        order.dateDelivery = new Date().plus(AdyenService.MAX_WAIT_CAPTURE_DAYS + 5)
        order.save(flush: true, failOnError: true)

        assert payment.getPredictedDateOfCapture() == new DateTime(payment.dateCreated).plusDays(AdyenService.MAX_WAIT_CAPTURE_DAYS).plusHours(12).toDate()

        payment.status = OrderPayment.Status.CAPTURED
        payment.save(flush: true, failOnError: true)

        assert payment.getPredictedDateOfCapture() == new DateTime(payment.lastUpdated).plusHours(AdyenOrderPayment.CAPTURE_DELAY).toDate()
    }

    @Test
    void testCaptureWhenCreditedAmount() {
        payment.status = OrderPayment.Status.AUTHED
        payment.credited = 87.5
        payment.amount = 100

        mockAdyenService.demand.amountToAdyenFormat(1) { BigDecimal a, String currency ->
            assert a == (payment.amount - payment.credited)
            return a * 100
        }

        mockAdyenService.demand.capture(1) { Map data, Closure callback ->
            assert data.get('modificationAmount').get('value') == ((payment.amount - payment.credited) * 100)

            Map response = [:]
            callback.call(response)
        }

        payment.capture()

        assert payment.status == OrderPayment.Status.CREDITED
        mockAdyenService.verify()
    }

    @Test
    void testCaptureWhenNoCreditedAmount() {
        payment.status = OrderPayment.Status.AUTHED
        payment.credited = 0
        payment.amount = 100

        mockAdyenService.demand.amountToAdyenFormat(1) { BigDecimal a, String currency ->
            assert a == (payment.amount - payment.credited)
            return a * 100
        }

        mockAdyenService.demand.capture(1) { Map data, Closure callback ->
            assert data.get('modificationAmount').get('value') == ((payment.amount - payment.credited) * 100)

            Map response = [:]
            callback.call(response)
        }

        payment.capture()

        assert payment.status == OrderPayment.Status.CAPTURED
        mockAdyenService.verify()
    }

    @Test
    void testGetReturnAction() {
        payment.status = OrderPayment.Status.AUTHED
        assert payment.getReturnAction() == AdyenOrderPayment.PROCESS_ACTION

        payment.status = OrderPayment.Status.CAPTURED
        assert payment.getReturnAction() == AdyenOrderPayment.PROCESS_ACTION

        // And the rest, no...
        OrderPayment.Status.list().findAll { !(it in [OrderPayment.Status.AUTHED, OrderPayment.Status.CAPTURED]) }.each { OrderPayment.Status status ->
            payment.status = status
            assert payment.getReturnAction() == status.name().toLowerCase()
        }
    }

    @Test
    void testStatusIsProcessable() {
        List<OrderPayment.Status> processables = [OrderPayment.Status.AUTHED, OrderPayment.Status.CAPTURED]

        OrderPayment.Status.list().findAll { !(it in processables) }.each { OrderPayment.Status status ->
            assert !status.isProcessable
        }

        OrderPayment.Status.list().findAll { it in processables }.each { OrderPayment.Status status ->
            assert status.isProcessable
        }
    }

    @Test
    void testOrdersHaveStatus() {
        AdyenOrderPayment payment = new AdyenOrderPayment()
        Order.Status.list().each { Order.Status status ->
            assert !payment.ordersHaveStatus(status)
        }

        payment.orders = []
        Order.Status.list().each { Order.Status status ->
            assert !payment.ordersHaveStatus(status)
        }

        order.status = Order.Status.NEW
        payment.orders << order

        assert payment.ordersHaveStatus(Order.Status.NEW)
        Order.Status.list().findAll { it != Order.Status.NEW }.each { Order.Status status ->
            assert !payment.ordersHaveStatus(status)
        }

        Order confirmedOrder = new Order(status: Order.Status.CONFIRMED)
        payment.orders << confirmedOrder

        Order.Status.list().each { Order.Status status ->
            assert !payment.ordersHaveStatus(status)
        }

        confirmedOrder.status = Order.Status.NEW
        assert payment.ordersHaveStatus(Order.Status.NEW)
        Order.Status.list().findAll { it != Order.Status.NEW }.each { Order.Status status ->
            assert !payment.ordersHaveStatus(status)
        }
    }

    @Test
    void testAllowLateRefund() {
        assert !payment.allowLateRefund()
    }

    @Test
    void testIsPendingChecks() {
        payment.status = OrderPayment.Status.PENDING
        payment.orders = []
        assert !payment.isPendingTimeout()
        assert !payment.isPendingWaiting()

        payment.orders = [order]
        payment.orders[0].status = Order.Status.CANCELLED
        assert payment.isPendingTimeout()
        assert !payment.isPendingWaiting()

        payment.orders[0].status = Order.Status.CONFIRMED
        assert !payment.isPendingTimeout()
        assert payment.isPendingWaiting()

        def mockAdyenNotification = mockFor(AdyenNotification)
        mockAdyenNotification.demand.hasWaitedForThreshold(1) { -> false }
        assert !payment.isPendingInterrupted(mockAdyenNotification.createMock())

        mockAdyenNotification.demand.hasWaitedForThreshold(1) { -> true }
        assert payment.isPendingInterrupted(mockAdyenNotification.createMock())
    }

    @Test
    void testOnAuthorisationEventLocalMethod() {
        def mockAdyenNotification = mockFor(AdyenNotification)

        /*
         * First case is to test a failed payment. Should be refunded.
         */

        def mockOrder = mockFor(Order)

        payment.orders = [mockOrder.createMock()]
        payment.status = OrderPayment.Status.FAILED

        shouldFail(IllegalAccessException) {
            payment.handleNotificationForPending(mockAdyenNotification.createMock(), TestUtils.createSystemEventInitiator())
        }

        /*
         * Second case is to test a timed out payment. Should be refunded also.
         */

        payment.status = OrderPayment.Status.PENDING
        mockOrder.demand.getStatus(1) { -> Order.Status.CANCELLED }
        // Order is CANCELLED before, no need to further cancel
        mockOrder.demand.refund(1) { String note ->
            assert note == AdyenOrderPayment.LATE_AUTH_REFUND_NOTE
        }

        payment.orders = [mockOrder.createMock()]
        payment.handleNotificationForPending(mockAdyenNotification.createMock(), TestUtils.createSystemEventInitiator())
        mockAdyenNotification.verify()
        mockOrder.verify()

        /*
         * Third case is to test an interrupted pending payment. Should be refunded also.
         */

        mockOrder.demand.getStatus(2) { -> Order.Status.CONFIRMED }
        mockOrder.demand.refund(1) { String note ->
            assert note == AdyenOrderPayment.INTERRUPTED_PENDING_REFUND_NOTE
        }
        mockOrder.demand.assertCustomer(1){ -> }
        mockOrder.demand.save(1){ -> }

        mockAdyenNotification.demand.hasWaitedForThreshold(1) { -> true }

        payment.orders = [mockOrder.createMock()]
        payment.handleNotificationForPending(mockAdyenNotification.createMock(), TestUtils.createSystemEventInitiator())
        mockAdyenNotification.verify()
        mockOrder.verify()

        /*
         * Final case is to test neither of the above, should result in COMPLETED/CAPTURE
         */

        mockAdyenNotification.demand.hasWaitedForThreshold(1) { -> false }
        mockOrder.demand.getStatus(2) { -> Order.Status.CONFIRMED }
        mockOrder.demand.refund(0) { -> }
        mockOrder.demand.assertCustomer(1){ -> }
        mockOrder.demand.save(1){ -> }

        payment.handleNotificationForPending(mockAdyenNotification.createMock(), TestUtils.createSystemEventInitiator())
        assert payment.status == OrderPayment.Status.CAPTURED
        mockOrder.verify()
        mockAdyenNotification.verify()
    }

    @Test
    void testSetPending() {
        Map data = [pspReference: 'abc123']
        payment.setPending(data)

        assert payment.status == OrderPayment.Status.PENDING
        assert payment.transactionId == data.pspReference
    }

    @Test
    void testHasReachedTimeout() {
        payment.lastUpdated = new Date()
        int limit = 15000

        // If now = payment.lastUpdated it has not reached timeout yet
        assert !payment.hasReachedTimeOut(limit, payment.lastUpdated)

        // Only half the time
        Date now = new DateTime(payment.lastUpdated).plusMillis((int) limit / 2).toDate()
        assert !payment.hasReachedTimeOut(limit, now)

        // Now, exactly
        now = new DateTime(payment.lastUpdated).plusMillis(limit).toDate()
        assert payment.hasReachedTimeOut(limit, now)

        // Twice the time
        now = new DateTime(payment.lastUpdated).plusMillis(limit * 2).toDate()
        assert payment.hasReachedTimeOut(limit, now)
    }

    private AdyenOrderPayment createAdyenOrderPayment() {
        AdyenOrderPayment orderPayment = new AdyenOrderPayment()
        orderPayment.id = GLOBAL_ID
        orderPayment.transactionId = GLOBAL_ID
        orderPayment.amount = FULL_ORDER_SUM
        orderPayment.credited = 0l
        orderPayment.issuer = new User(id: GLOBAL_ID, email: "test@email.com")
        orderPayment.orders = [order]

        orderPayment.save(validate: false)
    }

    private createOrder() {
        Order order = new Order(id: GLOBAL_ID)
        order.facility = new Facility(id: GLOBAL_ID, currency: FACILITY_CURRENCY)

        order.price = FULL_ORDER_SUM
        order.dateCreated = tomorrow.toDate()
        order.dateDelivery = tomorrow.toDate()
        order.payments = []

        order.save(validate: false)
    }
}
