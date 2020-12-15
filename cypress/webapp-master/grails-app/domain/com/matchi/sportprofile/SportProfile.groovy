package com.matchi.sportprofile

import com.matchi.Sport
import com.matchi.User

class SportProfile {

    static int EMPTY_SKILL_LEVEL = -99
    static def skillLevelRange = 1..10

    static belongsTo = [ Sport, User ]
    static hasMany = [ sportProfileAttributes:SportProfileAttribute, mindSets:SportProfileMindset ]

    Sport sport
    User user

    int skillLevel = EMPTY_SKILL_LEVEL

    Frequency frequency

    static SportProfile link(user, sport, skillLevel, spMindSets) {
        def sp = findByUserAndSport(user, sport)
        if (!sp) {
            sp = new SportProfile()
            if (skillLevelRange.contains(skillLevel)) {
                sp.skillLevel = skillLevel
            } else {
                sp.skillLevel = 0
            }
            spMindSets.each {
                sp.addToMindSets(SportProfileMindset.findByName(it))
            }
            user?.addToSportProfiles(sp)
            sport?.addToSportProfiles(sp)
            sp.save()
        }
        return sp
    }

    static constraints = {
        sport(nullable: false)
        mindSets(nullable: true)
        frequency(nullable: true)
    }

    static mapping = {
        sportProfileAttributes sort: 'id', order: 'asc'
        mindSets joinTable:[name:"sport_profile_mindsets", key:'sport_profile_id' ]
    }

    def getSportProfileAttribute(def sportAttributeId) {
        return sportProfileAttributes.find { it.sportAttribute.id == sportAttributeId }
    }

    def getSportProfileMindset(def mindSetId) {
        return mindSets.find { it.id == mindSetId }
    }

    public static enum Frequency {
        YEARLY,
        MONTHLY,
        WEEKLY,
        WEEKLY_FREQUENT

        static list() {
            return [YEARLY,MONTHLY,WEEKLY,WEEKLY_FREQUENT]
        }
    }
}
