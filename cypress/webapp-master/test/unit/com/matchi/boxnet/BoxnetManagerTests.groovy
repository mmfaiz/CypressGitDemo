package com.matchi.boxnet

import com.matchi.protocol.ApiPaymentRequest
import grails.test.mixin.TestMixin
import grails.test.mixin.domain.DomainClassUnitTestMixin
import org.codehaus.groovy.grails.plugins.codecs.MD5Codec
import org.junit.Before
import org.junit.Test

@TestMixin(DomainClassUnitTestMixin)
class BoxnetManagerTests {

    def boxnetManager
    def mockedConfig
    ApiPaymentRequest request

    @Before
    public void setUp() {
        mockCodec(MD5Codec)

        mockedConfig = new ConfigObject()
        mockedConfig.boxnet.secret="secret"

        boxnetManager = new BoxnetManager()
        boxnetManager.grailsApplication = [ config: mockedConfig ]

        request = new ApiPaymentRequest()
        request.orderIds = "1:2"
        request.prices = "100:200"
        request.confirmed = true
        request.errorMessage = ""
        request.paymentMethod = "CASH"
        request.cashRegisterTransactionId = 123
    }

    @Test
    public void testVerificationCodeValidates() {
        StringBuilder sb = new StringBuilder()
        def tmp = sb.append(request.orderIds)
            .append(request.prices)
            .append(request.confirmed)
            .append(request.errorMessage)
            .append(request.paymentMethod)
            .append(request.cashRegisterTransactionId)
            .append(boxnetManager.boxnetSecret()).toString().encodeAsMD5()
        request.hash = tmp

        assert boxnetManager.validateResponse(request)
    }
    @Test
    public void testVerificationCodeDoesNotValidateWithoutSecret() {
        StringBuilder sb = new StringBuilder()
        def tmp = sb.append(request.orderIds)
            .append(request.prices)
            .append(request.confirmed)
            .append(request.errorMessage)
            .append(request.paymentMethod)
            .append(request.cashRegisterTransactionId).toString().encodeAsMD5()
        request.hash = tmp

        assert !boxnetManager.validateResponse(request)
    }
    @Test
    public void testGenerateHashCreatesCorrectHash() {
        def orderIds = request.orderIds
        def prices = "100:200"
        def priceDescription = "Description1"
        def responseUrl = "http://matchi.se"

        StringBuilder sb = new StringBuilder()
        def tmp = sb.append(orderIds)
            .append(prices)
            .append(priceDescription)
            .append(responseUrl)
            .append(boxnetManager.boxnetSecret()).toString().encodeAsMD5()

        assert tmp == boxnetManager.createVerificationHash([orderIds, prices, priceDescription, responseUrl])
    }
}
