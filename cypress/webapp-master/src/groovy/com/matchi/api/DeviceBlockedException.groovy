package com.matchi.api

class DeviceBlockedException extends APIException {
    DeviceBlockedException(int status, Code errorCode, String userMessage) {
        super(status, errorCode, userMessage)
    }
}
