package com.matchi

import com.matchi.async.ScheduledTaskService
import com.matchi.orders.AdyenOrderPayment
import com.matchi.orders.Order
import com.matchi.payment.BookingPaymentController
import com.matchi.payment.ConfirmUpdateCommand
import com.matchi.payment.MultipleBookingPaymentCommand
import com.matchi.payment.PaymentFlow
import com.matchi.payment.PaymentMethod
import com.matchi.price.Price
import com.matchi.slots.AdjacentSlotGroup
import grails.plugin.springsecurity.SpringSecurityService
import grails.test.GrailsMock
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.web.GroovyPageUnitTestMixin
import org.joda.time.DateTime
import org.junit.Before
import org.junit.Test

import static com.matchi.TestUtils.*

@TestFor(BookingPaymentController)
@TestMixin(GroovyPageUnitTestMixin)
@Mock([Slot, Booking, AdyenOrderPayment, User, Order, Region, Municipality, Facility, Sport, Court, FacilityProperty, Customer])
class BookingPaymentControllerTests extends GenericPaymentControllerTests {
    GrailsMock userServiceControl
    GrailsMock priceListServiceControl
    GrailsMock bookingService
    GrailsMock springSecurityService
    GrailsMock slotService
    GrailsMock courtService
    GrailsMock paymentService
    GrailsMock notificationService
    GrailsMock scheduledTaskService
    GrailsMock couponService
    String slotId

    int i = 0

    @Before
    void setUp() {
        //controller = new PaymentController()
        Slot.deleteAll()

        userServiceControl = mockFor(UserService)
        controller.userService = userServiceControl.createMock()

        priceListServiceControl = mockFor(PriceListService)
        controller.priceListService = priceListServiceControl.createMock()

        bookingService = mockFor(BookingService)
        controller.bookingService = bookingService.createMock()

        springSecurityService = mockFor(SpringSecurityService)
        controller.springSecurityService = springSecurityService.createMock()

        slotService = mockFor(SlotService)
        controller.slotService = slotService.createMock()

        paymentService = mockFor(PaymentService)
        controller.paymentService = paymentService.createMock()

        notificationService = mockFor(NotificationService)
        controller.notificationService = notificationService.createMock()

        scheduledTaskService = mockFor(ScheduledTaskService)
        controller.scheduledTaskService = scheduledTaskService.createMock()

        couponService = mockFor(CouponService)
        controller.couponService = couponService.createMock()

        courtService = mockFor(CourtService)
        controller.courtService = courtService.createMock()

        Slot slot = new Slot(court: new Court(), startTime: new DateTime().toDate(), endTime: new DateTime().plusHours(1).toDate()).save()
        slotId = slot.id

        mockTagLib(MoneyTagLib)
    }

    @Test
    void testOrderNotFound() {
        def mockOrder = mockFor(Order)
        def mockStaticOrder = mockFor(Order)
        Order order = mockOrder.createMock()
        params.orderId = 1L

        mockStaticOrder.demand.static.get(1..1) { Long id ->
            return null
        }

        controller.startPaymentFlow(params.orderId, getFinishUrl())
        controller.process()

        PaymentFlow paymentFlow = PaymentFlow.getInstance(session, params.orderId)
        String expectedErrorMessageCode = "genericPaymentController.process.errors.orderNotFound"

        assert paymentFlow != null
        assert paymentFlow.state.equals(PaymentFlow.State.ERROR)
        assert paymentFlow.errorMessage.equals(expectedErrorMessageCode)

        mockOrder.verify()
        mockStaticOrder.verify()
    }

    @Test
    void testUnpaidOrder() {
        def mockOrder = mockFor(Order)
        def mockStaticOrder = mockFor(Order)
        Order order = mockOrder.createMock()
        params.orderId = 1L

        mockStaticOrder.demand.static.get(1..1) { Long id ->
            return order
        }

        mockOrder.demand.getId(1..1) { ->
            return params.orderId
        }

        mockOrder.demand.asBoolean(1..1) { ->
            return true
        }

        mockOrder.demand.isProcessable(1..1) { ->
            return false
        }

        mockOrder.demand.getId(1..1) { ->
            return params.orderId
        }

        controller.startPaymentFlow(params.orderId, getFinishUrl())
        controller.process()

        PaymentFlow paymentFlow = PaymentFlow.getInstance(session, params.orderId)
        String expectedErrorMessageCode = "genericPaymentController.process.errors.paymentNotCompleted"

        assert paymentFlow != null
        assert paymentFlow.state.equals(PaymentFlow.State.ERROR)
        assert paymentFlow.errorMessage.equals(expectedErrorMessageCode)

        mockOrder.verify()
        mockStaticOrder.verify()
    }

    @Test
    void testIncorrectArticleOrder() {
        def mockOrder = mockFor(Order)
        def mockStaticOrder = mockFor(Order)
        Order order = mockOrder.createMock()
        params.orderId = 1L

        mockStaticOrder.demand.static.get(1..1) { Long id ->
            return order
        }

        mockOrder.demand.getId(1..1) { ->
            return params.orderId
        }

        mockOrder.demand.asBoolean(1..1) { ->
            return true
        }

        mockOrder.demand.isProcessable(1..1) { ->
            return true
        }

        mockOrder.demand.getArticle(1..1) { ->
            return Order.Article.ACTIVITY
        }

        mockOrder.demand.getId(2..2) { ->
            return params.orderId
        }

        controller.startPaymentFlow(params.orderId, getFinishUrl())
        controller.process()

        PaymentFlow paymentFlow = PaymentFlow.getInstance(session, params.orderId)
        String expectedErrorMessageCode = "genericPaymentController.process.errors.incorrectArticleType"

        assert paymentFlow != null
        assert paymentFlow.state.equals(PaymentFlow.State.ERROR)
        assert paymentFlow.errorMessage.equals(expectedErrorMessageCode)

        mockOrder.verify()
        mockStaticOrder.verify()
    }

    @Test
    void testOrderAlreadyProcessed() {
        def mockOrder = mockFor(Order)
        def mockStaticOrder = mockFor(Order)

        def mockBooking = mockFor(Booking)
        def mockStaticBooking = mockFor(Booking)

        Order order = mockOrder.createMock()
        Booking booking = mockBooking.createMock()
        params.orderId = 1L

        mockStaticOrder.demand.static.get(1..1) { Long id ->
            return order
        }

        mockOrder.demand.getId(1..1) { ->
            return params.orderId
        }

        mockOrder.demand.asBoolean(1..1) { ->
            return true
        }

        mockOrder.demand.isProcessable(1..1) { ->
            return true
        }

        mockOrder.demand.getArticle(1..1) { ->
            return Order.Article.BOOKING
        }

        mockStaticBooking.demand.static.findByOrder(1..1) { Order o ->
            return booking
        }

        mockOrder.demand.getId(1..2) { ->
            return params.orderId
        }

        mockBooking.demand.asBoolean(1..1) { ->
            return true
        }

        mockOrder.demand.getId(2) { ->
            return params.orderId
        }

        controller.startPaymentFlow(params.orderId, getFinishUrl())
        controller.process()

        PaymentFlow paymentFlow = PaymentFlow.getInstance(session, params.orderId)
        String expectedErrorMessageCode = "paymentController.process.errors.alreadyProcessed"

        assert paymentFlow != null
        assert paymentFlow.state.equals(PaymentFlow.State.ERROR)
        assert paymentFlow.errorMessage.equals(expectedErrorMessageCode)

        mockOrder.verify()
        mockStaticOrder.verify()
        mockBooking.verify()
        mockStaticBooking.verify()
    }

    @Test
    void testGetArticleType() {
        assert controller.getArticleType().equals(Order.Article.BOOKING)
    }

    @Test
    void testUserNotAuthorized() {
        def mockOrder = mockFor(Order)
        def mockStaticOrder = mockFor(Order)

        def mockBooking = mockFor(Booking)
        def mockStaticBooking = mockFor(Booking)

        Order order = mockOrder.createMock()
        Booking booking = mockBooking.createMock()
        params.orderId = 1L

        mockStaticOrder.demand.static.get(1..1) { Long id ->
            return order
        }

        mockOrder.demand.getId(1..1) { ->
            return params.orderId
        }

        mockOrder.demand.asBoolean(1..1) { ->
            return true
        }

        mockOrder.demand.isProcessable(1..1) { ->
            return true
        }

        mockOrder.demand.getArticle(1..1) { ->
            return Order.Article.BOOKING
        }

        mockStaticBooking.demand.static.findByOrder(1..1) { Order o ->
            return booking
        }

        mockOrder.demand.getId(1..1) { ->
            return params.orderId
        }

        mockBooking.demand.asBoolean(1..1) { ->
            return false
        }

        mockOrder.demand.getIssuer(1..1) { ->
            return new User(id: 1L)
        }

        springSecurityService.demand.getCurrentUser(1..1) { ->
            return new User(id: 2L)
        }

        mockOrder.demand.getId(1..1) { ->
            return params.orderId
        }

        mockStaticOrder.demand.static.withTransaction { Closure callable ->
            callable.call(null)
        }

        mockStaticOrder.demand.static.get(1..1) { Long id ->
            return order
        }

        mockOrder.demand.getId(1..1) { ->
            return params.orderId
        }

        mockOrder.demand.getArticle(1..1) { ->
            return Order.Article.BOOKING
        }

        mockOrder.demand.refund(1..1) { ->

        }

        mockOrder.demand.assertCustomer(1..1) { ->

        }

        mockOrder.demand.setStatus(1..1) { def s ->

        }

        mockOrder.demand.save(1..1) { def args ->

        }

        mockOrder.demand.getId(1..1) { ->
            return params.orderId
        }

        controller.startPaymentFlow(params.orderId, getFinishUrl())
        controller.process()

        PaymentFlow paymentFlow = PaymentFlow.getInstance(session, params.orderId)
        String expectedErrorMessageCode = "paymentController.process.errors.authError"

        assert paymentFlow != null
        assert paymentFlow.state.equals(PaymentFlow.State.ERROR)
        assert paymentFlow.errorMessage.equals(expectedErrorMessageCode)

        mockOrder.verify()
        mockStaticOrder.verify()
        mockBooking.verify()
        mockStaticBooking.verify()
    }

    @Test
    void testBookingSuccessful() {
        def mockOrder = mockFor(Order)
        def mockStaticOrder = mockFor(Order)

        def mockBooking = mockFor(Booking)
        def mockStaticBooking = mockFor(Booking)

        Order order = mockOrder.createMock()
        Booking booking = mockBooking.createMock()
        params.orderId = 1L

        mockStaticOrder.demand.static.get(1..1) { Long id ->
            return order
        }

        mockOrder.demand.getId(1..1) { ->
            return params.orderId
        }

        mockOrder.demand.asBoolean(1..1) { ->
            return true
        }

        mockOrder.demand.isProcessable(1..1) { ->
            return true
        }

        mockOrder.demand.getArticle(1..1) { ->
            return Order.Article.BOOKING
        }

        mockStaticBooking.demand.static.findByOrder(1..1) { Order o ->
            return booking
        }

        mockOrder.demand.getId(1..1) { ->
            return params.orderId
        }

        mockBooking.demand.asBoolean(1..1) { ->
            return false
        }

        def mockUser = mockFor(User)
        mockUser.demand.getId(2..3) { ->
            return 1L
        }

        User currentUser = mockUser.createMock()

        mockOrder.demand.getIssuer(1..1) { ->
            return currentUser
        }

        mockOrder.demand.getId(1..1) { ->
            return params.orderId
        }

        springSecurityService.demand.getCurrentUser(2) { ->
            return currentUser
        }

        mockOrder.demand.getId(1..1) { ->
            return params.orderId
        }

        bookingService.demand.book(1..1) { Order o, boolean sendNote ->
            return booking
        }

        mockOrder.demand.getId(3..3) { ->
            return params.orderId
        }

        scheduledTaskService.demand.scheduleTask(1) { a, b, c, d ->

        }

        controller.startPaymentFlow(params.orderId, getFinishUrl())
        controller.process()

        PaymentFlow paymentFlow = PaymentFlow.getInstance(session, params.orderId)

        assert paymentFlow != null
        assert paymentFlow.state.equals(PaymentFlow.State.RECEIPT)

        mockOrder.verify()
        mockStaticOrder.verify()
        mockBooking.verify()
        mockStaticBooking.verify()
    }

    @Test
    void testBookingFailed() {
        def mockOrder = mockFor(Order)
        def mockStaticOrder = mockFor(Order)

        def mockBooking = mockFor(Booking)
        def mockStaticBooking = mockFor(Booking)

        Order order = mockOrder.createMock()
        Booking booking = mockBooking.createMock()
        params.orderId = 1L

        mockStaticOrder.demand.static.get(1..1) { Long id ->
            return order
        }

        mockOrder.demand.getId(1..1) { ->
            return params.orderId
        }

        mockOrder.demand.asBoolean(1..1) { ->
            return true
        }

        mockOrder.demand.isProcessable(1..1) { ->
            return true
        }

        mockOrder.demand.getArticle(1..1) { ->
            return Order.Article.BOOKING
        }

        mockStaticBooking.demand.static.findByOrder(1..1) { Order o ->
            return booking
        }

        mockOrder.demand.getId(1..1) { ->
            return params.orderId
        }

        mockBooking.demand.asBoolean(1..1) { ->
            return false
        }

        def mockUser = mockFor(User)
        mockUser.demand.getId(2..2) { ->
            return 1L
        }

        User currentUser = mockUser.createMock()

        mockOrder.demand.getIssuer(1..1) { ->
            return currentUser
        }

        springSecurityService.demand.getCurrentUser(1..1) { ->
            return currentUser
        }

        bookingService.demand.book(1..1) {
            throw new RuntimeException("error")
        }

        mockOrder.demand.getId(1..1) { ->
            return params.orderId
        }

        mockStaticOrder.demand.static.withTransaction { Closure callable ->
            callable.call(null)
        }

        mockStaticOrder.demand.static.get(1..1) { Long id ->
            return order
        }

        mockOrder.demand.getId(1..2) { ->
            return params.orderId
        }

        mockOrder.demand.getArticle(1..1) { ->
            return Order.Article.BOOKING
        }

        mockOrder.demand.refund(1..1) { ->

        }

        mockOrder.demand.assertCustomer(1..1) { ->

        }

        mockOrder.demand.setStatus(1..1) { def s ->

        }

        mockOrder.demand.save(1..1) { def args ->

        }

        mockOrder.demand.getId(1..1) { ->
            return params.orderId
        }

        controller.startPaymentFlow(params.orderId, getFinishUrl())
        controller.process()

        PaymentFlow paymentFlow = PaymentFlow.getInstance(session, params.orderId)

        assert paymentFlow != null
        assert paymentFlow.state.equals(PaymentFlow.State.ERROR)

        mockOrder.verify()
        mockStaticOrder.verify()
        mockBooking.verify()
        mockStaticBooking.verify()
    }

    @Test
    void testPayMultipleZeroSlots() {
        def cmd = new MultipleBookingPaymentCommand(
                facilityId: 1L, method: PaymentMethod.CREDIT_CARD_RECUR)
        cmd.validate()

        springSecurityService.demand.isLoggedIn(1) { -> return true }

        controller.payEntryPoint(cmd)

        assert view == "/bookingPayment/showError"
        springSecurityService.verify()
    }

    @Test
    void testPayMultipleFacilities() {
        String slotId1 = '123abc'
        String slotId2 = '456def'
        def cmd = new MultipleBookingPaymentCommand(slotIds: [slotId1, slotId2],
                facilityId: 1L, method: PaymentMethod.CREDIT_CARD_RECUR)
        cmd.validate()

        assert !cmd.hasErrors()

        def mockSlot1 = mockFor(Slot)
        mockSlot1.demand.getCourt(1) { -> return [facility: [id: 1]] }
        def mockSlot2 = mockFor(Slot)
        mockSlot2.demand.getCourt(1) { -> return [facility: [id: 2]] }

        springSecurityService.demand.isLoggedIn(1) { ->
            return true
        }

        def mockUser = mockFor(User)
        User currentUser = mockUser.createMock()
        springSecurityService.demand.getCurrentUser(1) { ->
            return currentUser
        }

        slotService.demand.getSlots(1) { List<String> ids ->
            assert ids == [slotId1, slotId2]
            return [mockSlot1.createMock(), mockSlot2.createMock()]
        }

        shouldFail {
            controller.payEntryPoint(cmd)
        }

        springSecurityService.verify()
        slotService.verify()
        mockUser.verify()

        mockSlot1.verify()
    }

    @Test
    void testPayMultipleFreeSuccess() {
        String slotId1 = '123abc'
        String slotId2 = '456def'
        def cmd = new MultipleBookingPaymentCommand(slotIds: [slotId1, slotId2],
                facilityId: 1L, method: PaymentMethod.CREDIT_CARD_RECUR)
        cmd.validate()

        springSecurityService.demand.isLoggedIn(1) { ->
            return true
        }


        Court court = new Court(facility: new Facility(id: 1l, showBookingHolder: false))
        List<Slot> slots = [new Slot(court: court), new Slot(court: court)]

        slotService.demand.getSlots(1) { List<String> ids ->
            assert ids == [slotId1, slotId2]
            return slots
        }

        def mockUser = mockFor(User)
        User currentUser = mockUser.createMock()

        springSecurityService.demand.getCurrentUser(1) { ->
            return currentUser
        }

        def mockOrder = mockFor(Order)

        Map slotsWithObjects = slots.collectEntries { Slot slot ->
            [(slot):
                     [
                             order: mockOrder.createMock()
                     ]
            ]
        }

        slotsWithObjects.each { def slot, def objects ->
            paymentService.demand.createBookingOrder(1) { s, u, o ->
                assert o == Order.ORIGIN_WEB
                assert u == currentUser
                return objects.order
            }
        }

        mockOrder.demand.assertCustomer(1) { -> }
        mockOrder.demand.save(1) { -> }
        mockOrder.demand.assertCustomer(1) { -> }
        mockOrder.demand.save(1) { -> }
        mockOrder.demand.total(2) { -> return 0 }
        mockOrder.demand.setStatus(2) { s -> assert s == Order.Status.COMPLETED }

        slotsWithObjects.each { def slot, def objects ->
            objects.mockBooking = mockFor(Booking)
            objects.booking = objects.mockBooking.createMock()
            bookingService.demand.book(1) { Order o, boolean n ->
                assert o == objects.order
                assert !n
                return objects.booking
            }
        }

        priceListServiceControl.demand.getPriceForSlot(2) { Slot s, Customer c, List<String> pe, Long cgi ->
            return new Price(price: 0l)
        }

        scheduledTaskService.demand.scheduleTask(1) { a, b, c, d ->

        }

        courtService.demand.getCourtGroupRestrictions{c, f, s -> [:]}

        controller.payEntryPoint(cmd)

        slotService.verify()
        mockUser.verify()
        springSecurityService.verify()
        mockOrder.verify()
        paymentService.verify()
        priceListServiceControl.verify()
        scheduledTaskService.verify()
        slotsWithObjects.each { def slot, def objects ->
            objects.mockBooking.verify()
        }
    }

    @Test
    void testPayMultipleCostMoneyCreditCard() {
        String slotId1 = '123abc'
        String slotId2 = '456def'
        def cmd = new MultipleBookingPaymentCommand(slotIds: [slotId1, slotId2],
                facilityId: 1L, method: PaymentMethod.CREDIT_CARD_RECUR)
        cmd.validate()

        Court court = new Court(facility: new Facility(id: 1l, showBookingHolder: false))
        List<Slot> slots = [new Slot(court: court), new Slot(court: court)]

        slotService.demand.getSlots(1) { List<String> ids ->
            assert ids == [slotId1, slotId2]
            return slots
        }

        def mockUser = mockFor(User)
        User currentUser = mockUser.createMock()

        springSecurityService.demand.isLoggedIn(1) { ->
            return true
        }

        springSecurityService.demand.getCurrentUser(1) { ->
            return currentUser
        }

        def mockOrder = mockFor(Order)

        Map slotsWithObjects = slots.collectEntries { def slot ->
            [(slot):
                     [
                             order: mockOrder.createMock()
                     ]
            ]
        }

        slotsWithObjects.each { def slot, def objects ->
            paymentService.demand.createBookingOrder(1) { s, u, o ->
                assert o == Order.ORIGIN_WEB
                assert u == currentUser
                return objects.order
            }
        }

        mockOrder.demand.assertCustomer(1) { -> }
        mockOrder.demand.save(1) { -> }
        mockOrder.demand.assertCustomer(1) { -> }
        mockOrder.demand.save(1) { -> }
        mockOrder.demand.total(2) { -> return 100 }

        Integer uniqueId = 1
        mockOrder.demand.getId(2) { -> uniqueId++ }
        slotsWithObjects.each { def slot, def objects ->
            paymentService.demand.handleCreditCardPayment(1) { o, u ->
                assert u == currentUser
                assert o == objects.order
            }
        }


        mockOrder.demand.isFinalPaid(2) { ->
            return true
        }

        slotsWithObjects.each { def mockSlot, def objects ->
            objects.mockBooking = mockFor(Booking)
            objects.booking = objects.mockBooking.createMock()
            bookingService.demand.book(1) { Order o, boolean n ->
                assert o == objects.order
                assert !n
                return objects.booking
            }
        }

        priceListServiceControl.demand.getPriceForSlot(2) { Slot s, Customer c, List<String> pe, Long cgi ->
            return new Price(price: 100l)
        }

        scheduledTaskService.demand.scheduleTask(1) { a, b, c, d ->

        }

        courtService.demand.getCourtGroupRestrictions{c, f, s -> [:]}

        mockOrder.demand.getId(2) { -> uniqueId++ / 2 }

        controller.payEntryPoint(cmd)

        slotService.verify()
        mockUser.verify()
        springSecurityService.verify()
        mockOrder.verify()
        paymentService.verify()
        priceListServiceControl.verify()
        scheduledTaskService.verify()
        slotsWithObjects.each { def slot, def objects ->
            objects.mockBooking.verify()
        }
    }

    @Test
    void testPayMultipleCostMoneyCreditCardOneFree() {
        String slotId1 = '123abc'
        String slotId2 = '456def'
        def cmd = new MultipleBookingPaymentCommand(slotIds: [slotId1, slotId2],
                facilityId: 1L, method: PaymentMethod.CREDIT_CARD_RECUR)
        cmd.validate()

        assert !cmd.hasErrors()

        Court court = new Court(facility: new Facility(id: 1l, showBookingHolder: false))
        List<Slot> slots = [new Slot(court: court), new Slot(court: court)]

        slotService.demand.getSlots(1) { List<String> ids ->
            assert ids == [slotId1, slotId2]
            return slots
        }

        def mockUser = mockFor(User)
        User currentUser = mockUser.createMock()

        springSecurityService.demand.isLoggedIn(1) { ->
            return true
        }

        springSecurityService.demand.getCurrentUser(1) { ->
            return currentUser
        }

        def mockOrder = mockFor(Order)

        Map slotsWithObjects = slots.collectEntries { def slot ->

            [(slot):
                     [
                             order: mockOrder.createMock()
                     ]
            ]
        }

        slotsWithObjects.values()[0].amount = 0
        slotsWithObjects.values()[1].amount = 100

        slotsWithObjects.each { def mockSlot, def objects ->
            paymentService.demand.createBookingOrder(1) { s, u, o ->
                assert o == Order.ORIGIN_WEB
                assert u == currentUser
                return objects.order
            }
        }

        mockOrder.demand.assertCustomer(1) { -> }
        mockOrder.demand.save(1) { -> }
        mockOrder.demand.assertCustomer(1) { -> }
        mockOrder.demand.save(1) { -> }

        List<Long> totals = [100, 0]
        mockOrder.demand.total(2) { ->
            return totals.pop()
        }

        mockOrder.demand.isFinalPaid(1) { ->
            return true
        }


        slotsWithObjects.each { def mockSlot, def objects ->
            objects.mockBooking = mockFor(Booking)
            objects.booking = objects.mockBooking.createMock()
            bookingService.demand.book(1) { Order o, boolean n ->
                assert o == objects.order
                assert !n
                return objects.booking
            }
        }

        List<Long> prices = [100, 0]
        priceListServiceControl.demand.getPriceForSlot(2) { Slot s, Customer c, List<String> pe, Long cgi ->
            return new Price(price: prices.pop())
        }

        paymentService.demand.handleCreditCardPayment(1) { o, u ->
            assert u == currentUser
        }

        scheduledTaskService.demand.scheduleTask(1) { a, b, c, d ->

        }
        courtService.demand.getCourtGroupRestrictions{c, f, s -> [:]}

        controller.payEntryPoint(cmd)

        slotService.verify()
        mockUser.verify()
        springSecurityService.verify()
        mockOrder.verify()
        paymentService.verify()
        priceListServiceControl.verify()
        scheduledTaskService.verify()
        slotsWithObjects.each { def mockSlot, def objects ->
            objects.mockBooking.verify()
        }
    }

    void testBookingPrice() {
        def slot = createSlot()
        def facility = slot.court.facility
        def user = createUser()
        def customer = createCustomer(facility, null, null, null, null, user)
        springSecurityService.demand.isLoggedIn(1) { -> return true }
        springSecurityService.demand.getCurrentUser(1) { ->
            return user
        }

        slotService.demand.groupByCourtAndAdjacency(1) { List<Slot> slots, List<Slot> firstSlots ->
            assert slots == firstSlots
            AdjacentSlotGroup slotGroup = new AdjacentSlotGroup(slots)
            slotGroup.subsequentSlots = slots
            return [slotGroup]
        }

        priceListServiceControl.demand.getPricesForSlots(1) { List<Slot> s, Customer c, List<String> pe, Long cgi, Map l ->
            return [(s[0].id): 200l]
        }

        PaymentInfo paymentInfo = new PaymentInfo()
        paymentService.demand.getAnyPaymentInfoByUser(1) { User u ->
            assert u == user
            return paymentInfo
        }

        couponService.demand.getValidCouponsByUserAndSlots { u, s, p, t, i -> [] }
        couponService.demand.getValidCouponsByUserAndSlots { u, s, p, t -> [] }
        couponService.demand.getActivePromoCodes { f -> [] }

        courtService.demand.getCourtGroupRestrictions{c, f, s -> [:]}

        ConfirmUpdateCommand cmd = new ConfirmUpdateCommand(slotIds: [slot.id], firstSlotIds: [slot.id])
        controller.updateConfirmModalModel(cmd)

        assert response.status == 200
        assert response.json.prices.size() == 1
        assert response.json.prices[slot.id] == 200
        slotService.verify()
        springSecurityService.verify()
        priceListServiceControl.verify()
    }

    void testBookingPriceAvg() {
        def slot = createSlot()
        def facility = slot.court.facility
        facility.setFacilityProperty(FacilityProperty.FacilityPropertyKey.FEATURE_CALCULATE_MULTIPLE_PLAYERS_PRICE, "1")
        facility.save(failOnError: true, flush: true)
        def user = createUser()
        def customer = createCustomer(facility, null, null, null, null, user)

        springSecurityService.demand.isLoggedIn(1) { -> return true }
        springSecurityService.demand.getCurrentUser(1) { -> return user }

        slotService.demand.groupByCourtAndAdjacency(1) { List<Slot> slots, List<Slot> firstSlots ->
            assert slots == firstSlots
            AdjacentSlotGroup slotGroup = new AdjacentSlotGroup(slots)
            slotGroup.subsequentSlots = slots
            return [slotGroup]
        }

        priceListServiceControl.demand.getPricesForSlots(1) { List<Slot> s, Customer c, List<String> pe, Long cgi, Map l ->
            return [(s[0].id): 100l]
        }

        PaymentInfo paymentInfo = new PaymentInfo()
        paymentService.demand.getAnyPaymentInfoByUser(1) { User u ->
            assert u == user
            return paymentInfo
        }

        couponService.demand.getValidCouponsByUserAndSlots { u, s, p, t, i -> [] }
        couponService.demand.getValidCouponsByUserAndSlots { u, s, p, t -> [] }
        couponService.demand.getActivePromoCodes { f -> [] }

        courtService.demand.getCourtGroupRestrictions{c, f, s -> [:]}

        params.slotIds = slot.id
        params.firstSlotIds = slot.id

        ConfirmUpdateCommand cmd = new ConfirmUpdateCommand(slotIds: [slot.id], firstSlotIds: [slot.id])
        controller.updateConfirmModalModel(cmd)

        assert response.status == 200
        assert response.json.prices.size() == 1
        assert response.json.prices[slot.id] == 100
        slotService.verify()
        springSecurityService.verify()
        priceListServiceControl.verify()
    }

    void testBookingPriceAvgWithPlayers() {
        def slot = createSlot()
        def facility = slot.court.facility
        User user = createUser()
        facility.setFacilityProperty(FacilityProperty.FacilityPropertyKey.FEATURE_CALCULATE_MULTIPLE_PLAYERS_PRICE, "1")
        facility.save(failOnError: true, flush: true)

        springSecurityService.demand.isLoggedIn(1) { -> return true }
        springSecurityService.demand.getCurrentUser(1) { -> return user }

        def customer1 = createCustomer(facility, "cust1@matchi.com")
        def customer2 = createCustomer(facility, "cust2@matchi.com")

        slotService.demand.groupByCourtAndAdjacency(1) { List<Slot> slots, List<Slot> firstSlots ->
            assert slots == firstSlots
            AdjacentSlotGroup slotGroup = new AdjacentSlotGroup(slots)
            slotGroup.subsequentSlots = slots
            return [slotGroup]
        }

        priceListServiceControl.demand.getPricesForSlots(1) { List<Slot> s, Customer c, List<String> pe, Long cgi, Map l ->
            return [(s[0].id): 150l]
        }

        PaymentInfo paymentInfo = new PaymentInfo()
        paymentService.demand.getAnyPaymentInfoByUser(1) { User u ->
            assert u == user
            return paymentInfo
        }

        couponService.demand.getValidCouponsByUserAndSlots { u, s, p, t, i -> [] }
        couponService.demand.getValidCouponsByUserAndSlots { u, s, p, t -> [] }
        couponService.demand.getActivePromoCodes { f -> [] }

        courtService.demand.getCourtGroupRestrictions{c, f, s -> [:]}

        params.slotIds = slot.id
        params.firstSlotIds = slot.id

        ConfirmUpdateCommand cmd = new ConfirmUpdateCommand(slotIds: [slot.id], firstSlotIds: [slot.id], playerEmails: [customer1.email, customer2.email, "new@matchi.com"])
        controller.updateConfirmModalModel(cmd)

        assert response.status == 200
        assert response.json.prices.size() == 1
        assert response.json.prices[slot.id] == 150
        springSecurityService.verify()
        slotService.verify()
        priceListServiceControl.verify()
    }
}