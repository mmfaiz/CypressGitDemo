package com.matchi


import com.matchi.orders.Order
import com.matchi.play.Recording
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.codehaus.groovy.grails.commons.InstanceFactoryBean
/**
 * @author Sergei Shushkevich
 */
@TestFor(RecordingPaymentService)
@Mock([Order, Region, Municipality, Facility, Customer, Court, Slot, Sport, Subscription, Booking, FacilityProperty, Camera, User])
class RecordingPaymentServiceTests {

    void testCreateRecordingPaymentOrder() {
//        def mockGameCamService = mockFor(GameCamService)
//        def gameCamService = mockGameCamService.createMock()

        def mockPaymentService = mockFor(PaymentService)
        def mockFacilityService = mockFor(FacilityService)

        def price = 50

        mockPaymentService.demand.getRecordingPrice(1..1) { a ->
            return 50
        }

        mockFacilityService.demand.getGlobalFacility(1..3) { ->
            return TestUtils.createFacility()
        }

        defineBeans {
            paymentService(InstanceFactoryBean, mockPaymentService.createMock(), PaymentService)
            facilityService(InstanceFactoryBean, mockFacilityService.createMock(), FacilityService)
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

        Booking booking = TestUtils.createBooking(customer, slot)
        booking.id = 1L
        booking.save(flush: true, failOnError: true)

        def recording = new Recording()
        recording.bookingId = 1L

        recording.booking.slot.court.facility

        def user = new User()

        def order = service.createRecordingPaymentOrder(user, recording)
        order.save(failOnError: true)

        assert order
        assert Order.Article.RECORDING == order.article
        assert order.metadata
        assert recording.bookingId.toString() == order.metadata['recording.bookingId']
        assert user == order.user
        assert user == order.issuer
        assert order.dateDelivery
        assert order.price == 50
        assert facility.vat == order.vat
        assert "web" == order.origin
        assert 1 == Order.count()
    }
}
