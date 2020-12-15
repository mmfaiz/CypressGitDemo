package com.matchi

import com.matchi.play.Recording
import com.matchi.orders.Order

class RecordingPurchase implements Serializable, IArticleItem {
    Booking booking
    Customer customer
    Order order
    Date dateCreated
    Date lastUpdated

    String archiveUrl

    static constraints = {
        archiveUrl nullable: true
    }

    @Override
    void replaceOrderAndSave(Order order) {
        this.order = order
        this.save(flush: true, failOnError: true)
    }

    static mapping = {
        id composite: ['booking', 'customer']
        order cascade: "save-update"
    }

    boolean isFinalPaid() {
        return order.isFinalPaid()
    }
}
