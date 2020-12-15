package com.matchi.facility

import com.matchi.Facility
import com.matchi.GenericController
import com.matchi.PriceList
import org.joda.time.DateTime
import org.joda.time.LocalDate

class FacilityPriceListController extends GenericController {

    public static final String LAST_LIST_ACTION_KEY = "facility_pricelist_list_action"

    def courtService
    def priceListService

    def index() {
        session[LAST_LIST_ACTION_KEY] = "index"
        def facility = getUserFacility()

        [facility: facility, pricelists: priceListService.getActivePriceLists(facility)]
    }

    def upcoming() {
        session[LAST_LIST_ACTION_KEY] = "upcoming"
        def facility = getUserFacility()

        render(view: "index", model: [facility: facility,
                pricelists: priceListService.getUpcomingPriceLists(facility)])
    }

    def inactive() {
        session[LAST_LIST_ACTION_KEY] = "inactive"
        def facility = getUserFacility()

        render(view: "index", model: [facility: facility,
                pricelists: priceListService.getInactivePriceLists(facility)])
    }

    def create() {
        def priceList = new PriceList(params)
        def facility = getRequiredUserFacility()

        priceList.startDate = new DateTime().toDate()

        priceList.properties = params

        return [ priceList: priceList, facility:  facility ]
    }

    def edit() {
        def priceList = PriceList.get(params.id)

        if (priceList) {
            assertFacilityAccessTo(priceList)
        }

        [ priceList: priceList ]
    }

    def save() {
        Facility facility = getUserFacility()

        PriceList priceList = new PriceList(facility: facility)
        if (priceList) {
            assertFacilityAccessTo(priceList)
        }
        priceList.properties = params

        PriceList existingPriceList = PriceList.findByStartDateAndSportAndFacilityAndSubscriptions(priceList.startDate, priceList.sport, priceList.facility, priceList.subscriptions)

        if(priceList && priceList.validate() && !existingPriceList) {

            // successful, creating pricelist
            priceListService.createPriceList(facility, priceList)
            flash.message = message(code: "facilityPriceList.save.success", args: [facility.name])
            redirect(action: session[LAST_LIST_ACTION_KEY] ?: "index")

        } else {
            priceList.errors.rejectValue("startDate", 'priceList.startDate.exists')
            render(view: "create", model: [priceList: priceList,facility:  facility])
        }
    }

    def update() {
        PriceList priceList = PriceList.read(params.id)
        if (priceList) {
            assertFacilityAccessTo(priceList)
        }

        bindData(priceList, params, [exclude: 'type'])
        PriceList existingPriceList = PriceList.findByStartDateAndSportAndFacilityAndSubscriptions(priceList.startDate, priceList.sport, priceList.facility, priceList.subscriptions)

        if(priceList && priceList.validate() && (!existingPriceList || existingPriceList.id == priceList.id)) {

            priceListService.updatePriceList(priceList)
            flash.message = message(code: "facilityPriceList.update.success", args: [priceList.name])
            redirect(action: session[LAST_LIST_ACTION_KEY] ?: "index")
        } else {
            priceList.errors.rejectValue("startDate", 'priceList.startDate.exists')
            render(view: "edit", model: [priceList: priceList])
        }
    }

    def delete() {

        log.info("Deleteing price list")
        def priceList = PriceList.get(params.long("id"))
        def priceListName
        if(priceList) {
            assertFacilityAccessTo(priceList)
            priceListName = priceList.name
            priceListService.delete(priceList)
        } else {
            flash.error = message(code: "facilityPriceList.delete.error")
            throw new Exception("ERROR")
        }
        flash.message = message(code: "facilityPriceList.delete.success", args: [priceListName])

        redirect(action: session[LAST_LIST_ACTION_KEY] ?: "index")
    }
}

