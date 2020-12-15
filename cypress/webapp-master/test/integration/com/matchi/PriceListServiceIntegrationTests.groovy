package com.matchi

import com.matchi.price.CourtPriceCondition
import com.matchi.price.DatePriceCondition
import com.matchi.price.Price
import com.matchi.price.PriceListConditionCategory
import com.matchi.price.PriceListCustomerCategory

import static com.matchi.TestUtils.*

/**
 * @author Sergei Shushkevich
 */
class PriceListServiceIntegrationTests extends GroovyTestCase {

    def priceListService

    void testCopyAndSave() {
        def f = createFacility()
        def court = createCourt(f)
        def cc = new PriceListCustomerCategory(facility: f, name: "cc1", defaultCategory: true)
                .save(flush: true, failOnError: true)
        def pl1 = new PriceList(name: "pl1", description: "desc", startDate: new Date(),
                facility: f, sport: createSport(), subscriptions: true)
                .addToPriceListConditionCategories(new PriceListConditionCategory(name: "cat1", defaultCategory: true)
                        .addToConditions(new DatePriceCondition(startDate: new Date(), endDate: new Date() + 1))
                        .addToConditions(new CourtPriceCondition().addToCourts(court))
                        .addToPrices(new Price(price: 100, account: "account", customerCategory: cc)))
                .save(flush: true, failOnError: true)

        assert priceListService.copyAndSave(pl1, new PriceList(name: "pl2", startDate: new Date() + 11))

        assert PriceList.countByFacility(f) == 2
        def pl2 = PriceList.findByName("pl2")
        assert pl2.startDate != pl1.startDate
        assert pl2.description == pl1.description
        assert pl2.facility == pl1.facility
        assert pl2.sport == pl1.sport
        assert pl2.subscriptions == pl1.subscriptions
        assert pl2.priceListConditionCategories.size() == 1
        def pl1Cat = pl1.priceListConditionCategories.iterator().next()
        def pl2Cat = pl2.priceListConditionCategories.iterator().next()
        assert pl2Cat != pl1Cat
        assert pl2Cat.name == pl1Cat.name
        assert pl2Cat.defaultCategory == pl1Cat.defaultCategory
        assert pl2Cat.conditions.size() == 2
        def pl1Cond = pl1Cat.conditions.find { it instanceof DatePriceCondition }
        def pl2Cond = pl2Cat.conditions.find { it instanceof DatePriceCondition }
        assert pl2Cond != pl1Cond
        assert pl2Cond.startDate == pl1Cond.startDate
        assert pl2Cond.endDate == pl1Cond.endDate
        def pl2CourtCond = pl2Cat.conditions.find { it instanceof CourtPriceCondition }
        assert pl2CourtCond
        assert pl2CourtCond.courts.size() == 1
        assert pl2CourtCond.courts.iterator().next() == court
        assert pl2Cat.prices.size() == 1
        def pl1Price = pl1Cat.prices.iterator().next()
        def pl2Price = pl2Cat.prices.iterator().next()
        assert pl2Price != pl1Price
        assert pl2Price.price == pl1Price.price
        assert pl2Price.account == pl1Price.account
        assert pl2Price.customerCategory == pl1Price.customerCategory
    }

    void testDeletePricelist() {
        def f = createFacility()
        def court = createCourt(f)
        def cc = new PriceListCustomerCategory(facility: f, name: "cc1", defaultCategory: true)
                .save(flush: true, failOnError: true)

        def pl1 = new PriceList(name: "pl1", description: "desc", startDate: new Date(),
                facility: f, sport: createSport(), subscriptions: true)
                .addToPriceListConditionCategories(new PriceListConditionCategory(name: "cat1", defaultCategory: true)
                .addToConditions(new DatePriceCondition(startDate: new Date(), endDate: new Date() + 1))
                .addToConditions(new CourtPriceCondition().addToCourts(court))
                .addToPrices(new Price(price: 100, account: "account", customerCategory: cc)))
                .save(flush: true, failOnError: true)

        assert PriceList.countByFacility(f) == 1

        priceListService.delete(pl1)
        assert PriceList.countByFacility(f) == 0
    }

    void testGetActivePriceLists() {
        def facility = createFacility()
        createPriceLists(facility)

        def result = priceListService.getActivePriceLists(facility)

        assert 3 == result.size()
        assert result.find { it.name == "pl1" }
        assert result.find { it.name == "pl5" }
        assert result.find { it.name == "pl7" }
    }

    void testGetInactivePriceLists() {
        def facility = createFacility()
        createPriceLists(facility)

        def result = priceListService.getInactivePriceLists(facility)

        assert 2 == result.size()
        assert result.find { it.name == "pl2" }
        assert result.find { it.name == "pl6" }
    }

    void testGetUpcomingPriceLists() {
        def facility = createFacility()
        createPriceLists(facility)

        def result = priceListService.getUpcomingPriceLists(facility)

        assert 3 == result.size()
        assert result.find { it.name == "pl3" }
        assert result.find { it.name == "pl4" }
        assert result.find { it.name == "pl8" }
    }

    private createPriceLists(facility) {
        def sport = createSport()
        new PriceList(name: "pl1", startDate: new Date() - 1, facility: facility,
                sport: sport).save(flush: true, failOnError: true)
        new PriceList(name: "pl2", startDate: new Date() - 2, facility: facility,
                sport: sport).save(flush: true, failOnError: true)
        new PriceList(name: "pl3", startDate: new Date() + 1, facility: facility,
                sport: sport).save(flush: true, failOnError: true)
        new PriceList(name: "pl4", startDate: new Date() + 1, facility: facility,
                sport: sport, subscriptions: true).save(flush: true, failOnError: true)
        new PriceList(name: "pl5", startDate: new Date() - 1, facility: facility,
                sport: sport, subscriptions: true).save(flush: true, failOnError: true)
        new PriceList(name: "pl6", startDate: new Date() - 2, facility: facility,
                sport: sport, subscriptions: true).save(flush: true, failOnError: true)
        new PriceList(name: "pl7", startDate: new Date() - 5, facility: facility,
                sport: createSport()).save(flush: true, failOnError: true)
        new PriceList(name: "pl8", startDate: new Date() + 5, facility: facility,
                sport: createSport(), subscriptions: true).save(flush: true, failOnError: true)
    }
}
