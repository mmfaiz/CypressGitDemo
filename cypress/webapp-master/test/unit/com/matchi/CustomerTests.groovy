package com.matchi

import com.matchi.facility.FilterCustomerCommand
import com.matchi.membership.Membership
import com.matchi.membership.MembershipFamily
import com.matchi.membership.MembershipType
import com.matchi.orders.Order
import grails.test.GrailsMock
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.joda.time.LocalDate
import org.joda.time.DateTime
import org.junit.After
import org.junit.Before
import org.junit.Test

@TestFor(Customer)
@Mock([Customer, CustomerGroup, Group, Facility, Membership, MembershipFamily, User, MembershipType])
class CustomerTests {

    Customer customer
    Facility facility

    @Before
    void setUp() {
        facility = new Facility(id: 1l)
        customer = _createCustomer()
    }

    @After
    void tearDown() {

    }

    @Test
    void testBirthYear() {
        customer.birthyear = 1970
        assert customer.shortBirthYear() == '-70'
        customer.birthyear = 2007
        assert customer.shortBirthYear() == '-07'
        // TODO This should really be -07 if we allow birthyears that old
        customer.birthyear = 0007
        assert customer.shortBirthYear() == ''
        // TODO These are incorrect birthyears and should not be present in the db
        customer.birthyear = 197
        assert customer.shortBirthYear() == ''
        customer.birthyear = 1
        assert customer.shortBirthYear() == ''
    }
    @Test
    void testCustomerBelongsToGroupReturnsTrue() {
        def group = new Group()
        group.id = 1
        customer.customerGroups = [CustomerGroup.link(customer, group)]
        assert customer.belongsTo(group)
    }
    @Test
    void testCustomerBelongsToGroupReturnsFalse() {
        def group = new Group()
        group.id = 1
        CustomerGroup.link(customer, group)

        def otherGroup = new Group()
        group.id = 2

        assert !customer.belongsTo(otherGroup)
    }

    @Test
    void testCustomerHasMembership() {
        assert !customer.hasMembership()

        def today = new LocalDate()
        customer.addToMemberships(new Membership(startDate: today, endDate: today,
                gracePeriodEndDate: today))

        assert customer.hasMembership()
    }

    @Test

    void testCustomerHasAnyMembershipAtSlotTime() {
        Slot slot = new Slot(startTime: new Date(), endTime: new Date())

        HashSet<Membership> membershipsSetA = new LinkedHashSet<Membership>()
        HashSet<Membership> membershipsSetB = new LinkedHashSet<Membership>()
        HashSet<Membership> membershipsSetC = new LinkedHashSet<Membership>()

        GrailsMock mockedMembershipA = mockFor(Membership) //invalid
        mockedMembershipA.demand.coversSlotTime(2) { s -> return false }

        GrailsMock mockedMembershipB = mockFor(Membership)
        mockedMembershipB.demand.coversSlotTime(2) { s -> return true }


        //Test if one is false and one is true
        membershipsSetA.add(mockedMembershipA.createMock())
        membershipsSetA.add(mockedMembershipB.createMock())
        Customer customerA = new Customer(memberships: membershipsSetA)
        assert customerA.hasAnyMembershipAtSlotTime(slot)


        //Test if one is false
        membershipsSetB.add(mockedMembershipA.createMock())
        Customer customerB = new Customer(memberships: membershipsSetB)
        assert !customerB.hasAnyMembershipAtSlotTime(slot)

        //Test if one is true
        membershipsSetC.add(mockedMembershipB.createMock())
        Customer customerC = new Customer(memberships: membershipsSetC)
        assert customerC.hasAnyMembershipAtSlotTime(slot)
    }

    @Test
    void testCustomerHasActiveMembership() {
        assert !customer.hasActiveMembership()

        def today = new LocalDate()
        customer.addToMemberships(new Membership(startDate: today.minusDays(1),
                endDate: today.minusDays(1), gracePeriodEndDate: today.minusDays(1),
                order: new Order(price: 0, status: Order.Status.COMPLETED)))

        assert !customer.hasActiveMembership()

        customer.addToMemberships(new Membership(startDate: today,
                endDate: today, gracePeriodEndDate: today,
                order: new Order(price: 0, status: Order.Status.COMPLETED)))

        assert customer.hasActiveMembership()
    }

    @Test
    void testCustomerCanRecieveMail() {
        customer.email = ""
        assert !customer.canReceiveMail()

        customer.user = new User(email: "user@email.com")
        assert !customer.canReceiveMail()

        customer.user = null
        customer.email = "email@customer.com"
        assert customer.canReceiveMail()
    }

    @Test
    void testInvoiceAddressCompanyName() {
        customer.type = Customer.CustomerType.ORGANIZATION
        customer.companyname = "Innitech"

        assert customer.getInvoiceAddress().name == customer.companyname
    }

    @Test
    void testInvoiceNameWithContact() {
        customer.type = Customer.CustomerType.ORGANIZATION
        customer.companyname = "Innitech"
        customer.contact = "John Doe"

        assert customer.getInvoiceAddress().names.get(1) == customer.contact
    }

    @Test
    void testInvoiceNameWithOverriddenContact() {
        customer.type = Customer.CustomerType.ORGANIZATION
        customer.companyname = "Innitech"
        customer.contact = "John Doe"
        customer.invoiceContact = "Joanna Doe"

        assert customer.getInvoiceAddress().names.get(1) == customer.invoiceContact
    }

    @Test
    void testInvoiceAddressContactName() {
        customer.type = Customer.CustomerType.MALE
        customer.firstname = "John"
        customer.lastname = "Doe"
        customer.invoiceContact = "Sara Doe"

        assert customer.getInvoiceAddress().names.get(0) == customer.invoiceContact

    }

    @Test
    void testInvoiceAddress() {
        customer.type = Customer.CustomerType.MALE
        customer.firstname = "John"
        customer.lastname = "Doe"
        customer.address1 = "Testgatan 12"
        customer.address2 = "3 tr."
        customer.city     = "Gothenburg"
        customer.zipcode  = "413 14"

        def invoiceAddress = customer.getInvoiceAddress()

        assert invoiceAddress.address1 == customer.address1
        assert invoiceAddress.address2 == customer.address2
        assert invoiceAddress.city     == customer.city
        assert invoiceAddress.zipcode     == customer.zipcode
    }

    @Test
    void testInvoiceAddressOverridden() {
        customer.type = Customer.CustomerType.MALE
        customer.firstname = "John"
        customer.lastname = "Doe"
        customer.address1 = "Testgatan 12"
        customer.address2 = "3 tr."
        customer.city     = "Gothenburg"
        customer.zipcode  = "413 14"

        // override
        customer.invoiceAddress1 = "Annangata 12"
        customer.invoiceAddress2 = "5 tr."
        customer.invoiceCity     = "Stockholm"
        customer.invoiceZipcode  = "411 25"

        def invoiceAddress = customer.getInvoiceAddress()

        assert invoiceAddress.address1 == customer.invoiceAddress1
        assert invoiceAddress.address2 == customer.invoiceAddress2
        assert invoiceAddress.city     == customer.invoiceCity
        assert invoiceAddress.zipcode     == customer.invoiceZipcode
    }

    @Test
    void testUnlinkGroups() {
        customer.customerGroups = [CustomerGroup.link(customer, new Group())]
        assert customer.customerGroups

        customer.unlinkGroups()

        assert !customer.customerGroups
    }

    @Test
    void testHasInvoiceEmail() {
        assert customer.hasInvoiceEmail()
        customer.email = null
        assert !customer.hasInvoiceEmail()

        customer.invoiceEmail = "invoice@email.com"
        assert customer.hasInvoiceEmail()
        customer.invoiceEmail = null
        assert !customer.hasInvoiceEmail()

        customer.guardianEmail = "guardian1@email.com"
        assert customer.hasInvoiceEmail()
        customer.guardianEmail = null
        assert !customer.hasInvoiceEmail()

        customer.guardianEmail2 = "guardian2@email.com"
        assert customer.hasInvoiceEmail()
        customer.guardianEmail2 = null
        assert !customer.hasInvoiceEmail()
    }

    @Test
    void testGetCustomerInvoiceEmail() {
        assert customer.getCustomerInvoiceEmail() == customer.email

        customer.invoiceEmail = "invoice@email.com"

        assert customer.getCustomerInvoiceEmail() == customer.invoiceEmail

        customer.guardianEmail2 = "guardian2@email.com"

        assert customer.getCustomerInvoiceEmail() == customer.guardianEmail2

        customer.guardianEmail = "guardian1@email.com"

        assert customer.getCustomerInvoiceEmail() == customer.guardianEmail
    }

    @Test
    void testHasMembershipTypeNoMembership() {
        MembershipType type = new MembershipType(id: 1l)
        assert !customer.hasMembershipType(type)
    }

    @Test
    void testHasMembershipTypeMembership() {
        MembershipType type = new MembershipType(id: 1l)
        Membership membership = new Membership(type: type,
                startDate: new LocalDate(), gracePeriodEndDate: new LocalDate())
        customer.addToMemberships(membership)

        assert customer.hasMembershipType(type) == true
    }

    @Test
    void testHasMembershipTypeMembershipWithoutType() {
        MembershipType type = new MembershipType(id: 1l)
        Membership membership = new Membership(
                startDate: new LocalDate(), gracePeriodEndDate: new LocalDate())
        customer.addToMemberships(membership)

        assert !customer.hasMembershipType(type)
    }

    @Test
    void testLastActivity() {
        assert !customer.lastActivity

        Date lastActivityDate = new Date()
        customer.lastActivity = lastActivityDate

        assert customer.lastActivity == lastActivityDate
    }

    @Test
    void testGetPersonalNumber() {
        defineBeans {
            dateUtil(DateUtil) {
                grailsApplication = [
                        config: [
                                customer: [
                                        personalNumber: [
                                                settings: [
                                                        NO: [
                                                                securityNumberLength: 5,
                                                                orgPattern: /^(\d{9})$/,
                                                                longFormat: "ddMMyyyy",
                                                                shortFormat: "ddMMyy",
                                                                readableFormat: "ddmmyy"
                                                        ],
                                                        SE: [
                                                                securityNumberLength: 4,
                                                                orgPattern: /^(\d{6}|\d{8})(?:-(\d{4}))?$/,
                                                                longFormat: "yyyyMMdd",
                                                                shortFormat: "yyMMdd",
                                                                readableFormat: "yymmdd"
                                                        ]
                                                ]
                                        ]
                                ]
                        ]
                ]
            }
        }

        assert customer.getPersonalNumber() == ""

        customer.securityNumber = "1234"

        assert customer.getPersonalNumber() == customer.securityNumber

        customer.securityNumber = null

        // No configuration, assuming it will go with the default settings
        customer.dateOfBirth = new DateTime("1988-12-09").toDate()
        PersonalNumberSettings personalNumberSettings = new PersonalNumberSettings()
        assert customer.getPersonalNumber() == customer.dateOfBirth.format(personalNumberSettings.longFormat)
        assert customer.getPersonalNumber() == "19881209"

        customer.securityNumber = "1234"
        assert customer.getPersonalNumber() == "19881209-1234"

        facility.country = "SE"
        customer.securityNumber = null
        assert customer.getPersonalNumber() == "19881209"

        customer.securityNumber = "1234"
        assert customer.getPersonalNumber() == "19881209-1234"

        // And Norway!
        facility.country = "NO"
        customer.securityNumber = null
        assert customer.getPersonalNumber() == "09121988"

        customer.securityNumber = "12345"
        assert customer.getPersonalNumber() == "09121988-12345"
    }

    void testGetLicense() {
        assert !new Customer().license
        assert new Customer(number: 100L).license == 100L
    }

    void testHasMembership() {
        def today = new LocalDate()
        def customer = new Customer()

        assert !customer.hasMembership()

        customer.addToMemberships(new Membership(startDate: today.plusDays(11),
                gracePeriodEndDate: today.plusDays(20)))

        assert !customer.hasMembership()

        customer.addToMemberships(new Membership(startDate: today,
                gracePeriodEndDate: today.plusDays(10)))

        assert customer.hasMembership()
    }

    void testGetMembership() {
        def today = new LocalDate()
        def customer = new Customer()
        def m1 = new Membership(startDate: today.minusDays(10), gracePeriodEndDate: today.minusDays(1))
        def m2 = new Membership(startDate: today, gracePeriodEndDate: today.plusDays(10))
        def m3 = new Membership(startDate: today.plusDays(11), gracePeriodEndDate: today.plusDays(20))
        def m4 = new Membership(startDate: today.plusDays(21), gracePeriodEndDate: today.plusDays(30))
        customer.memberships = [m1, m2, m3, m4]

        assert customer.getMembership() == m2
        assert customer.getMembership(new LocalDate(today.minusDays(5))) == m1
        assert customer.getMembership(new LocalDate(today.plusDays(5))) == m2
        assert customer.getMembership(new LocalDate(today.plusDays(15))) == m3
        assert customer.getMembership(new LocalDate(today.plusDays(25))) == m4
    }

    void testGetMembershipByFilter() {
        def today = new LocalDate()
        def mt = new MembershipType()
        mt.id = 5L
        def m1 = new Membership(startDate: today.minusDays(10), gracePeriodEndDate: today.minusDays(1),
                order: new Order(status: Order.Status.COMPLETED))
        def m2 = new Membership(startDate: today, gracePeriodEndDate: today.plusDays(10),
                order: new Order(status: Order.Status.COMPLETED))
        def m3 = new Membership(startDate: today.plusDays(11), gracePeriodEndDate: today.plusDays(20),
                order: new Order(status: Order.Status.COMPLETED), activated: false)
        def m4 = new Membership(startDate: today.plusDays(21), gracePeriodEndDate: today.plusDays(30),
                order: new Order(status: Order.Status.NEW))
        def m5 = new Membership(startDate: today.plusDays(31), gracePeriodEndDate: today.plusDays(40),
                order: new Order(status: Order.Status.COMPLETED), family: new MembershipFamily())
        def m6 = new Membership(startDate: today.plusDays(41), gracePeriodEndDate: today.plusDays(50),
                order: new Order(status: Order.Status.COMPLETED), type: mt)
        def m7 = new Membership(startDate: today.plusDays(51), gracePeriodEndDate: today.plusDays(60),
                order: new Order(status: Order.Status.COMPLETED))
        def customer = new Customer(memberships: [m1, m2, m3, m4, m5, m6, m7])

        assert customer.getMembershipByFilter(null) == m2
        assert customer.getMembershipByFilter(new FilterCustomerCommand()) == m2
        assert customer.getMembershipByFilter(new FilterCustomerCommand(
                membershipStartDate: today)) == m2
        assert customer.getMembershipByFilter(new FilterCustomerCommand(
                membershipStartDate: today.minusDays(1))) == m1
        assert customer.getMembershipByFilter(new FilterCustomerCommand(
                membershipStartDate: today.plusDays(11))) == m3
        assert !customer.getMembershipByFilter(new FilterCustomerCommand(
                membershipStartDate: today.minusDays(100)))
        assert !customer.getMembershipByFilter(new FilterCustomerCommand(
                membershipStartDate: today.minusDays(100), membershipEndDate: today.minusDays(50)))
        assert customer.getMembershipByFilter(new FilterCustomerCommand(
                membershipStartDate: today.minusDays(100), membershipEndDate: today.plusDays(100))) == m7
        assert customer.getMembershipByFilter(new FilterCustomerCommand(
                membershipStartDate: today.minusDays(100), membershipEndDate: today.plusDays(100),
                status: [FilterCustomerCommand.MemberStatus.UNPAID])) == m4
        assert customer.getMembershipByFilter(new FilterCustomerCommand(
                membershipStartDate: today.minusDays(100), membershipEndDate: today.plusDays(100),
                members: [FilterCustomerCommand.ShowMembers.FAMILY_MEMBERS])) == m5
        assert customer.getMembershipByFilter(new FilterCustomerCommand(
                membershipStartDate: today.minusDays(100), membershipEndDate: today.plusDays(100),
                type: [mt.id])) == m6
    }

    void testArchivedOrDeleted() {
        customer.archived = true
        assert customer.isArchivedOrDeleted()

        customer.archived = false
        customer.deleted = new Date()
        assert customer.isArchivedOrDeleted()

        customer.archived = true
        customer.deleted = new Date()
        assert customer.isArchivedOrDeleted()

        customer.archived = false
        customer.deleted = null
        assert !customer.isArchivedOrDeleted()
    }

    void testIsEmailReceivable() {
        customer.clubMessagesDisabled = true
        assert !customer.isEmailReceivable()

        customer.clubMessagesDisabled = false
        assert customer.isEmailReceivable()

        customer.email = "invalid-email"
        assert !customer.isEmailReceivable()
    }

    void testGetGuardianMessageInfo() {

        assert 0 == customer.getGuardianMessageInfo().size()

        customer.guardianName = "test"
        customer.guardianEmail = "test@matchi.se"

        assert 1 == customer.getGuardianMessageInfo().size()

        customer.guardianName2 = "test2"
        customer.guardianEmail2 = "test2@matchi.se"

        assert 2 == customer.getGuardianMessageInfo().size()

        customer.guardianEmail = "invalid-email"

        assert 1 == customer.getGuardianMessageInfo().size()

        customer.guardianEmail2 = "invalid-email"

        assert 0 == customer.getGuardianMessageInfo().size()
    }

    void testHasNonEndedMembership() {
        def today = new LocalDate()
        def m1 = new Membership(gracePeriodEndDate: today)
        m1.id = 100L
        def c1 = new Customer(memberships: [m1])
        def m2 = new Membership(gracePeriodEndDate: today.minusDays(1))
        def c2 = new Customer(memberships: [m2])
        def m3 = new Membership(gracePeriodEndDate: today.plusDays(1))
        def c3 = new Customer(memberships: [m3])

        assert c1.hasNonEndedMembership(null)
        assert !c1.hasNonEndedMembership(m1.id)
        assert !c2.hasNonEndedMembership(null)
        assert c3.hasNonEndedMembership(null)
    }

    def _createCustomer() {
        Customer customer = new Customer()
        customer.email = "email@customer.com"
        customer.firstname = "FIRSTNAME"
        customer.lastname = "LASTNAME"
        customer.facility = facility

        return customer
    }
}
