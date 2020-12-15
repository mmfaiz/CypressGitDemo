package com.matchi

import grails.test.mixin.TestFor
import org.junit.Before

import static grails.test.MockUtils.mockDomain

@TestFor(ResetPasswordController)
class ResetPasswordControllerTests {

    def mockTicketService
    def mockNotificationService
    def mockUserService
    def existingUser

    @Before
    public void setUp() {

        mockTicketService = mockFor(TicketService)
        controller.ticketService = mockTicketService.createMock()

        mockNotificationService = mockFor(NotificationService)
        controller.notificationService = mockNotificationService.createMock()

        mockUserService = mockFor(UserService)
        controller.userService = mockUserService.createMock()

        existingUser = createUser("test@test.com")

        mockDomain(User, [existingUser])
        mockForConstraintsTests(ResetPasswordCommand)
        mockForConstraintsTests(UpdatePasswordCommand)
        mockForConstraintsTests(ChangePasswordCommand)
    }

    void testResetPasswordCommandWithEmptyEmail() {
        ResetPasswordCommand cmd = new ResetPasswordCommand(email: "")
        assert !cmd.validate()
    }

    void testResetPasswordCommandWithNullEmail() {
        ResetPasswordCommand cmd = new ResetPasswordCommand(email: null)
        assert !cmd.validate()
    }

    void testResetPasswordCommandWithBadFormatEmail() {
        ResetPasswordCommand cmd = new ResetPasswordCommand(email: "testtest.com")
        assert !cmd.validate()
    }

    void testResetPasswordCommandWithCorrec2tEmail() {
        ResetPasswordCommand cmd = new ResetPasswordCommand(email: "test@testtest.com")
        assert cmd.validate()
    }

    void testUpdatePasswordWithCorrectPassword() {
        UpdatePasswordCommand cmd = new UpdatePasswordCommand()
        cmd.ticket = "ticket"
        cmd.newPassword = "a" * ValidationUtils.PASSWORD_MIN_LENGTH
        cmd.newPasswordConfirm = "a" * ValidationUtils.PASSWORD_MIN_LENGTH
        assert cmd.validate()
    }

    void testUpdatePasswordWithEmpty() {
        UpdatePasswordCommand cmd = new UpdatePasswordCommand()
        cmd.newPassword = ""
        cmd.newPasswordConfirm = ""
        assert !cmd.validate()
    }

    void testUpdatePasswordMismatching() {
        UpdatePasswordCommand cmd = new UpdatePasswordCommand()
        cmd.newPassword = "pelle"
        cmd.newPasswordConfirm = "pelle2"
        assert !cmd.validate()
    }

    void testFormErrorOnUserNotFound() {
        ResetPasswordCommand cmd = new ResetPasswordCommand(email: "test@test_not_exists.com")
        mockTicketService.demand.createResetPasswordTicket(0..0) { def user ->
            return null
        }
        controller.reset(cmd)
        assert view == "/resetPassword/index"
        mockTicketService.verify()
    }

    void testResetPasswordMailIsSent() {
        ResetPasswordCommand cmd = new ResetPasswordCommand(email: "test@test.com")

        setupEmptyMocks()

        controller.reset(cmd)
        mockTicketService.verify()
        mockNotificationService.verify()
    }

    void testResetPasswordTicketIsCreated() {
        ResetPasswordCommand cmd = new ResetPasswordCommand(email: "test@test.com")

        setupEmptyMocks()

        controller.reset(cmd)
        mockTicketService.verify()
    }

    void testSuccessfulTicketRedirectsToInfo() {
        ResetPasswordCommand cmd = new ResetPasswordCommand(email: "test@test.com")

        setupEmptyMocks()

        controller.reset(cmd)

        assert "/info/index" == response.redirectedUrl
    }

    void testFlashMessageIsSetAfterSuccessfullResetRequest() {
        ResetPasswordCommand cmd = new ResetPasswordCommand(email: "test@test.com")

        setupEmptyMocks()

        controller.reset(cmd)

        assert controller.flash.message.length() > 0
    }

    void testChangePasswordErrorIfInvalidKey() {
        ChangePasswordCommand cmd = new ChangePasswordCommand()
        cmd.ticket = "key"

        mockTicketService.demand.isTicketValid(1..1) { def key ->
            return false
        }

        controller.change(cmd)

        assert "/info/index" == response.redirectedUrl
        assert controller.flash.error.length() > 0
        mockTicketService.verify()
    }

    void testUpdatePasswordWithUnmatchingPassword() {
        UpdatePasswordCommand cmd = new UpdatePasswordCommand()
        cmd.ticket = "key"
        cmd.newPassword = "secret"
        cmd.newPasswordConfirm = "secret_ops"

        mockTicketService.demand.isTicketValid(1..1) { def key ->
            return true
        }

        controller.update(cmd)

        assert "/resetPassword/change" == view
        mockTicketService.verify()
    }

    void testUpdatePasswordIsCalledWhenFormIsCorrect() {
        UpdatePasswordCommand cmd = new UpdatePasswordCommand()
        cmd.ticket = "key"
        cmd.newPassword = "a" * ValidationUtils.PASSWORD_MIN_LENGTH
        cmd.newPasswordConfirm = "a" * ValidationUtils.PASSWORD_MIN_LENGTH

        mockTicketService.demand.isTicketValid(1..1) { def key ->
            return true
        }

        mockUserService.demand.changePasswordWithTicket(1..1) { def newPassword, def ticket ->
        }

        controller.update(cmd)

        assert "/info/index" == response.redirectedUrl
        mockTicketService.verify()
        mockUserService.verify()
    }

    void testShowFormIfKeyIsValid() {
        ChangePasswordCommand cmd = new ChangePasswordCommand()
        cmd.ticket = "key"

        mockTicketService.demand.isTicketValid(1..1) { def key ->
            return true
        }

        controller.change(cmd)

        mockTicketService.verify()
    }

    private setupEmptyMocks() {
        mockTicketService.demand.createResetPasswordTicket(1..1) { def user ->
            return null
        }

        mockNotificationService.demand.sendResetPasswordTicketMail(1..1) { def user, def ticket ->
            return
        }
    }

    private User createUser(def email) {
        return new User(email: email, password: "test")
    }
}
