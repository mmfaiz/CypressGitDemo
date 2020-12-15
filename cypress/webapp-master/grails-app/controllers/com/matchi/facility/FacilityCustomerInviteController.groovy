package com.matchi.facility
import com.matchi.Customer
import com.matchi.GenericController

class FacilityCustomerInviteController extends GenericController {

    static scope = "prototype"

    def ticketService
    def notificationService
    def customerService

    def index() {}

    def inviteFlow = {
        entry {
            action { FilterCustomerCommand cmd ->
                log.info("Flow action entry")

                if (!params.allselected && params.list("customerId")?.size() < 1) {
                    flow.error = message(code: "facilityCustomerInvite.invite.noCustomersSelected")
                    return error()
                }

                def facility = getCurrentUser().facility
                flow.returnUrl = params.returnUrl
                log.debug("Return url: ${flow.returnUrl}")

                def customerIds = params.allselected ?
                        customerService.findCustomers(cmd, facility, false, true) :
                        params.list("customerId").collect { Long.parseLong(it) }

                flow.customers = Customer.createCriteria().listDistinct {
                    join "user"
                    inList('id', customerIds)
                    order("number", "asc")
                }

                log.debug("${flow.customers?.size()} customers")

                flow.persistenceContext.clear()
                success()
            }
            on("success").to "confirm"
            on("error").to "confirm"
        }
        confirm {
            log.info("Flow view confirm and invite customers")
            on("cancel").to "cancel"
            on("next").to "inviteCustomers"
        }
        inviteCustomers {
            action {
                log.info("Flow action invite customers")
                if (params.list("customerId")?.size() < 1) {
                    flow.error = message(code: "facilityCustomerInvite.invite.noCustomersSelected")
                    return error()
                }

                def customerIds = []
                params.list("customerId").each { customerIds << Long.parseLong(it) }
                flow.customers = Customer.createCriteria().list { inList('id', customerIds) }

                flow.customers.each { Customer customer ->
                    def ticket = ticketService.createCustomerInviteTicket(customer)
                    notificationService.sendCustomerInvitation(customer, ticket)
                }

                flow.persistenceContext.clear()
                success()
            }
            on("return").to "confirm"
            on("error").to "confirm"
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
