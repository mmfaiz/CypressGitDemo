package com.matchi.admin

import com.matchi.Facility
import com.matchi.FacilityContract
import com.matchi.FacilityContractItem
import com.matchi.Municipality
import com.matchi.Region
import com.matchi.fortnox.FortnoxFacadeService
import com.matchi.fortnox.v3.Fortnox3ArticleService
import com.matchi.fortnox.v3.Fortnox3Service
import com.matchi.fortnox.v3.FortnoxArticle
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.junit.Before

import javax.servlet.http.HttpServletResponse

import static com.matchi.TestUtils.*

/**
 * @author Sergei Shushkevich
 */
@TestFor(AdminFacilityContractsController)
@Mock([Facility, FacilityContract, FacilityContractItem, Municipality, Region])
class AdminFacilityContractsControllerTests {

    def mockFortnoxFacadeService

    @Before
    void setUp() {
        mockFortnoxFacadeService = mockFor(FortnoxFacadeService)
        controller.fortnoxFacadeService = mockFortnoxFacadeService.createMock()
        mockFortnoxFacadeService.demand.listMatchiArticles(1..1) { -> return mockArticles() }
    }

    void testIndex() {
        def contract = createFacilityContract()
        params.id = contract.facility.id.toString()

        def model = controller.index()

        assert contract.facility == model.facility
        assert 1 == model.contracts.size()
        assert contract == model.contracts[0]
    }

    void testIndex_InvalidId() {
        params.id = "12345"
        controller.index()
        assert HttpServletResponse.SC_NOT_FOUND == response.status
    }

    void testCreate() {
        def model = controller.create()
        assert model.contract
    }

    void testSave() {
        def facility = createFacility()
        params."facility.id" = facility.id
        params.name = "test"
        params.fixedMonthlyFee = 1
        params.variableMediationFee = 2
        params.variableMediationFeePercentage = 3.0d
        params.variableCouponMediationFee = 5
        params.variableUnlimitedCouponMediationFee = 10
        params.variableGiftCardMediationFee = 7.8
        params.dateValidFrom = new Date()

        controller.save()

        assert "/admin/facility/contracts/index/${facility.id}" == response.redirectedUrl
        assert 1 == FacilityContract.countByFacilityAndName(facility, "test")
    }

    void testEdit() {
        def contract = createFacilityContract()
        def model = controller.edit(contract.id)
        assert contract == model.contract
    }

    void testUpdate() {
        def contract = createFacilityContract()
        def newName = "${contract.name} - updated"
        params.name = newName

        controller.update(contract.id, contract.version)

        assert "/admin/facility/contracts/index/${contract.facility.id}" == response.redirectedUrl
        assert FacilityContract.findByName(newName)
    }

    void testDelete() {
        def contract = createFacilityContract()
        def facilityId = contract.facility.id

        controller.delete(contract.id)

        assert "/admin/facility/contracts/index/${facilityId}" == response.redirectedUrl
        assert !FacilityContract.count()
    }

    void testCreateItem() {
        def model = controller.createItem()
        assert model.item
    }

    void testSaveItem() {
        def contract = createFacilityContract()
        params."contract.id" = contract.id
        params.description = "test desc"
        params.price = 99.99
        params.chargeMonths = [1]

        controller.saveItem()

        assert "/admin/facility/contracts/edit/${contract.id}" == response.redirectedUrl
        assert 1 == FacilityContractItem.countByContract(contract)
    }

    void testEditItem() {
        def item = createFacilityContractItem()
        def model = controller.editItem(item.id)
        assert item == model.item
    }

    void testUpdateItem() {
        def item = createFacilityContractItem()
        def newDesc = "${item.description} - updated"
        params.description = newDesc

        controller.updateItem(item.id, item.version)

        assert "/admin/facility/contracts/edit/${item.contract.id}" == response.redirectedUrl
        assert FacilityContractItem.findByDescription(newDesc)
    }

    void testDeleteItem() {
        def item = createFacilityContractItem()
        def contractId = item.contract.id

        controller.deleteItem(item.id)

        assert "/admin/facility/contracts/edit/${contractId}" == response.redirectedUrl
        assert !FacilityContractItem.count()
    }

    List<FortnoxArticle> mockArticles() {
        List<FortnoxArticle> articles = new ArrayList<>()

        FortnoxArticle article = new FortnoxArticle()
        article.setArticleNumber("1")
        article.setDescription("Test article")
        article.setSalesAccount(100)
        articles.add(article)

        return articles
    }
}
