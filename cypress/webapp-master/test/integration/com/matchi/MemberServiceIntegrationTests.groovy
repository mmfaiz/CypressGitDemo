package com.matchi

import com.matchi.FacilityProperty.FacilityPropertyKey
import com.matchi.invoice.Invoice
import com.matchi.invoice.InvoiceRow
import com.matchi.membership.Membership
import com.matchi.membership.MembershipFamily
import com.matchi.orders.InvoiceOrderPayment
import com.matchi.orders.Order
import com.matchi.orders.OrderPayment
import org.joda.time.LocalDate
import org.junit.After

import static com.matchi.TestUtils.*

/**
 * @author Sergei Shushkevich
 */
class MemberServiceIntegrationTests extends GroovyTestCase {

    def memberService
    def membersFamilyService

    void testHandleMembershipInvoicePayment() {
        def facility = createFacility()
        facility.invoicing = true
        facility.save(failOnError: true)
        def membershipType = createMembershipType(facility)
        def customer = createCustomer(facility)
        def user = createUser()
        def membership = createMembership(customer)
        def invoiceRow = new InvoiceRow(customer: customer, createdBy: user,
                amount: 1, price: membershipType.price).save(failOnError: true)
        def payment = new InvoiceOrderPayment(issuer: user,
                amount: invoiceRow.price, vat: invoiceRow.vat,
                invoiceRow: invoiceRow).save(failOnError: true)
        membership.order = new Order(facility: facility, article: Order.Article.MEMBERSHIP,
                metadata: [membershipTypeId: membershipType.id.toString()],
                issuer: user, dateDelivery: new Date(), customer: customer,
                description: "desc", price: membershipType.price).addToPayments(payment)
                .save(failOnError: true)
        membership.save(failOnError: true)
        def invoice = new Invoice(customer: customer, invoiceDate: new LocalDate(),
                status: Invoice.InvoiceStatus.PAID).addToRows(invoiceRow)
                .save(failOnError: true, flush: true)

        memberService.handleMembershipInvoicePayment(invoice)

        assert membership.order.status == Order.Status.COMPLETED
        assert membership.isActive()
        assert payment.status == OrderPayment.Status.CAPTURED
    }

    void testHandleMembershipInvoicePayment2() {
        def facility = createFacility()
        facility.invoicing = true
        facility.save(failOnError: true)
        def membershipType = createMembershipType(facility)
        def customer = createCustomer(facility)
        def user = createUser()
        def membership = createMembership(customer)
        def invoiceRow = new InvoiceRow(customer: customer, createdBy: user,
                amount: 1, price: membershipType.price).save(failOnError: true)
        def payment = new InvoiceOrderPayment(issuer: user,
                amount: invoiceRow.price, vat: invoiceRow.vat,
                invoiceRow: invoiceRow, status: OrderPayment.Status.CAPTURED).save(failOnError: true)
        membership.order = new Order(facility: facility, article: Order.Article.MEMBERSHIP,
                metadata: [membershipTypeId: membershipType.id.toString()],
                issuer: user, dateDelivery: new Date(), customer: customer,
                description: "desc", status: Order.Status.COMPLETED,
                price: membershipType.price).addToPayments(payment)
                .save(failOnError: true)
        membership.save(failOnError: true)
        def invoice = new Invoice(customer: customer, invoiceDate: new LocalDate(),
                status: Invoice.InvoiceStatus.CANCELLED).addToRows(invoiceRow)
                .save(failOnError: true, flush: true)

        memberService.handleMembershipInvoicePayment(invoice)

        assert membership.order.status == Order.Status.COMPLETED
        assert !membership.isActive()
        assert payment.status == OrderPayment.Status.ANNULLED
    }

    void testHandleMembershipInvoicePayment3() {
        def facility = createFacility()
        facility.invoicing = true
        facility.save(failOnError: true)
        def membershipType = createMembershipType(facility)
        def customer = createCustomer(facility)
        def user = createUser()
        def membership = createMembership(customer)
        def invoiceRow = new InvoiceRow(customer: customer, createdBy: user,
                amount: 1, price: membershipType.price).save(failOnError: true)
        def payment = new InvoiceOrderPayment(issuer: user,
                amount: invoiceRow.price, vat: invoiceRow.vat,
                invoiceRow: invoiceRow, status: OrderPayment.Status.CAPTURED).save(failOnError: true)
        membership.order = new Order(facility: facility, article: Order.Article.MEMBERSHIP,
                metadata: [membershipTypeId: membershipType.id.toString()],
                issuer: user, dateDelivery: new Date(), customer: customer,
                description: "desc", status: Order.Status.COMPLETED,
                price: membershipType.price).addToPayments(payment)
                .save(failOnError: true)
        membership.save(failOnError: true)
        def invoice = new Invoice(customer: customer, invoiceDate: new LocalDate(),
                status: Invoice.InvoiceStatus.POSTED).addToRows(invoiceRow)
                .save(failOnError: true, flush: true)

        memberService.handleMembershipInvoicePayment(invoice)

        assert membership.order.status == Order.Status.COMPLETED
        assert !membership.isActive()
        assert payment.status == OrderPayment.Status.NEW
    }

    void testHandleMembershipInvoicePaymentFamily() {
        def today = new LocalDate()
        def facility = createFacility()
        facility.invoicing = true
        facility.save(failOnError: true)
        def membershipType = createMembershipType(facility)
        def customer = createCustomer(facility)
        def membership = createMembership(customer, today, today.plusDays(1), today.plusDays(2), membershipType)
        def customer2 = createCustomer(facility)
        def membership2 = createMembership(customer2, today, today.plusDays(1), today.plusDays(2), membershipType)
        MembershipFamily mf = new MembershipFamily(contact: customer).addToMembers(membership)
                .addToMembers(membership2).save(failOnError: true)

        membersFamilyService.addFamilyMember(membership, mf)
        membersFamilyService.addFamilyMember(membership2, mf)

        def user = createUser()
        def invoiceRow = new InvoiceRow(customer: customer, createdBy: user,
                amount: 1, price: membershipType.price).save(failOnError: true)
        def payment = new InvoiceOrderPayment(issuer: user,
                amount: membershipType.price, vat: invoiceRow.vat,
                status: OrderPayment.Status.NEW, invoiceRow: invoiceRow)
                .save(failOnError: true)
        membership.order = new Order(facility: facility, article: Order.Article.MEMBERSHIP,
                metadata: [membershipTypeId: membershipType.id.toString()],
                issuer: user, dateDelivery: new Date(), customer: customer,
                description: "desc", price: membershipType.price).addToPayments(payment)
                .save(failOnError: true)
        membership.save(failOnError: true)
        membership2.order = new Order(facility: facility, article: Order.Article.MEMBERSHIP,
                metadata: [membershipTypeId: membershipType.id.toString()],
                issuer: user, dateDelivery: new Date(), customer: customer2,
                description: "desc", price: membershipType.price)
                .save(failOnError: true)
        membership2.save(failOnError: true)
        def invoice = new Invoice(customer: customer, invoiceDate: new LocalDate(),
                status: Invoice.InvoiceStatus.PAID)
                .addToRows(invoiceRow)
                .save(failOnError: true, flush: true)

        memberService.handleMembershipInvoicePayment(invoice)

        assert Membership.countByCustomer(customer) == 1
        assert membership.order.status == Order.Status.COMPLETED
        assert membership.order.price == membershipType.price
        assert membership.isPaid()
        assert Membership.countByCustomer(customer2) == 1
        assert membership2.order.status == Order.Status.COMPLETED
        assert membership2.order.price == 0
        assert membership2.isPaid()
    }

    void testHandleMembershipInvoicePaymentFamily2() {
        def today = new LocalDate()
        def facility = createFacility()
        facility.invoicing = true
        facility.save(failOnError: true)
        def membershipType = createMembershipType(facility)
        def customer = createCustomer(facility)
        def membership = createMembership(customer, today, today.plusDays(1), today.plusDays(2), membershipType)
        def customer2 = createCustomer(facility)
        def membership2 = createMembership(customer2, today, today.plusDays(1), today.plusDays(2), membershipType)
        MembershipFamily mf = new MembershipFamily(contact: customer).addToMembers(membership)
                .addToMembers(membership2).save(failOnError: true)

        membersFamilyService.addFamilyMember(membership, mf)
        membersFamilyService.addFamilyMember(membership2, mf)

        def user = createUser()
        def invoiceRow = new InvoiceRow(customer: customer, createdBy: user,
                amount: 1, price: membershipType.price).save(failOnError: true)
        def payment = new InvoiceOrderPayment(issuer: user,
                amount: membershipType.price, vat: invoiceRow.vat,
                status: OrderPayment.Status.CAPTURED, invoiceRow: invoiceRow)
                .save(failOnError: true)
        membership.order = new Order(facility: facility, article: Order.Article.MEMBERSHIP,
                metadata: [membershipTypeId: membershipType.id.toString()],
                issuer: user, dateDelivery: new Date(), customer: customer,
                description: "desc", price: membershipType.price,
                status: Order.Status.COMPLETED).addToPayments(payment)
                .save(failOnError: true)
        membership.save(failOnError: true)
        membership2.order = new Order(facility: facility, article: Order.Article.MEMBERSHIP,
                metadata: [membershipTypeId: membershipType.id.toString()],
                issuer: user, dateDelivery: new Date(), customer: customer2,
                description: "desc", price: 0, status: Order.Status.COMPLETED)
                .save(failOnError: true)
        membership2.save(failOnError: true)
        def invoice = new Invoice(customer: customer, invoiceDate: new LocalDate(),
                status: Invoice.InvoiceStatus.CREDITED)
                .save(failOnError: true, flush: true)
        invoiceRow.invoice = invoice
        invoiceRow.save(failOnError: true, flush: true)

        memberService.handleMembershipInvoicePayment(invoice)

        assert membership.order.status == Order.Status.COMPLETED
        assert membership.order.price == membershipType.price
        assert !membership.isPaid()
        assert payment.status == OrderPayment.Status.CREDITED
        assert payment.amount == payment.credited
        assert membership2.order.status == Order.Status.COMPLETED
        assert membership2.order.price == membershipType.price
        assert !membership2.isPaid()
    }

    void testListUserMemberships() {
        def today = new LocalDate()
        def facility1 = createFacility()
        def facility2 = createFacility()
        def user1 = createUser("u1@matchi.se")
        def user2 = createUser("u2@matchi.se")
        def customer11 = createCustomer(facility1, null, null, null, null, user1)
        def customer12 = createCustomer(facility2, null, null, null, null, user1)
        def customer21 = createCustomer(facility1, null, null, null, null, user2)
        def customer22 = createCustomer(facility2, null, null, null, null, user2)
        def m11 = createMembership(customer11, today.minusDays(1), today, today.plusDays(1))
        def m12 = createMembership(customer12, today.plusDays(10), today.plusDays(20), today.plusDays(30))
        createMembership(customer12, today.minusDays(10), today.minusDays(5), today.minusDays(1))
        createMembership(customer21, today.minusDays(10), today.minusDays(5), today.minusDays(1))
        createMembership(customer22, today.plusDays(10), today.plusDays(20), today.plusDays(30))
        def m21 = createMembership(customer22, today, today, today)

        def result = memberService.listUserMemberships(user1)

        assert result.size() == 2
        assert result.find {it.id == m11.id}
        assert result.find {it.id == m12.id}

        result = memberService.listUserMemberships(user2)

        assert result.size() == 1
        assert result[0].id == m21.id
    }

    void testGetMembership() {
        def today = new LocalDate()
        def facility = createFacility()
        def user1 = createUser("u1@matchi.se")
        def user2 = createUser("u2@matchi.se")
        def customer1 = createCustomer(facility, null, null, null, null, user1)
        def customer2 = createCustomer(facility, null, null, null, null, user2)
        def m1 = createMembership(customer1)
        def m2 = createMembership(customer2)
        def order = new Order(facility: facility, article: Order.Article.MEMBERSHIP,
                metadata: [:], issuer: user1, dateDelivery: new Date(), customer: customer1,
                description: "desc").save(failOnError: true)
        m1.order = order
        m1.save(failOnError: true)
        m2.order = order
        m2.save(failOnError: true, flush: true)

        assert memberService.getMembership(order, user1).id == m1.id
        assert memberService.getMembership(order, user2).id == m2.id
    }

    void testListUserRecurringMembershipsToRenew() {
        def today = new LocalDate()
        def facility1Recurring = createFacility()
        facility1Recurring.setFacilityProperty(FacilityPropertyKey.FEATURE_RECURRING_MEMBERSHIP, "1")
        facility1Recurring.save(failOnError: true, flush: true)
        def facility2Recurring = createFacility()
        facility2Recurring.setFacilityProperty(FacilityPropertyKey.FEATURE_RECURRING_MEMBERSHIP, "1")
        facility2Recurring.save(failOnError: true, flush: true)
        def facility3NonRecurring = createFacility()

        def mt1 = createMembershipType(facility1Recurring)
        mt1.paidOnRenewal = true
        mt1.save(failOnError: true, flush: true)
        def mt1NoAutoPay = createMembershipType(facility1Recurring)
        def mt2 = createMembershipType(facility2Recurring)
        mt2.paidOnRenewal = true
        mt2.save(failOnError: true, flush: true)
        def mt3 = createMembershipType(facility3NonRecurring)
        mt3.paidOnRenewal = true
        mt3.save(failOnError: true, flush: true)

        def user1 = createUser("c1@matchi.se")
        def u1c1 = createCustomer(facility1Recurring, null, null, null, null, user1)
        def u1m1 = createMembership(u1c1, today, today.plusDays(1), today.plusDays(1), mt1)
        u1m1.autoPay = true
        u1m1.save(failOnError: true, flush: true)
        def u1m12 = createMembership(u1c1, u1m1.endDate.plusDays(1), u1m1.endDate.plusDays(2),
                u1m1.endDate.plusDays(2), mt1)
        u1m12.autoPay = true
        u1m12.save(failOnError: true, flush: true)
        def u1c2 = createCustomer(facility2Recurring, null, null, null, null, user1)
        def u1m2 = createMembership(u1c2, today, today.plusDays(1), today.plusDays(1), mt2)
        u1m2.autoPay = true
        u1m2.save(failOnError: true, flush: true)
        def u1c3 = createCustomer(facility3NonRecurring, null, null, null, null, user1)
        createMembership(u1c3, today, today.plusDays(1), today.plusDays(1), mt3)

        def user2 = createUser("c2@matchi.se")
        def u2c1 = createCustomer(facility1Recurring, null, null, null, null, user2)
        def u2m1 = createMembership(u2c1, today, today.plusDays(1), today.plusDays(1), mt1)
        u2m1.autoPay = true
        u2m1.save(failOnError: true, flush: true)
        def u2m12 = createMembership(u2c1, u2m1.endDate.plusDays(1), u2m1.endDate.plusDays(2),
                u2m1.endDate.plusDays(2), mt1)
        def u2c2 = createCustomer(facility2Recurring, null, null, null, null, user2)
        def u2m2 = createMembership(u2c2, today, today.plusDays(1), today.plusDays(1), mt2)
        u2m2.autoPay = true
        u2m2.save(failOnError: true, flush: true)

        def user3 = createUser("c3@matchi.se")
        def u3c1 = createCustomer(facility1Recurring, null, null, null, null, user3)
        def u3m1 = createMembership(u3c1, today, today, today, mt1)
        u3m1.autoPay = true
        u3m1.save(failOnError: true, flush: true)

        def user4 = createUser("c4@matchi.se")
        def u4c1 = createCustomer(facility1Recurring, null, null, null, null, user4)
        def u4m1 = createMembership(u4c1, today, today.plusDays(1), today.plusDays(1), mt1)
        u4m1.autoPay = true
        u4m1.cancel = true
        u4m1.save(failOnError: true, flush: true)

        def user5 = createUser("c5@matchi.se")
        def u5c1 = createCustomer(facility1Recurring, null, null, null, null, user5)
        def u5m1 = createMembership(u5c1, today, today.plusDays(1), today.plusDays(1), mt1)
        u5m1.autoPay = true
        u5m1.activated = false
        u5m1.save(failOnError: true, flush: true)

        def user6 = createUser("c6@matchi.se")
        def u6c1 = createCustomer(facility1Recurring, null, null, null, null, user6)
        def u6m1 = createMembership(u6c1, today, today.plusDays(1), today.plusDays(1), mt1)
        u6m1.autoPay = true
        u6m1.order.price = 200
        u6m1.save(failOnError: true, flush: true)

        def user7 = createUser("c7@matchi.se")
        def u7c1 = createCustomer(facility1Recurring, null, null, null, null, user7)
        def u7m1 = createMembership(u7c1, today, today.plusDays(1), today.plusDays(1), mt1NoAutoPay)
        u7m1.autoPay = true
        u7m1.save(failOnError: true, flush: true)


        def result = memberService.listUserRecurringMembershipsToRenew(user1)

        assert result.size() == 2
        assert result.find {it.id == u1m12.id}
        assert result.find {it.id == u1m2.id}

        result = memberService.listUserRecurringMembershipsToRenew(user2)

        assert result.size() == 1
        assert result[0].id == u2m2.id

        assert !memberService.listUserRecurringMembershipsToRenew(user3)
        assert !memberService.listUserRecurringMembershipsToRenew(user4)
        assert !memberService.listUserRecurringMembershipsToRenew(user5)
        assert !memberService.listUserRecurringMembershipsToRenew(user6)
        assert !memberService.listUserRecurringMembershipsToRenew(user7)
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