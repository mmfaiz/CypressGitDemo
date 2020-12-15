package com.matchi

import com.matchi.facility.FacilityFilterCommand
import com.matchi.payment.PaymentFlow
import com.matchi.slots.SlotFilter
import grails.converters.JSON
import grails.validation.Validateable
import groovy.transform.ToString
import org.apache.http.HttpStatus
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsParameterMap
import org.joda.time.Interval
import org.joda.time.LocalDate
import org.springframework.util.StopWatch
import org.springframework.web.servlet.support.RequestContextUtils

import javax.servlet.http.HttpServletRequest

class BookController extends GenericController {

    def facilityService
    def slotService
    def courtService
    def priceListService

    def index(FacilityFilterCommand cmd) {
        PaymentFlow paymentFlow = PaymentFlow.getFinished(session, params.long("orderId"))

        [cmd: cmd, paymentFlow: paymentFlow]
    }

    def schedule() {
        def facilityId = getFacilityIdFromParams(request, params)
        def facility = Facility.get(facilityId)

        if (!facility) {
            log.info("No facility could be found with given facilityId: ${facilityId}")
            return response.sendError(404)
        }

        String date
        def week = null, year = null

        if (params.week) {
            week = params.week
            year = params.year
        }

        [facility: facility, date: date, week: week, year: year, wl: params.wl]
    }


    def listSlots(FindFacilitySlotsCommand cmd) {
        def facility = Facility.get(cmd.facility)
        def sports = []

        if (cmd.sport) {
            if (cmd.sport != 6) {
                sports = [Sport.get(cmd.sport)]
            } else {
                Sport.coreSportAndOther.list().each { Sport s ->
                    sports << s
                }
            }
        } else {
            sports = Sport.findAll()
        }

        def currentUser = getCurrentUser()

        if (!cmd.date) {
            cmd.date = new Date()
        }
        if (cmd.hasErrors()) {
            log.debug(cmd.errors)
            render status: HttpStatus.SC_BAD_REQUEST
            return
        }

        if (facility.isMembersOnly() && !currentUser?.hasActiveMembershipIn(facility)
                && !currentUser?.getMembershipIn(facility)?.inStartingGracePeriod) {
            return [timeSlots: [], noSlotsMessage: message(
                    code: "templates.bookingSchedules.userFacilitySchedule.membersBookingOnly")]
        }

        if (!facility.bookable) {
            return [timeSlots: [], noSlotsMessage: message(
                    code: "templates.bookingSchedules.userFacilitySchedule.noBookingAvailable")]
        }

        def courts = courtService.findUsersCourts([facility], sports, null, cmd.indoor, currentUser)
        def interval = new Interval(new LocalDate(cmd.date).toDateTimeAtStartOfDay(), new LocalDate(cmd.date).plusDays(1).toDateTimeAtStartOfDay())
        def filter = new SlotFilter(
                from: interval.start,
                to: interval.end,
                courts: courts,
                onlyFreeSlots: true
        )

        def slots = slotService.getSlots(filter)?.sort()

        if (slots && slots.every { !facility.isBookableForUser(it.toInterval().start, currentUser) }) {
            return [timeSlots: [], noSlotsMessage: message(
                    code: "templates.bookingSchedules.userFacilitySchedule.bookingDaysAhead",
                    args: [facility.name, facility.getBookingRuleNumDaysBookableForUser(currentUser)])]
        }

        def slotSports = courts.collect { it.sport.id }.unique()

        slots = slots.findAll { it.booking == null && it.isBookable() }

        /*slots.each {
            if(getCurrentUser()) {
                it.metaClass.price = priceListService.getBookingPrice(it, getCurrentUser())
            } else {
                it.metaClass.price = [price: 0l]
            }
        }*/

        ArrayList<TimeSlot> timeSlots = []
        Date slotStartTime
        Date oldSlotStartTime

        log.debug("Create timeslots for ${facility.name}")
        StopWatch watch = new StopWatch("Create timeslots for ${facility.name}")
        watch.start()

        slots?.each { Slot slot ->
            slotStartTime = slot.startTime

            if (!slotStartTime.equals(oldSlotStartTime)) {
                timeSlots << new TimeSlot(facilityId: facility.id, start: slot.startTime, slots: [slot])
            } else {
                timeSlots.last().slots << slot
            }

            oldSlotStartTime = slot.startTime
        }

        watch.stop();
        log.debug("Timeslots for ${facility.name} finished in ${watch.totalTimeSeconds} sec")
        log.debug("${timeSlots.size()} found for ${facility.name}")

        String locale = currentUser?.language
        if (!locale) {
            locale = RequestContextUtils.getLocale(request).language
        }

        render(view: "_listSlots", model: [timeSlots: timeSlots, facility: facility, locale: locale, date: cmd.date, sports: slotSports])
    }


    def findFacilities(FacilityFilterCommand cmd) {
        def facilitiesResult = facilityService.findActiveFacilities(cmd)
        def facilities = facilitiesResult.rows
        def count = facilitiesResult.count
        List<Long> facilityIds = facilities.collect { it["id"] as Long }

        String language = currentUser?.language
        Locale locale = language ? new Locale(language) : RequestContextUtils.getLocale(request)

        Map<Long, Map<String, Object>> facilitySlots = slotService.getSlotsForFacilities(currentUser, facilityIds, cmd.sport, new LocalDate(cmd.date), (cmd.outdoors == null) ? null : !cmd.outdoors, locale, cmd.hasCamera)
        facilities = facilities.collect { facility ->
            facility["slotsData"] = facilitySlots[facility["id"] as Long]
            facility
        }

        render view: "facilities", model: [facilities: facilities, count: count, cmd: cmd, user: currentUser]
    }

    def getSlotPrices() {
        List<Integer> slotIds = params.list("slotId")
        Boolean isList = slotIds?.size() > 1

        List<Slot> slots = Slot.withCriteria() { isList ? inList("id", slotIds) : eq("id", slotIds?.first()) }
        List result = []

        User user = getCurrentUser()
        Customer customer

        if (!user) customer = new Customer()

        slots.each { Slot slot ->
            result << [slotId: slot.id, currency: slot?.court?.facility?.currency,
                       price : slot?.court?.facility?.isFacilityPropertyEnabled(
                               FacilityProperty.FacilityPropertyKey.FEATURE_CALCULATE_MULTIPLE_PLAYERS_PRICE.name()) ? priceListService.getAvgBookingPrice(slot, user ?: [customer]).price : priceListService.getBookingPrice(slot, user ?: customer)?.price]
        }

        render result as JSON
    }

    Long getFacilityIdFromParams(HttpServletRequest request, GrailsParameterMap params) {
        long DEFAULT_FACILITY_ID = -1
        String facilityId = DEFAULT_FACILITY_ID.toString()

        if (params.facilityId instanceof String[]) {
            log.warn("facility passed as type String[] (using first value) and not String [${request.requestURI} ${request.queryString}] from <${request.getHeader("Referer")}>")
            facilityId = ((String[]) params.facilityId)[0]
        } else if (params.facilityId instanceof String) {
            facilityId = params.facilityId
        } else if (params.name instanceof String) {
            facilityId = Facility.findByShortname(params.name)?.id
        } else {
            log.warn("facility passed as type ${params.facilityId.getClass()} and not String [${request.requestURI} ${request.queryString}] from <${request.getHeader("Referer")}>")
        }
        try {
            return Long.parseLong(facilityId)
        } catch (NumberFormatException e) {
            return DEFAULT_FACILITY_ID
        }
    }
}

@ToString
@Validateable(nullable = true)
class FindFacilitySlotsCommand {
    Long facility
    Long sport
    Boolean indoor

    Date date

    static constraints = {
        facility nullable: false
        sport nullable: true
        date nullable: true
        indoor nullable: true
    }
}

class TimeSlot {
    Long facilityId
    Date start
    ArrayList<Slot> slots
}
