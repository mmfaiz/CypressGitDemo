package com.matchi.price
import com.matchi.Customer
import com.matchi.PriceList
import com.matchi.Slot

class PriceListConditionCategory implements Serializable  {

    static belongsTo = [ pricelist : PriceList ]
    static hasMany = [ conditions: BookingPriceCondition, prices: Price ]

    String name
    boolean defaultCategory

    static constraints = {
    }

    static mapping = {
        conditions sort: "id"
	}

    boolean accept(Slot slot, Customer customer) {
        def accepted = true
        if(conditions.isEmpty()) { return true }
        conditions.each {
            if(!it.accept(slot, customer)) {
                accepted = false
            }
        }
        return accepted
    }
}
