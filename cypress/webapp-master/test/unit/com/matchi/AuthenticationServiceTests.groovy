package com.matchi

import grails.test.mixin.*
import grails.test.mixin.domain.DomainClassUnitTestMixin
import org.apache.tools.ant.util.Base64Converter
import org.codehaus.groovy.grails.plugins.testing.GrailsMockHttpServletRequest
import org.junit.*
import org.springframework.http.HttpHeaders

/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@TestFor(AuthenticationService)
@TestMixin(DomainClassUnitTestMixin)
@Mock([User])
class AuthenticationServiceTests {
    private String userEmail = "user@matchi.se"

    @Before
    void setUp() {

        new User(email: userEmail, password: "secret").save(validate: false)
        def userServiceMock = mockFor(UserService)

        userServiceMock.demand.checkPassword { User user, String password ->
            return password.equals("secret")
        }

        service.userService = userServiceMock.createMock()
    }

    void testReturnsUserIfCredentialsIsCorrect() {
        assert service.authenticateCredentials("user@matchi.se", "secret") != null
    }

    void testReturnNullIfEmailPasswordIsNull() {
        assert service.authenticateCredentials(null, null) == null
    }

    void testReturnsNoUserIfInvalidCredentials() {
        assert service.authenticateCredentials("user@matchi.se", "wrongpassword") == null
    }

    void testReturnsNullIfUserNotFound() {
        assert service.authenticateCredentials("unknown@matchi.se", "secret") == null
    }

    void testExtractBasicAuthUsernameOnNull() {
        shouldFail(NullPointerException) {
            service.extractBasicAuthUsername(null)
        }
    }
    
    void testExtractBasicAuthUsernameOnNoAuthorization() {
        GrailsMockHttpServletRequest mockHttpServletRequest = new GrailsMockHttpServletRequest()

        assert !mockHttpServletRequest.getHeader('Authorization')
        assert !service.extractBasicAuthUsername(mockHttpServletRequest)
    }

    void testExtractBasicAuthUsernameJustWeirdHeader() {
        GrailsMockHttpServletRequest mockHttpServletRequest = new GrailsMockHttpServletRequest()
        HttpHeaders httpHeaders = new HttpHeaders()
        httpHeaders.set('Authorization', 'hello')
        mockHttpServletRequest.setRequestHeaders(httpHeaders)

        assert !service.extractBasicAuthUsername(mockHttpServletRequest)
    }

    void testExtractBasicAuthUsernameJustBasic() {
        GrailsMockHttpServletRequest mockHttpServletRequest = new GrailsMockHttpServletRequest()

        mockHttpServletRequest.addHeader('Authorization', 'Basic ')
        assert mockHttpServletRequest.getHeader('Authorization') == "Basic "

        assert !service.extractBasicAuthUsername(mockHttpServletRequest)
    }

    void testExtractBasicAuthUsernameJustEncodedUsername() {
        GrailsMockHttpServletRequest mockHttpServletRequest = new GrailsMockHttpServletRequest()

        String username = "Sune"

        // Ordinary encodeAsBase64() does not seem to work in unit tests
        Base64Converter base64Converter = new Base64Converter()
        mockHttpServletRequest.addHeader('Authorization', 'Basic ' + (base64Converter.encode(username)))

        assert service.extractBasicAuthUsername(mockHttpServletRequest) == username
    }

    void testExtractBasicAuthUsername() {
        GrailsMockHttpServletRequest mockHttpServletRequest = new GrailsMockHttpServletRequest()

        String username = "Sune"
        String totalString = "${username}:password"

        // Ordinary encodeAsBase64() does not seem to work in unit tests
        Base64Converter base64Converter = new Base64Converter()
        mockHttpServletRequest.addHeader('Authorization', 'Basic ' + (base64Converter.encode(totalString)))

        assert service.extractBasicAuthUsername(mockHttpServletRequest) == username
    }

    void testVersionComparison() {
        shouldFail(NumberFormatException) {
            service.isRequestVersionEqualToOrGreaterThan("", "")
        }

        assert service.isRequestVersionEqualToOrGreaterThan("0", "0")
        assert service.isRequestVersionEqualToOrGreaterThan("1", "0")
        assert service.isRequestVersionEqualToOrGreaterThan("19", "19")
        assert service.isRequestVersionEqualToOrGreaterThan("19.2", "19.2")
        assert service.isRequestVersionEqualToOrGreaterThan("19.2.00000", "19.2")
        assert service.isRequestVersionEqualToOrGreaterThan("19.2", "19.2.00000")
        assert service.isRequestVersionEqualToOrGreaterThan("19.2.1", "19.2.01")
        assert service.isRequestVersionEqualToOrGreaterThan("19.2.345.1", "19.2.345.1")
        assert service.isRequestVersionEqualToOrGreaterThan("20.2.345.1", "19.7.345.1")
        assert service.isRequestVersionEqualToOrGreaterThan("19.2", "19")
        assert service.isRequestVersionEqualToOrGreaterThan("19.2.345.1", "19.2.345")
        assert service.isRequestVersionEqualToOrGreaterThan("20.2.345.1", "19.7.345")
        assert service.isRequestVersionEqualToOrGreaterThan("20", "19.7.345")

        assert !service.isRequestVersionEqualToOrGreaterThan("0", "1")
        assert !service.isRequestVersionEqualToOrGreaterThan("19.2", "19.3")
        assert !service.isRequestVersionEqualToOrGreaterThan("19.2.345.1", "19.4.345.1")
        assert !service.isRequestVersionEqualToOrGreaterThan("20.2.345.1", "21.7")
        assert !service.isRequestVersionEqualToOrGreaterThan("19.7.345", "20")
        assert !service.isRequestVersionEqualToOrGreaterThan("19", "19.2.1")
        assert !service.isRequestVersionEqualToOrGreaterThan("19.0.0", "19.2.1")
    }

    void testCheckAppVersionNull() {
        assert !service.checkAppVersion(null)
    }

    void testCheckAppVersionNullWithConfig() {
        service.grailsApplication.config = [
                "minimumAppVersion": [
                        "android": "20.3.12"
                ]
        ]

        assert !service.checkAppVersion(null)
    }

    void testCheckAppVersionMissingHeadersWithConfig() {
        service.grailsApplication.config = [
                "minimumAppVersion": [
                        "android": "20.3.12"
                ]
        ]

        GrailsMockHttpServletRequest mockHttpServletRequest = new GrailsMockHttpServletRequest()

        assert !service.checkAppVersion(mockHttpServletRequest)
    }

    void testCheckAppVersionNoVersionConfigSet() {
        GrailsMockHttpServletRequest mockHttpServletRequest = new GrailsMockHttpServletRequest()

        service.grailsApplication.config = [
                "minimumAppVersion": null
        ]

        assert service.checkAppVersion(mockHttpServletRequest)
    }

    void testCheckAppVersionMissingOS() {
        service.grailsApplication.config = [
                "minimumAppVersion": [
                        "android": "20.3.12"
                ]
        ]

        GrailsMockHttpServletRequest mockHttpServletRequest = new GrailsMockHttpServletRequest()
        mockHttpServletRequest.addHeader(AuthenticationService.APP_PLATFORM_KEY, "ios")
        mockHttpServletRequest.addHeader(AuthenticationService.APP_VERSION_KEY, "20.4")

        assert !service.checkAppVersion(mockHttpServletRequest)
    }

    void testCheckAppVersionOK() {
        service.grailsApplication.config = [
                "minimumAppVersion": [
                        "android": "20.3.12"
                ]
        ]

        GrailsMockHttpServletRequest mockHttpServletRequest = new GrailsMockHttpServletRequest()
        mockHttpServletRequest.addHeader(AuthenticationService.APP_PLATFORM_KEY, "android")
        mockHttpServletRequest.addHeader(AuthenticationService.APP_VERSION_KEY, "20.4")

        assert service.checkAppVersion(mockHttpServletRequest)
    }
}
