package com.matchi.marshallers

import com.matchi.Payment
import grails.converters.JSON

import javax.annotation.PostConstruct

class PaymentMarshaller {
    @PostConstruct
    void register() {
        // Standard Node marshall
        JSON.registerObjectMarshaller(Payment) { Payment payment ->
            marshallPayment(payment)
        }
    }

    def marshallPayment(Payment payment) {
        [
                id: payment.id,
                created: payment.dateCreated,
                confirmed: payment.dateConfirmed,
                annulled: payment.dateAnnulled,
                reversed: payment.dateReversed,
                status: payment.status.toString(),
                method: payment.method.toString(),
                amount: payment.amount,
                vat: payment.vat
        ]
    }
}
