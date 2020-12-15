package com.matchi.requests

import com.matchi.User
import org.joda.time.DateTime

abstract class Request {

    static belongsTo = [ requester: User ]

    Status status = Status.NEW
    String message

    DateTime dateCreated
    DateTime lastUpdated

    static constraints = {
        status nullable: false
        message nullable: true
    }

    boolean isAccepted() {
        return status == Status.ACCEPTED
    }

    boolean isDenied() {
        return status == Status.DENIED
    }

    static enum Status {
        NEW, ACCEPTED, DENIED

        static list() {
            return [ NEW, ACCEPTED, DENIED ]
        }
    }
}
