package com.matchi.protocol
/* Created: 2012-11-27 Mattias (mattias@tdag.se) */
class ApiPaymentRequest {

    String orderIds
    String prices
    String errorMessage
    String paymentMethod
    String hash
    Boolean confirmed
    String cashRegisterTransactionId

    public String toString ( ) {
        return "ApiPaymentRequest [" + orderIds + "] " +
                "[" + prices + "] " +
                "[" + confirmed + "] " +
                "[" + errorMessage + "] " +
                "[" + paymentMethod + "] " +
                "[" + cashRegisterTransactionId + "] " +
                "[" + hash + "]";
    }
}
