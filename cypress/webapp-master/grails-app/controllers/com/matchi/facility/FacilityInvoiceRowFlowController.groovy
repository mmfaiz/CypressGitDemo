package com.matchi.facility

import com.matchi.Customer
import com.matchi.Facility
import com.matchi.FacilityProperty
import com.matchi.GenericController
import com.matchi.Group
import com.matchi.User
import com.matchi.invoice.Invoice
import com.matchi.invoice.InvoiceRow
import grails.validation.Validateable
import org.joda.time.LocalDate

class FacilityInvoiceRowFlowController extends GenericController {

    static scope = "prototype"

    def customerService
    def invoiceService

    def createInvoiceRowFlow = {

        start {
            action { FilterCustomerCommand cmd ->
                def facility = getCurrentUser().facility

                flow.returnUrl = params.returnUrl
                flow.customers = [] as Set
                flow.groups = [] as Set
                flow.availableGroups = Group.findAllByFacility(facility)
                flow.availableOrganizations = Organization.findAllByFacility(facility)
                flow.vats = getCountryVats(facility)
                if (params.allselected) {
                    flow.customers = customerService.findCustomers(cmd, facility)
                } else {
                    // If customerIds parameters is detected preload customers and skip to step 2
                    def customerIds = params.list("customerId")
                    if (session[CUSTOMER_IDS_KEY]) {
                        customerIds = session[CUSTOMER_IDS_KEY]
                        session.removeAttribute(CUSTOMER_IDS_KEY)
                    }
                    customerIds?.each {
                        def customer = Customer.get(it)
                        if (customer) {
                            flow.customers << customer
                        }
                    }
                }
                log.debug("${flow.customers?.size()} customers")

                if (flow.customers.isEmpty()) {
                    selectCustomers()
                } else {
                    selectRows()
                }
            }
            on("selectCustomers").to "selectCustomers"
            on("selectRows").to "selectRows"
        }

        selectCustomers {
            on("addCustomer").to "addCustomer"
            on("addGroup").to "addGroup"
            on("removeCustomer").to "removeCustomer"
            on("removeGroup").to "removeGroup"
            on("next") {
                if (flow.customers.isEmpty() && flow.groups.isEmpty()) {
                    error()
                } else {
                    success()
                }
            }.to "selectRows"
            on("cancel").to "cancel"
        }

        addOrganization {
            action {
                flow.organization = params.organizationId && params.organizationId != "null" ? Organization.get(params.organizationId) : null
                if (flow.organization?.fortnoxAccessToken) {
                    flow.items = invoiceService.getItemsForOrganization(flow.organization.id)
                }
            }
            on("success").to "selectRows"
        }
        addGroup {
            action {
                flow.groups << Group.get(params.groupId)
            }
            on("success").to "selectCustomers"
        }

        addCustomer {
            action {
                def customer = Customer.get(params.customerId)
                if (customer) {
                    flow.customers << customer
                }
            }
            on("success").to "selectCustomers"
        }

        removeCustomer {
            action {
                def customer = Customer.get(params.customerId)
                if (customer) {
                    flow.customers.remove(customer)
                }
            }
            on("success").to "selectCustomers"
        }

        removeGroup {
            action {
                def group = Group.get(params.groupId)
                if (group) {
                    flow.groups.remove(group)
                }
            }
            on("success").to "selectCustomers"
        }

        selectRows {
            onEntry {

                if (!flow.rows) {
                    flow.rows = new InvoiceRowsCommand()
                    flow.rows.rows = [new InvoiceRowCommand()]
                    flow.items = []

                    def facility = getCurrentUser().facility
                    flow.facility = facility

                    flow.items = invoiceService.getItemsForFacilityWithOrganization(facility, flow.organization)

                }
                flow.persistenceContext.clear()
            }
            on("addOrganization").to "addOrganization"
            on("next").to "processInvoices"
            on("back").to "selectCustomers"
            on("cancel").to "cancel"
        }

        processInvoices {
            action { InvoiceRowsCommand rowsCmd ->
                // TODO: workaround; validatable command object can't be properly passed to a web flow view (Grails bug?)
                flow.rows = [rows: rowsCmd.rows.collect { row ->
                    [rowId: row.rowId, price: row.price, vat: row.vat, discount: row.discount, discountType: row.discountType, description: row.description, itemId: row.itemId, amount: row.amount, account: row.account ]
                }]

                if (!rowsCmd.validate()) {
                    error()
                } else {
                    def invoices = []
                    def customers = [] as Set // unique set of customers

                    // add all selected customers
                    customers.addAll(flow.customers)

                    // collect all customers from groups
                    flow.groups.each { Group group ->
                        Group.find(group).customerGroups.each {
                            customers << Customer.find(it.customer)
                        }
                    }

                    // add invoice per customer
                    customers.each { customer ->
                        rowsCmd.rows.each { row ->

                            def invoiceRow = row.toInvoiceRow(customer, getCurrentUser())
                            if (flow.organization) {
                                invoiceRow.organization = flow.organization
                            }
                            invoices << invoiceRow
                        }
                    }

                    [invoices: invoices]
                }

            }

            on("success").to "confirmRows"
            on("error").to "selectRows"
        }

        confirmRows {
            on("next").to "createInvoices"
            on("back").to "selectRows"
            on("cancel").to "cancel"
        }

        createInvoices {
            action {
                flow.invoices.each {
                    it.save(failOnError: true)
                }
            }
            on("success").to("invoiceReceipt")
        }

        cancel {
            if (flow.returnUrl) {
                redirect(url: flow.returnUrl)
                return
            }
            redirect(controller: "facilityInvoiceRow", action: "index")
        }

        invoiceReceipt {
            if (flow.returnUrl) {
                redirect(url: flow.returnUrl)
                return
            }
            redirect(controller: "facilityInvoiceRow", action: "index")
        }

    }

    /*
     * Flow that handles invoice creation from a set of invoice rows (input: rowIds)
     */
    def createInvoiceFlow = {

        processInvoiceRows {
            action { FilterInvoiceRowCommand filter ->
                Facility facility = getCurrentUser().facility
                flow.facility = facility
                flow.vats = getCountryVats(facility)
                flow.returnUrl = params.returnUrl
                if (filter.allselected) {
                    flow.rows = invoiceService.getInvoiceRows(filter)
                } else if (params.invoiceRowIds) {
                    def rowIds = params.list("invoiceRowIds").collect { Long.parseLong(it) }
                    flow.rows = InvoiceRow.findAllByIdInList(rowIds)
                } else {
                    def rowIds = params.list("rowIds").collect { Long.parseLong(it) }
                    flow.rows = InvoiceRow.findAllByIdInList(rowIds)
                }
            }
            on("success").to "invoiceDetails"
        }

        invoiceDetails {
            onEntry {
                if (!flow.invoiceDetails) {
                    def defaultExpirationDays = 30 // TODO: Make facility config variable
                    flow.invoiceDetails = new InvoiceDetailsCommand(expirationDays: defaultExpirationDays, invoiceDate: new LocalDate(), createNoCreditInvoices: true, useInvoiceFees: flow.facility?.useInvoiceFees)
                }
            }

            on("next").to "processInvoiceDetails"
            on("back").to "cancel"
            on("cancel").to "cancel"
        }

        processInvoiceDetails {
            action { InvoiceDetailsCommand cmd ->
                if (cmd?.invoiceDate && cmd?.expirationDays) {
                    flow.invoiceDetails = cmd
                }
                if (!flow.invoiceDetails.validate()) {
                    flash.error = message(code: "invoiceDetailsCommand.invalid")
                    return error()
                } else {
                    def invoices = []
                    def user = getCurrentUser()
                    def maxAmount = flow.facility.getFacilityPropertyValue(
                            FacilityProperty.FacilityPropertyKey.FACILITY_MEMBERSHIP_FAMILY_MAX_AMOUNT.name())
                    def rowsByOrganization = flow.rows.groupBy { def row -> row.organization }
                    rowsByOrganization.each { rowByOrg ->
                        def organization = rowByOrg.key
                        def rowsByCustomer = rowByOrg.value.groupBy { def row -> row.customer }

                        rowsByCustomer.each {
                            def customer = it.key
                            def rows = it.value

                            Invoice invoice = new Invoice()
                            invoice.organization = organization
                            invoice.customer = customer
                            invoice.text = flow.invoiceDetails.text
                            invoice.invoiceDate = flow.invoiceDetails.invoiceDate
                            invoice.expirationDate = flow.invoiceDetails.getExpirationDate()
                            invoice.status = Invoice.InvoiceStatus.READY

                            if (maxAmount) {
                                def row = invoiceService.createFamilyMembershipDiscountRow(
                                        customer, rows, user, new BigDecimal(maxAmount))
                                if (row) {
                                    rows << row
                                }
                            }

                            if (cmd.useInvoiceFees) {
                                def row = invoiceService.createInvoiceFeeRowIfNeeded(customer, rows, user)
                                if (row) {
                                    rows << row
                                }
                            }

                            def invoiceSum = 0
                            rows.each { row ->
                                invoiceSum += row.getTotalIncludingVAT()

                                if (validateAddInvoiceRow(invoiceSum, row, cmd)) {
                                    invoice.addToRows(row)
                                }

                                flow.persistenceContext.evict(row)
                            }

                            log.debug("Invoice sum = ${invoiceSum}")

                            flow.persistenceContext.evict(invoice)

                            if (invoice?.rows?.size() > 0) {
                                invoices << invoice
                            }
                        }
                    }


                    def totalInvoiceSum = invoices.size() > 0 ? invoices.sum { it.getTotalIncludingVAT() } : 0

                    [invoices: invoices, total: totalInvoiceSum]
                }
            }

            on("error").to "invoiceDetails"
            on("success").to "selectInvoices"
        }

        selectInvoices {
            on("next").to "createInvoices"
            on("cancel").to "cancel"
            on("back").to "invoiceDetails"
        }

        createInvoices {
            action {
                flow.invoices.each { invoice ->
                    if (!invoice.customer.facility.hasExternalArticles()) {
                        invoice.number = Invoice.nextInvoiceNumber(invoice.customer.facility)
                    }
                    invoiceService.save(invoice)
                }
            }
            on("success").to "exit"
        }

        cancel {
            if (flow.returnUrl) {
                redirect(url: flow.returnUrl)
                return
            }
            redirect(controller: "facilityInvoiceRow", action: "index")
        }

        exit {
            if (flow.returnUrl) {
                redirect(url: flow.returnUrl)
                return
            }
            redirect(controller: "facilityInvoiceRow", action: "index")
        }
    }

    protected def validateAddInvoiceRow(def sum, InvoiceRow row, InvoiceDetailsCommand cmd) {
        if (cmd.createNoCreditInvoices) {
            if (sum < 0) {
                if (row.getTotalIncludingVAT() > 0) {
                    return true
                }
                return false

            } else {
                return true
            }
        }

        return true
    }
}

@Validateable(nullable = true)
class InvoiceRowsCommand implements Serializable {
    List<InvoiceRowCommand> rows = [].withLazyDefault {new InvoiceRowCommand()}

    void clearNullRows() {
        rows.removeAll { it == null }
    }

    def hasRowId(def rowId) {
        return rows.findIndexOf { it.rowId == rowId } > -1
    }

    static constraints = {
        rows(validator: { val, obj ->

            // remove null rows
            obj.clearNullRows()

            return val.every { it.validate() }
        }, minSize: 1)
    }
}

@Validateable(nullable = true)
class InvoiceRowCommand implements Serializable {
    BigDecimal rowId
    BigDecimal price
    BigDecimal vat
    BigDecimal discount = 0
    InvoiceRow.DiscountType discountType
    String description
    String account
    int amount = 1
    String itemId

    InvoiceRow toInvoiceRow(Customer customer, User createdBy) {
        def row = new InvoiceRow()
        if (rowId) {
            row = InvoiceRow.get(rowId)
        }

        row.customer = customer
        row.price = price
        row.amount = amount
        row.account = account
        row.discount = (discount ?: 0)
        row.discountType = discountType
        row.vat = vat
        row.description = description
        row.createdBy = createdBy

        if (!itemId == null || itemId?.size() > 0) {
            row.externalArticleId = itemId
        }

        return row
    }

    static constraints = {
        rowId(nullable: true)
        price(nullable: false, blank: false)
        vat(nullable: false)
        account(nullable: true)
        description(nullable: false, blank: false)
        amount(min: 1)
        discount(nullable: true)
        discountType(nullable: false)
        itemId(nullable: true)
    }
}

@Validateable(nullable = true)
class InvoiceDetailsCommand implements Serializable {
    Long id
    Integer expirationDays
    LocalDate invoiceDate
    String text
    boolean createNoCreditInvoices = true
    boolean useInvoiceFees = false

    def getExpirationDate() {
        return invoiceDate.plusDays(expirationDays)
    }

    static InvoiceDetailsCommand create(Invoice invoice) {
        InvoiceDetailsCommand cmd = new InvoiceDetailsCommand()
        cmd.id = invoice.id
        cmd.text = invoice.text
        cmd.invoiceDate = invoice.invoiceDate
        cmd.expirationDays = invoice.getExpirationDays()
        cmd
    }

    static constraints = {
        text(nullable: true, maxSize: 1000, matches: "[^\\t]+")
        expirationDays(nullable: false, min: 0)
    }
}
