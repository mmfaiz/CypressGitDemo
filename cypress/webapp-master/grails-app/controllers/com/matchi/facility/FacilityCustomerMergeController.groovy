package com.matchi.facility
import com.matchi.Customer

class FacilityCustomerMergeController {

    static scope = "prototype"

    def customerMergeService

    def index() {
        redirect(action: "merge")
    }

    def mergeFlow = {
        entry {
            action {
                log.info("Flow action entry")
                if (!params.allselected && params.list("customerId")?.size() < 2) {
                    flow.error = message(code: "facilityCustomerMerge.merge.noCustomersSelected")
                    return error()
                }

                def customerIds = []
                params.list("customerId").each { customerIds << Long.parseLong(it) }
                flow.customers = Customer.createCriteria().list { inList('id', customerIds) }
                flow.customers.each {
                    it.user
                }

                flow.persistenceContext.clear()
                success()
            }
            on("success").to "primary"
            on("error").to "primary"
        }
        primary {
            log.info("Flow view primary")
            on("cancel").to "cancel"
            on("submit").to "mergeResult"
        }
        mergeResult {
            action {
                log.info("Flow action mergeResult")
                if (!params.primary) {
                    flash.error = message(code: "facilityCustomerMerge.merge.noPrimary")
                    return error()
                }

                flow.primary  = Customer.findById(params.primary)
                flow.merges = flow.customers - flow.primary

                flow.result = customerMergeService.mergeCustomersResult(flow.primary, flow.merges)

                flow.persistenceContext.clear()
                success()
            }
            on("success").to "confirm"
            on("return").to "primary"
            on("error").to "primary"
        }
        confirm {
            log.info("Flow view confirm and merge customers")
            on("cancel").to "cancel"
            on("previous").to "primary"
            on("submit").to "mergeCustomers"
        }
        mergeCustomers {
            action {
                log.info("Flow action merge customers")
                flow.primary = customerMergeService.mergeCustomers(flow.primary, flow.merges)

                flow.persistenceContext.clear()
                success()
            }
            on("return").to "confirm"
            on("error").to "confirm"
            on("success").to "confirmation"
        }
        confirmation()

        cancel {
            redirect(controller: "facilityCustomer", action: "index", params: [error: flash.error])
        }
    }
}
