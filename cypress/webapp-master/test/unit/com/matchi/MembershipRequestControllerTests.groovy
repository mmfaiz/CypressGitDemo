package com.matchi

import com.matchi.membership.Membership
import com.matchi.membership.MembershipType
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.junit.Before

/**
 * See the API for {@link grails.test.mixin.web.ControllerUnitTestMixin} for usage instructions
 */
@TestFor(MembershipRequestController)
@Mock([Facility, FacilityProperty, MembershipType, Membership])
class MembershipRequestControllerTests {
    Facility facility
    Membership membership

    @Before
    void setUp() {
        defineBeans {
            dateUtil(DateUtil) {
                grailsApplication = [config: [customer: [personalNumber: [settings: [:]]]]]
            }
        }

        facility = new Facility(shortname: "Test", language: "sv").save(validate: false)
        new FacilityProperty(key: FacilityProperty.FacilityPropertyKey.FEATURE_MEMBERSHIP_REQUEST_PAYMENT.name(), value: "1", facility: facility).save()
        controller.springSecurityService = [getCurrentUser: { -> new User() }]
        controller.notificationService = [sendMembershipRequestNotification: { membership, facility, message -> }]
        controller.memberService = [requestMembership: { user, facility, membershipType, status ->
            membership = new Membership(type: membershipType, status: status, customer: new Customer()).save(validate: false)
        }, isUpcomingMembershipAvailableForPurchase: { m -> false }, getAvailableMembershipTypesWithDates: { a, b, c, d -> [] }]

        controller.facilityService = [getAllHierarchicalFacilities: { a -> [a] }]
    }

    void testRequestWithPayment() {
        def membershipType = new MembershipType(id: 1, name: "Test", facility: facility).save(validate: false)
        def membershipRequestCommand = new MembershipRequestCommand(sname: facility.shortname,
                items: [new MembershipRequestItemCommand(firstname: "Michael", lastname: "A",
                        email: "mail@test.co", address: "Street 1", zipcode: "123456",
                        type: Customer.CustomerType.MALE, city: "Minsk", country: "BY",
                        telephone: "+12611616846416", birthday: "870227", securitynumber: "6000",
                        membershipType: membershipType)]
        )

        def customerServiceControl = mockFor(CustomerService)
        customerServiceControl.demand.findHierarchicalUserCustomers { a, b -> new Customer() }
        customerServiceControl.demand.getOrCreateUserCustomer { u, f -> new Customer() }
        customerServiceControl.demand.updateCustomer { c, cmd -> c }
        controller.customerService = customerServiceControl.createMock()

        controller.request(membershipRequestCommand)

        assert 1l == model.membershipTypeToPay.id
        assert '/membershipRequest/index' == view
        customerServiceControl.verify()
    }
}
