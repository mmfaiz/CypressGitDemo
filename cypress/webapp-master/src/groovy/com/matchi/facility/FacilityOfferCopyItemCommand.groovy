package com.matchi.facility

import grails.validation.Validateable

/**
 * @author Sergei Shushkevich
 */
@Validateable(nullable = true)
class FacilityOfferCopyItemCommand implements Serializable {

    Long offerId
    String offerName
    String name

    static constraints = {
        offerId nullable: false
        offerName nullable: false
        name nullable: false, blank: false
    }
}
