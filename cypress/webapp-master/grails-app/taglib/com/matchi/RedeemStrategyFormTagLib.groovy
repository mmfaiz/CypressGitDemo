package com.matchi

import com.matchi.coupon.Coupon
import com.matchi.coupon.GiftCard
import com.matchi.facility.Organization

class RedeemStrategyFormTagLib {

    static String templatePrefix = "redeem_strategy_"

    def strategyForm = { attrs, body ->
        def facility = attrs.facility
        def strategy = attrs.strategy
        def coupons  = Coupon.findAllByFacility(facility)
        def giftCards  = GiftCard.findAllByFacility(facility)

        def facStrat = facility.subscriptionRedeem?.strategy

        def currentStrategy = facStrat?.type == strategy?.type ? facStrat : null

        def organizations = Organization.findAllByFacility(facility)

        def template = getTemplateNameFromStrategy(strategy)
        out << render(template:"/templates/redeemStrategy/${template}",
                model: [ currentStrategy:currentStrategy, coupons:coupons, strategy:strategy, articles: attrs.articles,
                         organizations: organizations, facility: facility, giftCards: giftCards ])
    }

    private def getTemplateNameFromStrategy(def strategy) {
        return "${templatePrefix}${strategy?.type?.toLowerCase()}"
    }

}
