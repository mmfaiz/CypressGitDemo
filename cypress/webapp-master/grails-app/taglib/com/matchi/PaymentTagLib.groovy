package com.matchi

import com.matchi.coupon.Coupon
import com.matchi.orders.OrderPayment
import org.joda.time.DateTime
import com.matchi.payment.ArticleType
import com.matchi.activities.ActivityOccasion

class PaymentTagLib {

    def springSecurityService
    def paymentService

    def paymentShortSummary = { attrs, body ->

        def payment = attrs.payment
        def order = attrs.order

        if(payment) {
            def created = new DateTime(payment.dateCreated)
            StringBuilder sb = new StringBuilder()
                    .append(created.toString(g.message(code:"date.format.timeShort")))
                    .append(" ")
                    .append(g.message(code: "payment.method.${payment.method.toString()}"))
                    .append(" (${payment.amountFormatted()})")

            out << sb.toString()
        }

        if(order) {

            StringBuilder sb = new StringBuilder()
            def currency = order?.facility?.currency
            def payments = !attrs.completedPaymentsOnly ? order.payments :
                    order.payments.findAll {it.status in [OrderPayment.Status.AUTHED, OrderPayment.Status.CAPTURED]}
            payments.each {
                def created = new DateTime(it.dateCreated)

                if(!"Coupon".equals(it.type)) {
                    sb.append("<span class=\"text-muted\">")
                    .append(g.message(code:"adminStatistics.amount.label"))
                    .append(": </span>")
                    .append("${it.total()} ${currency} (")
                    .append(g.message(code:"default.varav.label"))
                    .append(" ${it.vat} ${currency} ")
                    .append(g.message(code:"default.vat.label"))
                    .append(")")
                }

                sb.append("<br>")

                sb.append("<span class=\"text-muted\">")
                .append(g.message(code:"default.payment.method.label"))
                .append(": </span>")
                if (it.method) {
                    sb.append(g.message(code: "payment.method.${it.method}"))
                } else {
                    sb.append(g.message(code:"payment.type.${it.type}"))
                }

                sb.append("<br>")

                sb.append("<span class=\"text-muted\">")
                .append(g.message(code:"templates.customer.customerInvoicesPopup.message2"))
                .append(": </span>")
                .append(created.toString(g.message(code:"date.format.timeShort")))
            }

            out << sb.toString()
        }

    }

    def paymentSummary = { attrs, body ->

        def payment = attrs.payment

        if(payment) {
            def created = new DateTime(payment.dateCreated)
            StringBuilder sb = new StringBuilder()
                .append(g.message(code: "payment.method.${payment.method.toString()}"))
                .append(", ${payment.amountFormatted()}")

            out << sb.toString()
        }

    }

    def paymentArticleSummary = { attrs, body ->
        def payment = attrs.payment
        def booking = Booking.findByPayment(payment)
        if(payment) {
            if(payment.articleType.equals(ArticleType.ACTIVITY)) {
                if(payment.orderParameters?.occasionId) {
                    ActivityOccasion occasion = ActivityOccasion.get(payment.orderParameters.occasionId)
                    if(occasion) {
                        out << g.occasionTime([occasion: occasion])
                    } else {
                        out << "Unknown occasion"
                    }
                } else {
                    out << "Unknown occasion"
                }
            } else if (payment.articleType.equals(ArticleType.BOOKING)) {
                if(payment.orderParameters?.slotId) {
                    out << g.humanDateFormat(date: new DateTime(payment.dateDelivery))
                    if (!booking) {
                        out << "<small>Avbokad</small>"
                    }
                } else {
                    out << "Unknown slot"
                }
            } else if (payment.articleType.equals(ArticleType.COUPON)) {
                out << Coupon.get(payment.orderParameters.couponId)?.name
            } else {
                out << "Unknown article type"
            }
        }
    }
}
