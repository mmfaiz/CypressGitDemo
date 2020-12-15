package com.matchi.coupon

import com.matchi.price.PriceListCustomerCategory

/**
 * @author Sergei Shushkevich
 */
class CouponPrice implements Serializable {

    Integer price

    static belongsTo = [coupon: Offer, customerCategory: PriceListCustomerCategory]

    static constraints = {
        price(min: 0)
    }
}
