package com.matchi.facility

import com.matchi.Court
import com.matchi.GenericController
import com.matchi.PriceList
import org.joda.time.format.DateTimeFormat
import com.matchi.price.*

class FacilityPriceListConditionController extends GenericController {
    static def PRICE_CONDITIONS_KEY = "price_conditions"

    def priceListService

    def index() {
        def prices = [:]
        def priceList = PriceList.get(params.id)
        def facility = getUserFacility()
        clearUnsavedConditions()

        if (priceList) {
            assertFacilityAccessTo(priceList)
        }

        List<PriceListCustomerCategory> customerCategories = PriceListCustomerCategory.available(facility).listDistinct()

        customerCategories.each { customerCategory ->
            customerCategory.prices.each {
                prices.put("price_${it.customerCategory.id}_${it.priceCategory.id}".toString(), it)
            }

        }

        return [facility: facility, pricelist: priceList, customerCategories: customerCategories,
                prices: prices]
    }

    def form() {
        def availableConditions = [new DatePriceCondition(), new TimePriceCondition(), new WeekDayPriceCondition(),
                                   new CourtPriceCondition(), new TimeBeforeBookingCondition()]

        def priceList = PriceList.get(params.id)
        def category = PriceListConditionCategory.get(params.categoryId)

        if(!category) {
            category = new PriceListConditionCategory()
        }

        if(params.hiddenCategoryName) {
            category.name = params.hiddenCategoryName
        }

        def facility = getUserFacility()
        List<PriceListCustomerCategory> customerCategories = PriceListCustomerCategory.available(facility).listDistinct()

        [addedConditions    : getSessionConditions().added,
         removedConditionIds: getSessionConditions().removedIds,
         facility           : facility,
         availableConditions: availableConditions,
         pricelist          : priceList,
         category           : category,
         customerCategories : customerCategories]
    }

    def savePrices() {
        PriceList pricelist = PriceList.get(params.id)

        if (pricelist) {
            assertFacilityAccessTo(pricelist)
        }

        List<PriceListCustomerCategory> customerCategories = PriceListCustomerCategory.available(pricelist.facility).listDistinct()

        pricelist.priceListConditionCategories.each { priceCategory ->
            customerCategories.each { customerCategory ->

                Price.remove(priceCategory, customerCategory)
                Long price = getPriceFor(customerCategory, priceCategory)
                String account = getAccountFor(customerCategory, priceCategory)

                if(price != null) {
                    if(price < 0) {
                        flash.error = message(code: "facilityPriceListCondition.savePrices.error")
                    } else {
                        log.info("Setting price on ${priceCategory.name} / ${customerCategory.name} ${price}")
                        Price.create(price, priceCategory, customerCategory, account)
                    }
                }
            }
        }

        flash.message = message(code: "facilityPriceListCondition.savePrices.success")
        redirect(action: "index", id: params.id)
    }

    def save() {
        def priceList = PriceList.get(params.id)
        if (priceList) {
            assertFacilityAccessTo(priceList)
        }

        def priceListConditionName = params.name
        def category = PriceListConditionCategory.get(params.categoryId)

        if(!category) {
            category = new PriceListConditionCategory()
        }

        def conditions = getSessionConditions()

        PriceListConditionCategory.withTransaction {
            if (conditions && !conditions.isEmpty()) {
                if (conditions.removedIds) {
                    category.conditions.findAll {
                        conditions.removedIds.contains(it.id)
                    }.each {
                        category.removeFromConditions(it)
                        it.prepareForDelete()
                        it.delete()
                    }
                }

                conditions.added.each { BookingPriceCondition condition ->
                    condition.prepareForSave()
                    category.addToConditions(condition)
                }
            }

            category.name = priceListConditionName
            category.pricelist = priceList
            category.save()

            priceList.addToPriceListConditionCategories(category)
        }

        log.info("Saving pricelist")
        session.removeAttribute(PRICE_CONDITIONS_KEY)

        redirect(action: "index", id: params.id)
    }

    def delete() {
        def category = PriceListConditionCategory.get(params.categoryId)
        def name = category.name
        if(category) {
            assertFacilityAccessTo(category.pricelist)

            if(category.isDefaultCategory()) {
                flash.message = message(code: "facilityPriceListCondition.delete.defaultCategory")
                redirect(action: "index", id: params.id)
            } else {
                priceListService.deletePriceListConditionCategory(category)

                flash.message = message(code: "facilityPriceListCondition.delete.success", args: [name])
            }

        }
        redirect(action: "index", id: params.id)

    }

    def add() {
        def priceList = PriceList.get(params.id)
        def addedConditions = getSessionConditions().added

        if(!priceList) {
            log.error("No pricelist id")
            flash.message = message(code: "facilityPriceListCondition.add.error")
            //redirect(controller: "facilityPriceList")
        } else {
            def condition = createCondition()

            if(condition.hasErrors() || !condition.validate() || isAlreadyAdded(condition)) {
                def model = form()
                model.conditionBean = condition
                render(view: "form", model:model)
            } else {
                addedConditions << condition
                redirect(action: "form", id: priceList.id, params:[categoryId:params.categoryId,
                        hiddenCategoryName:params.hiddenCategoryName])
            }
        }
    }

    private void clearCategoryConditions(PriceListConditionCategory category) {
        def conditionsToBeRemoved = []

        category.conditions.each {
            conditionsToBeRemoved << it
        }

        conditionsToBeRemoved.each { BookingPriceCondition condition ->
            condition.prepareForDelete()
        }
    }

    private void clearUnsavedConditions() {
        session.removeAttribute(PRICE_CONDITIONS_KEY)
    }

    def remove() {
        if(params.conditionHashCode) {
            def tobeRemoved = getSessionConditions().added.find { (it.hashCode() == params.int("conditionHashCode")) }
            if(tobeRemoved)
                getSessionConditions()?.added?.removeAll(tobeRemoved)
        } else if(params.conditionId) {
            def condition = BookingPriceCondition.get(params.conditionId)
            getSessionConditions()?.removedIds << condition?.id
        }

        redirect(action: "form", id: params.id, params: [categoryId: params.categoryId, hiddenCategoryName: params.hiddenCategoryName])
    }

    private def getSessionConditions() {
        def conditions = session.getAttribute(PRICE_CONDITIONS_KEY)
        if (conditions == null) {
            conditions = [:]
            conditions.added = []
            conditions.removedIds = []
            session.putValue(PRICE_CONDITIONS_KEY, conditions)
        }
        return conditions
    }


    private def createCondition() {
        def type = params.type
        def condition

        if("DATE".equals(type)) {
            condition = parseDateCondition()
        } else if("TIME".equals(type)) {
            condition = parseTimeCondition()
        } else if("WEEKDAYS".equals(type)) {
            condition = parseWeekDaysCondition()
        } else if("COURT".equals(type)) {
            condition = parseCourtCondition()
        }  else if("TIMEBEFORE".equals(type)) {
            condition = parseTimeBeforeCondition()
        } else {
            throw new IllegalArgumentException("Could not find type: ${type}")
        }

        return condition
    }

    private def parseCourtCondition() {
        def condition = new CourtPriceCondition()
        def courtsIds = params.list("courtIds")

        if(courtsIds) {
            courtsIds.each {
                def court = Court.get(it)
                if(court) {
                    condition.courtsToBeSaved << court
                }
            }

        }

        if(condition.courtsToBeSaved.isEmpty()) {
            condition.errors.rejectValue("courts", "Inga banor valda")
        }

        return condition
    }

    private def parseWeekDaysCondition() {
        def condition = new WeekDayPriceCondition()

        log.info(params.weekDays)
        log.info(params.list("weekDays"))
        condition.setWeekDays(params.list("weekDays"))

        return condition
    }

    private def parseTimeCondition() {
        def condition = new TimePriceCondition()

        try {
            log.info(params.timeConditionFrom)
            log.info(params.timeConditionTo)
            def from = params.timeConditionFrom.split(':')
            def to   = params.timeConditionTo.split(':')

            log.info("form: " + from[0])
            condition.fromHour = Integer.parseInt(from[0])
            condition.fromMinute = Integer.parseInt(from[1])
            condition.toHour = Integer.parseInt(to[0])
            condition.toMinute = Integer.parseInt(to[1])


        } catch(Exception e) {
            log.info("error parsing", e)
            condition.errors.rejectValue("fromHour", "Ogiltliga värden")
        }

        return condition
    }

    private def parseTimeBeforeCondition() {
        def condition = new TimeBeforeBookingCondition()

        try {
            condition.hours = params.int("hours")
            condition.minutes = params.int("minutes")
        } catch(Exception e) {
            log.info("Error during parsing of hours/minutes for TimeBeforeBookingCondition: ${e.message} ", e)
            condition.errors.rejectValue("hours", g.message(code: 'timeBeforeBooking.condition.wrongValues').toString())
            condition.errors.rejectValue("minutes", g.message(code: 'timeBeforeBooking.condition.wrongValues').toString())
        }

        return condition
    }

    private def parseDateCondition() {
        def condition = new DatePriceCondition()

        try {
            condition.startDate = DateTimeFormat.forPattern("yyyy-MM-dd").parseDateTime(params.startDate)?.toDate()
            condition.endDate = DateTimeFormat.forPattern("yyyy-MM-dd").parseDateTime(params.endDate)?.toDate()
        } catch(IllegalArgumentException e) {
            log.debug("Unable to parse dates")
        }


        return condition
    }

    private def getPriceFor(PriceListCustomerCategory customerCategory, PriceListConditionCategory priceCategory) {
        return params.long("price_${customerCategory.id}_${priceCategory.id}")
    }

    private def getAccountFor(PriceListCustomerCategory customerCategory, PriceListConditionCategory priceCategory) {
        return params.long("account_${customerCategory.id}_${priceCategory.id}")
    }

    private boolean isAlreadyAdded(condition) {
        if (getSessionConditions().added.find {it.class == condition.class}
                || (params.categoryId && PriceListConditionCategory.get(params.categoryId).conditions
                            .find {it.instanceOf(condition.class) && !getSessionConditions().removedIds.contains(it.id)})) {
            condition.errors.reject("facilityPriceListCondition.add.error.duplicateCondition", null, null)
            return true
        } else {
            return false
        }
    }
}
