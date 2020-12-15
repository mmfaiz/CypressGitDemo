package com.matchi.jobs

import org.joda.time.LocalDate
import org.quartz.JobExecutionContext
import org.springframework.util.StopWatch

/**
 * @author Sergei Shushkevich
 */
class MembershipRenewJob {

    def memberService

    static triggers = {
        cron name: 'MembershipRenewJob.trigger', cronExpression: "30 32 1 * * ?" // 01:32:30 am
    }

    def concurrent = false
    def group = "MembershipRenewJob"
    def sessionRequired = true

    def execute(JobExecutionContext context) {
        def endDate = context.mergedJobDataMap.get("endDate")

        StopWatch stopWatch = new StopWatch()
        stopWatch.start()

        log.info("Running renew membership job ${endDate ? '(' + endDate + ')' : ''}")
        memberService.renewMemberships(endDate ? new LocalDate(endDate) : null)
        log.info("Renew membership job completed")

        stopWatch.stop()
        log.info("Finished MembershipRenewJob in ${stopWatch.totalTimeMillis} ms")
    }
}