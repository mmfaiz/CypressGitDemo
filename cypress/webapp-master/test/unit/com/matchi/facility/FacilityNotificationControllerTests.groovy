package com.matchi.facility

import com.matchi.*
import com.matchi.messages.FacilityMessage
import com.matchi.messages.FacilityMessage.Channel
import grails.test.mixin.Mock
import grails.test.mixin.TestFor

import javax.servlet.http.HttpServletResponse

import static com.matchi.TestUtils.*

/**
 * @author Sergei Shushkevich
 */
@TestFor(FacilityNotificationController)
@Mock([Facility, FacilityMessage, Municipality, Region])
class FacilityNotificationControllerTests {

    void testIndex() {
        def facility = createFacility()
        def fn = new FacilityMessage(headline: "title", content: "text",
                validFrom: new Date(), validTo: new Date() + 1, channel: Channel.NOTIFICATION, listPosition: 1)
        fn.facility = facility
        fn.save(failOnError: true)
        def facility2 = createFacility()
        def fn2 = new FacilityMessage(headline: "title2", content: "text2",
                validFrom: new Date(), validTo: new Date() + 1, channel: Channel.NOTIFICATION, listPosition: 2)
        fn2.facility = facility2
        fn2.save(failOnError: true)
        def securityServiceControl = mockSecurityService(facility)

        def model = controller.index()

        assert 1 == model.facilityNotificationInstanceList.size()
        assert fn == model.facilityNotificationInstanceList[0]
        assert 1 == model.facilityNotificationInstanceTotal
        securityServiceControl.verify()
    }

    void testArchived() {
        def facility = createFacility()
        def fm = new FacilityMessage(headline: "title", content: "text",
                validFrom: new Date() - 2, validTo: new Date() - 1, channel: Channel.NOTIFICATION, listPosition: 1)
        fm.facility = facility
        fm.save(failOnError: true)
        def securityServiceControl = mockSecurityService(facility, 2)

        controller.archived()
        assert "/facilityNotification/index" == view
        assert 1 == model.facilityNotificationInstanceList.size()
        assert model.facilityNotificationInstanceList.contains(fm)
        assert HttpServletResponse.SC_OK == response.status
        assert 1 == FacilityMessage.count()
        securityServiceControl.verify()
    }

    void testSwapListPosition() {
        def facility = createFacility()
        def fm = new FacilityMessage(headline: "title", content: "text",
                validFrom: new Date() - 2, validTo: new Date() - 1, channel: Channel.NOTIFICATION, listPosition: 1)
        fm.facility = facility
        fm.save(failOnError: true)
        def fm2 = new FacilityMessage(headline: "title2", content: "text2",
                validFrom: new Date() - 2, validTo: new Date() - 1, channel: Channel.NOTIFICATION, listPosition: 2)
        fm2.facility = facility
        fm2.save(failOnError: true)
        def pos1 = fm.listPosition
        def pos2 = fm2.listPosition
        controller.swapListPosition(fm.id, fm2.id)

        assert pos2 == fm.listPosition
        assert pos1 == fm2.listPosition
    }

    void testCreate() {
        def facility = createFacility()
        def securityServiceControl = mockSecurityService(facility, 1)
        def model = controller.create()
        assert model.facilityNotificationInstance
        securityServiceControl.verify()
    }

    void testSave() {
        request.method = "POST"
        def securityServiceControl = mockSecurityService()
        params.headline = "title"
        params.content = "text"
        params.validFrom = "2015-02-18"
        params.validTo = "2015-02-27"
        params.listPosition = 1

        controller.save()

        assert "/facility/notifications/index" == response.redirectedUrl
        assert 1 == FacilityMessage.count()
        securityServiceControl.verify()
    }

    void testEdit() {
        def facility = createFacility()
        def fn = new FacilityMessage(headline: "title", content: "text",
                validFrom: new Date(), validTo: new Date() + 1, channel: Channel.NOTIFICATION, listPosition: 1)
        fn.facility = facility
        fn.save(failOnError: true)
        def securityServiceControl = mockSecurityService(facility)
        params.id = fn.id

        def model = controller.edit()

        assert fn == model.facilityNotificationInstance
        securityServiceControl.verify()
    }

    void testUpdate() {
        request.method = "POST"
        def facility = createFacility()
        def fn = new FacilityMessage(headline: "title", content: "text",
                validFrom: new Date(), validTo: new Date() + 1, channel: Channel.NOTIFICATION, listPosition: 1)
        fn.facility = facility
        fn.save(failOnError: true)
        def securityServiceControl = mockSecurityService(facility)
        params.headline = "updated"

        controller.update(fn.id, fn.version)

        assert "/facility/notifications/index" == response.redirectedUrl
        assert 1 == FacilityMessage.count()
        assert FacilityMessage.findByHeadline("updated")
        securityServiceControl.verify()
    }

    void testDelete() {
        request.method = "POST"
        def facility = createFacility()
        def fn = new FacilityMessage(headline: "title", content: "text",
                validFrom: new Date(), validTo: new Date() + 1, channel: Channel.NOTIFICATION, listPosition: 1)
        fn.facility = facility
        fn.save(failOnError: true)
        def securityServiceControl = mockSecurityService(facility)
        params.id = fn.id

        controller.delete()

        assert "/facility/notifications/index" == response.redirectedUrl
        assert !FacilityMessage.count()
        securityServiceControl.verify()
    }

    private mockSecurityService(facility = null, count = 1) {
        def serviceControl = mockFor(SecurityService)
        serviceControl.demand.getUserFacility(1..count) { -> facility ?: createFacility() }
        controller.securityService = serviceControl.createMock()
        serviceControl
    }
}
