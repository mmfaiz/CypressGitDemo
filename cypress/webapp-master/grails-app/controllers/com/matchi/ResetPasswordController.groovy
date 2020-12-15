package com.matchi

import grails.validation.Validateable

class ResetPasswordController {
    def ticketService
    def notificationService
    def userService

    def index() { }

    def reset(ResetPasswordCommand cmd) {

        if(!cmd.validate()) {
            render view: "index", model: [cmd:cmd]
            return
        }

        def user = User.findByEmail(cmd.email)

        if(!user) {
            flash.error = message(code: "resetPassword.reset.error", args: [cmd.email])
            render view: "index", model: [cmd:cmd]
            return

        } else {
            def ticket = ticketService.createResetPasswordTicket(user)
            notificationService.sendResetPasswordTicketMail(user, ticket)

            def parameters = [:]
            if(params.wl) {
                parameters += [wl: 1]
            }

            if(params.returnUrl) {
                parameters += [returnUrl: params.returnUrl]
            }

            flash.message = message(code: "resetPassword.reset.success")
            redirect(controller: "info", action: "index", params: parameters)
        }

    }

    def change(ChangePasswordCommand cmd) {
        if(!cmd.ticket || !ticketService.isTicketValid(cmd.ticket)) {
            flash.error = message(code: "resetPassword.change.error")
            redirect(controller: "info", action: "index")
            return
        }

        render view: "change", model: [cmd:cmd]
    }

    def update(UpdatePasswordCommand cmd) {
        if(!cmd.ticket || !ticketService.isTicketValid(cmd.ticket)) {
            flash.error = message(code: "resetPassword.update.error")
            redirect(controller: "info", action: "index")
            return
        }

        if(!cmd.validate()) {
            render view: "change", model: [cmd:cmd]
            return
        }

        userService.changePasswordWithTicket(cmd.newPassword, cmd.ticket)
        flash.message = message(code: "resetPassword.update.success")

        redirect(controller: "info", action: "index")
    }

}

@Validateable(nullable = true)
class ResetPasswordCommand {
    String email

    static constraints = {
		email(email:true, nullable: false, blank: false)
    }
}

@Validateable(nullable = true)
class ChangePasswordCommand {
    String ticket
    String newPassword
    String newPasswordConfirm
}

@Validateable(nullable = true)
class UpdatePasswordCommand {
    String ticket
    String newPassword
    String newPasswordConfirm

    static constraints = {
        ticket(nullable: false)
		newPassword(blank:false)
		newPasswordConfirm(blank: false, nullable: false, validator: {password, obj ->
			def password2 = obj.properties['newPassword']
			password2 == password ? ValidationUtils.validateUserPassword(password) : ['invalid.matchingpasswords']
		})
    }

}
