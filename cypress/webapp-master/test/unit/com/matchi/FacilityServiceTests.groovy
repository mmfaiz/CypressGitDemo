package com.matchi

import com.matchi.facility.Organization
import com.matchi.fortnox.v3.FortnoxArticle
import com.matchi.subscriptionredeem.redeemstrategy.InvoiceRowRedeemStrategy
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.codehaus.groovy.grails.plugins.codecs.MD5Codec
import org.joda.time.LocalTime
import org.junit.Before
import org.junit.Test

import static com.matchi.TestUtils.*
import static org.hamcrest.Matchers.*
import static org.junit.Assert.*

@TestFor(FacilityService)
@Mock([Facility, Availability, Court, Customer, User, Municipality, Region, Organization, InvoiceRowRedeemStrategy])
class FacilityServiceTests {

    Facility fac1
    Facility fac2
    User user

    @Before
    void setUp() {
        Availability av1 = new Availability(begin: new LocalTime().withHourOfDay(7), end: new LocalTime().withHourOfDay(23), weekday: 1)
        Availability av2 = new Availability(begin: new LocalTime().withHourOfDay(6), end: new LocalTime().withHourOfDay(22), weekday: 1)

        fac1 = new Facility(name:"GLTK", enabled: true, active: true, shortname:  "gltk", bookable: true)
        fac1.addToAvailabilities(av1)
        fac1.save(validate: false)
        fac2 = new Facility(name:"Ullevi TK",enabled: true, active: true, shortname:  "ullevi", bookable: true)
        fac2.addToAvailabilities(av2)
        fac2.save(validate: false)

        Court court1 = new Court(facility: fac1, restriction: Court.Restriction.NONE, listPosition: 1).save(validate: false)
        Court court2 = new Court(facility: fac1, restriction: Court.Restriction.NONE, listPosition: 2).save(validate: false)

        Court court3 = new Court(facility: fac2, restriction: Court.Restriction.MEMBERS_ONLY, listPosition: 1).save(validate: false)
        Court court4 = new Court(facility: fac2, restriction: Court.Restriction.MEMBERS_ONLY, listPosition: 2).save(validate: false)

        user = new User().save(validate: false);

        mockCodec(MD5Codec)
    }

    @Test
    void testGetEarliestOpeningHour() {
        def facilities = Facility.list()

        assertEquals 6, service.getEarliestOpeningHour(facilities)
    }

    @Test
    void testGetLatestClosingHour() {
        def facilities = Facility.list()

        assertEquals 23, service.getLatestClosingHour(facilities)
    }

    @Test
    void testGetFacilityFromPublicRegistrationCode() {
        assert service.getFacilityFromPublicRegistrationCode("a0ff9e450ef7ddf9bcf617b38273b3c4") != null
    }

    @Test
    void testNullFacilityWhenCodeDoesNotMatch() {
        assert service.getFacilityFromPublicRegistrationCode("notexists4") == null
    }

    @Test
    void testNullFacilityWhenCodeIsNull() {
        assert service.getFacilityFromPublicRegistrationCode(null) == null
    }

    @Test
    void testGetUserAvailableFacilitiesReturnsCorrectFacilities() {
        def facilities = service.getUsersAvailableFacilities(user)

        assert facilities.size() == 1
        assert facilities[0].name == fac1.name
    }

    @Test
    void testUpdateAvailability() {
        def facility = createFacility()
        def params = [
                fromMinute_1: "0", toMinute_1: "1440", active_1: true,
                fromMinute_2: "30", toMinute_2: "1410", active_2: true,
                fromMinute_3: "60", toMinute_3: "1380", active_3: true,
                fromMinute_4: "90", toMinute_4: "1350", active_4: true,
                fromMinute_5: "120", toMinute_5: "1320", active_5: true,
                fromMinute_6: "150", toMinute_6: "1290", active_6: true,
                fromMinute_7: "180", toMinute_7: "1260", active_7: false
        ]

        service.updateAvailability(facility, params)

        assert 7 == facility.availabilities.size()
        def av = facility.availabilities.find {it.weekday == 1}
        assert "00:00:00.000" == av.begin.toString()
        assert "23:59:59.000" == av.end.toString()
        assert av.active
        av = facility.availabilities.find {it.weekday == 2}
        assert "00:30:00.000" == av.begin.toString()
        assert "23:30:00.000" == av.end.toString()
        assert av.active
        av = facility.availabilities.find {it.weekday == 3}
        assert "01:00:00.000" == av.begin.toString()
        assert "23:00:00.000" == av.end.toString()
        assert av.active
        av = facility.availabilities.find {it.weekday == 4}
        assert "01:30:00.000" == av.begin.toString()
        assert "22:30:00.000" == av.end.toString()
        assert av.active
        av = facility.availabilities.find {it.weekday == 5}
        assert "02:00:00.000" == av.begin.toString()
        assert "22:00:00.000" == av.end.toString()
        assert av.active
        av = facility.availabilities.find {it.weekday == 6}
        assert "02:30:00.000" == av.begin.toString()
        assert "21:30:00.000" == av.end.toString()
        assert av.active
        av = facility.availabilities.find {it.weekday == 7}
        assert "03:00:00.000" == av.begin.toString()
        assert "21:00:00.000" == av.end.toString()
        assert !av.active
    }

    @Test
    void testGetFacilityOrganizations() {
        def facility = createFacility()
        def organization1 = createOrganization(facility)
        def organization2 = createOrganization(facility)
        def organization3 = createOrganization(createFacility())

        def organizations = service.getFacilityOrganizations(facility)

        assert 2 == organizations.size()
        assert organizations.find { it == organization1 }
        assert organizations.find { it == organization2 }
        assert !organizations.find { it == organization3 }
    }

    @Test
    void testCollectArticles() {
        def facility = createFacility()
        def organizationForBooking = createOrganization(facility)
        def organizationForInvoiceFee = createOrganization(facility)
        def organizationForRedeem = createOrganization(facility)
        facility.bookingInvoiceRowOrganizationId = organizationForBooking.id
        facility.invoiceFeeOrganizationId = organizationForInvoiceFee.id
        facility.save(failOnError: true, flush: true)
        def invoiceRedeemStrategy = createInvoiceRowRedeemStrategy()
        invoiceRedeemStrategy.organizationId = organizationForRedeem.id
        invoiceRedeemStrategy.save(failOnError: true, flush: true)
        def facilityArticles = [new FortnoxArticle(ArticleNumber: "1"), new FortnoxArticle(ArticleNumber: "2")]
        def organizationArticles = [new FortnoxArticle(ArticleNumber: "3"), new FortnoxArticle(ArticleNumber: "4"), new FortnoxArticle(ArticleNumber: "5")]

        def invoiceServiceControl = mockFor(InvoiceService)
        invoiceServiceControl.demand.getItems { fId -> return facilityArticles}
        invoiceServiceControl.demand.getItemsForOrganization(0..3) { oId -> return [organizationArticles[oId.toInteger() - 1]] }
        service.invoiceService = invoiceServiceControl.createMock()

        def collectionArticles = service.collectArticles(facility, invoiceRedeemStrategy)

        assert 4 == collectionArticles.size()
        assert [organizationArticles[0]] == collectionArticles["bookingInvoiceRowArticles"]
        assert [organizationArticles[1]] == collectionArticles["invoiceFeeExternalArticles"]
        assert [organizationArticles[2]] == collectionArticles["redeemInvoiceRowArticles"]
        assert facilityArticles == collectionArticles["invoiceFeeArticles"]
        invoiceServiceControl.verify()
    }

    @Test
    void testList() {
        def actualFacilities = service.list()

        assert actualFacilities.size() == 2 //in setup method
        assertThat(actualFacilities, containsInAnyOrder(fac1, fac2))
    }

    @Test
    void testListActive() {
        def inactiveFacility = new Facility(name:"Old padel center", active: false, shortname:  "OPC", bookable: true)
        inactiveFacility.save(validate: false)

        def actualFacilities = service.listActive()

        assert actualFacilities.size() == 2
        assertThat(actualFacilities, containsInAnyOrder(fac1, fac2))
        assertThat(actualFacilities, not(contains(inactiveFacility)))
    }

}
