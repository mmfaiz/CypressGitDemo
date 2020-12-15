package com.matchi

import com.matchi.invoice.InvoiceRow

class PaymentTransaction implements Serializable {

    static belongsTo = [ payment:Payment ]

    Long paidAmount

    //Reference to invoicerow used for payment
    InvoiceRow invoiceRow
    //Boxnet transaction information
    String cashRegisterTransactionId

    Date dateCreated

    static constraints = {
        paidAmount(nullable: false)
        cashRegisterTransactionId(nullable: true)
        invoiceRow(nullable: true)
    }
    static mapping = {
        autoTimestamp true
        sort 'dateCreated'
    }
}
