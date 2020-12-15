package com.matchi

import com.matchi.FacilityProperty.FacilityPropertyKey
import com.matchi.membership.Membership
import com.matchi.membership.MembershipFamily
import com.matchi.membership.MembershipType
import com.matchi.orders.Order
import com.matchi.orders.OrderPayment
import com.matchi.orders.CashOrderPayment
import grails.test.mixin.Mock
import grails.test.mixin.TestFor

/**
 * @author Sergei Shushkevich
 */
@TestFor(MembershipPaymentService)
@Mock([Order])
class MembershipPaymentServiceTests {

    void testCreateMembershipPaymentOrder() {
        def user = new User()
        def mt = new MembershipType(facility: new Facility(name: "abc"), name: "junior", price: 100)
        mt.id = 1

        def order = service.createMembershipPaymentOrder(user, mt)

        assert order
        assert Order.Article.MEMBERSHIP == order.article
        assert mt.createOrderDescription() == order.description
        assert order.metadata
        assert mt.id.toString() == order.metadata.membershipTypeId
        assert user == order.user
        assert user == order.issuer
        assert mt.facility == order.facility
        assert order.dateDelivery
        assert mt.price == order.price
        assert mt.facility.vat == order.vat
        assert "web" == order.origin
        assert 1 == Order.count()
    }

    void testCreateFamilyMembershipPaymentOrder() {
        def user = new User()
        def facility = new Facility(name: "abc")
        def customer = new Customer(facility: facility)
        def mt = new MembershipType(facility: facility, name: "junior", price: 100)
        mt.id = 1
        def memberships = [
            new Membership(type: mt, order: new Order()),
            new Membership(type: mt, order: new Order())
        ]

        def order = service.createFamilyMembershipPaymentOrder(memberships, user, customer)

        assert order
        assert Order.Article.MEMBERSHIP == order.article
        assert mt.id.toString() == order.metadata.membershipTypeId
        assert user == order.user
        assert user == order.issuer
        assert mt.facility == order.facility
        assert (mt.price * 2) == order.price
        assert "web" == order.origin
        assert 1 == Order.count()
    }

    void testCreateFamilyMembershipPaymentOrder2() {
        def user = new User()
        def facility = new Facility(name: "abc")
        def mt = new MembershipType(facility: facility, name: "junior", price: 100)
        mt.id = 1
        def customer1 = new Customer(facility: facility)
        customer1.id = 100L
        def membership1 = new Membership(customer: customer1, type: mt, order: new Order())
        def customer2 = new Customer(facility: facility)
        customer2.id = 200L
        def membership2 = new Membership(customer: customer2, type: mt, order: new Order())
        def family = new MembershipFamily(contact: customer1, members: [membership1, membership2])
        membership1.family = family
        membership2.family = family

        def order = service.createFamilyMembershipPaymentOrder(family, user, customer1)

        assert order
        assert Order.Article.MEMBERSHIP == order.article
        assert mt.id.toString() == order.metadata.membershipTypeId
        assert user == order.user
        assert user == order.issuer
        assert mt.facility == order.facility
        assert 200 == order.price
        assert "web" == order.origin
        assert 1 == Order.count()

        facility.facilityProperties = [
                new FacilityProperty(key: FacilityPropertyKey.FACILITY_MEMBERSHIP_FAMILY_MAX_AMOUNT.toString(), value: "150")]

        order = service.createFamilyMembershipPaymentOrder(family, user, customer1)

        assert order
        assert 150 == order.price
        assert 2 == Order.count()

        membership2.order.price = 100
        membership2.order.status = Order.Status.COMPLETED
        membership2.order.payments = [new CashOrderPayment(status: OrderPayment.Status.CAPTURED, amount: 100)]

        order = service.createFamilyMembershipPaymentOrder(family, user, customer1)

        assert order
        assert 50 == order.price
        assert 3 == Order.count()

        facility.facilityProperties = [
                new FacilityProperty(key: FacilityPropertyKey.FACILITY_MEMBERSHIP_FAMILY_MAX_AMOUNT.toString(), value: "50")]

        order = service.createFamilyMembershipPaymentOrder(family, user, customer1)

        assert !order
        assert 3 == Order.count()
    }
}
