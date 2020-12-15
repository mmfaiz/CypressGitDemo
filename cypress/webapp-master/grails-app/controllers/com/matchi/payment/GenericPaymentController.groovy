package com.matchi.payment

import com.matchi.*
import com.matchi.coupon.Coupon
import com.matchi.coupon.CustomerCoupon
import com.matchi.coupon.CustomerOfferGroup
import com.matchi.coupon.GiftCard
import com.matchi.orders.Order
import grails.converters.JSON
import org.springframework.http.HttpStatus

abstract class GenericPaymentController extends GenericController {

    PaymentService paymentService
    CouponService couponService
    SlotService slotService
    CustomerService customerService

    static final String REFERER_KEY = 'Referer'

    def beforeInterceptor = [action: this.&checkPayment, only: ['pay']]

    /**
     * Generic action for all PaymentsController for online purchases
     * @return
     */
    def process() {
        Order order = Order.get(params.long("orderId"))
        log.info(LogHelper.formatOrder("Process", order))

        if (!order) {
            redirectNonPaymentError(message(code: 'genericPaymentController.process.errors.orderNotFound',
                    args: [params.long("orderId")]) as String, params.long("orderId"))
            return
        }

        if (!order.isProcessable()) {
            redirectNonPaymentError(message(code: 'genericPaymentController.process.errors.paymentNotCompleted') as String,
                    order.id)
            return
        }

        if (getArticleType() && order.article != getArticleType()) {
            redirectNonPaymentError(message(code: 'genericPaymentController.process.errors.incorrectArticleType',
                    args: [order.id, getArticleType().toString()]) as String, order.id)
            return
        }

        try {
            processArticle(order)
        } catch (IllegalStateException ise) {
            log.error("Failed to process article for Order.id = ${order.id} ${getClass()}" ,ise)
            redirectNonPaymentError(ise.getMessage(), order.id)
        } catch (ArticleCreationException ace) {
            log.error("Failed to process article for Order.id = ${order.id} ${getClass()}" ,ace)

            Order.withTransaction {
                order = Order.get(params.long("orderId"))
                order.refund("Unable to process ${order?.article?.toString()}: ${ace.getMessage()}")
                order.assertCustomer()
                order.status = Order.Status.ANNULLED
                order.save(flush: true)
            }

            redirectNonPaymentError(ace.getMessage(), order.id)
        } catch (Exception e) {
            IArticleItem articleItem = order.retrieveArticleItem()
            log.error("Failed to process article for Order.id = ${order.id} ${getClass()}" ,e)

            // Refund if no article was completed
            if (!articleItem) {
                Order.withTransaction {
                    order = Order.get(params.long("orderId"))
                    order.refund("Unable to process ${order?.article?.toString()}: ${e.getMessage()}")
                    order.assertCustomer()
                    order.status = Order.Status.ANNULLED
                    order.save(flush: true)
                }
            }

            redirectNonPaymentError(e.getMessage(), order.id)
        }
    }

    /**
     * Generic action for when receiving status PENDING from Adyen.
     */
    def pending() {
        Order order = Order.get(params.long("orderId"))
        PaymentFlow paymentFlow = PaymentFlow.getInstance(session, order.id)

        // To prevent accessing pending page for order not in existing payment flow
        if (!paymentFlow) {
            render status: HttpStatus.NOT_FOUND
            return
        }

        log.info(LogHelper.formatOrder("Pending", order))

        Map model = [
                order   : order,
                interval: grailsApplication.config.matchi.pending.interval,
                timeout : grailsApplication.config.matchi.pending.timeout
        ]

        render view: "/genericPayment/pending", model: model
    }

    /**
     * General checks before paying for the article
     */
    protected checkPayment() {
        log.info "Verify user is still logged in before processing of order $params.orderId"
        if (!springSecurityService.isLoggedIn()) {
            render status: 401
            return false
        }

        log.info "Check double payment for order $params.orderId"
        Order order = Order.get(params.long("orderId"))
        if (order.hasPayments()) {
            log.info(LogHelper.formatOrder("Double payment prevention", order.id))
            PaymentFlow paymentFlow = PaymentFlow.getInstance(session, order.id)
            paymentFlow?.error()
            // Depending on how user reloads page, this could be lost. But error should still show up.
            redirect(action: "error", params: [orderId: order.id])
            return false
        }
        return true
    }

    protected boolean hasRefererHeader() {
        return request.getHeader(REFERER_KEY)?.size() > 0
    }

    protected def startPaymentFlow(Long orderId, String finishUrl) {
        if (!finishUrl) {
            throw IllegalArgumentException("finishUrl cannot be null when creating PaymentFlow")
        }

        PaymentFlow paymentFlow = new PaymentFlow(orderId)
        paymentFlow.paymentController = this.controllerName
        paymentFlow.finishUrl = finishUrl

        log.debug(LogHelper.formatOrder("Got finishUrl: ${paymentFlow.finishUrl}", paymentFlow.orderId))
        PaymentFlow.addInstance(session, paymentFlow)
    }

    protected Map getPaymentProviderParameters(PaymentMethod method, Order order, User user, Long promoCodeId = null) {

        return [controller: "adyenPayment", action: "index", params:
                [orderId        : order.id,
                 method         : method,
                 issuerId       : params.issuerId,
                 savePaymentInfo: params.savePaymentInformation ? "true" : "false",
                 ignoreAssert   : params.ignoreAssert ? "true" : "false",
                 promoCodeId    : promoCodeId]]

    }

    protected redirectToFinish(PaymentFlow.State state, Long orderId, Map extraParams = [:]) {
        PaymentFlow paymentFlow = PaymentFlow.getInstance(session, orderId)
        paymentFlow.state = state
        log.info(LogHelper.formatOrder("Before `redirect to finish` paymentflow GenericPaymentController: ${paymentFlow as JSON}", orderId))

        extraParams.put("orderId", orderId)
        if (paymentFlow.isRedirect) {
            redirect([url: paymentFlow.finishUrl ?: createLink(action: paymentFlow.getFinalAction(), params: extraParams)])
        } else {
            redirect([action: paymentFlow.getFinalAction(), params: extraParams])
        }
    }

    protected def redirectNonPaymentError(String errorMessage, Long orderId) {
        log.error(LogHelper.formatOrder(errorMessage, orderId))
        PaymentFlow paymentFlow = PaymentFlow.getInstance(session, orderId)
        paymentFlow.error(errorMessage)
        redirectToFinish(PaymentFlow.State.ERROR, orderId)
    }

    /**
     * Note! This is an action method used by AdyenPaymentController. But it might not look like it when using IntelliJ.
     * @param orderId
     * @return
     */
    def redirectToError(Long orderId) {
        log.debug(LogHelper.formatOrder("Redirect to error", orderId))
        redirectToFinish(PaymentFlow.State.ERROR, orderId)
    }

    def error(Long orderId) {
        log.debug(LogHelper.formatOrder("Show generic error", orderId))
        PaymentFlow paymentFlow = PaymentFlow.popInstance(session, orderId)
        render view: "/genericPayment/showGenericError", model: [message: paymentFlow?.errorMessage]
    }

    /**
     * Returns the first PaymentInfo if exists.
     * @param user
     * @return
     */
    protected PaymentInfo getPaymentInfo(User user) {
        return paymentService.getAnyPaymentInfoByUser(user)
    }

    /**
     * Builds a view model for paying with local payment method, credit card or free
     * @param user
     * @param facility
     * @param totalPrice
     * @return
     */
    protected Map getPaymentMethodsModel(User user, Facility facility, BigDecimal totalPrice, boolean allowRedirect = true, List<String> agreementTemplates = []) {
        if (totalPrice == 0) {
            return [
                    methods            : [PaymentMethod.FREE],
                    translations       : [FREE: message(code: "payment.method.FREE")],
                    offers             : [],
                    localPaymentMethods: false,
                    isFree             : true,
                    agreements         : agreementTemplates
            ]
        }

        List<PaymentMethod> availableMethods = []

        if (getPaymentInfo(user)) {
            availableMethods.push(PaymentMethod.CREDIT_CARD_RECUR)
        }

        if (allowRedirect) {
            availableMethods.push(PaymentMethod.CREDIT_CARD)
        }

        return [
                methods            : availableMethods,
                offers             : [],
                localPaymentMethods: allowRedirect,
                translations       : translatePaymentMethods(availableMethods),
                isFree             : false,
                agreements         : agreementTemplates
        ]
    }

    /**
     * Translates a list of payment methods into presentable names
     * @param methods
     * @return
     */
    protected Map<String, String> translatePaymentMethods(List<PaymentMethod> methods) {
        return methods.collectEntries() { PaymentMethod method ->
            Map entry = [(method.name()): message(code: "payment.method.${method.name()}").toString()]

            if (message(code: "payment.method.${method.name()}.extraInfo", default: null)) {
                entry["${method.name()}.extraInfo"] = message(code: "payment.method.${method.name()}.extraInfo")
            }

            if (message(code: "payment.method.${method.name()}.hint", default: null)) {
                entry["${method.name()}.hint"] = message(code: "payment.method.${method.name()}.hint")
            }

            return entry
        }
    }

    protected abstract void processArticle(Order order) throws ArticleCreationException

    protected abstract Order.Article getArticleType()

    /**
     * Generates payment methods for the activity.
     * @param slots
     * @param user
     * @param customer
     * @param facility
     * @param totalPrice
     * @return
     */
    protected Map getPaymentMethodsModel(List<Slot> slots, User user, Customer customer, Facility facility, BigDecimal totalPrice, boolean allowRedirect = true) {
        Map model = getPaymentMethodsModel(user, facility, totalPrice, allowRedirect)

        if (model.isFree) return model

        List<CustomerCoupon> coupons = (user ? couponService.getValidCouponsByUserAndSlots(user, slots, null, Coupon.class, true) : []) as List<CustomerCoupon>
        List<CustomerCoupon> giftcards = (user ? couponService.getValidCouponsByUserAndSlots(user, slots, totalPrice, GiftCard.class) : []) as List<CustomerCoupon>

        addCouponsToViewModel(model, coupons)
        addGiftCardsToViewModel(model, giftcards)

        model.translations = translatePaymentMethods(model.methods)
        model.promoCodeAvailable = !couponService.getActivePromoCodes(facility).isEmpty()
        return model
    }

    /**
     * Adds a list of coupon cards to the payment method view model.
     * @param model
     * @param coupons
     * @param addPlaceholder
     */
    protected void addCouponsToViewModel(Map model, List<CustomerCoupon> coupons, boolean addPlaceholder = false, Integer minAmount = null) {
        addOffersToViewModel(model, coupons, PaymentMethod.COUPON, "paymentInformationCoupon", "customerCouponId", addPlaceholder, minAmount)
    }

    /**
     * Adds a list of gift cards to the payment method view model.
     * @param model
     * @param giftcards
     * @param addPlaceholder
     */
    protected void addGiftCardsToViewModel(Map model, List<CustomerCoupon> giftcards, boolean addPlaceholder = false) {
        addOffersToViewModel(model, giftcards, PaymentMethod.GIFT_CARD, "paymentInformationGiftCard", "customerGiftCardId", addPlaceholder)
    }

    /**
     * Adds a list of supplied offers to the view model of payment methods.
     * Can add a placeholder if needed for a dynamic UI (for example trailing bookings)
     * @param model
     * @param offers
     * @param paymentMethod
     * @param containerId
     * @param selectId
     * @param addPlaceholder
     */
    private void addOffersToViewModel(Map model, List<CustomerCoupon> offers,
                                      PaymentMethod paymentMethod, String containerId, String selectId,
                                      boolean addPlaceholder = false, Integer minAmount = null) {
        Map offerModel = [
                className  : paymentMethod,
                containerId: containerId,
                selectId   : selectId,
                list       : []
        ]

        if (offers && !offers.isEmpty()) {
            model.methods << paymentMethod

            offerModel.list = CustomerOfferGroup.fromCustomerCoupons(offers).findAll {
                it.remainingNrOfTickets == null || !minAmount || it.remainingNrOfTickets >= minAmount
            }

            model.offers << offerModel

            // If needed for changing the payment methods in the confirm modal
        } else if (addPlaceholder) {
            model.offers << offerModel
        }
    }
}
