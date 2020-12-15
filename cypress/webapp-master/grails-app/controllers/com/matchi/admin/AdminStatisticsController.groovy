package com.matchi.admin

import com.matchi.Facility
import com.matchi.FacilityContract
import com.matchi.FacilityProperty
import com.matchi.GenericController
import com.matchi.fortnox.v3.FortnoxException
import com.matchi.fortnox.v3.FortnoxInvoice
import com.matchi.fortnox.v3.FortnoxInvoiceRow
import com.matchi.orders.Order
import com.matchi.statistic.FacilityCouponFeeEntry
import com.matchi.statistic.FacilityFeeEntry
import com.matchi.statistic.FacilityPaymentSummary
import grails.util.Holders
import grails.validation.Validateable
import org.apache.commons.lang.BooleanUtils
import org.joda.time.DateMidnight
import org.joda.time.DateTime
import org.joda.time.Interval

class AdminStatisticsController extends GenericController {

    final static Integer MATCHi_VAT = 25

    def paymentService
    def dateUtil
    def groovySql
    def facilityService
    def facilityStatisticsService
    def fortnoxFacadeService
    def smsService

    def index(AdminListStatsCommand cmd) {
        def start = dateParam(params.start, new DateMidnight().withDayOfMonth(1))
        def end = dateUtil.endOfDay(dateParam(params.end, new DateMidnight().plusMonths(1).withDayOfMonth(1).minusDays(1)))

        List<Facility> selectedFacilities = []

        List<Facility> facilities = Facility.createCriteria().listDistinct {
            eq('currency', cmd.currency)
            isNull('archived')
            order("name", "asc")
        }

        if (cmd.fortnoxId) {
            selectedFacilities.clear()
            selectedFacilities = facilities.findAll { cmd.fortnoxId.equals(it.getFortnoxCustomerId()) }
            cmd.facilityIds = selectedFacilities.collect { it.id }
            cmd.fortnoxId = null
        } else if (cmd.facilityIds?.size() > 0) {
            selectedFacilities = facilities.findAll { cmd.facilityIds.contains(it.id) }
        } else {
            selectedFacilities = []
        }

        Map<Long, FacilityPaymentSummary> result = [:]
        def interval = new Interval(start, end)

        // Payment fees (adyen, coupons)
        selectedFacilities.each {
            result.put(it.id, facilityStatisticsService.getPaymentStatisticsFor(it, interval))
        }

        // Text messages fees
        addTextMessagesFees(interval, result)

        [result: result, facilities: facilities, cmd: cmd, start: start, end: end, selectedFacilities: selectedFacilities]
    }

    // Special case function. Every month, we need these values and this is to avoid one hour of work to setup fresh DB and fetch values
    def specialValues(AdminListStatsCommand cmd) {
        DateTime start = dateParam(params.start, new DateMidnight().withDayOfMonth(1))
        DateTime end = dateParam(params.end, new DateMidnight().plusMonths(1).withDayOfMonth(1).minusDays(1)).plusDays(1)

        long salkId = new Long(22)
        long mbcId = new Long(5)

        int mbc = facilityStatisticsService.getOnlineCouponBookings(mbcId, start, end)
        def salk = facilityStatisticsService.getCreditedBookings(salkId, start, end)

        List messages = []

        salk.each {
            messages.add("${it.count} x ${it.amount}kr")
        }

        flash.message = "MBC: ${mbc}, SALK: " + messages.join(", ")

        render(view: 'index', model: index(cmd))
    }

    private void addTextMessagesFees(Interval interval, def result) {
        try {
            def smsReport = smsService.report(interval.start.toLocalDate(), interval.end.toLocalDate())
            smsReport.each {
                if (it?.facility) {
                    FacilityPaymentSummary summary = result.get(it.facility.id)
                    FacilityContract contract = FacilityContract.activeContract(it?.facility, interval.start.toDate()).list()[0]
                    if (summary && contract) {
                        summary.fees << new FacilityFeeEntry(type: "sms", count: it.count, price: contract.variableTextMessageFee)
                    }
                }

            }
        } catch (Throwable t) {
            log.error("Unable to fetch SMS reports: ${t.getMessage()}")
        }
    }


    def income(String currency) {
        currency = currency ?: 'SEK'
        def serviceFee = grailsApplication.config.matchi.settings.currency[currency].serviceFee
        def start = dateParam(params.start, new DateMidnight().withDayOfMonth(1))
        def end = dateUtil.endOfDay(dateParam(params.end, new DateMidnight().plusMonths(1).withDayOfMonth(1).minusDays(1)))
        def interval = new Interval(start, end)
        def facilities = Facility.createCriteria().listDistinct { eq('currency', currency) }

        def query = """
            select
                count(op.id) as cnt,
                sum(CASE WHEN (op.type = 'netaxept' or op.type = 'adyen') THEN (amount-credited) ELSE 0 END) as total,
                sum(CASE WHEN (op.type = 'netaxept' or op.type = 'adyen') THEN (credited) ELSE 0 END) as total_credited,
                sum(CASE WHEN (op.type = 'coupon') THEN 1 ELSE 0 END) as num_coupon,
                sum(CASE WHEN (op.type = 'netaxept') THEN 1 ELSE 0 END) as num_netaxept,
                sum(CASE WHEN (op.type = 'adyen') THEN 1 ELSE 0 END) as num_adyen,
                sum(CASE WHEN (o.origin = 'api') THEN 1 ELSE 0 END) as num_api,
                op.date_created as date
            from
                order_payment op
            left join
                order_order_payments oop on op.id = oop.payment_id
            left join
                `order` o on oop.order_id = o.id
            left join
                facility fac on o.facility_id = fac.id
            where
                (op.type = 'adyen' or op.type = 'coupon')
                and (op.status = 'CAPTURED' or op.status = 'CREDITED')
                and fac.currency = '${currency}'
                and op.date_created > :start and op.date_created < :end and (o.origin = 'web' or o.origin = 'api')
            group by date(op.date_created);
            """

        def data = groovySql.rows(query, [start: start.toDate(), end: end.toDate()])

        def cancelFees1 = groovySql.firstRow("""select sum(op.amount-op.credited)
                from order_order_payments oop
                left join `order` o on o.id = oop.order_id
                left join order_payment op on op.id = oop.payment_id
                join facility f on f.id = o.facility_id
                where op.credited > 0 and f.currency = :currency and op.status = "CREDITED"
                and (op.amount-op.credited) = ${serviceFee} and op.last_updated > :start and op.last_updated < :end
                and (op.type = 'Netaxept' or op.type = 'Adyen')""",
                [currency: currency, start: start.toDate(), end: end.toDate()])[0]
        def cancelFees2 = groovySql.firstRow("""select count(*)*${serviceFee}
                from payment p
                join facility f on f.id = p.facility_id
                where date_reversed is not null and amount = :amount
                and f.currency = :currency and p.date_created > :start and p.date_created < :end""",
                [amount: (serviceFee * 100).intValue(), currency: currency, start: start.toDate(), end: end.toDate()])[0]

        def fixedFees = 0
        def oneTimeFees = 0
        def monthlyFees = 0
        def yearlyFees = 0


        //Contract
        def contractFacilities = []
        facilities.each {
            FacilityContract contract = FacilityContract.activeContract(it, interval.start.toDate()).list()[0]

            if (contract) {
                fixedFees += contract.fixedMonthlyFee
                oneTimeFees += facilityStatisticsService.getOneTimeContractItems(contract, interval)?.sum {
                    it.price
                } ?: 0
                monthlyFees += facilityStatisticsService.getMonthlyContractItems(contract, interval)?.sum {
                    it.price
                } ?: 0
                yearlyFees += facilityStatisticsService.getYearlyContractItems(contract, interval)?.sum {
                    it.price
                } ?: 0

                contractFacilities << it
            }
        }

        def facilityFees = [:]

        // Payment fees (adyen, netaxept, coupons)
        contractFacilities.each {
            facilityFees.put(it.id, facilityStatisticsService.getPaymentStatisticsFor(it, interval))
        }

        // Text messages fees
        addTextMessagesFees(interval, facilityFees)

        def variableFees = 0 //getTotalVariableFees()
        def offerFees = 0 //getTotalCouponFees() + getTotalGiftCardFees()
        def totalFees = 0 //getTotalFees()

        facilityFees.values().each {
            variableFees += it.getTotalVariableFees()
            offerFees += (it.getTotalCouponFees() + it.getTotalGiftCardFees())
            totalFees += it.getTotalFees()
        }


        groovySql.close()
        [start       : start, end: end, data: data, cancellationFees: (cancelFees1 ?: 0) + cancelFees2,
         fixedFees   : fixedFees, oneTimeFees: oneTimeFees, monthlyFees: monthlyFees, yearlyFees: yearlyFees,
         variableFees: variableFees, offerFees: offerFees, totalFees: totalFees, contractFacilities: contractFacilities]
    }

    def invoice(AdminListStatsCommand cmd) {
        def start = dateParam(params.start, new DateMidnight().withDayOfMonth(1))
        def end = dateUtil.endOfDay(dateParam(params.end, new DateMidnight().plusMonths(1).withDayOfMonth(1).minusDays(1)))
        def facilities = Facility.createCriteria().listDistinct {
            if (cmd.facilityIds?.size() > 0) {
                inList("id", cmd.facilityIds)
            }
            eq('currency', cmd.currency)

            order("name", "asc")
        }

        def invoiceErrors = []
        facilities.each { Facility f ->
            if (f.getFacilityProperty(FacilityProperty.FacilityPropertyKey.FORTNOX3_CUSTOMER_NUMBER.name())) {
                FacilityPaymentSummary summary = facilityStatisticsService.getPaymentStatisticsFor(f, new Interval(start, end))

                def rows = createInternalInvoiceRows(start, summary)

                if (log.debugEnabled) {
                    rows.each {
                        log.debug("ArticleNumber: ${it.ArticleNumber}, " +
                                "Description: ${it.Description}, " +
                                "Price: ${it.Price}, " +
                                "DeliveredQuantity: ${it.DeliveredQuantity}")
                    }
                }

                def invoice = createInternalInvoice(f, rows, start)

                log.info("Creating internal invoice for ${f.name}")
                try {
                    fortnoxFacadeService.saveMatchiInternalInvoiceToFortnox(invoice)
                } catch (FortnoxException e) {
                    invoiceErrors.add("${f.name}: ${e.getMessage()}")
                }
            } else {
                log.info("Could not create internal invoice for ${f.name}. No Fortnox customer number.")
            }
        }

        if (invoiceErrors.size() > 0) {
            flash.error = invoiceErrors.join(", ")
        } else {
            flash.message = message(code: "adminStatistics.invoice.success")
        }

        redirect(action: "index", params: params)
    }


    private DateTime dateParam(parameter, defaultValue) {
        if (parameter != null) {
            new DateMidnight(parameter).toDateTime()
        } else {
            defaultValue.toDateTime()
        }
    }

    private List<FortnoxInvoiceRow> createInternalInvoiceRows(DateTime date, FacilityPaymentSummary summary) {

        def invoiceRows = []
        def articleFees = Holders.config.matchi.fortnox.api.v3.article.fees
        def articleTransactionFees = Holders.config.matchi.fortnox.api.v3.article.transaction.fees
        def articleTransactionVariableFees = Holders.config.matchi.fortnox.api.v3.article.transaction.variable.fees
        def articleAddonFees = Holders.config.matchi.fortnox.api.v3.article.addon.fees
        def articleOnlineBookingDeduction = Holders.config.matchi.fortnox.api.v3.article.onlineBooking.deduction
        def articleCourseSubmissionDeduction = Holders.config.matchi.fortnox.api.v3.article.courseSubmission.deduction
        def articleActivityDeduction = Holders.config.matchi.fortnox.api.v3.article.activity.deduction
        def articleCouponDeduction = Holders.config.matchi.fortnox.api.v3.article.coupon.deduction
        def articleMembershipDeduction = Holders.config.matchi.fortnox.api.v3.article.membership.deduction
        def articlePromoCodes = Holders.config.matchi.fortnox.api.v3.article.promoCodes
        def articleCouponOfferFees = Holders.config.matchi.fortnox.api.v3.article.coupon.fees
        def articleCouponVariableOfferFees = Holders.config.matchi.fortnox.api.v3.article.coupon.variable.fees
        def articleGiftCardOfferFees = Holders.config.matchi.fortnox.api.v3.article.giftcard.fees
        def articleGiftCardVariableOfferFees = Holders.config.matchi.fortnox.api.v3.article.giftcard.variable.fees

        def vat = getVat(summary?.facility)

        // Describe facility
        invoiceRows << new FortnoxInvoiceRow(Description: "Refers to ${summary?.facility?.name}", VAT: 0)
        // Describe month
        invoiceRows << new FortnoxInvoiceRow(Description: "${date.toString("MMMM").capitalize()} ${date.toString("yyyy")}", VAT: 0)
        // Empty row
        invoiceRows << new FortnoxInvoiceRow(VAT: 0)
        // VAT entries (our income)
        if (summary.hasContract()) {
            // Fixed fees
            invoiceRows << new FortnoxInvoiceRow(
                    ArticleNumber: articleFees,
                    DeliveredQuantity: 1,
                    Price: summary.getFixedFee(),
                    VAT: vat)

            // Contract items
            summary.contractItems.each { contractItem ->
                invoiceRows << new FortnoxInvoiceRow(
                        AccountNumber: !contractItem.articleNumber ? contractItem.account ? Integer.parseInt(contractItem?.account) : null : null,
                        ArticleNumber: contractItem.articleNumber ? contractItem.articleNumber : !contractItem.account ? articleAddonFees : "",
                        Description: contractItem.description,
                        DeliveredQuantity: 1,
                        Price: contractItem.price,
                        VAT: vat)
            }

            // Minimal variable fees
            if (summary.getMinimalFeeVariableFeesEntries().size() > 0) {
                invoiceRows << new FortnoxInvoiceRow(
                        ArticleNumber: articleTransactionFees,
                        Description: "Payment debit/credit card fee",
                        DeliveredQuantity: (Integer) summary.getMinimalFeeVariableFeesEntries().sum(0) { it.num },
                        Price: summary.contract?.variableMediationFee,
                        VAT: vat)
            }

            // Variable fees %
            if (summary.getPercentageVariableFeesEntries().size() > 0 && summary.contract?.variableMediationFeePercentage) {
                def price = (BigDecimal) summary.getPercentageVariableFeesEntries().sum(0) {
                    summary.getVariableFee(it)
                }

                invoiceRows << new FortnoxInvoiceRow(
                        ArticleNumber: articleTransactionVariableFees,
                        Description: "Payment debit/credit card fee ${summary.contract?.variableMediationFeePercentage}% ${summary.contract.mediationFeeMode == FacilityContract.MediationFeeMode.AND ? "+ ${summary.contract.variableMediationFee} " : ""}(${summary.getPercentageVariableFeesEntries().sum(0) { it.num }}st)",
                        DeliveredQuantity: 1,
                        Price: price,
                        VAT: vat)
            }

            summary.getCouponEntries()?.each { entry ->
                invoiceRows << createOfferInvoiceRow(entry, vat, articleCouponOfferFees, articleCouponVariableOfferFees)
            }

            summary.getGiftCardEntries()?.each { entry ->
                invoiceRows << createOfferInvoiceRow(entry, vat, articleGiftCardOfferFees, articleGiftCardVariableOfferFees)
            }
        }

        // VAT info
        invoiceRows << new FortnoxInvoiceRow(Description: "The above amounts are subject to VAT", VAT: 0)
        // Empty row
        invoiceRows << new FortnoxInvoiceRow(VAT: 0)
        // None VAT info
        invoiceRows << new FortnoxInvoiceRow(Description: "Payment is due", VAT: 0)

        // None VAT entries (facility income)
        def totalNumberCouponBookings = [:]

        summary.entries?.each { row ->
            if (!row.type?.equals('coupon')) {
                def price = (BigDecimal) row.price

                if (row.article == Order.Article.FORM_SUBMISSION.name()) {
                    facilityStatisticsService.getSubmissionGroups(summary, price).each {
                        invoiceRows << new FortnoxInvoiceRow(
                                ArticleNumber: articleCourseSubmissionDeduction,
                                Description: message(code: "articleType.FORM_SUBMISSION.invoiceDescription",
                                        locale: "en",
                                        args: [it.submission_name]).toString().decodeHTML(),
                                DeliveredQuantity: -(Integer) it.num,
                                Price: price,
                                VAT: 0)
                    }
                } else if (row.article == Order.Article.ACTIVITY.name()) {
                    facilityStatisticsService.getActivitiesGroups(summary, price).each {
                        invoiceRows << new FortnoxInvoiceRow(
                                ArticleNumber: articleActivityDeduction,
                                Description: message(code: "articleType.ACTIVITY.invoiceDescription",
                                        locale: "en",
                                        args: [it.activity_name]).toString().decodeHTML(),
                                DeliveredQuantity: -(Integer) it.num,
                                Price: price,
                                VAT: 0)
                    }
                } else {
                    def article
                    switch (row.article) {
                        case Order.Article.MEMBERSHIP.name():
                            article = articleMembershipDeduction
                            break
                        case Order.Article.COUPON.name():
                            article = articleCouponDeduction
                            break
                        default:
                            article = articleOnlineBookingDeduction
                            break
                    }

                    invoiceRows << new FortnoxInvoiceRow(
                            ArticleNumber: article,
                            Description: message(code: "articleType.${row.article}.invoiceDescription", locale: "en")
                                    + (row.sport ? " " + row.sport : "")
                                    + (BooleanUtils.isNotFalse(row.indoor) ? "" : " " + message(code: "court.outdoors.label", locale: "en")),
                            DeliveredQuantity: -(Integer) row.num,
                            Price: price,
                            VAT: 0)
                }
            } else {
                totalNumberCouponBookings[row.couponFacilityId] = (totalNumberCouponBookings[row.couponFacilityId] ?: 0) + row.num
            }
        }

        totalNumberCouponBookings.each {
            // Summary total number of coupon bookings
            invoiceRows << new FortnoxInvoiceRow(
                ArticleNumber: articleCouponDeduction,
                Description: message(code: "articleType.COUPON_BOOKING.invoiceDescription", args: [Facility.get(it.key)]),
                DeliveredQuantity: -1 * Integer.parseInt(it.value.toString()),
                Price: 0,
                VAT: 0)
        }

        //Promo codes
        summary.getPromoCodeDiscounts()?.each { promo ->
            invoiceRows << new FortnoxInvoiceRow(
                    ArticleNumber: articlePromoCodes,
                    Description: "Total discount with promotion code: " + promo.type,
                    DeliveredQuantity: 1,
                    Price: promo.total,
                    VAT: 0)
        }

        if (!summary.detailedOfferEntries.empty && summary.facility.isMasterFacility()) {

            invoiceRows << new FortnoxInvoiceRow()

            invoiceRows << new FortnoxInvoiceRow(Description: "Specification for punch and gift cards")

            summary.detailedOfferEntries.groupBy { FacilityCouponFeeEntry it ->
                it.facility.name
            }.each {
                invoiceRows << new FortnoxInvoiceRow()
                invoiceRows << new FortnoxInvoiceRow(Description: it.key)

                it.value.each { FacilityCouponFeeEntry entry ->
                    // Summary total number of coupon bookings
                    invoiceRows << new FortnoxInvoiceRow(
                        ArticleNumber: "",
                        Description: message(code: "adminStatistics.index." + entry.type, args: [entry.fee, message(code: "adminStatistics.index.feeType." + entry.feeType)]),
                        DeliveredQuantity: -1 * entry.count,
                        Price: 0,
                        VAT: 0)
                }
            }
        }

        if (!summary.membershipEntries.empty) {

            invoiceRows << new FortnoxInvoiceRow()
            invoiceRows << new FortnoxInvoiceRow(Description: message(code: "adminStatistics.globalMembershipShare.description"))

            summary.membershipEntries.each { entry ->
                invoiceRows << new FortnoxInvoiceRow(
                    ArticleNumber: "",
                    Description: "Home facility " + (entry.facility?.name ?: "Unknown") + ":",
                    Price: 0,
                    VAT: 0)

                invoiceRows << new FortnoxInvoiceRow(
                    ArticleNumber: "",
                    Description: "----Memberships total amount: " + entry.totalFee,
                    Price: 0,
                    VAT: 0)
            }
        }

        // Simple fix to avoid API errors when admins copy text from Word into for example Activity descriptions.
        invoiceRows.each {
            if (it.Description) {
                it.Description = it.Description.replaceAll("[”]", "\"").replaceAll("[’]", "'")
            }
        }

        return invoiceRows
    }

    private createOfferInvoiceRow(def offer, Integer vat,def articleOfferFees, def articleVariableOfferFees) {
        def feeDescription = message(code: "adminStatistics.index.feeType.${offer.feeType}", locale: "en")
        def offerArticle

        switch (offer?.feeType) {
            case FacilityContract.CouponContractType.PER_COUPON.name() ||
                    FacilityContract.GiftCardContractType.PER_USE.name():
                offerArticle = articleOfferFees
                break
            default:
                offerArticle = articleVariableOfferFees
                break
        }

        return new FortnoxInvoiceRow(
                ArticleNumber: offerArticle,
                Description: message(code: "adminStatistics.index.${offer.type}", locale: "en", args: [offer.fee, feeDescription]),
                DeliveredQuantity: offer.count,
                Price: offer.fee,
                VAT: vat)
    }

    private FortnoxInvoice createInternalInvoice(Facility facility, List<FortnoxInvoiceRow> rows, DateTime date) {

        def invoiceDate = dateUtil.toEndOfMonth(date)
        def facilityAccount = facility.bankgiro ? "bankgironr ${facility.bankgiro}." : "plusgironr ${facility.plusgiro}."
        def total = 0

        def invoice = new FortnoxInvoice()
        invoice.CostCenter = getCostCenter(facility)

        rows.each {
            invoice.InvoiceRows.add(it)
            total += it.DeliveredQuantity && it.Price ? (it.DeliveredQuantity.toInteger() * it.Price) : 0
        }

        invoice.CustomerNumber = facility.getFacilityPropertyValue(FacilityProperty.FacilityPropertyKey.FORTNOX3_CUSTOMER_NUMBER.name())
        invoice.InvoiceDate = invoiceDate.toDate()
        invoice.Currency = facility.currency
        invoice.DueDate = FortnoxInvoice.createInternalInvoiceDueDate(invoiceDate, total)
        invoice.Remarks = "Notification of earnings and invoicing of costs. Any claim on MATCHi will be paid to your specified account.\n" +
                "For any changes, please contact finance@matchi.se"

        invoice.Language ="EN"

        return invoice
    }

    private static String getCostCenter(Facility facility) {
        return facility?.country
    }

    private static Integer getVat(Facility facility) {
        facility?.country == "SE" ? MATCHi_VAT : 0
    }

}

@Validateable(nullable = true)
class AdminListStatsCommand {
    List<Long> facilityIds
    String currency = "SEK"
    String fortnoxId
}