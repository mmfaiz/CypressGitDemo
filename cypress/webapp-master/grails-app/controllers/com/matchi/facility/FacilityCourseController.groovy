package com.matchi.facility
import com.matchi.Court
import com.matchi.Facility
import com.matchi.GenericController
import com.matchi.Season
import com.matchi.SubmissionService
import com.matchi.activities.Activity
import com.matchi.activities.ActivityOccasion
import com.matchi.activities.Participant
import com.matchi.activities.trainingplanner.CourseActivity
import com.matchi.activities.trainingplanner.Trainer
import com.matchi.activities.trainingplanner.TrainingCourt
import com.matchi.dynamicforms.Form
import com.matchi.dynamicforms.FormField
import com.matchi.dynamicforms.FormFieldValue
import com.matchi.dynamicforms.FormTemplate
import com.matchi.dynamicforms.Submission
import grails.validation.Validateable
import org.apache.http.HttpStatus
import org.hibernate.criterion.CriteriaSpecification
import org.joda.time.LocalDate
import org.joda.time.LocalTime
import org.springframework.dao.DataIntegrityViolationException

import javax.servlet.http.HttpServletResponse
/**
 * @author Sergei Shushkevich
 */
class FacilityCourseController extends GenericController {
    final static Integer MAX_NUMBER_OF_SUBMISSIONS = 1000000000
    static allowedMethods = [save          : "POST", update: "POST", delete: "POST",
                             addOccasion   : "POST", addOccasionParticipation: "POST", addOccasionParticipationFromSubmission: "POST",
                             removeOccasion: "DELETE", removeOccasionParticipation: "DELETE",
                             updateCourseHintColor: "PUT"]

    static templateRootURI = "/rendering/occasions"

    def activityService
    def courseParticipantService
    def editParticipantService
    def submissionService

    def index() {
        def facility = getUserFacility()

        params.max = Math.min(params.max ? params.int('max') : 50, 100)
        if (!params.sort) {
            params.sort = "name"
            params.order = "asc"
        }

        [
            courseInstanceList: CourseActivity.activeAndUpcomingCourses(facility).list(params),
            courseInstanceTotal: CourseActivity.activeAndUpcomingCourses(facility).count(),
            facility: facility
        ]
    }

    def archive() {
        def facility = getUserFacility()

        params.max = Math.min(params.max ? params.int('max') : 50, 100)
        if (!params.sort) {
            params.sort = "name"
            params.order = "asc"
        }

        render(view: "index", model: [
            courseInstanceList: CourseActivity.archivedCourses(facility).list(params),
            courseInstanceTotal: CourseActivity.archivedCourses(facility).count(),
            facility: facility
        ])
    }

    def create() {
        buildModel()
    }

    def save() {
        def courseInstance = new CourseActivity()
        Facility facility = getUserFacility()

        bindData(courseInstance, params)
        if (params.'form.relatedFormTemplate.id') {
            initAndSaveCourseForm(courseInstance?.form, params, courseInstance, facility)
        } else {
            courseInstance?.form = null
        }
        courseInstance.facility = facility
        if (courseInstance.save(flush: true)) {
            flash.message = message(code: 'default.created.message',
                    args: [message(code: 'course.label', default: 'Course'), courseInstance.name])
            redirect(action: "index")
        } else {
            render(view: "create", model: buildModel(courseInstance))
        }
    }


    def edit() {
        Facility facility = getUserFacility()
        def courseInstance = CourseActivity.findByIdAndFacility(params.id, facility)

        if (courseInstance) {
            assertFacilityAccessTo(courseInstance)
            buildModel(courseInstance, facility)
        } else {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'course.label'), courseInstance.name])
            redirect(action: "index")
        }
    }

    def update(Long id, Long version) {
        Facility facility = getUserFacility()
        def courseInstance = CourseActivity.findByIdAndFacility(id, facility)
        if (!courseInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'course.label'), courseInstance?.name])
            redirect(action: "index")
            return
        }

        assertFacilityAccessTo(courseInstance)

        if (version != null) {
            if (courseInstance.version > version) {
                courseInstance.errors.rejectValue("version", "default.optimistic.locking.failure",
                        [message(code: 'course.label', default: 'Course')] as Object[],
                        "Another user has updated this Course while you were editing")
                render(view: "edit", model: [courseInstance: courseInstance,
                                             formTemplates : FormTemplate.shared(facility).listDistinct(),
                                             seasons       : Season.findAllByFacilityAndEndTimeGreaterThan(facility, new Date())])
                return
            }
        }

        courseInstance.trainers?.clear()

        List<Integer> levels = params.useLevel ? params.level.toString().split(",").collect { Integer.parseInt(it) } : [null, null]

        params.levelMin = levels.first()
        params.levelMax = levels.last()

        bindData(courseInstance, params, [exclude: ['form*']])

        Form form = courseInstance?.form
        bindData(form, params.form)

        if (params.noLimit) {
            form.maxSubmissions = null
        }
        def newFields = parseFormFields(form, params)
        def fieldsToDelete = form.fields.findAll { f ->
            !newFields.find { it.id == f.id }
        }
        form.fields?.clear()
        newFields?.each {
            form.addToFields(it)
        }
        courseInstance.form = form
        if (!courseInstance.save(flush: true)) {
            render(view: "edit", model: buildModel(courseInstance))
            return
        }

        fieldsToDelete.each {
            it.delete()
        }

        flash.message = message(code: 'default.updated.message', args: [message(code: 'course.label'), courseInstance.name])

        redirect(action: "index")
    }

    def delete() {
        def courseInstance = CourseActivity.findByIdAndFacility(params.id, getUserFacility())
        if (!courseInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'course.label'), courseInstance.name])
            redirect(action: "index")
            return
        }

        assertFacilityAccessTo(courseInstance)

        try {
            courseInstance.delete(flush: true)
            flash.message = message(code: 'default.deleted.message', args: [message(code: 'course.label'), params.id])
        } catch (DataIntegrityViolationException e) {
            flash.message = message(code: 'default.not.deleted.message', args: [message(code: 'course.label'), params.id])
        }

        redirect(action: "index")
    }

    def planning(FilterOccasionCommand filter) {
        def facility = getUserFacility()
        def now = new Date()

        List<CourseActivity> activeCourses = []
        if(filter.seasonIds) {
            List<Season> seasons = Season.findAllByIdInList(filter.seasonIds)

            if(seasons) {
                activeCourses = CourseActivity.createCriteria().list {
                    eq('facility', facility)

                    // The condition is that a course is at some point within the season filter
                    or {
                        seasons.each { Season season ->
                            or {
                                //the course starts before season, ends mid season
                                and {
                                    and {
                                        gte('endDate', season.startTime)
                                        lte('endDate', season.endTime)
                                    }
                                    and {
                                        lte('startDate', season.startTime)
                                        lte('startDate', season.endTime)
                                    }
                                }
                                //the course starts mid season, ends after season
                                and {
                                    and {
                                        gte('endDate', season.startTime)
                                        gte('endDate', season.endTime)
                                    }
                                    and {
                                        gte('startDate', season.startTime)
                                        gte('startDate', season.endTime)
                                    }
                                }
                                //the course starts before season, ends after season
                                and {
                                    and {
                                        gte('endDate', season.startTime)
                                        gte('endDate', season.endTime)
                                    }
                                    and {
                                        lte('startDate', season.startTime)
                                        lte('startDate', season.endTime)
                                    }
                                }
                                //the course is mid season
                                and {
                                    and {
                                        gte('endDate', season.startTime)
                                        lte('endDate', season.endTime)
                                    }
                                    and {
                                        gte('startDate', season.startTime)
                                        lte('startDate', season.endTime)
                                    }
                                }

                            }
                        }
                    }
                }
            }
        } else {
            activeCourses = CourseActivity.findAllByFacilityAndEndDateGreaterThanEquals(
                    facility, now)
        }

        List<Trainer> trainers = Trainer.findAllByFacilityAndIsActive(facility, true)
        def occasions = activityService.findCourseOccasionsByHourOfDay(filter, facility, now)

        [ filter: filter, facility: facility, activeCourses: activeCourses,
          occasions: occasions, trainers: trainers ]
    }

    def settings() {
        def facility = getUserFacility()

        [
            trainingCourts: TrainingCourt.findAllByFacility(facility),
            facilityCourts: Court.available(facility).list(),
            facility: facility
        ]
    }

    def addTrainingCourt() {
        def facility = getUserFacility()

        def trainingCourt
        if (params.court) {
            def court = Court.get(params.long('court'))
            trainingCourt = new TrainingCourt(name: court.name, facility: facility, court: court)
        } else if (params.name) {
            if (isTrainingCourtExist(params.name, facility)) {
                render HttpStatus.SC_BAD_REQUEST
                return
            }
            trainingCourt = new TrainingCourt(name: params.name, facility: facility)
        }

        trainingCourt?.save(flush: true)

        render(template: "/facilityCourse/trainingCourts", model: [trainingCourts: TrainingCourt.findAllByFacility(facility),
                                                                   facilityCourts: Court.available(facility).list()])
    }

    def updateTrainingCourt() {
        def facility = getUserFacility()

        def trainingCourt
        if (params.name) {
            if (isTrainingCourtExist(params.name, facility)) {
                render HttpStatus.SC_BAD_REQUEST
                return
            }
            trainingCourt = TrainingCourt.get(params.long('id'))
            trainingCourt?.name = params.name
        }

        trainingCourt?.save(flush: true)

        render(template: "/facilityCourse/trainingCourts", model: [trainingCourts: TrainingCourt.findAllByFacility(facility),
                                                                   facilityCourts: Court.available(facility).list()])
    }

    def removeTrainingCourt() {
        def facility = getUserFacility()

        if (params.id) {
            def court = TrainingCourt.get(params.long('id'))
            try {
                court.delete(flush: true)
            } catch (Exception ex) {
                flash.error = message(code: "facilityTrainingCourts.delete.error")
                render HttpStatus.SC_BAD_REQUEST
                return
            }
        }

        render(template: "/facilityCourse/trainingCourts", model: [trainingCourts: TrainingCourt.findAllByFacility(facility),
                                                                   facilityCourts: Court.available(facility).list()])
    }

    def printOccasions(FilterOccasionCommand filter) {
        def facility = getUserFacility()
        def occasions = activityService.findCourseOccasionsByHourOfDay(filter, facility, new Date())
        List<CourseActivity> activeCourses = CourseActivity.findAllByFacilityAndEndDateGreaterThanEquals(facility, new Date())
        def template = "${templateRootURI}/${params.boolean("view") ? "occasionsView" : "occasions"}"
        def pdf = getPdfFile(template, [occasions: occasions, facility: facility, activeCourses: activeCourses])
        render(file:pdf,contentType: 'application/pdf')
    }

    /**
     * Ajax
     */
    def saveOccasion() {
        def courseInstance = CourseActivity.findByIdAndFacility(params.course ?: params.activeCourse, getUserFacility())

        assertFacilityAccessTo(courseInstance)

        def startTimeHour = params.startTime?.tokenize(":")[0]
        def startTimeMin = params.startTime?.tokenize(":")[1]
        def endTimeHour = params.endTime?.tokenize(":")[0]
        def endTimeMin = params.endTime?.tokenize(":")[1]

        ActivityOccasion occasion = new ActivityOccasion()
        if (params.occasionId) {
            occasion = ActivityOccasion.get(params.long('occasionId'))
            occasion.trainers = []
            if (courseInstance) {
                courseInstance.removeFromOccasions(occasion)
            }
        }

        occasion.startTime = new LocalTime().withHourOfDay(Integer.parseInt(startTimeHour)).withMinuteOfHour(Integer.parseInt(startTimeMin)).withSecondOfMinute(0)
        occasion.endTime = new LocalTime().withHourOfDay(Integer.parseInt(endTimeHour)).withMinuteOfHour(Integer.parseInt(endTimeMin)).withSecondOfMinute(0)
        occasion.court = TrainingCourt.findById(params.courtId)
        def dayOfWeek = params.int("dayOfWeek", 1)
        if (occasion.date) {
            if (dayOfWeek != occasion.date.getDayOfWeek()) {
                occasion.date = occasion.date.withDayOfWeek(dayOfWeek)
            }
        } else {
            occasion.date = new LocalDate().withDayOfWeek(dayOfWeek)
        }
        bindData(occasion, params, [include: ['trainers', 'message'], exclude: ['id']])
        courseInstance.addToOccasions(occasion)

        if (courseInstance.save(flush: true)) {
            render(template: "/templates/trainingPlanner/occasion", model: [occasion: occasion])
            return
        }

        render HttpStatus.SC_BAD_REQUEST
    }

    def removeOccasion() {
        def courseInstance = CourseActivity.findByIdAndFacility(params.id, getUserFacility())
        def occasion = ActivityOccasion.findById(params.occasionId)

        assertFacilityAccessTo(courseInstance)

        courseInstance.removeFromOccasions(occasion)
        occasion.delete(flush: true)
        if (courseInstance.save()) {
            render HttpStatus.SC_OK
            return
        }

        render HttpStatus.SC_BAD_REQUEST
    }

    def addParticipant() {
        Participant participant = Participant.findById(params.participantId)
        CourseActivity course = CourseActivity.findById(params.courseId)

        assertFacilityAccessTo(course)

        if(!participant || !course) {
            render status: HttpStatus.SC_BAD_REQUEST
            return
        }

        if(course.isAlreadyParticipant(participant.customer.id)) {
            Participant existingParticipant = course.participants?.asList().find { Participant p ->
                return p.customer.id == participant.customer.id
            }

            render(status: HttpStatus.SC_OK, contentType: 'application/json') {[
                    'id': existingParticipant.id,
                    'oldHintColor': participant.activity.hintColor,
                    'hintColor': course.hintColor
            ]}

            return
        }

        Participant newParticipant

        Participant.withTransaction {
            newParticipant = editParticipantService.copyParticipantWithSubmission(participant, course, true)
        }

        render(status: HttpStatus.SC_OK, contentType: 'application/json') {[
                'id': newParticipant.id,
                'oldHintColor': participant.activity.hintColor,
                'hintColor': course.hintColor
        ]}
    }

    def addOccasionParticipation() {
        def occasion = ActivityOccasion.findById(params.occasionId)
        def participant = Participant.findById(params.participantId)

        assertFacilityAccessTo(occasion.activity)

        occasion.addToParticipants(participant)

        if (occasion.save()) {
            if(!participant.status.equals(Participant.Status.ACTIVE)) {
                participant.status = Participant.Status.ACTIVE
                if(participant.save()) {
                    render HttpStatus.SC_OK
                    return
                }
            } else {
                render HttpStatus.SC_OK
                return
            }
        }

        render HttpStatus.SC_BAD_REQUEST
    }

    def addOccasionParticipationFromSubmission() {
        def occasion = ActivityOccasion.findById(params.occasionId)
        def submission = Submission.findById(params.submissionId)

        CourseActivity.HintColor hintColor = submission.form.course.hintColor

        assertFacilityAccessTo(occasion.activity)

        if (occasion?.maxNumParticipants && occasion?.participants?.size() >= occasion?.maxNumParticipants) {
            render status: HttpStatus.SC_UNAUTHORIZED
            return
        }

        submissionService.acceptSubmissionsToCourse([(submission.id): occasion.activity.id])
        Participant participant = Participant.findByCustomerAndActivity(submission.customer, occasion.activity)
        occasion.addToParticipants(participant)

        if (occasion.save()) {
            participant.status = Participant.Status.ACTIVE
            if(participant.save()) {
                render(status: HttpStatus.SC_OK, contentType: 'application/json') {[
                        'id': participant.id,
                        'oldHintColor': hintColor,
                        'hintColor': (occasion.activity as CourseActivity).hintColor
                ]}

                return
            }
        }

        render status: HttpStatus.SC_BAD_REQUEST
    }

    def removeOccasionParticipation() {
        if (!params.occasionId || !params.participantId) {
            log.warn("No occasionId or participantId in request")
            render HttpStatus.SC_BAD_REQUEST
            return
        }

        def occasion = ActivityOccasion.findById(params.occasionId)
        def participant = Participant.findById(params.participantId)

        if (!occasion) {
            log.warn("No occasion found for occasionId ${params.occasionId}")
            render HttpStatus.SC_NOT_FOUND
            return
        }
        assertFacilityAccessTo(occasion.activity)

        if (participant) {
            occasion.removeFromParticipants(participant)
        }

        if (occasion.save()) {

            if(participant?.occasions?.size() == 0) {
                participant.status = Participant.Status.RESERVED
                participant.save()
            }

            render HttpStatus.SC_OK
            return
        }

        render HttpStatus.SC_BAD_REQUEST
    }

    def updateCourseHintColor() {
        def course = CourseActivity.findById(params.courseId)

        assertFacilityAccessTo(course)

        course.hintColor = params.hintColor

        if (course.save()) {
            render HttpStatus.SC_OK
            return
        }

        render HttpStatus.SC_BAD_REQUEST
    }

    def getFormTemplate() {
        FormTemplate formTemplate = FormTemplate.get(params.formTemplateId)
        if (formTemplate) {
            render template: 'formFields', model: [formFields: formTemplate?.templateFields]
        }
    }

    /**
     * Ajax
     */
    def filterParticipants(FilterCourseParticipantCommand cmd) {
        def facility = getUserFacility()

        cmd.courses  = params.list('courses[]')?.collect { Long.parseLong(it) }
        cmd.statuses = params.list('statuses[]')?.collect { Participant.Status.valueOf(it) }

        // If no courses selected, show no participants
        if(!(cmd.courses?.size() > 0)) {
            render template: 'participants', model: [participants: []]
            return
        }

        def participants = courseParticipantService.findParticipants(facility, cmd).collect { Participant participant ->
            def returnObject = [
                    id: participant.id,
                    extendedId: "participant-${participant.id}",
                    numberOfOccasionsFromSubmission: participant.getNumberOfOccasionsFromSubmission(),
                    nOccasions: participant.occasions?.size() ?: 0,
                    status: participant.status,
                    activity: [
                            id: participant.activity.id,
                            hintColor: participant.activity.hintColor
                    ],
                    customer: [
                            id: participant.customer.id,
                            fullName: participant.customer.fullName(),
                            birthyear: participant.customer.birthyear
                    ]
            ]

            if(participant.customer.user) {
                returnObject['customer']['user'] = [
                        id: participant.customer.user.id
                ]
            }

            if(participant.submission) {
                returnObject['submission'] = [
                        id: participant.submission.id
                ]
            }

            return returnObject
        }

        List<Long> submissionIds = participants.findAll { it.submission != null }.collect { it.submission.id }

        if(!(cmd.statuses?.size() > 0)) {
            Submission.createCriteria().list() {
                createAlias('form', 'f')
                createAlias('f.course', 'crs', CriteriaSpecification.LEFT_JOIN)

                eq('f.facility', facility)
                inList('status', [Submission.Status.WAITING])

                if(submissionIds?.size() > 0) {
                    not {
                        inList('id', submissionIds)
                    }
                }

                // Submissions must be course submissions
                isNotNull('crs.id')

                if(cmd.courses.size() > 0) {
                    inList('crs.id', cmd.courses)
                }
            }.each { Submission submission ->
                def returnObject = [
                        id: submission.id,
                        extendedId: "submission-${submission.id}",
                        numberOfOccasionsFromSubmission: submission.values?.find { it.fieldType.equals(FormField.Type.NUMBER_OF_OCCASIONS.toString()) }?.value ?: "0",
                        nOccasions: 0,
                        activity: [
                                id: submission.form.course.id,
                                hintColor: submission.form.course.hintColor
                        ],
                        customer: [
                                id: submission.customer.id,
                                fullName: submission.customer.fullName(),
                                birthyear: submission.customer.birthyear
                        ],
                        submission: [
                                id: submission.id
                        ]
                ]

                if(submission.customer.user) {
                    returnObject['customer']['user'] = [
                            id: submission.customer.user.id
                    ]
                }

                participants.add(returnObject)
            }
        }


        participants = participants.sort {
            return it.customer.fullName
        }

        render template: 'participants', model: [participants: participants]
    }

    def preview() {
        def courseInstance = new CourseActivity()

        bindData(courseInstance, params)

        courseInstance.form.facility = getUserFacility()
        courseInstance.form.course = courseInstance
        courseInstance.form.fields = parseFormFields(courseInstance.form, params)
        courseInstance.form.fields.eachWithIndex { field, idx ->
            field.id = idx
        }
        courseInstance.discard()

        render view: "/form/show", model: [formInstance: courseInstance.form,
                                           fields: courseInstance.form.fields.findAll { it.isActive }, preview: true]
    }

    def addOccasionTrainer(Long occasionId, Long trainerId) {
        def occasion = ActivityOccasion.findById(occasionId, [fetch: [activity: "join"]])
        assertFacilityAccessTo(occasion.activity)

        occasion.addToTrainers(Trainer.get(trainerId))

        if (occasion.save()) {
            render HttpStatus.SC_OK
        } else {
            render HttpStatus.SC_BAD_REQUEST
        }
    }

    def removeOccasionTrainer(Long occasionId, Long trainerId) {
        def occasion = ActivityOccasion.findById(occasionId, [fetch: [activity: "join"]])
        assertFacilityAccessTo(occasion.activity)

        occasion.removeFromTrainers(Trainer.get(trainerId))
        if (occasion.save()) {
            render HttpStatus.SC_OK
        } else {
            render HttpStatus.SC_BAD_REQUEST
        }
    }

    /**
     * Ajax
     */
    def swapListPosition(Long id1, Long id2) {
        def course1 = CourseActivity.get(id1)
        def course2 = CourseActivity.get(id2)
        if (course1 && course2) {
            activityService.swapListPosition(course1, course2)
            render ""
        } else {
            response.sendError HttpServletResponse.SC_BAD_REQUEST
        }
    }

    private void initAndSaveCourseForm(Form form, params, courseInstance, facility) {
        form.fields = []
        form.course = courseInstance
        form.name = form?.relatedFormTemplate?.name
        ArrayList newFormFields = parseFormFields(form, params)

        newFormFields.each {
            form.addToFields(it)
        }
        form.facility = facility
        form.save(flush: true, failOnError: true)
    }

    private ArrayList<FormField> parseFormFields(Form form, params) {
        def newFormFields = []
        def formFieldIds = params.list("formFieldId")

        if (formFieldIds.size() > 1) {
            formFieldIds.sort {
                params.int("fieldPosition$it", -1)
            }
        }

        formFieldIds.collect{ it as Long }.eachWithIndex { id, index ->
            FormField formField = form.fields.find { it.id == id }
            if (!formField) {
                FormField templateField = FormField.get(id)
                formField = new FormField(templateField.properties)
                formField.predefinedValues?.clear()
                templateField.predefinedValues.eachWithIndex { pv, pvIdx ->
                    formField.addToPredefinedValues(new FormFieldValue(pv.properties))
                }
            }

            if (formField.isEditable) {
                formField.isActive = Boolean.valueOf(params."field.isActive$id")
                formField.isRequired = params."field.isRequired$id" == 'on'

                if (formField.isActive && formField.typeEnum.customizable) {
                    formField.predefinedValues.collect().each {
                        formField.removeFromPredefinedValues(it)
                        it.delete()
                    }
                    bindData(formField, params.field."$id")
                }
            }

            formField.form = form
            newFormFields << formField
        }

        newFormFields
    }

    private Map buildModel(CourseActivity courseInstance = new CourseActivity(params), Facility facility = getUserFacility()) {
        [courseInstance: courseInstance,
         formTemplates : FormTemplate.shared(facility).listDistinct(),
         seasons       : Season.findAllByFacilityAndEndTimeGreaterThan(facility, new Date()),
         trainers      : Trainer.findAllByFacilityAndIsActive(facility, true),
         facility      : facility]
    }

    private boolean isTrainingCourtExist(String name, Facility facility, Court court = null) {
        def result = false

        def existTrainingCourt = court ?
                TrainingCourt.findAllByFacilityAndCourtAndName(facility, court, name) :
                TrainingCourt.findAllByFacilityAndName(facility, name)
        if (existTrainingCourt) {
            result = true
        }

        return result
    }
}

@Validateable(nullable = true)
class FilterOccasionCommand {

    List<Long> seasonIds = []
    List<Long> courseIds = []
    List<Long> trainerIds = []
}
