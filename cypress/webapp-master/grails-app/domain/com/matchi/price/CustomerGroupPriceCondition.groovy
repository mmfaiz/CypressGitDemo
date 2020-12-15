package com.matchi.price
import com.matchi.Customer
import com.matchi.Group
import com.matchi.Slot
import com.matchi.coupon.Coupon
import com.matchi.coupon.Offer
import com.matchi.dynamicforms.Form

class CustomerGroupPriceCondition extends CustomerPriceCondition {

    static hasMany = [ groups: Group ]

    @Override
    boolean accept(Slot slot, Customer customer) {
        accept(customer)
    }

    @Override
    boolean accept(Offer coupon, Customer customer) {
        accept(customer)
    }

    @Override
    boolean accept(Form form, Customer customer) {
        accept(customer)
    }

    @Override
    boolean accept(Customer customer) {
        boolean result = false
        groups.each { group ->
            if(customer?.belongsTo(group)) {
                result = true
                //log.debug("Customer ${customer} belong to group ${group}")
            } else {
                //log.debug("Customer ${customer} does not belong to group ${group}")
            }
        }
        result
    }

    static constraints = {
    }
}
