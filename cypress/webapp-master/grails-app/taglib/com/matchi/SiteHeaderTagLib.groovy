package com.matchi

class SiteHeaderTagLib {

    def userService

    def b3Header = { attrs, body ->

        User user = userService.getLoggedInUser()
        def facilityName = user?.facility?.name
        attrs << [ activeFacility: user?.facility, facilityName:facilityName ]

        out << render(template:"/templates/general/header", model: attrs)
    }

    def b3FacilityHeader = { attrs, body ->

        User user = userService.getLoggedInUser()
        def facilityName = user?.facility?.name
        attrs << [ activeFacility: user?.facility, facilityName:facilityName ]

        out << render(template:"/templates/general/facilityHeader", model: attrs)
    }

    def headerLayout = { attrs, body ->

        if (attrs.facility) {
            User user = userService.getLoggedInUser()
            def facilityName = user?.facility?.name
            attrs << [ activeFacility: user.facility, facilityName:facilityName ]
        }

        out << render(template:"/templates/header", model: attrs)
    }

    def headerLayoutResponsive = { attrs, body ->

        if (attrs.facility) {
            User user = userService.getLoggedInUser()
            attrs << [ activeFacility: user.facility ]
        }

        out << render(template:"/templates/headerResponsive", model: attrs)
    }

}
