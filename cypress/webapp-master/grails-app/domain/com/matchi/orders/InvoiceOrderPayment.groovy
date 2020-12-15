package com.matchi.orders

import com.matchi.invoice.InvoiceRow
import com.matchi.membership.Membership
import com.matchi.payment.PaymentMethod

class InvoiceOrderPayment extends OrderPayment {
    private static final long serialVersionUID = 12L

    public static final String DISCRIMINATOR = "invoice"

    InvoiceRow invoiceRow
    PaymentMethod method = PaymentMethod.INVOICE

    @Override
    void refund(def Object amount) {
        if (invoiceRow) {
            invoiceRow.delete()
        }

        this.status = OrderPayment.Status.CREDITED
        this.setCredited(amount)
        this.invoiceRow = null
    }

    @Override
    def getType() {
        return "Invoice"
    }

    @Override
    boolean allowLateRefund() {
        true
    }

    @Override
    boolean isRefundableTypeAndStatus() {
        false
    }

    static constraints = {
        invoiceRow(nullable: true)
    }
    static mapping = {
        discriminator DISCRIMINATOR
    }
}
