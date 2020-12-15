package com.matchi

/**
 * @author Sergei Shushkevich
 */
enum MatchingCriteria {

    SPORT(1000, {it.sportProfiles}),
    SKILL(900, {it.sportProfiles}),
    MUNICIPALITY(800, {it.municipality}),
    REGION(700, {it.municipality}),
    AGE(600, {it.birthday}),
    GENDER(500, {it.gender}),
    MINDSET(400, {it.sportProfiles}),
    STRICT_SKILL(300, {it.sportProfiles}),
    AVAILABILITY(200, {it.availabilities})

    final int weight
    final Closure isApplicable

    MatchingCriteria(int weight, Closure isApplicable) {
        this.weight = weight
        this.isApplicable = isApplicable
    }

    static List<MatchingCriteria> listApplicable(User user) {
        values().findAll {
            it.isApplicable(user)
        }
    }

    static int getTotalWeight() {
        values().sum { it.weight }
    }
}