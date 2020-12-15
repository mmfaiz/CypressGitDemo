package com.matchi.facility

import com.matchi.Facility
import com.matchi.FacilityUser
import com.matchi.FacilityUserRole
import com.matchi.InvoiceService
import com.matchi.Municipality
import com.matchi.Region
import com.matchi.User
import com.matchi.fortnox.v3.FortnoxArticle
import grails.plugin.springsecurity.SpringSecurityService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor

import javax.servlet.http.HttpServletResponse

import static com.matchi.TestUtils.*

@TestFor(FacilityAdministrationController)
@Mock([Facility, Municipality, Region, User, FacilityUser, FacilityUserRole, Organization])
class FacilityAdministrationControllerTests {

    void testSwitchFacility() {
        def user = createUser()
        def facility1 = createFacility()
        def facility2 = createFacility()
        user.facility = facility1
        def fu1 = new FacilityUser(user: user)
                .addToFacilityRoles(new FacilityUserRole(accessRight: FacilityUserRole.AccessRight.CUSTOMER))
        fu1.facility = facility1
        fu1.save(failOnError: true)
        def fu2 = new FacilityUser(user: user)
                .addToFacilityRoles(new FacilityUserRole(accessRight: FacilityUserRole.AccessRight.CUSTOMER))
        fu2.facility = facility2
        fu2.save(failOnError: true)
        def springSecurityServiceControl = mockSpringSecurityService(user)
        controller.metaClass.defaultFacilityController = { -> "facilityBooking" }

        controller.switchFacility(facility2.id)

        assert facility2 == user.facility
        assert "/facility/booking" == response.redirectedUrl
        springSecurityServiceControl.verify()
    }

    void testSwitchFacility_InvalidFacilityId() {
        def user = createUser()
        def facility1 = createFacility()
        def facility2 = createFacility()
        user.facility = facility1
        def springSecurityServiceControl = mockSpringSecurityService(user)

        controller.switchFacility(facility2.id)

        assert facility1 == user.facility
        assert HttpServletResponse.SC_BAD_REQUEST == response.status
        springSecurityServiceControl.verify()
    }

    void testListOrganizationArticles() {
        def facility = createFacility()
        def organization = createOrganization(facility)
        def organizationArticles = [new FortnoxArticle(ArticleNumber: "1", Description: "descr1")]

        def invoiceServiceControl = mockFor(InvoiceService)
        invoiceServiceControl.demand.getItemsForOrganization { oId -> organizationArticles }
        controller.invoiceService = invoiceServiceControl.createMock()

        controller.params.organizationId = organization.id
        controller.params.articleName = "article"
        def organizationArticlesSelect = controller.select(name: controller.params.articleName, from: organizationArticles,
                value: '', optionKey: 'id', optionValue: 'descr', noSelection: ['': controller.message(code: 'default.article.multiselect.noneSelectedText')])

        controller.listOrganizationArticles()

        assert organizationArticlesSelect == response.text
        invoiceServiceControl.verify()
    }

    void testListOrganizationArticlesIfNoOrganization() {
        def facility = createFacility()
        def facilityArticles = [new FortnoxArticle(ArticleNumber: "1", Description: "descr1")]

        def invoiceServiceControl = mockFor(InvoiceService)
        invoiceServiceControl.demand.getItems { fId -> facilityArticles }
        controller.invoiceService = invoiceServiceControl.createMock()
        controller.metaClass.getUserFacility = { -> facility }

        controller.params.organizationId = null
        controller.params.articleName = "article"
        def facilityArticlesSelect = controller.select(name: controller.params.articleName, from: facilityArticles,
                value: '', optionKey: 'id', optionValue: 'descr', noSelection: ['': controller.message(code: 'default.article.multiselect.noneSelectedText')])

        controller.listOrganizationArticles()

        assert facilityArticlesSelect == response.text
        invoiceServiceControl.verify()
    }

    private mockSpringSecurityService(user) {
        def springSecurityServiceControl = mockFor(SpringSecurityService)
        springSecurityServiceControl.demand.getCurrentUser { -> user }
        controller.springSecurityService = springSecurityServiceControl.createMock()
        springSecurityServiceControl
    }
}
