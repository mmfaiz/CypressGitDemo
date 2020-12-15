package com.matchi.jobs

import com.matchi.ActivityService
import com.matchi.activities.ActivityOccasion
import org.joda.time.DateTime
import org.springframework.util.StopWatch

class ActivityCancellationByTooFewParticipantsJob {

    static triggers = {
        simple repeatInterval: 300000l // execute job every 5 minutes
    }

    def concurrent = false
    def group = "ActivityCancellationByTooFewParticipantsJob"
    def sessionRequired = true

    ActivityService activityService

    def execute() {
        log.info("Running ActivityCancellationByTooFewParticipantsJob")
        StopWatch stopWatch = new StopWatch()
        stopWatch.start()

        DateTime now = new DateTime()
        List<ActivityOccasion> activityOccasions = activityService.getOccasionsToCancelByTooFewParticipants(now)

        log.info "Automatic cancellation of ${activityOccasions?.size()} occasions on ${now}"

        activityOccasions.each { ActivityOccasion activityOccasion ->
            try {
                activityService.cancelOccasionWithFullRefundAutomatically(activityOccasion, ActivityOccasion.DELETE_REASON.BY_JOB)
            } catch (Throwable t) {
                log.error "Error on automatically cancelling ActivityOccasion $activityOccasion.id", t
            }
        }

        stopWatch.stop()
        log.info("Finished ActivityCancellationByTooFewParticipantsJob in ${stopWatch.totalTimeMillis} ms")
    }

}
