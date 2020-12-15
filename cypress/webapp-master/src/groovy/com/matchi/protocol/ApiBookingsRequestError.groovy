package com.matchi.protocol

/**
 * Created by IntelliJ IDEA.
 * User: mattias
 * Date: 2011-12-06
 * Time: 14:52
 * To change this template use File | Settings | File Templates.
 */
class ApiBookingsRequestError {

    public static enum Code {
        FACILITY_NOT_FOUND, PAGINATION_ERROR, DATE_ERROR, GENERIC_ERROR
    }

    ApiBookingsRequestError(errorCode, message) {
        this.errorCode = errorCode
        this.message = message
    }

    def errorCode
    def message
}

