package com.matchi.facility

import com.matchi.Customer
import com.matchi.Facility
import com.matchi.GenericController
import com.matchi.PhoneUtil
import groovyx.gpars.GParsPool
import org.apache.http.HttpStatus

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class FacilityCustomerSMSMessageController extends GenericController {

    static scope = "prototype"

    def customerService
    def smsService

    def check() {
        render "check is: ${session?.smsSendJob}"
    }

    def status() {
        def statusCode = session?.smsSendJob?.done ? HttpStatus.SC_CREATED : HttpStatus.SC_OK

        render template: "/facilityCustomerSMSMessage/message/status", status: statusCode, model: [status: session?.smsSendJob]
    }

    def messageFlow = {
        entry {
            action { FilterCustomerCommand cmd ->
                flow.returnUrl = params.returnUrl

                if (!session[CUSTOMER_IDS_KEY] && !params.allselected && params.list("customerId")?.size() < 1) {
                    flow.error = message(code: "facilityCustomerSMSMessage.message.noCustomersSelected")
                    return error()
                }

                String defaultOriginTitleCode = "facilityCustomer.show.list.facilityCustomer"
                flow.originTitle   = message(code: params.originTitle ?: defaultOriginTitleCode)

                flow.cantRecieve = []
                flow.canReceive = []
                flow.failed = []
                flow.facilityId = getCurrentUser().facility.id

                flow.customerIds = []
                def customerResult

                if (params.allselected) {
                    customerResult = customerService.findCustomers(cmd, getCurrentUser().facility)

                } else {
                    if (session[CUSTOMER_IDS_KEY]) {
                        flow.customerIds = session[CUSTOMER_IDS_KEY]
                        session.removeAttribute(CUSTOMER_IDS_KEY)
                    } else {
                        params.list("customerId").each { flow.customerIds << Long.parseLong(it) }
                    }
                    customerResult = Customer.createCriteria().list {
                        inList('id', flow.customerIds)
                        order("number", "asc")
                    }
                }

                def canRecieve = customerResult.findAll {
                    it.cellphone && !it.cellphone?.isEmpty() && PhoneUtil.isValid(it.cellphone, it.country)
                }
                flow.customerIds = canRecieve.collect { it.id }
                flow.cantRecieve = (customerResult - canRecieve).collect { it.id }

                flow.customerInfo = []
                canRecieve.each {
                    flow.customerInfo << [ number: it.number, name: it.fullName(), phone: PhoneUtil.convertToInternationalFormat(it.cellphone, it.country)]
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

                if (flow.customerIds?.size() < 1) {
                    flow.error = message(code: "facilityCustomerSMSMessage.message.noCustomersSelected")
                    return error()
                }

                if (!params.message) {
                    flow.error = message(code: "facilityCustomerSMSMessage.message.noMessage")
                    return error()
                }

                def message = params.message
                def customers = Customer.createCriteria().list { inList('id', flow.customerIds) order("lastname") }

                def job = new SMSSendJob()
                session.smsSendJob = job
                customers.each { Customer customer ->
                    def status = new SMSSendJobStatus(customer)
                    job.add(status)
                }

                def facilityId = flow.facilityId


                int numberOfThreads = grailsApplication.config.matchi.threading.numberOfThreads
                ExecutorService service = Executors.newFixedThreadPool(1)

                try {
                    service.execute({
                        GParsPool.withPool(numberOfThreads) {
                            job.statuses.values().eachParallel { SMSSendJobStatus sendJob ->
                                Facility.withNewSession {
                                    Facility facility = Facility.get(facilityId)
                                    sendJob.status = "sending"

                                    def receipt = smsService.send(facility, sendJob.customerPhone, message)

                                    sendJob.status = receipt.status
                                    sendJob.text = receipt.text
                                }

                            }
                            job.done = true

                        }
                    })
                } finally {
                    service?.shutdown()
                }

                flow.receipts = []

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

public class SMSSendJob implements Serializable {
    private static final long serialVersionUID = 12L

    boolean done = false
    def statuses = [:];

    void add(SMSSendJobStatus status) {
        statuses.put(status.customerNumber, status)
    }

    SMSSendJobStatus get(String number) {
        statuses.get(number)
    }

    int getNumDone() {
        statuses.values().findAll { it.status == "ok" || it.status == "error" }.size()
    }

    int getTotal() {
        statuses.values().size()
    }

    boolean hasErrors() {
        statuses.values().findAll { it.status == "error" }.size() > 0
    }


    @Override
    public String toString() {
        return "SMSSendJob{" +
                "done=" + done +
                ", statuses=" + statuses +
                '}';
    }
}


public class SMSSendJobStatus implements Serializable{
    private static final long serialVersionUID = 12L

    String customerNumber
    String customerName
    String customerPhone
    String status
    String text

    SMSSendJobStatus(Customer customer) {
        customerName = customer.fullName()
        customerNumber = customer.number
        customerPhone = PhoneUtil.convertToInternationalFormat(customer.cellphone, customer.country)
        status = "waiting"
    }

    @Override
    public String toString() {
        return "SMSSendJobStatus{" +
                "customerNumber='" + customerNumber + '\'' +
                ", customerName='" + customerName + '\'' +
                ", customerPhone='" + customerPhone + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
