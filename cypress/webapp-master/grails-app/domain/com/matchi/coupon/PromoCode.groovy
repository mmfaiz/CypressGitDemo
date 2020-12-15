package com.matchi.coupon

class PromoCode extends Offer {

    public final static String MAPPING_VALUE = "promo_code"

    Date startDate
    String code
    BigDecimal discountAmount
    BigDecimal discountPercent

    static mapping = {
        discriminator MAPPING_VALUE
    }

    static constraints = {
        code(nullable: false)
        startDate(nullable: true)
        discountPercent(nullable: true)
        discountAmount(nullable: true)
    }

    @Override
    String getOfferTypeString() {
        return 'promoCode'
    }
}
