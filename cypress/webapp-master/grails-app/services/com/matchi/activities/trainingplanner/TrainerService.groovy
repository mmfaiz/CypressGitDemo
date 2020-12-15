package com.matchi.activities.trainingplanner

import com.matchi.Booking
import com.matchi.Facility
import com.matchi.Sport
import com.matchi.User
import com.matchi.activities.ActivityOccasion
import com.matchi.requests.Request
import com.matchi.requests.TrainerRequest
import org.hibernate.criterion.CriteriaSpecification
import org.joda.time.DateTime
import org.joda.time.LocalDateTime
import org.joda.time.LocalTime

class TrainerService {

    void delete(Trainer trainer) {
        List<CourseActivity> courseActivities = getCourseActivitesWithTrainer(trainer)
        courseActivities.each { courseActivity ->
            courseActivity.removeFromTrainers(trainer)
            courseActivity.save(flush: true)
        }

        List<ActivityOccasion> activityOccasions = getActivityOccasionsWithTrainer(trainer)
        activityOccasions.each { activityOccasion ->
            activityOccasion.removeFromTrainers(trainer)
            activityOccasion.save(flush: true)
        }

        trainer.delete(flush: true)
    }

    private List<CourseActivity> getCourseActivitesWithTrainer(Trainer trainer) {
        CourseActivity.createCriteria().list { trainers { eq('id', trainer.id) } }
    }

    private List<ActivityOccasion> getActivityOccasionsWithTrainer(Trainer trainer) {
        ActivityOccasion.createCriteria().list { trainers { eq('id', trainer.id) } }
    }

    List<Trainer> getAvailableTrainers(Facility facility, Date start, Date end, Sport sport) {
        log.debug("Getting available trainers for ${facility.id}, ${start}, ${end}, ${sport}")

        LocalDateTime startLocalDateTime = new LocalDateTime(start)
        LocalDateTime endLocalDateTime = new LocalDateTime(end)

        int weekDay = startLocalDateTime.dayOfWeek
        LocalTime startLocalTime = startLocalDateTime.toLocalTime()
        LocalTime endLocalTime = endLocalDateTime.toLocalTime()

        List<Trainer> trainers = Trainer.createCriteria().list {
            createAlias("availabilities", "a", CriteriaSpecification.LEFT_JOIN)

            eq("facility", facility)
            eq("isBookable", Boolean.TRUE)
            eq("sport", sport)

            eq("a.weekday", weekDay)
            le("a.begin", startLocalTime)
            ge("a.end", endLocalTime)

            or {
                and {
                    isNull("a.validStart")
                    isNull("a.validEnd")
                }
                and {
                    not {
                        gt("a.validStart", endLocalDateTime.toLocalDate())
                        lt("a.validEnd", startLocalDateTime.toLocalDate())
                    }
                }

            }

            order("firstName", "asc")
            order("lastName", "asc")
        }

        if(trainers.size() == 0) return trainers

        List<TrainerRequest> trainerRequests = TrainerRequest.createCriteria().list {
            inList("trainer", trainers)
            eq("status", Request.Status.ACCEPTED)
            or {
                and {
                    gt("start", start)
                    lt("end", end)
                }
                and {
                    lt("start", end)
                    gte("end", end)
                }
                and {
                    gte("start", start)
                    lt("start", start)
                }
                and {
                    lte("start", start)
                    gte("end", end)
                }
            }
        }

        return trainers - trainerRequests*.trainer.unique()
    }

    List<Trainer> getUserBookableTrainers(User user) {
        return Trainer.createCriteria().list {
            createAlias("customer", "c", CriteriaSpecification.LEFT_JOIN)
            createAlias("c.user", "u", CriteriaSpecification.LEFT_JOIN)

            eq("isBookable", Boolean.TRUE)
            eq("u.id", user.id)

            order("firstName", "asc")
            order("lastName", "asc")
        }
    }

    List<Booking> getTrainersBookings(Facility facility, Trainer trainer, DateTime startDate, DateTime endDate) {
        Booking.createCriteria().list {
            createAlias("trainers", "t", CriteriaSpecification.INNER_JOIN)
            createAlias("slot", "s", CriteriaSpecification.INNER_JOIN)

            if (trainer) {
                eq("t.id", trainer.id)
            } else {
                eq("t.facility", facility)
            }
            gte("s.startTime", startDate.toLocalDateTime().toDate())
            lt("s.startTime", endDate.toLocalDateTime().toDate())
        }
    }

    List<TrainerRequest> getUserBookableTrainerRequests(User user) {
        return TrainerRequest.createCriteria().list {
            createAlias("trainer", "t", CriteriaSpecification.LEFT_JOIN)
            createAlias("t.customer", "c", CriteriaSpecification.LEFT_JOIN)
            createAlias("c.user", "u", CriteriaSpecification.LEFT_JOIN)

            eq("t.isBookable", Boolean.TRUE)
            eq("u.id", user.id)

            gt("end", new LocalDateTime().toDate())

            order("t.firstName", "asc")
            order("t.lastName", "asc")
        }
    }
}
