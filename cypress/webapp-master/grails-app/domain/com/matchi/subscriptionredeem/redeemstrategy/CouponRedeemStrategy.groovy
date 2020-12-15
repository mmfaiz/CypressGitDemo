package com.matchi.subscriptionredeem.redeemstrategy
import com.matchi.Customer
import com.matchi.User
import com.matchi.coupon.Coupon
import com.matchi.coupon.CustomerCoupon
import com.matchi.coupon.CustomerCouponTicket
import com.matchi.price.Price
import grails.util.Holders
import org.joda.time.LocalDate

class CouponRedeemStrategy extends RedeemStrategy {

    Coupon coupon

    static constraints = {
        coupon(nullable: false)
    }

    String getType() {
        return "COUPON"
    }

    static transients = ['type']

    @Override
    void populate(def params) {
        if (params.couponId) {
            this.coupon = Coupon.findById(params.long("couponId"))
        }
    }

    @Override
    def redeem(User user, Customer customer, Price price, String slotdescription, Boolean fullRedeem) {
        CustomerCoupon oldCoupon = getOldCoupon(customer)

        if (oldCoupon) {
            oldCoupon.addTicket(user, 1,
                    CustomerCouponTicket.Type.SUBSCRIPTION_REFUND, slotdescription)
            return oldCoupon.coupon
        }

        return CustomerCoupon.link(user, customer, this.coupon, this.coupon.nrOfTickets,
                this.coupon.expireDate?.toLocalDate(), "",
                CustomerCouponTicket.Type.SUBSCRIPTION_REFUND, slotdescription)?.coupon
    }

    private CustomerCoupon getOldCoupon(Customer customer) {
        CustomerCoupon.oldCoupon(customer, coupon,
                coupon.expireDate?.toLocalDate()).get()
    }
}
