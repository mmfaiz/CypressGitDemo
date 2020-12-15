package com.matchi.requirements

import com.matchi.Customer
import grails.test.mixin.TestFor
import org.junit.Before

/**
 * See the API for {@link grails.test.mixin.domain.DomainClassUnitTestMixin} for usage instructions
 */
@TestFor(RequirementProfile)
class RequirementProfileSpec {

    RequirementProfile domain

    @Before
    void setUp() {
        domain = new RequirementProfile()
    }

    void testRequirementsFulfilled() {
        def mockCustomer = mockFor(Customer)
        def mockRequirement = mockFor(Requirement)
        mockRequirement.demand.validate(1..1) { Customer c ->
            return true
        }

        Requirement requirement = mockRequirement.createMock()
        domain.requirements = [requirement].toSet()

        assert domain.validate(mockCustomer.createMock())

        mockCustomer.verify()
        mockRequirement.verify()
    }

    void testRequirementsNotFulfilled() {
        def mockCustomer = mockFor(Customer)
        def mockRequirement = mockFor(Requirement)
        mockRequirement.demand.validate(1) { Customer c ->
            return true
        }

        def mockRequirement2 = mockFor(Requirement)
        mockRequirement2.demand.validate(1) { Customer c ->
            return false
        }

        Requirement requirement = mockRequirement.createMock()
        Requirement requirement2 = mockRequirement2.createMock()
        domain.requirements = [requirement, requirement2].toSet()

        assert !domain.validate(mockCustomer.createMock())
    }
}
