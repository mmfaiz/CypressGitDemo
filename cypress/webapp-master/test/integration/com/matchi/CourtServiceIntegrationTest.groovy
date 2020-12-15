package com.matchi

import grails.test.MockUtils
import grails.transaction.Transactional
import org.junit.Before
import org.junit.Test

import static com.matchi.TestUtils.*

class CourtServiceIntegrationTest extends GroovyTestCase {

    CourtService courtService
    Sport sport
    Sport sportSecond
    Facility facility
    Court court1
    Court court2
    Court court3
    Court court4

    void setUp() {
        MockUtils.mockLogging(CourtService, true)

        sport       = createSport()
        sportSecond = createSport()
        facility    = createFacility()

        court1 = createCourt(facility, sport, 1, Court.Restriction.NONE, Court.Surface.HARD, true)
        court2 = createCourt(facility, sport, 2, Court.Restriction.NONE, Court.Surface.CLAY, false)
        court3 = createCourt(facility, sport, 3, Court.Restriction.MEMBERS_ONLY, Court.Surface.CLAY, false)
        court4 = createCourt(facility, sportSecond, 4, Court.Restriction.MEMBERS_ONLY, Court.Surface.HARD, false)

        facility.addToCourts(court1)
        facility.addToCourts(court2)
        facility.addToCourts(court3)
        facility.addToCourts(court4)
    }

    @Test
    void testAllSportCourts() {
        def courts = courtService.findCourts([facility], [sport], null, null)
        assert courts.size() == 3
    }

    @Test
    void testAllSportCourtsFilteredOnSurface() {
        def courts = courtService.findCourts([facility], [sport], [Court.Surface.CLAY], null)
        assert courts.size() == 2
    }

    @Test
    void testAllUsersCourts() {
        def user = createUser("test1@test.com")
        addUserMembership(user)
        def courts = courtService.findUsersCourts([facility], [sport], null, null, user)
        assert courts.size() == 3
    }

    @Test
    void testOnlyIndoorCourts() {
        def user = createUser("test2@test.com")
        addUserMembership(user)
        def courts = courtService.findUsersCourts([facility], [sport], null, true, user)
        assert courts.size() == 1
    }

    @Test
    void testOnlyOutdoorCourts() {
        def user = createUser("test3@test.com")
        addUserMembership(user)
        def courts = courtService.findUsersCourts([facility], [sport], null, false, user)
        assert courts.size() == 2
    }

    @Test
    void testNonMembersDoesNotGetPrivateCourts() {
        def user = createUser("test4@test.com")
        def courts = courtService.findUsersCourts([facility], [sport], null, null, user)
        assert courts.size() == 2
    }

    @Test
    void testSwapListPosition() {
        def pos1 = court1.listPosition
        def pos2 = court2.listPosition
        def pos3 = court3.listPosition

        courtService.swapListPosition(court1, court2)

        assert pos2 == court1.listPosition
        assert pos1 == court2.listPosition

        courtService.swapListPosition(court1, court3)

        assert pos3 == court1.listPosition
        assert pos2 == court3.listPosition
    }

    @Test
    void testFindMembersCourts() {
        def courts = courtService.findMembersCourts([facility], [sport], null, null)
        assert 1 == courts.size()
        assert court3 == courts[0]
    }

    @Test
    @Transactional
    void testAllPossibleValuesSportCourts() {
        assert 4 == courtService.findCourts([facility]).size()
        assert 4 == courtService.findCourts([facility], null, null, null).size()
        assert 3 == courtService.findCourts([facility], [sport], null, null).size()
        assert 4 == courtService.findCourts([facility], [sport, sportSecond], null, null).size()
        assert 2 == courtService.findCourts([facility], null, [Court.Surface.CLAY], null).size()
        assert 4 == courtService.findCourts([facility], null, [Court.Surface.CLAY, Court.Surface.HARD], null).size()
        assert 1 == courtService.findCourts([facility], null, null, true).size()
        assert 3 == courtService.findCourts([facility], null, null, false).size()

        assert 3 == courtService.findCourts([facility], [sport], null, null).size()
        assert 4 == courtService.findCourts([facility], [sport, sportSecond], null, null).size()
        assert 4 == courtService.findCourts([facility], null, [Court.Surface.CLAY, Court.Surface.HARD], null).size()
        assert 2 == courtService.findCourts([facility], null, [Court.Surface.CLAY], null).size()
        assert 1 == courtService.findCourts([facility], null, null, true).size()
        assert 3 == courtService.findCourts([facility], null, null, false).size()

        assert 2 == courtService.findCourts([facility], [sport], [Court.Surface.CLAY], null).size()
        assert 2 == courtService.findCourts([facility], [sport, sportSecond], [Court.Surface.CLAY], null).size()
        assert 3 == courtService.findCourts([facility], [sport], [Court.Surface.CLAY, Court.Surface.HARD], null).size()
        assert 4 == courtService.findCourts([facility], [sport, sportSecond], [Court.Surface.CLAY, Court.Surface.HARD], null).size()

        assert 1 == courtService.findCourts([facility], [sport], null, true).size()
        assert 1 == courtService.findCourts([facility], [sport, sportSecond], null, true).size()
        assert 2 == courtService.findCourts([facility], [sport], null, false).size()
        assert 3 == courtService.findCourts([facility], [sport, sportSecond], null, false).size()

        assert 0 == courtService.findCourts([facility], null, [Court.Surface.CLAY], true).size()
        assert 1 == courtService.findCourts([facility], null, [Court.Surface.CLAY, Court.Surface.HARD], true).size()
        assert 2 == courtService.findCourts([facility], null, [Court.Surface.CLAY], false).size()
        assert 3 == courtService.findCourts([facility], null, [Court.Surface.CLAY, Court.Surface.HARD], false).size()

        assert 0 == courtService.findCourts([facility], [sport], [Court.Surface.CLAY], true).size()
        assert 0 == courtService.findCourts([facility], [sport, sportSecond], [Court.Surface.CLAY], true).size()
        assert 1 == courtService.findCourts([facility], [sport], [Court.Surface.CLAY, Court.Surface.HARD], true).size()
        assert 1 == courtService.findCourts([facility], [sport, sportSecond], [Court.Surface.CLAY, Court.Surface.HARD], true).size()

        assert 2 == courtService.findCourts([facility], [sport], [Court.Surface.CLAY], false).size()
        assert 2 == courtService.findCourts([facility], [sport, sportSecond], [Court.Surface.CLAY], false).size()
        assert 2 == courtService.findCourts([facility], [sport], [Court.Surface.CLAY, Court.Surface.HARD], false).size()
        assert 3 == courtService.findCourts([facility], [sport, sportSecond], [Court.Surface.CLAY, Court.Surface.HARD], false).size()
    }

    def addUserMembership(user) {
        Customer c = createCustomer(facility)
        c.user = user
        createMembership(c)
    }
}
