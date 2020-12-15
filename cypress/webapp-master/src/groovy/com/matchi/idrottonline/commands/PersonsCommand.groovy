package com.matchi.idrottonline.commands

import grails.validation.Validateable

@Validateable()
class PersonsCommand {

    PersonCommand person

    static constraints = {
        person validator: { it.validate() }
    }
}
