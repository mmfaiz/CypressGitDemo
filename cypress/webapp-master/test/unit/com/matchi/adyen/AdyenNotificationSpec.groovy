package com.matchi.adyen

import com.matchi.PaymentInfo
import com.matchi.TestUtils
import com.matchi.User
import com.matchi.events.EventInitiator
import com.matchi.orders.AdyenOrderPayment
import com.matchi.orders.AdyenOrderPaymentError
import com.matchi.orders.Order
import com.matchi.orders.OrderPayment
import com.matchi.payment.PaymentMethod
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.codehaus.groovy.grails.commons.InstanceFactoryBean
import org.joda.time.DateTime
import org.junit.Before
import org.junit.Test

@TestFor(AdyenNotification)
@Mock([Order, AdyenOrderPayment, AdyenOrderPaymentError, User, PaymentInfo])
class AdyenNotificationSpec {

    AdyenOrderPayment orderPayment
    AdyenOrderPaymentError paymentError
    AdyenNotification notification
    User user

    def mockAdyenService
    def mockedConfig

    private static final def FULL_ORDER_SUM = 100
    private static final def GLOBAL_ID      = 1l

    @Before
    void setUp() {
        user = new User(id: GLOBAL_ID).save(validate: false)

        notification   = createNotificationRequest()
        orderPayment   = createAdyenOrderPayment()
        paymentError   = createAdyenOrderPaymentError()

        mockAdyenService = mockFor(AdyenService)

        mockedConfig = new ConfigObject()
        mockedConfig.adyen.merchant = "test"


        defineBeans {
            adyenService(InstanceFactoryBean, mockAdyenService.createMock(), AdyenService)
        }
    }

    @Test
    void testConstructor() {
        Map<String, Object> params = [:]
        params.put("eventCode", "CAPTURE")
        params.put("pspReference", "123")
        params.put("success", "true")
        params.put("reason", "TEST")
        params.put("additionalData", ["data": "999"])

        AdyenNotification notification = new AdyenNotification(params)

        assert notification.eventCode.equals(AdyenNotification.EventCode.CAPTURE)
        assert notification.pspReference == "123"
        assert notification.success == Boolean.TRUE
        assert notification.reason != ""
        assert !notification.hasProperty("additionalData") // We very much want it removed :D
    }

    @Test
    void testProcessNotificationSetsAnnulledOnCancellation() {
        notification.eventCode = AdyenNotification.EventCode.CANCELLATION
        notification.process(orderPayment, TestUtils.createSystemEventInitiator())
        assert orderPayment.status == OrderPayment.Status.ANNULLED
    }

    @Test
    void testProcessNotificationSetsCreditedOnRefund() {
        notification.eventCode = AdyenNotification.EventCode.REFUND
        orderPayment.credited = FULL_ORDER_SUM - 10
        notification.process(orderPayment, TestUtils.createSystemEventInitiator())
        assert orderPayment.status == OrderPayment.Status.CREDITED
    }

    @Test
    void testProcessNotificationSetsAnnulledOnRefund() {
        notification.eventCode = AdyenNotification.EventCode.REFUND
        orderPayment.credited = FULL_ORDER_SUM
        notification.process(orderPayment, TestUtils.createSystemEventInitiator())
        assert orderPayment.status == OrderPayment.Status.CREDITED
    }

    @Test
    void testProcessNotificationDoesNotCreateErrorIfSuccessful() {
        notification.eventCode = AdyenNotification.EventCode.CAPTURE
        notification.process(orderPayment, TestUtils.createSystemEventInitiator())
        assert !orderPayment.error
    }

    @Test
    void testProcessNotificationCreatesErrorIfFailed() {
        notification.eventCode = AdyenNotification.EventCode.CAPTURE
        notification.success   = Boolean.FALSE
        notification.process(orderPayment, TestUtils.createSystemEventInitiator())
        assert orderPayment.error
        assert orderPayment.error.action == AdyenNotification.EventCode.CAPTURE
    }

    @Test
    void testHasWaitedForThreshold() {
        notification.dateCreated = new DateTime().minusMinutes(AdyenService.MIN_NOTIFICATION_THRESHOLD - 1).toDate()
        assert !notification.hasWaitedForThreshold()

        notification.dateCreated = new DateTime().minusMinutes(AdyenService.MIN_NOTIFICATION_THRESHOLD + 1).toDate()
        assert notification.hasWaitedForThreshold()
    }

    @Test
    void testUpdateAdyenNotificationOnEventCode() {
        AdyenOrderPayment orderPayment = new AdyenOrderPayment(method: PaymentMethod.SWISH)
        notification.eventCode = AdyenNotification.EventCode.REFUND
        notification.updateAdyenOrderPaymentOnEventCode(orderPayment, TestUtils.createSystemEventInitiator())

        assert orderPayment.status == OrderPayment.Status.CREDITED

        notification.eventCode = AdyenNotification.EventCode.CANCELLATION
        notification.updateAdyenOrderPaymentOnEventCode(orderPayment, TestUtils.createSystemEventInitiator())
        assert orderPayment.status == OrderPayment.Status.ANNULLED

        def mockOrderPayment = mockFor(AdyenOrderPayment)
        mockOrderPayment.demand.getMethod(1) { -> PaymentMethod.SWISH }
        mockOrderPayment.demand.getStatus(1) { -> OrderPayment.Status.PENDING }

        mockOrderPayment.demand.handleNotificationForPending(1) { def n ->
            assert n == notification
        }

        orderPayment = mockOrderPayment.createMock()
        notification.eventCode = AdyenNotification.EventCode.AUTHORISATION
        notification.updateAdyenOrderPaymentOnEventCode(orderPayment, TestUtils.createSystemEventInitiator())
        mockOrderPayment.verify()

    }

    @Test
    void testUpdateFailedAdyenNotificationOnEventCodeNoArticle() {
        AdyenOrderPayment orderPayment = new AdyenOrderPayment(method: PaymentMethod.SWISH)
        notification.eventCode = AdyenNotification.EventCode.REFUND
        notification.updateAdyenOrderPaymentOnEventCode(orderPayment, TestUtils.createSystemEventInitiator())

        assert orderPayment.status == OrderPayment.Status.CREDITED

        notification.eventCode = AdyenNotification.EventCode.CANCELLATION
        notification.updateAdyenOrderPaymentOnEventCode(orderPayment, TestUtils.createSystemEventInitiator())
        assert orderPayment.status == OrderPayment.Status.ANNULLED

        def mockOrderPayment = mockFor(AdyenOrderPayment)
        mockOrderPayment.demand.getMethod(1) { -> PaymentMethod.CREDIT_CARD }
        mockOrderPayment.demand.getStatus(1) { -> OrderPayment.Status.FAILED }

        mockOrderPayment.demand.refundOrderIfNoArticleCreated(1) { EventInitiator eventInitiator ->
            return false
        }

        orderPayment = mockOrderPayment.createMock()
        notification.eventCode = AdyenNotification.EventCode.AUTHORISATION
        notification.updateAdyenOrderPaymentOnEventCode(orderPayment, TestUtils.createSystemEventInitiator())
        mockOrderPayment.verify()
    }

    @Test
    void testUpdateFailedAdyenNotificationOnEventCodeNoArticleSwish() {
        AdyenOrderPayment orderPayment = new AdyenOrderPayment(method: PaymentMethod.SWISH)
        notification.eventCode = AdyenNotification.EventCode.REFUND
        notification.updateAdyenOrderPaymentOnEventCode(orderPayment, TestUtils.createSystemEventInitiator())

        assert orderPayment.status == OrderPayment.Status.CREDITED

        notification.eventCode = AdyenNotification.EventCode.CANCELLATION
        notification.updateAdyenOrderPaymentOnEventCode(orderPayment, TestUtils.createSystemEventInitiator())
        assert orderPayment.status == OrderPayment.Status.ANNULLED

        def mockOrderPayment = mockFor(AdyenOrderPayment)
        mockOrderPayment.demand.getMethod(1) { -> PaymentMethod.SWISH }
        mockOrderPayment.demand.getStatus(2) { -> OrderPayment.Status.FAILED }

        mockOrderPayment.demand.refundOrderIfNoArticleCreated(1) { EventInitiator eventInitiator  ->
            return false
        }

        orderPayment = mockOrderPayment.createMock()
        notification.eventCode = AdyenNotification.EventCode.AUTHORISATION
        notification.updateAdyenOrderPaymentOnEventCode(orderPayment, TestUtils.createSystemEventInitiator())
        mockOrderPayment.verify()
    }

    @Test
    void testUpdateNewAdyenNotificationOnEventCodeNoArticle() {
        AdyenOrderPayment orderPayment = new AdyenOrderPayment(method: PaymentMethod.SWISH)
        notification.eventCode = AdyenNotification.EventCode.REFUND
        notification.updateAdyenOrderPaymentOnEventCode(orderPayment, TestUtils.createSystemEventInitiator())

        assert orderPayment.status == OrderPayment.Status.CREDITED

        notification.eventCode = AdyenNotification.EventCode.CANCELLATION
        notification.updateAdyenOrderPaymentOnEventCode(orderPayment, TestUtils.createSystemEventInitiator())
        assert orderPayment.status == OrderPayment.Status.ANNULLED

        def mockOrderPayment = mockFor(AdyenOrderPayment)
        mockOrderPayment.demand.getMethod(1) { -> PaymentMethod.CREDIT_CARD }
        mockOrderPayment.demand.getStatus(1) { -> OrderPayment.Status.NEW }

        mockOrderPayment.demand.refundOrderIfNoArticleCreated(1) { EventInitiator eventInitiator ->
            return false
        }

        orderPayment = mockOrderPayment.createMock()
        notification.eventCode = AdyenNotification.EventCode.AUTHORISATION
        notification.updateAdyenOrderPaymentOnEventCode(orderPayment, TestUtils.createSystemEventInitiator())
        mockOrderPayment.verify()
    }

    @Test
    void testUpdateNewAdyenNotificationOnEventCodeHasArticle() {
        AdyenOrderPayment orderPayment = new AdyenOrderPayment(method: PaymentMethod.SWISH)
        notification.eventCode = AdyenNotification.EventCode.REFUND
        notification.updateAdyenOrderPaymentOnEventCode(orderPayment, TestUtils.createSystemEventInitiator())

        assert orderPayment.status == OrderPayment.Status.CREDITED

        notification.eventCode = AdyenNotification.EventCode.CANCELLATION
        notification.updateAdyenOrderPaymentOnEventCode(orderPayment, TestUtils.createSystemEventInitiator())
        assert orderPayment.status == OrderPayment.Status.ANNULLED

        def mockOrderPayment = mockFor(AdyenOrderPayment)
        mockOrderPayment.demand.getMethod(1) { -> PaymentMethod.CREDIT_CARD }
        mockOrderPayment.demand.getStatus(1) { -> OrderPayment.Status.NEW }

        mockOrderPayment.demand.refundOrderIfNoArticleCreated(1) {  ->
            return true
        }

        def mockStaticOrder = mockFor(Order)
        mockStaticOrder.demand.static.withTransaction(1) { Closure closure ->
            closure.call()
        }

        def mockOrder = mockFor(Order)
        mockOrder.demand.asBoolean(1) { -> return true }
        mockOrder.demand.setStatus(1) { def status -> assert status == Order.Status.CONFIRMED }
        mockOrder.demand.getId(1) { -> return 1l }
        mockOrder.demand.save(1) { ->  }

        mockOrderPayment.demand.getOrders(1) { -> return [mockOrder.createMock()] }
        mockOrderPayment.demand.setStatus(1) { def status -> assert status == OrderPayment.Status.AUTHED }
        mockOrderPayment.demand.save(1) { -> }

        orderPayment = mockOrderPayment.createMock()
        notification.eventCode = AdyenNotification.EventCode.AUTHORISATION
        notification.updateAdyenOrderPaymentOnEventCode(orderPayment, TestUtils.createSystemEventInitiator())
        mockStaticOrder.verify()
        mockOrder.verify()
        mockOrderPayment.verify()
    }

    @Test
    void testUpdateFailedAdyenNotificationOnEventCodeHasArticle() {
        AdyenOrderPayment orderPayment = new AdyenOrderPayment(method: PaymentMethod.SWISH)
        notification.eventCode = AdyenNotification.EventCode.REFUND
        notification.updateAdyenOrderPaymentOnEventCode(orderPayment, TestUtils.createSystemEventInitiator())

        assert orderPayment.status == OrderPayment.Status.CREDITED

        notification.eventCode = AdyenNotification.EventCode.CANCELLATION
        notification.updateAdyenOrderPaymentOnEventCode(orderPayment, TestUtils.createSystemEventInitiator())
        assert orderPayment.status == OrderPayment.Status.ANNULLED

        def mockOrderPayment = mockFor(AdyenOrderPayment)
        mockOrderPayment.demand.getMethod(1) { -> PaymentMethod.CREDIT_CARD }
        mockOrderPayment.demand.getStatus(1) { -> OrderPayment.Status.FAILED }

        mockOrderPayment.demand.refundOrderIfNoArticleCreated(1) {  ->
            return true
        }

        def mockStaticOrder = mockFor(Order)
        mockStaticOrder.demand.static.withTransaction(1) { Closure closure ->
            closure.call()
        }

        def mockOrder = mockFor(Order)
        mockOrder.demand.asBoolean(1) { -> return true }
        mockOrder.demand.setStatus(1) { def status -> assert status == Order.Status.CONFIRMED }
        mockOrder.demand.getId(1) { -> return 1l }
        mockOrder.demand.save(1) { ->  }

        mockOrderPayment.demand.getOrders(1) { -> return [mockOrder.createMock()] }
        mockOrderPayment.demand.setStatus(1) { def status -> assert status == OrderPayment.Status.AUTHED }
        mockOrderPayment.demand.save(1) { -> }

        orderPayment = mockOrderPayment.createMock()
        notification.eventCode = AdyenNotification.EventCode.AUTHORISATION
        notification.updateAdyenOrderPaymentOnEventCode(orderPayment, TestUtils.createSystemEventInitiator())
        mockStaticOrder.verify()
        mockOrder.verify()
        mockOrderPayment.verify()
    }

    private static AdyenNotification createNotificationRequest() {
        Map map = [:]
        map.pspReference   = GLOBAL_ID
        map.success        = "true"
        map.reason         = "Not working"
        map.eventCode      = AdyenNotification.EventCode.CAPTURE

        return new AdyenNotification().create(map)
    }

    private AdyenOrderPayment createAdyenOrderPayment() {
        AdyenOrderPayment orderPayment = new AdyenOrderPayment()
        orderPayment.id            = GLOBAL_ID
        orderPayment.transactionId = GLOBAL_ID
        orderPayment.amount        = FULL_ORDER_SUM
        orderPayment.credited      = 0l
        orderPayment.issuer        = user
        orderPayment.method        = PaymentMethod.CREDIT_CARD

        orderPayment.save(validate: false)
    }

    private static AdyenOrderPaymentError createAdyenOrderPaymentError() {
        AdyenOrderPaymentError error = new AdyenOrderPaymentError()
        error.id            = GLOBAL_ID
        error.action        = AdyenNotification.EventCode.CAPTURE.toString()
        error.reason        = "Failed"

        error.save(validate: false)
    }

    @Test
    void testCreateChargeback() {
        Map<String, Object> params = [:]
        params.put("eventCode", "CHARGEBACK")
        params.put("pspReference", "123")
        params.put("success", "true")
        params.put("reason", "TEST")
        params.put("additionalData", ["data": "999"])

        AdyenNotification notification = AdyenNotification.create(params)

        assert notification.eventCode.equals(AdyenNotification.EventCode.CHARGEBACK)
        assert notification.pspReference == "123"
        assert notification.success == Boolean.TRUE
        assert notification.reason != ""
        assert !notification.hasProperty("additionalData")
    }

    @Test
    void testCreateChargebackReversed() {
        Map<String, Object> params = [:]
        params.put("eventCode", "CHARGEBACK_REVERSED")
        params.put("pspReference", "123")
        params.put("success", "true")
        params.put("reason", "TEST")
        params.put("additionalData", ["data": "999"])

        AdyenNotification notification = AdyenNotification.create(params)

        assert notification.eventCode.equals(AdyenNotification.EventCode.CHARGEBACK_REVERSED)
        assert notification.pspReference == "123"
        assert notification.success == Boolean.TRUE
        assert notification.reason != ""
        assert !notification.hasProperty("additionalData")
    }

    @Test
    void testCreateNotificationOfChargeback() {
        Map<String, Object> params = [:]
        params.put("eventCode", "NOTIFICATION_OF_CHARGEBACK")
        params.put("pspReference", "123")
        params.put("success", "true")
        params.put("reason", "TEST")
        params.put("additionalData", ["data": "999"])

        AdyenNotification notification = AdyenNotification.create(params)

        assert notification.eventCode.equals(AdyenNotification.EventCode.NOTIFICATION_OF_CHARGEBACK)
        assert notification.pspReference == "123"
        assert notification.success == Boolean.TRUE
        assert notification.reason != ""
        assert !notification.hasProperty("additionalData")
    }

    @Test
    void testCreateNotificationOfSecondChargeback() {
        Map<String, Object> params = [:]
        params.put("eventCode", "SECOND_CHARGEBACK")
        params.put("pspReference", "123")
        params.put("success", "true")
        params.put("reason", "TEST")
        params.put("additionalData", ["data": "999"])

        AdyenNotification notification = AdyenNotification.create(params)

        assert notification.eventCode.equals(AdyenNotification.EventCode.SECOND_CHARGEBACK)
        assert notification.pspReference == "123"
        assert notification.success == Boolean.TRUE
        assert notification.reason != ""
        assert !notification.hasProperty("additionalData")
    }
    @Test
    void testCreateNotificationOfPrearbitrationLost() {
        Map<String, Object> params = [:]
        params.put("eventCode", "PREARBITRATION_LOST")
        params.put("pspReference", "123")
        params.put("success", "true")
        params.put("reason", "TEST")
        params.put("additionalData", ["data": "999"])

        AdyenNotification notification = AdyenNotification.create(params)

        assert notification.eventCode.equals(AdyenNotification.EventCode.PREARBITRATION_LOST)
        assert notification.pspReference == "123"
        assert notification.success == Boolean.TRUE
        assert notification.reason != ""
        assert !notification.hasProperty("additionalData")
    }

    @Test
    void testCreateNotificationOfPrearbitrationWon() {
        Map<String, Object> params = [:]
        params.put("eventCode", "PREARBITRATION_WON")
        params.put("pspReference", "123")
        params.put("success", "true")
        params.put("reason", "TEST")
        params.put("additionalData", ["data": "999"])

        AdyenNotification notification = AdyenNotification.create(params)

        assert notification.eventCode.equals(AdyenNotification.EventCode.PREARBITRATION_WON)
        assert notification.pspReference == "123"
        assert notification.success == Boolean.TRUE
        assert notification.reason != ""
        assert !notification.hasProperty("additionalData")
    }
}
