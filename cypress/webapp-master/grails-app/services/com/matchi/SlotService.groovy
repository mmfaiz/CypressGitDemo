package com.matchi


import com.matchi.api_ext.model.APIExtSlot
import com.matchi.orders.Order
import com.matchi.orders.OrderPayment
import com.matchi.slots.AdjacentSlotGroup
import com.matchi.slots.SlotFilter
import grails.transaction.Transactional
import groovyx.gpars.GParsPool
import org.apache.commons.lang.StringUtils
import org.codehaus.groovy.grails.plugins.DomainClassGrailsPlugin
import org.hibernate.criterion.CriteriaSpecification
import org.joda.time.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.util.StopWatch

class SlotService {

    static transactional = false

    def grailsApplication
    def sessionFactory
    def groovySql
    def propertyInstanceMap = DomainClassGrailsPlugin.PROPERTY_INSTANCE_MAP
    def dateUtil
    def courtService
    def messageSource

    @Value('${matchi.slot.create.batchSize}')
    Integer createBatchSize

    @Value('${matchi.slot.create.poolSize}')
    Integer createPoolSize

    @Transactional
    def generateSlots(CreateSeason createSeason) {
        def slots = []
        def stopWatch = new StopWatch("Season slot generator ${createSeason.name}")
        def timeZone = DateTimeZone.forID("CET")

        if(!createSeason) {
            throw new IllegalArgumentException("CreateSeason cannot be null")
        }

        stopWatch.start()

        log.info("Generating slots for ${createSeason.name}")

        def currentTime = createSeason.startTime.toDateMidnight()
        def endTime     = createSeason.endTime.toDateMidnight()

        while(currentTime <= endTime) {

            LocalDate currentDate = currentTime.toLocalDate()

            createSeason.courts.each { CreateCourtSeason courtSeason ->

                OpenHours courtHours = courtSeason.getOpenHours(currentTime.dayOfWeek)

                if(courtHours) {
                    LocalTime opening = courtHours.opening
                    LocalTime closing = courtHours.closing

                    while(opening < closing) {
                        DateTime slotStartTime
                        try {
                            slotStartTime = currentDate.toDateTime(opening)
                        } catch (IllegalFieldValueException e) {
                            log.info("Summer time change, skipping")
                            opening = opening.plus(courtSeason.timeBetween.plus(courtSeason.bookingLength))
                            continue
                        }

                        DateTime slotEndTime = slotStartTime.plus(courtSeason.bookingLength)

                        def slot = new Slot()
                        slot.court = courtSeason.court
                        slot.startTime = slotStartTime.withZone(timeZone).toDate()
                        slot.endTime = slotEndTime.withZone(timeZone).toDate()

                        if (new LocalTime(slot.startTime) == new LocalTime(slot.endTime)) {
                            log.info("Winter time change, add extra period to slot end time")
                            slot.endTime = slotEndTime.toDateTime().plus(courtSeason.bookingLength).toDate()
                        }

                        slots << slot

                        if(opening.plus(courtSeason.timeBetween).plus(courtSeason.bookingLength) < opening) {
                            break
                        }

                        def oldOpening = opening
                        opening = slotEndTime.plus(courtSeason.timeBetween).toLocalTime()

                        if (opening == oldOpening) {
                            log.info("Winter time change, plus one more period to next start time")
                            opening = opening.plus(courtSeason.bookingLength)
                        }
                    }
                }
            }
            currentTime = currentTime.plusDays(1)

            log.debug(currentTime)
        }

        stopWatch.stop()

        log.info(stopWatch.shortSummary())
        def time = Math.max((int) stopWatch.lastTaskTimeMillis / 1000, 1)
        def slotPerSeconds = (slots.size() > 0? (int) (slots.size() / time):0)
        log.info ("Generated ${slots.size()} slots in ${stopWatch.lastTaskTimeMillis} (${slotPerSeconds} slots/sec)")

        return slots
    }

    void createSlots(Collection slots) {
        def stopWatch = new StopWatch("Season slot save batch")
        stopWatch.start()

        GParsPool.withPool(createPoolSize) {
            slots.collate(createBatchSize).eachParallel { list ->
                Slot.withNewSession {
                    Slot.withTransaction {
                        list.each { Slot slot ->
                            try {
                            if(!Slot.countByCourtAndStartTimeAndEndTime(slot.court, slot.startTime, slot.endTime)) {
                                if(!slot.save()) {
                                    log.error "Unable to save slot (${slot?.startTime} - ${slot?.endTime}, court ID: ${slot?.court?.id})"
                                }
                            } else {
                                log.debug("Could not create slot - already exists!")
                            }
                            } catch (e) {log.error e.message, e}
                        }
                    }
                }
            }
        }

        stopWatch.stop()
        log.info(stopWatch.shortSummary())
        log.info("Saved ${slots.size()} slots in ${stopWatch.totalTimeSeconds} seconds (${slots.size() / stopWatch.totalTimeSeconds})")
    }

    @Transactional
    def removeSlots(def slots) {
        log.info("Removing ${slots.size()} slots")
        slots.each {
            log.info(it.id)
            it.delete()
        }
    }

    def getNumBookableSlotsPerDay(SlotFilter command) {
        if(command.courts.size() == 0) {
            return [:]
        }

        def courtIds = command.courts.collect { it.id }
        def courtIdsStr = StringUtils.join(courtIds, ",")

        def rows = groovySql.rows("""
                select
                    count(s.id) as nrOfSlots, date(start_time) as date
                from
                    slot s LEFT JOIN court c on s.court_id = c.id LEFT JOIN facility f on c.facility_id = f.id LEFT JOIN booking b on b.slot_id = s.id
                where
                    s.court_id in (${courtIdsStr})
                and
                    s.start_time < date_add(now(), INTERVAL f.booking_rule_num_days_bookable DAY)
                and
                    s.start_time > :from
                and
                    s.start_time < :to
                and
                    b.id is null
                group by date(start_time);
                """, [from:command.from.toDate(), to:command.to.toDate()]);

        groovySql.close()
        return rows.groupBy { new LocalDate(it.date) }
    }

    Map<Long, Object> getSlotsForFacilities(User currentUser, List<Long> facilityIds, Long sport, LocalDate date, Boolean indoor, Locale locale, Boolean hasCamera = null) {
        List<Facility> facilities = Facility.findAllByIdInList(facilityIds, [cache: true])
        def sports = []

        if (sport) {
            if (sport != 6) {
                sports = [Sport.get(sport)]
            } else {
                Sport.findAll().each {s ->
                    if (s.id == 6 || !s.coreSport) {
                        sports << s
                    }
                }
            }
        } else {
            sports = Sport.findAll()
        }

        if (!date) {
            date = new LocalDate()
        }

        facilities.collect { facility ->
            if (facility.isMembersOnly() && !currentUser?.hasActiveMembershipIn(facility)
                    && !currentUser?.getMembershipIn(facility)?.inStartingGracePeriod) {
                return [timeSlots: [], facility: facility, noSlotsMessage: messageSource.getMessage("templates.bookingSchedules.userFacilitySchedule.membersBookingOnly",null, locale)]
            }

            if (!facility.bookable) {
                return [timeSlots: [], facility: facility, noSlotsMessage: messageSource.getMessage("templates.bookingSchedules.userFacilitySchedule.noBookingAvailable",null, locale)]
            }

            def courts = courtService.findUsersCourts([facility], sports, null, indoor, currentUser, hasCamera)
            def interval = new Interval(date.toDateTimeAtStartOfDay(), date.plusDays(1).toDateTimeAtStartOfDay())
            def filter = new SlotFilter(
                    from: interval.start,
                    to: interval.end,
                    courts: courts,
                    onlyFreeSlots: true
            )

            def slots = getSlots(filter)?.sort()

            if (slots && slots.every { !facility.isBookableForUser(it.toInterval().start, currentUser) }) {
                return [timeSlots: [], facility: facility, noSlotsMessage: messageSource.getMessage("templates.bookingSchedules.userFacilitySchedule.bookingDaysAhead",
                        [facility.name, facility.getBookingRuleNumDaysBookableForUser(currentUser)] as Object[], locale)]
            }

            def slotSports = courts.collect { it.sport.id }.unique()

            slots = slots.findAll { it.booking == null && it.isBookable() }

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

            return [timeSlots: timeSlots, facility: facility, locale: locale.language, date: date, sports: slotSports]
        }.collectEntries {[it.facility.id, it]}
    }

    List<Slot> getSlots(SlotFilter cmd) {
        if(cmd.courts.size() == 0) {
            return []
        }

        log.debug("Fetching slots from ${cmd.from} to ${cmd.to}")

        Slot.withCriteria {
            inList("court", cmd.courts)
            ge("startTime", cmd.from.toDate())
            lt("startTime", cmd.to.toDate())
            if(cmd.onlyFreeSlots) {
                createAlias("booking", "b", CriteriaSpecification.LEFT_JOIN)
                isNull("b.id")
            }

            /**
             *  Logic below is the following:
             *  - if a fixedStartTime is added just return slots from that time
             *  Otherwise:
             *  - If either fromTime or toTime is midnight, that means that there is basically no restriction
             *    on that end of the interval. So we skip checking.
             *  - To check that a slot is a part of interval, the slot end time must be greater than the start of the interval
             *    or the slot start time must be less than the end of the interval. So either part of the slot must be inside
             *  - A slot ending at midnight or any time next day is special case (handled by comparing date parts of slot start/end time)
             *  - A slot that starts at midnight is automatically before toTime, no need for special case
             */

            if (cmd.fixedStartTime) {
                sqlRestriction("time({alias}.start_time) = '${cmd.fixedStartTime.toString("HH:mm:ss")}'")
            }
            else {
                if (cmd.fromTime && !dateUtil.isMidnight(cmd.fromTime)) {
                    or {
                        sqlRestriction("time({alias}.end_time) > time('${cmd.fromTime.toString('HH:mm')}')")
                        sqlRestriction("time({alias}.start_time) >= time('${cmd.fromTime.toString('HH:mm')}') and datediff({alias}.end_time, {alias}.start_time) > 0")
                    }
                }

                if (cmd.toTime && !dateUtil.isMidnight(cmd.toTime)) {
                    sqlRestriction("time({alias}.start_time) < time('${cmd.toTime.toString('HH:mm')}')")
                }
            }

            if (cmd.onWeekDays) {
                def weekdays = cmd.onWeekDays.collect{it == 7 ? 1 : it + 1}.join(',')
                sqlRestriction("dayofweek({alias}.start_time) in ($weekdays)")
            }
            order("startTime", "asc")
        }
    }

    def getSlot(def courtId, def startTime, def endTime) {
        log.info("Getting slot for " + new Date(startTime))

        def slotCriteria = Slot.createCriteria()
        def slot = slotCriteria.get {
            eq("court", Court.findById(courtId))
            and {
                eq('startTime', new Date(startTime))
                eq('endTime', new Date(endTime))
            }
        }

        return slot
    }

    def getSlot(def slotId) {
        return Slot.get(slotId)
    }

    def getSlots(def slotsIds) {
        if(!slotsIds || slotsIds.isEmpty()) {
            throw new IllegalArgumentException("slotIds cannot be null or empty")
        }

        return Slot.createCriteria().list {
            'in'('id', slotsIds)
            order('startTime','asc')
        }

    }

    def getSubscriptionSlots(DateTime fromDate, DateTime toDate,
                             LocalTime time, List<String> courtIds, int interval) {

        return getSubscriptionSlots(fromDate, toDate, time, courtIds, interval, true)
    }

    def getSubscriptionSlots(DateTime fromDate, DateTime toDate,
                             Slot slot, List<String> courtIds, int interval, Boolean onlyFreeSlots) {


    }
    def getRecurrenceSlots(DateTime fromDate, DateTime toDate, List<String> weekDays, int frequency, int interval, List<Slot> recurrenceSlots) {
        return getRecurrenceSlots(fromDate, toDate, weekDays, frequency, interval, recurrenceSlots, false)
    }

    List<Slot> sortByTime(List<Slot> slots) {
        return slots?.sort { Slot s1, Slot s2 ->
            if(s1.startTime.compareTo(s2.startTime) != 0) {
                return s1.startTime.compareTo(s2.startTime)
            }

            return s1.endTime.compareTo(s2.endTime)
        }
    }

    boolean areSameCourt(List<Slot> slots) {
        if(slots.size() in [0,1]) {
            return true
        }

        return slots*.court*.id.unique().size() == 1
    }

    boolean areConsecutive(List<Slot> slots) {
        if(slots.size() in [0,1]) {
            return true
        }

        List<Slot> sorted = sortByTime(slots)

        Slot lastSlot
        boolean consecutive = true
        sorted.each { Slot slot ->
            if(!lastSlot) {
                lastSlot = slot
            } else {
                if(slot.startTime != lastSlot.endTime) {
                    consecutive = false
                }
                lastSlot = slot
            }
        }

        return consecutive
    }

    /**
     * Returns a sequence of bookable slots starting with the supplied slots. Groups them by court, adjacency and split up by first slots supplied.
     * @param slot
     * @param excluding
     * @return
     */
    List<AdjacentSlotGroup> getSubsequentBookableSlotsFor(List<Slot> firstSlots) {
        if(!firstSlots) {
            throw new IllegalArgumentException("firstSlots cannot be empty")
        }

        final List<String> firstSlotIds = firstSlots*.id

        List<Slot> result = Slot.createCriteria().list {
            or {
                firstSlots.collect { Slot slot ->
                    return and {
                        eq("court", slot.court)
                        gte("startTime", slot.startTime)
                        lt("startTime", dateUtil.endOfDay(slot.startTime).toDate())
                    }
                }
            }
        }

        /**
         * The logic below looks at the sorted lists of first slots and the resulting slots from the database.
         * For each first slot, we take every slot that is trailing. Then we look at them until there is a booking or they end.
         * So given for example the result list [s1, s2, s3, s4, s5] where first slots are [s1, s3], we search the whole list
         * [s1, s2, s3, s4, s5] but until s3 which is also a first slot. Then, we search [s3, s4, s5].
         */

        Map<Court, List<Slot>> groupedByCourt = result.sort().groupBy { Slot slot -> slot.court }
        Map<Court, List<Slot>> firstSlotsGroupedByCourt = firstSlots.sort().groupBy { Slot slot -> slot.court }
        List<Slot> finalList = []

        firstSlotsGroupedByCourt.each { Court court, List<Slot> slots ->
            List<Slot> slotsForCourt = groupedByCourt.get(court)

            // Look for trailing slots starting with each first slot
            slots.each { Slot firstSlot ->
                List<Slot> slotsStartingWithFirstSlot = slotsForCourt.dropWhile { Slot slot -> slot.id != firstSlot.id }

                for(int i = 0; i < slotsStartingWithFirstSlot.size(); i++) {
                    Slot current = slotsStartingWithFirstSlot.get(i)
                    if(!current.booking && !(slots.contains(current) && current.id != firstSlot.id)) {
                        finalList << current
                    } else {
                        break // No more trailings to add here
                    }
                }
            }
        }

        /**
         * Remove "later" slot groups (separated by space in schedule for example) by filtering out the ones
         * having their first slot in the original first slot list
         */
        final List<AdjacentSlotGroup> groupedByCourtAndAdjacency = groupByCourtAndAdjacency(finalList, firstSlots)
        return groupedByCourtAndAdjacency.findAll { AdjacentSlotGroup asg -> asg.firstSlot.id in firstSlotIds }
    }

    /**
     * Groups a list of slots by court and adjacency, and populates lists of subsequents slots
     * Assumes first slot in each adjacency group is also a selected slot.
     * @param slots
     * @return
     */
    List<AdjacentSlotGroup> createAdjacentSlotGroupsWithSubsequentSlots(List<Slot> slots) {
        List<AdjacentSlotGroup> adjacentSlotGroups = groupByCourtAndAdjacency(slots)
        List<AdjacentSlotGroup> subSequentSlotGroups = getSubsequentBookableSlotsFor(adjacentSlotGroups*.getFirstSlot())

        // Populating the subsequent slots, containing all possible slots that could be selected
        subSequentSlotGroups.each { AdjacentSlotGroup subSequentSlotGroup ->
            AdjacentSlotGroup adjacentSlotGroup = adjacentSlotGroups.find { AdjacentSlotGroup asg -> asg.firstSlot.id == subSequentSlotGroup.firstSlot.id }
            adjacentSlotGroup.subsequentSlots = subSequentSlotGroup.selectedSlots
        }

        return adjacentSlotGroups
    }

    /**
     * Groups a list of slots by court and adjacency, splitting if such slots are supplied
     * @param slots
     * @param splitAtSlots
     * @return
     */
    List<AdjacentSlotGroup> groupByCourtAndAdjacency(List<Slot> slots, List<Slot> splitAtSlots = null) {
        Map<Court, List<Slot>> groupedByCourts = slots.groupBy { Slot slot -> slot.court }

        List<AdjacentSlotGroup> adjacentSlotGroups = []

        groupedByCourts.each { Court court, List<Slot> slotsOfCourt ->
            adjacentSlotGroups += groupByAdjacency(slotsOfCourt, splitAtSlots)
        }

        // Sorting by court list position
        return adjacentSlotGroups.sort { AdjacentSlotGroup adjacentSlotGroup -> adjacentSlotGroup.firstSlot.court.listPosition }
    }

    /**
     * Groups a set of slots by adjacency. Assumes slots are on same court.
     * Splitting adjacent slots if split slots are supplied.
     * @param slots
     * @return
     */
    List<AdjacentSlotGroup> groupByAdjacency(List<Slot> slots, List<Slot> splitAtSlots = null) {
        if(!slots) {
            throw new IllegalArgumentException("Must have slots to group!")
        }

        if(!areSameCourt(slots)) {
            throw new IllegalArgumentException("Must have same court!")
        }

        List<Slot> sortedSlots = sortByTime(slots)
        List<AdjacentSlotGroup> adjacentSlotGroups = []

        // If all are consecutive, we are fine
        if(areConsecutive(sortedSlots) && !splitAtSlots) {
            adjacentSlotGroups = [new AdjacentSlotGroup(sortedSlots)]

        // Otherwise we have to loop
        } else {
            List<Slot> currentSorted = []

            for(Slot s : sortedSlots) {
                if(!currentSorted) {
                    currentSorted << s
                } else {
                    // If anyone knows a better way to append without modifying, be my guest!
                    // If a slot is consecutive with the last one, it is appended to the list. Also required
                    // that the slot is not a "split slot", meaning that it is meant to be the first of a sequence
                    if(areConsecutive([currentSorted, [s]].flatten()) && !splitAtSlots?.contains(s)) {
                        currentSorted << s
                    } else {
                        // OK, so the next slot is not adjacent to the one we have now.
                        // Starting over with the next slot.
                        adjacentSlotGroups << new AdjacentSlotGroup(currentSorted)
                        currentSorted = [s]
                    }
                }
            }

            adjacentSlotGroups << new AdjacentSlotGroup(currentSorted)
        }

        return adjacentSlotGroups
    }

    /**
     * Generates an id to id lookup table, so you can check for what slot the first slot id is in a sequence of trailing slots
     * @param adjacentSlotGroups
     * @return
     */
    Map<String, String> createFirstSlotLookupTable(List<AdjacentSlotGroup> adjacentSlotGroups) {
        Map<String, String> firstSlotLookupTable = [:]

        adjacentSlotGroups.each { AdjacentSlotGroup adjacentSlotGroup ->
            Slot firstSlot = adjacentSlotGroup.getFirstSlot()
            adjacentSlotGroup.subsequentSlots.each { Slot slot ->
                firstSlotLookupTable.put(slot.id, firstSlot.id)
            }
        }

        return firstSlotLookupTable
    }

    def getRecurrenceSlots(DateTime fromDate, DateTime toDate, List<String> weekDays, int frequency, int interval, List<Slot> recurrenceSlots, def onlyFreeSlots) {

        RecurringSlotsContainer slotsContainer = new RecurringSlotsContainer()
        def bookingGroupFrequency = new BookingGroupFrequencyHandler()
        List<SlotsGroup> groupedSlots = groupSlotsByStartingTimeAndDuration( recurrenceSlots )

        DateTime intervalStartingDay = dateUtil.beginningOfDay(fromDate)
        DateTime startingDay = dateUtil.beginningOfDay(fromDate)
        DateTime endingDay   = dateUtil.endOfDay(toDate)
        def daysInWeek       = weekDays.collect { it.toInteger() }
        int itsBeforeFreq    = weekDays.size()
        int daysIterated     = 0

        while (startingDay.isBefore(endingDay) || startingDay.isEqual(endingDay)) {
            if(daysInWeek.contains(startingDay.dayOfWeek)) {
                groupedSlots.each { SlotsGroup sg ->
                    DateTime startTime = new DateTime(startingDay).withHourOfDay(sg.hour).withMinuteOfHour(sg.minute).withSecondOfMinute(0).withMillisOfSecond(0)
                    DateTime endTime   = startTime.plus(sg.duration)

                    SlotFilter filter    = new SlotFilter()
                    filter.from          = startTime
                    filter.to            = endTime
                    filter.courts        = sg.slots.collect { it.court }
                    filter.onlyFreeSlots = onlyFreeSlots

                    def foundSlots = getSlots(filter)

                    foundSlots.each { Slot slot ->
                        if(slot.booking) {
                            slotsContainer.unavailableSlots << slot
                        } else {
                            slotsContainer.freeSlots << slot
                        }

                    }
                }

                daysIterated++
            }
            if(daysIterated < itsBeforeFreq) {
                startingDay = startingDay.plusDays(1)
            } else {
                intervalStartingDay = bookingGroupFrequency.getNextOccurence(intervalStartingDay, frequency, interval)
                startingDay  = intervalStartingDay
                daysIterated = 0
            }
        }

        log.info("Recurrence bookings on ${slotsContainer.freeSlots.size()} slots")
        log.info("Unavailable bookings on ${slotsContainer.unavailableSlots.size()} slots")

        return slotsContainer
    }

    def groupSlotsByStartingTimeAndDuration(List<Slot> unGroupedSlots) {

        unGroupedSlots = unGroupedSlots.sort { it.startTime }

        def groupedSlots    = []
        int compareHour, compareMinute
        Duration compareDuration = null

        unGroupedSlots.each { Slot slot ->
            def slotHour     = new DateTime(slot.startTime).hourOfDay
            def slotMinute   = new DateTime(slot.startTime).minuteOfHour
            def slotDuration = slot.getDuration()

            if((compareHour != slotHour || compareMinute != slotMinute || compareDuration != slotDuration) &&
               (!groupedSlots.find { it?.hour == slotHour && it?.minute == slotMinute && it?.duration == slotDuration })) {

                def slotsGroup = new SlotsGroup()
                slotsGroup.hour     = slotHour
                slotsGroup.minute   = slotMinute
                slotsGroup.duration = slotDuration
                slotsGroup.slots << slot

                groupedSlots << slotsGroup
            } else {
                groupedSlots.each { SlotsGroup sg ->
                    if( sg.hour == slotHour && sg.minute == slotMinute && sg.duration == slotDuration) {
                        sg.slots << slot
                    }
                }
            }

            compareHour     = slotHour
            compareMinute   = slotMinute
            compareDuration = slotDuration
        }

        return groupedSlots
    }

    def groupSlotsByDate(def slots) {
        def sortedSlots = slots.sort { a,b -> a.startTime.compareTo(b.startTime) }
        def result = [:]
        sortedSlots.each { slot ->
            def date = new LocalDate(slot.startTime)

            if(!result.containsKey(date)) {
                result[date] = []
            }

            result[date] << slot

        }
        return result
    }

    def sortSubscriptionSlots(def slots, def cmd) {
        log.debug("Sorting subscription slots... ${slots.size()}")

        def willBeRemoved = []
        def freeSlots = []
        def subscriptionSlots = []
        def notAvailableSlots = []

        slots.each { Slot slot ->
            if((!slot.subscription || slot.subscription?.id != cmd.id) && !slot.booking) {
                freeSlots << slot
            } else {
                if ((slot.subscription != null) && (slot.subscription?.id == cmd.id)) {
                    subscriptionSlots << slot
                } else {
                    notAvailableSlots << slot
                }
            }
        }

        if (cmd.id) {
            def allreadyInSubscription = Subscription.get(cmd.id).slots

            allreadyInSubscription.each { Slot slot ->
                def exists = false

                slots.each {
                    if(it.id == slot.id) {
                        exists = true
                    }
                }

                if (!exists) {
                    willBeRemoved << slot
                }
            }
        }

        log.debug("Finished sorting")
        log.debug("Freeslots ${freeSlots.size()}")
        log.debug("Subscriptionslots ${subscriptionSlots.size()}")
        log.debug("Notavailableslots ${notAvailableSlots.size()}")

        return [ freeSlots: freeSlots, subscriptionSlots: subscriptionSlots, notAvailableSlots: notAvailableSlots, willBeRemoved: willBeRemoved ]
    }
    def getScheduleSlotInfo(def startDate, def endDate, def facilityId) {

        String sqlquery = """SELECT s.id as sid, b.paid as bp, pt.id as pt, op.id as op
                            FROM (slot s, court c)
                            LEFT JOIN booking b on s.id = b.slot_id
                            LEFT JOIN payment p on p.id = b.payment_id
                            LEFT JOIN payment_transaction pt on p.id = pt.payment_id
                            LEFT JOIN `order` o on o.id = b.order_id
                            LEFT JOIN order_order_payments oop on oop.order_id = o.id
                            LEFT JOIN order_payment op on op.id = oop.payment_id
                            WHERE s.court_id = c.id
                            AND s.start_time >= ?
                            AND s.end_time <= ?
                            AND c.facility_id=?"""

        def parameters = [ startDate, endDate, facilityId ]
        def result = ""
        groovySql.eachRow( sqlquery, parameters ) {
            result += it.sid
            result += it.bp
            result += it.pt
            result += it.op
        }
        result = result.encodeAsMD5().toString()

        groovySql.close()
        return result
    }

    def getRedeemableSlots(List<Facility> facilities) {

        def facIds = facilities.collect { it.id }
        def facIdsStr = StringUtils.join(facIds, ",")

        def now       = new Date()
        def yesterday = new DateTime().minusDays(1).toDate()

        log.debug("Get redeemSlots from ${new Date()} and backwards")

        def slotIdsToRedeem = groovySql.rows("""
                select s.id as slotId from slot s
                    LEFT JOIN booking b on b.slot_id = s.id
                    LEFT JOIN customer bc on b.customer_id = bc.id
                    LEFT JOIN subscription sb on s.subscription_id = sb.id
                    LEFT JOIN `order` o on b.order_id = o.id
                    LEFT JOIN slot_redeem sr on sr.slot_id = s.id
                WHERE
                    (sr.id is null OR sr.redeemed = false)
                AND
                    bc.facility_id in (${facIdsStr})
                AND
                    b.last_updated < :now
                AND
                    s.start_time < now()
                AND
                    sb.id is not null
                AND
                    bc.id != sb.customer_id
                AND
                    (b.payment_id is not null or b.paid = true or o.status = 'COMPLETED');
                """, [now: new Date()]);

        slotIdsToRedeem = slotIdsToRedeem?.collect { it.slotId }

        groovySql.close()
        if (slotIdsToRedeem.size() > 0) {
            return Slot.withCriteria { inList("id", slotIdsToRedeem ) }
        }

        return []
    }

    def getSlotRefundPolicy(Slot slot) {
        if(slot) {
            def booking = slot.booking
            def order = booking?.order

            if(!slot.isRefundable()) {
                return [code: "payment.refund.notrefundable", args: [order?.facility?.getBookingCancellationLimit()]]
            } else {
                if(booking && order && !slot.isBookedBySubscriber()) {

                    def percentage = slot.refundPercentage()

                    if(order.isPaidByCoupon()) {
                        if(percentage == 100) {
                            return [code: "payment.refund.coupon"]
                        } else {
                            return [code: "payment.refund.notrefundable", args: [order.facility.getBookingCancellationLimit()]]
                        }
                    } else if(order.isPaidByCreditCard()) {

                        if(percentage == 100) {

                            if(order.status.equals(Order.Status.CONFIRMED) || (order.status.equals(Order.Status.ANNULLED) && !order.areAllPayments(OrderPayment.Status.CREDITED))) {
                                return [code: "payment.refund.servicefee.confirmed", args: [
                                        grailsApplication.config.matchi.settings.currency[order.facility.currency].serviceFee +
                                                " " + order.facility.currency]]

                            } else if (order.status.equals(Order.Status.COMPLETED) || (order.status.equals(Order.Status.ANNULLED) && order.areAllPayments(OrderPayment.Status.CREDITED))) {
                                return [code: "payment.refund.servicefee.completed", args: [
                                        grailsApplication.config.matchi.settings.currency[order.facility.currency].serviceFee +
                                                " " + order.facility.currency]]
                            } else {
                                return [code: "payment.refund.servicefee", args: [
                                        grailsApplication.config.matchi.settings.currency[order.facility.currency].serviceFee +
                                                " " + order.facility.currency]]
                            }

                        } else {
                            return [code: "payment.refund.procentage", args: ["${percentage}"]]
                        }
                    }
                }
            }
        }
    }

    def getDaySlotHours(DateTime date, Long facilityId, List<Long> sports = null) {

        def sportJoin      = ""
        def sportCondition = ""

        // Clear away nulls and non-Longs
        sports = sports?.findAll() {
            it != null && it instanceof Long
        }

        if (sports && sports.size() > 0) {
            sportJoin      = "left join sport sp on sp.id = c.sport_id"
            sportCondition = "and sp.id in (${StringUtils.join(sports, ',')})"
        }

        def result = groovySql.rows("""
            select DISTINCT(s.start_time) as slotHour
                from slot s
                left join court c on c.id = s.court_id
                ${sportJoin}
            where c.facility_id = :facilityId and
                DATE(s.start_time) = DATE(:date) and
                s.start_time > now() and
                c.offline_only is false
                ${sportCondition}
            ;
        """, [facilityId: facilityId, date: date.toDate()])?.collect { new DateTime(it.slotHour).toString("HH:mm") }.sort()

        groovySql.close()
        return result
    }

    def getSlotSummary(Facility facility, def from, def to, def courts) {
        String sql = """select s.id as slot_id,
                               s.start_time,
                               s.end_time,
                               c.id as court_id,
                               c.name as court_name,
                               c.list_position as court_position,
                               c.members_only as members_only,
                               c.offline_only as offline_only,
                               c.indoor as indoor,
                               c.surface as court_surface,
                               sp.id as sport_id,
                               sp.name as sport_name,
                               (select group_concat(distinct concat(cam.camera_id, ':' , cam.name)) as cameras
                                from camera cam
                                         left join court c3 on cam.court_id = c3.id
                                where c3.id = c.id) as cameras,
                               b.id as booking_id,
                               b.customer_id as booking_customer_id,
                               b.comments as booking_comments,
                               bg.type as booking_type,
                               f.show_booking_holder,
                               coalesce(concat(cu.firstname, ' ', cu.lastname), cu.companyname) as booking_name,
                               a.name as activity_name,
                               (select group_concat(coalesce(concat(c2.firstname, ' ', c2.lastname), coalesce(c2.companyname, 'n/a'))) as players
                                from player p2
                                    left join customer c2 on p2.customer_id = c2.id
                                where p2.booking_id = b.id) as players
                        from slot s
                        join court c on s.court_id = c.id
                        join facility f on c.facility_id = f.id
                        join sport sp on c.sport_id = sp.id
                        left join booking b on s.id = b.slot_id
                        left join booking_group bg on b.group_id = bg.id
                        left join activity_occasion_booking on b.id = activity_occasion_booking.booking_id
                        left join activity_occasion ao on activity_occasion_booking.activity_occasion_id = ao.id
                        left join activity a on ao.activity_id = a.id
                        left join customer cu on b.customer_id = cu.id
                        where s.end_time > ? 
                        and s.end_time <= ?
                        and f.id = ?
                        and c.archived = false
                        order by s.start_time, c.list_position;"""

        def parameters = [ from, to, facility.id ]
        def rows = groovySql.rows(sql, parameters).findAll {
            if (courts.contains(it.court_id)) {
                return it
            }
        }

        def result = []
        rows.each {
            result << new APIExtSlot(it)
        }

        groovySql.close()
        return result
    }
}

class SlotsGroup {
    int hour
    int minute
    Duration duration
    List<Slot> slots = []
}
