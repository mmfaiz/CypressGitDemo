package com.matchi

class CourtGroup implements Serializable {

    static belongsTo = [ facility: Facility ]
    static hasMany = [ courts : Court ]

    String  name
    Integer tabPosition
    Date    dateCreated
    Date    lastUpdated
    Boolean visible = true
    Integer maxNumberOfBookings

    static constraints = {
        name blank: false, maxSize: 255
        tabPosition nullable: true
        maxNumberOfBookings nullable: true
        visible nullable: false, default: true
    }

    static mapping = {
        courts joinTable: [ name: "court_groups", key: 'group_id', column: 'court_id' ]
        autoTimestamp true
        sort tabPosition: "asc"
    }

    static namedQueries = {
        facilityCourtGroups { Facility f ->
            eq("facility", f)

            order("tabPosition", "asc")
        }
    }

    def beforeInsert() {
        if (tabPosition == null) {
            def max = withCriteria(uniqueResult: true) {
                projections {
                    max("tabPosition")
                }
                eq("facility", facility)
            }
            tabPosition = max ? max + 1 : 1
        }
    }
}
