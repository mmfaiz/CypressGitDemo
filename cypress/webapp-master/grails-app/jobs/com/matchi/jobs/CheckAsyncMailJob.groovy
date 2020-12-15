package com.matchi.jobs


import grails.plugin.asyncmail.AsynchronousMailMessage
import grails.plugin.asyncmail.MessageStatus
import org.springframework.util.StopWatch

class CheckAsyncMailJob {
    private static final int MAX = 5000

    static triggers = {
        simple repeatInterval: 3600000L // Execute job every hour
    }

    def concurrent = false
    def group = "CheckAsyncMailJob"

    def execute() {
        log.info("Running CheckAsyncMailJob")

        StopWatch stopWatch = new StopWatch()
        stopWatch.start()

        List<AsynchronousMailMessage> emails = AsynchronousMailMessage.createCriteria().list(max: MAX) {
            eq("status", MessageStatus.CREATED)
        }

        if (emails.size() >= MAX) {
            log.error("Too many emails in status CREATED, restart server!")
        }

        stopWatch.stop()
        log.info("Finished CheckAsyncMailJob in ${stopWatch.totalTimeMillis} ms")
    }
}
