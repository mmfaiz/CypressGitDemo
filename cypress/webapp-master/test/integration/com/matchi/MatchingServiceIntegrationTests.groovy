package com.matchi

import com.matchi.sportprofile.SportProfile
import com.matchi.sportprofile.SportProfileMindset
import org.joda.time.LocalTime

import static com.matchi.TestUtils.*

/**
 * @author Sergei Shushkevich
 */
class MatchingServiceIntegrationTests extends GroovyTestCase {

    def matchingService

    void testFindMatchingUsers() {
        def region1 = createRegion()
        def municipality1 = createMunicipality(region1)
        def municipality2 = createMunicipality()
        def sport1 = createSport()
        def sport2 = createSport()
        def mindSet = new SportProfileMindset(name: "mindset", badgeColor: "red").save(failOnError: true, flush: true)
        def beginTime = new LocalTime("10:00")
        def endTime = new LocalTime("18:00")
        def user1 = createUser()
        def user2 = createUser("jane@matchi.com")

        // no results, because matchable = false
        assert !matchingService.findMatchingUsers(user1, 10)
        assert !matchingService.findMatchingUsers(user2, 10)

        user1.matchable = true
        user1.save(failOnError: true, flush: true)
        user2.matchable = true
        user2.save(failOnError: true, flush: true)

        // user2 matches by gender - 10%
        def result = matchingService.findMatchingUsers(user1, 10)
        assert result.size() == 1
        assert result[0].user.id == user2.id
        assert result[0].matchingValue == 10

        // the same result for user2 - user1 matches by gender only
        result = matchingService.findMatchingUsers(user2, 10)
        assert result.size() == 1
        assert result[0].user.id == user1.id
        assert result[0].matchingValue == 10

        user1.gender = null
        user1.save(failOnError: true, flush: true)

        // no matched properties
        assert !matchingService.findMatchingUsers(user1, 10)
        assert !matchingService.findMatchingUsers(user2, 10)

        user2.birthday = new Date()
        user2.municipality = municipality1
        user2.addToSportProfiles(new SportProfile(sport: sport1, user: user2, skillLevel: 5)
                .addToMindSets(mindSet))
                .addToAvailabilities(new Availability(weekday: 1, begin: beginTime, end: endTime))
                .save(failOnError: true, flush: true)

        // user2 has complete profile, but still no matched properties with user1
        assert !matchingService.findMatchingUsers(user1, 10)
        assert !matchingService.findMatchingUsers(user2, 10)

        user1.municipality = municipality1
        user1.save(failOnError: true, flush: true)

        // user2 matches by municipality and region - 28%
        result = matchingService.findMatchingUsers(user1, 10)
        assert result.size() == 1
        assert result[0].user.id == user2.id
        assert result[0].matchingValue == 28

        user1.addToSportProfiles(new SportProfile(sport: sport1, user: user1, skillLevel: 5)
                .addToMindSets(mindSet))
                .save(failOnError: true, flush: true)

        // + sport, level, mindset - 76%
        result = matchingService.findMatchingUsers(user1, 10)
        assert result.size() == 1
        assert result[0].user.id == user2.id
        assert result[0].matchingValue == 76

        user1.gender = User.Gender.male
        user1.birthday = new Date()
        user1.save(failOnError: true, flush: true)

        // + gender, age - 97%
        result = matchingService.findMatchingUsers(user1, 10)
        assert result.size() == 1
        assert result[0].user.id == user2.id
        assert result[0].matchingValue == 97

        user1.addToAvailabilities(new Availability(weekday: 1, begin: beginTime, end: endTime))
                .save(failOnError: true, flush: true)

        // + availability - 100%
        result = matchingService.findMatchingUsers(user1, 10)
        assert result.size() == 1
        assert result[0].user.id == user2.id
        assert result[0].matchingValue == 100

        def user3 = createUser("paul@matchi.com")
        user3.matchable = true
        user3.country = "SE"
        user3.save(failOnError: true, flush: true)

        // test with multiple matching users
        result = matchingService.findMatchingUsers(user1, 10)
        assert result.size() == 2
        assert result[0].user.id == user2.id
        assert result[0].matchingValue == 100
        assert result[1].user.id == user3.id
        assert result[1].matchingValue == 10

        user3.municipality = municipality2
        user3.addToSportProfiles(new SportProfile(sport: sport2, user: user3, skillLevel: 5))
                .save(failOnError: true, flush: true)

        // even if user3 has different sport and municipality, he still matches by gender
        result = matchingService.findMatchingUsers(user1, 10)
        assert result.size() == 2
        assert result[0].user.id == user2.id
        assert result[1].user.id == user3.id

        // test amount limit
        result = matchingService.findMatchingUsers(user1, 1)
        assert result.size() == 1
        assert result[0].user.id == user2.id

        // test filtering by sport
        result = matchingService.findMatchingUsers(
                user1, 10, new MatchingCommand(sport: sport2.id))
        assert result.size() == 1
        assert result[0].user.id == user3.id

        // test filtering by municipality
        result = matchingService.findMatchingUsers(
                user1, 10, new MatchingCommand(municipality: municipality1.id))
        assert result.size() == 1
        assert result[0].user.id == user2.id
    }
}