package com.matchi.requirements

import com.matchi.Customer
import grails.test.mixin.*

/**
 * See the API for {@link grails.test.mixin.domain.DomainClassUnitTestMixin} for usage instructions
 */
@TestFor(IsActiveMemberRequirement)
class IsActiveMemberRequirementSpec {

    void testActiveMembershipRequired() {
        def mockCustomer = mockFor(Customer)
        Customer customer = mockCustomer.createMock()

        mockCustomer.demand.hasActiveMembership(1..1) { ->
            return true
        }

        domain.membershipRequired = true

        assert domain.validate(customer)
        mockCustomer.verify()
    }

    void testActiveMembershipNotRequiredReturnsFalse() {
        def mockCustomer = mockFor(Customer)
        Customer customer = mockCustomer.createMock()

        mockCustomer.demand.hasActiveMembership(1..1) { ->
            return true
        }

        domain.membershipRequired = false

        assert !domain.validate(customer)
        mockCustomer.verify()
    }

    void testActiveMembershipNotRequiredReturnsTrue() {
        def mockCustomer = mockFor(Customer)
        Customer customer = mockCustomer.createMock()

        mockCustomer.demand.hasActiveMembership(1..1) { ->
            return false
        }

        domain.membershipRequired = false

        assert domain.validate(customer)
        mockCustomer.verify()
    }

    void testActiveMembershipRequiredReturnsFalse() {
        def mockCustomer = mockFor(Customer)
        Customer customer = mockCustomer.createMock()

        mockCustomer.demand.hasActiveMembership(1..1) { ->
            return false
        }

        domain.membershipRequired = true

        assert !domain.validate(customer)
        mockCustomer.verify()
    }
}
