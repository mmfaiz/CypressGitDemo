package com.matchi

import com.matchi.async.ScheduledTaskService
import com.matchi.season.CreateSeasonCommand
import com.matchi.season.UpdateSeasonCommand
import com.matchi.slots.SlotFilter
import grails.test.GrailsMock
import grails.test.MockUtils
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.domain.DomainClassUnitTestMixin
import org.joda.time.DateTime
import org.junit.After
import org.junit.Before
import org.junit.Test

@TestFor(SeasonService)
@TestMixin(DomainClassUnitTestMixin)
@Mock([Court, Season, Facility, SeasonCourtOpeningHours, SeasonDeviation])
class SeasonServiceTests {

    CreateSeasonCommand createSeasonCommand
    UpdateSeasonCommand updateSeasonCommand
    Facility facility
    Availability availability
    def mockScheduledTaskService

    @Before
    public void setUp() {
        MockUtils.mockLogging(SeasonService, true)

        facility = new Facility(name: "Test", shortname: "test", lat: 1, lng: 2, email: "facility@matchi.se")
        facility.save(validate: false)

        service.dateUtil = new DateUtil()

        createSeasonCommand = getSeasonCommandCreate()
        updateSeasonCommand = getSeasonCommandUpdate()

        mockScheduledTaskService= mockFor(ScheduledTaskService)
        service.scheduledTaskService = mockScheduledTaskService.createMock()

        mockScheduledTaskService.demand.scheduleTask(0..10) { String name, Long domainIdentifier, Facility facility, String successMessage,
                                                                  Class domainClass, String identifier, Closure codeToExecute ->
            codeToExecute()
        }

        mockScheduledTaskService.demand.scheduleTask(0..10) { String name, Long domainIdentifier, Facility facility, String successMessage,
                                                                  Class domainClass, Closure codeToExecute ->
            codeToExecute()
        }
    }

    @After
    public void tearDown() {

    }

    @Test
    public void testCreateSeasonOK() {
        def season = getSeason()

        def mockFacilityService = mockFor(FacilityService)
        service.facilityService = mockFacilityService.createMock()
        mockFacilityService.demand.getActiveFacility(1..2) { ->
            return facility
        }

        def mockSlotServiceControl = mockFor(SlotService)
        service.slotService = mockSlotServiceControl.createMock()
        mockSlotServiceControl.demand.generateSlots(1..1) { CreateSeason cmd -> }
        mockSlotServiceControl.demand.getSlots(1..2) { SlotFilter cmd -> return [] }
        mockSlotServiceControl.demand.removeSlots(1..1) { def slots -> }
        mockSlotServiceControl.demand.createSlots(1..1) { def slots -> }

        Season createdSeason = service.createSeason(createSeasonCommand)

        assert season.name == createdSeason.name
        assert new DateTime().minusDays(10).toDateMidnight().toDate() == createdSeason.startTime
        assert new DateTime().withHourOfDay(23).plusDays(10).hourOfDay == createdSeason.endTime.getHours()
    }

    @Test
    public void testGetCurrentSeasonIfNoneActiveReturnsEmptyList() {
        def mockFacilityService = mockFor(FacilityService)
        service.facilityService = mockFacilityService.createMock()
        mockFacilityService.demand.getActiveFacility(1..3) { ->
            return facility
        }

        def mockSlotServiceControl = mockFor(SlotService)
        service.slotService = mockSlotServiceControl.createMock()
        mockSlotServiceControl.demand.generateSlots(1..1) { CreateSeason cmd -> }
        mockSlotServiceControl.demand.getSlots(1..2) { SlotFilter cmd -> return [] }
        mockSlotServiceControl.demand.removeSlots(1..1) { def slots -> }
        mockSlotServiceControl.demand.createSlots(1..1) { def slots -> }


        service.createSeason(createSeasonCommand)
        def dateBeforeSeason = new DateTime().minusDays(30).toDate()

        assert service.getSeasonByDate(dateBeforeSeason) == null
    }

    @Test
    public void testGetCurrentSeasonIfActiveReturnSeason() {
        def mockFacilityService = mockFor(FacilityService)
        service.facilityService = mockFacilityService.createMock()
        mockFacilityService.demand.getActiveFacility(1..3) { ->
            return facility
        }

        def mockSlotServiceControl = mockFor(SlotService)
        service.slotService = mockSlotServiceControl.createMock()
        mockSlotServiceControl.demand.generateSlots(1..1) { CreateSeason cmd -> }
        mockSlotServiceControl.demand.getSlots(1..2) { SlotFilter cmd -> return [] }
        mockSlotServiceControl.demand.removeSlots(1..1) { def slots -> }
        mockSlotServiceControl.demand.createSlots(1..1) { def slots -> }


        def season = service.createSeason(createSeasonCommand)
        def dateInSeason = new DateTime().toDate()

        def fetchedSeason = service.getSeasonByDate(dateInSeason)

        assert fetchedSeason != null && season.name == fetchedSeason.name
    }

    @Test
    public void testGetAvailableSeasonsReturnsCorrectSeasons() {
        def mockFacilityService = mockFor(FacilityService)
        service.facilityService = mockFacilityService.createMock()
        mockFacilityService.demand.getActiveFacility(2..4) { ->
            return facility
        }

        def mockSlotServiceControl = mockFor(SlotService)
        service.slotService = mockSlotServiceControl.createMock()
        mockSlotServiceControl.demand.generateSlots(1..1) { CreateSeason cmd -> }
        mockSlotServiceControl.demand.getSlots(1..1) { SlotFilter cmd -> return [] }
        mockSlotServiceControl.demand.removeSlots(1..1) { def slots -> }
        mockSlotServiceControl.demand.createSlots(1..1) { def slots -> }

        service.createSeason(createSeasonCommand)

        createSeasonCommand.startTime = new DateTime().plusDays(11).toString("yyyy-MM-dd")
        createSeasonCommand.endTime = new DateTime().plusDays(21).toString("yyyy-MM-dd")

        mockSlotServiceControl.demand.generateSlots(1..1) { CreateSeason cmd -> }
        mockSlotServiceControl.demand.getSlots(1..1) { SlotFilter cmd -> return [] }
        mockSlotServiceControl.demand.removeSlots(1..1) { def slots -> }
        mockSlotServiceControl.demand.createSlots(1..1) { def slots -> }
        service.createSeason(createSeasonCommand)

        def seasons = service.getAvailableSeasons(facility)

        assert seasons.size() == 2
    }

    @Test
    public void testNextSeasonReturnsCorrectSeason() {
        def mockFacilityService = mockFor(FacilityService)
        service.facilityService = mockFacilityService.createMock()
        mockFacilityService.demand.getActiveFacility(2..4) { ->
            return facility
        }

        def mockSlotServiceControl = mockFor(SlotService)
        service.slotService = mockSlotServiceControl.createMock()

        mockSlotServiceControl.demand.generateSlots(1..1) { CreateSeason cmd -> }
        mockSlotServiceControl.demand.getSlots(1..1) { SlotFilter cmd -> return [] }
        mockSlotServiceControl.demand.removeSlots(1..1) { def slots -> }
        mockSlotServiceControl.demand.createSlots(1..1) { def slots -> }

        Season season1 = service.createSeason(createSeasonCommand)

        mockSlotServiceControl.demand.generateSlots(1..1) { CreateSeason cmd -> }
        mockSlotServiceControl.demand.getSlots(1..1) { SlotFilter cmd -> return [] }
        mockSlotServiceControl.demand.removeSlots(1..1) { def slots -> }
        mockSlotServiceControl.demand.createSlots(1..1) { def slots -> }
        createSeasonCommand.name = "SEASON 2"
        createSeasonCommand.startTime = new DateTime().plusDays(11).toString("yyyy-MM-dd")
        createSeasonCommand.endTime = new DateTime().plusDays(21).toString("yyyy-MM-dd")
        Season season2 = service.createSeason(createSeasonCommand)

        def nextSeason = service.getNextSeason(season1)

        assert nextSeason != null
        assert nextSeason.name == season2.name
    }

    @Test
    public void testPreviousSeasonReturnsCorrectSeason() {
        def mockFacilityService = mockFor(FacilityService)
        service.facilityService = mockFacilityService.createMock()
        mockFacilityService.demand.getActiveFacility(2..4) { ->
            return facility
        }

        def mockSlotServiceControl = mockFor(SlotService)
        service.slotService = mockSlotServiceControl.createMock()

        mockSlotServiceControl.demand.generateSlots(1..1) { CreateSeason cmd -> }
        mockSlotServiceControl.demand.getSlots(1..1) { SlotFilter cmd -> return [] }
        mockSlotServiceControl.demand.removeSlots(1..1) { def slots -> }
        mockSlotServiceControl.demand.createSlots(1..1) { def slots -> }

        Season season1 = service.createSeason(createSeasonCommand)

        createSeasonCommand.name = "SEASON 2"
        createSeasonCommand.startTime = new DateTime().plusDays(11).toString("yyyy-MM-dd")
        createSeasonCommand.endTime = new DateTime().plusDays(21).toString("yyyy-MM-dd")

        mockSlotServiceControl.demand.generateSlots(1..1) { CreateSeason cmd -> }
        mockSlotServiceControl.demand.getSlots(1..1) { SlotFilter cmd -> return [] }
        mockSlotServiceControl.demand.removeSlots(1..1) { def slots -> }
        mockSlotServiceControl.demand.createSlots(1..1) { def slots -> }
        Season season2 = service.createSeason(createSeasonCommand)

        def prevSeason = service.getPreviousSeason(season2)

        assert prevSeason != null
        assert prevSeason.name == getSeasonCommandCreate().name
    }

    @Test
    void testOverLappingSeasons() {
        def season1 = getSeason("s1", new DateTime("2018-03-15").toDate(), new DateTime("2018-03-20").toDate())
        def mockFacilityService = mockFor(FacilityService)
        service.facilityService = mockFacilityService.createMock()
        mockFacilityService.demand.getActiveFacility(1..9) { ->
            return facility
        }

        assert service.isSeasonOverlapping(new DateTime("2018-03-16").toDate(), new DateTime("2018-03-18").toDate())
        assert service.isSeasonOverlapping(new DateTime("2018-03-13").toDate(), new DateTime("2018-03-16").toDate())
        assert service.isSeasonOverlapping(new DateTime("2018-03-13").toDate(), new DateTime("2018-03-15").toDate())
        assert service.isSeasonOverlapping(new DateTime("2018-03-16").toDate(), new DateTime("2018-03-22").toDate())
        assert service.isSeasonOverlapping(new DateTime("2018-03-20").toDate(), new DateTime("2018-03-24").toDate())
        assert service.isSeasonOverlapping(new DateTime("2018-03-15").toDate(), new DateTime("2018-03-20").toDate())
        assert service.isSeasonOverlapping(new DateTime("2018-03-12").toDate(), new DateTime("2018-03-25").toDate())

        assert !service.isSeasonOverlapping(new DateTime("2018-03-10").toDate(), new DateTime("2018-03-14").toDate())
        assert !service.isSeasonOverlapping(new DateTime("2018-03-22").toDate(), new DateTime("2018-03-26").toDate())
    }

    @Test
    void testGetUpcomingSeasons() {
        def season1 = getSeason("s1", new Date() - 20, new Date() - 10)
        def season2 = getSeason("s2", new Date() - 10, new Date() + 10)
        def season3 = getSeason("s3", new Date() + 10, new Date() + 20)

        def result = service.getUpcomingSeasons(facility, null)
        assert 2 == result.size()
        assert result.contains(season2)
        assert result.contains(season3)

        result = service.getUpcomingSeasons(facility, season1)
        assert 2 == result.size()
        assert result.contains(season2)
        assert result.contains(season3)

        result = service.getUpcomingSeasons(facility, season2)
        assert 1 == result.size()
        assert result.contains(season3)
    }

    @Test
    void testUpdateSeasonStartDateToLater() {
        Season season1 = getSeason("s1", new Date(), new Date() + 100)
        Date originalSeasonStartDate = season1.startTime

        GrailsMock mockFacilityService = mockFor(FacilityService)
        GrailsMock mockSlotService = mockFor(SlotService)

        mockSlotService.demand.getSlots(1) { filter -> }
        mockSlotService.demand.removeSlots(1) { slotsToRemove -> }

        DateUtil dateUtil = new DateUtil()
        service.facilityService = mockFacilityService.createMock()
        service.slotService = mockSlotService.createMock()

        updateSeasonCommand.startDate = originalSeasonStartDate.plus(20)
        updateSeasonCommand.endDate = season1.endTime
        service.updateSeason(season1, updateSeasonCommand, true, "task")

        assert season1.startTime.equals(dateUtil.beginningOfDay(updateSeasonCommand.startDate).toDate())
        mockFacilityService.verify()
        mockSlotService.verify()
    }

    @Test
    void testLetSeasonEndTimeRemainSame() {
        Season season1 = getSeason("s1", new Date(), new Date().plus(100))
        Date originalSeasonEndDate = season1.endTime

        GrailsMock mockFacilityService = mockFor(FacilityService)
        GrailsMock mockSlotService = mockFor(SlotService)

        service.facilityService = mockFacilityService.createMock()
        service.slotService = mockSlotService.createMock()

        DateUtil dateUtil = new DateUtil()
        updateSeasonCommand.startDate = season1.startTime
        updateSeasonCommand.endDate = dateUtil.endOfDay(originalSeasonEndDate).toDate()
        service.updateSeason(season1, updateSeasonCommand, true, "task")

        assert season1.endTime.equals(new DateTime(updateSeasonCommand.endDate).withMillisOfSecond(0).toDate())
        mockFacilityService.verify()
        mockSlotService.verify()
    }

    @Test
    void testUpdateSeasonEndDateToBefore() {
        Season season1 = getSeason("s1", new Date(), new Date() + 100)
        Date originalSeasonEndDate = season1.endTime

        GrailsMock mockFacilityService = mockFor(FacilityService)
        GrailsMock mockSlotService = mockFor(SlotService)

        service.facilityService = mockFacilityService.createMock()
        service.slotService = mockSlotService.createMock()

        mockSlotService.demand.getSlots(1) { filter -> }
        mockSlotService.demand.removeSlots(1) { slotsToRemove -> }

        DateUtil dateUtil = new DateUtil()
        updateSeasonCommand.startDate = season1.startTime
        updateSeasonCommand.endDate = originalSeasonEndDate.minus(20)
        service.updateSeason(season1, updateSeasonCommand, true, "task")

        assert season1.endTime.equals(dateUtil.endOfDay(updateSeasonCommand.endDate).withMillisOfSecond(0).toDate())
        mockFacilityService.verify()
        mockSlotService.verify()
    }

    @Test
    void testUpdateSeasonEndDateToLater() {
        Date startDate = new Date()
        Date endDate = new Date() + 5
        Season season1 = getSeason("s1", startDate, endDate)

        def newSlot = new Slot()
        newSlot.startTime = endDate + 1
        newSlot.endTime = new DateTime(newSlot.startTime).plusHours(1).toDate()

        GrailsMock mockFacilityService = mockFor(FacilityService)
        GrailsMock mockSlotService = mockFor(SlotService)

        service.facilityService = mockFacilityService.createMock()
        service.slotService = mockSlotService.createMock()

        mockSlotService.demand.generateSlots(1..1) { def cs ->
            return [newSlot]
        }

        mockSlotService.demand.getSlots(1..1) { def f ->
            return []
        }

        mockSlotService.demand.removeSlots(1..1) { def s ->
            assert s.size() == 0
        }

        mockSlotService.demand.createSlots(1..1) { List s ->
            assert s.size() == 1 && s.contains(newSlot)
        }

        updateSeasonCommand.startDate = startDate
        updateSeasonCommand.endDate = endDate.plus(5)
        service.updateSeason(season1, updateSeasonCommand, true, "task")

        mockFacilityService.verify()
        mockSlotService.verify()
    }

    void testValidateCourtHoursParameters() {
        Long courtId = 1L
        Map params = [:]

        def mockCourt = mockFor(Court)

        mockCourt.demand.getId(1) { ->
            return courtId
        }

        service.WEEK_DAYS.each { Integer day ->
            params.put("_courts/" + courtId + "/" + day + "/1", "08:00:00")
            params.put("_courts/" + courtId + "/" + day + "/2", "22:00:00")
        }

        assert service.validateCourtHoursParameters([mockCourt.createMock()] as List<Court>, params, facility)

        mockCourt.verify()
    }

    void testValidateCourtHoursParametersShouldFail() {
        Long courtId = 1L
        Map params = [:]

        def mockCourt = mockFor(Court)

        mockCourt.demand.getId(1) { ->
            return courtId
        }

        service.WEEK_DAYS.each { Integer day ->
            params.put("_courts/" + courtId + "/" + day + "/1", "22:00:00")
            params.put("_courts/" + courtId + "/" + day + "/2", "07:00:00")
        }

        assert !service.validateCourtHoursParameters([mockCourt.createMock()] as List<Court>, params, facility)

        mockCourt.verify()
    }

    void testValidateCourtHoursParametersShouldPassIfSame() {
        Long courtId = 1L
        Map params = [:]

        def mockCourt = mockFor(Court)

        mockCourt.demand.getId(1) { ->
            return courtId
        }

        service.WEEK_DAYS.each { Integer day ->
            params.put("_courts/" + courtId + "/" + day + "/1", "07:00:00")
            params.put("_courts/" + courtId + "/" + day + "/2", "07:00:00")
        }

        assert service.validateCourtHoursParameters([mockCourt.createMock()] as List<Court>, params, facility)

        mockCourt.verify()
    }

    def getSeasonCommandCreate() {
        CreateSeasonCommand cmd = new CreateSeasonCommand()
        cmd.name        = "SEASON 1"
        cmd.description = "Lorem ipsum"
        cmd.startTime   = new DateTime().minusDays(10).toString("yyyy-MM-dd")
        cmd.endTime     = new DateTime().plusDays(10).toString("yyyy-MM-dd")

        return cmd
    }

    def getSeasonCommandUpdate() {
        UpdateSeasonCommand cmd = new UpdateSeasonCommand()
        cmd.name        = "SEASON 1"
        cmd.description = "Lorem ipsum"
        cmd.startDate   = new DateTime().minusDays(10).toDate()
        cmd.endDate     = new DateTime().plusDays(10).toDate()

        return cmd
    }

    def getSeason(String name = null, Date start = null, Date end = null) {
        Season season = new Season()
        season.facility    = facility
        season.name        = name ?: "SEASON 1"
        season.description = "Lorem ipsum"
        season.startTime   = start ?: new DateTime().minusDays(10).toDate()
        season.endTime     = end ?: new DateTime().plusDays(10).toDate()
        season.save(failOnError: true)

        return season
    }
}
