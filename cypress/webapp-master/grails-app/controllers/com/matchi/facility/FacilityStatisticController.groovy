package com.matchi.facility

import com.matchi.Court
import com.matchi.Customer
import com.matchi.Facility
import com.matchi.GenericController
import com.matchi.MatchiConfig
import com.matchi.MatchiConfigKey
import com.matchi.async.ScheduledTask
import com.matchi.async.ScheduledTaskService
import com.matchi.coupon.Coupon
import com.matchi.coupon.GiftCard
import com.matchi.excel.ExcelExportManager
import com.matchi.orders.*
import com.matchi.payment.ArticleType
import com.matchi.payment.PaymentMethod
import grails.validation.Validateable
import groovy.json.JsonOutput
import org.hibernate.Hibernate
import org.joda.time.DateMidnight
import org.joda.time.DateTime
import org.joda.time.Interval
import org.joda.time.LocalTime

import java.text.DecimalFormat

class FacilityStatisticController extends GenericController {
    def groovySql
    def dateUtil
    def courtService
    def fileArchiveService
    def paymentService
    ScheduledTaskService scheduledTaskService
    ExcelExportManager excelExportManager

    private String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss"
    static final String COLON = ":"
    static final String COLON_CODE = '\\:'

    def index(FilterStatsCommand cmd) {
        def facilityStatisticsConfig = MatchiConfig.findByKey(MatchiConfigKey.DISABLE_FACILITY_STATISTICS)
        if (facilityStatisticsConfig.isBlocked()) {
            render facilityStatisticsConfig.isBlockedMessage()
        }

        Facility facility = getUserFacility()
        if (!facility) {
            render(view: "noFacility")
            return
        }

        List<Court> courts = Court.available(facility).list()

        DateTime start = dateParam(cmd.start, new DateMidnight().withDayOfWeek(1))
        DateTime end = dateUtil.endOfDay(dateParam(cmd.end, new DateMidnight().withDayOfWeek(7)))
        def interval = new Interval(start, end)
        cmd.courtIds = paramSelectedCourts(courts, cmd.courtIds)

        def queryParameters = [
                start     : start.toString(DATE_FORMAT),
                end       : end.toString(DATE_FORMAT),
                facilityId: getUserFacility().id]
        def whereString = ""
        def courtString = ""

        if (cmd.startTime && cmd.endTime) {
            if(dateUtil.isMidnight(new LocalTime(cmd.endTime)) && !dateUtil.isMidnight(new LocalTime(cmd.startTime))) {
                queryParameters.put("startTime", cmd.startTime)
                whereString += " AND ((TIME(s.start_time) >= :startTime AND TIME(s.end_time) >= :startTime) OR (TIME(s.start_time) >= :startTime AND datediff(s.end_time, s.start_time) > 0)) "
            } else if(!dateUtil.isMidnight(new LocalTime(cmd.endTime)) && dateUtil.isMidnight(new LocalTime(cmd.startTime))) {
                queryParameters.put("endTime", new LocalTime(cmd.endTime)?.toString())
                whereString += " AND TIME(s.end_time) > TIME(s.start_time) AND TIME(s.end_time) <= :endTime "
            } else {
                queryParameters.put("startTime", cmd.startTime)
                queryParameters.put("endTime", new LocalTime(cmd.endTime)?.toString())
                whereString += " AND TIME(s.start_time) >= :startTime AND TIME(s.end_time) <= :endTime  AND TIME(s.end_time) > TIME(s.start_time) "
            }

        }
        else if (cmd.startTime && !cmd.endTime && !dateUtil.isMidnight(new LocalTime(cmd.startTime))) {
            queryParameters.put("startTime", cmd.startTime)
            whereString += " AND ((TIME(s.end_time) > TIME(s.start_time) AND TIME(s.start_time) >= :startTime) OR (TIME(s.start_time) >= :startTime AND datediff(s.end_time, s.start_time) > 0))"
        }
        else if (cmd.endTime && !cmd.startTime && !dateUtil.isMidnight(new LocalTime(cmd.endTime))) {
            queryParameters.put("endTime", new LocalTime(cmd.endTime)?.toString())
            whereString += " AND TIME(s.end_time) > TIME(s.start_time) AND TIME(s.end_time) <= :endTime "
        }

        if (cmd.weekdays) {
            whereString += " AND (WEEKDAY(s.start_time)+1) in (${cmd.weekdays.join(',')}) "
        }

        if(cmd.courtIds) {
            courtString += " AND c.id in (${cmd.courtIds.join(", ")}) "
        }

        def query = """SELECT DATE(s.start_time) AS date,
            count(s.id) as num_slots,
            count(b.id) as num_bookings,
            count(bg.id) as num_groups,
            count(p.id) as num_payment,

            SUM(CASE WHEN ((p.method = 'CREDIT_CARD' OR p.method = 'CREDIT_CARD_RECUR' OR p.method = 'COUPON') OR (o.id is not null and o.article != 'SUBSCRIPTION_BOOKING' and (o.status = 'CONFIRMED' or o.status = 'COMPLETED' or (o.status = 'NEW' and o.price="0.00")) and (o.origin = 'web' or o.origin = 'api'))) THEN 1 ELSE 0 END) as num_online,
            sum(CASE WHEN (bg.id is null AND b.id is not null) THEN 1 ELSE 0 END) as num_standalone,
            sum(CASE WHEN bg.type = 'SUBSCRIPTION' THEN 1 ELSE 0 END) as num_subscription,
            sum(CASE WHEN bg.type = 'NOT_AVAILABLE' THEN 1 ELSE 0 END) as num_na,
            sum(CASE WHEN bg.type = 'ACTIVITY' THEN 1 ELSE 0 END) as num_activity,
            sum(CASE WHEN bg.type = 'COMPETITION' THEN 1 ELSE 0 END) as num_competition,
            sum(CASE WHEN bg.type = 'TRAINING' THEN 1 ELSE 0 END) as num_training,
            sum(CASE WHEN bg.type = 'DEFAULT' THEN 1 ELSE 0 END) as num_default,
            sum(p.amount) as amount

            FROM slot s
                LEFT JOIN booking b on s.id = b.slot_id
                LEFT JOIN court c on s.court_id = c.id
                LEFT JOIN payment p on b.payment_id = p.id
                LEFT JOIN `order` o on b.order_id = o.id
                LEFT JOIN booking_group bg on b.group_id = bg.id

            WHERE
                s.start_time >= :start
                AND s.start_time <= :end
                AND c.facility_id = :facilityId
                ${courtString}
                ${whereString}

            GROUP BY DATE(s.start_time)
            ORDER BY s.start_time desc
                """

        def rows = groovySql.rows(query, queryParameters)
        def result = prepareBookingResultData(rows, interval)
        groovySql.close()

        [facility: facility, courts: courts, result: result, start: start, end: end, cmd: cmd]
    }

    def payment() {
        def facilityStatisticsConfig = MatchiConfig.findByKey(MatchiConfigKey.DISABLE_FACILITY_STATISTICS)
        if (facilityStatisticsConfig.isBlocked()) {
            render facilityStatisticsConfig.isBlockedMessage()
        }

        Facility facility = getUserFacility()
        if (!facility) {
            render(view: "noFacility")
            return
        }

        PaymentData paymentData = getPaymentData(facility, params)

        [start : paymentData.start, end: paymentData.end, courts: paymentData.courts, selectedCourtIds: paymentData.selectedCourtIds, selectedMethods: paymentData.selectedMethods,
         result: paymentData.result, transactions: paymentData.transactions, selectedIncomeTypes: paymentData.selectedIncomeTypes, facility: facility]
    }

    def customer() {
        def facilityStatisticsConfig = MatchiConfig.findByKey(MatchiConfigKey.DISABLE_FACILITY_STATISTICS)
        if (facilityStatisticsConfig.isBlocked()) {
            render facilityStatisticsConfig.isBlockedMessage()
        }

        Facility facility = getUserFacility()
        if (!facility) {
            render(view: "noFacility")
            return
        }

        String BOOKINGS_REF   = "bookings"
        String ACTIVITIES_REF = "activities"
        String COUPONS_REF    = "coupons"
        String OTHERS_REF     = "others"
        String TOTAL_REF      = "total"
        String AMOUNT_REF     = "amount"

        PaymentData paymentData = getPaymentData(facility, params)
        Map<Customer, Map> customers = [:]

        paymentData.transactions.each {
            Customer customer          = it.customer
            Order.Article article      = it.article
            Map<String, Integer> stats = [ bookings: 0, activities: 0, coupons: 0, others: 0, total: 0, amount: 0 ]

            Boolean customerInMapExists = customers.containsKey(customer)

            if (!customerInMapExists) {
                customers.put(customer, stats)
            }

            switch (article) {
                case Order.Article.BOOKING:
                    customers[customer][BOOKINGS_REF]   = customers[customer][BOOKINGS_REF] + 1
                    break
                case Order.Article.ACTIVITY:
                    customers[customer][ACTIVITIES_REF] = customers[customer][ACTIVITIES_REF] + 1
                    break
                case Order.Article.COUPON:
                    customers[customer][COUPONS_REF]    = customers[customer][COUPONS_REF] + 1
                    break
                default:
                    customers[customer][OTHERS_REF]     = customers[customer][OTHERS_REF] + 1
                    break
            }

            customers[customer][TOTAL_REF]  =  customers[customer][TOTAL_REF] + 1
            customers[customer][AMOUNT_REF] =  customers[customer][AMOUNT_REF].toInteger() + it.amount.toInteger()
        }

        Map sorted = [:]
        if (!params.sort || !params.order) {
            sorted = customers.sort { -it.value.amount }
        } else {
            if (params.order == "desc") {
                sorted = customers.sort { -it.value."$params.sort" }
            } else {
                sorted = customers.sort { it.value."$params.sort" }
            }
        }

        [start : paymentData.start, end: paymentData.end, courts: paymentData.courts, selectedCourtIds: paymentData.selectedCourtIds, selectedMethods: paymentData.selectedMethods,
         result: paymentData.result, transactions: paymentData.transactions, selectedIncomeTypes: paymentData.selectedIncomeTypes, customers: sorted, facility: facility ]
    }

    def export() {
        Facility facility = getUserFacility()
        if (!facility) {
            render(view: "noFacility")
            return
        }

        // Pass along reference to "params" due to no web request scope when executing a scheduled task (new thread).
        def params = params
        String resultFileName = message(code: 'adminStatistics.index.income') + "_" + params.start + "_" + params.end + "." + excelExportManager.EXCEL_FILE_EXTENSION;
        Locale locale = new Locale(getCurrentUser().language)
        PaymentData paymentData = getPaymentData(facility, params)

        scheduledTaskService.scheduleTask(message(code: 'scheduledTask.exportIncome.taskName'), facility.id, facility) { taskId ->
            def exportFile = File.createTempFile("statistics_export-", ".xls")

            exportFile.withOutputStream { out ->
                excelExportManager.exportStatisticsIncome(paymentData.transactions, locale, out)
            }

            def task = ScheduledTask.get(taskId)
            task.resultFileName = resultFileName
            task.resultFilePath = fileArchiveService.storeExportedFile(exportFile)
            task.save(flush: true)

            exportFile.delete()
        }
        redirect(action: "payment", params: params)
    }

    private PaymentData getPaymentData(Facility facility, def params) {
        def courts = Court.available(facility).list()

        def start = dateParam(params.start, new DateMidnight().withDayOfWeek(1))
        def end = dateUtil.endOfDay(dateParam(params.end, new DateMidnight().withDayOfWeek(7)))
        def interval = new Interval(start, end)

        List<Long> selectedCourtIds = params.list("courtIds").collect { it as Long }
        def courtRestriction = ""
        def orderCourtRestriction = ""
        if (selectedCourtIds) {
            courtRestriction = " AND c.id in (${selectedCourtIds.join(',')}) "
            orderCourtRestriction = " AND (c.id in (${selectedCourtIds.join(',')}) OR c2.id in (${selectedCourtIds.join(',')})) "
        }

        List selectedIncomeTypes = params.incomeTypes ? params.list('incomeTypes') : []
        def incomeRestrictionForOrders = ""
        if (selectedIncomeTypes) {
            incomeRestrictionForOrders = " AND o.article in (${selectedIncomeTypes.collect { "'${it}'" }.join(',')}) "
        }
        def incomeRestrictionForPayments = ""
        if (selectedIncomeTypes) {
            incomeRestrictionForPayments = " AND article_type in (${selectedIncomeTypes.collect { "'${it}'" }.join(',')}) "
        }
        List<String> selectedMethods = paramSelectedMethods(params)

        def methods = selectedMethods.clone()

        // We add recur credit card if credit card is selected
        if (selectedMethods.contains(PaymentMethod.CREDIT_CARD.toString())) {
            methods << PaymentMethod.CREDIT_CARD_RECUR.toString()
        }

        def paymentTypes = [] as Set

        methods.each {
            switch (it) {
                case PaymentMethod.CREDIT_CARD.toString():
                    paymentTypes << "netaxept"
                    paymentTypes << "adyen"
                    break;
                case PaymentMethod.CREDIT_CARD_RECUR.toString():
                    paymentTypes << "netaxept"
                    paymentTypes << "adyen"
                    break;
                case PaymentMethod.COUPON.toString():
                    paymentTypes << "coupon"
                    break;
                case PaymentMethod.GIFT_CARD.toString():
                    paymentTypes << "coupon"
                    break;
            }
        }
        def queryParameters = [
                start     : start.toString(DATE_FORMAT),
                end       : end.toString(DATE_FORMAT),
                startTime : params.startTime,
                endTime   : params.endTime,
                facilityId: facility.id]

        def timeRestrictionsForOrders = ""
        def timeRestrictionsForPayments = ""

        if (params.startTime) {
            String[] split = params.startTime.split(COLON);
            String hours = String.valueOf(split[0]);
            String minutes = String.valueOf(split[1]);
            timeRestrictionsForOrders += " AND TIME(s.start_time) >= TIME('" + hours + COLON_CODE + minutes+"')"
            timeRestrictionsForPayments += " AND TIME(start_time) >= TIME('" + hours + COLON + minutes+"')"

        }
        if (params.endTime) {
            String[] split2 = params.endTime.split(COLON);
            String hours = String.valueOf(split2[0]);
            String minutes = String.valueOf(split2[1]);
            timeRestrictionsForOrders += " AND TIME(s.end_time) <= TIME('" + hours + COLON_CODE + minutes+"')"
            timeRestrictionsForPayments += " AND TIME(end_time) <= TIME('" + hours + COLON + minutes+"')"
        }

        def query = """
            select
                sum(num_creditcard) as num_creditcard,
                sum(num_coupon) as num_coupon,
                sum(num_cancelled) as num_cancelled,
                sum(amount_online) as amount_online,
                date
                from
                (
                SELECT
                    DATE(p.date_delivery) as date,
                    COUNT(p.id),
                    SUM(CASE WHEN (p.method = 'CREDIT_CARD' OR p.method = 'CREDIT_CARD_RECUR') THEN 1 ELSE 0 END) as num_creditcard,
                    SUM(CASE WHEN (p.method = 'COUPON') THEN 1 ELSE 0 END) as num_coupon,
                    SUM(CASE WHEN (b.id is null and article_type = 'BOOKING') THEN 1 ELSE 0 END) as num_cancelled,
                    SUM(CASE WHEN (p.method = 'CREDIT_CARD' OR p.method = 'CREDIT_CARD_RECUR') THEN p.amount ELSE 0 END) as amount_online
                FROM payment p
                LEFT JOIN booking b on b.payment_id = p.id
                LEFT JOIN slot s on s.id = b.slot_id
                LEFT JOIN court c on c.id = s.court_id
                WHERE
                    p.facility_id = :facilityId
                    AND p.date_annulled is null
                    AND p.date_reversed is null
                    AND (p.method in (${methods.collect() { "'${it}'" }.join(',')}))
                    AND p.status = 'OK'
                    AND p.date_delivery >= :start
                    AND p.date_delivery <= :end
                    ${courtRestriction}
                    ${timeRestrictionsForPayments}
                    ${incomeRestrictionForPayments}

                GROUP BY
                    DATE(p.date_delivery)

                UNION ALL

                SELECT
                    DATE(o.date_delivery) as date,
                    COUNT(op.id),
                    SUM(CASE WHEN (op.type in ('netaxept', 'adyen') and (op.status = 'CAPTURED' or op.status = 'AUTHED' )) THEN 1 ELSE 0 END) as num_creditcard,
                    SUM(CASE WHEN (op.type = 'coupon' and (op.status = 'CAPTURED' or op.status = 'AUTHED' )) THEN 1 ELSE 0 END) as num_coupon,
                    SUM(CASE WHEN ((op.status = 'CAPTURED' or op.status = 'AUTHED' ) and b.id is null and o.article = 'BOOKING') THEN 1 ELSE 0 END) as num_cancelled,
                    SUM(CASE WHEN (op.type in ('netaxept', 'adyen') and (op.status = 'CAPTURED' or op.status = 'AUTHED' )) THEN op.amount*100 ELSE 0 END) as amount_online

                FROM order_payment op
                    LEFT JOIN order_order_payments oop on op.id = oop.payment_id
                    LEFT JOIN `order` o on oop.order_id = o.id
                    LEFT JOIN booking b on b.order_id = o.id
                    LEFT JOIN slot s on s.id = b.slot_id
                    LEFT JOIN court c on c.id = s.court_id
                    LEFT JOIN order_metadata md on md.metadata = o.id and md.metadata_idx = 'slotId'
                    LEFT JOIN slot s2 on s2.id = md.metadata_elt
                    LEFT JOIN court c2 on c2.id = s2.court_id

                WHERE
                    o.facility_id = :facilityId
                    AND o.date_delivery >= :start
                    AND o.date_delivery <= :end
                    AND (o.status in ('CONFIRMED', 'COMPLETED') OR (o.status = 'ANNULLED' and op.status = 'CAPTURED'))
                    AND op.status in ( 'CAPTURED' , 'CREDITED', 'AUTHED' )
                    AND op.type in (${paymentTypes.collect() { "'${it}'" }.join(',')})
                    ${orderCourtRestriction}
                    ${timeRestrictionsForOrders}
                    ${incomeRestrictionForOrders}
                GROUP BY
                    DATE(o.date_delivery)
                    ) t group by date
            """

        def rows = groovySql.rows(query, queryParameters)

        LinkedHashMap<String, Object> result = preparePaymentResultData(rows, interval)
        groovySql.close()
        List transactions = getPaymentTransactions(facility, interval, methods, selectedCourtIds, timeRestrictionsForPayments, incomeRestrictionForPayments)
        transactions += getOrderTransactions(facility, paymentTypes, methods, start, end, orderCourtRestriction, timeRestrictionsForOrders, incomeRestrictionForOrders)
        transactions.sort { a, b ->
            return a.date.compareTo(b.date)
        }
        return new PaymentData(
                start: start,
                end: end,
                courts: courts,
                selectedCourtIds: selectedCourtIds,
                selectedMethods: selectedMethods,
                result: result,
                transactions: transactions,
                selectedIncomeTypes: selectedIncomeTypes
        )
    }

    private List getOrderTransactions(Facility facility, paymentTypes, methods, DateTime start, DateTime end,
                                      courtRestriction, timeRestrictions, incomeRestriction) {
        retrieveOrderList(facility, start, end, courtRestriction, timeRestrictions,
                incomeRestriction, methods).collect {
                [
                        date    : it.dateDelivery,
                        customer: it.customer,
                        article : it.article.toString(),
                        status: it.status,
                        order_status: it.order_status,
                        method  : it.isPaidByGiftCard ? "Presentkort - $it.offerName" :
                                (it.isPaidByCoupon ? "Klippkort - $it.offerName" : "Kreditkort - netaxept/adyen"),
                        amount  : "${formatAmount(it.totalAmountPaid)}",
                        currency: "${facility?.currency}",
                        info: it.isPaidByGiftCard ? "(-${formatAmount(it.offerPaymentsAmount)})" : "-",
                        sport: it.sport,
                        indoor: it.indoor,
                        description: it.description
                ]
        }
    }

    private def formatAmount(def amount)
    {
        DecimalFormat df = new DecimalFormat("#")
        return df.format(amount);
    }

    private Collection<LinkedHashMap> retrieveOrderList(Facility facility, DateTime start, DateTime end,
                                   courtRestriction, timeRestrictions, incomeRestriction, ArrayList methods) {
        def ordersQuery = """ select distinct o.*,
            spo.name as sport,
            c2.indoor as indoor,
            activity.name as activity,
            op.id as op_id,
            op.amount as amount,
            op.credited as credited,
            coupon.name as offerName,
            op.method as method,
            op.status as status,
            o.status as order_status
            
            from `order` o 
            
            LEFT JOIN booking b on b.order_id = o.id
            LEFT JOIN slot s on s.id = b.slot_id
            LEFT JOIN court c on c.id = s.court_id
            LEFT JOIN order_metadata md on md.metadata = o.id and md.metadata_idx = 'slotId'
            LEFT JOIN slot s2 on s2.id = md.metadata_elt
            LEFT JOIN court c2 on c2.id = s2.court_id
            LEFT JOIN sport spo on c2.sport_id = spo.id
            LEFT JOIN submission sub on sub.order_id = o.id
            LEFT JOIN form on sub.form_id = form.id
            LEFT JOIN activity on activity.form_id = form.id 
            LEFT JOIN order_order_payments oop on o.id = oop.order_id
            LEFT JOIN order_payment op on oop.payment_id = op.id
            LEFT JOIN customer_coupon_ticket cct on op.ticket_id IS NOT NULL AND cct.id = op.ticket_id
            LEFT JOIN customer_coupon cc on op.ticket_id IS NOT NULL AND cct.customer_coupon_id = cc.id
            LEFT JOIN coupon on op.ticket_id IS NOT NULL AND cc.coupon_id = coupon.id 
            
            WHERE o.facility_id = :facilityId
            AND op.status in ('CREDITED', 'CAPTURED', 'AUTHED')
            AND op.method IN (${methods.collect() { "'${it}'" }.join(',')})
            AND o.date_delivery >= :start
            AND o.date_delivery <= :end
            AND (o.status='${Order.Status.CONFIRMED.name()}' or o.status='${Order.Status.COMPLETED.name()}' OR (o.status = '${Order.Status.ANNULLED.name()}' and op.status = '${OrderPayment.Status.CAPTURED.name()}'))
             ${courtRestriction}
             ${timeRestrictions}
             ${incomeRestriction}
            """
        LinkedHashMap<String, LinkedHashMap> orderList = new LinkedHashMap<>()

        try {
            def rows = groovySql.rows(ordersQuery, [
                    start     : start.toString(DATE_FORMAT),
                    end       : end.toString(DATE_FORMAT),
                    facilityId: facility.id
            ])

            rows.each { row ->

                if (orderList.get(row.id)) {
                    LinkedHashMap item = orderList.get(row.id)
                    item.totalAmountPaid += row.amount - row.credited

                }
                else {
                    def item = [:]
                    item.order_id = row.id
                    item.dateDelivery = new Date(row.date_delivery.time)
                    item.customer = Customer.read(row.customer_id)
                    item.article = row.article
                    item.status = row.status
                    item.order_status = row.order_status
                    item.sport = row.sport
                    item.indoor = row.indoor
                    item.description = (row.article == ArticleType.ACTIVITY.toString() ? row.description : row.activity)

                    item.totalAmountPaid = 0
                    item.offerPaymentsAmount = 0

                    item.isPaidByCoupon = (row.method == 'COUPON')
                    item.isPaidByGiftCard = (row.method == 'GIFT_CARD')
                    item.isPaidByCreditCard = row.method.equals("CREDIT_CARD") || row.method.equals("CREDIT_CARD_RECUR")


                    if (item.isPaidByCreditCard) {
                        item.totalAmountPaid = row.amount - row.credited
                    }
                    else if (item.isPaidByCoupon) {
                        item.offerName = row.offerName
                        item.offerPaymentsAmount = row.amount
                    }
                    else if (item.isPaidByGiftCard) {
                        item.offerName = row.offerName
                        item.offerPaymentsAmount = row.amount
                    }


                    orderList.put(row.id, item)
                }
            }
        } finally {
            groovySql.close()
        }

        orderList.values()
    }

    private List getPaymentTransactions(Facility facility, Interval interval, List methods,
                                        List<Long> selectedCourtIds, String timeRestrictions,
                                        String incomeRestrictionForPayments) {
        def transactions = []
        def payments = paymentService.getFacilityPayments(
                facility, interval, methods, selectedCourtIds, timeRestrictions, incomeRestrictionForPayments)

        payments.each {
            transactions << [
                    date    : it.dateDelivery,
                    customer: it.customer,
                    article : it.articleType.toString(),
                    method  : it.method.equals(PaymentMethod.COUPON) ?
                            "Klippkort - ${it.customerCoupon?.coupon?.name}" :
                            it.method.equals(PaymentMethod.GIFT_CARD) ?
                                    "Presentkort - ${it.customerCoupon?.coupon?.name}" : "Kreditkort/auriga",
                    amount  : it.amountFormatted(),
                    info: it.method.equals(PaymentMethod.GIFT_CARD) ? "(-${it.amountFormattedAmountOnly()})" : "-"
            ]
        }
        transactions
    }

    private def preparePaymentResultData(def rows, Interval interval) {

        def dates = dateUtil.createDateIntervals(interval.start, interval.end)
        def cache = [:]

        rows.each {
            cache.put(it.date.toString(), it)
        }

        def totalCancelled = rows.collect { it.num_cancelled }.sum(0)
        def totalAmount = (int) rows.collect { it.amount_online }.sum(0) / 100
        def totalNumCreditcard = rows.collect { it.num_creditcard }.sum(0)
        def totalNumCoupons = rows.collect { it.num_coupon }.sum(0)
        def result = [
                dates               : dates,
                rows                : rows,
                cache               : cache,
                total_amount        : totalAmount,
                total_num_creditcard: totalNumCreditcard,
                total_num_coupons   : totalNumCoupons,
                total_cancelled     : totalCancelled,
                avg_amount          : avg(totalAmount, dates.size()),
                avg_num_creditcard  : avg(totalNumCreditcard, dates.size()),
                avg_num_coupon      : avg(totalNumCoupons, dates.size())
        ]

        result
    }

    private def prepareBookingResultData(def rows, def interval) {

        def dates = dateUtil.createDateIntervals(interval.start, interval.end)

        rows.each {
            def numBookings = it.num_bookings - it.num_na
            it.num_bookings = numBookings
            it.num_available = (it.num_slots - it.num_na) - numBookings
            it.num_occupancy = percentage(numBookings, (it.num_slots - it.num_na))
        }

        def totalSlots = rows.collect() { it.num_slots }.sum(0)
        def totalNotAvailable = rows.collect() { it.num_na }.sum(0)
        def totalBookings = rows.collect() { it.num_bookings }.sum(0)
        def totalAvailable = (totalSlots - totalNotAvailable) - totalBookings
        def totalFree = rows.collect() { it.num_na }.sum(0)
        def totalStandalone = rows.collect() { it.num_standalone }.sum(0) + rows.collect() { it.num_default }.sum(0)
        def totalSlotsNaNotInluded = totalSlots - totalNotAvailable
        def totalOccupancy = percentage(totalBookings, totalSlots)
        def totalOccupancyNaNotIncluded = percentage(totalBookings, totalSlotsNaNotInluded)

        def avgStandalone = avg(totalStandalone, rows.size())
        def avgBookings = avg(totalBookings, rows.size())
        def totalOnline = rows.collect() { it.num_online }.sum(0)
        def totalTraining = rows.collect() { it.num_training }.sum(0)
        def totalSubscription = rows.collect() { it.num_subscription }.sum(0)
        def totalCompetition = rows.collect() { it.num_competition }.sum(0)
        def totalActivity = rows.collect() { it.num_activity }.sum(0)

        def result = [
                rows                           : rows,
                total_bookings                 : totalBookings,
                total_slots                    : totalSlots,
                total_occupancy                : totalOccupancy,
                total_occupancy_na_not_included: totalOccupancyNaNotIncluded,

                avg_bookings                   : avgBookings,

                total_online                   : totalOnline,
                avg_online                     : avg(totalOnline, dates.size()),

                total_training                 : totalTraining,
                total_subscription             : totalSubscription,
                total_competition              : totalCompetition,
                total_standalone               : totalStandalone,
                avg_standalone                 : avgStandalone,

                percentage_training            : percentage(totalTraining, totalBookings),
                percentage_subscription        : percentage(totalSubscription, totalBookings),
                percentage_competition         : percentage(totalCompetition, totalBookings),
                percentage_standalone          : percentage(totalStandalone, totalBookings),
                percentage_activity            : percentage(totalActivity,totalBookings),
        ]

        result
    }

    private def avg(def val, def sum) {
        (int) (val == 0 || sum == 0 ? 0 : (val / sum))
    }

    private def percentage(def val, def sum) {
        ((val == 0 || sum == 0) ? 0 : (float) ((val / sum) * 100)).round(2)
    }

    private def paramSelectedMethods(def params) {
        def selectedMethods
        if (params.methods) {
            selectedMethods = params.list("methods").collect() { it }
        } else {
            selectedMethods = [PaymentMethod.CREDIT_CARD.toString(), PaymentMethod.COUPON.toString(), PaymentMethod.GIFT_CARD.toString()]
        }
        selectedMethods
    }

    private def paramSelectedCourts(def courts, def courtIds) {
        def selectedCourtIds
        if (courtIds) {
            selectedCourtIds = courtIds
        } else {
            selectedCourtIds = courts.collect() { it.id }
        }
        selectedCourtIds
    }

    private DateTime dateParam(parameter, defaultValue) {
        if (parameter != null) {
            new DateMidnight(parameter).toDateTime()
        } else {
            defaultValue.toDateTime()
        }
    }

    class PaymentData {
        DateTime start
        DateTime end
        List<Court> courts
        List<Long> selectedCourtIds
        List<String> selectedMethods
        List selectedIncomeTypes
        LinkedHashMap<String, Object> result
        List transactions
    }
}

@Validateable(nullable = true)
class FilterStatsCommand {
    String start
    String end

    String startTime
    String endTime

    List<Long> courtIds
    List<Integer> weekdays
}

