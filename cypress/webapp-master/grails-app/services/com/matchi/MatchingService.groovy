package com.matchi

import com.matchi.sportprofile.SportProfile
import groovy.time.TimeCategory
import groovy.transform.CompileStatic
import org.hibernate.criterion.CriteriaSpecification

class MatchingService {

    static transactional = false

    @CompileStatic
    List<MatchingUser> findMatchingUsers(User user, int amount) {
        findMatchingUsers(user, amount, new MatchingCommand(country: user.municipality?.region?.country ?: user.country))
    }

    /**
     * Returns users that match the given user.
     *
     * The matching users are returned in the order of their conformity. The first user in the returned list is
     * the best available match
     *
     * A user matches the candidate if at least one of the specified match criteria matches.
     * @param user
     * @param amount
     * @param cmd
     * @return
     */
    @CompileStatic
    List<MatchingUser> findMatchingUsers(User user, int amount, MatchingCommand cmd) {
        log.debug("Find matching users for ${user.fullName()}")

        def matchingUsers = []
        def excludeUserIds = [user.id]
        def criteriasCombinations = getCriteriasCombinations(MatchingCriteria.listApplicable(user))
        def totalWeight = MatchingCriteria.getTotalWeight()
        Sport sport = cmd?.sport ? Sport.get(cmd.sport) : null
        Municipality municipality = cmd?.municipality ? Municipality.get(cmd.municipality) : null

        while (criteriasCombinations && (amount - matchingUsers.size())) {
            def criterias = criteriasCombinations.pop()
            def matchingValue = ((Integer) criterias.sum { MatchingCriteria mc -> mc.weight }) / totalWeight

            findMatchingUsersByCriteria(user, excludeUserIds, sport, municipality, cmd?.country,
                    criterias, amount - matchingUsers.size()).each { User u ->
                matchingUsers << new MatchingUser(user: u,
                        matchingValue: Math.ceil(matchingValue * 100).toInteger())
                excludeUserIds << u.id
            }
        }

        matchingUsers
    }

    @CompileStatic
    List<MatchingUser> randomizeMatchingResult(List<MatchingUser> matches, Integer max) {
        matches.sort { Math.random() }
        matches.size() > max ? matches[0..<max] : matches
    }

    @CompileStatic
    protected List<List<MatchingCriteria>> getCriteriasCombinations(List<MatchingCriteria> criterias) {
        List<List<MatchingCriteria>> result = []

        if (criterias) {
            def n = criterias.size()
            (1..<(1 << n)).each { Number num ->
                def combination = []
                (0..<n).each { Integer idx ->
                    if (num & (1 << idx)) {
                        combination << criterias[idx]
                    }
                }
                result << combination
            }
        }

        result.sort { c ->
            c.sum { MatchingCriteria mc -> mc.weight }
        }
    }

    protected List<User> findMatchingUsersByCriteria(User user, List<Long> excludeUserIds,
            Sport sportFilter, Municipality municipalityFilter, String country,
            List<MatchingCriteria> criterias, Integer max) {

        User.createCriteria().listDistinct {
            createAlias("municipality", "municipality", CriteriaSpecification.LEFT_JOIN)
            createAlias("municipality.region", "reg", CriteriaSpecification.LEFT_JOIN)

            not {
                'in'("id", excludeUserIds)
            }

            // if filter is used
            if(sportFilter) {
                sportProfiles {
                    'in'("sport", sportFilter)
                }
            }
            if(municipalityFilter) {
                eq("municipality", municipalityFilter)
            }
            if(country) {
                or {
                    and {
                        isNull("municipality.id")
                        eq("country", country)
                    }
                    eq("reg.country", country)
                }
            }

            // make sure the user has turned on the matchable setting
            eq("matchable", true)
            eq("accountLocked", false)
            eq("enabled", true)

            criterias.each { c ->

                // users with the same sport in their sport profiles (independent of any skill level!)
                if (c == MatchingCriteria.SPORT) {
                    sportProfiles {
                        'in'("sport", user.sportProfiles.sport)
                    }

                // users with a skill level that is +/-1 of current user's skill level
                } else if (c == MatchingCriteria.SKILL) {
                    sportProfiles {
                        or {
                            user.sportProfiles.each { sp ->
                                def skillLevelRange
                                switch (sp.skillLevel) {
                                    case 1:
                                        skillLevelRange = [1, 2, 3]
                                        break
                                    case 2:
                                        skillLevelRange = [1, 2, 3]
                                        break
                                    case 9:
                                        skillLevelRange = [8, 9, 10]
                                        break
                                    case 10:
                                        skillLevelRange = [8, 9, 10]
                                        break
                                    default:
                                        skillLevelRange = [(sp.skillLevel - 2), (sp.skillLevel - 1), sp.skillLevel, (sp.skillLevel + 1), (sp.skillLevel + 2)]
                                }
                                and {
                                    eq ("sport", sp.sport)
                                    'in'("skillLevel", skillLevelRange)
                                    or {
                                        not {eq("skillLevel", 0)}
                                        not {eq("skillLevel", SportProfile.EMPTY_SKILL_LEVEL)}
                                    }
                                }
                            }
                        }
                    }

                // users with the same municipality
                } else if (c == MatchingCriteria.MUNICIPALITY) {
                    eq("municipality", user.municipality)

                    // users with the same region
                } else if (c == MatchingCriteria.REGION) {
                    eq("reg.id", user.municipality?.region?.id)

                // users with age which is less than three years older or younger
                } else if (c == MatchingCriteria.AGE) {
                    use (TimeCategory) {
                        between("birthday", user.birthday - 3.years, user.birthday + 3.years)
                    }

                    // users with the same gender
                } else if (c == MatchingCriteria.GENDER) {
                    eq("gender", user.gender)

                // users who have the same mindset in one sport as current user
                } else if (c == MatchingCriteria.MINDSET) {
                    sportProfiles {
                        or {
                            user.sportProfiles.each { sp ->
                                and {
                                    eq ("sport", sp.sport)
                                    mindSets {
                                        or {
                                            sp.mindSets.each {
                                                idEq(it.id)
                                            }
                                        }
                                    }
                                    not {isNull("mindSets")}
                                }
                            }
                        }
                    }

                // users who have the same skill level in one sport as current user
                } else if (c == MatchingCriteria.STRICT_SKILL) {
                    sportProfiles {
                        or {
                            user.sportProfiles.each { sp ->
                                and {
                                    eq ("sport", sp.sport)
                                    eq ("skillLevel", sp.skillLevel)
                                    or {
                                        not {eq("skillLevel", 0)}
                                        not {eq("skillLevel", SportProfile.EMPTY_SKILL_LEVEL)}
                                    }
                                }
                            }
                        }
                    }

                // users who are available at the same time (two availabilities overlap at least 30 minutes)
                } else if (c == MatchingCriteria.AVAILABILITY) {
                    def MINI_SLOT_SIZE = 30
                    availabilities {
                        or {
                            user.availabilities.each {
                                def weekday = it.weekday
                                def myBegin = it.begin
                                def myEnd = it.end

                                // another user is available within the same time window
                                and {
                                    eq("weekday", weekday)
                                    ge("begin", myBegin)
                                    le("end", myEnd)
                                    ge("end", myBegin.plusMinutes(MINI_SLOT_SIZE))
                                }

                                // another user's availability is before, but overlaps at least 30 minutes
                                and {
                                    eq("weekday", weekday)
                                    le("begin", myBegin)
                                    ge("end", myBegin.plusMinutes(MINI_SLOT_SIZE))
                                    le("end", myEnd)
                                }

                                // another user's availability is after, but overlaps at least 30 minutes
                                and {
                                    eq("weekday", weekday)
                                    le("begin", myEnd.minusMinutes(MINI_SLOT_SIZE))
                                    ge("end", myEnd)
                                    ge("begin", myBegin)
                                }
                            }
                        }
                    }
                }
            }

            maxResults(max)
        }
    }
}
