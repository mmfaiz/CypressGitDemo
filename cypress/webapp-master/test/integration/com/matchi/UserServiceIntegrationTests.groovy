package com.matchi

import static com.matchi.TestUtils.*

/**
 * @author Sergei Shushkevich
 */
class UserServiceIntegrationTests extends GroovyTestCase {

    def springSecurityService
    def userService

    void testUpdateLastLoggedInDate() {
        def user = createUser()

        userService.updateLastLoggedInDate(user)

        user.refresh()
        assert user.lastLoggedIn
    }

    void testUpdateLastLoggedInDateForCurrentUser() {
        def user = createUser()
        springSecurityService.reauthenticate user.email

        userService.updateLastLoggedInDate()

        user.refresh()
        assert user.lastLoggedIn
    }
}