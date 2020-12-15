package com.matchi

class CustomerInviteTicket extends Ticket implements Serializable {
    static belongsTo = [customer: Customer]

    static constraints = {
        customer(nullable: false)
    }
}
