package com.matchi

import org.springframework.security.web.savedrequest.HttpSessionRequestCache

/**
 * Controller that handles redirects after successfull login
 */
class LoginSuccessController extends GenericController {

    def userService

    def index() {
        if (springSecurityService.isLoggedIn()) {

            def user = getCurrentUser()
            userService.updateLastLoggedInDate(user)

            // facility admins/users are always redirected to their clubs
            if (user.facility) {
                def controller = defaultFacilityController()
                if (controller) {
                    redirect(controller: defaultFacilityController())
                    return
                }
                log.error("Facility Admin user ${user.fullName()} [${user.id}] is missing access rights, redirecting")
                // else continue as a regular user
            }

            // check for login return url
            def afterLoginRedirectUrl = session[LoginController.SESSION_KEY_AFTER_LOGIN_URL]
            if (afterLoginRedirectUrl) {
                session[LoginController.SESSION_KEY_AFTER_LOGIN_URL] = null // reset
                redirect(uri: afterLoginRedirectUrl)
                return
            }

            // check for saved unauthorized request
            def savedRequest = session[HttpSessionRequestCache.SAVED_REQUEST]
            if (savedRequest) {
                redirect url: savedRequest.getRedirectUrl()
                return
            }

            // default redirect
            forward(action: 'successRedirect')

        } else {
            log.info "User not logged in, redirecting home"
            redirect(controller: "home")
        }
    }

    def successRedirect() {
        def user = getCurrentUser()
        if (user.isInRole("ROLE_ADMIN")) {
            // Admin
            log.info "Redirecting to admin home"
            redirect(controller: "home")
        } else if (user.isInRole("ROLE_USER")) {
            // User
            log.info "Redirecting to default"
            if (params.message) {
                flash.message = params.message
            }

            if (params.returnUrl) {
                redirect(url: params.returnUrl)
            } else {
                redirect(controller: "userProfile", action: "home")
            }
        }
    }
}
