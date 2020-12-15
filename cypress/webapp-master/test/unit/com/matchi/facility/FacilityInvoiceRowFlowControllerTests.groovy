package com.matchi.facility

import com.matchi.invoice.InvoiceRow
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.junit.Test

@TestFor(FacilityInvoiceRowFlowController)
@Mock([ InvoiceRow ])
class FacilityInvoiceRowFlowControllerTests {

    @Test
    void testValidateAddRowReturnsCorrectValues() {
        InvoiceDetailsCommand cmd = new InvoiceDetailsCommand()
        InvoiceRow row1 = new InvoiceRow(amount: 1, price: 100, vat: 0).save(validate: false)
        InvoiceRow row2 = new InvoiceRow(amount: 1,price: -150, vat: 0).save(validate: false)
        InvoiceRow row3 = new InvoiceRow(amount: 1,price: -50, vat: 0).save(validate: false)

        def sum1 = row1.getTotalIncludingVAT() + row2.getTotalIncludingVAT()
        def sum2 = row1.getTotalIncludingVAT() + row3.getTotalIncludingVAT()
        cmd.createNoCreditInvoices = true

        assert controller.validateAddInvoiceRow(sum1, row1, cmd)
        assert !controller.validateAddInvoiceRow(sum1, row2, cmd)
        assert controller.validateAddInvoiceRow(sum2, row3, cmd)

        cmd.createNoCreditInvoices = false
        assert controller.validateAddInvoiceRow(sum1, row2, cmd)
    }
}