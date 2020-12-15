package com.matchi

import grails.validation.Validateable

class UpdateCustomerRequestController extends GenericController {

    def customerService
    def ticketService
    def notificationService

    def index() {
        def ticket = params.ticket

        if (ticket && !ticketService.isUpdateRequestTicketValid(ticket)) {
            flash.error = message(code: "updateCustomerRequest.index.error")
            redirect(controller: "info", params: [ title: message(code: "updateCustomerRequest.index.param.title")])
            return
        }

        def updateTicket = CustomerUpdateRequestTicket.findByKey(ticket)
        def updateCustomer = updateTicket?.customer

        if (!updateCustomer) {
            flash.error = message(code: "updateCustomerRequest.index.error")
            redirect(controller: "info", params: [ title: message(code: "updateCustomerRequest.index.param.title")])
            return
        }

        ticketService.consumeUpdateRequestTicket(updateTicket)

        [ customer: updateCustomer, facility: updateCustomer?.facility]
    }

    def update(CustomerUpdateRequestCommand cmd) {
        Facility facility = Facility.findById(cmd.facilityId)

        if (cmd.hasErrors()) {
            render(view: "index", model: [cmd: cmd, facility: facility])
            return
        }

        def locale = new Locale(facility.language)
        PersonalNumberSettings personalNumberSettings = facility.getPersonalNumberSettings()
        personalNumberSettings.requireSecurityNumber = facility.requireSecurityNumber
        if (!ValidationUtils.isPersonalNumberValid(cmd.birthday, cmd.securitynumber, false, personalNumberSettings)) {
            cmd.errors.rejectValue("birthday", "membershipRequestCommand.birthday.matches.invalid",
                        [message(code: "membershipRequestCommand.birthday.format", locale: locale)] as String[], "")
            render(view: "index", model: [cmd: cmd, facility: facility])
            return
        }

        def customer = Customer.findByIdAndFacility(cmd.customerId, facility)
        if ( customer ) {
            customerService.updateCustomer(customer, cmd)
            notificationService.sendCustomerUpdatedConfirm(customer, facility, cmd.message)
            flash.message = message(code: "updateCustomerRequest.update.success")
            redirect(controller: "home", action: "index")
        } else {
            flash.message = message(code: "updateCustomerRequest.update.error")
            render(view: "index", model: [cmd: cmd, facility: Facility.findById(cmd.facilityId)])
        }
    }
}

@Validateable(nullable = true)
class CustomerUpdateRequestCommand {
    Long customerId //Customer ID
    Long facilityId

    String firstname
    String lastname
    String email
    String address
    String zipcode
    String city
    String country
    String telephone
    String cellphone
    String message

    Customer.CustomerType type

    String birthday
    String securitynumber

    boolean confirmation

    static constraints = {
        customerId(nullable: false, blank: false)
        facilityId(nullable: false, blank: false)
        firstname(nullable: false, blank: false)
        lastname(nullable: false, blank: false)
        email(nullable: false, blank: false, email: true)
        address(nullable: false, blank: false)
        zipcode(nullable: false, blank: false)
        type(nullable: false, blank: false, validator: { type, obj ->
            if (obj.securitynumber?.length() == 4) {
                def controlNr = Integer.parseInt(obj.securitynumber[2])
                return LuhnValidator.validateType(controlNr, type) ? true : ['invalid.type']
            }
            return true
        })
        city(nullable: false, blank: false)
        country(nullable: false, blank: false)
        telephone(nullable: true)
        cellphone(nullable: false, blank: false)
        birthday(nullable: false, blank: false)
        securitynumber(nullable: true)
        confirmation(validator: { confirmation ->
            confirmation?:['invalid.confirmation']
        })
        message(nullable: true, blank: true)
    }
}
