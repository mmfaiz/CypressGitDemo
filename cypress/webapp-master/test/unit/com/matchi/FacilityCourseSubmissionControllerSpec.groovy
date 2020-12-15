package com.matchi

import com.matchi.activities.trainingplanner.CourseActivity
import com.matchi.dynamicforms.Form
import com.matchi.dynamicforms.Submission
import com.matchi.dynamicforms.SubmissionValue
import com.matchi.facility.FacilityCourseSubmissionController
import grails.test.mixin.*
import grails.test.mixin.domain.DomainClassUnitTestMixin
import grails.test.mixin.web.GroovyPageUnitTestMixin
import spock.lang.*

import javax.servlet.http.HttpServletResponse

import static com.matchi.TestUtils.*
import static plastic.criteria.PlasticCriteria.* ; // mockCriteria() method

/**
 * See the API for {@link grails.test.mixin.web.ControllerUnitTestMixin} for usage instructions
 */
@TestFor(FacilityCourseSubmissionController)
@Mock([Submission, Facility, Region, Form, Municipality, CourseActivity, Customer, User, SubmissionValue])
@TestMixin([DomainClassUnitTestMixin, GroovyPageUnitTestMixin])
class FacilityCourseSubmissionControllerSpec extends Specification{

    Form form
    Facility facility
    Submission submission

    Long testSubmissionId

    def setup() {
        def securityServiceMock = mockFor(SecurityService)

        facility = createFacilityFor(name: "Beautiful facility")
        form = createFormFor(facility: facility)
        Customer customer = createCustomerFor(facility: facility)
        submission = createSubmissionFor(form: form, customer: customer);
        testSubmissionId = submission.getId()
        mockCriteria(Submission)

        securityServiceMock.demand.getUserFacility { -> facility}
        controller.securityService = securityServiceMock.createMock()
    }

    void "Course name is in model for correct data"() {
        given: "Proper environment with logged in user"
            createCourseFor(name: "Test course", form: form, facility: facility)

        when:"The index action is executed"
            def result = controller.show(testSubmissionId)
        then:"The model is correct"
            assert response.status == HttpServletResponse.SC_OK
            assert result.submission
            assert result.course.name == "Test course"
    }

    void "Submission without course displayed withour course information"() {
        given: "Proper environment with logged in user"
            //createCourseFor(name: "Test course", form: form, facility: facility)

        when:"The index action is executed"
            def result = controller.show(testSubmissionId)
        then:"The model is correct"
            assert response.status == HttpServletResponse.SC_OK
            assert result.submission
            assert result.course == null
    }


    void "Course without facility displayed properly"() {
        given: "Proper environment with logged in user"
            createCourseFor(name: "Test course", form: form, facility: null)

        when:"The index action is executed"
            def result = controller.show(testSubmissionId)
        then:"The model is correct"
            assert response.status == HttpServletResponse.SC_OK
            assert result.submission
            assert result.course.name == "Test course"
    }

    void "Course without form is not displayed"() {
        given: "Proper environment with logged in user"
            createCourseFor(name: "Test course", form: null, facility: facility)

        when:"The index action is executed"
            def result = controller.show(testSubmissionId)
        then:"The model is correct"
            assert response.status == HttpServletResponse.SC_OK
            assert result.submission
            assert result.course == null
    }

    void "Wrong submission id return 404"() {
        given: "Proper environment with logged in user"
            createCourseFor(name: "Test course", form: null, facility: facility)

        when:"The index action is executed"
            def result = controller.show(testSubmissionId - 100) //wrong submission Id
        then:"The model is correct"
            assert response.status == HttpServletResponse.SC_NOT_FOUND
    }

    void "View rendered correctly if course is valid"() {
        given: "Correctly created course"
            CourseActivity course = createCourseFor(name: "Test course", form: form, facility: facility)
        when: "Page is rendered"
            String output = render(
                    view: '/facilityCourseSubmission/show',
                    model: [submission: submission, submissionValues: null, course: course])
        then: "View rendered without errors"
            output.contains 'Test course'
            assertFalse output.contains('Error')
    }

    void "View rendered correctly if course is NULL"() {
        given: "Course is null"
            CourseActivity course = null
        when: "Page is rendered"
            String output = render(
                view: '/facilityCourseSubmission/show',
                model: [submission: submission, submissionValues: null, course: course])
        then: "View rendered without errors"
            output.contains 'facilitySubmission.show.title'
            assertFalse output.contains('Test course')
            assertFalse output.contains('Error')
    }
}
