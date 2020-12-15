package com.matchi.excel

import com.matchi.*
import com.matchi.dynamicforms.Submission
import com.matchi.dynamicforms.SubmissionValue
import com.matchi.membership.Membership
import grails.compiler.GrailsCompileStatic
import grails.plugin.jxl.Cell
import grails.plugin.jxl.builder.ExcelBuilder
import jxl.CellView
import jxl.format.Alignment
import jxl.format.Colour
import jxl.format.VerticalAlignment
import jxl.write.WritableCellFormat
import jxl.write.WritableFont
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.joda.time.LocalDate

import java.text.SimpleDateFormat

@Mixin(ExcelBuilder)
class ExcelExportManager {

    public static final String EXCEL_FILE_EXTENSION = "xls"
    public static final String BOM_CHARACTER = "\uFEFF"
    public static final String CSV_LINE_BREAK = "\r\n"

    private static final Log log = LogFactory.getLog(ExcelExportManager)
    private static final String LIST_SEPARATOR = ","

    private static final SimpleDateFormat NIF_DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy")

    def messageSource
    DateUtil dateUtil

    @GrailsCompileStatic
    static void withBomWriter(OutputStream outputStream, Closure closure) {
        outputStream.withWriter("UTF-8") { out ->
            out << BOM_CHARACTER     // add BOM to properly recognize UTF-8 in e.g. Excel 2007
            closure.call(out)
        }
    }

    def export(def customers, Boolean isFederation, ExportType type, OutputStream stream) {
        switch (type) {
            case ExportType.COMPLETE:
                return completeCustomerExport(customers, isFederation, stream)
                break
            case ExportType.IDROTT_ONLINE:
                return idrottOnlineCustomerExport(customers, stream)
                break
            case ExportType.NIF:
                return nifCustomerExport(customers, stream)
                break
            default:
                return completeCustomerExport(customers, isFederation, stream)
                break
        }
    }

    def exportPrivateLessons(def bookings, Facility facility, OutputStream stream) {
        def locale = new Locale(facility.language)

        def titles = [messageSource.getMessage("trainer.name.label", null, locale),
                      messageSource.getMessage("trainer.customer.lessonTime", null, locale),
                      messageSource.getMessage("trainer.customer.label", null, locale),
                      messageSource.getMessage("trainer.customer.email", null, locale),
                      messageSource.getMessage("trainer.price.label", null, locale),
                      messageSource.getMessage("trainer.paymentStatus.label", null, locale)]

        def data = [titles]

        bookings.each { Booking booking ->

            data << [booking.trainers?.first()?.fullName(),
                                dateUtil.formatDateAndTime(booking.slot.startTime),
                                booking.customer.fullName(),
                                booking.customer.email,
                                booking.order?.total(),
                                booking.isFinalPaid() ?  messageSource.getMessage("membership.status.PAID", null, locale)
                                        :  messageSource.getMessage("membership.status.UNPAID", null, locale)]

        }

        workbook(stream, {
            sheet("Lessons") {
                addData(data)
                (0..titles.size()).each { cell(it, 0).bold().left() }
            }
        })
    }


    def completeCustomerExport(def customers, Boolean isFederation, OutputStream stream) {
        def locale = new Locale(customers[0].facility.language)

        def titles = ["Nummer"] + completeMembershipColumns().collect { it.title } +
                completeCustomerColumns(isFederation).collect { it.title } +
                ["Familj", "Land", "MATCHi-user"]

        def data = [titles]

        customers.each { Customer c ->

            def customerAttrs = [c.number]

            completeMembershipColumns().each {
                def added = false

                if (c.membership) {
                    if (it.attr == "status") {
                        if (c.membership.activated && c.membership.isPaid()) {
                            customerAttrs << (c.membership.cancel ?
                                    Membership.Status.CANCEL.name() : Membership.Status.ACTIVE.name())
                            added = true
                        } else if (!c.membership.activated) {
                            customerAttrs << Membership.Status.PENDING.name()
                            added = true
                        }
                    } else {
                        def prop = c.membership["${it.attr}"]
                        if (prop && prop?.toString() != "null") {
                            customerAttrs << prop.toString()
                            added = true
                        }
                    }
                }

                if (!added) {
                    customerAttrs << ""
                }
            }

            completeCustomerColumns(isFederation).each {
                if (it.attr == "type") {
                    customerAttrs << getCustomerTypeString(c[it.attr])
                } else if (it.attr == "customerGroups") {
                    customerAttrs << (c[it.attr] ? c[it.attr].group.name.join(",") : "")
                } else if (it.attr == "clubMessagesDisabled") {
                    customerAttrs << (c[it.attr] ? "Nej" : "Ja")
                } else if (it.attr == "securityNumber") {
                    customerAttrs << (c.isCompany() ? c.orgNumber : c.getPersonalNumber())
                } else if(c[it.attr]) {
                    customerAttrs << c[it.attr]
                } else {
                    customerAttrs << ""
                }
            }

            customerAttrs << (c.membership?.family?.contact?.number ?: "") <<
                    (c.country ? messageSource.getMessage("country.${c.country}", null, locale) : "") <<
                    (c.user ? "X" : "")

            data << customerAttrs
        }

        workbook(stream, {
            sheet("Kunder") {
                addData(data)
                (0..titles.size()).each { cell(it, 0).bold().left() }
            }
        })
    }

    def idrottOnlineCustomerExport(def customers, OutputStream stream) {
        def titles = idrottOnlineCustomerColumns().collect { it.title }
        def data = [titles]

        customers.each { Customer c ->

            def customerAttrs = []

            idrottOnlineCustomerColumns().each {
                if (it.attr == "type") {
                    customerAttrs << getCustomerTypeString(c[it.attr])
                } else if (it.attr == "securityNumber") {
                    customerAttrs << (c.isCompany() ? c.orgNumber : c.getPersonalNumber())
                } else if(it.attr!="" && c[it.attr]) {
                    customerAttrs << c[it.attr]
                } else {
                    customerAttrs << ""
                }
            }

            data << customerAttrs
        }

        // Font
        WritableFont font = new WritableFont(WritableFont.createFont('Verdana'), 10)
        font.setColour(Colour.BLACK)
        font.setBoldStyle(WritableFont.NO_BOLD)

        WritableCellFormat cellFormat = new WritableCellFormat()
        cellFormat.setBackground(Colour.GRAY_25)
        cellFormat.setWrap(true)
        cellFormat.setAlignment(Alignment.LEFT)
        cellFormat.setVerticalAlignment(VerticalAlignment.CENTRE)

        workbook(stream, {
            sheet("Kunder") {
                data.eachWithIndex { row, rowNumber ->
                    row.eachWithIndex { col, colNumber ->
                        if (rowNumber == 0) {
                            Cell cell = new Cell(colNumber, rowNumber, col, [format: cellFormat])
                            CellView cellView = sheet.getColumnView(colNumber)
                            cellView.size = 6000
                            sheet.setColumnView(colNumber, cellView)
                            sheet.setRowView(rowNumber, 630);
                            cell.font = font
                            cell.write(sheet)
                        } else {
                            new Cell(colNumber, rowNumber, col).write(sheet)
                        }
                    }
                }
                (0..titles.size()).each { cell(it, 0).bold().left() }
            }
        })
    }

    def nifCustomerExport(def customers, OutputStream stream) {
        def titles = nifCustomerColumns().collect { it.title } + ["Medlem fom"]
        def data = [titles]

        customers.each { Customer c ->
            def customerAttrs = []

            nifCustomerColumns().each {
                def added = false

                c.properties.each { def prop ->
                    if (prop.key == it.attr) {

                        if (prop.key == "type") {
                            customerAttrs << getNifCustomerTypeString(prop.value)
                        } else if (prop.key == "dateOfBirth") {
                            customerAttrs << formatNifDate((Date) prop.value)
                        } else {
                            customerAttrs << prop.value
                        }

                        added = true
                    }
                }

                if (!added) {
                    customerAttrs << ""
                }
            }

            /*
             * "Medlem fom"
             * If the customer has an active and paid membership we try to
             * find the start date of the first membership they ever had for "Medlem fom".
             * If we cant't find one we fall back to the start date of the current membership.
             */
            Membership membership = c.membership
            if (membership && membership.activated && membership.isPaid()) {
                LocalDate startDate = membership.startDate

                List<Membership> memberships = findMemberships(c)
                if (!memberships.isEmpty()) {
                    Membership firstMembership = memberships.get(0)
                    if (firstMembership) {
                        startDate = firstMembership.startDate
                    }
                }

                customerAttrs << formatNifDate(startDate.toDate())
            } else {
                customerAttrs << ""
            }

            data << customerAttrs
        }

        workbook(stream, {
            sheet("Kunder") {
                addData(data)
                (0..titles.size()).each { cell(it, 0).bold().left() }
            }
        })
    }

    void exportAccessCodes(List accessCodes, OutputStream stream) {
        def titles = accessCodeColumns().collect { it.title }
        def data = [titles]
        def df = new SimpleDateFormat(ImportAccessCodeService.DATE_FORMAT)

        accessCodes.each { ac ->
            def codeAttrs = []
            accessCodeColumns().each { col ->
                if (ac."$col.attr" == null) {
                    codeAttrs << ""
                } else {
                    if (col.attr == "validFrom" || col.attr == "validTo") {
                        codeAttrs << df.format(ac."$col.attr")
                    } else if (ac."$col.attr" instanceof List) {
                        codeAttrs << ac."$col.attr".join(LIST_SEPARATOR)
                    } else {
                        codeAttrs << ac."$col.attr"
                    }
                }
            }
            data << codeAttrs
        }

        workbook(stream, {
            sheet("Koder") {
                addData(data)
                (0..titles.size()).each { cell(it, 0).bold().left() }
            }
        })
    }

    void exportStatisticsIncome(List transactions, Locale locale, OutputStream stream) {
        def titles = statisticsIncomeColumns().collect { messageSource.getMessage(it.title, null, locale) }
        def data = [titles]

        transactions.each { transaction ->
            def values = []
            statisticsIncomeColumns().each { col ->
                if (transaction."$col.attr" == null) {
                    values << ""
                } else {
                    if (col.attr == "customer")
                        values << transaction.customer?.fullName()
                    else if (col.attr == "date")
                        values << dateUtil.formatDateAndTime(transaction.date)
                    else if (col.attr == "article")
                        values << messageSource.getMessage("articleType.${transaction.article}", null, locale) + " (" + transaction.method + ")"
                    else if (col.attr == "indoor")
                        values << (transaction.indoor ? messageSource.getMessage("court.indoor.label", null, locale) : messageSource.getMessage("court.outdoors.label", null, locale))
                    else
                        values << transaction."$col.attr"
                }
            }
            data << values
        }

        workbook(stream, {
            sheet(messageSource.getMessage("adminStatistics.index.income", null, locale)) {
                addData(data)
                (0..titles.size()).each {
                    cell(it, 0).bold().left()

                    def cellView = sheet.getColumnView(it)
                    cellView.setAutosize(true)
                    sheet.setColumnView(it, cellView)
                }
            }
        })
    }

    void exportSubmissions(List<Submission> submissions, OutputStream stream) {
        def locale = new Locale(submissions[0].customer.facility.language)

        def fieldColumn = new LinkedHashMap<String, Integer>()
        workbook(stream, {
            sheet("Submissions") {

                submissions.eachWithIndex { Submission s, pidx ->
                    addCell("customer.number.label2", (String) messageSource.getMessage("customer.number.label2", null, locale), s.customer?.number?.toString(), pidx, fieldColumn)

                    SubmissionValue.findAllBySubmission(s).groupBy { sv ->
                        sv.label
                    }.sort { grp ->
                        grp.value[0].fieldId
                    }.each { grp ->
                        def cellValue = ""
                        grp.value.sort { i1, i2 ->
                            return i1.valueIndex <=> i2.valueIndex
                        }.groupBy { v ->
                            v.inputGroup
                        }.eachWithIndex { label, items, i ->
                            if (label) {
                                if (i) {
                                    cellValue += "\n"
                                }
                                cellValue += label + " "
                            }

                            items.sort { i1, i2 ->
                                i1.valueIndex <=> i2.valueIndex ?: i1.input <=> i2.input
                            }.eachWithIndex { itm, j ->
                                if (!label && j) {
                                    cellValue += "\n"
                                }
                                if (itm.input) {
                                    cellValue += messageSource.getMessage(
                                            "formField.type.${itm.fieldType}.${itm.input}",
                                            null, locale)
                                    cellValue += ": "
                                }
                                cellValue += itm.value + " "
                            }
                        }
                        addCell("${grp.key}-${grp.value[0].fieldType}", grp.key, cellValue, pidx, fieldColumn)
                    }

                    addCell("courseSubmission.course.label", (String) messageSource.getMessage("courseSubmission.course.label", null, locale), s.form?.course?.name, pidx, fieldColumn)
                    addCell("courseSubmission.submission.dateCreated.label", (String) messageSource.getMessage("courseSubmission.submission.dateCreated.label", null, locale), s.dateCreated?.toString(), pidx, fieldColumn)
                }
            }
        })
    }

    private void addCell(String fieldKey, String label, String value, int row, LinkedHashMap<String, Integer> fieldColumn) {
        def labelFormat = new WritableCellFormat()
        labelFormat.setVerticalAlignment(VerticalAlignment.TOP)
        def valueFormat = new WritableCellFormat()
        valueFormat.setVerticalAlignment(VerticalAlignment.TOP)
        valueFormat.setWrap(true)

        if (!fieldColumn.containsKey(fieldKey)) {
            fieldColumn[fieldKey] = fieldColumn.size()
            cell(fieldColumn[fieldKey], 0, label, [format: labelFormat]).bold()
            def cellView = sheet.getColumnView(fieldColumn[fieldKey])
            if (fieldColumn[fieldKey] == 0) {
                cellView.setAutosize(true)
            } else {
                cellView.setSize(9000)
            }
            sheet.setColumnView(fieldColumn[fieldKey], cellView)
        }
        cell(fieldColumn[fieldKey], row + 1, value, [format: valueFormat])

    }

    private List accessCodeColumns() {
        [
                [title: "Från", attr: "validFrom"],
                [title: "Till", attr: "validTo"],
                [title: "Kod", attr: "content"],
                [title: "Banor", attr: "courtNames"]
        ]
    }

    private List statisticsIncomeColumns() {
        [
                [title: "facilityStatistic.payment.message13", attr: "date"],
                [title: "facilityStatistic.payment.message14", attr: "article"],
                [title: "user.label.plural", attr: "customer"],
                [title: "facilityStatistic.payment.message16", attr: "info"],
                [title: "facilityStatistic.payment.message17", attr: "amount"],
                [title: "facilityStatistic.payment.message19", attr: "currency"],
                [title: "court.sport.label", attr: "sport"],
                [title: "court.indoorOutdoor.label", attr: "indoor"],
                [title: "default.description.label", attr: "description"]
        ]
    }

    static def completeCustomerColumns(Boolean isFederation) {
        def columns = [[title: "Efternamn", attr: "lastname"],
                       [title: "Förnamn", attr: "firstname"],
                       [title: "Företagsnamn", attr: "companyname"],
                       [title: "Kontaktperson", attr: "contact"]]

        if (isFederation)
            columns += [title: "Klubb", attr: "club"]

        columns += [[title: "Epost", attr: "email"],
                    [title: "Telefon", attr: "telephone"],
                    [title: "Mobil", attr: "cellphone"],
                    [title: "Adress1", attr: "address1"],
                    [title: "Adress2", attr: "address2"],
                    [title: "Postnr", attr: "zipcode"],
                    [title: "Stad", attr: "city"],
                    [title: "Person-/Orgnr", attr: "securityNumber"],
                    [title: "Kön/Typ", attr: "type"],
                    [title: "Webbadress", attr: "web"],
                    [title: "Faktura-adress1", attr: "invoiceAddress1"],
                    [title: "Faktura-adress2", attr: "invoiceAddress2"],
                    [title: "Faktura-postnr", attr: "invoiceZipcode"],
                    [title: "Faktura-stad", attr: "invoiceCity"],
                    [title: "Faktura-kontakt", attr: "invoiceContact"],
                    [title: "Faktura-tele", attr: "invoiceTelephone"],
                    [title: "Faktura-epost", attr: "invoiceEmail"],
                    [title: "Anteckning", attr: "notes"],
                    [title: "Målsmans namn", attr: "guardianName"],
                    [title: "Målsmans mejl", attr: "guardianEmail"],
                    [title: "Målsmans telefon", attr: "guardianTelephone"],
                    [title: "Målsmans namn 2", attr: "guardianName2"],
                    [title: "Målsmans mejl 2", attr: "guardianEmail2"],
                    [title: "Målsmans telefon 2", attr: "guardianTelephone2"],
                    [title: "Grupper", attr: "customerGroups"],
                    [title: "Vill ha mejlutskick", attr: "clubMessagesDisabled"]]

        return columns
    }

    static def completeMembershipColumns() {
        return [
                [title: "Medlemskapstatus", attr: "status"],
                [title: "Medlemstyp", attr: "type"]
        ]
    }

    static def idrottOnlineCustomerColumns() {
        return [
                [title: "Prova-på", attr: ""],
                [title: "Förnamn", attr: "firstname"],
                [title: "Alt.Förnamn", attr: ""],
                [title: "Efternamn", attr: "lastname"],
                [title: "Kön", attr: "type"],
                [title: "Nationalitet", attr: ""],
                [title: "IdrottsID", attr: ""],
                [title: "Födelsedat./Personnr.", attr: "securityNumber"],
                [title: "Telefon mobil", attr: "cellphone"],
                [title: "E-post kontakt", attr: "email"],
                [title: "Kontaktadress - c/o adress", attr: "address2"],
                [title: "Kontaktadress - Gatuadress", attr: "address1"],
                [title: "Kontaktadress - Postnummer", attr: "zipcode"],
                [title: "Kontaktadress - Postort", attr: "city"],
                [title: "Kontaktadress - Land", attr: "country"],
                [title: "Arbetsadress - c/o adress", attr: ""],
                [title: "Arbetsadress - Gatuadress", attr: ""],
                [title: "Arbetsadress - Postnummer", attr: ""],
                [title: "Arbetsadress - Postort", attr: ""],
                [title: "Arbetsadress - Land", attr: ""],
                [title: "Telefon bostad", attr: "telephone"],
                [title: "Telefon arbete", attr: ""],
                [title: "E-post privat", attr: "email"],
                [title: "E-post arbete", attr: ""],
                [title: "Medlemsnr.", attr: "number"],
                [title: "Medlem sedan", attr: ""],
                [title: "Medlem t.o.m.", attr: ""],
                [title: "Familj", attr: ""],
                [title: "Fam.Admin", attr: ""],
                [title: "GruppID", attr: ""]
        ]
    }

    static def nifCustomerColumns() {
        return [
                [title: "Kjøn", attr: "type"],
                [title: "Født", attr: "dateOfBirth"],
                [title: "Etternavn", attr: "lastname"],
                [title: "Fornavn", attr: "firstname"],
                [title: "Gateadresse", attr: "address1"],
                [title: "Postnr", attr: "zipcode"],
                [title: "Poststed", attr: "city"],
                [title: "Tlf privat", attr: "telephone"],
                [title: "Tlf mobil", attr: "cellphone"],
                [title: "Fax", attr: ""],
                [title: "E-post", attr: "email"]
        ]
    }

    static def getCustomerTypeString(def type) {
        switch (type) {
            case Customer.CustomerType.MALE:
                return "Man"
                break;
            case Customer.CustomerType.FEMALE:
                return "Kvinna"
                break;
            case Customer.CustomerType.ORGANIZATION:
                return "F"
                break;
            default:
                return ""
                break;
        }
    }

    static def getNifCustomerTypeString(def type) {
        switch (type) {
            case Customer.CustomerType.MALE:
                return "M"
            case Customer.CustomerType.FEMALE:
                return "K"
            default:
                return ""
        }
    }

    static def formatNifDate(Date date) {
        try {
            return NIF_DATE_FORMAT.format(date)
        } catch (Exception e) {
            return ""
        }
    }

    static def findMemberships(Customer customer) {
        Membership.createCriteria().listDistinct {
            eq("customer", customer)
            order("startDate", "asc")
        }
    }

    public enum ExportType {
        COMPLETE, IDROTT_ONLINE, NIF

        static list() {
            return [COMPLETE, IDROTT_ONLINE, NIF]
        }
    }
}
