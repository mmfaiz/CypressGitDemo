package com.matchi.api.v2

import com.matchi.Booking
import com.matchi.Court
import com.matchi.CourtGroup
import com.matchi.CourtTypeAttribute
import com.matchi.CourtTypeEnum
import com.matchi.Customer
import com.matchi.Facility
import com.matchi.FacilityProperty.FacilityPropertyKey
import com.matchi.Slot
import com.matchi.Sport
import com.matchi.User
import com.matchi.api.APIException
import com.matchi.api.GenericAPIController
import com.matchi.coupon.PromoCode
import com.matchi.payment.BookingRestrictionException
import com.matchi.payment.InvalidPriceException
import com.matchi.price.Price
import com.matchi.slots.SlotFilter
import grails.converters.JSON
import grails.validation.Validateable
import org.joda.time.DateTime
import org.joda.time.Interval
import org.joda.time.LocalDate
import org.joda.time.LocalTime
import org.joda.time.Minutes
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import org.joda.time.format.ISODateTimeFormat

import com.matchi.api.Code
import org.springframework.http.HttpStatus

import java.math.RoundingMode
import java.text.DecimalFormat

class SlotResourceController extends GenericAPIController {
    private static final DateTimeFormatter HH_MM = DateTimeFormat.forPattern("HH:mm")

    static namespace = "v2"

    def slotService
    def courtService
    def priceListService

    public static enum ErrorCode {
        LIMIT_REACHED,
        COURT_GROUP,
        TOO_SOON,
        NOT_BOOKABLE,
        MEMBERS_ONLY,
        CUSTOMER_PROFILE
    }

    def list(RetrieveFreeSlotsCommand cmd) {
        if (params.from && params.to) {
            cmd.from = new LocalDate(params.from)
            cmd.to = new LocalDate(params.to)
        }

        if (!cmd.validate()) {
            render cmd.errors as JSON
            return
        }

        def sports = cmd.sportIds ? Sport.findAllByIdInList(cmd.sportIds) : Sport.list()

        def facilities = cmd.facilityIds ?
                Facility.findAllByIdInList(cmd.facilityIds, [cached: true]) : Facility.activeFacilities.list(max: 15).unique()

        Map<String, String> courtTypesFilter = [:]
        cmd.courtTypes.each {
            def split = it.split("=")
            courtTypesFilter.put(split[0], split[1])
        }

        List<Court> courts = courtService.findUsersCourts(facilities, sports, null, cmd.indoor, getCurrentUser(), cmd.hasCamera, courtTypesFilter)

        if (facilities.isEmpty()) {
            error(400, Code.INPUT_ERROR, "Could not find any facility ${cmd.facilityIds}")
            return
        }

        def filter = getSlotFilter(cmd, courts)
        List<Slot> slots = slotService.getSlots(filter)

        Map<Long, List<Court>> courtsByFacility = courts.groupBy { it.facility.id }
        Map<Long, List<Slot>> slotsByCourt = slots.groupBy { it.court.id }
        Map<String, List<Slot>> additionalAdjacentSlotsBySlot = getAdditionalAdjacentSlots(cmd, slots)

        def result = []

        facilities.each { Facility fac ->
            def facility = [
                    id          : fac.id,
                    name        : fac.name,
                    courts      : [],
                    restrictions: []
            ]

            int bookingLimitForUser = fac.getBookingRuleNumDaysBookableForUser(getCurrentUser())
            Customer customer = Customer.findByUserAndFacility(getCurrentUser(), fac)
            def bookingAllowed = isBookingAllowed(customer, fac)

            List<CourtGroup> groups = CourtGroup.facilityCourtGroups(fac).list()
            Map<Long, Integer> courtBookings = customer ?
                Booking.upcomingBookings(customer).list().groupBy {Booking booking -> booking.slot.courtId}.collectEntries {[it.key, it.value.size()]} as Map<Long, Integer> :
                new HashMap<Long, Integer>()
            Map<Long, Integer> groupBookings = groups.collectEntries{ group ->
                [group.id, group.courts.inject(0) { count, court -> count + (courtBookings[court.id] ?: 0) }]
            } as Map<Long, Integer>

            if (!bookingAllowed) {
                facility.unavailableCode = ErrorCode.LIMIT_REACHED.toString()
                facility.restrictions << ["type"    : ErrorCode.LIMIT_REACHED.toString(),
                                          value     : fac.getFacilityPropertyValue(FacilityPropertyKey.MAXIMUM_NUMBER_OF_BOOKINGS_PER_USER.name()).toInteger()]
            }
            if (!fac.isBookableForLimit(new DateTime(cmd.from.toDate()), bookingLimitForUser)) {
                facility.unavailableCode = ErrorCode.TOO_SOON.toString()
                facility.restrictions << ["type": ErrorCode.TOO_SOON.toString(), value: bookingLimitForUser]
                facility.daysLimit = bookingLimitForUser
            }
            if (!fac.bookable) {
                facility.unavailableCode = ErrorCode.NOT_BOOKABLE.toString()
                facility.restrictions << ["type": ErrorCode.NOT_BOOKABLE.toString()]
            }
            if (fac.isMembersOnly() && !getCurrentUser()?.hasActiveMembershipIn(fac) && !getCurrentUser()?.getMembershipIn(fac)?.inStartingGracePeriod) {
                facility.unavailableCode = ErrorCode.MEMBERS_ONLY.toString()
                facility.restrictions << ["type": ErrorCode.MEMBERS_ONLY.toString()]
            }

            // facility courts
            courtsByFacility.get(facility.id)?.each { Court c ->
                def court = [id: c.id, name: c.name, indoor: c.indoor, sport: c.sport, surface: c.surface.toString(), slots: [], cameras: c.facility.hasCameraFeature() ? c.cameras : [], courtTypes: [:]]

                CourtTypeEnum.getBySport(c.sport).each { CourtTypeEnum courtTypeEnum ->
                    court.courtTypes.putAt(courtTypeEnum.name(), c.courtTypeAttributes.find { CourtTypeAttribute it ->
                        it.courtTypeEnum == courtTypeEnum
                    }?.value ?: "")
                }

                if (fac.bookable && (cmd.showNotBookable || facility.restrictions.empty)) {
                    // court slots
                    getBookableSlots(slotsByCourt.get(c.id), fac, bookingLimitForUser).each { Slot slot ->
                        def slotOnCourt = getDefaultSlotOnCourt(slot)
                        slotOnCourt.restrictions += facility.restrictions

                        if (fac.hasBookingRestrictions() && slot.bookingRestriction &&
                                !slot.bookingRestriction.accept(customer ?: new Customer(), slot))
                            slotOnCourt.restrictions  << ["type"   : ErrorCode.CUSTOMER_PROFILE.toString(),
                                                           value    : slot.bookingRestriction.requirementProfiles.collect{ it.name }.join(", ")]
                        populatePrice(cmd.showPrices, slotOnCourt, slot)

                        if(!slotOnCourt.restrictions.find {it.type == ErrorCode.LIMIT_REACHED.toString()}) {
                            def groupRestriction = getBookingRestrictionForCourt(customer, c, groups, groupBookings)
                            if (groupRestriction) {
                                slotOnCourt.restrictions << ["type": ErrorCode.COURT_GROUP.toString(),
                                                             "name": groupRestriction.groupName,
                                                             value : groupRestriction.maxNumber]
                            }
                        }

                        if (cmd.fetchAdjacentSlots) {
                            def adjacentSlots = getAdjacentSlots(cmd, slot, additionalAdjacentSlotsBySlot)

                            int adjustmentNumber = 0
                            adjacentSlots.each { Slot adjacentSlot ->
                                def resultedAdjacentSlot = getAdjacentSlots(adjacentSlot)
                                resultedAdjacentSlot.restrictions += facility.restrictions

                                if (fac.hasBookingRestrictions() && adjacentSlot.bookingRestriction &&
                                        !adjacentSlot.bookingRestriction.accept(customer ?: new Customer(), adjacentSlot)) {
                                    resultedAdjacentSlot.restrictions << ["type": ErrorCode.CUSTOMER_PROFILE.toString(),
                                                                          value : adjacentSlot.bookingRestriction.requirementProfiles.collect { it.name }.join(", ")]
                                }
                                if (!resultedAdjacentSlot.restrictions.find {it.type == ErrorCode.LIMIT_REACHED.toString()}) {
                                    Boolean adjustmentAllowed = isAdjustmentAllowed(customer, fac, ++adjustmentNumber + (customer ? Booking.upcomingBookings(customer).count() : 0))
                                    def groupRestriction = getBookingRestrictionForCourt(customer, c, groups, groupBookings, adjustmentNumber)
                                    if (!adjustmentAllowed)
                                        resultedAdjacentSlot.restrictions  << ["type"    : ErrorCode.LIMIT_REACHED.toString(),
                                                                               value     : fac.getFacilityPropertyValue(FacilityPropertyKey.MAXIMUM_NUMBER_OF_BOOKINGS_PER_USER.name()).toInteger()]
                                    if (groupRestriction) {
                                        resultedAdjacentSlot.restrictions << ["type": ErrorCode.COURT_GROUP.toString(),
                                                                 "name": groupRestriction.groupName,
                                                                 value : groupRestriction.maxNumber]
                                    }
                                }

                                populatePrice(cmd.showPrices, resultedAdjacentSlot, adjacentSlot)
                                slotOnCourt.adjacentSlots << resultedAdjacentSlot
                                if (!cmd.showNotBookable)
                                    slotOnCourt.adjacentSlots = slotOnCourt.adjacentSlots.findAll {it.restrictions.empty}
                            }
                        }

                        court.slots << slotOnCourt
                    }
                    if (!cmd.showNotBookable)
                        court.slots = court.slots.findAll {it.restrictions.empty}
                }

                facility.courts << court
            }

            result << facility
        }

        if (params.boolean("aggregated")) {
            render aggregated(result) as JSON
        } else {
            render result as JSON
        }
    }

    def getCourtGroups(def id, def facility) {
        List<CourtGroup> groups = CourtGroup.facilityCourtGroups(facility).list()
        groups.findAll { it.courts.collect { it.id }.contains(id) && it.maxNumberOfBookings }
    }
    /**
     * The purpose of this method is to improve mobile app performance by
     * executing some of the necessary logic service side instead of in the client.
     * @param input
     */
    def aggregated(def input) {
        def result = []

        input.each { f ->
            List<String> times = new ArrayList<>()

            f.courts.each { c ->
                c.slots.each { s ->
                    if (s.start != null) {
                        times.add(HH_MM.print(new DateTime(s.start).toLocalTime()))
                    }
                }
            }

            // Remove duplicate times and sort
            List aggregated = new ArrayList<>(new HashSet<>(times))
            Collections.sort(aggregated)

            result << [
                    id             : f.id,
                    name           : f.name,
                    availableTimes : aggregated,
                    unavailableCode: f.unavailableCode
            ]
        }

        return result
    }

    private static List<Slot> getBookableSlots(List<Slot> slots, Facility facility, int bookingLimitForUser) {
        slots.findAll { Slot slot ->
            slot.booking == null && slot.isBookable()
        }
    }

    private static Map getDefaultSlotOnCourt(Slot slot) {
        DateTime startTime = new DateTime(slot.startTime)
        DateTime endTime = new DateTime(slot.endTime)

        return [
                id           : slot.id,
                duration     : Minutes.minutesBetween(startTime, endTime).getMinutes(),
                start        : ISODateTimeFormat.dateTime().print(startTime),
                end          : ISODateTimeFormat.dateTime().print(endTime),
                adjacentSlots: [],
                restrictions: []
        ]
    }

    private static Map getAdjacentSlots(Slot adjacentSlot) {
        DateTime adjacentSlotStartTime = new DateTime(adjacentSlot.startTime)
        DateTime adjacentSlotEndTime = new DateTime(adjacentSlot.endTime)

        return [
                id          : adjacentSlot.id,
                duration    : Minutes.minutesBetween(adjacentSlotStartTime, adjacentSlotEndTime).getMinutes(),
                start       : ISODateTimeFormat.dateTime().print(adjacentSlotStartTime),
                end         : ISODateTimeFormat.dateTime().print(adjacentSlotEndTime),
                restrictions: []]
    }

    private static List<Slot> getAdjacentSlots(RetrieveFreeSlotsCommand cmd, Slot slot, Map<String, List<Slot>> additionalAdjacentSlotsBySlot) {
        Integer maxSize = cmd.fetchAdjacentSlots - 1
        List<Slot> adjacentSlots = additionalAdjacentSlotsBySlot[slot.id]
        if (adjacentSlots.size() < cmd.fetchAdjacentSlots) {
            maxSize = adjacentSlots.size() - 1
        }

        // [0..0] would still get 1 item
        if (maxSize < 0) {
            adjacentSlots = []
        } else {
            adjacentSlots = adjacentSlots[0..maxSize]
        }
        return adjacentSlots
    }

    private Boolean isBookingAllowed(Customer customer, Facility facility) {
        Boolean bookingAllowed = true
        if (facility.isFacilityPropertyEnabled(
                FacilityPropertyKey.FEATURE_MAXIMUM_NUMBER_OF_BOOKINGS_PER_USER.name())) {
            if (customer && !customer.exludeFromNumberOfBookingsRule
                    && facility.getFacilityPropertyValue(FacilityPropertyKey.MAXIMUM_NUMBER_OF_BOOKINGS_PER_USER.name()).toInteger() <= Booking.upcomingBookings(customer).count()) {
                bookingAllowed = false
            }
        }
        return bookingAllowed
    }

    private def getBookingRestrictionForCourt(Customer customer, Court court, List<CourtGroup> groups, Map<Long, Integer> groupBookings, int adjustmentNumber = 0) {
        def result
        if (court.facility.isFacilityPropertyEnabled(
            FacilityPropertyKey.FEATURE_MAXIMUM_NUMBER_OF_BOOKINGS_PER_COURT_GROUP.name()) && !customer?.exludeFromNumberOfBookingsRule) {
            groups.findAll { it.courts.collect { it.id }.contains(court.id) && it.maxNumberOfBookings }.each {
                if (it.maxNumberOfBookings <= groupBookings[it.id] + adjustmentNumber)
                    result = [groupName: it.name, maxNumber: it.maxNumberOfBookings]
                    return
            }
        }
        return result
    }

    private Boolean isAdjustmentAllowed(Customer customer, Facility facility, int upcomingBookings) {
        if (facility.isFacilityPropertyEnabled(
                FacilityPropertyKey.FEATURE_MAXIMUM_NUMBER_OF_BOOKINGS_PER_USER.name())) {
            if (!customer?.exludeFromNumberOfBookingsRule
                    && facility.getFacilityPropertyValue(FacilityPropertyKey.MAXIMUM_NUMBER_OF_BOOKINGS_PER_USER.name()).toInteger() <= upcomingBookings) {
                return false
            }
        }
        return true
    }

    private static SlotFilter getSlotFilter(RetrieveFreeSlotsCommand cmd, def courts) {
        LocalTime fixedStartTime = null
        if (cmd.fixedStartTime) {
            fixedStartTime = new LocalTime(cmd.fixedStartTime)
        }

        Interval interval = cmd.toInterval()

        return new SlotFilter(
                from: interval.start,
                to: interval.end,
                courts: courts,
                onlyFreeSlots: true,
                fixedStartTime: fixedStartTime,
        )
    }

    private Map<String, List<Slot>> getAdditionalAdjacentSlots(RetrieveFreeSlotsCommand cmd, List<Slot> slots) {
        def additionalAdjacentSlotsBySlot = [:]
        if (slots && cmd.fetchAdjacentSlots > 0) {
            List adjacentSlots = slotService.createAdjacentSlotGroupsWithSubsequentSlots(slots)
            adjacentSlots.each {

                def slotList = it.getSubsequentSlots()
                it.getSubsequentSlots().each { Slot slot ->
                    additionalAdjacentSlotsBySlot[slot.id] = slotList.clone() - slot
                    slotList.remove(slot)
                }

            }
        }
        return additionalAdjacentSlotsBySlot
    }

    private Boolean isPriceCalculated(Slot slot) {
        slot.court?.facility?.isFacilityPropertyEnabled(
                FacilityPropertyKey.FEATURE_CALCULATE_MULTIPLE_PLAYERS_PRICE.name())
    }

    private Price getPriceForSlot(Boolean calculatedPrice, Slot slot, User user) {
        def price
        if (calculatedPrice) {
            price = priceListService.getAvgBookingPrice(slot, user)
        } else {
            price = priceListService.getBookingPrice(slot, user)
        }
        return price
    }

    private def populatePrice(Boolean showPrices, def objectToPopulate, Slot slot) {
        if (showPrices) {
            def calculatedPrice = isPriceCalculated(slot)
            def price = getPriceForSlot(calculatedPrice, slot, getCurrentUser() as User)
            if (calculatedPrice || price.valid()) {
                objectToPopulate.price = price.price
                objectToPopulate.vat = price.VATAmount
                objectToPopulate.currency = slot.court?.facility?.currency
            }
        }
    }

    def price() {
        def cmd = new RetrieveSlotOrderCommand()
        bindData(cmd, request?.JSON)

        def result = [
                prices        : [],
                paymentMethods: [:]
        ]

        def slots = cmd.toSlots()
        List<Price> prices = []

        // Make sure same facility on each slot
        // Not handled by the app in any special way and the app restricts this case anyway,
        // but we add it here to avoid unexpected problems down the line
        if (slots.collect { Slot slot -> slot.court.facility.id }.toSet().size() > 1) {
            throw new APIException(400, Code.INPUT_ERROR, "Trying to book at more than 1 facility")
        }
        slots.each { slot ->
            def calculatedPrice = isPriceCalculated(slot)
            def price = getPriceForSlot(calculatedPrice, slot, getCurrentUser() as User)

            if (calculatedPrice || price.valid()) {
                result.prices << [
                        id      : slot.id,
                        price   : price.price,
                        vat     : price.VATAmount,
                        currency: slot.court?.facility?.currency
                ]

                prices << price
            }

        }

        Long totalPrice = prices.sum { Price price -> price.price }

        if (!prices.isEmpty())
            result.paymentMethods = getPaymentMethods(getCurrentUser() as User, slots, totalPrice)

        Slot firstSlot = slots ? slots.first() : null

        if (firstSlot) {
            Facility facility = firstSlot.court.facility
            result.facilityCancellationLimit = facility.getBookingCancellationLimit()
            result.facilityFeatureCalculateMultiplePlayersPrice = facility.isFacilityPropertyEnabled(
                    FacilityPropertyKey.FEATURE_CALCULATE_MULTIPLE_PLAYERS_PRICE.name())
        }

        render result as JSON
    }

    def priceModel() {
        def cmd = new RetrieveSlotOrderCommand()
        bindData(cmd, request?.JSON)

        def result = [
                price         : [:],
                slots         : [],
                players       : [],
                paymentMethods: [:]
        ]

        def slots = cmd.toSlots()

        if (slots.isEmpty()) {
            render result as JSON
            return
        }

        List<Price> prices = []

        // Make sure same facility on each slot
        // Not handled by the app in any special way and the app restricts this case anyway,
        // but we add it here to avoid unexpected problems down the line
        if (slots.collect { Slot slot -> slot.court.facility.id }.toSet().size() > 1) {
            throw new APIException(400, Code.INPUT_ERROR, "Trying to book at more than 1 facility")
        }

        Facility facility = slots.first().court?.facility
        Customer customer = ((getCurrentUser() && facility) ? Customer.findByUserAndFacility(getCurrentUser(), facility) : null) ?: new Customer(facility: facility)
        Map<String, Map> priceCalcLog = [:]


        result.slots = slots.collect { Slot slot ->
            try {
                ["id": slot.id, "price": priceListService.getPriceForSlot(slot, customer, cmd.playerEmails, null, priceCalcLog).price]
            } catch (BookingRestrictionException bre) {
                log.info("Booking restriction for slot ${slot.id}", bre)
                return error(400, Code.INPUT_ERROR, "Booking restriction for slot ${slot.id}")
            } catch (InvalidPriceException ipe) {
                log.info("Invalid price for slot ${slot.id}", ipe)
                return error(400, Code.INPUT_ERROR, "Invalid price for slot ${slot.id}")
            }
        }


        def playersMap = [:]

        priceCalcLog.each { courtName, data ->
            data.players.each {
                playersMap[it.key] = (playersMap[it.key] ?: 0) + it.value
            }
        }

        result.players = playersMap.collect { [email: it.key, price: it.value] }
        if (result.players.isEmpty() && cmd.playerEmails != null) {
            result.players = cmd.playerEmails.collect { [email: it, price: null] }
        }

        Long totalPrice = result.slots.sum { it.price }

        if (cmd.promoCodeId) {
            int decimalPoints = grailsApplication.config.matchi.settings.currency[facility.currency].decimalPoints
            PromoCode promoCode = PromoCode.findByIdAndFacility(cmd.promoCodeId, facility)
            if (promoCode?.discountPercent) {
                totalPrice = ((100 - promoCode.discountPercent).divide(100) * totalPrice).setScale(decimalPoints, RoundingMode.HALF_UP)
                result.slots.each { slot ->
                    slot.price = ((100 - promoCode.discountPercent).divide(100) * slot.price).setScale(decimalPoints, RoundingMode.HALF_UP)
                }
            } else if (promoCode?.discountAmount) {
                def newPrice = (totalPrice - promoCode.discountAmount).max(0)
                def discountProportion = newPrice / totalPrice
                def totalDiscounted = 0
                result.slots.each { slot ->
                    def newSlotPrice = new BigDecimal(slot.price * discountProportion).setScale(decimalPoints, RoundingMode.HALF_UP)
                    totalDiscounted += (slot.price - newSlotPrice)
                    slot.price = newSlotPrice
                }
                Double extraPenny = totalPrice - newPrice - totalDiscounted
                def lastSlot = result.slots.last()
                lastSlot.price = lastSlot.price + extraPenny
                totalPrice = newPrice
            }
        }

        Long baseprice = slots.sum { Slot slot -> priceListService.getBookingPrice(slot, customer).price }

        result.price = [
                base    : baseprice,
                total   : totalPrice,
                currency: facility.currency,
                vat     : Price.calculateVATAmount(totalPrice, new Double(facility.vat))
        ]

        if (!result.slots.isEmpty())
            result.paymentMethods = getPaymentMethods(getCurrentUser() as User, slots, totalPrice)

        Slot firstSlot = slots ? slots.first() : null

        if (firstSlot) {
            result.facilityCancellationLimit = facility.getBookingCancellationLimit()
            result.facilityFeatureCalculateMultiplePlayersPrice = facility.isFacilityPropertyEnabled(
                    FacilityPropertyKey.FEATURE_CALCULATE_MULTIPLE_PLAYERS_PRICE.name())
        }

        render result as JSON
    }

    def promoCode() {
        def cmd = new RetrieveSlotOrderCommand()
        bindData(cmd, request?.JSON)
        def slots = cmd.toSlots()
        Facility facility = slots.first().court.facility
        User user = getCurrentUser()
        def promoCodeValidation = couponService.checkPromoCode(facility, user, cmd.promoCode, slots)
        def result
        if (promoCodeValidation.valid) {
            DecimalFormat df = new DecimalFormat()
            df.setMinimumFractionDigits(0)
            result = [status         : HttpStatus.OK.value(), id: promoCodeValidation.id,
                      discountPercent: promoCodeValidation.discountPercent, discountAmount: promoCodeValidation.discountAmount]
        } else {
            error(400, promoCodeValidation.errorCode, message(code: promoCodeValidation.message))
        }
        render result as JSON
    }
}

enum BookingRestriction {
    CUSTOMER_PROFILE, TOO_MANY_BOOKINGS, TOO_EARLY
}

@Validateable(nullable = true)
class RetrieveSlotOrderCommand {
    List<String> slotIds
    List<String> playerEmails
    String promoCode
    Long promoCodeId

    static constraints = {
        slotIds(nullable: false, blank: false)
        playerEmails(nullable: true, blank: true)
        promoCodeId(nullable: true, blank: false)
        promoCode(nullable: true, blank: true)
    }

    def toSlots() {
        Slot.findAllByIdInList(slotIds)
    }

}

@Validateable(nullable = true)
class RetrieveFreeSlotsCommand {
    static long MAX_NUM_DAYS = 3

    LocalDate from
    LocalDate to
    String fixedStartTime
    List<Long> facilityIds = []
    List<Long> sportIds = []
    Boolean indoor
    Integer fetchAdjacentSlots = 0
    Boolean hasCamera
    Boolean showPrices
    Boolean showNotBookable
    List<String> courtTypes = []

    static constraints = {
        from(nullable: false)
        to(nullable: false,
                // validates that to >= from

                validator: { val, obj ->
                    if (val != null && obj.from != null) {
                        if (obj.from.isAfter(val)) {
                            return ["toBeforeFrom"]
                        }

                        if (obj.toInterval().toDuration().toStandardDays().days > MAX_NUM_DAYS) {
                            return ["toLargeInterval"]
                        }

                    }

                }
        )
        fixedStartTime(nullable: true)
        indoor(nullable: true)
        hasCamera(nullable: true)
        showNotBookable(nullable: true)

        facilityIds(validator: { val, obj ->
            if (!val && !obj.sportIds) {
                return ["facilityOrSport"]
            }
        })

    }

    /**
     * Converts to interval and adds one day to end date (since call is end inclusive)
     * @return
     */
    def toInterval() {
        new Interval(from.toDateTimeAtStartOfDay(), to.plusDays(1).toDateTimeAtStartOfDay())
    }
}