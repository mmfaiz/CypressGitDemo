package com.matchi.facility

import com.matchi.GenericController
import com.matchi.subscriptionredeem.SlotRedeem
import grails.validation.Validateable
import org.joda.time.DateMidnight
import org.joda.time.LocalDate

/**
 * @author Sergei Shushkevich
 */
class FacilitySlotRedeemController extends GenericController {

    def redeemService

    def index(FacilityRedeemFilterCommand filter) {
        filter.redeemed = true
        def redeems = redeemService.getSlotRedeems(filter)

        [redeems: redeems, filter: filter, facility: getUserFacility()]
    }

    def remove() {
        SlotRedeem slotRedeem = SlotRedeem.get(params.slotRedeemId)

        slotRedeem.delete()

        redirect(action: "index")
    }
}

@Validateable(nullable = true)
class FacilityRedeemFilterCommand {

    int offset = 0
    int max = 200
    Boolean redeemed
    String order = "desc"
    String sort  = "dateCreated"
    String q
    LocalDate start = new DateMidnight().minusMonths(1).withDayOfMonth(1).toLocalDate()
    LocalDate end = new DateMidnight().plusMonths(1).withDayOfMonth(1).minusDays(1).toLocalDate()
}
