package com.matchi.facility

import com.matchi.Customer
import com.matchi.CustomerDisableMessagesTicket
import com.matchi.CustomerService
import com.matchi.Facility
import com.matchi.Municipality
import com.matchi.Region
import com.matchi.TicketService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor

import javax.servlet.http.HttpServletResponse

import static com.matchi.TestUtils.*

/**
 * See the API for {@link grails.test.mixin.web.ControllerUnitTestMixin} for usage instructions
 */
@TestFor(FacilityCustomerController)
@Mock([Customer, CustomerDisableMessagesTicket, Facility, Municipality, Region])
class FacilityCustomerControllerTests {

    void testShowByTicket() {
        def ticket = createCustomerDisableMessagesTicket()
        params.ticket = ticket.key

        def model = controller.showByTicket()

        assert ticket == model.ticket
    }

    void testShowByTicket_InvalidTicketKey() {
        params.ticket = "invalid"
        controller.showByTicket()
        assert HttpServletResponse.SC_NOT_FOUND == response.status
    }

    void testShowByTicket_InvalidTicket() {
        def ticket = createCustomerDisableMessagesTicket()
        ticket.consumed = new Date()
        ticket.save()
        params.ticket = ticket.key

        def model = controller.showByTicket()

        assert "facilityCustomer.showByTicket.alreadyDisabled" == model.errorMessage
    }

    void testShowByTicket_InvalidTicket_Expired() {
        def ticket = createCustomerDisableMessagesTicket()
        ticket.expires = new Date() - 62
        ticket.save()
        params.ticket = ticket.key

        def model = controller.showByTicket()

        assert "facilityCustomer.showByTicket.expired" == model.errorMessage
    }

    void testDisableClubMessagesByTicket() {
        def ticket = createCustomerDisableMessagesTicket()
        def customerServiceControl = mockFor(CustomerService)
        customerServiceControl.demand.disableClubMessages { c -> }
        controller.customerService = customerServiceControl.createMock()
        def ticketServiceControl = mockFor(TicketService)
        ticketServiceControl.demand.consumeCustomerDisableMessagesTicket { t -> }
        controller.ticketService = ticketServiceControl.createMock()
        params.ticket = ticket.key

        def model = controller.disableClubMessagesByTicket()

        assert HttpServletResponse.SC_OK == response.status
        assert ticket.customer == model.customer
        customerServiceControl.verify()
        ticketServiceControl.verify()
    }

    void testDisableClubMessagesByTicket_InvalidTicketKey() {
        params.ticket = "invalid"
        controller.disableClubMessagesByTicket()
        assert HttpServletResponse.SC_NOT_FOUND == response.status
    }

    void testDisableClubMessagesByTicket_InvalidTicket() {
        def ticket = createCustomerDisableMessagesTicket()
        ticket.consumed = new Date()
        ticket.save()
        params.ticket = ticket.key

        controller.disableClubMessagesByTicket()

        assert HttpServletResponse.SC_NOT_FOUND == response.status
    }
}
