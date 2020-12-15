package com.matchi.admin

import com.matchi.Booking
import com.matchi.BookingException
import com.matchi.OrderStatusService
import com.matchi.UserService
import com.matchi.adyen.AdyenNotification
import com.matchi.orders.AdyenOrderPayment

import com.matchi.orders.Order
import com.matchi.orders.OrderPayment
import grails.validation.Validateable
import org.hibernate.FetchMode
import org.joda.time.DateMidnight
import org.joda.time.DateTime
import com.matchi.DateUtil

class AdminOrderController {

    def groovySql
    def dateUtil
    def bookingService
    OrderStatusService orderStatusService
    UserService userService

    def index(AdminOrderFilterCommand cmd) {
        List<Order> orders = []
        List ordersTotal = []


        if (params.start != null && params.end != null) {
            cmd.start = dateUtil.beginningOfDay(Date.parse("yyyy-MM-dd", params.start))
            cmd.end = dateUtil.endOfDay(Date.parse("yyyy-MM-dd", params.end))
        } else {
            cmd.start = dateUtil.beginningOfDay(dateUtil.toBeginningOfMonth(cmd.start))
            cmd.end = dateUtil.endOfDay(dateUtil.toEndOfMonth(cmd.end))
        }

        if (cmd.submit!=null) {

            if (cmd?.q?.isLong() && cmd?.idSearch) {
                // If searching by id, and we find the order, we immediately return that one
                Long id = Long.parseLong(cmd.q)
                Order order = Order.get(id)
                if (order) {
                    return [orders: [order], cmd: cmd, start: cmd.start, end: cmd.end, totalCount: 1]
                }
            }


            String filterQuery = ""
            String articleQuery = ""
            String noArticleJoinQuery = ""
            String noArticleAndQuery = ""
            String orderStatusQuery = ""
            String orderPaymentStatusQuery = ""
            String originSearchQuery = ""
            String doublePaymentSearchQuery = ""

            if (cmd.q) {

                filterQuery += """ AND ( """
                filterQuery += """ o.description like "%${cmd.q}%"  """
                filterQuery += """ OR f.name like "%${cmd.q}%"  """
                filterQuery += """ OR c.email like "%${cmd.q}%"  """
                filterQuery += """ OR u.email like "%${cmd.q}%"  """
                filterQuery += """ OR c.firstname like "%${cmd.q}%"  """
                filterQuery += """ OR c.lastname like "%${cmd.q}%"  """
                filterQuery += """ OR c.companyname like "%${cmd.q}%"  """
                if(cmd.q.isLong()) {
                    filterQuery += """ OR o.id = "${cmd.q}" """
                    filterQuery += """ OR p.transaction_id = "${cmd.q}" """
                    filterQuery += """ OR c.number = "${cmd.q}"  """
                }
                filterQuery += """ ) """
            }
            if (cmd.article) {
                println(cmd.article.toString())
                articleQuery = """ AND o.article = "${cmd.article.toString()}" """
            }
            if (cmd.orderStatus) {
                orderStatusQuery = """ AND o.status = "${cmd.orderStatus.toString()}" """
            }
            if (cmd.orderPaymentStatus) {
                orderPaymentStatusQuery = """ AND p.status = "${cmd.orderPaymentStatus.toString()}" """
            }
            if (!cmd?.originSearch) {
                originSearchQuery = """ AND (o.origin != "${Order.ORIGIN_FACILITY.toString()}" OR (o.origin = "${
                    Order.ORIGIN_FACILITY.toString()
                }" AND p.type = 'adyen')) """
            }
            if (cmd.noArticle) {
                noArticleJoinQuery = """ LEFT JOIN booking b ON o.id = b.order_id
                    LEFT JOIN participation part ON o.id = part.order_id
                    LEFT JOIN membership m ON o.id = m.order_id
                    LEFT JOIN customer_coupon cp ON o.id = cp.order_id
                    LEFT JOIN submission s ON o.id = s.order_id """
                noArticleAndQuery = """ AND b.order_id IS NULL AND part.order_id IS NULL AND m.order_id IS NULL AND cp.order_id IS NULL AND s.order_id IS NULL  """
            }

            if (cmd.doublePayment) {
                doublePaymentSearchQuery = """ AND p.status in ('CAPTURED', 'CREDITED') GROUP BY oop.order_id HAVING COUNT(*) > 1 """
            }

            String query = """SELECT SQL_CALC_FOUND_ROWS o.id
            FROM `order` as o
            LEFT JOIN order_order_payments oop on o.id = oop.order_id
            LEFT JOIN order_payment p on p.id = oop.payment_id
            LEFT JOIN facility f on o.facility_id = f.id
            LEFT JOIN customer c on o.customer_id = c.id
            LEFT JOIN user u on u.id = o.issuer_id
            ${noArticleJoinQuery}
            WHERE
            o.date_created >= "${cmd.start.toString(DateUtil.DEFAULT_DATE_AND_TIME_FORMAT)}"
            AND o.date_created <= "${cmd.end.toString(DateUtil.DEFAULT_DATE_AND_TIME_FORMAT)}"
            AND o.status != "${Order.Status.NEW.toString()}"
            AND o.article != "${Order.Article.SUBSCRIPTION.toString()}"
            AND o.article != "${Order.Article.SUBSCRIPTION_BOOKING.toString()}"
            ${filterQuery}
            ${noArticleAndQuery}
            ${articleQuery}
            ${orderStatusQuery}
            ${orderPaymentStatusQuery}
            ${originSearchQuery}
            ${doublePaymentSearchQuery}
            ORDER BY o.id desc LIMIT ${cmd.offset}, ${cmd.max};

        """

            String queryTotal = """SELECT FOUND_ROWS();"""
            List ordersRows = groovySql.rows(query)

            ordersTotal = groovySql.rows(queryTotal)
            orders = Order.findAllByIdInList(ordersRows.collect { it.id }, [sort: "id", order: "desc"])
        }
        groovySql.close()

        [ orders: orders, cmd: cmd, start: cmd.start, end: cmd.end , totalCount: ordersTotal["FOUND_ROWS()"][0].toString() ]
    }

    def errors(AdminPaginationCommand cmd) {
        def payments = AdyenOrderPayment.createCriteria().list(max: cmd.max, offset: cmd.offset) {
            isNotNull("error")

            if (params.q && params.q?.isLong()) {
                eq("id", params.q.toLong())
            }

            fetchMode("error", FetchMode.JOIN)
            fetchMode("orders", FetchMode.JOIN)

            order 'error.id', 'desc'
        }

        [ payments: payments, cmd: cmd ]
    }

    def notifications(AdminPaginationCommand cmd) {
        def notifications = AdyenNotification.createCriteria().list(max: cmd.max, offset: cmd.offset) {
            if (params.q && params.q?.isLong()) {
                eq("id", params.q.toLong())
            }

            order 'id', 'desc'
        }

        [ notifications: notifications, cmd: cmd ]
    }

    def paymentDetail() {
        def payment = OrderPayment.get(params.paymentId)
        if(payment) {
            def query
            if(payment.type.equals("Netaxept")) {
                query = payment.query()
            }
            [payment: payment, query: query]
        }
    }

    def creditAdyenOrder(Long id) {
        def op = AdyenOrderPayment.get(id)

        OrderPayment.withTransaction {
            op.refund(op.amount)
            op.orders.each {
                orderStatusService.annul(it, userService.getCurrentUser())
            }
        }

        redirect(action: "index", params: params)
    }

    private static DateTime dateParam(parameter, defaultValue) {
        if (parameter?.size() > 0) {
            new DateMidnight(parameter).toDateTime()
        } else {
            defaultValue.toDateTime()
        }
    }

    def checkBookingOrder() {

        def orderId = params.id
        Order order = Order.get(orderId)

        renderOrder(order)
        render """
            * Process: <a href='/admin/orders/processBookingOrder/${params.id}'>Go</a>
        """
    }

    def processBookingOrder() {

        def orderId = params.id
        Order order = Order.get(orderId)
        OrderPayment payment = order.payments?.first()

        if(!order.article.equals(Order.Article.BOOKING)) {
            render "not a booking order"
            return
        }

        payment.status = OrderPayment.Status.CAPTURED

        renderOrder(order)

        try {
            bookingService.book(order)
        } catch(BookingException be) {
            render "booking already made"
            return
        }


        renderOrder(order)

    }

    private renderOrder(Order order) {
        def payment = order.payments?.first()

        render """
        * Is booking: ${order.article.equals(Order.Article.BOOKING)?"Yes":"No"} <br>
        * Has booking: ${Booking.findByOrder(order)? "Yes": "No"} <br>
        * MATCHi payment: ${payment?.status} <br><br>
        * Netaxpt: <pre>${payment.query()?.Summary?.AmountCaptured}</pre>
                  <pre>${payment.query()?.Summary?.AmountCredited}</pre> <br><br>

         """
    }
}

@Validateable(nullable = true)
class AdminOrderFilterCommand {
    int max = 50
    int offset = 0

    String q
    String submit
    DateTime start = new DateTime()
    DateTime end = new DateTime()

    Order.Article article
    Order.Status orderStatus
    OrderPayment.Status orderPaymentStatus

    boolean idSearch = false
    boolean originSearch = false
    boolean doublePayment = false
    boolean noArticle = false

    static constraints = {
        max nullable: false
        offset nullable: false

        q nullable: true
        start nullable: true
        end nullable: true

        article nullable: true
        orderStatus nullable: true
        orderPaymentStatus nullable: true

        idSearch nullable: true
        originSearch nullable: true
        doublePayment nullable: true
        noArticle nullable: true
    }
}

@Validateable(nullable = true)
class AdminPaginationCommand {
    int max = 50
    int offset = 0

    static constraints = {
        max nullable: false
        offset nullable: false
    }
}