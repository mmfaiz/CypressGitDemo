package com.matchi.jobs

import com.matchi.Facility
import com.matchi.enums.RedeemAt
import org.hibernate.criterion.CriteriaSpecification
import org.springframework.util.StopWatch

class SubscriptionRedeemJob {
    def redeemService

    static triggers = {
        //cronExpression: "Seconds Minutes Hours Day-of-month Month Day-of-week Year (Optional)"
        //cron name: 'SubscriptionRedeemJob.trigger', cronExpression: "0 0/1 * * * ?" // Every minute
        cron name: 'SubscriptionRedeemJob.trigger', cronExpression: "30 52 0 * * ?" // 00:52:30 am
    }

    def concurrent = false
    def group = "SubscriptionRedeemJob"
    def sessionRequired = true

    def execute() {
        log.info("Running Subscription redeem job")

        StopWatch stopWatch = new StopWatch()
        stopWatch.start()

        def facilities = Facility.createCriteria().listDistinct {
            createAlias("subscriptionRedeem", "s", CriteriaSpecification.LEFT_JOIN)

            isNotNull("s.id")
            eq("s.redeemAt", RedeemAt.SLOTREBOOKED)
        }
        log.info("Redeem for ${facilities.size()} facilities")

        if (facilities.size() > 0) {
            redeemService.redeemUnredeemedCancelations(facilities)
        }

        stopWatch.stop()
        log.info("Finished SubscriptionRedeemJob in ${stopWatch.totalTimeMillis} ms")
    }

}
