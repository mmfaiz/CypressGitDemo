package com.matchi.price
import com.matchi.Customer
import com.matchi.Slot
import com.matchi.coupon.Coupon
import com.matchi.coupon.Offer
import com.matchi.dynamicforms.Form

class CustomerPriceCondition extends AbstractPriceCondition {

    static belongsTo = [ customerCategory : PriceListCustomerCategory ]

    static constraints = {
    }

    @Override
    boolean accept(Slot slot, Customer customer) {
        return true
    }

    boolean accept(Offer coupon, Customer customer) {
        return true
    }

    boolean accept(Form form, Customer customer) {
        return true
    }

    boolean accept(Customer customer) {
        return true
    }
}
