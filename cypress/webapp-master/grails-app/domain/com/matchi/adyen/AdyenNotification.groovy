package com.matchi.adyen

import com.matchi.LogHelper
import com.matchi.events.EventInitiator
import com.matchi.orders.AdyenOrderPayment
import com.matchi.orders.Order
import com.matchi.orders.OrderPayment
import org.joda.time.DateTime

class AdyenNotification implements Serializable {
    private static final long serialVersionUID = 12L

    static enum EventCode {
        AUTHORISATION, CAPTURE, CANCEL_OR_REFUND, CANCELLATION, REFUND, CAPTURE_FAILED, REFUND_FAILED, REFUSED, NOTIFICATION_OF_FRAUD,
        // Not used but should be supported
        REPORT_AVAILABLE, CHARGEBACK_REVERSED, NOTIFICATION_OF_CHARGEBACK, CHARGEBACK, SECOND_CHARGEBACK, PREARBITRATION_LOST, PREARBITRATION_WON
    }

    EventCode eventCode
    String pspReference
    Boolean success
    String reason

    boolean executed = Boolean.FALSE

    Date dateCreated
    Date lastUpdated

    static constraints = {
        eventCode nullable: false
        pspReference nullable: false
        success nullable: false
        reason nullable: true
        executed nullable: false
    }

    static AdyenNotification create(Map request) {
        String pspReference = request.originalReference ?: request.pspReference
        AdyenNotification adyenNotification = findByPspReferenceAndEventCode(pspReference, (request.eventCode as EventCode))

        if (!adyenNotification) {
            adyenNotification = new AdyenNotification()
            adyenNotification.eventCode         = request.eventCode
            adyenNotification.pspReference      = request.originalReference ?: request.pspReference
            adyenNotification.success           = Boolean.parseBoolean(request.success)
            adyenNotification.reason            = request.reason
            adyenNotification.save(flush: true)
        }
    }

    void process(AdyenOrderPayment orderPayment, EventInitiator eventInitiator) {
        log.info("Process Adyen notification: ${this}")

        if (orderPayment && this.success) {
            if (this.eventCode.equals(EventCode.REFUSED)||
                    this.eventCode.equals(EventCode.CAPTURE_FAILED) ||
                    this.eventCode.equals(EventCode.REFUND_FAILED)) {
                orderPayment.addError(this as AdyenNotification, eventInitiator)
            } else {
                updateAdyenOrderPaymentOnEventCode(orderPayment, eventInitiator)
            }
        } else if (orderPayment) {
            orderPayment.addError(this as AdyenNotification, eventInitiator)
        }
    }

    void updateAdyenOrderPaymentOnEventCode(AdyenOrderPayment orderPayment, EventInitiator eventInitiator) {
        switch (this.eventCode) {
            case EventCode.AUTHORISATION:

                // If an OrderPayment is local and PENDING, we have some special logic
                if (orderPayment.method.isLocal() && orderPayment.status.equals(OrderPayment.Status.PENDING)) {
                    orderPayment.handleNotificationForPending(this, eventInitiator)

                // If a payment is in a pre-paid state, we update if they have an article
                } else if (orderPayment.status in [OrderPayment.Status.NEW, OrderPayment.Status.FAILED]) {

                    // If we have an article, we update the order and the payment
                    if(orderPayment.refundOrderIfNoArticleCreated(eventInitiator)) {

                        Order.withTransaction {
                            Order order = orderPayment.orders?.first()
                            if(order) {
                                order.status = Order.Status.CONFIRMED
                                log.info(LogHelper.formatOrder("Updating order with existing article to CONFIRMED/AUTHED", order))
                                order.save()
                            }

                            orderPayment.status = OrderPayment.Status.AUTHED
                            orderPayment.save()
                        }

                    }
                }

                break
            case EventCode.CANCELLATION:
                orderPayment.status = OrderPayment.Status.ANNULLED
                break
            case EventCode.REFUND:
                orderPayment.status = OrderPayment.Status.CREDITED
                break
            default:
                break
        }
    }

    /**
     * Checks if notification is old enough to be processed by AdyenNotificationJob.
     * Used to differ between notifications run by the job and by the pending page.
     * @return
     */
    boolean hasWaitedForThreshold() {
        return new DateTime(this.dateCreated).isBefore(new DateTime().minusMinutes(AdyenService.MIN_NOTIFICATION_THRESHOLD))
    }

    String toString() {
        return "{eventCode: ${eventCode}, pspReference: ${pspReference}, success: ${success}, reason: ${reason}, executed: ${executed}"
    }
}
