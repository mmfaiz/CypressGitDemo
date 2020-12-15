package com.matchi
import com.matchi.payment.ArticleType
import com.matchi.payment.PaymentMethod

class PaymentOrder implements Serializable {
    String id
    User user
    Customer customer
    ArticleType articleType

    Date dateCreated
    Date lastUpdated

    Facility facility

    String orderNumber
    String orderDescription
    Map orderParameters

    PaymentMethod method
    BigDecimal price
    BigDecimal vat
    Long customerCouponId
    String priceDescription
    boolean savePaymentInfo = false

    def toAmount() {
        Amount amountValue = new Amount()
        amountValue.amount = price
        amountValue.VAT    = vat
        return amountValue
    }

    def isFree() {
        price.equals(0)
    }

    static mapping = {
        id generator:'uuid2'
        autoTimestamp true
        sort 'dateCreated'
    }

    static constraints = {
        price(nullable: true)
        vat(nullable: true)
        method(nullable:  true)
        priceDescription(nullable: true)
        customerCouponId(nullable: true)
        user(nullable: true)
        customer(nullable: true)
    }
}
