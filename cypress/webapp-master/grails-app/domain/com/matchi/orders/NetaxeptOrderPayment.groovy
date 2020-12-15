package com.matchi.orders

class NetaxeptOrderPayment extends OrderPayment {
    private static final long serialVersionUID = 12L

    def netaxeptService

    String transactionId

    @Override
    void refund(amount) {
        if (status == OrderPayment.Status.CAPTURED
                || netaxeptService.query(transactionId)?.Summary?.AmountCaptured?.text() != "0") {
            netaxeptService.credit(transactionId, amount)
            this.setStatus(OrderPayment.Status.CREDITED)
            this.setCredited(amount)
        }
    }

    def query() {
        return netaxeptService.query(transactionId)
    }

    @Override
    def getType() {
        return "Netaxept"
    }

    @Override
    boolean allowLateRefund() {
        false
    }

    @Override
    boolean isRefundableTypeAndStatus() {
        this.isRefundable()
    }

    static constraints = {
        transactionId nullable: true
    }

    static mapping = {
        discriminator "netaxept"
    }

}