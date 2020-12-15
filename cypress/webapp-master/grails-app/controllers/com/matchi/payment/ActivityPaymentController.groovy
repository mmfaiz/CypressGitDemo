package com.matchi.payment

import com.matchi.Customer
import com.matchi.Facility
import com.matchi.LogHelper
import com.matchi.OrderStatusService
import com.matchi.activities.ActivityOccasion
import com.matchi.coupon.Offer
import com.matchi.orders.CouponOrderPayment
import com.matchi.orders.Order
import com.matchi.orders.OrderPayment
import grails.validation.Validateable

class ActivityPaymentController extends GenericPaymentController {

    public static final String ACTIVITY_FIELDS_TEMPLATE = "activity"
    public static final String ACTIVITY_TERMS_TEMPLATE = "activityTerms"

    def activityService
    def objectWatchNotificationService
    OrderStatusService orderStatusService

    def confirm() {
        def user = getCurrentUser()
        def occasion = ActivityOccasion.get(params.id)
        Facility facility = occasion.activity.facility
        def customer = Customer.findByUserAndFacility(user, facility)

        flash.error = ""

        if (occasion?.isDeleted()) {
            render view: "showError", model: [message: message(code: 'facilityActivityOccasion.delete.notFound')]
        }else if (occasion.isParticipating(user)) {
            if (!occasion.activity.cancelByUser) {
                render view: "showError", model: [message: message(code: 'payment.cancel.forbidden.error')]
            } else {
                render(view: "cancel", model: [occasion: occasion, participation: occasion.getParticipation(user)])
            }
        } else {
            def order = activityService.createActivityPaymentOrder(user, occasion)
            order.metadata << [cancelUrl : createLink(action: "confirm", params: params),
                               processUrl: createLink(action: "process")]
            order.save()

            String finishUrl = PaymentFlow.createFinishUrl(request.getHeader(REFERER_KEY), order.id)
            startPaymentFlow(order.id, finishUrl)

            def slots = occasion.bookings.collect { it.slot }

            Map paymentMethodsModel = getPaymentMethodsModel(slots, user, customer, facility, order.price)
            paymentMethodsModel.extraFieldsTemplate = ACTIVITY_FIELDS_TEMPLATE
            if (occasion.activity.terms) {
                paymentMethodsModel.agreements << ACTIVITY_TERMS_TEMPLATE
            }

            [occasion: occasion, totalPrice: order.price, order: order, facility: facility, paymentMethodsModel: paymentMethodsModel]
        }
    }

    def pay(OccasionPaymentCommand cmd) {
        def user = getCurrentUser()
        def occasion = ActivityOccasion.get(cmd.id)
        def customer = Customer.findByUserAndFacility(user, occasion.activity.facility)
        def order = Order.get(cmd.orderId)
        def slots = occasion.bookings.collect { it.slot }
        def method = cmd.method as PaymentMethod

        if (cmd.hasErrors()) {
            render view: "showError"
            return
        }
        if (!order || !occasion) {
            render view: "showError"
            return
        }
        if (occasion.activity.terms && !cmd.acceptTerms) {
            render view: "showError", model: [message: message(code: 'payment.confirm.activityTerms.error')]
            return
        }
        if (occasion.isFull()) {
            render view: "showError", model: [message: message(code: 'facilityActivityOccasion.edit.message12')]
            return
        }

        if (cmd.userMessage) {
            order.metadata[Order.META_USER_MESSAGE] = cmd.userMessage
            order.save()
        }

        // free bookings
        if (order.total() == 0) {
            getOrderStatusService().confirm(order, user)
            redirect(action: "process", params: [orderId: order.id])
            return
        }

        try {
            if (method.isUsingPaymentGatewayMethod()) {
                redirect(getPaymentProviderParameters(method, order, user))
                return
            }

            if ([PaymentMethod.COUPON, PaymentMethod.GIFT_CARD].contains(method)) {

                if (order.article.equals(Order.Article.ACTIVITY)) {
                    def coupon = couponService.getExpiresFirstCustomerCoupon(
                            customer, Offer.get(cmd.customerCouponId), order.price)
                    def ticket = coupon ? couponService.consumeTicket(
                            coupon, order, occasion.shortDescription, occasion.id) : null

                    Order.withTransaction {
                        CouponOrderPayment couponOrderPayment = new CouponOrderPayment(order: order,
                                amount: order.total(), vat: order.vat(), issuer: getCurrentUser(), ticket: ticket,
                                status: OrderPayment.Status.CAPTURED, method: method)
                        if (ticket) {
                            log.info("CustomerCoupon ${coupon?.id} \"${coupon?.coupon?.name}\" for customer ${coupon?.customer?.id} \"${coupon?.customer}\": Ticket with amount ${ticket.getAmount()} consumed successfully for ${order?.id}")
                            order.status = Order.Status.COMPLETED
                        } else {
                            couponOrderPayment.status = OrderPayment.Status.FAILED
                            couponOrderPayment.errorMessage = "No tickets left on customer coupon ${coupon?.id}"
                            order.status = Order.Status.CANCELLED
                        }

                        order.customer = customer

                        couponOrderPayment.save(failOnError: true)
                        order.addToPayments(couponOrderPayment)
                        order.save(flush: true)
                    }

                    try {
                        if (order.status == Order.Status.COMPLETED) {
                            activityService.book(order)

                            log.info("Order processed successfully ${order?.id}")
                        } else {
                            render view: "showError", model: [message: message(code: 'paymentController.pay.errors.notEnoughCredits')]
                            return
                        }

                    } catch (Throwable t) {
                        log.error("Error while processing activity booking order", t)
                        Order.withTransaction {
                            order = order = Order.get(params.orderId)
                            order.refund("Unable to process activity booking: ${t.message}")
                        }

                        throw new RuntimeException(t)
                    }
                }

                redirect action: "receipt", params: [orderId: order.id]
                // redirect(action: "process", params: [orderId: order.id])
            }

        } catch (RuntimeException re) {
            log.error(re)
            render view: "showError", model: [message: "Exception: ${re.message}"]
        }
    }

    @Override
    protected void processArticle(Order order) throws ArticleCreationException {
        ActivityOccasion occasion = ActivityOccasion.get(order.metadata?.activityOccasionId)

        if (occasion.isFull()) {
            throw new ArticleCreationException(message(code: 'facilityActivityOccasion.edit.message12') as String)
        }

        try {
            activityService.book(order)
        } catch (Throwable t) {
            log.error(t.message)
            throw new ArticleCreationException(message(code: 'activityPaymentController.process.errors.couldNotProcess') as String)
        }
        log.info(LogHelper.formatOrder("before `redirect to finish` from ActivityPaymentController", params.orderId as Long))
        redirectToFinish(PaymentFlow.State.RECEIPT, order.id, [orderId: params.orderId])
    }

    @Override
    protected Order.Article getArticleType() {
        return Order.Article.ACTIVITY
    }

    def receipt() {
        def order = Order.get(params.orderId)
        PaymentFlow paymentFlow = PaymentFlow.popInstance(session, order.id)

        if (!order) { // check user access to order
            // display error
        }

        def occasion = ActivityOccasion.get(order.metadata.activityOccasionId)

        render view: "receipt", model: [occasion: occasion, order: order, showPage: paymentFlow.showPage()]
    }

    def cancel() {
        log.info "ActivityPaymentController.cancel()"
        def user = getCurrentUser()
        def occasion = ActivityOccasion.get(params.id)
        def participation = occasion.getParticipation(user)

        if (participation) {
            def cancelResponse = activityService.cancelAndRefundParticipant(occasion, user)

            if (cancelResponse == true) {
                return render(view: "cancel.confirm", model: [occasion: occasion, participation: participation])
            } else if (cancelResponse instanceof String) {
                return render(view: "showError", model: [message: cancelResponse])
            }
        }
        return render(view: "showError", model: [message: message(code: "activityPayment.cancel.error2")])
    }
}

@Validateable(nullable = true)
class OccasionPaymentCommand {
    def userService
    def springSecurityService

    Long id
    Long customerCouponId
    String orderId
    String method
    boolean savePaymentInformation
    String userMessage
    Boolean acceptTerms

    static constraints = {
        id(nullable: false)
        orderId(nullable: false)
        method(nullable: false)
        customerCouponId(nullable: true, blank: true)
        userMessage(nullable: true, maxSize: 255)
        acceptTerms(nullable: true)
    }
}
