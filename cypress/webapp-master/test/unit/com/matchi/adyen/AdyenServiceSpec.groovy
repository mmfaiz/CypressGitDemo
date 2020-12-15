package com.matchi.adyen

import com.matchi.*
import com.matchi.integration.IntegrationService
import com.matchi.orders.AdyenOrderPayment
import com.matchi.orders.Order
import com.matchi.orders.OrderPayment
import com.matchi.payment.PaymentMethod
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.codehaus.groovy.grails.commons.DefaultGrailsApplication
import org.codehaus.groovy.grails.commons.InstanceFactoryBean
import org.joda.time.LocalDateTime
import org.junit.Before
import org.junit.Test

import static com.matchi.TestUtils.*

/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@TestFor(AdyenService)
@Mock([User, PaymentInfo, AdyenNotification, AdyenOrderPayment, Facility, Order, Region, Municipality, Customer])
class AdyenServiceSpec {

    User user
    OrderStatusService orderStatusService

    private static final def CARD_EXPIRY_MONTH_TWO = "10"
    private static final def CARD_EXPIRY_MONTH_ONE = "8"
    private static final def CARD_EXPIRY_YEAR      = "2018"
    private static final def CARD_NUMBER           = "9245"
    private static final def CARD_ISSUER           = "visa"
    private static final def CARD_HOLDER_NAME      = "John Doe"
    private static final def GLOBAL_ID             = 1l


    @Before
    void setUp() {
        user = new User(id: GLOBAL_ID).save(validate: false)

        orderStatusService = new OrderStatusService()
        def mockIntegrationService = mockFor(IntegrationService)
        orderStatusService.integrationService = mockIntegrationService.createMock()
        mockIntegrationService.demand.send(1..1) {  }
        defineBeans {
            orderStatusService(InstanceFactoryBean, orderStatusService, OrderStatusService)
        }
    }

    @Test
    void testAdyenAmount() {
        def grailsApplication = new DefaultGrailsApplication()
        grailsApplication.config.matchi.settings.currency = [
                SEK: [
                        serviceFee   : 12.5,
                        decimalPoints: 2
                ],
                IDR: [
                        serviceFee   : 14000,
                        decimalPoints: 0
                ]
        ]

        service.grailsApplication = grailsApplication
        assert service.amountToAdyenFormat(12, 'SEK') == 1200
        assert service.amountToAdyenFormat(1231, 'IDR') == 1231
        assert TestUtils.npeHappens {service.amountToAdyenFormat(1234, 'FOOBAR')}
    }

    @Test
    void testSavePaymentInfoCardDetails() {
        PaymentInfo paymentInfo = service.savePaymentInfo(user, createAuthResponseAdditionalData())

        def mockAdyenHttpService = mockFor(AdyenHttpService)
        String testUrl = "http://localhost"

        mockAdyenHttpService.demand.getDisableRecurringUrl(1) { ->
            return testUrl
        }

        mockAdyenHttpService.demand.request(1) { url, map ->
            assert url == testUrl
        }

        service.adyenHttpService = mockAdyenHttpService.createMock()

        assert paymentInfo.expiryDate  == "0" + CARD_EXPIRY_MONTH_ONE+CARD_EXPIRY_YEAR.substring(2,4)
        assert paymentInfo.expiryMonth == "0" + CARD_EXPIRY_MONTH_ONE
        assert paymentInfo.expiryYear  == CARD_EXPIRY_YEAR
        assert paymentInfo.number      == CARD_NUMBER
        assert paymentInfo.issuer      == CARD_ISSUER
        assert paymentInfo.holderName  == CARD_HOLDER_NAME

        paymentInfo = service.savePaymentInfo(user, createAuthResponseAdditionalData(CARD_EXPIRY_MONTH_TWO))

        assert paymentInfo.expiryDate  == CARD_EXPIRY_MONTH_TWO+CARD_EXPIRY_YEAR.substring(2,4)
        assert paymentInfo.expiryMonth == CARD_EXPIRY_MONTH_TWO
        assert paymentInfo.expiryYear  == CARD_EXPIRY_YEAR
        assert paymentInfo.number      == CARD_NUMBER
        assert paymentInfo.issuer      == CARD_ISSUER
        assert paymentInfo.holderName  == CARD_HOLDER_NAME

        mockAdyenHttpService.verify()
    }

    @Test
    void testProcessNotificationsDoesntRunSamePSPAndEventCodeTwice() {
        Facility facility         = createFacility(createMunicipality(createRegion()))
        User user                 = createUser()
        Order order               = createOrder(user, facility)
        AdyenOrderPayment payment = createAdyenOrderPayment(user, order,"1", OrderPayment.Status.CAPTURED)

        AdyenNotification n1 = createAdyenNotification(1l, "1", AdyenNotification.EventCode.CAPTURE,
                Boolean.TRUE)
        AdyenNotification n2 = createAdyenNotification(2l,"1", AdyenNotification.EventCode.CAPTURE,
                Boolean.FALSE)

        n1.dateCreated = new LocalDateTime().minusMinutes(10).toDate()
        n2.dateCreated = new LocalDateTime().minusMinutes(10).toDate()

        n1.save(failOnError: true, flush: true)
        n2.save(failOnError: true, flush: true)

        service.processNotificationsAsJob()

        assert payment.status == OrderPayment.Status.CAPTURED
    }

    @Test
    void testProcessNotificationsKeepsSamePSPIfEventCodeDiffers() {
        Facility facility         = createFacility(createMunicipality(createRegion()))
        User user                 = createUser()
        Order order               = createOrder(user, facility)
        AdyenOrderPayment payment1 = createAdyenOrderPayment(user, order,"1", OrderPayment.Status.CAPTURED)
        AdyenOrderPayment payment2 = createAdyenOrderPayment(user, order,"2", OrderPayment.Status.CAPTURED)

        AdyenNotification n1 = createAdyenNotification(1l, "1", AdyenNotification.EventCode.CAPTURE,
                Boolean.TRUE)
        AdyenNotification n2 = createAdyenNotification(2l,"1", AdyenNotification.EventCode.REFUND,
                Boolean.TRUE)
        AdyenNotification n3 = createAdyenNotification(3l,"2", AdyenNotification.EventCode.CAPTURE,
                Boolean.TRUE)

        n1.dateCreated = new LocalDateTime().minusMinutes(AdyenService.MIN_NOTIFICATION_THRESHOLD*2).toDate()
        n2.dateCreated = new LocalDateTime().minusMinutes(AdyenService.MIN_NOTIFICATION_THRESHOLD*2).toDate()
        n3.dateCreated = new LocalDateTime().minusMinutes(AdyenService.MIN_NOTIFICATION_THRESHOLD*2).toDate()

        n1.save(failOnError: true, flush: true)
        n2.save(failOnError: true, flush: true)
        n3.save(failOnError: true, flush: true)

        service.processNotificationsAsJob()
        assert payment1.status == OrderPayment.Status.CAPTURED
        assert payment2.status == OrderPayment.Status.CAPTURED

        service.processNotificationsAsJob()
        assert payment1.status == OrderPayment.Status.CREDITED
    }

    @Test
    void checkAndProcessNotifications() {
        Facility facility         = createFacility(createMunicipality(createRegion()))
        User user                 = createUser()
        Customer customer         = createCustomer(facility, user.email)

        customer.user = user
        customer.save(failOnError: true, flush: true)

        Order order = createOrder(user, facility)
        order.status = Order.Status.CONFIRMED
        order.customer = customer
        order.save(failOnError: true, flush: true)

        Order order2 = createOrder(user, facility)
        order2.status = Order.Status.CONFIRMED
        order2.customer = customer
        order2.save(failOnError: true, flush: true)

        AdyenOrderPayment payment1 = createAdyenOrderPayment(user, order,"1", OrderPayment.Status.PENDING, PaymentMethod.SWISH)
        AdyenOrderPayment payment2 = createAdyenOrderPayment(user, order2,"2", OrderPayment.Status.PENDING, PaymentMethod.SWISH)

        AdyenNotification n1 = createAdyenNotification(1l, "1", AdyenNotification.EventCode.AUTHORISATION,
                Boolean.TRUE, Boolean.FALSE)
        AdyenNotification n2 = createAdyenNotification(1l, "2", AdyenNotification.EventCode.AUTHORISATION,
                Boolean.TRUE, Boolean.FALSE)

        service.checkAndProcessNotificationsWithoutTimeLimit(payment1, TestUtils.createSystemEventInitiator())
        assert payment1.status == OrderPayment.Status.CAPTURED
        assert payment1.orders[0].status == Order.Status.COMPLETED

        assert payment2.status == OrderPayment.Status.PENDING
        assert payment2.orders[0].status == Order.Status.CONFIRMED

        assert n1.executed
        assert !n2.executed
    }

    @Test
    void testprocessOrTimeOutPending() {
        def mockAdyenOrderPayment = mockFor(AdyenOrderPayment)

        mockAdyenOrderPayment.demand.isPendingWaiting(1) { ->
            return false
        }

        shouldFail {
            service.processOrTimeOutPending(mockAdyenOrderPayment.createMock(), TestUtils.createSystemEventInitiator())
        }

        mockAdyenOrderPayment.verify()

        mockAdyenOrderPayment.demand.isPendingWaiting(1) { ->
            return true
        }

        mockAdyenOrderPayment.demand.getTransactionId(1) { ->
            return 1l
        }

        mockAdyenOrderPayment.demand.isPendingWaiting(1) { ->
            return true
        }

        long timeout = 15000

        mockAdyenOrderPayment.demand.hasReachedTimeOut(1) { t, d ->
            assert t == timeout
            return true
        }

        def mockOrder = mockFor(Order)
        mockOrder.demand.assertCustomer(1) { -> }
        mockOrder.demand.save(1) { -> }

        mockAdyenOrderPayment.demand.getOrders(1) { ->
            return [mockOrder.createMock()]
        }

        service.grailsApplication = [ config: [ matchi: [ pending: [ timeout: timeout ]] ] ]
        service.processOrTimeOutPending(mockAdyenOrderPayment.createMock(), TestUtils.createSystemEventInitiator())

        mockAdyenOrderPayment.verify()
        mockOrder.verify()
    }

    private static Map createAuthResponseAdditionalData(String month = CARD_EXPIRY_MONTH_ONE) {
        Map response = [:]

        response.put("expiryDate",     "${month}/${CARD_EXPIRY_YEAR}")
        response.put("cardSummary",    "${CARD_NUMBER}")
        response.put("paymentMethod",  "${CARD_ISSUER}")
        response.put("cardHolderName", "${CARD_HOLDER_NAME}")

        response
    }
}
