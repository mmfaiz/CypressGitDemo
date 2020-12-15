package com.matchi.facility

import com.matchi.Customer
import com.matchi.GenericController
import com.matchi.Group

/**
 * @author Sergei Shushkevich
 */
class FacilityCustomerGroupController extends GenericController {

    static scope = "prototype"

    def customerService
    def groupService
    def userService

    def index() {
        redirect(action: "add")
    }

    def addFlow = {
        entry {
            action { FilterCustomerCommand cmd ->
                log.info("Flow action entry")

                flow.returnUrl = params.returnUrl
                log.debug("Return url: ${flow.returnUrl}")

                if (!session[CUSTOMER_IDS_KEY] && !params.allselected && !params.list("customerId")) {
                    flow.returnUrl += addParam(flow.returnUrl, "error",
                            message(code: "facility.customer.addToGroup.error"))
                    return error()
                }

                def facility = getCurrentUser().facility
                def customerIds = []
                if (params.allselected) {
                    customerIds = customerService.findCustomers(cmd, facility, false, true)
                } else {
                    if (session[CUSTOMER_IDS_KEY]) {
                        customerIds = session[CUSTOMER_IDS_KEY]
                        session.removeAttribute(CUSTOMER_IDS_KEY)
                    } else {
                        params.list("customerId").each { customerIds << Long.parseLong(it) }
                    }
                }
                flow.customers = Customer.createCriteria().listDistinct {
                    inList('id', customerIds)
                    order("number", "asc")
                }
                log.debug("${flow.customers?.size()} customers")

                flow.facilityGroups = Group.findAllByFacility(userService.getUserFacility())
            }
            on("success").to "selectGroups"
            on("error").to "finish"
        }
        selectGroups {
            log.info("Flow view selectGroups")
            on("submit") {
                flow.groups = params.list("groupId").collect {
                    Group.get(it)
                }
                if (!flow.groups) {
                    flash.error = message(code: "facility.customer.addToGroup.select.error")
                    return error()
                }
            }.to "confirm"
            on("cancel").to "finish"
        }
        confirm {
            log.info("Flow view confirm")
            on("cancel").to "finish"
            on("submit").to "addCustomersToGroups"
        }
        addCustomersToGroups {
            action {
                log.info("Flow action addCustomersToGroups")
                flow.customers.each { c ->
                    flow.groups.each { g ->
                        groupService.addCustomerToGroup(g, c)
                    }
                }
                flow.returnUrl += addParam(flow.returnUrl, "message",
                        message(code: "facility.customer.addToGroup.success",
                                args: [flow.customers.size(), flow.groups.name.join(', '), flow.groups.size()]))
            }
            on("success").to "finish"
            on(Exception).to "finish"
        }
        finish {
            redirect(url: flow.returnUrl)
        }
    }

    def removeFlow = {
        entry {
            action { FilterCustomerCommand cmd ->
                log.info("Flow action entry")

                flow.returnUrl = params.returnUrl
                log.debug("Return url: ${flow.returnUrl}")

                if (!session[CUSTOMER_IDS_KEY] && !params.allselected && !params.list("customerId")) {
                    flow.returnUrl += addParam(flow.returnUrl, "error",
                            message(code: "facility.customer.addToGroup.error"))
                    return error()
                }

                def facility = getCurrentUser().facility
                def customerIds = []
                if (params.allselected) {
                    customerIds = customerService.findCustomers(cmd, facility, false, true)
                } else {
                    if (session[CUSTOMER_IDS_KEY]) {
                        customerIds = session[CUSTOMER_IDS_KEY]
                        session.removeAttribute(CUSTOMER_IDS_KEY)
                    } else {
                        params.list("customerId").each { customerIds << Long.parseLong(it) }
                    }
                }
                flow.customers = Customer.createCriteria().listDistinct {
                    inList('id', customerIds)
                    order("number", "asc")
                }
                log.debug("${flow.customers?.size()} customers")

                flow.facilityGroups = Group.findAllByFacility(userService.getUserFacility())
            }
            on("success").to "selectGroups"
            on("error").to "finish"
        }
        selectGroups {
            log.info("Flow view selectGroups")
            on("submit") {
                flow.groups = params.list("groupId").collect {
                    Group.get(it)
                }
                if (!flow.groups) {
                    flash.error = message(code: "facility.customer.addToGroup.select.error")
                    return error()
                }
            }.to "confirm"
            on("cancel").to "finish"
        }
        confirm {
            log.info("Flow view confirm")
            on("cancel").to "finish"
            on("submit").to "addCustomersToGroups"
        }
        addCustomersToGroups {
            action {
                log.info("Flow action addCustomersToGroups")
                flow.customers.each { c ->
                    flow.groups.each { g ->
                        groupService.removeCustomerFromGroup(g, c)
                    }
                }
                flow.returnUrl += addParam(flow.returnUrl, "message",
                        message(code: "facility.customer.removeFromGroup.success",
                                args: [flow.customers.size(), flow.groups.name.join(', '), flow.groups.size()]))
            }
            on("success").to "finish"
            on(Exception).to "finish"
        }
        finish {
            redirect(url: flow.returnUrl)
        }
    }
}
