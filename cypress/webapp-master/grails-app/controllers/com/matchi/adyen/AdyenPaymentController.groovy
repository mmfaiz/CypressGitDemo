package com.matchi.adyen

import com.matchi.Facility
import com.matchi.LogHelper
import com.matchi.OrderStatusService
import com.matchi.User
import com.matchi.UserService
import com.matchi.adyen.authorization.AdyenAuthorizationBuilder
import com.matchi.adyen.authorization.AdyenAuthorizationBuilderFactory
import com.matchi.coupon.CustomerCoupon
import com.matchi.coupon.PromoCode
import com.matchi.orders.AdyenOrderPayment
import com.matchi.orders.Order
import com.matchi.orders.OrderPayment
import com.matchi.payment.PaymentFlow
import com.matchi.payment.PaymentMethod
import grails.converters.JSON
import org.joda.time.DateTime
import org.springframework.http.HttpStatus

import javax.servlet.http.HttpServletResponse

class AdyenPaymentController {

    def adyenService
    def grailsApplication
    def grailsLinkGenerator
    def messageSource
    def springSecurityService
    def couponService
    def customerService
    def slotService
    OrderStatusService orderStatusService
    UserService userService

    /*
    * Entry point for all Adyen payments, showing credit card form is method is selected
    * */

    def index() {
        def method = params.method as PaymentMethod

        if (!method.equals(PaymentMethod.CREDIT_CARD)) {
            redirect(action: "pay", params: [orderId : params.orderId, method: method,
                                             issuerId: params.issuerId, promoCodeId: params.promoCodeId])
            return
        }

        def selectableYears = (0..10).collect { new DateTime().plusYears(it).toString("yyyy") }
        // Used by directory method to fetch available cards
        Long facilityId = Order.get(params.orderId)?.facility?.id

        [timestamp   : new DateTime(), orderId: params.orderId, facilityId: facilityId, savePaymentInfo: params.savePaymentInfo,
         ignoreAssert: params.ignoreAssert, method: method, acceptedCards: adyenService.getAcceptedCardProviders() as JSON,
         promoCodeId : params.promoCodeId, selectableYears: selectableYears, texts: getTerminalTexts()]
    }

    /*
    * Payment (authorisation is made) is made
    * */

    def pay() {
        PaymentMethod method = params.method as PaymentMethod
        boolean useStoredDetails = method.equals(PaymentMethod.CREDIT_CARD_RECUR)
        boolean storePaymentDetails = params.boolean("savePaymentInfo")
        boolean ignoreAssert = params.boolean("ignoreAssert")
        User user = springSecurityService.getCurrentUser()

        Order order = Order.get(params.orderId)
        PromoCode promoCode

        if (order.article == Order.Article.BOOKING && params.promoCodeId) {
            promoCode = couponService.getValidPromoCode(params.promoCodeId as Long, order.facility)
            if (!promoCode || !promoCode.accept(slotService.getSlot(order.metadata?.slotId))) {
                throw new Exception(message(code: 'paymentController.process.errors.invalidPromoCode'))
            } else if (couponService.isPromoCodeUsed(user, promoCode)) {
                throw new Exception(message(code: 'paymentController.process.errors.promoCodeAlreadyUsed'))
            }
            couponService.usePromoCodeForOrders(promoCode, [order], grailsApplication.config.matchi.settings.currency[order.facility.currency].decimalPoints)
        }
        PaymentFlow paymentFlow = PaymentFlow.getInstance(session, order.id)
        if (order.total() == 0) {
            order.status = Order.Status.COMPLETED
            log.info(LogHelper.formatOrder("Order is free. Return from authorisation", order))
            forward controller: paymentFlow.paymentController, action: "process", params: [orderId: order.id, promoCodeId: promoCode?.id]
            return
        }
        AdyenOrderPayment orderPayment = AdyenOrderPayment.create(order, method, user)

        AdyenAuthorizationBuilder adyenAuthorizationBuilder

        if (useStoredDetails) {
            adyenAuthorizationBuilder = AdyenAuthorizationBuilderFactory.createStoredDetailsAuthorization()
        } else {
            adyenAuthorizationBuilder = AdyenAuthorizationBuilderFactory.createNewDetailsAuthorization(storePaymentDetails)
        }

        try {
            // ---------------------------
            // -- Local payment methods --
            // ---------------------------
            if (method.isLocal()) {
                log.debug(LogHelper.formatOrder("Pay using local payment method ${method}", order))
                orderStatusService.confirm(order, user) // TODO: Move confirmation of orders to one place. See Jira story MW-3529
                paymentFlow.setRedirectTrue()
                forward(action: "redirectLocal",
                        params: [method : params.method, issuerId: params.issuerId,
                                 orderId: order.id, paymentId: orderPayment.id])
                return
            }

            // ---------------------------
            // -- Authorisation request --
            // ---------------------------
            log.info(LogHelper.formatOrder("Authorisation start", order))
            def verification3DS = null
            orderPayment.authorise(params, order, adyenAuthorizationBuilder, ignoreAssert, { json ->
                verification3DS = json
            })
            log.info(LogHelper.formatOrder("Authorisation end", order))

            // -------------------------------------------------
            // -- Extra verification with 3D Secure if needed --
            // -------------------------------------------------
            if (verification3DS) {

                // Setting transactionId so we can refund authorisation notifications on failed comebacks
                if (verification3DS.pspReference) {
                    orderPayment.transactionId = verification3DS.pspReference
                    orderPayment.save()
                }

                log.info(LogHelper.formatOrder("Extra verification with 3D Secure", order))
                paymentFlow.setRedirectTrue()
                paymentFlow.setSavePaymentInfo(adyenAuthorizationBuilder.storePaymentDetails())

                log.info(LogHelper.formatOrder("before redirect to `redirect3DS` from pay", params.orderId as Long))
                forward(action: "redirect3DS",
                        params: [orderId  : order.id, paymentId: orderPayment.id,
                                 PaReq    : verification3DS.paRequest, MD: verification3DS.md,
                                 issuerUrl: verification3DS.issuerUrl, ignoreAssert: ignoreAssert, promoCodeId: promoCode?.id])
                return
            }
            log.info(LogHelper.formatOrder("before redirect to `process` from pay", params.orderId as Long))
            forward controller: paymentFlow.paymentController, action: "process", params: [orderId: order.id, promoCodeId: promoCode?.id]
        } catch (AdyenException e) {
            handleAdyenException(e, orderPayment, order, ignoreAssert)
        } catch (Exception e) {
            handleException(e, orderPayment, order, message(code: "adyen.error.message1") as String, ignoreAssert)
        }
        log.info(LogHelper.formatOrder("Pay end", order))
    }

    /*
    * Payment of 3DS
    * */

    def pay3DS() {
        Order order = Order.get(params.orderId)
        log.debug(LogHelper.formatOrder("Pay3DS" + params, order))
        Boolean ignoreAssert = params.boolean("ignoreAssert")

        AdyenOrderPayment orderPayment = AdyenOrderPayment.get(params.paymentId)
        PaymentFlow paymentFlow = PaymentFlow.getInstance(session, order.id)

        if (!paymentFlow) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND)
            return
        }
        params.put("userAgent", request.getHeader("user-agent"))
        params.put("acceptHeader", "application/json")

        try {
            orderPayment.authorise3DS(params, order, paymentFlow.savePaymentInfo)
            paymentFlow.setRedirectTrue()
            log.info(LogHelper.formatOrder("before redirect to `process` from pay3DS", params.orderId as Long))
            forward controller: paymentFlow.paymentController, action: "process", params: [orderId: order.id, promoCodeId: params.promoCodeId]
        } catch (AdyenException e) {
            handleAdyenException(e, orderPayment, order, ignoreAssert)
        } catch (Exception e) {
            handleException(e, orderPayment, order, message(code: "payment.showError.message1") as String, ignoreAssert)
        }
    }

    /*
    * Redirecting to 3DS
    * */

    def redirect3DS() {
        log.debug(LogHelper.formatOrder("redirect3DS", params.orderId as Long))
        def termUrl = grailsLinkGenerator.link(controller: "adyenPayment", action: "pay3DS", absolute: 'true',
                params: [orderId: params.orderId, paymentId: params.paymentId, ignoreAssert: params.ignoreAssert, promoCodeId: params.promoCodeId])

        [PaReq: params.PaReq, MD: params.MD, issuerUrl: params.issuerUrl, termUrl: termUrl]
    }

    /*
    * Redirecting to local payment method
    * */

    def redirectLocal() {
        log.debug(LogHelper.formatOrder("redirectLocal", params.orderId as Long))
        Map model = adyenService.skipDetails(params)
        model.put("url", "${grailsApplication.config.adyen.skipDetailsUrl}")

        [model: model]
    }

    /**
     * Validation of response for payment with local payment method (signature check)
     * Depending on the status, we might redirect to a PENDING page where we await the AdyenNotfication
     * @return
     */
    def validate() {
        Order order = Order.get(params.orderId)
        log.info(LogHelper.formatOrder("validate", order))

        AdyenOrderPayment orderPayment = AdyenOrderPayment.get(params.paymentId)
        PaymentFlow paymentFlow = PaymentFlow.getInstance(session, order.id)

        try {
            adyenService.validateLocalPaymentResponse(params)
            adyenService.updatePaymentOnLocalPaymentReponse(params, orderPayment)

            paymentFlow.setRedirectTrue()
            redirect controller: paymentFlow.paymentController, action: orderPayment.getReturnAction(), params: [orderId: order.id]
        } catch (AdyenException e) {
            handleAdyenException(e, orderPayment, order)
        }
    }

    /**
     * Checking PENDING payment from the UI
     * @return
     */
    def checkPendingPayment() {
        Order order = Order.get(params.long("orderId"))
        PaymentFlow paymentFlow = PaymentFlow.getInstance(session, order.id)

        if (!paymentFlow) {
            render status: HttpStatus.NOT_FOUND
            return
        }

        AdyenOrderPayment adyenOrderPayment = AdyenOrderPayment.findById(order.payments.first().id)

        // Must be AdyenOrderPayment
        if (!adyenOrderPayment) {
            throw new IllegalArgumentException("Can only check PENDING on AdyenOrderPayment")
        }

        adyenService.processOrTimeOutPending(adyenOrderPayment, userService.getCurrentUser())

        Map data = [:]

        // If the order is cancelled due to failed or timeout, we will redirect to finish page to show error
        if (adyenOrderPayment.ordersHaveStatus(Order.Status.CANCELLED)) {
            paymentFlow.error(adyenOrderPayment, messageSource.getMessage('adyen.error.message.PENDING', null, new Locale(order.user.language)))
            data.url = paymentFlow.finishUrl
        } else if (adyenOrderPayment.ordersHaveStatus(Order.Status.COMPLETED)) {
            data.url = createLink(controller: paymentFlow.paymentController, action: "process", params: [orderId: order.id], absolute: true)
        }

        render data as JSON
    }

    /*
    * Confirm notification from Adyen
    * */

    def confirm() {
        params.notificationItems.each { ni ->
            AdyenNotification.create(ni.NotificationRequestItem)
        }

        Map data = [notificationResponse: "[accepted]"]
        render data as JSON
    }

    /*
    * Get valid payment methods
    * */

    def directory() {
        Facility facility = Facility.get(params.facilityId)

        String merchantReference = (new Date().getTime()).toString()
        BigDecimal total = 1 // Cannot be zero when fetching the cards
        String countryCode = facility?.country ?: "SV"
        String currencyCode = facility?.currency ?: "SEK"

        def acceptedCardProviders = adyenService.getAcceptedCardProviders() as List
        def acceptedLocalMethods = adyenService.getAcceptedLocalPaymentMethods() as List
        def data = ["cards": [], "methods": []]

        adyenService.directory(merchantReference, total, countryCode, currencyCode, { def result ->
            result?.paymentMethods?.each { pm ->
                if (acceptedCardProviders.contains(pm.brandCode)) {
                    pm['i18code'] = message(code: "payment.method.adyen.${pm.brandCode}")
                    data["cards"] << pm
                } else if (acceptedLocalMethods.contains(pm.brandCode)) {
                    pm['i18code'] = message(code: "payment.method.adyen.${pm.brandCode}")
                    data["methods"] << pm
                }
            }
        })

        render data as JSON
    }

    private def handleAdyenException(AdyenException e, AdyenOrderPayment orderPayment, Order order, Boolean ignoreAssert = false) {

        PaymentFlow paymentFlow = PaymentFlow.getInstance(session, order.id)
        orderPayment.status = (orderPayment.status == OrderPayment.Status.PENDING) ? OrderPayment.Status.PENDING : OrderPayment.Status.FAILED

        orderPayment.errorMessage = e.getMessage()

        String msg = LogHelper.formatOrder("Handle AdyenException: " + orderPayment.errorMessage, order)

        if (e.resultCode == "Refused") {
            log.warn(msg, e)
        } else {
            // 103 => CVC is not the right length
            if (e.errorCode == "103") {
                log.info(msg, e)
            } else {
                log.error(msg, e)
            }
        }

        // Avoid overwriting with null
        if (e.pspReference) {
            orderPayment.transactionId = e.pspReference
        }

        orderPayment.save()
        orderPayment.orders.each {orderStatusService.cancel(order, userService.getCurrentUser(), ignoreAssert)}

        paymentFlow.error(e.getMessage())
        redirect(controller: paymentFlow.paymentController, action: "redirectToError", params: [orderPaymentId: orderPayment.id, orderId: order.id])
    }

    private def handleException(Exception e, AdyenOrderPayment orderPayment, Order order, String errorMessage, Boolean ignoreAssert = false) {
        log.error(LogHelper.formatOrder("Handle Exception: " + e.getStackTrace()?.toString(), order))

        PaymentFlow paymentFlow = PaymentFlow.getInstance(session, order.id)
        orderPayment.status = OrderPayment.Status.FAILED
        orderPayment.errorMessage = e.message ? e.getMessage() : errorMessage

        orderPayment.save()
        orderPayment.orders.each {orderStatusService.cancel(it, userService.getCurrentUser(), ignoreAssert)}

        // NOTE! Payment flow can be null if using multiple instances with round robin because then the session have not been stored/replicated when
        // using super fast request (302 redirect). Need to use sticky mode for instances and new payment solution should not use 302 redirects in the flow.
        // More information: https://github.com/magro/memcached-session-manager/issues/263
        // Preferred implementation is a chechkout based or use payment gateways own frontend solutions.
        if (paymentFlow) {
            paymentFlow.error(orderPayment.errorMessage)
            redirect(controller: paymentFlow.paymentController, action: "redirectToError", params: [orderPaymentId: orderPayment.id, orderId: order.id])
        } else {
            paymentFlow = PaymentFlow.popInstance(session, order.id)
            paymentFlow.error(orderPayment.errorMessage)
            render view: "/genericPayment/showGenericError", model: [message: paymentFlow.errorMessage]
        }
    }

    private Map getTerminalTexts() {
        Order order = Order.get(params.long("orderId"))

        Map texts = [
                header : message(code: "payment.form.header"),
                ingress: message(code: "payment.form.ingress"),
                payBtn : message(code: "button.finish.purchase.label")
        ]

        switch (order.article) {
            case Order.Article.PAYMENT_UPDATE:
                texts.header = message(code: "payment.form.header2")
                texts.ingress = message(code: "payment.form.ingress2")
                texts.payBtn = message(code: "button.finish.savecard.label")
                break
            default:
                break
        }

        return texts
    }
}
