package com.matchi.price
import com.matchi.Customer
import com.matchi.Slot

abstract class AbstractPriceCondition {
    String name
    String description

    static constraints = {
        name(nullable: true)
        description(nullable: true)
    }

    @Override
    int hashCode() {
        return super.hashCode()    //To change body of overridden methods use File | Settings | File Templates.
    }

    abstract boolean accept(Slot slot, Customer customer)
}
