package com.matchi.facility
import com.matchi.GenericController
import com.matchi.Season
import com.matchi.Subscription
import groovyx.gpars.GParsPool
import java.util.concurrent.ConcurrentHashMap
import org.joda.time.DateTime
import org.springframework.beans.factory.annotation.Value

class FacilitySubscriptionCopyController extends GenericController  {

    static scope = "prototype"

    def subscriptionService
    def slotService
    def seasonService
    def bookingService
    def scheduledTaskService

    @Value('${matchi.subscription.copy.batchSize}')
    Integer batchSize

    @Value('${matchi.threading.numberOfThreads}')
    Integer poolSize

    def index() {
        redirect(action: "copy")
    }

    def copyFlow = {
        entry {
            action { FacilitySubscriptionFilterCommand cmd ->
                log.info("Flow action entry")
                def facility = getCurrentUser().facility
                def fromSeason = Season.get(params.long("season"))

                def subscriptions
                def subscriptionIds
                // get all subscriptions that are being invoiced
                if (params.allselected) {
                    def subscriptionsResult = subscriptionService.getSubscriptions(facility, cmd)
                    subscriptionIds = subscriptionsResult.rows.collect { it.id }
                } else {
                    subscriptionIds = params.list("subscriptionId").collect { Long.parseLong(it) }
                }

                if (subscriptionIds?.size() < 1) {
                    flow.error = message(code: "facilitySubscriptionCopy.copy.noSubscriptionsSelected")
                    return error()
                }

                subscriptions = Subscription.withCriteria {
                    inList("id", subscriptionIds)
                }

                flow.upcomingSeasons = seasonService.getUpcomingSeasons(facility, fromSeason)

                if (flow.upcomingSeasons?.size() < 1) {
                    flow.error = message(code: "facilitySubscriptionCopy.copy.noRecentSeason")
                    return error()
                }


                flow.facility = getUserFacility()
                flow.subscriptionInfo = []
                flow.nonActiveSubscriptionsCount = 0
                subscriptions.each { Subscription subscription ->
                    if (subscription.status == Subscription.Status.ACTIVE) {
                        flow.subscriptionInfo << parseSubscriptionInfo(subscription)
                    } else {
                        flow.nonActiveSubscriptionsCount++
                    }
                }

                flow.persistenceContext.clear()
                success()
            }
            on("success").to "enterDetails"
            on("error").to "enterDetails"
        }
        enterDetails {
            log.info("Flow view enterDetails")
            on("cancel").to "cancel"
            on("next").to "copyResult"
        }
        copyResult {
            action {
                log.info("Flow action copyResult")
                if (params.list("subscriptionId")?.size() < 1) {
                    flash.error = message(code: "facilitySubscriptionCopy.copy.noSubscriptionsSelected")
                    return error()
                }

                def subscriptionIds = []
                params.list("subscriptionId").each { subscriptionIds << Long.parseLong(it) }

                flow.fromDate = new DateTime(params.fromDate).toDateMidnight().toDateTime()
                flow.toDate   = new DateTime(params.toDate).toDateMidnight().toDateTime()
                flow.copySubscriptionResult = [ fromDate: flow.fromDate, toDate: flow.toDate,
                        subscriptions: [].asSynchronized(),
                        subscriptionUnavailableSlots: new ConcurrentHashMap()]
                def facility = getUserFacility()

                GParsPool.withPool(poolSize) {
                    subscriptionIds.collate(batchSize).eachParallel { ids ->
                        Subscription.withNewSession {
                            Subscription.withCriteria {
                                inList("id", ids)
                                customer {
                                    eq("facility", facility)
                                }
                            }.each { subscription ->
                                flow.copySubscriptionResult.subscriptions << parseSubscriptionInfo(subscription)

                                def slotsContainer = slotService.getRecurrenceSlots(flow.fromDate, flow.toDate,
                                        [new DateTime(subscription.firstSlot().startTime).dayOfWeek.toString()],
                                        1, subscription.timeInterval, [subscription.firstSlot()], false)
                                flow.copySubscriptionResult.subscriptionUnavailableSlots[subscription.id] =
                                        slotsContainer.unavailableSlots.collect { it.shortDescription }
                            }
                        }
                    }
                }

                flow.persistenceContext.clear()
                success()
            }
            on("success").to "confirm"
            on("return").to "enterDetails"
            on("error").to "enterDetails"
        }
        confirm {
            log.info("Flow view confirm")
            on("cancel").to "cancel"
            on("previous").to "enterDetails"
            on("next").to "copySubscriptions"
        }
        copySubscriptions {
            action {
                log.info("Flow action copy subscriptions")

                def subscriptionIds = []
                params.list("subscriptionId").each { subscriptionIds << Long.parseLong(it) }
                def user = getCurrentUser()
                def facility = user.facility
                def fromDate = flow.copySubscriptionResult.fromDate.toDate()
                def toDate = flow.copySubscriptionResult.toDate.toDate()

                scheduledTaskService.scheduleTask(
                        message(code: "facilitySubscriptionCopy.copy.taskName"), facility.id, facility) {
                    subscriptionService.copySubscriptions(fromDate, toDate,
                            Subscription.withCriteria { inList("id", subscriptionIds) }, user.id)
                }

                flow.copySubscriptionResult = [ fromDate: flow.fromDate, toDate: flow.toDate ]

                flow.persistenceContext.clear()
                success()
            }
            on("return").to "confirm"
            on("error").to "confirm"
            on("success").to "confirmation"
        }
        confirmation()

        cancel {
            redirect(controller: "facilitySubscription", action: "index", params: [error: flash.error])
        }
    }

    private def parseSubscriptionInfo(Subscription subscription) {
        def firstSlot = subscription.slots.first()
        def lastSlot = subscription.slots.last()
        def customer = subscription.customer
        def court = subscription?.court?.name
        def startTime = subscription.time.toString("HH:mm")
        def startDate = new DateTime(firstSlot.startTime).toString("yyyy-MM-dd")
        def endDate = new DateTime(lastSlot.startTime).toString("yyyy-MM-dd")
        def nrOfSlots = subscription.slots.size()

        return [ id: subscription.id,
                 weekDay: subscription.weekday,
                 customer: customer,
                 startTime: startTime,
                 court: court,
                 startDate: startDate,
                 endDate: endDate,
                 nrOfSlots:nrOfSlots ]
    }
}
