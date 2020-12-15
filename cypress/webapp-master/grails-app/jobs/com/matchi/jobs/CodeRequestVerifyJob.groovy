package com.matchi.jobs

import com.matchi.mpc.CodeRequest
import org.hibernate.criterion.CriteriaSpecification
import org.springframework.util.StopWatch

class CodeRequestVerifyJob {

    def mpcService

    static triggers = {
        //simple repeatInterval: 30000l // execute job once in 30 seconds
        cron name: "CodeRequestVerifyJob.trigger", cronExpression: "0 0/10 * * * ?" // every 10 min
    }

    def concurrent = false
    def group = "CodeRequestVerifyJob"

    def execute() {
        StopWatch stopWatch = new StopWatch()
        stopWatch.start()

        log.info("Running CodeRequestVerifyJob")

        // Only verify code requests within a specific interval
        def nDaysForward = CodeRequest.getUpdateFutureLimit()

        def bookingIds = CodeRequest.createCriteria().list(max: 5000) {
            projections {
                distinct("b.id")
            }

            createAlias("booking", "b")
            createAlias("b.slot", "s")

            eq("status", CodeRequest.Status.UNVERIFIED)
            le("s.startTime", nDaysForward)
            gt("s.endTime", new Date())

            order("s.startTime", "asc")
        }

        if (bookingIds) {
            stopWatch.stop()
            log.info("Sending ${bookingIds?.size()} unverified CodeRequests to MPC. (Database select took ${stopWatch.totalTimeMillis} ms)")

            stopWatch.start()
            mpcService.verifyMultiple(bookingIds)
        }

        stopWatch.stop()
        log.info("Completed CodeRequestVerifyJob in " + stopWatch.totalTimeMillis + " ms")
    }
}
