package com.matchi.membership

import com.matchi.Customer
import com.matchi.CustomerService
import com.matchi.DateUtil
import com.matchi.Facility
import com.matchi.FacilityProperty
import com.matchi.Municipality
import com.matchi.OrderStatusService
import com.matchi.Region
import com.matchi.User
import com.matchi.Slot
import com.matchi.adyen.AdyenService
import com.matchi.facility.FilterCustomerCommand
import com.matchi.integration.IntegrationService
import com.matchi.orders.AdyenOrderPayment
import com.matchi.orders.CashOrderPayment
import com.matchi.orders.Order
import com.matchi.orders.OrderPayment
import grails.test.GrailsMock
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.codehaus.groovy.grails.commons.InstanceFactoryBean
import org.joda.time.LocalDate
import org.junit.Before
import org.junit.Test

import static com.matchi.TestUtils.*
import static com.matchi.TestUtils.createMembership

@TestFor(Membership)
@Mock([MembershipType, MembershipFamily, Customer, Facility, User, Region, Municipality, Order])
class MembershipTests {

    Facility facility
    Customer customer1
    Customer customer2
    MembershipType type1
    User user
    OrderStatusService orderStatusService

    @Before
    void setUp() {
        user = new User(id: 1l)
        facility = new Facility(id: 1l)
        customer1 = new Customer(id: 1l, facility: facility)
        customer2 = new Customer(id: 2l, facility: facility)
        type1 = new MembershipType(name: "Type", price: 100, facility: facility)

        orderStatusService = new OrderStatusService()
        def mockIntegrationService = mockFor(IntegrationService)
        orderStatusService.integrationService = mockIntegrationService.createMock()
        mockIntegrationService.demand.send(1..1) {  }
        defineBeans {
            orderStatusService(InstanceFactoryBean, orderStatusService, OrderStatusService)
        }
    }

    @Test
    void testUnlinkMembership() {
        def today = new LocalDate()
        def customer = createCustomer()
        createMembership(customer, today, today.plusDays(1), today.plusDays(2))
        createMembership(customer, today.plusDays(3), today.plusDays(4), today.plusDays(5))

        Membership.unlink(customer)

        assert !customer.memberships
        assert !Membership.count()
    }

    void testNewInstanceWithDates() {
        def today = new LocalDate()
        def facility = new Facility(membershipValidTimeAmount: 2,
                membershipValidTimeUnit: TimeUnit.MONTH)

        // test without membership type
        def m = Membership.newInstanceWithDates(null, facility, today)

        assert m != null
        assert m.startDate == today
        assert m.endDate == today.plusMonths(2).minusDays(1)
        assert m.gracePeriodEndDate == m.endDate
        assert m.type == null

        // test with grace period
        facility.membershipGraceNrOfDays = 10

        m = Membership.newInstanceWithDates(null, facility, today)

        assert m.startDate == today
        assert m.endDate == today.plusMonths(2).minusDays(1)
        assert m.gracePeriodEndDate == m.endDate.plusDays(10)

        // test override facility settings by membership type
        def mt = new MembershipType(validTimeAmount: 3, validTimeUnit: TimeUnit.WEEK)

        m = Membership.newInstanceWithDates(mt, facility, today)

        assert m.startDate == today
        assert m.endDate == today.plusWeeks(3).minusDays(1)
        assert m.gracePeriodEndDate == m.endDate.plusDays(10)
        assert m.type != null

        // test start date in future
        m = Membership.newInstanceWithDates(mt, facility, today.plusDays(5))

        assert m.startDate == today.plusDays(5)
        assert m.endDate == m.startDate.plusWeeks(3).minusDays(1)
        assert m.gracePeriodEndDate == m.endDate.plusDays(10)

        // test yearly membership with fixed start date
        mt.validTimeUnit = TimeUnit.YEAR
        facility.yearlyMembershipStartDate = new LocalDate("1970-01-01")

        m = Membership.newInstanceWithDates(mt, facility, new LocalDate("2018-12-01"))

        assert m.startDate == new LocalDate("2018-12-01")
        assert m.endDate == new LocalDate("2020-12-31")
        assert m.gracePeriodEndDate == m.endDate.plusDays(10)

        // test fixed start + purchase days in advance
        facility.yearlyMembershipPurchaseDaysInAdvance = 31

        m = Membership.newInstanceWithDates(mt, facility, new LocalDate("2018-12-01"))

        assert m.startDate == new LocalDate("2019-01-01")
        assert m.endDate == new LocalDate("2021-12-31")
        assert m.gracePeriodEndDate == m.endDate.plusDays(10)

        m = Membership.newInstanceWithDates(mt, facility, new LocalDate("2018-11-30"))

        assert m.startDate == new LocalDate("2018-11-30")
        assert m.endDate == new LocalDate("2020-12-31")
        assert m.gracePeriodEndDate == m.endDate.plusDays(10)

        facility.yearlyMembershipStartDate = new LocalDate("1970-06-01")

        m = Membership.newInstanceWithDates(mt, facility, new LocalDate("2018-09-10"))

        assert m.startDate == new LocalDate("2018-09-10")
        assert m.endDate == new LocalDate("2021-05-31")
        assert m.gracePeriodEndDate == m.endDate.plusDays(10)

        m = Membership.newInstanceWithDates(mt, facility, new LocalDate("2018-05-10"))

        assert m.startDate == new LocalDate("2018-06-01")
        assert m.endDate == new LocalDate("2021-05-31")
        assert m.gracePeriodEndDate == m.endDate.plusDays(10)

        m = Membership.newInstanceWithDates(mt, facility, new LocalDate("2018-04-10"))

        assert m.startDate == new LocalDate("2018-04-10")
        assert m.endDate == new LocalDate("2020-05-31")
        assert m.gracePeriodEndDate == m.endDate.plusDays(10)

        m = Membership.newInstanceWithDates(mt, facility, new LocalDate("2018-06-01"))

        assert m.startDate == new LocalDate("2018-06-01")
        assert m.endDate == new LocalDate("2021-05-31")
        assert m.gracePeriodEndDate == m.endDate.plusDays(10)

        facility.yearlyMembershipStartDate = new LocalDate("1970-02-01")

        m = Membership.newInstanceWithDates(mt, facility, new LocalDate("2018-12-31"))

        assert m.startDate == new LocalDate("2018-12-31")
        assert m.endDate == new LocalDate("2021-01-31")
        assert m.gracePeriodEndDate == m.endDate.plusDays(10)

        m = Membership.newInstanceWithDates(mt, facility, new LocalDate("2019-01-01"))

        assert m.startDate == new LocalDate("2019-02-01")
        assert m.endDate == new LocalDate("2022-01-31")
        assert m.gracePeriodEndDate == m.endDate.plusDays(10)

        // test override facility settings
        mt.startDateYearly = new LocalDate("1970-09-01")

        m = Membership.newInstanceWithDates(mt, facility, new LocalDate("2018-08-01"))

        assert m.startDate == new LocalDate("2018-09-01")
        assert m.endDate == new LocalDate("2021-08-31")
        assert m.gracePeriodEndDate == m.endDate.plusDays(10)

        mt.purchaseDaysInAdvanceYearly = 10

        m = Membership.newInstanceWithDates(mt, facility, new LocalDate("2018-08-01"))

        assert m.startDate == new LocalDate("2018-08-01")
        assert m.endDate == new LocalDate("2020-08-31")
        assert m.gracePeriodEndDate == m.endDate.plusDays(10)
    }

    void testIsActive() {
        def today = new LocalDate()
        assert new Membership(startDate: today, gracePeriodEndDate: today,
                order: new Order(status: Order.Status.COMPLETED)).isActive()
        assert new Membership(startDate: today, gracePeriodEndDate: today,
                order: new Order(status: Order.Status.CONFIRMED)).isActive()
        assert !new Membership(startDate: today, gracePeriodEndDate: today,
                order: new Order(status: Order.Status.CANCELLED)).isActive()
        assert !new Membership(startDate: today, gracePeriodEndDate: today,
                order: new Order(status: Order.Status.ANNULLED)).isActive()
        assert !new Membership(activated: false, startDate: today, gracePeriodEndDate: today,
                order: new Order(status: Order.Status.COMPLETED)).isActive()
        assert new Membership(cancel: true,
                startDate: today, gracePeriodEndDate: today,
                order: new Order(status: Order.Status.COMPLETED)).isActive()
        assert !new Membership(activated: false,
                startDate: today, gracePeriodEndDate: today,
                order: new Order(status: Order.Status.COMPLETED)).isActive()
        assert !new Membership(startDate: today, gracePeriodEndDate: today,
                order: new Order(status: Order.Status.NEW)).isActive()
        assert !new Membership(startDate: today, gracePeriodEndDate: today,
                order: new Order(status: Order.Status.COMPLETED, price: 100)).isActive()
        assert new Membership(startDate: today, gracePeriodEndDate: today,
                order: new Order(status: Order.Status.COMPLETED, price: 100,
                        payments: [new CashOrderPayment(amount: 100,
                                status: OrderPayment.Status.CAPTURED)])).isActive()
        assert new Membership(startDate: today.minusDays(1), gracePeriodEndDate: today.plusDays(1),
                order: new Order(status: Order.Status.COMPLETED)).isActive()
        assert new Membership(startDate: today.minusDays(1), gracePeriodEndDate: today,
                order: new Order(status: Order.Status.COMPLETED)).isActive()
        assert !new Membership(startDate: today.minusDays(2), gracePeriodEndDate: today.minusDays(1),
                order: new Order(status: Order.Status.COMPLETED)).isActive()
        assert new Membership(startDate: today, gracePeriodEndDate: today.plusDays(1),
                order: new Order(status: Order.Status.COMPLETED)).isActive()
        assert !new Membership(startDate: today.plusDays(1), gracePeriodEndDate: today.plusDays(1),
                order: new Order(status: Order.Status.COMPLETED)).isActive()
    }

    void testIsEnding() {
        LocalDate today = new LocalDate()
        assert new Membership(endDate: today.minusDays(1), gracePeriodEndDate: today,
                order: new Order(status: Order.Status.COMPLETED)).isEnding()
        assert !new Membership(endDate: today, gracePeriodEndDate: today,
                order: new Order(status: Order.Status.COMPLETED)).isEnding()
    }

    void testIsUpcoming() {
        assert !new Membership(startDate: new LocalDate().minusDays(1)).isUpcoming()
        assert !new Membership(startDate: new LocalDate()).isUpcoming()
        assert new Membership(startDate: new LocalDate().plusDays(1)).isUpcoming()
    }

    void testCoversSlotTime() {
        Membership paidMembership = new Membership(
                order: new Order(status: Order.Status.COMPLETED),
                startDate: new LocalDate().minusDays(1),
                endDate: new LocalDate().plusDays(1),
                gracePeriodEndDate: new LocalDate().plusDays(2),
                startingGracePeriodDays: 1
        )

        assert !paidMembership.coversSlotTime(new Slot( //before startDate
                startTime: new LocalDate().minusDays(2).toDate(),
                endTime: new LocalDate().minusDays(2).toDate() //Same time as startTime but shouldn't matter
        ))

        assert paidMembership.coversSlotTime(new Slot( //on startDate
                startTime: new LocalDate().minusDays(1).toDate(),
                endTime: new LocalDate().minusDays(1).toDate()
        ))

        assert paidMembership.coversSlotTime(new Slot( //on today
                startTime: new LocalDate().minusDays(0).toDate(),
                endTime: new LocalDate().minusDays(0).toDate()
        ))

        assert paidMembership.coversSlotTime(new Slot( //on endDate
                startTime: new LocalDate().plusDays(1).toDate(),
                endTime: new LocalDate().plusDays(1).toDate()
        ))

        assert paidMembership.coversSlotTime(new Slot( //after startDate but on GracePeriodEndDate
                startTime: new LocalDate().plusDays(2).toDate(),
                endTime: new LocalDate().plusDays(2).toDate()
        ))

        assert !paidMembership.coversSlotTime(new Slot( //after startDate but on GracePeriodEndDate
                startTime: new LocalDate().plusDays(3).toDate(),
                endTime: new LocalDate().plusDays(3).toDate()
        ))

        Membership unpaidMembership = new Membership(
                order: new Order(status: Order.Status.ANNULLED),
                startDate: new LocalDate().minusDays(1),
                endDate: new LocalDate().plusDays(1),
                gracePeriodEndDate: new LocalDate().plusDays(2),
                startingGracePeriodDays: 1
        )

        assert unpaidMembership.coversSlotTime(new Slot( //on startDate and grace starting period
                startTime: new LocalDate().minusDays(1).toDate(),
                endTime: new LocalDate().minusDays(1).toDate() //Same time as startTime but shouldn't matter
        ))

        assert !unpaidMembership.coversSlotTime(new Slot( //after grace starting period
                startTime: new LocalDate().toDate(),
                endTime: new LocalDate().toDate()
        ))
    }

    void testIsPaid() {
        assert new Membership(order: new Order(status: Order.Status.COMPLETED)).isPaid()
        assert new Membership(order: new Order(status: Order.Status.CONFIRMED)).isPaid()
        assert !new Membership(order: new Order(status: Order.Status.CANCELLED)).isPaid()
        assert !new Membership(order: new Order(status: Order.Status.ANNULLED)).isPaid()
        assert !new Membership(order: new Order(status: Order.Status.NEW)).isPaid()
        assert !new Membership(order: new Order(status: Order.Status.CONFIRMED, price: 100)).isPaid()
        assert new Membership(order: new Order(status: Order.Status.CONFIRMED, price: 100,
                payments: [new AdyenOrderPayment(amount: 100, status: OrderPayment.Status.AUTHED)])).isPaid()
        assert new Membership(order: new Order(status: Order.Status.COMPLETED, price: 100,
                payments: [new CashOrderPayment(amount: 100, status: OrderPayment.Status.CAPTURED)])).isPaid()
        assert !new Membership(order: new Order(status: Order.Status.COMPLETED, price: 100,
                payments: [new CashOrderPayment(amount: 100, status: OrderPayment.Status.NEW)])).isPaid()
        assert !new Membership(order: new Order(status: Order.Status.COMPLETED, price: 100,
                payments: [new CashOrderPayment(amount: 100, status: OrderPayment.Status.FAILED)])).isPaid()
        assert !new Membership(order: new Order(status: Order.Status.COMPLETED, price: 100,
                payments: [new CashOrderPayment(amount: 100, status: OrderPayment.Status.PENDING)])).isPaid()
        assert !new Membership(order: new Order(status: Order.Status.COMPLETED, price: 100,
                payments: [new CashOrderPayment(amount: 100, status: OrderPayment.Status.ANNULLED)])).isPaid()
        assert !new Membership(order: new Order(status: Order.Status.COMPLETED, price: 100,
                payments: [new CashOrderPayment(amount: 100, credited: 100,
                        status: OrderPayment.Status.CREDITED)])).isPaid()
        assert new Membership(order: new Order(status: Order.Status.COMPLETED, price: 100,
                payments: [new CashOrderPayment(amount: 100, credited: 100, status: OrderPayment.Status.CREDITED),
                           new CashOrderPayment(amount: 100, status: OrderPayment.Status.CAPTURED)])).isPaid()
    }

    void testIsInStatus() {
        assert !new Membership(order: new Order()).isInStatus(
                FilterCustomerCommand.MemberStatus.PENDING)
        assert !new Membership(order: new Order()).isInStatus(
                FilterCustomerCommand.MemberStatus.PAID)
        assert new Membership(order: new Order()).isInStatus(
                FilterCustomerCommand.MemberStatus.UNPAID)
        assert !new Membership(order: new Order()).isInStatus(
                FilterCustomerCommand.MemberStatus.CANCEL)

        assert new Membership(activated: false, order: new Order()).isInStatus(
                FilterCustomerCommand.MemberStatus.PENDING)
        assert !new Membership(activated: false, order: new Order()).isInStatus(
                FilterCustomerCommand.MemberStatus.PAID)
        assert !new Membership(activated: false, order: new Order()).isInStatus(
                FilterCustomerCommand.MemberStatus.UNPAID)
        assert !new Membership(activated: false, order: new Order()).isInStatus(
                FilterCustomerCommand.MemberStatus.CANCEL)

        assert new Membership(activated: false, order: new Order(status: Order.Status.COMPLETED)).isInStatus(
                FilterCustomerCommand.MemberStatus.PENDING)
        assert !new Membership(activated: false, order: new Order(status: Order.Status.COMPLETED)).isInStatus(
                FilterCustomerCommand.MemberStatus.PAID)
        assert !new Membership(activated: false, order: new Order(status: Order.Status.COMPLETED)).isInStatus(
                FilterCustomerCommand.MemberStatus.UNPAID)
        assert !new Membership(activated: false, order: new Order(status: Order.Status.COMPLETED)).isInStatus(
                FilterCustomerCommand.MemberStatus.CANCEL)

        assert !new Membership(order: new Order(status: Order.Status.COMPLETED)).isInStatus(
                FilterCustomerCommand.MemberStatus.PENDING)
        assert new Membership(order: new Order(status: Order.Status.COMPLETED)).isInStatus(
                FilterCustomerCommand.MemberStatus.PAID)
        assert !new Membership(order: new Order(status: Order.Status.COMPLETED)).isInStatus(
                FilterCustomerCommand.MemberStatus.UNPAID)
        assert !new Membership(order: new Order(status: Order.Status.COMPLETED)).isInStatus(
                FilterCustomerCommand.MemberStatus.CANCEL)

        assert !new Membership(order: new Order(status: Order.Status.COMPLETED, price: 100)).isInStatus(
                FilterCustomerCommand.MemberStatus.PENDING)
        assert !new Membership(order: new Order(status: Order.Status.COMPLETED, price: 100)).isInStatus(
                FilterCustomerCommand.MemberStatus.PAID)
        assert new Membership(order: new Order(status: Order.Status.COMPLETED, price: 100)).isInStatus(
                FilterCustomerCommand.MemberStatus.UNPAID)
        assert !new Membership(order: new Order(status: Order.Status.COMPLETED, price: 100)).isInStatus(
                FilterCustomerCommand.MemberStatus.CANCEL)

        assert !new Membership(cancel: true, order: new Order(status: Order.Status.COMPLETED)).isInStatus(
                FilterCustomerCommand.MemberStatus.PENDING)
        assert !new Membership(cancel: true, order: new Order(status: Order.Status.COMPLETED)).isInStatus(
                FilterCustomerCommand.MemberStatus.PAID)
        assert !new Membership(cancel: true, order: new Order(status: Order.Status.COMPLETED)).isInStatus(
                FilterCustomerCommand.MemberStatus.UNPAID)
        assert new Membership(cancel: true, order: new Order(status: Order.Status.COMPLETED)).isInStatus(
                FilterCustomerCommand.MemberStatus.CANCEL)

        assert !new Membership(cancel: true, order: new Order(status: Order.Status.NEW)).isInStatus(
                FilterCustomerCommand.MemberStatus.PENDING)
        assert !new Membership(cancel: true, order: new Order(status: Order.Status.NEW)).isInStatus(
                FilterCustomerCommand.MemberStatus.PAID)
        assert new Membership(cancel: true, order: new Order(status: Order.Status.NEW)).isInStatus(
                FilterCustomerCommand.MemberStatus.UNPAID)
        assert !new Membership(cancel: true, order: new Order(status: Order.Status.NEW)).isInStatus(
                FilterCustomerCommand.MemberStatus.CANCEL)

        assert new Membership(activated: false, cancel: true, order: new Order(status: Order.Status.COMPLETED)).isInStatus(
                FilterCustomerCommand.MemberStatus.PENDING)
        assert !new Membership(activated: false, cancel: true, order: new Order(status: Order.Status.COMPLETED)).isInStatus(
                FilterCustomerCommand.MemberStatus.PAID)
        assert !new Membership(activated: false, cancel: true, order: new Order(status: Order.Status.COMPLETED)).isInStatus(
                FilterCustomerCommand.MemberStatus.UNPAID)
        assert !new Membership(activated: false, cancel: true, order: new Order(status: Order.Status.COMPLETED)).isInStatus(
                FilterCustomerCommand.MemberStatus.CANCEL)
    }

    void testIsInFamilyStatus() {
        def customer1 = new Customer()
        customer1.id = 100L
        def customer2 = new Customer()
        customer2.id = 200L

        assert !new Membership().isInFamilyStatus(
                FilterCustomerCommand.ShowMembers.FAMILY_MEMBERS)
        assert !new Membership().isInFamilyStatus(
                FilterCustomerCommand.ShowMembers.FAMILY_MEMBER_CONTACTS)
        assert !new Membership().isInFamilyStatus(
                FilterCustomerCommand.ShowMembers.FAMILY_MEMBER_MEMBERS)
        assert new Membership().isInFamilyStatus(
                FilterCustomerCommand.ShowMembers.NO_FAMILY_MEMBERS)
        assert new Membership().isInFamilyStatus(
                FilterCustomerCommand.ShowMembers.MEMBERS_ONLY)

        assert new Membership(family: new MembershipFamily(contact: customer1),
                customer: customer2).isInFamilyStatus(FilterCustomerCommand.ShowMembers.FAMILY_MEMBERS)
        assert !new Membership(family: new MembershipFamily(contact: customer1),
                customer: customer2).isInFamilyStatus(FilterCustomerCommand.ShowMembers.FAMILY_MEMBER_CONTACTS)
        assert new Membership(family: new MembershipFamily(contact: customer1),
                customer: customer2).isInFamilyStatus(FilterCustomerCommand.ShowMembers.FAMILY_MEMBER_MEMBERS)
        assert !new Membership(family: new MembershipFamily(contact: customer1),
                customer: customer2).isInFamilyStatus(FilterCustomerCommand.ShowMembers.NO_FAMILY_MEMBERS)
        assert new Membership(family: new MembershipFamily(contact: customer1),
                customer: customer2).isInFamilyStatus(FilterCustomerCommand.ShowMembers.MEMBERS_ONLY)

        assert new Membership(family: new MembershipFamily(contact: customer1),
                customer: customer1).isInFamilyStatus(FilterCustomerCommand.ShowMembers.FAMILY_MEMBERS)
        assert new Membership(family: new MembershipFamily(contact: customer1),
                customer: customer1).isInFamilyStatus(FilterCustomerCommand.ShowMembers.FAMILY_MEMBER_CONTACTS)
        assert !new Membership(family: new MembershipFamily(contact: customer1),
                customer: customer1).isInFamilyStatus(FilterCustomerCommand.ShowMembers.FAMILY_MEMBER_MEMBERS)
        assert !new Membership(family: new MembershipFamily(contact: customer1),
                customer: customer1).isInFamilyStatus(FilterCustomerCommand.ShowMembers.NO_FAMILY_MEMBERS)
        assert new Membership(family: new MembershipFamily(contact: customer1),
                customer: customer1).isInFamilyStatus(FilterCustomerCommand.ShowMembers.MEMBERS_ONLY)
    }

    void testIsInGracePeriod() {
        def today = new LocalDate()
        assert !new Membership(startDate: today, endDate: today, gracePeriodEndDate: today).inGracePeriod
        assert !new Membership(startDate: today.minusDays(1), endDate: today.minusDays(1),
                gracePeriodEndDate: today.minusDays(1)).inGracePeriod
        assert !new Membership(startDate: today.plusDays(1), endDate: today.plusDays(1),
                gracePeriodEndDate: today.plusDays(1)).inGracePeriod
        assert !new Membership(startDate: today.minusDays(1), endDate: today,
                gracePeriodEndDate: today.plusDays(1)).inGracePeriod
        assert new Membership(startDate: today.minusDays(1), endDate: today.minusDays(1),
                gracePeriodEndDate: today).inGracePeriod
        assert new Membership(startDate: today.minusDays(20), endDate: today.minusDays(10),
                gracePeriodEndDate: today.plusDays(10)).inGracePeriod
        assert !new Membership(startDate: today.minusDays(10), endDate: today.plusDays(10),
                gracePeriodEndDate: today.plusDays(20)).inGracePeriod
    }

    void testHasGracePeriod() {
        def today = new LocalDate()
        assert !new Membership(startDate: today, endDate: today,
                gracePeriodEndDate: today).hasGracePeriod()
        assert new Membership(startDate: today, endDate: today,
                gracePeriodEndDate: today.plusDays(1)).hasGracePeriod()
        assert !new Membership(startDate: today, endDate: today.plusDays(1),
                gracePeriodEndDate: today.plusDays(1)).hasGracePeriod()
        assert new Membership(startDate: today, endDate: today.plusDays(1),
                gracePeriodEndDate: today.plusDays(10)).hasGracePeriod()
    }

    void testIsInStartingGracePeriod() {
        def today = new LocalDate()
        assert !new Membership(startDate: today, endDate: today).inStartingGracePeriod
        assert new Membership(startDate: today, endDate: today,
                startingGracePeriodDays: 1).inStartingGracePeriod
        assert !new Membership(startDate: today, endDate: today,
                startingGracePeriodDays: 1, activated: false).inStartingGracePeriod
        assert !new Membership(startDate: today.minusDays(1), endDate: today.minusDays(1),
                startingGracePeriodDays: 1).inStartingGracePeriod
        assert !new Membership(startDate: today.plusDays(1), endDate: today.plusDays(1),
                startingGracePeriodDays: 1).inStartingGracePeriod
        assert !new Membership(startDate: today.minusDays(1), endDate: today.plusDays(1),
                startingGracePeriodDays: 1).inStartingGracePeriod
    }

    @Test
    void testIsRemotePayableOrderNotRemotePayable() {
        def mockOrder = mockFor(Order)
        mockOrder.demand.isRemotePayable(1) { ->
            return false
        }

        LocalDate now = new LocalDate()
        Membership membership = new Membership(endDate: now.plusDays(1), order: mockOrder.createMock())

        assert !membership.isRemotePayable()

        mockOrder.verify()
    }

    @Test
    void testIsRemotePayableOrderIsRemotePayable() {
        def mockOrder = mockFor(Order)
        mockOrder.demand.isRemotePayable(1) { ->
            return true
        }

        LocalDate now = new LocalDate()
        Membership membership = new Membership(endDate: now.plusDays(1), order: mockOrder.createMock())

        assert membership.isRemotePayable()

        mockOrder.verify()
    }

    @Test
    void testIsRemotePayableOrderNotActivatedIsNotRemotePayable() {
        def mockOrder = mockFor(Order)
        mockOrder.demand.isRemotePayable(1) { ->
            return true
        }

        LocalDate now = new LocalDate()
        Membership membership = new Membership(endDate: now.plusDays(1), order: mockOrder.createMock())
        membership.activated = false

        assert !membership.isRemotePayable()

        mockOrder.verify()
    }

    @Test
    void testIsRemotePayableOrderIsRemotePayableButEndDatePassed() {
        def mockOrder = mockFor(Order)
        mockOrder.demand.isRemotePayable(0) { ->
            return true
        }

        LocalDate now = new LocalDate()
        Membership membership = new Membership(endDate: now.minusDays(1), order: mockOrder.createMock())

        assert !membership.isRemotePayable()

        mockOrder.verify()
    }

    @Test
    void testIsRemotePayableOrderIsRemotePayableEndDateToday() {
        def mockOrder = mockFor(Order)
        mockOrder.demand.isRemotePayable(1) { ->
            return true
        }

        LocalDate now = new LocalDate()
        Membership membership = new Membership(endDate: now, order: mockOrder.createMock())

        assert membership.isRemotePayable()

        mockOrder.verify()
    }

    void testGetPrice() {
        def facility = new Facility()
        def contactCustomer = new Customer(facility: facility)
        contactCustomer.id = 123L
        def nonContactCustomer = new Customer(facility: facility)
        nonContactCustomer.id = 321L
        def family = new MembershipFamily(contact: contactCustomer)
        def mt = new MembershipType(price: 100L)

        assert new Membership().price == 0
        assert new Membership(type: mt).price == 100L

        def m = new Membership(family: family, customer: contactCustomer)
        family.members = [m]
        assert m.price == 0

        m.type = mt
        assert m.price == 100L

        def m2 = new Membership(family: family, customer: nonContactCustomer, type: mt)
        family.members = [m, m2]
        assert m.price == 200L
        assert m2.price == 0

        m2.cancel = true
        assert m.price == 200L
        assert m.getPrice(true) == 100L

        facility.facilityProperties = [new FacilityProperty(value: "150",
                key: FacilityProperty.FacilityPropertyKey.FACILITY_MEMBERSHIP_FAMILY_MAX_AMOUNT.name())]
        assert m.price == 150L
    }

    void testSetSharedOrder() {

        def membership = createMembership(createCustomer())
        def oldOrder = membership.order
        def newOrder = new Order(article: Order.Article.MEMBERSHIP, issuer: createUser(),
                description: "desc", dateDelivery: new Date()).save(failOnError: true)

        membership.setSharedOrder(newOrder)

        assert oldOrder.status == Order.Status.ANNULLED
        assert membership.order.id == newOrder.id

        membership.setSharedOrder(newOrder)

        assert newOrder.status == Order.Status.NEW
        assert membership.order.id == newOrder.id
    }

    void testGetRecurringPrice() {
        def facility = createFacility()
        def contactCustomer = createCustomer(facility)
        def nonContactCustomer = createCustomer(facility)
        def family = new MembershipFamily(contact: contactCustomer).save(failOnError: true)
        def mt = createMembershipType(facility)
        mt.price = 100L
        mt.save(failOnError: true)

        assert new Membership().recurringPrice == 0
        assert new Membership(type: mt).recurringPrice == 100L

        def m = createMembership(contactCustomer)
        m.family = family
        m.save(failOnError: true)
        family.members = [m]
        family.save(failOnError: true)
        assert m.recurringPrice == 0

        m.type = mt
        m.save(failOnError: true)
        assert m.recurringPrice == 100L

        def m2 = createMembership(nonContactCustomer)
        m2.type = mt
        m2.family = family
        m2.save(failOnError: true)
        family.members = [m, m2]
        family.save(failOnError: true)
        assert m.recurringPrice == 100L
        assert m2.recurringPrice == 100L

        m2.order = m.order
        m2.save(failOnError: true)
        assert m.recurringPrice == 200L
        assert m2.recurringPrice == 100L

        facility.facilityProperties = [new FacilityProperty(value: "150",
                key: FacilityProperty.FacilityPropertyKey.FACILITY_MEMBERSHIP_FAMILY_MAX_AMOUNT.name())]
        facility.save(failOnError: true)
        assert m.recurringPrice == 150L

        m2.cancel = true
        m2.save(failOnError: true)
        assert m.recurringPrice == 100L

        m.activated = false
        m.save(failOnError: true)
        assert m.recurringPrice == 0
    }

    void testGetPreviousMembership() {
        def today = LocalDate.now()
        def c1 = createCustomer()
        def c2 = createCustomer()
        def m1 = createMembership(c1, today.plusDays(10), today.plusDays(20), today.plusDays(20))
        createMembership(c2, today, today.plusDays(1), today.plusDays(1))

        assert !m1.getPreviousMembership()

        def m2 = createMembership(c1, today, today.plusDays(1), today.plusDays(1))

        assert m2.id == m1.getPreviousMembership().id

        createMembership(c1, today.minusDays(1), today.minusDays(1), today.minusDays(1))

        assert m2.id == m1.getPreviousMembership().id
    }
}