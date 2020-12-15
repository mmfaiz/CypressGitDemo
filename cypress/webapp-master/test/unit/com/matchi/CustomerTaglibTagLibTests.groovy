package com.matchi

import com.matchi.orders.Order
import com.matchi.membership.Membership
import grails.test.mixin.TestFor
import grails.test.mixin.Mock
import org.joda.time.LocalDate
import spock.lang.Specification

import static com.matchi.TestUtils.*

/**
 * @author Sergei Shushkevich
 */
@TestFor(CustomerTaglibTagLib)
@Mock([Membership, Customer, Facility, User, Region, Municipality, Order])
class CustomerTaglibTagLibTests extends Specification {

    void testMembershipStatus() {
        def today = new LocalDate()
        def m1 = new Membership(startDate: today, endDate: today, gracePeriodEndDate: today,
                order: new Order(price: 0, status: Order.Status.COMPLETED))
        def m2 = new Membership(activated: false, cancel: true, startDate: today.minusDays(1),
                endDate: today.minusDays(1), gracePeriodEndDate: today,
                order: new Order(price: 0, status: Order.Status.COMPLETED))
        def m3 = new Membership(activated: false, startDate: today,
                endDate: today, gracePeriodEndDate: today,
                order: new Order(price: 0, status: Order.Status.COMPLETED))
        def m4 = new Membership(cancel: true, startDate: today,
                endDate: today, gracePeriodEndDate: today,
                order: new Order(price: 0, status: Order.Status.COMPLETED))
        def m5 = new Membership(activated: false, cancel: true, startDate: today,
                endDate: today, gracePeriodEndDate: today,
                order: new Order(price: 0, status: Order.Status.COMPLETED))
        def m6 = new Membership(startDate: today, endDate: today, gracePeriodEndDate: today,
                order: new Order(price: 0, status: Order.Status.NEW))
        def m7 = new Membership(cancel: true, startDate: today, endDate: today,
                gracePeriodEndDate: today, order: new Order(price: 0, status: Order.Status.NEW))
        def m8 = new Membership(activated: false, startDate: today, endDate: today,
                gracePeriodEndDate: today, order: new Order(price: 0, status: Order.Status.NEW))
        messageSource.addMessage("membership.status.PAID", new Locale("en"), "paid status")
        messageSource.addMessage("membership.status.UNPAID", new Locale("en"), "status unpaid")
        messageSource.addMessage("membership.status.PENDING", new Locale("en"), "pending status")

        expect:
        tagLib.membershipStatus(membership: null).toString() == ""
        tagLib.membershipStatus(membership: m1).toString().contains("paid status")
        tagLib.membershipStatus(membership: m2).toString().contains("pending status")
        tagLib.membershipStatus(membership: m3).toString().contains("pending status")
        tagLib.membershipStatus(membership: m4).toString().contains("paid status")
        tagLib.membershipStatus(membership: m4).toString().contains("label-warning")
        tagLib.membershipStatus(membership: m5).toString().contains("pending status")
        tagLib.membershipStatus(membership: m6).toString().contains("status unpaid")
        tagLib.membershipStatus(membership: m7).toString().contains("status unpaid")
        !tagLib.membershipStatus(membership: m7).toString().contains("label-warning")
        tagLib.membershipStatus(membership: m8).toString().contains("pending status")
    }

    void testMemberBadge() {
        def today = new LocalDate()
        def customer1 = createCustomer()
        def customer2 = createCustomer()
        createMembership(customer2)
        def customer3 = createCustomer()
        createMembership(customer3, today.minusDays(1), today.minusDays(1), today)
        messageSource.addMessage("default.member.label", new Locale("en"), "is member")

        expect:
        tagLib.memberBadge(customer: customer1).toString() == ""
        tagLib.memberBadge(customer: customer2).toString().contains("is member")
        tagLib.memberBadge(customer: customer3).toString().contains("is member")
    }
}
