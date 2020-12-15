package com.matchi.subscriptionredeem.redeemstrategy

import com.matchi.Customer
import com.matchi.User
import com.matchi.coupon.GiftCard
import com.matchi.coupon.CustomerCoupon
import com.matchi.coupon.CustomerCouponTicket
import com.matchi.price.Price

class GiftCardRedeemStrategy extends RedeemStrategy {

    GiftCard giftCard
    Long amount = 0l
    InvoiceRowRedeemStrategy.RedeemAmountType redeemAmountType

    static constraints = {
        giftCard(nullable: false)
    }

    String getType() {
        return "GIFT_CARD"
    }

    static transients = ['type']

    @Override
    void populate(def params) {
        this.properties["amount", "redeemAmountType"] = params
        if (params.giftCardId) {
            this.giftCard = GiftCard.findById(params.long("giftCardId"))
        }
    }

    @Override
    def redeem(User user, Customer customer, Price price, String slotdescription, Boolean fullRedeem) {
        def redeemTickets = price.price.intValue()
        if (redeemTickets == 0) {
            return null
        }

        if (!fullRedeem) {
            switch (redeemAmountType) {
                case InvoiceRowRedeemStrategy.RedeemAmountType.PERCENTAGE_BACK:
                    def priceReduction = amount / 100
                    redeemTickets = (price.price * priceReduction).intValue()
                    break
                case InvoiceRowRedeemStrategy.RedeemAmountType.PRICE_REDUCTION_BACK:
                    redeemTickets = (price.price - amount).intValue()
                    break
            }
        }

        CustomerCoupon oldCoupon = getOldCoupon(customer)

        if (oldCoupon) {
            oldCoupon.addTicket(user, redeemTickets,
                    CustomerCouponTicket.Type.SUBSCRIPTION_REFUND, slotdescription)
            return oldCoupon.coupon
        } else {
            return CustomerCoupon.link(user, customer, this.giftCard, redeemTickets,
                    this.giftCard.expireDate?.toLocalDate(), "",
                    CustomerCouponTicket.Type.SUBSCRIPTION_REFUND, slotdescription)?.coupon
        }
    }

    private CustomerCoupon getOldCoupon(Customer customer) {
        CustomerCoupon.oldCoupon(customer, giftCard,
                giftCard.expireDate?.toLocalDate()).get()
    }
}
