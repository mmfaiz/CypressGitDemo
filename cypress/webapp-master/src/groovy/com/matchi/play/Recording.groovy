package com.matchi.play


import com.matchi.Booking
import com.matchi.Camera
import com.matchi.Facility
import com.matchi.RecordingPurchase
import com.matchi.RecordingStatus
import com.matchi.User
import grails.util.Holders
import org.joda.time.DateTime
import org.joda.time.LocalDateTime
import org.joda.time.format.DateTimeFormat


class Recording {
    static final RECORDING_LIFESPAN = 48 * 60 * 60 //48 hours

    Long bookingId
    Date start
    Date end

    RecordingPurchase recordingPurchase

    boolean hasStarted() {
        new Date() > start
    }

    boolean isLive() {
        Date now = new Date()
        now > start && now < end
    }

    boolean hasRecording() {
        booking.recordingStatus?.status == RecordingStatus.Status.RECORDING_EXISTS
    }

    boolean hasLive() {
        isLive() && booking.recordingStatus?.status == RecordingStatus.Status.LIVE_EXISTS
    }

    boolean requiresPayment() {
        return true
    }

    boolean isPurchased() {
        return recordingPurchase?.isFinalPaid()
    }

    def createOrderDescription(User user) {
        return "${booking.id} ${start.toString()} ${end.toString()} ${facility}";
    }

    Booking getBooking() {
        return Booking.get(bookingId)
    }

    Facility getFacility() {
        return booking.slot.court.facility
    }

    Long getPrice() {
        def paymentService = Holders.grailsApplication.mainContext.getBean('paymentService')
        long priceInclVAT = paymentService.getRecordingPrice(this.booking.slot.court.facility.currency)
        return priceInclVAT
    }

    String getDescription() {
        StringBuilder sb = new StringBuilder()
        sb.append(DateTimeFormat.forPattern("yyyy-MM-dd").print(new DateTime(booking.slot.startTime)))
        sb.append(" ")
        sb.append(DateTimeFormat.forPattern("HH:mm").print(new DateTime(booking.slot.startTime)))
        sb.append("-")
        sb.append(DateTimeFormat.forPattern("HH:mm").print(new DateTime(booking.slot.endTime)))
        sb.append(" ")
        sb.append(booking.slot.court.facility.name)
        sb.append(" ")
        sb.append(booking.slot.court.name)
        sb.append(" video")
    }

    Date getLastViewableDate() {
        LocalDateTime.fromDateFields(end).plusSeconds(RECORDING_LIFESPAN).toDate()
    }

    boolean canBePurchased() {
        def now = new LocalDateTime().toDate()
        return now <= lastViewableDate && hasStarted()
    }

    boolean isPossiblyAccessed() {
        return isLive() || canBePurchased() || recordingPurchase?.isFinalPaid()
    }

    String getArchiveUrl() {
        if (recordingPurchase?.archiveUrl == null) {
            return booking.recordingStatus?.mediaUrl
        }
        return recordingPurchase.archiveUrl
    }

    Camera.CameraProvider getCameraProvider() {
        booking.slot.court.cameras?.first()?.cameraProvider
    }

    String getLiveStreamUrl() {
        return booking.recordingStatus?.mediaUrl
    }

    String getInternalPlayerUrl() {
        return "/play/player/" + bookingId
    }
}
