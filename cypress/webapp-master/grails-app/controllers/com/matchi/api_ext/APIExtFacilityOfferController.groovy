package com.matchi.api_ext

import com.matchi.Facility
import com.matchi.api_ext.model.APIExtOffer
import com.matchi.coupon.Offer
import grails.converters.JSON

class APIExtFacilityOfferController extends APIExtGenericController {

    def offers(Long facilityId) {
        Facility facility = getFacility(facilityId)

        def result = []
        Offer.findAllByFacilityAndArchived(facility, false, [sort: "name", order: "asc", max: 50]).each { offer ->
            APIExtOffer apiExtOffer = new APIExtOffer(offer)
            result << apiExtOffer
        }

        render result as JSON
    }

}
