package com.matchi.price
import com.matchi.Customer
import com.matchi.Facility
import com.matchi.Slot
import com.matchi.coupon.CouponPrice

class PriceListCustomerCategory {
    static def belongsTo = [ facility : Facility ]
    static def hasMany   = [ conditions: CustomerPriceCondition, prices: Price, couponPrices: CouponPrice ]

    String name
    boolean defaultCategory
    boolean onlineSelect
    boolean forceUseCategoryPrice
    Integer daysBookable
    boolean deleted = false

    static constraints = {
        daysBookable min: 0, nullable: true
    }

    static namedQueries = {
        available { facility ->
            eq "facility", facility
            eq "deleted", false
        }

    }

    boolean accept(Object obj, Customer customer) {
        if (obj instanceof Slot && obj?.booking?.selectedCustomerCategory?.id == this.id) {
            return true
        }

        conditions.every {
            it.accept(obj, customer)
        }
    }

    boolean acceptConditions(Customer customer) {
        return conditions.every {
            it.accept(customer)
        }
    }
}
