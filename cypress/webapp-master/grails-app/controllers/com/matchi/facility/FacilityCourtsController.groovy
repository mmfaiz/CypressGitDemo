package com.matchi.facility

import com.matchi.Court
import com.matchi.CourtTypeAttribute
import com.matchi.CourtTypeEnum
import com.matchi.Facility
import com.matchi.GenericController
import com.matchi.requirements.RequirementProfile
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsParameterMap

import javax.servlet.http.HttpServletResponse

class FacilityCourtsController extends GenericController {
    def courtService

    def index() {
        def facility = getUserFacility()

        if (facility == null) {
            render(view: "noFacility")
            return
        }

        return [facility: facility, courts: Court.available(facility).list()]
    }

    def archive() {
        def facility = getUserFacility()

        if (!facility) {
            render(view: "noFacility")
            return
        }

        [facility: facility, courts: Court.archivable(facility).list()]
    }

    def edit() {
        def courtInstance = Court.get(params.id)
        if (!courtInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'court.label', default: 'Court'), params.id])}"
            redirect(action: "index")
        } else {
            assertFacilityAccessTo(courtInstance)
        }

        return [courtInstance: courtInstance, requirementProfiles: RequirementProfile.findAllByFacility(courtInstance.facility)]
    }

    def create() {
        def courtInstance = new Court()
        courtInstance.properties = params

        Facility facility = getUserFacility()

        return [courtInstance: courtInstance, facility: facility, requirementProfiles: RequirementProfile.findAllByFacility(facility)]
    }

    def confirmDelete() {
        def courtInstance = Court.get(params.id)
        if (!courtInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'court.label', default: 'Court'), params.id])}"
            redirect(action: "index")
        } else {
            assertFacilityAccessTo(courtInstance)
        }

        return [courtInstance: courtInstance]
    }

    def delete() {
        def courtInstance = Court.get(params.id)
        if (!courtInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'court.label', default: 'Court'), params.id])}"
            redirect(action: "index")
        } else {
            assertFacilityAccessTo(courtInstance)
        }

        courtService.deleteCourt(courtInstance)
        flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'court.label', default: 'Court'), courtInstance.id])}"
        redirect(action: "index")
    }

    def save() {
        Court courtInstance = new Court(params)
        courtInstance.facility = getUserFacility()

        courtService.updateCourtInstanceWithCourtTypeAttributes(courtInstance, params as GrailsParameterMap)

        if (params.list("requirementProfiles")?.any()) {
            courtInstance.setRequirementProfiles(params.list("requirementProfiles"))
        }

        if (courtInstance.validateCourtTypeAttribute()) {
            courtInstance.errors.reject(courtInstance.validateCourtTypeAttribute())
        }

        if (courtService.createCourt(courtInstance)) {
            flash.message = message(code: "facilityCourts.save.success", args: [courtInstance.name])
            redirect(action: "index")
        } else {
            render(view: "create", model: [courtInstance: courtInstance, facility: getUserFacility()])
        }
    }

    def update() {
        def courtInstance = Court.get(params.id)
        if (courtInstance) {
            assertFacilityAccessTo(courtInstance)

            if (params.version) {
                def version = params.version.toLong()
                if (courtInstance.version > version) {

                    courtInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [message(code: 'court.label', default: 'Court')] as Object[], "Another user has updated this Court while you were editing")
                    render(view: "edit", model: [courtInstance: courtInstance])
                    return
                }
            }
            courtInstance.properties = params

            courtService.updateCourtInstanceWithCourtTypeAttributes(courtInstance, params)

            if (params.list("requirementProfiles")?.any()) {
                courtInstance.setRequirementProfiles(params.list("requirementProfiles"))
            }

            if (courtInstance.validateCourtTypeAttribute()) {
                courtInstance.errors.reject(courtInstance.validateCourtTypeAttribute())
            }

            if (!courtInstance.hasErrors() && courtService.updateCourt(courtInstance)) {
                flash.message = message(code: "facilityCourts.update.success")
                redirect(action: "index")
            } else {
                render(view: "edit", model: [courtInstance: courtInstance])
            }
        } else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'court.label', default: 'Court'), params.id])}"
            redirect(action: "index")
        }
    }

    def updateAvailability(Court court, def params) {
        switch (params.bookingAvailability) {
            case "membersOnly":
                court.restriction = Court.Restriction.MEMBERS_ONLY
                break
            case "offlineOnly":
                court.restriction = Court.Restriction.OFFLINE_ONLY
                break
            default:
                court.restriction = Court.Restriction.NONE
        }
    }

    def swapListPosition(Long id1, Long id2) {
        def court1 = Court.get(id1)
        def court2 = Court.get(id2)
        if (court1 && court2) {
            courtService.swapListPosition(court1, court2)
            render ""
        } else {
            response.sendError HttpServletResponse.SC_BAD_REQUEST
        }
    }

    def putToArchive() {
        def courtInstance = Court.get(params.long('id'))
        if (!courtInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'court.label', default: 'Court'), params.id])}"
            redirect(action: "index")
        } else {
            assertFacilityAccessTo(courtInstance)
        }

        courtInstance.archived = true
        courtService.updateCourt(courtInstance)

        flash.message = message(code: 'facilityCourts.archive.success')

        redirect(action: "index")
    }

    def getFromArchive() {
        def courtInstance = Court.get(params.long('id'))
        if (courtInstance) {
            courtInstance.archived = false
            courtService.updateCourt(courtInstance)
            flash.message = message(code: 'facilityCourts.reactivate.success')
            redirect action: "index"
        } else {
            flash.error = message(code: 'facilityCourts.reactivate.failure')
            redirect action: "archive"
        }
    }

}
