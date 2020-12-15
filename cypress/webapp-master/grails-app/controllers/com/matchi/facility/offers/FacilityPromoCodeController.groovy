package com.matchi.facility.offers

import com.matchi.Facility
import com.matchi.GenericController
import com.matchi.User
import com.matchi.coupon.Offer
import com.matchi.coupon.PromoCode
import grails.converters.JSON
import grails.validation.Validateable
import org.springframework.http.HttpStatus

import java.text.DecimalFormat

class FacilityPromoCodeController extends GenericController {
    def couponService

    def amountStep(Facility facility) {
        Integer decimalPlaces = grailsApplication.config.matchi.settings.currency[facility.currency].decimalPoints
        return 1 / Math.pow(10, decimalPlaces)
    }

    def index() {
        def facility = getUserFacility()
        def coupons = (params.sort) ? couponService.getActivePromoCodes(facility, params.sort, params.order)
                : couponService.getActivePromoCodes(facility)

        [ facility:facility, coupons:coupons ]
    }

    def archive() {
        def facility = getUserFacility()
        def coupons = (params.sort) ? couponService.getExpiredPromoCodes(facility, params.sort, params.order) :
                couponService.getExpiredPromoCodes(facility)

        [facility: facility, coupons: coupons]
    }

    def add() {
        Facility facility = getUserFacility()
        [ facility: facility, amountStep: amountStep(facility) ]
    }

    def edit() {
        def facility = getUserFacility()
        def offer = PromoCode.get(params.id)

        if (offer) {
            assertFacilityAccessTo(offer)
        }

        [ facility: facility, coupon: offer, amountStep: amountStep(facility)]
    }

    def save(CreatePromoCodeCommand cmd) {
        def facility = getUserFacility()

        if (cmd.hasErrors()) {
            render(view: 'add', model: [ facility: facility, cmd:cmd] )
            return
        }


        Offer offer = new PromoCode()

        offer.name = cmd.name
        offer.code = cmd.code
        offer.startDate = cmd.startDate
        offer.endDate = cmd.endDate
        if (cmd.discountType == "discountPercent") {
            offer.discountPercent = new BigDecimal(cmd.discountPercent)
            offer.discountAmount = null
        } else {
            offer.discountPercent = null
            offer.discountAmount = new BigDecimal(cmd.discountAmount)
        }

        offer.facility = facility
        offer.nrOfTickets = 0
        PromoCode existingCode = couponService.getValidPromoCode(facility, cmd.code)

        if (existingCode) {
            flash.error = message(code: "facilityPromoCode.codeAlreadyExist")
        } else if (offer.save()) {
            flash.message = message(code: "promoCode.save.success", args: [offer.name])
            redirect(action: "index")
            return
        }

        render(view: 'add', model: [ facility: facility, cmd:cmd, amountStep: amountStep(facility)] )
    }

    def update(CreatePromoCodeCommand cmd) {
        def facility = getUserFacility()

        if (cmd.hasErrors()) {
            render(view: 'edit', model: [ facility: facility, cmd:cmd] )
            return
        }

        Offer offer = PromoCode.get(cmd.couponId)
        PromoCode existingCode = couponService.getValidPromoCode(facility, cmd.code)
        if (existingCode && existingCode.id != offer.id) {
            flash.error = message(code: "facilityPromoCode.codeAlreadyExist")
            render(view: 'edit', params:[ cmd: cmd, amountStep: amountStep(facility) ] )
        }

        if(offer) {
            offer.name = cmd.name
            offer.code = cmd.code
            offer.startDate = cmd.startDate
            offer.endDate = cmd.endDate
            if (cmd.discountType == "discountPercent") {
                offer.discountPercent = new BigDecimal(cmd.discountPercent)
                offer.discountAmount = null
            } else {
                offer.discountPercent = null
                offer.discountAmount = new BigDecimal(cmd.discountAmount)
            }
        }

        if (!offer.hasErrors() && offer.save(failOnError: true)) {
            flash.message = message(code: "promoCode.update.success", args: [offer.name])
            redirect(action: "edit", id: offer.id, mapping: params.type)
            return
        }

        render(view: 'edit', params:[ cmd: cmd, amountStep: amountStep(facility) ] )
    }

    def delete(String type) {
        Offer offer = PromoCode.get(params.couponId)

        if (offer) {
            assertFacilityAccessTo(offer)
            offer.delete()
            flash.message = message(code: "promoCode.delete.success", args: [offer.name])
        }

        if(params.returnUrl && params.returnUrl.size() > 0) {
            redirect url: params.returnUrl
        } else {
            redirect(action: "index")
        }
    }

}

@Validateable(nullable = true)
class CreatePromoCodeCommand {
    Long couponId
    String name
    String code
    Date startDate
    Date endDate
    String discountType
    String discountPercent
    String discountAmount


    static constraints = {
        name(nullable: false, blank: false)
        code(nullable: false, blank: false)
        startDate(nullable: false, blank:false)
        endDate(nullable: false, blank: false)
        discountPercent(nullable: true, validator: { val, obj -> val || obj.discountType != "discountPercent"})
        discountAmount(nullable: true, validator: { val, obj -> val || obj.discountType != "discountAmount"})
    }
}
