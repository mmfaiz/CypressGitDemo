package com.matchi.api

import com.matchi.User
import com.matchi.UserService
import grails.test.mixin.*
import org.junit.*
import org.springframework.validation.Errors

/**
 * See the API for {@link grails.test.mixin.web.ControllerUnitTestMixin} for usage instructions
 */
@TestFor(UserResourceController)
@Mock([User, UserService])
class UserResourceControllerTests {

    def userServiceMock = mockFor(UserService)

    @Before
    void setUp() {


        new User(email: "exists@matchi.se").save(validate: false)


        controller.userService = userServiceMock.createMock()

    }

    void testValidRegistrationReturns201() {
        request.method = "POST"
        request.JSON = validJSON

        userServiceMock.demand.isUserExist { String userEmail ->
            return Boolean.FALSE
        }
        userServiceMock.demand.registerUser { def user, def params, boolean activate ->
            def u = new User()
            u.id = 102
            return u
        }
        controller.register()

        assert response.status == 201 // Created
        assert response.json.result == "success"
        assert response.json.id == 102 // id of user
    }

    void testValidRegistrationFailedInTheMiddle() {
        request.method = "POST"
        request.JSON = validJSON

        userServiceMock.demand.isUserExist { String userEmail ->
            return Boolean.FALSE
        }
        userServiceMock.demand.registerUser { def user, def params, boolean activate ->
            def u = new User()
            u.id = 102
            u.errors.reject("Test error to save")
            return u
        }
        userServiceMock.demand.userErrorsToString {Errors errors ->
            return "Test errors"
        }

        controller.register()

        assert response.status == 409 // Created
    }

    void testValidRegistrationButUserExists() {
        userServiceMock.demand.isUserExist { String userEmail ->
            return Boolean.TRUE
        }

        request.method = "POST"
        request.JSON = validJSONUserExists
        controller.register()

        assert response.status == 409 // Conflict
    }

    void testInvalidJSONNoPwd() {
        request.JSON = invalidJSONNoPwd

        controller.register()
        assert response.status == 405
    }

    void testInvalidJSONNoEmail() {
        request.JSON = invalidJSONNoEmail

        controller.register()
        assert response.status == 405
    }

    void testRegister() {
        request.method = "POST"
        request.JSON = validJSON
        userServiceMock.demand.isUserExist { String userEmail ->
            return Boolean.FALSE
        }
        userServiceMock.demand.registerUser { def user, def params, boolean activate ->
            def u = new User()
            u.id = 102
            return u
        }

        controller.register()

        assert response.status == 201 // Created
        assert response.json.result == "success"
        assert response.json.id == 102 // id of user
    }

    // valid registration json
    static def validJSON = """
    {
        email: "user@matchi.se",
        password: "your-password",
        firstname: "John",
        lastname: "Doe",
        city: "Gothenburg"
    }
    """

    // valid registration json
    static def validJSONUserExists = """
    {
        email: "exists@matchi.se",
        password: "your-password",
        firstname: "John",
        lastname: "Doe",
        city: "Gothenburg"
    }
    """

    // invalid registration json (no password)
    static def invalidJSONNoPwd = """
    {
        email: "exists@matchi.se",
        password: "",
        firstname: "John",
        lastname: "Doe",
        city: "Gothenburg"
    }
    """

    // invalid registration json (no password)
    static def invalidJSONNoEmail = """
    {
        password: "secret",
        firstname: "John",
        lastname: "Doe",
        city: "Gothenburg"
    }
    """


}
