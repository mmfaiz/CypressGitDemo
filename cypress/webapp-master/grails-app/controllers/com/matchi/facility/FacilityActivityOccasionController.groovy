package com.matchi.facility

import com.matchi.*
import com.matchi.activities.ClassActivity
import com.matchi.activities.ActivityOccasion
import com.matchi.activities.Participation
import com.matchi.orders.Order
import com.matchi.payment.PaymentException
import com.matchi.watch.ClassActivityWatch
import grails.validation.Validateable
import org.joda.time.DateTime
import org.joda.time.LocalDate
import org.joda.time.LocalTime

class FacilityActivityOccasionController extends GenericController {
    def slotService
    def activityService
    UserService userService
    def cashService
    def objectWatchNotificationService
    OrderStatusService orderStatusService

    def create(FacilityBookingCommand cmd) {
        def slots = getOccasionSlots(cmd)
        def facility = getUserFacility()
        def activities = activityService.getActiveClassActivitiesByFacility(facility)

        def comments = ["comment": cmd.comments, "showComment": cmd.showComment]

        def command = new CreateActivityOccasionsCommand()

        if (cmd.customerId) {
            command.customerId = cmd.customerId
        }

        command.occasions = createActivityOccasionsCommands(slotService.groupSlotsByDate(slots.free))

        return [slots: slots, groupedSlots: slotService.groupSlotsByDate(slots.free), activities: activities, command: command, comments: comments, facility: facility]
    }

    def edit() {
        ActivityOccasion occasion = ActivityOccasion.findById(
                params.id, [fetch: [activity: "join"]])
        assertFacilityAccess(occasion.activity.facility)
        def paymentStatus = activityService.getOccasionPaymentStatus(occasion)
        def participants = activityService.getParticipants(occasion)
        def facility = occasion.activity.facility

        def model = [occasion: occasion, paymentStatus: paymentStatus, participants: participants, facility: facility]

        if (!occasion.isPast() && facility.isFacilityPropertyEnabled(
                FacilityProperty.FacilityPropertyKey.FEATURE_QUEUE)) {
            def activityWatchQueue = occasion.watchQueue.collect {
                def c = Customer.findByFacilityAndUser(facility, it.user)
                [userId       : it.user.id, userName: it.user.fullName(), userEmail: it.user.email,
                 customerId   : c?.id, customerNr: c?.number, customerName: c?.fullName(),
                 customerEmail: c?.email]
            }
            model.activityWatchQueue = activityWatchQueue
        }

        model
    }

    def update(UpdateActivityOccasionCommand cmd) {
        ActivityOccasion occasion = ActivityOccasion.get(params.id)

        assertFacilityAccess(occasion.activity.facility)

        occasion.price = cmd.price
        occasion.maxNumParticipants = cmd.maxNumParticipants
        occasion.startTime = cmd.startTime
        occasion.endTime = cmd.endTime
        occasion.message = cmd.message
        occasion.availableOnline = cmd.availableOnline
        occasion.signUpDaysInAdvanceRestriction = cmd.signUpDaysInAdvanceRestriction
        occasion.signUpDaysUntilRestriction = cmd.signUpDaysUntilRestriction
        occasion.membersOnly = cmd.membersOnly

        if (cmd.minNumParticipants && cmd.cancelHoursInAdvance) {
            occasion.minNumParticipants = cmd.minNumParticipants
            occasion.setCancellationDateTime(cmd.cancelHoursInAdvance)
        } else {
            occasion.minNumParticipants = null
            occasion.automaticCancellationDateTime = null
        }

        if (cmd.validate()) {

            occasion.save(failOnError: true)

            flash.message = message(code: "facilityActivityOccasion.update.success")
            redirect(controller: "facilityActivity", action: "occasions", id: occasion.activity.id)
        } else {
            def paymentStatus = activityService.getOccasionPaymentStatus(occasion)
            def participants = activityService.getParticipants(occasion)
            render(view: "edit", model: [occasion: occasion, cmd: cmd, participants: participants, paymentStatus: paymentStatus])
        }
    }

    def addParticipantPayment() {
        def participation = Participation.get(params.participant)

        assertFacilityAccess(participation.occasion.activity.facility)

        if (participation) {
            Participation.withTransaction {
                User user = userService.getLoggedInUser()
                def order = activityService.createActivityPaymentOrder(participation.customer.user,
                        participation.occasion, Order.ORIGIN_FACILITY, user)
                if (order.total() == 0) {
                    orderStatusService.confirm(order, user)
                } else {
                    cashService.createCashOrderPayment(order)
                }

                participation.order = order
                participation.save()
            }

            flash.message = message(code: "facilityActivityOccasion.addParticipantPayment.success",
                    args: [participation.customer.fullName()])
        }

        redirect(action: "edit", id: participation.occasion.id)
    }

    def removeParticipantPayment() {
        def participation = Participation.get(params.participant)
        def occasionId = participation.occasion.id

        boolean orderCouldBeCredited = participation.order?.isStillRefundable()

        assertFacilityAccess(participation.occasion.activity.facility)

        try {
            activityService.tryAnnulParticipantPayment(participation, userService.getCurrentUser(), true)
        } catch (PaymentException pe) {
            log.error("Unable to annul user payment", pe)
            flash.error = message(code: "facilityActivityOccasion.removeParticipantPayment.error")
            redirect(action: "edit", id: occasionId)
            return
        }

        if (orderCouldBeCredited) {
            flash.message = message(code: "facilityActivityOccasion.removeParticipantPayment.success")
        } else {
            flash.message = message(code: "facilityActivityOccasion.removeParticipantPayment.semiSuccess")
        }

        redirect(action: "edit", id: occasionId)
    }

    def addParticipant() {
        def occasion = ActivityOccasion.get(params.id)
        if (params.list("customerId").contains("")) {
            flash.error = message(code: "facilityActivityOccasion.addParticipant.noCustomer")
            redirect(action: "edit", id: occasion.id)
            return
        }
        def customer = Customer.get(params.customerId)

        assertFacilityAccess(occasion.activity.facility)

        if (customer) {
            def participation = activityService.findParticipant(occasion, customer)

            if (!participation) {
                activityService.addParticipant(occasion, customer)
                flash.message = message(code: "facilityActivityOccasion.addParticipant.success",
                        args: [customer.fullName()])
            } else {
                flash.error = message(code: "facilityActivityOccasion.addParticipant.alreadyAdded")
            }

        } else {
            flash.error = message(code: "facilityActivityOccasion.addParticipant.noCustomer")
        }

        redirect(action: "edit", id: occasion.id)
    }

    def removeParticipant(boolean refund) {
        def participation = Participation.get(params.participant)
        if (!participation) {
            flash.error = message(code: "facilityActivityOccasion.noParticipant.error")
            redirect(action: "edit", id: params.occasion)
            return
        }


        def customer = participation.customer
        def occasion = participation.occasion

        assertFacilityAccess(occasion.activity.facility)

        if (customer) {
            try {
                // when facility cancels customer booking, always cancel the payment (if any)
                if (refund) {
                    activityService.tryAnnulParticipantPayment(occasion.getParticipation(customer), getCurrentUser(), true)
                } else if (participation.order) { // If no order, nothing to annul
                    orderStatusService.annul(participation.order, getCurrentUser())
                }
                activityService.removeParticipant(occasion, customer)

                if (occasion?.participations?.size() < occasion?.maxNumParticipants) {
                    objectWatchNotificationService.sendActivityNotificationsFor(occasion.id)
                }
            } catch (PaymentException pe) {
                log.error(pe)
                flash.error = message(code: "facilityActivityOccasion.removeParticipant.error")
                redirect(action: "edit", id: occasion.id)
                return
            }

            flash.message = message(code: "facilityActivityOccasion.removeParticipant.success",
                    args: [customer.fullName()])
        } else {
            flash.error = message(code: "facilityActivityOccasion.removeParticipant.noCustomer")
        }

        redirect(action: "edit", id: occasion.id)
    }

    def removeParticipantAndRefund() {
        removeParticipant(true)
    }

    def removeParticipantWithoutRefund() {
        removeParticipant(false)
    }

    def deleteAndRefund(DeleteAndRefundActivityOccasionCommand cmd) {
        if (!cmd.validate()) {
            flash.error = message(code: "facilityActivityOccasion.delete.notFound")
            redirect(controller: "facilityActivity", action: "index")
            return
        }

        ActivityOccasion activityOccasion = ActivityOccasion.get(cmd.id)

        assertFacilityAccess(activityOccasion.activity.facility)

        if (activityOccasion == null) {
            flash.error = message(code: "facilityActivityOccasion.delete.notFound")
            redirect(controller: "facilityActivity", action: "index")
            return
        }

        Long activityId = activityOccasion.activity.id

        try {
            activityService.cancelOccasionWithFullRefundManually(activityOccasion)
        } catch (Throwable t) {
            log.error t
            flash.error = message(code: "facilityActivityOccasion.delete.blockingPayments")
            redirect(action: "edit", id: activityOccasion.id)
            return
        }

        flash.message = message(code: "facilityActivityOccasion.delete.success")
        redirect(controller: "facilityActivity", action: "occasions", id: activityId)
    }

    def save(CreateActivityOccasionsCommand cmd) {

        // validate form and nested forms (occasions)
        def valid = true
        valid = cmd.validate()
        cmd.occasions.each {
            it.slots = slotService.getSlots(Slot.parseAll(it.slotIds))
            if (!it.validate()) {
                valid = false
            }
        }

        def customer = Customer.get(cmd.customerId)
        def activity = ClassActivity.get(cmd.activityId)
        def activities = activityService.getActiveClassActivitiesByFacility(getUserFacility())

        if (!valid) {
            render(view: "create", model: [activities: activities, command: cmd]);
        } else {
            if (activity) {
                cmd.occasions.each {
                    def comments = params.comment ? ["comment": params.comment, "showComment": params.boolean("showComment")] : [:]
                    // create occasion
                    activityService.createOccasion(activity, it.date, it.startTime, it.endTime, it.slots,
                            customer, it.price, it.numParticipants, it.message, it.availableOnline,
                            it.signUpDaysInAdvanceRestriction, it.signUpDaysUntilRestriction, it.membersOnly, comments, it.minNumParticipants, it.cancelHoursInAdvance)
                }

                flash.message = message(code: "facilityActivityOccasion.save.success",
                        args: [cmd.occasions.size(), activity.name])

            } else {
                throw new IllegalArgumentException("Could not find activity with id ${cmd.activityId}")
            }

            redirect(controller: "facilityActivity", action: "occasions", id: activity.id)
        }

    }

    def delete() {
        def occasion = ActivityOccasion.get(params.id)
        def activity = occasion.activity

        assertFacilityAccess(occasion.activity.facility)

        try {
            activityService.removeOccasion(occasion, null, ActivityOccasion.DELETE_REASON.MANUAL_NO_REFUND)
        } catch (IllegalArgumentException e) {
            log.error(e)
            flash.error = message(code: "facilityActivityOccasion.delete.error")
            redirect(action: "edit", id: params.id)
            return
        }

        redirect(controller: "facilityActivity", action: "occasions", id: activity.id)
    }

    def createActivityWatch(Long occasionId, Long customerId) {
        def occasion = ActivityOccasion.get(occasionId)
        if (!occasion) {
            flash.error = message(code: "facilityActivityOccasion.delete.notFound")
            redirect(controller: "facilityActivity", action: "index")
            return
        }
        assertFacilityAccess(occasion.activity.facility)

        def customer = Customer.get(customerId)
        if (!customer?.user) {
            flash.error = message(code: "facilityActivityOccasion.edit.watchQueue.customersSearchNote")
            redirect(action: "edit", id: occasionId)
            return
        }
        assertFacilityAccess(customer.facility)

        if (occasion.isParticipating(customer.user)) {
            flash.error = message(code: "facilityActivityOccasion.createActivityWatch.alreadyParticipant")
            redirect(action: "edit", id: occasionId)
            return
        }

        objectWatchNotificationService.addNotificationFor(customer.user,
                occasion.activity, occasion.date.toDateTime(occasion.startTime).toDate(), false)

        flash.message = message(code: "facilityActivityOccasion.createActivityWatch.success")
        redirect(action: "edit", id: occasionId)
    }

    def removeActivityWatch(Long occasionId, Long userId) {
        def occasion = ActivityOccasion.get(occasionId)
        if (!occasion) {
            flash.error = message(code: "facilityActivityOccasion.delete.notFound")
            redirect(controller: "facilityActivity", action: "index")
            return
        }
        assertFacilityAccess(occasion.activity.facility)

        ClassActivityWatch.findByUserAndClassActivityAndFromDate(User.get(userId),
                occasion.activity, occasion.date.toDateTime(occasion.startTime).toDate())?.delete()

        flash.message = message(code: "facilityActivityOccasion.removeActivityWatch.success")
        redirect(action: "edit", id: occasionId)
    }

    private List<ActivityOccasionCommand> createActivityOccasionsCommands(def groupedSlots) {
        def result = []
        groupedSlots.each {
            def date = it.key
            def slots = it.value
            result << new ActivityOccasionCommand(date: date,
                    slotIds: slots.collect { it.id }.join(","),
                    startTime: new LocalTime(slots.first().startTime),
                    endTime: new LocalTime(slots.last().endTime),
                    slots: slots
            )
        }
        return result
    }

    private def getOccasionSlots(FacilityBookingCommand cmd) {
        def originalSlots = Slot.findAllByIdInList(Slot.parseAll(cmd.slotId))

        if (cmd.useRecurrence) {
            def slots = slotService.getRecurrenceSlots(new DateTime(cmd.recurrenceStart),
                    new DateTime(cmd.recurrenceEnd), cmd.weekDays,
                    cmd.frequency, cmd.interval, originalSlots)

            return [free: slots.freeSlots, unavailable: slots.unavailableSlots];
        } else {
            return [free: originalSlots, unavailable: []];
        }

    }
}

@Validateable(nullable = true)
class CreateActivityOccasionsCommand {
    Long activityId
    Long customerId
    List<ActivityOccasionCommand> occasions = [].withLazyDefault { new ActivityOccasionCommand() }

    static constraints = {
        customerId(nullable: false)
        activityId(nullable: false)
    }
}

@Validateable(nullable = true)
class ActivityOccasionCommand {
    String slotIds
    LocalDate date
    LocalTime startTime
    LocalTime endTime
    int price
    int numParticipants
    String message
    boolean availableOnline
    Integer signUpDaysInAdvanceRestriction
    Integer signUpDaysUntilRestriction
    boolean membersOnly
    Integer minNumParticipants
    Integer cancelHoursInAdvance

    def slots = []

    static constraints = {
        price(min: 0)
        numParticipants(min: 1)
        date(nullable: false)
        startTime(nullable: false)
        endTime(nullable: false, validator: { val, obj ->
            return !val.isBefore(obj.startTime)
        })
        message(nullable: true, blank: true, maxSize: 255)
    }

}

@Validateable(nullable = true)
class UpdateActivityOccasionCommand {
    Long id // activity occasion id
    LocalTime startTime
    LocalTime endTime
    int price
    int maxNumParticipants
    String message
    boolean availableOnline
    Integer signUpDaysInAdvanceRestriction
    Integer signUpDaysUntilRestriction
    boolean membersOnly
    Integer minNumParticipants
    Integer cancelHoursInAdvance

    static constraints = {
        price(min: 0)
        maxNumParticipants(min: 1)
        startTime(nullable: false)
        endTime(nullable: false, validator: { val, obj ->
            return !val.isBefore(obj.startTime)
        })
        message(nullable: true, blank: true, maxSize: 255)
        minNumParticipants validator: { val, obj ->
            return (val <= obj.maxNumParticipants)
        }
    }

}

@Validateable(nullable = true)
class DeleteAndRefundActivityOccasionCommand {
    Long id

    static constraints = {
        id(nullable: false)
    }

}
