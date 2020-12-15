package com.matchi.api.v2

import com.matchi.Facility
import com.matchi.User
import com.matchi.UserFavorite
import com.matchi.UserFavoriteCommand
import com.matchi.api.Code
import com.matchi.api.InputErrorAPIException
import grails.converters.JSON
import grails.validation.Validateable

class UserResourceController extends com.matchi.api.UserResourceController {

    static namespace = "v2"

    static allowedMethods = [register: ['POST']]

    Object renderJson(Object o) {
        JSON.use('apiV2', {
            render o as JSON
        })
    }

    def addFavorite() {
        def cmd = new UserFavoriteCommand(request?.JSON)
        if(!cmd.validate()) {
            throw new InputErrorAPIException(cmd.errors)
        }

        Facility facility = Facility.get(cmd.facilityId)
        if (!facility) {
            error(404, Code.RESOURCE_NOT_FOUND, "Could not find facility $cmd.facilityId")
            return
        }

        UserFavorite favorite = userService.addFavorite(getCurrentUser(), facility)
        if (favorite) {
            response.status = 201
            renderJson(favorite)
        } else {
            error(400, Code.INPUT_ERROR, "Unable to add favorite facility")
        }
    }

    def removeFavorite() {
        def cmd = new UserFavoriteCommand(request?.JSON)
        if(!cmd.validate()) {
            throw new InputErrorAPIException(cmd.errors)
        }

        Facility facility = Facility.get(cmd.facilityId)
        if (!facility) {
            error(404, Code.RESOURCE_NOT_FOUND, "Could not find facility $cmd.facilityId")
            return
        }

        if (userService.removeFavorite(getCurrentUser(), facility)) {
            render status: 204
        } else {
            error(404, Code.RESOURCE_NOT_FOUND, "Could not find facility $cmd.facilityId in favorites list")
        }
    }
}

@Validateable(nullable = true)
class RegisterUserCommand extends com.matchi.api.RegisterUserCommand {
    String language

    static constraints = {
        importFrom com.matchi.api.RegisterUserCommand
        language nullable: true
        city nullable: true, blank: true
    }

    User toUser() {
        User user = super.toUser()

        if(language) {
            user.language = language
        }

        return user
    }
}