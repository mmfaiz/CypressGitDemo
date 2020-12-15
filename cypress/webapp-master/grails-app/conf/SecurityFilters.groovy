import com.matchi.FacilityUserRole
import javax.servlet.http.HttpServletResponse
import grails.plugin.springsecurity.SpringSecurityUtils

class SecurityFilters {

    def securityService

    def filters = {
        facilityAccess(uri: "/facility/**") {
            before = {
                if (SpringSecurityUtils.ifAnyGranted("ROLE_ADMIN")) {
                    return true
                } else if (SpringSecurityUtils.ifAnyGranted("ROLE_USER")) {
                    if (controllerName == "facilityAdministration" && actionName == "switchFacility") {
                        return true
                    }
                    def accessRights = FacilityUserRole.AccessRight.byController(controllerName)
                    accessRights << FacilityUserRole.AccessRight.FACILITY_ADMIN
                    if (securityService.hasFacilityAccessRights(accessRights)) {
                        return true
                    }
                }

                response.sendError HttpServletResponse.SC_FORBIDDEN
                return false
            }
        }
    }
}
