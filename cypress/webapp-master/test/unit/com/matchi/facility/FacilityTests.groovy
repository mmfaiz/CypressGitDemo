package com.matchi.facility

import com.matchi.*
import com.matchi.FacilityProperty.FacilityPropertyKey
import com.matchi.async.ScheduledTask
import com.matchi.enums.MembershipRequestSetting
import com.matchi.orders.Order
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.codehaus.groovy.grails.plugins.codecs.MD5Codec
import org.joda.time.DateTime
import org.joda.time.LocalTime
import org.junit.Before
import org.junit.Test

import static com.matchi.TestUtils.*

@TestFor(Facility)
@Mock([Facility, FacilityProperty, Court, Availability, Customer, Sport, Organization,
        Municipality, Region, ScheduledTask, Season])
class FacilityTests {

	Facility facility
    Availability availability
    def mockedConfig

    Sport tennis
    Sport padel

    @Before
    void setUp() {
        availability = new Availability(weekday: 1, active: true)
		facility = new Facility()
        facility.addToAvailabilities(availability)

        mockedConfig = new ConfigObject()

        def existingFacility = _createValidFacility()
        existingFacility.shortname = "test-1"
        mockForConstraintsTests(Facility, [existingFacility])
        mockCodec(MD5Codec)

        tennis = new Sport(id: 1l, name: "Tennis", position: 0)
        padel = new Sport(id: 2l, name: "Padel", position: 1)

        facility.addToSports(tennis)
        facility.addToSports(padel)

    }
    @Test
    void testValidFacility() {
        def facility = _createValidFacility()
        assert facility.validate()
    }
    @Test
    void testOpenHoursReturnNullWithAvailabilityNotActive() {
        facility.removeFromAvailabilities(availability)

        assert !facility.getOpeningHour(1)
    }
    @Test
    void testOpenHoursReturnNullWithNoAvailability() {
        availability.active = false

        assert !facility.getOpeningHour(1)
    }
    @Test
    void testClosingHoursReturnNullWithAvailabilityNotActive() {
        facility.removeFromAvailabilities(availability)

        assert !facility.getClosingHour(1)
    }
    @Test
    void testClosingHoursReturnNullWithNoAvailability() {
        availability.active = false

        assert !facility.getClosingHour(1)
    }
    @Test
    void testOpeningHours() {
		availability.begin = new LocalTime().withHourOfDay(11)
		assertEquals 11, facility.getOpeningHour(1)
    }
    @Test
	void testOpeningHoursWithZero() {
        availability.begin = new LocalTime().withHourOfDay(9)
		assertEquals 9, facility.getOpeningHour(1)
	}
    @Test
	void testClosingHours() {
        availability.end = new LocalTime().withHourOfDay(10)
		assertEquals 10, facility.getClosingHour(1)
	}
    @Test
	void testClosingHoursWithZero() {
        availability.end = new LocalTime().withHourOfDay(9)
		assertEquals 9, facility.getClosingHour(1)
	}
    @Test
    void testPublicCode() {
        facility.shortname = "gltk"
        println facility.getRegistrationCode()
    }

    @Test
    void testIsMembersOnly() {
        def f = new Facility().addToCourts(new Court())
        assert !f.isMembersOnly()

        f = new Facility().addToCourts(new Court(restriction: Court.Restriction.MEMBERS_ONLY))
        assert f.isMembersOnly()

        f = new Facility().addToCourts(new Court()).addToCourts(new Court(restriction: Court.Restriction.MEMBERS_ONLY))
        assert !f.isMembersOnly()

        f = new Facility().addToCourts(new Court(restriction: Court.Restriction.MEMBERS_ONLY)).addToCourts(new Court(restriction: Court.Restriction.OFFLINE_ONLY))
        assert f.isMembersOnly()

        f = new Facility().addToCourts(new Court()).addToCourts(new Court(restriction: Court.Restriction.OFFLINE_ONLY))
        assert !f.isMembersOnly()
    }

    @Test
    void testGetNextCustomerNumberReturnsCorrectNumber() {

        assert facility.getNextCustomerNumber() == 1

        def numberToAdd = 1
        _createCustomer(numberToAdd)
        assert facility.getNextCustomerNumber() == 2

        numberToAdd = 3
        _createCustomer(numberToAdd)
        assert facility.getNextCustomerNumber() == 4

    }

    @Test
    void testIsBookableReturnsTrueIfBookableSameDate() {
        facility.bookingRuleNumDaysBookable = 3
        assert facility.isBookableForUser(new DateTime().plusDays(3), null)
    }

    @Test
    void testIsBookableReturnsTrueIfEarlier() {
        facility.bookingRuleNumDaysBookable = 10
        assert facility.isBookableForUser(new DateTime().plusDays(5), null)
    }

    @Test
    void testIsBookableReturnsTrueIfNumDaysIsZero() {
        facility.bookingRuleNumDaysBookable = 0
        assert facility.isBookableForUser(new DateTime().plusDays(30), null)
    }

    @Test
    void testIsBookableReturnsFalseIfAfterLastBookableDate() {
        facility.bookingRuleNumDaysBookable = 3
        assert !facility.isBookableForUser(new DateTime().plusDays(4), null)
    }

    @Test
    void testIsBookableIfStartsAtMidnight() {
        facility.bookingRuleNumDaysBookable = 1
        assert facility.isBookableForUser(new DateTime().toDateMidnight(), null)
        assert facility.isBookableForUser(new DateTime().plusDays(1).toDateMidnight(), null)
        assert !facility.isBookableForUser(new DateTime().plusDays(2).toDateMidnight(), null)
        assert !facility.isBookableForUser(new DateTime().plusDays(3).toDateMidnight(), null)
    }

    @Test
    void testSetFacilityProperty() {
        facility.setFacilityProperty(FacilityProperty.FacilityPropertyKey.INVOICE_NUMBER_START, "10")

        assert facility.getFacilityPropertyValue(FacilityProperty.FacilityPropertyKey.INVOICE_NUMBER_START.toString()) == "10"
    }

    @Test
    void testGetFacilityPropertyDefaultValue() {
        assert facility.getFacilityPropertyValue("INVOICE_NUMBER_START") == "0"
    }

    @Test
    void testGetDefaultSportReturnsCorrectSport() {
        def tennisCourt1 = new Court(sport: tennis)
        def tennisCourt2 = new Court(sport: tennis)
        def padelCourt1 = new Court(facility: facility, sport: padel)

        facility.addToCourts(tennisCourt1)
        facility.addToCourts(tennisCourt2)
        facility.addToCourts(padelCourt1)

        assert facility.getDefaultSport() == tennis
    }

    @Test
    void testGetDefaultSportReturnsASportIfEqualNr() {
        def tennisCourt1 = new Court(sport: tennis)
        def padelCourt1 = new Court(facility: facility, sport: padel)

        facility.addToCourts(tennisCourt1)
        facility.addToCourts(padelCourt1)

        assert facility.getDefaultSport() != null
    }

    @Test
    void testGetDefaultSportReturnsNullIfNoCourt() {
        assert facility.getDefaultSport() == null
    }


    @Test
    void testIsLateCancellationReturnsTrueIfInFuture() {
        assert !facility.isLateCancellation(new DateTime().plusHours(100))
    }

    @Test
    void testIsLateCancellationReturnsFalse() {
        assert !facility.isLateCancellation(new DateTime().plusHours(7))
    }

    @Test
    void testIsLateCancellationReturnsTrue() {
        assert facility.isLateCancellation(new DateTime().plusHours(5))
    }

    @Test
    void testIsLateCancellationReturnsTrueIfYesterday() {
        assert facility.isLateCancellation(new DateTime().minusDays(1))
    }

    @Test
    void testIsLateCancellationReturnsTrueIfInFuture24Hours() {
        // given
        facility.facilityProperties = [new FacilityProperty(
                key: FacilityProperty.FacilityPropertyKey.BOOKING_CANCELLATION_LIMIT.toString(),
                value: "24")]

        assert !facility.isLateCancellation(new DateTime().plusHours(100))
    }

    @Test
    void testIsLateCancellationReturnsFalse24Hours() {
        // given
        facility.facilityProperties = [new FacilityProperty(
                key: FacilityProperty.FacilityPropertyKey.BOOKING_CANCELLATION_LIMIT.toString(),
                value: "24")]

        assert !facility.isLateCancellation(new DateTime().plusHours(25))
    }

    @Test
    void testIsLateCancellationReturnsTrue24Hours() {
        // given
        facility.facilityProperties = [new FacilityProperty(
                key: FacilityProperty.FacilityPropertyKey.BOOKING_CANCELLATION_LIMIT.toString(),
                value: "24")]

        assert facility.isLateCancellation(new DateTime().plusHours(23))
    }

    @Test
    void testIsLateCancellationReturnsTrueIfYesterday24Hours() {
        // given
        facility.facilityProperties = [new FacilityProperty(
                key: FacilityProperty.FacilityPropertyKey.BOOKING_CANCELLATION_LIMIT.toString(),
                value: "24")]

        assert facility.isLateCancellation(new DateTime().minusDays(1))
    }

    @Test
    void testGetRefundableInPercentageWithPropertyDefaultLimit() {

        // given
        facility.facilityProperties = [new FacilityProperty(
                key: FacilityProperty.FacilityPropertyKey.BOOKING_LATE_REFUND_PERCENTAGE.toString(),
                value: "50")]

        // late cancellation
        assert 50 == facility.getRefundPercentage(new DateTime().plusHours(1))

        // in time cancellation
        assert 100 == facility.getRefundPercentage(new DateTime().plusHours(10))
    }

    @Test
    void testGetRefundableInPercentageWithPropertyExtraLimit() {

        // given
        facility.facilityProperties = [new FacilityProperty(
                key: FacilityProperty.FacilityPropertyKey.BOOKING_CANCELLATION_LIMIT.toString(),
                value: "24"), new FacilityProperty(
                key: FacilityProperty.FacilityPropertyKey.BOOKING_LATE_REFUND_PERCENTAGE.toString(),
                value: "50")]

        // late cancellation
        assert 50 == facility.getRefundPercentage(new DateTime().plusHours(20))

        // in time cancellation
        assert 100 == facility.getRefundPercentage(new DateTime().plusHours(30))
    }

    @Test
    void testGetRefundableInPercentageWithoutProperty() {

        // given no facility specific option
        // late cancellation
        assert 0 == facility.getRefundPercentage(new DateTime().plusHours(1))

        // in time cancellation
        assert 100 == facility.getRefundPercentage(new DateTime().plusHours(10))
    }

    @Test
    void testGetRefundableInPercentageWithoutPropertyWithExtraLimit() {

        // given
        facility.facilityProperties = [new FacilityProperty(
                key: FacilityProperty.FacilityPropertyKey.BOOKING_CANCELLATION_LIMIT.toString(),
                value: "24")]

        // given no facility specific option
        // late cancellation
        assert 0 == facility.getRefundPercentage(new DateTime().plusHours(20))

        // in time cancellation
        assert 100 == facility.getRefundPercentage(new DateTime().plusHours(30))
    }

    @Test
    void testGetCourtInfoReturnsCorrectInfo() {
        def tennisCourt1 = new Court(sport: tennis, indoor: true)
        def tennisCourt2 = new Court(sport: tennis, indoor: false)
        def padelCourt1 = new Court(sport: padel, indoor: true)

        facility.addToCourts(tennisCourt1)
        facility.addToCourts(tennisCourt2)
        facility.addToCourts(padelCourt1)

        assert facility.getSportsGroupedByIndoor().size() == 3
    }

    @Test
    void testIsOnlyOutDoorReturnsFalseIfIndoorCourts() {
        def tennisCourt1 = new Court(sport: tennis, indoor: false)
        def tennisCourt2 = new Court(sport: tennis, indoor: false)
        def padelCourt1 = new Court(sport: padel, indoor: true)

        facility.addToCourts(tennisCourt1)
        facility.addToCourts(tennisCourt2)
        facility.addToCourts(padelCourt1)

        assert !facility.isOnlyOutDoor()
    }

    @Test
    void testIsOnlyOutDoorReturnsTrueIfNoIndoorCourts() {
        def tennisCourt1 = new Court(sport: tennis, indoor: false)
        def tennisCourt2 = new Court(sport: tennis, indoor: false)
        def padelCourt1 = new Court(sport: padel, indoor: false)

        facility.addToCourts(tennisCourt1)
        facility.addToCourts(tennisCourt2)
        facility.addToCourts(padelCourt1)

        assert facility.isOnlyOutDoor()
    }

    @Test
    void testIsSportOnlyOutDoorReturnsCorrectValue() {
        def tennisCourt1 = new Court(sport: tennis, indoor: false)
        def tennisCourt2 = new Court(sport: tennis, indoor: false)
        def padelCourt1 = new Court(sport: padel, indoor: false)
        def padelCourt2 = new Court(sport: padel, indoor: true)

        facility.addToCourts(tennisCourt1)
        facility.addToCourts(tennisCourt2)
        facility.addToCourts(padelCourt1)
        facility.addToCourts(padelCourt2)

        assert facility.isSportOnlyOutDoor(tennis)
        assert !facility.isSportOnlyOutDoor(padel)
    }

    @Test
    void testHasOrganization() {
        def featureOrganization = new FacilityProperty(facility: facility, key: FacilityProperty.FacilityPropertyKey.FEATURE_ORGANIZATIONS.name(), value: "1").save(flush: true)
        facility.facilityProperties = [featureOrganization]
        facility.save(flush: true)
        def organization = createOrganization(facility)

        assert facility.hasOrganization()

        organization.delete(flush: true)

        assert !facility.hasOrganization()

        createOrganization(facility)
        facility.removeFromFacilityProperties(featureOrganization)
        featureOrganization.delete(flush: true)
        facility.save(flush: true)

        assert !facility.hasOrganization()
    }

    @Test
    void testFeatureProperties() {
        // given
        facility.facilityProperties = [
                new FacilityProperty(key: FacilityProperty.FacilityPropertyKey.FEATURE_MPC.toString(), value: "0"),
                new FacilityProperty(key: FacilityProperty.FacilityPropertyKey.FEATURE_FEDERATION.toString(), value: "1")]

        assert !facility.isFacilityPropertyTrue(FacilityProperty.FacilityPropertyKey.FEATURE_MPC.toString())
        assert !facility.isFacilityPropertyTrue(FacilityProperty.FacilityPropertyKey.FEATURE_MPC)
        assert !facility.isFacilityPropertyTrue(FacilityProperty.FacilityPropertyKey.FEATURE_QUEUE.toString())
        assert !facility.isFacilityPropertyTrue(FacilityProperty.FacilityPropertyKey.FEATURE_QUEUE)
        assert facility.isFacilityPropertyTrue(FacilityProperty.FacilityPropertyKey.FEATURE_FEDERATION.toString())
        assert facility.isFacilityPropertyTrue(FacilityProperty.FacilityPropertyKey.FEATURE_FEDERATION)
    }


    @Test
    void testHasFortnox() {
        assert !facility.hasFortnox()

        facility.facilityProperties = [
                new FacilityProperty(key: FacilityProperty.FacilityPropertyKey.FORTNOX3_ACCESS_TOKEN.toString(), value: "d634b1bf-dd7e-4881-ad2c-a50beac97bf3")
        ]

        assert facility.hasFortnox()
    }

    @Test
    void testHasPersonalAccessCodes() {
        facility.facilityProperties = []

        assert !facility.hasPersonalAccessCodes()

        facility.facilityProperties = [
                new FacilityProperty(key: FacilityProperty.FacilityPropertyKey.FEATURE_PERSONAL_ACCESS_CODE.toString(), value: "1")]

        assert facility.hasPersonalAccessCodes()
    }

    @Test
    void testHasSubscriptionAccessCodes() {
        facility.facilityProperties = []

        assert !facility.hasSubscriptionAccessCode()

        facility.facilityProperties = [
                new FacilityProperty(key: FacilityProperty.FacilityPropertyKey.FEATURE_SUBSCRIPTION_ACCESS_CODE.toString(), value: "1")]

        assert facility.hasSubscriptionAccessCode()
    }

    @Test
    void testMpcWarningEmailsNoneSet() {
        Facility facility = createFacility()
        facility.email = 'info@matchi.se'

        assert facility.getMpcNotificationMails().size() == 1
        assert facility.getMpcNotificationMails().contains(facility.email)
    }

    @Test
    void testMpcWarningEmailsSet() {
        Facility facility = createFacility()
        facility.email = "info@matchi.se"

        String linkMail = "link@matchi.se"
        String zeldaMail = "zelda@matchi.se"

        facility.facilityProperties = [
                new FacilityProperty(key: FacilityProperty.FacilityPropertyKey.MPC_NOTIFY_EMAIL_ADDRESSES.toString(), value: "${linkMail},${zeldaMail}")
        ]

        assert facility.getMpcNotificationMails().size() == 2
        assert facility.getMpcNotificationMails().contains(linkMail)
        assert facility.getMpcNotificationMails().contains(zeldaMail)
    }

    @Test
    void testGetMultiplePlayersNumber() {
        def facility = createFacility()

        assert facility.getMultiplePlayersNumber() != null
        assert facility.getMultiplePlayersNumber().isEmpty()

        facility.facilityProperties = [
                new FacilityProperty(key: FacilityProperty.FacilityPropertyKey.MULTIPLE_PLAYERS_NUMBER.name(),
                        value: "['1': '2', '2': '4', '3': '']")
        ]

        def n = facility.getMultiplePlayersNumber()
        assert n.size() == 3
        assert n['1'] == '2'
        assert n['2'] == '4'
        assert n['3'] == ''
    }

    @Test
    void testRemotePaymentMethods() {
        Facility facility = createFacility()
        assert !facility.hasEnabledRemotePaymentsFor(Order.Article.MEMBERSHIP)
        assert !facility.hasEnabledRemotePaymentsFor(Order.Article.BOOKING)
        assert !facility.hasEnabledRemotePaymentsFor(null)

        assert !facility.hasEnabledRemotePayments()
        assert !facility.getRemotePaymentArticles()

        facility.facilityProperties = [
                new FacilityProperty(key: FacilityProperty.FacilityPropertyKey.FEATURE_REMOTE_PAYMENT_MEMBERSHIP.name(), value: "1")
        ]

        assert facility.hasEnabledRemotePaymentsFor(Order.Article.MEMBERSHIP)
        assert !facility.hasEnabledRemotePaymentsFor(Order.Article.BOOKING)
        assert !facility.hasEnabledRemotePaymentsFor(null)
        assert facility.hasEnabledRemotePayments()
        assert facility.getRemotePaymentArticles() == [Order.Article.MEMBERSHIP]

        facility.facilityProperties = [
                new FacilityProperty(key: FacilityProperty.FacilityPropertyKey.FEATURE_REMOTE_PAYMENT_MEMBERSHIP.name(), value: "1"),
                new FacilityProperty(key: FacilityProperty.FacilityPropertyKey.FEATURE_REMOTE_PAYMENT_BOOKING.name(), value: "0")
        ]

        assert facility.hasEnabledRemotePaymentsFor(Order.Article.MEMBERSHIP)
        assert !facility.hasEnabledRemotePaymentsFor(Order.Article.BOOKING)
        assert !facility.hasEnabledRemotePaymentsFor(null)
        assert facility.hasEnabledRemotePayments()
        assert facility.getRemotePaymentArticles() == [Order.Article.MEMBERSHIP]

        facility.facilityProperties = [
                new FacilityProperty(key: FacilityProperty.FacilityPropertyKey.FEATURE_REMOTE_PAYMENT_BOOKING.name(), value: "1")
        ]

        assert !facility.hasEnabledRemotePaymentsFor(Order.Article.MEMBERSHIP)
        assert facility.hasEnabledRemotePaymentsFor(Order.Article.BOOKING)
        assert !facility.hasEnabledRemotePaymentsFor(null)
        assert facility.hasEnabledRemotePayments()
        assert facility.getRemotePaymentArticles() == [Order.Article.BOOKING]

        facility.facilityProperties = [
                new FacilityProperty(key: FacilityProperty.FacilityPropertyKey.FEATURE_REMOTE_PAYMENT_BOOKING.name(), value: "1"),
                new FacilityProperty(key: FacilityProperty.FacilityPropertyKey.FEATURE_REMOTE_PAYMENT_MEMBERSHIP.name(), value: "0")
        ]

        assert !facility.hasEnabledRemotePaymentsFor(Order.Article.MEMBERSHIP)
        assert facility.hasEnabledRemotePaymentsFor(Order.Article.BOOKING)
        assert !facility.hasEnabledRemotePaymentsFor(null)
        assert facility.hasEnabledRemotePayments()
        assert facility.getRemotePaymentArticles() == [Order.Article.BOOKING]

        facility.facilityProperties = [
                new FacilityProperty(key: FacilityProperty.FacilityPropertyKey.FEATURE_REMOTE_PAYMENT_BOOKING.name(), value: "1"),
                new FacilityProperty(key: FacilityProperty.FacilityPropertyKey.FEATURE_REMOTE_PAYMENT_MEMBERSHIP.name(), value: "1")
        ]

        assert facility.hasEnabledRemotePaymentsFor(Order.Article.MEMBERSHIP)
        assert facility.hasEnabledRemotePaymentsFor(Order.Article.BOOKING)
        assert !facility.hasEnabledRemotePaymentsFor(null)
        assert facility.hasEnabledRemotePayments()
        assert facility.getRemotePaymentArticles() == [Order.Article.BOOKING, Order.Article.MEMBERSHIP]
    }

    void testIsFamilyMembershipRequestAllowed() {
        def facility = createFacility()

        assert !facility.familyMembershipRequestAllowed

        facility.recieveMembershipRequests = true
        facility.facilityProperties = [
                new FacilityProperty(key: FacilityPropertyKey.FEATURE_MEMBERSHIP_REQUEST_PAYMENT.name(), value: "1"),
                new FacilityProperty(key: FacilityPropertyKey.FEATURE_USE_FAMILY_MEMBERSHIPS.name(), value: "1")
        ]

        assert facility.familyMembershipRequestAllowed

        facility.recieveMembershipRequests = false

        assert !facility.familyMembershipRequestAllowed

        facility.recieveMembershipRequests = true
        facility.facilityProperties = [
                new FacilityProperty(key: FacilityPropertyKey.FEATURE_MEMBERSHIP_REQUEST_PAYMENT.name(), value: "1")
        ]

        assert !facility.familyMembershipRequestAllowed

        facility.facilityProperties = [
                new FacilityProperty(key: FacilityPropertyKey.FEATURE_USE_FAMILY_MEMBERSHIPS.name(), value: "1")
        ]

        assert !facility.familyMembershipRequestAllowed
    }

    void testGetFamilyMaxPrice() {
        def facility = createFacility()

        assert facility.getFamilyMaxPrice() == Long.MAX_VALUE

        facility.facilityProperties = [
                new FacilityProperty(key: FacilityPropertyKey.FACILITY_MEMBERSHIP_FAMILY_MAX_AMOUNT.name(), value: "12345")
        ]

        assert facility.getFamilyMaxPrice() == 12345L
    }

    void testIsMembershipStartingGracePeriodEnabled() {
        assert !new Facility().isMembershipStartingGracePeriodEnabled()
        assert !new Facility(recieveMembershipRequests: false, membershipStartingGraceNrOfDays: 30,
                membershipRequestSetting: MembershipRequestSetting.DIRECT)
                .isMembershipStartingGracePeriodEnabled()
        assert !new Facility(recieveMembershipRequests: true, membershipStartingGraceNrOfDays: 0,
                membershipRequestSetting: MembershipRequestSetting.DIRECT)
                .isMembershipStartingGracePeriodEnabled()
        assert !new Facility(recieveMembershipRequests: true, membershipStartingGraceNrOfDays: null,
                membershipRequestSetting: MembershipRequestSetting.DIRECT)
                .isMembershipStartingGracePeriodEnabled()
        assert !new Facility(recieveMembershipRequests: true, membershipStartingGraceNrOfDays: 30,
                membershipRequestSetting: MembershipRequestSetting.MANUAL)
                .isMembershipStartingGracePeriodEnabled()
        assert new Facility(recieveMembershipRequests: true, membershipStartingGraceNrOfDays: 30,
                membershipRequestSetting: MembershipRequestSetting.DIRECT)
                .isMembershipStartingGracePeriodEnabled()
    }

    void testIsAnySeasonUpdating() {
        def facility = createFacility()
        def season1 = createSeason(facility)
        def season2 = createSeason(facility)

        assert !facility.isAnySeasonUpdating()

        new ScheduledTask(facility: facility, name: "task1",
                relatedDomainClass: Season.class.simpleName, domainIdentifier: season1.id,
                isTaskFinished: true).save(failOnError: true)
        new ScheduledTask(facility: facility, name: "task2",
                relatedDomainClass: Season.class.simpleName, domainIdentifier: season2.id,
                isTaskFinished: false).save(failOnError: true)

        assert facility.isAnySeasonUpdating()
    }

    private def _createValidFacility() {
        Facility f = new Facility()
        f.name = "Test facility"
        f.shortname = "test"
        f.lat = 12.13d
        f.lng = 14.12d
        f.vat = 25
        f.municipality = new Municipality()
        f.country = "sv"
        f.email = "facility@matchi.se"

        f
    }

    private def _createCustomer(def number) {
        def customer = new Customer()
        customer.number = number
        facility.addToCustomers(customer)
        customer.save(validate: false)

        return customer
    }
}
