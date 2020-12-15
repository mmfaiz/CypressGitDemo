package com.matchi

import com.matchi.activities.ActivityOccasion
import com.matchi.enums.RedeemAt
import com.matchi.enums.RedeemType
import com.matchi.mpc.MpcService
import com.matchi.orders.Order
import com.matchi.payment.PaymentMethod
import com.matchi.watch.ObjectWatchNotificationService
import com.matchi.subscriptionredeem.SlotRedeem
import com.matchi.subscriptionredeem.SubscriptionRedeem
import grails.plugin.springsecurity.SpringSecurityService
import grails.test.MockUtils
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import org.joda.time.DateTime
import org.junit.Before
import org.junit.Test

@TestFor(BookingService)
@TestMixin(GrailsUnitTestMixin)
@Mock([Court, Facility, UserRole, Role, User, Customer, Sport, Slot, Booking, SubscriptionRedeem,
        SlotRedeem, ActivityOccasion, FacilityProperty, Order])
class BookingServiceTests {

    def mockNotificationServiceControl
    def mockSlotService
    def mockPaymentService
    def mockSpringSecurityService
    def mockUserService
    def mockCustomerService
    def mockCouponService
    def mockRedeemService
    def mockCourtRelationsBookingService
    def mockObjectWatchNotificationService
    def mockMpcService
    def mockOrderStatusService

    Facility existingFacility
    Sport existingSport
    Slot existingSlot
    def existingBookings = []
    def existingCourts = []

    User user
    Customer customer

    @Before
    public void setUp() {
        MockUtils.mockLogging(BookingService, true)

        mockPaymentService = mockFor(PaymentService)
        mockCouponService = mockFor(CouponService)
        mockNotificationServiceControl = mockFor(NotificationService)
        mockSlotService = mockFor(SlotService)
        mockSpringSecurityService = mockFor(SpringSecurityService)
        mockUserService = mockFor(UserService)
        mockCustomerService = mockFor(CustomerService)
        mockRedeemService = mockFor(RedeemService)
        mockCourtRelationsBookingService = mockFor(CourtRelationsBookingService)
        mockObjectWatchNotificationService = mockFor(ObjectWatchNotificationService)
        mockMpcService = mockFor(MpcService)
        mockOrderStatusService = mockFor(OrderStatusService)

        service.paymentService = mockPaymentService.createMock()
        service.springSecurityService = mockSpringSecurityService.createMock()
        service.userService = mockUserService.createMock()
        service.notificationService = mockNotificationServiceControl.createMock()
        service.slotService = mockSlotService.createMock()
        service.customerService = mockCustomerService.createMock()
        service.redeemService = mockRedeemService.createMock()
        service.courtRelationsBookingService = mockCourtRelationsBookingService.createMock()
        service.couponService = mockCouponService.createMock()
        service.objectWatchNotificationService = mockObjectWatchNotificationService.createMock()
        service.mpcService = mockMpcService.createMock()
        service.orderStatusService = mockOrderStatusService.createMock()

        setupSports()
        setupFacility()
        setupUser()
        setupCustomer()
        setupCourts()
        setupSlots()
        setupBookings()

        mockPaymentService.demand.refundUserPayment(0..1) { def booking -> }
        mockSpringSecurityService.demand.getCurrentUser(1..1) { -> return user }
        mockUserService.demand.getUserFacility(1..1) { -> return existingFacility }
        mockCouponService.demand.refundCustomerCoupon(1..1) { booking, forceAnnul -> }

        mockCustomerService.demand.createCustomer(0..1) { def p -> customer }
    }

    @Test
    void testNotificationServiceIsCalledOnBooking() {
        existingSlot.booking = null
        mockCourtRelationsBookingService.demand.tryBookRelatedCourts(1..1) { def booking -> }
        mockNotificationServiceControl.demand.sendNewBookingNotification(1..1) { def booking -> }
        mockSlotService.demand.getSlot(1..1) { def slotId ->
            return existingSlot
        }

        service.makeBooking(createBookingCommand(customer.id, 2))
        mockNotificationServiceControl.verify()
    }

    @Test
    void testNotificationServiceIsCalledOnBookingCancel() {
        mockCourtRelationsBookingService.demand.tryCancelRelatedCourts(1..1) { def booking -> }
        mockObjectWatchNotificationService.demand.trySendNotificationsFor(1..1) { def booking -> }
        mockMpcService.demand.tryDelete(1..1) { def booking -> }
        mockNotificationServiceControl.demand.sendBookingCanceledNotification(1..1) { def booking, def message -> }

        Booking booking = (Booking) existingBookings.get(0)

        mockSpringSecurityService.demand.getCurrentUser(1..1) { -> return user }

        mockPaymentService.demand.refundUserPayment(1..1) { def b, def annul ->
            return new Payment(method: PaymentMethod.FREE)
        }

        service.cancelBooking(booking, "")
        mockNotificationServiceControl.verify()
    }

    @Test
    void testSlotIsFreeAfterCancelledBooking() {
        mockCourtRelationsBookingService.demand.tryCancelRelatedCourts(1..1) { def booking -> }
        mockObjectWatchNotificationService.demand.trySendNotificationsFor(1..1) { def booking -> }
        mockMpcService.demand.tryDelete(1..1) { def booking -> }
        mockNotificationServiceControl.demand.sendBookingCanceledNotification(1..1) { def booking, def message -> }

        Booking booking = (Booking) existingBookings.get(0)
        Slot slot = booking.slot
        slot.booking = booking

        assert slot.booking

        mockSpringSecurityService.demand.getCurrentUser(1..1) { -> return user }

        mockPaymentService.demand.refundUserPayment(1..1) { def b, def annul ->
            return new Payment(method: PaymentMethod.FREE)
        }

        service.cancelBooking(booking, "Cancelled!")
        assert !slot.booking

        mockNotificationServiceControl.verify()
    }


    @Test
    void testSlotIsFreeButStillInSubscriptionAfterCancelledBooking() {
        mockCourtRelationsBookingService.demand.tryCancelRelatedCourts(1..1) { def booking -> }
        mockObjectWatchNotificationService.demand.trySendNotificationsFor(1..1) { def booking -> }
        mockMpcService.demand.tryDelete(1..1) { def booking -> }
        mockNotificationServiceControl.demand.sendBookingCanceledNotification(1..1) { def booking, def message -> }

        Subscription subscription = new Subscription(customer: customer)

        Booking booking = (Booking) existingBookings.get(0)
        Slot slot = booking.slot
        slot.booking = booking
        slot.subscription = subscription

        assert slot.subscription
        assert slot.booking

        mockSpringSecurityService.demand.getCurrentUser(1..1) { -> return user }

        mockPaymentService.demand.refundUserPayment(1..1) { def b, def annul ->
            return new Payment(method: PaymentMethod.FREE)
        }

        service.cancelBooking(booking, "Cancelled!")
        assert slot.subscription
        assert !slot.booking

        mockNotificationServiceControl.verify()
    }

    @Test
    void testFullRedeemOfSubscriptionBookingIsCalledCorrect() {
        mockCourtRelationsBookingService.demand.tryCancelRelatedCourts(1..1) { def booking -> }
        mockObjectWatchNotificationService.demand.trySendNotificationsFor(1..1) { def booking -> }
        mockMpcService.demand.tryDelete(1..1) { def booking -> }
        mockNotificationServiceControl.demand.sendBookingCanceledNotification(1..1) { def booking, def message -> }

        Subscription subscription = new Subscription(customer: customer)

        Booking booking = (Booking) existingBookings.get(0)
        Slot slot = booking.slot
        slot.booking = booking
        slot.subscription = subscription

        existingFacility.subscriptionRedeem = new SubscriptionRedeem(id: 1l)

        assert slot.subscription
        assert slot.booking

        mockSpringSecurityService.demand.getCurrentUser(1..1) { -> return user }
        mockPaymentService.demand.refundUserPayment(1..1) { def b, def annul ->
            return new Payment(method: PaymentMethod.FREE)
        }
        mockRedeemService.demand.redeem(1..1) { def b, def redeemType -> new SlotRedeem() }

        service.cancelBooking(booking, "Cancelled!", false, RedeemType.FULL, true)

        mockRedeemService.verify()
    }

    @Test
    void testEmptyRedeemOfSubscriptionBookingIsCalledCorrect() {
        mockCourtRelationsBookingService.demand.tryCancelRelatedCourts(1..1) { def booking -> }
        mockObjectWatchNotificationService.demand.trySendNotificationsFor(1..1) { def booking -> }
        mockMpcService.demand.tryDelete(1..1) { def booking -> }
        mockNotificationServiceControl.demand.sendBookingCanceledNotification(1..1) { def booking, def message -> }

        Subscription subscription = new Subscription(customer: customer)

        Booking booking = (Booking) existingBookings.get(0)
        Slot slot = booking.slot
        slot.booking = booking
        slot.subscription = subscription

        existingFacility.subscriptionRedeem = new SubscriptionRedeem(id: 1l)

        assert slot.subscription
        assert slot.booking
        assert slot.court.facility

        mockSpringSecurityService.demand.getCurrentUser(1..1) { -> return user }
        mockPaymentService.demand.refundUserPayment(1..1) { def b, def annul ->
            return new Payment(method: PaymentMethod.FREE)
        }
        mockRedeemService.demand.redeem(1..1) { def b, def redeemType -> new SlotRedeem() }

        service.cancelBooking(booking, "Cancelled!", false, RedeemType.EMPTY, true)

        mockRedeemService.verify()
    }

    @Test
    void testSettingBookingCancelRedeemOfSubscriptionBookingIsCalledCorrect() {
        mockCourtRelationsBookingService.demand.tryCancelRelatedCourts(1..1) { def booking -> }
        mockObjectWatchNotificationService.demand.trySendNotificationsFor(1..1) { def booking -> }
        mockMpcService.demand.tryDelete(1..1) { def booking -> }
        mockNotificationServiceControl.demand.sendBookingCanceledNotification(1..1) { def booking, def message -> }

        Subscription subscription = new Subscription(customer: customer)

        Booking booking = (Booking) existingBookings.get(0)
        Slot slot = booking.slot
        slot.booking = booking
        slot.subscription = subscription

        existingFacility.subscriptionRedeem = new SubscriptionRedeem(redeemAt: RedeemAt.BOOKINGCANCEL)

        assert slot.subscription
        assert slot.booking

        mockSpringSecurityService.demand.getCurrentUser(1..1) { -> return user }
        mockPaymentService.demand.refundUserPayment(1..1) { def b, def annul ->
            return new Payment(method: PaymentMethod.FREE)
        }
        mockRedeemService.demand.redeem(1..1) { -> new SlotRedeem() }

        service.cancelBooking(booking, "Cancelled!")

        mockRedeemService.verify()
    }

    @Test
    void testSettingSlotReBookedRedeemOfSubscriptionBookingIsCalledCorrect() {
        mockCourtRelationsBookingService.demand.tryCancelRelatedCourts(1..1) { def booking -> }
        mockObjectWatchNotificationService.demand.trySendNotificationsFor(1..1) { def booking -> }
        mockMpcService.demand.tryDelete(1..1) { def booking -> }
        mockNotificationServiceControl.demand.sendBookingCanceledNotification(1..1) { def booking, def message -> }

        Subscription subscription = new Subscription(customer: customer)

        Booking booking = (Booking) existingBookings.get(0)
        Slot slot = booking.slot
        slot.booking = booking
        slot.subscription = subscription

        existingFacility.subscriptionRedeem = new SubscriptionRedeem(redeemAt: RedeemAt.SLOTREBOOKED)

        assert slot.subscription
        assert slot.booking

        mockSpringSecurityService.demand.getCurrentUser(1..1) { -> return user }
        mockPaymentService.demand.refundUserPayment(1..1) { def b, def annul ->
            return new Payment(method: PaymentMethod.FREE)
        }
        mockRedeemService.demand.redeem(1..1) { -> new SlotRedeem() }

        service.cancelBooking(booking, "Cancelled!")

        !mockRedeemService.verify()
    }

    @Test
    void testBookingRefundCalculusWithNoRefundPercentage() {
        assert 87.5 == getAmountActualCredited(100, 100)
    }

    @Test
    void testBookingRefundCalculusWithRefundPercentage() {
        assert 50 == getAmountActualCredited(100, 50)
    }

    @Test
    void testDisableReminder() {
        def booking = existingBookings[0]
        service.disableReminder(booking)
        assert booking.dateReminded
    }

    @Test
    void testGetBooking() {
        def booking = existingBookings[0]
        assert booking == service.getBooking(booking.id)
    }

    @Test
    void testCancelNotRefundableBooking() {
        def mockOrder = mockFor(Order)

        Order order = mockOrder.createMock()
        order.article = Order.Article.BOOKING
        order.description = "Booking"
        order.facility = existingFacility
        order.issuer = user
        order.dateDelivery = new Date().minus(Order.REFUND_LIMIT_DAYS - 1)

        mockOrder.demand.asBoolean(1..1) { -> return true }

        mockOrder.demand.isStillRefundable(1..1) { ->
            return false
        }

        mockNotificationServiceControl.demand.sendBookingCanceledNotification(0..1) { def booking, def message -> }
        mockCourtRelationsBookingService.demand.tryCancelRelatedCourts(1..1) { def booking -> }
        mockObjectWatchNotificationService.demand.trySendNotificationsFor(1..1) { def booking -> }
        mockMpcService.demand.tryDelete(1..1) { def booking -> }
        mockOrderStatusService.demand.annul(1..1) { def eventInitiator, def whyMessage, def amount ->
            assert whyMessage == null
            assert amount == null
        }

        Booking booking = new Booking(customer: customer, slot: existingSlot, order: order)
        booking.save(validate: true, failOnError: true)

        mockSpringSecurityService.demand.getCurrentUser(1..2) { -> return user }

        service.cancelBooking(booking, "Cancelled!")
    }

    @Test
    void testCancelRefundableBooking() {
        def mockOrder = mockFor(Order)

        Order order = mockOrder.createMock()
        order.article = Order.Article.BOOKING
        order.description = "Booking"
        order.facility = existingFacility
        order.issuer = user
        order.dateDelivery = new Date().minus(Order.REFUND_LIMIT_DAYS + 1)

        mockSpringSecurityService.demand.getCurrentUser(1..1) { -> return user }

        mockOrder.demand.asBoolean(1..1) { -> return true }

        mockOrder.demand.isStillRefundable(1..1) { ->
            return true
        }

        mockOrder.demand.total(1..1) { -> return 100.00 }

        mockPaymentService.demand.getServiceFee(0..1) { def currency ->
            return new Amount(amount: 12.5, VAT: 2.5)
        }

        mockNotificationServiceControl.demand.sendBookingCanceledNotification(0..1) { def booking, def message -> }
        mockCourtRelationsBookingService.demand.tryCancelRelatedCourts(1..1) { def booking -> }
        mockObjectWatchNotificationService.demand.trySendNotificationsFor(1..1) { def booking -> }
        mockMpcService.demand.tryDelete(1..1) { def booking -> }
        mockOrderStatusService.demand.annul(1..1) { def whyMessage, def amount ->
            assert whyMessage != null
            assert amount != null
        }

        Booking booking = new Booking(customer: customer, slot: existingSlot, order: order)
        booking.save(validate: true, failOnError: true)

        service.cancelBooking(booking, "Cancelled!")
    }

    /* setup method for testing calculateAmountToCredit */

    private def getAmountActualCredited(def total, def refundPercentage) {
        def booking = new Booking()
        def mockSlot = mockFor(Slot)
        def mockOrder = mockFor(Order)

        mockOrder.demand.total(1..1) { ->
            return new BigDecimal(total)
        }

        mockSlot.demand.refundPercentage(1..1) { ->
            return refundPercentage
        }

        mockPaymentService = mockFor(PaymentService)
        mockPaymentService.demand.getServiceFee(1..1) { cur ->
            return new Amount(amount: 12.5, VAT: 2.5)
        }
        service.paymentService = mockPaymentService.createMock()

        booking.slot = mockSlot.createMock()
        booking.slot.court = new Court(facility: new Facility())
        booking.order = mockOrder.createMock()

        return service.calculateAmountToCredit(booking)
    }

    CreateBookingCommand createBookingCommand(def customerId, def slotId) {
        CreateBookingCommand cmd = new CreateBookingCommand()
        cmd.customerId = customerId
        cmd.slotId = slotId
        return cmd
    }

    def setupSports() {
        existingSport = new Sport()
        existingSport.name = "Tennis"
        existingSport.position = 0
        existingSport.save(validate: false)
    }

    def setupCourts() {
        def court = new Court()
        court.id = 1
        court.name = "Bana 1"
        court.facility = existingFacility
        court.sport = existingSport
        court.save(validate: false)

        existingCourts << court
        existingFacility.courts = existingCourts;
    }

    private Customer createCustomer() {
        Customer customer = new Customer()
        customer.id = 1l
        customer.number = 1
        customer.email = "test@test.com"
        customer.firstname = "Firstname"
        customer.lastname = "Lastname"
        customer.facility = existingFacility
        customer.user = user
        customer.save(validate: false)

        return customer
    }

    private User createUser() {
        User user = new User()
        user.id = 1l
        user.email = "test@test.com"
        user.firstname = "Firstname"
        user.lastname = "Lastname"
        user.telephone = "031-031031"
        user.save(validate: false)

        return user
    }

    def setupFacility() {
        existingFacility = new Facility()
        existingFacility.name = "Test TK"
        existingFacility.shortname = "testtk"
        existingFacility.courts = existingCourts
        existingFacility.save(validate: false)
    }

    def setupCustomer() {
        customer = createCustomer()
    }

    def setupUser() {
        user = createUser()
    }

    void setupSlots() {
        existingSlot = new Slot()
        existingSlot.id = 1
        existingSlot.startTime = new DateTime(2020, 10, 1, 17, 0, 0, 0).toDate()
        existingSlot.endTime = new DateTime(2020, 10, 1, 18, 0, 0, 0).toDate()
        existingSlot.court = Court.findById(1)
        existingSlot.save(validate: false)
    }

    void setupBookings() {
        Booking booking = new Booking()
        booking.customer = customer
        booking.slot = existingSlot
        booking.save(validate: false)

        existingBookings << booking
    }
}
