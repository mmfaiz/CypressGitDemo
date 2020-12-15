package com.matchi

import com.matchi.activities.Participant
import com.matchi.activities.trainingplanner.CourseActivity
import com.matchi.dynamicforms.Form
import com.matchi.dynamicforms.Submission
import grails.test.mixin.*
import static com.matchi.TestUtils.*;

/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@TestFor(SubmissionService)
@Mock([Region, Municipality, Facility, Customer, CourseActivity, User, Form, Participant, Submission])
class SubmissionServiceSpec {

    void testAcceptSubmissions() {
        Submission submission = createSubmission()
        CourseActivity course = createCourse(submission.form.facility, new Date(), new Date() + 1, submission.form)

        Map map = [(submission.id): course.id]
        service.acceptSubmissionsToCourse(map)

        Participant participant = Participant.findBySubmission(submission)
        assert participant != null
        assert participant.customer == submission.customer
        assert participant.activity == course
    }
}
