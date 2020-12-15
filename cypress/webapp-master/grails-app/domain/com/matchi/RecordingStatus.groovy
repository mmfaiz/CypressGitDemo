package com.matchi

class RecordingStatus {
    Booking booking
    Status status
    Date dateCreated
    Date lastUpdated
    String mediaUrl

    static constraints = {
        booking unique: true
        status nullable: false
    }

    static mapping = {
        id column: "booking_id", generator: 'foreign', params: [ property: 'booking' ]
        booking column: "booking_id", insertable: false, updateable: false
    }

    static enum Status {
        LIVE_NOT_EXISTS, LIVE_EXISTS, RECORDING_NOT_EXISTS, RECORDING_EXISTS
    }
}
