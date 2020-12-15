package com.matchi.messages
import com.matchi.Facility
import com.matchi.Slot

class FacilityMessage {

    public static final enum Channel {
        BOOKING_CONFIRMED, ACCESS_CODE, NOTIFICATION
    }

    static belongsTo = [ facility: Facility ]


    String headline
    String content
    Channel channel
    boolean html

    boolean active
    Date validFrom
    Date validTo

    Integer listPosition

    static constraints = {
        content(nullable: false)
        headline(nullable: true)
        validFrom(nullable: true)
        validTo(nullable: true)
        channel(nullable: false)
        listPosition(nullable: true)
    }

    static mapping = {
        content type: "text"
    }

    static namedQueries = {
        facilityMessages { facility ->
            eq "facility", facility
            order("listPosition", "asc")
        }
    }

    def beforeInsert() {
        if (listPosition == null) {
            def max = withCriteria(uniqueResult: true) {
                projections {
                    max("listPosition")
                }
                eq("facility", facility)
            }
            listPosition = max ? max + 1 : 1
        }
    }
}
