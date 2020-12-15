package com.matchi.protocol

abstract class AbstractRequest {

    /** Request type */
    public static enum RequestType {
        BOOKINGS, PAYMENT, UNKNOWN
    }

    /** Client remote IP Address  */
    String remoteIP

    /** Date and time of request */
    Date timestamp = new Date()

    /** Facility text Id  */
    String facilityId

    /** Request type  */
    RequestType type

    /** Wheather or not the request was successfully handles  */
    boolean success
}