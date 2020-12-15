package com.matchi.subscriptionredeem
import com.matchi.Facility
import com.matchi.enums.RedeemAt
import com.matchi.subscriptionredeem.redeemstrategy.RedeemStrategy
import org.joda.time.DateTime

class SubscriptionRedeem implements Serializable {

    private static final long serialVersionUID = 12L

    static belongsTo = [ facility: Facility ]

    RedeemAt redeemAt
    RedeemStrategy strategy

    DateTime dateCreated
    DateTime lastUpdated

    static constraints = {
        redeemAt(nullable: false)
        strategy(nullable: false)
    }

    static mapping = {
        autoTimestamp(true)
        strategy lazy: false, cache: true
        cache true
    }

    def clear() {
        def s = strategy
        strategy = null
        delete()
        facility.subscriptionRedeem = null
        s.delete()
    }

    def clearStrategy(def s) {
        strategy = null
        save()
        s.delete()
    }
}
