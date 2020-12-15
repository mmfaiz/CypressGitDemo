package com.matchi.fortnox.v3

import com.matchi.Facility
import com.matchi.external.ExternalSynchronizationEntity
import com.matchi.facility.Organization
import grails.converters.JSON
import org.apache.commons.lang.BooleanUtils
import org.apache.http.HttpResponse

/**
 * @author Michael Astreiko
 */
class Fortnox3InvoiceService {
    static transactional = false
    private static final String INVOICES = "invoices"
    private static final String INVOICE_PAYMENTS = "invoicepayments"
    private static final String CANCEL_ACTION = "cancel"
    private static final String LAST_MODIFIED = "lastmodified"
    def fortnox3Service

    /**
     *
     * @param invoice
     * @return
     */
    FortnoxInvoice set(Facility facility, FortnoxInvoice invoice) {
        FortnoxInvoice result = null
        def requestBody = [Invoice: parseFortnoxInvoice(invoice)]
        log.debug(requestBody as JSON)

        if (invoice.id) { //updating existing
            fortnox3Service.doPut(facility, INVOICES, invoice.id, null, requestBody) { def invoiceJSON ->
                log.info("Updated invoice with number ${invoiceJSON.Invoice.DocumentNumber}")
                result = new FortnoxInvoice(getInvoicePropertiesBasedOnJSON(invoiceJSON.Invoice))
            }
        } else {//new invoice
            fortnox3Service.doPost(facility, INVOICES, requestBody) { def invoiceJSON ->
                log.info("Created invoice with number ${invoiceJSON.Invoice.DocumentNumber}")
                result = new FortnoxInvoice(getInvoicePropertiesBasedOnJSON(invoiceJSON.Invoice))
            }
        }
        return result
    }

    /**
     *
     * @param invoice
     * @return
     */
    FortnoxInvoice setForOrganization(Organization organization, FortnoxInvoice invoice) {
        FortnoxInvoice result = null
        def requestBody = [Invoice: parseFortnoxInvoice(invoice)]
        if (invoice.id) { //updating existing
            fortnox3Service.doPutForOrganization(organization, INVOICES, invoice.id, null, requestBody) { def invoiceJSON ->
                log.info("Updated invoice with number ${invoiceJSON.Invoice.DocumentNumber}")
                result = new FortnoxInvoice(getInvoicePropertiesBasedOnJSON(invoiceJSON.Invoice))
            }
        } else {//new invoice
            fortnox3Service.doPostForOrganization(organization, INVOICES, requestBody) { def invoiceJSON ->
                log.info("Created invoice with number ${invoiceJSON.Invoice.DocumentNumber}")
                result = new FortnoxInvoice(getInvoicePropertiesBasedOnJSON(invoiceJSON.Invoice))
            }
        }
        return result
    }

    /**
     *
     * @param documentNumber
     * @return
     */
    FortnoxInvoice get(Facility facility, String documentNumber) {
        FortnoxInvoice result = null
        fortnox3Service.doGet(facility, INVOICES, documentNumber) { def invoiceJSON ->
            result = new FortnoxInvoice(getInvoicePropertiesBasedOnJSON(invoiceJSON.Invoice))
        }
        result
    }

    /**
     *
     * @param documentNumber
     * @return
     */
    FortnoxInvoice getForOrganization(Organization organization, String documentNumber) {
        FortnoxInvoice result = null
        fortnox3Service.doGetForOrganization(organization, INVOICES, documentNumber) { def invoiceJSON ->
            result = new FortnoxInvoice(getInvoicePropertiesBasedOnJSON(invoiceJSON.Invoice))
        }
        result
    }

    /**
     *
     * @param documentNumber
     * @return
     */
    Date getInvoiceDatePaid(Facility facility, String documentNumber) {
        Date result = null
        fortnox3Service.doGet(facility, INVOICE_PAYMENTS, null, ["invoicenumber": documentNumber]) { def invoicePaymentsJSON ->
            def invoicePaymentNumber = invoicePaymentsJSON.InvoicePayments ? invoicePaymentsJSON.InvoicePayments[0].Number : null
            if (invoicePaymentNumber) {
                fortnox3Service.doGet(facility, INVOICE_PAYMENTS, invoicePaymentNumber) { def invoicePaymentJSON ->
                    result = Date.parse(Fortnox3Service.FORTNOX_API_DATE_FORMAT, invoicePaymentJSON.InvoicePayment.PaymentDate)
                }
            }
        }
        result
    }

    /**
     *
     * @param documentNumber
     * @return
     */
    Date getInvoiceDatePaidForOrganization(Organization organization, String documentNumber) {
        Date result = null
        fortnox3Service.doGetForOrganization(organization, INVOICE_PAYMENTS, null, ["invoicenumber": documentNumber]) { def invoicePaymentsJSON ->
            def invoicePaymentNumber = invoicePaymentsJSON.InvoicePayments ? invoicePaymentsJSON.InvoicePayments[0].Number : null
            if (invoicePaymentNumber) {
                fortnox3Service.doGetForOrganization(organization, INVOICE_PAYMENTS, invoicePaymentNumber) { def invoicePaymentJSON ->
                    result = Date.parse(Fortnox3Service.FORTNOX_API_DATE_FORMAT, invoicePaymentJSON.InvoicePayment.PaymentDate)
                }
            }
        }
        result
    }

    /**
     *
     * @param lastModified date that we need to format to yyyy-MM-dd (2014-03-15)
     * @param filter
     * @return
     */
    List<FortnoxInvoice> list(Facility facility, FortnoxInvoice.Filters filter = null,
                              Date lastModified = null, Integer page = null, Map params = null) {
        def result = []
        def requestParams = filter ? ['filter': filter.filterId] : [:]

        if (params) {
            params.each{ k, v -> requestParams.putAll(params) }
        }

        //Max possible based on http://developer.fortnox.se/documentation/general/parameters/
        requestParams['limit'] = 500
        if (lastModified) {
            requestParams[LAST_MODIFIED] = lastModified.format(Fortnox3Service.FORTNOX_API_DATE_FORMAT)
        }
        if(page) {
            requestParams['page'] = page
        }
        fortnox3Service.doGet(facility, INVOICES, null, requestParams) { def responseJSON ->
            responseJSON.Invoices?.each { invoiceJSON ->
                try {
                    if (validateCurrency(facility?.currency, invoiceJSON)) {
                        result << new FortnoxInvoice(getInvoicePropertiesBasedOnJSON(invoiceJSON))
                    }
                } catch (ex) {
                    log.error "Fail to process Fortnox Invoice for ${facility?.name}: ${invoiceJSON}: $ex.message", ex
                }
            }
            if(responseJSON.MetaInformation.'@TotalPages' > responseJSON.MetaInformation.'@CurrentPage') {
                result += list(facility, filter, lastModified, responseJSON.MetaInformation.'@CurrentPage' + 1)
            }
        }
        result
    }

    /**
     *
     * @param lastModified date that we need to format to yyyy-MM-dd (2014-03-15)
     * @param filter
     * @return
     */
    List<FortnoxInvoice> listForOrganization(Organization organization, FortnoxInvoice.Filters filter = null,
                              Date lastModified = null, Integer page = null) {
        def result = []
        def requestParams = filter ? ['filter': filter.filterId] : [:]
        //Max possible based on http://developer.fortnox.se/documentation/general/parameters/
        requestParams['limit'] = 500
        if (lastModified) {
            requestParams[LAST_MODIFIED] = lastModified.format(Fortnox3Service.FORTNOX_API_DATE_FORMAT)
        }
        if(page) {
            requestParams['page'] = page
        }
        fortnox3Service.doGetForOrganization(organization, INVOICES, null, requestParams) { def responseJSON ->
            responseJSON.Invoices?.each { invoiceJSON ->
                try {
                    if (validateCurrency(organization?.facility?.currency, invoiceJSON)) {
                        result << new FortnoxInvoice(getInvoicePropertiesBasedOnJSON(invoiceJSON))
                    }
                } catch (ex) {
                    log.error "Fail to process Fortnox Invoice for ${organization?.name}: ${invoiceJSON}: $ex.message", ex
                }
            }
            if(responseJSON.MetaInformation.'@TotalPages' > responseJSON.MetaInformation.'@CurrentPage') {
                result += listForOrganization(organization, filter, lastModified, responseJSON.MetaInformation.'@CurrentPage' + 1)
            }
        }
        result
    }

    /**
     *
     * @param documentNumber
     * @return
     */
    FortnoxInvoice cancel(Facility facility, String documentNumber) {
        FortnoxInvoice result = null
        fortnox3Service.doPut(facility, INVOICES, documentNumber, CANCEL_ACTION, null) { def invoiceJSON ->
            result = new FortnoxInvoice(getInvoicePropertiesBasedOnJSON(invoiceJSON.Invoice))
        }
        result
    }

    /**
     *
     * @param documentNumber
     * @return
     */
    FortnoxInvoice cancelForOrganization(Organization organization, String documentNumber) {
        FortnoxInvoice result = null
        fortnox3Service.doPutForOrganization(organization, INVOICES, documentNumber, CANCEL_ACTION, null) { def invoiceJSON ->
            result = new FortnoxInvoice(getInvoicePropertiesBasedOnJSON(invoiceJSON.Invoice))
        }
        result
    }

    /**
     * Returns FortnoxInvoices for a customer
     * @param facility
     * @param ese
     * @return
     */
    List<FortnoxInvoice> getForCustomerExternalSynchronizationEntity(Facility facility, ExternalSynchronizationEntity ese) {
        List<FortnoxInvoice> result = []
        fortnox3Service.doGet(facility, INVOICES, null, [customernumber: ese.externalEntityId]) { def invoiceJSON ->
            invoiceJSON.Invoices.each {
                result << new FortnoxInvoice(getInvoicePropertiesBasedOnJSON(it))
            }
        }

        return result.sort { FortnoxInvoice fortnoxInvoice ->
            fortnoxInvoice.DocumentNumber as Integer
        }
    }

    /**
     * Returns FortnoxInvoices for a customer
     * @param facility
     * @param ese
     * @return
     */
    List<FortnoxInvoice> getForCustomerExternalSynchronizationEntity(Organization organization, ExternalSynchronizationEntity ese) {
        List<FortnoxInvoice> result = []
        fortnox3Service.doGetForOrganization(organization, INVOICES, null, [customernumber: ese.externalEntityId]) { def invoiceJSON ->
            invoiceJSON.Invoices.each {
                result << new FortnoxInvoice(getInvoicePropertiesBasedOnJSON(it))
            }
        }

        return result.sort { FortnoxInvoice fortnoxInvoice ->
            fortnoxInvoice.DocumentNumber as Integer
        }
    }

    /**
     * Make a payment for Invoice
     *
     * @param documentNumber
     * @return
     */
    boolean pay(Facility facility, String documentNumber, Float amount) {
        boolean result = false
        def requestBody = [InvoicePayment: ['InvoiceNumber': documentNumber as Integer, 'Amount': amount]]
        fortnox3Service.doPost(facility, INVOICE_PAYMENTS, requestBody) { def invoicePaymentJSON ->
            result = BooleanUtils.toBoolean(invoicePaymentJSON.InvoicePayment.Number)
        }
        result
    }

    private Map parseFortnoxInvoice(FortnoxInvoice invoice) {
        Map propertiesMap = fortnox3Service.getPossiblePropertiesMap(invoice, FortnoxInvoice)
        List invoiceRows = []
        invoice.InvoiceRows.each { FortnoxInvoiceRow invoiceRow ->
            invoiceRows << fortnox3Service.getPossiblePropertiesMap(invoiceRow, FortnoxInvoiceRow)
        }
        propertiesMap['InvoiceRows'] = invoiceRows
        propertiesMap
    }

    /**
     *
     * @param invoiceJSON
     * @return
     */
    protected LinkedHashMap getInvoicePropertiesBasedOnJSON(invoiceJSON) {
        def availableFieldNames = FortnoxInvoice.class.getDeclaredFields()*.name
        def invoiceProperties = [:]
        invoiceJSON.entrySet().each {
            if (availableFieldNames.contains(it.key) && !it.key.startsWith('@') && !['EDIInformation', 'EmailInformation'].contains(it.key)) {
                //Few fields on List view differ from detail view
                if (it.key.indexOf('Date') >= 0) {
                    if (!it.value || it.value?.getClass() == net.sf.json.JSONNull) {
                        invoiceProperties[it.key] = null
                    } else {
                        invoiceProperties[it.key] = Date.parse(Fortnox3Service.FORTNOX_API_DATE_FORMAT, it.value)
                    }
                } else if (it.value == '0,00' || it.value == '0.00') {//workaround for Fortnox API bug
                    invoiceProperties[it.key] = 0
                } else if (it.value == '1.0000') {//workaround for another Fortnox API bug :)
                    invoiceProperties[it.key] = 1
                } else {
                    invoiceProperties[it.key] = it.value?.getClass() == net.sf.json.JSONNull ? null : it.value
                }
            }
        }
        invoiceProperties
    }

    def preview(Facility facility = null, String documentNumber) {
        def bytes = null
        fortnox3Service.doPreview(facility, INVOICES, documentNumber, "preview") { def resp ->
            bytes = ((HttpResponse) resp).getEntity().content.getBytes()
        }
        bytes
    }

    boolean validateCurrency(facilityCurrency, invoiceJSON) {
        // Also used when retrieving MATCHi invoices in which case facilityCurrency will be null.
        if (!facilityCurrency) {
            return true
        }

        boolean valid = false
        invoiceJSON.entrySet().each {
            if (it.key == "Currency" && it.value == facilityCurrency) {
                valid = true
            }
        }
        return valid
    }

}
