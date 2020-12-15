package com.matchi

import org.joda.time.DateTime

/**
 * @author Sergei Shushkevich
 */
class CustomerDisableMessagesTicket extends Ticket implements Serializable {

    static belongsTo = [customer: Customer]

    static constraints = {
        customer(nullable: false)
    }

    boolean isUsed() {
        consumed
    }

}
