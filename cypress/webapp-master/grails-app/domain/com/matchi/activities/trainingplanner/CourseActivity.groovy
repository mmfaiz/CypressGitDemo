package com.matchi.activities.trainingplanner


import com.matchi.Facility
import com.matchi.activities.Activity
import com.matchi.activities.Participant
import com.matchi.dynamicforms.Form
import org.joda.time.LocalDate
/**
 * @author Sergei Shushkevich
 */
class CourseActivity extends Activity {
    private static final long serialVersionUID = 12L

    public static final String DISCRIMINATOR = "course_activity"

    Date startDate
    Date endDate
    Form form
    String emails
    Integer listPosition
    Boolean showOnline

    String extendedEmailMessage

    String hintColor = HintColor.GREEN

    static belongsTo = [facility: Facility]

    static hasMany = [participants: Participant, trainers: Trainer]

    static constraints = {
        endDate validator: { val, obj ->
            return val && !val.before(obj.startDate) ?: 'course.endDate.validation.error'
        }
        form nullable: false    // it's only on domain level, in DB it should remain nullable
        showOnline nullable: true
        emails nullable: true
        listPosition nullable: true
        extendedEmailMessage nullable: true
    }

    static mapping = {
        discriminator DISCRIMINATOR
    }

    static namedQueries = {
        facilityActiveCourses { Facility facility ->
            eq("facility.id", facility.id)
            ge("startDate", new Date())
            order("listPosition", "asc")
        }
        activeAndUpcomingCourses { f ->
            eq("facility", f)
            ge("endDate", new Date().clearTime())
            order("listPosition", "asc")
        }
        archivedCourses { f ->
            eq("facility", f)
            lt("endDate", new Date().clearTime())
            order("listPosition", "asc")
        }
    }

    def beforeInsert() {
        if (listPosition == null) {
            def max = withCriteria(uniqueResult: true) {
                projections {
                    max("listPosition")
                }
                eq("facility", facility)
            }
            listPosition = max ? max + 1 : 1
        }
    }

    String toString() {
        name
    }

    @Override
    String[] getToMails() {
        return emails ? emails?.split(",") : [facility?.email]
    }

    public enum HintColor {
        BLUE, BLUEGREEN, GREEN, YELLOWGREEN, YELLOW, YELLOWORANGE, ORANGE, REDORANGE, RED, REDPURPLE, PURPLE, BLUEPURPLE, BLACK, BROWN, PINK, GREY

        static list() {
            return [BLUE, BLUEGREEN, GREEN, YELLOWGREEN, YELLOW, YELLOWORANGE, ORANGE, REDORANGE, RED, REDPURPLE, PURPLE, BLUEPURPLE, BLACK, BROWN, PINK, GREY]
        }
    }

    /**
     * Checks if customer is participating in course by check id
     * @param customerId
     * @return
     */
    boolean isAlreadyParticipant(Long customerId) {
        return participants?.asList().any { Participant p ->
            return p.customer.id == customerId
        }
    }

    boolean isArchived() {
        final LocalDate thisLocalEndDate = new LocalDate(this.endDate)
        final LocalDate nowLocalDate = new LocalDate()
        return nowLocalDate.isAfter(thisLocalEndDate)
    }


}
