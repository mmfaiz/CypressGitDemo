package com.matchi.events

import com.matchi.orders.Order

class OrderEventRecord extends EventRecord {

   OrderEventRecord(EventType eventType, EventInitiator eventInitiator, Order order) {
      super(eventType, eventInitiator)
      this.order = order
   }

   Order order

   @Override
   def getData() {
      [
           id: order.id,
           status: order.status?.name(),
           article: order.article?.name(),
           origin: order.origin,
           bookingId: order.bookingId,
           facilityId: order.facilityId,
           customerId: order.customerId,
           user: order.userId,
           issuer: order.issuerId,
           description: order.description,
           price: order.price,
           vat: order.vat,
           dateDelivery: order.dateDelivery,
           dateCreated: order.dateCreated,
           lastUpdated: order.lastUpdated,
           metadata: order.metadata
      ]
   }
}


