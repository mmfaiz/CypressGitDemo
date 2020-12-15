package com.matchi.adyen

import grails.test.mixin.TestFor
import groovy.mock.interceptor.MockFor
import groovyx.net.http.ContentType
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.Method
import org.junit.Before
import org.junit.Test
/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@TestFor(AdyenHttpService)
class AdyenHttpServiceSpec {

    def mockedConfig

    @Before
    void setUp() {
        mockedConfig = new ConfigObject()

        mockedConfig.adyen.paymentUrl           = "https://pal-test.adyen.com/pal/servlet/Payment/v25/"
        mockedConfig.adyen.recurringUrl         = "https://pal-test.adyen.com/pal/servlet/Recurring/v25/"
        mockedConfig.adyen.detailsUrl           = "https://test.adyen.com/hpp/directory/v2.shtml"
        mockedConfig.adyen.skipDetailsUrl       = "https://test.adyen.com/hpp/skipDetails.shtml"

        service.grailsApplication = [ config: mockedConfig ]
    }

    /*
    @Test
    void testRequestThrowsExceptionOnSuccessFalse() {
        def httpMock = mockHttpBuilder([ "success":"false" ] as JSON, false)

        shouldFail(AdyenException) {
            httpMock.use {
                service.request("https://adyen.com", [ "test": "one" ])
                //fail "Should throw AdyenException"
            }
        }
    }*/

    @Test
    void testGetAuthUrl() {
        service.getPaymentUrl(AdyenService.AdyenProcessOperation.AUTHORISE) ==
                "https://pal-test.adyen.com/pal/servlet/Payment/v25/authorise"
    }

    @Test
    void testGetAuth3DSUrl() {
        service.getPaymentUrl(AdyenService.AdyenProcessOperation.AUTHORISE3D) ==
                "https://pal-test.adyen.com/pal/servlet/Payment/v25/authorise3d"
    }

    @Test
    void testGetCaptureUrl() {
        service.getPaymentUrl(AdyenService.AdyenProcessOperation.CAPTURE) ==
                "https://pal-test.adyen.com/pal/servlet/Payment/v25/capture"
    }

    @Test
    void testGetRefundUrl() {
        service.getPaymentUrl(AdyenService.AdyenProcessOperation.REFUND) ==
                "https://pal-test.adyen.com/pal/servlet/Payment/v25/refund"
    }

    @Test
    void testGetCancelUrl() {
        service.getPaymentUrl(AdyenService.AdyenProcessOperation.CANCEL) ==
                "https://pal-test.adyen.com/pal/servlet/Payment/v25/cancel"
    }

    @Test
    void testGetRecurringDetailsUrl() {
        service.getRecurringUrl(AdyenService.AdyenRecurringOperation.listRecurringDetails) ==
                "https://pal-test.adyen.com/pal/servlet/Recurring/v25/listrecurringdetails"
    }

    @Test
    void testGisableRecurringUrl() {
        service.getRecurringUrl(AdyenService.AdyenRecurringOperation.DISABLE) ==
                "https://pal-test.adyen.com/pal/servlet/Recurring/v25/disable"
    }

    // Helper for creating mock HttpBuilder object
    static def mockHttpBuilder(def responseJson, def success = true, def responseMap = [:]) {
        def mock = new MockFor(HTTPBuilder)
        def requestDelegate = [response: responseMap]

        mock.demand.getEncoder(1..1) { }
        mock.demand.getAuthentication(1..1) { }
        mock.demand.request { Method method, ContentType contentType, Closure body ->

            body.delegate = requestDelegate
            body.call()
            if(success) {
                requestDelegate.response.success(responseMap, new ByteArrayInputStream( responseJson.getBytes() ))
            } else {
                requestDelegate.response.failure(responseMap)
            }

        }

        return mock
    }
}
