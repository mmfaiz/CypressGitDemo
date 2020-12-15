package com.matchi.payment

enum PaymentMethod {
    CREDIT_CARD(true),
    CREDIT_CARD_RECUR,
    COUPON,
    FREE,
    CASH,
    REGISTER,
    INVOICE,
    GIFT_CARD,
    IDEAL(true),
    DOTPAY(true),
    SWISH(true),
    UNKNOWN

    final boolean requiresRedirect

    PaymentMethod(boolean requiresRedirect = false) {
        this.requiresRedirect = requiresRedirect
    }

    boolean isLocal() {
        return this.equals(IDEAL) || this.equals(DOTPAY) || this.equals(SWISH)
    }

    boolean isUsingPaymentGatewayMethod() {
        return this.equals(CREDIT_CARD) || this.equals(CREDIT_CARD_RECUR) || this.isLocal()
    }

    static list() {
        return [ CREDIT_CARD, CREDIT_CARD_RECUR, COUPON, FREE, CASH, REGISTER, INVOICE, GIFT_CARD, IDEAL, DOTPAY, SWISH]
    }
}