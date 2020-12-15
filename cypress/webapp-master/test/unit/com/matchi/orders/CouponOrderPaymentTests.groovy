package com.matchi.orders



import grails.test.mixin.*
import org.junit.*

/**
 * See the API for {@link grails.test.mixin.domain.DomainClassUnitTestMixin} for usage instructions
 */
@TestFor(CouponOrderPayment)
class CouponOrderPaymentTests {

    @Test
    void testAllowLateRefund() {
        assert domain.allowLateRefund()
    }
}
