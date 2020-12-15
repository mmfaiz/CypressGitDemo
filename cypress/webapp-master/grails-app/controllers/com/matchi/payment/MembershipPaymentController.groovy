package com.matchi.payment

import com.matchi.Customer
import com.matchi.Facility
import com.matchi.FacilityProperty.FacilityPropertyKey
import com.matchi.LogHelper
import com.matchi.OrderStatusService
import com.matchi.User
import com.matchi.membership.Membership
import com.matchi.membership.MembershipType
import com.matchi.orders.Order
import com.matchi.price.Price
import grails.validation.Validateable
import org.joda.time.LocalDate

/**
 * @author Sergei Shushkevich
 */
class MembershipPaymentController extends GenericPaymentController {
    def membershipPaymentService
    def memberService
    def notificationService
    def userService
    def membersFamilyService
    OrderStatusService orderStatusService

    def confirm(Long id, String facilityMessage,
                Boolean purchaseUpcomingMembership, Long purchasedAtFacilityId) {
        User user = userService.loggedInUser
        MembershipType membershipType = MembershipType.get(id)
        Membership baseMembership = Membership.get(params.baseMembership)
        Facility facility = membershipType.facility

        def customer = customerService.getOrCreateUserCustomer(user, facility)
        def startDate = params.startDate ? new LocalDate(params.startDate) : LocalDate.now()
        def membership = Membership.newInstanceWithDates(
                membershipType, membershipType.facility, startDate)

        List<String> agreementTemplates = []
        boolean allowRedirect = true

        if (membership.type?.recurring) {
            agreementTemplates << "recurringMembershipAgreement"
            allowRedirect = false
        }

        if ((!baseMembership && customer.hasUpcomingMembership())
                || (customer.getMembership(membership.startDate) && baseMembership
                && customer.getMembership(membership.startDate) != baseMembership)) {
            render(view: "/membershipPayment/showError",
                    model: [membershipType: membershipType, startDate: membership.startDate.toDate()])
            return
        }

        def order = membershipPaymentService.createMembershipPaymentOrder(user, membershipType)

        order.metadata << [cancelUrl : createLink(action: "confirm", params: params, absolute: true),
                           processUrl: createLink(action: "process", absolute: true),
                           customerId: customer.id.toString(), facilityMessage: facilityMessage,
                           startDate : membership.startDate.toString(), purchasedAtFacilityId: purchasedAtFacilityId.toString()]
        if (purchaseUpcomingMembership) {
            order.metadata.purchaseUpcomingMembership = purchaseUpcomingMembership.toString()
        }
        if (baseMembership) {
            order.metadata.baseMembershipId = baseMembership.id.toString()
        }

        def members = params.findAll { it.key.startsWith("member_") }
        if (members) {
            order.metadata << members
            order.price = Math.min(members.values().sum(order.price.toLong()) { MembershipType.get(it).price },
                    facility.familyMaxPrice)
            order.vat = Price.calculateVATAmount(order.price.toLong(),
                    new Double(customer.facility.vat))
        }

        order.save()

        Map paymentMethodsModel = getPaymentMethodsModel(user, facility, order.price, allowRedirect, agreementTemplates)

        if (!paymentMethodsModel.methods) {
            render view: "error", model: [errorMessage: message(code: "payment.confirm.warning.savedCardRequired", args: [createLink(controller: "userProfile", action: "account")])]
            return
        }

        String finishUrl = createLink(controller: "facility", action: "show",
                params: [name: (Facility.get(purchasedAtFacilityId) ?: facility).shortname])
        startPaymentFlow(order.id, finishUrl)


        render(template: "confirm", model: [order              : order, membershipType: membershipType,
                                            paymentMethodsModel: paymentMethodsModel, startDate: membership.startDate.toDate(),
                                            endDate            : membership.endDate.toDate(), facility: facility])
    }

    def pay(MembershipPaymentCommand cmd) {
        def user = userService.loggedInUser
        def order = Order.get(cmd.orderId)

        if (order.total() == 0) {
            log.info("Order is free, skipping to process for order ${order}")
            orderStatusService.complete(order, user)
            redirect(action: "process", params: [orderId: order.id])
            return
        }

        if (!cmd.validate()) {
            // Custom log and error when missing payment method.
            if (cmd.errors.getFieldErrors("method").any()) {
                log.error "Unable to pay membership because missing payment method: $cmd.errors"
                render view: "showError", model: [message: message(code: 'paymentController.process.errors.missingPaymentMethod')]
                return
            }
            log.error "Unable to make bookings because of parameters errors: $cmd.errors"
            render view: "showError", model: [message: message(code: 'paymentController.process.errors.couldNotProcess')]
            return

        } else {
            if (cmd.allowRecurring) {
                order.metadata[Order.META_ALLOW_RECURRING] = "true"
                order.save()
            }

            def method = cmd.method as PaymentMethod

            if (method.isUsingPaymentGatewayMethod()) {
                redirect(getPaymentProviderParameters(method, order, user))
            }
        }
    }

    @Override
    protected void processArticle(Order order) throws ArticleCreationException {
        def membershipType = MembershipType.get(order.metadata.membershipTypeId)

        if (membershipType && order.metadata.customerId) {
            def customer = Customer.get(order.metadata.customerId)
            def purchaseUpcomingMembership = order.metadata.purchaseUpcomingMembership?.toBoolean()
            def startDate = new LocalDate(order.metadata.startDate)
            def membership

            try {
                membership = memberService.requestMembership(
                        customer, membershipType, order, startDate)
            } catch (Throwable t) {
                log.error(t)
                throw new ArticleCreationException(message(code: 'membershipPaymentController.process.errors.creationError') as String)
            }

            if (!membership) throw new ArticleCreationException(message(code: 'membershipPaymentController.process.errors.creationError') as String)
            membership.save()
            memberService.sendMembershipRequestNotification(membership, customer, order.metadata.facilityMessage, order)

            if (!purchaseUpcomingMembership) {
                def members = order.metadata.findAll { it.key.startsWith("member_") }
                if (members) {
                    def family = membersFamilyService.createFamily(membership)
                    members.each { k, mtid ->
                        def m = memberService.requestMembership(
                                Customer.get(Long.valueOf(k.substring(7))),
                                MembershipType.get(Long.valueOf(mtid)),
                                order, startDate)
                        if (m) {
                            memberService.sendMembershipRequestNotification(m, customer, order.metadata.facilityMessage, order)
                            membersFamilyService.addFamilyMember(m, family)
                        }
                    }
                }
            }
            log.info(LogHelper.formatOrder("before `redirect to finish` from MembershipPaymentController", params.orderId as Long))
            redirectToFinish(PaymentFlow.State.RECEIPT, params.long("orderId"), [orderId: params.orderId, membershipId: membership.id])
        } else {
            throw new IllegalStateException("Could not process order with id ${params.orderId}")
        }
    }

    @Override
    protected Order.Article getArticleType() {
        return Order.Article.MEMBERSHIP
    }

    def receipt() {
        def order = Order.get(params.orderId)
        def membership = memberService.getMembership(order)
        PaymentFlow paymentFlow = PaymentFlow.popInstance(session, order.id)

        render view: "receipt", model: [order: order, membership: membership, showPage: paymentFlow.showPage(), finishUrl: paymentFlow.finishUrl]
    }
}

@Validateable(nullable = true)
class MembershipPaymentCommand {

    def userService
    def springSecurityService

    Long id
    String orderId
    String method
    boolean savePaymentInformation
    boolean allowRecurring

    static constraints = {
        id(nullable: false)
        orderId(nullable: false)
        method(nullable: false)
    }
}