package com.matchi.orders

import com.matchi.IArticleItem
import com.matchi.LogHelper
import com.matchi.OrderStatusService
import com.matchi.User
import com.matchi.adyen.AdyenException
import com.matchi.adyen.AdyenNotification
import com.matchi.adyen.AdyenService
import com.matchi.adyen.authorization.AdyenAuthorizationBuilder
import com.matchi.events.AdyenEventInitiator
import com.matchi.events.EventInitiator
import com.matchi.payment.PaymentException
import com.matchi.payment.PaymentMethod
import grails.util.Holders

class AdyenOrderPayment extends OrderPayment {
    private static final long serialVersionUID = 12L

    private static final transient List<String> refusalReasons = ["Refused", "Cancelled", "Error"]
    private static final transient String redirect3DS = "RedirectShopper"

    public static final int CAPTURE_DELAY = 12
    public static final String PROCESS_ACTION = "process"

    public static final String LATE_AUTH_REFUND_NOTE = "Failed PENDING payment refunded on late AUTHORISATION"
    public static final String INTERRUPTED_PENDING_REFUND_NOTE

    AdyenOrderPaymentError error
    String transactionId //pspReference

    static constraints = {
        transactionId nullable: true
        error         nullable: true
    }

    static mapping = {
        discriminator "adyen"
    }

    @Override
    def getType() {
        return "Adyen"
    }

    @Override
    boolean allowLateRefund() {
        false
    }

    @Override
    boolean isRefundableTypeAndStatus() {
        this.isRefundable()
    }

    void refund(amount, Closure callback = null) {
        orders.each { log.info(LogHelper.formatOrder("Refund", it)) }

        this.setCredited(amount)

        // Do refund if payment has been captured
        if (this.status == OrderPayment.Status.CAPTURED || this.status == OrderPayment.Status.PENDING) {
            getAdyenService().refund(createModificationRequest(Boolean.FALSE), callback)
            setCredited()

            // Cancel payment if no amount remains and payment hasn't been captured
        } else if (total() == 0) {
            getAdyenService().cancel(createCancelRequest(), callback)
            setCancelled()
        }

        this.save(flush: true)
    }

    def authorise(Map map, Order order, AdyenAuthorizationBuilder authorizationBuilder, Boolean ignoreAssert = false, Closure callback = null) {
        log.info(LogHelper.formatOrder("Authorise", order))

        User paymentIssuer = this.issuer
        orders.each {getOrderStatusService().confirm(it, paymentIssuer, ignoreAssert)}
        getAdyenService().authorise(createAuthorizeRequest(map, order, authorizationBuilder),
                { json ->
                    verifyResultCode(json)

                    log.debug(LogHelper.formatOrder("Authorise result: " + json, order))
                    if (json.resultCode && json.resultCode == redirect3DS) {
                        log.debug(LogHelper.formatOrder("3DS enrolled, do checkup", order))
                        callback(json)
                    } else {
                        log.debug(LogHelper.formatOrder("NOT 3DS enrolled, no checkup needed", order))
                        setAuthed(json)

                        if (authorizationBuilder.storePaymentDetails() && json.additionalData) {
                            // Save PAYMENT issuer (end user) since order.issuer not always the one who pays.
                            // The order can be created by admin for example memberships and then paid by end user.
                            getAdyenService().savePaymentInfo(paymentIssuer, json.additionalData as Map)
                        }
                    }
                })
    }

    private AdyenService getAdyenService() {
        return Holders.grailsApplication.mainContext.getBean('adyenService')
    }

    private OrderStatusService getOrderStatusService() {
        return Holders.grailsApplication.mainContext.getBean('orderStatusService')
    }

    def authorise3DS(Map map, Order order, Boolean savePaymentInfo = false) {
        def merchant    = Holders.grailsApplication.config.adyen.merchant
        def request     = [:]

        request.put("browserInfo", [userAgent: map.get("userAgent"), "acceptHeader": "application/json"])
        request.put("merchantAccount", merchant)
        request.put("md", map.get("MD"))
        request.put("paResponse", map.get("PaRes"))

        User paymentIssuer = this.issuer

        getAdyenService().authorise3DS(request,
                { json ->
                    log.debug(LogHelper.formatOrder("Authorise 3DS result:" + json, order))
                    verifyResultCode(json)
                    setAuthed(json)

                    if (savePaymentInfo && json.additionalData) {
                        // Save PAYMENT issuer (end user) since order.issuer not always the one who pays.
                        // The order can be created by admin for example memberships and then paid by end user.
                        getAdyenService().savePaymentInfo(paymentIssuer, json.additionalData as Map)
                    }
                })
    }

    def capture(Closure callback = null) {
        log.info("Processing capture on transactionId ${transactionId}")
        getAdyenService().capture(createModificationRequest(),
                { json ->
                    log.debug(json)
                    verifyResultCode(json)

                    if(this.credited > 0) {
                        setCredited()
                    } else {
                        setCompleted(json)
                    }

                    if (callback) {
                        callback(json)
                    }
                })

        if (orders?.first()?.status?.equals(Order.Status.CONFIRMED)) {
            orders.each {getOrderStatusService().complete(it, new AdyenEventInitiator())}
        }
    }

    def cancel(Closure callback = null) {
        getAdyenService().cancel(createCancelRequest(), callback)
    }

    def createAuthorizeRequest(Map map, Order order, AdyenAuthorizationBuilder authorizationBuilder) {
        def dateUtil = Holders.grailsApplication.mainContext.getBean('dateUtil')
        def amount      = this.amount
        def orderNumber = order.id
        def currency    = getOrderCurrency(order)
        def merchant    = Holders.grailsApplication.config.adyen.merchant

        def shopperStatement = "MATCHi - ${order.id}".toString()

        def deliveryDate = order.dateDelivery.format(dateUtil.ISO8601_DATE_AND_TIME_FORMAT, TimeZone.getTimeZone('UTC'))
        def deliveryDateRef = order.dateDelivery.format(dateUtil.DEFAULT_DATE_SHORT_FORMAT, TimeZone.getTimeZone('UTC'))

        def request = [:]

        request.put("amount", ["value": getAdyenService().amountToAdyenFormat((BigDecimal) amount, currency), "currency": currency ])
        request.put("reference", orderNumber + "-" + deliveryDateRef)
        request.put("merchantAccount", merchant)
        request.put("browserInfo", ["userAgent": map.get("userAgent"), "acceptHeader": "application/json"])
        request.put("shopperStatement", shopperStatement)
        request.put("deliveryDate", deliveryDate)

        authorizationBuilder.setRequestParameters(request, map, this.issuer)

        log.debug "Authorization request:"
        log.debug request.dump()

        request
    }

    def createModificationRequest(Boolean capture = Boolean.TRUE) {
        def merchant    = Holders.grailsApplication.config.adyen.merchant
        def currency    = getOrderCurrency(orders?.first())

        def modificationAmount = capture ? getAdyenService().amountToAdyenFormat((BigDecimal) total(), currency) :
                getAdyenService().amountToAdyenFormat((BigDecimal) credited, currency)

        def request = [:]
        request.put("merchantAccount", merchant)
        request.put("originalReference", transactionId)

        request.put("modificationAmount",
                ["value": modificationAmount, "currency": currency ])

        request
    }

    def createCancelRequest() {
        String merchant = Holders.grailsApplication.config.adyen.merchant

        def request = [:]
        request.put("merchantAccount", merchant)
        request.put("originalReference", transactionId)

        request
    }

    /**
     * Returns what PaymentController action to redirect to depending on state of payment and order.
     * To be used after coming back from Adyen. Assumes we handle non-processable redirections using the name of the payment status.
     * Hopefully works as an extra layer of security to avoid processing failed payments etc.
     * @return
     */
    String getReturnAction() {
        return this.status.isProcessable ? PROCESS_ACTION : this.status.name().toLowerCase()
    }


    def setCredited() {
        this.status = OrderPayment.Status.CREDITED
        this.save(flush: true)
    }

    def setCancelled() {
        this.status = OrderPayment.Status.ANNULLED
        this.save(flush: true)
    }

    def setPending(Map data) {
        this.transactionId = data.pspReference
        this.status = OrderPayment.Status.PENDING
        this.save(flush: true)
    }

    def setAuthed(def data) {
        this.transactionId = data.pspReference
        this.status = OrderPayment.Status.AUTHED
        this.save(flush: true)
    }

    def setCompleted(def data, def updatePsP = false) {
        if (updatePsP && data.pspReference) {
            this.transactionId = data.pspReference
        }

        this.status = OrderPayment.Status.CAPTURED

        this.save(flush: true)
    }

    def addError(AdyenNotification notification, EventInitiator eventInitiator) {
        AdyenOrderPaymentError error = new AdyenOrderPaymentError()
        error.action = notification.eventCode
        error.reason = notification.reason
        error.save()

        this.error  = error
        this.status = OrderPayment.Status.FAILED
        this.orders.each {getOrderStatusService().cancel(it, eventInitiator)}

        this.save(flush: true)
    }

    def verifyResultCode(def json) {
        if (refusalReasons.contains(json.resultCode)) {
            log.debug("Refused, cancelled or error")
            log.debug(json.resultCode)
            throw new AdyenException(json)
        }

        return true
    }

    def refundOrderIfNoArticleCreated(EventInitiator eventInitiator) {
        Order order = orders?.first()
        log.info(LogHelper.formatOrder("Verify article created", order))

        IArticleItem articleItem = order.retrieveArticleItem()
        // Refund if no article was completed
        if(!articleItem) {
            log.info(LogHelper.formatOrder("No article item created for order", order))
            Order.withTransaction {
                getOrderStatusService().annul(order, eventInitiator, "No article item created for ${order.article.toString()}, refunding ${order.id}", order.total())
            }
            return false
        }

        return true
    }

    def retry() {
        try {
            switch (this.error?.action) {
                case AdyenNotification.EventCode.CAPTURE_FAILED.toString():
                    this.capture()
                    break
                case AdyenNotification.EventCode.CAPTURE.toString():
                    this.capture()
                    break
                case AdyenNotification.EventCode.REFUND_FAILED.toString():
                    this.refund(this.credited)
                    break
                case AdyenNotification.EventCode.CANCEL_OR_REFUND.toString():
                    this.refund(this.credited)
                    break
                case AdyenNotification.EventCode.REFUND.toString():
                    this.refund(this.credited)
                    break
                case AdyenNotification.EventCode.CANCELLATION.toString():
                    this.refund(this.credited)
                    break
                default:
                    log.debug("No match")
                    break
            }
        } catch (AdyenException e) {
            this.status        = OrderPayment.Status.FAILED
            this.errorMessage  = e.refusalReason ?: e.message
            this.transactionId = e.pspReference
            this.save()

            log.error(this.errorMessage)
        }
    }

    /**
     * Returns predicted date of CAPTURE.
     * If delivery date more than a week later, its a week after creation date.
     * If less than a week, it is when delivered.
     * @return
     */
    Date getPredictedDateOfCapture() {
        if(!orders) return null

        Date weekFromCreation = this.dateCreated.plus(AdyenService.MAX_WAIT_CAPTURE_DAYS)
        Order order = orders.first()

        Date returnValue

        if(this.status == OrderPayment.Status.CAPTURED) {
            returnValue = this.lastUpdated
        } else if(order.dateDelivery.after(weekFromCreation)) {
            returnValue = weekFromCreation
        } else {
            returnValue = order.dateDelivery
        }

        return new org.joda.time.DateTime(returnValue).plusHours(CAPTURE_DELAY).toDate()
    }

    static AdyenOrderPayment create(Order order, PaymentMethod method, User issuer = null) {
        if (order.payments.find {
            payment ->
                OrderPayment orderPayment = (payment as OrderPayment)
                orderPayment.getType() == "Adyen" && ![OrderPayment.Status.FAILED, OrderPayment.Status.ANNULLED].contains(orderPayment.status)
        })
            throw new PaymentException("Could not create AdyenOrderPayment. Order with id:$order.id has already been paid")

        AdyenOrderPayment adyenPayment = new AdyenOrderPayment()
        adyenPayment.issuer = issuer ?: order.issuer
        adyenPayment.amount = order.total()
        adyenPayment.vat    = order.vat()
        adyenPayment.method = method

        order.addToPayments(adyenPayment.save(failOnError: true))
        order.save(failOnError: true)

        adyenPayment.addToOrders(order)
        adyenPayment.save(flush: true)

        adyenPayment
    }

    /**
     * Checks if payment was timed out during pending checking, due to no answer received from Adyen.
     * @return
     */
    boolean isPendingTimeout() {
        return status?.equals(OrderPayment.Status.PENDING) && ordersHaveStatus(Order.Status.CANCELLED)
    }

    /**
     * Checks if payment is still in waiting mode.
     * This is true when waiting for answer directly after doing the payment at Adyen, but can also be true
     * if pending page was interrupted for some reason.
     * @return
     */
    boolean isPendingWaiting() {
        return status?.equals(OrderPayment.Status.PENDING) && ordersHaveStatus(Order.Status.CONFIRMED)
    }

    /**
     * Checks if a pending is still in waiting mode, but the notification is processed by the AdyenNotificationJob.
     * That means that the pending page did not finish its job to time out the payment (due to lost connection or similar).
     * We decide if it is interrupted by looking at how old the notification is.
     * @param adyenNotification
     * @return
     */
    boolean isPendingInterrupted(AdyenNotification adyenNotification) {
        return isPendingWaiting() && adyenNotification.hasWaitedForThreshold()
    }

    /**
     * Checks if payment has timed out and we need to cancel the order.
     * @param timeoutMilliSeconds
     * @param now
     * @return
     */
    boolean hasReachedTimeOut(Long timeoutMilliSeconds, Date now) {
        return lastUpdated.getTime() <= (now.getTime() - timeoutMilliSeconds)
    }

    /**
     * Used if local payment method. Refunds if payment is already failed or pending has timed out
     */
    void handleNotificationForPending(AdyenNotification notification, EventInitiator eventInitiator) {
        // Illegal operation if not pending
        if(!status?.equals(OrderPayment.Status.PENDING)) {
            throw new IllegalAccessException("Cannot run handleNotificationForPending if not PENDING")
        }

        /*
         * If a pending payment has timed out, and an authorisation event comes in afterwards.
         * Since payment status is PENDING, the resulting status will be CREDITED
         */
        if(isPendingTimeout()) {
            orders*.refund(LATE_AUTH_REFUND_NOTE)

        /*
         * If the pending page was interrupted and the payment is stuck in waiting mode, we need to cancel the order ourselves.
         * Since payment status is PENDING, the resulting status will be CREDITED
         */
        } else if(isPendingInterrupted(notification)) {
            orders*.refund(INTERRUPTED_PENDING_REFUND_NOTE)
            orders.each {getOrderStatusService().cancel(it, eventInitiator)}

        // Otherwise, we capture.
        } else {
            status = OrderPayment.Status.CAPTURED
            orders.each {getOrderStatusService().complete(it, eventInitiator)}
        }
    }

    String getOrderCurrency(Order order){
        order.article.equals(Order.Article.PAYMENT_UPDATE) ? "SEK" : order.facility?.currency
    }
}
