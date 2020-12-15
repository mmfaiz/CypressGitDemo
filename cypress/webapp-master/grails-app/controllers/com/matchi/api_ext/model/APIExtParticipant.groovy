package com.matchi.api_ext.model

import com.matchi.Customer

class APIExtParticipant {
    String name
    String profileImg

    APIExtParticipant(Customer customer) {
        this.name = customer.firstname && customer.lastname ? customer.firstname + " " + customer.lastname : customer.companyname
        this.profileImg = customer?.user?.profileImage?.absoluteFileURL
    }

}
