package com.matchi

class FacilityAccessCode {

    static belongsTo = [ facility: Facility ]
    static hasMany = [ courts: Court ]

    Set courts = []

    String content
    boolean active
    Date validFrom
    Date validTo

    static constraints = {
        content(nullable: false)
        validFrom(nullable: true)
        validTo(nullable: true)
    }

    static mapping = {
        content type: "text"
        courts joinTable: [name: "court_facility_access_codes", key: 'facility_access_code_id' ]
    }

    static namedQueries = {
        facilityAccessCodes { facility ->
            eq "facility", facility
            order("validFrom")
        }

        validAccessCode { Facility facility, Court court, Date date ->
            facilityAccessCodes(facility)
            le("validFrom", date)
            gt("validTo", date)
            createAlias("courts", "c")
            eq("c.id", court.id)
        }

        invalidAccessCode { Facility facility ->
            facilityAccessCodes(facility)
            lt("validTo", new Date())
        }
    }

    static validAccessCodeFor(Slot slot) {
        def accessCodes = validAccessCode(slot?.court?.facility, slot?.court, slot?.startTime)?.list(max: 1)
        if(!accessCodes.isEmpty()) {
            return accessCodes.get(0)
        }
        return null
    }

    static String validAccessCodeContentFor(Slot slot) {
        FacilityAccessCode.validAccessCode(slot.court.facility, slot.court, slot.startTime) {
            projections {
                property("content")
            }
            maxResults(1)
        }[0]
    }
}
