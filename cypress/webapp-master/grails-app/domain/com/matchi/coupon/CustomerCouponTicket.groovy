package com.matchi.coupon

import com.matchi.User
import com.matchi.orders.Order.Article
import org.apache.commons.lang.StringUtils

class CustomerCouponTicket {

    public static final int DESCRIPTION_MAX_SIZE = 255

    static belongsTo = [ customerCoupon: CustomerCoupon ]

    Long purchasedObjectId              // e.g. booking id; can be null if not applicable
    BigDecimal purchasedObjectPrice
    Type type
    Integer nrOfTickets                 // amount of tickets added or consumed (negative value); can be null for old data
    String description                  // extra description about purchased object (purchased object can be removed at some point)
    User issuer                         // user who initiated purchase by card; can be null for old data or system change
    Date dateCreated

    static constraints = {
        purchasedObjectId(nullable: true)
        purchasedObjectPrice(nullable: true)
        nrOfTickets(nullable: true)
        description(nullable: true, maxSize: 255)
        issuer(nullable: true)
    }

    static mapping = {
        version false
        type index: "ticket_obj_type_idx"
        purchasedObjectId index: "ticket_obj_type_idx"
    }

    void addDescription(String desc) {
        if (desc) {
            description = StringUtils.abbreviate(
                    desc.replaceFirst(/^[\s:]*/, ""), DESCRIPTION_MAX_SIZE)
        }
    }

    BigDecimal getAmount() {
        customerCoupon.coupon.instanceOf(Coupon) ? 1 : purchasedObjectPrice
    }

    enum Type {
        ACTIVITY,
        ACTIVITY_REFUND,
        ADMIN_CREATE,
        ADMIN_CHANGE,
        BOOKING,
        BOOKING_REFUND,
        COUPON,
        COUPON_REFUND,
        CUSTOMER_PURCHASE,
        SUBSCRIPTION_REFUND

        Type getRefundType() {
            switch(this) {
                case ACTIVITY:
                    return ACTIVITY_REFUND
                case BOOKING:
                    return BOOKING_REFUND
                case COUPON:
                    return COUPON_REFUND
                default:
                    return this
            }
        }

        static Type getTypeByOrderArticle(Article article) {
            switch(article) {
                case Article.BOOKING:
                    return BOOKING
                case Article.ACTIVITY:
                    return ACTIVITY
                case Article.COUPON:
                    return COUPON
                default:
                    return null
            }
        }
    }
}
