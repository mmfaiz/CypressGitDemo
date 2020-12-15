package com.matchi.activities.trainingplanner

import com.matchi.*
import com.matchi.requests.Request
import com.matchi.requests.TrainerRequest
import org.joda.time.DateTime
import org.joda.time.LocalDate
import org.joda.time.LocalDateTime
import org.joda.time.LocalTime
import org.junit.Test

import static com.matchi.TestUtils.*

class TrainerServiceIntegrationTests extends GroovyTestCase {

    TrainerService trainerService

    @Test
    void testGetAvailableTrainers() {
        LocalDateTime thisHour = new LocalDateTime().withHourOfDay(12).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0)
        LocalDateTime inAnHour     = thisHour.plusHours(1)
        LocalDateTime inTwoHours   = thisHour.plusHours(2)
        LocalDateTime inThreeHours = thisHour.plusHours(3)

        Facility facility = Facility.first()

        Sport sport1 = Sport.get(1)
        Sport sport2 = Sport.get(2)

        Trainer trainer1 = createTrainer(facility, sport1, null, true)
        Trainer trainer2 = createTrainer(facility, sport1, null, true)
        Trainer trainer3 = createTrainer(facility, sport2, null, true)

        Availability a1 = createAvailability(1, thisHour.toLocalTime(), inThreeHours.toLocalTime())
        Availability a2 = createAvailability(2, inTwoHours.toLocalTime(), inThreeHours.toLocalTime())

        trainer1.addToAvailabilities(a1)
        trainer2.addToAvailabilities(a2)
        trainer3.addToAvailabilities(a1)

        Date start1 = thisHour.withDayOfWeek(2).toDate()
        Date end1 = inAnHour.withDayOfWeek(2).toDate()

        // Search for wrong day
        assert trainerService.getAvailableTrainers(facility, start1, end1, sport1).isEmpty()

        Date start2 = inAnHour.withDayOfWeek(1).toDate()
        Date end2 = inTwoHours.withDayOfWeek(1).toDate()

        // Search for other sport
        assert trainerService.getAvailableTrainers(facility, start2, end2, sport2).size() == 1
        assert trainerService.getAvailableTrainers(facility, start2, end2, sport2).first() == trainer3

        // Should return trainer1
        assert trainerService.getAvailableTrainers(facility, start2, end2, sport1).size() == 1
        assert trainerService.getAvailableTrainers(facility, start2, end2, sport1).first() == trainer1

        trainer2.addToAvailabilities(a1).save(flush: true)

        // Should return both trainers
        assert trainerService.getAvailableTrainers(facility, start2, end2, sport1).size() == 2
    }

    @Test
    void testGetAvailableTrainersHavingTrainerRequests() {
        LocalDateTime thisHour = new LocalDateTime().withHourOfDay(12).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0)
        LocalDateTime inAnHour     = thisHour.plusHours(1)

        Facility facility = Facility.first()
        Sport sport = Sport.first()
        User user = createUser()

        Trainer trainer1 = createTrainer(facility, sport, null, true)

        Availability a1 = createAvailability(1, thisHour.toLocalTime(), inAnHour.toLocalTime())
        trainer1.addToAvailabilities(a1)

        Date start = thisHour.withDayOfWeek(1).toDate()
        Date end = inAnHour.withDayOfWeek(1).toDate()

        // Should return trainer1
        assert trainerService.getAvailableTrainers(facility, start, end, sport).size() == 1
        assert trainerService.getAvailableTrainers(facility, start, end, sport).first() == trainer1

        // Request is exactly the same
        TrainerRequest request = new TrainerRequest(requester: user, trainer: trainer1, status: Request.Status.NEW,
                start: start, end: end).save(failOnError: true, flush: true)

        // Should return trainer1
        assert trainerService.getAvailableTrainers(facility, start, end, sport).size() == 1
        assert trainerService.getAvailableTrainers(facility, start, end, sport).first() == trainer1

        request.status = Request.Status.ACCEPTED
        request.save(flush: true, failOnError: true)

        // Now the trainer is busy
        assert trainerService.getAvailableTrainers(facility, start, end, sport).size() == 0

        // Starts 5 minutes later
        request.start = thisHour.withDayOfWeek(1).withMinuteOfHour(5).toDate()
        request.save(flush: true, failOnError: true)

        // Still busy
        assert trainerService.getAvailableTrainers(facility, start, end, sport).size() == 0

        // Ends 5 minutes later also (so +5/-5)
        request.end = inAnHour.withDayOfWeek(1).withMinuteOfHour(5).toDate()
        request.save(flush: true, failOnError: true)

        // Still busy
        assert trainerService.getAvailableTrainers(facility, start, end, sport).size() == 0

        // Starts same time as slot, ends later
        request.start = start
        request.save(flush: true, failOnError: true)

        // Still busy
        assert trainerService.getAvailableTrainers(facility, start, end, sport).size() == 0

        // Ends same time as slot, starts 5 minutes earlier
        request.end = end
        request.start = thisHour.minusHours(1).withDayOfWeek(1).withMinuteOfHour(55).toDate()
        request.save(flush: true, failOnError: true)

        // Still busy
        assert trainerService.getAvailableTrainers(facility, start, end, sport).size() == 0

        // Also ends later (so -5/+5)
        request.end = inAnHour.withDayOfWeek(1).withMinuteOfHour(5).toDate()
        request.save(flush: true, failOnError: true)

        // Still busy
        assert trainerService.getAvailableTrainers(facility, start, end, sport).size() == 0

        // One hour earlier, ends when slot starts
        request.start = end
        request.end = inAnHour.plusHours(1).withDayOfWeek(1).toDate()
        request.save(flush: true, failOnError: true)

        // Not busy
        assert trainerService.getAvailableTrainers(facility, start, end, sport).size() == 1
        assert trainerService.getAvailableTrainers(facility, start, end, sport).first() == trainer1

        // Same time, different day
        request.start = thisHour.withDayOfWeek(2).toDate()
        request.end = inAnHour.withDayOfWeek(2).toDate()
        request.save(flush: true, failOnError: true)

        // Not busy
        assert trainerService.getAvailableTrainers(facility, start, end, sport).size() == 1
        assert trainerService.getAvailableTrainers(facility, start, end, sport).first() == trainer1

        // Adding other trainers
        Trainer trainer2 = createTrainer(facility, Sport.first(), null, true)

        Availability a2 = createAvailability(1, thisHour.toLocalTime(), inAnHour.toLocalTime())
        trainer2.addToAvailabilities(a2)

        assert trainerService.getAvailableTrainers(facility, start, end, sport).size() == 2
        assert trainerService.getAvailableTrainers(facility, start, end, sport).contains(trainer1)
        assert trainerService.getAvailableTrainers(facility, start, end, sport).contains(trainer2)

        request.start = start
        request.end = end
        request.save(flush: true, failOnError: true)

        assert trainerService.getAvailableTrainers(facility, start, end, sport).size() == 1
        assert trainerService.getAvailableTrainers(facility, start, end, sport).first() == trainer2
    }

    @Test
    void testGetAvailableTrainersAvailabilityRestrictions() {
        LocalDateTime thisHour = new LocalDateTime().withHourOfDay(12).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0)
        LocalDateTime inAnHour     = thisHour.plusHours(1)
        LocalDateTime inTwoHours   = thisHour.plusHours(2)
        LocalDateTime inThreeHours = thisHour.plusHours(3)

        Facility facility = Facility.first()
        Sport sport = Sport.first()

        Trainer trainer1 = createTrainer(facility, sport, null, true)

        Availability a1 = createAvailability(1, thisHour.toLocalTime(), inThreeHours.toLocalTime())

        trainer1.addToAvailabilities(a1)

        Date start1 = inAnHour.withDayOfWeek(1).toDate()
        Date end1 = inTwoHours.withDayOfWeek(1).toDate()

        // Should return trainer1
        assert trainerService.getAvailableTrainers(facility, start1, end1, sport).size() == 1
        assert trainerService.getAvailableTrainers(facility, start1, end1, sport).first() == trainer1

        // Availability is for that day
        a1.validStart = new LocalDate(start1)
        a1.validEnd = new LocalDate(end1)
        a1.save(flush: true, failOnError: true)

        // So the trainer is available
        assert trainerService.getAvailableTrainers(facility, start1, end1, sport).size() == 1
        assert trainerService.getAvailableTrainers(facility, start1, end1, sport).first() == trainer1

        // But now it was only valid yesterday
        a1.validEnd = new LocalDate(start1.minus(1))
        a1.validStart = new LocalDate(start1.minus(1))
        a1.save(flush: true, failOnError: true)

        // So no trainer in sight :(
        assert trainerService.getAvailableTrainers(facility, start1, end1, sport).size() == 0

        // Or instead, only available the day after
        a1.validEnd = new LocalDate(start1.plus(1))
        a1.validStart = new LocalDate(start1.plus(1))
        a1.save(flush: true, failOnError: true)

        // So no trainer in sight :(
        assert trainerService.getAvailableTrainers(facility, start1, end1, sport).size() == 0

        // Covering three days
        a1.validStart = new LocalDate(start1.minus(1))
        a1.validEnd = new LocalDate(end1.plus(1))
        a1.save(flush: true, failOnError: true)

        // So the trainer is available
        assert trainerService.getAvailableTrainers(facility, start1, end1, sport).size() == 1
        assert trainerService.getAvailableTrainers(facility, start1, end1, sport).first() == trainer1

        // Special case if slot starts super early, at midnight - the same time as the availability
        Date start2 = a1.validStart.toDate()
        Date end2 = new DateTime(start2).plusHours(1).toDate()
        a1.begin = new LocalTime(a1.validStart.toDate())
        a1.end = new DateTime(start2).withHourOfDay(23).withMinuteOfHour(59).withSecondOfMinute(59).withMillisOfSecond(0).toLocalTime()
        a1.weekday = new DateTime(start2).dayOfWeek
        a1.validEnd = a1.validStart

        a1.save(flush: true, failOnError: true)

        // Trainer still available
        assert trainerService.getAvailableTrainers(facility, start2, end2, sport).size() == 1
        assert trainerService.getAvailableTrainers(facility, start2, end2, sport).first() == trainer1

        // Or ends super later
        Date end3 = new DateTime(a1.validEnd.toDate()).withHourOfDay(23).withMinuteOfHour(59).withSecondOfMinute(59).withMillisOfSecond(0).toDate()
        Date start3 = new DateTime(end3).minusHours(1).toDate()

        // Trainer still available
        assert trainerService.getAvailableTrainers(facility, start3, end3, sport).size() == 1
        assert trainerService.getAvailableTrainers(facility, start3, end3, sport).first() == trainer1

    }

    @Test
    void testGetAvailableTrainersAvailabilityRestrictionsPotentiallyColliding() {
        LocalDateTime thisHour = new LocalDateTime().withHourOfDay(12).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0)
        LocalDateTime inAnHour     = thisHour.plusHours(1)
        LocalDateTime inTwoHours   = thisHour.plusHours(2)
        LocalDateTime inThreeHours = thisHour.plusHours(3)

        Facility facility = Facility.first()
        Sport sport = Sport.first()

        Trainer trainer1 = createTrainer(facility, sport, null, true)

        Availability a1 = createAvailability(1, thisHour.toLocalTime(), inThreeHours.toLocalTime())

        trainer1.addToAvailabilities(a1)

        Date start1 = inAnHour.withDayOfWeek(1).toDate()
        Date end1 = inTwoHours.withDayOfWeek(1).toDate()

        // Should return trainer1
        assert trainerService.getAvailableTrainers(facility, start1, end1, sport).size() == 1
        assert trainerService.getAvailableTrainers(facility, start1, end1, sport).first() == trainer1

        // Availability is for that day
        a1.validStart = new LocalDate(start1)
        a1.validEnd = new LocalDate(end1)
        a1.save(flush: true, failOnError: true)

        // So the trainer is available
        assert trainerService.getAvailableTrainers(facility, start1, end1, sport).size() == 1
        assert trainerService.getAvailableTrainers(facility, start1, end1, sport).first() == trainer1

        // Passed availability, for same weekday and time
        Availability a2 = createAvailability(1, thisHour.toLocalTime(), inThreeHours.toLocalTime())
        a2.validStart = new LocalDate(start1.minus(7))
        a2.validEnd = new LocalDate(end1.minus(7))
        a2.save(flush: true, failOnError: true)


        trainer1.addToAvailabilities(a2)

        // So the trainer is still available
        assert trainerService.getAvailableTrainers(facility, start1, end1, sport).size() == 1
        assert trainerService.getAvailableTrainers(facility, start1, end1, sport).first() == trainer1

        trainer1.removeFromAvailabilities(a1)
        a1.delete(flush: true)

        // Not anymore
        assert trainerService.getAvailableTrainers(facility, start1, end1, sport).size() == 0

    }

    @Test
    void testDateToLocalTimeAndWeekDay() {
        Date start = new LocalDateTime().withDayOfWeek(1).withHourOfDay(13).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0).toDate()

        LocalDateTime startDateTime = new LocalDateTime(start)

        int weekday = startDateTime.dayOfWeek
        LocalTime localTime = startDateTime.toLocalTime()

        assert localTime.toString() == "13:00:00.000"
        assert weekday == 1
    }

    @Test
    void testGetUserBookableTrainers() {
        Facility facility = Facility.first()
        Sport sport = Sport.first()
        User user = createUser()
        Customer customer = createCustomer(facility)

        Trainer trainer1 = createTrainer(facility, sport, customer)
        Trainer trainer2 = createTrainer(facility, sport, customer)

        assert trainerService.getUserBookableTrainers(user).isEmpty()
        customer.user = user
        assert trainerService.getUserBookableTrainers(user).isEmpty()
        trainer1.isBookable = true
        assert trainerService.getUserBookableTrainers(user).size() == 1
        trainer2.isBookable = true
        assert trainerService.getUserBookableTrainers(user).size() == 2
    }

    @Test
    void testGetUserBookableTrainerRequests() {
        Facility facility = Facility.first()
        User user = createUser()
        Customer customer = createCustomer(facility)
        customer.user = user

        Trainer trainer = createTrainer(facility, Sport.first(), customer, true)

        assert trainerService.getUserBookableTrainerRequests(user).isEmpty()

        // Upcoming request
        new TrainerRequest(requester: user, trainer: trainer, status: Request.Status.NEW,
                start: new LocalDateTime().plusDays(1).toDate(),
                end: new LocalDateTime().plusDays(2).toDate()).save(failOnError: true, flush: true)
        // Past request
        new TrainerRequest(requester: user, trainer: trainer, status: Request.Status.NEW,
                start: new LocalDateTime().minusDays(2).toDate(),
                end: new LocalDateTime().minusDays(1).toDate()).save(failOnError: true, flush: true)

        assert trainerService.getUserBookableTrainerRequests(user).size() == 1
    }
}
