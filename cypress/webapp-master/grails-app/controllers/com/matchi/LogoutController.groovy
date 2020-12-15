package com.matchi
import grails.plugin.springsecurity.SpringSecurityUtils
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder


class LogoutController {

	private static final String HEADER_PRAGMA = "Pragma";
	private static final String HEADER_EXPIRES = "Expires";
	private static final String HEADER_CACHE_CONTROL = "Cache-Control";

    def logoutHandlers
	
	 /* Index action. Redirects to the Spring security logout uri.
	 */
	def index() {
		response.setHeader(HEADER_PRAGMA, "no-cache")
		response.setDateHeader(HEADER_EXPIRES, 1L)
		response.setHeader(HEADER_CACHE_CONTROL, "no-cache")
		response.addHeader(HEADER_CACHE_CONTROL, "no-store")

        Authentication auth = SecurityContextHolder.context.authentication
        if (auth) {
            logoutHandlers.each  { handler->
                handler.logout(request,response,auth)
            }
        }

        if(params.returnUrl) {
            redirect url: params.returnUrl
        } else {
            redirect url: '/'
        }

	}
}
