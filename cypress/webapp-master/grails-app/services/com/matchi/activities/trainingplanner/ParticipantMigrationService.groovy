package com.matchi.activities.trainingplanner

import com.matchi.activities.ActivityOccasion
import com.matchi.activities.Participant
import com.matchi.dynamicforms.Submission
import grails.transaction.Transactional

class ParticipantMigrationService {

    static transactional = false

    def groovySql
    def editParticipantService

    @Transactional
    void runMigration() {
        List<Participant> participants = Participant.createCriteria().list {
            createAlias('activity', 'a')
            eq('status', Participant.Status.QUEUED)
            eq('a.class', 'course_activity')
        }

        log.info "Updating and removing ${participants.size()}..."

        int nRemovable = 0
        int nReservable = 0
        int nActivateable = 0

        participants.each { Participant participant ->

            if(isRemovable(participant)) {
                nRemovable++
                participant.delete()
            } else if(isReservable(participant)) {
                nReservable++
                participant.status = Participant.Status.RESERVED
                participant.save()
            } else if(isActivateable(participant)) {
                nActivateable++
                participant.status = Participant.Status.ACTIVE
                participant.save()
            } else {
                log.error("Participant ${participant.id} cannot be handled!")
            }
        }

        log.info "Removed ${nRemovable} participants..."
        log.info "Reserved ${nReservable} participants..."
        log.info "Activated ${nActivateable} participants..."
    }

    /**
     * Migrates course submissions. To be run after the above.
     * Non-transactional because takes too long time and it is pretty straight forward (small risk of failure)
     */
    void migrateSubmissions() {
        migrateAcceptedSubmissions()
        migrateWaitingSubmissions()

        log.info "Finished upgrading submissions"
    }

    /**
     * Migrates submissions having a participant into ACCEPTED.
     */
    private void migrateAcceptedSubmissions() {
        List result = groovySql.rows("""select s.id as id from submission s
                left join customer c on s.customer_id = c.id
                left join form f on s.form_id = f.id
                left join activity a on a.form_id = f.id
                left join participant p on p.submission_id = s.id
                where a.class = 'course_activity'
                and s.status is null
                and p.id is not null;""")

        log.info "accepted submissions to migrate = ${result.size()}"

        result.each {
            Submission submission = Submission.findById(it.id as Long)
            submission.status = Submission.Status.ACCEPTED
            submission.save()
        }
        groovySql.close()
    }

    /**
     * Migrates submissions not having a participant into WAITING.
     */
    private void migrateWaitingSubmissions() {
        List result = groovySql.rows("""select s.id as id from submission s
                left join customer c on s.customer_id = c.id
                left join form f on s.form_id = f.id
                left join activity a on a.form_id = f.id
                left join participant p on p.submission_id = s.id
                where a.class = 'course_activity'
                and s.status is null
                and p.id is null;""")

        log.info "waiting submissions to migrate = ${result.size()}"

        result.each {
            Submission submission = Submission.findById(it.id as Long)
            submission.status = Submission.Status.WAITING
            submission.save()
        }
        groovySql.close()
    }

    /**
     * Removes participants that do not belong to a course
     */
    @Transactional
    void removeNonCourseParticipants() {
        List<Participant> participants = Participant.createCriteria().list() {
            createAlias('activity', 'a')
            eq('a.class', 'event_activity')
        }

        participants.each { Participant p ->
            p.delete()
        }

        log.info "Deleted ${participants.size()} non-course participants"
    }

    /**
     * Makes sure participants planned to other courses get their specific participant objects on those courses
     */
    void migrateSubmissionsToRightCourse() {
        List result = groovySql.rows("""select s.id as submissionId, a.id as courseId, p.id as participantId from participant p 
                        left join activity a on p.activity_id = a.id
                        left join submission s on p.submission_id = s.id
                    where a.class = 'course_activity' and s.id is not null and a.form_id != s.form_id;""")

        log.info "Submissions to fix ${result.size()}"

        result.each {
            Long submissionId = it.submissionId
            Long courseId = it.courseId
            Long participantId = it.participantId

            Submission originalSubmission = Submission.get(submissionId)
            CourseActivity targetCourse = CourseActivity.get(courseId)
            Participant participant = Participant.get(participantId)

            Submission newSubmission = editParticipantService.copySubmission(originalSubmission, targetCourse)
            participant.submission = newSubmission
            participant.save()
            originalSubmission.delete()
        }
        groovySql.close()
    }

    /**
     * Makes sure participants planned to other courses get their specific participant objects on those courses
     */
    void migrateParticipantsPlannedElsewhere() {
        List result = groovySql.rows("""select p.id as participantId, plannedActivity.id as targetCourseId from activity_occasion_participants aop
              left join activity_occasion ao on ao.id = aop.activity_occasion_id
              left join activity plannedActivity on plannedActivity.id = ao.activity_id
              left join participant p on aop.participant_id = p.id
              left join activity registeredActivity on registeredActivity.id = p.activity_id
              left join customer c on c.id = p.customer_id
            where plannedActivity.id != registeredActivity.id 
            and registeredActivity.class = 'course_activity' and plannedActivity.class = 'course_activity'
            group by participantId, plannedActivity.id;""")
        /**
         * The reason for these lists are to separate migration to avoid lock/session problems.
         * But also because one two different participants might be copied into ONE new.
         * And one existing might be copied into several new.
         * It depends on how they are planned.
         */
        Map<Participant, List<Participant>> oldToNew = [:]

        Participant.withTransaction {
            result.each {
                Participant participant = Participant.get(it.participantId)
                CourseActivity targetCourse = CourseActivity.get(it.targetCourseId)

                if(targetCourse.isAlreadyParticipant(participant.customer.id)) {
                    // We will point the activityOccasions to the existing participant instead
                    Participant existingParticipant = targetCourse.participants?.asList().find { Participant p ->
                        return p.customer.id == participant.customer.id
                    }

                    // We don't want to process same existing participant twice
                    // Different participants can point to the same new course, but we only need to create one
                    if(!oldToNew[participant]?.contains(existingParticipant)) {
                        if(!oldToNew.containsKey(participant)) oldToNew[participant] = []
                        oldToNew[participant].add(existingParticipant)
                    }

                } else {
                    Participant newParticipant = editParticipantService.copyParticipantWithSubmission(participant, targetCourse, true)

                    // Since we might look at this course later on in this migration
                    targetCourse.addToParticipants(newParticipant)

                    if(!oldToNew.containsKey(participant)) oldToNew[participant] = []
                    oldToNew[participant].add(newParticipant)
                }
            }
        }

        ActivityOccasion.withTransaction {
            oldToNew.each { Participant oldParticipant, List<Participant> news ->
                news.each { Participant newParticipant ->
                    List<ActivityOccasion> occasionsOnNewCourse = oldParticipant.occasions?.asList().findAll { ActivityOccasion occasion ->
                        return occasion.activity.id == newParticipant.activity.id
                    }

                    occasionsOnNewCourse.each { ActivityOccasion occasion ->
                        occasion.removeFromParticipants(oldParticipant)
                        occasion.addToParticipants(newParticipant)
                        occasion.save(failOnError: true)
                    }
                }
            }
        }

        log.info "Finished migrating participants step 1"
        groovySql.close()
    }

    /**
     * Checks if participant can be removed.
     * @param p
     * @return
     */
    boolean isRemovable(Participant p) {
        return !(p.occasions?.size() > 0) && p.submission && p.status.equals(Participant.Status.QUEUED)
    }

    /**
     * Checks if participant status should be changed to RESERVED.
     * @param p
     * @return
     */
    boolean isReservable(Participant p) {
        return !(p.occasions?.size() > 0) && !p.submission && p.status.equals(Participant.Status.QUEUED)
    }

    /**
     * Checks if participant status should be changed to ACTIVE.
     * @param p
     * @return
     */
    boolean isActivateable(Participant p) {
        return p.occasions?.size() > 0 && p.status.equals(Participant.Status.QUEUED)
    }
}
