package com.matchi

/**
 * @author Sergei Shushkevich
 */
class UserMessageService {
    static transactional = false
    def notificationService

    List listConversations(User user) {
        def ids = UserMessage.withCriteria {
            projections {
                distinct("to.id")
            }
            eq("from", user)
        }
        ids += UserMessage.withCriteria {
            projections {
                distinct("from.id")
            }
            eq("to", user)
        }

        ids.unique().collect { id ->
            UserMessage.withCriteria {
                or {
                    and {
                        eq("from", user)
                        to {
                            eq("id", id)
                        }
                    }
                    and {
                        from {
                            eq("id", id)
                        }
                        eq("to", user)
                    }
                }
                order("dateCreated", "desc")
                maxResults(1)
            }[0]
        }.sort {
            -it.dateCreated.time
        }
    }

    List listConversationMessages(User currentUser, User targetUser) {
        def msgs = UserMessage.conversationMessages(currentUser, targetUser).list(sort: "dateCreated", order: "asc")
        msgs.each {
            if (!it.markedAsRead && it.to == currentUser) {
                it.markedAsRead = true
                it.save()
            }
        }
        msgs
    }

    List listUnreadIncomingMessages(User currentUser, User fromUser) {
        def msgs = UserMessage.unreadIncomingMessages(currentUser, fromUser).list(fetch: [from: "join", to: "join"])
        msgs.each {
            it.markedAsRead = true
            it.save()
        }
        msgs
    }

    UserMessage send(User from, User to, String message) {
        def msg = new UserMessage(from: from, to: to, message: message).save()
        if (msg) {
            notificationService.sendUserMessage(msg.from, msg.to, msg.message)
        }
        msg
    }
}
