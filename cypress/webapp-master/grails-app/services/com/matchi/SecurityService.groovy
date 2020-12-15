package com.matchi

import grails.transaction.NotTransactional
import grails.plugin.springsecurity.SpringSecurityUtils

class SecurityService {
    static transactional = true
    def grailsApplication
    def springSecurityService

    @NotTransactional
    def getLoginPostURL() {
        def securityConfig = SpringSecurityUtils.securityConfig
        return securityConfig.apf.filterProcessesUrl
    }

    Facility getUserFacility() {
        springSecurityService.isLoggedIn() ? User.findById(springSecurityService.principal.id,
                [cache: true, fetch: [facility: "join"]])?.facility : null
    }

    boolean hasFacilityAccess() {
        hasFacilityFullRights() ||
                (SpringSecurityUtils.ifAnyGranted("ROLE_USER") && getUserFacility())
    }

    boolean hasFacilityAccessRights(List accessRights) {
        FacilityUserRole.granted(
                User.findById(springSecurityService.principal.id, [cache: true, fetch: [facility: "join"]]),
                accessRights
        ).count()
    }

    boolean hasFacilityAccessTo(Facility facility) {
        if(!facility) {
            return false
        }

        if(SpringSecurityUtils.ifAnyGranted("ROLE_ADMIN")) {
            return true
        }

        if (!springSecurityService.principal) {
            return false
        }
        if (!springSecurityService.principal.hasProperty('id')) {
            log.info("Principal has no 'id' property")
            return false
        }
        User user
        Long id
        try {
            id = springSecurityService.principal.id
            user = User.findById(id)
            if(!user) {
                log.error("Can't find user by id: ${id}")
                return false
            }
        } catch (Exception e) {
            log.error("Error while find a user or get it from context: ${id}", e)
            return false
        }

        boolean userOnFacility = FacilityUser.findByUserAndFacility(user, facility) != null
        boolean role = SpringSecurityUtils.ifAnyGranted("ROLE_USER")

        return userOnFacility && role
    }

    boolean hasFacilityFullRights(Boolean ignoreAdminRole = false) {
        if (!ignoreAdminRole && SpringSecurityUtils.ifAnyGranted("ROLE_ADMIN")) {
            return true
        } else {
            return SpringSecurityUtils.ifAnyGranted("ROLE_USER") &&
                    hasFacilityAccessRights([FacilityUserRole.AccessRight.FACILITY_ADMIN])
        }
    }

    List listFacilityAccessRights() {
        FacilityUserRole.granted(User.findById(springSecurityService.principal.id,
                [cache: true, fetch: [facility: "join"]])).list()*.accessRight
    }
}
