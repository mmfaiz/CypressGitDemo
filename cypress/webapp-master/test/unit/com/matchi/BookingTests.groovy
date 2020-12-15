package com.matchi

import com.matchi.enums.BookingGroupType
import com.matchi.mpc.CodeRequest
import com.matchi.orders.Order
import com.matchi.payment.ArticleType
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.junit.Before
import org.junit.Test

@TestFor(Booking)
@Mock([Booking, BookingGroup, FacilityAccessCode, CodeRequest, Subscription, Order, Court, Slot, User, Player, Region, Municipality, Facility, Customer, Sport])
class BookingTests {

    @Before
    void setUp() {

    }

    @Test
    void testOwnerReturnsFalseIfNotOwner() {
        Booking booking = createBooking()
        booking.customer = new Customer()
        booking.customer.id = 1l

        def other = new Customer()
        other.id  = 2l

        assert !booking.isOwner(other)
    }

    @Test
    void testOwner() {
        Booking booking = createBooking()
        booking.customer = new Customer()
        booking.customer.id = 1l

        def other = booking.customer

        assert booking.isOwner(other)
    }

    @Test
    void testAddPlayers() {
        Booking booking = createBooking()
        def customers = [new Customer(number: 1), new Customer()]

        booking.addPlayers(customers)

        assert booking.players
        assert 2 == booking.players.size()
    }

    @Test
    void testUpdatePlayers() {
        Booking booking = createBooking()
        def customer1 = new Customer(number: 1)
        def customer2 = new Customer(number: 2)
        def customer3 = new Customer()
        def customers = [customer1, customer3]

        assert booking.addPlayers(customers).players

        customers.add(customer2)
        assert 3 == booking.updatePlayers(customers).players.size()
        assert booking.players.find { it.customer == customer1 }
        assert booking.players.find { it.customer == customer2 }
        assert booking.players.find { !it.customer }

        customers.remove(customer1)
        assert 2 == booking.updatePlayers(customers).players.size()
        assert !booking.players.find { it.customer == customer1 }
        assert booking.players.find { it.customer == customer2 }
        assert booking.players.find { !it.customer }

        assert !booking.updatePlayers([]).players
    }

    @Test
    void testIsSubscriptionTrue() {
        Booking booking = TestUtils.createBooking()
        BookingGroup bookingGroup = TestUtils.createBokingGroup(BookingGroupType.SUBSCRIPTION)
        bookingGroup.addToBookings(booking)
        bookingGroup.save(failOnError: true, flush: true)

        assert booking.isSubscription()
    }

    @Test
    void testIsSubscriptionFalse() {
        Booking booking = TestUtils.createBooking()
        BookingGroup bookingGroup = TestUtils.createBokingGroup(BookingGroupType.DEFAULT)
        bookingGroup.addToBookings(booking)
        bookingGroup.save(failOnError: true, flush: true)

        assert !booking.isSubscription()
    }

    @Test
    void testGetAccessCodePersonal() {
        String code = "1234ABCD"
        Booking booking = TestUtils.createBooking()
        Customer customer = booking.customer
        customer.accessCode = code
        customer.save(failOnError: true, flush: true)

        Facility facility = booking.slot.court.facility
        facility.facilityProperties = [
                new FacilityProperty(key: FacilityProperty.FacilityPropertyKey.FEATURE_PERSONAL_ACCESS_CODE.toString(), value: "1")]
        facility.save(failOnError: true, flush: true)

        assert booking.getAccessCode() == code
    }

    @Test
    void testGetAccessCodeSubscription() {
        String code = "1234ABCD"
        Booking booking = TestUtils.createBooking()
        Customer customer = booking.customer
        Court court = booking.slot.court

        Facility facility = booking.slot.court.facility
        facility.facilityProperties = [
                new FacilityProperty(key: FacilityProperty.FacilityPropertyKey.FEATURE_SUBSCRIPTION_ACCESS_CODE.toString(), value: "1")]
        facility.save(flush: true, failOnError: true)

        BookingGroup bookingGroup = TestUtils.createBokingGroup(BookingGroupType.SUBSCRIPTION)
        Subscription subscription = TestUtils.createSubscription(customer, bookingGroup, court)
        subscription.accessCode = code
        subscription.save(failOnError: true, flush: true)

        bookingGroup.addToBookings(booking)
        bookingGroup.save(failOnError: true, flush: true)

        assert booking.getAccessCode() == code
    }

    @Test
    void testGetAccessCodeSubscriptionDespiteCustomerHaving() {
        String code = "1234ABCD"
        Booking booking = TestUtils.createBooking()
        Customer customer = booking.customer
        Court court = booking.slot.court

        customer.accessCode = "2342342"
        customer.save(failOnError: true, flush: true)

        Facility facility = booking.slot.court.facility
        facility.facilityProperties = [
                new FacilityProperty(key: FacilityProperty.FacilityPropertyKey.FEATURE_SUBSCRIPTION_ACCESS_CODE.toString(), value: "1")]
        facility.save(flush: true, failOnError: true)

        BookingGroup bookingGroup = TestUtils.createBokingGroup(BookingGroupType.SUBSCRIPTION)
        Subscription subscription = TestUtils.createSubscription(customer, bookingGroup, court)
        subscription.accessCode = code
        subscription.save(failOnError: true, flush: true)

        bookingGroup.addToBookings(booking)
        bookingGroup.save(failOnError: true, flush: true)

        assert booking.getAccessCode() == code
    }

    @Test
    void testGetAccessCodeSubscriptionDespitePersonalActivated() {
        String code = "1234ABCD"
        Booking booking = TestUtils.createBooking()
        Customer customer = booking.customer
        Court court = booking.slot.court

        Facility facility = booking.slot.court.facility
        facility.facilityProperties = [
                new FacilityProperty(key: FacilityProperty.FacilityPropertyKey.FEATURE_PERSONAL_ACCESS_CODE.toString(), value: "1"),
                new FacilityProperty(key: FacilityProperty.FacilityPropertyKey.FEATURE_SUBSCRIPTION_ACCESS_CODE.toString(), value: "1")
        ]
        facility.save(flush: true, failOnError: true)

        BookingGroup bookingGroup = TestUtils.createBokingGroup(BookingGroupType.SUBSCRIPTION)
        Subscription subscription = TestUtils.createSubscription(customer, bookingGroup, court)
        subscription.accessCode = code
        subscription.save(failOnError: true, flush: true)

        bookingGroup.addToBookings(booking)
        bookingGroup.save(failOnError: true, flush: true)

        assert booking.getAccessCode() == code
    }

    @Test
    void testGetAccessCodeMPC() {
        String code = "1234ABCD"
        Booking booking = TestUtils.createBooking()

        Facility facility = booking.slot.court.facility
        facility.facilityProperties = [
                    new FacilityProperty(key: FacilityProperty.FacilityPropertyKey.FEATURE_MPC.toString(), value: "1")]
        facility.save(flush: true, failOnError: true)

        def mockCodeRequest = mockFor(CodeRequest)
        mockCodeRequest.demand.static.findByBooking(1..1) { Booking b ->
            return [code: code]
        }

        assert booking.getAccessCode() == code
        mockCodeRequest.verify()
    }

    @Test
    void testGetAccessCodeFacilityAccessCodeDespiteMpcActivated() {
        String code = "1234ABCD"
        Booking booking = TestUtils.createBooking()

        Facility facility = booking.slot.court.facility
        facility.facilityProperties = [
                new FacilityProperty(key: FacilityProperty.FacilityPropertyKey.FEATURE_MPC.toString(), value: "1")]
        facility.save(flush: true, failOnError: true)

        def mockCodeRequest = mockFor(CodeRequest)
        mockCodeRequest.demand.static.findByBooking(1..1) { Booking b ->
            return [code: null]
        }

        def mockFacilityAccessCode = mockFor(FacilityAccessCode)
        mockFacilityAccessCode.demand.static.validAccessCodeFor(1..1) { Slot s ->
            return [content: code]
        }

        assert booking.getAccessCode() == code
        mockCodeRequest.verify()
    }

    @Test
    void testGetAccessCodeFacilityAccessCodeSinceCodeRequestPending() {
        String code = "1234ABCD"
        Booking booking = TestUtils.createBooking()

        Facility facility = booking.slot.court.facility
        facility.facilityProperties = [
                new FacilityProperty(key: FacilityProperty.FacilityPropertyKey.FEATURE_MPC.toString(), value: "1")]
        facility.save(flush: true, failOnError: true)

        def mockCodeRequest = mockFor(CodeRequest)
        mockCodeRequest.demand.static.findByBooking(1..1) { Booking b ->
            return [code: "Hellooo", status: CodeRequest.Status.PENDING]
        }

        def mockFacilityAccessCode = mockFor(FacilityAccessCode)
        mockFacilityAccessCode.demand.static.validAccessCodeFor(1..1) { Slot s ->
            return [content: code]
        }

        assert booking.getAccessCode() == code
        mockCodeRequest.verify()
    }

    @Test
    void testGetAccessCodeFacilityAccessCode() {
        String code = "1234ABCD"
        Booking booking = TestUtils.createBooking()

        def mockFacilityAccessCode = mockFor(FacilityAccessCode)
        mockFacilityAccessCode.demand.static.validAccessCodeFor(1..1) { Slot s ->
            return [content: code]
        }

        assert booking.getAccessCode() == code
    }

    @Test
    void testIReservationMethods() {
        Booking booking = TestUtils.createBooking()
        assert booking.getArticleType() == ArticleType.BOOKING
        assert booking.getDate() == booking.slot.startTime

        booking.slot = null

        assert !booking.getDate()

        booking.delete(flush: true)
    }

    @Test
    public void testShowRemotePaymentNotificationInEmailFalse() {
        Booking booking = TestUtils.createBooking()
        BookingGroup bookingGroup = TestUtils.createBokingGroup(BookingGroupType.DEFAULT)
        bookingGroup.addToBookings(booking)
        bookingGroup.save(failOnError: true, flush: true)

        assert !booking.showRemotePaymentNotificationInEmail()
    }

    @Test
    public void testShowRemotePaymentNotificationInEmailTrue() {
        Facility facility = TestUtils.createFacility();
        facility.facilityProperties = [
                new FacilityProperty(key: FacilityProperty.FacilityPropertyKey.FEATURE_REMOTE_PAYMENT_BOOKING.name(), value: "1")
        ]
        facility.save()
        Booking booking = TestUtils.createBooking(null, TestUtils.createSlot(TestUtils.createCourt(facility)))
        BookingGroup bookingGroup = TestUtils.createBokingGroup(BookingGroupType.DEFAULT)
        bookingGroup.addToBookings(booking)
        booking.order = TestUtils.createOrder(TestUtils.createUser("test@matchi.se"), facility, Order.Article.BOOKING)
        bookingGroup.save(failOnError: true, flush: true)

        assert booking.showRemotePaymentNotificationInEmail()
    }

    private static Booking createBooking() {
        def booking = new Booking()
        def slot = new Slot()
        slot.court = new Court()
        slot.endTime = new Date()
        slot.startTime = new Date()
        booking.slot = slot
        return booking
    }
}
