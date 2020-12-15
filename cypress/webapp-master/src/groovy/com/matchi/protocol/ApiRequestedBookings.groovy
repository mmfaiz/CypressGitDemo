package com.matchi.protocol

class ApiRequestedBookings implements Serializable  {
    String name
    String description
    boolean success
    def entries = []
    def executionTime = 0
    def errors = []

    def addError(def code, def message) {
        this.errors << new ApiBookingsRequestError(code, message)
    }
}

