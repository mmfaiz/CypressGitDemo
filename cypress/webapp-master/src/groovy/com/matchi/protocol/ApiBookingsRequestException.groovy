package com.matchi.protocol

class ApiBookingsRequestException extends RuntimeException {

    ApiBookingsRequestError.Code errorCode

    ApiBookingsRequestException(ApiBookingsRequestError.Code errorCode) {
        this.errorCode = errorCode
    }
}
