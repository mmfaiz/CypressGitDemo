package com.matchi.coupon

import com.google.common.collect.Lists
import com.matchi.*
import com.matchi.conditions.SlotCondition
import com.matchi.membership.Membership
import com.matchi.orders.Order
import com.matchi.price.CustomerGroupPriceCondition
import com.matchi.price.MemberPriceCondition
import com.matchi.price.PriceListCustomerCategory
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.codehaus.groovy.grails.commons.InstanceFactoryBean
import org.junit.Before

import static com.matchi.TestUtils.*

@TestFor(Coupon)
@Mock([Coupon, CouponConditionGroup, Facility, Municipality, Region, PriceListCustomerCategory,
CouponPrice, Group, Customer, Offer, Membership, User, Order])
class CouponTests {

    Coupon coupon
    def slot
    def facility
    def category
    def mockFacilityService
    def mockCustomerService

    @Before
    void setUp() {
        facility = createFacility()
        category = new PriceListCustomerCategory(facility: facility, name: "test").save(failOnError: true)
        coupon = createCoupon(facility)
        slot   = new Slot()

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

    void testAcceptReturnsTrueIfContainsValidConditionGroups() {
        coupon.addToCouponConditionGroups(mockConditionGroup(true))
        assert coupon.accept(slot)
    }

    void testAcceptReturnsTrueIfContainsValidAndInvalidConditionGroups() {
        coupon.addToCouponConditionGroups(mockConditionGroup(true))
        coupon.addToCouponConditionGroups(mockConditionGroup(false))
        assert coupon.accept(slot)
    }

    void testAcceptReturnsFalseIfContainsNonValidCondition() {
        coupon.addToCouponConditionGroups(mockConditionGroup(false))
        assert !coupon.accept(slot)
    }

    void testAcceptReturnsTrueIfNoGroups() {
        assert coupon.accept(slot)
    }

    void testToAmountWithZeroPrice() {
        new CouponPrice(price: 0, coupon: coupon, customerCategory: category).save(failOnError: true)
        Amount amount = coupon.toAmount(null)
        assert amount.amount == 0
        assert amount.VAT == 0
    }

    void testToAmountWithVAT() {
        facility.vat = 25
        new CouponPrice(price: 100, coupon: coupon, customerCategory: category).save(failOnError: true)
        Amount amount = coupon.toAmount(null)
        assert amount.amount == 100
        assert amount.VAT == 20
    }

    void testToAmountWithoutVAT() {
        facility.vat = 0
        new CouponPrice(price: 100, coupon: coupon, customerCategory: category).save(failOnError: true)
        Amount amount = coupon.toAmount(null)
        assert amount.amount == 100
        assert amount.VAT == 0
    }

    void testGetPrice() {
        def group = new Group(facility: facility, name: "group1").save(failOnError: true)
        category.conditions = [new MemberPriceCondition()]
        def category2 = new PriceListCustomerCategory(facility: facility, name: "test2").save(failOnError: true)
        category2.conditions = [new CustomerGroupPriceCondition(groups: [group])]
        new CouponPrice(price: 200, coupon: coupon, customerCategory: category).save(failOnError: true)
        new CouponPrice(price: 100, coupon: coupon, customerCategory: category2).save(failOnError: true)

        assert 0 == coupon.getPrice(null)

        def customer = createCustomer(facility)
        assert 0 == coupon.getPrice(customer)

        createMembership(customer)
        assert 200 == coupon.getPrice(customer)

        customer.customerGroups = [new CustomerGroup(group: group)]
        assert 100 == coupon.getPrice(customer)
    }

    private def mockConditionGroup(def valid) {
        return new MockCouponConditionGroup(valid)
    }

    public class MockCouponConditionGroup extends CouponConditionGroup {
        def valid

        MockCouponConditionGroup(valid) {
            this.valid = valid
        }

        @Override
        def accept(Slot slot) {
            return valid
        }
    }



}

public class ValidCondition extends SlotCondition {
    @Override
    boolean accept(Slot slot) {
        return true
    }
}

public class NotValidCondition extends SlotCondition {
    @Override
    boolean accept(Slot slot) {
        return false
    }
}
