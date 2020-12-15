package com.matchi

class IOSync {

    Long customerId
    Long activityOccasionId
    String batchId
    String message
    Date dateCreated
    Date lastUpdated

    Status status

    static constraints = {
        batchId nullable: true, maxSize: 255
        message nullable: true, maxSize: 255
        status nullable: true
        customerId(nullable: true)
        activityOccasionId(nullable: true)
    }

    static mapping = {
        version false
        customerId index: true
        activityOccasionId index: true
        batchId index: true
    }

    public static enum Status {
        OK, ERROR

        static list() {
            return [OK, ERROR]
        }
    }
}