package com.matchi.activities.trainingplanner

import com.matchi.*
import com.matchi.requests.TrainerRequest
import org.joda.time.LocalDate
import org.joda.time.LocalDateTime

class Trainer {

    static belongsTo = [ Facility, Booking ]
    static hasMany   = [ availabilities: Availability, bookings: Booking ]

    User user
    Facility facility
    String firstName
    String lastName
    String phone
    String email
    MFile profileImage
    Sport sport
    Customer customer

    String description

    Boolean isActive = false
    Boolean isBookable = false

    boolean showOnline = true

    static constraints = {
        user(nullable: true)
        phone(nullable: true)
        email(nullable: true, email: true)
        description(nullable: true)
        profileImage(nullable: true)
        customer(nullable: true)
        isBookable(nullable: true)
    }

    static mapping = {
        bookings joinTable: [name: "booking_trainer", key: "trainer_id", column: "booking_id"]
        description type: 'text'
        sort([firstName: 'asc', lastName:'asc'])
    }
    
    String toString() {
        "${firstName} ${lastName}"
    }

    String fullName() {
        "${firstName} ${lastName}"
    }

    Boolean hasAvailability() {
        return this.isBookable && this.facility.hasBookATrainer()
    }

    List<TrainerRequest> getRequests() {
        return TrainerRequest.findAllByTrainerAndEndGreaterThan(this, new LocalDateTime().toDate())
    }

    Set<Availability> getCurrentAndFutureAvailabilities(Date compareDate) {
        return getCurrentAndFutureAvailabilities(new LocalDate(compareDate))
    }

    /**
     * Filters out only availabilities that are non-restricted, or currently active or in the future
     * @param compareDate
     * @return
     */
    Set<Availability> getCurrentAndFutureAvailabilities(LocalDate compareDate) {
        if(!this.availabilities || this.availabilities.size() == 0) return []

        return this.availabilities.findAll { Availability availability ->
            return !availability.validEnd || !compareDate.isAfter(availability.validEnd)
        }
    }
}
