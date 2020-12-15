package com.matchi

import grails.test.ControllerUnitTestCase

class UserRegistrationControllerTests extends ControllerUnitTestCase {


    def mockUserService

    protected void setUp() {
        super.setUp()

        mockUserService = mockFor(UserService)

        mockDomain(User, [new User(id: 1, email: "test@test.com", activationcode: "123"),
        new User(id: 2, password: "password", email: "test@test.com", activationcode: "321")])

        controller.userService = mockUserService.createMock()

    }

    protected void tearDown() {
        super.tearDown()
    }

    void testEnableUserNoPasswordRendersPasswordForm() {
        EnableUserCommand cmd = new EnableUserCommand(ac: "123")
        controller.enable(cmd)
        assertEquals "change", renderArgs.view
    }

    void testEnableUserWithPasswordGetsEnabled() {
        mockUserService.demand.enableUser(1..1) { def user -> }
        EnableUserCommand cmd = new EnableUserCommand(ac: "321")
        controller.enable(cmd)
        assertEquals "activation", renderArgs.view
        mockUserService.verify()
    }

    void testEnableUserUserNotFoundShowsInfoPage() {
        EnableUserCommand cmd = new EnableUserCommand(ac: "notfound")
        controller.enable(cmd)
        assertEquals "activation", renderArgs.view
    }
}
