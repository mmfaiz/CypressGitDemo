package com.matchi

import com.matchi.enums.BookingGroupType
import com.matchi.orders.Order
import com.matchi.payment.PaymentStatus
import com.matchi.schedule.DateSchedule
import com.matchi.schedule.Schedule
import com.matchi.slots.SlotFilter
import org.joda.time.DateTime
import org.joda.time.Interval
import org.joda.time.LocalDate
/**
 * Manages booking slots and facility schedules
 */
class ScheduleService {

    def dateUtil
    def slotService
    def userService
    def bookingService
    def groovySql
    def customerService

    static transactional = false

    def facilitySchedule(def facility, SlotFilter cmd) {
        //log.debug("Fetching schedule for ${facility.name} from: ${cmd.from} to: ${cmd.to}")
        def user = userService.getLoggedInUser()

        def customer = user ? Customer.findByFacilityAndUser(facility, user) : null
        def slotItems = getScheduleItems(cmd, customer)

        def schedule = new Schedule(cmd.from, cmd.to, facility, slotItems, user)
        return schedule
    }

    def getScheduleItems(def filter, def customer) {
        if (filter.courts.size() == 0) {
            return []
        }

        String sqlquery = """
            select
                s.id as slotId,
                start_time as start,
                end_time as end,
                hour(start_time) as start_hour_time,
                hour(end_time) as end_hour_time,
                b.id as bookingId,
                b.customer_id as bookingCustomerId,
                b.online as madeOnline,
                b.paid as bookingPaid,
                b.comments as bookingComment,
                b.show_comment as bookingShowComment,
                b.hide_booking_holder as hideBookingHolder,
                bg.id as groupId,
                bg.type as bookingType,
                subs.id as subscriptionId,
                subs.customer_id as subscriptionCustomerId,
                sc.id as subscriptionCustomerId,
                sc.number as subscriptionCustomerNumber,
                sc.firstname as subscriptionCustomerFirstname,
                sc.lastname as subscriptionCustomerLastname,
                sc.companyname as subscriptionCustomerCompanyname,
                sc.type as subscriptionCustomerType,
                c.id as customerId,
                c.firstname as customerFirstname,
                c.lastname as customerLastname,
                c.companyname as customerCompanyname,
                c.type as customerType,
                co.id as courtId,
                co.name as courtName,
                p.id as paymentId,
                p.status as paymentStatus,
                f.name,
                cpn.unlimited as paymentSeasonCard,
                ocpn.unlimited as orderSeasonCard,
                o.id as orderId,
                o.status as orderStatus,                
                o.origin as orderOrigin,
                o.price as orderPrice,
                op.amount as orderPaid,
                op.credited as orderCredited,
                s.booking_restriction_id as restricted
            from slot s
                left join court co on s.court_id = co.id
                left join subscription subs on s.subscription_id = subs.id
                left join facility f on co.facility_id = f.id
                left join booking b on b.slot_id = s.id
                left join booking_group bg on b.group_id = bg.id
                left join customer c on b.customer_id = c.id
                left join customer sc on subs.customer_id = sc.id
                left join payment p on b.payment_id = p.id
                left join customer_coupon cc on p.coupon_id = cc.id
                left join coupon cpn on cc.coupon_id = cpn.id
                left join `order` o on b.order_id = o.id
                left join order_payment op on op.id = (select min(oop.payment_id) from order_order_payments oop where oop.order_id = o.id)
                left join customer_coupon_ticket occt on op.ticket_id = occt.id
                left join customer_coupon occ on occt.customer_coupon_id = occ.id
                left join coupon ocpn on occ.coupon_id = ocpn.id
            where
                s.start_time between ? and ? and s.court_id in (${filter.courts.collect { it.id }.join(",")})
                order by s.start_time
                ;"""

        def parameters = [new DateTime(filter.from).toString("yyyy-MM-dd HH:mm"), new DateTime(filter.to).toString("yyyy-MM-dd HH:mm")]
        def result = []
        def rows = groovySql.rows(sqlquery, parameters)

        rows.each {
            result << createScheduleItem(it, customer)
        }

        addPaymentStatus(result)
        addColor(result)

        groovySql.close()
        return result
    }

    private def createScheduleItem(def row, def customer = null) {

        def start = new DateTime(row.start)
        def end = new DateTime(row.end)

        def item = [
                id                    : row.slotId,
                start                 : start,
                end                   : end,
                interval              : new Interval(start, end),
                subscriptionId        : row.subscriptionId,
                subscriptionCustomerId: row.subscriptionCustomerId,
                color                 : "",
                seasonCard            : row.paymentSeasonCard || row.orderSeasonCard,
                restricted            : row.restricted,

                court                 : [
                        id  : row.courtId,
                        name: row.courtName
                ]
        ]

        if (row.bookingId) {
            item.booking = [
                    id               : row.bookingId,
                    online           : row.madeOnline && row.orderOrigin != Order.ORIGIN_FACILITY,
                    groupId          : row.groupId,
                    type             : row.bookingType,
                    paid             : (row.orderStatus == "COMPLETED" || row.paymentStatus == "OK" || row.bookingPaid),
                    comment          : row.bookingComment,
                    showComment      : row.bookingShowComment,
                    hideBookingHolder: row.hideBookingHolder,
                    owned            : (customer && customer.id.equals(row.bookingCustomerId)),
                    //players: row.bookingPlayers,

                    payment          : [
                            id    : row.paymentId,
                            status: row.paymentStatus
                    ],

                    order            : [
                            id    : row.orderId,
                            status: row.orderStatus,
                            price : row.orderPrice
                    ],
                    orderPayment     : [
                            paid:     row.orderPaid,
                            credited: row.orderCredited
                    ],
                    customer         : [
                            id       : row.customerId,
                            firstname: row.customerFirstname,
                            lastname : row.customerLastname,
                            type     : row.customerType,
                            fullName : { ->
                                Customer.CustomerType.ORGANIZATION.toString().equals(row.customerType) ?
                                        "${row.customerCompanyname}" :
                                        "${row.customerFirstname} ${row.customerLastname}"
                            }
                    ]
            ]
        }

        if (row.subscriptionId) {
            item.subscription = [
                    id      : row.subscriptionId,
                    customer: [
                            id       : row.subscriptionCustomerId,
                            number   : row.subscriptionCustomerNumber,
                            firstname: row.subscriptionCustomerFirstname,
                            lastname : row.subscriptionCustomerLastname,
                            type     : row.subscriptionCustomerType,
                            fullName : { ->
                                Customer.CustomerType.ORGANIZATION.toString().equals(row.subscriptionCustomerType) ?
                                        "${row.subscriptionCustomerCompanyname}" :
                                        "${row.subscriptionCustomerFirstname} ${row.subscriptionCustomerLastname}"
                            }
                    ]

            ]
        }

        return item
    }

    private void addPaymentStatus(List items) {
        def orderIds = []
        items.each {
            if (it.booking?.order) {
                orderIds << it.booking.order.id
            }
        }

        if (orderIds) {
            def orders = [:]
            Order.createCriteria().listDistinct {
                inList("id", orderIds)
                // to avoid extra queries:
                join "booking"
                join "metadata"
                join "payments"
                join "refunds"
            }.each {
                orders[it.id] = it
            }

            items.each {
                if (it.booking) {
                    it.booking.paidStatus = getPaymentStatus(it.booking, orders)
                }
            }
        }
    }

    private PaymentStatus getPaymentStatus(Map booking, Map orders) {
        def order   = booking.order
        def payment = booking.payment

        if (order) {
            def o = orders[order.id]
            if (o?.isPartlyPaid()) {
                return PaymentStatus.PARTLY
            } else if (o?.isFinalPaid()) {
                return PaymentStatus.OK
            } else {
                return PaymentStatus.PENDING
            }
        }

        return payment?.status ? PaymentStatus.valueOf(payment?.status) : null
    }

    private void addColor(List items) {
        items.each { item ->
            def isSubscriptionLeased = (item.subscriptionId && item.booking &&
                (item.subscriptionCustomerId != item.booking.customer.id || item.booking.type != BookingGroupType.SUBSCRIPTION.name()))

            def type = item?.booking?.type ? BookingGroupType.valueOf(item.booking.type) : null

            item.color = ColorFetcher.slotColor(item?.booking, type, item?.booking?.paidStatus,
                    item?.subscriptionId, isSubscriptionLeased, item?.booking?.paid)
        }
    }
}
