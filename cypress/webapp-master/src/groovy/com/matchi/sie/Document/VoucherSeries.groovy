package com.matchi.sie.Document

class VoucherSeries
{
    static final String DEBTOR_INVOICE = "KF"
    static final String DEBTOR_PAYMENT = "KI"
    static final String SUPPLIER_INVOICE = "LF"
    static final String SUPPLIER_PAYMENT = "KB"
    static final String OTHER = "LV"

    static String selfFor (Boolean creditor, String type) {
        switch (type) {
            case "invoice":
                return creditor ? SUPPLIER_INVOICE : DEBTOR_INVOICE
                break
            case "payment":
                return creditor ? SUPPLIER_PAYMENT : DEBTOR_PAYMENT
                break
            default:
                return OTHER
                break
        }
    }
}
