package com.matchi

class ResetPasswordTicket extends Ticket implements Serializable {
    static belongsTo = [user: User]

    static constraints = {
        user nullable: false
    }
}
