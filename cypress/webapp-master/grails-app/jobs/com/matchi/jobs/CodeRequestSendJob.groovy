package com.matchi.jobs

import com.matchi.mpc.CodeRequest
import org.hibernate.criterion.CriteriaSpecification
import org.springframework.util.StopWatch

class CodeRequestSendJob {

    def mpcService

    static triggers = {
        // simple repeatInterval: 30000l // execute job once in 30 seconds
        cron name: "CodeRequestSendJob.trigger", cronExpression: "40 0/5 * * * ?" // every 5 min (at 40 seconds of a minute)
    }

    def concurrent = false
    def group = "CodeRequestSendJob"

    def execute() {
        log.info("Running CodeRequestSendJob")

        StopWatch stopWatch = new StopWatch()
        stopWatch.start()

        def pendingCodeRequestIds = CodeRequest.createCriteria().list(max: 100) {
            projections {
                distinct("id")
            }
            createAlias("booking", "b", CriteriaSpecification.LEFT_JOIN)
            createAlias("b.slot", "s", CriteriaSpecification.LEFT_JOIN)
            eq("status", CodeRequest.Status.PENDING)
            gt("s.endTime", new Date())
            order("s.startTime", "asc")
        }

        log.info("Sending ${pendingCodeRequestIds?.size()} pending CodeRequests out of ${pendingCodeRequestIds?.getTotalCount()}")

        pendingCodeRequestIds.each {
            mpcService.addPending(it)
        }

        stopWatch.stop()
        log.info("Finished CodeRequestSendJob in ${stopWatch.totalTimeMillis} ms")
    }
}
