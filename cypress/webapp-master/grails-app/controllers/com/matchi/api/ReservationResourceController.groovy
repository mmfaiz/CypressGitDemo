package com.matchi.api

import com.matchi.*
import com.matchi.activities.ActivityOccasion
import com.matchi.activities.Participation
import com.matchi.enums.RedeemType
import com.matchi.payment.ArticleType
import grails.converters.JSON
import grails.validation.Validateable
import org.apache.http.HttpStatus

class ReservationResourceController extends GenericAPIController {

    def bookingService
    def activityService

    def list(ReservationCommand cmd) {
        if (cmd.id && cmd.type) {
            IReservation reservation
            if (cmd.type == ArticleType.BOOKING) {
                reservation = getBooking(cmd)
            }
            else if (cmd.type == ArticleType.ACTIVITY) {
                reservation = getParticipation(cmd)
            }
            if (reservation) {
                JSON.use('IReservation', {
                    render reservation as JSON
                })
                return
            }
            return error(404, Code.RESOURCE_NOT_FOUND, "Reservation not found")
        }
        else {
            List<IReservation> reservations = new LinkedList<IReservation>()
            User user = getCurrentUser()

            if (cmd.type == ArticleType.BOOKING || !cmd.type) {
                reservations += bookingService.getUserBookings(user)
            }
            if (cmd.type == ArticleType.ACTIVITY || !cmd.type) {
                reservations += activityService.getUserUpcomingParticipations(user)
            }

            reservations = reservations.sort { IReservation iReservation ->
                return iReservation.getDate()
            }

            JSON.use('IReservation', {
                render reservations as JSON
            })
        }
    }

    Booking getBooking(ReservationCommand cmd) {
        User user = getCurrentUser()
        bookingService.getUserBooking(cmd.id, user)
    }

    Participation getParticipation(ReservationCommand cmd) {
        User user = getCurrentUser()
        activityService.getUserParticipation(cmd.id, user)
    }

    def cancel(ReservationCommand cmd) {
        User user = getCurrentUser()

        if (cmd.type == ArticleType.BOOKING) {
            Booking booking = getBooking(cmd)

            if (booking) {
                if (booking.customer.user && booking.customer.user.id == user.id) {
                    bookingService.cancelBooking(booking, "", false, RedeemType.NORMAL, getCurrentUser(), true)
                    render booking as JSON
                } else {
                    error(403, Code.ACCESS_DENIED, "Access denied")
                }
                return
            }
            return error(400, Code.RESOURCE_NOT_FOUND, "Could not locate booking (${cmd.id})")
        }
        else if (cmd.type == ArticleType.ACTIVITY) {
            Participation participation = getParticipation(cmd)

            if(participation) {
                ActivityOccasion occasion    = participation.occasion
                def cancelResponse = activityService.cancelAndRefundParticipant(occasion, user)
                if (cancelResponse == true) {
                    render status: HttpStatus.SC_OK
                    return
                }
                else if (cancelResponse instanceof String) {
                    return error(400, Code.UNKNOWN_ERROR, "Could not delete participation (${cmd.id}) - " + cancelResponse)
                }
            }

            return error(400, Code.RESOURCE_NOT_FOUND, "Could not locate participation (${cmd.id})")
        }
    }
}

@Validateable(nullable = true)
class ReservationCommand {
    Long id
    ArticleType type

    static constraints = {
        id nullable: false
    }
}

