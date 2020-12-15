package com.matchi.admin

import com.matchi.CustomerService
import com.matchi.User
import com.matchi.UserService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor

import static com.matchi.TestUtils.*

@TestFor(AdminUserController)
@Mock([User])
class AdminUserControllerTests {

    void testUpdate() {
        def user = createUser()
        def userService = mockFor(UserService)
        userService.demand.updateUserRoles { u, r -> }
        controller.userService = userService.createMock()
        def customerService = mockFor(CustomerService)
        customerService.demand.updateCustomersEmail { u -> }
        controller.customerService = customerService.createMock()
        def cmd = new UpdateUserCommand(id: user.id, email: "changed@local.net",
                firstname: user.firstname, lastname: user.lastname)
        cmd.validate()

        controller.update(cmd)

        assert response.redirectedUrl == "/admin/users/index"
        userService.verify()
        customerService.verify()
    }

    void testUpdateNoEmailChange() {
        def user = createUser()
        def userService = mockFor(UserService)
        userService.demand.updateUserRoles { u, r -> }
        controller.userService = userService.createMock()
        def customerService = mockFor(CustomerService)
        customerService.demand.updateCustomersEmail(0) { u -> }
        controller.customerService = customerService.createMock()
        def cmd = new UpdateUserCommand(id: user.id, email: user.email,
                firstname: user.firstname, lastname: user.lastname)
        cmd.validate()

        controller.update(cmd)

        assert response.redirectedUrl == "/admin/users/index"
        userService.verify()
        customerService.verify()
    }

    void testUpdateValidationFailure() {
        def user = createUser()
        def cmd = new UpdateUserCommand(id: user.id)
        cmd.validate()

        controller.update(cmd)

        assert view == "/adminUser/edit"
    }
}
