package com.matchi.facility

import com.matchi.GenericController
import com.matchi.invoice.Invoice
import grails.validation.Validateable
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

class FacilityInvoicePaymentController extends GenericController {

    static scope = "prototype"

    def s3FileService
    def springSecurityService

    def index() {
        redirect(action: "makePayments")
    }

    def makePaymentsFlow = {
        entry {
            action {
                flow.facility = springSecurityService.getCurrentUser()?.facility
            }
            on("success").to("upload")
        }
        upload {
            log.info("Flow view upload")
            on("cancel").to "cancel"
            on("submit").to "getRecords"
        }
        getRecords {
            action {
                log.debug("Flow action getRecords")
                HashMap paymentRecords
                try {
                    def fileName = s3FileService.uploadTemporaryFile(request).name
                    paymentRecords = flow.facility.getGiroPaymentRecords(s3FileService.downloadTemporaryFile(fileName))
                    flow.error = ""
                } catch (Exception e) {
                    log.error(e)
                    flow.error = message(code: "facilityInvoicePayment.makePayments.error")
                    return error()
                }

                flow.invoicePaymentsInfo = PaymentInfoCommand.create(paymentRecords, flow.facility)

                flow.persistenceContext.clear()
                success()
            }
            on("success").to "confirm"
            on("return").to "upload"
            on("error").to "upload"
        }
        confirm {
            log.info("Flow view confirm and create customers")
            on("cancel").to "cancel"
            on("back").to "upload"
            on("submit").to "registerPayments"
        }
        registerPayments {
            action {
                log.debug("Flow action register payments")

                flow.successful = 0
                flow.errors = 0

                flow.invoicePaymentsInfo.success.each {
                    def invoice = Invoice.get(it.invoice.id)
                    def payment = invoice.addPayment(it.date, it.amount)
                    invoice.save(flush: true)

                    if (payment) flow.successful++
                    else errors++
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
            redirect(controller: "facilityInvoice", action: "index")
        }
    }
}

@Validateable(nullable = true)
class PaymentInfoCommand implements Serializable {
    private static final Log log = LogFactory.getLog(PaymentInfoCommand)

    BigDecimal total = 0
    List<Object> success = []
    List<Object> error = []

    static PaymentInfoCommand create(def paymentRecords, def facility) {
        PaymentInfoCommand cmd = new PaymentInfoCommand()

        log.error(facility)

        paymentRecords.each { info ->
            def invoice = Invoice.where {
                number == Invoice.ocrToNumber(info.key)
                customer.facility == facility
            }.find()

            if(invoice)
                log.error(invoice.id + " <--------> " + invoice.number + " <----> ")

            if (!invoice) {
                cmd.error << [ ocr: info.key, invoice: null, customer: null,
                                  date: info.value.date, amount: info.value.amount ]
            } else {
                cmd.success << [ ocr: info.key, invoice: invoice, customer: invoice.customer.fullName(),
                                  date: info.value.date, amount: info.value.amount ]
                cmd.total += info.value.amount
            }
        }

        return cmd
    }

    static constraints = {
        total nullable: false, min: 1.0
        success nullable: false
        error nullable: false
    }
}
