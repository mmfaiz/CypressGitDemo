package com.matchi

/**
 * @author Sergei Shushkevich
 */
class UserMessage {

    User from
    User to
    String message
    Boolean markedAsRead = false
    Date dateCreated

    static constraints = {
        message blank: false, maxSize: 2000, markup: true
        dateCreated nullable: true
    }

    static mapping = {
        version false
        message type: "text"
    }

    static namedQueries = {
        conversationMessages { currentUser, targeUser = null ->
            or {
                and {
                    eq("from", currentUser)
                    if (targeUser) {
                        eq("to", targeUser)
                    }
                }
                and {
                    if (targeUser) {
                        eq("from", targeUser)
                    }
                    eq("to", currentUser)
                }
            }
        }
        unreadIncomingMessages { currentUser, fromUser = null ->
            eq("to", currentUser)
            if (fromUser) {
                eq("from", fromUser)
            }
            eq("markedAsRead", false)
        }
    }
}
