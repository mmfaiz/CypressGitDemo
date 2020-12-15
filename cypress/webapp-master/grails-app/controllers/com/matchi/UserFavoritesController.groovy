package com.matchi

import grails.converters.JSON
import grails.validation.Validateable

class UserFavoritesController extends GenericController {

    def userService

    def addFavourite(UserFavoriteCommand cmd) {
        if(cmd.hasErrors()) {
            flash.error = message(code: "userFavorite.validation.error")
        } else {
            Facility facility = Facility.get(cmd.facilityId)
            if (facility) {
                userService.addFavorite(currentUser, facility)
            } else {
                flash.error = message(code: "default.not.found.message",
                        args: [message(code: "default.facility.label")])
            }
        }

        if(params.returnUrl && params.returnUrl.size() > 0) {
            redirect url: params.returnUrl
        } else {
            redirect(controller: "book", action: "index")
        }
    }

    def removeFavourite(UserFavoriteCommand cmd) {
        if(cmd.hasErrors()) {
            flash.error = message(code: "userFavorite.validation.error")
        } else {
            Facility facility = Facility.get(cmd.facilityId)
            if (facility) {
                userService.removeFavorite(currentUser, facility)
            } else {
                flash.error = message(code: "default.not.found.message",
                        args: [message(code: "default.facility.label")])
            }
        }

        if(params.returnUrl && params.returnUrl.size() > 0) {
            redirect url: params.returnUrl
        } else {
            redirect(controller: "book", action: "index")
        }
    }
}

@Validateable
class UserFavoriteCommand {
    Long facilityId

    static constraints = {
        facilityId nullable: false
    }
}
