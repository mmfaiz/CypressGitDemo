package com.matchi.facility

import com.matchi.FacilityUser
import com.matchi.GenericController
import com.matchi.User
import org.springframework.dao.DataIntegrityViolationException

class FacilityUserController extends GenericController {

    def userService

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def index() {
        def facility = getUserFacility()

        params.max = Math.min(params.max ? params.int('max') : 50, 100)
        if (!params.sort) {
            params.sort = "email"
            params.order = "asc"
        }

        [userInstanceList: User.allFacilityUsers(facility).list(params),
                userInstanceTotal: User.allFacilityUsers(facility).count(), facility: facility]
    }

    def create() {
        [facility: getUserFacility()]
    }

    def save() {
        def user = User.findByEmail(params.email)
        if (user) {
            if (user.isInRole("ROLE_ADMIN")) {
                flash.error = message(code: "facilityUser.save.adminUserError")
                redirect(action: "index")
            } else {
                grantAccess(user, message(code: 'facilityUser.save.success',
                        args: [user.fullName().encodeAsHTML()]), "create")
            }
        } else {
            flash.error = message(code: "facilityUser.user.nullable")
            render(view: "create", model: [user: user, facility: getUserFacility()])
        }
    }

    def edit() {
        def user = User.get(params.id)
        if (user) {
            return [user: user, facility: getUserFacility()]
        } else {
            flash.error = message(code: 'default.not.found.message', args: [message(code: 'facilityUser.label', default: 'FacilityUser'), params.id])
            redirect(action: "index")
        }
    }

    def update(Long id, Long version) {
        def user = User.get(id)
        if (!user) {
            flash.error = message(code: 'default.not.found.message', args: [message(code: 'facilityUser.label', default: 'FacilityUser'), id])
            redirect(action: "index")
            return
        }

        if (version != null) {
            if (user.version > version) {
                user.errors.rejectValue("version", "default.optimistic.locking.failure",
                          [message(code: 'facilityUser.label', default: 'FacilityUser')] as Object[],
                          "Another user has updated this FacilityUser while you were editing")
                render(view: "edit", model: [user: user, facility: getUserFacility()])
                return
            }
        }

        grantAccess(user, message(code: 'facilityUser.update.success',
                args: [user.fullName().encodeAsHTML()]), "edit")
    }

    def delete() {
        if (!User.countById(params.id)) {
            flash.error = message(code: 'default.not.found.message', args: [message(code: 'facilityUser.label', default: 'FacilityUser'), params.id])
            redirect(action: "index")
            return
        }

        try {
            userService.removeFacilityUser(params.long("id"))
            flash.message = message(code: 'facilityUser.delete.success')
            redirect(action: "index")
        } catch (DataIntegrityViolationException e) {
            flash.error = message(code: 'default.not.deleted.message', args: [message(code: 'facilityUser.label', default: 'FacilityUser'), params.id])
            redirect(action: "edit", id: params.id)
        }
    }

    private void grantAccess(user, successMessage, errorAction) {
        User.withTransaction {
            def facility = getUserFacility()
            user.facility = facility

            def facilityUser = user.facilityUsers.find {it.facility.id == facility.id}
            if (facilityUser) {
                ([] + facilityUser.facilityRoles).each {
                    facilityUser.removeFromFacilityRoles(it)
                    it.delete()
                }
            } else {
                facilityUser = new FacilityUser(user: user)
                user.addToFacilityUsers(facilityUser)
            }
            facilityUser.properties = params
            facilityUser.facility = facility

            if (user.save(flush: true)) {
                flash.message = successMessage
                redirect(action: "index")
            } else {
                user.discard()
                render(view: errorAction, model: [user: user, facility: facility])
            }
        }
    }
}
