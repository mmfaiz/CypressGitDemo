package com.matchi.api_ext

import com.matchi.User
import com.matchi.api.Code
import grails.converters.JSON

class APIExtAuthController extends APIExtGenericController {
    def authenticationService
    def userService

    /**
     * Authenticates a request to the API component.
     * @return a JSON object with the user object, assigned facilities and roles.
     */
    def auth() {
        def token = request.getHeader("token")

        def credentials = null
        if (token) {
            def encodedPair = token - 'Basic '
            def decodedPair = new String(encodedPair.decodeBase64())
            credentials = decodedPair.split(':')
        } else {
            error(401, Code.BAD_CREDENTIALS, "Access denied")
        }

        def user = authenticationService.authenticateCredentials(credentials[0], credentials[1])
        if (!user) {
            error(401, Code.BAD_CREDENTIALS, "Access denied")
        }


        def response = [
                user: user,
                roles: getUserRoles(user),
                facilities: getUserFacilities(user)
        ]

        render response as JSON
    }

    private List<String> getUserRoles(User user) {
        List<String> roles = new ArrayList<>()
        if (user != null) {
            user.getRoles().each {
                roles.add(it.role.authority)
            }
        }
        return roles
    }

    private def getUserFacilities(User user) {
        def userFacilities = []
        if (user != null) {
            userService.getUserFacilities(user).each {facility ->
                def facilityRoles = []
                user.facilityUsers.find {it.facility.id == facility.id}.facilityRoles.each {facilityRole ->
                    facilityRoles << facilityRole.accessRight.name()
                }

                def userFacility = [
                        id: facility.id,
                        name: facility.name,
                        roles: facilityRoles
                ]

                userFacilities << userFacility
            }
        }
        return userFacilities
    }

}
