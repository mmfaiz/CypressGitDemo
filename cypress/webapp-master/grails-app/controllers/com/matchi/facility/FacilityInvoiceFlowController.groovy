package com.matchi.facility

import com.matchi.GenericController
import com.matchi.InvoiceService
import com.matchi.NotificationService
import com.matchi.invoice.Invoice
import org.apache.commons.validator.EmailValidator

class FacilityInvoiceFlowController extends GenericController {

    static scope = "prototype"

    def invoiceService
    def notificationService
    def springSecurityService
    def pdfRenderingService

    def sendFlow = {
        entry {
            action { FilterInvoiceCommand cmd ->
                log.info("Flow action entry")

                flow.returnUrl = params.returnUrl
                log.debug("Return url: ${flow.returnUrl}")

                flow.invoicesToSend = []
                flow.customersWithoutEmail = [] as Set
                invoiceService.selectedInvoices(cmd, params).each { Invoice inv ->
                    if (inv.customer.hasInvoiceEmail()) {
                        flow.invoicesToSend << inv
                    } else {
                        flow.customersWithoutEmail << inv.customer
                    }
                }
                flow.customers = flow.invoicesToSend.customer.unique()

                flow.user = springSecurityService.getCurrentUser()
                flow.facility = flow.user.facility
            }
            on("success").to "confirm"
            on("error").to "finish"
        }
        confirm {
            log.info("Flow view confirm")
            on("submit") {
                flow.emailMessage = params.emailMessage
                flow.fromMail = params.fromMail

                if (!flow.fromMail) {
                    flash.error = message(code: "facilityInvoice.send.confirm.emailFrom.blank")
                    return error()
                }
                if (!EmailValidator.getInstance().isValid(flow.fromMail)) {
                    flash.error = message(code: "facilityInvoice.send.confirm.emailFrom.invalid")
                    return error()
                }
                if (!flow.emailMessage) {
                    flash.error = message(code: "facilityInvoice.send.confirm.message.blank")
                    return error()
                }
                if (flow.emailMessage.length() > NotificationService.MAX_INPUT_SIZE) {
                    flash.error = message(code: "facilityInvoice.send.confirm.message.overflow")
                    return error()
                }
            }.to "sendInvoices"
            on("cancel").to "finish"
        }
        sendInvoices {
            action {
                log.info("Flow action sendInvoices")

                def template = invoiceService.getPrintTemplate(flow.facility)

                flow.invoicesToSend.each { Invoice inv ->
                    def pdf = pdfRenderingService.render(template: "${InvoiceService.PRINT_TEMPLATE_ROOT_URI}/${template}",
                            model: [facility: flow.facility, invoices: [inv],
                                    accounts: prepareAccountSummary([inv])])
                    notificationService.sendCustomerInvoice(inv.customer, flow.emailMessage,
                            pdf.toByteArray(), flow.fromMail)
                    invoiceService.markAsSentByEmail(inv)
                }

                flow.returnUrl += addParam(flow.returnUrl, "message",
                        message(code: "facilityInvoice.send.success", args: [flow.invoicesToSend.size()]))
            }
            on("success").to "finish"
            on(Exception).to "finish"
        }
        finish {
            redirect(url: flow.returnUrl)
        }
    }
}