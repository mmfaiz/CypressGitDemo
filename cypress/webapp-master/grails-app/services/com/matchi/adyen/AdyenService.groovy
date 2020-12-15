package com.matchi.adyen

import com.matchi.OrderStatusService
import com.matchi.PaymentInfo
import com.matchi.User
import com.matchi.UserService
import com.matchi.events.EventInitiator
import com.matchi.orders.AdyenOrderPayment
import com.matchi.orders.Order
import com.matchi.orders.OrderPayment
import groovyx.net.http.URIBuilder
import org.hibernate.criterion.CriteriaSpecification
import org.joda.time.LocalDateTime

class AdyenService {

    def grailsApplication
    UserService userService
    def adyenHttpService
    def grailsLinkGenerator
    def messageSource
    def dateUtil
    OrderStatusService orderStatusService

    private static final int MIN_RETRY_INTERVAL = 1
    private static final int MAX_RETRY_INTERVAL = 7

    public static final int MAX_WAIT_CAPTURE_DAYS = 7
    private static final int MAX_WAIT_CAPTURE_DELIVERY_HOURS = 12

    public static final DATE_OF_PAYMENT_LOGIC_CHANGE = '2018-03-28'

    public static final int MIN_NOTIFICATION_THRESHOLD = 5

    private static final int SESSION_VALIDITY_MINUTES = 10

    static enum AdyenRecurringOperation {
        listRecurringDetails, DISABLE
    }

    static enum AdyenProcessOperation {
        AUTHORISE, AUTHORISE3D, CAPTURE, REFUND, CANCEL
    }

    static enum AdyenAuthCodes {
        PENDING, AUTHORISED, CANCELLED, REFUSED
    }

    def authorise(Map map, Closure callback = null) throws AdyenException {
        String url = adyenHttpService.getAuthUrl()
        adyenHttpService.request(url, map, callback)
    }

    def authorise3DS(Map map, Closure callback = null) throws AdyenException {
        String url = adyenHttpService.getAuth3DSUrl()
        adyenHttpService.request(url, map, callback)
    }

    def capture(Map map, Closure callback = null) throws AdyenException {
        String url = adyenHttpService.getCaptureUrl()
        adyenHttpService.request(url, map, callback)
    }

    def refund(Map map, Closure callback = null) throws AdyenException {
        String url = adyenHttpService.getRefundUrl()
        adyenHttpService.request(url, map, { result ->
            log.debug(result)
        })
    }

    def cancel(Map map, Closure callback = null) throws AdyenException {
        String url = adyenHttpService.getCancelUrl()
        adyenHttpService.request(url, map, { result ->
            log.debug(result)
        })
    }

    def directory(String merchantReference, BigDecimal totalPrice, String countryCode, String currencyCode, Closure callback = null) throws AdyenException {
        def url = new URIBuilder(grailsApplication.config.adyen.detailsUrl)

        SortedMap<String, String> data = new TreeMap<>()
        data.put("countryCode", countryCode)
        data.put("currencyCode", currencyCode)
        data.put("merchantAccount", "${grailsApplication.config.adyen.merchant}")
        data.put("paymentAmount", String.valueOf(amountToAdyenFormat(totalPrice, currencyCode)))
        data.put("skinCode", "${grailsApplication.config.adyen.skin}")
        data.put("merchantReference", merchantReference)
        data.put("sessionValidity", "${new LocalDateTime().plusMinutes(SESSION_VALIDITY_MINUTES)}")

        def merchantSig = adyenHttpService.generateMerchantSignature(data)
        data.put("merchantSig", merchantSig)

        adyenHttpService.request(url?.addQueryParams(data)?.toString(), null, callback)
    }

    def skipDetails(Map map) {
        Order order = Order.get(map.orderId)
        def resURL  = grailsLinkGenerator.link(controller: "adyenPayment", action: "validate", absolute: 'true',
                params: [orderId: order.id, paymentId: map.paymentId])

        def orderId      = order?.id?.toString()
        def locale       = order?.issuer?.language?.toUpperCase()
        def currencyCode = order?.facility?.currency
        def countryCode  = order?.facility?.country
        def shipBeforeDate = order?.dateDelivery?.format(dateUtil.ISO8601_DATE_AND_TIME_FORMAT, TimeZone.getTimeZone('UTC'))
        def deliveryDateRef = order?.dateDelivery?.format(dateUtil.DEFAULT_DATE_SHORT_FORMAT, TimeZone.getTimeZone('UTC'))


        SortedMap<String, String> data = new TreeMap<>()
        data.put("currencyCode", currencyCode)
        data.put("countryCode", countryCode)
        data.put("shopperLocale", locale)
        data.put("merchantAccount", "${grailsApplication.config.adyen.merchant}")
        data.put("paymentAmount", String.valueOf(amountToAdyenFormat((BigDecimal) order?.total(), currencyCode)))
        data.put("skinCode", "${grailsApplication.config.adyen.skin}")
        data.put("merchantReference", orderId + "-" + deliveryDateRef)
        data.put("brandCode", map?.method?.toString()?.toLowerCase())
        data.put("issuerId", map?.issuerId?.toString() ?: "")
        data.put("sessionValidity", "${new LocalDateTime().plusMinutes(SESSION_VALIDITY_MINUTES)}")
        data.put("resURL", resURL)
        data.put("shipBeforeDate", shipBeforeDate)

        def merchantSig = adyenHttpService.generateMerchantSignature(data)
        data.put("merchantSig", merchantSig)

        return data
    }

    def getUserRecurring(User user, Closure callback = null) throws AdyenException {
        String url = adyenHttpService.getRecurringDetailsUrl()

        def merchant = grailsApplication.config.adyen.merchant
        def shopperId = user.id

        Map map = [:]
        map.put("merchantAccount", merchant)
        map.put("shopperReference", shopperId)

        adyenHttpService.request(url, map, { result ->
            callback(parseRecurringResult(result))
        })
    }

    def disableUserRecurring(User user) throws AdyenException {
        String url = adyenHttpService.getDisableRecurringUrl()

        def merchant = grailsApplication.config.adyen.merchant
        def shopperId = user.id

        Map map = [:]
        map.put("merchantAccount", merchant)
        map.put("shopperReference", shopperId)

        adyenHttpService.request(url, map)
    }

    def validateLocalPaymentResponse(Map response) throws AdyenException{
        log.debug("Validate local response ${response.authResult}")

        response.remove("action")
        response.remove("controller")
        response.remove("orderId")
        response.remove("paymentId")

        def responseMerchantSig = response.remove("merchantSig")
        log.debug(responseMerchantSig)

        SortedMap<String, String> data = new TreeMap<>()
        response.each { k, v ->
            data.put(k, v)
        }

        log.debug(data)

        def calculatedMerchantSig = adyenHttpService.generateMerchantSignature(data)

        if (!responseMerchantSig.equals(calculatedMerchantSig)) {
            throw new AdyenException()
        }
    }

    // TODO: Test this
    def updatePaymentOnLocalPaymentReponse(Map response, AdyenOrderPayment orderPayment) throws AdyenException {
        switch (response?.authResult) {
            case AdyenAuthCodes.AUTHORISED.toString():
                log.debug("Received AUTHORISED response")
                orderPayment.setCompleted(response, true)
                orderPayment.orders.each {orderStatusService.complete(it, userService.getCurrentUser())}
                break
            case AdyenAuthCodes.PENDING.toString():
                log.debug("Received PENDING response")
                orderPayment.setPending(response)
                break
            case AdyenAuthCodes.CANCELLED.toString():
                log.debug("Received CANCELLED response")
                handleLocalPaymentException(AdyenAuthCodes.CANCELLED, response, new Locale(orderPayment?.issuer?.language))
                break
            case AdyenAuthCodes.REFUSED.toString():
                log.debug("Received REFUSED response")
                handleLocalPaymentException(AdyenAuthCodes.REFUSED, response, new Locale(orderPayment?.issuer?.language))
                break
            default:
                log.debug("Received ${response?.authResult} response")
                throw new AdyenException()
                break
        }
    }

    def getAcceptedCardProviders() {
        return grailsApplication.config.adyen.cardProviders
    }

    def getAcceptedLocalPaymentMethods() {
        return grailsApplication.config.adyen.localMethods
    }

    def getPaymentsToBeCaptured() {
        return AdyenOrderPayment.createCriteria().listDistinct {
            createAlias("orders", "o", CriteriaSpecification.LEFT_JOIN)
            eq("status", OrderPayment.Status.AUTHED)
            isNotNull("transactionId")

            or {
                le("o.dateDelivery", new LocalDateTime().minusHours(MAX_WAIT_CAPTURE_DELIVERY_HOURS).toDate())
                le("o.dateCreated", new LocalDateTime().minusDays(MAX_WAIT_CAPTURE_DAYS).toDate())
            }
        }
    }

    def getPaymentsToRetry() {
        return AdyenOrderPayment.createCriteria().listDistinct {
            createAlias("error", "e", CriteriaSpecification.LEFT_JOIN)
            eq("status", OrderPayment.Status.FAILED)
            isNotNull("e.id")

            or {
                le("e.lastUpdated", new LocalDateTime().minusDays(MIN_RETRY_INTERVAL).toDate())
                le("e.dateCreated", new LocalDateTime().minusDays(MAX_RETRY_INTERVAL).toDate())
            }
        }
    }

    def processNotificationsAsJob(EventInitiator eventInitiator) {
        List<AdyenNotification> notifications = getNotificationsToProcess()
        log.info("Found ${notifications.size()} to process")
        processNotificationsList(notifications, eventInitiator)
    }

    def processNotificationsList(List<AdyenNotification> notifications, EventInitiator eventInitiator) {
        // This to make sure we do not process order payment with same pspReference and eventCode more than once
        Map<String, AdyenNotification.EventCode> pspReferencesRun = [:]

        notifications.each { AdyenNotification n ->
            if (!pspReferencesRun.get(n.pspReference)) {
                AdyenOrderPayment orderPayment = AdyenOrderPayment.findByTransactionId(n.pspReference)
                n.process(orderPayment, eventInitiator)
            }

            // Add to checkup map if it same pspReference doesn't exist since before
            if (!pspReferencesRun.get(n.pspReference)) {
                pspReferencesRun.put(n.pspReference, n.eventCode)
            }


            // Do not set to executed if same pspReference and different eventCode is present
            if (pspReferencesRun.get(n.pspReference) == n.eventCode) {
                n.executed = Boolean.TRUE
                n.save(flush: true)
            }
        }
    }

    /**
     * Process AdyenNotification if exists. Times out if still PENDING after set timeout interval
     * @param orderPayment
     * @param now
     */
    boolean processOrTimeOutPending(AdyenOrderPayment orderPayment, EventInitiator eventInitiator, Date now = new Date()) {
        if(!orderPayment.isPendingWaiting()) {
            throw new IllegalArgumentException("Cannot process orderPayments not being PENDING + waiting")
        }

        boolean notificationProcessed = checkAndProcessNotificationsWithoutTimeLimit(orderPayment, eventInitiator)
        Long timeout = grailsApplication.config.matchi.pending.timeout

        // If no notification processed, payment is still PENDING but time is up, we set order(s) to CANCELLED
        if(!notificationProcessed && orderPayment.isPendingWaiting() && orderPayment.hasReachedTimeOut(timeout, now)) {
            orderPayment.orders.each {orderStatusService.cancel(it, eventInitiator)}
        }
    }

    /**
     * Checks if exists unprocessed AdyenNotifications for OrderPayment.
     * @returns if any notification was processed
     * @param orderPayment
     */
    boolean checkAndProcessNotificationsWithoutTimeLimit(AdyenOrderPayment orderPayment, EventInitiator eventInitiator) {
        List<AdyenNotification> adyenNotificationList = AdyenNotification.createCriteria().listDistinct {
            eq("pspReference", orderPayment.transactionId)
            eq("executed", Boolean.FALSE)
            order("id", "asc")
        }

        if(adyenNotificationList) {
            processNotificationsList(adyenNotificationList, eventInitiator)
            return true
        }

        return false
    }

    def getNotificationsToProcess() {
        return AdyenNotification.createCriteria().listDistinct {
            le("dateCreated", new LocalDateTime().minusMinutes(MIN_NOTIFICATION_THRESHOLD).toDate())
            eq("executed", Boolean.FALSE)
            order("id", "asc")
            maxResults(1000)
        }
    }

    def savePaymentInfo(User user, Map data = null) {
        log.debug("Save payment info with: ${data}")
        deletePaymentInfo(user)
        PaymentInfo paymentInfo = new PaymentInfo()
        paymentInfo.user = user
        paymentInfo.provider = PaymentInfo.PaymentProvider.ADYEN

        String dataExpiryDate = data?.get("expiryDate")

        // Add leading zero to month if it doesn't exist
        if (dataExpiryDate.size() == 6) {
            dataExpiryDate = "0" + dataExpiryDate
        }

        String expiryMonth = dataExpiryDate?.substring(0, 2)
        String expiryYear  = dataExpiryDate?.substring(3, 7)

        paymentInfo.expiryDate  = expiryMonth + expiryYear?.substring(2,4)
        paymentInfo.expiryMonth = expiryMonth
        paymentInfo.expiryYear  = expiryYear
        paymentInfo.number      = data?.get("cardSummary")
        paymentInfo.issuer      = data?.get("paymentMethod")
        paymentInfo.holderName  = data?.get("cardHolderName")

        paymentInfo.save(flush: true)
    }

    def deletePaymentInfo(User user) {
        def paymentInfo = PaymentInfo.findByUserAndProvider(user, PaymentInfo.PaymentProvider.ADYEN)
        if(paymentInfo) {
            paymentInfo.delete(flush: true)

            disableUserRecurring(user)
        }
    }

    def parseRecurringResult(def result) {
        def cardDetailsList = []

        result?.details?.each {
            cardDetailsList << it.RecurringDetail
        }

        return cardDetailsList
    }

    def handleLocalPaymentException(AdyenAuthCodes code, Map response, Locale locale) throws AdyenException {
        throw new AdyenException([message: messageSource.getMessage("adyen.error.message.${code}", null,
                locale),pspReference: response.pspReference])
    }

    def amountToAdyenFormat(BigDecimal amount, String currencyCode) {
        int decimalPoints = grailsApplication.config.matchi.settings.currency[currencyCode].decimalPoints
        return (int)(Math.round(amount*100.0)/100.0) * Math.pow(10, decimalPoints)
    }
}
