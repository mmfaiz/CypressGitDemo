package com.matchi.facility

import com.matchi.*
import com.matchi.membership.Membership
import com.matchi.membership.MembershipType
import com.matchi.orders.Order
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.joda.time.LocalDate
import spock.lang.Specification

import static com.matchi.TestUtils.*

@TestFor(FacilityCustomerMembersController)
@Mock([Customer, Facility, Membership, MembershipType, Municipality, Order, Region, User])
class FacilityCustomerMembersControllerTests extends Specification {

    def cashService = Mock(CashService)
    def membershipPaymentService = Mock(MembershipPaymentService)
    def memberService = Mock(MemberService)
    def securityService = Mock(SecurityService)
    def userService = Mock(UserService)

    def setup() {
        controller.cashService = cashService
        controller.membershipPaymentService = membershipPaymentService
        controller.memberService = memberService
        controller.securityService = securityService
        controller.userService = userService
    }

    void testRemoveMembership() {
        def today = new LocalDate()
        def facility = createFacility()
        def customer = createCustomer(facility)
        def membership = createMembership(customer)

        when:
        controller.removeMembership(membership.id, true)

        then:
        1 * securityService.getUserFacility() >> facility
        1 * memberService.removeMembership(_, true, new LocalDate().minusDays(1))
        response.redirectedUrl == "/facility/customers/show/$customer.id"

        when:
        response.reset()
        controller.removeMembership(12345L, true)

        then:
        response.status == 404
    }

    void testCreateMembershipForm() {
        def facility = createFacility()
        def customer = createCustomer(facility)

        when:
        controller.createMembershipForm(customer.id)

        then:
        response.status == 200

        when:
        response.reset()
        controller.createMembershipForm(12345L)

        then:
        response.status == 404
    }

    void testSaveMembership() {
        def today = new Date()
        def user = createUser()
        def facility = createFacility()
        def customer = createCustomer(facility)
        def order = createOrder(user, facility)
        order.customer = customer
        order.save(failOnError: true)
        def membershipType = createMembershipType(facility)
        def cmd = new MembershipCommand(type: membershipType, paid: true, cancel: true,
                startDate: today, endDate: today.plus(1), gracePeriodEndDate: today.plus(2),
                startingGracePeriodDays: 10)
        cmd.validate()

        when:
        controller.saveMembership(customer.id, cmd)

        then:
        1 * memberService.addMembership(customer, cmd.type, new LocalDate(cmd.startDate),
                new LocalDate(cmd.endDate), new LocalDate(cmd.gracePeriodEndDate), null, null, true, Order.ORIGIN_FACILITY,
                null, cmd.startingGracePeriodDays) >> new Membership(order: new Order(price: 100))
        1 * cashService.createCashOrderPayment(_)
        1 * memberService.disableAutoRenewal(_)
        response.redirectedUrl == "/facility/customers/show/$customer.id"

        when:
        response.reset()
        controller.saveMembership(12345L, cmd)

        then:
        response.status == 404
    }

    void testEditMembershipForm() {
        def today = new LocalDate()
        def facility = createFacility()
        def customer = createCustomer(facility)
        def membership = createMembership(customer)

        messageSource.addMessage("date.format.dateOnly", new Locale("en"), "yyyy-MM-dd")

        when:
        controller.editMembershipForm(membership.id)

        then:
        1 * securityService.getUserFacility() >> facility
        response.status == 200

        when:
        response.reset()
        controller.editMembershipForm(12345L)

        then:
        response.status == 404
    }

    void testUpdateMembership() {
        def today = new Date()
        def facility = createFacility()
        def customer = createCustomer(facility)
        def membershipType1 = createMembershipType(facility)
        def membershipType2 = createMembershipType(facility)
        def membership = createMembership(customer, new LocalDate(today), new LocalDate(today.plus(1)),
                new LocalDate(today.plus(2)), membershipType1, true)
        def cmd = new MembershipCommand(type: membershipType2,
                startDate: today.minus(10), endDate: today.plus(10),
                gracePeriodEndDate: today.plus(20))
        cmd.validate()

        when: "changes in dates overlaps other membership"
        controller.updateMembership(membership.id, cmd)

        then: "response is redirected without any changes saved"
        1 * securityService.getUserFacility() >> facility
        1 * memberService.isMembershipOverlapping(_, _, _, _) >> true
        0 * memberService.updateMembership(_, _)
        response.redirectedUrl == "/facility/customers/show/$customer.id"
        assert Membership.findByStartDate(new LocalDate(today))

        when: "there is no overlap"
        response.reset()
        controller.updateMembership(membership.id, cmd)

        then: "changes are saved"
        1 * securityService.getUserFacility() >> facility
        1 * memberService.isMembershipOverlapping(_, _, _, _) >> false
        1 * memberService.updateMembership(_, _) >> membership
        response.redirectedUrl == "/facility/customers/show/$customer.id"

        when: "membership id is invalid"
        response.reset()
        controller.updateMembership(12345L, cmd)

        then:
        response.status == 404
    }
}
