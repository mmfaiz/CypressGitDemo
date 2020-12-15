package com.matchi.protocol

/* Created: 2012-11-27 Mattias (mattias@tdag.se) */
class ApiRequestedPayment implements Serializable {

    boolean success
    def executionTime = 0
    def errors = []

    def addError(def code, def message) {
        this.errors << new ApiBookingsRequestError(code, message)
    }
}
