package com.matchi.sportprofile

class SportProfileAttribute {

    static int EMPTY_SKILL_LEVEL = -99

    def static belongsTo = [ sportAttribute: SportAttribute, sportProfile:SportProfile]

    int skillLevel = EMPTY_SKILL_LEVEL

    static constraints = {
    }
}
