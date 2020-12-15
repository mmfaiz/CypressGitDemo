package com.matchi.orders

import com.matchi.payment.PaymentMethod

class CashOrderPayment extends OrderPayment {
    private static final long serialVersionUID = 12L

    public static final String DISCRIMINATOR = "cash"

    String referenceId // Used for reference against Boxnet
    PaymentMethod method = PaymentMethod.CASH

    @Override
    void refund(amount) {
        this.status = OrderPayment.Status.CREDITED
        this.setCredited(amount)
    }

    @Override
    def getType() {
        return "Cash"
    }

    @Override
    boolean allowLateRefund() {
        true
    }

    @Override
    boolean isRefundableTypeAndStatus() {
        false
    }

    static constraints = {
        referenceId(nullable: true)
    }

    static mapping = {
        discriminator DISCRIMINATOR
    }

    static CashOrderPayment create(Order order, BigDecimal amount = null, String referenceId = null) {
        CashOrderPayment orderPayment = new CashOrderPayment()
        orderPayment.issuer      = order.issuer
        orderPayment.amount      = amount ?: order.total()
        orderPayment.vat         = order.vat()
        orderPayment.referenceId = referenceId

        order.addToPayments(orderPayment.save(failOnError: true))
        order.save(failOnError: true)

        orderPayment.addToOrders(order)
        orderPayment.save()

        orderPayment
    }
}
