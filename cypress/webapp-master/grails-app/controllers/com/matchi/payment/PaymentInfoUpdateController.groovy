package com.matchi.payment

import com.matchi.LogHelper
import com.matchi.PaymentInfo
import com.matchi.User
import com.matchi.orders.Order

class PaymentInfoUpdateController extends GenericPaymentController {

    final static BigDecimal PAYMENT_INFO_PRICE = 2.0
    final static BigDecimal PAYMENT_INFO_VAT = 0.0

    def userService

    def confirm() {
        User user = userService.loggedInUser
        PaymentInfo paymentInfo = paymentService.getAnyPaymentInfoByUser(user)

        def order = new Order()
        order.article = Order.Article.PAYMENT_UPDATE
        order.description = "Update of payment information"
        order.user = user
        order.issuer = user
        order.dateDelivery = new Date()
        order.origin = Order.ORIGIN_WEB

        order.price = PAYMENT_INFO_PRICE
        order.vat = PAYMENT_INFO_VAT
        order.metadata = [cancelUrl : createLink(action: "confirm", params: params, absolute: true),
                          processUrl: createLink(action: "process", absolute: true)]

        order.save(failOnError: true)

        String finishUrl = createLink(controller: "userProfile", action: "account",
                params: [orderId: order.id])
        startPaymentFlow(order.id, finishUrl)

        render(template: "confirm", model: [order: order, paymentInfo: paymentInfo])
    }

    def pay() {
        Order order = Order.get(params.long("orderId"))

        if (!order) {
            throw new IllegalStateException("No order number")
        }

        redirect(getPaymentProviderParameters(PaymentMethod.CREDIT_CARD, order, userService.loggedInUser))
    }

    def receipt() {
        def order = Order.get(params.orderId)
        PaymentFlow.popInstance(session, order.id)
        order.refund("Annul after payment info update")

        flash.message = message(code: "user.account.savedcard.finished")
        redirect(controller: "userProfile", action: "account")
    }

    @Override
    protected void processArticle(Order order) throws ArticleCreationException {
        log.info(LogHelper.formatOrder("before `redirect to finish` from PaymentInfoUpdateController", params.orderId as Long))
        redirectToFinish(PaymentFlow.State.RECEIPT, params.long("orderId"), [orderId: params.orderId])
    }

    @Override
    protected Order.Article getArticleType() {
        return Order.Article.PAYMENT_UPDATE
    }
}
