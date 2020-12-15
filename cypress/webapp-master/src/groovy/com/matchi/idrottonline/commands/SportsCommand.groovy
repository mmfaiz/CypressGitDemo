package com.matchi.idrottonline.commands

import grails.validation.Validateable

@Validateable()
class SportsCommand {

    SportCommand sport

    static constraints = {
        sport validator: { it.validate() }
    }
}
