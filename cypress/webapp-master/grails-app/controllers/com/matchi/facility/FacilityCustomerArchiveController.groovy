package com.matchi.facility

import com.matchi.*

class FacilityCustomerArchiveController extends GenericController {

    public static final String LIST_FILTER_KEY = "facility_customer_archive_filter"

    static scope = "prototype"

    def customerService

    def index(FilterCustomerCommand filter) {
        log.debug("Fetching archive")

        if (!params.boolean("reset") && session[LIST_FILTER_KEY]?.isNotExpired()) {
            filter = session[LIST_FILTER_KEY]
        } else {
            session[LIST_FILTER_KEY] = filter
        }

        def facility = (Facility)getUserFacility()
        def facilityGroups = Group.findAllByFacility(facility, [sort: "name"]).asList()
        def birthyears = Customer.birthyears(facility).list()
        def seasons = Season.findAllByFacility(facility, [sort: "startTime", order: "desc"])
        def archived = customerService.findArchivedCustomers(filter, facility)

        [ facility: facility, archived: archived, filter: filter, facilityGroups: facilityGroups,
                types: facility.membershipTypes, birthyears: birthyears, seasons: seasons ]
    }

    def archiveFlow = {
        entry {
            action { FilterCustomerCommand cmd ->
                if (!params.allselected && params.list("customerId")?.size() < 1) {
                    flow.error = message(code: "facilityCustomerArchive.archive.noCustomersSelected")
                    return error()
                }

                def facility = getCurrentUser().facility
                def customerIds = params.allselected ?
                        customerService.findCustomers(cmd, facility, false, true) :
                        params.list("customerId").collect { Long.parseLong(it) }

                flow.customers = Customer.createCriteria().list { inList('id', customerIds) }
                log.debug("${flow.customers?.size()} customers")

                [ customers: flow.customers ?: null ]
            }
            on("error").to "cancel"
            on(Exception).to "cancel"
            on("success").to "confirm"
        }
        confirm {
            on("cancel").to "cancel"
            on("submit").to "archiveCustomers"
        }
        archiveCustomers {
            action {
                flow.customers.each { Customer c ->
                    customerService.archiveCustomer(c)
                }
            }
            on("error").to "confirm"
            on(Exception).to "cancel"
            on("success").to "confirmation"
        }

        confirmation {
            redirect(action: "index")
        }

        cancel {
            redirect(controller: "facilityCustomer", action: "index")
        }
    }

    def unarchiveFlow = {
        entry {
            action { FilterCustomerCommand cmd ->
                def facility = getCurrentUser().facility
                def customerIds = []

                if (params.allselected) {
                    def customerResult = customerService.findArchivedCustomers(cmd, facility)
                    customerIds = customerResult.collect { it.id }
                } else if (params.customerId) {
                    params.list("customerId").each { customerIds << Long.parseLong(it) }
                }

                flow.customers = Customer.createCriteria().list { inList('id', customerIds) }

                [ customers: flow.customers ?: null ]
            }
            on("error").to "cancel"
            on(Exception).to "cancel"
            on("success").to "confirm"
        }
        confirm {
            on("cancel").to "cancel"
            on("submit").to "unarchiveCustomers"
        }
        unarchiveCustomers {
            action {
                flow.customers.each { Customer c ->
                    customerService.unArchiveCustomer(c)
                }
            }
            on("error").to "confirm"
            on(Exception).to "cancel"
            on("success").to "confirmation"
        }

        confirmation {
            redirect(action: "index")
        }

        cancel {
            redirect(controller: "facilityCustomerArchive", action: "index")
        }
    }
}
