package com.matchi

import com.matchi.enums.BookingGroupType
import org.apache.commons.lang.StringUtils

class BookingGroup implements Serializable {

    public static final int COMMENT_MAX_SIZE = 255

    static hasMany = [ bookings: Booking ]
    static hasOne  = [ subscription: Subscription ]

    SortedSet bookings
    BookingGroupType type
    String comment

    def isType(BookingGroupType type) {
        return this.type.equals(type)
    }
	
    static constraints = {
		type(nullable: false)
        subscription(nullable: true)
        comment(nullable: true, maxSize: COMMENT_MAX_SIZE)
    }

    static mapping = {
		//bookings sort: "slot.startTime"
	}

    void addComment(String c) {
        comment = StringUtils.abbreviate(c, COMMENT_MAX_SIZE)
    }
}
