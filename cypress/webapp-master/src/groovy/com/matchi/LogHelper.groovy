package com.matchi

import com.matchi.orders.Order

// Purpose of this class is to format log messages in a standard way so it is easier to follow when
// troubleshooting and searching in LogEntries. Use case: log.info(LogHelper.format("Message")).
class LogHelper {

    static String formatOrder(String message, Order order) {
        formatOrderInternal(message, order?.id, null)
    }

    static String formatOrder(String message, Order order, Booking booking) {
        formatOrderInternal(message, order?.id, booking?.id)
    }

    static String formatOrder(String message, Long orderId) {
        formatOrderInternal(message, orderId, null)
    }

    private static String formatOrderInternal(String message, Long orderId, Long bookingId) {
        StringBuilder builder = new StringBuilder()
        builder.append(message)

        if(orderId)
            builder.append(" - Order.id = " + orderId)

        if(bookingId)
            builder.append(" - Booking.id = " + bookingId)

        builder.toString()
    }
}
