package com.matchi.async

import com.matchi.Facility
import org.apache.commons.lang.StringUtils
import org.apache.commons.lang.time.StopWatch

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * @author Michael Astreiko
 */
class ScheduledTaskService {
    static transactional = false

    /**
     *
     * @param name
     * @param domainIdentifier
     * @param facility
     * @param codeToExecute should not contain references to current user like facilityService.activeFacility
     */
    void scheduleTask(String name, Long domainIdentifier, Facility facility,
            Closure codeToExecute = {}) {
        scheduleTask(name, domainIdentifier, facility, null, codeToExecute)
    }

    void scheduleTask(String name, Long domainIdentifier, Facility facility, String successMessage,
            Closure codeToExecute = {}) {
        scheduleTask(name, domainIdentifier, facility, successMessage, null, codeToExecute)
    }

    void scheduleTask(String name, Long domainIdentifier, Facility facility, String successMessage,
                      Class domainClass, Closure codeToExecute = {}) {
        scheduleTask(name, domainIdentifier, facility, successMessage, domainClass, null, codeToExecute)

    }

    void scheduleTask(String name, Long domainIdentifier, Facility facility, String successMessage,
            Class domainClass, String identifier, Closure codeToExecute = {}) {
        ScheduledTask task = new ScheduledTask()
        task.name = name
        task.identifier = identifier
        task.domainIdentifier = domainIdentifier
        task.relatedDomainClass = domainClass?.simpleName
        task.facility = facility
        task.successMessage = successMessage
        task.validate()
        task.save(flush: true, failOnError: true)

        ExecutorService service = Executors.newFixedThreadPool(1)

        try {
            service.execute({
                ScheduledTask.withNewSession {
                    StopWatch stopWatch = new StopWatch()
                    stopWatch.start()
                    String error = null
                    try {
                        codeToExecute(task.id)
                    } catch (ex) {
                        log.error "Error occurred during ScheduledTask execution: ${ex.message}", ex
                        error = StringUtils.abbreviate(ex.message, 255)
                    }

                    task = ScheduledTask.get(task.id)
                    task.errorString = error
                    task.isTaskFinished = true
                    task.save(flush: true)
                    stopWatch.stop()
                    log.info "Task for ${task.name}: ${task.domainIdentifier} is done in ${stopWatch.toString()}"
                }
            })
        } finally {
            service?.shutdown()
        }
    }

    void markTaskAsFinished(ScheduledTask task) {
        task.isTaskFinished = true
        task.save()
    }

    List<ScheduledTask> getUnfinishedTasks() {
        List<ScheduledTask> unfinishedTasks = ScheduledTask.withCriteria {
            eq("isTaskFinished", false)
        }

        return unfinishedTasks
    }

}
