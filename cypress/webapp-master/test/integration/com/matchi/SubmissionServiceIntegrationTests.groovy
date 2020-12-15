package com.matchi

import com.matchi.dynamicforms.Submission

import static com.matchi.TestUtils.*

/**
 * @author Sergei Shushkevich
 */
class SubmissionServiceIntegrationTests extends GroovyTestCase {

    def submissionService

    void testGetAllSubmissions() {
        def facility = createFacility()
        def form = createForm(facility)
        def course = createCourse(facility, new Date(), new Date() + 1, form)
        def user1 = createUser("u1@matchi.se")
        def customer1 = createCustomer(facility)
        customer1.deleted = new Date()
        customer1.save(failOnError: true)
        def sub1 = createSubmission(customer1, form, user1)
        sub1.status = Submission.Status.WAITING
        sub1.save(failOnError: true)
        def user2 = createUser("u2@matchi.se")
        def customer2 = createCustomer(facility)
        def sub2 = createSubmission(customer2, form, user2)
        sub2.status = Submission.Status.WAITING
        sub2.save(failOnError: true, flush: true)

        def result = submissionService.getAllSubmissions(facility, null)

        assert result.size() == 1
        assert result[0] == sub2
    }
}