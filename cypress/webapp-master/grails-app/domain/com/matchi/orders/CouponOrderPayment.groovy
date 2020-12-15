package com.matchi.orders

import com.matchi.coupon.CustomerCouponTicket
/**
 * This Payment domain used for any Offer, not only for Coupon.
 *
 */
class CouponOrderPayment extends OrderPayment {
    private static final long serialVersionUID = 12L

    def couponService

    CustomerCouponTicket ticket

    @Override
    void refund(amount) {
        log.info("Refunding coupon ticket to user on payment ${id}")
        if(ticket && this.status.equals(OrderPayment.Status.CAPTURED)) {
            couponService.refundCustomerCoupon(ticket, this.amount.intValue())
            this.status = OrderPayment.Status.CREDITED
            this.setCredited(amount)
        }
    }

    @Override
    def getType() {
        return "Coupon"
    }

    @Override
    boolean allowLateRefund() {
        true
    }

    @Override
    boolean isRefundableTypeAndStatus() {
        this.isRefundable()
    }

    static constraints = {
        ticket nullable: true
    }

    static mapping = {
        discriminator "coupon"
    }
}
