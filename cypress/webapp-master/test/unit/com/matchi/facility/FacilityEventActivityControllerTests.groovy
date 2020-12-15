package com.matchi.facility

import com.matchi.*
import com.matchi.activities.EventActivity
import com.matchi.dynamicforms.Form
import com.matchi.dynamicforms.FormField
import com.matchi.dynamicforms.FormTemplate
import com.matchi.dynamicforms.Submission
import com.matchi.SecurityService
import com.matchi.requirements.RequirementProfile
import grails.test.mixin.Mock
import grails.test.mixin.TestFor

import static com.matchi.TestUtils.*

/**
 * @author Sergei Shushkevich
 */
@TestFor(FacilityEventActivityController)
@Mock([EventActivity, Facility, Municipality, Region, Form, FormTemplate, Season, FormField, Submission, Sport, RequirementProfile])
class FacilityEventActivityControllerTests {

    void testIndex() {
        def facility = createFacility()
        def securityServiceControl = mockSecurityService(facility)
        def eventActivity = new EventActivity(name: "test", facility: facility,
                startDate: new Date() - 5, endDate: new Date() + 5)
        initAndSaveForm(facility, eventActivity)


        def eventActivity2 = new EventActivity(name: "test2", facility: facility, startDate: new Date() - 5, endDate: new Date() - 1)
        initAndSaveForm(facility, eventActivity2)
        def eventActivity3 = new EventActivity(name: "test3", facility: facility, startDate: new Date() + 5, endDate: new Date() + 10)
        initAndSaveForm(facility, eventActivity3)
        def model = controller.index()

        assert 2 == model.eventActivityInstanceList.size()
        assert model.eventActivityInstanceList.contains(eventActivity)
        assert model.eventActivityInstanceList.contains(eventActivity3)
        assert 2 == model.eventActivityInstanceTotal
        securityServiceControl.verify()
    }

    void testArchive() {
        def facility = createFacility()
        def securityServiceControl = mockSecurityService(facility)
        def eventActivity = new EventActivity(name: "test", facility: facility, startDate: new Date() - 5, endDate: new Date() + 5)
        initAndSaveForm(facility, eventActivity)
        def eventActivity2 = new EventActivity(name: "test2", facility: facility, startDate: new Date() - 5, endDate: new Date() - 1)
        initAndSaveForm(facility, eventActivity2)
        def eventActivity3 = new EventActivity(name: "test3", facility: facility, startDate: new Date() + 5, endDate: new Date() + 10)
        initAndSaveForm(facility, eventActivity3)

        controller.archive()

        assert "/facilityEventActivity/index" == view
        assert 1 == model.eventActivityInstanceList.size()
        assert eventActivity2 == model.eventActivityInstanceList[0]
        assert 1 == model.eventActivityInstanceTotal
        securityServiceControl.verify()
    }

    void testCreate() {
        def securityServiceControl = mockSecurityService()
        def model = controller.create()
        assert model.containsKey("eventActivityInstance")
        securityServiceControl.verify()
    }

    void testSaveWithoutForm() {
        def securityServiceControl = mockSecurityService(createFacility(), 3)
        params.name = "test"
        params.startDate = "2015-02-17"
        params.endDate = "2015-02-26"
        controller.save()

        assert "/facilityEventActivity/create" == view
        assert !EventActivity.count()
        securityServiceControl.verify()
    }

    void testSaveWithForm() {
        def securityServiceControl = mockSecurityService(createFacility(), 3)
        initAndSaveTemplate()
        params.name = "test with form"
        params.startDate = "2015-02-18"
        params.endDate = "2015-02-27"
        params."form.activeFrom" = "2015-02-18"
        params."form.activeTo" = "2015-02-27"
        params.'form.relatedFormTemplate.id' = 1
        params."form.membershipRequired" = true
        params."form.maxSubmissions" = 10
        params."form.paymentRequired" = true
        params."field.isRequired0" = 'on'
        params."field.isActive0" = 'true'
        params.formFieldId = ["1", "2"] as String[]
        controller.save()

        assert "/facility/events/index" == response.redirectedUrl
        assert 1 == EventActivity.count()
        assert 2 == EventActivity.get(1).form.fields.size()
        assert EventActivity.findByName("test with form")
        securityServiceControl.verify()
    }

    void testEdit() {
        def facility = createFacility()
        def securityServiceControl = mockSecurityService(facility, 2)
        def eventActivity = new EventActivity(name: "test", facility: facility, startDate: new Date() - 5, endDate: new Date() + 5)
        initAndSaveForm(facility, eventActivity)
        params.id = eventActivity.id

        def model = controller.edit()

        assert eventActivity == model.eventActivityInstance
        securityServiceControl.verify()
    }

    void testUpdateWithoutForm() {
        def facility = createFacility()
        def securityServiceControl = mockSecurityService(facility, 3)
        def eventActivity = new EventActivity(name: "test", facility: facility, startDate: new Date(), endDate: new Date() + 1)
        initAndSaveForm(facility, eventActivity)
        params.name = "new name"
        params.startDate = "2015-02-17"
        params.endDate = "2015-02-26"

        controller.update(eventActivity.id, eventActivity.version)

        //Form is still there - nothing changed. No deletion on update
        assert "/facility/events/index" == response.redirectedUrl
        assert 1 == EventActivity.count()
        assert 1 == Form.count()
        securityServiceControl.verify()
    }

    void testUpdateWithForm() {
        def facility = createFacility()
        def securityServiceControl = mockSecurityService(facility, 3)
        def eventActivity = new EventActivity(name: "test", facility: facility, startDate: new Date(), endDate: new Date() + 1)
        initAndSaveForm(facility, eventActivity)
        initAndSaveTemplate()
        params.name = "test with form"
        params.startDate = "2015-02-18"
        params.endDate = "2015-02-27"
        params."form.activeFrom" = "2015-02-18"
        params."form.activeTo" = "2015-02-27"
        params.'form.relatedFormTemplate.id' = 1
        params."form.membershipRequired" = true
        params."form.maxSubmissions" = 10
        params."form.paymentRequired" = true
        params."form.isRequired0" = 'on'
        params.formFieldId = ["1", "2"] as String[]
        def oldForm = eventActivity.form
        controller.update(eventActivity.id, eventActivity.version)

        assert "/facility/events/index" == response.redirectedUrl
        assert 1 == EventActivity.count()
        assert EventActivity.findByName("test with form")
        //if no submissions - just same form updated
        assert oldForm.id == eventActivity.form?.id
        assert oldForm.hash == eventActivity.form?.hash
        assert oldForm.fields.size() == eventActivity.form?.fields?.size()
        securityServiceControl.verify()

    }

    void testUpdateWithFormWithSubmission() {
        def facility = createFacility()
        def securityServiceControl = mockSecurityService(facility, 3)
        def eventActivity = new EventActivity(name: "test", facility: facility, startDate: new Date(), endDate: new Date() + 1)
        initAndSaveForm(facility, eventActivity)
        initAndSaveTemplate()
        params.name = "test with form"
        params.startDate = "2015-02-18"
        params.endDate = "2015-02-27"
        params."form.activeFrom" = "2015-02-18"
        params."form.activeTo" = "2015-02-27"
        params.'form.relatedFormTemplate.id' = 1
        params."form.membershipRequired" = true
        params."form.maxSubmissions" = 10
        params."form.paymentRequired" = true
        params."form.isRequired${eventActivity.form.fields[0].id}" = 'on'
        params.formFieldId = ["1", "2"] as String[]
        params."field.isRequired${eventActivity.form.fields[1].id}" = 'on'
        def oldForm = eventActivity.form
        new Submission(form: eventActivity.form).save(validate: false, flush: true)
        controller.update(eventActivity.id, eventActivity.version)

        assert "/facility/events/index" == response.redirectedUrl
        assert 1 == EventActivity.count()
        assert EventActivity.findByName("test with form")
        //if no submissions - just same form updated
        assert oldForm.id == eventActivity.form?.id
        assert oldForm.hash == eventActivity.form?.hash
        assert oldForm.fields.size() == eventActivity.form?.fields?.size()
        assert eventActivity.form.fields.findAll { !it.isEditable }.size() == 1
        assert eventActivity.form.fields.findAll { it.isRequired }.size() == 1

        securityServiceControl.verify()

    }

    void testDelete() {
        def facility = createFacility()
        def securityServiceControl = mockSecurityService(facility, 3)
        def eventActivity = new EventActivity(name: "test", facility: facility, startDate: new Date(), endDate: new Date() + 2)
        initAndSaveForm(facility, eventActivity)
        params.id = eventActivity.id

        controller.delete()

        assert "/facility/events/index" == response.redirectedUrl
        assert !EventActivity.count()
        securityServiceControl.verify()
    }

    void testPreview() {
        def securityServiceControl = mockSecurityService()
        def tmpl = initAndSaveTemplate()
        params.'form.relatedFormTemplate.id' = tmpl.id
        params."field.isActive${tmpl.templateFields[0].id}" = 'true'
        params.formFieldId = [tmpl.templateFields[0].id, tmpl.templateFields[1].id]

        controller.preview()

        assert "/form/show" == view
        assert model.formInstance
        assert 1 == model.fields.size()
        assert tmpl.templateFields[0].label == model.fields[0].label
        assert model.preview
        assert !EventActivity.count()
        assert !Form.count()
        securityServiceControl.verify()
    }

    private mockSecurityService(facility = null, maxCalls = 1) {
        def serviceControl = mockFor(SecurityService)
        serviceControl.demand.getUserFacility(1..maxCalls) { -> facility ?: createFacility() }
        controller.securityService = serviceControl.createMock()
        serviceControl
    }

    private FormTemplate initAndSaveTemplate() {
        FormTemplate template = new FormTemplate(name: 'form', description: 'desc')
        FormField field1 = new FormField(template: template, label: '123', type: FormField.Type.TEXT)
        FormField field2 = new FormField(template: template, label: '321', type: FormField.Type.TEXT)
        template.addToTemplateFields(field1)
        template.addToTemplateFields(field2)
        template.save(flush: true, failOnError: true)
    }

    private void initAndSaveForm(Facility facility, EventActivity eventActivity) {
        Form form = new Form(facility: facility, eventActivity: eventActivity, name: '123', activeFrom: new Date(), activeTo: new Date() + 2, membershipRequired: true, paymentRequired: true)
        FormField field1 = new FormField(form: form, label: '123', type: FormField.Type.TEXT, isActive: false, isEditable: false)
        FormField field2 = new FormField(form: form, label: '321', type: FormField.Type.TEXT, isRequired: true)
        form.addToFields(field1)
        form.addToFields(field2)
        form.save(flush: true, failOnError: true)
        eventActivity.form = form
        eventActivity.save(flush: true)
    }
}
