package com.matchi.jobs.adyen

import com.matchi.adyen.AdyenException
import com.matchi.orders.AdyenOrderPayment
import com.matchi.orders.OrderPayment
import org.springframework.util.StopWatch

class AdyenCaptureJob {

    def adyenService

    static triggers = {
        //simple repeatInterval: 60000l // execute job once in 30 seconds
        cron name: "AdyenCaptureJob.trigger", cronExpression: "30 22 * * * ?" // every hour at 22 min 30 sec
    }

    def concurrent = false
    def group = "AdyenCaptureJob"
    def sessionRequired = true

    def execute() {
        log.info("Running capture adyen payments job")
        StopWatch stopWatch = new StopWatch()
        stopWatch.start()

        List<AdyenOrderPayment> payments = adyenService.getPaymentsToBeCaptured()

        log.info("Found ${payments.size()} to capture")
        payments.each { AdyenOrderPayment p ->
            try {
                p.capture()
            } catch (Exception e) {
                if (e instanceof AdyenException) {
                    p.status = OrderPayment.Status.FAILED
                    p.errorMessage = e.message
                    p.transactionId = e.pspReference
                    p.save()

                } else {
                    log.error("Error!!! Failed to process order payment:${p.id}", e)
                }
            }
        }

        stopWatch.stop()
        log.info("Finished AdyenCaptureJob in ${stopWatch.totalTimeMillis} ms")
    }
}
