package com.matchi.payment

import com.matchi.Customer
import com.matchi.Facility
import com.matchi.LogHelper
import com.matchi.PaymentInfo
import com.matchi.User
import com.matchi.membership.Membership
import com.matchi.orders.Order
import com.matchi.orders.OrderPayment
import grails.validation.Validateable
import org.apache.commons.lang.time.DateUtils
import org.apache.http.HttpStatus
import org.joda.time.DateTime

class RemotePaymentController extends GenericPaymentController {

    def memberService
    def membershipPaymentService
    def notificationService
    def scheduledTaskService
    def orderService

    def index() {}

    def confirm(Boolean familyMembership) {
        def memberships = []
        User user = getCurrentUser()

        if (!user) {
            render view: "error", model: [errorMessage: message(code: "form.submit.noCustomer.error")]
            return
        }

        Order order = Order.get(params.id)

        if (!order) {
            throw new IllegalStateException()
        }

        if (!order.remotePayable) {
            render view: "error", model: [errorMessage: message(code: "remotePayment.confirm.notRemotePayableError", args: [order.id])]
            return
        }

        Facility facility = order.facility
        Customer customer = Customer.findByUserAndFacility(user, facility)

        // The user must be valid to pay for this order
        if (!customer || customer != order.customer) {
            render status: HttpStatus.SC_UNAUTHORIZED
            return
        }

        List<String> agreementTemplates = []
        boolean allowRedirect = true

        // TODO: Move this into subclasses such as MembershipRemotePaymentController and make RemotePaymentController abstract
        if (order.article == Order.Article.MEMBERSHIP) {
            def membership = memberService.getMembership(order, user)
            if (!membership) {
                render status: HttpStatus.SC_NOT_FOUND
                return
            }

            if (membership.type?.recurring) {
                agreementTemplates << "recurringMembershipAgreement"
                allowRedirect = false
            }

            memberships = [membership]
            if (familyMembership && membership.family) {
                order = membershipPaymentService.createFamilyMembershipPaymentOrder(
                        membership.family, user, membership.customer)
                if (order) {
                    def membershipIdsToUpdate = order.metadata.membershipsToUpdate.tokenize(",")*.toLong()
                    memberships = membership.family.members.findAll {
                        membershipIdsToUpdate.contains(it.id)
                    }
                } else {
                    render view: "error", model: [errorMessage: message(code: "remotePayment.confirm.familyPriceError")]
                    return
                }
            }
        }

        // An order could potentially have been tried at before, with failed payment
        // We solve this by replacing with a NEW order (just like in the ordinary flow, when an order/payment fails)
        if (order.payments || order.status != Order.Status.NEW) {
            order = orderService.replaceOrderWithFreshCopy(order)
        }

        Map model = [totalPrice: order.price, order: order, facility: facility, memberships: memberships]
        model.paymentMethodsModel = super.getPaymentMethodsModel(user, facility, order.price as BigDecimal, allowRedirect, agreementTemplates)

        if (!model.paymentMethodsModel.methods) {
            render view: "error", model: [errorMessage: message(code: "payment.confirm.warning.savedCardRequired", args: [createLink(controller: "userProfile", action: "account")])]
            return
        }

        String finishUrl = params.finishUrl ?: createLink(controller: "userProfile", action: "remotePayments", params: [orderId: order.id])
        startPaymentFlow(order.id, finishUrl)

        return model
    }

    def pay(RemotePaymentCommand cmd) {
        User user = getCurrentUser()

        if (!user) {
            render status: HttpStatus.SC_UNAUTHORIZED
            return
        }

        Order order = Order.get(cmd.orderId)

        if (!order) {
            throw new IllegalStateException()
        }

        if (!order.remotePayable) {
            render view: "error", model: [errorMessage: message(code: "remotePayment.confirm.notRemotePayableError", args: [order.id])]
            return
        }

        Facility facility = order.facility
        Customer customer = Customer.findByUserAndFacility(user, facility)

        // The user must be valid to pay for this order
        if (!customer || customer != order.customer) {
            render status: HttpStatus.SC_UNAUTHORIZED
            return
        }

        PaymentMethod method = cmd.method as PaymentMethod

        if (!cmd.validate()) {
            render view: "showError", model: [message: "Could not process"]
            return
        } else {
            if (cmd.allowRecurring) {
                order.metadata[Order.META_ALLOW_RECURRING] = "true"
                order.save()
            }

            try {
                if (method.isUsingPaymentGatewayMethod()) {
                    redirect(getPaymentProviderParameters(method, order, user))
                    return
                }

                throw new IllegalArgumentException("Non-gateway payment method used for remote payment")

            } catch (RuntimeException re) {
                log.error(re)
                render view: "showError", model: [message: "Exception: ${re.message}"]
            }
        }
    }

    @Override
    protected void processArticle(Order order) throws ArticleCreationException {
        User user = getCurrentUser()
        Long orderId = order.id

        // Update Order deliveryDate to payment created date to have them synced if delivery date is before payment date
        OrderPayment firstPayment = order.payments.first() as OrderPayment
        if (!DateUtils.isSameDay(order.dateDelivery, firstPayment.dateCreated) &&
                order.dateDelivery < firstPayment.dateCreated) {
            order.dateDelivery = firstPayment.dateCreated
            order.save(flush: true)
        }

        if (order.metadata?.membershipsToUpdate) {
            Membership.findAllByIdInList(order.metadata.membershipsToUpdate.tokenize(",")*.toLong()).each {
                it.setSharedOrder(order, true)
            }
        }

        if (Boolean.valueOf((String) order.metadata?.get(Order.META_ALLOW_RECURRING))) {
            def membership = memberService.getMembership(order, user)
            membership.autoPay = true
            membership.save()
        }

        scheduledTaskService.scheduleTask(message(code: 'scheduledTask.sendEmailConfirmation.taskName'), user.id, null) { taskId ->
            notificationService.sendPaidRemoteOrderNotification(Order.get(orderId))
        }
        log.info(LogHelper.formatOrder("before `redirect to finish` from RemotePaymentController", params.orderId as Long))
        redirectToFinish(PaymentFlow.State.RECEIPT, order.id, [orderId: params.orderId])
    }

    def receipt() {
        Order order = Order.get(params.orderId)
        PaymentFlow paymentFlow = PaymentFlow.popInstance(session, order.id)

        render view: "receipt", model: [order: order, showPage: paymentFlow.showPage()]
    }

    @Override
    protected Order.Article getArticleType() {
        return null
    }
}

@Validateable(nullable = true)
class RemotePaymentCommand {
    Long orderId
    String method
    boolean allowRecurring

    static constraints = {
        orderId(nullable: false)
        method(nullable: false)
    }
}