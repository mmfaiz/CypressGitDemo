package com.matchi

class FacilityGroup implements Serializable {
    private static final long serialVersionUID = 12L

    Facility masterFacility

    Set facilities

    static hasMany = [facilities: Facility]

    static constraints = {

    }

    static mapping = {
        table "facility_hierarchy_group"
        facilities joinTable: [name: "facility_hierarchy_group_facilities", key: "facility_group_id", column: "facility_id"]
    }
}
