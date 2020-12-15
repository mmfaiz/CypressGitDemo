package com.matchi.api


import com.matchi.AppleIDToken
import com.matchi.devices.Token
import grails.converters.JSON
import grails.validation.Validateable

class AuthenticationController extends GenericAPIController {

    def authenticationService
    def facebookService
    def appleIDService
    def userService

    static allowedMethods = [auth: ['POST']]

    def auth() {
        AuthenticationCommand cmd = new AuthenticationCommand()
        bindData(cmd, request?.JSON)

        if (!cmd.validate()) {
            throw new InputErrorAPIException(cmd.errors)
        }

        def token = null
        def result = [:]

        // validate user credentials
        def user = authenticationService.authenticateCredentials(cmd.email, cmd.password)

        if (user) {
            // provision user device and generate token
            token = authenticationService.provision(user, cmd.deviceId, cmd.deviceModel, cmd.deviceDescription)
        } else {
            error(401, Code.BAD_CREDENTIALS, "Wrong username or password")
        }

        if (token) {
            result.token = token.identifier
            result.new = Boolean.FALSE

            render result as JSON
        } else {
            error(401, Code.BAD_CREDENTIALS, "Unable to provision user")
        }

    }

    // https://www.facebook.com/dialog/oauth?client_id=363136690366152&redirect_uri=http://localhost:8080/api/mobile/v1/authenticateCredentials/fb&scope=read_stream&response_type=token
    def authFacebook() {
        FacebookAuthenticationCommand cmd = new FacebookAuthenticationCommand()
        bindData(cmd, request?.JSON)

        if (!cmd.validate()) {
            throw new InputErrorAPIException(cmd.errors)
        }

        def facebook = facebookService.auth(cmd.oauthToken)

        if (!facebook) {
            error(401, Code.BAD_CREDENTIALS, "Unable to authenticate with Facebook")
        } else {

            def user = facebookService.getOrConnectUserByFacebookProfile(facebook)
            def token = authenticationService.provision(user, cmd.deviceId, cmd.deviceModel, cmd.deviceDescription)

            if (token) {
                def result = [:]
                result.token = token.identifier
                result.new = Boolean.FALSE

                render result as JSON
            } else {
                error(401, Code.BAD_CREDENTIALS, "Unable to authenticate with Facebook")
            }
        }
    }

    def authAppleID() {
        AppleIDAuthenticationCommand cmd = new AppleIDAuthenticationCommand()
        bindData(cmd, request?.JSON)

        if (!cmd.validate()) {
            throw new InputErrorAPIException(cmd.errors)
        }

        AppleIDToken appleID = appleIDService.appleAuth(cmd.authToken)

        if (!appleID) {
            error(401, Code.BAD_CREDENTIALS, "Unable to authenticate with Apple ID")
        } else {
            def user = appleIDService.getOrConnectUserByAppleIDProfile(appleID, cmd)
            def token = authenticationService.provision(user, cmd.deviceId, cmd.deviceModel, cmd.deviceDescription)
            if (token) {
                def result = [:]
                result.token = token.identifier
                result.new = Boolean.FALSE

                render result as JSON
            } else {
                error(401, Code.BAD_CREDENTIALS, "Unable to create token from Apple ID")
            }
        }
    }

    def authSession() {
        AuthSessionCommand cmd = new AuthSessionCommand()
        bindData(cmd, request?.JSON)

        if (!cmd.validate()) {
            throw new InputErrorAPIException(cmd.errors)
        }

        // validate user credentials
        def user = authenticationService.authenticateCredentials(cmd.email, cmd.password)

        if (user) {
            userService.logIn(user)
            def r = [status: 200, message: "Authentication successful", session: session.id]
            render r as JSON
        } else {
            error(401, Code.BAD_CREDENTIALS, "Unable to authenticate email and password")
        }
    }

    def authFacebookSession() {
        FacebookSessionCommand cmd = new FacebookSessionCommand()
        bindData(cmd, request?.JSON)

        if (!cmd.validate()) {
            throw new InputErrorAPIException(cmd.errors)
        }

        def facebook = facebookService.auth(cmd.oauthToken)

        if (!facebook) {
            error(401, Code.BAD_CREDENTIALS, "Unable to authenticate with Facebook")
        } else {

            def user = facebookService.getOrConnectUserByFacebookProfile(facebook)

            if (user) {
                userService.logIn(user)
                def r = [status: 200, message: "Authentication successful", session: session.id]
                render r as JSON
            } else {
                error(401, Code.BAD_CREDENTIALS, "Unable to authenticate with Facebook")
            }
        }
    }

    def authSessionWithToken() {
        // validate user credentials
        Token token = authenticationService.authenticate(authenticationService.extractBasicAuthUsername(request))

        if (token) {
            userService.logIn(token.device.user)
            def r = [status: 200, message: "Authentication successful", session: session.id]
            render r as JSON
        } else {
            error(401, Code.BAD_CREDENTIALS, "Unable to authenticate token")
        }
    }
}

@Validateable(nullable = true)
class AuthenticationCommand {
    String email
    String password
    String deviceId
    String deviceModel
    String deviceDescription

    static constraints = {
        email blank: false
        password blank: false
        deviceId blank: false
        deviceModel blank: false
        deviceDescription blank: false
    }
}

@Validateable(nullable = true)
class FacebookAuthenticationCommand {
    String oauthToken
    String deviceId
    String deviceModel
    String deviceDescription

    static constraints = {
        oauthToken blank: false, nullable: false
        deviceId blank: false, nullable: false
        deviceModel blank: false, nullable: false
        deviceDescription blank: false, nullable: false
    }
}

@Validateable(nullable = true)
class AppleIDAuthenticationCommand {
    String authToken
    String deviceId
    String deviceModel
    String deviceDescription

    String firstName
    String lastName

    String email

    static constraints = {
        authToken blank: false, nullable: false
        deviceId blank: false, nullable: false
        deviceModel blank: false, nullable: false
        deviceDescription blank: false, nullable: false

        firstName nullable: true
        lastName nullable: true

        email nullable: true
    }
}

@Validateable(nullable = true)
class FacebookSessionCommand {
    String oauthToken

    static constraints = {
        oauthToken blank: false, nullable: false
    }
}

@Validateable(nullable = true)
class AuthSessionCommand {
    String email
    String password
    boolean rememberMe = false

    static constraints = {
        email blank: false, nullable: false
        password blank: false, nullable: false
        rememberMe blank: true, nullable: true
    }
}
