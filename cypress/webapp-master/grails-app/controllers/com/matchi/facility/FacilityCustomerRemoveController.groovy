package com.matchi.facility
import com.matchi.Customer
import com.matchi.GenericController

class FacilityCustomerRemoveController extends GenericController {

    static scope = "prototype"

    def customerService

    def index() {}

    def removeFlow = {
        entry {
            action { FilterCustomerCommand cmd ->
                log.info("Flow action entry")

                if (!params.allselected && params.list("customerId")?.size() < 1) {
                    flow.error = message(code: "facilityCustomerRemove.remove.noCustomersSelected")
                    return error()
                }

                def facility = getCurrentUser().facility
                flow.returnUrl = params.returnUrl
                log.debug("Return url: ${flow.returnUrl}")

                def customerIds = params.allselected ?
                        customerService.findCustomers(cmd, facility, params.boolean("archived"), true) :
                        params.list("customerId").collect { Long.parseLong(it) }

                flow.customers = customerService.getDistinctCustomersFromIdsList(customerIds)

                log.debug("${flow.customers?.size()} customers")

                flow.persistenceContext.clear()
                success()
            }
            on("success").to "confirm"
            on("error").to "confirm"
        }
        confirm {
            log.info("Flow view confirm and remove customers")
            on("cancel").to "cancel"
            on("next").to "removeCustomers"
        }
        removeCustomers {
            action {
                log.info("Flow action remove customers")
                if (params.list("customerId")?.size() < 1) {
                    flow.error = message(code: "facilityCustomerRemove.remove.noCustomersSelected")
                    return error()
                }

                def customerIds = []
                params.list("customerId").each { customerIds << Long.parseLong(it) }
                flow.customers = customerService.getCustomersFromIdsList(customerIds)

                flow.customers.each { Customer customer ->
                    customerService.clearCustomer(customer)
                }

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
