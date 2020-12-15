package com.matchi

import com.matchi.enums.BookingGroupType
import com.matchi.orders.Order
import grails.plugin.springsecurity.SpringSecurityService
import grails.test.MockUtils
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.joda.time.DateTime
import org.joda.time.LocalTime
import org.junit.After
import org.junit.Before
import org.junit.Test

import static com.matchi.TestUtils.*

@TestFor(SubscriptionService)
@Mock([Facility, BookingGroup, Slot, Subscription, Customer, User, Booking, Court, Order])
class SubscriptionServiceTests {

    def mockBookingAvailabilityService
    Facility facility
    Customer customer
    Court court1
    BookingGroup group
    Slot slot
    int startTime = 11
    int endTime = 12

    @Before
    public void setUp() {

        MockUtils.mockLogging(SubscriptionService)

        mockBookingAvailabilityService = mockFor(BookingAvailabilityService)
        service.bookingAvailabilityService = mockBookingAvailabilityService.createMock()

        facility = new Facility(id: 1l, name: "Test", shortname: "test").save(validate: false)
        customer = new Customer(id: 1l, number: 1l, email: 'user@mail.com', firstname: "Test", lastname: "User", facility: facility).save(validate: false)
        court1 = new Court(name: "Bana1", facility: facility, listPosition: 1).save(validate: false)

        slot = new Slot(court: court1, startTime: new DateTime().withHourOfDay(startTime).toDate(), endTime: new DateTime().withHourOfDay((endTime)).toDate()).save(validate: false)
        group = new BookingGroup(type: BookingGroupType.SUBSCRIPTION)

        group.save(flush: true)
    }

    @After
    public void tearDown() {

    }

    @Test
    public void testCreateSubscriptionReturnsSubscription() {
        def priceListServiceControl = mockPriceListService()
        def springSecurityServiceControl = mockSpringSecurity()
        mockBookingAvailabilityService.demand.createSubscriptionBookingGroup(1..2) { Subscription subscription, DateTime fromDate, DateTime toDate,
                                                                         Slot slot, List<String> weekDays, int interval, Customer c, def showComment, def comment ->
            subscription.addToSlots(slot)
            return group
        }

        def subscription = service.createSubscription("", false, new DateTime().minusDays(10), new DateTime().plusDays(10), slot, 1, 1, customer)

        assert subscription != null
        assert subscription.customer.email == customer.email
        assert subscription.customer.facility.name == facility.name
        assert subscription.order
        assert Order.Article.SUBSCRIPTION == subscription.order.article
        assert 1 == Order.count()
        priceListServiceControl.verify()
        springSecurityServiceControl.verify()
    }

    @Test
    public void testCreateSubscriptionThrowsExceptionWithNoCourt() {
        slot.court.id = null

        shouldFail(IllegalArgumentException) {
            service.createSubscription("", false, new DateTime().minusDays(10), new DateTime().plusDays(10), slot, 1, 1, customer)
        }
    }

    @Test
    public void testCreatedSubscriptionGotMetaData() {
        def priceListServiceControl = mockPriceListService()
        def springSecurityServiceControl = mockSpringSecurity()
        mockBookingAvailabilityService.demand.createSubscriptionBookingGroup(1..2) { Subscription subscription, DateTime fromDate, DateTime toDate,
                                                                    Slot slot, List<String> weekDays, int interval, Customer c, def showComment, def comment ->
            subscription.addToSlots(slot)
            return group
        }

        def interval = 1
        def showComment = false

        def subscription = service.createSubscription("", showComment, new DateTime().minusDays(10), new DateTime().plusDays(10), slot, 1, 1, customer)

        assert subscription != null
        assert subscription.weekday == 1
        assert subscription.time == new LocalTime(slot.startTime)
        assert subscription.showComment == showComment
        assert subscription.timeInterval == interval
        priceListServiceControl.verify()
        springSecurityServiceControl.verify()
    }


    @Test
    public void testNoSubscriptionIsCreatedIfNoSlotsAvailable() {
        mockBookingAvailabilityService.demand.createSubscriptionBookingGroup(1..2) { Subscription subscription, DateTime fromDate, DateTime toDate,
                                                                    Slot slot, List<String> weekDays, int interval, Customer c, def showComment, def comment ->
            group.bookings = null
            return group
        }

        def subscription = service.createSubscription("", false, new DateTime().minusDays(10), new DateTime().plusDays(10), slot, 1, 1, customer)

        assert !subscription
    }

    @Test
    public void testUpdateSubscriptionSetsNewTime() {
        def priceListServiceControl = mockPriceListService()
        def springSecurityServiceControl = mockSpringSecurity()
        def userServiceControl = mockUserService()
        def mockOrderStatusService = mockFor(OrderStatusService)
        service.orderStatusService = mockOrderStatusService.createMock()
        mockOrderStatusService.demand.complete(1..1) { }

        mockBookingAvailabilityService.demand.createSubscriptionBookingGroup(1..2) { Subscription subscription, DateTime fromDate, DateTime toDate,
                                                                    Slot slot, List<String> weekDays, int interval, Customer c, def showComment, def comment ->
            subscription.addToSlots(slot)
            return group
        }

        mockBookingAvailabilityService.demand.updateSubscriptionBookingGroup(1..1) { Subscription subscription, DateTime fromDate, DateTime toDate, Slot slot,
                                               int interval, Customer customer, def showComment, def comment ->
            return group
        }

        slot.startTime = new DateTime(slot.startTime).plusHours(1).toDate()
        slot.endTime = new DateTime(slot.endTime).plusHours(1).toDate()

        def subscription = service.createSubscription("", false, new DateTime().minusDays(10), new DateTime().plusDays(10), slot, 1, 1, customer)
        subscription = service.updateSubscription(subscription, "", new DateTime().minusDays(10), new DateTime().plusDays(10), slot, 1, customer, false)

        assert subscription
        assert subscription.time == new LocalTime(slot.startTime)
        userServiceControl.verify()
        priceListServiceControl.verify()
        springSecurityServiceControl.verify()
    }

    private mockUserService() {
        def serviceControl = mockFor(UserService)
        serviceControl.demand.getLoggedInUser { -> new User() }
        service.userService = serviceControl.createMock()
        serviceControl
    }

    private mockPriceListService() {
        def serviceControl = mockFor(PriceListService)
        serviceControl.demand.getActiveSubscriptionPriceList { f, s -> null }
        service.priceListService = serviceControl.createMock()
        serviceControl
    }

    private mockSpringSecurity() {
        def serviceControl = mockFor(SpringSecurityService)
        serviceControl.demand.getCurrentUser { -> createUser() }
        service.springSecurityService = serviceControl.createMock()
        serviceControl
    }
}

