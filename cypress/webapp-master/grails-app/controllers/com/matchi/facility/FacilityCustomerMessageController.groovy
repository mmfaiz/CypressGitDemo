package com.matchi.facility

import com.matchi.Customer
import com.matchi.Facility
import com.matchi.GenericController

class FacilityCustomerMessageController extends GenericController {

    static scope = "prototype"

    def customerService
    def notificationService
    def ticketService

    def index() {}

    def messageFlow = {
        entry {
            action { FilterCustomerCommand cmd ->
                log.info("Flow action entry")
                flow.returnUrl = params.returnUrl
                log.debug("returnUrl: ${flow.returnUrl}")

                if (!session[CUSTOMER_IDS_KEY] && !params.allselected && !params.list("customerId")) {
                    flow.error = message(code: "facilityCustomerMessage.message.noCustomersSelected")
                    return error()
                }

                String defaultOriginTitleCode = "facilityCustomer.show.list.facilityCustomer"
                flow.originTitle   = message(code: params.originTitle ?: defaultOriginTitleCode)

                flow.facility = getCurrentUser().facility
                flow.user     = getCurrentUser()

                flow.customerIds = []
                def customerResult

                if (params.allselected) {
                    customerResult = customerService.findCustomers(cmd, flow.facility)

                } else {
                    if (session[CUSTOMER_IDS_KEY]) {
                        flow.customerIds = session[CUSTOMER_IDS_KEY]
                        session.removeAttribute(CUSTOMER_IDS_KEY)
                    } else {
                        params.list("customerId").each {
                            if (it) {
                                flow.customerIds << Long.parseLong(it)
                            }
                        }
                    }
                    customerResult = Customer.createCriteria().list {
                        inList('id', flow.customerIds ?: [-1L])
                        order("number", "asc")
                    }
                }

                def canRecieve = customerResult.findAll { Customer c -> c.isEmailReceivable() }

                flow.customerIds = canRecieve.collect { it.id }
                def cantRecieve = (customerResult - canRecieve)

                def customerInfo = []
                def guardianInfo = []
                def customerMails = []
                def guardianMails = []

                canRecieve.each { Customer c ->
                    def receiveMail = c.email
                    def thisCustomerInfo = c.getEmailCustomerInfo()
                    if (receiveMail && !customerInfo.contains(thisCustomerInfo)) {
                        customerInfo << thisCustomerInfo
                        customerMails << receiveMail
                    }

                    if(c.hasGuardianEmails()) {
                        def thisGuardianInfo = c.getGuardianMessageInfo()
                        if(!guardianInfo.contains(thisGuardianInfo)) {
                            guardianInfo.addAll(thisGuardianInfo)
                            guardianMails.addAll(thisGuardianInfo.collect { it.email })
                        }
                    }
                }

                def uniqueGuardianInfo = guardianInfo.unique()

                flow.customerInfo = customerInfo
                flow.customerInfoAll = customerInfo + uniqueGuardianInfo

                flow.cantReceiveCustomerInfo = []
                cantRecieve.each {
                    flow.cantReceiveCustomerInfo << it.getEmailCustomerInfo()
                }

                def uniqueCustomerMails = customerMails.unique()
                def uniqueAllMails = uniqueCustomerMails + guardianMails.unique()

                flow.nMails = uniqueCustomerMails.size()
                flow.nMailsAll = uniqueAllMails.size()

                flow.receiveString = uniqueCustomerMails.join(";")
                flow.receiveStringAll = uniqueAllMails.join(";")

                log.debug("${flow.customerInfoAll?.size()} customers")

                flow.persistenceContext.clear()
                success()
            }
            on("success").to "createMessage"
            on("error").to "createMessage"
        }
        createMessage {
            log.info("Flow view create message")
            on("cancel").to "cancel"
            on("next").to "sendMessage"
        }
        sendMessage {
            action {
                log.info("Flow action create message")
                if (flow.customerIds?.size() < 1) {
                    flow.error = message(code: "facilityCustomerMessage.message.noCustomersSelected")
                    return error()
                }
                if (!params.message) {
                    flow.error = message(code: "facilityCustomerMessage.message.noMessage")
                    return error()
                }

                def mailSubject = params.subject
                def mailMessage = params.message
                def fromMail = params.fromMail

                Facility facility = getCurrentUser().getFacility()
                List<Customer> selectedCustomers = Customer.createCriteria().list { inList('id', flow.customerIds) }
                List<Customer> finalCustomerList

                if (params.boolean("uniqueEmail")) {
                    finalCustomerList = selectedCustomers.groupBy {
                        it.email
                    }.collect { email, customers ->
                        def customer = customers[0]
                        if (customers.size() > 1) {
                            def c = customers.find {
                                it.membership?.family?.contact
                            }
                            if (c) {
                                return c.membership.family.contact
                            }
                        }

                        return customer
                    }
                } else {
                    finalCustomerList = selectedCustomers
                }

                List<Long> ids = finalCustomerList*.id
                boolean includeGuardian = params.boolean('includeGuardian', false)

                Closure closure = { List<Long> customerIds ->
                    Customer.withTransaction {
                        List<Customer> customers = Customer.createCriteria().list {
                            inList("id", customerIds)
                        }

                        customers.each {
                            notificationService.sendCustomerMessage(
                                    it,
                                    mailMessage,
                                    mailSubject,
                                    fromMail,
                                    includeGuardian,
                                    ticketService.createCustomerDisableMessagesTicket(it))
                        }

                    }
                }

                notificationService.executeSending(ids, message(code: "facilityCustomerMessage.message.taskName") as String, facility, closure)

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