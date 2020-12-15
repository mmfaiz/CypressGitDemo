package com.matchi

import org.joda.time.LocalDate
import org.joda.time.LocalTime
import org.joda.time.Period
import org.joda.time.DateTime
import com.matchi.slots.SlotFilter
import com.matchi.schedule.TimeSpan
import org.joda.time.Interval

class SeasonDeviation implements Serializable {

    static belongsTo = [Slot, Season]
    static hasMany = [slots : Slot]

    String name

    Season season

    LocalDate fromDate
    LocalDate toDate

    LocalTime fromTime
    LocalTime toTime

    boolean open

    String weekDays
    String courtIds

    Period timeBetween
    Period bookingLength

    static constraints = {
        fromDate(nullable: true)
        toDate(nullable: true)
        fromTime(nullable: true)
        toTime(nullable: true)
        weekDays(nullable: true)
        courtIds(nullable: true)
    }

    static mapping = {
        slots joinTable: [name: "season_deviation_slot", key: 'season_deviation_id' ]
    }

    def toCreateSeasonCommand() {
        CreateSeasonDeviation seasonDeviation = new CreateSeasonDeviation()
        seasonDeviation.startTime = new DateTime(fromDate.toDate())
        seasonDeviation.endTime   = new DateTime(toDate.toDate())
        seasonDeviation.open      = open
        seasonDeviation.weekDays  = weekDays.split(",").collect {Integer.parseInt(it)}

        courtIds.split(",").collect { Long.parseLong(it)} .each {
            def court = Court.get(it)
            def courtSeason = new CreateCourtSeason()


            courtSeason.timeBetween   = timeBetween
            courtSeason.bookingLength = bookingLength
            courtSeason.court = court
            weekDays.split(",").collect {Integer.parseInt(it)} .each { def weekDay ->

                courtSeason.addOpenHours(weekDay, new OpenHours(
                        opening: new LocalTime(fromTime),
                        closing: new LocalTime(toTime),
                ))
            }

            seasonDeviation.courts << courtSeason
        }

        return seasonDeviation
    }

    def toSlotFilter() {
        SlotFilter slotFilter = new SlotFilter()

        slotFilter.from = this.fromDate.toDateTime(LocalTime.MIDNIGHT)
        slotFilter.to = this.toDate.toDateTime(LocalTime.MIDNIGHT.minusMillis(1))
        slotFilter.courts = courtIdList().collect { Court.get(it) }
        slotFilter.onWeekDays = weekDaysList()
        slotFilter.fromTime = this.fromTime
        slotFilter.toTime   = this.toTime

        return slotFilter
    }

    def isWithin(Slot slot) {

        // date
        if(fromDate && toDate) {
            def timepan = new TimeSpan(fromDate.toDateMidnight().toDateTime(), toDate.toDateMidnight().plusDays(1).toDateTime())
            if(!slot.timeSpan.isWithin(timepan)) {
                println("DATE")
                return false
            }
        }

        // time
        def slotStartTime = new DateTime(slot.startTime).toLocalTime()
        def slotEndTime = new DateTime(slot.endTime).toLocalTime()
        if(slotStartTime >  toTime || slotEndTime < fromTime) {
            println("TIME")
            return false
        }

        // courts
        if(this.courtIdList().size() > 0) {
            if(!this.courtIdList().contains(slot.court.id)) {
                println("COURT")
                return false
            }
        }

        // weekdays
        if(this.weekDaysList().size() > 0) {
            if(!this.weekDaysList().contains(slot.startTime.getDay())) {
                log.info("WEEKD")
                return false
            }
        }

        return true
    }

    def weekDaysList() {
        if(weekDays) {
            return weekDays.split(",").collect { Integer.parseInt(it) }
        } else {
            return []
        }
    }

    def courtIdList() {
        if(courtIds) {
            return courtIds.split(",").collect { Long.parseLong(it) }
        } else {
            return []
        }
    }
}
