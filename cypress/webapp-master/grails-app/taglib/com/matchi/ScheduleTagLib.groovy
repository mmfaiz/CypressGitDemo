package com.matchi

import com.matchi.activities.ActivityOccasion
import com.matchi.payment.PaymentStatus
import com.matchi.schedule.Schedule
import com.matchi.schedule.Schedules
import com.matchi.schedule.TimeSpan
import com.matchi.slots.SlotFilter
import org.hibernate.criterion.CriteriaSpecification
import org.joda.time.DateTime
import org.joda.time.DateTimeConstants

class ScheduleTagLib {
    def scheduleService
    def slotService
    def priceListService
    def dateUtil
    def courtService
    def springSecurityService
    def facilityService

    def userFacilitySchedule = { attrs, body ->
        attrs.user   = springSecurityService.currentUser
        attrs.date   = currentDate()
        attrs.sport  = Sport.get(params.getLong("sport")) ?: attrs.facility?.getDefaultSport()
        def sports   = attrs.sport ? [attrs.sport] : null
        attrs.indoor = params.indoor != null ? params.boolean("indoor") : (attrs.facility?.isSportOnlyOutDoor(attrs.sport) ? false : !attrs.facility?.isOnlyOutDoor())
        attrs.sportsWithIndoor = attrs.facility?.getSportsGroupedByIndoor(attrs.facility?.getAllPublicBookableCourts())
        attrs.courts = courtService.findUsersCourts([ attrs.facility ], sports, null, attrs.indoor, attrs.user)
        attrs.membersCourts = courtService.findMembersCourts([ attrs.facility ], sports, null, attrs.indoor)

        def linkDateValue = (attrs.date.isBeforeNow()?new DateTime():attrs.date).toString("yyyy-MM-dd")

        attrs.model = [
                date: attrs.date,
                facility: attrs.facility,
                sport:  attrs.sport,
                sportsWithIndoor: attrs.sportsWithIndoor,
                indoor: attrs.indoor,
                courts: attrs.courts,
                user: attrs.user,
                switchToWeekLinkParams: [facilityId: attrs.facility?.id, name: attrs.facility?.shortname, "year":attrs.date.getWeekyear(), "week":attrs.date.getWeekOfWeekyear(), sport: attrs.sport?.id, indoor: attrs.indoor, wl: params.wl],
                switchToDateLinkParams: [facilityId: attrs.facility?.id, name: attrs.facility?.shortname, "date": linkDateValue, sport: attrs.sport?.id, indoor: attrs.indoor, wl: params.wl]
        ]

        if(params.week) {
            out << g.userWeeklySchedule(attrs, body)
        } else {
            out << g.userDailySchedule(attrs, body)
        }
    }

    def userWeeklySchedule = { attrs, body ->

        def current = attrs.date
        def facility = attrs.facility
        def previous = current.minusDays(7)
        def next = current.plusDays(7)
        def dates = dateUtil.composeWeeklyCalendar(current.getWeekyear(), current.getWeekOfWeekyear())

        Schedules schedules = new Schedules()
        def memberSchedules = new Schedules()

        dates.each { date ->
            DateTime start = dateUtil.beginningOfDay(new DateTime(date))
            DateTime end = dateUtil.endOfDay(new DateTime(date))

            SlotFilter slotFilter = new SlotFilter(from: start, to: end, courts: attrs.courts)

            def schedule = scheduleService.facilitySchedule(facility, slotFilter)
            schedules.addSchedule(schedule)

            def memberSlotFilter = new SlotFilter(from: start, to:  end, courts: attrs.membersCourts)
            def memberSchedule = scheduleService.facilitySchedule(facility, memberSlotFilter)
            memberSchedules.addSchedule(memberSchedule)
        }

        def startHour = facilityService.getEarliestOpeningHour([facility])
        def endHour   = facilityService.getLatestClosingHour([facility])

        /**
         * Below fetching the earliest and latest hour of all schedules
         */
        def updatedEarlyHour = schedules?.firstSlotHour()?.start?.getHourOfDay()
        def updateLateHour   = schedules?.lastSlotHour()?.end?.minusMillis(1)?.getHourOfDay()

        if (updatedEarlyHour != null) {
            startHour = updatedEarlyHour
        }
        if (updateLateHour != null) {
            endHour = updateLateHour
        }

        if(attrs.facility) {
            out << render(template:"/templates/bookingSchedules/userWeeklySchedule",
                    model: [schedules: schedules, memberSchedules: memberSchedules, dates: dates, previous: previous,
                            next: next, startHour: startHour, endHour: endHour]+attrs.model)
        }
    }

    def userDailySchedule = { attrs, body ->
        DateTime current = attrs.date
        def courts = attrs.courts

        if(current.isBeforeNow()) {
            current = attrs.model.date = attrs.date = new DateTime()
        }

        def from   = dateUtil.beginningOfDay(current)
        def to     = dateUtil.endOfDay(current)

        SlotFilter slotFilter = new SlotFilter(from: from, to:  to, courts: courts)
        def schedule = scheduleService.facilitySchedule(attrs.facility, slotFilter)

        def memberSlotFilter = new SlotFilter(from: from, to:  to, courts: attrs.membersCourts)
        def memberSchedule = scheduleService.facilitySchedule(attrs.facility, memberSlotFilter)

        def timeSpans = []
        if(schedule.getAllSlots()) {
            timeSpans = dateUtil.createTimeSpans(
                    new DateTime(current),
                    schedule?.firstSlot()?.start?.getHourOfDay(),
                    schedule?.lastSlot()?.end?.minusMillis(1)?.getHourOfDay())
        }

        out << render(template: "/templates/bookingSchedules/userFacilitySchedule",
                model: attrs.model + [schedule: schedule, timeSpans: timeSpans, memberSchedule: memberSchedule])
    }

    def userScheduleSlot = { attrs, body ->
        def schedule = attrs.schedule
        def span = attrs.span
        Facility facility = schedule.facility
        int bookingLimit = attrs.bookingLimit

        def date = span.start.toString("yyyy-MM-dd")
        def color = schedule.color(span.toInterval(), bookingLimit)
        def startHourTime = span.start.toString("HH")
        def status = schedule.status(span, bookingLimit)

        if ( status.contains(Schedule.Status.PAST) ) {
            out << render(template:"/templates/schedule/user/fullslot", model: [color:color, slotpast: true])
        } else if (status.contains(Schedule.Status.NOT_AVAILABLE)) {
            out << render(template:"/templates/schedule/user/fullslot", model: [color:color])
        } else if (status.contains(Schedule.Status.FULL)) {
            out << render(template:"/templates/schedule/user/fullslot", model: [color:color])
        } else {
            out << render(template:"/templates/schedule/user/freeslot",
                    model: [id: attrs.id, date: date, start: span.toInterval().start,
                            end: span.toInterval().end, facility: facility, color:color,
                            sport: attrs.sport, startHourTime: startHourTime])
        }
    }

    def userDailyScheduleSlot = { attrs, body ->
        def schedule = attrs.schedule
        Facility facility = schedule.facility
        def slot = attrs.slot
        def user = springSecurityService.currentUser
        int bookingLimit = attrs.bookingLimit

        def isBookable = slot && slot.interval.end.isAfterNow() && facility.isBookableForLimit(slot.interval.start as DateTime, bookingLimit)

        if (slot && isBookable && slot.booking) {
            def booking = slot.booking

            if(user && booking?.owned) {
                def span = new TimeSpan(new DateTime(slot.start), new DateTime(slot.end))
                def color = schedule.color(slot, bookingLimit)

                out << render(template:"/templates/schedule/user/daily/booking",
                        model: [slot: slot, facility: facility, color: color]+attrs)
            } else {
                out << render(template:"/templates/schedule/user/daily/full",
                        model: [slot: slot, facility: facility]+attrs)
            }


        }  else if(slot && isBookable && !slot.booking) {
            out << render(template:"/templates/schedule/user/daily/free",
                    model: [slot: slot, facility: facility]+attrs)
        } else {
            out << render(template:"/templates/schedule/user/daily/noslot",
                    model: [past: slot?.interval?.end?.isBeforeNow()]+attrs)
        }
    }

    def scheduleSlot = { attrs, body ->
        def slot = attrs.slot

        if (slot && (slot.booking || slot.subscriptionId)) {
            Booking booking = slot.booking

            // def codeRequest = CodeRequest.findByBooking(Booking.findById(slot.booking?.id))
            // def warnAboutCodeRequest = codeRequest?.hasProblems()

            out << render(template:"/templates/schedule/booking",
                    model: [slot: slot, warnAboutCodeRequest: false]+attrs)

        }  else if(slot && !slot.booking) {
            out << render(template:"/templates/schedule/slot",
                    model: [slot: slot]+attrs)
        } else {
            out << render(template:"/templates/schedule/noslot",
                    model: [:]+attrs)
        }

    }

    def scheduleActivityName = { attrs, body ->
        def bookingId = attrs.bookingId
        if (bookingId) {
             String result = ActivityOccasion.createCriteria().get {
                createAlias("activity", "a")
                bookings {
                    eq("id", bookingId)
                }
                projections {
                    property("a.name")
                }
            }.encodeAsHTML()

            out << result
        }
    }

    def schedulePlayers = { attrs, body ->
        def bookingId = attrs.bookingId
        def customerId = attrs.customerId
        if (bookingId && customerId) {
            def players = Player.createCriteria().list {
                createAlias("booking", "b")
                createAlias("customer", "c", CriteriaSpecification.LEFT_JOIN)
                eq("b.id", bookingId)
                or {
                    ne("c.id", customerId)
                    isNull("c.id")
                }
            }
            out << render(template: "/templates/schedule/players", model: [players: players])
        }
    }

    def scheduleListBooking = { attrs, body ->
        Booking booking = attrs.booking
        def color = ColorFetcher.getFacilityColor(booking.slot)
        def paidStatus = booking?.payment ? booking?.payment?.status : (booking?.isFinalPaid() ? PaymentStatus.OK : PaymentStatus.PENDING)
        out << render(template:"/templates/schedule/listBooking",
                model: [booking:booking, color:color, paidStatus:paidStatus])

    }

    def scheduleViewControl = { attrs, body ->
        out << render(template: "/templates/schedule/viewControl",
                model: [date:attrs.date, print:attrs.print, fullscreen: attrs.fullscreen])
    }

    private DateTime currentDate() {
        def week = params.int("week")
        def year = params.int("year")
        def date = params.date("date", "yyyy-MM-dd")

        def current = new DateTime()

        if (week && year) {
            current = current.withWeekyear(year).withWeekOfWeekyear(week).withDayOfWeek(DateTimeConstants.MONDAY)
        } else if (date) {
            current = new DateTime(date)
        }
        return current
    }
}
