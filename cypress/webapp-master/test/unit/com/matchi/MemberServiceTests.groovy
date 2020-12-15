package com.matchi

import com.matchi.MembershipPaymentService
import com.matchi.enums.MembershipRequestSetting
import com.matchi.facility.MembershipCommand
import com.matchi.integration.IntegrationService
import com.matchi.membership.Membership
import com.matchi.membership.MembershipFamily
import com.matchi.membership.MembershipType
import com.matchi.membership.TimeUnit
import com.matchi.orders.AdyenOrderPayment
import com.matchi.orders.CashOrderPayment
import com.matchi.orders.Order
import com.matchi.orders.OrderPayment
import com.matchi.payment.PaymentMethod
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.joda.time.LocalDate
import spock.lang.Specification

import static com.matchi.TestUtils.*

@TestFor(MemberService)
@Mock([User, Customer, Facility, Membership, Region, Municipality, MembershipFamily,
        MembershipType, Order, CashOrderPayment, AdyenOrderPayment])
class MemberServiceTests extends Specification {

    def cashService = Mock(CashService)
    def membersFamilyService = Mock(MembersFamilyService)
    def membershipPaymentService = Mock(MembershipPaymentService)
    def notificationService = Mock(NotificationService)
    def userService = Mock(UserService)
    def integrationService = mockFor(IntegrationService)

    def setup() {
        service.cashService = cashService
        service.membersFamilyService = membersFamilyService
        service.membershipPaymentService = membershipPaymentService
        service.notificationService = notificationService
        service.userService = userService

        service.orderStatusService = new OrderStatusService()
        service.orderStatusService.integrationService = integrationService.createMock()
        integrationService.demand.send(1..1) {  }
    }

    void testAddMembership() {
        def today = new LocalDate()
        def facility = createFacility()
        def customer = createCustomer(facility)
        def mt = createMembershipType(facility)
        def issuer = createUser("issuer@matchi.se")

        when:
        def membership = service.addMembership(customer, null)

        then:
        1 * userService.getLoggedInUser() >> issuer
        1 * membershipPaymentService.createMembershipPaymentOrder(_, null, _, _, _) >>
                new Order(article: Order.Article.MEMBERSHIP, user: issuer, issuer: issuer,
                        description: "desc", facility: facility, dateDelivery: new Date(),
                        customer: customer).save(failOnError: true)

        membership
        !membership.type
        membership.customer == customer
        membership.startDate == today
        membership.endDate == today.plusYears(1).minusDays(1)
        membership.gracePeriodEndDate == membership.endDate
        membership.order
        membership.order.status == Order.Status.COMPLETED
        membership.order.price == 0
        !membership.autoPay
        Membership.count() == 1

        when:
        membership = service.addMembership(customer, mt, today.plusYears(1),
                today.plusYears(1).plusDays(1), today.plusYears(1).plusDays(2))

        then:
        1 * userService.getLoggedInUser() >> issuer
        1 * membershipPaymentService.createMembershipPaymentOrder(_, _, _, _, _) >>
                new Order(article: Order.Article.MEMBERSHIP, user: issuer, issuer: issuer,
                        description: "desc", facility: facility, dateDelivery: new Date(),
                        customer: customer, price: mt.price).save(failOnError: true)
        membership
        membership.type == mt
        membership.customer == customer
        membership.startDate == today.plusYears(1)
        membership.endDate == today.plusYears(1).plusDays(1)
        membership.gracePeriodEndDate == today.plusYears(1).plusDays(2)
        membership.order
        membership.order.status == Order.Status.NEW
        membership.order.price == mt.price
        !membership.autoPay
        Membership.count() == 2

        when: "customer is already a member for the same start date"
        membership = service.addMembership(customer, null)

        then: "null is returned"
        !membership

        when: "add membership with predefined order"
        def customer2 = createCustomer(facility)
        def order2 = createOrder(issuer, facility)
        membership = service.addMembership(customer2, mt, today, order2)

        then: "order is assigned to a new membership"
        1 * userService.getLoggedInUser() >> issuer
        membership
        membership.type == mt
        membership.order.id == order2.id
        !membership.autoPay
        Membership.count() == 3

        when: "add membership with predefined order and auto-pay option"
        def customer3 = createCustomer(facility)
        def order3 = createOrder(issuer, facility, Order.Article.MEMBERSHIP,
                [(Order.META_ALLOW_RECURRING): "true"])
        membership = service.addMembership(customer3, mt, today, order3)

        then: "order is assigned to a new membership but autoPay is disabled, because issue should be a customer's user"
        1 * userService.getLoggedInUser() >> issuer
        membership
        membership.type == mt
        membership.order.id == order3.id
        !membership.autoPay
        Membership.count() == 4

        when: "add membership with predefined order and auto-pay option"
        def customer4 = createCustomer(facility)
        customer4.user = issuer
        customer4.save(failOnError: true)
        def order4 = createOrder(issuer, facility, Order.Article.MEMBERSHIP,
                [(Order.META_ALLOW_RECURRING): "true"])
        membership = service.addMembership(customer4, mt, today, order4)

        then: "order is assigned to a new membership but autoPay is enabled"
        1 * userService.getLoggedInUser() >> issuer
        membership
        membership.type == mt
        membership.order.id == order4.id
        membership.autoPay
        Membership.count() == 5
    }

    void testRemoveMembership() {
        def today = new LocalDate()
        def customer = createCustomer()
        def membership = createMembership(customer, today, today)
        def payment = new CashOrderPayment(issuer: createUser(), amount: 100,
                status: OrderPayment.Status.CAPTURED).save(failOnError: true)
        membership.order.payments = [payment]
        membership.order.save(failOnError: true)
        new MembershipFamily(contact: customer).addToMembers(membership)
                .save(failOnError: true)

        when:
        service.removeMembership(membership)

        then:
        1 * membersFamilyService.removeFamilyMember(_)
        Membership.count() == 1
        def m = Membership.first()
        m.activated == true
        m.cancel == false
        m.startDate == today
        m.endDate == today
        m.gracePeriodEndDate == today
        payment.status == OrderPayment.Status.CAPTURED
    }

    void testRemoveMembershipWithRefundButNonRefundableOrder() {
        def today = new LocalDate()
        def customer = createCustomer()
        def membership = createMembership(customer, today, today)
        def payment = new AdyenOrderPayment(issuer: createUser(), amount: 100,
                status: OrderPayment.Status.CAPTURED, method: PaymentMethod.CREDIT_CARD).save(failOnError: true)
        membership.order.payments = [payment]
        membership.order.dateDelivery = today.minusDays(3).toDate()
        membership.order.save(failOnError: true)
        new MembershipFamily(contact: customer).addToMembers(membership)
                .save(failOnError: true)

        when:
        service.removeMembership(membership, true)

        then:
        1 * membersFamilyService.removeFamilyMember(_)
        Membership.count() == 1
        def m = Membership.first()
        m.activated == true
        m.cancel == false
        m.startDate == today
        m.endDate == today
        m.gracePeriodEndDate == today
        m.order.status == Order.Status.COMPLETED
        payment.status == OrderPayment.Status.CAPTURED
    }

    void testRemoveMembershipWithForceEndDate() {
        def today = new LocalDate()
        def forceEndDate = today.plusDays(10)
        def customer = createCustomer()
        def membership = createMembership(customer, today, today)
        def payment = new CashOrderPayment(issuer: createUser(), amount: 100,
                status: OrderPayment.Status.CAPTURED).save(failOnError: true)
        membership.order.payments = [payment]
        membership.order.save(failOnError: true)
        new MembershipFamily(contact: customer).addToMembers(membership)
                .save(failOnError: true)

        when:
        service.removeMembership(membership, false, forceEndDate)

        then:
        1 * membersFamilyService.removeFamilyMember(_)
        Membership.count() == 1
        def m = Membership.first()
        m.activated == true
        m.cancel == false
        m.startDate == today
        m.endDate == today
        m.gracePeriodEndDate == today
        payment.status == OrderPayment.Status.CAPTURED
    }

    void testRemoveMembershipWithForceEndDate2() {
        def today = new LocalDate()
        def forceEndDate = today.minusDays(10)
        def customer = createCustomer()
        def membership = createMembership(customer)
        def payment = new CashOrderPayment(issuer: createUser(), amount: 100,
                status: OrderPayment.Status.CAPTURED).save(failOnError: true)
        membership.order.payments = [payment]
        membership.order.save(failOnError: true)
        new MembershipFamily(contact: customer).addToMembers(membership)
                .save(failOnError: true)

        when:
        service.removeMembership(membership, false, forceEndDate)

        then:
        1 * membersFamilyService.removeFamilyMember(_)
        Membership.count() == 1
        def m = Membership.first()
        m.activated == true
        m.cancel == false
        m.startDate == forceEndDate
        m.endDate == forceEndDate
        m.gracePeriodEndDate == forceEndDate
        payment.status == OrderPayment.Status.CAPTURED
    }

    void testRequestMembershipAddActiveMember() {
        def facility = createFacility()
        facility.recieveMembershipRequests = true
        facility.membershipRequestSetting = MembershipRequestSetting.DIRECT
        facility.save(failOnError: true)
        def customer = createCustomer(facility)
        def user = createUser()

        when:
        def membership = service.requestMembership(customer, null)

        then:
        1 * userService.getLoggedInUser() >> user
        1 * membershipPaymentService.createMembershipPaymentOrder(_, null, _, _, _) >>
                new Order(price: 100, article: Order.Article.MEMBERSHIP, user: user, issuer: user,
                        description: "desc", facility: facility, dateDelivery: new Date(),
                        customer: customer).save(failOnError: true)
        0 * cashService.createCashOrderPayment(_)
        membership
        membership.activated
        !membership.startingGracePeriodDays
    }

    void testRequestMembershipAddActiveMemberWithGracePeriod() {
        def facility = createFacility()
        facility.recieveMembershipRequests = true
        facility.membershipRequestSetting = MembershipRequestSetting.DIRECT
        facility.membershipStartingGraceNrOfDays = 15
        facility.save(failOnError: true)
        def customer = createCustomer(facility)
        def user = createUser()

        when:
        def membership = service.requestMembership(customer, null)

        then:
        1 * userService.getLoggedInUser() >> user
        1 * membershipPaymentService.createMembershipPaymentOrder(_, null, _, _, _) >>
                new Order(price: 100, article: Order.Article.MEMBERSHIP, user: user, issuer: user,
                        description: "desc", facility: facility, dateDelivery: new Date(),
                        customer: customer).save(failOnError: true)
        0 * cashService.createCashOrderPayment(_)
        membership
        membership.activated
        membership.startingGracePeriodDays == 15
    }

    void testRequestMembershipAddPendingMember() {
        def facility = createFacility()
        facility.recieveMembershipRequests = true
        facility.membershipRequestSetting = MembershipRequestSetting.MANUAL
        facility.save(failOnError: true)
        def customer = createCustomer(facility)
        def user = createUser()

        when:
        def membership = service.requestMembership(customer, null)

        then:
        1 * userService.getLoggedInUser() >> user
        1 * membershipPaymentService.createMembershipPaymentOrder(_, null, _, _, _) >>
                new Order(price: 100, article: Order.Article.MEMBERSHIP, user: user, issuer: user,
                        description: "desc", facility: facility, dateDelivery: new Date(),
                        customer: customer).save(failOnError: true)
        membership
        !membership.activated
        !membership.startingGracePeriodDays
    }

    void testDisableAutoRenewal() {
        def today = new LocalDate()
        def membership = createMembership(createCustomer(), today,
                today.plusDays(1), today.plusDays(2))

        when:
        service.disableAutoRenewal(membership)

        then:
        membership.cancel
        membership.gracePeriodEndDate == membership.endDate
    }

    void testEnableAutoRenewal() {
        def today = new LocalDate()
        def membership = createMembership(createCustomer(), today, today, today, null, true)

        when:
        service.enableAutoRenewal(membership)

        then:
        !membership.cancel
    }

    void testIsMembershipOverlapping() {
        def c1 = createCustomer()
        def c2 = createCustomer()
        def today = new LocalDate()
        def m = createMembership(c1, today, today.plusDays(10), today.plusDays(20))

        expect:
        service.isMembershipOverlapping(c1, today.minusDays(10), today.minusDays(1)) == false
        service.isMembershipOverlapping(c1, today.minusDays(10), today) == true
        service.isMembershipOverlapping(c1, today, today) == true
        service.isMembershipOverlapping(c1, today.plusDays(5), today.plusDays(6)) == true
        service.isMembershipOverlapping(c1, today, today.plusDays(10)) == true
        service.isMembershipOverlapping(c1, today.plusDays(10), today.plusDays(20)) == true
        service.isMembershipOverlapping(c1, today.plusDays(11), today.plusDays(20)) == false
        service.isMembershipOverlapping(c1, today.minusDays(100), today.plusDays(100)) == true

        service.isMembershipOverlapping(c1, today.minusDays(10), today, m.id) == false
        service.isMembershipOverlapping(c1, today, today, m.id) == false
        service.isMembershipOverlapping(c1, today.plusDays(5), today.plusDays(6), m.id) == false
        service.isMembershipOverlapping(c1, today, today.plusDays(10), m.id) == false
        service.isMembershipOverlapping(c1, today.plusDays(10), today.plusDays(20), m.id) == false
        service.isMembershipOverlapping(c1, today.minusDays(100), today.plusDays(100), m.id) == false

        service.isMembershipOverlapping(c2, today.minusDays(10), today) == false
        service.isMembershipOverlapping(c2, today, today) == false
        service.isMembershipOverlapping(c2, today.plusDays(5), today.plusDays(6)) == false
        service.isMembershipOverlapping(c2, today, today.plusDays(10)) == false
        service.isMembershipOverlapping(c2, today.plusDays(10), today.plusDays(20)) == false
        service.isMembershipOverlapping(c2, today.minusDays(100), today.plusDays(100)) == false
    }

    void testUpdateMembership() {
        def today = new LocalDate()
        def facility = createFacility()
        def customer = createCustomer(facility)
        def issuer = createUser()
        def membershipType2 = createMembershipType(facility)
        def membership = createMembership(customer, today, today.plusDays(1),
                today.plusDays(2), null, true)
        def cmd = new MembershipCommand(paid: true, type: membershipType2,
                startDate: today.minusDays(10).toDate(), endDate: today.plusDays(10).toDate(),
                gracePeriodEndDate: today.plusDays(20).toDate())

        when:
        service.updateMembership(membership, cmd)

        then:
        1 * userService.getLoggedInUser() >> issuer
        1 * membershipPaymentService.createMembershipPaymentOrder(_, _, _, _, _) >>
                new Order(article: Order.Article.MEMBERSHIP, user: issuer, issuer: issuer,
                        description: "desc", facility: customer.facility,
                        dateDelivery: new Date(), customer: customer, price: 100)
                        .save(failOnError: true, flush: true)
        1 * cashService.createCashOrderPayment(_)
        Membership.countByCustomer(customer) == 1
        membership.type == membershipType2
        membership.startDate == today.minusDays(10)
        membership.endDate == today.plusDays(10)
        membership.gracePeriodEndDate == today.plusDays(20)
        membership.order.price == 100
    }

    void testIsUpcomingMembershipAvailableForPurchase() {
        def today = LocalDate.now()
        def facility = new Facility(facilityProperties: [new FacilityProperty(value: "1",
                key: FacilityProperty.FacilityPropertyKey.FEATURE_MEMBERSHIP_REQUEST_PAYMENT)])
        def facilityNoPayment = new Facility()
        def mt = new MembershipType(price: 100L, facility: facility)
        def mtNoPayment = new MembershipType(price: 100L, facility: facilityNoPayment)
        def mtFree = new MembershipType(price: 0, facility: facility)
        def mtOffline = new MembershipType(price: 100L, facility: facility, availableOnline: false)
        def mtWithStartDate = new MembershipType(price: 100L, facility: facility,
                purchaseDaysInAdvanceYearly: 10)
        mtWithStartDate.startDateYearly = today.plusDays(10)
        def mtWithStartDateAndNoDays = new MembershipType(price: 100L, facility: facility)
        mtWithStartDateAndNoDays.startDateYearly = today.plusDays(10)
        def mtMonthly = new MembershipType(validTimeAmount: 1, validTimeUnit: TimeUnit.MONTH,
                price: 100L, facility: facility)
        def mtWithStartDateInPast = new MembershipType(price: 100L, facility: facility,
                purchaseDaysInAdvanceYearly: 10)
        mtWithStartDateInPast.startDateYearly = today.minusDays(10)

        expect:
        !service.isUpcomingMembershipAvailableForPurchase(null)
        !service.isUpcomingMembershipAvailableForPurchase(new Membership())
        service.isUpcomingMembershipAvailableForPurchase(new Membership(startDate: today,
                endDate: today, gracePeriodEndDate: today, type: mt,
                order: new Order(status: Order.Status.COMPLETED)))
        !service.isUpcomingMembershipAvailableForPurchase(new Membership(startDate: today,
                endDate: today, gracePeriodEndDate: today, type: mt, autoPay: true,
                order: new Order(status: Order.Status.COMPLETED)))
        !service.isUpcomingMembershipAvailableForPurchase(new Membership(startDate: today,
                endDate: today.plusYears(1), gracePeriodEndDate: today.plusYears(1), type: mt,
                order: new Order(status: Order.Status.COMPLETED)))
        !service.isUpcomingMembershipAvailableForPurchase(new Membership(startDate: today,
                endDate: today, gracePeriodEndDate: today, type: mtNoPayment,
                order: new Order(status: Order.Status.COMPLETED)))
        !service.isUpcomingMembershipAvailableForPurchase(new Membership(startDate: today,
                endDate: today, gracePeriodEndDate: today, type: mtFree,
                order: new Order(status: Order.Status.COMPLETED)))
        !service.isUpcomingMembershipAvailableForPurchase(new Membership(startDate: today,
                endDate: today, gracePeriodEndDate: today, type: mtOffline,
                order: new Order(status: Order.Status.COMPLETED)))
        !service.isUpcomingMembershipAvailableForPurchase(new Membership(startDate: today,
                endDate: today, gracePeriodEndDate: today, type: mt, cancel: true,
                order: new Order(status: Order.Status.COMPLETED)))
        !service.isUpcomingMembershipAvailableForPurchase(new Membership(startDate: today,
                endDate: today.plusYears(2), gracePeriodEndDate: today.plusYears(2), type: mt,
                order: new Order(status: Order.Status.COMPLETED)))
        service.isUpcomingMembershipAvailableForPurchase(new Membership(startDate: today,
                endDate: today, gracePeriodEndDate: today, type: mtWithStartDate,
                order: new Order(status: Order.Status.COMPLETED)))
        !service.isUpcomingMembershipAvailableForPurchase(new Membership(startDate: today,
                endDate: today, gracePeriodEndDate: today, type: mtWithStartDateAndNoDays,
                order: new Order(status: Order.Status.COMPLETED)))
        !service.isUpcomingMembershipAvailableForPurchase(new Membership(startDate: today,
                endDate: today, gracePeriodEndDate: today, type: mtWithStartDateInPast,
                order: new Order(status: Order.Status.COMPLETED)))
        !service.isUpcomingMembershipAvailableForPurchase(new Membership(startDate: today,
                endDate: today.plusMonths(1), gracePeriodEndDate: today.plusMonths(1),
                type: mtMonthly, order: new Order(status: Order.Status.COMPLETED)))
        service.isUpcomingMembershipAvailableForPurchase(new Membership(startDate: today.minusDays(20),
                endDate: today.plusDays(10), gracePeriodEndDate: today.plusDays(10),
                type: mtMonthly, order: new Order(status: Order.Status.COMPLETED)))
        !service.isUpcomingMembershipAvailableForPurchase(new Membership(startDate: today.minusDays(20),
                endDate: today.plusDays(10), gracePeriodEndDate: today.plusDays(10),
                type: mtMonthly, autoPay: true, order: new Order(status: Order.Status.COMPLETED)))
    }

    void testRefundCashPayments() {
        def today = LocalDate.now()
        def issuer = createUser()
        def membership = createMembership(createCustomer(), today, today, today)
        def payment1 = new CashOrderPayment(issuer: issuer, amount: 100,
                status: OrderPayment.Status.CAPTURED).save(failOnError: true)
        def payment2 = new CashOrderPayment(issuer: issuer, amount: 50,
                status: OrderPayment.Status.ANNULLED).save(failOnError: true)
        def payment3 = new AdyenOrderPayment(issuer: issuer, amount: 100,
                status: OrderPayment.Status.CAPTURED, method: PaymentMethod.CREDIT_CARD_RECUR)
                .save(failOnError: true)
        membership.order.payments = [payment1, payment2, payment3]
        membership.order.price = 200
        membership.order.save(failOnError: true)

        when:
        service.refundCashPayments(membership)

        then:
        payment1.status == OrderPayment.Status.CREDITED
        payment1.credited == 100
        payment2.status == OrderPayment.Status.ANNULLED
        payment2.credited == 0
        payment3.status == OrderPayment.Status.CAPTURED
        payment3.credited == 0
    }

    void testGetFormMembershipTypes() {
        def today = LocalDate.now()
        def facility = createFacility()
        facility.membershipGraceNrOfDays = 5
        facility.save(failOnError: true)
        def mt = createMembershipType(facility)
        def customer = createCustomer(facility)
        createMembership(customer, today.plusDays(10),
                today.plusYears(1).minusDays(1), today.plusYears(1).plusDays(4), mt)

        when:
        def result = service.getFormMembershipTypes(facility)

        then:
        result.size() == 1
        result[0].id == mt.id
        result[0].name == mt.name
        result[0].startDate == today
        result[0].endDate == today.plusYears(1).minusDays(1)
        result[0].gracePeriodEndDate == result[0].endDate.plusDays(5)

        when:
        result = service.getFormMembershipTypes(facility, customer)

        then:
        result[0].startDate == today
        result[0].endDate == today.plusDays(9)
        result[0].gracePeriodEndDate == result[0].endDate

        when:
        createMembership(customer, today, today.plusDays(9), today.plusDays(9), mt)
        result = service.getFormMembershipTypes(facility, customer)

        then:
        result[0].startDate == today.plusYears(1)
        result[0].endDate == today.plusYears(2).minusDays(1)
        result[0].gracePeriodEndDate == result[0].endDate.plusDays(5)
    }


    void testClearGracePeriod() {
        def today = LocalDate.now()
        def c1 = createCustomer()
        def m1 = createMembership(c1, today, today.plusDays(10), today.plusDays(20))

        when:
        service.clearGracePeriod(m1)

        then:
        m1.endDate == m1.gracePeriodEndDate
    }

    void testUpdateMembershipFields() {
        def facility = createFacility()
        def customer = createCustomer(facility)
        def issuer = createUser()
        def m = createMembership(customer)
        def today = LocalDate.now()
        def start = today.plusDays(100)
        def end = today.plusDays(200)
        def grace = today.plusDays(300)
        def sgrace = 10
        def mt = createMembershipType(facility)

        when:
        service.updateMembershipFields(m, issuer, [startDate         : start, endDate: end,
                                                   gracePeriodEndDate: grace, startingGracePeriodDays: sgrace,
                                                   typeId            : mt.id])

        then:
        1 * membershipPaymentService.createMembershipPaymentOrder(_, _, _, _, _) >>
                new Order(article: Order.Article.MEMBERSHIP, user: issuer, issuer: issuer,
                        description: "desc", facility: facility,
                        dateDelivery: new Date(), customer: customer, price: 100)
                        .save(failOnError: true, flush: true)
        m.startDate == start
        m.endDate == end
        m.gracePeriodEndDate == grace
        m.startingGracePeriodDays == sgrace
        m.type == mt
    }
}
