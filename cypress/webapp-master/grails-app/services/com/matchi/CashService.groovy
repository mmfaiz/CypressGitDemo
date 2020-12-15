package com.matchi

import com.matchi.orders.CashOrderPayment
import com.matchi.orders.Order
import com.matchi.orders.OrderPayment

class CashService {

    def springSecurityService
    OrderStatusService orderStatusService

    CashOrderPayment createCashOrderPayment(Order order) {
        CashOrderPayment payment = new CashOrderPayment()
        payment.issuer        = getCurrentUser(order)
        payment.amount        = order.total()
        payment.vat           = order.vat()
        payment.status        = OrderPayment.Status.CAPTURED

        payment.save(failOnError: true)

        order.addToPayments(payment)

        log.debug("Added payment: ${payment.id} to order: ${order.id}")

        if(order.isFinalPaid()) {
            orderStatusService.complete(order, getCurrentUser(order))
        } else {
            orderStatusService.confirm(order, getCurrentUser(order))
        }
        order.save(failOnError: true)

        return payment
    }

    User getCurrentUser(Order order) {
        (User)springSecurityService.currentUser ?: order.issuer
    }
}
