package com.matchi.requirements

import com.matchi.Customer
import com.matchi.membership.Membership
import com.matchi.membership.MembershipType
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.joda.time.LocalDate
import org.junit.Before
import org.junit.Test
/**
 * See the API for {@link grails.test.mixin.domain.DomainClassUnitTestMixin} for usage instructions
 */
@TestFor(MemberTypeRequirement)
@Mock([Customer, Membership, MembershipType])
class MemberTypeRequirementSpec {

    Customer customer
    Membership membership
    MembershipType senior
    MembershipType junior

    @Before
    void setUp() {
        customer   = new Customer()
        membership = new Membership(
                startDate: new LocalDate(), gracePeriodEndDate: new LocalDate())
        senior     = new MembershipType(id: 1l, name: "Senior").save(validate: false)
        junior     = new MembershipType(id: 2l, name: "Junior").save(validate: false)

        customer.addToMemberships(membership)
    }

    @Test
    void testRequirementWithNullTypes() {
        assert !domain.validate(customer)
    }

    @Test
    void testRequirementWithEmptyTypeList() {
        domain.types = []

        assert !domain.validate(customer)
    }

    @Test
    void testRequirementTrue1() {
        membership.type = senior
        domain.types    = [ senior ]

        assert domain.validate(customer)
    }

    @Test
    void testRequirementTrue2() {
        membership.type = junior
        domain.types    = [ senior, junior ]

        assert domain.validate(customer)
    }

    @Test
    void testRequirementFalse1() {
        membership.type = junior
        domain.types    = [ senior ]

        assert !domain.validate(customer)
    }
}
