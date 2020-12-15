package com.matchi.facility

import com.matchi.Customer
import com.matchi.Facility
import com.matchi.GenericController
import com.matchi.Subscription
import com.matchi.FacilityProperty

class FacilitySubscriptionMessageController extends GenericController {

    static scope = "prototype"

    def notificationService
    def subscriptionService
    def priceListService
    def index() {
        redirect(action: "message")
    }

    def messageFlow = {
        start {
            action { FacilitySubscriptionFilterCommand cmd ->

                flow.facility = getCurrentUser().facility
                flow.facilityMessage = flow.facility.getFacilityProperty(FacilityProperty.FacilityPropertyKey.SUBSCRIPTION_CONTRACT_TEXT)
                flow.user = getCurrentUser()
                flow.returnUrl = params.returnUrl

                // get all subscriptions that are being invoiced
                if (params.allselected) {
                    def subscriptionsResult = subscriptionService.getSubscriptions(flow.facility, cmd)
                    flow.subscriptionIds = subscriptionsResult.rows.collect { it.id }
                } else {
                    flow.subscriptionIds = params.list("subscriptionId").collect { Long.parseLong(it) }
                }

                if(flow.subscriptionIds.isEmpty()) {
                    return error()
                }

                flow.persistenceContext.clear()
                success()
            }
            on("success").to "createMessage"
            on("error").to "createMessage"
        }
        createMessage {
            on("cancel").to "cancel"
            on("next").to "sendMessage"
        }
        sendMessage {
            action {
                if (flow.subscriptionIds?.size() < 1) {
                    flow.error = message(code: "facilitySubscriptionMessage.message.noSubscriptionsSelected")
                    return error()
                }
                if (!params.message) {
                    flow.error = message(code: "facilitySubscriptionMessage.message.noMessage")
                    return error()
                }

                def messageText     = params.message
                def fromMail    = params.fromMail
                def bccMail     = params.bccMail
                def subscriptions = Subscription.createCriteria().list { inList('id', flow.subscriptionIds) }


                String taskName = message(code: "facilitySubscriptionMessage.message.taskName") as String
                Facility facility = getCurrentUser().facility

                Map<Customer,List<Subscription>> customerSubscriptions = subscriptions.groupBy { it.customer }
                List customerIdToSubscriptionIds = []

                customerSubscriptions.each { Customer customer, List<Subscription> subscriptionList ->
                    customerIdToSubscriptionIds << [ customerId: customer.id, subscriptionIds: subscriptionList*.id ]
                }

                notificationService.executeSending(customerIdToSubscriptionIds, taskName, facility) { ids ->
                    Customer.withTransaction {
                        Customer customer = Customer.get(ids.customerId)
                        List<Subscription> subscriptionList = Subscription.findAllByIdInListAndCustomer(ids.subscriptionIds, customer)
                        notificationService.sendSubscriptionInformationMessage(customer, subscriptionList, messageText, fromMail, bccMail)
                    }
                }

                flow.persistenceContext.clear()
                success()
            }
            on("return").to "createMessage"
            on("error").to "createMessage"
            on("success").to "done"
        }

        cancel {
            if (flow.returnUrl) {
                redirect(url: flow.returnUrl, params: flash.error)
                return
            }

            redirect(controller: "facilitySubscription", action: "index")
        }
        done {
            if (flow.returnUrl) {
                redirect(url: flow.returnUrl)
                return
            }

            redirect(controller: "facilitySubscription", action: "index")
        }
    }
}
