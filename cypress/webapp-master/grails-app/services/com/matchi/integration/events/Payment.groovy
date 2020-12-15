package com.matchi.integration.events

class Payment {
    final String id
    final String articleType
    final String method
    final String status
    final Date dateCreated
    final Date lastUpdated
    final String orderNumber
    final String orderDescription

    Payment(com.matchi.Payment payment) {
        this.id = payment.id
        this.articleType = payment.articleType.toString()
        this.method = payment.method.toString()
        this.status = payment.status.toString()
        this.dateCreated = payment.dateCreated
        this.lastUpdated = payment.lastUpdated
        this.orderNumber = payment.orderNumber
        this.orderDescription = payment.orderDescription
    }
}
