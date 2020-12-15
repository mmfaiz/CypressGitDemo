package com.matchi

import com.matchi.adyen.AdyenException
import com.matchi.membership.Membership
import com.matchi.membership.MembershipFamily
import com.matchi.orders.AdyenOrderPayment
import com.matchi.orders.Order
import com.matchi.orders.OrderPayment
import com.matchi.payment.PaymentMethod
import org.joda.time.LocalDate
import org.junit.After
import org.junit.Before

import static com.matchi.TestUtils.*

/**
 * @author Sergei Shushkevich
 */
class MemberServiceNonTransactionalTests extends GroovyTestCase {

    static transactional = false

    def memberService
    def membersFamilyService

    @Before
    void setUp() {
        List<MembershipFamily> families = MembershipFamily.all
        families.each { MembershipFamily family ->
            if (family.members) {
                List<Membership> members = family.members.toList()
                members.each {
                    Membership member
                    if (member) {
                        family.removeFromMembers(member)
                        member.family = null
                    }
                }
            }
            family.delete(flush: true)
        }
    }

    void testRenewMemberships() {
        def today = new LocalDate()
        def facility = createFacility()
        def mtRenew = createMembershipType(facility)
        def mtZeroPrice = createMembershipType(facility)
        mtZeroPrice.price = 0L
        mtZeroPrice.save(failOnError: true)
        def mtRenewAndPay = createMembershipType(facility, true)
        def customer1 = createCustomer(facility, null, null, null, null, createUser("c1@matchi.se"))
        createMembership(customer1, today.minusDays(1), today, today.plusDays(1), mtRenew)
        def customer2 = createCustomer(facility, null, null, null, null, createUser("c2@matchi.se"))
        createMembership(customer2, today.minusDays(1), today, today.plusDays(1), mtRenew, true)
        def customer3 = createCustomer(facility, null, null, null, null, createUser("c3@matchi.se"))
        createMembership(customer3, today.minusDays(1), today, today.plusDays(1), mtZeroPrice)
        def customer4 = createCustomer(facility, null, null, null, null, createUser("c4@matchi.se"))
        createMembership(customer4, today.minusDays(1), today, today, mtRenew)
        createMembership(customer4, today.plusDays(1), today.plusDays(10), today.plusDays(20), mtRenew)
        def customer5 = createCustomer(facility, null, null, null, null, createUser("c5@matchi.se"))
        createMembership(customer5, today.minusDays(1), today, today.plusDays(1), mtRenew, false, false)
        def customer6 = createCustomer(facility, null, null, null, null, createUser("c6@matchi.se"))
        def m = createMembership(customer6, today.minusDays(1), today, today.plusDays(1), mtRenew)
        m.order.status = Order.Status.NEW
        m.order.save(failOnError: true, flush: true)
        def customer7 = createCustomer(facility, null, null, null, null, createUser("c7@matchi.se"))
        createMembership(customer7, today.minusDays(1), today, today.plusDays(1), mtRenewAndPay)
        def customer8 = createCustomer(facility, null, null, null, null, createUser("c8@matchi.se"))
        def m8 = createMembership(customer8, today.minusDays(1), today, today.plusDays(1), mtRenew)
        def customer9 = createCustomer(facility, null, null, null, null, createUser("c9@matchi.se"))
        def m9 = createMembership(customer9, today.minusDays(1), today, today.plusDays(1), mtRenew)
        def family = new MembershipFamily(contact: customer8).addToMembers(m8).addToMembers(m9)
                .save(failOnError: true, flush: true)

        membersFamilyService.addFamilyMember(m8, family)
        membersFamilyService.addFamilyMember(m9, family)

        memberService.renewMemberships()

        // membership is renewed but order is not completed since membership type is not free:
        assert Membership.countByCustomer(customer1) == 2
        m = Membership.findByCustomerAndStartDate(customer1, today.minusDays(1))
        assert m.gracePeriodEndDate == today.plusDays(1)
        m = Membership.findByCustomerAndStartDate(customer1, today.plusDays(1))
        assert m.endDate == today.plusDays(1).plusYears(1).minusDays(1)
        assert m.order
        assert m.order.status == Order.Status.NEW
        // membership is renewed and order is completed since membership type has "paidOnRenewal" enabled:
        assert Membership.countByCustomer(customer7) == 2
        m = Membership.findByCustomerAndStartDate(customer7, today.minusDays(1))
        m.refresh()
        assert m.gracePeriodEndDate == today
        m = Membership.findByCustomerAndStartDate(customer7, today.plusDays(1))
        assert m.endDate == today.plusDays(1).plusYears(1).minusDays(1)
        assert m.order
        assert m.order.status == Order.Status.COMPLETED
        assert m.order.payments.size() == 1
        def payment = m.order.payments.iterator().next()
        assert payment.status == OrderPayment.Status.CAPTURED
        assert payment.type == "Cash"
        // membership is not renewed if "cancel" is true:
        assert Membership.countByCustomer(customer2) == 1
        m = Membership.findByCustomerAndStartDate(customer2, today.minusDays(1))
        assert m.endDate == today
        assert m.gracePeriodEndDate == today.plusDays(1)
        // membership is renewed and order is completed since membership type is free:
        assert Membership.countByCustomer(customer3) == 2
        m = Membership.findByCustomerAndStartDate(customer3, today.minusDays(1))
        m.refresh()
        assert m.endDate == today
        assert m.gracePeriodEndDate == today
        m = Membership.findByCustomerAndStartDate(customer3, today.plusDays(1))
        m.endDate = today.plusDays(1).plusYears(1).minusDays(1)
        assert m.order
        assert m.order.status == Order.Status.COMPLETED
        // membership is not renewed if customer already has renewed/upcoming membership
        assert Membership.countByCustomer(customer4) == 2
        m = Membership.findByCustomerAndStartDate(customer4, today.minusDays(1))
        assert m.endDate == today
        assert m.gracePeriodEndDate == today
        m = Membership.findByCustomerAndStartDate(customer4, today.plusDays(1))
        assert m.endDate == today.plusDays(10)
        assert m.gracePeriodEndDate == today.plusDays(20)
        // membership is not renewed if not activated:
        assert Membership.countByCustomer(customer5) == 1
        m = Membership.findByCustomerAndStartDate(customer5, today.minusDays(1))
        assert m.endDate == today
        assert m.gracePeriodEndDate == today.plusDays(1)
        // membership is not renewed if it's not paid:
        assert Membership.countByCustomer(customer6) == 1
        m = Membership.findByCustomerAndStartDate(customer6, today.minusDays(1))
        assert m.endDate == today
        assert m.gracePeriodEndDate == today.plusDays(1)
        // memberships are renewed for family members and family is renewed
        assert Membership.countByCustomer(customer8) == 2
        assert Membership.countByCustomer(customer9) == 2
        assert MembershipFamily.countByContact(customer8) == 2
        def m82 = Membership.findByCustomerAndStartDate(customer8, today.plusDays(1))
        def m92 = Membership.findByCustomerAndStartDate(customer9, today.plusDays(1))
        assert m82.family.id == m92.family.id
        assert m82.family.id != m8.family.id

        memberService.renewMemberships(today.plusDays(10))

        assert Membership.countByCustomer(customer4) == 3
        m = Membership.findByCustomerAndStartDate(customer4, today.minusDays(1))
        assert m.endDate == today
        assert m.gracePeriodEndDate == today
        m = Membership.findByCustomerAndStartDate(customer4, today.plusDays(1))
        assert m.endDate == today.plusDays(10)
        assert m.gracePeriodEndDate == today.plusDays(20)
        m = Membership.findByCustomerAndStartDate(customer4, today.plusDays(11))
        assert m.endDate == today.plusDays(11).plusYears(1).minusDays(1)
    }

    void testRetryFailedMembershipPayments() {
        def today = LocalDate.now()
        def c1 = createCustomer()
        def m1 = createMembership(c1, today, today.plusDays(10), today.plusDays(20))
        def p1 = new AdyenOrderPayment(issuer: createUser("retryPayment@matchi.se"), amount: 100,
                status: OrderPayment.Status.FAILED, method: PaymentMethod.CREDIT_CARD_RECUR).save(failOnError: true)
        m1.order.payments = [p1]
        m1.order.price = 100
        m1.order.status = Order.Status.CANCELLED
        m1.order.save(failOnError: true)
        m1.autoPayAttempts = 3
        m1.startingGracePeriodDays = 5
        m1.save(failOnError: true, flush: true)

        memberService.paymentService = [handleCreditCardPayment: { o, u -> throw new AdyenException() }] as PaymentService

        memberService.retryFailedMembershipPayments()

        m1.refresh()
        assert !m1.autoPay
        assert m1.autoPayAttempts == 4
        assert m1.startingGracePeriodDays == 5

        memberService.paymentService = [handleCreditCardPayment: { o, u -> }] as PaymentService

        memberService.retryFailedMembershipPayments()

        m1.refresh()
        assert m1.autoPay
        assert !m1.autoPayAttempts
        assert !m1.startingGracePeriodDays
    }

    @After
    void tearDown() {
        List<MembershipFamily> families = MembershipFamily.all
        families.each { MembershipFamily family ->
            if (family.members) {
                List<Membership> members = family.members.toList()
                members.each { Membership member ->
                    if (member) {
                        family.removeFromMembers(member)
                        member.family = null
                    }
                }
            }
            family.delete(flush: true)
        }
    }
}