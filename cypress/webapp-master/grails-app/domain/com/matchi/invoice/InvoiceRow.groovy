package com.matchi.invoice
import com.matchi.Customer
import com.matchi.User
import com.matchi.facility.Organization
import org.joda.time.DateTime

class InvoiceRow implements Serializable {
    private static decimalFormat = new java.text.DecimalFormat( "###,##0.00" )
    private static final String DEFAULT_UNIT = "st"

    static belongsTo = [invoice: Invoice]

    Customer customer
    User createdBy

    Organization organization

    BigDecimal price = 0
    BigDecimal vat = 0
    BigDecimal discount = 0
    DiscountType discountType = DiscountType.AMOUNT
    String description
    String account
    int amount
    String unit = DEFAULT_UNIT
    String externalArticleId
    DateTime dateCreated
    DateTime lastUpdated
    public static final int DESCRIPTION_MAX_SIZE = 50

    BigDecimal getTotalIncludingVAT() {
        return (price * amount).minus(getDiscountValue())
    }

    BigDecimal getTotalVAT() {
        def price = getTotalIncludingVAT()
        return (vat ? price.minus(price / (1 + (vat/100))): 0 )
    }

    BigDecimal getTotalExcludingVAT() {
        getTotalIncludingVAT().minus(getTotalVAT())
    }

    BigDecimal getPriceVAT() {
        return (vat ? price.minus(price / (1 + (vat/100))) : 0 )
    }

    BigDecimal getPriceExcludingVAT() {
        return (price).minus(getPriceVAT())
    }

    BigDecimal getDiscountValue() {
        discountType == DiscountType.AMOUNT ? discount : (price * amount * discount) / 100
    }

    static constraints = {
        amount(scale:2)
        price(nullable: false)
        vat(nullable: false)
        account(nullable: true)
        externalArticleId(nullable: true)
        invoice(nullable: true)
        organization(nullable: true)
        description(nullable: true, maxSize: DESCRIPTION_MAX_SIZE)
    }

    boolean isNew() {
        if (dateCreated && dateCreated.plusMinutes(5).isAfter(new DateTime())) {
            return true
        }
        return false
    }

    static mapping = {
        autoTimestamp true
        sort "description": "asc"
    }

    String toString() {
        return "${description} ${decimalFormat.format(getTotalIncludingVAT())}"
    }

    static enum DiscountType {
        AMOUNT,
        PERCENT
    }
}
