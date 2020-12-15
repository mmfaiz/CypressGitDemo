package com.matchi.facility

import com.matchi.*
import com.matchi.activities.ActivityOccasion
import com.matchi.activities.trainingplanner.TrainingCourt
import com.matchi.dynamicforms.Form
import com.matchi.dynamicforms.FormField
import com.matchi.dynamicforms.FormTemplate
import com.matchi.activities.trainingplanner.Trainer
import com.matchi.activities.trainingplanner.CourseActivity
import com.matchi.activities.Participant
import com.matchi.dynamicforms.Submission
import com.matchi.SecurityService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.junit.Before

import javax.servlet.http.HttpServletResponse
import org.joda.time.LocalDate
import org.joda.time.LocalTime

import static com.matchi.TestUtils.*

/**
 * @author Sergei Shushkevich
 */
@TestFor(FacilityCourseController)
@Mock([CourseActivity, Facility, Municipality, Region, Form, FormTemplate, Season, FormField,
Trainer, Submission, ActivityOccasion, Sport, Court, TrainingCourt, Participant])
class FacilityCourseControllerTests {

    @Before
    void setUp() {
        CourseActivity.metaClass.static.withCriteria = { Map obj, Closure cls -> 1 }
    }

    void testIndex() {
        def facility = createFacility()
        def securityServiceControl = mockSecurityService(facility)
        def course = new CourseActivity(name: "test", facility: facility, startDate: new Date() - 5, endDate: new Date() + 5)
        initAndSaveForm(facility, course)
        def course2 = new CourseActivity(name: "test2", facility: facility, startDate: new Date() - 5, endDate: new Date() - 1)
        initAndSaveForm(facility, course2)
        def course3 = new CourseActivity(name: "test3", facility: facility, startDate: new Date() + 5, endDate: new Date() + 10)
        initAndSaveForm(facility, course3)

        def model = controller.index()

        assert 2 == model.courseInstanceList.size()
        assert model.courseInstanceList.contains(course)
        assert model.courseInstanceList.contains(course3)
        assert 2 == model.courseInstanceTotal
        securityServiceControl.verify()
    }

    void testArchive() {
        def facility = createFacility()
        def securityServiceControl = mockSecurityService(facility)
        def course = new CourseActivity(name: "test", facility: facility, startDate: new Date() - 5, endDate: new Date() + 5)
        initAndSaveForm(facility, course)
        def course2 = new CourseActivity(name: "test2", facility: facility, startDate: new Date() - 5, endDate: new Date() - 1)
        initAndSaveForm(facility, course2)
        def course3 = new CourseActivity(name: "test3", facility: facility, startDate: new Date() + 5, endDate: new Date() + 10)
        initAndSaveForm(facility, course3)

        controller.archive()

        assert "/facilityCourse/index" == view
        assert 1 == model.courseInstanceList.size()
        assert course2 == model.courseInstanceList[0]
        assert 1 == model.courseInstanceTotal
        securityServiceControl.verify()
    }

    void testCreate() {
        def securityServiceControl = mockSecurityService()
        def model = controller.create()
        assert model.containsKey("courseInstance")
        securityServiceControl.verify()
    }

    void testSaveWithoutForm() {
        request.method = "POST"
        def securityServiceControl = mockSecurityService(createFacility(), 3)
        params.name = "test"
        params.startDate = "2015-02-17"
        params.endDate = "2015-02-26"
        controller.save()

        assert "/facilityCourse/create" == view
        assert !CourseActivity.count()
        securityServiceControl.verify()
    }

    void testSaveWithForm() {
        request.method = "POST"
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

        assert "/facility/courses/index" == response.redirectedUrl
        assert 1 == CourseActivity.count()
        assert 2 == CourseActivity.get(1).form.fields.size()
        assert CourseActivity.findByName("test with form")
        securityServiceControl.verify()
    }

    void testEdit() {
        def facility = createFacility()
        def securityServiceControl = mockSecurityService(facility, 2)
        def course = new CourseActivity(name: "test", facility: facility, startDate: new Date() - 5, endDate: new Date() + 5)
        initAndSaveForm(facility, course)
        params.id = course.id

        def model = controller.edit()

        assert course == model.courseInstance
        securityServiceControl.verify()
    }

    void testUpdateWithoutForm() {
        request.method = "POST"
        def facility = createFacility()
        def securityServiceControl = mockSecurityService(facility, 3)
        def course = new CourseActivity(name: "test", facility: facility, startDate: new Date(), endDate: new Date() + 1)
        initAndSaveForm(facility, course)
        params.name = "new name"
        params.startDate = "2015-02-17"
        params.endDate = "2015-02-26"

        controller.update(course.id, course.version)

        //Form is still there - nothing changed. No deletion on update
        assert "/facility/courses/index" == response.redirectedUrl
        assert 1 == CourseActivity.count()
        assert 1 == Form.count()
        securityServiceControl.verify()
    }


    void testUpdateWithForm() {
        request.method = "POST"
        def facility = createFacility()
        def securityServiceControl = mockSecurityService(facility, 3)
        def course = new CourseActivity(name: "test", facility: facility, startDate: new Date(), endDate: new Date() + 1)
        initAndSaveForm(facility, course)
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
        def oldForm = course.form
        controller.update(course.id, course.version)

        assert "/facility/courses/index" == response.redirectedUrl
        assert 1 == CourseActivity.count()
        assert CourseActivity.findByName("test with form")
        //if no submissions - just same form updated
        assert oldForm.id == course.form?.id
        assert oldForm.hash == course.form?.hash
        assert oldForm.fields.size() == course.form?.fields?.size()
        securityServiceControl.verify()

    }

    void testUpdateWithFormWithSubmission() {
        request.method = "POST"
        def facility = createFacility()
        def securityServiceControl = mockSecurityService(facility, 3)
        def course = new CourseActivity(name: "test", facility: facility, startDate: new Date(), endDate: new Date() + 1)
        initAndSaveForm(facility, course)
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
        params."form.isRequired${course.form.fields[0].id}" = 'on'
        params.formFieldId = ["1", "2"] as String[]
        params."field.isRequired${course.form.fields[1].id}" = 'on'
        def oldForm = course.form
        new Submission(form: course.form).save(validate: false, flush: true)
        controller.update(course.id, course.version)

        assert "/facility/courses/index" == response.redirectedUrl
        assert 1 == CourseActivity.count()
        assert CourseActivity.findByName("test with form")
        //if no submissions - just same form updated
        assert oldForm.id == course.form?.id
        assert oldForm.hash == course.form?.hash
        assert oldForm.fields.size() == course.form?.fields?.size()
        assert course.form.fields.findAll { !it.isEditable }.size() == 1
        assert course.form.fields.findAll { it.isRequired }.size() == 1

        securityServiceControl.verify()

    }

    void testDelete() {
        request.method = "POST"
        def facility = createFacility()
        def securityServiceControl = mockSecurityService(facility, 3)
        def course = new CourseActivity(name: "test", facility: facility, startDate: new Date(), endDate: new Date() + 2)
        initAndSaveForm(facility, course)
        params.id = course.id

        controller.delete()

        assert "/facility/courses/index" == response.redirectedUrl
        assert !CourseActivity.count()
        securityServiceControl.verify()
    }

    void testPreview() {
        def tmpl = initAndSaveTemplate()
        params.'form.relatedFormTemplate.id' = tmpl.id
        params."field.isActive${tmpl.templateFields[0].id}" = 'true'
        params.formFieldId = [tmpl.templateFields[0].id, tmpl.templateFields[1].id]
        def securityServiceControl = mockSecurityService()

        controller.preview()

        assert "/form/show" == view
        assert model.formInstance
        assert 1 == model.fields.size()
        assert tmpl.templateFields[0].label == model.fields[0].label
        assert model.preview
        assert !CourseActivity.count()
        assert !Form.count()
        securityServiceControl.verify()
    }

    void testAddOccasionTrainer() {
        def facility = createFacility()
        def course = createCourse(facility)
        def occasion = new ActivityOccasion(activity: course, date: new LocalDate(),
                startTime: new LocalTime(), endTime: new LocalTime()).save(failOnError: true)
        def trainer = new Trainer(facility: facility, firstName: "John", lastName: "Doe",
                sport: createSport()).save(failOnError: true)
        def securityServiceControl = mockSecurityService(facility)

        controller.addOccasionTrainer(occasion.id, trainer.id)

        assert HttpServletResponse.SC_OK == response.status
        assert 1 == occasion.trainers.size()
        assert trainer == occasion.trainers.iterator().next()
        securityServiceControl.verify()
    }

    void testRemoveOccasionTrainer() {
        def facility = createFacility()
        def course = createCourse(facility)
        def trainer = new Trainer(facility: facility, firstName: "John", lastName: "Doe",
                sport: createSport()).save(failOnError: true)
        def occasion = new ActivityOccasion(activity: course, date: new LocalDate(),
                startTime: new LocalTime(), endTime: new LocalTime())
                .addToTrainers(trainer).save(failOnError: true)
        def securityServiceControl = mockSecurityService(facility)

        controller.removeOccasionTrainer(occasion.id, trainer.id)

        assert HttpServletResponse.SC_OK == response.status
        assert !occasion.trainers
        securityServiceControl.verify()
    }

    void testAddTrainingCourtWithCourt() {
        def facility = createFacility()
        def court = createCourt(facility)
        def securityServiceControl = mockSecurityService(facility)
        controller.params.court = court.id

        controller.addTrainingCourt()

        assert 1 == TrainingCourt.count()
        assert TrainingCourt.findByName(court.name)
        securityServiceControl.verify()
    }

    void testAddTrainingCourtWithoutCourt() {
        def facility = createFacility()
        def securityServiceControl = mockSecurityService(facility)
        controller.params.name = "test"

        controller.addTrainingCourt()

        assert 1 == TrainingCourt.count()
        assert TrainingCourt.findByName("test")
        securityServiceControl.verify()
    }

    void testRemoveTrainingCourt() {
        def facility = createFacility()
        def trainingCourt = createTrainingCourt(facility)
        def securityServiceControl = mockSecurityService(facility)
        assert 1 == TrainingCourt.count()
        controller.params.id = trainingCourt.id

        controller.removeTrainingCourt()

        assert !TrainingCourt.count()
        securityServiceControl.verify()
    }

    void testUpdateTrainingCourt() {
        def facility = createFacility()
        def trainingCourt = createTrainingCourt(facility)
        def securityServiceControl = mockSecurityService(facility)
        assert 1 == TrainingCourt.count()
        assert TrainingCourt.get(trainingCourt.id)
        controller.params.id = trainingCourt.id
        controller.params.name = "test"

        controller.updateTrainingCourt()

        assert "test" == TrainingCourt.get(trainingCourt.id)?.name
        securityServiceControl.verify()
    }

    void testRemoveOccasionParticipationNoParams() {
        request.method = 'DELETE'

        //when
        controller.removeOccasionParticipation()

        //then
        assert  response.dump().contains("content=400")
    }

    void testRemoveOccasionParticipationOccasionNotFound() {
        controller.params.occasionId = 1
        controller.params.participantId = 1
        request.method = 'DELETE'

        //when
        controller.removeOccasionParticipation()

        //then
        assert  response.dump().contains("content=404")
    }

    void testRemoveOccasionParticipationOccasionFoundNoParticipantsFound() {
        def facility = createFacility()
        def course = createCourse(facility)
        def occasion = new ActivityOccasion(activity: course, date: new LocalDate(),
                startTime: new LocalTime(), endTime: new LocalTime()).save(failOnError: true)
        assert occasion != null
        assert occasion.id != null
        def securityServiceControl = mockSecurityService(facility)

        controller.params.occasionId = occasion.id
        controller.params.participantId = 1
        request.method = 'DELETE'

        //when
        controller.removeOccasionParticipation()

        //than
        assert  response.dump().contains("content=200")
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

    private void initAndSaveForm(Facility facility, CourseActivity course) {
        Form form = new Form(facility: facility, course: course, name: '123', activeFrom: new Date(), activeTo: new Date() + 2, membershipRequired: true, paymentRequired: true)
        FormField field1 = new FormField(form: form, label: '123', type: FormField.Type.TEXT, isActive: false, isEditable: false)
        FormField field2 = new FormField(form: form, label: '321', type: FormField.Type.TEXT, isRequired: true)
        form.addToFields(field1)
        form.addToFields(field2)
        form.save(flush: true, failOnError: true)
        course.form = form
        course.save(flush: true)
    }
}
