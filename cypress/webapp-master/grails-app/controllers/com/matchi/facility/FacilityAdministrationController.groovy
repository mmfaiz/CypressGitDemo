package com.matchi.facility

import com.matchi.*
import com.matchi.enums.MembershipRequestSetting
import org.apache.commons.validator.EmailValidator
import org.apache.http.HttpStatus

import javax.servlet.http.HttpServletResponse

/**
 * Facility administration controller
 *
 */
class FacilityAdministrationController extends GenericController {

    static layout = 'facilityLayout'
    def facilityService
    def userService
    def customerService
    def invoiceService

    def index() {
        def facility = getUserFacility()

        if(facility == null) {
            render(view: "noFacility")
            return
        }

        [ facility: facility, regions:Region.getAll(),
            sports:facility.listSports(), availabilities:facility.availabilities ]
    }

    def settings() {
        def facility = getUserFacility()

        if(facility == null) {
            render(view: "noFacility")
            return
        }
        def redeemStrategies = facilityService.getAvailableRedeemStrategies(facility)
        def articles = facility.hasFortnox() || facility.hasExternalArticles() ?
                facilityService.collectArticles(facility, redeemStrategies.find { it.type.equals("INVOICE_ROW") }) : [:]
        def organizations = facilityService.getFacilityOrganizations(facility)

        [ facility: facility, redeemStrategies: redeemStrategies, articles: articles, organizations: organizations ]
    }

    def save() {
        def facility = getUserFacility()
        if (facility) {

            if(params.email && !new EmailValidator().isValid(params.email)) {
                flash.error = message(code: "facilityAdministration.save.email.invalid")
                redirect(action: "index")
                return
            }


            facility.name             = params.name
            facility.description      = params.description
            facility.email            = params.email
            facility.address          = params.address
            facility.zipcode          = params.zipcode
            facility.city             = params.city
            facility.country          = params.country
            facility.telephone        = params.telephone
            facility.website          = params.website
            facility.fax              = params.fax
            facility.plusgiro         = params.plusgiro
            facility.bankgiro         = params.bankgiro
            facility.orgnr            = params.orgnr
            facility.municipality     = Municipality.get(params.municipality)
            facility.openingHoursType = params.openingHoursType
            facility.setFacilityProperty(FacilityProperty.FacilityPropertyKey.INVOICE_EMAIL, params.invoiceEmail)

            def sports = []
            params.list("sports").each {
                sports << Sport.findById(it)
            }
            def sportsToRemove = facility.sports - sports
            def sportsToadd    = sports - facility.sports
            sportsToRemove?.each { Sport s ->
                facility.removeFromSports(s)
            }
            sportsToadd?.each { Sport s ->
                facility.addToSports(s)
            }

            facilityService.updateAvailability(facility, params)

            if (!facility.hasErrors() && facility.save(flush: true)) {
                flash.message = message(code: "facilityAdministration.save.success")
                redirect(action: "index")
            }
            else {
                log.debug(facility.errors)
                flash.error = message(code: "facilityAdministration.save.error")
                redirect(action: "index")
                //render(view: "index", model: [facility: facility])
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'facility.label', default: 'Facility'), params.id])}"
            redirect(action: "index")
        }

    }

    def saveSettings() {
        def facility = getUserFacility()

        if (facility) {
            bindData(facility, params, [include: ["bookingNotificationNote", "membershipRequestEmail",
                    "membershipRequestDescription", "bookingInvoiceRowDescription",
                    "bookingInvoiceRowExternalArticleId", "bookingInvoiceRowOrganizationId",
                    "membershipValidTimeAmount", "membershipValidTimeUnit", "membershipGraceNrOfDays",
                    "yearlyMembershipStartDate", "yearlyMembershipPurchaseDaysInAdvance"]])

            facility.recieveMembershipRequests                        = params.receiveMembershipRequests ?: false
            facility.whetherToSendEmailConfirmationByDefault          = params.whetherToSendEmailConfirmationByDefault ?: false
            facility.membershipRequestSetting                         = facility.recieveMembershipRequests ? params.membershipRequestSetting : null
            facility.bookingInvoiceRowOrganizationId                  = params.long("bookingInvoiceRowOrganizationId")
            facility.useInvoiceFees                                   = params.useInvoiceFees ?: false
            facility.showBookingHolder                                = params.showBookingHolder ?: false
            facility.isAllCourtsTabDefault                            = params.isAllCourtsTabDefault ?: false
            facility.googleTagManagerContainerId                      = params.googleTagManagerContainerId ?: null
            facility.membershipStartingGraceNrOfDays                  =
                    facility.recieveMembershipRequests && facility.membershipRequestSetting == MembershipRequestSetting.DIRECT ?
                            params.int("membershipStartingGraceNrOfDays") : null

            if (params.defaultBookingCustomerId) {
                facility.defaultBookingCustomer = Customer.findById(params.defaultBookingCustomerId)
            } else {
                facility.defaultBookingCustomer = null
            }

            try {
                facilityService.updateSubscriptionRedeem(facility, params)
            } catch (IllegalArgumentException e) {
                facility.errors.rejectValue("subscriptionRedeem","facility.subscriptionRedeem.strategy.error");
            }

            if (params.useInvoiceFees) {
                facility.invoiceFeeArticles          = params.list("invoiceFeeArticles")
                facility.invoiceFeeExternalArticleId = params.invoiceFeeExternalArticleId
                facility.invoiceFeeDescription       = params.invoiceFeeDescription
                facility.invoiceFeeAmount            = params.long("invoiceFeeAmount")
                facility.invoiceFeeOrganizationId    = params.long("invoiceFeeOrganizationId")
            }

            facility.setFacilityProperty(FacilityProperty.FacilityPropertyKey.FEATURE_USE_FAMILY_MEMBERSHIPS,
                    (facility.recieveMembershipRequests && params.useFamilyMembersFeature) ? "1" : "0")
            if (params.membershipMaxAmount?.isNumber()) {
                facility.setFacilityProperty(FacilityProperty.FacilityPropertyKey.FACILITY_MEMBERSHIP_FAMILY_MAX_AMOUNT,
                        params.membershipMaxAmount)
            } else {
                facility.removeFacilityProperty(FacilityProperty.FacilityPropertyKey.FACILITY_MEMBERSHIP_FAMILY_MAX_AMOUNT)
            }

            if (params.bookingRestrictionsEnabled) {
                facility.setFacilityProperty(FacilityProperty.FacilityPropertyKey.FEATURE_MAXIMUM_NUMBER_OF_BOOKINGS_PER_USER,
                        params.maxBookingsFeature ? "1" : "0")
                if (params.maxBookings?.isInteger()) {
                    facility.setFacilityProperty(FacilityProperty.FacilityPropertyKey.MAXIMUM_NUMBER_OF_BOOKINGS_PER_USER,
                            params.maxBookings)
                }
            }

            // Set properties available for facility
            FacilityProperty.getPropertiesAvailableForFacility().each {
                def value = params.get(it.toString())

                if(value && value.length() > 0) {
                    facility.setFacilityProperty(it, value)
                    log.info it.toString() + ": " + params.get(it.toString())
                } else {
                    facility.removeFacilityProperty(it)
                    log.info "Removing property"
                }
            }

            if (!facility.hasErrors() && facility.save()) {
                flash.message = message(code: "facilityAdministration.save.success")
                redirect(action: "settings")
            }
            else {
                render(view: "settings", model: [ facility: facility, redeemStrategies: facilityService.getAvailableRedeemStrategies(facility) ])
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'facility.label', default: 'Facility'), params.id])}"
            redirect(action: "index")
        }
    }

    def switchFacility(Long aFacility) {
        def user = getCurrentUser()
        def facility = user.facilityUsers.find {
            it.facility.id == aFacility
        }?.facility

        if (facility) {
            User.withTransaction {
                user.facility = facility
                user.save()
            }
            redirect(controller: defaultFacilityController())
        } else {
            response.sendError HttpServletResponse.SC_BAD_REQUEST
        }
    }

    /**
     * Ajax
     */
    def listOrganizationArticles() {
        try {
            def organizationId = params.long("organizationId")
            def articles = organizationId ? invoiceService.getItemsForOrganization(organizationId) : invoiceService.getItems(getUserFacility())
            render select(name: params.articleName, from: articles, value: '', optionKey: 'id', optionValue: 'descr',
                    noSelection: ['': message(code: 'default.article.multiselect.noneSelectedText')])
        } catch (Exception ex) {
            flash.error = ex.message
            render HttpStatus.SC_BAD_REQUEST
            return
        }
    }

}
