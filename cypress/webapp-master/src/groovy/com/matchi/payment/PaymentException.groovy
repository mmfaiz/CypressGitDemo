package com.matchi.payment

import com.matchi.Payment


class PaymentException extends RuntimeException {

    Payment payment

    PaymentException() {
    }

    PaymentException(String s) {
        super(s)
    }

    PaymentException(String s, Throwable throwable) {
        super(s, throwable)
    }

    PaymentException(Throwable throwable) {
        super(throwable)
    }

    PaymentException(Payment payment) {
        this.payment = payment
    }

    PaymentException(String s, Payment payment) {
        super(s)
        this.payment = payment
    }

    PaymentException(String s, Throwable throwable, Payment payment) {
        super(s, throwable)
        this.payment = payment
    }

    PaymentException(Throwable throwable, Payment payment) {
        super(throwable)
        this.payment = payment
    }
}
