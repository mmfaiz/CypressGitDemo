package com.matchi.facility

import com.matchi.*
import com.matchi.FacilityProperty.FacilityPropertyKey
import com.matchi.activities.Participant
import com.matchi.activities.trainingplanner.CourseActivity
import com.matchi.async.ScheduledTask
import com.matchi.coupon.CustomerCoupon
import com.matchi.dynamicforms.Form
import com.matchi.dynamicforms.FormField
import com.matchi.excel.ExcelExportManager
import com.matchi.fortnox.v3.FortnoxException
import com.matchi.invoice.Invoice
import com.matchi.invoice.InvoiceRow
import com.matchi.orders.Order
import grails.util.Holders
import grails.validation.Validateable
import org.apache.http.HttpStatus
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsParameterMap
import org.joda.time.DateTime
import org.joda.time.LocalDate

import javax.servlet.http.HttpServletResponse

class FacilityCustomerController extends GenericController {

    public static final String LIST_FILTER_KEY = "facility_customer_filter"
    public static final String CUSTOMER_LIST_CONTROLLER_KEY = "facility_customer_list_controller"

    def cashService
    def fileArchiveService
    def ticketService
    def customerService
    def memberService
    def notificationService

    def excelExportManager
    def scheduledTaskService

    def index(FilterCustomerCommand filter) {
        if (!params.boolean("reset") && session[LIST_FILTER_KEY]?.isNotExpired()) {
            filter = session[LIST_FILTER_KEY]
            filter.allselected = false
            if (session[CUSTOMER_LIST_CONTROLLER_KEY] == "facilityCustomerMembers" && filter.members.size() == 1
                    && filter.members[0] == FilterCustomerCommand.ShowMembers.MEMBERS_ONLY) {
                filter.members.clear()
            }
            filter.membershipStartDate = null
            filter.membershipEndDate = null
        } else {
            session[LIST_FILTER_KEY] = filter
        }
        session[CUSTOMER_LIST_CONTROLLER_KEY] = "facilityCustomer"

        def facility = (Facility)getUserFacility()
        def facilityGroups = Group.findAllByFacility(facility, [sort: "name"]).asList()
        def birthyears = Customer.birthyears(facility).list()
        def seasons = Season.findAllByFacility(facility, [sort: "startTime", order: "desc"])
        def courses = CourseActivity.findAllByFacility(facility, [sort: "name"]).sort{it.isArchived()}
        def clubs = Customer.clubs(facility).list()
        def membershipTypes = facility.membershipTypes + facility.masterFacilities.collect {it.membershipTypes}.flatten()
        def localFacilities
        if (facility.isMasterFacility()) {
            localFacilities = facility.memberFacilities
        }

        def customers = customerService.findCustomers(filter, facility)

        [ facility: facility, customers: customers, filter: filter, seasons: seasons, courses: courses,
          facilityGroups: facilityGroups, types: membershipTypes, birthyears: birthyears, clubs: clubs, localFacilities: localFacilities]
    }

    def edit() {
        def customer = Customer.findById(params.id)
        if(customer.deleted) {
            return response.sendError(HttpStatus.SC_NOT_FOUND)
        } else if (customer) {
            if (customer?.facility?.hasLinkedFacilities()) {
                assertHierarchicalFacilityAccessTo(customer)
            } else {
                assertFacilityAccessTo(customer)
            }
            def cmd = new UpdateCustomerCommand(customer.properties)
            cmd.orgNumber = customer.orgNumber
            cmd.personalNumber = customer.getPersonalNumber(false)
            cmd.securityNumber = customer.securityNumber

            return [cmd: cmd, customer: customer, facility: customer.facility, returnUrl: params.returnUrl]
        } else {
            redirect(action: "index")
        }
    }

    def show() {
        def model = [:]
        def customer = customerService.getCustomer(params.long("id"))
        def facility = getUserFacility()
        Collection<Customer> hierarchicalCustomers = []
        if (session[FacilityCustomerMembersController.MEMBERSHIP_TYPE_REQUIRED]) {
            flash.error = message(code: "facilityCustomerMembers.invoice.errorMembershipTypeRequired")
            session.removeAttribute(FacilityCustomerMembersController.MEMBERSHIP_TYPE_REQUIRED)
        }

        if (customer.deleted) {
            return response.sendError(HttpStatus.SC_NOT_FOUND)
        } else if (customer) {

            model.masterFacilityCustomers = customerService.findMasterFacilityCustomers(customer?.user, customer?.facility)

            hierarchicalCustomers = customerService.findHierarchicalCustomersForCustomerPage(getUserFacility(), customer)
            if (hierarchicalCustomers) {
                model.hierarchicalCustomers = hierarchicalCustomers
                model.facility = getUserFacility()
            } else {
                if (customer?.facility?.hasLinkedFacilities()) {
                    assertHierarchicalFacilityAccessTo(customer)
                } else {
                    assertFacilityAccessTo(customer)
                }
            }
            def filter = session[LIST_FILTER_KEY]?.isNotExpired() ? session[LIST_FILTER_KEY] : new FilterCustomerCommand()
            filter.allselected = true
            def ids = customerService.findCustomers(filter, getUserFacility(), false, true)
            def index = ids.indexOf(customer.id)
            if (index != -1) {
                model.prevCustomerId = index > 0 ? ids[index - 1] : null
                model.nextCustomerId = index < (ids.size() - 1) ? ids[index + 1] : null
            }

            model.customer = customer
            model.facility = facility
        }

        model
    }
    def create(CreateCustomerCommand cmd) {
        def facility = getUserFacility()
        cmd = new CreateCustomerCommand()

        cmd.number = facility?.getNextCustomerNumber()
        cmd.country = facility?.country
        cmd.facilityId = facility?.id

        [ cmd:cmd, groups: Group.findAllByFacility(facility),
                types: memberService.getFormMembershipTypes(facility), facility: facility ]
    }

    def add(CreateCustomerCommand cmd) {
        Facility facility = getUserFacility()

        if (cmd.hasErrors()) {
            render(view: "create", model: [ cmd:cmd, groups: Group.findAllByFacility(facility),
                    types: memberService.getFormMembershipTypes(facility), facility: facility ])
            return
        }

        def customer = customerService.createCustomer(cmd)
        addFlashErrorIfFortnoxHasError(customer)
        if (customer) {
            assertFacilityAccessTo(customer)
        }

        if (customer.number != cmd.number) {
            flash.error = message(code: "facilityCustomer.add.error")
            redirect(action: "index")
            return
        }

        if (!customer.errors.hasFieldErrors() && customer.save()) {
            if (cmd.createMembership && !customer.membership) {
                def membership = memberService.addMembership(customer, cmd.membershipType,
                        cmd.startDate, cmd.endDate, cmd.gracePeriodEndDate, null, null, true,
                        Order.ORIGIN_FACILITY, null, cmd.startingGracePeriodDays)
                if (membership && cmd.membershipCancel) {
                    memberService.disableAutoRenewal(membership)
                }
                if (membership?.order?.total() && cmd.membershipPaid) {
                    cashService.createCashOrderPayment(membership.order)
                }
            }

            customerService.linkCustomerToUser(customer)

            flash.message = message(code: "facilityCustomer.add.success", args: [customer.fullName()])

            if (params.returnUrl && params.returnUrl.size() > 0) {
                redirect(url: params.returnUrl)
                return
            }
            redirect(action: "show", params: [id: customer.id])
            return
        }

        render(view: "create", model: [ cmd:cmd, customer:customer])
    }

    def update(UpdateCustomerCommand cmd) {
        def customer = Customer.get(cmd.id)

        if (customer) {
            if (customer?.facility?.hasLinkedFacilities()) {
                assertHierarchicalFacilityAccessTo(customer)
            } else {
                assertFacilityAccessTo(customer)
            }
        }
        if (cmd.hasErrors()) {
            render(view: "edit", model: [ cmd:cmd, customer:customer, facility: customer.facility ])
            return
        }

        if (!customerService.updateCustomer(customer, cmd)) {
            render(view: "edit", model: [ cmd:cmd, customer:customer, facility: customer.facility ])
            return
        }
        addFlashErrorIfFortnoxHasError(customer)

        if (!customer.user) {
            customerService.linkCustomerToUser(customer)
        }

        flash.message = message(code: "facilityCustomer.update.success", args: [cmd.number])

        if (params.returnUrl && params.returnUrl.size() > 0) {
            redirect(url: params.returnUrl)
            return
        }
        redirect(action: "show", params: [ id:cmd.id ])
    }
    def unlink() {
        def customer = Customer.findById(params.id)

        if (customer) {
            assertFacilityAccessTo(customer)

            def user = customer.user
            customer.user = null

            if (!customer.hasErrors() && customer.save()) {
                flash.message = message(code: "facilityCustomer.unlink.success", args: [user.email])
            } else {
                flash.error = message(code: "facilityCustomer.unlink.error2")
            }
        } else {
            flash.error = message(code: "facilityCustomer.unlink.error1")
        }

        redirect(action: "show", id: params.id)
    }
    def link() {
        Customer customer = Customer.findById(params.id)
        User user = User.findById(params.userId)

        if (customer && user && !user.isCustomerIn(customer.facility)) {
            assertFacilityAccessTo(customer)

            customer.user = user

            if (!customer.hasErrors() && customer.save()) {
                flash.message = message(code: "facilityCustomer.link.success", args: [user.email])
            } else {
                flash.error = message(code: "facilityCustomer.link.error2")
            }
        } else {
            flash.error = message(code: "facilityCustomer.link.error1")
        }

        redirect(action: "show", id: params.id)
    }

    def remove() {
        Customer customer = Customer.findById(params.id)
        Long oldNumber = customer.number

        if(customer) {
           customerService.clearCustomer(customer)
        }

        flash.message = message(code: "facilityCustomer.remove.success", args: [oldNumber])
        redirect(action: "index")
    }

    def invite() {
        Customer customer = Customer.findById(params.id)

        def ticket = ticketService.createCustomerInviteTicket(customer)
        notificationService.sendCustomerInvitation(customer, ticket)

        flash.message = message(code: "facilityCustomer.invite.success")
        redirect(action: "show", id: customer.id)
    }

    def showBookings() {
        def bookings = Booking.where { id in RequestUtil.toLongList(RequestUtil.toListFromString(params.bookingIds)) }
        def customer = Customer.get(params.customerId)

        render template: "/templates/customer/customerBookingsPopup", model: [ bookings: bookings, customer: customer ]
    }

    def showCoupons() {
        def coupons = CustomerCoupon.where { id in RequestUtil.toLongList(RequestUtil.toListFromString(params.couponIds)) }
        def customer = Customer.get(params.customerId)

        render template: "/templates/customer/customerCouponsPopup", model: [ coupons: coupons, customer: customer ]
    }

    def showInvoices() {
        def invoices = Invoice.where { id in RequestUtil.toLongList(RequestUtil.toListFromString(params.invoiceIds)) }
        def customer = Customer.get(params.customerId)
        def totalSum = 0

        invoices.each {
            totalSum += it.getTotalIncludingVAT()
        }

        render template: "/templates/customer/customerInvoicesPopup", model: [ invoices: invoices, totalSum: totalSum, customer: customer ]
    }

    def showInvoiceRows() {
        def invoiceRows = InvoiceRow.where { id in RequestUtil.toLongList(RequestUtil.toListFromString(params.invoiceRowIds)) }
        def customer = Customer.get(params.customerId)
        def totalSum = 0

        invoiceRows.each {
            totalSum += it.getTotalIncludingVAT()
        }

        render template: "/templates/customer/customerInvoiceRowsPopup", model: [ invoiceRows: invoiceRows, totalSum: totalSum, customer: customer ]
    }

    def showSubscriptions() {

        List<Long> idList = RequestUtil.toLongList(RequestUtil.toListFromString(params.subscriptionIds))
        List<Subscription> subscriptions

        if(idList?.size() > 0) {
            subscriptions = Subscription.createCriteria().list() {
                inList('id', idList)
                createAlias('court', 'c')

                order('weekday', 'asc')
                order('time', 'asc')
                order('c.listPosition', 'asc')
            }
        } else {
            subscriptions = []
        }


        def customer = Customer.get(params.customerId)

        render template: "/templates/customer/customerSubscriptionsPopup", model: [ subscriptions: subscriptions, customer: customer ]
    }

    def showCashRegisterTransactions() {
        def transactions = CashRegisterTransaction.where { id in RequestUtil.toLongList(RequestUtil.toListFromString(params.transactionIds)) }
        def customer = Customer.get(params.customerId)

        render template: "/templates/customer/customerCashRegisterTransactionsPopup", model: [ transactions: transactions, customer: customer ]
    }

    def filterList() {
        return [ "membership", "subscription" ]
    }

    def showByTicket() {
        def ticket = CustomerDisableMessagesTicket.findByKey(params.ticket, [fetch: [customer: "join"]])
        if (ticket) {
            if(ticket.isValid()){
                [ticket: ticket]
            } else {
                def errorMessage = g.message(code: 'facilityCustomer.showByTicket.expired')
                if (ticket.isUsed()) {
                    errorMessage = g.message(code: 'facilityCustomer.showByTicket.alreadyDisabled',
                            args: [ticket.customer.facility.name])
                }
                [errorMessage: errorMessage]
            }
        } else {
            response.sendError HttpServletResponse.SC_NOT_FOUND
        }
    }

    def export(FilterCustomerCommand cmd) {
        def facility = getCurrentUser().facility
        Boolean isFederation = facility.isFacilityPropertyEnabled(FacilityProperty.FacilityPropertyKey.FEATURE_FEDERATION.name())

        // get all customers that are being exported
        def customerIds
        if (cmd.allselected) {
            customerIds = customerService.findCustomers(cmd, facility, false, true)
        } else if (session[CUSTOMER_IDS_KEY]) {
            customerIds = session[CUSTOMER_IDS_KEY]
            session.removeAttribute(CUSTOMER_IDS_KEY)
        } else if(params.customerId) {
            customerIds = params.list("customerId").collect { Long.parseLong(it) }
        } else{
            flash.error = message(code: 'no.customer.selected.for.export')
            redirect(action: "index", params: params)
            return
        }

        log.debug("Running export ${params.exportType}")

        def exportType = ExcelExportManager.ExportType.valueOf(params.exportType)

        scheduledTaskService.scheduleTask(message(code: 'scheduledTask.exportCustomer.domainClassName'), facility.id, facility) { taskId ->
            Customer.withNewSession {
                def customers = Customer.createCriteria().listDistinct { inList("id", customerIds) }
                def exportFile = File.createTempFile("customer_export-", ".xls")

                exportFile.withOutputStream { out ->
                    excelExportManager.export(customers, isFederation, exportType, out)
                }

                def task = ScheduledTask.get(taskId)
                task.resultFileName = "kunder_export-${new DateTime().toString("yyyy-MM-dd")}.xls"
                task.resultFilePath = fileArchiveService.storeExportedFile(exportFile)
                task.save(flush: true)

                exportFile.delete()
            }
        }

        if(params.returnUrl) {
            redirect(url: params.returnUrl)
        } else {
            redirect(action: "index", params: params)
        }
    }

    def disableClubMessagesByTicket() {
        def ticket = CustomerDisableMessagesTicket.findByKey(params.ticket, [fetch: [customer: "join"]])
        if (ticket && ticket.isValid()) {
            customerService.disableClubMessages(ticket.customer)
            ticketService.consumeCustomerDisableMessagesTicket(ticket)
            return [customer: ticket.customer]
        } else {
            response.sendError HttpServletResponse.SC_NOT_FOUND
        }
    }

    def addToCourse(Long id, Long courseId) {
        Customer customer = Customer.get(id)
        assertFacilityAccessTo(customer)

        CourseActivity courseActivity = CourseActivity.get(courseId)
        assertFacilityAccessTo(courseActivity)

        if (courseActivity?.form?.maxSubmissions && courseActivity?.participants?.size() >= courseActivity?.form?.maxSubmissions) {
            flash.error = message(code: "form.maxSubmissions.error", args: [(courseActivity?.name ?: courseActivity?.form?.name)])
            redirect(action: "show", params: [id: id])
            return
        }

        Participant participant = new Participant(customer: customer, activity: courseActivity)
        participant.save()

        flash.message = message(code: "facilityCustomer.show.addToCourseSuccess", args: [customer.fullName(), courseActivity.name])
        redirect(action: "show", params: [id: id])
    }

    def redirectToForm(Long id, String hash) {
        def customer = Customer.get(id)
        assertFacilityAccessTo(customer)

        def redirectParams = [hash: hash,
                              returnUrl: createLink(controller: "facilityCustomer", action: "show", id: id)]

        def form = Form.findByHash(hash)
        def fields = FormField.findAllByFormAndIsActive(form, true)

        def pi = fields.find {
            it.type == FormField.Type.PERSONAL_INFORMATION.name()
        }
        redirectParams."${pi.id}.firstname" = customer.firstname
        redirectParams."${pi.id}.lastname" = customer.lastname
        redirectParams."${pi.id}.personal_number" = customer.personalNumber
        redirectParams."${pi.id}.security_number" = customer.getPersonalNumber()
        redirectParams."${pi.id}.org_number" = customer.orgNumber
        redirectParams."${pi.id}.email" = customer.email
        redirectParams."${pi.id}.cellphone" = customer.cellphone
        redirectParams."${pi.id}.telephone" = customer.telephone
        redirectParams."${pi.id}.gender" = customer.type

        def addr = fields.find {
            it.type == FormField.Type.ADDRESS.name()
        }
        if (addr) {
            redirectParams."${addr.id}.address1" = customer.address1
            redirectParams."${addr.id}.address2" = customer.address2
            redirectParams."${addr.id}.postal_code" = customer.zipcode
            redirectParams."${addr.id}.city" = customer.city
        }

        def par = fields.find {
            it.type == FormField.Type.PARENT_INFORMATION.name()
        }
        if (par) {
            if (customer.guardianName) {
                def tokens = customer.guardianName.tokenize(" ")
                redirectParams."${par.id}.firstname" = tokens[0]
                redirectParams."${par.id}.lastname" = tokens[1]
            }
            redirectParams."${par.id}.email" = customer.guardianEmail
            redirectParams."${par.id}.cellphone" = customer.guardianTelephone
        }

        redirect(controller: "form", action: "show", params: redirectParams)
    }

    /**
     * Show fortnox error if necessary
     *
     * @param customer
     */
    private void addFlashErrorIfFortnoxHasError(Customer customer) {
        if (customer.errors.hasGlobalErrors() && customer.errors.globalError.code == FortnoxException.ERROR_CODE) {
            flash.error = customer.errors?.globalError?.defaultMessage
        }
    }
}

@Validateable(nullable = true)
class FilterCustomerCommand implements Serializable {
    String q = ""
    String order = "desc"
    String sort  = "number"
    int max = 100
    int offset = 0
    List<ShowMembers> members = []
    boolean allselected = false
    List<Long> group   = []
    List<Long> type    = []
    List<Customer.CustomerType> gender  = []
    List<Long> seasons  = []
    List<Long> localFacilities  = []
    List<MemberStatus> status  = []
    List<Integer> birthyear = []
    List<Invoice.InvoiceStatus> invoiceStatus = []
    List<Long> courses = []
    List<String> clubs = []
    Integer lastActivity
    boolean dontIncludeMemberFacilitysCustomer
    LocalDate membershipStartDate
    LocalDate membershipEndDate

    private Date dateCreated = new Date()

    private static final long serialVersionUID = 12L

    /**
     * To enable this command to be built later than directly as a parameter
     * @param params
     * @return
     */
    static FilterCustomerCommand buildFromParameters(GrailsParameterMap params) {
        return new FilterCustomerCommand(
                q: params.q,
                seasons: params.list("seasons").collect { Long.valueOf(it) },
                members: params.list("members").collect { Enum.valueOf(ShowMembers, it) },
                courses: params.list("courses").collect { Long.valueOf(it) },
                group: params.list("group").collect { Long.valueOf(it) },
                localFacilities: params.list("localFacilities").collect { Long.valueOf(it) },
                type: params.list("type").collect { Long.valueOf(it) },
                gender: params.list("gender").collect { Enum.valueOf(Customer.CustomerType, it) },
                status: params.list("status").collect { Enum.valueOf(MemberStatus, it) },
                birthyear: params.list("birthyear").collect { Integer.valueOf(it) },
                invoiceStatus: params.list("invoiceStatus"),
                allselected: params.boolean("allselected", false),
                lastActivity: params.lastActivity,
                membershipStartDate: params.membershipStartDate ? new LocalDate(params.membershipStartDate) : null,
                membershipEndDate: params.membershipEndDate ? new LocalDate(params.membershipEndDate) : null
        )
    }

    public static enum ShowMembers {
        MEMBERS_ONLY, NO_MEMBERS, FAMILY_MEMBERS, FAMILY_MEMBER_CONTACTS, FAMILY_MEMBER_MEMBERS, NO_FAMILY_MEMBERS

        static list() {
            return [MEMBERS_ONLY, NO_MEMBERS, FAMILY_MEMBERS, FAMILY_MEMBER_CONTACTS, FAMILY_MEMBER_MEMBERS, NO_FAMILY_MEMBERS]
        }

        static familyList() {
            return [FAMILY_MEMBERS, FAMILY_MEMBER_CONTACTS, FAMILY_MEMBER_MEMBERS, NO_FAMILY_MEMBERS]
        }
    }

    enum MemberStatus {
        PAID, CANCEL, UNPAID, FAILED_PAYMENT, PENDING

        static List<MemberStatus> list(Facility facility) {
            facility.isFacilityPropertyEnabled(FacilityPropertyKey.FEATURE_RECURRING_MEMBERSHIP) ?
                    values() : [PAID, CANCEL, UNPAID, PENDING]
        }
    }

    String toString() {
        return "[q: ${q}, order: ${order}, sort: ${sort}, max: ${max}, offset: ${offset}, members: ${members}, seasons: ${seasons}, group: ${group}, type: ${type}, gender: ${gender}, status: ${status}, birthyear: $birthyear, courses: ${courses}, clubs: ${clubs}, lastActivity: ${lastActivity}]"
    }

    boolean isActive(membersList = false) {
        def isMembersFilterActive = !members.isEmpty()
        if (membersList && members.size() == 1 && members[0] == ShowMembers.MEMBERS_ONLY) {
            isMembersFilterActive = false
        }
        q || isMembersFilterActive || seasons || group || type || gender || status ||
                birthyear || invoiceStatus || courses || clubs || lastActivity ||
                membershipStartDate || membershipEndDate
    }

    boolean isNotExpired() {
        new Date().time - dateCreated.time <= Holders.grailsApplication.config.facility.customerFilter.timeout * 60 * 1000
    }
}
