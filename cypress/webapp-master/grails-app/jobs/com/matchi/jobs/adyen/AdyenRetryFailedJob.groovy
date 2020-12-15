package com.matchi.jobs.adyen

class AdyenRetryFailedJob {

    def adyenService

    static triggers = {
        //simple repeatInterval: 30000l // execute job once in 30 seconds
        //simple repeatInterval: 60000l // execute job every minute
        //cron name: 'AdyenRetryFailedJob.trigger', cronExpression: "0 0 5 * * ?" // 05:00 am
    }

    def concurrent = false
    def group = "AdyenRetryFailedJob"
    def sessionRequired = true

    def execute() {
        /*log.debug("Running failed adyen payments job")
        List<AdyenOrderPayment> failedPayments = adyenService.getPaymentsToRetry()
        log.debug("Found ${failedPayments.size()} to retry")

        failedPayments.each { AdyenOrderPayment p ->
            //p.retry()
        }*/
    }
}
