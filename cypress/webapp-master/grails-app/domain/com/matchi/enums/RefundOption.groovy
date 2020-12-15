package com.matchi.enums

public enum RefundOption {

    FULL_REFUND, CUSTOMER_PAYS_FEE, NO_REFUND

    static list() {
        return [ FULL_REFUND, CUSTOMER_PAYS_FEE, NO_REFUND ]
    }
}