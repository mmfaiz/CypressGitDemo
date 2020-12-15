package com.matchi.protocol

import grails.validation.Validateable

class ApiPaymentController extends AbstractApiController {

    def apiPaymentService

    def payment(ApiPaymentCommand cmd) {
        log.debug("Request to API payment using method: " + request.method)

        ApiRequestedPayment apiPayment = null

        ApiPaymentRequest apiPaymentRequest = createApiPaymentRequest(cmd)
        try {
            log.debug("" + apiPaymentRequest.toString())

            apiPayment = apiPaymentService.processApiPaymentRequest(apiPaymentRequest)
            apiPayment.success = true

        } catch(ApiPaymentRequestException e) {
            log.error(e.errorCode)

            apiPayment = new ApiRequestedPayment()
            apiPayment.addError(e.errorCode, "Error in boxnet payment request")
            apiPayment.success = false
        }

        renderResponse(apiPayment)
    }

    private ApiPaymentRequest createApiPaymentRequest(ApiPaymentCommand cmd) {
        ApiPaymentRequest apiPaymentRequest = new ApiPaymentRequest()

        try {
            apiPaymentRequest.orderIds = cmd.paymentId
            apiPaymentRequest.prices = cmd.price
            apiPaymentRequest.errorMessage = cmd.errorMessage
            apiPaymentRequest.paymentMethod = cmd.paymentMethod
            apiPaymentRequest.hash = cmd.hash
            apiPaymentRequest.confirmed = cmd.confirmed
            apiPaymentRequest.cashRegisterTransactionId = cmd.cashRegisterTransactionId

        } catch(Exception e) {
            log.debug("Error in retrieving data from boxnet request: " + e)
        }

        return apiPaymentRequest
    }
    
    protected def renderResponse(ApiRequestedPayment apiPayment) {
        render(contentType: "text/json") {
            result("success": (apiPayment.success?"true":"false"))
        }
    }

    @Override
    protected AbstractRequest.RequestType getRequestType() {
        return AbstractRequest.RequestType.PAYMENT
    }
}

@Validateable(nullable = true)
class ApiPaymentCommand {
    String paymentId
    String price
    String errorMessage
    String paymentMethod
    String hash
    String cashRegisterTransactionId

    Boolean confirmed

    static constraints = {

    }
}
