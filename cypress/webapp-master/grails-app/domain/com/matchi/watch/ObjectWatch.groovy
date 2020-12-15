package com.matchi.watch

import com.matchi.Facility
import com.matchi.User

/**
 * @author Sergei Shushkevich
 */
abstract class ObjectWatch {

    User user
    Facility facility

    Date fromDate
    Date toDate

    Date dateCreated
    Date lastUpdated

    boolean smsNotify

    static constraints = {
        user(nullable: false)
        facility(nullable: false)
        fromDate(nullable: false)
        toDate(nullable: false)
        smsNotify(nullable: false)
    }

    static mapping = {
        autoTimestamp true
        sort "fromDate"
        order "asc"
    }
}