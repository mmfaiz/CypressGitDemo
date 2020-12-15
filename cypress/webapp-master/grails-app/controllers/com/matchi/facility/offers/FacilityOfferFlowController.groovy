package com.matchi.facility.offers

import com.matchi.Customer
import com.matchi.GenericController
import com.matchi.coupon.CustomerCoupon
import com.matchi.coupon.Offer
import com.matchi.facility.FacilityOfferCopyCommand
import com.matchi.facility.FilterCustomerCommand
import org.joda.time.LocalDate

class FacilityOfferFlowController extends GenericController {

    static scope = "prototype"

    def couponService
    def customerService

    def bulkAddFlow = {
        entry {
            action { FilterCustomerCommand cmd ->
                log.info("Flow action entry")

                flow.returnUrl = params.returnUrl
                log.debug("Return url: ${flow.returnUrl}")

                if (!params.allselected && !params.list("customerId")) {
                    flow.returnUrl += addParam(flow.returnUrl, "error",
                            message(code: "facilityOffer.bulkAdd.noCustomersSelected"))
                    return error()
                }

                def facility = getCurrentUser().facility

                def customers = []
                if (params.allselected) {
                    customers = customerService.findCustomers(cmd, facility)
                } else {
                    params.list("customerId").each { customers << Customer.get(it) }
                }

                flow.customerIds = []
                flow.customerInfo = []
                customers.sort { it.number }.each {
                    flow.customerIds << it.id
                    flow.customerInfo << [number: it.number, name: it.fullName()]
                }

                flow.coupons = [:]
                Offer.facilityCoupons(facility).list().each {
                    if (!flow.coupons[it.class]) {
                        flow.coupons[it.class] = []
                    }
                    flow.coupons[it.class] << [id: it.id, name: it.name, nrOfTickets: it.nrOfTickets,
                            expireDate: it.getExpireDate(), unlimited: it.unlimited]
                }

                flow.persistenceContext.clear()
            }
            on("success").to "selectOffer"
            on("error").to "finish"
        }
        selectOffer {
            log.info("Flow view selectOffer")
            on("submit") {
                // TODO: dates are not bindable for some reason, use manual binding
                flow.cmd = new FacilityOfferAddToCustomerCommand()
                bindData(flow.cmd, params)
                flow.cmd.customerId = flow.customerIds[0]
                if (flow.cmd.validate()) {
                    def offer = Offer.get(flow.cmd.couponId)
                    assertFacilityAccessTo(offer)
                    flow.selectedOffer = [type: offer.class.simpleName, name: offer.name]
                    flow.persistenceContext.clear()
                    return success()
                } else {
                    return error()
                }
            }.to "confirm"
            on("cancel").to "finish"
        }
        confirm {
            log.info("Flow view confirm")
            on("back").to "selectOffer"
            on("cancel").to "finish"
            on("submit").to "addOfferToCustomers"
        }
        addOfferToCustomers {
            action {
                def offer = Offer.get(flow.cmd.couponId)
                flow.customerIds.each { c ->
                    def customer = Customer.get(c)
                    CustomerCoupon.link(springSecurityService.currentUser, customer, offer,
                            flow.cmd.nrOfTickets as int, new LocalDate(flow.cmd.expireDate), flow.cmd.note as String)
                }
                flow.returnUrl += addParam(flow.returnUrl, "message",
                        message(code: "facilityOffer.bulkAdd.success"))
            }
            on("success").to "finish"
            on(Exception).to "finish"
        }
        finish {
            redirect(url: flow.returnUrl)
        }
    }

    def copyFlow = {
        entry {
            action {
                log.info("Flow action entry")

                flow.returnUrl = params.returnUrl
                log.debug("Return url: ${flow.returnUrl}")

                def facility = getCurrentUser().facility

                def offers = Offer.findAllByFacilityAndIdInList(facility,
                        params.list("offerId").collect { id -> Long.parseLong(id) })

                if (!offers) {
                    flow.returnUrl += addParam(flow.returnUrl, "error",
                            message(code: "facilityOffer.copy.noSelection"))
                    return error()
                }

                flow.cmd = [items: offers.collect { o ->
                    [offerId: o.id, offerName: o.name]
                }]
            }
            on("success").to "enterDetails"
            on("error").to "finish"
        }
        enterDetails {
            log.info("Flow view enterDetails")
            on("cancel").to "finish"
            on("submit") { FacilityOfferCopyCommand cmd ->
                // TODO: workaround; validatable command object can't be properly passed to a web flow view (Grails bug?)
                flow.cmd = [items: cmd.items.collect { item ->
                    [offerId: item.offerId, offerName: item.offerName, name: item.name]
                }]

                if (!cmd.validate()) {
                    flash.error = message(code: "facilityOffer.copy.validationError")
                    return error()
                }
            }.to "copyOffer"
        }
        copyOffer {
            action {
                log.info("Flow action copy offer")

                flow.cmd.items.each { item ->
                    couponService.copyOffer(Offer.get(item.offerId), item.name)
                }

                flow.returnUrl += addParam(flow.returnUrl, "message",
                        message(code: "facilityOffer.copy.success"))

                success()
            }
            on("success").to "finish"
            on(Exception).to "finish"
        }
        finish {
            redirect(url: flow.returnUrl)
        }
    }
}