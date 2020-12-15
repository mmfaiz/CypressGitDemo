package com.matchi.facility

import com.matchi.Facility
import com.matchi.Municipality
import com.matchi.Region
import com.matchi.User
import com.matchi.dynamicforms.Form
import com.matchi.dynamicforms.FormTemplate
import com.matchi.dynamicforms.Submission
import com.matchi.SecurityService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor

import static com.matchi.TestUtils.*

/**
 * @author Sergei Shushkevich
 */
@TestFor(FacilityFormController)
@Mock([Form, FormTemplate, Facility, Municipality, Region, Submission])
class FacilityFormControllerTests {

    void testIndex() {
        def facility = createFacility()
        def securityServiceControl = mockSecurityService(facility)
        def f = new Form(name: "test", facility: facility, activeFrom: new Date(),
                activeTo: new Date() + 1).save(failOnError: true)

        def model = controller.index()

        assert 1 == model.forms.size()
        assert f == model.forms[0]
        assert 1 == model.formsCount
        securityServiceControl.verify()
    }

    void testCreate() {
        def securityServiceControl = mockSecurityService()

        def model = controller.create()

        assert model.containsKey("formTemplates")
        securityServiceControl.verify()
    }

    void testSave() {
        def securityServiceControl = mockSecurityService()
        params.name = "test"
        params.activeFrom = new Date()
        params.activeTo = new Date() + 1

        controller.save()

        assert "/facility/forms/index" == response.redirectedUrl
        assert 1 == Form.count()
        assert Form.findByName("test")
        securityServiceControl.verify()
    }

    void testEdit() {
        def facility = createFacility()
        def securityServiceControl = mockSecurityService(facility)
        def f = new Form(name: "test", facility: facility, activeFrom: new Date(),
                activeTo: new Date() + 1).save(failOnError: true)

        def model = controller.edit(f.id)

        assert f == model.formInstance
        securityServiceControl.verify()
    }

    void testUpdate() {
        def facility = createFacility()
        def securityServiceControl = mockSecurityService(facility)
        def f = new Form(name: "test", facility: facility, activeFrom: new Date(),
                activeTo: new Date() + 1).save(failOnError: true)
        params.name = "new name"

        controller.update(f.id, f.version)

        assert "/facility/forms/index" == response.redirectedUrl
        assert 1 == Form.count()
        assert Form.findByName("new name")
        securityServiceControl.verify()
    }

    void testDelete() {
        def facility = createFacility()
        def securityServiceControl = mockSecurityService(facility)
        def f = new Form(name: "test", facility: facility, activeFrom: new Date(),
                activeTo: new Date() + 1).save(failOnError: true)

        controller.delete(f.id)

        assert "/facility/forms/index" == response.redirectedUrl
        assert !Form.count()
        securityServiceControl.verify()
    }

    void testSubmissions() {
        def facility = createFacility()
        def securityServiceControl = mockSecurityService(facility)
        def f = new Form(name: "test", facility: facility, activeFrom: new Date(),
                activeTo: new Date() + 1).save(failOnError: true)
        def submission = new Submission(form: f).save(failOnError: true)

        def model = controller.submissions(f.id)

        assert f == model.formInstance
        assert model.submissions
        assert submission == model.submissions[0]
        assert 1 == model.submissionsCount
        securityServiceControl.verify()
    }

    private mockSecurityService(facility = null) {
        def serviceControl = mockFor(SecurityService)
        serviceControl.demand.getUserFacility { -> facility ?: createFacility() }
        controller.securityService = serviceControl.createMock()
        serviceControl
    }
}
