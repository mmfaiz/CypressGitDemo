package com.matchi

import com.matchi.price.PriceListCustomerCategory
import com.matchi.price.Price
import grails.transaction.Transactional

class CustomerCategoryService {
    static transactional = false

    def getCustomerCategory(def id) {
        return PriceListCustomerCategory.get(id)
    }

    def getFacilityCustomerCategories(def facility) {
        return PriceListCustomerCategory.findAllByFacilityAndDeleted(facility,false)
    }

    def getFacilityCustomerCategories(def facility, boolean excludeDefaults) {
        if(excludeDefaults) {
            return PriceListCustomerCategory.findAllByFacilityAndDefaultCategoryAndDeleted(facility, false,false)
        } else {
            return PriceListCustomerCategory.findAllByFacilityAndDeleted(facility,false)
        }

    }

    @Transactional
    def removeCustomerCategory(PriceListCustomerCategory customerCategory) {

        List<Price> categoryPricesList = customerCategory.prices.toList()
        categoryPricesList.each { Price price ->
            customerCategory.removeFromPrices(price)
            price.delete()
        }

        customerCategory.deleted = true
    }
}
