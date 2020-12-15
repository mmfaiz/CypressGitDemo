package com.matchi

import com.matchi.async.ScheduledTask

class Season implements Serializable {
    static belongsTo = [ facility : Facility ]

    String name
    String description
    Date startTime
    Date endTime

    static constraints = {
        name(blank: false)
        description(nullable: true, blank: true)
        startTime(nullable: false)
        endTime(nullable: false)
    }

    String toString() { "$name" }

    boolean hasBookings() {
        Booking.withCriteria(uniqueResult: true) {
            createAlias("customer", "c")
            projections {
                rowCount()
            }
            eq("c.facility", facility)
            slot {
                ge("startTime", startTime)
                lt("endTime", endTime)
            }
        }
    }

    boolean isInitializing() {
        ScheduledTask.withCriteria {
            eq('facility', facility)
            eq('domainIdentifier', id)
            eq('relatedDomainClass', Season.class.simpleName)
            eq('isTaskFinished', false)
        }
    }
}
