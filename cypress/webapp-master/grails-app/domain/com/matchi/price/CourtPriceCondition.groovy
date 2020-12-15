package com.matchi.price
import com.matchi.Court
import com.matchi.Customer
import com.matchi.Slot

class CourtPriceCondition extends BookingPriceCondition {

    static belongsTo = Court
    static hasMany = [ courts:Court ]
    static transients = [ "courtsToBeSaved" ]

    def courtsToBeSaved = []

    @Override
    def prepareForSave() {
        courtsToBeSaved.each { Court court ->
            court = court.merge()
            court.addToCourtPriceConditions(this)
        }
    }
    @Override
    def prepareForDelete() {
        def courtsConnected = []
        this.courts.each {
            courtsConnected << it
        }

        courtsConnected.each { Court court ->
            court.removeFromCourtPriceConditions(this)
        }
    }

    static mapping = {
        courts joinTable:[name:"court_price_condition_courts", key:'cc_courtpricecondition_id' ]
    }

    static constraints = {
    }

    @Override
    boolean accept(Slot slot, Customer customer) {
        return courts.collect() { it.id }.contains(slot.court.id)
    }
}
