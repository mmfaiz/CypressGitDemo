package com.matchi.facility

import com.matchi.Customer
import com.matchi.Facility
import com.matchi.Municipality
import com.matchi.Region
import com.matchi.SecurityService
import com.matchi.User
import com.matchi.membership.Membership
import com.matchi.membership.MembershipType
import com.matchi.orders.Order
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.joda.time.LocalDate
import org.junit.Before
import static com.matchi.TestUtils.createCustomer
import static com.matchi.TestUtils.createMembership
import static com.matchi.TestUtils.createMembershipType

@TestFor(FacilityMembershipTypeController)
@Mock([Customer, Facility, Membership, MembershipType, Municipality, Order, Region, User])
class FacilityMembershipTypeControllerTests {
    def membershipType
    def membership

    @Before
    public void setUp() {
        membershipType = createMembershipType()
        def customer = createCustomer(membershipType.facility)
        def serviceControl = mockFor(SecurityService)
        serviceControl.demand.getUserFacility { -> membershipType.facility }
        controller.securityService = serviceControl.createMock()
        membership = createMembership(customer)
        membership.type = membershipType
        membership.gracePeriodEndDate = new LocalDate().plusDays(1)
        membership.save()
    }

    void testMemberShipTypeWithMembershipIsNotDeletable() {
        params.id = membershipType.id
        controller.cancel()
        assert response.redirectedUrl == "/facility/membertypes/index"
        assert flash.error.length() > 0
    }
}