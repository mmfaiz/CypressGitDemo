package com.matchi.facility

import com.matchi.GenericController
import com.matchi.requirements.Requirement
import com.matchi.requirements.RequirementProfile
import grails.validation.Validateable
import org.springframework.dao.DataIntegrityViolationException

class FacilityRequirementsController extends GenericController {

    def requirementService

    def index() {
        def facility = getUserFacility()
        List profiles = RequirementProfile.createCriteria().list {
            eq('facility', facility)
        }

        [profiles: profiles, facility: facility]
    }

    def create() {
        RequirementProfileCommand cmd = new RequirementProfileCommand()
        List<Class<Requirement>> requirementClasses = Requirement.getSubClasses()

        List<Requirement> requirements = requirementClasses.collect { Class clazz ->
            return clazz.newInstance()
        }

        cmd.requirements = requirements.collectEntries { Requirement r ->
            [(r.hashCode()): [requirementClassName: r.class.getSimpleName(), properties: r.getRequirementProperties(), skip: true]]
        }

        [cmd: cmd, facility: getUserFacility()]
    }

    def save(RequirementProfileCommand cmd) {
        def facility = getUserFacility()

        if(cmd.hasErrors()) {
            flash.error = message(code: "facilityRequirements.save.failure", args: [cmd.name])
            render view: "create", model: [cmd: cmd]
            return
        }

        try {
            if (cmd.id) {
                requirementService.updateRequirementProfile(cmd)
            } else {
                requirementService.addRequirementProfile(facility, cmd)
            }
            flash.message = message(code: "facilityRequirements.save.success", args: [cmd.name])
            redirect(action: 'index')
        } catch (Exception e) {
            flash.error = message(code: "facilityRequirements.save.failure", args: [cmd.name])
            redirect(action: 'index')
            return
        }
    }

    def edit() {
        RequirementProfile requirementProfile = RequirementProfile.get(params.id)
        RequirementProfileCommand cmd = new RequirementProfileCommand()
        cmd.name = requirementProfile.name
        cmd.id = requirementProfile.id

        List<Class<Requirement>> requirementClasses = Requirement.getSubClasses()

        // We are checking for what requirements are already attached to the profile, and create new instances if not
        // so that they can be used in the view/logic and later be saved
        List<Requirement> requirements = requirementClasses.collect { Class clazz ->

            Requirement existingRequirement = requirementProfile.requirements.find() { Requirement r ->
                return r.class.getSimpleName().equals(clazz.getSimpleName())
            }

            return existingRequirement ?: clazz.newInstance()
        }

        cmd.requirements = requirements.collectEntries { Requirement r ->
            [(r.hashCode()): [requirementClassName: r.class.getSimpleName(), id: r.id, properties: r.getRequirementProperties(), skip: r.id == null]]
        }

        [cmd: cmd, facility: getUserFacility()]
    }

    def delete() {
        RequirementProfile requirementProfile = RequirementProfile.findById(params.id)

        try {
            requirementProfile.delete(failOnError: true)
            flash.message = message(code: "facilityRequirements.delete.success", args: [requirementProfile.name])
            redirect(action: 'index')
        } catch (DataIntegrityViolationException e) {
            e.printStackTrace()
            flash.error = message(code: "facilityRequirements.delete.foreignKeyFailure", args: [requirementProfile.name])
            redirect(action: 'index')
        } catch (Exception e){
            e.printStackTrace()
            flash.error = message(code: "facilityRequirements.delete.failure", args: [requirementProfile.name])
            redirect(action: 'index')
        }
    }

    def update(RequirementProfileCommand cmd) {
        if(cmd.hasErrors()) {
            flash.error = message(code: "facilityRequirements.update.failure", args: [cmd.name])
            render view: "edit", model: [cmd: cmd]
            return
        }

        try {
            requirementService.updateRequirementProfile(cmd)
            flash.message = message(code: "facilityRequirements.update.success", args: [cmd.name])
            redirect(action: 'index')
        } catch (Exception e) {
            flash.error = message(code: "facilityRequirements.update.failure", args: [cmd.name])
            redirect(action: 'index')
            return
        }
    }
}

@Validateable(nullable = true)
class RequirementProfileCommand {
    Long id
    String name
    Map requirements

    static constraints = {
        id(nullable: true)
        name(nullable: false)
        requirements(nullable: false)
    }
}
