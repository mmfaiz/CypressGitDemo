package com.matchi.facility

import com.matchi.*
import grails.util.Holders
import grails.validation.Validateable
import org.joda.time.DateTime
import org.joda.time.LocalTime
import org.springframework.dao.DataIntegrityViolationException

class FacilitySubscriptionController extends GenericController {

    public static final String LIST_FILTER_KEY = "facility_subscription_filter"

    def bookingAvailabilityService
    def subscriptionService
    def slotService
    def seasonService
    def bookingService
    def redeemService
    def courtRelationsBookingService
    def priceListService

    def index(FacilitySubscriptionFilterCommand cmd) {
        if (!params.boolean("reset") && session[LIST_FILTER_KEY]?.isNotExpired()) {
            cmd = session[LIST_FILTER_KEY]
        } else {
            session[LIST_FILTER_KEY] = cmd
        }

        def facility = getUserFacility()

        if(!cmd.season) {
            cmd.season = seasonService.getSeasonByDate(new Date())?.id
        }

        def result = subscriptionService.getSubscriptions( facility, cmd )
        def subscriptions = result.rows
        def count = result.count

        [ facility: facility, count: count, subscriptions: subscriptions, cmd: cmd ]
    }

    def create() {
        [ form:initForm(), facility:getUserFacility() ]
    }

    def edit() {
        Facility facility = getUserFacility()
        def subscription = Subscription.get(params.id)
        if (subscription) {
            assertFacilityAccessTo(subscription.customer)
        } else {
            flash.error = message(code: "default.not.found.message",
                    args: [message(code: "subscription.label")])
            redirect(action: "index")
            return
        }

        CreateSubscriptionCommand cmd = new CreateSubscriptionCommand()
        cmd.id = subscription.id
        cmd.customerId = subscription.customer.id
        cmd.courtId = subscription.court?.id
        cmd.dateFrom = new DateTime(subscription.slots.first().startTime).toString("yyyy-MM-dd")
        cmd.dateTo = new DateTime(subscription.slots.last().endTime).toString("yyyy-MM-dd")
        cmd.time = new LocalTime(subscription.slots.first().startTime)
        cmd.season = seasonService.getSeasonByDate(subscription.slots.last().startTime).id
        cmd.description = subscription.description
        cmd.accessCode = subscription.accessCode

        def form = initForm()
        def currentSeason = Season.get(cmd.season)
        if (currentSeason && !form.seasons?.contains(currentSeason)) {
            if (form.seasons != null) {
                form.seasons << currentSeason
            } else {
                form.seasons = [currentSeason]
            }
        }

        [ cmd: cmd, form: form, subscription: subscription, facility:facility ]
    }

    def updatePrice(Long id) {
        def subscription = Subscription.findById(id,
            [fetch: [bookingGroup: "join", customer: "join"], lock: true])

        def priceList = priceListService.getActivePriceList(
            subscription.firstSlot(), true) ?: priceListService.getActiveSubscriptionPriceList(
            subscription.customer.facility, subscription.slots.first().court.sport)

        def price = subscription.getPrice(priceList)

        subscription.order.price = price
        subscription.order.vat = price * (subscription.customer?.facility?.vat/100)
        subscription.save(flush:true)

        subscription.bookingGroup.bookings.each { booking ->
                price = priceListService.getBookingPrice(booking.slot, subscription.customer)

                booking.order.price = price.price
                booking.order.vat = price.VATAmount
                booking.order.save()
        }

        flash.message = message(code: "facilitySubscription.updatePrice.success")

        if(params.returnUrl && params.returnUrl.size() > 0) {
            redirect url: params.returnUrl
        } else {
            redirect(action: "index")
        }
    }

    def updatePrices(FacilitySubscriptionFilterCommand cmd) {

        def pricelist
        def price

        selectedSubscriptions(cmd).each { s ->
            pricelist = priceListService.getActivePriceList(
                s.firstSlot(), true) ?: priceListService.getActiveSubscriptionPriceList(
                s.customer.facility, s.slots.first().court.sport)

            price = s.getPrice(pricelist)

            s.order.price = price
            s.order.vat = price * (s.customer?.facility?.vat/100)
            s.save(flush:true)

            s.bookingGroup.bookings.each { booking ->
                price = priceListService.getBookingPrice(booking.slot, s.customer)

                booking.order.price = price.price
                booking.order.vat = price.VATAmount
                booking.order.save()
            }
        }


        flash.message = message(code: "facilitySubscription.updatePrice.success")

        redirect(action: "index", params:[subscriptionId: params.subscriptionId, newStatus:params.newStatus])
    }

    def save(CreateSubscriptionCommand cmd) {
        def subscriptionsConfig = MatchiConfig.findByKey(MatchiConfigKey.DISABLE_SUBSCRIPTIONS)
        if (subscriptionsConfig.isBlocked()) {
            render subscriptionsConfig.isBlockedMessage()
        }

        if(cmd.hasErrors()) {
            render(view: "create", model: [ cmd:cmd, form:initForm() ])
            return
        }

        if (cmd.id) {
            update(cmd)
            return
        }

        try {
            def time         = new LocalTime(cmd.time)
            def court        = Court.get(cmd.courtId)
            def startTime    = new DateTime(cmd.dateFrom).withHourOfDay(time.hourOfDay).withMinuteOfHour(time.minuteOfHour)
            def slot         = Slot.findByCourtAndStartTime(court, startTime.toDate())
            def subscription = subscriptionService.createSubscription(cmd.description, cmd.showComment, new DateTime(cmd.dateFrom),
                new DateTime(cmd.dateTo), slot, startTime.dayOfWeek,cmd.interval, Customer.findById(cmd.customerId), cmd.accessCode)

            if (!subscription) {
                flash.error = message(code: "facilitySubscription.save.error1")
                render(view: "create", model: [ cmd:cmd, form:initForm() ])
                return
            }

            subscriptionService.save(subscription)

            subscription.bookingGroup.bookings.each {
                courtRelationsBookingService.tryBookRelatedCourts(it)
            }

            flash.message = message(code: "facilitySubscription.save.success")

        } catch (DataIntegrityViolationException e) {
            log.info(e.message)
            flash.error = message(code: "facilitySubscription.save.error2")
            return
        }

        if(params.returnUrl && params.returnUrl.size() > 0) {
            redirect url: params.returnUrl
        } else {
            redirect(action: "index")
        }

    }

    def confirm(CreateSubscriptionCommand cmd) {
        Facility facility = getUserFacility()

        if(cmd.hasErrors()) {
            render(view: "create", model: [ cmd:cmd, form:initForm(), facility:facility ])
            return
        }

        def time         = new LocalTime(cmd.time)
        def court        = Court.get(cmd.courtId)
        def startTime    = new DateTime(cmd.dateFrom).withHourOfDay(time.hourOfDay).withMinuteOfHour(time.minuteOfHour)
        def slot         = Slot.findByCourtAndStartTime(court, startTime.toDate())

        def intervalSlots = slotService.getRecurrenceSlots(new DateTime(cmd.dateFrom), new DateTime(cmd.dateTo), [startTime.dayOfWeek.toString()], 1, cmd.interval, [slot], false)
        def slots = intervalSlots.freeSlots + intervalSlots.unavailableSlots

        return [ cmd:cmd, facility:getUserFacility(),
                slots:slots,
                sortedSlots: slotService.sortSubscriptionSlots(slots, cmd), customer:Customer.get(cmd.customerId) ]
    }

    def update(CreateSubscriptionCommand cmd) {
        log.debug("Updating....")
        def subscription = Subscription.findById(cmd.id)
        if (subscription) {
            assertFacilityAccessTo(subscription.customer)
        }

        def time         = new LocalTime(cmd.time)
        def court        = Court.get(cmd.courtId)
        def startTime    = new DateTime(cmd.dateFrom).withHourOfDay(time.hourOfDay).withMinuteOfHour(time.minuteOfHour)
        def slot         = Slot.findByCourtAndStartTime(court, startTime.toDate())

        def updatedSubscription = subscriptionService.updateSubscription(subscription, cmd.description, new DateTime(cmd.dateFrom),
                new DateTime(cmd.dateTo), slot, cmd.interval, Customer.findById(cmd.customerId), cmd.showComment, cmd.accessCode)

        if (!updatedSubscription) {
            flash.error = message(code: "facilitySubscription.update.error")
            render(view: "edit", model: [ cmd:cmd, form:initForm(), subscription:subscription ])
            return
        }

        flash.message = message(code: "facilitySubscription.update.success",
                args: [updatedSubscription.customer.fullName()])

        if(params.returnUrl && params.returnUrl.size() > 0) {
            redirect url: params.returnUrl
        } else {
            redirect(action: "index")
        }
    }

    def updateAccessCode(UpdateAccessCodeCommand cmd) {
        def subscription = Subscription.findById(cmd.id)
        if (subscription) {
            assertFacilityAccessTo(subscription.customer)
        }

        if(cmd.hasErrors()) {
            flash.error = message(code: "facilitySubscription.updateAccessCode.error")
            redirect(action: "edit", params: [id: cmd.id])
            return
        }

        subscription.accessCode = cmd.accessCode
        subscription.save()

        flash.message = message(code: "facilitySubscription.updateAccessCode.success")
        redirect(action: "edit", params: [id: cmd.id])
    }

    def removeAccessCode(UpdateAccessCodeCommand cmd) {
        def subscription = Subscription.findById(cmd.id)
        if (subscription) {
            assertFacilityAccessTo(subscription.customer)
        }

        subscription.accessCode = null
        subscription.save()

        flash.message = message(code: "facilitySubscription.removeAccessCode.success")
        redirect(action: "edit", params: [id: cmd.id])
    }

    def cancel(Long id) {
        Subscription.withTransaction {
            def subscription = Subscription.findById(id,
                    [fetch: [bookingGroup: "join", customer: "join"], lock: true])
            if (subscription) {
                def customerName = subscription.customer.fullName()
                try {
                    subscriptionService.deleteSubscription(subscription)
                    flash.message = message(code: "facilitySubscription.cancel.success1",
                            args: [customerName])
                } catch (org.springframework.dao.DataIntegrityViolationException e) {
                    flash.error = message(code: 'default.not.deleted.message',
                            args: [message(code: 'subscription.label'), id])
                    redirect(action: "edit", id: id)
                }

                if(params.returnUrl && params.returnUrl.size() > 0) {
                    flash.message = message(code: "facilitySubscription.cancel.success2")
                    redirect url: params.returnUrl
                } else {
                    redirect(action: "index")
                }
            } else {
                flash.error = message(code: "default.not.found.message",
                        args: [message(code: "subscription.label")])
                redirect(action: "index")
            }
        }
    }

    private def initForm() {
        def facility = getUserFacility()

        def createdSeasons = seasonService.getAvailableSeasons(facility)
        def startHour = facility.getOpeningHour(1) != null ? facility.getOpeningHour(1) : 6
        def endHour   = facility.getClosingHour(1) != null ? (facility.getClosingHour(1) + 1) : 23

        def availableHours = []
        for(def i = startHour; i < endHour; i++ ) {
            availableHours << i
        }

        return [ facility: facility, availableHours:availableHours, seasons:createdSeasons ]
    }

    def invoice() {

        log.info("Invoice subscription")
        redirect(action: "index")
    }

    def runFacilitySubscriptionRedeemJob() {
        def facility = getUserFacility()

        if (facility) {
            redeemService.redeemUnredeemedCancelations([facility])
        }

        flash.message = message(code: "facilitySubscription.runFacilitySubscriptionRedeemJob.success")
        redirect(action: "index")
    }

    def changeStatus(FacilitySubscriptionFilterCommand cmd) {
        def status = Subscription.Status.valueOf(params.newStatus)

        selectedSubscriptions(cmd).each { s ->
            Subscription.withTransaction {
                s.status = status
                s.save()
            }
        }

        flash.message = message(code: "facilitySubscription.changeStatus.success")

        redirect(action: "index", params:[subscriptionId: params.subscriptionId, newStatus:params.newStatus])
    }

    def customerAction(FacilitySubscriptionFilterCommand cmd) {
        def customerIds = selectedSubscriptions(cmd).collect{it.customer.id}.unique()

        redirect(controller: params.targetController, action: params.targetAction,
                params: [returnUrl: params.returnUrl, customerId: customerIds])
    }

    def changeReminder(FacilitySubscriptionFilterCommand cmd) {
        def enabled = params.boolean("enabled")

        selectedSubscriptions(cmd).each { s ->
            Subscription.withTransaction {
                s.reminderEnabled = enabled
                s.save()
            }
        }

        flash.message = message(code: "facilitySubscription.changeReminder.success")

        redirect(action: "index", params: params)
    }

    def removeSlot(Long id, String slotId) {
        def subscription = Subscription.get(id)
        if (!subscription) {
            redirect(action: "index")
        }

        assertFacilityAccessTo(subscription.customer)

        def slot = Slot.findByIdAndSubscription(slotId, subscription)
        if (!slot) {
            redirect(action: "index")
        }

        bookingAvailabilityService.removeBookingFromGroup(
                subscription, subscription.bookingGroup, slot)

        redirect(action: "edit", id: subscription.id)
    }

    private List selectedSubscriptions(FacilitySubscriptionFilterCommand cmd) {
        def subscriptionIds = []

        if (params.allselected) {
            subscriptionIds = subscriptionService.getSubscriptions(getUserFacility(), cmd).rows.collect { it.id }
        } else {
            params.list("subscriptionId").each { subscriptionIds << Long.parseLong(it) }
        }

        Subscription.findAllByIdInList(subscriptionIds)
    }
}

@Validateable(nullable = true)
class FacilitySubscriptionFilterCommand implements Serializable {
    private static final long serialVersionUID = 1L

    public static final String NOT_INVOICED_STATUS = "NOT_INVOICED"

    Long season
    int offset = 0
    int max = 200
    String order = "desc"
    String sort  = "startTime"
    String q
    List<Integer> weekday = []
    LocalTime time
    List<Long> court = []
    List<Subscription.Status> status = []
    List<String> invoiceStatus = []
    boolean allselected = false

    private Date dateCreated = new Date()

    boolean isActive() {
        q || weekday || time || court || status || invoiceStatus ||
                (season && season !=  Holders.grailsApplication.mainContext.getBean(com.matchi.SeasonService).getSeasonByDate(new Date())?.id)
    }

    boolean isNotExpired() {
        new Date().time - dateCreated.time <= Holders.config.facility.customerFilter.timeout * 60 * 1000
    }
}

@Validateable(nullable = true)
class UpdateAccessCodeCommand {
    Long id
    String accessCode

    static constraints = {
        id(nullable: false)
        accessCode(nullable: false, maxSize: 255)
    }
}
