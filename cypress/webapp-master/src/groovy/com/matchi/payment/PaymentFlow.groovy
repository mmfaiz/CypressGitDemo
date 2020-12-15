package com.matchi.payment

import com.matchi.LogHelper
import com.matchi.StringHelper
import com.matchi.orders.AdyenOrderPayment
import groovyx.net.http.URIBuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import javax.servlet.http.HttpSession

class PaymentFlow implements Serializable {

    private static final long serialVersionUID = 12L

    String paymentController
    String errorMessage
    String finishUrl

    State state

    Long orderId

    boolean isRedirect = false
    boolean savePaymentInfo = false

    final static String PAYMENT_FLOW = 'PaymentFlow'
    final static String RECEIPT_ACTION = 'receipt'
    final static String ERROR_ACTION = 'error'
    final static String DEFAULT_ERROR_MESSAGE = 'Unknown error occurred'

    PaymentFlow(Long orderId) {
        this.orderId = orderId
        this.state = State.UNFINISHED
    }

    boolean showPage() {
        return (!finishUrl && isRedirect)
    }

    enum State {
        UNFINISHED, RECEIPT, ERROR
    }

    def receipt() {
        state = State.RECEIPT
    }

    def error() {
        this.error(DEFAULT_ERROR_MESSAGE)
    }

    def error(String errorMessage) {
        state = State.ERROR
        if(errorMessage?.isEmpty()) {
            this.errorMessage = DEFAULT_ERROR_MESSAGE
        } else {
            this.errorMessage = errorMessage
        }
    }

    def error(AdyenOrderPayment adyenOrderPayment, String defaultErrorMessage = null) {
        if(adyenOrderPayment.error?.reason) {
            this.error(adyenOrderPayment.error?.reason)
        } else {
            this.error(defaultErrorMessage ?: DEFAULT_ERROR_MESSAGE)
        }
    }

    def setRedirectTrue() {
        isRedirect = true
    }

    Map getModalParams() {
        if(this.state.equals(State.RECEIPT) || this.state.equals(State.ERROR)) {
            return [ orderId: orderId ]
        }

        return [:]
    }

    boolean isFinished() {
        return !this.state.equals(State.UNFINISHED)
    }

    String getFinalAction() {
        switch(state) {
            case State.RECEIPT:
                return RECEIPT_ACTION
            default:
                return ERROR_ACTION
        }
    }

    static String createFinishUrl(String url, Long orderId) {
        Map<String, Object> result = StringHelper.splitUrl(url)
        Map queryParams            = result?.queryParams as Map

        if (queryParams.get("orderId")) {
            queryParams["orderId"] = orderId
        } else {
            queryParams.put("orderId", orderId)
        }

        def resultUrl = new URIBuilder(result.uri as String)
        result.queryParams.each { k, v ->
            resultUrl.addQueryParam(k, v)
        }

        return resultUrl.toString()
    }

    /**
     * Returns Map of PaymentFlow instances stored in session.
     * @param session
     * @return Map<Long, PaymentFlow> instance
     */
    static Map<Long, PaymentFlow> getPaymentFlows(HttpSession session) {
        Map<Long, PaymentFlow> paymentFlows = session.getAttribute(PAYMENT_FLOW) as Map<Long, PaymentFlow>  ?: [:]
        session[PAYMENT_FLOW] = paymentFlows
        return paymentFlows
    }

    static Logger getLogger() {
        return LoggerFactory.getLogger(PaymentFlow.class)
    }

    static String hostName() {
        return InetAddress.getLocalHost().getHostName()
    }

    static void log(String message , Long orderId) {
        getLogger().info(LogHelper.formatOrder("${hostName()} - ${message}",  orderId))
    }

    /**
     * Returns new PaymentFlow instances stored in session.
     * @param session, orderId
     * @return Map<Long, PaymentFlow> instance
     */
    static PaymentFlow addInstance(HttpSession session, PaymentFlow paymentFlow) {
        Map<Long, PaymentFlow> paymentFlows = getPaymentFlows(session)
        log("addInstance", paymentFlow.orderId)
        return paymentFlows.put(paymentFlow.orderId, paymentFlow)
    }

    /**
     * Returns PaymentFlow instance stored in session.
     * @param session, orderId
     * @return PaymentFlow instance
     */
    static PaymentFlow getInstance(HttpSession session, Long orderId) {
        Map<Long, PaymentFlow> paymentFlows = getPaymentFlows(session)
        log("getInstance", orderId)
        return paymentFlows?.get(orderId)
    }

    /**
     * Returns PaymentFlow instance if finished. If not finished, it deletes and returns null.
     * @param session, orderId
     * @return Finished PaymentFlow instance, or null
     */
    static PaymentFlow getFinished(HttpSession session, Long orderId) {
        Map<Long, PaymentFlow> paymentFlows = getPaymentFlows(session)
        PaymentFlow paymentFlow = paymentFlows?.get(orderId)
        log("getFinished - ${paymentFlow}", orderId)
        if(paymentFlow?.isFinished()) {
            return paymentFlow
        }

        popInstance(session, orderId)
        return null
    }

    /**
     * Returns PaymentFlow instance and removes it from session.
     * @param session, orderId
     * @return PaymentFlow instance
     */
    static PaymentFlow popInstance(HttpSession session, Long orderId) {
        Map<Long, PaymentFlow> paymentFlows = getPaymentFlows(session)
        log("popInstance", orderId)
        return paymentFlows?.remove(orderId)
    }
}
