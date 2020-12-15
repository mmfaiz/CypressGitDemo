package com.matchi.price
import com.matchi.Customer
import com.matchi.Slot

class BookingPriceCondition extends AbstractPriceCondition implements Serializable  {

    static belongsTo = [category : PriceListConditionCategory]

    static constraints = {
        category nullable: true
    }

    @Override
    boolean accept(Slot slot, Customer customer) {
        return true
    }

    def prepareForSave() {
        // nothing
    }
    def prepareForDelete() {
        // nothing
    }
}
