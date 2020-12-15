package com.matchi.subscriptionredeem.redeemstrategy

import com.matchi.Customer
import com.matchi.User
import com.matchi.price.Price
import org.joda.time.DateTime

abstract class RedeemStrategy implements Serializable{

    private static final long serialVersionUID = 12L

    DateTime dateCreated
    DateTime lastUpdated

    static mapping = {
        autoTimestamp(true)
        table("redeem_strategy")
        cache true
    }

    void populate(def params) {
        this.properties = params
    }

    abstract def redeem(User user, Customer customer, Price price, String slotdescription, Boolean fullRedeem)
}
