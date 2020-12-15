package com.matchi

import com.matchi.requirements.Requirement
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.joda.time.DateTime
import org.joda.time.LocalDateTime
import org.junit.Before
import org.junit.Test

import static com.matchi.TestUtils.*
/**
 * See the API for {@link grails.test.mixin.domain.DomainClassUnitTestMixin} for usage instructions
 */
@TestFor(BookingRestriction)
@Mock([Customer, Facility, Region, Municipality, Slot, Sport, Court])
class BookingRestrictionSpec {

    Customer customer
    def mockRequirement
    Requirement requirement
    Slot slot

    @Before
    void setUp() {
        slot            = createSlot(createCourt(), new Date(), new Date() + 1)
        customer        = createCustomer()
        mockRequirement = mockFor(Requirement)
        requirement     = mockRequirement.createMock()
    }

    @Test
    void testRequirementsAreRun() {
        mockRequirement.demand.validate(1..1) { Customer c ->
            return true
        }

        domain.requirementProfiles      = [requirement].toSet()
        domain.validUntilMinBeforeStart = 0

        assert domain.accept(customer, createSlot(createCourt(), new Date() + 1, new Date() + 2))
        mockRequirement.verify()
    }

    @Test
    void testSlotStartIsNotValidatedIfStartMoreThanCondition() {
        mockRequirement.demand.validate(1..1) { Customer c ->
            return false
        }

        domain.requirementProfiles      = [requirement].toSet()
        domain.validUntilMinBeforeStart = 10

        slot.startTime = new DateTime().plusMinutes(60).toDate()
        assert !domain.accept(customer, slot)
        mockRequirement.verify()
    }

    @Test
    void testSlotStartIsValidatedIfLessThanCondition() {
        domain.validUntilMinBeforeStart = 10

        slot.startTime = new DateTime().plusMinutes(5).toDate()
        assert domain.accept(customer, slot)
    }

    @Test
    void testSlotStartIsValidatedIfSlotStartPassed() {
        slot.startTime = new DateTime().minusMinutes(5).toDate()
        assert domain.accept(customer, slot)
    }
}