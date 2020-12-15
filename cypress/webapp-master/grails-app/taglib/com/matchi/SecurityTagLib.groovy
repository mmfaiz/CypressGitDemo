package com.matchi

import grails.plugin.springsecurity.SpringSecurityUtils

class SecurityTagLib {

    static returnObjectForTags = ["defaultFacilityController", "hasFacilityFullRights", "hasAccessToFacility"]

    def securityService
    def springSecurityService
    def userService

    def defaultFacilityController = { attr, body ->
        if (securityService.hasFacilityFullRights()) {
            return "facilityBooking"
        } else {
            def rights = securityService.listFacilityAccessRights()
            def controllers = FacilityUserRole.AccessRight.list().find { rights.contains(it) }
            if (controllers) {
                return controllers.controllers[0]
            }
            return null
        }
    }

    def ifFacilityAccessible = { attrs, body ->
        if (securityService.hasFacilityAccess()) {
            out << body()
        }
    }

    def ifFacilityAccessGranted = { attrs, body ->
        def accessRights = attrs.accessRights
        if (!accessRights) {
            throwTagError("Tag [ifFacilityAccessGranted] is missing required attribute [accessRights]")
        }

        if (securityService.hasFacilityFullRights()
                || (SpringSecurityUtils.ifAnyGranted("ROLE_USER")
                && securityService.hasFacilityAccessRights(accessRights instanceof List ? accessRights : [accessRights]))) {
            out << body()
        }
    }

    def ifFacilityFullRightsGranted = { attrs, body ->
        if (securityService.hasFacilityFullRights(Boolean.valueOf(attrs.ignoreAdminRole))) {
            out << body()
        }
    }

    def hasFacilityFullRights = { attrs, body ->
        springSecurityService.isLoggedIn() &&
                securityService.hasFacilityFullRights(Boolean.valueOf(attrs.ignoreAdminRole))
    }

    def hasAccessToFacility = { attrs, body ->
        springSecurityService.isLoggedIn() &&
                securityService.hasFacilityAccessTo(attrs.facility)
    }

    def canSendDirectMessage = { attrs, body ->
        if (userService.canSendDirectMessage(userService.getLoggedInUser(), attrs.to)) {
            out << body()
        }
    }
}
