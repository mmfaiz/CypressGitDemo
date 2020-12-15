package com.matchi.async
import com.matchi.Facility
import org.codehaus.groovy.grails.plugins.web.taglib.ValidationTagLib

import java.math.RoundingMode
/**
 * @author Michael Astreiko
 */
class ScheduledTask {
    Facility facility

    Date dateCreated
    String name
    String identifier

    String relatedDomainClass
    Long domainIdentifier

    Boolean isTaskFinished = false
    Boolean isTaskReportRead = false
    String errorString

    String resultFileName
    String resultFilePath
    String successMessage

    static constraints = {
        identifier(nullable: true)
        errorString(nullable: true)
        resultFileName(nullable: true, maxSize: 100)
        resultFilePath(nullable: true, maxSize: 255)
        successMessage(nullable: true, maxSize: 255)
        facility(nullable: true)
        relatedDomainClass(nullable: true)
    }

    @Override
    public String toString() {
        BigDecimal minAgo = new BigDecimal((new Date().time - dateCreated.time) / 1000).setScale(0, RoundingMode.HALF_DOWN)

        def g = new ValidationTagLib()
        g.message(code: "scheduledTask.toString", args: [name, minAgo])
    }
}
