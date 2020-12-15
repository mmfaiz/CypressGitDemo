package com.matchi

import com.matchi.invoice.InvoiceRow
import grails.test.mixin.TestFor

@TestFor(MoneyTagLib)
class MoneyTagLibTests {

    void testFormatDiscount() {
        assert applyTemplate('<g:formatDiscount/>') == ""
        assert applyTemplate('<g:formatDiscount zeroValue="-"/>') == "-"
        assert applyTemplate('<g:formatDiscount invoiceRow="${row}"/>',
                [row: new InvoiceRow()]) == ""
        assert applyTemplate('<g:formatDiscount invoiceRow="${row}" zeroValue="-"/>',
                [row: new InvoiceRow()]) == "-"
        assert applyTemplate('<g:formatDiscount invoiceRow="${row}" facility="${facility}"/>',
                [row: new InvoiceRow(discount: 10), facility: [currency: "SEK"]]) == "10 SEK"
        assert applyTemplate('<g:formatDiscount invoiceRow="${row}" zeroValue="-"/>',
                [row: new InvoiceRow(discount: 10, discountType: InvoiceRow.DiscountType.PERCENT)]) == "10%"
    }
}
