package com.matchi

/**
 * Represents an amount with VAT
 */
class Amount {
    BigDecimal amount = BigDecimal.ZERO
    BigDecimal VAT    = BigDecimal.ZERO

    def sum() {
        return amount.plus(VAT)
    }

    static def toPaymentPriceFormat(double price) {
        int result = (int)(Math.round(price*100.0)/100.0) * 100
        return (result == 0?"00":String.valueOf(result))
    }

    static def toPaymentPriceFormat(Long price) {
        return toPaymentPriceFormat(price.doubleValue())
    }

    static def toPaymentPriceFormat(BigDecimal price) {
        return toPaymentPriceFormat(price.doubleValue())
    }
}
