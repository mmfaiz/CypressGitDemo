package com.matchi.protocol

class ApiBookingsRequest {

    int start = 0
    int max = 20
    Date timestamp = new Date()
    String remoteIP
    String facilityId
    String startDate
    String endDate

    public String toString ( ) {
        return "ApiBookingsRequest [" + remoteIP + "] " +
                                "[" + facilityId + "] " +
                                "[" + startDate + "] " +
                                "[" + endDate + "]";
    }
}
