package com.matchi.fortnox

import com.matchi.Customer
import com.matchi.Facility
import com.matchi.FacilityProperty
import com.matchi.StringHelper
import com.matchi.User
import com.matchi.api.ArticleCommand
import com.matchi.external.ExternalSynchronizationEntity
import com.matchi.facility.Organization
import com.matchi.fortnox.v3.FortnoxArticle
import com.matchi.fortnox.v3.FortnoxCustomer
import com.matchi.fortnox.v3.FortnoxInvoice
import com.matchi.fortnox.v3.FortnoxInvoiceRow
import com.matchi.invoice.Invoice
import com.matchi.invoice.InvoiceRow
import org.joda.time.LocalDate

/**
 * @author Michael Astreiko
 */
class FortnoxFacadeService {
    static transactional = false
    def fortnox3CustomerService
    def fortnox3InvoiceService
    def fortnox3ArticleService
    def springSecurityService
    def externalSynchronizationService
    def messageSource

    List listArticles(Facility facility = getUserFacility()) {
        if (isFacilityUsingFortnoxV3(facility)) {
            return fortnox3ArticleService.list(facility)
        }
    }

    List listMatchiArticles() {
        return fortnox3ArticleService.list(null)
    }

    List listArticlesForOrganization(Organization organization) {
        return fortnox3ArticleService.listForOrganization(organization)
    }

    List listCustomers(Facility facility = getUserFacility()) {
        if (isFacilityUsingFortnoxV3(facility)) {
            return fortnox3CustomerService.list(facility)
        }
    }

    List listFullyPaidInvoices(Facility facility = getUserFacility(), Date lastModified = null) {
        if (isFacilityUsingFortnoxV3(facility)) {
            return fortnox3InvoiceService.list(facility, FortnoxInvoice.Filters.FULLY_PAID, lastModified)
        }
    }

    List listInvoices(Facility facility = getUserFacility(), Date lastModified = null) {
        return fortnox3InvoiceService.list(facility, null, lastModified)
    }

    List listInvoicesForOrganization(Organization organization, Date lastModified = null) {
        return fortnox3InvoiceService.listForOrganization(organization, null, lastModified)
    }

    List listMatchiInvoices(Date lastModified = null, Map params = null) {
        return fortnox3InvoiceService.list(null, null, lastModified, null, params)
    }

    def previewMatchiInvoice(String documentNumber) {
        return fortnox3InvoiceService.preview(null, documentNumber)
    }

    void cancelInvoice(Invoice invoice) {
        def externalId = externalSynchronizationService.getFortnoxInvoiceNumber(invoice)
        if (invoice.organization) {
            fortnox3InvoiceService.cancelForOrganization(invoice.organization, externalId)
        } else if (isFacilityUsingFortnoxV3(invoice.customer?.facility)) {
            fortnox3InvoiceService.cancel(invoice.customer?.facility, externalId)
        }
    }

    FortnoxInvoice getFortnoxInvoice(Invoice invoice, String documentNumber) {
        if (invoice.organization) {
            return fortnox3InvoiceService.getForOrganization(invoice.organization, documentNumber)
        } else if (isFacilityUsingFortnoxV3(invoice.customer?.facility)) {
            return fortnox3InvoiceService.get(invoice.customer?.facility, documentNumber)
        }
        return null
    }

    def saveMatchiCustomerToFortnox(Customer customer, Facility facility = getUserFacility()) {
        if (isFacilityUsingFortnoxV3(facility)) {
            FortnoxCustomer fortnoxCustomer = toFortnox3Customer(customer)
            fortnoxCustomer = fortnox3CustomerService.set(facility, fortnoxCustomer)
            if(customer.deleted) {
                externalSynchronizationService.delete(ExternalSynchronizationEntity.ExternalSystem.FORTNOX,
                        facility?.getFortnoxAuthentication()?.db, customer, fortnoxCustomer.id)
            } else {
                externalSynchronizationService.markSynchronized(ExternalSynchronizationEntity.ExternalSystem.FORTNOX,
                        facility?.getFortnoxAuthentication()?.db, customer, fortnoxCustomer.id)
            }

            return fortnoxCustomer
        }
    }

    FortnoxCustomer saveMatchiOrganizationCustomerToFortnox(Customer customer, Organization organization) {
        FortnoxCustomer fortnoxCustomer = toFortnox3Customer(customer, organization)
        fortnoxCustomer = fortnox3CustomerService.setForOrganization(organization, fortnoxCustomer)
        if(customer.deleted) {
            externalSynchronizationService.delete(ExternalSynchronizationEntity.ExternalSystem.FORTNOX,
                    organization.fortnoxAccessToken, customer, fortnoxCustomer.id)
        } else {
            externalSynchronizationService.markSynchronized(ExternalSynchronizationEntity.ExternalSystem.FORTNOX,
                    organization.fortnoxAccessToken, customer, fortnoxCustomer.id)
        }

        return fortnoxCustomer
    }

    def saveMatchiInvoiceToFortnox(Invoice invoice) {
        def accessToken
        def result
        if (invoice.organization) {
            FortnoxInvoice fortnoxInvoice = toFortnox3Invoice(invoice)
            if (!fortnoxInvoice.CustomerNumber) {
                log.info("Customer ${invoice.customer} is not synchronized to fortnox, synchronizing now...")
                fortnoxInvoice.CustomerNumber = saveMatchiOrganizationCustomerToFortnox(invoice.customer, invoice.organization)?.id
            }
            result = fortnox3InvoiceService.setForOrganization(invoice.organization, fortnoxInvoice)
            accessToken = invoice.organization.fortnoxAccessToken
        } else {
            def facility = invoice?.customer?.facility
            FortnoxInvoice fortnoxInvoice = toFortnox3Invoice(invoice)
            if (!fortnoxInvoice.CustomerNumber) {
                log.info("Customer ${invoice.customer} is not synchronized to fortnox, synchronizing now...")
                fortnoxInvoice.CustomerNumber = saveMatchiCustomerToFortnox(invoice.customer, facility)?.id
            }
            result = fortnox3InvoiceService.set(facility, fortnoxInvoice)
            accessToken = facility.getFortnoxAuthentication()?.db
        }
        externalSynchronizationService.markSynchronized(
                ExternalSynchronizationEntity.ExternalSystem.FORTNOX, accessToken,
                invoice, result.id)
        return result
    }

    def saveMatchiInternalInvoiceToFortnox(FortnoxInvoice invoice) {
        return fortnox3InvoiceService.set(null, invoice)
    }

    Date getInvoiceDatePaid(FortnoxInvoice listInvoice, Invoice invoice) {
        if (invoice.organization) {
            return fortnox3InvoiceService.getInvoiceDatePaidForOrganization(invoice.organization, listInvoice?.DocumentNumber)
        } else {
            Facility facility = invoice.customer?.facility
            if (isFacilityUsingFortnoxV3(facility)) {
                return fortnox3InvoiceService.getInvoiceDatePaid(facility, listInvoice?.DocumentNumber)
            }
        }
    }

    BigDecimal getAmountPaid(listInvoice, Facility facility = getUserFacility()) {
        if (isFacilityUsingFortnoxV3(facility)) {
            return listInvoice?.getAmountPaid()
        }
    }

    private FortnoxCustomer toFortnox3Customer(Customer customer, Organization organization = null) {
        def externalId = externalSynchronizationService.getFortnoxCustomerNumber(customer, organization)
        def isCompany = Customer.CustomerType.ORGANIZATION.equals(customer.getType())

        FortnoxCustomer fortnoxCustomer     = new FortnoxCustomer()

        fortnoxCustomer.CustomerNumber      = externalId
        fortnoxCustomer.CountryCode         = customer.country
        fortnoxCustomer.Active              = !customer.isArchivedOrDeleted()
        fortnoxCustomer.Name                = customer.fullName()
        fortnoxCustomer.Type                = (isCompany ? "COMPANY" : "PRIVATE")
        fortnoxCustomer.Email               = customer.email
        fortnoxCustomer.Address1            = customer.invoiceAddress1 ?: customer.address1
        fortnoxCustomer.Address2            = customer.invoiceAddress2 ?: customer.address2
        fortnoxCustomer.City                = customer.invoiceCity ?: customer.city
        fortnoxCustomer.ZipCode             = customer.invoiceZipcode ?: customer.zipcode
        fortnoxCustomer.Comments            = customer.notes
        fortnoxCustomer.OrganisationNumber  = isCompany ? customer.orgNumber : customer.getPersonalNumber()
        fortnoxCustomer.Phone1              = customer.invoiceTelephone ?: customer.telephone
        fortnoxCustomer.Phone2              = customer.cellphone
        fortnoxCustomer.YourReference       = customer.invoiceContact ?: customer.contact
        fortnoxCustomer.EmailInvoice        = customer.getCustomerInvoiceEmail()

        fortnoxCustomer.DeliveryName        = customer.invoiceContact
        fortnoxCustomer.DeliveryAddress1    = customer.invoiceAddress1
        fortnoxCustomer.DeliveryAddress2    = customer.invoiceAddress2
        fortnoxCustomer.DeliveryZipCode     = StringHelper.safeSubstring(customer.invoiceZipcode, 0, 10)
        fortnoxCustomer.DeliveryCity        = customer.invoiceCity

        return fortnoxCustomer
    }

    /**
     * Converts a Matchi invoice to Fortnox 3 invoice
     * @param invoice Matchi invoice
     * @param authentication Fortnox authentication
     * @return A fortnox3 invoice row
     */
    private FortnoxInvoice toFortnox3Invoice(Invoice invoice) {
        FortnoxInvoice fortnoxInvoice = new FortnoxInvoice()
        def externalInvoiceId = externalSynchronizationService.getFortnoxInvoiceNumber(invoice)
        FortnoxCustomer customer = toFortnox3Customer(invoice.customer, invoice?.organization)

        fortnoxInvoice.DocumentNumber = externalInvoiceId
        fortnoxInvoice.CustomerNumber = customer.CustomerNumber
        fortnoxInvoice.CustomerName = getFortnox3InvoiceCustomerName(invoice, customer)
        fortnoxInvoice.InvoiceDate = invoice.invoiceDate?.toDate()
        fortnoxInvoice.DueDate = invoice.expirationDate?.toDate()
        fortnoxInvoice.Remarks = invoice.text
        fortnoxInvoice.VATIncluded = false
        fortnoxInvoice.CostCenter = getCostCenterCode(invoice)

        invoice.rows.each { InvoiceRow row ->
            fortnoxInvoice.InvoiceRows.add(toFortnox3InvoiceRow(row))
        }
        fortnoxInvoice.InvoiceRows.sort()
        return fortnoxInvoice
    }

    private FortnoxInvoiceRow toFortnox3InvoiceRow(InvoiceRow row) {
        FortnoxInvoiceRow fortnoxInvoiceRow = new FortnoxInvoiceRow()
        fortnoxInvoiceRow.Description = row.description
        fortnoxInvoiceRow.Unit = row.unit
        fortnoxInvoiceRow.Price = row.getPriceExcludingVAT()
        //Fortnox 3 does not allow use vat another from sed in their system
        fortnoxInvoiceRow.VAT = row.vat ? row.vat.toInteger() : 0
        fortnoxInvoiceRow.DeliveredQuantity = row.amount
        fortnoxInvoiceRow.Discount = row.discount.toFloat()
        fortnoxInvoiceRow.DiscountType = row.discountType.name()
        fortnoxInvoiceRow.ArticleNumber = row.externalArticleId
        fortnoxInvoiceRow
    }

    private String getFortnox3InvoiceCustomerName(Invoice invoice, FortnoxCustomer fortnoxCustomer) {
        Customer customer = invoice.customer
        if (customer.facility?.isSwedish() && customer.personalNumber && customer.securityNumber && customer.age < 18 && customer.guardianName) {
            log.info("Invoice ${invoice.id}: Customer is under 18, changing CustomerName to \"To the guardian of ...\"")
            return messageSource.getMessage("facilityInvoice.send.customerNameToGuardian", [fortnoxCustomer.Name] as String[], Locale.forLanguageTag("sv"))
        }
        return fortnoxCustomer.Name
    }

    private Facility getUserFacility() {
        User user = springSecurityService.currentUser
        return user?.facility
    }

    private boolean isFacilityUsingFortnoxV3(Facility facility) {
        FacilityProperty.countByKeyAndFacility(
                FacilityProperty.FacilityPropertyKey.FORTNOX3_ACCESS_TOKEN.name(), facility) > 0
    }

    private String getCostCenterCode(Invoice invoice) {
        invoice.organization?.fortnoxCostCenter ?:
                invoice.customer.facility.getFacilityPropertyValue(
                        FacilityProperty.FacilityPropertyKey.FORTNOX3_COST_CENTER)
    }

    /**
     * Sets the right invoice status on matchi invoice based on fortnox invoice
     * @param invoice
     * @param fortnoxInvoice
     */
    def processInvoiceStatus(Invoice invoice, def fortnoxInvoice) {
        log.info "Process invoice: " +
                "invoice.id=${invoice?.id} " +
                "invoice.status=${invoice?.status}, " +
                "fortnoxInvoice.Cancelled=${fortnoxInvoice?.Cancelled}, " +
                "fortnoxInvoice.Sent=${fortnoxInvoice?.Sent}, " +
                "fortnoxInvoice.Balance=${fortnoxInvoice?.Balance}, " +
                "fortnoxInvoice.DueDate=${fortnoxInvoice?.DueDate}, " +
                "fortnoxInvoice.Booked=${fortnoxInvoice?.Booked}"

        //Update only not credited status if necessary
        if (invoice.status != Invoice.InvoiceStatus.CREDITED) {
            def newStatus = Invoice.InvoiceStatus.READY
            if (fortnoxInvoice.Cancelled) {
                newStatus = Invoice.InvoiceStatus.CANCELLED
            } else if (fortnoxInvoice.Balance <= 0) {
                newStatus = Invoice.InvoiceStatus.PAID
            } else if (fortnoxInvoice.Balance > 0 && fortnoxInvoice.DueDate < new LocalDate().toDate()) {
                newStatus = Invoice.InvoiceStatus.OVERDUE
            } else if (fortnoxInvoice.Booked) {
                newStatus = Invoice.InvoiceStatus.POSTED
            }
            log.info(" => Processed status on invoice (${invoice?.id}) from ${invoice.status} to ${newStatus}")
            invoice.status = newStatus
        }
    }

    /**
     * Sets the right pay date on matchi invoice based on fortnox invoice
     * @param invoice Matchi invoice
     * @param fortnoxInvoiceId Fortnox invoice id
     * @param invoicesById List of invoices grouped by id
     */
    def processInvoiceFinalPayDate(Invoice invoice, def fortnoxInvoice) {
        if (!invoice.paidDate && fortnoxInvoice && fortnoxInvoice.Balance <= 0) {
            def endPayDate = new LocalDate(getInvoiceDatePaid(fortnoxInvoice, invoice))
            invoice.paidDate = endPayDate

            log.info(" => Processed endpay date (${endPayDate}) on invoice (${endPayDate})")
        }

    }

    def processInvoiceOutputStatus(Invoice invoice, def fortnoxInvoice) {
        invoice.sent = fortnoxInvoice.Sent ? Invoice.InvoiceSentStatus.SENT : Invoice.InvoiceSentStatus.NOT_SENT
        log.info(" => Processed last output on invoice (Sent: ${fortnoxInvoice.Sent} / ${invoice.sent})")

    }

    List articleCommands(Facility facility, Organization organization = null) {
        List<ArticleCommand> cmds = new ArrayList<>()
        List<FortnoxArticle> fortnoxArticles = !organization ? listArticles(facility) : listArticlesForOrganization(organization)

        fortnoxArticles.each {fortnoxArticle ->
            log.debug("Received FortnoxArticle for ${facility.name}: ${fortnoxArticle.toString()}")

            try {
                ArticleCommand cmd = new ArticleCommand()
                cmd.articleNumber = fortnoxArticle.getArticleNumber() ? Long.valueOf(fortnoxArticle.getArticleNumber()) : null
                cmd.name = fortnoxArticle.getDescr()
                cmd.description = fortnoxArticle.getDescription()
                cmd.price = fortnoxArticle.getSalesPrice() ? Float.valueOf(fortnoxArticle.getSalesPrice()) : 0

                // MATCHi only support VAT as integer so rounding will work.
                cmd.vat = fortnoxArticle.getVAT() ? Math.round(fortnoxArticle.getVAT()) : 0

                cmds.add(cmd)
            } catch (Exception e) {
                log.warn("Unable to parse FortnoxArticle for ${facility.name}: ${fortnoxArticle.toString()}")
            }
        }

        return cmds;
    }
}
