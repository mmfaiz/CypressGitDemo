package com.matchi.idrottonline.commands

import grails.validation.Validateable

@Validateable()
class OrganisationsCommand {

    OrganisationCommand organisation

    static constraints = {
        organisation validator: { it.validate() }
    }
}
