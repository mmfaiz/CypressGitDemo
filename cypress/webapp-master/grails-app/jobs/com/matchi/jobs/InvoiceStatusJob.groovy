package com.matchi.jobs

import com.matchi.invoice.Invoice
import org.joda.time.LocalDate
import org.springframework.util.StopWatch

/**
 * @author Michael Astreiko
 */
class InvoiceStatusJob {
    static triggers = {
        cron name: 'InvoiceStatusJob.trigger', cronExpression: "30 52 4 * * ?" // 04:52:30 am
    }

    def group = "InvoiceStatusJob"
    def sessionRequired = true

    /**
     * Update Invoices status any -> OVERDUE if necessary.
     */
    def execute() {
        log.info("Running Invoice Status Job")

        StopWatch stopWatch = new StopWatch()
        stopWatch.start()

        Invoice.createCriteria().listDistinct {
            eq('status', Invoice.InvoiceStatus.POSTED)
            lt('expirationDate', new LocalDate())
        }.each { Invoice invoiceToUpdate ->
            log.info("Changing status to Overdue for Invoice: ${invoiceToUpdate?.id}")
            invoiceToUpdate.status = Invoice.InvoiceStatus.OVERDUE
            invoiceToUpdate.save()
        }

        stopWatch.stop()
        log.info("Finished InvoiceStatusJob in ${stopWatch.totalTimeMillis} ms")
    }
}
