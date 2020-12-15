package com.matchi.api

import com.matchi.enums.BookingGroupType
import com.matchi.orders.CashOrderPayment
import com.matchi.orders.Order
import com.matchi.orders.OrderPayment
import grails.converters.JSON
import grails.rest.RestfulController
import grails.validation.Validateable
import groovy.transform.ToString
import org.apache.http.HttpStatus

class OrderResourceController extends RestfulController  {

    static namespace = "v1"
    static responseFormats = ['json']

    def orderService
    def paymentService

    def index() {
        ListOrderCommand cmd = new ListOrderCommand()
        cmd.q    = params.q
        cmd.date = params.date

        if(params.email) {
            cmd.email = params.email
        }

        Boolean paid = params.boolean("paid")
        if(paid != null) {
            cmd.paid = paid
        }

        if (!cmd.validate()) {
            response.status = HttpStatus.SC_BAD_REQUEST
            render cmd.errors as JSON
            return
        }

        List<Order> orders = orderService.getOrders(cmd, request.facilityId)

        respond orders
    }

    def addOrderPayment() {

        params?.orders?.each { op ->
            OrderPaymentCommand opc = new OrderPaymentCommand()
            opc.id          = op.id
            opc.amount      = op.amount
            opc.referenceId = op.referenceId

            if (!opc.validate()) {
                response.status = HttpStatus.SC_BAD_REQUEST
                render opc.errors as JSON
                return
            }

            Order order = Order.get(opc.id)

            if (!order) {
                response.status = HttpStatus.SC_NOT_FOUND
                return
            }

            CashOrderPayment orderPayment = CashOrderPayment.create(order, opc.amount, opc.referenceId)
            orderPayment.status = OrderPayment.Status.CAPTURED
            orderPayment.save()

            if (order.isFinalPaid()) {
                order.status = Order.Status.COMPLETED
            } else {
                order.status = Order.Status.CONFIRMED
            }

            order.save()
        }

        render (status: HttpStatus.SC_OK) as JSON
    }
}

@ToString
@Validateable(nullable = true)
class ListOrderCommand {

    String q
    String date
    String email
    Boolean paid

    BookingGroupType type = BookingGroupType.DEFAULT // e.g SUBSCRIPTION, TRAINING, COMPETITION, ACTIVITY

    static constraints = {
        q(nullable: true)
        date(nullable: true)
        type(nullable: false)
        email(nullable: true)
        paid(nullable: true)
    }
}

@Validateable(nullable = true)
class RegisterOrdersPaymentCommand {
    List<OrderPaymentCommand> orders
}

@ToString
@Validateable(nullable = true)
class OrderPaymentCommand {
    Long id
    Long amount

    String referenceId

    static constraints = {
        id(nullable: false)
        amount(nullable: false)
        referenceId(nullable: false)
    }
}
