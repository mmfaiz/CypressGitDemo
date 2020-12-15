package com.matchi.facility

import com.matchi.Court
import com.matchi.Facility
import com.matchi.MatchiConfigMethodAvailability
import com.matchi.Municipality
import com.matchi.Region
import com.matchi.Season
import com.matchi.SeasonDeviation
import com.matchi.SlotService
import com.matchi.User
import com.matchi.slots.SlotFilter
import com.matchi.SecurityService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.domain.DomainClassUnitTestMixin
import org.joda.time.LocalTime
import org.joda.time.Period
import org.junit.Before

import static com.matchi.TestUtils.*

@TestFor(FacilitySeasonDeviationController)
@Mock([SeasonDeviation, Season, Facility, Municipality, Region, MatchiConfigMethodAvailability])
@TestMixin(DomainClassUnitTestMixin)
class FacilitySeasonDeviationControllerTests {

    def mockSlotService

    Facility facility
    Season season

    @Before
    public void setUp() {
        facility = createFacility()
        season = createSeason(facility)
        mockDomain(Court, [new Court(id: 1L), new Court(id: 2L), new Court(id: 3L)])
        mockForConstraintsTests(SeasonDeviationCommand)

        mockSlotService = mockFor(SlotService)

        mockSlotService.demand.generateSlots(1..1) {
            return []
        }

        mockSlotService.demand.getSlots(1..1) {
            return []
        }

        controller.slotService = mockSlotService.createMock()

        controller.params.seasonId = 1
    }

    void testConfirmRendersConfirmView() {
        def securityServiceControl = mockSecurityService()
        def cmd = createDeviationCommand()

        controller.confirm(cmd)
        assertEquals("/facilitySeasonDeviation/confirm",controller.modelAndView.viewName)
        securityServiceControl.verify()
    }

    void testConfirmReturnsCreateSeasonDeviation() {
        def securityServiceControl = mockSecurityService()
        def cmd = createDeviationCommand()
        controller.confirm(cmd)
        assertNotNull(controller.modelAndView.model.createSeason)
        securityServiceControl.verify()
    }

    void testConfirmReturnsCreateSeasonDeviationCourts() {
        def securityServiceControl = mockSecurityService()
        def cmd = createDeviationCommand()
        controller.confirm(cmd)
        assertEquals(3, controller.modelAndView.model.createSeason.courts.size())
        securityServiceControl.verify()
    }

    void testConfirmReturnsCreateSeasonDeviationClosed() {
        def securityServiceControl = mockSecurityService()
        def cmd = createDeviationCommand(false)
        controller.confirm(cmd)
        assertFalse("Should be closed", controller.modelAndView.model.createSeason.open)
        assert model.containsKey("slots")
        assert model.containsKey("tableSlots")
        securityServiceControl.verify()
    }

    void testConfirmReturnsCreateSeasonDeviationOpen() {
        def securityServiceControl = mockSecurityService()
        def cmd = createDeviationCommand(true)
        controller.confirm(cmd)
        assertTrue("Should be open", controller.modelAndView.model.createSeason.open)
        assert model.containsKey("slots")
        assert model.containsKey("overlaps")
        securityServiceControl.verify()
    }

    void testConfirmWeekDays() {
        def securityServiceControl = mockSecurityService()
        def cmd = createDeviationCommand(true)
        controller.confirm(cmd)
        assertEquals([1,2,3,4,5,6], controller.modelAndView.model.createSeason.weekDays)
        securityServiceControl.verify()
    }

   void testConfirmSetsBookingLength() {
        def securityServiceControl = mockSecurityService()
        def cmd = createDeviationCommand(true)
        controller.confirm(cmd)
        assertEquals(new Period().plusHours(1), controller.modelAndView.model.createSeason.courts.get(0).bookingLength)
        securityServiceControl.verify()
    }

    void testConfirmSetsBookingLengthWithOtherLength() {
        def securityServiceControl = mockSecurityService()
        def cmd = createDeviationCommand(true)
        cmd.bookingLength = "00:45"
        controller.confirm(cmd)
        assertEquals(new Period().plusMinutes(45), controller.modelAndView.model.createSeason.courts.get(0).bookingLength)
        securityServiceControl.verify()
    }

    void testConfirmRightNumberOfOpenHours() {
        def securityServiceControl = mockSecurityService()
        def cmd = createDeviationCommand(true)
        controller.confirm(cmd)
        assertEquals(6, controller.modelAndView.model.createSeason.courts.get(0).openHoursPerWeekDay.size())
        securityServiceControl.verify()
    }

    void testConfirmRightWeekDaysIsSetOnCourt() {
        def securityServiceControl = mockSecurityService()
        def cmd = createDeviationCommand(true)
        controller.confirm(cmd)
        assertNotNull(controller.modelAndView.model.createSeason.courts.get(0).openHoursPerWeekDay[1])
        securityServiceControl.verify()
    }

    void testConfirmRightWeekDaysIsNotSetOnCourt() {
        def securityServiceControl = mockSecurityService()
        def cmd = createDeviationCommand(true)
        controller.confirm(cmd)
        assertNull(controller.modelAndView.model.createSeason.courts.get(0).openHoursPerWeekDay[7])
        securityServiceControl.verify()
    }

    void testConfirmRightOpenHoursIsSetOnCourt() {
        def securityServiceControl = mockSecurityService()
        def cmd = createDeviationCommand(true)
        controller.confirm(cmd)
        assertEquals(new LocalTime(10, 0),controller.modelAndView.model.createSeason.courts.get(0).openHoursPerWeekDay[1].opening)
        securityServiceControl.verify()
    }

    void testConfirmRightClosingHoursIsSetOnCourt() {
        def securityServiceControl = mockSecurityService()
        def cmd = createDeviationCommand(true)
        controller.confirm(cmd)
        assertEquals(new LocalTime(22, 0),controller.modelAndView.model.createSeason.courts.get(0).openHoursPerWeekDay[1].closing)
        securityServiceControl.verify()
    }

    /*
    //On hold for now
    void testConfirmHandles24hourBasedTime() {
        def cmd = createDeviationCommand(true)
        cmd.closingTimeHour = "24"
        controller.confirm(cmd)
    }
    */

    void testCreateSlotFilterFromSeasonDeviationForm() {
        def cmd = createDeviationCommand(true)
        SlotFilter filter = controller.createSlotFilter(cmd)

        assertNotNull(filter)
    }

    void testCreateSlotFilterCourtsArePopulated() {
        def cmd = createDeviationCommand(true)
        SlotFilter filter = controller.createSlotFilter(cmd)

        assertEquals(3, filter.courts.size())
    }

    void testCreateSlotFilterFromAndToArePopulated() {
        def cmd = createDeviationCommand(true)
        SlotFilter filter = controller.createSlotFilter(cmd)

        assertNotNull(filter.from)
        assertNotNull(filter.to)
    }

    void testCreateSlotFilterOnlyFreeSlotsIsFalse() {
        def cmd = createDeviationCommand(true)
        SlotFilter filter = controller.createSlotFilter(cmd)

        assertFalse(filter.onlyFreeSlots)
    }

    void testCreateSlotFilterWeekDaysArePopulated() {
        def cmd = createDeviationCommand(true)
        SlotFilter filter = controller.createSlotFilter(cmd)

        assertEquals([1,2,3,4,5,6], filter.onWeekDays)
    }

    void testCreateSlotFilterFromAndToTimeArePopulated() {
        def cmd = createDeviationCommand(true)
        SlotFilter filter = controller.createSlotFilter(cmd)

        assertNotNull(filter.toTime)
        assertNotNull(filter.fromTime)
    }

    SeasonDeviationCommand createDeviationCommand(def open = true) {
        SeasonDeviationCommand cmd = new SeasonDeviationCommand();
        cmd.fromDate = new Date()
        cmd.toDate = new Date() +10;
        cmd.timeBetween = "01:00"
        cmd.bookingLength = "01:00"
        cmd.fromTime = "10:00"
        cmd.toTime = "22:00"
        cmd.courtIds = [1,2,3] as List<Long>
        cmd.weekDays = [1,2,3,4,5,6] as List<Integer>
        cmd.open = open
        cmd.seasonId = season.id
        cmd.name = "test"

        return cmd
    }

    private mockSecurityService() {
        def serviceControl = mockFor(SecurityService)
        serviceControl.demand.getUserFacility { -> facility }
        controller.securityService = serviceControl.createMock()
        serviceControl
    }
}