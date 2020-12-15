package com.matchi

class FacilityContract implements Serializable {

    static belongsTo = [facility: Facility]

    String name
    String description

    BigDecimal fixedMonthlyFee
    BigDecimal variableMediationFee
    BigDecimal variableCouponMediationFee
    BigDecimal variableUnlimitedCouponMediationFee
    BigDecimal variableTextMessageFee
    BigDecimal variableMediationFeePercentage
    MediationFeeMode mediationFeeMode = MediationFeeMode.OR


    Date dateValidFrom

    CouponContractType couponContractType = CouponContractType.PER_TICKET
    CouponContractType unlimitedCouponContractType = CouponContractType.PER_TICKET

    GiftCardContractType giftCardContractType = GiftCardContractType.PER_USE
    BigDecimal variableGiftCardMediationFee

    static hasMany = [items: FacilityContractItem]

    static constraints = {
        name(nullable: false, blank: false)
        description(nullable:  true)
        fixedMonthlyFee(nullable: false)
        variableMediationFee(nullable: false)
        variableMediationFeePercentage(nullable: true)
        variableTextMessageFee(nullable: true)
    }

    static mapping = {
        dateValidFrom type: "date"
    }

    static namedQueries = {
        activeContract { Facility f, Date date ->
            facility {
                eq("id", f.id)
            }
            le("dateValidFrom", date)
            order("dateValidFrom", "desc")
            maxResults(1)
        }
    }

    @Override
    String toString() {
        return "${name} (${fixedMonthlyFee} ${facility?.currency} / ${variableMediationFee} ${facility?.currency} / ${variableMediationFeePercentage}%)"
    }

    boolean useMinimumFee(def amount) {
        if(amount == null) return false

        mediationFeeMode == MediationFeeMode.OR ?
                amount <= getMinimalFeeThreshold() : false
    }

    def getPercentageFree(def amount) {
        def fee = (variableMediationFeePercentage / 100) * amount
        return Math.round(fee * 100) / 100
    }

    def getFee(def amount) {
        mediationFeeMode == MediationFeeMode.OR ?
                (useMinimumFee(amount) ? variableMediationFee : getPercentageFree(amount)) :
                variableMediationFee + getPercentageFree(amount)
    }

    def getMinimalFeeThreshold() {
        if(variableMediationFee == 0) {
            return 1
        }
        if(variableMediationFeePercentage == 0) {
            return 0
        }
        return (int) Math.floor((variableMediationFee / (variableMediationFeePercentage / 100)));
    }

    static enum CouponContractType {
        PER_COUPON, PER_TICKET
    }

    static enum GiftCardContractType {
        PER_GIFT_CARD, PER_USE
    }

    static enum MediationFeeMode {
        OR, AND
    }
}
