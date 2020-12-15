package com.matchi.protocol

/* Created: 2012-11-27 Mattias (mattias@tdag.se) */
class ApiPaymentRequestException extends RuntimeException {
    ApiRequestError.Code errorCode

    ApiPaymentRequestException(ApiRequestError.Code errorCode) {
        this.errorCode = errorCode
    }
}
