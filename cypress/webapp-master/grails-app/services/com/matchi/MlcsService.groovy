package com.matchi


import org.joda.time.Interval
import org.joda.time.format.DateTimeFormatter
import org.joda.time.format.ISODateTimeFormat

class MlcsService {
    public static final DateTimeFormatter DATE_FORMAT = ISODateTimeFormat.dateTime()
    private static final String FACILITY_NAME_KEY   = "facility"
    private static final String COURTS_KEY          = "courts"

    def bookingService
    def dateUtil

    /**
     * Updates last heart beat from MLCS client
     * @param facility
     */
    def updateFacilityMLCSHeartBeat(Facility facility) {
        facility.setFacilityProperty(FacilityProperty.FacilityPropertyKey.MLCS_LAST_HEARTBEAT, dateUtil.formatDateTime(new Date()))
    }

    /**
     * Builds a MLCS booking schedule.
     *
     * @param cmd The retrieving schedule request
     * @return Response (Map)
     */
    def buildScheduleResponse(def facility, def interval) {

        // retrieve all bookings by interval
        def bookings = bookingService.findAllBookingsByInterval(facility, interval)

        // group all bookings by cort (Map<Court, List<Bookings>>)
        def bookingsByCourt = bookings.groupBy { it.slot.court }

        def courts = []

        // build court schedules
        facility.courts.each {
            if(!it.archived && it.externalId && !"".equals(it.externalId?.trim())) {
                courts << [id: it.externalId, name: it.name, schedule: buildCourtSchedule(facility, bookingsByCourt.get(it))]
            }
        }

        // build object map
        def result = [:]
        result.put(FACILITY_NAME_KEY, facility.name)
        result.put(COURTS_KEY, courts)

        result
    }

    /**
     * Unions all slot intervals and composes schedule item:
     *  { from: '2012-01-01T14:00..', to: '2012-01-01T15:00..' }
     *
     *  If bookings list is null or empty an empty list will be returned.
     *  @param bookings List of bookings
     */
    def buildCourtSchedule(def facility, def bookings) {
        def schedule = []

        if (bookings) {
            // flattens and unions all slot intervals
            def intervals = bookings.collect { it.slot.toInterval() }
            intervals = addStartAndEndGraceTime(facility, intervals)

            def flattenIntervals = dateUtil.flatten(intervals)

            flattenIntervals.each {
                schedule << [from: DATE_FORMAT.print(it.start), to:  DATE_FORMAT.print(it.end)]
            }
        }

        return schedule
    }

    /**
     *
     * @param facility
     * @param intervals
     * @return
     */
    def addStartAndEndGraceTime(def facility, def intervals) {
        if(facility.getMlcsGraceMinutesStart() == 0 && facility.getMlcsGraceMinutesEnd() == 0) {
            return intervals
        }

        return intervals.collect() { Interval interval ->
            def gracedInterval = interval

            if(facility.getMlcsGraceMinutesStart() != 0) {
                gracedInterval = gracedInterval.withStart(interval.start.minusMinutes(facility.getMlcsGraceMinutesStart()))
            }

            if(facility.getMlcsGraceMinutesEnd() != 0) {
                gracedInterval = gracedInterval.withEnd(interval.end.plusMinutes(facility.getMlcsGraceMinutesEnd()))
            }
            gracedInterval
        }


    }

}
