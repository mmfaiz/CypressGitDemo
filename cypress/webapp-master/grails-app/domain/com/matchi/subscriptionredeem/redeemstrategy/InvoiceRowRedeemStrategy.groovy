package com.matchi.subscriptionredeem.redeemstrategy
import com.matchi.Customer
import com.matchi.User
import com.matchi.facility.Organization
import com.matchi.invoice.InvoiceRow
import com.matchi.price.Price
import org.apache.commons.lang.StringUtils

class InvoiceRowRedeemStrategy extends RedeemStrategy {

    String externalArticleId
    Long organizationId
    String description
    Long amount = 0l
    RedeemAmountType redeemAmountType
    BigDecimal vat = 0

    static constraints = {
        externalArticleId(nullable: true)
        organizationId(nullable: true)
        description(nullable: false)
        amount(nullable: false)
        redeemAmountType(nullable: false)
        vat(nullable: false)
    }

    String getType() {
        return "INVOICE_ROW"
    }

    static transients = ['type']

    @Override
    def redeem(User user, Customer customer, Price  price, String slotdescription, Boolean fullRedeem) {
        InvoiceRow row = new InvoiceRow()
        row.customer = customer
        row.description = StringUtils.abbreviate(description + slotdescription, 50)
        row.externalArticleId = externalArticleId
        row.organization = Organization.get(organizationId)
        row.amount = 1
        row.createdBy = user
        row.vat = vat

        log.debug("Set vat to ${row.vat} (${vat})")

        if (!fullRedeem) {
            setNormalRedeemPrice(row, price.price)
        } else {
            row.price = -price.price
        }

        if (row.price < 0) {
            row.save(failOnError: true)
        } else {
            return null
        }

        return row
    }

    void setNormalRedeemPrice(InvoiceRow row, BigDecimal price) {
        switch (redeemAmountType) {
            case (RedeemAmountType.PERCENTAGE_BACK):
                def priceReduction = (amount / 100).toBigDecimal()
                row.price = -(price * priceReduction)
                break
            case (RedeemAmountType.PRICE_REDUCTION_BACK):
                def priceReduction = amount.toBigDecimal()
                row.price = -(price - priceReduction)
                break
            default:
                break
        }
    }

    enum RedeemAmountType {
        PERCENTAGE_BACK,PRICE_REDUCTION_BACK

        static list() {
            return [ PERCENTAGE_BACK, PRICE_REDUCTION_BACK ]
        }
    }
}
