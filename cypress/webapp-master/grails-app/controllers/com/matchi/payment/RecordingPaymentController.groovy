package com.matchi.payment

import com.matchi.Booking
import com.matchi.Facility
import com.matchi.LogHelper
import com.matchi.OrderStatusService
import com.matchi.RecordingPurchase
import com.matchi.User

import com.matchi.play.Recording

import com.matchi.orders.Order
import grails.validation.Validateable
import groovy.transform.ToString

class RecordingPaymentController extends GenericPaymentController {
    def userService
    def playService
    def recordingPaymentService
    def scheduledTaskService
    def notificationService
    OrderStatusService orderStatusService
    def facilityService

    def confirm() {
        User user = userService.loggedInUser
        Booking booking = Booking.get(params.bookingId)
        Recording recording = playService.getRecordingFromBooking(booking)
        Facility facility = facilityService.getGlobalFacility()

        if (!playService.userCanAccessRecording(user, recording)) {
            String authError = message(code: 'recordingPaymentController.process.errors.notAPlayer') as String
            log.error(authError)
            throw new ArticleCreationException(authError)
        }

        Order order = recordingPaymentService.createRecordingPaymentOrder(user, recording)

        order.metadata << [cancelUrl : createLink(action: "confirm", params: params, absolute: true),
                           processUrl: createLink(action: "process", absolute: true)]

        order.save()

        String finishUrl = createLink(controller: "userProfile", action: "pastBookings")
        startPaymentFlow(order.id, finishUrl)

        getConfirmModel(user, recording, order)
    }

    def getConfirmModel(User user, Recording recording, Order order) {
        Map model = [recording: recording, facility: order.facility, order: order]
        model.paymentMethodsModel = getPaymentMethodsModel(user, order.facility, recording.price)
        return model
    }

    def pay(RecordingPaymentCommand cmd) {
        User user = userService.loggedInUser
        Recording recording = playService.getRecordingFromBooking(Booking.get(cmd.bookingId))
        Booking booking = Booking.get(cmd.bookingId)
        Order order = Order.get(cmd.orderId)

        Facility facility = order.facility

        PaymentMethod method = cmd.method as PaymentMethod

        if (order.total() == 0) {
            log.info("Order is free, skipping to process for order ${order}")
            orderStatusService.complete(order, user)
            redirect(action: "process", params: [orderId: order.id])
            return
        }

        if (!cmd.validate()) {
            def model = getConfirmModel(user, order.facility, recording)
            model.paymentInfo = getPaymentInfo(user)
            model.command = cmd
            render(view: "confirm", model: model)
        } else {

            if (method.isUsingPaymentGatewayMethod()) {
                redirect(getPaymentProviderParameters(method, order, user))
            }
        }

    }

    @Override
    protected void processArticle(Order order) throws ArticleCreationException {
        Recording recording = playService.getRecordingFromBooking(Booking.get(order.metadata['recording.bookingId']))

        RecordingPurchase recordingPurchase = recordingPaymentService.getRecordingPurchaseByOrder(recording, order)

        log.info(LogHelper.formatOrder("Process article", order))

        if (recordingPurchase != null) {
            String alreadyProcessedError = message(code: 'recordingPaymentController.process.errors.alreadyProcessed') as String
            log.error(LogHelper.formatOrder(alreadyProcessedError, order))
            throw new IllegalStateException(alreadyProcessedError)
        }

        if (!order?.issuer?.id?.equals(getCurrentUser()?.id)) {
            String authError = message(code: 'recordingPaymentController.process.errors.authError') as String
            log.error(LogHelper.formatOrder(authError, order))
            throw new ArticleCreationException(authError)
        }

        log.info(LogHelper.formatOrder("Process recording purchase for order", order))
        try {
            recordingPurchase = recordingPaymentService.createRecordingPurchase(recording, order)
            log.info(LogHelper.formatOrder("Process recording purchase for order successfully", order))
        } catch (Throwable t) {
            String articleError = message(code: 'recordingPaymentController.process.errors.couldNotProcess') as String
            log.error(LogHelper.formatOrder("Error while processing recording purchase for order", order), t)
            throw new ArticleCreationException(articleError)
        }

        User user = getCurrentUser()

        scheduledTaskService.scheduleTask(message(code: 'scheduledTask.sendEmailConfirmation.taskName') as String, user.id, order.customer.facility) { taskId ->
            notificationService.sendNewRecordingPurchaseNotification(recording, recordingPurchase)
        }

        log.info(LogHelper.formatOrder("Showing receipt", order, recording.booking))
        redirectToFinish(PaymentFlow.State.RECEIPT, order.id, [orderId: order.id])
    }

    @Override
    protected Order.Article getArticleType() {
        return Order.Article.RECORDING
    }

    def receipt() {
        def order = Order.get(params.orderId)
        RecordingPurchase recordingPurchase = RecordingPurchase.findByOrder(order)
        PaymentFlow paymentFlow = PaymentFlow.popInstance(session, order.id)

        render view: "receipt", model: [recordingPurchase: recordingPurchase, order: order, showPage: paymentFlow.showPage()]
    }
}

@ToString(includeNames = true)
@Validateable(nullable = true)
class RecordingPaymentCommand {
    Long orderId
    Long bookingId
    String method
    Long customerRecordingId

    static constraints = {
        orderId(nullable: false)
        bookingId(nullable: false)
        method(nullable: false)
        customerRecordingId(nullable: true)
    }
}

