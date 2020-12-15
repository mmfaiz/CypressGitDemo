package com.matchi.invoice

import org.joda.time.LocalDate

class InvoicePayment {
    Invoice invoice
    BigDecimal amount
    LocalDate paymentDate
    Date dateCreated
    Date lastUpdated

    static constraints = {
        invoice(nullable: false)
        amount nullable: false
    }
}
