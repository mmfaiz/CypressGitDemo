package com.matchi.integration.events

class Order {
    final String id
    final String status
    final BigDecimal price
    final BigDecimal vat
    final String description

    Order(com.matchi.orders.Order order) {
        this.id = order.id
        this.status = order.status.toString()
        this.price = order.price
        this.vat = order.vat
        this.description = order.description
    }
}
