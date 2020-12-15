package com.matchi

import com.matchi.SecurityService
import com.matchi.activities.EventActivity
import com.matchi.activities.Participant
import com.matchi.activities.trainingplanner.CourseActivity
import com.matchi.dynamicforms.Form
import com.matchi.dynamicforms.FormField
import com.matchi.dynamicforms.Submission
import grails.plugin.springsecurity.SpringSecurityService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.apache.commons.lang.RandomStringUtils

import static com.matchi.TestUtils.*

/**
 * @author Sergei Shushkevich
 */
@TestFor(FormController)
@Mock([Customer, Facility, Form, FormField, Municipality, Region, Submission, User,
        Participant, CourseActivity, EventActivity])
class FormControllerTests {

    void testShow() {
        def form = new Form(facility: createFacility(), name: "f", activeFrom: new Date() - 1,
                activeTo: new Date() + 1).save(failOnError: true)

        def securityServiceControl = mockFor(SecurityService, false)

        securityServiceControl.demand.hasFacilityAccessTo(1) { Facility facility ->
            assert facility.id == form.facility.id
            return true
        }

        controller.securityService = securityServiceControl.createMock()

        def formResponse = controller.show(form.hash)
        assert view.toString() == "/form/show"

        securityServiceControl.verify()
    }

    void testShow_InactiveForm1() {
        def form = new Form(facility: createFacility(), name: "f", activeFrom: new Date() - 2,
                activeTo: new Date() - 1).save(failOnError: true)

        def securityServiceControl = mockSecurityService(null, false)
        def user = createUser()
        def springSecurityServiceControl = mockSpringSecurity(user)

        securityServiceControl.demand.hasFacilityAccessTo(1) { Facility f2 ->
            return false
        }

        controller.show(form.hash)
        assert view == "/form/inactive"

        securityServiceControl.verify()
        springSecurityServiceControl.verify()
    }

    void testShow_InactiveForm2() {
        def form = new Form(facility: createFacility(), name: "f", activeFrom: new Date() + 1,
                activeTo: new Date() + 2).save(failOnError: true)

        def securityServiceControl = mockSecurityService(null, false)
        def user = createUser()
        def springSecurityServiceControl = mockSpringSecurity(user)

        securityServiceControl.demand.hasFacilityAccessTo(1) { Facility f2 ->
            return false
        }

        controller.show(form.hash)
        assert view.toString() == "/form/inactive"

        securityServiceControl.verify()
        springSecurityServiceControl.verify()
    }

    void testShow_InactiveFormFacilityAdminAccess() {
        def facility = createFacility()
        def form = new Form(facility: facility, name: "f", activeFrom: new Date() - 2,
                activeTo: new Date() - 1).save(failOnError: true)
        def securityServiceControl = mockSecurityService(facility, true)

        controller.show(form.hash)
        assert view.toString() == "/form/show"

        securityServiceControl.verify()
    }

    void testSubmit() {
        def form = new Form(facility: createFacility(), name: "f", activeFrom: new Date() - 1,
                activeTo: new Date() + 1).save(failOnError: true)

        form.course = createCourse(form.facility)
        form.save(flush: true)

        def user = createUser()
        def springSecurityServiceControl = mockSpringSecurity(user, 2)
        def customerServiceControl = mockCustomerService()
        def securityServiceControl = mockFor(SecurityService)
        def submissionServiceControl = mockFor(SubmissionService)

        controller.metaClass.hasFacilityFullRights = { ->
            return false
        }

        securityServiceControl.demand.hasFacilityAccess(2) { -> true }
        controller.securityService = securityServiceControl.createMock()

        submissionServiceControl.demand.findExistingActivitySubmission(1..1) { c, a -> null }
        controller.submissionService = submissionServiceControl.createMock()

        customerServiceControl.demand.linkCustomerToUser(1) { c -> }

        def model = controller.submit(form.id)

        assert 1 == Submission.count()
        assert Submission.findByFormAndSubmissionIssuer(form, user)
        springSecurityServiceControl.verify()
        customerServiceControl.verify()
        securityServiceControl.verify()
        submissionServiceControl.verify()
    }

    void testSubmit_Failure() {
        def form = new Form(facility: createFacility(), name: "f", activeFrom: new Date() - 1,
                activeTo: new Date() + 1).save(failOnError: true)

        form.course = createCourse(form.facility)
        form.save(flush: true)

        new FormField(form: form, label: "l", type: FormField.Type.TEXT, isRequired: true).save(failOnError: true)

        def springSecurityServiceControl = mockSpringSecurity(createUser(), 2)

        controller.metaClass.hasFacilityFullRights = { ->
            return false
        }

        def submissionServiceControl = mockFor(SubmissionService)
        submissionServiceControl.demand.findExistingActivitySubmission(1..1) { c, a -> null }
        controller.submissionService = submissionServiceControl.createMock()

        controller.submit(form.id)

        assert "/form/show" == view
        assert form == model.formInstance
        assert model.submission
        assert !Submission.count()
        springSecurityServiceControl.verify()
        submissionServiceControl.verify()
    }

    void testSubmitAndPay() {
        def form = new Form(facility: createFacility(), name: "f", activeFrom: new Date() - 1,
                activeTo: new Date() + 1).save(failOnError: true)
        def springSecurityServiceControl = mockSpringSecurity(createUser(), 2)
        def customerServiceControl = mockCustomerService()
        def securityServiceControl = mockFor(SecurityService)
        securityServiceControl.demand.hasFacilityAccess(2) { -> true }
        controller.securityService = securityServiceControl.createMock()

        customerServiceControl.demand.linkCustomerToUser(1) { c -> }

        controller.submitAndPay(form.id)

        assert "/form/show" == view
        assert form == model.formInstance
        assert model.submission
        assert model.pay
        assert session[FormController.SUBMISSION_SESSION_KEY]
        assert !Submission.count()
        springSecurityServiceControl.verify()
        customerServiceControl.verify()
        securityServiceControl.verify()
    }

    void testSubmitAndPay_Failure() {
        def form = new Form(facility: createFacility(), name: "f", activeFrom: new Date() - 1,
                activeTo: new Date() + 1).save(failOnError: true)
        new FormField(form: form, label: "l", type: FormField.Type.TEXT, isRequired: true).save(failOnError: true)

        def springSecurityServiceControl = mockSpringSecurity(createUser(), 2)

        controller.submitAndPay(form.id)

        assert "/form/show" == view
        assert form == model.formInstance
        assert model.submission
        assert !Submission.count()
        springSecurityServiceControl.verify()
    }

    void testCheckConstraints() {
        Facility facility = createFacility()
        Form form = new Form(facility: facility, name: "f", activeFrom: new Date() - 1,
                activeTo: new Date() + 1, hash: RandomStringUtils.random(10), maxSubmissions: 1).save(flush: true, failOnError: true)
        CourseActivity activity = createCourse(facility, new Date() + 3, new Date() + 4, form)
        form.course = activity
        form.save(flush: true, failOnError: true)

        params.hash = form.hash

        def securityServiceControl = mockSecurityService(facility, true)
        securityServiceControl.demand.hasFacilityAccessTo(2) { Facility f2 ->
            return false
        }

        assert controller.checkConstraints()

        Customer customer = createCustomer(facility)
        User user = createUser()
        Submission submission = createSubmission(customer, form, user)

        // Submission does not count
        assert controller.checkConstraints()

        Participant participant = createCourseParticipant(customer, activity)
        participant.submission = submission
        participant.save(flush: true, failOnError: true)
        activity.addToParticipants(participant).save(flush: true, failOnError: true)

        // But the participant does!!
        assert !controller.checkConstraints()
    }

    void testCheckConstraintsForEventActivity() {
        def facility = createFacility()
        def form = new Form(facility: facility, name: "f", activeFrom: new Date() - 1,
                activeTo: new Date() + 1, hash: RandomStringUtils.random(10), maxSubmissions: 1)
                .save(flush: true, failOnError: true)
        def activity = new EventActivity(name: RandomStringUtils.randomAlphabetic(10),
                facility: facility, startDate: new Date(), endDate: new Date() + 1, form: form)
                .save(failOnError: true, flush: true)
        form.event = activity
        form.save(flush: true, failOnError: true)
        params.hash = form.hash

        def securityServiceControl = mockSecurityService(facility, true)
        securityServiceControl.demand.hasFacilityAccessTo(2) { Facility f2 ->
            return false
        }

        assert controller.checkConstraints()

        createSubmission(createCustomer(facility), form)

        assert !controller.checkConstraints()
    }

    private mockSecurityService(facility = null, hasFacilityAccessCall = false) {
        def serviceControl = mockFor(SecurityService)

        if (hasFacilityAccessCall) {
            serviceControl.demand.hasFacilityAccessTo(1..2) { Facility f2 ->
                return true
            }
        }
        serviceControl.demand.getUserFacility(0..10) { -> facility }
        controller.securityService = serviceControl.createMock()
        serviceControl
    }

    private mockSpringSecurity(user = null, maxCalls = 1) {
        def serviceControl = mockFor(SpringSecurityService)
        serviceControl.demand.getCurrentUser(1..maxCalls) { -> user }
        controller.springSecurityService = serviceControl.createMock()
        serviceControl
    }

    private mockCustomerService() {
        def customerServiceControl = mockFor(CustomerService)
        customerServiceControl.demand.getOrCreateUserCustomer { u, f -> new Customer() }
        controller.customerService = customerServiceControl.createMock()
        customerServiceControl
    }
}
