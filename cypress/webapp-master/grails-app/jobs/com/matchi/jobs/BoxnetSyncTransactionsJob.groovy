package com.matchi.jobs

import org.springframework.util.StopWatch

class BoxnetSyncTransactionsJob {

    def boxnetSyncService

    static triggers = {
        //cronExpression: "Seconds Minutes Hours Day-of-month Month Day-of-week Year (Optional)"
        //cron name: 'BoxnetSyncTransactionsJob.trigger', cronExpression: "0 0/1 * * * ?" // Every minute
        cron name: 'BoxnetSyncTransactionsJob.trigger', cronExpression: "30 2 4 * * ?" // 04:02:30 am
    }

    def concurrent = false
    def group = "BoxnetSyncTransactionsJob"
    def sessionRequired = true

    def execute() {
        log.info("Running Boxnet sync transactions job")
        StopWatch stopWatch = new StopWatch()
        stopWatch.start()

        boxnetSyncService.syncTransactions()

        stopWatch.stop()
        log.info("Finished BoxnetSyncTransactionsJob in ${stopWatch.totalTimeMillis} ms")
    }

}
