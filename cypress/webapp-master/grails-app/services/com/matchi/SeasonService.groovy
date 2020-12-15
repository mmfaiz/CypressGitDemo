package com.matchi

import com.matchi.async.ScheduledTask
import com.matchi.season.CreateSeasonCommand
import com.matchi.season.UpdateSeasonCommand
import com.matchi.slots.SlotDeleteException
import com.matchi.slots.SlotDelta
import com.matchi.slots.SlotFilter
import grails.transaction.Transactional
import org.joda.time.DateTime
import org.joda.time.DateTimeConstants
import org.joda.time.LocalTime
import org.joda.time.Period

import java.text.SimpleDateFormat

class SeasonService {

    static transactional = false
    def facilityService
    def slotService
    def scheduledTaskService
    def messageSource
    def dateUtil

    static final List<Integer> WEEK_DAYS = [
                                DateTimeConstants.MONDAY,
                                DateTimeConstants.TUESDAY,
                                DateTimeConstants.WEDNESDAY,
                                DateTimeConstants.THURSDAY,
                                DateTimeConstants.FRIDAY,
                                DateTimeConstants.SATURDAY,
                                DateTimeConstants.SUNDAY ]

    Season getSeasonByDate(Date date) {
        getSeasonByDate(date, facilityService.getActiveFacility())
    }

    Season getSeasonByDate(Date date, Facility facility) {
        Season season = Season.withCriteria(uniqueResult: true) {
            eq("facility", facility)
            and {
                le("startTime", date)
                ge("endTime", date)
            }
        }

        if(!season) {
            return null
        }

        return season
    }

    def getAvailableSeasons(Facility facility) {
        return Season.withCriteria {
            eq("facility", facility)
            and {
                ge("endTime", new Date())
            }
        }
    }

    def getUpcomingSeasons(Facility facility, Season fromSeason) {
        return Season.withCriteria {
            eq("facility", facility)
            gt("endTime", new Date())

            if (fromSeason) {
                ne("id", fromSeason.id)
            }
        }
    }

    def getSeasons(Facility facility, String filter) {
        return Season.withCriteria {
            eq("facility", facility)

            if(filter) {
                or {
                    like("name", "%${filter}%")
                    like("description", "%${filter}%")
                }
            }
            order("startTime", "desc")
        }
    }

    /**
     * Since facility seasons are ordered by startTime, the one previous to the one used as param is the previous one
     * @param season
     */
    public Season getPreviousSeason(Season season) {
        def seasons = Season.withCriteria {
            eq("facility", season.facility)
            lt("startTime", season.startTime)
            order("startTime", "desc")
        }

        return seasons[0]
    }

    /**
     * Since facility seasons are ordered by startTime, the one previous to the one used as param is the previous one
     * @param season
     */
    public Season getNextSeason(Season season) {
        def seasons = Season.withCriteria {
            eq("facility", season.facility)
            gt("startTime", season.startTime)
            order("startTime", "asc")
        }

        return seasons[0]
    }

    @Transactional
    Season createSeason(CreateSeasonCommand cmd) {
        Season season = new Season()
        season.facility     = facilityService.getActiveFacility()
        season.name         = cmd.name
        season.description  = cmd.description
        season.startTime    = new DateTime(new SimpleDateFormat('yyyy-MM-dd').parse(cmd.startTime)).toDateMidnight().toDate()
        season.endTime      = new DateTime(new SimpleDateFormat('yyyy-MM-dd').parse(cmd.endTime)).withHourOfDay(23).withMinuteOfHour(59).withSecondOfMinute(59).toDate()
        season.save(failOnError: true)

        return season
    }

    def executeUpdateSeasonCourtHours(CreateSeasonCommand cmd, Season season,Map params, String taskName, Facility activeFacility) {
        scheduledTaskService.scheduleTask(taskName, season.id, activeFacility, null, Season, "CREATE_SEASON") {
            def slots = new CreateSeason()
            slots.name = cmd.name
            slots.startTime = new DateTime(cmd.startTime)
            slots.endTime = new DateTime(cmd.endTime)
            slots.courts = parseCourtHours(params, Facility.get(activeFacility?.id))
            updateSeasonCourtHours(season, slots.courts)
        }
    }

    @Transactional
    void updateSeason(Season season, UpdateSeasonCommand cmd, Boolean forceDeleteSlots, String taskName) {
        if(season == null || cmd == null || cmd.startDate == null || cmd.endDate == null) {
            throw new IllegalArgumentException()
        }

        Date newStartTime = dateUtil.beginningOfDay(cmd.startDate).toDate()
        Date oldStartTime = dateUtil.beginningOfDay(season.startTime).toDate()

        Date newEndTime = dateUtil.endOfDay(cmd.endDate).toDate()
        Date oldEndTime = dateUtil.endOfDay(season.endTime).toDate()
        def seasonId = season.id

        scheduledTaskService.scheduleTask(taskName, season.id, season.facility, null, Season) {
            def seasonToUpdate = Season.get(seasonId)
            seasonToUpdate.name = cmd.name
            seasonToUpdate.description = cmd.description
            seasonToUpdate.startTime = newStartTime
            seasonToUpdate.endTime = new DateTime(newEndTime).withTime(23, 59, 59, 0).toDate() // To avoid rounding

            if(!newStartTime.equals(oldStartTime)) {
                if(newStartTime.before(oldStartTime)) {
                    extendSeason(newStartTime, oldStartTime.minus(1), seasonToUpdate, forceDeleteSlots)
                } else {
                    shortenSeason(oldStartTime, newStartTime.minus(1), seasonToUpdate, forceDeleteSlots)
                }
            }

            if(!newEndTime.equals(oldEndTime)) {
                if(newEndTime.after(oldEndTime)) {
                    extendSeason(oldEndTime.plus(1), newEndTime, seasonToUpdate, forceDeleteSlots)
                } else {
                    shortenSeason(newEndTime.plus(1), oldEndTime, seasonToUpdate, forceDeleteSlots)
                }
            }

            seasonToUpdate.save()
        }
    }

    @Transactional
    Boolean deleteSeason(Season season) {
        if(season.hasBookings()) {
            throw new IllegalStateException("Cannot delete season with bookings")
            return false
        }

        Season.withTransaction {
            SeasonCourtOpeningHours.findAllBySeason(season).each {
                it.delete()
            }
            def slots = slotService.getSlots(new SlotFilter(courts: season.facility.courts,
                    from: new DateTime(season.startTime), to: new DateTime(season.endTime)))
            slotService.removeSlots(slots)
            SeasonDeviation.findAllBySeason(season)*.delete()
            season.delete()
        }

        return true
    }

    void extendSeason(Date intervalStart, Date intervalEnd, Season season, boolean forceDeleteSlots) {
        DateTime intervalStartDateTime = dateUtil.beginningOfDay(intervalStart)
        DateTime intervalEndDateTime = dateUtil.endOfDay(intervalEnd)

        def courtHours = SeasonCourtOpeningHours.withCriteria {
            eq("season", season)
            court {
                eq("archived", false)
            }
        }.groupBy {
            it.court
        }.collect { court, hours ->
            def createCourtSeason = new CreateCourtSeason(court: court,
                    timeBetween: hours[0].timeBetween, bookingLength: hours[0].bookingLength)
            hours.each {
                createCourtSeason.addOpenHours(it.weekDay,
                        new OpenHours(opening: it.opens, closing: it.closes))
            }
            createCourtSeason
        }

        List<Slot> newSlots = slotService.generateSlots(new CreateSeason(startTime: intervalStartDateTime,
                endTime: intervalEndDateTime, courts: courtHours))
        List<Slot> oldSlots = slotService.getSlots(new SlotFilter(courts: Court.available(season.facility).list(),
                from: intervalStartDateTime, to: intervalEndDateTime))
        def delta = new SlotDelta(newSlots, oldSlots)

        // Keeping this as a safety measure to avoid bookings to be deleted, and to avoid duplicate slots
        // Should never happen though
        if (!forceDeleteSlots && delta.rightOnly().find {it.slot.booking}) {
            season.discard()
            throw new SlotDeleteException("Unable to update season because it requires to remove existing bookings")
        }

        // Deleting existing slots that are not part of the new season schedule
        slotService.removeSlots(delta.rightOnly().collect { it.slot })

        // Creating slots that do not exist in old schedule, but in new
        slotService.createSlots(delta.leftOnly().collect { it.slot })
    }

    void shortenSeason(Date intervalStart, Date intervalEnd, Season season, boolean forceDeleteSlots) {
        DateTime intervalStartDateTime = dateUtil.beginningOfDay(intervalStart)
        DateTime intervalEndDateTime = dateUtil.endOfDay(intervalEnd)

        List<Slot> oldSlots = slotService.getSlots(new SlotFilter(courts: Court.available(season.facility).list(),
                from: intervalStartDateTime, to: intervalEndDateTime))

        // Keeping this as a safety measure to avoid bookings to be deleted
        if (!forceDeleteSlots && oldSlots.find { Slot slot -> slot.booking }) {
            season.discard()
            throw new SlotDeleteException("Unable to update season because it requires to remove existing bookings")
        }

        // Deleting existing slots that are not part of the new season schedule
        slotService.removeSlots(oldSlots)
    }

    /**
     * Updates the court open hours for a given season
     * @param season The actual season
     * @param courtHours
     */
    @Transactional
    def updateSeasonCourtHours(def season, def courtHours) {
        def oldCourtHours = SeasonCourtOpeningHours.withCriteria {
            eq("season", season)
            court {
                eq("archived", false)
            }
        }
        oldCourtHours.each { it.delete() }

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
                seasonCourtOpeningHours.save()
            }
        }

        def newSlots = slotService.generateSlots(new CreateSeason(startTime: new DateTime(season.startTime),
                endTime: new DateTime(season.endTime), courts: courtHours))
        def oldSlots = slotService.getSlots(new SlotFilter(courts: Court.available(season.facility).list(),
                from: new DateTime(season.startTime), to: new DateTime(season.endTime)))

        SlotDelta delta = new SlotDelta(newSlots, oldSlots)

        def slotToBeRemoved = delta.rightOnly().collect { it.slot }
        def slotsToBeCreated = delta.leftOnly().collect { it.slot }

        // Deviations
        def deviations = SeasonDeviation.findAllBySeasonAndOpen(season, false)

        def filteredSlots = []

        log.info("Season has ${deviations.size()} deviations that closes slots. Filtering.")
        deviations.each { def deviation ->
            slotsToBeCreated.each { slot ->
                if(deviation.isWithin(slot)) {
                    filteredSlots << slot
                }
            }
        }

        log.info("Found ${filteredSlots.size()} slots that was affected by a closing deviation.")
        slotsToBeCreated = slotsToBeCreated - filteredSlots

        slotService.removeSlots(slotToBeRemoved)
        slotService.createSlots(slotsToBeCreated)

    }

    def parseCourtHours(def params, Facility facility = facilityService.activeFacility) {
        def courtSeasonsList = new ArrayList()
        def courts = Court.available(facility).list()

        courts.each { Court court ->
            def createCourtSeason = new CreateCourtSeason()
            createCourtSeason.court = court

            LocalTime interval   = new LocalTime(params.get("_timeBetween/" + court.id))
            LocalTime length    = new LocalTime(params.get("_bookingLength/" + court.id))

            createCourtSeason.timeBetween = new Period().plusHours(interval.hourOfDay).plusMinutes(interval.minuteOfHour)
            createCourtSeason.bookingLength = new Period().plusHours(length.hourOfDay).plusMinutes(length.minuteOfHour)

            createCourtSeason = getCourtHours(params, createCourtSeason)

            courtSeasonsList.add(createCourtSeason)
        }

        return courtSeasonsList

    }

    def getCourtHours(def params, CreateCourtSeason createCourtSeason) {

        def courtId = createCourtSeason.court.id

        WEEK_DAYS.each { day ->

            OpenHours openHours = new OpenHours()
            openHours.opening = new LocalTime(params.get("_courts/" + courtId + "/" + day + "/1"))
            openHours.closing = new LocalTime(params.get("_courts/" + courtId + "/" + day + "/2"))

            createCourtSeason.addOpenHours(day, openHours)
        }

        return createCourtSeason
    }

    /**
     * Checks if court hours parameters are valid, meaning that the opening
     * @param params
     * @param facility
     * @return
     */
    boolean validateCourtHoursParameters(List<Court> courts, Map params, Facility facility = facilityService.activeFacility) {
        boolean result = true
        courts.each { Court court ->
            if(!result) return

            Long courtId = court.id

            WEEK_DAYS.each { Integer day ->
                if(!result) return

                LocalTime opening = new LocalTime(params.get("_courts/" + courtId + "/" + day + "/1"))
                LocalTime closing = new LocalTime(params.get("_courts/" + courtId + "/" + day + "/2"))

                if(opening.isAfter(closing)) {
                    result = false
                }
            }
        }

        result
    }

    def isSeasonOverlapping(Date startTime, Date endTime, Long seasonId = null) {
        def seasons = Season.withCriteria {
            eq("facility", facilityService.getActiveFacility())
            or {
                and {
                    le("startTime", startTime)
                    ge("endTime", startTime)
                }
                and {
                    le("startTime", endTime)
                    ge("endTime", endTime)
                }
                and {
                    gt("startTime", startTime)
                    lt("endTime", endTime)
                }
            }
            if (seasonId) {
                ne("id", seasonId)
            }
        }

        if(seasons.size() > 0) {
            return true
        }

        return false
    }

    List<SeasonDeviation> getSeasonDeviations(Season season) {
        return SeasonDeviation.findAllBySeason(season)
    }

    Boolean removeUnfinishedCreateSeasonTask(ScheduledTask task) {
        if (task.relatedDomainClass == "Season" && task.identifier == "CREATE_SEASON") {
            Season season = Season.get(task.domainIdentifier)
            if (season) {
                log.info("Deleting season ${season.id}")
                return deleteSeason(season)
            }
            log.info("Season don't exists ${task.domainIdentifier}")
            return true
        }
        return true
    }
}
