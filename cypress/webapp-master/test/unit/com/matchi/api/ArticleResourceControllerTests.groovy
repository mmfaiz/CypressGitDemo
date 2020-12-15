package com.matchi.api

import com.matchi.Facility
import com.matchi.FacilityProperty
import com.matchi.FacilityService
import com.matchi.InvoiceService
import com.matchi.Municipality
import com.matchi.Region
import com.matchi.invoice.InvoiceArticle
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.domain.DomainClassUnitTestMixin
import org.junit.Before
import static com.matchi.TestUtils.*

/**
 * @author Sergei Shushkevich
 */
@TestFor(ArticleResourceController)
@Mock([Facility, InvoiceService, InvoiceArticle, Municipality, Region])
@TestMixin(DomainClassUnitTestMixin)
class ArticleResourceControllerTests {

    def facility

    @Before
    void setUp() {
        controller.invoiceService = new InvoiceService()
        controller.invoiceService.transactionManager = getTransactionManager()

        facility = createFacility()
        facility.facilityProperties = [
                new FacilityProperty(key: FacilityProperty.FacilityPropertyKey.FEATURE_EXTERNAL_ARTICLES.name(), value: "1")
        ]

        def mockFacilityService = mockFor(FacilityService)
        controller.facilityService = mockFacilityService.createMock()
        mockFacilityService.demand.getFacility{ fid -> facility}
    }

    void testUpdateArticles() {
        request.json = '[{"articleNumber": 123, "name": "Article 1"}]'
        controller.updateArticles()
        assert response.status == 200
    }

    void testUpdateArticlesWithInvalidRequest() {
        request.json = '{"articleNumber": 123, "name": "Article 1"}'
        shouldFail(APIException) {
            controller.updateArticles()
        }
    }

    void testUpdateArticlesWithValidationError() {
        request.json = '[{"articleNumber": 123}]'
        controller.updateArticles()
        assert response.status == 422
        assert response.json.errors.size() == 1
        assert response.json.errors[0].path == "/0/name"
    }

    void testUpdateArticlesNonExternal() {
        facility.facilityProperties = [
                new FacilityProperty(key: FacilityProperty.FacilityPropertyKey.FEATURE_EXTERNAL_ARTICLES.name(), value: "0")
        ]

        request.json = '[{"articleNumber": 123, "name": "Article 1"}]'
        controller.updateArticles()
        assert response.status == 400
    }

}