package com.matchi.facility

import grails.validation.Validateable

/**
 * @author Sergei Shushkevich
 */
@Validateable(nullable = true)
class FacilityPriceListCopyItemCommand {

    Long priceListId
    String priceListName
    String name
    String startDate

    static constraints = {
        priceListId nullable: false
        priceListName nullable: false
        name nullable: false, blank: false
        startDate nullable: false, blank: false
    }
}
