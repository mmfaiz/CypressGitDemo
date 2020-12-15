package com.matchi

import grails.converters.JSON

import javax.servlet.http.HttpServletResponse
import org.grails.plugins.sanitizer.*

/**
 * @author Sergei Shushkevich
 */
class UserMessageController extends GenericController {

    def userMessageService
    def userService
    def markupSanitizerService

    def index() {
        def currentUser = getCurrentUser()
        def msg = UserMessage.conversationMessages(currentUser).list(
                sort: "dateCreated", order: "desc", max: 1, fetch: [from: "join", to: "join"])[0]
        if (msg) {
            redirect(action: "conversation", id: msg.from == currentUser ? msg.to.id : msg.from.id)
        }
    }

    def conversation(Long id) {
        def targetUser = userService.getUser(id)
        if (targetUser) {
            def currentUser = getCurrentUser()
            return [currentUser: currentUser, targetUser: targetUser,
                    messages: userMessageService.listConversationMessages(currentUser, targetUser),
                    conversations: userMessageService.listConversations(currentUser)]
        } else {
            response.sendError HttpServletResponse.SC_NOT_FOUND
        }
    }

    def sendMessage(Long id, String message) {
        def targetUser = userService.getUser(id)
        if (targetUser) {
            def fromUser = getCurrentUser()
            if (!userService.canSendDirectMessage(fromUser, targetUser)) {
                response.sendError HttpServletResponse.SC_BAD_REQUEST
                return
            }

            def msg = userMessageService.send(fromUser, targetUser, message)
            if (msg) {
                MarkupSanitizerResult result = markupSanitizerService.sanitize(msg.message)
                render([message: result.cleanString, date: formatDate(date: msg.dateCreated,
                        formatName: "date.format.dateOnly")] as JSON)
            } else {
                response.sendError HttpServletResponse.SC_BAD_REQUEST
            }
        } else {
            response.sendError HttpServletResponse.SC_NOT_FOUND
        }
    }

    def listUnreadMessages(Long id) {
        def targetUser = userService.getUser(id)
        if (targetUser) {
            def result = userMessageService.listUnreadIncomingMessages(getCurrentUser(), targetUser).collect {
                [from: it.from.id, to: it.to.id, message: it.message,
                        date: formatDate(date: it.dateCreated, formatName: "date.format.dateOnly")]
            }
            render(result as JSON)
        } else {
            response.sendError HttpServletResponse.SC_NOT_FOUND
        }
    }

    def countUnreadMessages() {
        render([count: UserMessage.unreadIncomingMessages(getCurrentUser()).count()] as JSON)
    }
}
