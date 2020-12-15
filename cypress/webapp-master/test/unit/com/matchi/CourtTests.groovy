package com.matchi

import com.matchi.membership.Membership
import com.matchi.orders.Order
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.junit.Before
import org.junit.Test

import static com.matchi.TestUtils.*

@TestFor(Court)
@Mock([Court, Sport, Facility, Region, Municipality, Sport, User, Customer, Membership, Order])
class CourtTests {

    Facility facility
    Sport sport
    User user

    @Before
    void setUp() {
        user     = createUser()
        sport    = createSport()
        facility = createFacility()
    }

    @Test
    void testSurfaceToListWithString() {
        String test = "[CLAY, HARD]"

        List<Court.Surface> surfaces = Court.Surface.toListFromString(test)

        assert surfaces.get(0) == Court.Surface.CLAY
        assert surfaces.get(1) == Court.Surface.HARD
    }


    @Test
    void testGetAllCourtsByFacilityId() {
        createCourt(facility)
        createCourt(facility)

        assert Court.countByFacility(facility) == 2
    }

    @Test
    void testUserHasAccess() {
        Court court1 = createCourt(facility, sport, 1, Court.Restriction.NONE)
        Court court2 = createCourt(facility, sport, 2, Court.Restriction.MEMBERS_ONLY)
        Court court3 = createCourt(facility, sport, 2, Court.Restriction.OFFLINE_ONLY)

        assert court1.hasUserAccess(user)
        assert !court2.hasUserAccess(user)
        assert !court3.hasUserAccess(user)

        Customer c = createCustomer(facility)
        c.user = user
        createMembership(c)

        assert court1.hasUserAccess(user)
        assert court2.hasUserAccess(user)
        assert !court2.hasUserAccess(null)
        assert !court3.hasUserAccess(user)
        assert !court3.hasUserAccess(null)
    }
}
