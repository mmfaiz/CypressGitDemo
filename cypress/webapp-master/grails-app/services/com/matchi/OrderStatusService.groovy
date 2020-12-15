package com.matchi


import com.matchi.events.EventType
import com.matchi.events.EventInitiator
import com.matchi.integration.IntegrationService
import com.matchi.orders.Order

// Responsible for handling status changes for an order.
class OrderStatusService {

    // Methods refactored from Order.grooyv so to mimic previous behaviour the service class is set to not transactional as it was in Order domain.
    static transactional = false

    IntegrationService integrationService

    void confirm(Order order, EventInitiator initiator, Boolean ignoreAssert = false) {
        if (!ignoreAssert) {
            order.assertCustomer()
        }
        order.status = Order.Status.CONFIRMED
        order.save()

        integrationService.send(EventType.ORDER_CONFIRMED, initiator, order)
    }

    void complete(Order order, EventInitiator initiator, Boolean ignoreAssert = false) {
        if (!ignoreAssert) {
            order.assertCustomer()
        }
        order.status = Order.Status.COMPLETED
        order.save(flush: true)

        integrationService.send(EventType.ORDER_COMPLETED, initiator, order)
    }

    void cancel(Order order, EventInitiator initiator, Boolean ignoreAssert = false) {
        if (!ignoreAssert) {
            order.assertCustomer()
        }
        order.status = Order.Status.CANCELLED
        order.save(flush: true)

        integrationService.send(EventType.ORDER_CANCELLED, initiator, order)
    }

    void annul(Order order, EventInitiator initiator, def refundNote = null, def refundAmount = null) {
        if (refundAmount) {
            order.refund(refundAmount, refundNote)
        }

        Date now = new Date()
        // Only change delivery date on future deliveries when annulling order
        if (order.dateDelivery.after(now)) {
            order.dateDelivery = now
        }

        order.status = Order.Status.ANNULLED
        order.save()

        integrationService.send(EventType.ORDER_ANNULLED, initiator, order)
    }
}
