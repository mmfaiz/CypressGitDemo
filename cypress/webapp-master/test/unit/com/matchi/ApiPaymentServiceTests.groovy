package com.matchi
import com.matchi.boxnet.BoxnetManager
import com.matchi.protocol.ApiPaymentRequest
import com.matchi.protocol.ApiPaymentRequestException
import grails.test.MockUtils
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.junit.Before
import org.junit.Test

@TestFor(ApiPaymentService)
@Mock([PaymentOrder, Payment])
class ApiPaymentServiceTests {

    def mockPaymentService
    def mockBoxnetManager

    ApiPaymentRequest request
    PaymentOrder paymentOrder1
    PaymentOrder paymentOrder2

    @Before
    public void setUp() {
        MockUtils.mockLogging(ApiPaymentService, true)

        mockPaymentService = mockFor(PaymentService)
        mockBoxnetManager = mockFor(BoxnetManager)
        service.paymentService = mockPaymentService.createMock()
        service.boxnetManager = mockBoxnetManager.createMock()

        request = new ApiPaymentRequest()
        request.orderIds = "1:2"
        request.prices = "100:200"
        request.confirmed = true
        request.errorMessage = ""
        request.paymentMethod = "CASH"
        request.cashRegisterTransactionId = 123

        paymentOrder1 = new PaymentOrder(id: "1").save(validate: false)
        paymentOrder2 = new PaymentOrder(id: "2").save(validate: false)

        mockPaymentService.demand.registerPayment(0..1) { PaymentOrder po ->
            return new Payment()
        }
    }

    @Test
    void testExceptionOnValidationError() {
        mockBoxnetManager.demand.validateResponse(1..1) { ApiPaymentRequest r ->
            return false
        }

        shouldFail(ApiPaymentRequestException) {
            service.processApiPaymentRequest(request)
        }
    }

    @Test
    void testExceptionOnNoOrdersFound() {
        mockBoxnetManager.demand.validateResponse(1..1) { ApiPaymentRequest r ->
            return true
        }

        paymentOrder1.delete()
        paymentOrder2.delete()

        shouldFail(ApiPaymentRequestException) {
            service.processApiPaymentRequest(request)
        }
    }

    @Test
    void testExceptionOnNoConfirmation() {
        mockBoxnetManager.demand.validateResponse(1..1) { ApiPaymentRequest r ->
            return true
        }

        request.confirmed = false

        shouldFail(ApiPaymentRequestException) {
            service.processApiPaymentRequest(request)
        }
    }

    @Test
    void testExceptionOnNoTransactionId() {
        mockBoxnetManager.demand.validateResponse(1..1) { ApiPaymentRequest r ->
            return true
        }

        request.cashRegisterTransactionId = null

        shouldFail(ApiPaymentRequestException) {
            service.processApiPaymentRequest(request)
        }
    }
    @Test
    void testRetrieveOrdersReturnExistingOrdersWithPricesIfOrderFound() {
        mockBoxnetManager.demand.validateResponse(1..1) { ApiPaymentRequest r ->
            return true
        }

        def orderAndPrices = service.retrievePaymentOrdersAndPrices(request)

        assert orderAndPrices.size() == 2
        assert orderAndPrices.collect { it.order }.size() == 2
        assert orderAndPrices.collect { it.pricePaid }.size() == 2
    }
}
