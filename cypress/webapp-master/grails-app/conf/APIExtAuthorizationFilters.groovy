import com.matchi.User
import com.matchi.api.Code
import grails.converters.JSON

/**
 * Filter for authenticating API calls from other system using system user
 * before proceeding to authenticate end user.
 */
class APIExtAuthorizationFilters {
    // Add controller/action that should be excluded from audit logging.
    private static final List excludedUri = new ArrayList()
    static {
        excludedUri.add("APIExtAuth/auth")
        excludedUri.add("APIExtHealth/health")
    }

    def authenticationService
    def filters = {

        apiExt(uri: "/api/ext/v1/**") {
            before = {
                if (!isExcludedUri(controllerName + "/" + actionName)) {
                    log.info("API-EXT: requestid: ${request.getHeader("requestid")}, action: ${controllerName}/${actionName}")
                }

                def credentials = extractCredentials(request)
                if (!credentials) {
                    log.warn("Authentication failed, no credentials.")
                    response.setStatus(400)
                    def error = [status: 400, code: Code.ACCESS_DENIED.toString(), message: "No Credentials"]
                    render error as JSON
                    return false
                }

                def user = authenticationService.authenticateCredentials(credentials[0], credentials[1])
                if(!user) {
                    log.warn("Authentication failed for ${credentials[0]}, wrong user or password.")
                    response.setStatus(400)
                    def error = [status: 400, code: Code.UNKNOWN_ERROR.toString(), message: "Access denied"]
                    render error as JSON
                    return false
                }

                if (!getUserRoles(user).contains("ROLE_ADMIN")) {
                    log.warn("Authentication failed for ${credentials[0]}, missing role.")
                    response.setStatus(400)
                    def error = [status: 400, code: Code.UNKNOWN_ERROR.toString(), message: "Not authorized"]
                    render error as JSON
                    return false
                }

                log.debug("Authentication success for ${credentials[0]}.")
            }
            after = { Map model ->
            }

            afterView = { Exception e ->
            }
        }

    }

    def extractCredentials(def request) {
        def authString = request.getHeader('Authorization')
        def credentials = null

        if (authString) {
            def encodedPair = authString - 'Basic '
            def decodedPair = new String(encodedPair.decodeBase64())
            credentials = decodedPair.split(':')
        } else {
            log.info("No HTTP Basic header found")
        }

        credentials
    }

    List<String> getUserRoles(User user) {
        List<String> roles = new ArrayList<>()
        if (user != null) {
            user.getRoles().each {
                roles.add(it.role.authority)
            }
        }
        return roles
    }

    private static boolean isExcludedUri(String uri) {
        if (excludedUri.contains(uri)) {
            return true
        }
        return false
    }

}
