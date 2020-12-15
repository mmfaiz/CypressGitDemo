package com.matchi

import com.matchi.facility.FilterCustomerCommand
import com.matchi.membership.MembershipFamily
import grails.converters.JSON
import grails.validation.Validateable
import org.joda.time.DateTime
import org.joda.time.LocalTime

import javax.servlet.http.HttpServletResponse

class AutoCompleteSupportController {

    def springSecurityService
    def userService
    def customerService
    def facilityService
    def priceListService
    def couponService

    def index() { }

    def userOnEmail(AutoCompleteQueryCommand cmd) {
        log.debug("Autocomplete userOnEmail ${cmd.query}")

        def loggedInUser = springSecurityService.getCurrentUser()
        def foundUsers = []

        if(loggedInUser == null || loggedInUser?.facility == null) {
            renderError("ACCESS DENIED")
            return
        }

        foundUsers = userService.findUserNotCustomerByEmail(cmd.query, loggedInUser.facility)

        def users = []
        foundUsers.each { User u ->
            def user = [
                    id: u.id,
                    fullname: u.fullName().encodeAsSanitizedMarkup(),
                    firstname: u.firstname.encodeAsSanitizedMarkup(),
                    lastname: u.lastname.encodeAsSanitizedMarkup(),
                    telephone: u.telephone.encodeAsSanitizedMarkup(),
                    email: u.email ]

            users << user
        }

        log.debug("Found users on email: ${users.size()}")

        render users as JSON
    }

    def opponents(AutoCompleteQueryCommand cmd) {

        log.debug("Autocomplete opponents ${cmd.query}")

        def loggedInUser = springSecurityService.getCurrentUser()
        def foundUsers = []

        if(loggedInUser == null) {
            renderError("ACCESS DENIED")
            return
        }

        foundUsers = userService.searchUserByNames(cmd.query)

        def users = []
        foundUsers.each { User u ->

            def user = [
                    firstname: u.firstname.encodeAsSanitizedMarkup(),
                    lastname: u.lastname.encodeAsSanitizedMarkup(),
                    fullname: u.fullName().encodeAsSanitizedMarkup(),
                    url: createLink(controller: "userProfile", action: "index", id: u.id)
            ]

            users << user
        }

        render users as JSON
    }

    def customers(AutoCompleteQueryCommand cmd) {
        log.debug("Autocomplete customers ${cmd.query}")

        def loggedInUser = springSecurityService.getCurrentUser()
        def foundCustomers = []

        if(loggedInUser == null || loggedInUser?.facility == null) {
            renderError("ACCESS DENIED")
            return
        }

        log.debug("Excluding customers in group: " + cmd.excludeCustomersInGroup)

        def excludeCustomersInGroup
        if(cmd.excludeUsersInGroup) {
            excludeCustomersInGroup = Group.get(cmd.excludeCustomersInGroup)
        }

        foundCustomers = customerService.findCustomers(new FilterCustomerCommand(q: cmd.query), loggedInUser.facility)

        def customers = []
        foundCustomers.each { Customer c ->
            def customer = [
                    id: c.id,
                    number: c.number,
                    fullname: c.fullName().encodeAsSanitizedMarkup(),
                    firstname: c.firstname.encodeAsSanitizedMarkup(),
                    lastname: c.lastname.encodeAsSanitizedMarkup(),
                    email: c.email,
                    telephone: c.telephone.encodeAsSanitizedMarkup()
            ]

            if(!excludeCustomersInGroup || !c.belongsTo(excludeCustomersInGroup)) {
                customers << customer
            }
        }

        render customers as JSON
    }

    def customerSelect2(AutoCompleteQueryCommand cmd) {

        def page = params.int("page")
        def pageLimit = params.int("page_limit")
        def availableCustomerIds = params.list("availableCustomerIds[]")

        if(!page) { page = 1 }
        if(!pageLimit) { pageLimit = 10 }

        def loggedInUser = springSecurityService.getCurrentUser()
        def foundCustomers = []

        log.debug("Excluding customers in group: " + cmd.excludeCustomersInGroup)

        def excludeCustomersInGroup
        if(cmd.excludeCustomersInGroup) {
            excludeCustomersInGroup = Group.get(cmd.excludeCustomersInGroup)
        }

        if(loggedInUser == null || loggedInUser?.facility == null) {
            renderError("ACCESS DENIED")
            return
        }

        if(params.id) {
            def c = Customer.get(params.id)

            def customerModel = [
                    id: c.id,
                    email: c.email?:"",
                    firstname: c.firstname.encodeAsSanitizedMarkup(),
                    lastname: c.lastname.encodeAsSanitizedMarkup(),
                    fullname: c.fullName().encodeAsSanitizedMarkup(),
                    telephone: c.telephone.encodeAsSanitizedMarkup(),
                    number: c.number
            ]

            render customerModel as JSON
            return

        } else {
            foundCustomers = customerService.findCustomers(new FilterCustomerCommand(q: cmd.query), loggedInUser.facility)
        }

        def customers = []

        foundCustomers.each { Customer c ->

            def customer = [
                    id: c.id,
                    email: c.email?:"-",
                    firstname: c.firstname.encodeAsSanitizedMarkup(),
                    lastname: c.lastname.encodeAsSanitizedMarkup(),
                    fullname: c.fullName().encodeAsSanitizedMarkup(),
                    telephone: c.telephone.encodeAsSanitizedMarkup(),
                    number: c.number
            ]

            if((!excludeCustomersInGroup || !c.belongsTo(excludeCustomersInGroup))
                    && !availableCustomerIds.contains(c.id.toString())
                    && (!cmd.customersWithConnectedUsers || c.user)) {
                customers << customer
            }
        }

        page = page - 1 // zero indexed
        def total = customers.size()
        def start = page * pageLimit
        def end = Math.min(customers.size(), start+pageLimit)

        customers = customers.subList(start, end)

        def more = (end <= (total-1))

        def result = [more: more, total: total, results: customers, query: cmd.query]

        render result as JSON
    }

    def familyMemberSelect(AutoCompleteQueryCommand cmd) {
        log.debug("Autocomplete familyMember (select2) ${cmd.query}")

        def page = params.int("page")
        def pageLimit = params.int("page_limit")

        if(!page) { page = 1 }
        if(!pageLimit) { pageLimit = 10 }

        def loggedInUser = springSecurityService.getCurrentUser()
        def foundCustomers = []

        if(loggedInUser == null || loggedInUser?.facility == null) {
            renderError("ACCESS DENIED")
            return
        }

        foundCustomers = customerService.findCustomers(new FilterCustomerCommand(q: cmd.query,
                members: [FilterCustomerCommand.ShowMembers.MEMBERS_ONLY]), loggedInUser.facility)
        def customers = []
        def family = MembershipFamily.get(params.long("familyId"))

        foundCustomers.each { Customer c ->

            def customer = [
                    id: c.id,
                    email: c.email?:"",
                    firstname: c.firstname.encodeAsSanitizedMarkup(),
                    lastname: c.lastname.encodeAsSanitizedMarkup(),
                    fullname: c.fullName().encodeAsSanitizedMarkup(),
                    telephone: c.telephone.encodeAsSanitizedMarkup(),
                    number: c.number
            ]
            if(!c.belongsTo(family) && !c.membership.isInGracePeriod()) {
                customers << customer
            }
        }

        page = page - 1 // zero indexed
        def total = customers.size()
        def start = page * pageLimit
        def end = Math.min(customers.size(), start+pageLimit)

        customers = customers.subList(start, end)

        def more = (end <= (total-1))

        def result = [more: more, total: total, results: customers, query: cmd.query]

        render result as JSON
    }

    def facilities() {
        log.debug("Autocomplete facilities ${params.term} with sport ${params.sportId}")

        def facilities = Facility.withCriteria {
            if(params.sportId) {
                and {
                    sports {
                        eq("id", new Long(params.sportId))
                    }
                }
            }
            if(params.term) {
                or {
                    like("name", "%${params.term}%")
                    like("address", "%${params.term}%")
                    like("zipcode", "%${params.term}%")
                    like("city", "%${params.term}%")
                }
            }

            order("name","asc")
        }

        def facilityResult = []

        facilities.each {
            def facility = [
                id: it.id,
                name: it.name.encodeAsSanitizedMarkup()
            ]

            facilityResult << facility
        }


        render facilityResult as JSON
    }

    def courtHours(CourtHoursCommand cmd) {
        log.debug "Autocomplete courtHours for court: ${cmd.courtId} and date: ${cmd.date}"

        def loggedInUser = springSecurityService.getCurrentUser()

        if(loggedInUser == null || loggedInUser?.facility == null) {
            renderError("ACCESS DENIED")
            return
        }

        def court = Court.findById(cmd.courtId)
        def slots = Slot.withCriteria {
            eq("court", court)
            between("startTime", new DateTime(cmd.date).toDateMidnight().toDate(), new DateTime(cmd.date).withHourOfDay(23).withMinuteOfHour(59).withSecondOfMinute(59).toDate())

            order("startTime")
        }

        if (slots.size() < 1) {
            renderError("NO SLOTS FOUND")
            return
        }

        def time = []

        slots.each {
            time << new LocalTime(it.startTime).toString("HH:mm")
        }

        render time as JSON
    }

    private renderError(def message) {
        def error = [
                type: "ERROR",
                message: message
        ]
        render error as JSON
    }

    JSON personalInformation(String fullName) {
        Object currentUser = springSecurityService.getCurrentUser()
        if (currentUser?.facility) {
            List<Map<String,String>> result = Customer.withCriteria {
                eq("facility", currentUser.facility)
                eq("archived", false)
                sqlRestriction("concat(firstname,' ',lastname) like ?", ["%${fullName}%" as String])
            }.collect { Customer customer ->
                [name: customer.fullName(), firstname: customer.firstname.encodeAsSanitizedMarkup(), lastname: customer.lastname.encodeAsSanitizedMarkup(),
                        email: customer.email, cellphone: customer.cellphone.encodeAsSanitizedMarkup(), telephone: customer.telephone.encodeAsSanitizedMarkup(),
                        personalNumber: customer.getPersonalNumber(), address1: customer.address1.encodeAsSanitizedMarkup(),
                        address2: customer.address2.encodeAsSanitizedMarkup(), zipcode: customer.zipcode.encodeAsSanitizedMarkup(), city: customer.city.encodeAsSanitizedMarkup(),
                        guardianName: customer.guardianName, guardianEmail: customer.guardianEmail,
                        guardianTelephone: customer.guardianTelephone, type: customer.type?.name()]
            }

            render result as JSON
        } else {
            response.sendError HttpServletResponse.SC_BAD_REQUEST
        }
    }
}

@Validateable(nullable = true)
class AutoCompleteQueryCommand {
    String query
    Boolean noMembers
    Long excludeUsersInGroup
    Long excludeCustomersInGroup
    Boolean customersWithConnectedUsers
}

@Validateable(nullable = true)
class CourtHoursCommand {
    Long courtId
    String date
}
