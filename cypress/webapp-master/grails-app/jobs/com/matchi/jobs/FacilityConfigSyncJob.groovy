package com.matchi.jobs

import org.springframework.util.StopWatch

/**
 * The purpose of this job is to push all facilities with select configuration
 * to the Asynchronous Integration Platform (AIP) via an Apache Kafka Topic so that
 * new or modified facilities are automatically reflected in AIP. That is necessary
 * because integrations will need facility specific properties. Based on the current
 * number of facilities (400) this job takes roughly a minute to execute.
 * The job can also be triggered manually via Black Admin.
 * @author Magnus Lundahl
 */
class FacilityConfigSyncJob {

    static triggers = {
        cron name: "FacilityConfigSyncJob.trigger", cronExpression: "0 35 20 ? * *" // Every day at 20:35:00
    }

    def concurrent = false
    def group = "FacilityConfigSyncJob"
    def facilityService

    def execute() {
        log.info("Synchronizing Facilities to Asynchronous Integration Platform...")

        StopWatch stopWatch = new StopWatch()
        stopWatch.start()

        def count = facilityService.syncFacilityConfigToIntegrationPlatform()

        stopWatch.stop()
        log.info("Synchronized " + count + " Facilities in " + stopWatch.totalTimeMillis + " ms.")
    }
}
