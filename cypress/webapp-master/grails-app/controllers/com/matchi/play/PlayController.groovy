package com.matchi.play

import com.matchi.Booking
import com.matchi.BookingService
import com.matchi.GenericController
import com.matchi.SecurityService

class PlayController extends GenericController {

    PlayService playService
    BookingService bookingService
    SecurityService securityService

    def player(Long bookingId) {
        Booking booking = bookingService.getBooking(bookingId)
        Recording recording = playService.getRecordingFromBooking(booking)

        if (!currentUser) {
            redirect controller: "login", action: "auth", params: [returnUrl: recording.internalPlayerUrl]
            return
        }

        if (!securityService.hasFacilityAccess() && !playService.userCanViewRecording(currentUser, recording) && !recording.isLive()) {
            return response.sendError(404)
        }
        [recording: recording]
    }
}
