package com.matchi.protocol

/* Created: 2012-11-27 Mattias (mattias@tdag.se) */
class ApiRequestError {
    public static enum Code {
        VERIFICATION_ERROR, CONFIRMATION_ERROR, PAYMENT_ORDER_NOT_FOUND, NO_TRANSACTION_ID, GENERIC_ERROR
    }

    ApiRequestError(errorCode, message) {
        this.errorCode = errorCode
        this.message = message
    }

    def errorCode
    def message
}
