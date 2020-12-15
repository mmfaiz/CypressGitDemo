package com.matchi.facility

import com.matchi.GenericController
import com.matchi.activities.trainingplanner.CourseActivity
import grails.validation.Validateable

class FacilityCourseFlowController extends GenericController {

    static scope = "prototype"

    def activityService

    def copyFlow = {
        copyCourse {
            on("submit") {
                // TODO: dates are not bindable for some reason, use manual binding
                flow.cmd = new CopyCourseCommand()
                bindData(flow.cmd, params)
                if (flow.cmd.validate()) {
                    def course = CourseActivity.get(flow.cmd.srcCourseId)
                    assertFacilityAccessTo(course)

                    if (flow.cmd.copyTrainers) {
                        flow.trainers = course.trainers.collect { it.toString() }.sort().join(", ")
                    } else {
                        flow.trainers = null
                    }
                    if (flow.cmd.copySettings) {
                        flow.formSettings = [maxSubmissions: course.form.maxSubmissions,
                                membershipRequired: course.form.membershipRequired,
                                paymentRequired: course.form.paymentRequired,
                                price: course.form.price]
                    } else {
                        flow.formSettings = null
                    }

                    flow.persistenceContext.clear()
                    return success()
                } else {
                    return error()
                }
            }.to "confirmCourse"
            on("cancel").to "finish"
        }
        confirmCourse {
            on("back").to "copyCourse"
            on("submit").to "doCopy"
            on("cancel").to "finish"
        }
        doCopy {
            action {
                activityService.copyCourse(flow.cmd)
            }
            on("success").to "finish"
            on(Exception).to "finish"
        }
        finish {
            redirect(controller: "facilityCourse", action: "index")
        }
    }
}

@Validateable(nullable = true)
class CopyCourseCommand implements Serializable {

    Long srcCourseId
    String name
    Date startDate
    Date endDate
    Date activeFrom
    Date activeTo
    Boolean copyTrainers
    Boolean copySettings
    Boolean copyParticipants
    Boolean copyWaitingSubmissions
    Boolean copyOccasions

    static constraints = {
        srcCourseId nullable: false
        name nullable: false, blank: false, maxSize: 255, matches: "[0-9\\p{L}\\s\\-+_:.,!'´/()]+"
        startDate nullable: false
        endDate nullable: false, validator: { val, obj ->
            if (val && obj.startDate) {
                return val.after(obj.startDate) ?: 'course.endDate.validation.error'
            }
        }
        activeFrom nullable: false
        activeTo nullable: false, validator: { val, obj ->
            if (val && obj.activeFrom) {
                return val.after(obj.activeFrom) ?: 'form.activeTo.validation.error'
            }
        }
        copyTrainers nullable: true
        copySettings nullable: true
        copyParticipants nullable: true
        copyWaitingSubmissions nullable: true
        copyOccasions nullable: true
    }
}