package com.matchi.admin

import com.matchi.Facility
import com.matchi.GenericController
import com.matchi.facility.Organization
import org.springframework.dao.DataIntegrityViolationException

import javax.servlet.http.HttpServletResponse

/**
 * @author Michael Astreiko
 */
class AdminFacilityOrganizationsController extends GenericController {
    def fortnox3Service
    def fortnox3CustomerService

    def index() {
        def facility = Facility.get(params.id)

        if (facility) {
            return [facility: facility, organizations: Organization.findAllByFacility(facility)]
        } else {
            response.sendError HttpServletResponse.SC_NOT_FOUND
        }
    }

    def create() {
        [organization: new Organization(params)]
    }

    def save() {
        def organization = new Organization(params)
        if (organization.save(flush: true)) {
            flash.message = message(code: "adminFacilityContracts.save.success")
            redirect(action: "index", id: organization.facility.id)
        } else {
            render(view: "create", model: [organization: organization])
        }
    }

    def edit(Long id) {
        def organization = Organization.get(id)
        if (organization) {
            return [organization: organization]
        } else {
            response.sendError HttpServletResponse.SC_NOT_FOUND
        }
    }

    def update(Long id) {
        def organization = Organization.get(id)
        if (organization) {
            bindData(organization, params, ['facility'])
            if (organization.save(flush: true)) {
                flash.message = message(code: "adminFacilityContracts.update.success")
                redirect(action: "index", id: organization.facility.id)
            } else {
                render(view: "edit", model: [organization: organization])
            }
        } else {
            response.sendError HttpServletResponse.SC_NOT_FOUND
        }
    }

    def delete(Long id) {
        def organization = Organization.get(id)
        if (organization) {
            def facilityId = organization.facility.id
            try {
                organization.delete(flush: true)
                flash.message = message(code: "adminFacilityContracts.delete.success")
            } catch (DataIntegrityViolationException e) {
                log.error(e.message, e)
                flash.error = message(code: "adminFacilityContracts.delete.failure")
            }
            redirect(action: "index", id: facilityId)
        } else {
            response.sendError HttpServletResponse.SC_NOT_FOUND
        }
    }

    def renewAccessToken(Long id, String authCode) {
        def organization = Organization.load(id)
        render fortnox3Service.retrieveAccessToken(organization, authCode) ?: 'ERR'
    }

    def testFortnox3Values(Long id){
        def organization = Organization.load(id)

        if(organization.fortnoxAccessToken && fortnox3CustomerService.isFortnoxEnabledForOrganization(organization)) {
            render "Successfully connected to Fortnox 3 API"
        } else {
            render "ERR"
        }
    }
}
