package com.matchi.api_ext.model

class APIExtBooking {
    Long id
    String comments
    String type
    Boolean showBookingHolder
    String booking_name
    Long customerId
    String activity_name
    String players

    APIExtBooking(Long id, String booking_comments, String type, Boolean showBookingHolder, String booking_name, Long customerId, String activity_name, String players) {
        this.id = id
        this.comments = booking_comments
        this.type = type
        this.showBookingHolder = showBookingHolder
        this.booking_name = booking_name
        this.customerId = customerId
        this.activity_name = activity_name
        this.players = players
    }
}
