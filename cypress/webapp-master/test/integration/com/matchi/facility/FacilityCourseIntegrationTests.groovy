package com.matchi.facility

import com.matchi.activities.ActivityOccasion
import com.matchi.activities.trainingplanner.CourseActivity
import com.matchi.activities.trainingplanner.TrainingCourt
import com.matchi.dynamicforms.Form
import com.matchi.dynamicforms.FormField
import com.matchi.dynamicforms.Submission
import org.apache.http.HttpStatus
import org.joda.time.LocalDate
import org.joda.time.LocalTime

import static com.matchi.TestUtils.*

/**
 * @author Michael Astreiko
 */
class FacilityCourseIntegrationTests extends GroovyTestCase {
    def controller = new FacilityCourseController()
    def springSecurityService

    @Override
    void setUp() {
        super.setUp()

    }

    void testUpdateWithNewForm() {
        def user1 = createUser()
        def facility1 = createFacility()
        user1.facility = facility1
        user1.save()
        def form1 = createForm(facility1)
        form1.addToFields(new FormField(label: "l1", type: FormField.Type.TEXT.name()))
                .addToFields(new FormField(label: "l2", type: FormField.Type.PERSONAL_INFORMATION.name()))
                .save(flush: true, failOnError: true)
        new Submission(form: form1).save(flush: true, failOnError: true)
        def course1 = createCourse(facility1, new Date() - 1, new Date() + 1, form1)

        springSecurityService.reauthenticate user1.email

        form1 = Form.get(form1.id)
        Long form1Id = form1.id
        assert form1.id == course1.form.id

        controller.update(course1.id, course1.version)

        form1 = Form.get(form1Id)
        assert course1.form?.id

        CourseActivity.withNewSession {
            course1 = CourseActivity.get(course1.id)
            assert form1.id == course1.form?.id
            assert form1.hash == course1.form?.hash
        }
    }

    void testRemoveTrainingCourtIfHasOccasion() {
        def facility = createFacility()
        def trainingCourt = createTrainingCourt(facility)
        new ActivityOccasion(message: "msg", date: new LocalDate(),
                startTime: new LocalTime(), endTime: new LocalTime(),
                court: trainingCourt, activity: createCourse(facility))
                .save(flush: true, failOnError: true)
        assert 1 == TrainingCourt.count()
        controller.params.id = trainingCourt.id

        controller.removeTrainingCourt()

        assert 1 == TrainingCourt.count()
        assert controller.flash.error
        assert HttpStatus.SC_BAD_REQUEST == Integer.parseInt(controller.response.contentAsString)
    }
}
