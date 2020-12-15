package com.matchi.jobs


import grails.transaction.Transactional
import org.hibernate.Session
import org.hibernate.SessionFactory
import org.joda.time.LocalDate
import org.springframework.transaction.annotation.Propagation
import org.springframework.util.StopWatch

class PurgeMailMessagesJob {
    private static final int BATCH_SIZE = 2000
    private static final int MAX_ITERATIONS = 25
    private static final int RETENTION_DAYS = 7

    static triggers = {
        cron name: "PurgeMailMessagesJob.trigger", cronExpression: "30 32 2 ? * *" // 02:32:30 am
    }

    def concurrent = false
    def group = "PurgeMailMessagesJob"
    def sessionRequired = true

    SessionFactory sessionFactory

    def execute() {
        StopWatch stopWatch = new StopWatch()
        stopWatch.start()

        log.info("Purging old emails...")
        Date date = LocalDate.now().minusDays(RETENTION_DAYS).toDateTimeAtStartOfDay().toDate()

        int batch = this.purge(date)
        int total = batch
        int iterations = 1
        while (batch > 0 && iterations < MAX_ITERATIONS) {
            batch = this.purge(date)
            total += batch
            iterations++
        }

        stopWatch.stop()
        log.info("Purged " + total + " emails in " + stopWatch.totalTimeMillis + " ms.")
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private int purge(Date date) {
        Session session = sessionFactory.currentSession

        return session.createSQLQuery("delete from async_mail_mess where create_date <= :date limit :limit").with {
            setDate("date", date)
            setLong("limit", BATCH_SIZE)
        }.executeUpdate()

    }
}
