package com.matchi.api

public enum Code {
    BAD_CREDENTIALS,
    ACCESS_DENIED,
    DEVICE_BLOCKED,
    USER_BLOCKED,
    RESOURCE_NOT_FOUND,
    USER_EXISTS,
    INPUT_ERROR,
    INVALID_PROMO_CODE,
    ALREADY_USED_PROMO_CODE,
    PAYMENT_ERROR,
    UNKNOWN_ERROR,
    UPGRADE_APP_VERSION,
    INVALID_EMAIL
}

class APIException extends Exception {
    int status = 400
    Code errorCode = Code.UNKNOWN_ERROR
    String userMessage

    APIException(int status, Code errorCode, String userMessage) {
        this.status = status
        this.errorCode = errorCode
        this.userMessage = userMessage
    }

    APIException(Throwable throwable, int status, Code errorCode, String userMessage) {
        super(throwable)
        this.status = status
        this.errorCode = errorCode
        this.userMessage = userMessage
    }
}
