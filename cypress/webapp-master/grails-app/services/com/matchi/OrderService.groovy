package com.matchi

import com.matchi.api.ListOrderCommand
import com.matchi.enums.BookingGroupType
import com.matchi.orders.Order
import grails.transaction.NotTransactional
import grails.transaction.Transactional
import org.hibernate.criterion.CriteriaSpecification
import org.joda.time.DateTime

class OrderService {

    def dateUtil

    Order getOrder(Long orderId) {
        Order.get(orderId)
    }

    def getOrders(ListOrderCommand cmd, def facilityId) {
        def orders = Booking.createCriteria().listDistinct {
            createAlias("order", "o")
            createAlias("o.payments", "op", CriteriaSpecification.LEFT_JOIN)
            createAlias("group", "bg", CriteriaSpecification.LEFT_JOIN)
            createAlias("slot", "s")
            createAlias("customer", "c")

            eq("c.facility.id", facilityId)
            eq("o.origin", Order.ORIGIN_FACILITY)
            gt("o.price", new BigDecimal(0))

            if (cmd.date) {
                ge("s.startTime", dateUtil.beginningOfDay(dateUtil.parseDate(cmd?.date))?.toDate())
                le("s.startTime", dateUtil.endOfDay(dateUtil.parseDate(cmd?.date))?.toDate())
            } else {
                ge("s.startTime", dateUtil.beginningOfDay(new DateTime())?.toDate())
                le("s.startTime", dateUtil.endOfDay(new DateTime())?.toDate())
            }

            if (cmd.type != BookingGroupType.DEFAULT) {
                isNotNull("bg.id")
                eq("bg.type", cmd.type)
            } else {
                or {
                    isNull("bg.id")
                    eq("bg.type", BookingGroupType.DEFAULT)
                }
            }

            // Email filter has precedence over q filter
            if(cmd.email) {
                eq("c.email", cmd.email)
            } else if (cmd.q) {
                or {
                    like("c.email", "%${cmd.q}%")
                    like("c.firstname", "%${cmd.q}%")
                    like("c.lastname", "%${cmd.q}%")
                    like("c.companyname", "%${cmd.q}%")
                    like("c.telephone", "%${cmd.q}%")
                    like("c.cellphone", "%${cmd.q}%")
                    like("c.contact", "%${cmd.q}%")
                    sqlRestriction("number like ?", ["%${cmd.q}%" as String])
                    sqlRestriction("concat(firstname,' ',lastname) like ?", ["%${cmd.q}%" as String])
                }
            }

            order("s.startTime", "c.firstname")
        }*.order

        log.debug("Orders listed: ${orders?.size()}")

        // Only do extra filtering on isFinalPaid if boolean is supplied
        if(cmd.paid != null) {
            return orders.findAll { Order order ->
                return order.isFinalPaid() == cmd.paid
            }
        }

        return orders
    }

    def getByPaymentsAndSlots(List<Slot> slots) {
        def slotPayments = []
        def total = 0L

        slots.each { Slot slot ->
            def order = slot?.booking?.order

            if (order) {
                total = order?.getTotalAmountPaid()
                slotPayments << [ slot:slot, payments: order?.payments ]
            }
        }

        return [total: total, rows: slotPayments]
    }

    @NotTransactional
    Order replaceOrderWithFreshCopy(Order order) {
        IArticleItem articleItem = order.retrieveArticleItem()
        order = order.createCopyForRemotePayment()
        articleItem.replaceOrderAndSave(order)
        return order
    }
}
