package com.matchi.sportprofile

class SportProfileMindset {

    static belongsTo = SportProfile
    static hasMany = [ sportProfiles: SportProfile]

    String name
    String badgeColor

    static constraints = {
        name(nullable: false, unique: true)
    }

    static mapping = {
        sort "name"
        sportProfiles joinTable:[name:"sport_profile_mindsets", key:'sport_profile_mindset_id' ]
    }

    String toString() { "$name" }
}
