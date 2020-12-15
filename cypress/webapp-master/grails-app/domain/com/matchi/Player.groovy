package com.matchi

class Player {

    String email
    Customer customer

    static belongsTo = [booking: Booking]

    static constraints = {
        email nullable: true, email: true
        customer nullable: true
    }

    static mapping = {
        sort "id"
        email index: "player_email_idx"
    }

    boolean isBookingCustomer(String customerEmail) {
        if(this.customer?.email && this.email){
            return (this.customer?.email == customerEmail && this.email == customerEmail)
        } else if(!this.customer?.email && this.email) {
            return (this.email == customerEmail)
        } else if (this.customer?.email && !this.email) {
            return (this.customer?.email == customerEmail)
        } else {
            return false
        }
    }
}
