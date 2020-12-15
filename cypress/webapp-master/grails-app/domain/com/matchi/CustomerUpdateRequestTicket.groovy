package com.matchi

class CustomerUpdateRequestTicket extends Ticket implements Serializable {
    static belongsTo = [customer: Customer]

    static constraints = {
        customer(nullable: false)
    }
}
