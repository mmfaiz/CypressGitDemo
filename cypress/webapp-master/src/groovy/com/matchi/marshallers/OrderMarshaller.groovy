package com.matchi.marshallers

import com.matchi.Booking
import com.matchi.external.ExternalSynchronizationEntity
import com.matchi.orders.Order
import com.matchi.payment.PaymentMethod
import grails.converters.JSON
import org.joda.time.DateTime

import javax.annotation.PostConstruct

class OrderMarshaller {

    @PostConstruct
    void register() {
        // Standard Node marshall
        JSON.registerObjectMarshaller(Order) { Order order ->
            marshallOrder(order)
        }
    }

    def marshallOrder(Order order) {
        Booking booking = Booking.findByOrder(order)


        def result = [
                id               : order.id,
                price            : order.price,
                currency         : order.facility?.currency,
                paidAmount       : order.isPaidByCoupon() ? 0 : order.getTotalAmountPaid(),
                description      : order.description,
                customerName     : order?.customer?.fullName(),
                fortnoxId        : ExternalSynchronizationEntity.findByEntityIdAndEntity(order.customer.id, ExternalSynchronizationEntity.LocalEntity.CUSTOMER)?.externalEntityId,

                created          : order.dateCreated,
                totalAmount      : order.total(),
                vat              : order.vat(),
                status           : "OK",
                method           : order.getFirstPaymentMethod().toString(),
                isPaidByCoupon   : order.isPaidByCoupon(),
                isStillRefundable: order.isStillRefundable(),
        ]

        if (booking) {
            def slot = Booking.findByOrder(order).slot
            def slotCourt = slot.court

            def bookingStart = new DateTime(slot.startTime)
            def bookingEnd = new DateTime(slot.endTime)
            result += [
                    date : bookingStart.toString('yyyy-MM-dd'),
                    time : "${bookingStart.toString("HH:mm")}-${bookingEnd.toString("HH:mm")}",
                    court: slotCourt.name,
            ]
        }

        return result
    }
}
