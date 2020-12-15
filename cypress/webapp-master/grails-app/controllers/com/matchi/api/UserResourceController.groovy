package com.matchi.api
import com.matchi.UpdateUserProfileCommand
import com.matchi.User
import com.matchi.ValidationUtils
import grails.converters.JSON
import grails.plugin.asyncmail.Validator
import grails.validation.Validateable

class UserResourceController extends GenericAPIController {

    static allowedMethods = [register: ['POST']]

    def userService
    def dateUtil

    Object renderJson(Object o) {
        render o as JSON
    }

    def current() {
        renderJson(getCurrentUser())
    }

    def register() {
        def command = new RegisterUserCommand()
        bindData(command, request?.JSON)

        if(!command.validate()) {
            error(400, Code.INPUT_ERROR, "Can't register user due to errors: ${userService.userErrorsToString(command.errors)}")
            return
        }

        User user = command.toUser()

        if(userService.isUserExist(user.email)) {
            error(409, Code.USER_EXISTS, "User already exists")
            return
        }

        def result = [:]
        user = userService.registerUser(user, null, false)

        if (user.hasErrors()) {
            error(409, Code.INPUT_ERROR, "Can't register user due to errors: ${userService.userErrorsToString(user.errors)}")
            return
        }

        result.result = "success"
        result.id     = user.id

        response.setStatus(201, "Created")
        renderJson(result)
    }

    def update() {
        User user = getCurrentUser()
        UserCommand cmd = new UserCommand()
        bindData(cmd, request?.JSON)

        if(!cmd.validate()) {
            throw new InputErrorAPIException(cmd.errors)
        }

        bindData(user, cmd.properties.findAll {it.value != null})

        if (cmd.agreedToTerms && !user.dateAgreedToTerms) {
            user.dateAgreedToTerms = new Date()
        }

        if(user.save()) {
            response.setStatus(201, "Updated")
            renderJson(user)
        } else {
            error(400, Code.INPUT_ERROR, command.errors.toString())
        }
    }
}

@Validateable(nullable = true)
class UserCommand {
    String firstname
    String lastname
    String city

    Boolean agreedToTerms
    Boolean receiveNewsletters = false
    Boolean receiveCustomerSurveys = false

    static constraints = {
        agreedToTerms nullable: true
        receiveNewsletters nullable: true
        receiveCustomerSurveys nullable: true
        firstname nullable: true
        lastname nullable: true
        city blank: true
    }

    User toUser() {
        User user = new User()
        user.firstname = firstname
        user.lastname = lastname
        user.city = city
        user.dateAgreedToTerms = agreedToTerms ? new Date() : null
        user.receiveNewsletters = receiveNewsletters
        user.receiveCustomerSurveys = receiveCustomerSurveys

        return user
    }
}

@Validateable(nullable = true)
class RegisterUserCommand  extends UserCommand {
    String email
    String password

    static constraints = {
        email nullable: false, blank: false, validator: { val ->
            return Validator.isMailbox(val)
        }
        password blank: false, validator: {
            it ? ValidationUtils.validateUserPassword(it) : [ValidationUtils.PASSWORD_MIN_LENGTH_ERROR]
        }
        firstname nullable: false, blank: false
        lastname nullable: false, blank: false
    }

    User toUser() {
        User user = super.toUser()
        user.email = email
        user.password = password

        return user
    }
}
