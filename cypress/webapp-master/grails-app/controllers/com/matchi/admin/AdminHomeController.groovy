package com.matchi.admin

import com.matchi.Facility

class AdminHomeController {

    def userService

    def index() { }

    def switchFacility() {
        def currentUser = userService.getLoggedInUser()
        currentUser.facility = Facility.get(params.aFacility)
        currentUser.save()

        redirect(controller: "facilityBooking", action: "index")
    }
}
