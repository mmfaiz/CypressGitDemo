package com.matchi.subscriptionredeem

import com.matchi.Customer
import com.matchi.User
import com.matchi.coupon.Coupon
import com.matchi.coupon.CustomerCoupon
import com.matchi.coupon.CustomerCouponTicket
import com.matchi.price.Price
import com.matchi.subscriptionredeem.redeemstrategy.CouponRedeemStrategy
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.junit.Before
import org.junit.Test

@TestFor(CouponRedeemStrategy)
@Mock([Coupon, CustomerCoupon, CustomerCouponTicket, User, Customer,])
class CouponRedeemStrategyTests {

    CouponRedeemStrategy couponRedeemStrategy
    Coupon coupon

    @Before
    void setUp() {
        coupon = new Coupon(id: 1l, nrOfTickets: 10).save(validate: false)
        couponRedeemStrategy = new CouponRedeemStrategy(coupon: coupon).save(validate: false)
    }


    @Test
    void testTypeIsCorrect() {
        assert couponRedeemStrategy.type == "COUPON"
    }

    @Test
    void testRedeemAddsCustomerCouponToCustomer() {
        User user = new User(id: 1l)
        Customer customer = new Customer(id: 1l)
        Price price = new Price()

        Coupon c = couponRedeemStrategy.redeem(user, customer, price, "DESC", false)

        assert c
        assert customer.customerCoupons.size() == 1
    }
}
