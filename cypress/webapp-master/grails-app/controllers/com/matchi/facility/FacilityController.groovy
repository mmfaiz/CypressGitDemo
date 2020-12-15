package com.matchi.facility

import com.matchi.*
import com.matchi.activities.trainingplanner.Trainer
import com.matchi.coupon.Offer
import com.matchi.payment.PaymentFlow
import grails.converters.JSON
import grails.validation.Validateable
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsParameterMap
import org.joda.time.LocalDate
import org.springframework.web.servlet.support.RequestContextUtils

class FacilityController extends GenericController {

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def facilityService
    def courtService
    def dateUtil
    def activityService
    def fileArchiveService
    def couponService
    def excelLeagueService
    def memberService

    def beforeInterceptor = [action: this.&checkRedirect, only: ['show']]

    def index(FacilityFilterCommand cmd) {
        def municipalities = Municipality.executeQuery("""
            select new map(
                 municipality.id as id, municipality.name as name, region.name as regionName, count(distinct f.id) as numFacilities)
            from
                Municipality as municipality
            LEFT JOIN municipality.region as region
            JOIN municipality.facilities as f with f.active = true
            GROUP BY municipality.id
        """)

        def regions = municipalities.groupBy { it["regionName"] }.collect { k, v ->
            [name: k, municipalities: v.sort { it.name }]
        }


        def user = getCurrentUser()
        def municipality = null

        if (user && !cmd.municipality) {
            municipality = user.municipality
        } else if (cmd.municipality) {
            municipality = Municipality.findById(cmd.municipality)
        }

        String locale = user?.language
        if (!locale) {
            locale = RequestContextUtils.getLocale(request).language
        }

        [municipality: municipality, regions: regions, sports: Sport.coreSportAndOther.list(), cmd: cmd, locale: locale]
    }

    def findFacilities(FacilityFilterCommand cmd) {
        def user = getCurrentUser()
        def municipality = null

        if (user && !cmd.municipality) {
            municipality = user.municipality
        } else if (cmd.municipality) {
            municipality = Municipality.findById(cmd.municipality)
        }

        def result = facilityService.findActiveFacilitiesCached(cmd, user)
        def facilities = result.rows
        def facilitiesCount = result.count
        def facilityIds = facilities.collect { it.id }
        def restOfFacilities = facilityService.findActiveNotInList(facilityIds)

        render template: "facilities", model: [facilities  : facilities, totalCount: facilitiesCount,
                                               municipality: municipality, cmd: cmd, user: user, restOfFacilities: restOfFacilities]
    }

    /**
     * Checks against config if facility shortname should be redirected
     */
    def checkRedirect() {
        Map redirectSettings = grailsApplication.config.matchi.settings.redirect
        if (!redirectSettings) return

        Map facilityControllerShowSettings = redirectSettings.facilityControllerShow
        if (!facilityControllerShowSettings) return

        String toShortName = facilityControllerShowSettings[params.name]

        if (toShortName) {
            // Check so that the facility actually exists by that name
            if (Facility.findByShortname(toShortName)) {
                GrailsParameterMap parameterMap = params
                parameterMap.name = toShortName
                redirect(action: "show", params: parameterMap, permanent: true)
                return
            }
        }
    }

    def show() {
        def user = getCurrentUser()
        def facility = Facility.findByShortname(params.name)
        if (!facility || !facility.enabled || facility.hasBeenArchived()) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'facility.label', default: 'Facility'), params.name])}"
            redirect(action: "index")
        } else {
            def customer = ((user && facility) ? Customer.findByUserAndFacility(user, facility) : null) ?: new Customer(user: user, facility: facility)
            def membership = memberService.getMembership(customer as Customer)
            boolean purchaseUpcomingMembership = membership ? memberService.isUpcomingMembershipAvailableForPurchase(membership) : false

            def activities = activityService.getActiveActivitiesWithOccasions(facility)
            def sport = facility.getDefaultSport()?.id ?: 0

            if (params.sport && params.sport instanceof String) {
                try {
                    sport = Long.parseLong(params.sport)
                } catch (NumberFormatException e) {
                }
            }
            def couponsAvailableForPurchase = [:]
            Offer.availableForPurchase(facility).listDistinct().each {
                if (!couponsAvailableForPurchase[it.class]) {
                    couponsAvailableForPurchase[it.class] = []
                }
                couponsAvailableForPurchase[it.class] << it
            }
            def trainers = Trainer.findAllByFacilityAndShowOnline(facility, true)
            def logoUrl = facility.facilityLogotypeImage ? fileArchiveService.getFileURL(facility.facilityLogotypeImage) : null

            PaymentFlow paymentFlow = null
            if (params.long("orderId")) {
                paymentFlow = PaymentFlow.getFinished(session, params.long("orderId"))
            }


            def activeMemberships =             memberService.getActiveMemberships(user, facility)
            def unpaidStartedMemberships =      memberService.getUnpaidStartedMemberships(user, facility) -     activeMemberships
            def remotelyPayableMemberships =   (memberService.getRemotelyPayableMemberships(user, facility) -   activeMemberships) - unpaidStartedMemberships
            def upcomingMemberships =         ((memberService.getUpcomingMemberships(user, facility) -          activeMemberships) - unpaidStartedMemberships) - remotelyPayableMemberships

            [facility                   : facility,
             courtinfo                  : courtService.findFacilitySurfaces(facility),
             user                       : user,
             availabilities             : facility.availabilities,
             activities                 : activities,
             logoUrl                    : logoUrl,
             sport                      : sport,
             date                       : params.date,
             trainers                   : trainers,
             couponsAvailableForPurchase: couponsAvailableForPurchase,
             customer                   : customer,
             membership                 : membership,
             purchaseUpcomingMembership : purchaseUpcomingMembership,
             activeMemberships          : activeMemberships,
             upcomingMemberships        : upcomingMemberships,
             remotelyPayableMemberships : remotelyPayableMemberships,
             unpaidStartedMemberships   : unpaidStartedMemberships,
             courses                    : activityService.getCoursesWithPublishedForm(facility),
             events                     : activityService.getOnlineEvents(facility),
             paymentFlow                : paymentFlow]
        }
    }

    def leagues() {
        def facility = Facility.findByShortname(params.name)
        if (!facility) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'facility.label', default: 'Facility'), params.name])}"
            redirect(action: "index")
        } else {
            def activities = activityService.getActiveActivitiesWithOccasions(facility)
            def couponsAvailableForPurchase = [:]
            Offer.availableForPurchase(facility).listDistinct().each {
                if (!couponsAvailableForPurchase[it.class]) {
                    couponsAvailableForPurchase[it.class] = []
                }
                couponsAvailableForPurchase[it.class] << it
            }
            def trainers = Trainer.findAllByFacilityAndShowOnline(facility, true)
            def logoUrl = facility.facilityLogotypeImage ? fileArchiveService.getFileURL(facility.facilityLogotypeImage) : null
            def user = getCurrentUser()
            Customer customer = user ? Customer.findByUserAndFacility(user, facility) : null

            [facility                   : facility, activities: activities, logoUrl: logoUrl, trainers: trainers,
             couponsAvailableForPurchase: couponsAvailableForPurchase,
             courses                    : activityService.getCoursesWithPublishedForm(facility),
             excelLeague                : excelLeagueService.getExcelLeague(facility, customer)]
        }
    }

    def getMunicipality() {
        if (params.municipality) {
            def municipality = Municipality.findById(Long.parseLong(params.municipality))
            render municipality as JSON
            return
        }

        render null
    }
}

@Validateable(nullable = true)
class FacilityFilterCommand {
    Long municipality
    Long sport
    String q
    double minLat
    double minLng
    double maxLat
    double maxLng
    int max = 10
    int offset = 0
    String lat
    String lng
    Date date
    Boolean onlyFavorites = false
    Boolean withAvailableCoupons = false
    Boolean indoors
    Boolean outdoors
    Boolean hasCamera

    /*
    this is for caching purpose only
     */

    String key(User user) {
        "${user?.email}${date?.format('ddMMyyyy')}${municipality}${sport}${q}${lat}${lng}${max}${offset}${indoors}${outdoors}${onlyFavorites}${withAvailableCoupons}"
    }

    String toString() {
        return "q:${q}, offset:${offset}, max:${max}"
    }
}
