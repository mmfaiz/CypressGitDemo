package com.matchi.jobs

import com.matchi.watch.ObjectWatch
import org.springframework.util.StopWatch

class ObjectWatchCleanUpJob {
    static triggers = {
        //simple repeatInterval: 30000l // execute job once in 30 seconds
        cron name: "ObjectWatchCleanUpJob.trigger", cronExpression: "30 41 * * * ?" // every hour at 41 min 30 sec
    }

    def concurrent = false
    def group = "ObjectWatchCleanUpJob"
    def sessionRequired = true

    def execute() {
        log.info("Running ObjectWatchCleanUpJob")

        StopWatch stopWatch = new StopWatch()
        stopWatch.start()

        def watches = ObjectWatch.createCriteria().list {
            lte("toDate", new Date())
        }

        log.info("Found ${watches.size()} records to remove")

        watches.each {
            it.delete()
        }

        stopWatch.stop()
        log.info("Finished ObjectWatchCleanUpJob in ${stopWatch.totalTimeMillis} ms")

    }
}
