package com.matchi.facility

import com.matchi.*
import com.matchi.activities.trainingplanner.Trainer
import com.matchi.coupon.Coupon
import com.matchi.coupon.CustomerOfferGroup
import com.matchi.coupon.GiftCard
import com.matchi.enums.BookingGroupType
import com.matchi.excel.ExcelExportManager
import com.matchi.mpc.CodeRequest
import com.matchi.orders.OrderPayment
import com.matchi.payment.PaymentException
import com.matchi.payment.PaymentStatus
import com.matchi.play.PlayService
import com.matchi.play.Recording
import com.matchi.schedule.TimeSpan
import com.matchi.slots.SlotFilter
import grails.converters.JSON
import grails.plugin.mail.GrailsMailException
import grails.plugins.rest.client.RestResponse
import grails.validation.Validateable
import org.apache.commons.lang.time.StopWatch
import org.joda.time.DateTime
import org.joda.time.Duration
import org.joda.time.format.DateTimeFormat
import org.springframework.beans.factory.annotation.Value
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.security.core.context.SecurityContextHolder

import javax.servlet.http.HttpServletResponse
import java.text.SimpleDateFormat
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class FacilityBookingController extends GenericBookingController {

    public static final String CURRENT_DATE_HEADER = "X-Current-Date"
    public static final String CURRENT_WEEK_HEADER = "X-Current-Week"

    def userService
    def dateUtil
    def scheduleService
    def bookingService
    def facilityService
    def slotService
    def subscriptionService
    def priceListService
    def couponService
    def paymentService
    def customerService
    def invoiceService
    def bookingAvailabilityService
    def courtRelationsBookingService
    def memberService
    def mpcService
    PlayService playService

    @Value('${matchi.booking.cancel.maxBatchSize}')
    Integer cancelMaxBatchSize

    def index() {
        User user = getCurrentUser()
        if (!user) {
            redirect controller: "login", action: "auth", params: params
            return
        }

        def facility = user.facility
        def startHour
        def endHour
        def timeSpans
        def date = new Date()

        if (params.date) {
            date = new SimpleDateFormat("yyyy-MM-dd").parse(params.date)
        }

        DateTime start = dateUtil.beginningOfDay(date)
        DateTime end = dateUtil.endOfDay(date)

        SlotFilter slotFilter = new SlotFilter(from: start, to: end,
                courts: Court.available(facility).list())
        def schedule = scheduleService.facilitySchedule(facility, slotFilter)

        if (!schedule.isEmpty()) {

            timeSpans = dateUtil.createTimeSpans(
                    new DateTime(date),
                    schedule?.firstSlot()?.start?.getHourOfDay(),

                    // minus one millis for slots that ends on even our
                    schedule?.lastSlot()?.end?.minusMillis(1)?.getHourOfDay())
        } else {
            startHour = facility.getOpeningHour(new DateTime(date).getDayOfWeek()) ?: 0
            endHour = facility.getClosingHour(new DateTime(date).getDayOfWeek()) ?: 23
            timeSpans = dateUtil.createTimeSpans(new DateTime(date), startHour, endHour)
        }

        def courtGroups = CourtGroup.facilityCourtGroups(facility).list().findAll {
            it.courts.any { !it.archived } && it.visible
        }

        if (params.update) {
            header CURRENT_DATE_HEADER, formatDate(date: date, format: "EEEE d MMMM", locale: user.language)
            header CURRENT_WEEK_HEADER, message(code: "facilityBooking.index.message8") + " " +
                    formatDate(date: date, format: "w", locale: user.language)
            render(template: "../templates/bookingSchedules/facilityDailyScheduleWrap",
                    model: [facility: facility, timeSpans: timeSpans, schedule: schedule, date: new DateTime(date), courtGroups: courtGroups])
            return
        }

        return [facility: facility, schedule: schedule, timeSpans: timeSpans, date: new DateTime(date), user: user, courtGroups: courtGroups]
    }

    def checkUpdate() {
        def facility = (Facility) getUserFacility()
        def date = new Date()

        if (params.date) {
            date = new SimpleDateFormat("yyyy-MM-dd").parse(params.date)
        }
        DateTime start = dateUtil.beginningOfDay(new DateTime(date))
        DateTime end = dateUtil.beginningOfDay(new DateTime(start).plusDays(1))

        def scheduleInfo = slotService.getScheduleSlotInfo(start.toDate(), end.toDate(), facility.id)

        if (params.scheduleInfo?.toString().equals(scheduleInfo)) {
            response.status = 304
            render status: 304
            return
        }

        response.status = 200
        def result = [result: scheduleInfo.toString()]
        render result as JSON
    }

    def list(FilterBookingsCommand cmd) {
        def facilityBookingListConfig = MatchiConfig.findByKey(MatchiConfigKey.DISABLE_FACILITY_BOOKING_LIST)
        if (facilityBookingListConfig.isBlocked()) {
            render facilityBookingListConfig.isBlockedMessage()
        }

        def facility = getUserFacility()

        DateTime start, end
        (end, start) = parseStartAndEnd()

        if (end.isBefore(start)) {
            flash.error = message(code: "facilityBooking.list.invalidDates")
            end = start.plusDays(1)
        }

        start = dateUtil.beginningOfDay(start)
        end = dateUtil.beginningOfDay(end)

        def timespan = new TimeSpan(start, end)
        def bookings = bookingService.searchBooking(facility, timespan, cmd)
        User user = getCurrentUser()

        def totalPrice = bookings.sum(0) {
            priceListService.getBookingPrice(it.slot, it.customer).price
        }

        return [facility      : facility, timespans: timespan, start: start, end: end, bookings: bookings,
                cmd           : cmd, totalPrice: totalPrice, bookingTypes: bookingTypes(facility), user: user,
                facilityGroups: Group.findAllByFacility(facility, [sort: "name"])]
    }

    private def parseStartAndEnd() {
        def start = null
        def end = null
        def savedRange = session["matchi.facility.booking.list.range"]

        if (!start && params.start && params.end) {
            start = parseDateParam(params.start)
            end = parseDateParam(params.end)
        }

        if (!start && params.date) {
            start = parseDateParam(params.date)
        }

        if (savedRange) {
            if (!start) {
                start = savedRange.start
            }

            if (!end) {
                def duration = new Duration(savedRange.start, savedRange.end);
                end = start.plus(duration)
            }
        }

        if (!start) {
            start = new DateTime()
        }

        if (!end) {
            end = new DateTime()
        }

        session["matchi.facility.booking.list.range"] = [end: end, start: start]

        [end, start]
    }

    private DateTime parseDateParam(String date) {
        DateTime start = null
        try {
            start = DateTimeFormat.forPattern("yyyy-MM-dd").parseDateTime(date)
        } catch (IllegalArgumentException e) {
            log.info("Error parsing date ${date}")
        }
        start
    }

    def bookingForm() {
        List<Slot> slots = []
        Facility facility = getUserFacility()
        if (params.slotId) {
            slots = slotService.getSlots(Slot.parseAll(params.slotId))
        }

        String accessCode
        def warnAboutCodeRequest = false
        if (!slots.isEmpty()) {
            Booking booking = slots.first()?.booking
            accessCode = booking?.getAccessCode()

            if (facility?.hasMPC()) {
                def codeRequest = CodeRequest.findByBooking(booking)
                warnAboutCodeRequest = codeRequest?.hasProblems()
            }
        }

        Slot firstSlot = slots?.first()
        Booking booking = firstSlot?.belongsToSubscription() ?
                    firstSlot?.subscription?.bookingGroup?.bookings?.find { it?.slot == firstSlot } : firstSlot?.booking as Booking
        def optionUpdateGroup = booking?.group?.bookings?.size() > 1
        def isRefundable = booking?.order?.isStillRefundable()
        def isRestricted = facility.hasBookingRestrictions() && slots?.any { it.bookingRestriction }
        Recording recording = playService.getRecordingFromBooking(booking)

        // If everyone is equally restricted as the first slot, then they all equal each other
        boolean sameRestrictions = isRestricted && !slots?.tail()?.any { Slot s -> !s.isEquallyRestrictedAs(slots.first()) }

        render(view: "facilityBookingForm", model: [
                user                : getCurrentUser(),
                facility            : getUserFacility(),
                slots               : slots,
                accessCode          : accessCode,
                warnAboutCodeRequest: warnAboutCodeRequest,
                bookingTypes: bookingTypes(facility),
                trainers: Trainer.findAllByFacilityAndIsActive(facility, true),
                date:params.date,
                optionUpdateGroup:optionUpdateGroup,
                isRefundable: isRefundable,
                isRestricted: isRestricted,
                sameRestrictions: sameRestrictions,
                recording: recording
                ])
    }

    def bookingAlterForm() {
        log.debug("BookingAlterForm slotIds: ${params.alterSlotsData}")

        def slots = []
        if (params.alterSlotsData) {
            slots = slotService.getSlots(Slot.parseAll(params.alterSlotsData))
        }

        Facility facility = getUserFacility()

        render(view: "facilityBookingForm", model: [
                user        : getCurrentUser(),
                facility    : facility,
                slots       : slots,
                bookingTypes: bookingTypes(facility),
                date        : params.date])
    }

    def bookingFormInfo() {
        def booking = bookingService.getBooking(params.long("bookingId"))
        def slot = slotService.getSlot(params.slotId)
        render(g.bookingFormInfo(booking: booking, slots: [slot]))
    }

    def extraInfo() {
        Booking booking = bookingService.getBooking(params.long("bookingId"))

        if (booking.customer.facility.id != getUserFacility().id) {
            log.info("User ${getCurrentUser().id} tries to access booking ${booking.id} without permission")
            return
        }

        Map mpcInfo = [:]

        if (booking.order.facility.hasMPC()) {
            RestResponse codeRequest

            try {
                CodeRequest internalCodeRequest = CodeRequest.findByBooking(booking)
                if (internalCodeRequest && internalCodeRequest.status != CodeRequest.Status.PENDING) {
                    codeRequest = mpcService.get(internalCodeRequest)
                }
            } catch (Exception e) {
                log.error("Failure in CodeRequest handling", e)
            }

            if (codeRequest) {
                Court court = Court.findById(codeRequest?.json?.court[0] as Long)

                DateTime codeValidStart = new DateTime(codeRequest?.json?.start[0])
                DateTime codeValidEnd = new DateTime(codeRequest?.json?.end[0])

                DateTime bookingStart = codeValidStart.plusMinutes(booking.order.facility.mlcsGraceMinutesStart)
                DateTime bookingEnd = codeValidEnd.minusMinutes(booking.order.facility.mlcsGraceMinutesEnd)

                String status = codeRequest?.json?.status[0]

                mpcInfo = [codeValidStart: codeValidStart, codeValidEnd: codeValidEnd, bookingStart: bookingStart, bookingEnd: bookingEnd, court: court, status: status]
            }
        }

        render(view: "extraInfo", model: [booking: booking, mpcInfo: mpcInfo])
    }

    def newCustomerBookingForm() {
        def facility = getUserFacility()

        def slots = []
        if (params.slotId) {
            // adding new booking(s)
            slots = slotService.getSlots(Slot.parseAll(params.slotId))
        }

        render(view: "facilityNewCustomerBookingForm", model: [
                memberTypes : memberService.getFormMembershipTypes(facility),
                facility    : facility,
                slots       : slots,
                bookingTypes: bookingTypes(facility),
                date        : params.date])
    }

    def cancelForm() {
        log.debug(params)
        def slots = Slot.parseAll(params.cancelSlotsData)
        if (slots.size() > cancelMaxBatchSize) {
            render status: 400
            return;
        }
        def bookings = bookingService.getBookingsBySlots(slots)
        if (bookings.size() == 0) {
            flash.error = message(code: "facilityBooking.book.error.cancelled")
            render view: "error"
            return
        }

        def facility = getUserFacility()
        def optionCancelRecurring = false
        def warnActivityBooking = 0
        def optionRedeem = false
        Boolean allRefundOption = false
        Boolean refundOption = false
        Boolean noRefundOption = false

        bookings.each { Booking booking ->
            if (booking.group != null && !booking.slot.subscription) {

                if (booking.group.type.equals(BookingGroupType.ACTIVITY)) {
                    warnActivityBooking++;
                }

                if (bookingService.getRecurringBookings(booking)?.size() > 0) {
                    optionCancelRecurring = true
                }
            }
            if (facility.subscriptionRedeem && booking?.slot?.subscription
                    && booking?.customer == booking?.slot?.subscription?.customer
                    && (!booking?.group || booking.group.type == BookingGroupType.SUBSCRIPTION)) {
                optionRedeem = true
            }
            allRefundOption = (booking?.startsWithinSixHours() || booking?.order?.facility?.isLateCancellation(new DateTime(booking?.slot?.startTime))) && booking?.order?.isStillRefundable()
            refundOption = booking?.startsMoreThanSixHours() && !booking?.order?.facility?.isLateCancellation(new DateTime(booking?.slot?.startTime))
            noRefundOption = !booking?.order?.isStillRefundable()
        }

        def bookingsToBeRefunded = bookings.findAll() { it.payment?.isCoupon() || (it.payment?.isCreditCard() && it.order?.isStillRefundable()) }
        def warnNoRefund = bookings.any() { !it.order?.isStillRefundable() }

        if (warnActivityBooking > 0) {
            flash.error = message(code: "facilityBooking.cancelForm.error", args: [warnActivityBooking])
        }

        render(view: "facilityBookingCancel", model: [bookings: bookings, optionCancelRecurring: optionCancelRecurring,
                                                      facility: facility, optionRedeem: optionRedeem, bookingsToBeRefunded: bookingsToBeRefunded, warnNoRefund: warnNoRefund, allRefundOption: allRefundOption, refundOption: refundOption, noRefundOption: noRefundOption])
    }

    def moveBookingForm() {
        def model = []
        if (params.bookingSlotId && params.slotId) {
            def bookingSlot = Slot.get(params.bookingSlotId)
            def slot = Slot.get(params.slotId)
            model = [slot: slot, bookingSlot: bookingSlot, date: params.date]
            if (slot.booking?.isActivity() && slot.booking.group?.bookings?.size() > 1) {
                model.moveError = message(code: "facilityBooking.facilityMoveBookingForm.moveToGroupActivityError")
            }
        }

        render(view: "facilityMoveBookingForm", model: model)
    }

    def cancel(CancelBookingCommand cmd) {
        def slots = Slot.parseAll(cmd.cancelSlotsData)
        if (slots.size() > cancelMaxBatchSize) {
            flash.error = message(code: "facilityBooking.list.cancelLimit",
                    args: [cancelMaxBatchSize])
            redirect(action: "list")
            return;
        }

        StopWatch stopWatch = new StopWatch()
        stopWatch.start()
        def bookings = bookingService.getBookingsBySlots(slots)

        if (bookings) {
            assertFacilityAccessTo(bookings[0].slot.court)
        }

        log.info("Cancel bookings operation start (${bookings.size()} bookings)")

        def startTime = bookings[0].slot.startTime
        def toRemove = []
        def bookingGroupIdsToCheck = new HashSet<Long>()
        bookings.each { Booking booking ->
            bookingGroupIdsToCheck.add(booking.group?.id)
            if (cmd.removeRecurrence && booking.group) {
                toRemove << booking
                def reccuringBookings = bookingService.getRecurringBookings(booking)
                log.debug("Found ${reccuringBookings.size()} bookings...")
                reccuringBookings.each { Booking b ->
                    if (!toRemove.contains(b)) {
                        toRemove << b
                    }
                }
            } else {
                toRemove << booking
            }
            startTime = booking.slot.startTime
        }
        try {
            def currentUser = springSecurityService.getCurrentUser()

            int numberOfThreads = grailsApplication.config.matchi.threading.numberOfThreads
            def threadPool = Executors.newFixedThreadPool(numberOfThreads)

            def authentication = SecurityContextHolder.getContext().authentication

            toRemove.each { Booking b ->
                threadPool.execute({
                    try {
                        // Store security context so that we can retrieve the actual user later
                        SecurityContextHolder.getContext().authentication = authentication
                        Booking.withNewSession {
                            b = Booking.get(b.id)
                            currentUser = User.get(currentUser.id)
                            bookingService.cancelBooking(b, cmd.message, true, cmd.redeemType, currentUser, cmd.sendNotification, true, false, cmd.refundOption)
                        }
                    } catch (ex) {
                        log.error "Error during cancelBooking ${b} (User ID: ${currentUser?.id}, Facility ID: ${b.slot?.court?.facility?.id}, Slot ID: ${b.slot?.id}): ${ex.message}", ex
                    }
                })
            }
            threadPool.shutdown()
            if (!threadPool.awaitTermination(110, TimeUnit.SECONDS)) {
                log.error "Cancel bookings operation took more then 110 seconds for ${cmd}. Forwarding response as timeout on page otherwise"
            }
            //@todo: rewrite it to be not so persistent
            if (bookingGroupIdsToCheck) {
                BookingGroup.withNewSession {
                    bookingGroupIdsToCheck.each { groupId ->
                        if (groupId) {
                            def group = BookingGroup.get(groupId)
                            if (group && !group.type.equals(BookingGroupType.SUBSCRIPTION) && group.bookings.size() < 1) {
                                log.debug("Booking group not containing any more bookings, removing group")
                                group.delete()
                            }
                        }
                    }
                }
            }
        } catch (PaymentException pe) {
            flash.error = "${pe.message}"
        } catch (Exception e) {
            flash.error = "${e.message}"
            log.error "Error during booking cancel: ${e.message}", e
        }
        stopWatch.stop()
        log.info "Total time for cancel operation ${stopWatch.toString()}"

        flash.message = message(code: "facilityBooking.cancel.cancelled"
                + (cmd.sendNotification ? "AndNotified" : ""), args: [toRemove.size()])

        if (params.returnUrl && params.returnUrl.size() > 0) {
            redirect url: params.returnUrl
        } else {
            redirect(action: "index", params: [date: dateUtil.formatDate(startTime)]);
        }
    }

    def book(FacilityBookingCommand cmd) {

        if (cmd.type.equals(BookingGroupType.ACTIVITY)) {
            // remove action_book from parameters for facilityActivitOccasion
            def redirectParams = params.clone()
            params.findAll { k, v -> k.startsWith("_action_") }.each { redirectParams.remove(it.key) }
            def booking = slotService.getSlot(cmd.slotId)?.booking
            if (booking) {
                booking.comments = cmd.comments
                booking.showComment = cmd.showComment
                booking.save(failOnError: true)

                flash.message = composeBookingMessage(cmd, true)

                if (params.returnUrl && params.returnUrl.size() > 0) {
                    redirect url: params.returnUrl
                } else {
                    redirect(controller: "facilityBooking", action: "index", params: [date: params.date != "null" ? params.date : ""])
                }
            } else {
                redirect(controller: "facilityActivityOccasion", action: "create", params: redirectParams)
            }
            return
        }


        // Check for errors...
        if (cmd.hasErrors()) {
            log.debug(cmd.errors)
            flash.error = message(code: "facilityBooking.book.error")
            redirect(action: "index", params: [date: params.date != "null" ? params.date : ""])
            return
        }

        log.info("Processing booking request: ${cmd}")
        def slotIds = cmd.slotIds()
        def slots = slotService.getSlots(slotIds)
        def booking = Booking.get(params.id)

        // Make sure none of the slots has any booking. Could potentially happen that someone else books while you are in the modal
        if (slots.any { Slot slot -> slot.booking }) {
            if (!params.version) { //the slot didn't have any booking before submitting the modal
                flash.error = message(code: "facilityBooking.book.error.booked")
                redirect(action: "index", params: [date: params.date != "null" ? params.date : ""])
                return
            } else if (params.version && booking.version > params.version.toLong()) {
                //the booking was modified by someone else
                flash.error = message(code: "default.optimistic.locking.failure", args: booking)
                redirect(action: "index", params: [date: params.date != "null" ? params.date : ""])
                return
            }
        }

        if (params.version && !slots.any { Slot slot -> slot.booking }) { //booking was cancelled already
            flash.error = message(code: "facilityBooking.book.error.cancelled")
            redirect(action: "index", params: [date: params.date != "null" ? params.date : ""])
            return
        }

        def slot = slots.get(0)

        def isUpdate = false
        def errors = false

        switch (cmd.type) {
            case BookingGroupType.SUBSCRIPTION:
                if (slot && slot?.subscription && !slot.booking) {
                    flash.error = message(code: "facilityBooking.book.subscription.error")
                    redirect(action: "index", params: [date: params.date != "null" ? params.date : ""])
                    return
                }
                def playerCustomers = customerService.collectPlayers(removeNull(params.list("playerCustomerId")),
                        params.list("unknownPlayer").size())
                isUpdate = subscriptionService.createSubscriptions(cmd, playerCustomers)
                break
            default:
                try {
                    isUpdate = bookingService.makeBookings(cmd, removeNull(params.list("playerCustomerId")),
                            params.list("unknownPlayer").size())
                } catch (Exception e) {
                    if (e instanceof GrailsMailException) {
                        log.error("Error sending emails to customer. Please check logs before this message", e)
                    }
                    if (e instanceof DataIntegrityViolationException
                            && e.mostSpecificCause.message?.contains("slot_id")) {
                        flash.error = message(code: "facilityBooking.book.error.booked")
                        redirect(action: "index", params: [date: params.date != "null" ? params.date : ""])
                        return
                    }
                    flash.error = message(code: "error.subheading")
                    log.error("Error during booking: ${e.message}", e)
                    errors = true
                }
                break
        }

        if (!errors) {
            flash.message = composeBookingMessage(cmd, isUpdate)
        }

        if (params.returnUrl && params.returnUrl.size() > 0) {
            redirect url: params.returnUrl
        } else {
            redirect(controller: "facilityBooking", action: "index", params: [date: params.date != "null" ? params.date : ""])
        }
    }

    def move() {
        def bookingSlot = Slot.get(params.bookingSlotId)
        def slot = Slot.get(params.slotId)

        log.info("Moving booking with slot: ${bookingSlot.id}, to: ${slot.id}")
        bookingService.moveBooking(bookingSlot, slot, params.notify, params.message)

        flash.message = message(code: "facilityBooking.move.moved" + (params.notify ? "AndNotified" : ""))

        if (params.returnUrl) {
            redirect url: params.returnUrl
        } else {
            redirect(action: "index", params: [date: params.date != "null" ? params.date : ""])
        }
    }

    private void makePayments(FacilityBookingCommand cmd) {
        def slots = slotService.getSlots(cmd.slotIds())
        def user = userService.getLoggedInUser()

        log.debug("Make payments: ${cmd}")

        if (cmd.paid) {
            slots.each {
                paymentService.makePayment(it.booking?.order, cmd)
            }
        } else {
            bookingService.getBookingsBySlots(slots.collect { it.id }).each {
                if (it.order?.isStillRefundable()) {
                    it.order?.payments.each { payment ->
                        if (!payment.status.equals(OrderPayment.Status.CREDITED)) {
                            payment.refund(payment.amount)
                        }
                    }
                }
            }
        }
    }

    def confirmRecurrence(FacilityBookingCommand cmd) {

        log.debug("Confirm recurrence with ${cmd}")

        if (cmd.hasErrors()) {
            flash.error = message(code: "facilityBooking.confirmRecurrence.error")
            redirect(action: "bookingForm", params: [slotId: cmd.slotId, date: params.date])
            return
        }

        def customer = bookingService.getBookingCustomer(cmd)
        if (!cmd.customerId) {
            cmd.customerId = customer.id
        }
        def membership = customer?.membership
        def slotIds = cmd.slotIds()
        def slots = slotService.getSlots(slotIds)
        def playerCustomers = cmd.type == BookingGroupType.SUBSCRIPTION ?
                customerService.collectPlayers(removeNull(params.list("playerCustomerId")), params.list("unknownPlayer").size()) : []

        RecurringSlotsContainer recurringSlots = slotService.getRecurrenceSlots(new DateTime(cmd.recurrenceStart), new DateTime(cmd.recurrenceEnd), cmd.weekDays, cmd.frequency, cmd.interval, slots)

        log.debug("RECURRENCE SLOTS: ${recurringSlots.freeSlots.size()}")

        render(view: "facilityBookingConfirmRecurrence", model: [date: params.date, cmd: cmd, recurringSlots: recurringSlots, customer: customer, membership: membership, players: playerCustomers])
    }

    def bookingPayment() {
        def facility = getUserFacility()
        def customer = customerService.getCustomer(params.long('customerId'))

        def slots = slotService.getSlots(Slot.parseAll(params.slotId))
        def bookingPrices
        if (facility.isFacilityPropertyEnabled(
                FacilityProperty.FacilityPropertyKey.FEATURE_CALCULATE_MULTIPLE_PLAYERS_PRICE.name())) {
            def playerCustomers = customerService.collectPlayers(params.list("playerCustomerId"), params.list("unknownPlayer").size())
            if (!playerCustomers) {
                playerCustomers = [customer]
            }
            bookingPrices = priceListService.getAvgBookingPrices(slots, playerCustomers)
        } else {
            bookingPrices = priceListService.getBookingPrices(slots, customer)
        }

        def coupons = CustomerOfferGroup.fromCustomerCoupons(
                couponService.getValidCouponsByCustomerAndSlots(
                        customer, slots, null, Coupon.class)).findAll {
            it.remainingNrOfTickets == null || it.remainingNrOfTickets >= slots.size()
        }
        def giftCards = CustomerOfferGroup.fromCustomerCoupons(
                couponService.getValidCouponsByCustomerAndSlots(
                        customer, slots, null, GiftCard.class))

        def bookingWarning = ""
        if (customer && !customer.exludeFromNumberOfBookingsRule
                && facility.isFacilityPropertyEnabled(FacilityProperty.FacilityPropertyKey.FEATURE_MAXIMUM_NUMBER_OF_BOOKINGS_PER_USER.name())) {
            def upcomingBookings = Booking.upcomingBookings(customer).count()
            if (facility.getFacilityPropertyValue(FacilityProperty.FacilityPropertyKey.MAXIMUM_NUMBER_OF_BOOKINGS_PER_USER.name()).toInteger() <= upcomingBookings) {
                bookingWarning = message(code: "facilityBooking.bookingPayment.maxBookingsWarning",
                        args: [upcomingBookings])
            }
        }

        render(view: "facilityBookingFormPayment", model: [facility      : facility, slots: slots, customer: customer,
                                                           bookingPrices : bookingPrices, coupons: coupons, giftCards: giftCards,
                                                           bookingWarning: bookingWarning])
    }

    def alterPaid() {
        def slots = []

        if (params.alterSlotsData) {
            slots = slotService.getSlots(Slot.parseAll(params.alterSlotsData))
            slots.each { Slot s ->
                FacilityBookingCommand cmd = new FacilityBookingCommand()
                cmd.slotId = s.id

                def useInvoice = params.boolean("useInvoice")
                if (s?.booking?.isFinalPaid() || s?.booking?.payment?.status?.equals(PaymentStatus.OK)) {
                    cmd.paid = Boolean.FALSE
                    makePayments(cmd)
                    s.booking.save(failOnError: true)
                } else {
                    if (useInvoice) {
                        cmd.useInvoice = Boolean.TRUE
                    }
                    cmd.paid = Boolean.TRUE
                    makePayments(cmd)
                    s.booking.save(failOnError: true)
                }
            }
        }

        flash.message = message(code: "facilityBooking.alterPaid.message", args: [slots.size()])

        if (params.returnUrl) {
            redirect url: params.returnUrl
        } else {
            redirect(action: "index", params: [date: params.date])
        }
    }

    private def composeBookingMessage(FacilityBookingCommand cmd, boolean isUpdate) {
        def message = ""

        switch (cmd.type) {
            case BookingGroupType.DEFAULT:
                message = !isUpdate ? g.message(code: "facilityBooking.composeBookingMessage.bookingSaved" + (cmd.sendNotification ? "AndNotified" : ""))
                        : g.message(code: "facilityBooking.composeBookingMessage.bookingUpdated")
                break
            case BookingGroupType.SUBSCRIPTION:
                message = g.message(code: "facilityBooking.composeBookingMessage.subscription" + (!isUpdate ? "Saved" : "Updated"))
                break
            default:
                message = g.message(code: "facilityBooking.composeBookingMessage.booking" + (!isUpdate ? "Saved" : "Updated"))
                break
        }

        return message
    }

    def getBookingInfo() {
        Slot slot = Slot.get(params.slotId)
        def result = []

        def booking = [
                slotId  : slot.id,
                court   : slot.court.name,
                date    : new DateTime(slot.startTime).toString("d/M"),
                start   : new DateTime(slot.startTime).toString("HH:mm"),
                end     : new DateTime(slot.endTime).toString("HH:mm"),
                customer: slot.booking?.customer?.fullName().encodeAsSanitizedMarkup()
        ]

        render booking as JSON
    }

    private def bookingTypes(Facility facility) {
        def bookingTypes = [BookingGroupType.DEFAULT, BookingGroupType.NOT_AVAILABLE, BookingGroupType.SUBSCRIPTION, BookingGroupType.COMPETITION, BookingGroupType.TRAINING, BookingGroupType.ACTIVITY]
        if (facility.hasPrivateLesson())
            bookingTypes << BookingGroupType.PRIVATE_LESSON
        return bookingTypes
    }

    def exportBookings() {
        if (params.exportSlotsData) {
            def slotIds = Slot.parseAll(params.exportSlotsData)
            if (slotIds) {
                slotIds = Slot.withCriteria {
                    projections {
                        property("id")
                    }
                    inList('id', slotIds)
                    order('startTime', 'asc')
                }
            }

            def facility = getUserFacility()
            def date = new DateTime().toString("yyyy-MM-dd_HHmmss")

            response.setHeader("Content-disposition",
                    "attachment; filename=bokningar_${facility.shortname}_${date}.csv")
            response.contentType = "text/csv"

            ExcelExportManager.withBomWriter(response.outputStream) { out ->
                bookingService.exportBookings(slotIds, out, new Locale(facility.language))
            }
        } else {
            response.sendError HttpServletResponse.SC_BAD_REQUEST
        }
    }

    def exportBookingCustomers() {
        if (params.exportSlotsData) {
            def slotIds = Slot.parseAll(params.exportSlotsData)
            def date = new DateTime().toString("yyyy-MM-dd_HHmmss")

            response.setHeader("Content-disposition",
                    "attachment; filename=kunder_${getUserFacility().shortname}_${date}.csv")
            response.contentType = "text/csv"

            ExcelExportManager.withBomWriter(response.outputStream) { out ->
                bookingService.exportBookingCustomers(slotIds, out)
            }
        } else {
            response.sendError HttpServletResponse.SC_BAD_REQUEST
        }
    }

    def sendMessage() {
        if (params.exportSlotsData) {
            def slots = slotService.getSlots(Slot.parseAll(params.exportSlotsData))
            def customerIds = slots.collect { it.booking?.customer?.id }.unique()
            redirect(controller: params.type == "sms" ? "facilityCustomerSMSMessage" : "facilityCustomerMessage",
                    action: "message", params: [returnUrl: params.returnUrl, customerId: customerIds])
        } else {
            response.sendError HttpServletResponse.SC_BAD_REQUEST
        }
    }

    def rebookSubscriber(FacilityBookingCommand cmd) {
        def slots = slotService.getSlots(Slot.parseAll(cmd.slotId))
        def subscription = slots[0].subscription

        if (subscription) {
            def playerCustomers = customerService.collectPlayers(removeNull(params.list("playerCustomerId")), params.list("unknownPlayer").size())
            bookingAvailabilityService.addBookingGroupBookings(subscription, subscription.bookingGroup,
                    slots, subscription.customer, cmd.showComment, cmd.comments, false, playerCustomers)
            subscriptionService.createBookingsOrders(subscription)

            slots.each { Slot slot ->
                courtRelationsBookingService.tryBookRelatedCourts(slot.booking)
            }

        } else {
            log.info "Unable to rebook subscription - no subscription found for slot ${slots[0].id}"
        }

        if (params.returnUrl) {
            redirect(url: params.returnUrl)
        } else {
            redirect(action: "index", params: [date: params.date])
        }
    }

    private List removeNull(List paramsList) {
        return paramsList - null - ""
    }
}

@Validateable(nullable = true)
class FilterBookingsCommand {
    String start
    String end
    String q
    Boolean markpaid = null
    List<BookingPaymentMethod> paymentMethods = []
    List<Long> courtIds = []
    List<BookingGroupType> bookingTypes = []
    List<Long> groups = []

    public static enum BookingPaymentMethod {
        CREDIT_CARD, REGULAR_COUPON, UNLIMITED_COUPON, GIFT_CARD, CASH, INVOICE, FREE

        static list() {
            return [CREDIT_CARD, REGULAR_COUPON, UNLIMITED_COUPON, GIFT_CARD, CASH, INVOICE, FREE]
        }
    }
}
