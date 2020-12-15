package com.matchi

import grails.test.mixin.Mock
import grails.test.mixin.TestFor

import static com.matchi.TestUtils.*

/**
 * @author Sergei Shushkevich
 */
@TestFor(ChangeEmailController)
@Mock([ChangeEmailTicket, User])
class ChangeEmailControllerTests {

    void testChange() {
        def user = createUser()
        def ticket = new ChangeEmailTicket(user: user, newEmail: "new@matchi.com", key: "key",
                expires: new Date()).save(failOnError: true, flush: true)
        def ticketService = mockFor(TicketService)
        ticketService.demand.isChangeMailTicketValid { t -> true }
        controller.ticketService = ticketService.createMock()
        def userService = mockFor(UserService)
        userService.demand.changeEmailWithTicket { e, t -> }
        controller.userService = userService.createMock()
        def customerService = mockFor(CustomerService)
        customerService.demand.updateCustomersEmail { u -> }
        controller.customerService = customerService.createMock()
        params.ticket = ticket.key

        controller.change()

        assert response.redirectedUrl == "/info/index"
        ticketService.verify()
        userService.verify()
    }
}