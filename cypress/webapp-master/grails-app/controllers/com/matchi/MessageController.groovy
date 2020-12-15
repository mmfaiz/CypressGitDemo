package com.matchi

class MessageController extends GenericController {

    def userMessageService
    def userService

    def index() {
        [ user: User.get(params.id), returnUrl: params.returnUrl ]
    }

    def sendMessage() {
        def userFrom = getCurrentUser()
        def userTo = User.get(params.id)

        if (!userService.canSendDirectMessage(userFrom, userTo)) {
            flash.error = message(code: "message.sendMessage.error")
        } else if (!userMessageService.send(userFrom, userTo, params.message)) {
            flash.error = message(code: "message.sendMessage.error")
        } else {
            flash.message = message(code: "message.sendMessage.success", args: [userTo.fullName()])
        }

        if(params.returnUrl && params.returnUrl.size() > 0) {
            redirect(url: params.returnUrl)
            return
        }

        redirect(controller: "userProfile", action: "index", params: [id: params.id])
    }
}
