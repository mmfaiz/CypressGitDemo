package com.matchi

import org.codehaus.groovy.grails.validation.Validateable

class ChangeEmailController {

    def customerService
    def ticketService
    def userService

    def index() { }

    def change() {
        if(!params.ticket || !ticketService.isChangeMailTicketValid(params.ticket)) {
            flash.error = message(code: "changeEmail.update.error")
            redirect(controller: "info", action: "index")
            return
        }

        userService.changeEmailWithTicket(params.newEmail, params.ticket)
        customerService.updateCustomersEmail(ChangeEmailTicket.findByKey(params.ticket).user)
        flash.message = message(code: "changeEmail.update.success")

        redirect(controller: "info", action: "index")
    }

}
