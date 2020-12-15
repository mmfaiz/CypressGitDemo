package com.matchi.payment

import com.matchi.Facility
import com.matchi.FormController
import com.matchi.LogHelper
import com.matchi.User
import com.matchi.dynamicforms.Form
import com.matchi.dynamicforms.Submission
import com.matchi.orders.Order
import grails.validation.Validateable
import org.apache.http.HttpStatus

/**
 * @author Sergei Shushkevich
 */
class FormPaymentController extends GenericPaymentController {

    def courseParticipantService
    def formPaymentService
    def notificationService
    def userService
    def securityService

    def confirm(Long id) {
        User user = userService.loggedInUser

        if (!user) {
            render status: HttpStatus.SC_UNAUTHORIZED
            return
        }

        Form form = Form.get(id)

        if (securityService.hasFacilityAccessTo(form.facility)) {
            redirect(action: "processWithoutPayment")
            return
        }

        Facility facility = form.facility

        Order order = formPaymentService.createFormPaymentOrder(user, form)
        order.metadata << [cancelUrl : createLink(action: "confirm", params: params, absolute: true),
                           processUrl: createLink(action: "process", absolute: true), returnUrl: params.returnUrl]

        // Redirecting to facility page
        order.save()

        String finishUrl = createLink(controller: "facility", action: "show", params: [name: facility.shortname, orderId: order.id])
        startPaymentFlow(order.id, finishUrl)

        Map paymentMethodsModel = getPaymentMethodsModel(user, facility, order.price)

        render(template: "confirm", model: [order: order, form: form, orderId: order.id, facility: facility, paymentMethodsModel: paymentMethodsModel])
    }

    def pay(FormPaymentCommand cmd) {
        def user = userService.loggedInUser
        def order = Order.get(cmd.orderId)

        if (!cmd.validate()) {
            render(view: "confirm", model: [order      : order, form: Form.get(cmd.id),
                                            paymentInfo: getPaymentInfo(user), command: cmd])
        } else {
            def method = cmd.method as PaymentMethod

            if (method.isUsingPaymentGatewayMethod()) {
                redirect(getPaymentProviderParameters(method, order, user))
            }
        }
    }

    @Override
    protected void processArticle(Order order) throws ArticleCreationException {
        Submission submission = session[FormController.SUBMISSION_SESSION_KEY] as Submission

        if (!submission) {
            throw new ArticleCreationException(message(code: 'formPaymentController.process.errors.noSubmission') as String)
        }

        if (!(submission.form.activity && submission.customer)) {
            throw new ArticleCreationException(message(code: 'genericPaymentController.process.errors.couldNotProcess') as String)
        }

        if (!submission.form.submissionAllowed) {
            throw new ArticleCreationException(message(code: "form.maxSubmissions.error",
                    args: [(submission.form.course?.name ?: submission.form.name)]) as String)
        }

        submission.order = order
        submission.save()

        notificationService.sendFormSubmissionReceipt(submission)
        notificationService.sendActivitySubmissionNotification(submission.customer, submission.form.getActivity())
        session.removeAttribute(FormController.SUBMISSION_SESSION_KEY)
        log.info(LogHelper.formatOrder("before `redirect to finish` from FormPaymentController", params.orderId as Long))
        redirectToFinish(PaymentFlow.State.RECEIPT, params.long("orderId"), [orderId: params.orderId])
    }

    @Override
    protected Order.Article getArticleType() {
        return Order.Article.FORM_SUBMISSION
    }

    def processWithoutPayment() {
        def submission = session[FormController.SUBMISSION_SESSION_KEY] as Submission
        if (submission?.save()) {

            notificationService.sendFormSubmissionReceipt(submission)
            session.removeAttribute(FormController.SUBMISSION_SESSION_KEY)

            redirect action: "receipt", params: [submissionId: submission.id, adminEmail: submission.submissionIssuer.email]
        }
    }

    def receipt() {
        if (params.orderId) {
            def order = Order.get(params.orderId)
            PaymentFlow paymentFlow = PaymentFlow.popInstance(session, order.id)

            [order: order, returnUrl: order.metadata?.returnUrl ?: createLink(controller: "home", action: "index"), showPage: paymentFlow.showPage()]
        } else {
            [adminEmail: params.adminEmail, returnUrl: createLink(controller: "facilityForm", action: "showSubmission", id: params.submissionId)]
        }
    }
}

@Validateable(nullable = true)
class FormPaymentCommand {

    def userService
    def springSecurityService

    Long id
    String orderId
    String method
    boolean savePaymentInformation

    static constraints = {
        id(nullable: false)
        orderId(nullable: false)
        method(nullable: false)
    }
}
