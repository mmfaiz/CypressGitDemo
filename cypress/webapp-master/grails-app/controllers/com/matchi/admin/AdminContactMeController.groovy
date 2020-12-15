package com.matchi.admin

import com.matchi.ContactMe

class AdminContactMeController {

    def index() {
        [signups: ContactMe.findAll()]
    }

    def toggle() {
        def contactme = ContactMe.get(params.id)
        contactme.contacted = !contactme.contacted
        contactme.save()
        redirect(action: "index")
    }
}
