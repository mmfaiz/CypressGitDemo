package com.matchi.facility

import com.matchi.Facility
import com.matchi.GenericController
import com.matchi.activities.ClassActivity
import org.springframework.web.multipart.MultipartHttpServletRequest
import org.springframework.web.multipart.commons.CommonsMultipartFile

class FacilityActivityController extends GenericController {

    def activityService
    def fileArchiveService

    def index() {
        def facility = getUserFacility()
        [activities: activityService.getActivitiesByFacility(facility, false), facility: facility]
    }

    def archive() {
        def facility = getUserFacility()
        render(view: "index", model:
                [activities: activityService.getActivitiesByFacility(facility, true), facility: facility])
    }

    def create() {
        [facility: getUserFacility()]
    }

    def edit(Long id) {
        def activity = getClassActivity(id)
        if (activity) {
            buildModel(activity)
        }
    }

    def occasions() {
        def activity = ClassActivity.get(params.long("id"))
        if (!activity) {
            return response.sendError(404)
        }
        def occasions = activityService.getOccasionsByActivity(activity)

        def pastOccasions = activityService.getFinishedOccasions(occasions)
        def futureOccasions = activityService.getUpcomingOccasions(occasions)
        Facility facility = getUserFacility()

        render(view: "occasions", model: [activity: activity, pastOccasions: pastOccasions, futureOccasions: futureOccasions, facility: facility])
    }

    def save() {
        def classActivityInstance = new ClassActivity()
        Facility facility = getUserFacility()
        bindData(classActivityInstance, params)

        classActivityInstance.facility = facility

        classActivityInstance.levelMin = getLevels().first()
        classActivityInstance.levelMax = getLevels().last()

        if (classActivityInstance.save(flush: true)) {
            flash.message = message(code: 'default.created.message',
                    args: [message(code: 'course.label', default: 'Course'), classActivityInstance.name])
            redirect(action: "index")
        } else {
            render(view: "create", model: buildModel(classActivityInstance))
        }
    }

    def update(Long id, Long version) {
        Facility facility = getUserFacility()
        def classActivityInstance = ClassActivity.findByIdAndFacility(id, facility)

        if (!classActivityInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'course.label'), classActivityInstance?.name])
            redirect(action: "index")
            return
        }

        assertFacilityAccessTo(classActivityInstance)

        if (version != null) {
            if (classActivityInstance.version > version) {
                classActivityInstance.errors.rejectValue("version", "default.optimistic.locking.failure",
                        [message(code: 'course.label', default: 'Course')] as Object[],
                        "Another user has updated this Course while you were editing")
                render(view: "edit", model: [classActivityInstance: classActivityInstance])
                return
            }
        }


        List<Integer> levels = params.useLevel ? params.level.toString().split(",").collect { Integer.parseInt(it) } : [null, null]

        params.levelMin = levels.first()
        params.levelMax = levels.last()

        bindData(classActivityInstance, params)

        if (!classActivityInstance.save(flush: true)) {
            render(view: "edit", model: buildModel(classActivityInstance))
            return
        }

        flash.message = message(code: 'default.updated.message', args: [message(code: 'course.label'), classActivityInstance.name])

        redirect(action: "index")
    }

    def archiveActivity() {
        ClassActivity activity = ClassActivity.get(params.id)

        if (activity) {
            try {
                activityService.archive(activity)
            } catch (IllegalArgumentException e) {
                log.error(e)
                flash.error = message(code: "facilityActivity.archive.error")
                render(view: "edit", model: [activity: activity])
                return
            }
            flash.message = message(code: "facilityActivity.archive.success", args: [activity.name])
        }

        redirect(action: "index")
    }

    def delete() {
        ClassActivity activity = ClassActivity.get(params.id)

        if (activity) {
            // Not allowed to delete activity with pending/upcoming occasions.
            if (!activityService.isDeletable(activity)) {
                flash.error = message(code: "facilityActivity.delete.notAllowed")
                render(view: "edit", model: [activity: activity])
                return
            }

            try {
                activityService.delete(activity)
            } catch (IllegalArgumentException e) {
                log.error(e)
                flash.error = message(code: "facilityActivity.delete.error")
                render(view: "edit", model: [activity: activity])
                return
            }
            flash.message = message(code: "facilityActivity.delete.success", args: [activity.name])
        }

        redirect(action: "index")
    }

    def upload() {
        ClassActivity activity = ClassActivity.get(params.id)

        MultipartHttpServletRequest mpr = (MultipartHttpServletRequest) request;
        CommonsMultipartFile image = (CommonsMultipartFile) mpr.getFile(params.fileParameterName);

        if (image && image.size > 0) {
            def mfile = fileArchiveService.storeFile(image)

            // remove previous file
            if (activity.largeImage) {
                def imageToRemove = activity.largeImage
                activity.largeImage = null
                fileArchiveService.removeFile(imageToRemove)
                activity.save()
            }

            activity.largeImage = mfile
            activity.save()

            flash.message = message(code: "facilityActivity.upload.success")
        } else {
            flash.error = message(code: "facilityActivity.upload.error")
        }

        redirect(action: "edit", id: activity.id)
    }

    def removeImage() {
        ClassActivity activity = ClassActivity.get(params.long("id"))

        flash.message = message(code: "facilityActivity.removeImage.success")

        if (activity.largeImage) {
            def imageToRemove = activity.largeImage
            activity.largeImage = null
            fileArchiveService.removeFile(imageToRemove)
            activity.save()
        }

        redirect(action: "edit", id: activity.id)
    }

    private ClassActivity getClassActivity(id) {
        def classActivity = ClassActivity.findByIdAndFacility(id, getUserFacility())

        if (!classActivity) {
            flash.message = message(code: 'default.not.found.message',
                    args: [message(code: 'eventActivity.label')])
            redirect(action: "index")
        }

        classActivity
    }

    List<Integer> getLevels() {
        if (params.useLevel) {
            return params.level.split(",").collect { Integer.parseInt(it) }
        } else {
            return [null, null]
        }
    }

    private Map buildModel(classActivityInstance = new ClassActivity(params)) {
        [
            classActivityInstance: classActivityInstance
        ]
    }
}
