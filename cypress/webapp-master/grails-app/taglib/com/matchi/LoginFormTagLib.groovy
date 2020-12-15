package com.matchi

import grails.plugin.springsecurity.SpringSecurityUtils

class LoginFormTagLib {

	def springSecurityService
    def securityService
	
	def loginForm = { attrs, body ->
		
		def config = SpringSecurityUtils.securityConfig
		
		String postUrl = securityService.getLoginPostURL()
		
		out << render(template:"/templates/loginForm", model: [postUrl: postUrl,
								   rememberMeParameter: config.rememberMe.parameter])
	}

    def loginBar = { attrs, body ->

        def returnUrl = request.forwardURI
        def loginUrl = g.createLink(action: "auth", controller: "login", params: [wl: 1])
        def config = SpringSecurityUtils.securityConfig

        def user = springSecurityService.getCurrentUser()

        String postUrl = securityService.getLoginPostURL()

        out << render(template:"/templates/loginBar", model: [postUrl: postUrl,
                rememberMeParameter: config.rememberMe.parameter, user: user, returnUrl: returnUrl, loginUrl: loginUrl])

    }
}
