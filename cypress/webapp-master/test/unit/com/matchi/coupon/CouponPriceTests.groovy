package com.matchi.coupon

import com.matchi.price.PriceListCustomerCategory
import grails.test.mixin.TestFor

/**
 * @author Sergei Shushkevich
 */
@TestFor(CouponPrice)
class CouponPriceTests {

    void testConstraints() {
        mockForConstraintsTests(CouponPrice)

        def obj = new CouponPrice()
        assert !obj.validate()
        assert 3 == obj.errors.errorCount
        assert "nullable" == obj.errors.price
        assert "nullable" == obj.errors.coupon
        assert "nullable" == obj.errors.customerCategory

        obj = new CouponPrice(price: -1)
        assert !obj.validate()
        assert "min" == obj.errors.price

        obj = new CouponPrice(price: 0, coupon: new Coupon(), customerCategory: new PriceListCustomerCategory())
        assert obj.validate()
    }
}
