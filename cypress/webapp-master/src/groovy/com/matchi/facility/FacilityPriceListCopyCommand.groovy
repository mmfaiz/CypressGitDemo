package com.matchi.facility

import grails.validation.Validateable

/**
 * @author Sergei Shushkevich
 */
@Validateable(nullable = true)
class FacilityPriceListCopyCommand {

    List<FacilityPriceListCopyItemCommand> items = [].withLazyDefault { new FacilityPriceListCopyItemCommand() }

    static constraints = {
        items minSize: 1, validator: { val -> val.every { it.validate() } }
    }
}
