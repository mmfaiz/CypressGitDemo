package com.matchi.jobs.adyen

import org.apache.commons.lang.time.StopWatch

/**
 * @author Sergei Shushkevich
 */
class RecurringPaymentRetryJob {

    static triggers = {
        cron name: 'RecurringPaymentRetryJob.trigger', cronExpression: "30 2 23 * * ?" // 11:02:30 pm
    }

    def concurrent = false
    def group = "RecurringPaymentRetryJob"
    def sessionRequired = true

    def memberService

    def execute() {
        def watch = new StopWatch()
        watch.start()
        log.info("Started RecurringPaymentRetryJob")

        memberService.retryFailedMembershipPayments()

        watch.stop()
        log.info("Finished RecurringPaymentRetryJob (time: $watch)")
    }
}