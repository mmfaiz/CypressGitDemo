package com.matchi

class HelpTagLib {
    def cookiesModal = { attrs, body ->

        out << render(template: "/templates/user/cookiesModal", model: attrs)
    }
}
