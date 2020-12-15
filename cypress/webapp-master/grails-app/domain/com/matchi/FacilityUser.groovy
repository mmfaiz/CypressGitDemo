package com.matchi

class FacilityUser implements Serializable {

    private static final long serialVersionUID = 12L

    Facility facility

    static belongsTo = [user: User]

    static hasMany = [facilityRoles: FacilityUserRole]

    static mapping = {
        cache true
    }

    static constraints = {
        facility bindable: false
        user unique: "facility"
        facilityRoles nullable: false, minSize: 1
    }
}
