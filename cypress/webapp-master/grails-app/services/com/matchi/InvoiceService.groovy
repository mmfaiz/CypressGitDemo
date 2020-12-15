package com.matchi

import com.matchi.api.ArticleCommand
import com.matchi.api.InvoiceUpdateCommand
import com.matchi.facility.FilterInvoiceCommand
import com.matchi.facility.FilterInvoiceRowCommand
import com.matchi.facility.Organization
import com.matchi.fortnox.v3.FortnoxException
import com.matchi.invoice.Invoice
import com.matchi.invoice.InvoiceArticle
import com.matchi.invoice.InvoiceRow
import com.matchi.orders.InvoiceOrderPayment
import com.matchi.orders.Order
import com.matchi.orders.OrderPayment
import com.matchi.payment.PaymentMethod
import com.matchi.subscriptionredeem.SlotRedeem
import grails.orm.PagedResultList
import grails.transaction.Transactional
import org.apache.commons.lang3.StringUtils
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsParameterMap
import org.hibernate.criterion.CriteriaSpecification
import org.joda.time.LocalDate

class InvoiceService {

    public static final String PRINT_TEMPLATE_ROOT_URI = "/rendering/invoice"

    // Real number is 1000 but due to trailing spaces, we have some margin
    public static final int TEXT_MAX_INPUT_SIZE = 970

    static transactional = false

    def groovyPageLocator
    def fortnoxFacadeService
    UserService userService
    def externalSynchronizationService
    def springSecurityService
    OrderStatusService orderStatusService

    @Transactional
    void save(Invoice invoice) {
        def user = userService.getLoggedInUser()
        def facility = user.facility
        log.info("Creating invoice on customer ${invoice.customer.fullName()}")

        if (invoice.save(flush: true)) {
            if (facility.hasFortnox() || (invoice.organization && invoice.organization.fortnoxAccessToken)) {
                try {
                    saveToFortnox(invoice, facility)
                } catch (ex) {
                    log.error("Error occurred during saving to Fortnox: ${ex.message}", ex)
                }
            }
        }
    }

    @Transactional
    def cancel(Invoice invoice) {
        def user = userService.getLoggedInUser()
        def facility = user.facility
        def oldStatus = invoice.status

        invoice.status = Invoice.InvoiceStatus.CANCELLED
        invoice.rows.each {
            removeFromSubscription(it)
        }

        invoice.save()

        if (oldStatus != Invoice.InvoiceStatus.INCORRECT && invoice.number != null &&
                (facility.hasFortnox() || (invoice.organization && invoice.organization.fortnoxAccessToken))) {
            fortnoxFacadeService.cancelInvoice(invoice)
        }
    }

    @Transactional
    def delete(InvoiceRow row) {
        removeFromSubscription(row)
        removeFromSubscriptionRedeem(row)

        def transaction = PaymentTransaction.findByInvoiceRow(row)

        if (transaction) {
            def booking = Booking.findByPayment(transaction?.payment)
            if (booking) {
                booking.clearPayments()
                booking.save()
            } else {
                transaction.invoiceRow = null
                transaction.save()
            }
        }

        def orderPayment = InvoiceOrderPayment.findByInvoiceRow(row)

        if (orderPayment) {

            if (orderPayment.method.equals(PaymentMethod.INVOICE)) {
                orderPayment.orders.each { Order order ->
                    order.removeFromPayments(orderPayment)

                    if (!(order.payments?.size() > 0)) {
                        order.status = Order.Status.NEW
                    }

                    order.save()
                }

                orderPayment.delete()
                row.delete()
            } else {
                orderPayment.refund(row.price)
                orderPayment.save()

                orderPayment.orders.each {
                    if (it.booking && !it.isFinalPaid()) {
                        it.booking.save()
                    }
                }
            }

        } else {
            row.delete()
        }
    }

    @Transactional
    def removeFromSubscription(InvoiceRow row) {
        Subscription.findByInvoiceRow(row).each {
            it.invoiceRow = null
            it.save()
        }
    }

    @Transactional
    def removeFromSubscriptionRedeem(InvoiceRow row) {
        SlotRedeem.findByInvoiceRow(row).each {
            it.invoiceRow = null
            it.save()
        }
    }

    def getInvoiceRows(FilterInvoiceRowCommand filter) {
        def user = userService.getLoggedInUser()
        def facility = user.facility

        return InvoiceRow.createCriteria().list(max: filter.allselected ? 100000 : filter.max, offset: filter.allselected ? 0 : filter.offset) {
            createAlias("customer", "c", CriteriaSpecification.LEFT_JOIN)
            between("dateCreated", filter.start.toDateMidnight().toDateTime(), filter.end.plusDays(1).toDateMidnight().toDateTime())

            if (filter.q) {
                or {
                    def q = StringUtils.replace(filter.q, "_", "\\_")

                    like("description", "%${q}%")
                    like("c.email", "%${q}%")
                    like("c.companyname", "%${q}%")
                    like("externalArticleId", "%${q}%")
                    if (q.isLong()) {
                        eq("c.number", q.toLong())
                    }
                    sqlRestriction("concat(firstname,' ',lastname) like ?", ["%${q}%" as String])
                }
            }

            if (filter.organizations) {
                def organizationIds = filter.organizations.clone()
                if (organizationIds.contains(-1l)) {
                    organizationIds.remove(-1l)
                    if (organizationIds) {
                        or {
                            organization(CriteriaSpecification.LEFT_JOIN) {
                                inList("id", organizationIds)
                            }
                            isNull('organization')
                        }
                    } else {
                        isNull('organization')
                    }
                } else {
                    organization {
                        inList("id", organizationIds)
                    }
                }
            }

            eq("c.facility", facility)
            isNull("invoice")

            and {
                def sort = filter.sort.tokenize(",")
                sort.each {
                    order(it, filter.order)
                }
            }

        }
    }

    def createInvoiceFeeRowIfNeeded(Customer customer, List<InvoiceRow> rows, User user) {
        def facility = customer.facility
        def invoiceFeeArticles = facility.invoiceFeeArticles.toList()
        def createInvoiceFeeRow = rows.find {
            it.externalArticleId in invoiceFeeArticles || (!it.externalArticleId && ("0" in invoiceFeeArticles))
        }

        if (createInvoiceFeeRow) {
            InvoiceRow row = new InvoiceRow()
            row.customer = customer
            row.externalArticleId = facility.invoiceFeeExternalArticleId
            row.organization = getOrganization(facility.invoiceFeeOrganizationId)
            row.description = facility.invoiceFeeDescription
            row.price = facility.invoiceFeeAmount
            row.amount = 1
            row.vat = 0l
            row.discount = 0
            row.createdBy = user

            return row
        }
    }

    InvoiceRow createFamilyMembershipDiscountRow(Customer customer, List<InvoiceRow> rows,
                                                 User user, BigDecimal maxAmount) {
        if (customer.membership?.family) {
            def sum = rows.sum(0) { row ->
                def o = Order.withCriteria {
                    createAlias("payments", "p")
                    eq("p.invoiceRow", row)
                    eq("article", Order.Article.MEMBERSHIP)
                    projections {
                        rowCount()
                    }
                }[0]
                o ? row.getTotalIncludingVAT() : 0
            }
            if (sum > maxAmount) {
                return new InvoiceRow(customer: customer, createdBy: user, price: maxAmount - sum,
                        vat: 0, discount: 0, amount: 1, description: "Familjemedlemskap - rabatt")
            }
        }

        null
    }

    def nextInvoiceNumber(def facility) {

        def maxInvoiceNumber = Invoice.createCriteria().get() {
            createAlias("customer", "c", CriteriaSpecification.LEFT_JOIN)
            eq("c.facility", facility)
            projections {
                max("invoiceNumber")
            }

        }
        maxInvoiceNumber + 1

    }

    /**
     * Saves or upates an invoice to Fortnox
     * @param invoice
     * @param authentication
     * @return The updated invoice (with external Id)
     */
    void saveToFortnox(Invoice invoice, Facility facility) {
        def logMsg = "facility=${facility?.name}, id=${invoice?.id}, number=${invoice?.number}, rows=${invoice?.rows?.size()}"
        if (invoice?.rows?.size() > 0) {
            log.info("Saving invoice to Fortnox: ${logMsg}.")
        } else {
            log.warn("Saving invoice to Fortnox without any rows: ${logMsg}.")
        }

        Invoice.withNewTransaction {
            if (isFortnoxUpdateAllowed(invoice)) {
                try {
                    def result = fortnoxFacadeService.saveMatchiInvoiceToFortnox(invoice)
                    // settings fortnox invoice number
                    invoice.number = Long.parseLong(result.id)
                    //rollback status to READY for incorrect invoices
                    if (invoice.status == Invoice.InvoiceStatus.INCORRECT) {
                        invoice.status = Invoice.InvoiceStatus.READY
                    }
                } catch (com.matchi.fortnox.v3.FortnoxException ex) {
                    log.error "Error during saving of Invoice ${invoice?.id} to Fortnox: ${ex.message}", ex
                    invoice.number = null
                    invoice.status = Invoice.InvoiceStatus.INCORRECT
                    invoice.errors.reject(FortnoxException.ERROR_CODE, ex.message)
                }

            }
        }
    }

    boolean isFortnoxUpdateAllowed(invoice) {
        return true
    }

    List getItemsForFacilityWithOrganization(Facility facility, Organization organization) {
        (organization && organization.fortnoxAccessToken) ? getItemsForOrganization(organization.id) : getItems(facility)
    }

    @Transactional
    void updateItems(Long facilityId, Long organizationId = null, List<ArticleCommand> items) {
        log.info("Updating ${items.size()} articles for facilityId=${facilityId}, organizationId=${organizationId}")

        // Get all existing articles for the facility or organization
        List<InvoiceArticle> existingArticles = getArticleItems(facilityId, organizationId)

        // Iterate the incoming articles
        items.each { item ->
            // Find an incoming item in the list of existing articles
            InvoiceArticle invoiceArticle = existingArticles.findAll {
                it.articleNumber == item.articleNumber
            }[0]

            if (invoiceArticle) {
                log.debug("Updating existing article: facilityId=${facilityId}, organizationId=${organizationId}, ${invoiceArticle.toString()}")
            } else {
                log.info("Creating new article: facilityId=${facilityId}, organizationId=${organizationId}, ${item.toString()}")
                invoiceArticle = new InvoiceArticle()
                invoiceArticle.facilityId = facilityId
                invoiceArticle.organizationId = organizationId
                invoiceArticle.articleNumber = item.articleNumber
            }

            invoiceArticle.name = item.name
            invoiceArticle.description = item.description
            invoiceArticle.firstPrice = item.price
            invoiceArticle.salesPrice = item.price
            invoiceArticle.vat = item.vat
            invoiceArticle.save(failOnError: true)
        }

        // Find and remove all existing articles that are not in the list if incoming articles.
        existingArticles.each { existingArticle ->
            boolean found = items.findAll {
                it.articleNumber == existingArticle.articleNumber
            }.any()

            if (!found) {
                log.info("Deleting article: facilityId=${facilityId}, organizationId=${organizationId}, ${existingArticle.toString()}")
                existingArticle.delete(failOnError: true)
            }
        }
    }

    List getItems(Facility facility) {
        return formatFortnox(getArticleItems(facility?.id))
    }

    List<InvoiceArticle> getArticleItems(Long facilityId, Long organizationId = null) {
        List<InvoiceArticle> invoiceArticles = InvoiceArticle.withCriteria {
            eq("facilityId", facilityId)

            // Important to separate articles between facility and its organization(s)!
            if (!organizationId) {
                isNull("organizationId")
            } else {
                eq("organizationId", organizationId)
            }

            order("articleNumber", "asc")
        }
        return invoiceArticles
    }

    def formatFortnox(List<InvoiceArticle> invoiceArticles) {
        invoiceArticles.collect {
            [
                    id         : String.valueOf(it.articleNumber),
                    descr      : it.name,
                    Description: it.description,
                    firstPrice : it.firstPrice,
                    SalesPrice : it.salesPrice,
                    VAT        : it.vat
            ]
        }
    }

    def getFortnoxItems(Long facilityId) {
        formatFortnox(getArticleItems(facilityId))
    }

    List getItemsForOrganization(Long organizationId) {
        formatFortnox(getArticleItems(Organization.get(organizationId)?.facility?.id, organizationId))
    }

    Organization getOrganization(Long organizationId) {
        return Organization.get(organizationId)
    }

    @Transactional
    void markAsSentByEmail(Invoice invoice) {
        invoice.sent = Invoice.InvoiceSentStatus.EMAIL
        invoice.lastSent = new Date()
        invoice.save()
    }

    @Transactional
    InvoiceOrderPayment createInvoiceOrderPayment(Order order) {
        Slot slot = Slot.findById(order?.metadata?.slotId)

        InvoiceRow row = new InvoiceRow()
        row.customer = order.customer
        String fullDescription = (order.facility?.bookingInvoiceRowDescription ? order.facility?.bookingInvoiceRowDescription + ": " : "") + slot?.getShortDescription()
        // Strip description to max invoice description by integration limit
        row.description = StringUtils.abbreviate(fullDescription, InvoiceRow.DESCRIPTION_MAX_SIZE)
        row.externalArticleId = order.facility?.bookingInvoiceRowExternalArticleId
        row.organization = getOrganization(order.facility?.bookingInvoiceRowOrganizationId)
        row.amount = 1
        row.vat = order.facility.vat
        row.price = order.getRestAmountToPay()
        row.createdBy = (User) springSecurityService.currentUser
        row.save(failOnError: true)

        InvoiceOrderPayment payment = new InvoiceOrderPayment()
        payment.issuer = (User) springSecurityService.currentUser
        payment.amount = order.getRestAmountToPay()
        payment.vat = order.vat()
        payment.status = OrderPayment.Status.CAPTURED
        payment.invoiceRow = row

        payment.save(failOnError: true)

        order.addToPayments(payment)

        log.debug("Added payment: ${payment.id} to order: ${order.id}")

        if (order.isFinalPaid()) {
            orderStatusService.complete(order, userService.getCurrentUser())
        } else {
            orderStatusService.confirm(order, userService.getCurrentUser())
        }
        order.save(failOnError: true)

        return payment
    }

    Invoice getInvoice(Long id, Facility facility) {
        Invoice.withCriteria {
            eq("id", id)
            customer {
                eq("facility", facility)
            }
        }[0]
    }

    PagedResultList listInvoices(Facility facility, Integer max, Integer offset,
                                 LocalDate from, LocalDate to) {
        Invoice.createCriteria().list(max: max, offset: offset) {
            customer {
                eq("facility", facility)
            }
            if (from) {
                ge("invoiceDate", from)
            }
            if (to) {
                le("invoiceDate", to)
            }
        }
    }

    @Transactional
    void updateInvoice(InvoiceUpdateCommand cmd, Facility facility) {
        def invoice = getInvoice(cmd.id, facility)
        if (invoice) {
            if (cmd.number) {
                invoice.number = cmd.number
            }
            if (cmd.dueDate && cmd.dueDate != invoice.expirationDate) {
                invoice.expirationDate = cmd.dueDate
            }

            if (invoice.status != Invoice.InvoiceStatus.CREDITED) {
                def newStatus = Invoice.InvoiceStatus.READY
                if (!invoice.number) {
                    newStatus = Invoice.InvoiceStatus.INCORRECT
                } else if (cmd.cancelled) {
                    newStatus = Invoice.InvoiceStatus.CANCELLED
                } else if (cmd.balance <= 0) {
                    newStatus = Invoice.InvoiceStatus.PAID
                    if (!invoice.paidDate) {
                        invoice.paidDate = cmd.paidDate
                    }
                } else if (cmd.balance > 0 && cmd.dueDate && cmd.dueDate <= new LocalDate()) {
                    newStatus = Invoice.InvoiceStatus.OVERDUE
                } else if (cmd.booked) {
                    newStatus = Invoice.InvoiceStatus.POSTED
                } else if (cmd.credited && cmd.creditInvoiceReference) {
                    newStatus = Invoice.InvoiceStatus.CREDITED
                }
                log.debug(" => Processed status on invoice $invoice.id from $invoice.status to $newStatus")
                invoice.status = newStatus
            }

            if (cmd.sent != null) {
                invoice.sent = cmd.sent ? Invoice.InvoiceSentStatus.SENT : Invoice.InvoiceSentStatus.NOT_SENT
            }

            invoice.save()
        }
    }

    List<Invoice> selectedInvoices(FilterInvoiceCommand filter, GrailsParameterMap params) {
        if (filter.allselected) {
            log.debug("All selected")
            return getInvoices(filter)
        }

        def invoiceIds = params.list("invoiceIds")
        List<Long> invoiceIdLongs = invoiceIds.collect { Long.parseLong(it) }
        if (!invoiceIds) {
            return []
        }

        return Invoice.createCriteria().list {
            createAlias("customer", "c", CriteriaSpecification.LEFT_JOIN)
            eq('c.facility', userService.getUserFacility())
            inList('id', invoiceIdLongs)
        }
    }

    List<Invoice> getInvoices(FilterInvoiceCommand filter) {

        def invoices = Invoice.createCriteria().list(max: filter.allselected ? Integer.MAX_VALUE : filter.max, offset: filter.offset) {
            createAlias("customer", "c", CriteriaSpecification.LEFT_JOIN)

            if (filter.status && filter.status.size() > 0) {
                inList("status", filter.statuses())
            }

            if (filter.organizations) {
                def organizationIds = filter.organizations.clone()
                if (organizationIds.contains(-1l)) {
                    organizationIds.remove(-1l)
                    if (organizationIds) {
                        or {
                            organization(CriteriaSpecification.LEFT_JOIN) {
                                inList("id", organizationIds)
                            }
                            isNull('organization')
                        }
                    } else {
                        isNull('organization')
                    }
                } else {
                    organization {
                        inList("id", organizationIds)
                    }
                }
            }

            between("invoiceDate", filter.start, filter.end.toDateMidnight().toLocalDate())

            if (filter.q) {
                or {
                    def q = StringUtils.replace(filter.q, "_", "\\_")

                    sqlRestriction("concat(firstname,' ',lastname) like ?", ["%${q}%" as String])
                    like("text", "%${q}%")
                    like("c.email", "%${q}%")
                    like("c.companyname", "%${q}%")

                    if (q.isNumber()) {
                        eq("c.number", Long.parseLong(q))
                        eq("number", Long.parseLong(q))
                    }
                }
            }

            eq("c.facility", userService.getUserFacility())

            and {
                def sort = filter.sort.tokenize(",")
                sort.each {
                    order(it, filter.order)
                }
            }

        }

        return invoices
    }

    String getPrintTemplate(Facility facility) {
        def countryPrefix = "${facility.country}"

        log.debug("Getting invoice printing template for ${facility.name}")
        def facilityTemplate = groovyPageLocator.findViewByPath("${PRINT_TEMPLATE_ROOT_URI}/${countryPrefix}/${facility.shortname}/_invoice")

        if (facilityTemplate) {
            return "${countryPrefix}/${facility.shortname}/invoice"
        }

        def countryTemplateExists = groovyPageLocator.findViewByPath("${PRINT_TEMPLATE_ROOT_URI}/${countryPrefix}/_invoice")
        return countryTemplateExists ? "${countryPrefix}/invoice" : "invoice"
    }
}
