package com.matchi

import com.matchi.activities.trainingplanner.Trainer
import com.matchi.activities.trainingplanner.TrainerService
import com.matchi.api.Code
import com.matchi.api.GenericAPIController
import com.matchi.requests.Request
import com.matchi.requests.TrainerRequest
import org.apache.http.HttpStatus
import org.joda.time.DateTime
import org.joda.time.LocalTime

class UserTrainerController extends GenericAPIController {

    TrainerService trainerService
    NotificationService notificationService

    static allowedMethods = [ remove: "DELETE" ]

    def index() {
        User user = getCurrentUser()
        [ trainers: trainerService.getUserBookableTrainers(user) ]
    }

    def update() {
        Trainer trainer = Trainer.get(params.long("id"))
        User user = getCurrentUser()

        if (!trainer) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'trainer.label', default: 'Trainer')])
            redirect(action: "index")
            return
        }

        if(trainer?.hasAvailability()) {
            int nrOfAvailabilities = (int) params.count { String key, value -> key.contains("weekDay_") }
            boolean validStartEndError = false
            if (nrOfAvailabilities > 0) {
                (1..(nrOfAvailabilities + 20)).each { // Add 10 to iterations just to make sure we don't miss any
                    if(validStartEndError) return

                    if (params.get("startDate_" + it) || params.get("endDate_" + it)) {

                        String startDateString = params.get("startDate_" + it)
                        String endDateString = params.get("endDate_" + it)

                        if(!(startDateString && endDateString) && (startDateString || endDateString)) {
                            validStartEndError = true
                        }
                    }
                }
            }

            if(validStartEndError) {
                flash.error = message(code: 'trainer.availability.validDatesBothRequired')
                render(view: "index", model: [trainers: trainerService.getUserBookableTrainers(user)])
                return
            }
        }

        if (trainer?.hasAvailability()) {
            trainer.availabilities.clear()

            int nrOfAvailabilities = (int) params.count { String key, value -> key.contains("weekDay_") }
            if (nrOfAvailabilities > 0) {
                (1..(nrOfAvailabilities + 20)).each { Integer index -> // Add 10 to iterations just to make sure we don't miss any
                    if (params.get("fromTime_" + index)) {
                        Availability a = Availability.buildFromParams(params, index)
                        trainer.addToAvailabilities(a)
                    }
                }
            }
        }

        if (trainer.save(flush: true)) {
            flash.message = message(code: 'default.updated.message', args: [message(code: 'trainer.label', default: 'Trainer'), trainer])
        }

        redirect(action: "index")
    }

    def requests() {
        User user = getCurrentUser()
        [ requestMap: trainerService.getUserBookableTrainerRequests(user)?.groupBy { it.status } ]
    }

    def accept() {
        TrainerRequest request = TrainerRequest.get(params.long("id"))
        request.status = Request.Status.ACCEPTED
        request.save()

        notificationService.sendTrainerRequestNotificationToRequester(request)

        flash.message = message(code: "request.accept.flash.message", args: [request.requester])
        redirect(action: "requests")
    }

    def deny() {
        TrainerRequest request = TrainerRequest.get(params.long("id"))
        request.status = Request.Status.DENIED
        request.save()

        notificationService.sendTrainerRequestNotificationToRequester(request)

        flash.message = message(code: "request.denied.flash.message", args: [request.requester])
        redirect(action: "requests")
    }

    def remove() {
        def request = TrainerRequest.get(params.id)

        log.debug("Removing ${request?.id} TrainerRequest")

        if (!request) {
            error(400, Code.INPUT_ERROR, "Invalid input")
            return
        }

        notificationService.sendDeletedTrainerRequestNotification(request)

        request?.delete()
        render status: HttpStatus.SC_OK
    }
}
