package com.matchi.price

import com.google.common.collect.Lists
import com.matchi.*
import com.matchi.membership.Membership
import com.matchi.orders.Order
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.codehaus.groovy.grails.commons.InstanceFactoryBean
import org.joda.time.LocalDate
import org.junit.After
import org.junit.Before
import org.junit.Test

@TestFor(MemberPriceCondition)
@Mock([User, Customer, Facility, Slot, Court, Membership])
class MemberPriceConditionTests {

    MemberPriceCondition condition
    Slot slot
    Court court
    Membership membership
    User user
    Customer customer
    Facility facility
    Facility anotherFacility
    def mockFacilityService
    def mockCustomerService

    @Before
    public void setUp() {
        facility = new Facility(id: 1).save(validate: false)
        anotherFacility = new Facility(id: 2).save(validate: false)

        user = new User(id: 1).save(validate: false)
        customer = new Customer(id: 1, number: 1, facility: facility, user: user).save(validate: false)

        membership = new Membership(customer: customer,
                startDate: new LocalDate(), gracePeriodEndDate: new LocalDate(),
                order: new Order(status: Order.Status.COMPLETED)).save(validate: false)

        court = new Court()
        court.facility = facility
        court.save(validate: false)

        slot = new Slot()
        slot.court = court
        slot.save(validate: false)

        condition = new MemberPriceCondition()

        mockFacilityService = mockFor(FacilityService)
        mockCustomerService = mockFor(CustomerService)
        defineBeans {
            facilityService(InstanceFactoryBean, mockFacilityService.createMock(), FacilityService)
            customerService(InstanceFactoryBean, mockCustomerService.createMock(), CustomerService)
        }
        this.mockFacilityService.demand.getAllHierarchicalFacilities(1..100) { f ->
            return Lists.asList(f)
        }
        this.mockCustomerService.demand.findHierarchicalUserCustomers(1..100) { c ->
            return Lists.asList(c)
        }
    }

    @After
    public void tearDown() {

    }
    @Test
    void testReturnsTrueIfCustomerIsMember() {
        assert condition.accept(slot, customer)
    }
    @Test
    void testReturnsTrueIfCustomerMembershipNotActive() {
        membership.activated = false
        assert !condition.accept(slot, customer)
    }
    @Test
    void testReturnsFalseIfCustomerIsNotMember() {
        slot.court.facility = anotherFacility
        assert !condition.accept(slot, customer)
    }
}
