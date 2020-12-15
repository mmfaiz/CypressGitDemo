package com.matchi.facility

import com.matchi.*
import com.matchi.async.ScheduledTask
import com.matchi.season.UpdateSeasonCommand
import com.matchi.slots.SlotDeleteException
import grails.test.MockUtils
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.junit.Before
import org.junit.Test

@TestFor(FacilitySeasonController)
@Mock([Season, Facility, User, UserRole, Role, SeasonCourtOpeningHours, ScheduledTask])
class FacilitySeasonControllerTests {

    Season season

    @Before
    void setUp() {
        MockUtils.mockLogging(FacilitySeasonController, true)

        User user = new User().save(validate: false)
        Facility facility = new Facility(id: 1)
        facility.addToUsers(user).save(validate: false)
        UserRole.create(user, new Role(authority: "ROLE_USER"))

        season = new Season(id: 1, name: "Test season", endTime: new Date() + 1, startTime: new Date(), description: "Test description", facility: facility).save(validate: false)

        def mockSecurityService = mockFor(SecurityService)
        controller.securityService = mockSecurityService.createMock()

        controller.dateUtil = new DateUtil()

        mockSecurityService.demand.getUserFacility(1..3) { -> return facility }
    }

    @Test
    void testUpdateSeasonNameIsSaved() {
        def seasonServiceControl = mockSeasonService()
        UpdateSeasonCommand command = new UpdateSeasonCommand(id: 1, name: "New name", endDate: new Date() + 1, startDate: new Date())
        controller.update(command)
        seasonServiceControl.verify()
    }

    @Test
    void testUpdateSeasonDescriptionIsSaved() {
        def seasonServiceControl = mockSeasonService()
        UpdateSeasonCommand command = new UpdateSeasonCommand(id: 1, name: "New name", description: "description-test", endDate: new Date() + 1, startDate: new Date())
        controller.update(command)
        seasonServiceControl.verify()
    }

    @Test
    void testUpdateSeasonRedirectToIndex() {
        def seasonServiceControl = mockSeasonService()
        UpdateSeasonCommand command = new UpdateSeasonCommand(id: 1, name: "New name", description: "description-test", endDate: new Date() + 1, startDate: new Date())
        controller.update(command)
        assert response.redirectUrl.endsWith("/facility/seasons/index")
        seasonServiceControl.verify()
    }

    @Test
    void testUpdateSeasonErrorOnEndDateBeforeStartDate() {
        def seasonServiceControl = mockFor(SeasonService)
        seasonServiceControl.demand.getSeasonDeviations(1) { def season -> return [] }
        controller.seasonService = seasonServiceControl.createMock()

        UpdateSeasonCommand command = new UpdateSeasonCommand(id: 1, name: "New name", description: "description-test", endDate: new Date() - 1, startDate: new Date() +1)
        controller.update(command)

        assert command.hasErrors()
        assert command.errors.getFieldError('endDate')
        assert command.errors.getFieldError('endDate').code == 'invalid.datemismatch'


        seasonServiceControl.verify()
    }

    @Test
    void testUpdateSeasonErrorOnOverlapping() {
        Date startDate = new Date() - 1
        Date endDate = new Date() + 1
        DateUtil dateUtil = new DateUtil()

        def seasonServiceControl = mockFor(SeasonService)
        seasonServiceControl.demand.isSeasonOverlapping(1) { Date start, Date end, Long id ->
            assert start.equals(dateUtil.beginningOfDay(startDate).toDate())
            assert end.equals(dateUtil.endOfDay(endDate).toDate())
            assert id == 1
            return true
        }

        seasonServiceControl.demand.getSeasonDeviations(1) { def season -> return [] }
        controller.seasonService = seasonServiceControl.createMock()

        UpdateSeasonCommand command = new UpdateSeasonCommand(id: 1, name: "New name", description: "description-test", endDate: endDate, startDate: startDate)
        controller.update(command)

        assert command.hasErrors()
        assert command.errors.getFieldError('endDate')
        assert command.errors.getFieldError('endDate').code == 'updateSeasonCommand.error.overlap'

        seasonServiceControl.verify()
    }

    private mockSeasonService() {
        def seasonServiceControl = mockFor(SeasonService)
        seasonServiceControl.demand.isSeasonOverlapping { d1, d2, id -> false }
        seasonServiceControl.demand.updateSeason { s, cmd, fds ->
            s.name = cmd.name
            s.description = cmd.description
            s.save()
        }
        controller.seasonService = seasonServiceControl.createMock()
        seasonServiceControl
    }
}
