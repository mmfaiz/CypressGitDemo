package com.matchi.viewhelpers

import com.matchi.Customer
import com.matchi.User
import com.matchi.activities.Activity
import com.matchi.activities.Participant
import com.matchi.activities.trainingplanner.CourseActivity
import com.matchi.dynamicforms.FormField
import grails.util.Holders
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsParameterMap

abstract class ParticipantViewHelper {

    def activityService = Holders.grailsApplication.mainContext.getBean('activityService')
    def editParticipantService = Holders.grailsApplication.mainContext.getBean('editParticipantService')

    void setup(User user, def flow, GrailsParameterMap params) {
        flow.returnUrl = params.returnUrl
        flow.actionTitle = params.actionTitle
        flow.facility = user.facility

        List<CourseActivity> courses = activityService.getCurrentAndUpcomingActivities(
                CourseActivity, user.facility, FormField.Type.PERSONAL_INFORMATION
        )

        flow.courses = []
        courses.each { Activity a ->
            flow.courses << [name: a.name, id: a.id]
        }
    }

    /**
     * Filters out the participants not already in course
     * @param participants
     * @return
     */
    private List<Participant> getParticipantsToChange(List customers) {
        return customers
                .findAll { !it.alreadyParticipant && !it.alreadyInFlow }
                .collect { Participant.get(it.id) }
    }

    /**
     * Filters out the customers not already in course
     * @param participants
     * @return
     */
    private List<Customer> getCustomersToChange(List customers) {
        return customers
                .findAll { !it.alreadyParticipant && !it.alreadyInFlow }
                .collect { Customer.get(it.customerId) }
    }

    /**
     * The action to be performed when starting a EditParticipant flow
     */
    abstract void entryAction(GrailsParameterMap params, def user, def flow)

    /**
     * Check if a participant already is in course. Based on participants or customers
     */
    void checkAlreadyParticipants(def flow, CourseActivity chosenCourse) {
        List<Long> customersAlreadyInFlow = []

        flow.customers.each {
            it.alreadyInFlow = customersAlreadyInFlow.contains(it.customerId)
            it.alreadyParticipant = chosenCourse.isAlreadyParticipant(it.customerId)

            if(!it.alreadyInFlow && !it.alreadyParticipant) {
                customersAlreadyInFlow.push(it.customerId)
            }
        }
    }

    /**
     * Executes the changes
     * @param flow
     * @param course
     */
    abstract int execute(def flow, CourseActivity course)
}
