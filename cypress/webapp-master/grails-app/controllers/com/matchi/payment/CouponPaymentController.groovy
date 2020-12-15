package com.matchi.payment

import com.matchi.Customer
import com.matchi.Facility
import com.matchi.LogHelper
import com.matchi.OrderStatusService
import com.matchi.User
import com.matchi.coupon.CustomerCoupon
import com.matchi.coupon.GiftCard
import com.matchi.coupon.Offer
import com.matchi.orders.CouponOrderPayment
import com.matchi.orders.Order
import com.matchi.orders.OrderPayment
import grails.validation.Validateable
import groovy.transform.ToString

class CouponPaymentController extends GenericPaymentController {
    def couponPaymentService
    def userService
    OrderStatusService orderStatusService

    def confirm() {
        User user = userService.loggedInUser
        Offer coupon = Offer.get(params.id)
        Facility facility = coupon.facility
        Order order = couponPaymentService.createCouponPaymentOrder(user, coupon)

        order.metadata << [cancelUrl : createLink(action: "confirm", params: params, absolute: true),
                           processUrl: createLink(action: "process", absolute: true)]

        order.save()

        String finishUrl = createLink(controller: "facility", action: "show", params: [name: facility.shortname, orderId: order.id])
        startPaymentFlow(order.id, finishUrl)

        Map model = [order: order, coupon: coupon, orderId: order.id, facility: facility]
        model.paymentMethodsModel = getPaymentMethodsModel(coupon, user, facility, order.price)

        model
    }

    /**
     * Generates payment methods for the coupon card. If it is a punch card and the customer has a gift card, it is added.
     * @param coupon
     * @param user
     * @param facility
     * @param totalPrice
     * @return
     */
    protected Map getPaymentMethodsModel(Offer coupon, User user, Facility facility, BigDecimal totalPrice) {
        Map model = super.getPaymentMethodsModel(user, facility, totalPrice)
        if (model.isFree) return model

        if (!coupon.instanceOf(GiftCard)) {
            Customer customer = Customer.findByUserAndFacility(user, coupon.facility)

            List<CustomerCoupon> giftcards = couponService.getValidCouponsByCustomerUser(customer, GiftCard, totalPrice)
            addGiftCardsToViewModel(model, giftcards)
        }


        model.translations = translatePaymentMethods(model.methods)
        return model
    }

    def pay(CouponPaymentCommand cmd) {
        def user = userService.loggedInUser
        def coupon = Offer.get(cmd.id)
        def order = Order.get(cmd.orderId)
        def method = cmd.method as PaymentMethod

        if (order.total() == 0) {
            log.info("Order is free, skipping to process for order ${order}")
            orderStatusService.complete(order, user)
            redirect(action: "process", params: [orderId: order.id])
            return
        }

        if (!cmd.validate()) {
            render(view: "confirm", model: [coupon: coupon, paymentInfo: getPaymentInfo(user), order: order, command: cmd])
        } else {

            if (method.isUsingPaymentGatewayMethod()) {
                redirect(getPaymentProviderParameters(method, order, user))
                return
            }

            if (method.equals(PaymentMethod.GIFT_CARD)) {
                log.info("Processing payment with gift card for ${order}")

                def customerCoupon = couponService.getExpiresFirstCustomerCoupon(
                        Customer.findByUserAndFacility(user, coupon.facility),
                        Offer.get(cmd.customerCouponId), order.price)

                Order.withTransaction {
                    def ticket = customerCoupon ? couponService.consumeTicket(
                            customerCoupon, order, coupon.name, coupon.id) : null

                    def couponOrderPayment = new CouponOrderPayment(order: order,
                            amount: order.total(), vat: order.vat(), issuer: user,
                            ticket: ticket, status: OrderPayment.Status.CAPTURED, method: method)
                    if (ticket?.save()) {
                        log.info("CustomerCoupon ${customerCoupon?.id} \"${customerCoupon?.coupon?.name}\" for customer ${customerCoupon?.customer?.id} \"${customerCoupon?.customer}\": Ticket with amount ${ticket.getAmount()} consumed successfully for ${order?.id}")
                        order.status = Order.Status.COMPLETED
                    } else {
                        couponOrderPayment.status = OrderPayment.Status.FAILED
                        couponOrderPayment.errorMessage =
                                "No tickets left on customer coupon ${customerCoupon?.id}"
                        order.status = Order.Status.CANCELLED
                    }

                    couponOrderPayment.save(failOnError: true)
                    order.addToPayments(couponOrderPayment)
                    order.save()
                }

                if (order.status == Order.Status.COMPLETED) {
                    def purchasedCoupon = couponPaymentService.registerCouponPurchase(
                            order.user, coupon, coupon.nrOfTickets, order.user, order)

                    redirect action: "receipt", params: [orderId         : params.orderId,
                                                         customerCouponId: purchasedCoupon?.id]
                } else {
                    render view: "/bookingPayment/showError",
                            model: [message: g.message(code: "payment.giftCard.error")]
                }
            }
        }

    }

    @Override
    protected void processArticle(Order order) throws ArticleCreationException {
        def coupon = Offer.get(order.metadata.couponId)
        CustomerCoupon customerCoupon

        if (!coupon) {
            throw new ArticleCreationException(message(code: 'couponPaymentController.process.errors.couponNotFound') as String)
        }

        try {
            customerCoupon =
                    couponPaymentService.registerCouponPurchase(order.user, coupon, coupon.nrOfTickets, order.user, order)
        } catch (Throwable t) {
            log.error(t.message)
            throw new ArticleCreationException(message(code: 'couponPaymentController.process.errors.creationError') as String)
        }

        if (!customerCoupon) throw new ArticleCreationException(message(code: 'couponPaymentController.process.errors.creationError') as String)

        log.info("Showing receipt for ${order}")
        log.info(LogHelper.formatOrder("before `redirect to finish` from CouponPaymentController", params.orderId as Long))
        redirectToFinish(PaymentFlow.State.RECEIPT, params.long("orderId"),
                [orderId: params.orderId, customerCouponId: customerCoupon?.id])
    }

    @Override
    protected Order.Article getArticleType() {
        return Order.Article.COUPON
    }

    def receipt() {
        def order = Order.get(params.orderId)
        def customerCoupon = CustomerCoupon.findByOrder(order)
        PaymentFlow paymentFlow = PaymentFlow.popInstance(session, order.id)

        if (!order) { // check user access to order
            // display error
        }

        render view: "receipt", model: [customerCoupon: customerCoupon, order: order, showPage: paymentFlow.showPage()]
    }
}

@ToString(includeNames = true)
@Validateable(nullable = true)
class CouponPaymentCommand {
    def userService
    def springSecurityService

    Long id
    String orderId
    String method
    boolean savePaymentInformation
    Long customerCouponId

    static constraints = {
        id(nullable: false)
        orderId(nullable: false)
        method(nullable: false)
        customerCouponId(nullable: true)
    }
}

