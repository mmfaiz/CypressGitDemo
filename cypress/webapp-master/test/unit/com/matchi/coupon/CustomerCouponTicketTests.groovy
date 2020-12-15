package com.matchi.coupon

import grails.test.mixin.Mock
import grails.test.mixin.TestFor

/**
 * See the API for {@link grails.test.mixin.domain.DomainClassUnitTestMixin} for usage instructions
 */
@TestFor(CustomerCouponTicket)
@Mock([Coupon, GiftCard])
class CustomerCouponTicketTests {

    void testGetAmount() {
        Coupon coupon = new Coupon()
        domain.customerCoupon = new CustomerCoupon(coupon: coupon)
        assert domain.getAmount() == 1

        GiftCard giftCard = new GiftCard()
        BigDecimal value = new BigDecimal(150)
        domain.purchasedObjectPrice = value
        domain.customerCoupon = new CustomerCoupon(coupon: giftCard)
        assert domain.getAmount() == value
    }
}
