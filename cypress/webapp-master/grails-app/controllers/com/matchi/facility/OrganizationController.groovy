package com.matchi.facility

import com.matchi.GenericController
import org.springframework.dao.DataIntegrityViolationException

class OrganizationController extends GenericController {

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def index() {
        redirect(action: "list", params: params)
    }

    def list() {
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
        def organizations = Organization.createCriteria().list(params) {
            eq('facility', getUserFacility())
        }
        [organizationInstanceList: organizations, organizationInstanceTotal: organizations.totalCount]
    }

    def create() {
        [organizationInstance: new Organization(params)]
    }

    def save() {
        def organizationInstance = new Organization(params)
        organizationInstance.facility = getUserFacility()
        if (organizationInstance.save(flush: true)) {
            flash.message = message(code: 'default.created.message', args: [message(code: 'organization.label', default: 'Organization'), organizationInstance.id])
            redirect(action: "list")
        } else {
            render(view: "create", model: [organizationInstance: organizationInstance])
        }
    }

    def edit() {
        def organizationInstance = Organization.get(params.id)
        if (organizationInstance) {
            [organizationInstance: organizationInstance]
        } else {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'organization.label', default: 'Organization'), params.id])
            redirect(action: "list")
        }
    }

    def update(Long id, Long version) {
        def organizationInstance = Organization.get(id)
        if (!organizationInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'organization.label', default: 'Organization'), id])
            redirect(action: "list")
            return
        }

        if (version != null) {
            if (organizationInstance.version > version) {
                organizationInstance.errors.rejectValue("version", "default.optimistic.locking.failure",
                        [message(code: 'organization.label', default: 'Organization')] as Object[],
                        "Another user has updated this Organization while you were editing")
                render(view: "edit", model: [organizationInstance: organizationInstance])
                return
            }
        }

        organizationInstance.properties = params

        if (!organizationInstance.save(flush: true)) {
            render(view: "edit", model: [organizationInstance: organizationInstance])
            return
        }

        flash.message = message(code: 'default.updated.message', args: [message(code: 'organization.label', default: 'Organization'), organizationInstance.id])
        redirect(action: "list")
    }

    def delete() {
        def organizationInstance = Organization.get(params.id)
        if (!organizationInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'organization.label', default: 'Organization'), params.id])
            redirect(action: "list")
            return
        }

        try {
            organizationInstance.delete(flush: true)
            flash.message = message(code: 'default.deleted.message', args: [message(code: 'organization.label', default: 'Organization'), params.id])
            redirect(action: "list")
        }
        catch (DataIntegrityViolationException e) {
            flash.message = message(code: 'default.not.deleted.message', args: [message(code: 'organization.label', default: 'Organization'), params.id])
            redirect(action: "show", id: params.id)
        }
    }
}
