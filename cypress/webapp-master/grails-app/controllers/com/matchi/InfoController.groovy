package com.matchi

import grails.plugin.springsecurity.SpringSecurityUtils
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.servlet.support.RequestContextUtils

import javax.servlet.http.HttpSession

class InfoController extends GenericController {

    def ticketService
    def userService
    def customerService
    def authenticationManager

    def index() {
        def header  = message(code: "default.success")
        def message = ""
        def title   = "Meddelande"

        if(flash.message) {
            message = flash.message
        } else if(flash.error) {
            header  = "Oops!"
            title   = "Ett fel har uppstått"
            message = flash.error
        }
        if (params.header) {
            header = params.header
        }
        if (params.title) {
            title = params.title
        }

        flash.message = null
        flash.error   = null

        [ message:message, title:title, header:header ]
    }


    // Landing page for facility invite
    def invite() {
        def ticket = params.ticket

        if (ticket && !ticketService.isInviteTicketValid(ticket)) {
            flash.error = message(code: "info.invite.error")
            redirect(controller: "info", params: [ title: "Kunde inte acceptera inbjudan"])
            return
        }

        def inviteTicket = CustomerInviteTicket.findByKey(ticket)
        def invitedCustomer = inviteTicket?.customer
        def currentUser = getCurrentUser()

        if (currentUser) {
            def userMessage = ""

            if (!currentUser.isCustomerIn(invitedCustomer.facility)) {
                ticketService.useInviteTicket(ticket, currentUser)
                userMessage = message(code: "info.invite.accepted", args: [invitedCustomer?.facility])
            } else {
                ticketService.consumeInviteTicket(inviteTicket)
                userMessage = message(code: "info.invite.alreadyCustomer", args: [invitedCustomer?.facility])
            }

            redirect(controller: "loginSuccess", params: [ message: userMessage ])
            return
        }

        def config = SpringSecurityUtils.securityConfig
        params.wl = invitedCustomer?.facility?.getFacilityPropertyValue(FacilityProperty.FacilityPropertyKey.FEATURE_WHITE_LABEL.toString())?.toBoolean()?1:""
        params.returnUrl = invitedCustomer?.facility?.getFacilityPropertyValue(FacilityProperty.FacilityPropertyKey.WHITE_LABEL_EXT_URL.toString())
        params.f  = invitedCustomer?.facility?.id

        [ ticket: ticket, invitedCustomer: invitedCustomer, rememberMeParameter: config.rememberMe.parameter ]
    }


    // Login from invite-page is intercepted here to handle the invite-ticket
    def login() {
        def invitedCustomer = CustomerInviteTicket.findByKey(params.ticket)?.customer

        try {
            UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(params.j_username, params.j_password);

            // Authenticate the user
            Authentication authentication = authenticationManager.authenticate(authRequest);
            SecurityContext securityContext = SecurityContextHolder.getContext();
            securityContext.setAuthentication(authentication);

            // Create a new session and add the security context
            HttpSession session = request.getSession(true);
            session.setAttribute("SPRING_SECURITY_CONTEXT", securityContext);

            if (authentication?.authenticated) {
                redirect(action: "invite", params: [ticket: params.ticket])
                return
            }

        } catch (Exception e) {
            flash.error = message(code: "login.authfail.invalidCredentials")
        }

        if(!invitedCustomer.isAttached()){
            invitedCustomer.attach()
        }

        def config = SpringSecurityUtils.securityConfig

        render(view: "invite", model: [ ticket: params.ticket, invitedCustomer: invitedCustomer, rememberMeParameter: config.rememberMe.parameter ])
    }

    // Login from invite-page is intercepted here to handle the invite-ticket
    def register(CreateUserCommand cmd) {
        def invitedCustomer = CustomerInviteTicket.findByKey(params.ticket)?.customer

        if(cmd.hasErrors()) {
            render(view: "invite", model: [ cmd: cmd, invitedCustomer: invitedCustomer, ticket: params.ticket ])
            return
        }

        def user = new User(params)
        if(params.acceptTerms?.toString()?.asBoolean()) user.dateAgreedToTerms = new Date()

        user.language = RequestContextUtils.getLocale(request).language
        user = userService.registerUser(user, params)

        if (!user.hasErrors()) {
            ticketService.useInviteTicket(params.ticket, user)

            def parameters = [:]
            if (params.wl) { parameters += [wl: params.wl] }
            if (params.returlUrl) { parameters += [returlUrl: params.returlUrl] }

            flash.message = message(code: "info.register.success", args: [invitedCustomer.facility])
            redirect(controller: "userRegistration", action: "registrationComplete", params: parameters)
        }
        else {
            render(view: "invite", model: [cmd: cmd, ticket: params.ticket])
        }
    }
}
