package com.matchi

class AdminTagLib {

    static returnObjectForTags = ["getUserFacility"]

    def userService

    def switchFacility = { attrs, body ->
        def facilities = Facility.list([cache: true]).unique().findAll { Facility it ->
            it.enabled && !it.hasBeenArchived()
        }.sort { Facility it ->
            it.name
        }
        def currentFacility = userService.getUserFacility()
        out << render(template: "/templates/admin/switchFacilityDropDown", model: [facilities: facilities, currentFacility: currentFacility])
    }

    def switchUserFacility = { attrs, body ->
        def facilities = userService.userFacilities
        if (facilities.size() > 1) {
            out << " | " << render(template: "/templates/admin/switchFacilityDropDown",
                    model: [facilities      : facilities, currentFacility: userService.userFacility,
                            targetController: "facilityAdministration"])
        }
    }

    def facilityProperty = { attrs, body ->
        def facility = attrs.facility ?: userService.getUserFacility()
        def property = FacilityProperty.findByKeyAndFacility(attrs.property, facility)?.value
        if (attrs.resultVar) {
            pageScope."${attrs.resultVar}" = property
        } else {
            out << property ?: ''
        }

    }

    def ifFacilityPropertyEnabled = { attrs, body ->
        def facility = attrs.facility ?: userService.getUserFacility()
        if (facility?.isFacilityPropertyEnabled(attrs.name)) {
            out << body()
        }
    }

    def getUserFacility = { attrs, body ->
        userService.getUserFacility()
    }
}
