package com.matchi

import com.matchi.membership.Membership
import com.matchi.membership.MembershipFamily
import com.matchi.membership.MembershipType
import com.matchi.orders.Order
import com.matchi.price.Price
import grails.compiler.GrailsCompileStatic

/**
 * @author Sergei Shushkevich
 */
@GrailsCompileStatic
class MembershipPaymentService {

    Order createMembershipPaymentOrder(User user, MembershipType membershipType,
            String origin = Order.ORIGIN_WEB, User issuer = null, Customer customer = null) {
        def order = createOrder(user, issuer, origin, customer)

        if (membershipType) {
            order.facility = membershipType.facility
            order.description = membershipType.createOrderDescription()
            order.metadata = [membershipTypeId: membershipType.id.toString()]
            Amount amount = membershipType.toAmount()
            order.price = amount.amount
            order.vat = amount.VAT
        } else {
            order.facility = customer?.facility
            order.description = customer?.facility?.name
            order.price = 0
            order.vat = 0
        }

        order.save(failOnError: true)
    }

    Order createFamilyMembershipPaymentOrder(Membership contactMembership, User user, Customer customer) {
        def memberships = [contactMembership]
        def membersNotContact = contactMembership.family.membersNotContact
        if (membersNotContact) {
            memberships.addAll(membersNotContact)
        }
        createFamilyMembershipPaymentOrder(memberships, user, customer)
    }

    // TODO: refactor it later (when all family membership specific stories will be merged into develop; MW-3903, MW-3931)
    //       use only one method with MembershipFamily argument below (which also takes into account paid family memberships)
    //       if possible and if it makes sense
    Order createFamilyMembershipPaymentOrder(List<Membership> memberships, User user, Customer customer) {
        def order = createOrder(user, user, Order.ORIGIN_WEB, customer)
        order.facility = customer.facility
        order.description = memberships[0].type ?
                memberships[0].type.createOrderDescription() : customer.facility.name
        order.metadata = [membershipTypeId: memberships[0].type?.id?.toString()]

        order.price = 0
        def membershipsToUpdate = []
        memberships.each { fm ->
            if (!fm.paid && fm.type) {
                order.price += fm.type.price
                membershipsToUpdate << fm.id
            }
        }
        order.price = Math.min(order.price.toLong(),
                customer.facility.familyMaxPrice)
        order.vat = Price.calculateVATAmount(order.price.toLong(),
                new Double(customer.facility.vat))
        order.metadata.membershipsToUpdate = membershipsToUpdate.join(",")

        order.save(failOnError: true)
    }

    Order createFamilyMembershipPaymentOrder(MembershipFamily family, User user, Customer customer) {
        def contactMembership = family.members.find {it.isFamilyContact()}
        def order = createOrder(user, user, Order.ORIGIN_WEB, customer)
        order.facility = customer.facility
        order.description = contactMembership.type ?
                contactMembership.type.createOrderDescription() : customer.facility.name
        order.metadata = [membershipTypeId: contactMembership.type?.id?.toString()]

        order.price = 0
        def paidAmount = 0
        def membershipsToUpdate = []
        family.members.each { fm ->
            if (fm.paid) {
                paidAmount += fm.order.getTotalAmountPaid()
            } else if (fm.type) {
                order.price += fm.type.price
                membershipsToUpdate << fm.id
            }
        }
        order.price = Math.min(order.price.toLong(),
                (customer.facility.familyMaxPrice - paidAmount).toLong())
        if (order.price <= 0) {
            return null
        }
        order.vat = Price.calculateVATAmount(order.price.toLong(),
                new Double(customer.facility.vat))
        order.metadata.membershipsToUpdate = membershipsToUpdate.join(",")

        order.save(failOnError: true)
    }

    private Order createOrder(User user, User issuer, String origin, Customer customer) {
        def order = new Order()
        order.article = Order.Article.MEMBERSHIP
        order.user = user
        order.issuer = issuer ?: user
        order.dateDelivery = new Date()
        order.origin = origin
        order.customer = customer
        order
    }
}
