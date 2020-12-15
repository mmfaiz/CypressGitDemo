package com.matchi.subscriptionredeem.redeemstrategy

import com.matchi.Customer
import com.matchi.Facility
import com.matchi.User
import com.matchi.coupon.Coupon
import com.matchi.coupon.CustomerCoupon
import com.matchi.price.Price
import org.apache.commons.lang.RandomStringUtils
import org.joda.time.LocalDate

import static com.matchi.TestUtils.*

/**
 * @author Sergei Shushkevich
 */
class CouponRedeemStrategyIntegrationTests extends GroovyTestCase {

    Coupon coupon
    CouponRedeemStrategy couponRedeemStrategy
    Customer customer
    Facility facility
    User user

    @Override
    void setUp() {
        user = createUser("${RandomStringUtils.randomAlphabetic(10)}@matchi.se")
        facility = createFacility()
        customer = createCustomer(facility)
        coupon = new Coupon(facility: facility, name: "test", nrOfTickets: 1,
                nrOfDaysValid: 365).save(failOnError: true)
        couponRedeemStrategy = new CouponRedeemStrategy(coupon: coupon)
                .save(failOnError: true, flush: true)
    }

    void testRedeemAppendCouponIfExists() {
        def cc = new CustomerCoupon(customer: customer, coupon: coupon,
                createdBy: user, nrOfTickets: 1, expireDate: LocalDate.now().plusDays(365))
                .save(failOnError: true)

        def c = couponRedeemStrategy.redeem(user, customer, new Price(), null, false)

        assert c
        assert CustomerCoupon.countByCustomer(customer) == 1
        cc.refresh()
        assert cc.nrOfTickets == 2
        assert cc.expireDate == LocalDate.now().plusDays(365)
    }

    void testRedeemCreateNewCouponIfExistsWithDiffExpDate() {
        def cc = new CustomerCoupon(customer: customer, coupon: coupon,
                createdBy: user, nrOfTickets: 1, expireDate: LocalDate.now().plusDays(100))
                .save(failOnError: true)

        def c = couponRedeemStrategy.redeem(user, customer, new Price(), null, false)

        assert c
        assert CustomerCoupon.countByCustomer(customer) == 2
        cc.refresh()
        assert cc.nrOfTickets == 1
        assert cc.expireDate == LocalDate.now().plusDays(100)
        def cc2 = CustomerCoupon.findByCustomerAndExpireDate(customer, LocalDate.now().plusDays(365))
        assert cc2 != null
        assert cc2.nrOfTickets == 1
    }

    void testRedeemCreateNewCouponIfUsed() {
        def cc1 = new CustomerCoupon(customer: customer, coupon: coupon,
                createdBy: user, nrOfTickets: 0, expireDate: LocalDate.now().plusDays(100))
                .save(failOnError: true)

        def c = couponRedeemStrategy.redeem(user, customer, new Price(), null, false)

        assert c
        assert CustomerCoupon.countByCustomer(customer) == 2
        cc1.refresh()
        assert cc1.nrOfTickets == 0
        def cc2 = CustomerCoupon.findByCustomerAndIdNotEqual(customer, cc1.id)
        assert cc2.nrOfTickets == 1
        assert cc2.expireDate == LocalDate.now().plusDays(365)
    }

    void testRedeemCreateNewCouponIfExpired() {
        def cc1 = new CustomerCoupon(customer: customer, coupon: coupon,
                createdBy: user, nrOfTickets: 1, expireDate: LocalDate.now().minusDays(1))
                .save(failOnError: true)

        def c = couponRedeemStrategy.redeem(user, customer, new Price(), null, false)

        assert c
        assert CustomerCoupon.countByCustomer(customer) == 2
        cc1.refresh()
        assert cc1.nrOfTickets == 1
        def cc2 = CustomerCoupon.findByCustomerAndIdNotEqual(customer, cc1.id)
        assert cc2.nrOfTickets == 1
        assert cc2.expireDate == LocalDate.now().plusDays(365)
    }

    void testRedeemCreateNewCouponIfExpiresSoon() {
        def cc1 = new CustomerCoupon(customer: customer, coupon: coupon,
                createdBy: user, nrOfTickets: 1, expireDate: LocalDate.now().plusDays(10))
                .save(failOnError: true)

        def c = couponRedeemStrategy.redeem(user, customer, new Price(), null, false)

        assert c
        assert CustomerCoupon.countByCustomer(customer) == 2
        cc1.refresh()
        assert cc1.nrOfTickets == 1
        def cc2 = CustomerCoupon.findByCustomerAndIdNotEqual(customer, cc1.id)
        assert cc2.nrOfTickets == 1
        assert cc2.expireDate == LocalDate.now().plusDays(365)
    }

    void testRedeemCreateNewCouponIfLocked() {
        def cc1 = new CustomerCoupon(customer: customer, coupon: coupon, dateLocked: new Date(),
                createdBy: user, nrOfTickets: 1, expireDate: LocalDate.now().plusDays(100))
                .save(failOnError: true)

        def c = couponRedeemStrategy.redeem(user, customer, new Price(), null, false)

        assert c
        assert CustomerCoupon.countByCustomer(customer) == 2
        cc1.refresh()
        assert cc1.nrOfTickets == 1
        assert cc1.dateLocked
        def cc2 = CustomerCoupon.findByCustomerAndIdNotEqual(customer, cc1.id)
        assert cc2.nrOfTickets == 1
        assert cc2.expireDate == LocalDate.now().plusDays(365)
        assert !cc2.dateLocked
    }
}