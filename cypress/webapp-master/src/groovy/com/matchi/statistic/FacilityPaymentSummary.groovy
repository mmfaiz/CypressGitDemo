package com.matchi.statistic

import com.matchi.FacilityContractItem

/**
 * Wrapper for a set of facility payment entries
 *
 f.id,
 f.name,
 o.article,
 op.type,
 o.price,
 CASE WHEN (op.type = 'netaxept') THEN (o.price) ELSE 0 END as revenue,
 o.date_delivery as date,

 sum(CASE WHEN (op.status = 'CAPTURED') THEN 1 ELSE 0 END) as num,
 sum(CASE WHEN (op.status = 'CREDITED') THEN 1 ELSE 0 END) as num_credited,

 -- Netaxept
 sum(CASE WHEN (op.type = 'netaxept' and op.status = 'CAPTURED') THEN (amount) ELSE 0 END) as total,
 sum(CASE WHEN (op.type = 'netaxept' and op.status = 'CREDITED') THEN (amount-credited) ELSE 0 END) as total_cancel_fees,
 sum(CASE WHEN (op.type = 'netaxept' and op.status = 'CREDITED') THEN (credited) ELSE 0 END) as total_credited,

 -- Origin statistics
 sum(CASE WHEN (o.origin = 'api') THEN 1 ELSE 0 END) as num_api,
 sum(CASE WHEN (o.origin = 'web') THEN 1 ELSE 0 END) as num_web,
 fc.variable_mediation_fee
 */
class FacilityPaymentSummary {

    def facility
    def contract
    List<FacilityContractItem> contractItems
    def entries
    def couponEntries
    List<FacilityCouponFeeEntry> detailedOfferEntries
    def giftCardEntries
    def interval
    def fees = []
    List<FacilityPromoDiscountEntry> promoCodeDiscounts
    List<FacilityMembershipDetailEntry> membershipEntries

    FacilityPaymentSummary(facility, entries, interval, couponEntries, List<FacilityCouponFeeEntry> detailedOfferEntries, List<FacilityContractItem> contractItems = [], giftCardEntries = [], List<FacilityPromoDiscountEntry> promoCodeDiscounts = [], List<FacilityMembershipDetailEntry> membershipEntries = []) {
        this.facility = facility
        this.entries = entries
        this.interval = interval
        this.couponEntries = couponEntries
        this.detailedOfferEntries = detailedOfferEntries
        this.contract = facility.getActiveContract(interval.start.toDate())
        this.contractItems = contractItems
        this.giftCardEntries = giftCardEntries
        this.promoCodeDiscounts = promoCodeDiscounts
        this.membershipEntries = membershipEntries
    }

    def getTotalRevenue() {
        def revenue = entries.sum(0) { it.getTotalRevenue() }
        revenue - getTotalPromoDiscounts()
    }

    def getTotalMonthlyProfit() {

        // facility monthly fixed charge
        def fixedCharge = getFixedFee()

        // variable fee
        def variableFee = getTotalVariableFees()

        // total
        return getTotalRevenue() - (variableFee + fixedCharge + getTotalCouponFees() +
                getTotalGiftCardFees() + getTotalContractItemFees() + getTotalExtraFees())

    }

    def getTotalFees(Boolean includeFixedFees = Boolean.TRUE) {
        return (includeFixedFees ? getFixedFee() : 0) + getTotalVariableFees() + getTotalCouponFees() +
                getTotalGiftCardFees() + getTotalContractItemFees() + getTotalExtraFees()
    }

    def getTotalContractItemFees() {
        contractItems.sum(0) {it.price}
    }

    def getTotalVariableFees() {
        if(!hasContract()) return 0
        return entries.findAll { it.type != "coupon" }.sum(0) { getVariableFee(it) }
    }

    def getTotalCouponFees() {
        couponEntries.sum(0) { it.totalFee }
    }

    def getTotalGiftCardFees() {
        giftCardEntries.sum(0) { it.totalFee }
    }

    def getTotalExtraFees() {
        fees?.sum(0) { it.total }
    }

    def getTotalPromoDiscounts() {
        promoCodeDiscounts.sum(0) {it.total}
    }

    def getPercentageVariableFeesEntries() {
        return entries.findAll { it.type != "coupon" && !useMinimalFee(it) }
    }

    def getMinimalFeeVariableFeesEntries() {
        return entries.findAll { it.type != "coupon" && useMinimalFee(it) }
    }

    def getVariableFee(def entry) {
        if(!hasContract()) return 0
        return contract.getFee(entry.price) * entry.num
    }

    def useMinimalFee(entry) {
        if(!hasContract()) return false

        return contract.useMinimumFee(entry.price)
    }

    def getFixedFee() {
        if(!hasContract()) return 0
        contract ? contract.getFixedMonthlyFee() : 0
    }

    def hasContract() {
        return this.contract != null
    }


}
