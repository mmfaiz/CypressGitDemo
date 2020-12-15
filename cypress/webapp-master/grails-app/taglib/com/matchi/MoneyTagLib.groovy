package com.matchi

import com.matchi.invoice.InvoiceRow

class MoneyTagLib {

    static final String CURRENCY_SHORT = "kr"
    static final String DECIMAL_FORMAT = "##,##0.##"
    static final String SERVICE_FEE_FORMAT = "0.00"

    static returnObjectForTags = ["serviceFeeValue"]

    def userService

    def formatMoney = { attrs, body ->
        if(attrs.value || attrs.forceZero) {
            def facility = attrs.facility ?: (Facility)userService.getUserFacility()
            def amount = attrs.value
            out << formatMoneyShort(value: amount, forceZero: attrs.forceZero) << " " << facility?.currency?.encodeAsHTML()
        }
    }

    /**
     * If you want to show 0 also formatted use 'forceZero=true'
     */
    def formatMoneyShort = { attrs, body ->
        if(attrs.value || attrs.forceZero) {
            def amount = attrs.value
            out << formatNumber(number: amount, format: DECIMAL_FORMAT)
        }
    }

    def currentFacilityCurrency = { attrs, body ->
        def facility = attrs.facility ?: (Facility)userService.getUserFacility()
        out << facility?.currency?.encodeAsHTML()
    }

    def serviceFeeValue = { attrs, body ->
        def currency = attrs.currency
        if (!currency) {
            throwTagError("Tag [serviceFeeValue] is missing required attribute [currency]")
        }

        formatNumber(number: grailsApplication.config.matchi.settings.currency[currency].serviceFee,
                format: SERVICE_FEE_FORMAT) + " " + currency
    }

    def formatDiscount = { attrs, body ->
        def invoiceRow = attrs.invoiceRow

        if (invoiceRow?.discount) {
            out << formatMoneyShort(value: invoiceRow.discount)

            if (invoiceRow.discountType == InvoiceRow.DiscountType.AMOUNT) {
                out << " " << (attrs.facility ?: userService.getUserFacility())?.currency?.encodeAsHTML()
            } else {
                out << "%"
            }

        } else if (attrs.zeroValue) {
            out << attrs.zeroValue
        }
    }
}
