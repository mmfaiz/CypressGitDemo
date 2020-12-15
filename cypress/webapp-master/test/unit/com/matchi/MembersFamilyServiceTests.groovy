package com.matchi

import com.matchi.membership.Membership
import com.matchi.membership.MembershipFamily
import com.matchi.orders.Order
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.junit.Before

import static com.matchi.TestUtils.*

@TestFor(MembersFamilyService)
@Mock([ MembershipFamily, Membership, Customer, Facility, User, Order, Region, Municipality ])
class MembersFamilyServiceTests {

    Membership membership

    @Before
    void setUp() {
        membership = createMembership(createCustomer())
    }

    void testCreateFamilyCreatesFamily() {
        def family = service.createFamily(membership)

        assert family.members.contains(membership)
        assert family.contact == membership.customer
    }

    void testAddFamilyMemberAddsMember() {
        Customer c = createCustomer()
        Membership m = createMembership(c)

        def family = service.createFamily(membership)
        service.addFamilyMember(m, family)

        assert family.members.size() == 2
        assert family.members.contains(m)
        assert family.contact != c
    }

    void testRemoveFamilyMemberRemovesMember() {
        Customer c = createCustomer()
        Membership m = createMembership(c)

        def family = service.createFamily(membership)
        service.addFamilyMember(m, family)
        service.removeFamilyMember(m)

        assert family.members.size() == 1
        assert !family.members.contains(m)
    }

    void testRemoveFamilyRemovesFamilyMembers() {
        Customer c = createCustomer()
        Membership m = createMembership(c)

        def family = service.createFamily(membership)
        service.addFamilyMember(m, family)
        service.removeFamily(family)

        assert !membership.family
        assert !m.family
    }
}
