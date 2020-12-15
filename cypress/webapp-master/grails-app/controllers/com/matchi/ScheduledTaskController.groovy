package com.matchi

import com.matchi.async.ScheduledTask
import grails.compiler.GrailsCompileStatic
import grails.converters.JSON
import grails.transaction.Transactional
import org.apache.commons.io.FilenameUtils
import org.joda.time.DateTime

import javax.servlet.http.HttpServletResponse
/**
 * @author Michael Astreiko
 */
class ScheduledTaskController {

    def facilityService
    def fileArchiveService
    def grailsMimeUtility

    def getCurrentlyRunningTasks() {
        Facility facility = facilityService.getActiveFacility()
        try {
            def tasks = []
            def dateRestriction = new DateTime().minusMinutes(30).toDate()

            ScheduledTask.createCriteria().list {
                eq('facility', facility)
                eq('isTaskReportRead', false)
                isNotNull('errorString')
                gt('dateCreated', dateRestriction)
            }?.each { ScheduledTask task ->
                tasks << [
                        text: message(code: "scheduledTask.getCurrentlyRunningTasks.error", args: [task.name]),
                        error: task.errorString,
                        closeLink: createLink(action: "markAsRead", id: task.id, absolute: true)
                ]
            }

            ScheduledTask.createCriteria().list {
                eq('facility', facility)
                eq('isTaskFinished', true)
                eq('isTaskReportRead', false)
                isNotNull('resultFilePath')
                gt('dateCreated', dateRestriction)
            }?.each { task ->
                tasks << [
                        file: true,
                        text: message(code: "scheduledTask.getCurrentlyRunningTasks.fileDownload",
                                args: [task.name, createLink(action: "download", id: task.id, absolute: true)])
                ]
            }

            ScheduledTask.createCriteria().list {
                eq('facility', facility)
                eq('isTaskFinished', true)
                eq('isTaskReportRead', false)
                isNull('resultFilePath')
                isNull('errorString')
                isNotNull('successMessage')
                gt('dateCreated', dateRestriction)
            }.each { task ->
                tasks << [
                        successMessage: true,
                        text: task.successMessage,
                        closeLink: createLink(action: "markAsRead", id: task.id, absolute: true)
                ]
            }

            ScheduledTask.createCriteria().list {
                eq('facility', facility)
                eq('isTaskFinished', false)
                gt('dateCreated', dateRestriction)
            }?.each { task ->
                tasks << [text: task.toString()]
            }

            def converter = tasks as JSON
            render text: converter.toString(), contentType: 'application/json'
        } catch (ex) {
            log.error "Error occurred during retrieving of currently running processes: $ex.message", ex
        }
    }

    def download(Long id) {
        def task = ScheduledTask.findByIdAndFacility(id, facilityService.getActiveFacility())
        if (task?.resultFilePath) {

            def file = fileArchiveService.downloadFile(task.resultFilePath)

            // TODO: Backward compatibiliy. Remove it later, when no local files will exist
            if (!file) {
                file = new File(task.resultFilePath)
            }

            if (file.exists()) {
                String extension = grailsMimeUtility.getMimeTypeForExtension(FilenameUtils.getExtension(task.resultFileName))
                writeResponse(extension, task.resultFileName, file.bytes)
                task.isTaskReportRead = true
                task.save(flush: true)

                file.delete()
            } else {
                response.sendError HttpServletResponse.SC_NOT_FOUND
            }
        } else {
            response.sendError HttpServletResponse.SC_NOT_FOUND
        }
    }

    @GrailsCompileStatic
    // This is needed to avoid the wrong javax.servlet classes to be inserted here (https://matchi.atlassian.net/browse/MW-4617)
    def writeResponse(String responseType, String fileName, byte[] bytes) {
        response.contentType = responseType
        response.setHeader("Content-disposition", "attachment; filename=${fileName}")
        response.outputStream << bytes
        response.outputStream.flush()
    }
    @Transactional
    def markAsRead(Long id) {
        def task = ScheduledTask.findByIdAndFacility(id, facilityService.getActiveFacility())
        if (task) {
            task.isTaskReportRead = true
            task.save(flush: true)
            render([:] as JSON)
        } else {
            response.sendError HttpServletResponse.SC_NOT_FOUND
        }
    }
}
