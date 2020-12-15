package com.matchi.facility

import com.matchi.slots.SlotDelta
import com.matchi.slots.SlotFilter
import grails.validation.Validateable
import grails.validation.ValidationException
import org.joda.time.DateTime
import org.joda.time.LocalDate
import org.joda.time.LocalTime
import org.joda.time.Period
import org.joda.time.format.DateTimeFormat
import com.matchi.*

class FacilitySeasonDeviationController extends GenericController {
    private static String SESSION_KEY_FORM = "deviationForm"

    def bookingService
    def slotService
    def seasonDeviationService
    DateUtil dateUtil

    def create() {
        def facilityDeviationConfig = MatchiConfig.findByKey(MatchiConfigKey.DISABLE_DEVIATION)
        if (facilityDeviationConfig.isBlocked()) {
            render facilityDeviationConfig.isBlockedMessage()
        }

        Season season = Season.get(params.seasonId)
        if (season) {
            assertFacilityAccessTo(season)
        }

        SeasonDeviation seasonDeviation

        if(params.id) {
            seasonDeviation = SeasonDeviation.read(params.id)
        }

        session.removeAttribute(SESSION_KEY_FORM)

        if(!season) {
            flash.error = message(code: "facilitySeasonDeviation.create.error")
            redirect(controller: "facilitySeason", action: "index")
            return
        }

        // default form values
        def seasonDeviationForm = new SeasonDeviationCommand()
        seasonDeviationForm.seasonId = season.id

        def deviationSlots = []
        def existingSlots = []
        def delta = null

        if(seasonDeviation) {
            seasonDeviationForm.id = seasonDeviation.id
            seasonDeviationForm.fromDate = seasonDeviation.fromDate.toDate()
            seasonDeviationForm.toDate = seasonDeviation.toDate.toDate()
            seasonDeviationForm.name = seasonDeviation.name
            seasonDeviationForm.courtIds = seasonDeviation.courtIds.split(",").collect { Long.parseLong(it)}
            seasonDeviationForm.weekDays = seasonDeviation.weekDays.split(",").collect { Integer.parseInt(it)}
            seasonDeviationForm.fromTime = DateTimeFormat.forPattern("HH:mm").print(seasonDeviation.fromTime)
            seasonDeviationForm.toTime = DateTimeFormat.forPattern("HH:mm").print(seasonDeviation.toTime)
            seasonDeviationForm.open   = seasonDeviation.open
            seasonDeviationForm.timeBetween = dateUtil.format(seasonDeviation.timeBetween)
            seasonDeviationForm.bookingLength   = dateUtil.format(seasonDeviation.bookingLength)

            // slot list
            deviationSlots = slotService.generateSlots(toSeasonDeviation(seasonDeviationForm).toCreateSeasonCommand())
            existingSlots  = slotService.getSlots(createSlotFilter(seasonDeviationForm))
            delta = new SlotDelta(deviationSlots, existingSlots)

        } else {
            seasonDeviationForm.fromDate = season.startTime
            seasonDeviationForm.toDate = season.endTime
            seasonDeviationForm.name = ""
            seasonDeviationForm.courtIds = Court.available(season.facility) {projections {property("id")}}
            seasonDeviationForm.weekDays = (1..7).collect { it }
            seasonDeviationForm.fromTime = season.facility.getOpeningLocalTime(1)?.toString("HH:mm") ?: "07:00"
            seasonDeviationForm.toTime = season.facility.getClosingLocalTime(1)?.toString("HH:mm") ?: "22:00"
            seasonDeviationForm.timeBetween = "00:00"
            seasonDeviationForm.bookingLength   = "01:00"
        }

        render(view: "create", model: [form:seasonDeviationForm,
                minDate: new DateTime(season.startTime),
                maxDate: new DateTime(season.endTime),
                courts: Court.available(season.facility).list(),
                delta: delta
        ])
    }

    def confirm(SeasonDeviationCommand seasonDeviationForm) {
        def facilityDeviationConfig = MatchiConfig.findByKey(MatchiConfigKey.DISABLE_DEVIATION)
        if (facilityDeviationConfig.isBlocked()) {
            render facilityDeviationConfig.isBlockedMessage()
        }

        if (!params.seasonId) {
            seasonDeviationForm = session.getValue(SESSION_KEY_FORM)
        }

        if(seasonDeviationForm.toDate.before(seasonDeviationForm.fromDate)) {
            seasonDeviationForm.errors.reject("toDate", "before.startdate")
        }

        Season season = Season.findById(seasonDeviationForm.seasonId)
        if (season) {
            assertFacilityAccessTo(season)
        }

        if(!seasonDeviationForm.validate()) {
            render(view: "create", model: [form:seasonDeviationForm,
                    minDate: new DateTime(season.startTime),
                    maxDate: new DateTime(season.endTime),
                    courts: Court.available(season.facility).list()])
        } else {
            session.putValue(SESSION_KEY_FORM, seasonDeviationForm)
            def deviation = createDeviation(seasonDeviationForm)
            def newSlots = slotService.generateSlots(toSeasonDeviation(seasonDeviationForm).toCreateSeasonCommand())
            def oldSlots = slotService.getSlots(createSlotFilter(seasonDeviationForm))

            def delta = new SlotDelta(newSlots, oldSlots)
            def model = [form: seasonDeviationForm, createSeason: deviation, delta: delta]
            if (deviation.open) {
                model.slots = delta.leftOnly()
                model.overlaps = delta.rightOverlaps(delta.leftOnly())
                if (model.slots && model.overlaps) {
                    model.tableSlots = model.slots.findAll {
                        model.overlaps[it.slot]
                    }
                }
            } else {
                model.slots = delta.right
                model.tableSlots = model.slots.findAll { it.slot.booking }
            }

            render(view: "confirm", model: model)
        }

    }

    def applyDeviations() {
        SeasonDeviationCommand seasonDeviationCommand = (SeasonDeviationCommand) session.getValue(SESSION_KEY_FORM)

        if(!seasonDeviationCommand) {
            redirect(controller: "facilitySeason", view: "index")
        } else {
            log.info("Applying deviations on ${seasonDeviationCommand.courtIds.size()} courts")

            try {
                seasonDeviationService.saveAndApply(toSeasonDeviation(seasonDeviationCommand))
            } catch (ValidationException e) {
                flash.error = message(code: 'facilitySeasonDeviation.applyDeviations.error')
                redirect(controller: "facilitySeason", action: "index")
                return
            }

            flash.message = message(code: "facilitySeasonDeviation.applyDeviations.success")

            redirect(controller: "facilitySeason", action: "edit", id:  seasonDeviationCommand.seasonId);
        }
    }

    def delete() {
        SeasonDeviation seasonDeviation = SeasonDeviation.findById(params.id)
        if (seasonDeviation) {
            assertFacilityAccessTo(seasonDeviation.season)
        }
        def seasonId = seasonDeviation.season.id

        if (seasonDeviation) {
            seasonDeviationService.remove(seasonDeviation)
        }
        redirect(controller: "facilitySeason", action: "edit", id: seasonId)
    }

    protected SeasonDeviation toSeasonDeviation(SeasonDeviationCommand seasonDeviationCommand) {

        SeasonDeviation seasonDeviation = new SeasonDeviation()
        if(seasonDeviationCommand.id) {
            seasonDeviation = SeasonDeviation.findById(seasonDeviationCommand.id)
        }

        seasonDeviation.season = Season.read(seasonDeviationCommand.seasonId)
        seasonDeviation.fromDate = new LocalDate(seasonDeviationCommand.fromDate)
        seasonDeviation.toDate   = new LocalDate(seasonDeviationCommand.toDate)
        seasonDeviation.fromTime = new LocalTime(seasonDeviationCommand.fromTime)
        seasonDeviation.toTime   = new LocalTime(seasonDeviationCommand.toTime)
        seasonDeviation.weekDays = seasonDeviationCommand.weekDays.join(",")
        seasonDeviation.courtIds = seasonDeviationCommand.courtIds.join(",")
        seasonDeviation.open     = seasonDeviationCommand.open
        seasonDeviation.name     = seasonDeviationCommand.name

        LocalTime bookingLength = new LocalTime(seasonDeviationCommand.bookingLength)
        LocalTime timeBetween   = new LocalTime(seasonDeviationCommand.timeBetween)

        seasonDeviation.bookingLength = new Period(bookingLength.getHourOfDay(), bookingLength.getMinuteOfHour(), 0, 0)
        seasonDeviation.timeBetween   = new Period(timeBetween.getHourOfDay(), timeBetween.getMinuteOfHour(), 0, 0)

        return seasonDeviation
    }

    protected SlotFilter createSlotFilter(SeasonDeviationCommand cmd) {
        SlotFilter slotFilter = new SlotFilter()

        slotFilter.from = new DateTime(cmd.fromDate)
        slotFilter.to = new DateTime(cmd.toDate).plusDays(1)
        slotFilter.courts = cmd.courtIds.collect { Court.read(it) }
        slotFilter.onWeekDays = cmd.weekDays
        slotFilter.fromTime = new LocalTime(cmd.fromTime)
        slotFilter.toTime   = new LocalTime(cmd.toTime)

        return slotFilter
    }

    protected CreateSeasonDeviation createDeviation(SeasonDeviationCommand cmd) {
        CreateSeasonDeviation seasonDeviation = new CreateSeasonDeviation()
        seasonDeviation.name      = cmd.name
        seasonDeviation.startTime = new DateTime(cmd.fromDate)
        seasonDeviation.endTime   = new DateTime(cmd.toDate)
        seasonDeviation.open      = cmd.open
        seasonDeviation.weekDays  = cmd.weekDays

        cmd.courtIds.each {
            def court = Court.get(it)
            def courtSeason = new CreateCourtSeason()
            def timeBetween = new LocalTime(cmd.timeBetween)
            def bookingLength = new LocalTime(cmd.bookingLength)

            courtSeason.timeBetween   = new Period().plusHours(timeBetween.hourOfDay).plusMinutes(timeBetween.minuteOfHour)
            courtSeason.bookingLength = new Period().plusHours(bookingLength.hourOfDay).plusMinutes(bookingLength.minuteOfHour)
            courtSeason.court = court
            cmd.weekDays.each { def weekDay ->

                courtSeason.addOpenHours(weekDay, new OpenHours(
                        opening: new LocalTime(cmd.fromTime),
                        closing: new LocalTime(cmd.toTime),
                ))
            }

            seasonDeviation.courts << courtSeason
        }

        return seasonDeviation
    }

}

@Validateable(nullable = true)
class SeasonDeviationCommand implements Serializable {
    Long id
    Long seasonId
    String name
    Date fromDate
    Date toDate
    String fromTime
    String toTime
    List<Integer> weekDays = [] as List<Integer>
    String bookingLength
    String timeBetween
    List<Long> courtIds = [] as List<Long>
    boolean open = true

    static constraints = {
        id(nullable:  true)
        seasonId(nullable: false)
        name(nullable: false, blank: false)
        fromDate(nullable: false)
        toDate(nullable: false)
        toTime(nullable: false)
        fromTime(nullable: false)
        courtIds(nullable: false, empty: false)
        weekDays(nullable: false, empty: false)
        bookingLength(nullable: false, empty: false, validator: {it != "00:00"})

        toTime(validator: {closing, obj ->
            def opening = obj.properties['fromTime']
            true
            //new LocalTime(closing).isAfter(new LocalTime(opening)) ? true : ['invalid.length']
        })
    }

}
