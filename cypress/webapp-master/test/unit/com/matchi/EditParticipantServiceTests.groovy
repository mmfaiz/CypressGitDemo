package com.matchi

import com.matchi.activities.ActivityOccasion
import com.matchi.activities.Participant
import com.matchi.activities.trainingplanner.CourseActivity
import com.matchi.activities.trainingplanner.CourseParticipantService
import com.matchi.courses.EditParticipantService
import com.matchi.dynamicforms.Form
import com.matchi.dynamicforms.Submission
import grails.test.GrailsMock
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin

import static com.matchi.TestUtils.*

@TestFor(EditParticipantService)
@TestMixin(GrailsUnitTestMixin)
@Mock([Region, Municipality, Facility, Customer, CourseActivity, User, Form, Participant, ActivityOccasion])
class EditParticipantServiceTests {

    void test() {
        assert true
    }

}

