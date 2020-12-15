package com.matchi.facility

import grails.validation.Validateable

/**
 * @author Sergei Shushkevich
 */
@Validateable(nullable = true)
class FacilityOfferCopyCommand implements Serializable {

    List<FacilityOfferCopyItemCommand> items = [].withLazyDefault {new FacilityOfferCopyItemCommand()}

    static constraints = {
        items minSize: 1, validator: { val -> val.every { it.validate() } }
    }
}