package com.matchi

import grails.converters.JSON
import grails.plugin.springsecurity.SpringSecurityUtils
import org.springframework.security.authentication.AccountExpiredException
import org.springframework.security.authentication.CredentialsExpiredException
import org.springframework.security.authentication.DisabledException
import org.springframework.security.authentication.LockedException
import org.springframework.security.core.context.SecurityContextHolder as SCH
import org.springframework.security.web.WebAttributes

import javax.servlet.http.HttpServletResponse

class LoginController extends GenericController {

    static layout = 'main'

    public static String SESSION_KEY_AFTER_LOGIN_URL = "com.matchi.afterLoginRedirectUrl"
    public static String SESSION_KEY_LOGIN_FORM_URL = "com.matchi.loginFormRedirectUrl"

    /**
     * Dependency injection for the authenticationTrustResolver.
     */
    def authenticationTrustResolver

    /**
     * Dependency injection for the springSecurityService.
     */
    def springSecurityService

    def userService
    def securityService

    /**
     * Default action; redirects to 'defaultTargetUrl' if logged in, /login/authenticateCredentials otherwise.
     */
    def index() {
        if (springSecurityService.isLoggedIn()) {
            redirect uri: SpringSecurityUtils.securityConfig.successHandler.defaultTargetUrl
        } else {
            redirect action: "auth", params: params
        }
    }

    /**
     * Show the login page.
     */
    def auth() {
        def config = SpringSecurityUtils.securityConfig

        if (params.returnUrl) {
            session[SESSION_KEY_AFTER_LOGIN_URL] = params.returnUrl
        } else {
            session[SESSION_KEY_AFTER_LOGIN_URL] = null
        }

        // in case of failed authentication, return to this url
        session[SESSION_KEY_LOGIN_FORM_URL] = request.forwardURI + "?" +
                params.findAll {
                    return it.key instanceof String && it.value instanceof String
                }.collect {
                    URLEncoder.encode(it.key, "UTF-8") + "=" + URLEncoder.encode(it.value, "UTF-8")
                }.join('&')
        if (springSecurityService.isLoggedIn()) {
            forward(controller: 'loginSuccess', action: 'successRedirect')
        }

        String postUrl = "${request.contextPath}${config.apf.filterProcessesUrl}"

        render view: 'auth', model: [postUrl            : postUrl,//securityService.getLoginPostURL(),
                                     rememberMeParameter: config.rememberMe.parameter]
    }

    /**
     * The redirect action for Ajax requests.
     */
    def authAjax() {
        session.SPRING_SECURITY_SAVED_REQUEST_KEY = null
        response.setHeader 'Location', SpringSecurityUtils.securityConfig.auth.ajaxLoginFormUrl
        response.sendError HttpServletResponse.SC_UNAUTHORIZED
    }

    /**
     * Show denied page.
     */
    def denied() {
        if (springSecurityService.isLoggedIn() &&
                authenticationTrustResolver.isRememberMe(SCH.context?.authentication)) {
            // have cookie but the page is guarded with IS_AUTHENTICATED_FULLY
            redirect action: full, params: params
        }
    }

    /**
     * Login page for users with a remember-me cookie but accessing a IS_AUTHENTICATED_FULLY page.
     */
    def full() {
        def config = SpringSecurityUtils.securityConfig
        render view: 'auth', params: params,
                model: [hasCookie: authenticationTrustResolver.isRememberMe(SCH.context?.authentication),
                        postUrl  : securityService.getLoginPostURL()]
    }

    /**
     * Callback after a failed login. Redirects to the authenticateCredentials page with a warning message.
     */
    def authfail() {

        String msg = ''
        def exception = session[WebAttributes.AUTHENTICATION_EXCEPTION]
        if (exception) {
            if (exception instanceof AccountExpiredException) {
                msg = message(code: "login.authfail.accountExpired")
            } else if (exception instanceof CredentialsExpiredException) {
                msg = message(code: "login.authfail.credentialsExpired")
            } else if (exception instanceof DisabledException) {
                msg = message(code: "login.authfail.disabled")
            } else if (exception instanceof LockedException) {
                msg = message(code: "login.authfail.locked")
            } else {
                msg = message(code: "login.authfail.invalidCredentials")
            }
            log.warn("Unable to login user: ${exception.message}")
        }

        if (springSecurityService.isAjax(request)) {
            render([error: msg] as JSON)
        } else {
            flash.error = msg

            def loginFormUrl = session[SESSION_KEY_LOGIN_FORM_URL]
            if (loginFormUrl) {
                session[SESSION_KEY_LOGIN_FORM_URL] = null // reset
                redirect(uri: loginFormUrl)
                return
            }

            redirect action: "auth", params: params
        }
    }

    /**
     * The Ajax success redirect url.
     */
    def ajaxSuccess() {
        render([success: true, username: springSecurityService.authentication.name] as JSON)
    }

    /**
     * The Ajax denied redirect url.
     */
    def ajaxDenied() {
        render([error: 'access denied'] as JSON)
    }
}
