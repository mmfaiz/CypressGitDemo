package com.matchi.dynamicforms

import com.matchi.Amount
import com.matchi.Customer
import com.matchi.Facility
import com.matchi.Municipality
import com.matchi.Region
import com.matchi.User
import com.matchi.activities.Participant
import com.matchi.activities.trainingplanner.CourseActivity
import com.matchi.orders.Order
import com.matchi.price.PriceListCustomerCategory
import com.matchi.requirements.RequirementProfile
import grails.test.GrailsMock
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.junit.Before

import static com.matchi.TestUtils.*
/**
 * @author Michael Astreiko
 */
@TestFor(Form)
@Mock([Customer, Facility, Form, FormField, Municipality, Order, Region, Submission, User,
        CourseActivity, Participant])
class FormTest {

    Form domain

    @Before
    void setUp() {
        domain = new Form()
    }

    void testUniqueHash() {
        def testHash = "adasdfamsv;asdfnm3498u"
        new Form(hash: testHash).save(validate: false, flush: true)
        def form2 = new Form(hash: testHash)
        assert !form2.validate(['hash'])
        assert form2.errors.getFieldError('hash').code == 'unique'
    }

    void testMaxSubmissionConstraints() {
        def form = createTestForm()
        assert form.validate()
        //now do error
        form.maxSubmissions = 0
        assert !form.validate()
        assert 1 == form.errors.errorCount
        assert form.errors['maxSubmissions']
        //back to normal
        form.maxSubmissions = 5
        assert form.validate()
    }

    void testCheckUserFacilityNoRequirementProfiles() {
        GrailsMock mockFacility = mockFor(Facility)
        GrailsMock mockUser = mockFor(User)

        mockFacility.demand.hasRequirementProfiles(1) { ->
            return false
        }

        domain.facility = mockFacility.createMock()

        assert domain.checkUser(mockUser.createMock())
        mockFacility.verify()
    }

    void testCheckUserNoRequirementProfile() {
        GrailsMock mockFacility = mockFor(Facility)
        GrailsMock mockUser = mockFor(User)
        GrailsMock mockRequirementProfile = mockFor(RequirementProfile)

        mockFacility.demand.hasRequirementProfiles(1) { ->
            return true
        }

        mockRequirementProfile.demand.asBoolean(1) { ->
            return false
        }

        domain.facility = mockFacility.createMock()
        domain.requirementProfile = mockRequirementProfile.createMock()

        assert domain.checkUser(mockUser.createMock())
        mockFacility.verify()
        mockRequirementProfile.verify()
    }

    void testCheckUserNoCustomerFound() {
        GrailsMock mockFacility = mockFor(Facility)
        GrailsMock mockUser = mockFor(User)
        GrailsMock mockRequirementProfile = mockFor(RequirementProfile)
        GrailsMock mockStaticCustomer = mockFor(Customer)

        User user = mockUser.createMock()

        mockFacility.demand.hasRequirementProfiles(1) { ->
            return true
        }

        mockRequirementProfile.demand.asBoolean(1) { ->
            return true
        }

        mockStaticCustomer.demand.static.findByUserAndFacility(1) { u, f ->
            assert u == user
            return null
        }

        domain.facility = mockFacility.createMock()
        domain.requirementProfile = mockRequirementProfile.createMock()

        assert !domain.checkUser(user)
        mockFacility.verify()
        mockRequirementProfile.verify()
        mockStaticCustomer.verify()
    }

    void testCheckUserCustomerFound() {
        GrailsMock mockFacility = mockFor(Facility)
        GrailsMock mockUser = mockFor(User)
        GrailsMock mockRequirementProfile = mockFor(RequirementProfile)
        GrailsMock mockStaticCustomer = mockFor(Customer)
        GrailsMock mockCustomer = mockFor(Customer)

        User user = mockUser.createMock()
        Customer customer = mockCustomer.createMock()

        mockFacility.demand.hasRequirementProfiles(1) { ->
            return true
        }

        mockRequirementProfile.demand.asBoolean(1) { ->
            return true
        }

        mockStaticCustomer.demand.static.findByUserAndFacility(1) { u, f ->
            assert u == user
            return customer
        }

        mockCustomer.demand.asBoolean(1) { ->
            return true
        }

        mockRequirementProfile.demand.validate(1) { c ->
            assert c == customer
            return true
        }

        domain.facility = mockFacility.createMock()
        domain.requirementProfile = mockRequirementProfile.createMock()

        assert domain.checkUser(user)
        mockFacility.verify()
        mockRequirementProfile.verify()
        mockStaticCustomer.verify()
        mockCustomer.verify()
    }

    void testGetAcceptedSubmissionsAmount() {
        def form = createTestForm()
        def user = createUser()

        assert 0 == form.acceptedSubmissionsAmount

        createSubmission(createCustomer(), form, user)

        assert 1 == form.acceptedSubmissionsAmount

        def s = createSubmission(createCustomer(), form, user)
        s.status = Submission.Status.ACCEPTED
        s.save(failOnError: true)
        s = createSubmission(createCustomer(), form, user)
        s.status = Submission.Status.DISCARDED
        s.save(failOnError: true)
        s = createSubmission(createCustomer(), form, user)
        s.status = Submission.Status.WAITING
        s.save(failOnError: true)

        assert 4 == form.acceptedSubmissionsAmount

        def course = createCourse(form.facility, new Date(), new Date() + 1, form)

        assert 1 == form.acceptedSubmissionsAmount

        def p = createCourseParticipant(createCustomer(), course)
        course.addToParticipants(p)
        course.save(failOnError: true)

        assert 2 == form.acceptedSubmissionsAmount
    }

    void testIsSubmissionAllowed() {
        def user = createUser()
        def form = createTestForm()
        form.maxSubmissions = 1
        form.save(failOnError: true)
        def form2 = createTestForm()
        form2.maxSubmissions = 1
        form2.save(failOnError: true)

        assert form.isSubmissionAllowed()
        assert form2.isSubmissionAllowed()

        createSubmission(createCustomer(), form, user)
        def course = createCourse(form2.facility, new Date(), new Date() + 1, form2)
        form2.course = course
        form2.save(failOnError: true)
        def p = createCourseParticipant(createCustomer(), course)
        course.addToParticipants(p)
        course.save(failOnError: true)

        assert !form.isSubmissionAllowed()
        assert !form2.isSubmissionAllowed()

        form.maxSubmissions = 2
        form.save(failOnError: true)
        form2.maxSubmissions = 2
        form2.save(failOnError: true)

        assert form.isSubmissionAllowed()
        assert form2.isSubmissionAllowed()

        createSubmission(createCustomer(), form, user)
        def s = createSubmission(createCustomer(), form2, user)
        s.status = Submission.Status.WAITING
        s.save(failOnError: true)

        assert !form.isSubmissionAllowed()
        assert !form2.isSubmissionAllowed()

        form.maxSubmissions = null
        form.save(failOnError: true)
        form2.maxSubmissions = null
        form2.save(failOnError: true)

        assert form.isSubmissionAllowed()
        assert form2.isSubmissionAllowed()
    }

    Form testToAmount() {
        def form = createTestForm()
        def customer = createCustomer()
        form.facility.vat = 6

        Amount amount = form.toAmount(customer)
        assert amount.VAT == 5.66
    }

    Form createTestForm() {
        def facility = createFacility()
        def form = createForm(facility)
        form.price = 100
        form.save(failOnError: true)
        new FormField(form: form, label: "l", type: FormField.Type.TEXT, isRequired: true).save(failOnError: true)
        form
    }


}
