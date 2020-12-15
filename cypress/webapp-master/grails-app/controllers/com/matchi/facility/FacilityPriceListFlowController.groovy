package com.matchi.facility

import com.matchi.GenericController
import com.matchi.PriceList

class FacilityPriceListFlowController extends GenericController {

    static scope = "prototype"

    def priceListService

    def copyFlow = {
        entry {
            action {
                log.info("Flow action entry")

                flow.returnUrl = params.returnUrl
                log.debug("Return url: ${flow.returnUrl}")

                def facility = getCurrentUser().facility
                def priceLists
                if (params.allselected) {
                    priceLists = priceListService.getByFacility(facility)
                } else {
                    priceLists = PriceList.findAllByFacilityAndIdInList(facility,
                            params.list("priceListId").collect { id -> Long.parseLong(id) })
                }

                flow.cmd = [items: priceLists.collect { pl ->
                    [priceListId: pl.id, priceListName: pl.name]
                }]
            }
            on("success").to "confirm"
        }
        confirm {
            log.info("Flow view confirm")
            on("submit") { FacilityPriceListCopyCommand cmd ->
                // TODO: workaround; validatable command object can't be properly passed to a web flow view (Grails bug?)
                flow.cmd = [items: cmd.items.collect { item ->
                    [priceListId: item.priceListId, priceListName: item.priceListName, name: item.name,
                     startDate  : item.startDate ? Date.parse(message(code: "date.format.dateOnly"), item.startDate) : null]
                }]

                if (!cmd.validate()) {
                    flash.error = message(code: "facilityPriceList.copy.validationError")
                    return error()
                }
            }.to "copyPriceList"
            on("cancel").to "finish"
        }
        copyPriceList {
            action {
                log.info("Flow action copyPriceList")

                flow.cmd.items.each { item ->
                    def srcPriceList = PriceList.get(item.priceListId)
                    priceListService.copyAndSave(srcPriceList,
                            new PriceList(name: item.name, startDate: item.startDate))
                }

                flow.returnUrl += addParam(flow.returnUrl, "message",
                        message(code: "facilityPriceList.copy.success", args: [flow.cmd.items.size()]))
            }
            on("success").to "finish"
            on("error").to "confirm"
            on(Exception).to "finish"
        }
        finish {
            redirect(url: flow.returnUrl)
        }
    }
}