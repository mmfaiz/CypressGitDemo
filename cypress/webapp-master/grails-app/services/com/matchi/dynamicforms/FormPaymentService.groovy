package com.matchi.dynamicforms

import com.matchi.User
import com.matchi.orders.Order
import com.matchi.price.Price

/**
 * @author Sergei Shushkevich
 */
class FormPaymentService {
    static transactional = true

    Order createFormPaymentOrder(User user, Form form) {
        def order = new Order()
        order.article = Order.Article.FORM_SUBMISSION
        order.description = form.createOrderDescription()
        order.metadata = [formId: form.id.toString()]
        order.user = user
        order.issuer = user
        order.facility = form.facility
        order.dateDelivery = new Date()
        order.origin = Order.ORIGIN_WEB

        order.price = form.price
        order.vat = Price.calculateVATAmount(form.price.toLong(), form.facility?.vat ?: 0)

        order.save(failOnError: true)
    }
}
