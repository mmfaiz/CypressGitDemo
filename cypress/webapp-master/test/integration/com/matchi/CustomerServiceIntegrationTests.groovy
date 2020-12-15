package com.matchi

import com.matchi.FacilityProperty.FacilityPropertyKey
import com.matchi.dynamicforms.AddressBinder
import com.matchi.dynamicforms.ParentInformationBinder
import com.matchi.dynamicforms.PersonalInformationBinder
import com.matchi.facility.FilterCustomerCommand
import com.matchi.invoice.Invoice
import com.matchi.membership.Membership
import com.matchi.membership.MembershipFamily
import com.matchi.orders.Order
import org.apache.commons.lang.RandomStringUtils
import org.joda.time.DateTime
import org.joda.time.LocalDate
import org.joda.time.LocalDateTime
import org.junit.After
import org.junit.Test

import static com.matchi.TestUtils.*

/**
 * @author Sergei Shushkevich
 */
class CustomerServiceIntegrationTests extends GroovyTestCase {

    def customerService
    def groovySql

    @Test
    void testLinkCustomerToUser() {
        def user = createUser()

        def customer = createCustomer()
        customerService.linkCustomerToUser(customer)
        assert !Customer.findByIdAndUser(customer.id, user)

        customer = createCustomer(null, user.email)
        customerService.linkCustomerToUser(customer)
        assert !Customer.findByIdAndUser(customer.id, user)

        customer = createCustomer(null, user.email, user.firstname, user.lastname)
        customerService.linkCustomerToUser(customer)
        assert Customer.findByIdAndUser(customer.id, user)
    }

    @Test
    void testGetOrCreateCustomer() {
        def customerProps = [guardian: [:]]
        def facility = createFacility()

        // at least "email" should be provided in order to get or create customer
        assert !customerService.getOrCreateCustomer(customerProps, facility)
        assert !Customer.countByFacility(facility)

        PersonalInformationBinder.inputs.each {
            if(it.contains('email')) {
                customerProps[it] = RandomStringUtils.randomAlphabetic(10) + "@matchi.se"
            } else {
                customerProps[it] = RandomStringUtils.randomAlphabetic(10)
            }

        }
        AddressBinder.inputs.each {
            customerProps[it] = RandomStringUtils.randomAlphabetic(10)
        }
        ParentInformationBinder.inputs.each {
            if(it.contains('email')) {
                customerProps.guardian[it] = RandomStringUtils.randomAlphabetic(10) + "@matchi.se"
            } else {
                customerProps.guardian[it] = RandomStringUtils.randomAlphabetic(10)
            }

        }
        customerProps.dateOfBirth = Date.parse("yyyyMMdd", "19580923")
        customerProps.securityNumber = "5236"

        // customer is created according to submitted form values
        def customer = customerService.getOrCreateCustomer(customerProps, facility)
        assert customer
        assert customer.firstname
        assert customer.lastname
        assert customer.email
        assert customer.securityNumber
        assert customer.cellphone
        assert customer.telephone
        assert customer.address1
        assert customer.address2
        assert customer.zipcode
        assert customer.city
        assert customer.guardianName
        assert customer.guardianEmail
        assert customer.guardianTelephone
        assert 1 == Customer.countByFacility(facility)

        // existing customer is returned for the same "email", "firstname", "lastname"
        assert customerService.getOrCreateCustomer(customerProps, facility)
        assert 1 == Customer.countByFacility(facility)

        // existing customer is returned for the same "security_number"
        assert customerService.getOrCreateCustomer([guardian: [:], dateOfBirth: customer.dateOfBirth,
                securityNumber: customer.securityNumber], facility)
        assert 1 == Customer.countByFacility(facility)
    }

    @Test
    void testSetLastActivityDoesNotCrash() {
        customerService.assignLastActivity([])
    }

    @Test
    void testSetLastActivityIsLastUpdated() {
        Customer customer = createCustomer()
        customerService.assignLastActivity(customer)
        assert customer.lastActivity == customer.lastUpdated
    }

    @Test
    void testSetLastActivitySeveralCustomers() {
        Customer customer = createCustomer()
        Customer customer2 = createCustomer()
        customerService.assignLastActivity([customer, customer2])
        assert customer.lastActivity == customer.lastUpdated
        assert customer2.lastActivity == customer2.lastUpdated
    }

    @Test
    void testSetLastActivityOrderInvoicesMemberships() {
        User user = createUser()
        Customer customer = createCustomer()
        customer.user = user
        customer.save(flush: true, failOnError: true)
        Customer.executeUpdate("update Customer set lastUpdated = ?",
                [new LocalDateTime().minusDays(2).withMillisOfDay(0).toDate()])
        customer.refresh()

        Order order = createOrder(user, customer.facility)
        order.customer = customer
        order.save(flush: true, failOnError: true)
        Order.executeUpdate("update Order set lastUpdated = ?",
                [new LocalDateTime().minusDays(1).withMillisOfDay(0).toDate()])
        order.refresh()

        assert !customer.lastActivity

        customerService.assignLastActivity([customer])
        assert customer.lastActivity == order.lastUpdated
        assert customer.lastActivity != customer.lastUpdated

        Invoice invoice = createInvoice(customer)

        customerService.assignLastActivity([customer])
        assert customer.lastActivity == invoice.lastUpdated.toDate()
        assert customer.lastActivity != order.lastUpdated
        assert customer.lastActivity != customer.lastUpdated

        Customer.executeUpdate("update Customer set lastUpdated = ?",
                [new LocalDateTime().plusDays(1).withMillisOfDay(0).toDate()])
        customer.refresh()

        customerService.assignLastActivity([customer])
        assert customer.lastActivity != invoice.lastUpdated.toDate()
        assert customer.lastActivity != order.lastUpdated
        assert customer.lastActivity == customer.lastUpdated

        def today = new LocalDate()
        Membership membership = createMembership(customer, today, today.plusDays(10), today.plusDays(20))
        customerService.assignLastActivity([customer])
        assert customer.lastActivity == membership.endDate.toDate()
        assert customer.lastActivity != order.lastUpdated
        assert customer.lastActivity != invoice.lastUpdated
        assert customer.lastActivity != customer.lastUpdated

        Customer.executeUpdate("update Customer set lastUpdated = ?",
                [new LocalDateTime().plusDays(11).withMillisOfDay(0).toDate()])
        customer.refresh()
        customerService.assignLastActivity([customer])
        assert customer.lastActivity != invoice.lastUpdated.toDate()
        assert customer.lastActivity != order.lastUpdated
        assert customer.lastActivity != membership.endDate.toDate()
        assert customer.lastActivity == customer.lastUpdated
    }

    @Test
    void testSetLastActivityInvoices() {
        User user = createUser()
        Customer customer = createCustomer()
        customer.user = user
        customer.save(flush: true, failOnError: true)

        Invoice invoice = createInvoice(customer)
        Invoice invoice2 = createInvoice(customer)
        Invoice invoice3 = createInvoice(customer)
        Invoice invoice4 = createInvoice(customer)
        Invoice.executeUpdate("update Invoice set lastUpdated = ? where id = ?",
                [new LocalDateTime().plusDays(1).withMillisOfDay(0).toDateTime(), invoice4.id])
        invoice4.refresh()

        customerService.assignLastActivity([customer])
        assert customer.lastActivity == invoice4.lastUpdated.toDate()
        assert customer.lastActivity != invoice3.lastUpdated.toDate()
        assert customer.lastActivity != invoice2.lastUpdated.toDate()
        assert customer.lastActivity != invoice.lastUpdated.toDate()
        assert customer.lastActivity != customer.lastUpdated
    }

    @Test
    void testSetLastActivityOrders() {
        User user = createUser()
        Customer customer = createCustomer()
        customer.user = user
        customer.save(flush: true, failOnError: true)

        Order order = createOrder(user, customer.facility)
        order.customer = customer
        order.save(flush: true, failOnError: true)

        Order order2 = createOrder(user, customer.facility)
        order2.customer = customer
        order2.save(flush: true, failOnError: true)

        Order order3 = createOrder(user, customer.facility)
        order3.customer = customer
        order3.save(flush: true, failOnError: true)

        Order order4 = createOrder(user, customer.facility)
        order4.customer = customer
        order4.save(flush: true, failOnError: true)
        Order.executeUpdate("update Order set lastUpdated = ? where id = ?",
                [new LocalDateTime().plusDays(1).withMillisOfDay(0).toDate(), order4.id])
        order4.refresh()

        customerService.assignLastActivity([customer])
        assert customer.lastActivity == order4.lastUpdated
        assert customer.lastActivity != order3.lastUpdated
        assert customer.lastActivity != order2.lastUpdated
        assert customer.lastActivity != order.lastUpdated
        assert customer.lastActivity != customer.lastUpdated
    }

    @Test
    void testSetLastActivityMembership() {
        User user = createUser()
        Customer customer = createCustomer()
        customer.user = user
        customer.save(flush: true, failOnError: true)

        def today = new LocalDate()
        Membership membership = createMembership(customer, today, today.plusDays(1), today.plusDays(1))
        membership.save(flush: true, failOnError: true)

        customerService.assignLastActivity([customer])

        assert customer.lastActivity == membership.endDate.toDate()
        assert customer.lastActivity != customer.lastUpdated
    }

    @Test
    void testSetLastActivityMemberships() {
        User user = createUser()
        Customer customer = createCustomer()
        customer.user = user
        customer.save(flush: true, failOnError: true)

        def today = new LocalDate()
        Membership membership = createMembership(customer, today, today.plusDays(10), today.plusDays(20))
        membership.save(flush: true, failOnError: true)

        Membership membership2 = createMembership(customer, today, today.plusDays(10), today.plusDays(20))
        membership.save(flush: true, failOnError: true)

        Membership membership3 = createMembership(customer, today, today.plusDays(10), today.plusDays(20))
        membership.save(flush: true, failOnError: true)

        Membership membership4 = createMembership(customer, today, today.plusDays(10), today.plusDays(20))
        membership4.save(flush: true, failOnError: true)

        Membership.executeUpdate("update Membership set endDate = ? where id = ?",
                [new LocalDate().plusDays(21), membership4.id])
        membership4.refresh()

        customerService.assignLastActivity([customer])

        assert customer.lastActivity == membership4.endDate.toDate()
        assert customer.lastActivity != membership3.endDate.toDate()
        assert customer.lastActivity != membership2.endDate.toDate()
        assert customer.lastActivity != membership.endDate.toDate()
        assert customer.lastActivity != customer.lastUpdated
    }

    @Test
    void testSetLastActivityOrdersDifferentCustomers() {
        User user = createUser()
        Customer customer = createCustomer()
        customer.user = user
        customer.save(flush: true, failOnError: true)

        User user2 = createUser("johndoe2@matchi.se")
        Customer customer2 = createCustomer()
        customer2.user = user2
        customer2.save(flush: true, failOnError: true)

        Order order = createOrder(user, customer.facility)
        order.customer = customer
        order.save(flush: true, failOnError: true)

        Order order2 = createOrder(user, customer.facility)
        order2.customer = customer
        order2.save(flush: true, failOnError: true)
        Order.executeUpdate("update Order set lastUpdated = ? where id = ?",
                [new LocalDateTime().plusDays(1).withMillisOfDay(0).toDate(), order2.id])
        order2.refresh()

        Order order3 = createOrder(user2, customer.facility)
        order3.customer = customer2
        order3.save(flush: true, failOnError: true)

        Order order4 = createOrder(user2, customer.facility)
        order4.customer = customer2
        order4.save(flush: true, failOnError: true)
        Order.executeUpdate("update Order set lastUpdated = ? where id = ?",
                [new LocalDateTime().plusDays(1).withMillisOfDay(0).toDate(), order4.id])
        order4.refresh()

        customerService.assignLastActivity([customer, customer2])
        assert customer2.lastActivity == order4.lastUpdated
        assert customer2.lastActivity != order3.lastUpdated
        assert customer2.lastActivity != customer2.lastUpdated

        assert customer.lastActivity == order2.lastUpdated
        assert customer.lastActivity != order.lastUpdated
        assert customer.lastActivity != customer.lastUpdated
    }

    @Test
    void testLastActivityFilter() {
        User user = createUser()
        Customer customer = createCustomer()
        customer.user = user
        customer.save(flush: true, failOnError: true)

        Date newLastUpdated = new DateTime().withMillisOfSecond(0).minusYears(4).toDate()

        groovySql.execute("update customer set last_updated = :lastUpdated where id = :customerId",
                [lastUpdated: newLastUpdated.format(DateUtil.DATE_AND_TIME_FORMAT), customerId: customer.id])

        customer = customer.refresh()
        assert newLastUpdated == customer.lastUpdated

        FilterCustomerCommand cmd = new FilterCustomerCommand(order: 'desc', sort: 'id')

        assert !customer.lastActivity

        List customers = customerService.findCustomers(cmd, customer.facility, false)
        assert customers.size() == 1
        assert customers.first() == customer
        assert !customer.lastActivity

        cmd.lastActivity = 3
        customers = customerService.findCustomers(cmd, customer.facility, false)
        assert customers.size() == 1
        assert customers.first() == customer
        assert customer.lastActivity == customer.lastUpdated

        cmd.lastActivity = 5
        customers = customerService.findCustomers(cmd, customer.facility, false)
        assert customers.size() == 0
        assert customer.lastActivity == customer.lastUpdated

        cmd.lastActivity = 3
        Invoice invoice = createInvoice(customer)
        customers = customerService.findCustomers(cmd, customer.facility, false)
        assert customers.size() == 0
        assert customer.lastActivity == invoice.lastUpdated.toDate()

        cmd.lastActivity = null
        customers = customerService.findCustomers(cmd, customer.facility, false)
        assert customers.size() == 1
        assert customers.first() == customer
        assert customer.lastActivity == invoice.lastUpdated.toDate()
    }

    @Test
    void testGetConnectedPlayersData() {
        def facility = createFacility()
        def customer1 = createCustomer(facility, "johndoe@matchi.com")
        def customer2 = createCustomer(facility, "jdoe1@matchi.com", "Jane", "Doe")
        def booking1 = createBooking(customer1)
        def booking2 = createBooking(customer2)
        def booking3 = createBooking(customer1)
        new Player(booking: booking1, email: customer1.email).save(failOnError: true, flush: true)
        new Player(booking: booking1, email: "jimmy@matchi.com").save(failOnError: true, flush: true)
        new Player(booking: booking1, email: customer2.email, customer: customer2).save(failOnError: true, flush: true)
        new Player(booking: booking1).save(failOnError: true, flush: true)
        new Player(booking: booking2, email: "someone@matchi.com").save(failOnError: true, flush: true)
        new Player(booking: booking3, email: customer2.email, customer: customer2).save(failOnError: true, flush: true)

        def result = customerService.getConnectedPlayersData(customer1, [], "j")
        assert result.size() == 2
        assert result.find {it.id == "jdoe1@matchi.com" && it.name == "Jane Doe (jdoe1@matchi.com)" && it.fieldValue == "Jane Doe"}
        assert result.find {it.id == "jimmy@matchi.com" && it.name == "jimmy@matchi.com"}

        result = customerService.getConnectedPlayersData(customer1, ["jdoe1@matchi.com"], "j")
        assert result.size() == 1
        assert result[0].id == "jimmy@matchi.com"
        assert result[0].name == "jimmy@matchi.com"

        result = customerService.getConnectedPlayersData(customer1, [], "jane")
        assert result.size() == 1
        assert result[0].id == "jdoe1@matchi.com"
        assert result[0].name == "Jane Doe (jdoe1@matchi.com)"

        result = customerService.getConnectedPlayersData(customer1, [], "jane doe")
        assert result.size() == 1
        assert result[0].id == "jdoe1@matchi.com"

        result = customerService.getConnectedPlayersData(customer1, [], "doe")
        assert result.size() == 1
        assert result[0].id == "jdoe1@matchi.com"
        assert result[0].name == "Jane Doe (jdoe1@matchi.com)"

        result = customerService.getConnectedPlayersData(customer1, [], "doe jane")
        assert result.size() == 1
        assert result[0].id == "jdoe1@matchi.com"

        assert !customerService.getConnectedPlayersData(customer1, [], "john")

        result = customerService.getConnectedPlayersData(customer2, [], "some")
        assert result.size() == 1
        assert result[0].id == "someone@matchi.com"
        assert result[0].name == "someone@matchi.com"
    }

    @Test
    void testListFederationsCustomers() {
        def user = createUser()
        def facility1 = createFacility()
        facility1.setFacilityProperty(FacilityPropertyKey.FEATURE_FEDERATION, "1")
        facility1.setFacilityProperty(FacilityPropertyKey.FEATURE_USE_CUSTOMER_NUMBER_AS_LICENSE, "1")
        facility1.save(failOnError: true, flush: true)
        def facility2 = createFacility()
        facility2.setFacilityProperty(FacilityPropertyKey.FEATURE_FEDERATION, "1")
        facility2.setFacilityProperty(FacilityPropertyKey.FEATURE_USE_CUSTOMER_NUMBER_AS_LICENSE, "1")
        facility2.save(failOnError: true, flush: true)
        def facility3 = createFacility()
        facility3.setFacilityProperty(FacilityPropertyKey.FEATURE_FEDERATION, "1")
        facility3.save(failOnError: true, flush: true)
        def facility4 = createFacility()
        facility4.setFacilityProperty(FacilityPropertyKey.FEATURE_USE_CUSTOMER_NUMBER_AS_LICENSE, "1")
        facility4.save(failOnError: true, flush: true)
        def facility5 = createFacility()
        def customer1 = createCustomer(facility1, null, null, null, null, user)
        createCustomer(facility3, null, null, null, null, user)
        createCustomer(facility4, null, null, null, null, user)
        createCustomer(facility5, null, null, null, null, user)
        createCustomer(facility1)
        createCustomer(facility2)
        createCustomer(facility3)

        def result = customerService.listFederationsCustomers(user)

        assert result.size() == 1
        assert result[0].id == customer1.id
    }

    @Test
    void testFindMatchingCustomers() {
        Facility facility = createFacility()
        Customer customer = createCustomer(facility)
        customer.email = 'sune@matchi.se'
        customer.save(flush: true, failOnError: true)

        User user = createUser(customer.email)
        user.firstname = customer.firstname
        user.lastname = customer.lastname
        user.save(flush: true, failOnError: true)

        List<Customer> customers = customerService.findMatchingCustomers([], user.email, user.firstname, user.lastname)
        assert customers.size() == 1
        assert customers[0] == customer

        customer.user = user
        customer.save(flush: true, failOnError: true)

        customers = customerService.findMatchingCustomers([], user.email, user.firstname, user.lastname)
        assert customers.size() == 0
    }

    @Test
    void testLinkCustomerToSpecificUser() {
        Facility facility = createFacility()
        Customer customer = createCustomer(facility)
        customer.email = 'sune@matchi.se'
        customer.save(flush: true, failOnError: true)

        User user = createUser(customer.email)
        user.firstname = customer.firstname
        user.lastname = customer.lastname
        user.save(flush: true, failOnError: true)

        customerService.linkCustomerToUser(customer, user, true)
        assert Customer.findAllByIdAndUser(customer.id, user).size() == 1

        Customer customer2 = createCustomer(facility)

        // Cannot connect another customer to user
        customerService.linkCustomerToUser(customer2, user, true)
        assert Customer.findAllByIdAndUser(customer2.id, user).size() == 0

        // It wont be saved either if doing this
        customerService.linkCustomerToUser(customer2, user, false)
        assert Customer.findAllByIdAndUser(customer2.id, user).size() == 0
    }

    void testArchiveCustomer() {
        def facility = createFacility()
        def group = new Group(name: "test", facility: facility).save(failOnError: true, flush: true)
        def customer1 = createCustomer(facility, null, null, null, null, createUser())
        assert CustomerGroup.link(customer1, group)
        createMembership(customer1)
        assert customer1.membership
        def customer2 = createCustomer(facility)
        createMembership(customer2)
        assert customer2.membership
        def mf = new MembershipFamily(contact: customer2).save(failOnError: true, flush: true)
        customer1.membership.family = mf
        customer1.save(failOnError: true, flush: true)
        customer1.refresh()
        customer2.membership.family = mf
        customer2.save(failOnError: true, flush: true)
        customer2.refresh()

        customerService.archiveCustomer(customer1)

        def customer = Customer.findById(customer1.id)
        assert customer.archived
        assert !customer.user
        assert !customer.memberships
        assert !CustomerGroup.count()
        assert MembershipFamily.count() == 1
        assert Membership.countByFamily(MembershipFamily.first()) == 1
        assert Membership.findByFamily(MembershipFamily.first()).id == customer2.membership.id

        customerService.archiveCustomer(customer2)
        customer = Customer.findById(customer2.id)
        assert customer.archived
        assert !customer.memberships
        assert !MembershipFamily.count()
    }

    void testGetMembersIds() {
        def today = new LocalDate()
        def facility = createFacility()
        def facility2 = createFacility()
        def customer1 = createCustomer(facility)
        createMembership(customer1, today, today.plusDays(1), today.plusDays(2))
        def customer2 = createCustomer(facility)
        createMembership(customer2, today.plusDays(1), today.plusDays(1), today.plusDays(2))
        def customer3 = createCustomer(facility)
        createMembership(customer3, today.minusDays(5), today.minusDays(1), today)
        def customer4 = createCustomer(facility2)
        createMembership(customer4, today, today.plusDays(1), today.plusDays(2))

        def result = customerService.getMembersIds(facility)

        assert result
        assert result.size() == 2
        assert result.contains(customer1.id)
        assert result.contains(customer3.id)

        result = customerService.getMembersIds(facility2)

        assert result
        assert result.size() == 1
        assert result[0] == customer4.id

        result = customerService.getMembersIds(createFacility())

        assert result
        assert result.size() == 1
        assert result[0] == -1L
    }

    void testCourseFilter() {
        def user = createUser()
        def facility = createFacility()
        def customer = createCustomer()
        customer.user = user
        customer.facility = facility
        customer.save()
        def courseParticipant = createCourseParticipant(customer)
        FilterCustomerCommand cmd = new FilterCustomerCommand(order: 'desc', sort: 'id')
        cmd.courses = [courseParticipant?.activity?.id]
        List customers = customerService.findCustomers(cmd, customer.facility, false)
        assert customers.size() == 1
        assert customers[0] == customer
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
