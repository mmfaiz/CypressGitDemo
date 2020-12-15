package com.matchi

import com.matchi.play.Recording
import com.matchi.orders.Order
import com.matchi.price.Price
import grails.transaction.Transactional

class RecordingPaymentService {
    def customerService
    def facilityService

    static transactional = false

    RecordingPurchase getRecordingPurchaseByOrder(Recording recording, Order order) {
        RecordingPurchase.findByBookingAndCustomer(recording.booking, order.customer)
    }

    RecordingPurchase getRecordingPurchaseByUser(Recording recording, User user) {
        Facility facility = facilityService.getGlobalFacility()
        Customer customer = customerService.findUserCustomer(user,facility)
        getRecordingPurchaseByCustomer(recording, customer as Customer)
    }

    RecordingPurchase getRecordingPurchaseByCustomer(Recording recording, Customer customer) {
        RecordingPurchase.findByBookingAndCustomer(recording.booking, customer)
    }

    RecordingPurchase createRecordingPurchase(Recording recording, Order order) {
        RecordingPurchase recordingPurchase = new RecordingPurchase()

        recordingPurchase.booking = recording.booking
        recordingPurchase.customer = order.customer
        recordingPurchase.order = order

        recordingPurchase.save(failOnError: true)

        return recordingPurchase
    }

    /**
     * Creates a payment order for a recording
     * @param user the user buying the recording
     * @param recording the recording beeing purchased
     * @return
     */
    @Transactional
    Order createRecordingPaymentOrder(User user, Recording recording) {
        Facility facility = facilityService.getGlobalFacility()
        Customer customer = Customer.findByUserAndFacility(user, facility)

        Order order        = new Order()
        order.article      = Order.Article.RECORDING
        order.description  = recording.getDescription()
        order.metadata     = ["recording.bookingId" : recording.bookingId.toString(), "recording.archiveUrl" : recording.getArchiveUrl()]
        order.customer     = customer
        order.user         = user
        order.issuer       = user
        order.facility     = facility
        order.dateDelivery = new Date()
        Amount amount      = getAmount(recording)
        order.origin       = Order.ORIGIN_WEB

        order.price        = amount.amount
        order.vat          = amount.VAT

        order.save(failOnError: true)

        return order
    }

    Amount getAmount(Recording recording) {
        Facility facility = facilityService.getGlobalFacility()
        Long priceInclVAT = recording.getPrice()
        double vatAmount = Price.calculateVATAmount(priceInclVAT, new Double((facility.vat ?: 0)))
        return new Amount(
                amount: priceInclVAT.toBigDecimal(),
                VAT: vatAmount.toBigDecimal())
    }
}
