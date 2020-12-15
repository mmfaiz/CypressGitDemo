package com.matchi.facility

import com.matchi.*
import com.matchi.season.CreateSeasonCommand
import com.matchi.season.UpdateSeasonCommand
import com.matchi.slots.SlotDelta
import com.matchi.slots.SlotFilter
import org.joda.time.DateTime
import org.joda.time.DateTimeConstants

import javax.servlet.http.HttpServletResponse

class FacilitySeasonController extends GenericController {

    def seasonService
    def facilityService
    def slotService
    def bookingService
    def dateUtil

    def beforeInterceptor = [action: this.&checkConstraints, only: ["create", "save", "update"]]

    def index() {
        def facility = getUserFacility()
        def seasons = seasonService.getSeasons(facility, params.q)

        [facility: facility, seasons: seasons]
    }

    def create() {
        def facility = getUserFacility()

        def seasonInstance = new Season()
        seasonInstance.properties = params

        [facility: facility, seasonInstance: seasonInstance, form: initSeasonForm()]
    }

    def save(CreateSeasonCommand cmd) {
        if (cmd.hasErrors()) {
            render(view: 'create', model: [cmd: cmd, form: initSeasonForm(), params: params])
            return
        }

        if (params.findAll { it.key.startsWith("_bookingLength/") }.any { it.value == "00:00" }) {
            cmd.errors.reject("seasonDeviationCommand.bookingLength.validator.error", null, null)
            render(view: 'create', model: [cmd: cmd, form: initSeasonForm(), params: params])
            return
        }

        //Check that no created season is overlapping
        if (seasonService.isSeasonOverlapping(Date.parse("yyyy-MM-dd", cmd.startTime),
                Date.parse("yyyy-MM-dd", cmd.endTime))) {
            cmd.errors.rejectValue("startTime", "updateSeasonCommand.error.overlap")
            render(view: 'create', model: [cmd: cmd, form: initSeasonForm(), params: params])
            return
        }

        // Check start times to not start after closing times
        List<Court> courts = Court.available(getUserFacility()).list()
        if (!seasonService.validateCourtHoursParameters(courts, params)) {
            cmd.errors.rejectValue("startTime", "invalid.secondTimeLarger")
            render(view: 'create', model: [cmd: cmd, form: initSeasonForm(), params: params])
            return
        }

        def season = seasonService.createSeason(cmd)

        seasonService.executeUpdateSeasonCourtHours(cmd, season, params, message(code: "scheduledTask.createSeason.taskName"), facilityService.getActiveFacility())

        if (!season.hasErrors() && season.save(flush: true, failOnError: true)) {
            flash.message = message(code: "facilitySeason.save.success", args: [season.name])

            redirect(action: 'index')
            return
        }

        [cmd: cmd, season: season, form: initSeasonForm()]
    }

    def edit() {
        Season season = Season.findById(params.id)

        if (season) {
            assertFacilityAccessTo(season)

            UpdateSeasonCommand updateSeasonCommand = new UpdateSeasonCommand()
            updateSeasonCommand.id = season.id
            updateSeasonCommand.name = season.name
            updateSeasonCommand.description = season.description
            updateSeasonCommand.startDate = season.startTime
            updateSeasonCommand.endDate = season.endTime

            getEditViewModel(season, updateSeasonCommand)
        } else {
            flash.error = message(code: "facilitySeason.noSeason.error")
            redirect(action: 'index')
            return
        }
    }

    def update(UpdateSeasonCommand updateSeasonCommand) {

        Season season = Season.findById(updateSeasonCommand.id)

        if (season) {
            assertFacilityAccessTo(season)

            if (season.initializing) {
                flash.error = message(code: "facilitySeason.update.initError")
                redirect(action: "edit", id: season.id)
                return
            }

            updateSeasonCommand.startDate = dateUtil.beginningOfDay(updateSeasonCommand.startDate).toDate()
            updateSeasonCommand.endDate = dateUtil.endOfDay(updateSeasonCommand.endDate).toDate()

            if (updateSeasonCommand.startDate.after(updateSeasonCommand.endDate)) {
                updateSeasonCommand.errors.rejectValue("endDate", "invalid.datemismatch")
                render(view: 'edit', model: getEditViewModel(season, updateSeasonCommand))
                return
            }

            if (seasonService.isSeasonOverlapping(updateSeasonCommand.startDate,
                    updateSeasonCommand.endDate, season.id)) {
                updateSeasonCommand.errors.rejectValue("endDate", "updateSeasonCommand.error.overlap")
                render(view: 'edit', model: getEditViewModel(season, updateSeasonCommand))
                return
            }

            seasonService.updateSeason(season, updateSeasonCommand, false,
                    message(code: "scheduledTask.updateSeason.taskName"))

            flash.message = message(code: "facilitySeason.update.success", args: [updateSeasonCommand.name])
        }

        redirect(action: "index")
    }

    private initSeasonForm() {
        def facility = getUserFacility()

        def weekDays = [
                DateTimeConstants.MONDAY,
                DateTimeConstants.TUESDAY,
                DateTimeConstants.WEDNESDAY,
                DateTimeConstants.THURSDAY,
                DateTimeConstants.FRIDAY,
                DateTimeConstants.SATURDAY,
                DateTimeConstants.SUNDAY]
        def hours = ["00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12",
                     "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23"]
        def minutes = ["00", "05", "10", "15", "20", "25", "30", "35", "40", "45", "50", "55"]

        def seasonCriteria = Season.createCriteria()
        def seasons = seasonCriteria {
            eq("facility", facility)
            maxResults(1)
            order("endTime", "desc")
        }

        DateTime lastSeasonEndDate;

        if (!seasons.isEmpty()) {
            lastSeasonEndDate = new DateTime(seasons.first().endTime).plusDays(1)
        } else {
            lastSeasonEndDate = new DateTime()
        }

        Date nextStartDate = lastSeasonEndDate.toDate()
        Date nextEndDate = lastSeasonEndDate.plusMonths(3).toDate()

        def openingHour = facility.getOpeningHour(1) ?: 7
        def closingHour = facility.getClosingHour(1) ?: 22

        return [
                facility     : facility,
                openingHour  : openingHour,
                closingHour  : closingHour,
                weekDays     : weekDays,
                hours        : hours,
                minutes      : minutes,
                nextStartDate: nextStartDate,
                nextEndDate  : nextEndDate
        ]
    }

    def editOpenHours() {

        session.removeValue("newOpeningHours")

        Season season = Season.findById(params.id)
        if (season) {
            assertFacilityAccessTo(season)
        }

        def openingHours = SeasonCourtOpeningHours.withCriteria {
            eq("season", season)
            court {
                eq("archived", false)
            }
        }
        def openHoursPerCourtAndWeekDay = [:]

        openingHours.each { def openingHour ->
            openHoursPerCourtAndWeekDay.put("${openingHour.weekDay}_${openingHour.court.id}", openingHour)
        }

        render view: "openHours", model: [facility    : getUserFacility(), form: initSeasonForm(),
                                          openingHours: openHoursPerCourtAndWeekDay, season: season]
    }

    def confirmOpenHours() {
        Season season = Season.findById(params.id)
        if (season) {
            assertFacilityAccessTo(season)
        }

        def openingHours = SeasonCourtOpeningHours.withCriteria {
            eq("season", season)
            court {
                eq("archived", false)
            }
        }
        def openHoursPerCourtAndWeekDay = [:]
        def newOpenHoursPerCourtAndWeekDay = [:]

        openingHours.each { def openingHour ->
            openHoursPerCourtAndWeekDay.put("${openingHour.weekDay}_${openingHour.court.id}", openingHour)
        }

        def courtHours = session.getValue("newOpeningHours")

        if (!courtHours || params.form) {
            courtHours = seasonService.parseCourtHours(params);
        }

        courtHours.each { CreateCourtSeason courtSeason ->

            courtSeason.openHoursPerWeekDay.eachWithIndex {
                int weekDay = it.key
                OpenHours openHours = it.value

                SeasonCourtOpeningHours seasonCourtOpeningHours = new SeasonCourtOpeningHours()
                seasonCourtOpeningHours.season = season
                seasonCourtOpeningHours.court = courtSeason.court
                seasonCourtOpeningHours.weekDay = weekDay
                seasonCourtOpeningHours.opens = openHours.opening
                seasonCourtOpeningHours.closes = openHours.closing
                seasonCourtOpeningHours.bookingLength = courtSeason.bookingLength
                seasonCourtOpeningHours.timeBetween = courtSeason.timeBetween

                newOpenHoursPerCourtAndWeekDay.put("${seasonCourtOpeningHours.weekDay}_${seasonCourtOpeningHours.court.id}", seasonCourtOpeningHours)
            }
        }


        session.putValue("newOpeningHours", courtHours)

        def newSlots = slotService.generateSlots(new CreateSeason(startTime: new DateTime(season.startTime), endTime: new DateTime(season.endTime), courts: courtHours))
        def oldSlots = slotService.getSlots(new SlotFilter(
                courts: Court.available(getUserFacility()).list(),
                from: new DateTime(season.startTime), to: new DateTime(season.endTime)))

        SlotDelta delta = new SlotDelta(newSlots, oldSlots)

        def slotIds = delta.rightOnly().collect { it.slot.id }

        def existingBookings = (slotIds.size() > 0 ? bookingService.getBookingsBySlots(slotIds) : [])

        render view: "confirmOpenHours", model: [facility    : getUserFacility(), form: initSeasonForm(),
                                                 openingHours: openHoursPerCourtAndWeekDay, newOpeningHours: newOpenHoursPerCourtAndWeekDay,
                                                 season      : season, existingBookings: existingBookings]
    }

    def saveOpenHours() {
        Season season = Season.findById(params.id)
        if (season) {
            assertFacilityAccessTo(season)
        }

        def courtHours = session.getValue("newOpeningHours")

        if (!courtHours) {
            throw new IllegalStateException("No opening hours in session")
        }

        seasonService.updateSeasonCourtHours(season, courtHours)
        session.removeValue("newOpeningHours")

        render view: "receiptOpenHours", model: [season: season]
    }

    def openHoursChangeStatus() {
        Season season = Season.findById(params.id)
        if (season) {
            assertFacilityAccessTo(season)
        }

        def openingHours = SeasonCourtOpeningHours.withCriteria {
            eq("season", season)
            court {
                eq("archived", false)
            }
        }
        def openHoursPerCourtAndWeekDay = [:]

        openingHours.each { def openingHour ->
            openHoursPerCourtAndWeekDay.put("${openingHour.weekDay}_${openingHour.court.id}", openingHour)
        }

        def courtHours = seasonService.parseCourtHours(params);

        def newSlots = slotService.generateSlots(new CreateSeason(startTime: new DateTime(season.startTime), endTime: new DateTime(season.endTime), courts: courtHours))
        def oldSlots = slotService.getSlots(new SlotFilter(
                courts: Court.available(getUserFacility()).list(),
                from: new DateTime(season.startTime), to: new DateTime(season.endTime)))

        SlotDelta delta = new SlotDelta(newSlots, oldSlots)

        [delta: delta]
    }

    def delete() {
        Season season = Season.findById(params.id)
        if (season) {
            assertFacilityAccessTo(season)

            if (season.initializing) {
                flash.error = message(code: "facilitySeason.delete.initError")
                redirect(action: "edit", id: season.id)
                return
            }

            seasonService.deleteSeason(season)

            flash.message = message(code: 'default.deleted.message',
                    args: [message(code: 'season.label'), season])

            redirect(action: "index")
        } else {
            response.sendError HttpServletResponse.SC_NOT_FOUND
        }
    }

    protected boolean checkConstraints() {
        if (getUserFacility().anySeasonUpdating) {
            flash.error = message(code: "facilitySeason.updateNotAllowed")
            redirect(action: "index")
            return false
        }

        return true
    }

    private getEditViewModel(season, cmd) {
        def openingHours = SeasonCourtOpeningHours.findAllBySeason(season)
        def openHoursPerCourtAndWeekDay = [:]
        openingHours.each { def openingHour ->
            openHoursPerCourtAndWeekDay.put("${openingHour.weekDay}_${openingHour.court.id}", openingHour)
        }

        // Otherwise the check for hasBookings() might be incorrect later
        season = season.refresh()

        [season       : season, deviations: seasonService.getSeasonDeviations(season),
         seasonCommand: cmd, facility: getUserFacility(), form: initSeasonForm(),
         openingHours : openHoursPerCourtAndWeekDay]
    }
}
