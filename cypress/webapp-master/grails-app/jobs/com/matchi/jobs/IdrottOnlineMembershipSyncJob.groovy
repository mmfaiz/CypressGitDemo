package com.matchi.jobs

import com.matchi.Customer
import com.matchi.Facility
import com.matchi.idrottonline.IdrottOnlineService
import org.apache.commons.lang.time.DateUtils
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.quartz.JobExecutionContext
import org.quartz.Scheduler
import org.springframework.util.StopWatch
import grails.util.Environment


class IdrottOnlineMembershipSyncJob {
    // If true all members will be sent, regardless if they have been updated recently or not.
    // With all members for all facilities the job will complete in under 5 minutes.
    private static final boolean ALL_MEMBERS = true

    IdrottOnlineService idrottOnlineService
    Scheduler quartzScheduler
    GrailsApplication grailsApplication

    static triggers = {
        cron name: 'IdrottOnlineMembershipSyncJob.trigger', cronExpression: "30 27 1 * * ?" // 01:27:30 am
    }

    def concurrent = false
    def group = "IdrottOnlineMembershipSyncJob"
    def sessionRequired = true

    void execute(JobExecutionContext context) {
        if (!grailsApplication.config.matchi.io.syncEnabled) {
            log.info("IdrottOnline sync jobs disabled by config")
            return
        }

        log.info("Running IdrottOnline members sync job")

        StopWatch stopWatch = new StopWatch()
        stopWatch.start()

        Date previousFireTime = getPreviousFireTime(context)

        ArrayList<Facility> facilities = Facility.findAllByActive(true).findAll() { it.hasIdrottOnlineMembershipSync() }

        ArrayList<Integer> testFacilities = grailsApplication.config.matchi.io.testFacilities

        if ((Environment.current == Environment.DEVELOPMENT || Environment.current == Environment.TEST) && testFacilities[0] != -1) {
            facilities = facilities.findAll { testFacilities.contains( it.id.intValue() ) }
        }
        facilities.each { facility ->
            log.info("Facility: " + facility.name)

            StopWatch facilityStopWatch = new StopWatch()
            facilityStopWatch.start()

            List<Customer> customers
            if (ALL_MEMBERS) {
                customers = idrottOnlineService.getCustomersToImport(facility)
            } else {
                customers = idrottOnlineService.getAllUpdatedCustomers(facility, previousFireTime)
            }

            if (customers.any()) {
                idrottOnlineService.importCustomers(facility, customers, true)
            }

            facilityStopWatch.stop()
            log.info("Imported customers for ${facility.name} in ${facilityStopWatch.totalTimeMillis} ms")

        }

        stopWatch.stop()
        log.info("Finished IdrottOnlineMembershipSyncJob in ${stopWatch.totalTimeMillis} ms")
    }

    private static Date getPreviousFireTime(JobExecutionContext context) {
        Date previousFireTime = context.trigger.getPreviousFireTime()

        // First time when the job never triggered before.
        if (!previousFireTime)
            previousFireTime = DateUtils.addDays(new Date(), -1)

        //
        // NOTE! Manually adjust firetime because trigger.getPreviousFireTime() returns currently executed and not last time as you would expect.
        // Need to find a way to get last fire/executed time better, in the mean time manually calculate previous fire time.
        previousFireTime = DateUtils.addDays(previousFireTime, -1)
        previousFireTime
    }

}
