package com.matchi

import com.matchi.requirements.RequirementProfile
import com.matchi.subscriptionredeem.SlotRedeem
import com.matchi.schedule.TimeSpan
import org.joda.time.DateTime
import org.joda.time.Duration
import org.joda.time.Interval
import org.joda.time.format.DateTimeFormat

class Slot implements Comparable<Slot>, Serializable {

    def grailsApplication

    static belongsTo = [ court: Court ]
    static hasOne    = [ booking: Booking, subscription: Subscription, bookingRestriction: BookingRestriction ]
    static hasMany   = [ payments: Payment, seasonDeviations: SeasonDeviation ]

    String id
    Date startTime
    Date endTime

    int hourStart
    int hourEnd

    static constraints = {
        startTime(nullable: false)
        endTime(nullable: false)
        endTime(validator: { val, obj ->
            return val?.after(obj.startTime)?true:['beforeStartTime']
        })
        booking(nullable: true)
        subscription(nullable: true)
        bookingRestriction(nullable: true)
    }

    static mapping = {
        hourStart formula: "HOUR(start_time)"
        hourEnd formula: "HOUR(end_time)"
        id generator:'uuid'
        seasonDeviations joinTable: [name: "season_deviation_slot", key: 'slot_id' ]
        booking fetch: 'join'

        startTime index: 'start_end_court_idx'
        endTime index: 'start_end_court_idx'
        court index: 'start_end_court_idx'
    }

    def beforeDelete() {
        SlotRedeem.withNewSession {
            SlotRedeem.findAllBySlot(this)*.delete(flush: true)
        }
    }

    def getTimeSpan() {
        return new TimeSpan(new DateTime(startTime), new DateTime(endTime))
    }

    def getDuration() {
        return new Duration(new DateTime(startTime), new DateTime(endTime))
    }

    def getShortDescription() {
        def slotTime = new DateTime(startTime)
        return slotTime.toString("dd/MM")+" "+ slotTime.toString("HH:mm")+", "+court.name
    }

    def toInterval() {
        new Interval(new DateTime(startTime), new DateTime(endTime))
    }

    def refundPercentage() {
        return court.facility.getRefundPercentage(new DateTime(startTime))
    }

    def isRefundable() {
        def refundPercentage = refundPercentage()
        return refundPercentage > 0
    }

    def isRefundableUntil() {
        DateTime start = new DateTime(startTime)
        return start.minusHours(this.court.facility.getBookingCancellationLimit())
    }

    def isBookable() {
        return endTime.after(new Date())
    }

    static def parseAll(def slotIds) {
        if(slotIds == null) return []
        return slotIds.tokenize(',')
    }

    boolean belongsToSubscription() {
        return this.subscription != null
    }

    boolean isBookedBySubscriber() {
        return belongsToSubscription() && (this.booking?.customer == this.subscription?.customer)
    }

    boolean isCanceledSubscriptionSlot() {
        return belongsToSubscription() && !isBookedBySubscriber()
    }

    List<RequirementProfile> getRequirementProfiles() {
        return this.bookingRestriction?.requirementProfiles?.unique()?.sort()
    }

    /**
     * Two Slots are equally restricted if they have the exact same list of requirement profiles
     * @param otherSlot
     * @return
     */
    boolean isEquallyRestrictedAs(Slot otherSlot) {
        List<RequirementProfile> otherRequirementProfiles = otherSlot.getRequirementProfiles()
        boolean equals = otherRequirementProfiles?.size() == this.getRequirementProfiles()?.size()

        if(equals) {
            this.getRequirementProfiles()?.eachWithIndex { RequirementProfile rp, int i ->
                if(rp.id != otherRequirementProfiles?.get(i).id) equals = false
            }
        }

        return equals
    }

    def getDescription() {
        StringBuilder sb = new StringBuilder()
        sb.append(DateTimeFormat.forPattern("yyyy-MM-dd").print(new DateTime(this.startTime)))
        sb.append(" ")
        sb.append(DateTimeFormat.forPattern("HH:mm").print(new DateTime(this.startTime)))
        sb.append("-")
        sb.append(DateTimeFormat.forPattern("HH:mm").print(new DateTime(this.endTime)))
        sb.append(" ")
        sb.append(this.court.facility.name)
        sb.append(" ")
        sb.append(this.court.name)
    }

    int compareTo(Slot other) {
        int res = 0
        if(other == null) {
            return 1
        }
        if(this) {
            res = this.startTime.compareTo(other?.startTime)
            if(res == 0) {
                res = this.endTime.compareTo(other?.endTime)
            }
        }


        if(res == 0 && this && other.court) {
            res = this.court.id.compareTo(other.court.id)
        }
        return res
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        Slot slot = (Slot) o

        if (startTime != slot.startTime) return false
        if (endTime != slot.endTime) return false
        if (court != null && slot.court != null && (court.id != slot.court.id)) return false

        return true
    }
}
