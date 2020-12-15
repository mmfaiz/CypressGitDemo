package com.matchi.facility

import com.matchi.Customer
import com.matchi.GenericController

class FacilityCustomerUpdateRequestController extends GenericController {

    static scope = "prototype"

    def ticketService
    def notificationService
    def customerService

    def index() {
        redirect(action: "sendRequest")
    }

    def sendRequestFlow = {
        entry {
            action { FilterCustomerCommand cmd ->
                log.info("Flow action entry")
                flow.returnUrl = params.returnUrl
                log.debug("returnUrl: ${flow.returnUrl}")

                if (!params.allselected && params.list("customerId")?.size() < 1) {
                    flow.error = message(code: "facilityCustomerUpdateRequest.sendRequest.noCustomersSelected")
                    return error()
                }

                def facility = getCurrentUser().facility

                flow.customerIds = []
                def customerResult

                if (params.allselected) {
                    customerResult = customerService.findCustomers(cmd, facility)

                } else {
                    params.list("customerId").each { flow.customerIds << Long.parseLong(it) }
                    customerResult = Customer.createCriteria().list {
                        inList('id', flow.customerIds)
                        order("number", "asc")
                    }
                }

                def canRecieve = customerResult.findAll { it.email || it.guardianEmail }
                flow.customerIds = canRecieve.collect { it.id }
                flow.cantRecieve = (customerResult - canRecieve).collect { it.id }

                flow.customerInfo = []
                canRecieve.each {
                    def recieveMail = it.email ?: it.guardianEmail
                    flow.customerInfo << [ number: it.number, name: it.fullName(), email: recieveMail ]
                }


                log.debug("${flow.customerInfo?.size()} customers")

                flow.persistenceContext.clear()
                success()
            }
            on("success").to "createMessage"
            on("error").to "createMessage"
        }
        createMessage {
            log.info("Flow view create message and send update request")
            on("cancel").to "cancel"
            on("next").to "sendUpdateRequest"
        }
        sendUpdateRequest {
            action {
                log.info("Flow action send customer update request.")
                if (flow.customerIds?.size() < 1) {
                    flow.error = message(code: "facilityCustomerUpdateRequest.sendRequest.noCustomersSelected")
                    return error()
                }

                def customers = Customer.createCriteria().list {
                    inList('id', flow.customerIds)
                    order("number", "asc")
                }
                flow.textMessage = params.message

                customers.each { Customer customer ->
                    def ticket = ticketService.createCustomerUpdateTicket(customer)
                    notificationService.sendCustomerUpdateRequest(customer, ticket, flow.textMessage)
                }

                flow.persistenceContext.clear()
                success()
            }
            on("return").to "createMessage"
            on("error").to "createMessage"
            on("success").to "confirmation"
        }
        confirmation()

        cancel {
            if (flow.returnUrl) {
                redirect(url: flow.returnUrl, params: flash.error)
                return
            }

            redirect(controller: "facilityCustomer", action: "index", params: [error: flash.error])
        }
    }
}
