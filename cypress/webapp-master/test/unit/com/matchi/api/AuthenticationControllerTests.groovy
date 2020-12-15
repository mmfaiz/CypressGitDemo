package com.matchi.api

import com.matchi.AuthenticationService
import com.matchi.devices.Token
import grails.test.mixin.*
import org.junit.Before

@TestFor(AuthenticationController)
class AuthenticationControllerTests {

    static def validDeviceId = "token"
    static def authToken = "secret-auth-token"

    @Before
    void setUp() {
        def authenticationServiceMock = mockFor(AuthenticationService)

        authenticationServiceMock.demand.authenticateCredentials { def email, def password ->
            if("secret".equals(password)) return "secret-token"
            return null
        }

        authenticationServiceMock.demand.provision { def user, def deviceId, deviceModel, def deviceDescription  ->
            if(user != null && validDeviceId.equals(deviceId)) {
                return new Token(identifier: authToken)
            }
            return null
        }

        controller.authenticationService = authenticationServiceMock.createMock()
    }

    void testAuthAppleID() {
        request.method = "POST"
        request.JSON = "{}"

        shouldFail(InputErrorAPIException) {
            controller.authAppleID()
        }
    }

    void testValidJSON() {
        request.method = "POST"
        request.JSON = validJSON
        controller.auth()

        assert response.json.token == authToken
    }

    void testWrongCredentials() {
        request.method = "POST"
        request.JSON = validJSONWrongCredentials
        controller.auth()

        assert response.json.token == null
    }

    void testNoJson() {
        request.method = "POST"
        request.JSON = "{}"
        controller.auth()

        assert response.status == 401
    }

    // valid authenticateCredentials json
    static def validJSON = """
        {
            email: "calle@matchi.se",
            password: "secret",
            deviceId: "token",
            deviceModel: "model",
            deviceDescription: "iOS 6.0.1",
        }
        """

    // valid authenticateCredentials json (wrong credentials)
    static def validJSONWrongCredentials = """
        {
            email: "calle@matchi.se",
            password: "wrongpassword",
            deviceId: "token",
            deviceModel: "model",
            deviceDescription: "iOS 6.0.1",
        }
        """

    // password missing
    static def invalidJSON = """
        {
            email: "calle@matchi.se",
            deviceToken: "phone-666",
            deviceDescription: "iOS 6.0.1",
        }
        """
}
