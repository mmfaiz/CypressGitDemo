package com.matchi

class ChangeEmailTicket extends Ticket implements Serializable {

    String newEmail

    static belongsTo = [user: User]

    static constraints = {
        user nullable: false
    }
}
