package com.matchi.facility

import au.com.bytecode.opencsv.CSVWriter
import com.matchi.Facility
import com.matchi.GenericController
import com.matchi.InvoiceService
import com.matchi.excel.ExcelExportManager
import com.matchi.fortnox.v3.FortnoxException
import com.matchi.invoice.Invoice
import com.matchi.invoice.InvoicePayment
import com.matchi.sie.Document
import com.matchi.sie.Document.DataSource
import grails.compiler.GrailsCompileStatic
import grails.util.Holders
import grails.validation.Validateable
import net.fortuna.ical4j.model.DateRange
import org.codehaus.groovy.grails.web.pages.discovery.GrailsConventionGroovyPageLocator
import org.joda.time.DateMidnight
import org.joda.time.DateTime
import org.joda.time.LocalDate

class FacilityInvoiceController extends GenericController {

    public static final String LIST_FILTER_KEY = "facility_invoice_filter"
    public static final String SIE_TYPE4_DATE_FORMAT = "yyyyMMdd"

    GrailsConventionGroovyPageLocator groovyPageLocator
    def fortnoxFacadeService
    def invoiceService
    def externalSynchronizationService
    def fileArchiveService
    def notificationService

    def index(FilterInvoiceCommand filter) {
        if (!params.boolean("reset") && session[LIST_FILTER_KEY]?.isNotExpired()) {
            filter = session[LIST_FILTER_KEY]
        } else {
            session[LIST_FILTER_KEY] = filter
        }

        def invoices = invoiceService.getInvoices(filter)
        [invoices: invoices, facility: getUserFacility(), filter: filter]
    }

    def edit() {
        def invoice = Invoice.get(params.id)
        def fortnoxInvoice = null
        def externalId = externalSynchronizationService.getFortnoxInvoiceNumber(invoice)

        // Synchronize status and paid date with Fortnox.
        if (externalId) {
            def facility = getUserFacility()
            try {
                fortnoxInvoice = fortnoxFacadeService.getFortnoxInvoice(invoice, externalId)
                //Need to update 'CREDITED' status. Do not access on invoice list to this field
                if (fortnoxInvoice.CreditInvoiceReference && fortnoxInvoice.CreditInvoiceReference != "0" && invoice.status != Invoice.InvoiceStatus.CREDITED) {
                    invoice.status = Invoice.InvoiceStatus.CREDITED
                    invoice.save()
                } else {
                    fortnoxFacadeService.processInvoiceStatus(invoice, fortnoxInvoice)
                    fortnoxFacadeService.processInvoiceFinalPayDate(invoice, fortnoxInvoice)
                    fortnoxFacadeService.processInvoiceOutputStatus(invoice, fortnoxInvoice)
                    if (!invoice.number) {
                        invoice.number = Long.parseLong(fortnoxInvoice.id)
                    }
                    invoice.save()
                }

            } catch (FortnoxException ex) {
                flash.error = ex.message
            }
        }

        [cmd  : InvoiceDetailsCommand.create(invoice), fortnoxInvoice: fortnoxInvoice, invoice: invoice,
         rows : [rows: invoice.rows], customer: invoice.customer, isEditable: invoice.isEditable(), vats: getCountryVats(invoice?.customer?.facility),
         items: invoiceService.getItemsForFacilityWithOrganization(invoice?.customer?.facility, invoice?.organization)]
    }

    def save(InvoiceDetailsCommand cmd, InvoiceRowsCommand rows) {
        def invoice = Invoice.load(cmd.id)

        if (cmd.validate() && rows.validate()) {
            invoice.text = cmd.text
            invoice.invoiceDate = cmd.invoiceDate
            invoice.expirationDate = cmd.getExpirationDate()

            // remove
            def toBeRemoved = invoice.rows.findAll { !rows.hasRowId(it.id) }
            toBeRemoved.each {
                if (it) {
                    invoice.removeFromRows(it)
                }
            }

            // add
            def toBeAdded = rows.rows.findAll { !it.rowId }
            toBeAdded.each { InvoiceRowCommand rowCmd ->
                invoice.addToRows(rowCmd.toInvoiceRow(invoice.customer, getCurrentUser()))
            }

            // update
            rows.rows.findAll { it.rowId }.each {
                it.toInvoiceRow(invoice.customer, getCurrentUser()).save()
            }

            invoiceService.save(invoice)
            //Show fortnox error if necessary
            if (invoice.errors.hasGlobalErrors() && invoice.errors.globalError.code == FortnoxException.ERROR_CODE) {
                flash.error = invoice.errors?.globalError?.defaultMessage
            } else if (invoice.hasErrors()) {
                render view: "edit", model: [cmd     : cmd, invoice: invoice, rows: [rows: invoice.rows],
                                             customer: invoice.customer, isEditable: invoice.isEditable(), rowsCmd: rows,
                                             items   : invoiceService.getItemsForFacilityWithOrganization(invoice?.customer?.facility, invoice?.organization)]
                return
            }
            flash.message = g.message(code: 'facilityInvoice.save.success', args: [invoice.customer.fullName()])

            if (params.returnUrl && params.returnUrl.size() > 0) {
                redirect(url: params.returnUrl)
                return
            }
            redirect action: "index"
        } else {
            flash.error = message(code: "invoiceDetailsCommand.invalid")
            render view: "edit", model: [cmd     : cmd, invoice: invoice, rows: [rows: invoice.rows],
                                         customer: invoice.customer, isEditable: invoice.isEditable(), rowsCmd: rows,
                                         items   : invoiceService.getItemsForFacilityWithOrganization(invoice?.customer?.facility, invoice?.organization)]
        }

    }

    def print(FilterInvoiceCommand filter) {
        List<Invoice> invoices = invoiceService.selectedInvoices(filter, params)

        Facility facility = getUserFacility()

        invoices.each { invoice ->
            invoice.sent = Invoice.InvoiceSentStatus.PRINT
        }

        def template = "${InvoiceService.PRINT_TEMPLATE_ROOT_URI}/${invoiceService.getPrintTemplate(facility)}"

        def pdf = getPdfFile(template,[
                facility: facility,
                invoices: invoices,
                accounts: prepareAccountSummary(invoices)
        ])

        render(file:pdf,contentType: 'application/pdf')
    }

    List prepareSieAccounts(def invoices) {

        List<Map> accounts = []
        invoices.each { invoice ->
            invoice.rows.each {
                if (it?.account?.isInteger()) accounts.add(number: it.account, description: it.description)
            }
        }
        return accounts.unique { it.number }.reverse()
    }

    def prepareAccountSummary(def invoices) {

        def accounts = [:]

        invoices.each { def invoice ->
            invoice.rows.each { def row ->
                def accountkey = (row.account ?: "-")
                def account = accounts.get(accountkey)

                if (!account) {
                    account = [debit: 0, credit: 0]
                    accounts.put(accountkey, account)
                }

                if (row.invoice.getTotalExcludingVAT() >= 0) {
                    account.debit += row.invoice.getTotalExcludingVAT()
                } else {
                    account.credit += row.invoice.getTotalExcludingVAT()
                }
            }
        }

        accounts
    }

    def remove(FilterInvoiceCommand filter) {
        def invoices = invoiceService.selectedInvoices(filter, params)
        log.info("Cancelling ${invoices.size()} invoices")

        log.info(filter.toString())

        for(invoice in invoices){
            try {
                invoiceService.cancel(invoice)
            } catch (FortnoxException fe) {
                flash.error = message(code: "facilityInvoice.remove.error", args: [fe.message])
                redirect action: "index"
                return
            }
        }

        if (params.returnUrl && params.returnUrl.size() > 0) {
            redirect(url: params.returnUrl)
            return
        }
        redirect action: "index"
    }

    def status(FilterInvoiceCommand filter) {

        def status = Invoice.InvoiceStatus.valueOf(params.newStatus)

        if (getUserFacility().hasFortnox()) {
            flash.error = message(code: "facilityInvoice.status.error")
        } else {
            def invoices = invoiceService.selectedInvoices(filter, params)

            invoices.each { invoice ->
                if (status == Invoice.InvoiceStatus.PAID) {
                    invoice.addPayment(new LocalDate(), invoice.getTotalAmountPaymentRemaining())
                } else if (invoice.status != Invoice.InvoiceStatus.PAID) {
                    invoice.status = status
                }
            }

            flash.message = message(code: "facilityInvoice.status.updated")
        }

        redirect(action: "index")
    }

    def payment(InvoicePaymentCommand cmd) {

        def invoice = Invoice.get(cmd.id)

        if (invoice && cmd.validate()) {

            invoice.addPayment(cmd.paymentDate, cmd.paymentAmount)

            flash.message = message(code: "facilityInvoice.payment.success")
            redirect(action: "edit", params: [id: cmd.id])
        } else {

            if (invoice) {
                flash.error = message(code: "facilityInvoice.payment.error1")
                redirect(action: "edit", params: [id: cmd.id])
            } else {
                flash.error = message(code: "facilityInvoice.payment.error2")
                redirect(action: "index")
            }
        }

    }

    def removePayment() {
        InvoicePayment payment = InvoicePayment.get(params.id)

        if (payment) {
            def invoice = payment.invoice
            invoice.removePayment(payment)
            payment.delete()

            invoice.save()

            flash.message = message(code: "facilityInvoice.removePayment.success")

            redirect(action: "edit", params: [id: invoice.id])
        } else {
            flash.error = message(code: "facilityInvoice.removePayment.error")
            redirect(action: "index")
        }

    }

    def export(FilterInvoiceCommand filter) {
        def invoices = invoiceService.selectedInvoices(filter, params)
        def facility = getUserFacility()
        def date = new DateTime().toString("yyyy-MM-dd_HHmmss")

        log.info "Exporting ${invoices.size()} invoices"


        def formatAmount = { def val ->
            return String.format(Locale.ENGLISH, "%.2f", val)
        }

        def baos = new ByteArrayOutputStream()
        def csvWriter = setupCsvWriter(baos, new ArrayList<String>())
        String[] properties = new String[11]

        invoices.each { invoice ->

            properties[0] = invoice.customer.getNumber()
            properties[1] = invoice.getNumber()
            properties[2] = invoice.getInvoiceDate().toString("yyyy-MM-dd")
            properties[3] = formatAmount(invoice.getTotalIncludingVATRounded())
            properties[4] = formatAmount(invoice.getTotalVAT())
            properties[5] = formatAmount(invoice.getTotalIncludingVATRounded())
            properties[6] = "0"
            properties[7] = invoice.getExpirationDate().toString("yyyy-MM-dd")
            properties[8] = invoice.getStatus().equals(Invoice.InvoiceStatus.PAID) ? 1 : 0
            properties[9] = "0"
            properties[10] = invoice.getOCR().toString()

            csvWriter.writeNext(properties)
        }
        csvWriter.flush()
        writeResponse("reskontra_${facility.shortname}_${date}.csv", "text/csv", baos.toByteArray())
    }

    def listExport(FilterInvoiceCommand filter) {
        def invoices = invoiceService.selectedInvoices(filter, params)
        def facility = getUserFacility()
        def date = new DateTime().toString("yyyy-MM-dd_HHmmss")

        log.info "Exporting ${invoices.size()} invoices"


        def formatAmount = { def val ->
            return String.format(Locale.ENGLISH, "%.2f", val)
        }

        def columnHeaders = [
                "Kundnr", "Kundnamn", "Fakturanr", "Skapad", "Fakturadatum", "Slutdatum", "Betaldatum", "Pris ex. moms", "Moms", "Pris inkl. moms", "OCR nr."
        ]

        def baos = new ByteArrayOutputStream()
        def csvWriter = setupCsvWriter(baos, columnHeaders)
        String[] properties = new String[11]

        invoices.each { invoice ->
            properties[0] = invoice.customer.number
            properties[1] = invoice.customer.fullName()
            properties[2] = invoice.number
            properties[3] = invoice.dateCreated.toString("yyyy-MM-dd")
            properties[4] = invoice.invoiceDate.toString("yyyy-MM-dd")
            properties[5] = invoice.expirationDate.toString("yyyy-MM-dd")
            properties[6] = invoice.paidDate ? invoice.paidDate.toString("yyyy-MM-dd") : ""
            properties[7] = formatAmount(invoice.getTotalExcludingVAT())
            properties[8] = formatAmount(invoice.getTotalVAT())
            properties[9] = formatAmount(invoice.getTotalIncludingVAT())
            properties[10] = invoice.getOCR().toString()

            csvWriter.writeNext(properties)
        }
        csvWriter.flush()
        writeResponse("fakturalista_${facility.shortname}_${date}.csv", "text/csv", baos.toByteArray())
    }

    def invoiceJournalExport(FilterInvoiceCommand filter) {
        def invoices = invoiceService.selectedInvoices(filter, params)
        def invoiceRows = []
        invoices.each { it.rows.each { row -> invoiceRows << row } }
        def facility = getUserFacility()
        def date = new DateTime().toString("yyyy-MM-dd_HHmmss")

        log.info "Exporting ${invoiceRows.size()} invoicerows"

        def formatAmount = { def val ->
            return String.format(Locale.ENGLISH, "%.2f", val)
        }

        def columnHeaders = [
                "Kundnr", "Kundnamn", "Fakturanr", "Fakturadatum", "Slutdatum", "Konto", "Pris ex. moms", "Moms", "Pris inkl. moms"
        ]

        String[] properties = new String[9]
        def baos = new ByteArrayOutputStream()
        def csvWriter = setupCsvWriter(baos, columnHeaders)

        invoiceRows.each { invoiceRow ->
            properties[0] = invoiceRow.customer.number
            properties[1] = invoiceRow.customer.fullName()
            properties[2] = invoiceRow.invoice.number
            properties[3] = invoiceRow.invoice.invoiceDate.toString("yyyy-MM-dd")
            properties[4] = invoiceRow.invoice.expirationDate.toString("yyyy-MM-dd")
            properties[5] = invoiceRow.account
            properties[6] = formatAmount(invoiceRow.getTotalExcludingVAT())
            properties[7] = formatAmount(invoiceRow.getPriceVAT())
            properties[8] = formatAmount(invoiceRow.getTotalIncludingVAT())

            csvWriter.writeNext(properties)
        }
        csvWriter.flush()
        writeResponse("journal_${facility.shortname}_${date}.csv", "text/csv", baos.toByteArray())
    }

    def invoiceJournalSummaryExport(FilterInvoiceCommand filter) {
        def invoices = invoiceService.selectedInvoices(filter, params)
        def invoiceRows = []
        invoices.each { it.rows.each { row -> invoiceRows << row } }

        def summary = []

        invoiceRows.groupBy { it.account }.each {
            def total = it.value?.sum { it.getTotalIncludingVAT() }
            summary << [it.key, total]
        }

        def facility = getUserFacility()
        def date = new DateTime().toString("yyyy-MM-dd_HHmmss")

        log.info "Exporting ${invoiceRows.size()} invoicerows"
        def formatAmount = { def val ->
            return String.format(Locale.ENGLISH, "%.2f", val)
        }

        def columnHeaders = [
                "Konto", "Debet", "Kredit"
        ]

        def baos = new ByteArrayOutputStream()
        def csvWriter = setupCsvWriter(baos, columnHeaders)

        String[] properties = new String[3]
        summary.each { sum ->
            properties[0] = sum[0]
            properties[1] = sum[1] < 0 ? sum[1] : ""
            properties[2] = sum[1] > 0 ? sum[1] : ""

            csvWriter.writeNext(properties)
        }
        csvWriter.flush()
        writeResponse("sammanfattning_${facility.shortname}_${date}.csv", "text/csv", baos.toByteArray())

    }

    def paymentExport(FilterInvoiceCommand filter) {
        def invoices = invoiceService.selectedInvoices(filter, params)
        def facility = getUserFacility()
        def date = new DateTime().toString("yyyy-MM-dd_HHmmss")

        log.info "Exporting payments for ${invoices.size()} invoices"

        def formatAmount = { def val ->
            return String.format(Locale.ENGLISH, "%.2f", val)
        }

        def columnHeaders = [
                "Kundnr", "Kundnamn", "Fakturanr", "Fakturadatum", "Betaldatum", "Pris inkl. moms", "OCR nr."
        ]

        String[] properties = new String[7]

        def baos = new ByteArrayOutputStream()
        def csvWriter = setupCsvWriter(baos, columnHeaders)

        invoices.each { invoice ->
            invoice.invoicePayments?.each { payment ->
                properties[0] = invoice.customer.number
                properties[1] = invoice.customer.fullName()
                properties[2] = invoice.number
                properties[3] = invoice.invoiceDate.toString("yyyy-MM-dd")
                properties[4] = payment.paymentDate.toString("yyyy-MM-dd")
                properties[5] = formatAmount(payment.amount)
                properties[6] = invoice.getOCR().toString()

                csvWriter.writeNext(properties)
            }
        }
        csvWriter.close()
        writeResponse("fakturabetalningar_${facility.shortname}_${date}.csv", "text/csv", baos.toByteArray())
    }

    def sieType4Export(FilterInvoiceCommand filter) {

        def invoices = invoiceService.selectedInvoices(filter, params)
        def facility = getUserFacility()

        DataSource dataSource = new DataSource() {
            @Override
            String balanceBefore(Integer accountNumber, Date date) {
                Integer sum = 0
                invoices.findAll { it.invoiceDate.toString(SIE_TYPE4_DATE_FORMAT) <= date.format(SIE_TYPE4_DATE_FORMAT).toString() }.each {
                    it.rows.findAll { it.account == accountNumber.toString() }.each {
                        sum += it.getTotalIncludingVAT()
                    }
                }
                return sum.toString()
            }
        }
        dataSource.accounts = prepareSieAccounts(invoices)
        dataSource.program = "MATCHi"
        dataSource.programVersion = grailsApplication.metadata.getApplicationVersion()
        dataSource.generatedOn = new Date().format(SIE_TYPE4_DATE_FORMAT).toString()
        dataSource.companyName = facility.name
        dataSource.financialYears = [
                new DateRange(filter.start.toDate(), filter.end.toDate()),
        ]
        dataSource.balanceAccountNumbers = dataSource.accounts.collect { if (it.number.isInteger()) Integer.parseInt(it?.number) }
        dataSource.closingAccountNumbers = dataSource.accounts.collect { if (it.number.isInteger()) Integer.parseInt(it?.number) }

        Document doc = new Document(dataSource)

        String render = doc.render()

        writeResponse("${facility.shortname}_${new Date()}.se", "text/plain", render.bytes)
    }

    def updateFortnoxArticles() {
        def userFacility = getUserFacility()

        invoiceService.updateItems(userFacility?.id, fortnoxFacadeService.articleCommands(userFacility))

        Organization.findAllByFacility(userFacility)?.each {
            invoiceService.updateItems(userFacility?.id, it.id, fortnoxFacadeService.articleCommands(userFacility, it))
        }

        flash.message = message(code: "facilityInvoice.updateArticles.success")
        redirect(action: "index")
    }


    private CSVWriter setupCsvWriter(OutputStream os, List<String> columnHeaders) {
        String[] properties = new String[columnHeaders.size()]

        def writer = new OutputStreamWriter(os, "UTF-8")
        def csvWriter = new CSVWriter(writer, CSVWriter.DEFAULT_SEPARATOR,
                CSVWriter.NO_QUOTE_CHARACTER, ExcelExportManager.CSV_LINE_BREAK);

        columnHeaders.eachWithIndex { header, i ->
            properties[i] = header
        }

        csvWriter.writeNext(properties)
        return csvWriter
    }

    @GrailsCompileStatic
    private void writeResponse(String filename, String contentType, byte[] bytes) {
        response.setHeader("Content-disposition", "attachment; filename=${filename}")
        response.contentType = contentType
        OutputStream os = response.getOutputStream()
        os.write(bytes)
        os.flush()
    }

}

@Validateable(nullable = true)
class InvoicePaymentCommand {
    Long id
    LocalDate paymentDate
    BigDecimal paymentAmount

    static constraints = {
        paymentDate nullable: false
        paymentAmount nullable: false, min: 1.0
        id nullable: false
    }
}

@Validateable(nullable = true)
class FilterInvoiceCommand implements Serializable {

    private static final long serialVersionUID = 1L

    String q
    int offset = 0
    int max = 50
    boolean allselected = false
    String order = "desc"
    String sort = "expirationDate"
    LocalDate start = new DateMidnight().minusMonths(1).withDayOfMonth(1).toLocalDate()
    LocalDate end = new DateMidnight().plusMonths(1).withDayOfMonth(1).minusDays(1).toLocalDate()
    List<String> status = Invoice.InvoiceStatus.preselectedInvoiceFilter()
    List<Long> organizations = []

    private Date dateCreated = new Date()

    def statuses() {
        return status.collect() { Invoice.InvoiceStatus.valueOf(it) }
    }

    @Override
    String toString() {
        return "FilterInvoiceCommand{" +
                "q='" + q + '\'' +
                ", offset=" + offset +
                ", max=" + max +
                ", allselected=" + allselected +
                ", order='" + order + '\'' +
                ", sort='" + sort + '\'' +
                ", start=" + start +
                ", end=" + end +
                ", status=" + status +
                '}';
    }

    boolean isActive() {
        q || organizations || (start && start != new DateMidnight().minusMonths(1).withDayOfMonth(1).toLocalDate()) ||
                (end && end != new DateMidnight().plusMonths(1).withDayOfMonth(1).minusDays(1).toLocalDate()) ||
                (status && (status.size() != Invoice.InvoiceStatus.preselectedInvoiceFilter().size() || !status.containsAll(Invoice.InvoiceStatus.preselectedInvoiceFilter())))
    }

    boolean isNotExpired() {
        new Date().time - dateCreated.time <= Holders.config.facility.customerFilter.timeout * 60 * 1000
    }
}
