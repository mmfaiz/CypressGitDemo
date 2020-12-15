package com.matchi

import com.matchi.sportprofile.SportProfile

class UserProfileTagLib {

    def userService
    def bookingService
    def springSecurityService
    ActivityService activityService

    def profileMenuButton = { attrs, body ->
        def user = userService.getLoggedInUser()

        if (user) {
            out << render(template:"/templates/profile/profileMenuButton", model: [ user: user ])
        }
    }

    def inboxMenuButton = { attrs, body ->
        User.withNewSession { session ->
            User user = userService.getLoggedInUser()
            List<UserMessage> messages = UserMessage.unreadIncomingMessages(user).list(sort: "dateCreated", order: "desc", max: 6)

            if (user) {
                out << render(template:"/templates/profile/inboxMenuButton", model: [ user: user, messages: messages ])
            }
        }
    }

    def bookingsMenuButton = { attrs, body ->
        User user = userService.getLoggedInUser()
        List<IReservation> reservations = bookingService.getUserBookings(user)
        reservations += activityService.getUserUpcomingParticipations(user)

        List<IReservation> sortedReservations = reservations.sort { IReservation iReservation ->
            return iReservation.getDate()
        }

        if (user) {
            out << render(template:"/templates/profile/bookingsMenuButton", model: [ user: user, reservations: sortedReservations ])
        }
    }

    def facilityMenuButton = { attrs, body ->
        def user = userService.getLoggedInUser()

        if (user) {
            out << render(template:"/templates/profile/facilityMenuButton", model: [ user: user ])
        }
    }

    def skillLevel = { attrs, body ->
        def user = User.get(attrs.id)

        if (user) {
            out << render(template:"/templates/profile/skillLevel", model: [user: user])
        }
    }

    def sportProfileExtended = { attrs, body ->
        def currentUser = userService.getLoggedInUser()
        def profile = SportProfile.get(attrs.id)
        def sport = Sport.get(attrs.sport)

        out << render(template:"/templates/profile/sportProfileExtended", model: [profile: profile, sport:sport, currentUser:currentUser])
    }

    def ifBookableTrainer = { attrs, body ->
        User user = springSecurityService.getCurrentUser() as User
        if (user.isBookableTrainer()) {
            out << body()
        }
    }

    def facebookNagger = { attrs, body ->
        out << render(template:"/templates/profile/facebookNagger", model: [])
    }
}
