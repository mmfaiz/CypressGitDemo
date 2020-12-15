package com.matchi.jobs

import com.matchi.ActivityService
import com.matchi.Facility
import com.matchi.idrottonline.ActivityOccasionOccurence
import com.matchi.idrottonline.IdrottOnlineService
import grails.util.Environment
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.joda.time.LocalDate
import org.springframework.util.StopWatch

class IdrottOnlineActivitiesSyncJob {

    IdrottOnlineService idrottOnlineService
    ActivityService activityService
    GrailsApplication grailsApplication

    static triggers = {
        //cronExpression: "Seconds Minutes Hours Day-of-month Month Day-of-week Year (Optional)"
        cron name: 'IdrottOnlineActivitiesSyncJob.trigger', cronExpression: "30 2 1 * * ?" // 01:02:30 am
    }

    def concurrent = false
    def group = "IdrottOnlineActivitiesSyncJob"
    def sessionRequired = true

    def execute() {
        if (!grailsApplication.config.matchi.io.syncEnabled) {
            log.info("IdrottOnline sync jobs disabled by config")
            return
        }

        log.info("Running IdrottOnline activities sync job")

        StopWatch stopWatch = new StopWatch()
        stopWatch.start()

        LocalDate today = new LocalDate()
        ArrayList<Facility> facilities = Facility.findAllByActive(true).findAll() { it.hasIdrottOnlineActivitySync() }

        ArrayList<Integer> testFacilities = grailsApplication.config.matchi.io.testFacilities

        if ((Environment.current == Environment.DEVELOPMENT || Environment.current == Environment.TEST) && testFacilities[0] != -1) {
            facilities = facilities.findAll { testFacilities.contains( it.id.intValue() ) }
        }

        facilities.each { facility ->
            log.info("Facility: " + facility.name)

            StopWatch facilityStopWatch = new StopWatch()
            facilityStopWatch.start()

            List<ActivityOccasionOccurence> activityOccasions = activityService.findCourseOccasionByRange(facility, today, today)
            log.info("Retrieved ${activityOccasions.size()} activity occasions for ${today.toDate()}")

            idrottOnlineService.importActivityOccasions(facility, activityOccasions, true)

            facilityStopWatch.stop()
            log.info("Imported activities for ${facility.name} in ${facilityStopWatch.totalTimeMillis} ms")
        }

        stopWatch.stop()
        log.info("Finished IdrottOnlineActivitiesSyncJob in ${stopWatch.totalTimeMillis} ms")

    }

}
