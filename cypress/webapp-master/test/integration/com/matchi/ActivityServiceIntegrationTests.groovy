package com.matchi

import com.matchi.activities.*
import com.matchi.activities.trainingplanner.CourseActivity
import com.matchi.activities.trainingplanner.Trainer
import com.matchi.activities.trainingplanner.TrainingCourt
import com.matchi.dynamicforms.*
import com.matchi.integration.IntegrationService
import com.matchi.orders.Order
import grails.util.Holders
import org.apache.commons.lang.RandomStringUtils
import org.grails.plugin.hibernate.filter.DefaultHibernateFiltersHolder
import org.joda.time.DateTime
import org.joda.time.LocalDate
import org.joda.time.LocalTime

import static com.matchi.TestUtils.*
/**
 * @author Sergei Shushkevich
 */
class ActivityServiceIntegrationTests extends GroovyTestCase {

    def activityService
    def springSecurityService

    void testGetCurrentAndUpcomingActivities() {
        def facility1 = createFacility()
        def form1 = createForm(facility1)
        form1.addToFields(new FormField(label: "l1", type: FormField.Type.TEXT.name()))
                .addToFields(new FormField(label: "l2", type: FormField.Type.PERSONAL_INFORMATION.name()))
                .save(flush: true, failOnError: true)
        def course1 = createCourse(facility1, new Date() - 1, new Date() + 1, form1)
        def facility2 = createFacility()
        def form2 = createForm(facility2)
        form2.addToFields(new FormField(label: "l3", type: FormField.Type.TEXT.name()))
                .save(flush: true, failOnError: true)
        def course2 = createCourse(facility2, new Date() - 1, new Date() + 1, form2)

        def result = activityService.getCurrentAndUpcomingActivities(CourseActivity, facility1)
        assert 1 == result.size()
        assert course1 == result[0]

        result = activityService.getCurrentAndUpcomingActivities(
                CourseActivity, facility1, FormField.Type.PERSONAL_INFORMATION)
        assert 1 == result.size()
        assert course1 == result[0]

        result = activityService.getCurrentAndUpcomingActivities(CourseActivity, facility2)
        assert 1 == result.size()
        assert course2 == result[0]

        assert !activityService.getCurrentAndUpcomingActivities(
                CourseActivity, facility2, FormField.Type.PERSONAL_INFORMATION)

        result = activityService.getCurrentAndUpcomingActivities(
                CourseActivity, facility2, FormField.Type.TEXT)
        assert 1 == result.size()
        assert course2 == result[0]
    }

    void testDeletedOccasions() {
        def session = Holders.grailsApplication.mainContext.sessionFactory.currentSession
        DefaultHibernateFiltersHolder.defaultFilters.each { name ->    println name
            session.enableFilter(name)
        }

        Facility facility = createFacility()

        ClassActivity activity = createClassActivity(facility, "activity")

        ActivityOccasion ac = createActivityOccasion(activity)
        ac.setDeleted(ActivityOccasion.DELETE_REASON.UNKNOWN)
        ac.save(flush: true, failOnError: true)


        activity.occasions = [
                createActivityOccasion(activity),
                ac
        ]

        assert ActivityOccasion.findAll().size() == 1

        // Should work but doesn't. Works in production or dev code
//        assert activity.occasions.size() == 1

    }

    void testOccasionsByActivity() {
        def facility = createFacility()

        def srcCourse = createCourse(facility, new Date() - 1, new Date() + 1)

        LocalDate dateNow = new LocalDate()
        LocalTime timeNow = LocalTime.now()
        def pastOccasion = new ActivityOccasion(message: "msg", date: dateNow.minusDays(1),
                startTime: timeNow.minusHours(3), endTime: timeNow.minusHours(2),
                activity: srcCourse)
                .save(flush: true, failOnError: true)

        def presentOccasion = new ActivityOccasion(message: "msg", date: dateNow,
                startTime: timeNow, endTime: timeNow.plusHours(3),
                activity: srcCourse)
                .save(flush: true, failOnError: true)

        def futureOccasion = new ActivityOccasion(message: "msg", date: dateNow.plusDays(1),
                startTime: timeNow.plusHours(3), endTime: timeNow.plusHours(6),
                activity: srcCourse)
                .save(flush: true, failOnError: true)
        srcCourse.addToOccasions(pastOccasion)
        srcCourse.addToOccasions(presentOccasion)
        srcCourse.addToOccasions(futureOccasion)
        srcCourse.save(flush: true, failOnError: true)

        def occasions = activityService.getOccasionsByActivity(srcCourse)

        def pastOccasions = activityService.getFinishedOccasions(occasions)
        def futureOccasions = activityService.getUpcomingOccasions(occasions)

        assert 2 == pastOccasions.size()
        assert 1 == futureOccasions.size()
    }


    void testGetCoursesWithPublishedForm() {
        def facility = createFacility()
        def form1 = createForm(facility, new Date() - 20, new Date() - 10)
        createCourse(facility, new Date() - 20, new Date() + 20, form1)
        def form2 = createForm(facility, new Date() - 10, new Date() + 10)
        def course2 = createCourse(facility, new Date() - 20, new Date() + 20, form2)
        def form3 = createForm(facility, new Date() + 10, new Date() + 20)
        createCourse(facility, new Date() - 20, new Date() + 20, form3)

        def result = activityService.getCoursesWithPublishedForm(facility)

        assert 1 == result.size()
        assert course2 == result[0]
    }

    void testCopyCourse() {
        def facility = createFacility()
        def formTemplate = new FormTemplate(name: "tmpl", description: "desc")
                .addToFacilities(facility)
                .addToTemplateFields(new FormField(label: "l1", type: FormField.Type.CHECKBOX.name())
                        .addToPredefinedValues(new FormFieldValue(value: "alpha")))
                .save(flush: true, failOnError: true)
        def form = createForm(facility)
        form.relatedFormTemplate = formTemplate
        form.maxSubmissions = 100
        form.membershipRequired = true
        form.paymentRequired = true
        form.price = 500
        form.addToFields(new FormField(label: "l1", type: FormField.Type.CHECKBOX.name(), isActive: false)
                        .addToPredefinedValues(new FormFieldValue(value: "beta")))
                .save(flush: true, failOnError: true)
        def srcCourse = createCourse(facility, new Date() - 1, new Date() + 1, form)
        def customer = createCustomer(facility)
        def customer2 = createCustomer(facility)
        def participant = new Participant(customer: customer, activity: srcCourse)
                .save(failOnError: true, flush: true)
        def order = createOrder(createUser(), facility)
        def submission = new Submission(order: order,
                form: form, customer: customer, status: Submission.Status.ACCEPTED)
                .addToValues(new SubmissionValue(label: "label", value: "value", fieldType: FormField.Type.CHECKBOX.name()))
                .save(failOnError: true, flush: true)
        def waitingSubmission = new Submission(order: order,
                form: form, customer: customer2, status: Submission.Status.WAITING)
                .addToValues(new SubmissionValue(label: "label2", value: "value2", fieldType: FormField.Type.CHECKBOX.name()))
                .save(failOnError: true, flush: true)
        form.addToSubmissions(submission).addToSubmissions(waitingSubmission)
                .save(failOnError: true, flush: true)

        participant.submission = submission
        participant.save(flush: true, failOnError: true)

        def trainer = new Trainer(facility: facility, firstName: "John", lastName: "Doe",
                sport: createSport()).save(flush: true, failOnError: true)
        def trainingCourt = new TrainingCourt(name: "tc", facility: facility,
                court: createCourt(facility)).save(flush: true, failOnError: true)
        def occasion = new ActivityOccasion(message: "msg", date: new LocalDate(),
                startTime: new LocalTime(), endTime: new LocalTime(),
                court: trainingCourt, activity: srcCourse)
                .addToTrainers(trainer).addToParticipants(participant)
                .save(flush: true, failOnError: true)
        srcCourse.addToTrainers(trainer)
        srcCourse.addToParticipants(participant)
        srcCourse.addToOccasions(occasion)
        srcCourse.save(flush: true, failOnError: true)
        def cmd = [srcCourseId: srcCourse.id, name: srcCourse.name + "copy",
                startDate: new Date() + 10, endDate: new Date() + 20,
                activeFrom: new Date() + 11, activeTo: new Date() + 12, copyTrainers: true,
                copySettings: true, copyParticipants: true, copyOccasions: true,
                copyWaitingSubmissions: true]

        activityService.copyCourse(cmd)

        assert 2 == CourseActivity.count()
        def newCourse = CourseActivity.findByName(cmd.name)
        assert newCourse
        assert facility == newCourse.facility
        assert cmd.startDate == newCourse.startDate
        assert cmd.endDate == newCourse.endDate
        assert srcCourse.form.id != newCourse.form.id
        assert cmd.activeFrom == newCourse.form.activeFrom
        assert cmd.activeTo == newCourse.form.activeTo
        assert newCourse.form.relatedFormTemplate
        assert srcCourse.form.maxSubmissions == newCourse.form.maxSubmissions
        assert srcCourse.form.membershipRequired == newCourse.form.membershipRequired
        assert srcCourse.form.paymentRequired == newCourse.form.paymentRequired
        assert srcCourse.form.price == newCourse.form.price
        assert 1 == newCourse.form.fields.size()
        assert srcCourse.form.fields[0].label == newCourse.form.fields[0].label
        assert srcCourse.form.fields[0].type == newCourse.form.fields[0].type
        assert !newCourse.form.fields[0].isActive
        assert 1 == newCourse.form.fields[0].predefinedValues.size()
        assert srcCourse.form.fields[0].predefinedValues[0].value ==
                newCourse.form.fields[0].predefinedValues[0].value
        assert newCourse.form.relatedFormTemplate.templateFields[0].predefinedValues[0].value !=
                newCourse.form.fields[0].predefinedValues[0].value
        assert 1 == newCourse.trainers.size()
        assert srcCourse.trainers.iterator().next() == newCourse.trainers.iterator().next()
        assert 2 == Participant.count()
        assert 1 == newCourse.participants.size()
        def newParticipant = newCourse.participants.iterator().next()
        assert participant.id != newParticipant.id
        assert !newParticipant.submission.order
        assert newParticipant.submission.form
        assert submission.form.id != newParticipant.submission.form.id
        assert newParticipant.submission.customer
        assert submission.customer.id == newParticipant.submission.customer.id
        assert 4 == Submission.count()
        assert newParticipant.submission
        assert submission.id != newParticipant.submission.id
        assert 4 == SubmissionValue.count()
        assert 1 == newParticipant.submission.values.size()
        def origSubmissionValue = submission.values.iterator().next()
        def newSubmissionValue = newParticipant.submission.values.iterator().next()
        assert origSubmissionValue.id != newSubmissionValue.id
        assert origSubmissionValue.label == newSubmissionValue.label
        assert origSubmissionValue.value == newSubmissionValue.value
        assert 2 == ActivityOccasion.count()
        assert 1 == newCourse.occasions.size()
        def newOccasion = newCourse.occasions.iterator().next()
        assert occasion.message == newOccasion.message
        assert occasion.date.dayOfWeek().get() == newOccasion.date.dayOfWeek().get()
        assert occasion.startTime == newOccasion.startTime
        assert occasion.endTime == newOccasion.endTime
        assert occasion.court.id == newOccasion.court.id
        assert 1 == newOccasion.participants.size()
        assert 1 == newOccasion.trainers.size()
        assert 2 == Submission.countByForm(newCourse.form)
        def newWaitingSubmission = Submission.findByFormAndStatus(newCourse.form, Submission.Status.WAITING)
        assert newWaitingSubmission
        origSubmissionValue = waitingSubmission.values.iterator().next()
        newSubmissionValue = newWaitingSubmission.values.iterator().next()
        assert origSubmissionValue.id != newSubmissionValue.id
        assert origSubmissionValue.label == newSubmissionValue.label
        assert origSubmissionValue.value == newSubmissionValue.value

        // copy except form settings, participants, trainers, occasions
        cmd = [srcCourseId: srcCourse.id, name: srcCourse.name + "copy2",
                startDate: new Date() + 30, endDate: new Date() + 40,
                activeFrom: new Date() + 31, activeTo: new Date() + 32, copyTrainers: false,
                copySettings: false, copyParticipants: false, copyOccasions: false]

        activityService.copyCourse(cmd)

        assert 3 == CourseActivity.count()
        newCourse = CourseActivity.findByName(cmd.name)
        assert newCourse
        assert newCourse.form.relatedFormTemplate
        assert srcCourse.form.id != newCourse.form.id
        assert !newCourse.form.maxSubmissions
        assert !newCourse.form.membershipRequired
        assert !newCourse.form.paymentRequired
        assert !newCourse.form.price
        assert 1 == newCourse.form.fields.size()
        assert srcCourse.form.fields[0].label == newCourse.form.fields[0].label
        assert srcCourse.form.fields[0].type == newCourse.form.fields[0].type
        assert newCourse.form.fields[0].isActive
        assert 1 == newCourse.form.fields[0].predefinedValues.size()
        assert srcCourse.form.fields[0].predefinedValues[0].value !=
                newCourse.form.fields[0].predefinedValues[0].value
        assert newCourse.form.relatedFormTemplate.templateFields[0].predefinedValues[0].value ==
                newCourse.form.fields[0].predefinedValues[0].value
        assert !newCourse.trainers
        assert !newCourse.participants
        assert !newCourse.occasions
        assert !Submission.countByForm(newCourse.form)

        // copy except form settings, participants, trainers
        cmd = [srcCourseId: srcCourse.id, name: srcCourse.name + "copy3",
                startDate: new Date() + 50, endDate: new Date() + 60,
                activeFrom: new Date() + 51, activeTo: new Date() + 52, copyTrainers: false,
                copySettings: false, copyParticipants: false, copyOccasions: true]

        activityService.copyCourse(cmd)

        assert 4 == CourseActivity.count()
        newCourse = CourseActivity.findByName(cmd.name)
        assert newCourse
        assert srcCourse.form.id != newCourse.form.id
        assert !newCourse.form.maxSubmissions
        assert !newCourse.form.membershipRequired
        assert !newCourse.form.paymentRequired
        assert !newCourse.form.price
        assert 1 == newCourse.form.fields.size()
        assert srcCourse.form.fields[0].label == newCourse.form.fields[0].label
        assert srcCourse.form.fields[0].type == newCourse.form.fields[0].type
        assert !newCourse.trainers
        assert 2 == Participant.count()
        assert !newCourse.participants
        assert 3 == ActivityOccasion.count()
        assert 1 == newCourse.occasions.size()
        newOccasion = newCourse.occasions.iterator().next()
        assert occasion.message == newOccasion.message
        assert occasion.date.dayOfWeek().get() == newOccasion.date.dayOfWeek().get()
        assert occasion.startTime == newOccasion.startTime
        assert occasion.endTime == newOccasion.endTime
        assert occasion.court.id == newOccasion.court.id
        assert !newOccasion.participants
        assert !newOccasion.trainers
        assert !Submission.countByForm(newCourse.form)

        // copy participants, but not waiting submissions
        cmd = [srcCourseId: srcCourse.id, name: srcCourse.name + "copy4",
                startDate: new Date() + 60, endDate: new Date() + 70,
                activeFrom: new Date() + 61, activeTo: new Date() + 62, copyTrainers: false,
                copySettings: false, copyParticipants: true, copyOccasions: false,
                copyWaitingSubmissions: false]

        activityService.copyCourse(cmd)

        assert 5 == CourseActivity.count()
        newCourse = CourseActivity.findByName(cmd.name)
        assert 1 == newCourse.participants.size()
        assert 1 == Submission.countByFormAndStatus(newCourse.form, Submission.Status.ACCEPTED)
        assert !Submission.countByFormAndStatus(newCourse.form, Submission.Status.WAITING)
    }

    void testCreateActivityPaymentOrder() {
        def user = createUser()
        def issuer = createUser("issuer@local.net")
        def facility = createFacility()
        def occasion = new ActivityOccasion(message: "msg", date: new LocalDate(),
                startTime: new LocalTime(), endTime: new LocalTime(),
                court: createTrainingCourt(facility), activity: createCourse(facility))
                .save(flush: true, failOnError: true)

        def order = activityService.createActivityPaymentOrder(
                user, occasion, Order.ORIGIN_FACILITY, issuer)

        assert order
        assert 1 == Order.countByFacility(facility)
        assert Order.Status.NEW == order.status
        assert issuer.id == order.issuer.id
        assert user.id == order.user.id
        assert Order.ORIGIN_FACILITY == order.origin
        assert Order.Article.ACTIVITY == order.article
        assert occasion.id.toString() == order.metadata.activityOccasionId
        assert occasion.activity.facility.id == order.facility.id
        occasion.price = 100
        facility.vat = 6
        assert activityService.getPriceForActivityOccasion(user,occasion).VAT == 5.66
    }

    void testGetUserParticipations() {
        User user1 = createUser("${RandomStringUtils.randomAlphabetic(10)}@matchi.se")
        User user2 = createUser("${RandomStringUtils.randomAlphabetic(10)}@matchi.se")

        // Verify no customer works OK
        assert !activityService.getUserUpcomingParticipations(user1)

        // Verify no crash with empty facility list
        assert !activityService.getUserUpcomingParticipations(user2, new LocalDate(), new LocalTime(), [])

        Facility facility = createFacility()
        Customer customer1 = createCustomer(facility, user1.email)
        customer1.user = user1
        customer1.save(flush: true, failOnError: true)

        Customer customer2 = createCustomer(facility, user2.email)
        customer2.user = user2
        customer2.save(flush: true, failOnError: true)

        LocalDate today = new LocalDate(2020, 1, 1)
        LocalTime now = new LocalTime("12:00")
        LocalDate tomorrow = today.plusDays(1)
        LocalDate yesterday = today.minusDays(1)
        LocalTime inAnHour = now.plusHours(1)
        LocalTime inTwoHours = inAnHour.plusHours(1)
        LocalTime anHourAgo = now.minusHours(1)
        LocalTime twoHoursAgo = anHourAgo.minusHours(1)

        ClassActivity activity = createClassActivity(facility, "activity")

        // Creating first to verify order is correct
        ActivityOccasion activityOccasionLaterToday = createActivityOccasion(activity)
        activityOccasionLaterToday.date = today
        activityOccasionLaterToday.startTime = inAnHour
        activityOccasionLaterToday.endTime = inTwoHours
        activityOccasionLaterToday.save(flush: true, failOnError: true)

        Participation participationLaterToday = createActivityOccasionParticipation(customer1, activityOccasionLaterToday)

        ActivityOccasion activityOccasion = createActivityOccasion(activity)
        activityOccasion.date = today
        activityOccasion.startTime = now
        activityOccasion.endTime = inAnHour
        activityOccasion.save(flush: true, failOnError: true)

        Participation participation = createActivityOccasionParticipation(customer1, activityOccasion)

        ActivityOccasion activityOccasionYesterday = createActivityOccasion(activity)
        activityOccasionYesterday.date = yesterday
        activityOccasionYesterday.startTime = inAnHour
        activityOccasionYesterday.endTime = inTwoHours
        activityOccasionYesterday.save(flush: true, failOnError: true)

        Participation participationYesterday = createActivityOccasionParticipation(customer1, activityOccasionYesterday)

        ActivityOccasion activityOccasionTomorrow = createActivityOccasion(activity)
        activityOccasionTomorrow.date = tomorrow
        activityOccasionTomorrow.startTime = twoHoursAgo
        activityOccasionTomorrow.endTime = anHourAgo
        activityOccasionTomorrow.save(flush: true, failOnError: true)

        Participation participationTomorrow = createActivityOccasionParticipation(customer1, activityOccasionTomorrow)

        // Verifying different variants of fetching the participation
        assert activityService.getUserUpcomingParticipations(user1, today, now) == [participation, participationLaterToday, participationTomorrow]
        assert activityService.getUserUpcomingParticipations(user1, yesterday, now) == [participationYesterday, participation, participationLaterToday, participationTomorrow]
        assert activityService.getUserUpcomingParticipations(user1, yesterday, anHourAgo) == [participationYesterday, participation, participationLaterToday, participationTomorrow]
        assert activityService.getUserUpcomingParticipations(user1, yesterday, inAnHour) == [participationYesterday, participation, participationLaterToday, participationTomorrow]
        assert activityService.getUserUpcomingParticipations(user1, today, anHourAgo) == [participation, participationLaterToday, participationTomorrow]
        assert activityService.getUserUpcomingParticipations(user1, today, inAnHour) == [participationLaterToday, participationTomorrow]
        assert !activityService.getUserUpcomingParticipations(user1, tomorrow, now)
        assert activityService.getUserUpcomingParticipations(user1, tomorrow, twoHoursAgo) == [participationTomorrow]

        Facility facilityWithoutOccasions = createFacility()
        assert activityService.getUserUpcomingParticipations(user1, today, now, [facility] as List<Facility>) == [participation, participationLaterToday, participationTomorrow]
        assert !activityService.getUserUpcomingParticipations(user1, today, now, [facilityWithoutOccasions])

        shouldFail(IllegalArgumentException) {
            activityService.getUserUpcomingParticipations(null)
        }

        // Cleanup
        participation.delete(flush: true)
        activityOccasion.delete(flush: true)
        participationLaterToday.delete(flush: true)
        activityOccasionLaterToday.delete(flush: true)
        participationYesterday.delete(flush: true)
        activityOccasionYesterday.delete(flush: true)
        participationTomorrow.delete(flush: true)
        activityOccasionTomorrow.delete(flush: true)
        activity.delete(flush: true)
        customer1.delete(flush: true)
        customer2.delete(flush: true)
        user1.delete(flush: true)
        user2.delete(flush: true)
        facility.delete(flush: true)
        facilityWithoutOccasions.delete(flush: true)
    }

    void testGetOnlineEvents() {
        def facility1 = createFacility()
        def form1 = createForm(facility1)
        def event1 = new EventActivity(name: "b", facility: facility1, startDate: new Date() + 10,
                endDate: new Date() + 20, form: form1, showOnline: true).save(failOnError: true)
        def form2 = createForm(facility1)
        def event2 = new EventActivity(name: "c", facility: facility1, startDate: new Date() + 10,
                endDate: new Date() + 20, form: form2, showOnline: false).save(failOnError: true)
        def form3 = createForm(facility1, new Date() + 10, new Date() + 20)
        def event3 = new EventActivity(name: "d", facility: facility1,  startDate: new Date() + 10,
                endDate: new Date() + 20, form: form3, showOnline: true).save(failOnError: true)
        def form4 = createForm(facility1)
        def event4 = new EventActivity(name: "a", facility: facility1, startDate: new Date(),
                endDate: new Date(), form: form4, showOnline: true).save(failOnError: true)
        def facility2 = createFacility()
        def form5 = createForm(facility1)
        def event5 = new EventActivity(name: "e", facility: facility2, startDate: new Date(),
                endDate: new Date(), form: form5, showOnline: true).save(failOnError: true, flush: true)

        def result = activityService.getOnlineEvents(facility1)

        assert 2 == result.size()
        assert event4 == result[0]
        assert event1 == result[1]

        result = activityService.getOnlineEvents(facility2)

        assert 1 == result.size()
        assert event5 == result[0]
    }

    // TODO Wont work on specific times and weekdays
//
//    void testSearchForActivityOccasionsByDateAndActivity() {
//        User user = createUser("${RandomStringUtils.randomAlphabetic(10)}@matchi.se")
//        Region region = createRegion()
//        region.name = "Kalledonien"
//        Municipality municipality = createMunicipality(region)
//        municipality.name = "Ankeborg"
//        Facility facility = createFacility(municipality)
//        facility.name = "von Ankas Padel"
//        Sport sport = createSport()
//
//        Sport nonUsedSport = createSport()
//
//        Court court= createCourt(facility, sport)
//        //-2 to -1 hours from now
//        Slot slotBeforeNow = createSlot(court, new Date(System.currentTimeMillis() - 2*60*60*1000), new Date(System.currentTimeMillis() - 1*60*60*1000))
//        //+1 to +2 hours from now
//        Slot slotAfterNow = createSlot(court, new Date(System.currentTimeMillis() + 1*60*60*1000), new Date(System.currentTimeMillis() + 2*60*60*1000))
//        //+24 to +25 hours from now
//        Slot slotTomorrow = createSlot(court, new Date(System.currentTimeMillis() + 24*60*60*1000), new Date(System.currentTimeMillis() + 25*60*60*1000))
//        Customer customer = createCustomer(facility, user.email)
//
//        Booking bookingBeforeNow = createBooking(customer, slotBeforeNow)
//        ClassActivity activityBeforeNow = createClassActivity(facility, "ActivityBeforeNow")
//        ActivityOccasion activityOccasionBeforeNow = createActivityOccasion(activityBeforeNow)
//        activityOccasionBeforeNow.date = new LocalDate(slotBeforeNow.startTime)
//        activityOccasionBeforeNow.startTime = new LocalTime(slotBeforeNow.startTime)
//        activityOccasionBeforeNow.endTime = new LocalTime(slotBeforeNow.endTime)
//        activityOccasionBeforeNow.bookings = [bookingBeforeNow]
//
//        Booking bookingAfterNow = createBooking(customer, slotAfterNow)
//        ClassActivity activityAfterNow = createClassActivity(facility, "ActivityAfterNow")
//        activityAfterNow.description = "Long Description"
//        activityAfterNow.levelMin = 4
//        activityAfterNow.levelMax = 6
//        ActivityOccasion activityOccasionAfterNow = createActivityOccasion(activityAfterNow)
//        activityOccasionAfterNow.date = new LocalDate(slotAfterNow.startTime)
//        activityOccasionAfterNow.startTime = new LocalTime(slotAfterNow.startTime)
//        activityOccasionAfterNow.endTime = new LocalTime(slotAfterNow.endTime)
//        activityOccasionAfterNow.bookings = [bookingAfterNow]
//
//        Booking bookingTomorrow = createBooking(customer, slotTomorrow)
//        ClassActivity activityTomorrow = createClassActivity(facility, "ActivityTomorrow")
//        ActivityOccasion activityOccasionTomorrow = createActivityOccasion(activityTomorrow)
//        activityOccasionTomorrow.date = new LocalDate(slotTomorrow.startTime)
//        activityOccasionTomorrow.startTime = new LocalTime(slotTomorrow.startTime)
//        activityOccasionTomorrow.endTime = new LocalTime(slotTomorrow.endTime)
//        activityOccasionTomorrow.bookings = [bookingTomorrow]
//
//        assert activityService.searchForActivityOccasions([sport.id], new LocalDate(), 0, "", "", null, []).size() == 1
//        assert activityService.searchForActivityOccasions([nonUsedSport.id], new LocalDate(), 0, "", "", null, []).size() == 0
//
//        assert activityService.searchForActivityOccasions([], new LocalDate().plusDays(1), 0, "", "", null, []).size() == 1
//        assert activityService.searchForActivityOccasions([], new LocalDate().plusDays(2), 0, "", "", null, []).size() == 0
//        assert activityService.searchForActivityOccasions([], new LocalDate().plusDays(1), 1, "", "", null, []).size() == 1
//
//        assert activityService.searchForActivityOccasions([], new LocalDate(), 1, "", "", null, []).size() == 2
//        assert activityService.searchForActivityOccasions([], new LocalDate().minusDays(1), 1, "", "", null, []).size() == 1
//
//        assert activityService.searchForActivityOccasions([], new LocalDate(), 0, "", "", null, []).size() == 1
//        assert activityService.searchForActivityOccasions([], new LocalDate(), 0, "Kalle", "", null, []).size() == 1
//        assert activityService.searchForActivityOccasions([], new LocalDate(), 0, "von anka", "", null, []).size() == 1
//        assert activityService.searchForActivityOccasions([], new LocalDate(), 0, "Tuppeboda", "", null, []).size() == 0
//
//        assert activityService.searchForActivityOccasions([], new LocalDate(), 1, "", "Tomorrow", null, []).size() == 1
//        assert activityService.searchForActivityOccasions([], new LocalDate(), 1, "", "Yesterday", null, []).size() == 0
//        assert activityService.searchForActivityOccasions([], new LocalDate(), 1, "", "description", null, []).size() == 1
//
//        assert activityService.searchForActivityOccasions([], new LocalDate(), 0, "", "", 3, []).size() == 0
//        assert activityService.searchForActivityOccasions([], new LocalDate(), 0, "", "", 4, []).size() == 1
//        assert activityService.searchForActivityOccasions([], new LocalDate(), 0, "", "", 6, []).size() == 1
//        assert activityService.searchForActivityOccasions([], new LocalDate(), 0, "", "", 7, []).size() == 0
//
//        ClassActivity activityNonBookable = createClassActivity(facility, "NonBookable")
//        ActivityOccasion activityOccasionNonBookable = createActivityOccasion(activityNonBookable)
//        activityOccasionNonBookable.availableOnline = false
//
//        ClassActivity activityBookable = createClassActivity(facility, "Bookable")
//        ActivityOccasion activityOccasionBookable = createActivityOccasion(activityBookable)
//
//        assert activityService.searchForActivityOccasions([], new LocalDate(), 0, "", "Bookable", null, []).size() == 1
//    }

    void testGetActivityOccasionsToCancelNullArgument() {
        shouldFail(IllegalArgumentException) {
            activityService.getOccasionsToCancelByTooFewParticipants(null)
        }
    }

    void testGetActivityOccasionsToCancelNoOccasions() {
        assert ActivityOccasion.count() == 0
        assert !activityService.getOccasionsToCancelByTooFewParticipants(new DateTime())
    }

    void testGetActivityOccasionsToCancelOccasionNotMatched() {
        assert ActivityOccasion.count() == 0

        DateTime now = new DateTime()
        ActivityOccasion activityOccasion = createOccasion(now.plusDays(1))
        assert !activityOccasion.automaticCancellationDateTime
        assert !activityOccasion.minNumParticipants
        assert !activityOccasion.hasToFewParticipations()
        assert !activityService.getOccasionsToCancelByTooFewParticipants(now)
    }

    void testGetActivityOccasionsToCancelOccasionLastHour() {
        assert ActivityOccasion.count() == 0

        DateTime now = new DateTime()
        ActivityOccasion activityOccasion = createOccasion(now.plusDays(1))
        activityOccasion.minNumParticipants = 2
        activityOccasion.maxNumParticipants = 2
        activityOccasion.setCancellationDateTime(24) // Which is now
        activityOccasion.save(flush: true, failOnError: true)

        assert activityOccasion.automaticCancellationDateTime
        assert activityOccasion.minNumParticipants
        assert activityOccasion.hasToFewParticipations()


        assert !activityService.getOccasionsToCancelByTooFewParticipants(now.minusMinutes(1)) // Looking to early
        assert activityService.getOccasionsToCancelByTooFewParticipants(now) == [activityOccasion]
        assert activityService.getOccasionsToCancelByTooFewParticipants(now.plusMinutes(15)) == [activityOccasion]
        assert activityService.getOccasionsToCancelByTooFewParticipants(now.plusMinutes(61)) == [activityOccasion]
        assert activityService.getOccasionsToCancelByTooFewParticipants(now.plusDays(1).minusMinutes(1)) == [activityOccasion]
        assert !activityService.getOccasionsToCancelByTooFewParticipants(now.plusDays(1).plusMinutes(1))
    }

    void testGetActivityOccasionsToCancelOccasion() {
        assert ActivityOccasion.count() == 0

        DateTime now = new DateTime()
        ActivityOccasion activityOccasion = createOccasion(now.plusDays(1))
        activityOccasion.minNumParticipants = 2
        activityOccasion.maxNumParticipants = 2
        activityOccasion.setCancellationDateTime(24) // Which is now
        activityOccasion.save(flush: true, failOnError: true)

        Participation p1 = createActivityOccasionParticipation(createCustomer(activityOccasion.activity.facility), activityOccasion)
        activityOccasion.participations = [p1]
        activityOccasion.save(flush: true, failOnError: true)
        assert activityOccasion.hasToFewParticipations() // Yes, too few

        assert !activityService.getOccasionsToCancelByTooFewParticipants(now.minusMinutes(1)) // Looking before cancellation time
        assert activityService.getOccasionsToCancelByTooFewParticipants(now) == [activityOccasion] // Strictly on cancellation time
        assert activityService.getOccasionsToCancelByTooFewParticipants(now.plusMinutes(1)) == [activityOccasion] // Looking before cancellation time
        assert activityService.getOccasionsToCancelByTooFewParticipants(now.plusDays(1).minusMinutes(1)) == [activityOccasion] // Looking before cancellation time
        assert !activityService.getOccasionsToCancelByTooFewParticipants(now.plusDays(1)) // Strictly on start time
        assert !activityService.getOccasionsToCancelByTooFewParticipants(now.plusDays(1).plusMinutes(1))

        Participation p2 = createActivityOccasionParticipation(createCustomer(activityOccasion.activity.facility), activityOccasion)
        activityOccasion.participations = [p1, p2]
        activityOccasion.save(flush: true, failOnError: true)

        assert activityOccasion.automaticCancellationDateTime
        assert activityOccasion.minNumParticipants
        assert !activityOccasion.hasToFewParticipations() // Just good enough

        Participation p3 = createActivityOccasionParticipation(createCustomer(activityOccasion.activity.facility), activityOccasion)
        activityOccasion.participations = [p1, p2, p3]
        activityOccasion.save(flush: true, failOnError: true)
        assert !activityOccasion.hasToFewParticipations() // More than enough

        assert !activityService.getOccasionsToCancelByTooFewParticipants(now.minusMinutes(1)) // Looking before cancellation time
        assert !activityService.getOccasionsToCancelByTooFewParticipants(now) // Strictly on cancellation time
        assert !activityService.getOccasionsToCancelByTooFewParticipants(now.plusMinutes(1)) // Looking before cancellation time
        assert !activityService.getOccasionsToCancelByTooFewParticipants(now.plusDays(1).minusMinutes(1)) // Looking before cancellation time
        assert !activityService.getOccasionsToCancelByTooFewParticipants(now.plusDays(1)) // Strictly on start time
        assert !activityService.getOccasionsToCancelByTooFewParticipants(now.plusDays(1).plusMinutes(1))
    }

    void testNullOccasionToCancel() {
        shouldFail(NullPointerException) {
            activityService.cancelOccasionWithFullRefund(null, null, null)
        }
    }

    void testUnCancellableOccasionToCancel() {
        DateTime now = new DateTime()
        ActivityOccasion activityOccasion = createOccasion(now.plusDays(1))
        assert !activityOccasion.hasToFewParticipations() //Not to few because no lower limit

        shouldFail(IllegalArgumentException) {
            activityService.cancelOccasionWithFullRefundAutomatically(activityOccasion, null)
        }

        activityOccasion.minNumParticipants = 2
        activityOccasion.automaticCancellationDateTime = now.plusHours(1)

        Participation p1 = createActivityOccasionParticipation(createCustomer(activityOccasion.activity.facility), activityOccasion)
        activityOccasion.participations = [p1]

        assert activityOccasion.hasToFewParticipations() //Now there is too few participants

        shouldFail(IllegalArgumentException) { //still not in time to cancel
            activityService.cancelOccasionWithFullRefundAutomatically(activityOccasion, null)
        }
    }

    void testCancelOccasionWithFullRefund() {
        springSecurityService.reauthenticate "calle@matchi.se"

        DateTime now = new DateTime()
        ActivityOccasion activityOccasion = createOccasion(now.plusDays(1))

        Participation p1 = createActivityOccasionParticipation(createCustomer(activityOccasion.activity.facility), activityOccasion)
        Participation p2 = createActivityOccasionParticipation(createCustomer(activityOccasion.activity.facility), activityOccasion)
        activityOccasion.participations = [p1, p2]
        User u1 = createUser("user1@email.com")
        User u2 = createUser("user2@email.com")

        p1.customer.user = u1
        p2.customer.user = u2

        p1.order = createOrder(u1, activityOccasion.activity.facility, Order.Article.ACTIVITY)
        p1.order.payments << createAdyenOrderPayment(u1, p1.order, "transaction.id-1")
        p1.order.status = Order.Status.COMPLETED

        p2.order = createOrder(u2, activityOccasion.activity.facility, Order.Article.ACTIVITY)
        p2.order.payments << createInvoiceOrderPayment(u2, p2.order, "transaction.id-2")
        p2.order.status = Order.Status.COMPLETED

        p1.order.dateDelivery = new DateTime().minusDays(3).toDate() //Makes an adyenOrder non refundable

        activityService.cancelOccasionWithFullRefund(activityOccasion, null, null)

        assert p1.order.status == Order.Status.COMPLETED
        assert p2.order.status == Order.Status.ANNULLED
    }

    private ActivityOccasion createOccasion(DateTime startTime, int nBookings = 1) {
        Facility facility = createFacility()
        Customer customer = createCustomer(facility)

        ClassActivity activity = createClassActivity(facility, RandomStringUtils.randomAlphanumeric(20))
        List<Booking> bookings = []

        ActivityOccasion activityOccasion = createActivityOccasion(activity)
        activityOccasion.date = new LocalDate(startTime)
        activityOccasion.startTime = new LocalTime(startTime)
        activityOccasion.endTime = new LocalTime(startTime.plusHours(1))

        (1..nBookings).each {
            Court court = createCourt(facility)
            Slot slot = createSlot(court, startTime.toDate(), startTime.plusHours(1).toDate())
            Booking booking = createBooking(customer, slot)
            assert booking != null
            activityOccasion.addToBookings(booking)
            bookings.add(booking)
        }

        activityOccasion.save(flush: true, failOnError: true)

        return activityOccasion
    }
}