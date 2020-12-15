package com.matchi

import com.matchi.payment.PaymentMethod

class CashRegisterTransaction {

    static def belongsTo = [ customer: Customer ]

    Date date
    String title
    PaymentMethod method
    BigDecimal paidAmount
    String vat
    String receiptNumber

    Date dateCreated
    Date lastUpdated

    static constraints = {
        date(nullable: false)
        title(nullable: false)
        method(nullable: false)
        paidAmount(nullable: false)
        vat(nullable: true)
        receiptNumber(nullable: true)
    }

    static mapping = {
        sort "date":"desc"
    }
}
