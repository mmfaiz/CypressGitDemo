package com.matchi.facility

import com.matchi.GenericController
import com.matchi.Subscription
import groovyx.gpars.GParsPool
import org.springframework.beans.factory.annotation.Value

/**
 * @author Sergei Shushkevich
 */
class FacilitySubscriptionDeleteController extends GenericController {

    static scope = "prototype"

    def scheduledTaskService
    def subscriptionService

    @Value('${matchi.subscription.delete.batchSize}')
    Integer batchSize

    @Value('${matchi.subscription.delete.poolSize}')
    Integer poolSize

    def index() {
        redirect(action: "delete")
    }

    def deleteFlow = {
        entry {
            action { FacilitySubscriptionFilterCommand cmd ->
                log.info("Flow action entry")

                flow.returnUrl = params.returnUrl
                log.debug("Return url: ${flow.returnUrl}")

                if (!params.allselected && !params.list("subscriptionId")) {
                    flow.returnUrl += addParam(flow.returnUrl, "error",
                            message(code: "facility.subscription.bulkDelete.error"))
                    return error()
                }

                def facility = getCurrentUser().facility
                flow.facility = getUserFacility()

                flow.subscriptionIds = []
                if (params.allselected) {
                    flow.subscriptionIds = subscriptionService.getSubscriptions(facility, cmd).rows.collect { it.id }
                } else {
                    params.list("subscriptionId").each { flow.subscriptionIds << Long.parseLong(it) }
                }
                flash.subscriptions = subscriptionService.getSubscriptionsSummary(flow.subscriptionIds)

                if (!flash.subscriptions) {
                    return error()
                }
            }
            on("success").to "confirm"
            on("error").to "finish"
        }
        confirm {
            log.info("Flow view confirm")
            on("cancel").to "finish"
            on("submit").to "deleteSubscriptions"
        }
        deleteSubscriptions {
            action {
                log.info("Flow action deleteSubscriptions")
                def facility = getCurrentUser().facility
                scheduledTaskService.scheduleTask(
                        message(code: "facility.subscription.bulkDelete.taskName"), facility.id,
                        facility, message(code: "facility.subscription.bulkDelete.success")) {

                    GParsPool.withPool(poolSize) {
                        flow.subscriptionIds.collate(batchSize).eachParallel { ids ->
                            Subscription.withNewSession {
                                ids.each { id ->
                                    Subscription.withTransaction {
                                        def s = Subscription.findById(id,
                                                [fetch: [bookingGroup: "join", customer: "join"], lock: true])
                                        if (s) {
                                            subscriptionService.deleteSubscription(s)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            on("success").to "finish"
            on(Exception).to "finish"
        }
        finish {
            redirect(url: flow.returnUrl)
        }
    }
}
