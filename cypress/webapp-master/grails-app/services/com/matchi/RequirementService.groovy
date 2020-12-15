package com.matchi

import com.matchi.facility.RequirementProfileCommand
import com.matchi.requirements.Requirement
import com.matchi.requirements.RequirementProfile
import grails.transaction.Transactional

class RequirementService {

    static transactional = false

    @Transactional
    def addRequirementProfile(Facility facility, RequirementProfileCommand cmd) {
        RequirementProfile requirementProfile = new RequirementProfile()
        requirementProfile.name = cmd.name
        requirementProfile.facility = facility

        requirementProfile.save(failOnError: true)

        cmd.requirements.each { key, values ->
            if(!values.skip) {
                String className = values.requirementClassName
                if(Requirement.isValidClassName(className)) {
                    String realClassName = Requirement.getRealClassName(className)
                    Requirement requirement = Class.forName(realClassName).newInstance()
                    requirement.setValues(values)
                    requirement.profile = requirementProfile
                    requirement.save(failOnError: true)
                }
            }
        }
    }

    @Transactional
    def updateRequirementProfile(RequirementProfileCommand cmd) {
        RequirementProfile requirementProfile = RequirementProfile.findById(cmd.id)
        requirementProfile.name = cmd.name

        cmd.requirements.each { key, values ->
            String className = values.requirementClassName

            if(!values.skip) {
                if(Requirement.isValidClassName(className)) {
                    String realClassName = Requirement.getRealClassName(className)

                    Requirement requirement
                    if(values.id) {
                        requirement = Class.forName(realClassName).get(values.id)
                    } else {
                        requirement = Class.forName(realClassName).newInstance()
                    }

                    requirement.setValues(values)
                    requirement.profile = requirementProfile
                    requirement.save(failOnError: true)
                }

            } else {

                if(Requirement.isValidClassName(className) && values.id) {
                    String realClassName = Requirement.getRealClassName(className)
                    Requirement requirement = Class.forName(realClassName).get(values.id)
                    requirementProfile.removeFromRequirements(requirement)
                    requirement.delete()
                }

            }
        }

        requirementProfile.save(failOnError: true)
    }
}
