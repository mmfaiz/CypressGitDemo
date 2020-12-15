package com.matchi.price

import com.google.common.collect.Lists
import com.matchi.Court
import com.matchi.Customer
import com.matchi.CustomerService
import com.matchi.Facility
import com.matchi.FacilityService
import com.matchi.Slot
import com.matchi.User
import com.matchi.membership.Membership
import com.matchi.membership.MembershipType
import com.matchi.orders.Order
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.codehaus.groovy.grails.commons.InstanceFactoryBean
import org.joda.time.LocalDate
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * See the API for {@link grails.test.mixin.domain.DomainClassUnitTestMixin} for usage instructions
 */
@TestFor(MemberTypePriceCondition)
@Mock([User, Customer, Facility, Slot, Court, Membership, MembershipType])
class MemberTypePriceConditionTests {

    MemberTypePriceCondition condition
    Slot slot
    Court court
    Membership membership
    MembershipType membershipType1
    MembershipType membershipType2
    User user
    Customer customer
    Facility facility
    Facility anotherFacility
    def mockFacilityService
    def mockCustomerService

    @Before
    public void setUp() {

        facility = new Facility(id: 1l).save(validate: false)
        anotherFacility = new Facility(id: 2l).save(validate: false)

        user = new User(id: 1l).save(validate: false)
        customer = new Customer(id: 1l, number: 1, facility: facility, user: user, firstname: "User", lastname: "Last").save(validate: false)

        membershipType1 = new MembershipType(id: 1l, facility: facility, name: "Type1").save(validate: false)
        membershipType2 = new MembershipType(id: 2l, facility: facility, name: "Type2").save(validate: false)
        membership = new Membership(id: 1l, customer: customer, type: membershipType1,
                startDate: new LocalDate(), order: new Order(status: Order.Status.COMPLETED),
                gracePeriodEndDate: new LocalDate()).save(validate: false)

        court = new Court()
        court.facility = facility
        court.save(validate: false)

        slot = new Slot()
        slot.court = court
        slot.save(validate: false)

        condition = new MemberTypePriceCondition(membershipTypes: [membershipType1])

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
    void testReturnsTrueIfCustomerHasMemberType() {
        assert condition.accept(slot, customer)
    }
    @Test
    void testReturnsTrueIfCustomerHasIncorrentMemberType() {
        customer.membership.type = membershipType2
        assert !condition.accept(slot, customer)
    }
    @Test
    void testReturnsFalseIfCustomerDoesNotHaveMemberType() {
        customer.membership.type = null
        assert !condition.accept(slot, customer)
    }
    @Test
    void testReturnsFalseIfCustomerDoesNotHaveActiveMemberShip() {
        customer.membership.activated = false
        assert !condition.accept(slot, customer)
    }
    @Test
    void testReturnsFalseIfInCorrectFacility() {
        slot.court.facility = anotherFacility
        assert !condition.accept(slot, customer)
    }

    @Test
    void testMultipleMemberships() {
        def today = LocalDate.now()
        membership.startDate = today.minusDays(1)
        membership.endDate = today.minusDays(1)
        membership.gracePeriodEndDate = today.minusDays(1)
        def membership2 = new Membership(id: 1l, customer: customer, type: membershipType1,
                startDate: today, endDate: today, gracePeriodEndDate: today,
                order: new Order(status: Order.Status.NEW, price: 100)).save(validate: false)

        assert !condition.accept(slot, customer)

        membership2.startingGracePeriodDays = 10

        assert condition.accept(slot, customer)

        membership.gracePeriodEndDate = today
        membership2.startingGracePeriodDays = 0

        assert condition.accept(slot, customer)

        membership.order.price = 100
        membership.order.status = Order.Status.NEW

        assert !condition.accept(slot, customer)
    }
}
