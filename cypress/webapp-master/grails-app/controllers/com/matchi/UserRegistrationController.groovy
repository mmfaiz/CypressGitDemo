package com.matchi


import com.matchi.orders.Order
import grails.validation.Validateable
import org.apache.http.HttpStatus
import org.springframework.web.servlet.support.RequestContextUtils

class UserRegistrationController extends GenericController {

    static allowedMethods = [save: "POST", update: "POST", delete: "POST", edit: ["POST", "GET"]]

    public static String SESSION_KEY_AFTER_LOGIN_URL = "com.matchi.afterLoginRedirectUrl"
    public static String SESSION_KEY_FACEBOOK_FIRST_LOGIN = "com.matchi.firstTimeFacebookLogin"
    private static Integer TWO_MINUTES_IN_MILLISECONDS = 120000

    def springSecurityService
    def userService
    def facilityService
    def memberService
    def ticketService
    def customerService
    def facebookService
    def recaptchaService

    def index() {

        def facility = null

        if (params.returnUrl) {
            session[SESSION_KEY_AFTER_LOGIN_URL] = params.returnUrl
        }

        if (params.c) {
            log.info("Club code ${params.c}")
            facility = facilityService.getFacilityFromPublicRegistrationCode(params.c)
            if (facility) {
                session['facilityRegistration'] = facility.getRegistrationCode()
            }
        } else {
            session['facilityRegistration'] = null
        }

        def userInstance = new User()
        userInstance.properties = params
        return [userInstance: userInstance, facility: facility]
    }

    def save(CreateUserCommand cmd) {

        def facility = null
        def facilityRegistrationCode = session['facilityRegistration']
        if (facilityRegistrationCode) {
            facility = facilityService.getFacilityFromPublicRegistrationCode(facilityRegistrationCode)
        }

        if (cmd.hasErrors() || !recaptchaService.verifyAnswer(session, request.getRemoteAddr(), params)) {
            render(view: "index", model: [cmd: cmd, facility: facility])
            return
        }

        def userInstance = new User(params)
        if (params.acceptTerms?.toString()?.asBoolean()) userInstance.dateAgreedToTerms = new Date()

        userInstance.language = RequestContextUtils.getLocale(request).language
        userInstance = userService.registerUser(userInstance, params)

        if (!userInstance.hasErrors()) {

            if (facility) {
                def customer = customerService.getOrCreateUserCustomer(userInstance, facility)
                if (!customer.membership) {
                    memberService.addMembership(customer, null, userInstance, false, Order.ORIGIN_WEB)
                }
                recaptchaService.cleanUp(session)
                session['facilityRegistration'] = null
            }

            def parameters = [:]
            if (params.wl) {
                parameters += [wl: 1]
            }

            if (params.returnUrl) {
                parameters += [returnUrl: params.returnUrl]
            }

            redirect(action: "registrationComplete", params: parameters)
        } else {
            render(view: "index", model: [cmd: cmd, userInstance: userInstance, facility: facility])
        }
    }

    def registrationComplete() {
    }

    def enable(EnableUserCommand cmd) {
        log.info("Enabling user with activation code: ${cmd.ac}")

        def activationCode = cmd.ac

        def user = User.findByActivationcode(activationCode)

        if (user != null && activationCode) {

            if (user.password && user.password.length() > 0) {
                userService.enableUser(user, params)
                log.info("User ${user.email} successfully enabled")
                render(view: "activation", model: [success: true, userInstance: user])
            } else {
                log.info("User ${user.email} does not have a password, showing change password form")
                def enableWithPasswordCmd = new EnableUserWithPasswordCommand()
                enableWithPasswordCmd.ac = activationCode
                render(view: "change", model: [userInstance: user, cmd: enableWithPasswordCmd])
            }
        } else {
            log.info("Could not find any user with registration code ${params.ac}")
            render(view: "activation", model: [success: false, userInstance: user])
        }
    }

    def enableWithPassword(EnableUserWithPasswordCommand cmd) {
        log.info("Enabling user with password and activation code: ${cmd?.ac}")

        def user = User.findByActivationcode(cmd.ac)

        if (!user) {
            log.info("Could not find any user with registration code ${cmd.ac}")
            render(view: "activation", model: [success: false, userInstance: user])
        } else if (cmd.hasErrors()) {
            render(view: "change", model: [cmd: cmd, userInstance: user])
        } else {
            userService.enableUser(user, params, cmd.newPassword)
            render(view: "activation", model: [success: true, userInstance: user])
        }
    }

    def activation() {}

    def checkFbUser() {
        def accessToken = params.token

        def facebook = facebookService.auth(accessToken)

        if (facebook) {
            def profile = facebookService.getUserProfile(facebook)

            if (profile) {
                def user = User.findByFacebookUID(profile.id)

                if (user) {
                    render status: HttpStatus.SC_OK
                    return
                } else {
                    user = User.findByEmail(profile.email)
                    if (user) {
                        render status: HttpStatus.SC_OK
                        return
                    }
                }
            }
        }

        render status: HttpStatus.SC_NOT_FOUND
    }

    def fbConnect() {
        def ticket = params.ticket // ticket for connecting user with customer
        def accessToken = params.token  // access token received from Facebook after OAuth authorization

        def facebook = facebookService.auth(accessToken)

        if (facebook) {
            if (!facebookService.isValidProfile(facebook)) {
                log.info("Not valid Facebook profile...")
                flash.error = message(code: "userRegistration.fbConnect.noEmail")
                if (!userService.isLoggedIn()) {
                    redirect(action: "index")
                } else {
                    redirect(controller: "userProfile", action: "home")
                }
                return
            }

            def user = facebookService.getOrConnectUserByFacebookProfile(facebook)

            if (!user?.hasErrors()) {
                // New user created.
                if (!userService.isLoggedIn() && new Date().time.minus(user.dateCreated.time) <= TWO_MINUTES_IN_MILLISECONDS) {
                    session[SESSION_KEY_FACEBOOK_FIRST_LOGIN] = true

                    // Update with registering consent optional settings if creating a new user.
                    user.receiveNewsletters = params.receiveNewsletters?.toString()?.toBoolean() ?: false
                    user.receiveCustomerSurveys = params.receiveCustomerSurveys?.toString()?.toBoolean() ?: false

                    if (params.terms?.toString()?.toBoolean()) {
                        user.dateAgreedToTerms = new Date()
                    }
                    user.save(flush: true)
                }

                if (ticket) {
                    ticketService.useInviteTicket(ticket, user)
                }

                def facilityRegistrationCode = session['facilityRegistration']
                if (facilityRegistrationCode) {
                    def facility = facilityService.getFacilityFromPublicRegistrationCode(facilityRegistrationCode)
                    if (facility) {
                        def customer = customerService.getOrCreateUserCustomer(user, facility)
                        if (!customer.membership) {
                            memberService.addMembership(customer, null, user, false, Order.ORIGIN_WEB)
                        }
                        session['facilityRegistration'] = null
                    }
                }

                if (!userService.getLoggedInUser()) {
                    login(user)
                    return
                } else {
                    flash.message = message(code: "userRegistration.fbConnect.success")
                }
            }
        } else {
            flash.message = message(code: "userRegistration.fbConnect.error")
        }

        if (params.returnUrl && params.returnUrl.size() > 0) {
            redirect(url: params.returnUrl)
            return
        }

        redirect(controller: "userProfile", action: "home")
    }

    def login(user) {

        if (userService.logIn(user)) {
            if (session[SESSION_KEY_FACEBOOK_FIRST_LOGIN]) {
                redirect(controller: 'userProfile', action: "passwordForFb")
                session[SESSION_KEY_FACEBOOK_FIRST_LOGIN] = null
            } else {
                redirect(controller: "loginSuccess", action: "index")
            }
        } else {
            flash.error = message(code: "userRegistration.login.error")
            redirect(controller: "login", action: "authfail")
        }

    }
}

@Validateable(nullable = true)
class CreateUserCommand {
    String firstname
    String lastname
    String email
    String telephone
    String password
    String password2

    static constraints = {
        firstname(blank: false, markup: true, validator: { firstname, obj ->
            ValidationUtils.validateUserNameLength(firstname)
        })
        lastname(blank: false, markup: true, validator: { lastname, obj ->
            ValidationUtils.validateUserNameLength(lastname)
        })
        telephone(markup: true)
        email(blank: false, email: true, validator: { email, obj ->
            def user = User.findByEmail(email)
            user ? ['invalid.emailnotunique'] : true
        })
        password2(blank: false)
        password(blank: false, nullable: false, validator: { password, obj ->
            def password2 = obj.properties['password2']
            password2 == password ? ValidationUtils.validateUserPassword(password) :
                    ['invalid.matchingpasswords']
        })
    }
}

@Validateable(nullable = true)
class EnableUserCommand {
    String ac

    static constraints = {
        ac(nullable: true)
    }
}

@Validateable(nullable = true)
class EnableUserWithPasswordCommand extends EnableUserCommand {
    String newPassword
    String newPasswordConfirm

    static constraints = {
        newPassword(blank: false)
        newPasswordConfirm(blank: false, nullable: false, validator: { password, obj ->
            def password2 = obj.properties['newPassword']
            password2 == password ? ValidationUtils.validateUserPassword(password) :
                    ['invalid.matchingpasswords']
        })
    }
}
