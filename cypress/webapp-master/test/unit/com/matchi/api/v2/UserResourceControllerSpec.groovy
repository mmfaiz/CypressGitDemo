package com.matchi.api.v2

import com.matchi.*
import com.matchi.api.InputErrorAPIException
import grails.converters.JSON
import grails.plugin.springsecurity.SpringSecurityService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification

import static com.matchi.TestUtils.*

/**
 * @author Sergei Shushkevich
 */
@TestFor(UserResourceController)
@Mock([Facility, Municipality, Region, User])
class UserResourceControllerSpec extends Specification {

    def userService = Mock(UserService)
    def securityService = Mock(SpringSecurityService)

    def setup() {
        controller.userService = userService
        controller.springSecurityService = securityService

        JSON.createNamedConfig( 'apiV2' ) {
            com.matchi.marshallers.v2.UserMarshaller.register(it)
        }
    }

    void "add favorite responds with 201 code"() {
        def facility = createFacility()

        when:
        request.JSON = '{"facilityId": ' + facility.id + '}'
        controller.addFavorite()

        then:
        1 * securityService.getCurrentUser() >> new User()
        1 * userService.addFavorite(_, _) >> new UserFavorite()
        response.status == 201
    }

    void "add favorite throws exception in case of invalid input"() {
        when:
        request.JSON = '{}'
        controller.addFavorite()

        then:
        thrown InputErrorAPIException
    }

    void "add favorite responds with 404 code if facility id is invalid"() {
        when:
        request.JSON = '{"facilityId": 12345}'
        controller.addFavorite()

        then:
        response.status == 404
    }

    void "remove favorite responds with 204 code"() {
        def facility = createFacility()

        when:
        request.JSON = '{"facilityId": ' + facility.id + '}'
        controller.removeFavorite()

        then:
        1 * securityService.getCurrentUser() >> new User()
        1 * userService.removeFavorite(_, _) >> true
        response.status == 204

        when: "if favorite doesn't exist"
        response.reset()
        controller.removeFavorite()

        then: "404 code is returned"
        1 * securityService.getCurrentUser() >> new User()
        1 * userService.removeFavorite(_, _) >> false
        response.status == 404
    }

    void "remove favorite throws exception in case of invalid input"() {
        when:
        request.JSON = '{}'
        controller.removeFavorite()

        then:
        thrown InputErrorAPIException
    }

    void "remove favorite responds with 404 code if facility id is invalid"() {
        when:
        request.JSON = '{"facilityId": 12345}'
        controller.removeFavorite()

        then:
        response.status == 404
    }

    void "test command object validation empty"() {
        when:
        RegisterUserCommand registerUserCommand = new RegisterUserCommand()

        then:
        !registerUserCommand.validate()
    }

    void "test command object validation all requirements met"() {
        when:
        RegisterUserCommand registerUserCommand = new RegisterUserCommand()

        registerUserCommand.firstname = "Sune"
        registerUserCommand.lastname = "Andersson"
        registerUserCommand.email = "sune@matchi.se"
        registerUserCommand.password = "password"

        then:
        registerUserCommand.validate()
    }
}