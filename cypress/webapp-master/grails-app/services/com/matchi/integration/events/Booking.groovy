package com.matchi.integration.events

class Booking implements Event {
    final Long id
    final Slot slot
    final Customer customer
    final String bookingNumber
    final String comments
    final String telephone
    final Boolean paid
    final Boolean online
    final Payment payment
    final Order order
    final Date dateCreated
    final Date lastUpdated
    final Boolean dateReminded
    final Boolean hideBookingHolder

    Booking(com.matchi.Booking booking) {
        this.id = booking.id
        this.slot = new Slot(booking.slot)
        this.customer = new Customer(booking.customer)
        this.bookingNumber = booking.bookingNumber
        this.comments = booking.comments
        this.telephone = booking.telephone
        this.paid = booking.paid
        this.online = booking.online
        if (booking.payment != null) {
            this.payment = new Payment(booking.payment)
        }
        if (booking.order != null) {
            this.order = new Order(booking.order)
        }
        this.dateCreated = booking.dateCreated
        this.lastUpdated = booking.lastUpdated
        this.dateReminded = booking.dateReminded
        this.hideBookingHolder = booking.hideBookingHolder
    }

    @Override
    String getKey() {
        return id
    }

    @Override
    String getTopic() {
        return "bookingsystem.fct.booking.1"
    }
}

enum BookingEventType implements EventType<Booking> {
    CREATED, UPDATED, MOVED, CANCELLED, DELETED
}