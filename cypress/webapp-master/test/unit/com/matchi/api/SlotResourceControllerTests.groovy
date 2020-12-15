package com.matchi.api

import com.matchi.*
import com.matchi.api.v2.SlotResourceController
import com.matchi.price.Price
import com.matchi.price.PriceListConditionCategory
import com.matchi.price.PriceListCustomerCategory
import grails.test.GrailsMock
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.junit.Before
/**
 * See the API for {@link grails.test.mixin.web.ControllerUnitTestMixin} for usage instructions
 */
@TestFor(SlotResourceController)
@Mock([Slot, PaymentInfo])
class SlotResourceControllerTests {

    @Before
    void setUp() {
        def slot = new Slot()
        slot.id = firstId
        slot.save(validate: false)

        def couponServiceMock = mockFor(CouponService)
        def pricelistServiceMock = mockFor(PriceListService)

        pricelistServiceMock.demand.getBookingPrice() { s, u ->
            return new Price(name: "Test", price: 100, priceCategory: new PriceListConditionCategory(),
                    customerCategory: new PriceListCustomerCategory(facility: new Facility(vat: 0)))
        }

        couponServiceMock.demand.getValidCouponsByUserAndSlots() { u, slots, price ->
            return []
        }
        couponServiceMock.demand.getValidCouponsByUserAndSlots() { u, slots, price, type ->
            return []
        }
        couponServiceMock.demand.getActivePromoCodes() { f->
            return []
        }

        controller.couponService = couponServiceMock.createMock()
        controller.priceListService = pricelistServiceMock.createMock()
    }

    void testEmptyResultIfSlotNotFound() {
        request.JSON = slotJsonSingleNotExist

        GrailsMock slotStaticMock = mockFor(Slot)
        slotStaticMock.demand.static.findAllByIdInList(1) { List ids ->
            return []
        }

        controller.price()

        assert response.json.prices.isEmpty()
    }

    void testEmptyPriceModelIfSlotNotFound() {
        request.JSON = slotJsonSingleNotExist

        GrailsMock slotStaticMock = mockFor(Slot)
        slotStaticMock.demand.static.findAllByIdInList(1) { List ids ->
            return []
        }

        controller.priceModel()

        assert response.json.slots.isEmpty()
    }

    void testSomething() {
        GrailsMock slotStaticMock = mockFor(Slot)
        GrailsMock slotMock = mockFor(Slot)
        Slot slot = slotMock.createMock()

        GrailsMock facilityMock = mockFor(Facility)
        Facility facility = facilityMock.createMock()
        def springSecurityService = [getCurrentUser: {  ->
            return new User()
        }]
        controller.springSecurityService = springSecurityService

        slotStaticMock.demand.static.findAllByIdInList(1) { List ids ->
            assert ids[0] == firstId
            return [slot]
        }

        slotMock.demand.getCourt(4) { ->
            return [facility: facility]
        }

        slotMock.demand.asBoolean(1) { ->
            return true
        }

        slotMock.demand.getCourt(1) { ->
            return [facility: facility]
        }

        facilityMock.demand.isFacilityPropertyEnabled(1) { String propertyName ->
            return false
        }

        facilityMock.demand.getCurrency(1) { ->
            return "SEK"
        }

        facilityMock.demand.isFacilityPropertyEnabled(1) { String propertyName ->
            return false
        }

        facilityMock.demand.getBookingCancellationLimit(1) { ->
            return 6
        }

        facilityMock.demand.isFacilityPropertyEnabled(1) { String propertyName ->
            return false
        }

        request.JSON = slotJsonSingle
        controller.price()
        assert !response.json.prices.isEmpty()
        assert response.json.facilityCancellationLimit == 6
        assert !response.json.facilityFeatureCalculateMultiplePlayersPrice
        slotMock.verify()
        slotStaticMock.verify()
        facilityMock.verify()
    }

    void testPriceModel() {
        GrailsMock slotStaticMock = mockFor(Slot)
        GrailsMock customerStaticMock = mockFor(Customer)
        GrailsMock slotMock = mockFor(Slot)
        GrailsMock customerMock = mockFor(Customer)
        Slot slot = slotMock.createMock()
        Customer customer = customerMock.createMock()

        GrailsMock facilityMock = mockFor(Facility)
        Facility facility = facilityMock.createMock()
        def springSecurityService = [getCurrentUser: {  ->
                return null
        }]
        controller.springSecurityService = springSecurityService

        slotStaticMock.demand.static.findAllByIdInList(1) { List ids ->
            assert ids[0] == firstId
            return [slot]
        }

        slotMock.demand.getCourt(3) { ->
            return [facility: facility]
        }

        slotMock.demand.asBoolean(1) { ->
            return true
        }

        customerStaticMock.demand.static.findByUserAndFacility(1) { user, usersFacility ->
            return
        }

        def pricelistServiceMock = mockFor(PriceListService)

        pricelistServiceMock.demand.getPriceForSlot(1) { s, c, pe, cc, pcl ->
            return new Price(name: "Test", price: 100, priceCategory: new PriceListConditionCategory(),
                    customerCategory: new PriceListCustomerCategory(facility: new Facility(vat: 0)))
        }
        pricelistServiceMock.demand.getBookingPrice() { s, u ->
            return new Price(name: "Test", price: 100, priceCategory: new PriceListConditionCategory(),
                    customerCategory: new PriceListCustomerCategory(facility: new Facility(vat: 0)))
        }

        controller.priceListService = pricelistServiceMock.createMock()

        facilityMock.demand.isFacilityPropertyEnabled(1) { String propertyName ->
            return true
        }

        facilityMock.demand.getBookingCancellationLimit(1) { ->
            return 6
        }

        facilityMock.demand.isFacilityPropertyEnabled(1) { String propertyName ->
            return false
        }

        request.JSON = slotJsonWithPlayers
        controller.priceModel()
        assert !response.json.slots.isEmpty()
        assert !response.json.players.isEmpty()
        assert response.json.base == response.json.total
        assert response.json.facilityCancellationLimit == 6
        assert !response.json.facilityFeatureCalculateMultiplePlayersPrice
        slotMock.verify()
        slotStaticMock.verify()
        facilityMock.verify()
    }

    static String firstId = "edca8b9a3968cb220139c002be800f2f"
    static String secondId = "edca8b9a3968cb220139c002be500f29"

    static String slotJsonSingle =
        """{slotIds: ["$firstId"]}"""

    static String slotJsonWithPlayers =
            """{slotIds: ["$firstId"], playerEmails: ["first@gmail.com", "second@gmail.com"]}"""

    static String slotJsonSingleNotExist =
        """{slotIds: ["edca8b9a3968cb22awdawdawdawexist"]}"""

    static String slotJsonMultiple =
        """{slotIds: ["$firstId", "$secondId"]}"""
}
