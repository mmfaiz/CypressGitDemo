package com.matchi.api

import com.matchi.Facility
import com.matchi.User
import com.matchi.activities.ActivityOccasion
import com.matchi.activities.ClassActivity
import com.matchi.watch.ClassActivityWatch
import grails.converters.JSON
import grails.validation.Validateable
import org.grails.databinding.BindingFormat

/**
 * @author Sergei Shushkevich
 */
class ClassActivityWatchController extends GenericAPIController {

    def customerService
    def objectWatchNotificationService
    def userService

    def list() {
        def watches = ClassActivityWatch.withCriteria {
            eq("user", getCurrentUser())
            ge("fromDate", new Date())
            if (params.facility) {
                eq("facility", Facility.get(params.facility))
            }
            order("fromDate","asc")
        }

        render watches as JSON
    }

    def confirm(Long occasionId) {
        def occasion = ActivityOccasion.get(occasionId)
        if (!occasion) {
            error(404, Code.RESOURCE_NOT_FOUND, "Occasion not found")
            return
        }

        def user = userService.getLoggedInUser()
        if (!user) {
            error(401, Code.ACCESS_DENIED, "User not logged in")
            return
        }

        [occasion: occasion, user: user]
    }

    def add(AddActivityWatchCommand cmd) {
        User user = userService.getLoggedInUser()

        if (!user) {
            error(401, Code.ACCESS_DENIED, "User not logged in")
            return
        }

        if(!cmd.validate()) {
            error(400, Code.INPUT_ERROR, "Invalid input")
            return
        }

        def activity = ClassActivity.get(cmd.activityId)
        if (!activity) {
            error(400, Code.INPUT_ERROR, "Invalid input")
            return
        }

        log.debug("Adding activity $activity.id watch for user $user.id")

        customerService.getOrCreateUserCustomer(user, activity.facility)

        def watch = objectWatchNotificationService.addNotificationFor(
                user, activity, cmd.fromDateTime, cmd.smsNotify)

        if (watch) {
            render watch as JSON
        } else {
            error(400, Code.INPUT_ERROR, "Invalid input")
        }
    }

    def remove() {
        def watch = ClassActivityWatch.findByIdAndUser(params.id, getCurrentUser())

        log.debug("Removing ${watch?.id} ClassActivityWatch")

        if (!watch) {
            error(404, Code.RESOURCE_NOT_FOUND, "Invalid input")
            return
        }
        
        watch?.delete()
        render status: 204
    }
}

@Validateable
class AddActivityWatchCommand {

    Long activityId
    @BindingFormat("yyyy-MM-dd HH:mm")
    Date fromDateTime
    boolean smsNotify = false
}