package com.matchi.facility

import com.matchi.*
import com.matchi.activities.trainingplanner.Trainer
import com.matchi.coupon.Offer
import grails.plugin.springsecurity.SpringSecurityService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.junit.Before
import spock.lang.Specification

import static com.matchi.TestUtils.createFacility

@TestFor(FacilityController)
@Mock([Facility, FacilityProperty, Offer, Trainer, Customer])
class FacilityControllerTests extends Specification {

    def springSecurityService = Mock(SpringSecurityService)
    def activityService = Mock(ActivityService)
    def courtService = Mock(CourtService)
    def memberService = mockFor(MemberService)

    @Before
    void setup() {
        new Facility(name: "Test 1", shortname: 'test1', enabled: false, active: true).save(validate: false)
        new Facility(name: "Test 2", shortname: 'test2', enabled: false, active: true).save(validate: false)
        new Facility(name: "no_default_sport", shortname: 'no_default_sport', enabled: true, active: true).save(validate: false)

        def facility = new Facility(name: "default_sport_2", shortname: 'default_sport_2', enabled: true, active: true)
        facility.save(validate: false)
        def prop = new FacilityProperty(key: FacilityProperty.FacilityPropertyKey.FACILITY_DEFAULT_SPORT.name(), value: '2', facility: facility)
        prop.save()

        memberService.demand.getMembership() { -> null }
        memberService.demand.isUpcomingMembershipAvailableForPurchase() { -> null }
        memberService.demand.getActiveMemberships() { a,b -> [] }
        memberService.demand.getUnpaidStartedMemberships() { a,b -> [] }
        memberService.demand.getRemotelyPayableMemberships() { a,b -> [] }
        memberService.demand.getUpcomingMemberships() { a,b -> [] }
        memberService.demand.getActiveActivitiesWithOccasions() { -> null }
        memberService.demand.getCoursesWithPublishedForm() { -> null }
        memberService.demand.getOnlineEvents() { -> null }

        controller.springSecurityService = springSecurityService
        controller.activityService = activityService
        controller.courtService = courtService
        controller.memberService = memberService.createMock()
    }

    void testFacityNotEnabled() {
        when:
        params.name = "test2"
        controller.show()

        then:
        response.redirectedUrl == "/facilities/index"
    }

    void testSportIdInCorrectFormatParsed() {

        when:
        params.sport = "1"
        params.name = "no_default_sport"

        def value = controller.show()

        then:
        value['sport'] == 1
    }

    void testWrongSportIdFormatReturns0IfNoDefaultSportConfigured() {

        when:
        params.sport = "test2"
        params.name = "no_default_sport"
        def value = controller.show()

        then:
        value['sport'] == 0
    }

    void testWrongSportIdFormatReturnsDefaultSport() {

        when:
        params.sport = "test2"
        params.name = "default_sport_2"
        def mockSport = mockFor(Sport)
        def mockDefaultSport = new Sport()
        mockDefaultSport.metaClass.id = 2
        mockSport.demand.static.findById(1) { Long id -> return mockDefaultSport }
        def value = controller.show()

        then:
        value['sport'] == 2
    }

    void testSportParamNotStringReturnsDefaultSport() {

        when:
        params.sport = ["test2"]
        params.name = "default_sport_2"
        def mockSport = mockFor(Sport)
        def mockDefaultSport = new Sport()
        mockDefaultSport.metaClass.id = 2
        mockSport.demand.static.findById(1) { Long id -> return mockDefaultSport }
        def value = controller.show()

        then:
        value['sport'] == 2
    }
}
