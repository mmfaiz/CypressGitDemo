package com.matchi.admin

import com.matchi.*
import com.matchi.requirements.RequirementProfile
import org.springframework.validation.Errors

class AdminFacilityCourtsController extends GenericController {
    CourtService courtService
    CameraService cameraService

    static final PARAMS_CAMERA_NAME_PREFIX = "cameraName_"
    static final PARAMS_CAMERA_EXTERNAL_ID_PREFIX = "cameraExternalId_"
    static final PARAMS_CAMERA_PROVIDER_PREFIX = "cameraProvider_"

    def index() {
        def facility = Facility.get(params.id)

        if(facility == null) {
            render(view: "noFacility")
            return
        }

        def courts = facility.courts
        return [facility:facility, courts:courts]
    }

    def edit() {
        Court courtInstance = Court.get(params.id)
        if (!courtInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'court.label', default: 'Court'), params.id])}"
            redirect(action: "index")
        }

        def parents = courtInstance.facility.courts.findAll { it.id != courtInstance.id }

        return [ courtInstance: courtInstance, facility: courtInstance.facility, parents: parents, requirementProfiles: RequirementProfile.findAllByFacility(courtInstance.facility), cameras: courtInstance.cameras ]
    }

    def create() {
        def courtInstance = new Court()
        courtInstance.facility = Facility.get(params.facilityId)
        courtInstance.properties = params

        return [courtInstance: courtInstance, facility: courtInstance.facility, requirementProfiles: RequirementProfile.findAllByFacility(courtInstance.facility)]
    }

    def confirmDelete() {
        def courtInstance = Court.get(params.id)
        if (!courtInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'court.label', default: 'Court'), params.id])}"
            redirect(action: "index")
        }

        return [courtInstance: courtInstance]
    }

    def delete() {
        def courtInstance = Court.get(params.id)
        if (!courtInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'court.label', default: 'Court'), params.id])}"
            redirect(action: "index")
        }

        courtService.deleteCourt(courtInstance)
        flash.message = message(code: "adminFacilityCourts.delete.success")
        redirect(action: "index", params: [id: courtInstance.facility.id])
    }

    def save() {
        def courtInstance = new Court(params)
        courtInstance.facility = Facility.get(params.facilityId)

        courtService.updateCourtInstanceWithCourtTypeAttributes(courtInstance, params)

        // Need to invalidate cache by modifying the collection on owning object (Facility)
        // https://planet.jboss.org/post/collection_caching_in_the_hibernate_second_level_cache
        courtInstance.facility.courts.add(courtInstance)
        if (params.list("requirementProfiles")?.any()) {
            courtInstance.setRequirementProfiles(params.list("requirementProfiles"))
        }

        log.info("Saving court")

        if (courtInstance.validateCourtTypeAttribute()) {
            courtInstance.errors.reject(courtInstance.validateCourtTypeAttribute())
        }

        if (courtService.createCourt(courtInstance)) {
            if(params.parentCourtId) {
                log.info("Adding to parent court")
                Court parent = Court.get(params.parentCourtId)
                parent.addToChilds(courtInstance)
                parent.save()

                courtInstance.parent = parent
            }

            flash.message = courtInstance.name + " skapad"
            redirect(action: "index", params: [id: courtInstance.facility.id])
        }
        else {
            render(view: "create", model: [courtInstance: courtInstance, facility: courtInstance.facility, requirementProfiles: RequirementProfile.findAllByFacility(courtInstance.facility)])
        }
    }

    def update() {
        def courtInstance = Court.get(params.id)
        if (courtInstance) {
            if (params.version) {
                def version = params.version.toLong()
                if (courtInstance.version > version) {

                    courtInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [message(code: 'court.label', default: 'Court')] as Object[], "Another user has updated this Court while you were editing")
                    render(view: "edit", model: [courtInstance: courtInstance])
                    return
                }
            }
            courtInstance.properties = params
            if (params.list("requirementProfiles")?.any()) {
                courtInstance.setRequirementProfiles(params.list("requirementProfiles"))
            }

            updateCameras(courtInstance)

            courtService.updateCourtInstanceWithCourtTypeAttributes(courtInstance, params)

            if (courtInstance.validateCourtTypeAttribute()) {
                courtInstance.errors.reject(courtInstance.validateCourtTypeAttribute())
            }

            if (!courtInstance.hasErrors() && courtService.updateCourt(courtInstance)) {
                if(params.parentCourtId) {
                    log.info("Adding to parent court")
                    Court parent = Court.get(params.parentCourtId)
                    parent.addToChilds(courtInstance)
                    parent.save()
                    courtInstance.parent = parent

                } else {
                    if(courtInstance.parent) {
                        courtInstance.parent = null
                        courtInstance.save()
                    }

                }

                flash.message = message(code: "adminFacilityCourts.update.success")
                redirect(action: "index", params: [id: courtInstance.facility.id])
            }
            else {
                render(view: "edit", model: [courtInstance: courtInstance, facility: courtInstance.facility, requirementProfiles: RequirementProfile.findAllByFacility(courtInstance.facility)])
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'court.label', default: 'Court'), params.id])}"
            redirect(action: "index")
        }
    }

    private void updateCameras(Court courtInstance) {
        // Delete all current cameras.
        cameraService.deleteCamerasByCourt(courtInstance)

        // Loop request params and create cameras for each added.
        params.each {
            String paramKey = it.key.toString()
            if (paramKey.startsWith(PARAMS_CAMERA_NAME_PREFIX)) {
                int cameraIndex = paramKey.substring(PARAMS_CAMERA_NAME_PREFIX.length()).toInteger()
                String cameraName = params.get(PARAMS_CAMERA_NAME_PREFIX + cameraIndex).toString()
                Integer cameraId = Integer.parseInt(params.get(PARAMS_CAMERA_EXTERNAL_ID_PREFIX + cameraIndex).toString())
                Camera.CameraProvider cameraProvider = Camera.CameraProvider.valueOfName(params.get(PARAMS_CAMERA_PROVIDER_PREFIX + cameraIndex).toString())
                cameraService.createCamera(cameraName, cameraId, cameraProvider, courtInstance)
            }
        }
    }
}
