package com.matchi.api

import com.matchi.*
import grails.converters.JSON
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.codehaus.groovy.grails.web.json.JSONObject
import org.joda.time.DateTime
import org.junit.Before

/**
 * See the API for {@link grails.test.mixin.web.ControllerUnitTestMixin} for usage instructions
 */

@TestFor(MLCSController)
@Mock([Facility, FacilityProperty])
class MLCSControllerTests {
    def facility
    MLCSController controller
    def bookings = []

    // test methods
    void testRightFacilityName() {
        def result = makeRequestAndReturnJSON()
        assert result.facility == facility.name
    }

    void testNumberOfCourts() {
        def result = makeRequestAndReturnJSON()
        assert result.courts.size() == 2
    }

    void testNumberOfScheduleItems() {
        def result = makeRequestAndReturnJSON()
        assert result.courts.get(0).schedule.size() == 1
    }

    void testRightFromAndEndTimesInSchedule() {
        facility.setFacilityProperty("MLCS_GRACE_MINUTES_START", "0")
        facility.setFacilityProperty("MLCS_GRACE_MINUTES_END", "0")

        def result = makeRequestAndReturnJSON()

        def court1Schedule = result.courts.find { it.id == "1" }

        assert court1Schedule.schedule.get(0).from ==
                MLCSController.DATE_FORMAT.print(new DateTime(2012, 1, 1, 12, 0, 0, 0))
        assert court1Schedule.schedule.get(0).to ==
                MLCSController.DATE_FORMAT.print(new DateTime(2012, 1, 1, 14, 0, 0, 0))
    }

    void testGraceTimeRightFromAndEndTimesInSchedule() {
        facility.setFacilityProperty("MLCS_GRACE_MINUTES_START", "5")
        facility.setFacilityProperty("MLCS_GRACE_MINUTES_END", "5")

        def result = makeRequestAndReturnJSON()

        def court1Schedule = result.courts.find { it.id == "1" }

        assert court1Schedule.schedule.get(0).from ==
                MLCSController.DATE_FORMAT.print(new DateTime(2012, 1, 1, 11, 55, 0, 0))
        assert court1Schedule.schedule.get(0).to ==
                MLCSController.DATE_FORMAT.print(new DateTime(2012, 1, 1, 14, 5, 0, 0))
    }

    void testGraceTimeOnlyStartRightFromAndEndTimesInSchedule() {
        facility.setFacilityProperty("MLCS_GRACE_MINUTES_START", "10")
        facility.setFacilityProperty("MLCS_GRACE_MINUTES_END", "0")

        def result = makeRequestAndReturnJSON()

        def court1Schedule = result.courts.find { it.id == "1" }

        assert court1Schedule.schedule.get(0).from ==
                MLCSController.DATE_FORMAT.print(new DateTime(2012, 1, 1, 11, 50, 0, 0))
        assert court1Schedule.schedule.get(0).to ==
                MLCSController.DATE_FORMAT.print(new DateTime(2012, 1, 1, 14, 0, 0, 0))
    }

    void testGraceTimeOnlyEndRightFromAndEndTimesInSchedule() {
        facility.setFacilityProperty("MLCS_GRACE_MINUTES_START","0")
        facility.setFacilityProperty("MLCS_GRACE_MINUTES_END","5")

        def result = makeRequestAndReturnJSON()

        def court1Schedule = result.courts.find { it.id == "1" }

        assert court1Schedule.schedule.get(0).from ==
                MLCSController.DATE_FORMAT.print(new DateTime(2012, 1, 1, 12, 0, 0, 0))
        assert court1Schedule.schedule.get(0).to ==
                MLCSController.DATE_FORMAT.print(new DateTime(2012, 1, 1, 14, 5, 0, 0))
    }

    // setup methods
    private def makeRequestAndReturnJSON() {
        params.from = "2012-01-01"
        params.to = "2012-01-10"
        controller.schedule()
        JSONObject result = JSON.parse(controller.response.contentAsString)
        result
    }

    @Before
    public void setUp() {
        setupFacility()
        setupBookings()

        def facilityMockController = mockFor(FacilityService)
        facilityMockController.demand.getFacility(0..1) { def key ->
            return facility
        }

        def bookingServiceMockController = mockFor(BookingService)
        bookingServiceMockController.demand.findAllBookingsByInterval(0..1) { def facility, def interval ->
            return bookings
        }

        controller = new MLCSController()
        controller.facilityService = facilityMockController.createMock()
        controller.mlcsService = new MlcsService()
        controller.mlcsService.bookingService = bookingServiceMockController.createMock()
        controller.mlcsService.dateUtil = new DateUtil()

        facility.facilityProperties = [
                new FacilityProperty(key: FacilityProperty.FacilityPropertyKey.MLCS_LAST_HEARTBEAT.toString(), value: new DateTime().toString()),
                new FacilityProperty(key: FacilityProperty.FacilityPropertyKey.MLCS_GRACE_MINUTES_START.toString(), value: "0"),
                new FacilityProperty(key: FacilityProperty.FacilityPropertyKey.MLCS_GRACE_MINUTES_END.toString(), value: "0")]


    }

    private Facility setupFacility() {
        facility = new Facility()
        facility.id = 1
        facility.name = "Test Facility TK"
        facility.apikey = "apikeydummy"
        return facility
    }

    private void setupBookings() {
        def court1 = new Court(); court1.externalId = "1"; court1.id = 1; court1.name = "Bana 1"
        def court2 = new Court(); court2.externalId = "2"; court2.id = 2; court2.name = "Bana 2"

        // no external ID - should not appear in MLCS
        def court3 = new Court(); court3.externalId = null; court3.id = 3; court3.name = "Bana 3"

        bookings << new Booking(slot: createSlot(court1, 1, 12, 13))
        bookings << new Booking(slot: createSlot(court1, 1, 13, 14))
        bookings << new Booking(slot: createSlot(court2, 1, 13, 14))
        bookings << new Booking(slot: createSlot(court3, 1, 13, 14))

        facility.courts = [court1, court2, court3]
    }

    private Slot createSlot(Court court, day, start, end) {
        new Slot(startTime: createDate(day,start).toDate(), endTime: createDate(day, end).toDate(), court: court)
    }

    private DateTime createDate(def day, def hour) {
        new DateTime(2012,1,day,hour,0,0)
    }
}
