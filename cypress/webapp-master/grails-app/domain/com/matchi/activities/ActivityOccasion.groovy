package com.matchi.activities

import com.matchi.Booking
import com.matchi.Customer
import com.matchi.DateUtil
import com.matchi.GetActivitiesCommand
import com.matchi.User
import com.matchi.activities.trainingplanner.Trainer
import com.matchi.activities.trainingplanner.TrainingCourt
import com.matchi.watch.ClassActivityWatch
import org.joda.time.DateTime
import org.joda.time.LocalDate
import org.joda.time.LocalTime
import org.joda.time.Period

class ActivityOccasion {

    static belongsTo = [activity: Activity]
    static hasMany = [participations: Participation, participants: Participant, bookings: Booking, trainers: Trainer]

    String message
    LocalDate date
    LocalTime startTime
    LocalTime endTime

    //only for ClassActivity
    int price
    int maxNumParticipants
    boolean availableOnline = true
    Integer signUpDaysInAdvanceRestriction
    Integer signUpDaysUntilRestriction
    Boolean membersOnly

    //Only for CourseActivity
    Set<Trainer> trainers
    TrainingCourt court

    Integer minNumParticipants
    DateTime automaticCancellationDateTime

    Boolean fieldDeleted = false
    GetActivitiesCommand JSONoptions = null

    DELETE_REASON deleteReason

    static transients = ['JSONoptions']

    static enum DELETE_REASON {
        MANUAL_WITH_REFUND, MANUAL_NO_REFUND, BY_JOB, UNKNOWN
    }

    def isFull() {
        return numParticipations() >= maxNumParticipants
    }

    def numParticipations() {
        return participations.size()
    }

    def availableParticipations() {
        return maxNumParticipants - numParticipations()
    }

    def isPast() {
        return date.toDateTime(endTime).isBefore(new DateTime())
    }

    def isFinished() {
        return date.toDateTime(startTime).isBeforeNow()
    }

    def isUpcoming() {
        return date.toDateTime(startTime).isAfterNow()
    }

    def isUpcomingOnlineOccasion() {
        return availableOnline && !isPast() && hasOpenedRegistration() && hasNotClosedRegistration()
    }

    def lengthInMinutes() {
        return toPeriod().toStandardMinutes().minutes
    }

    def isParticipating(User user) {
        return getParticipation(user) != null
    }

    DateTime getRefundableUntil() {
        this.getStartDateTime().minusHours(this.activity.cancelLimitWithFallback)
    }

    boolean hasNotClosedRegistration() {
        signUpDaysUntilRestriction == null ||
                new LocalDate().plusDays(signUpDaysUntilRestriction) <= date
    }

    boolean hasOpenedRegistration() {
        signUpDaysInAdvanceRestriction == null ||
                new LocalDate().plusDays(signUpDaysInAdvanceRestriction) >= date
    }

    def getParticipation(Customer customer) {
        return participations.find { it.customer.id == customer.id }
    }

    def getParticipation(User user) {
        def customer = Customer.findByUserAndFacility(user, activity.facility)

        if (customer) {
            return getParticipation(customer)
        }

        return null
    }

    def toPeriod() {
        def start = date.toDateTime(startTime)
        def end = date.toDateTime(endTime)
        return new Period(start, end)
    }

    def getStartDateTime() {
        return date.toDateTime(startTime)
    }

    def getEndDateTime() {
        return date.toDateTime(endTime)
    }

    int day() {
        return date.getDayOfWeek()
    }

    def createOrderDescription(User user) {
        return "${activity.name} ${date.toString()} ${startTime.toString("HH:mm")} ${lengthInMinutes()}min";
    }

    String getShortDescription() {
        // consistent with Slot#getShortDescription
        "${date.toString('dd/MM')} ${startTime.toString('HH:mm')}, $activity.name"
    }

    def createOrderNumber(User user) {
        return "${this.id}-${user.id}"
    }

    static constraints = {
        message(nullable: true, blank: true, maxSize: 255)
        date(nullable: false)
        startTime(nullable: false)
        endTime(nullable: false)
        price(min: 0, nullable: true)
        maxNumParticipants(min: 0, nullable: true)
        availableOnline(nullable: true)
        signUpDaysInAdvanceRestriction(nullable: true)
        signUpDaysUntilRestriction(nullable: true)
        membersOnly(nullable: true)
        court(nullable: true)
        deleteReason(nullable: true)
        minNumParticipants(nullable: true, validator: { val, obj ->
            return (val <= obj.maxNumParticipants)
        })
        automaticCancellationDateTime(nullable: true)
    }

    static mapping = {
        bookings joinTable: [name  : 'activity_occasion_booking',
                             column: 'booking_id',
                             key   : 'activity_occasion_id']
        participants batchSize: 20
        trainers batchSize: 10
        fieldDeleted column: "deletecol"

    }

    static namedQueries = {
        byParticipant { participant ->
            participants {
                eq("id", participant.id)
            }
        }
    }

    static hibernateFilters = {
        nonDeletedFilter(condition: 'deletecol=0', default: true)
    }

    @Override
    public String toString() {
        return "ActivityOccasion{" +
                "date=" + date +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", automaticCancellationDateTime=" + automaticCancellationDateTime +
                '}';
    }

    void setCancellationDateTime(int cancelHoursInAdvance) {
        if (cancelHoursInAdvance < 0) {
            throw new IllegalArgumentException("cancelHoursInAdvance cannot be negative")
        }

        automaticCancellationDateTime = date.toDateTime(startTime).minusHours(cancelHoursInAdvance)
    }

    Integer getCancelHoursInAdvance() {
        if (!automaticCancellationDateTime) {
            return null
        }

        return (date.toDateTime(startTime).millis - automaticCancellationDateTime.millis) / DateUtil.MILLISECONDS_PER_HOUR
    }

    boolean hasToFewParticipations() {
        return minNumParticipants && (!participations || participations.size() < minNumParticipants)
    }

    boolean isInAutomaticCancellationTime() {
        DateTime now = new DateTime()
        return (now >= automaticCancellationDateTime && now < startDateTime)
    }

    boolean mayBeCancelledAutomatically() {
        return minNumParticipants && automaticCancellationDateTime
    }

    boolean canNowBeCancelledAutomatically() {
        return hasToFewParticipations() && isInAutomaticCancellationTime()
    }

    void setDeleted(DELETE_REASON reason = null) {
        if (reason == null) {
            reason = DELETE_REASON.UNKNOWN
        }
        this.deleteReason = reason
        this.fieldDeleted = true
    }

    boolean isDeleted() {
        return this.fieldDeleted
    }

    void undelete() {
        this.fieldDeleted = false
    }

    List<ClassActivityWatch> getWatchQueue() {
        ClassActivityWatch.findAllByFacilityAndClassActivityAndFromDate(
                activity.facility, activity, date.toDateTime(startTime).toDate())
    }

    Integer getWatchQueueSize() {
        ClassActivityWatch.countByFacilityAndClassActivityAndFromDate(
                activity.facility, activity, date.toDateTime(startTime).toDate())
    }
}
