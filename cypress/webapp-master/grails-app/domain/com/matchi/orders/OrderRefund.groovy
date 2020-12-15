package com.matchi.orders

import com.matchi.User

class OrderRefund {

    static belongsTo = [order: Order]

    String note
    BigDecimal amount = 0
    BigDecimal vat = 0
    Date dateCreated
    Date lastUpdated
    String promoCode

    User issuer

    static constraints = {
        note nullable: true
        issuer nullable: true
        promoCode nullable: true
    }

    static mapping = {
        autoTimestamp true
        note type: "text"
    }
}
