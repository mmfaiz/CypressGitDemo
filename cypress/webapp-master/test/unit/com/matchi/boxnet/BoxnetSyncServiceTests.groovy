package com.matchi.boxnet
import com.matchi.CashRegisterTransaction
import com.matchi.Facility
import com.matchi.external.BoxnetTransaction
import com.matchi.payment.PaymentMethod
import grails.plugin.springsecurity.SpringSecurityService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import grails.test.mixin.support.GrailsUnitTestMixin
import org.joda.time.DateTime
import org.junit.Before

import java.text.DecimalFormat

@TestFor(BoxnetSyncService)
@Mock([Facility, CashRegisterTransaction, BoxnetTransaction])
class BoxnetSyncServiceTests {

    def mockSecurityServiceControl
    def mockedConfig
    def userParam = "user"
    def keyParam = "key"
    def shortnameParam = "matchitk"
    def startDateParam = "2013-01-01"
    def stopDateParam  = "2013-03-01"

    @Before
    void setUp() {

        mockSecurityServiceControl = mockFor(SpringSecurityService)
        service.springSecurityService = mockSecurityServiceControl.createMock()

        mockedConfig = new ConfigObject()
        mockedConfig.boxnet.transactions.user = userParam
        mockedConfig.boxnet.transactions.key  = keyParam
        service.grailsApplication = [ config: mockedConfig ]
    }

    void testGetVatReturnsCorrectString() {
        def df = new DecimalFormat("###.##")
        assert service.getVat("0.8") == df.format(0.25)
        assert service.getVat("0.94") == df.format(0.06)
    }

    void testAppendParametersAppendsCorrectParams() {
        def paramsString = service.appendParameters(new Facility(shortname: shortnameParam), new DateTime(startDateParam), new DateTime(stopDateParam))
        def expectedResult = "user=${userParam}&key=${keyParam}&shortname=matchitk&start=${startDateParam}&stop=${stopDateParam}"

        assert paramsString == expectedResult
    }

    void testgetPaymentMethod() {
        assert service.getPaymentMethod("K") == PaymentMethod.CASH
        assert service.getPaymentMethod("B") == PaymentMethod.CREDIT_CARD
        assert service.getPaymentMethod("I") == PaymentMethod.INVOICE
        assert service.getPaymentMethod("S") == PaymentMethod.SWISH
    }
}
