package com.matchi

import com.matchi.price.CourtPriceCondition



class PriceListConditionFormTagLib {

    def userService

    def priceListConditionForm = { attrs, body ->
        def model = createModel(attrs.condition, attrs.pricelist)
        def conditionName =  attrs.condition.getClass().getSimpleName().toLowerCase()
        out << render(template:"/templates/pricelist/${conditionName}.form", model: model)
    }

    def priceListConditionEntry = { attrs, body ->
        def model = createModel(attrs.condition, attrs.pricelist)
        def conditionName =  attrs.condition.getClass().getSimpleName().toLowerCase()
        out << render(template:"/templates/pricelist/${conditionName}.read", model: model)
    }

    private def createModel(def condition, def pricelist = null) {
        def model = [:]

        model.condition = condition


        if(condition instanceof CourtPriceCondition) {
            model.courts = Court.available(userService.getLoggedInUser().facility).list()
            if(pricelist?.sport) {
                model.courts = model.courts.findAll { it.sport.id == pricelist.sport.id }
            }
        }

        return model;
    }
}
