package com.matchi.facility

import com.matchi.*
import com.matchi.enums.BookingGroupType
import com.matchi.orders.AdyenOrderPayment
import com.matchi.orders.Order
import com.matchi.orders.OrderPayment
import grails.test.mixin.Mock
import grails.test.mixin.TestFor

import static com.matchi.TestUtils.*

@TestFor(FacilityBookingController)
@Mock([BookingGroup, Court, Customer, Facility, Municipality, Region, Slot, Sport, Subscription, Booking, FacilityProperty])
class FacilityBookingControllerTests {

    void testExportBookings() {
        def slotServiceControl = mockFor(SlotService)
        def securityServiceControl = mockSecurityService()
        def bookingServiceControl = mockFor(BookingService)
        bookingServiceControl.demand.exportBookings { slots, out -> }
        controller.bookingService = bookingServiceControl.createMock()
        params.exportSlotsData = "12345"

        controller.exportBookings()

        assert response.containsHeader("Content-disposition")
        assert "text/csv" == response.contentType
        securityServiceControl.verify()
        bookingServiceControl.verify()
    }

    void testExportBookingCustomers() {
        def securityServiceControl = mockSecurityService()
        def bookingServiceControl = mockFor(BookingService)
        bookingServiceControl.demand.exportBookingCustomers { ids, out -> }
        controller.bookingService = bookingServiceControl.createMock()
        params.exportSlotsData = "12345"

        controller.exportBookingCustomers()

        assert response.containsHeader("Content-disposition")
        assert "text/csv" == response.contentType
        securityServiceControl.verify()
        bookingServiceControl.verify()
    }

    void testRebookSubscriber() {
        def subscription = createSubscription()
        def slot = createSlot()
        slot.subscription = subscription
        slot.save(failOnError: true)
        def slotServiceControl = mockFor(SlotService)
        slotServiceControl.demand.getSlots { ids -> [slot] }
        controller.slotService = slotServiceControl.createMock()
        def customerServiceControl = mockFor(CustomerService)
        customerServiceControl.demand.collectPlayers { pc, up -> }
        controller.customerService = customerServiceControl.createMock()
        def bookingAvailabilityServiceControl = mockFor(BookingAvailabilityService)
        bookingAvailabilityServiceControl.demand.addBookingGroupBookings { s, bg, sl, c, shc, cmt, pc -> }
        controller.bookingAvailabilityService = bookingAvailabilityServiceControl.createMock()
        def subscriptionServiceControl = mockFor(SubscriptionService)
        subscriptionServiceControl.demand.createBookingsOrders { s -> }
        controller.subscriptionService = subscriptionServiceControl.createMock()

        def courtRelationServiceControl = mockFor(CourtRelationsBookingService)
        courtRelationServiceControl.demand.tryBookRelatedCourts(1) { b -> }
        controller.courtRelationsBookingService = courtRelationServiceControl.createMock()

        controller.rebookSubscriber(new FacilityBookingCommand(slotId: slot.id))

        assert "/facility/booking/index?date=" == response.redirectedUrl
        slotServiceControl.verify()
        bookingAvailabilityServiceControl.verify()
        subscriptionServiceControl.verify()
    }

    void testMakePaymentsNotRefundableBooking() {
        def mockSlotService = mockFor(SlotService)
        def mockUserService = mockFor(UserService)
        def mockBookingService = mockFor(BookingService)
        def mockOrder = mockFor(Order)

        controller.slotService = mockSlotService.createMock()
        controller.userService = mockUserService.createMock()
        controller.bookingService = mockBookingService.createMock()

        def slot = createSlot()
        slot.save()

        mockSlotService.demand.getSlots(1..1) { def ids ->
            return [slot]
        }

        mockUserService.demand.getLoggedInUser(1..1) { ->
            return null
        }

        mockBookingService.demand.getBookingsBySlots(1..1) { def ids ->
            return [new Booking(order: mockOrder.createMock())]
        }

        mockOrder.demand.isStillRefundable(1..1) { ->
            return false
        }

        def cmd = new FacilityBookingCommand(slotId: slot.id, paid: false)
        controller.makePayments(cmd)

        mockSlotService.verify()
        mockUserService.verify()
        mockBookingService.verify()
        mockOrder.verify()
    }

    void testMakePaymentsRefundableBooking() {
        def mockSlotService = mockFor(SlotService)
        def mockUserService = mockFor(UserService)
        def mockBookingService = mockFor(BookingService)
        def mockOrder = mockFor(Order)
        def mockAdyenOrderPayment = mockFor(AdyenOrderPayment)

        controller.slotService = mockSlotService.createMock()
        controller.userService = mockUserService.createMock()
        controller.bookingService = mockBookingService.createMock()

        def slot = createSlot()
        slot.save()

        Order order = mockOrder.createMock()
        AdyenOrderPayment op = mockAdyenOrderPayment.createMock()
        op.id = 1L
        op.amount = 100
        op.status = OrderPayment.Status.CAPTURED
        order.payments = [op]

        mockSlotService.demand.getSlots(1..1) { def ids ->
            return [slot]
        }

        mockUserService.demand.getLoggedInUser(1..1) { ->
            return null
        }

        mockBookingService.demand.getBookingsBySlots(1..1) { def ids ->
            return [new Booking(order: order)]
        }

        mockOrder.demand.isStillRefundable(1..1) { ->
            return true
        }

        mockAdyenOrderPayment.demand.refund(1..1) { def amount ->
            assert amount == op.amount
        }

        def cmd = new FacilityBookingCommand(slotId: slot.id, paid: false)
        controller.makePayments(cmd)

        mockSlotService.verify()
        mockUserService.verify()
        mockBookingService.verify()
        mockOrder.verify()
        mockAdyenOrderPayment.verify()
    }

    void testBookingPayment() {
        def slot = createSlot()
        def facility = slot.court.facility
        def customer = createCustomer(facility)
        def securityService = mockSecurityService(facility)
        def customerService = mockFor(CustomerService)
        customerService.demand.getCustomer { id -> customer }
        controller.customerService = customerService.createMock()
        def slotService = mockFor(SlotService)
        slotService.demand.getSlots { s -> [slot] }
        controller.slotService = slotService.createMock()
        def priceListService = mockFor(PriceListService)
        priceListService.demand.getBookingPrices { s, c ->
            [total: 200, rows: [:]]
        }
        controller.priceListService = priceListService.createMock()
        def couponService = mockFor(CouponService)
        couponService.demand.getValidCouponsByCustomerAndSlots { c, s, p, t, i -> [] }
        couponService.demand.getValidCouponsByCustomerAndSlots { c, s, p, t -> [] }
        controller.couponService = couponService.createMock()
        params.customerId = customer.id
        params.slotId = slot.id

        controller.bookingPayment()

        assert view == "/facilityBooking/facilityBookingFormPayment"
        assert model.bookingPrices.total == 200
        securityService.verify()
        customerService.verify()
        slotService.verify()
        priceListService.verify()
        couponService.verify()
    }

    void testBookingPaymentAvg() {
        def slot = createSlot()
        def facility = slot.court.facility
        facility.setFacilityProperty(FacilityProperty.FacilityPropertyKey.FEATURE_CALCULATE_MULTIPLE_PLAYERS_PRICE, "1")
        facility.save(failOnError: true, flush: true)
        def customer = createCustomer(facility)
        def securityService = mockSecurityService(facility)
        def customerService = mockFor(CustomerService)
        customerService.demand.getCustomer { id -> customer }
        customerService.demand.collectPlayers { p, u -> [] }
        controller.customerService = customerService.createMock()
        def slotService = mockFor(SlotService)
        slotService.demand.getSlots { s -> [slot] }
        controller.slotService = slotService.createMock()
        def priceListService = mockFor(PriceListService)
        priceListService.demand.getAvgBookingPrices { s, customers ->
            assert customers.size() == 1
            assert customers[0] == customer
            [total: 100, rows: [:]]
        }
        controller.priceListService = priceListService.createMock()
        def couponService = mockFor(CouponService)
        couponService.demand.getValidCouponsByCustomerAndSlots { c, s, p, t, i -> [] }
        couponService.demand.getValidCouponsByCustomerAndSlots { c, s, p, t -> [] }
        controller.couponService = couponService.createMock()
        params.customerId = customer.id
        params.slotId = slot.id

        controller.bookingPayment()

        assert view == "/facilityBooking/facilityBookingFormPayment"
        assert model.bookingPrices.total == 100
        securityService.verify()
        customerService.verify()
        slotService.verify()
        priceListService.verify()
        couponService.verify()
    }

    private mockSecurityService(facility = null) {
        def serviceControl = mockFor(SecurityService)
        serviceControl.demand.getUserFacility { -> facility ?: createFacility() }
        controller.securityService = serviceControl.createMock()
        serviceControl
    }
}