package com.matchi.facility

import com.matchi.CourtGroup
import com.matchi.Facility
import com.matchi.GenericController
import grails.transaction.Transactional
import javax.servlet.http.HttpServletResponse
import org.springframework.dao.DataIntegrityViolationException

class FacilityCourtsGroupsController extends GenericController {

    def index() {
        def facility = getUserFacility()

        if(facility == null) {
            render(view: "noFacility")
            return
        }
        return [facility: facility, groups: CourtGroup.facilityCourtGroups(facility).list()]
    }

    def edit(Long id) {
        Facility facility = getUserFacility()
        def courtGroupInstance = CourtGroup.findByIdAndFacility(id, facility)
        if (courtGroupInstance) {
            return [courtGroupInstance: courtGroupInstance, facility: facility]
        } else {
            flash.error = message(code: 'default.not.found.message',
                    args: [message(code: 'courtGroup.label'), id])
            redirect(action: "index")
        }
    }

    def create() {
        [courtGroupInstance: new CourtGroup(params), facility: getUserFacility()]
    }

    @Transactional
    def delete(Long id) {
        def courtGroupInstance = CourtGroup.findByIdAndFacility(id, getUserFacility())
        if (!courtGroupInstance) {
            flash.error = message(code: 'default.not.found.message',
                    args: [message(code: 'courtGroup.label'), id])
            redirect(action: "index")
            return
        }

        try {
            courtGroupInstance.delete(flush: true)
            flash.message = message(code: 'default.deleted.message',
                    args: [message(code: 'courtGroup.label'), id])
        } catch (DataIntegrityViolationException e) {
            flash.error = message(code: 'default.not.deleted.message',
                    args: [message(code: 'courtGroup.label'), id])
        }

        redirect(action: "index")
    }

    @Transactional
    def save() {
        Facility facility = getUserFacility()
        CourtGroup courtGroupInstance = new CourtGroup(params)
        courtGroupInstance.facility = facility

        if (courtGroupInstance.save(flush: true)) {
            flash.message = message(code: 'default.created.message',
                    args: [message(code: 'courtGroup.label'), courtGroupInstance.name])
            redirect(action: "index")
        }
        else {
            render(view: "create", model: [courtGroupInstance: courtGroupInstance, facility: facility])
        }
    }

    @Transactional
    def update(Long id, Long version) {
        Facility facility = getUserFacility()
        def courtGroupInstance = CourtGroup.findByIdAndFacility(id, facility)
        if (!courtGroupInstance) {
            flash.error = message(code: 'default.not.found.message',
                    args: [message(code: 'courtGroup.label'), id])
            redirect(action: "index")
            return
        }

        if (version != null) {
            if (courtGroupInstance.version > version) {
                courtGroupInstance.errors.rejectValue("version", "default.optimistic.locking.failure",
                        [message(code: 'courtGroup.label')] as Object[], "")
                render(view: "edit", model: [courtGroupInstance: courtGroupInstance, facility: facility])
                return
            }
        }

        courtGroupInstance.courts?.clear()
        courtGroupInstance.properties = params

        if (courtGroupInstance.save(flush: true)) {
            flash.message = message(code: "default.updated.message",
                    args: [message(code: 'courtGroup.label'), courtGroupInstance.name])
            redirect(action: "index")
        } else {
            render(view: "edit", model: [courtGroupInstance: courtGroupInstance, facility: facility])
        }
    }

    @Transactional
    def swapListPosition(Long id1, Long id2) {
        def facility = getUserFacility()
        def courtGroup1 = CourtGroup.findByIdAndFacility(id1, facility)
        def courtGroup2 = CourtGroup.findByIdAndFacility(id2, facility)
        if (courtGroup1 && courtGroup2) {
            def pos1 = courtGroup1.tabPosition
            courtGroup1.tabPosition = courtGroup2.tabPosition
            courtGroup1.save()
            courtGroup2.tabPosition = pos1
            courtGroup2.save()
            render ""
        } else {
            response.sendError HttpServletResponse.SC_BAD_REQUEST
        }
    }
}
