package com.matchi.marshallers

import com.matchi.invoice.Invoice
import com.matchi.invoice.InvoiceRow
import grails.converters.JSON

import javax.annotation.PostConstruct

class InvoiceMarshaller {

    def invoiceService

    @PostConstruct
    void register() {
        // Standard Node marshall
        JSON.registerObjectMarshaller(Invoice) { Invoice invoice ->
            marshallInvoice(invoice)
        }
    }

    def marshallInvoice(Invoice invoice) {
        [
                id: invoice.id,
                number: invoice.number,
                invoiceDate: invoice.invoiceDate,
                dueDate: invoice.expirationDate,
                currency: invoice.customer.facility.currency,
                customerId: invoice.customer.id,
                customerExternalId: null, // TODO: we still need to figure out whether this property is necessary or not for Visma or XLeadger
                cancelled: invoice.status == Invoice.InvoiceStatus.CANCELLED,
                credited: invoice.status == Invoice.InvoiceStatus.CREDITED,
                creditInvoiceReference: null, // TODO: we do not store it anywhere, so, we can't return any value currently
                remarks: invoice.text,
                rows: invoice.rows.collect {[
                        articleNumber: it.externalArticleId ?
                                (it.externalArticleId.isLong() ? it.externalArticleId.toLong() : it.externalArticleId) : null,
                        articleName: getArticleName(invoice, it),
                        accountNumber: it.account,
                        quantity: it.amount,
                        unit: it.unit,
                        description: it.description,
                        discount: it.discount.toFloat(),
                        discountType: it.discountType == InvoiceRow.DiscountType.PERCENT ? "PERCENTAGE" : "AMOUNT",
                        price: it.getPriceExcludingVAT(),
                        vat: it.vat ? it.vat.toInteger() : 0,
                        total: it.getTotalIncludingVAT()
                ]}
        ]
    }

    /**
     * If an article is removed, invoices with the removed article will return null as the article name.
     * If no name is found we return the description as the name instead since that is stored on the
     * invoice row (which the name is not).
     * @param invoice
     * @param invoiceRow
     * @return
     */
    def getArticleName(Invoice invoice, InvoiceRow invoiceRow) {
        def name = null
        if (invoiceRow.externalArticleId) {
            name = invoiceService.getItems(invoice.customer.facility).find { a ->
                a.id == invoiceRow.externalArticleId
            }?.descr
        }
        name ? name : invoiceRow.description
    }
}
