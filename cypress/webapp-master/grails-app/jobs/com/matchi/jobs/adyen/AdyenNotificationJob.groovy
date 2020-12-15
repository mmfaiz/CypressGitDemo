package com.matchi.jobs.adyen

import com.matchi.events.AdyenEventInitiator
import org.springframework.util.StopWatch

class AdyenNotificationJob {
    def adyenService
    def sessionFactory

    static triggers = {
        //simple repeatInterval: 30000l // execute job once in 30 seconds
        simple repeatInterval: 60000l // execute job every minute
    }

    def concurrent = false
    def group = "AdyenNotificationJob"
    def sessionRequired = true

    def execute() {
        log.info("Running Adyen notification job")
        StopWatch stopWatch = new StopWatch()
        stopWatch.start()

        adyenService.processNotificationsAsJob(new AdyenEventInitiator())

        stopWatch.stop()
        log.info("Finished AdyenNotificationJob in ${stopWatch.totalTimeMillis} ms")
    }
}
