package com.matchi


import com.matchi.orders.Order
import com.matchi.play.PlayService
import com.matchi.play.Recording
import grails.plugin.springsecurity.SpringSecurityService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor

@TestFor(PlayService)
@Mock([Order, Region, Municipality, Facility, Customer, Court, Slot, Sport, Subscription, Booking, FacilityProperty, Camera, User])
class PlayServiceTests {

    void testGetRecordingFromBookingIsNull() {
        def booking = new Booking()
        booking.id = 1

        Recording recording = service.getRecordingFromBooking(booking)

        assert recording == null
    }

    void testGetRecordingFromBooking() {
        def mockSpringSecurityService = mockFor(SpringSecurityService)
        service.springSecurityService = mockSpringSecurityService.createMock()

        def mockRecordingPaymentService = mockFor(RecordingPaymentService)
        service.recordingPaymentService = mockRecordingPaymentService.createMock()

        User user = TestUtils.createUser("test@email.com")

        mockSpringSecurityService.demand.getCurrentUser(1..1) { ->
            return user
        }
        mockRecordingPaymentService.demand.getRecordingPurchaseByUser(1..1) { recording, u ->
            return null
        }

        Facility facility = TestUtils.createFacility()
        facility.setFacilityProperty(FacilityProperty.FacilityPropertyKey.FEATURE_MATCHI_PLAY, "1")

        Customer customer = TestUtils.createCustomer(facility)
        Court court = TestUtils.createCourt(facility)
        Camera camera = TestUtils.createCamera(court)
        court.addToCameras(camera)

        Date startTime = new Date()
        Date endTime = new Date().plus(1)

        Slot slot = TestUtils.createSlot(court, startTime, endTime)
        def booking = TestUtils.createBooking(customer, slot)

        Recording recording = service.getRecordingFromBooking(booking)

        assert recording
    }
}
