package com.matchi.orders

import com.matchi.StringHelper
import com.matchi.User
import com.matchi.payment.PaymentMethod

abstract class OrderPayment implements Serializable {

    public static final Integer ERROR_MESSAGE_MAX_SIZE = 255

    static belongsTo = Order
    static hasMany = [orders: Order]

    User issuer
    Date dateCreated
    Date lastUpdated

    BigDecimal amount = 0
    BigDecimal vat = 0
    BigDecimal credited = 0

    Status status = Status.NEW
    String errorMessage

    PaymentMethod method

    abstract void refund(def amount)

    abstract def getType()

    abstract boolean allowLateRefund()

    abstract boolean isRefundableTypeAndStatus()

    def total() {
        return amount.minus(credited)
    }

    static constraints = {
        issuer nullable: false
        errorMessage nullable: true
        vat scale: 2
    }

    static mapping = {
        autoTimestamp true
        orders joinTable: [name: "order_order_payments", key: 'payment_id']
        discriminator column: "type"
    }

    def beforeInsert() {
        errorMessage = StringHelper.safeSubstring(errorMessage, 0, 255)
    }

    def beforeUpdate() {
        errorMessage = StringHelper.safeSubstring(errorMessage, 0, 255)
    }

    static enum Status {
        /* Transaction has been registered. */
        NEW,

        /* Transaction failed */
        FAILED,

        /* Payment is waiting to be either authorised or captured */
        PENDING,

        /* Payment has been authorised (reserved on clients credit card) */
        AUTHED(true, true),

        /* Payment has been captured (amount withdrawn from client credit card) */
        CAPTURED(true, true),

        /* Payment has been credited */
        CREDITED,

        /* Payment has been annulled */
        ANNULLED

        // A payment is processable if we want to proceed and deliver the article
        boolean isProcessable = false

        // A payment is refundable if it was made through us
        boolean isRefundable = false

        Status(boolean isProcessable = false, boolean isRefundable = false) {
            this.isProcessable = isProcessable
            this.isRefundable = isRefundable
        }

        static list() {
            return [NEW, FAILED, PENDING, AUTHED, CAPTURED, CREDITED, ANNULLED]
        }
    }

    boolean ordersHaveStatus(Order.Status status) {
        return orders ? orders.every { Order order -> order.status == status } : false
    }

    boolean isProcessable() {
        return this.status.isProcessable
    }

    boolean isRefundable() {
        return this.status.isRefundable
    }

    def vat() {
        if (ordersHaveStatus(Order.Status.ANNULLED)) {
            0
        } else {
            vat
        }
    }
}
