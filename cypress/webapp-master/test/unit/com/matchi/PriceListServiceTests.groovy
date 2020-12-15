package com.matchi

import com.google.common.collect.Lists
import com.matchi.membership.Membership
import com.matchi.membership.MembershipType
import com.matchi.orders.Order
import com.matchi.price.*
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.codehaus.groovy.grails.commons.InstanceFactoryBean
import org.joda.time.DateTime
import org.joda.time.LocalDate
import org.junit.After
import org.junit.Before
import org.junit.Test

import static com.matchi.TestUtils.createCustomer
import static com.matchi.TestUtils.createMembership
import static plastic.criteria.PlasticCriteria.mockCriteria

@TestFor(PriceListService)
@Mock([ PriceList, PriceListConditionCategory, PriceListCustomerCategory, Price, Facility, MemberPriceCondition, MembershipType,
        Court, Slot, Customer, Sport, Subscription, Membership, Order, User, WeekDayPriceCondition, MemberTypePriceCondition, FacilityProperty ])
class PriceListServiceTests {

    Slot slot
    Customer customer
    PriceList priceList
    PriceList priceListForSubscription
    Facility facility
    Sport sport
    DateUtil dateUtil = new DateUtil()
    def mockFacilityService
    def mockCustomerService

    @Before
    public void setUp() {
        mockCriteria([PriceList])

        Court court = new Court(name: "Bana 1", listPosition: 1).save(validate: false)
        slot = new Slot(startTime: new DateTime().toDate(), endTime: new DateTime().plusHours(1).toDate(), court: court).save(validate: false)
        customer = new Customer().save(validate: false)
        priceList = new PriceList()

        assert slot != null

        priceList.priceListConditionCategories = [createPriceListCategory(100L)]
        priceList.startDate = new DateTime().minusDays(1).toDate()
        priceList.save(validate: false)

        priceListForSubscription = new PriceList()
        priceListForSubscription.priceListConditionCategories = [createPriceListCategory(80L)]
        priceListForSubscription.subscriptions = true
        priceListForSubscription.startDate = new DateTime().minusDays(1).toDate()
        priceListForSubscription.name = "With Subscription"
        priceListForSubscription.save(validate: false)

        sport = new Sport(name: "sportTest", position: 0).save(validate: false)
        facility = new Facility().save(validate: false)

        service.dateUtil = dateUtil
        mockFacilityService = mockFor(FacilityService)
        mockCustomerService = mockFor(CustomerService)
        defineBeans {
            facilityService(InstanceFactoryBean, mockFacilityService.createMock(), FacilityService)
            customerService(InstanceFactoryBean, mockCustomerService.createMock(), CustomerService)
        }
        this.mockFacilityService.demand.getAllHierarchicalFacilities(1..100) { f ->
            return Lists.asList(f)
        }
        this.mockCustomerService.demand.findHierarchicalUserCustomers(1..100) { c ->
            return Lists.asList(c)
        }
    }

    private PriceListConditionCategory createPriceListCategory(Long netPrice) {
        def userCategory = new PriceListCustomerCategory(conditions: []).save(validate: false)
        def category = new PriceListConditionCategory(conditions: [])
        Price price = new Price(price: netPrice, priceCategory: category, customerCategory: userCategory).save(validate: false)
        category.prices = [price]
        category.conditions = []
        category.save(validate: false)
        category
    }


    @After
    public void tearDown() { }

    @Test
    void testSimpleBookingPriceNotNull() {
        assert slot != null
        assert customer != null
        assert priceList != null
        assert service.getBookingPrice(slot, customer, priceList) != null
    }

    @Test
    void testSimpleBookingPrice() {
        assert service.getBookingPrice(slot, customer, priceList).price == 100L
    }

    void testActivePriceListBookingPrice() {
        assert service.getBookingPrice(slot, customer).price == 100L
    }

    void testActivePriceListNotFound() {
        priceList.startDate = new DateTime().plusDays(1).toDate()
        priceList.save(validate: false)
        assert !service.getBookingPrice(slot, customer).price
    }

    void testSubscriptionBookingPrice() {
        def subscription = new Subscription(description: "test", customer: customer).save(validate: false)
        slot.subscription = subscription

        assert service.getBookingPrice(slot, customer).price == 80L
    }

    void testSubscriptionPriceListNotFound() {
        def subscription = new Subscription(description: "test", customer: customer).save(validate: false)
        slot.subscription = subscription

        priceListForSubscription.startDate = new DateTime().plusDays(1).toDate()
        priceListForSubscription.save(validate: false)

        assert !service.getBookingPrice(slot, customer).price
    }

    @Test
    void testGetPriceForSlot() {
        Court court = new Court(name: "Bana 2", listPosition: 1, sport: sport).save(validate: false)
        Slot slot = new Slot(startTime: new DateTime().toDate(), endTime: new DateTime().plusHours(1).toDate(), court: court).save(validate: false)

        facility.addToFacilityProperties(new FacilityProperty(key: FacilityProperty.FacilityPropertyKey.FEATURE_CALCULATE_MULTIPLE_PLAYERS_PRICE.name(), value: "1", facility: facility).save(failOnError: true))
        facility.addToFacilityProperties(new FacilityProperty(key: FacilityProperty.FacilityPropertyKey.MULTIPLE_PLAYERS_NUMBER.name(), value: "['1':'4']", facility: facility).save(failOnError: true))

        MembershipType membershipType = new MembershipType(facility: facility, name: "normal member", price: 0)

        String emailCustomer0 = 'email0@matchi.se'
        String emailCustomerMember = 'emailMember@matchi.se'
        String emailCustomerDuplicate = 'emailDuplicate@matchi.se'
        String emailRandom = 'random@matchi.se'

        Customer customer0 = createCustomer(facility, emailCustomer0)
        Customer customerDuplicateMember = createCustomer(facility, emailCustomerDuplicate)
        Customer customerDuplicateNotMember = createCustomer(facility, emailCustomerDuplicate)
        Customer customerMember = createCustomer(facility, emailCustomerMember)


        createMembership(customerMember, new LocalDate().minusDays(10), new LocalDate().plusDays(10), new LocalDate().plusDays(20), membershipType)
        createMembership(customerDuplicateMember, new LocalDate().minusDays(10), new LocalDate().plusDays(10), new LocalDate().plusDays(20), membershipType)

        PriceList pricelist = new PriceList(facility: facility, name: "test price list", sport: sport, startDate: new LocalDate().minusDays(30), endDate: new LocalDate().plusDays(30)).save(failOnError: true)

        PriceListConditionCategory pc1 = new PriceListConditionCategory(pricelist: pricelist, name: "normal price", defaultCategory: true).save(failOnError: true)
        new BookingPriceCondition(name: "No condition", category: pc1).save(failOnError: true)

        PriceListCustomerCategory cc1 = new PriceListCustomerCategory(name: "member Customer Category", facility: facility).save(failOnError: true)
        PriceListCustomerCategory cc2 = new PriceListCustomerCategory(name: "fallback Customer Category", facility: facility).save(failOnError: true)

        new MemberTypePriceCondition(customerCategory: cc1, membershipTypes: [membershipType]).save()

        new Price(price: 100, priceCategory: pc1, customerCategory: cc1).save(failOnError: true).save() //member price
        new Price(price: 200, priceCategory: pc1, customerCategory: cc2).save(failOnError: true).save() //non-member price

        slot.court.setFacility(facility)
        assert service.getPriceForSlot(slot, customerDuplicateNotMember, [emailCustomerDuplicate, emailRandom], null).price == 200
        assert service.getPriceForSlot(slot, customerDuplicateNotMember, [emailCustomerDuplicate], null).price == 200
        assert service.getPriceForSlot(slot, customerDuplicateNotMember, [emailRandom], null).price == 200
        assert service.getPriceForSlot(slot, customerDuplicateNotMember, [emailCustomerDuplicate, emailCustomerMember], null).price == 175 // = (200+175+200+200)/4 = 175
        assert service.getPriceForSlot(slot, customerDuplicateNotMember, [emailCustomerMember, emailCustomerDuplicate], null).price == 175
        assert service.getPriceForSlot(slot, customerDuplicateNotMember, [emailRandom, emailCustomerMember], null).price == 175
        assert service.getPriceForSlot(slot, customerDuplicateNotMember, [emailCustomerMember], null).price == 175

        assert service.getPriceForSlot(slot, customerMember, [], null).price == 175
        assert service.getPriceForSlot(slot, customerDuplicateMember, [], null).price == 175

        assert service.getPriceForSlot(slot, customer0, [emailCustomerDuplicate], null).price == 175 //Will chose first customer with emailCustomerDuplicate which is a member

        assert service.getPriceForSlot(slot, customerMember, [emailCustomerMember, emailCustomerDuplicate], null).price == 150
        assert service.getPriceForSlot(slot, customerDuplicateMember, [emailCustomerDuplicate, emailCustomerDuplicate], null).price == 175
        assert service.getPriceForSlot(slot, customerDuplicateMember, [emailCustomerDuplicate], null).price == 175
        assert service.getPriceForSlot(slot, customerMember, [emailCustomerMember, emailCustomerMember], null).price == 175

        assert service.getPriceForSlot(slot, null, [emailCustomerMember, emailCustomerMember], null).price == 150
    }

    @Test
    void testGetPriceForSlot2() {
        MembershipType mtJunior = new MembershipType(id: 1, facility: facility, name: "junior", price: 0).save(flush:true, validate: false)
        MembershipType mtSenior = new MembershipType(id: 2, facility: facility, name: "senior", price: 0).save(flush:true, validate: false)

        Customer customer = createCustomer(facility, "john.doe@example.com")

        createMembership(customer, new LocalDate().plusDays(1), new LocalDate().plusDays(3), new LocalDate().plusDays(5), mtJunior, false, true)
        createMembership(customer, new LocalDate().plusDays(4), new LocalDate().plusDays(6), new LocalDate().plusDays(8), mtSenior, false, true)

        PriceList pricelist = new PriceList(facility: facility, name: "test price list", sport: sport, startDate: new LocalDate().minusDays(30), endDate: new LocalDate().plusDays(30)).save(failOnError: true)

        PriceListConditionCategory pc0 = new PriceListConditionCategory(pricelist: pricelist, name: "normal price", defaultCategory: true).save(failOnError: true)
        PriceListConditionCategory pcJ = new PriceListConditionCategory(pricelist: pricelist, name: "Junior price", defaultCategory: true).save(failOnError: true)
        PriceListConditionCategory pcS = new PriceListConditionCategory(pricelist: pricelist, name: "Senior price", defaultCategory: true).save(failOnError: true)

        new BookingPriceCondition(name: "No condition", category: pc0).save(failOnError: true)
        new BookingPriceCondition(name: "No condition", category: pcJ).save(failOnError: true)
        new BookingPriceCondition(name: "No condition", category: pcS).save(failOnError: true)

        PriceListCustomerCategory cc0 = new PriceListCustomerCategory(name: "no member", facility: facility).save(failOnError: true)
        PriceListCustomerCategory ccJ = new PriceListCustomerCategory(name: "member Junior", facility: facility).save(failOnError: true)
        PriceListCustomerCategory ccS = new PriceListCustomerCategory(name: "member Senior", facility: facility).save(failOnError: true)

        new MemberTypePriceCondition(id: 1, customerCategory: ccJ, membershipTypes: [mtJunior]).save()
        new MemberTypePriceCondition(id: 2, customerCategory: ccS, membershipTypes: [mtSenior]).save()

        new Price(price: 100, priceCategory: pc0, customerCategory: cc0).save(failOnError: true).save() //member price
        new Price(price: 80, priceCategory: pcJ, customerCategory: ccJ).save(failOnError: true).save() //member price
        new Price(price: 90, priceCategory: pcS, customerCategory: ccS).save(failOnError: true).save() //member price

        Court court = new Court(name: "Bana 2", listPosition: 1, sport: sport, facility: facility).save(validate: false)

        Slot slotNoMember =         new Slot(startTime: new DateTime().plusDays(0).toDate(), endTime: new DateTime().plusDays(0).plusHours(1).toDate(), court: court).save(validate: false)
        Slot slotJuniorMember =     new Slot(startTime: new DateTime().plusDays(2).toDate(), endTime: new DateTime().plusDays(2).plusHours(1).toDate(), court: court).save(validate: false)
        Slot slotJSMember =         new Slot(startTime: new DateTime().plusDays(5).toDate(), endTime: new DateTime().plusDays(5).plusHours(1).toDate(), court: court).save(validate: false)
        Slot slotSeniorMember =     new Slot(startTime: new DateTime().plusDays(8).toDate(), endTime: new DateTime().plusDays(8).plusHours(1).toDate(), court: court).save(validate: false)

        assert service.getPriceForSlot(slotNoMember, customer, [], null).price == 100
        assert service.getPriceForSlot(slotJuniorMember, customer, [], null).price == 100
        assert service.getPriceForSlot(slotJSMember, customer, [], null).price == 100
        assert service.getPriceForSlot(slotSeniorMember, customer, [], null).price == 100

        // Creating junior membership right now, so we should get junior member price for all future slots
        createMembership(customer, new LocalDate(), new LocalDate(), new LocalDate(), mtJunior, false, true)

        assert service.getPriceForSlot(slotNoMember, customer, [], null).price == 80
        assert service.getPriceForSlot(slotJuniorMember, customer, [], null).price == 80
        assert service.getPriceForSlot(slotJSMember, customer, [], null).price == 80
        assert service.getPriceForSlot(slotSeniorMember, customer, [], null).price == 80
    }

    @Test
    void testExistsPriceListBetween() {
        createAndSavePriceList(new DateTime("2012-07-04").toDate(), new DateTime("2012-07-06").toDate())
        assert service.getOverlappingPriceList(createPriceList(new DateTime("2012-07-04").toDate(), new DateTime("2012-07-05").toDate()))
    }

    @Test
    void testExistsPriceListStartsAfterNewlyCreated() {
        createAndSavePriceList(new DateTime("2012-07-04").toDate())
        assert service.getOverlappingPriceList(createPriceList(new DateTime("2012-07-03").toDate()))
    }

    @Test
    void testExistsPriceListStartsBeforeNewlyCreated() {
        createAndSavePriceList(new DateTime("2012-07-01").toDate())
        assert !service.getOverlappingPriceList(createPriceList(new DateTime("2012-07-03").toDate()))
    }

    @Test
    void testExistsPriceListBetweenOnSameStartDates() {
        createAndSavePriceList(new DateTime("2012-07-04").toDate(), new DateTime("2012-07-08").toDate())
        assert service.getOverlappingPriceList(createPriceList(new DateTime("2012-07-04").toDate(), new DateTime("2012-07-06").toDate()))
    }
    @Test
    void testExistsPriceListBetweenOnSameDates() {
        createAndSavePriceList(new DateTime("2012-07-04").toDate(), new DateTime("2012-07-08").toDate())
        assert service.getOverlappingPriceList(createPriceList(new DateTime("2012-07-04").toDate(), new DateTime("2012-07-08").toDate()))
    }

    @Test
    void testExistsPriceListWithStartDateBefore() {
        createAndSavePriceList(new DateTime("2012-07-04").toDate(), new DateTime("2012-07-08").toDate())
        assert service.getOverlappingPriceList(createPriceList(new DateTime("2012-07-03").toDate(), new DateTime("2012-07-07").toDate()))
    }

    @Test
    void testExistsPriceListWithDateAfter() {
        createAndSavePriceList(new DateTime("2012-07-04").toDate(), new DateTime("2012-07-08").toDate())
        assert service.getOverlappingPriceList(
                createPriceList(new DateTime("2012-07-09").toDate(), new DateTime("2012-07-10").toDate())).isEmpty()
    }

    @Test
    void testGetAvgBookingPrice() {
        slot.court.facility = facility
        slot.court.sport = sport
        slot.court.save(validate: false)
        def memberCategory = new PriceListCustomerCategory()
        memberCategory.conditions = [new MemberPriceCondition()]
        memberCategory.save(validate: false)
        def memberPrice = new Price(price: 50, priceCategory: priceList.priceListConditionCategories[0], customerCategory: memberCategory).save(validate: false)
        priceList.facility = facility
        priceList.sport = sport
        priceList.startDate = slot.startTime - 1
        priceList.subscriptions = false
        priceList.priceListConditionCategories[0].prices << memberPrice
        priceList.save(validate: false)
        def customer1 = createCustomer(facility, "test@matchi.se")
        createMembership(customer1)
        def customer2 = createCustomer(facility, "test2@matchi.se")
        createMembership(customer2)
        def customer3 = createCustomer(facility, "test3@matchi.se")
        def customer4 = createCustomer(facility, "test4@matchi.se")

        // test without MULTIPLE_PLAYERS_NUMBER
        assert 50 == service.getAvgBookingPrice(slot, [customer1]).price
        assert 75 == service.getAvgBookingPrice(slot, [customer1, new Customer()]).price
        assert 83 == service.getAvgBookingPrice(slot, [customer1, new Customer(), new Customer()]).price
        assert 87 == service.getAvgBookingPrice(slot, [customer1, new Customer(), new Customer(), new Customer()]).price
        assert 75 == service.getAvgBookingPrice(slot, [customer1, customer2, new Customer(), new Customer()]).price
        assert 80 == service.getAvgBookingPrice(slot, [customer1, customer2, new Customer(), new Customer(), new Customer()]).price

        // test with MULTIPLE_PLAYERS_NUMBER set
        facility.facilityProperties = [new FacilityProperty(key: FacilityProperty.FacilityPropertyKey.MULTIPLE_PLAYERS_NUMBER.name(),
                value: "['${sport.id}': '4']")]

        assert 87 == service.getAvgBookingPrice(slot, [customer1]).price
        assert 87 == service.getAvgBookingPrice(slot, [customer1, new Customer()]).price
        assert 87 == service.getAvgBookingPrice(slot, [customer1, new Customer(), new Customer()]).price
        assert 87 == service.getAvgBookingPrice(slot, [customer1, new Customer(), new Customer(), new Customer()]).price
        assert 90 == service.getAvgBookingPrice(slot, [customer1, new Customer(), new Customer(), new Customer(), new Customer()]).price
        assert 75 == service.getAvgBookingPrice(slot, [customer1, customer2]).price
        assert 75 == service.getAvgBookingPrice(slot, [customer1, customer2, new Customer(), new Customer()]).price
        assert 80 == service.getAvgBookingPrice(slot, [customer1, customer2, new Customer(), new Customer(), new Customer()]).price


        Map<String, Map> priceCalcLog1 = new HashMap<String, Map>()
        service.getAvgBookingPrice(slot, [customer1], priceCalcLog1)
        assert priceCalcLog1.get(slot.court.name).remainingPlayerPrice == 100
        assert priceCalcLog1.get(slot.court.name).remainingPlayers == 3

        Map<String, Map> priceCalcLog2 = new HashMap<String, Map>()
        service.getAvgBookingPrice(slot, [customer1, customer2, customer3, customer4], priceCalcLog2)
        assert priceCalcLog2.get(slot.court.name).remainingPlayerPrice == 0
        assert priceCalcLog2.get(slot.court.name).remainingPlayers == 0

        Map<String, Map> priceCalcLog3 = new HashMap<String, Map>()
        service.getAvgBookingPrice(slot, [customer1, customer2,  new Customer()], priceCalcLog3)
        assert priceCalcLog3.get(slot.court.name).remainingPlayerPrice == 100
        assert priceCalcLog3.get(slot.court.name).remainingPlayers == 2

        Map<String, Map> priceCalcLog4 = new HashMap<String, Map>()
        service.getAvgBookingPrice(slot, [customer1, customer2], priceCalcLog4)
        assert priceCalcLog4.get(slot.court.name).remainingPlayerPrice == 100
        assert priceCalcLog4.get(slot.court.name).remainingPlayers == 2
    }

    private PriceList createAndSavePriceList(Date from, Date to = null) {
        PriceList pl = createPriceList(from, to)
        pl.save(failOnError: true)

        return pl
    }

    private PriceList createPriceList(Date from, Date to = null) {
        PriceList pl = new PriceList()
        pl.startDate = dateUtil.beginningOfDay(from).toDate()
        pl.name = "TestPriceList"
        pl.facility = facility
        pl.sport = sport
        pl
    }
}
