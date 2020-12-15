import com.matchi.Facility
import com.matchi.api.Code
import grails.converters.JSON
import org.springframework.security.core.context.SecurityContextHolder

import javax.servlet.http.HttpServletRequest

class APIAuthorizationFilters {

    def authenticationService
    def springSecurityService

    def filters = {
        mlcs(controller: '(MLCS|backhandSmash|orderResource|customerResource|articleResource|invoiceResource)') {
            before = {
                log.debug("Incoming request to Matchi API to ${controllerName}/${actionName}")
                if (!authenticate(request)) {
                    response.setStatus(400, "Not authorized")
                    render(text: "Not authorized")
                    return false
                }
            }
            after = { Map model ->
            }

            afterView = { Exception e ->
            }
        }

        api_v1_public(uri: "/api/mobile/v1/**") {
            before = {
                log.debug(">>> ${request.forwardURI}")
                log.debug(request.JSON.toString().replaceAll("\"password\":\"(.*?)\"", "\"password\":\"******\""))
            }
            after = { Map model ->

            }

            afterView = { Exception e ->
            }
        }

        api_slotwatch_v1(controller: "slotWatch") {
            before = {
                // TODO: Check user
            }
            after = { Map model ->

            }

            afterView = { Exception e ->
            }
        }

        api_v1(uri: "/api/mobile/v1/secure/**") {
            before = {
                def token = authenticationService.authenticate(authenticationService.extractBasicAuthUsername(request))

                if (!token) {
                    SecurityContextHolder.clearContext();
                    response.setStatus(401)
                    def error = [status: 401, code: Code.ACCESS_DENIED.toString(), message: "Access denied"]
                    render error as JSON
                    return false
                } else {
                    springSecurityService.reauthenticate(token.device.user.email)
                }
            }
            after = { Map model ->
            }

            afterView = { Exception e ->
            }
        }

        api_v2(uri: "/api/mobile/v2/secure/**") {
            before = {
                def token = authenticationService.authenticate(authenticationService.extractBasicAuthUsername(request))

                if (!token) {
                    SecurityContextHolder.clearContext();
                    response.setStatus(401)
                    def error = [status: 401, code: Code.ACCESS_DENIED.toString(), message: "Access denied"]
                    render error as JSON
                    return false
                } else if (authenticationService.checkAppVersion(request)) {
                    springSecurityService.reauthenticate(token.device.user.email)
                } else {
                    response.setStatus(501)
                    def error = [status: 501, code: Code.UPGRADE_APP_VERSION.toString(), message: "App version not supported"]
                    render error as JSON
                    return false
                }
            }
            after = { Map model ->
            }

            afterView = { Exception e ->
            }
        }

        idrottOnline(controller: "IOSync") {
            before = {
                def auth = request.getHeader('Authorization')

                if (auth && auth.startsWith("Basic ")) {
                    def credentials = new String((auth - "Basic ").decodeBase64()).split(":")
                    if (credentials.size() == 2
                            && credentials[0] == grailsApplication.config.matchi.io.username
                            && credentials[1] == grailsApplication.config.matchi.io.password) {
                        return true
                    } else {
                        response.setStatus(403)
                        return false
                    }
                } else {
                    response.setStatus(401)
                    return false
                }
            }
        }

        adyen(controller: "adyenPayment", action: "confirm") {
            before = {
                def auth = request.getHeader('Authorization')

                if (auth && auth.startsWith("Basic ")) {
                    def credentials = new String((auth - "Basic ").decodeBase64()).split(":")
                    if (credentials.size() == 2
                            && credentials[0] == grailsApplication.config.adyen.confirmUser
                            && credentials[1] == grailsApplication.config.adyen.confirmPassword) {
                        return true
                    } else {
                        response.setStatus(403)
                        return false
                    }
                } else {
                    response.setStatus(401)
                    return false
                }
            }
        }
    }


    boolean authenticate(HttpServletRequest request) {
        String facilityApiKey = authenticationService.extractBasicAuthUsername(request)

        if (facilityApiKey) {
            def facility = Facility.findByApikey(facilityApiKey)

            if (facility) {
                request.facilityId = facility.id
                return true
            } else {
                log.error("Could not find facility with api key ${facilityApiKey}")
            }
        }

        return false
    }


}
