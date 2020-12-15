package com.matchi

import com.matchi.facility.FacilityFilterCommand
import com.matchi.facility.Organization
import com.matchi.fortnox.v3.FortnoxArticle
import com.matchi.subscriptionredeem.SubscriptionRedeem
import com.matchi.subscriptionredeem.redeemstrategy.CouponRedeemStrategy
import com.matchi.subscriptionredeem.redeemstrategy.GiftCardRedeemStrategy
import com.matchi.subscriptionredeem.redeemstrategy.InvoiceRowRedeemStrategy
import grails.plugin.cache.CacheEvict
import grails.plugin.cache.Cacheable
import grails.transaction.Transactional
import org.joda.time.DateTimeFieldType
import org.joda.time.LocalTime

class FacilityService {

    static transactional = false
    def springSecurityService
    def userService
    def groovySql
    def invoiceService
    def integrationService
    def grailsApplication

    Facility getGlobalFacility() {
        Facility facility = Facility.get(grailsApplication?.config?.matchi?.defaultFacilityId as Long)
        if (facility) {
            return facility
        }
        else {
            throw new Exception("Could not find global facility")
        }
    }

    Facility getFacility(def facilityId) {
        return Facility.get(facilityId)
    }

    List<Facility> getAllHierarchicalFacilities(Facility facility, Boolean includeSelf = true) {
        if (facility) {
            if (includeSelf) {
                [facility] + facility.masterFacilities
            } else {
                facility.masterFacilities
            }
        } else {
            return []
        }
    }

    def getFacilityByApiKey(def key) {
        Facility.findByApikey(key)
    }

    def getActiveFacilityContract(def facility) {
        return FacilityContract.findByFacilityAndActive(facility, true)
    }

    def findActiveNotInList(List list) {
        return Facility.executeQuery("""
            select new map(f.id as id, f.shortname as shortname, f.name as name, f.address as address, f.zipcode as zipcode,
                f.city as city, f.lat as lat, f.lng as lng) 
            from Facility f 
            where f.active=true and f.id not in (0${(list.size() > 0) ? ' ,' + list.join(', ') : ''})
        """)
    }

    def findActiveFacilities(FacilityFilterCommand cmd) {
        def currentUser = userService.getLoggedInUser()
        return findActiveFacilitiesCached(cmd, currentUser)
    }

    def findActiveFacilitiesCached(FacilityFilterCommand cmd, User currentUser) {
        log.debug "[findActiveFacilitiesCached] cached method invoked command: ${cmd} for current user: ${currentUser}"
        def filter = "%" + ((cmd?.q)?:'') + "%"
        def offset = (cmd?.offset)?:0
        def max    = (cmd?.max)?:10

        def queryParameters = [
            filter:filter,
            offset: offset,
            max: max
        ]

        def joinString  = ""
        def whereString = ""
        def orderString = ""
        def distanceSelect = ""

        if (currentUser) {
            joinString += "LEFT JOIN user_favorite uf on f.id = uf.facility_id and uf.user_id = :userId"
            queryParameters.put("userId", currentUser.id)

            orderString += "uf.facility_id desc, "
        }

        if (cmd.lat && cmd.lng) {
            distanceSelect = ", 3956 * 2 * asin(sqrt(power(sin((${cmd.lat} - abs(f.lat)) * pi() / 180 / 2), 2) + cos(${cmd.lat} * pi() / 180) * cos(abs(f.lat) * pi() / 180) * power(sin((${cmd.lng} - f.lng) * pi() / 180 / 2), 2))) as distance"
            orderString += "distance asc"
        } else {
            orderString += "f.name asc"
        }

        if (cmd?.municipality) {
            whereString += " and m.id = :municipality "
            queryParameters.put("municipality", cmd.municipality)
        }

        if (cmd?.sport && cmd?.sport != 6) {
            whereString += "and sp.sport_id = :sport"
            queryParameters.put("sport", cmd.sport)
        } else if(cmd?.sport && Sport.realNonCoreSports.list().size() > 0) {
            whereString += "and sp.sport_id in (${Sport.realNonCoreSports.list()*.id.join(",")})"
        }

        if (cmd.outdoors != null) {
            joinString += " JOIN court crt on f.id = crt.facility_id and crt.indoor = :indoor"
            queryParameters.put("indoor", !cmd.outdoors)
        }

        if (cmd.hasCamera) {
            joinString += " JOIN facility_property fp on f.id = fp.facility_id and fp.key_name = :playFeatureName"
            queryParameters.put("playFeatureName", FacilityProperty.FacilityPropertyKey.FEATURE_MATCHI_PLAY.name())
        }

        def query = """
            SELECT
                distinct(f.id), f.shortname, f.name, f.bookable, f.address, f.zipcode, f.lat, f.lng, f.city, mf.id as facilityLogotypeId $distanceSelect
            FROM facility f
            LEFT JOIN municipality m on f.municipality_id = m.id
            LEFT JOIN region r on m.region_id = r.id
            LEFT JOIN facility_sport sp on sp.facility_sports_id = f.id
            LEFT JOIN mfile mf on f.facility_logotype_image_id = mf.id
            $joinString
            WHERE
                f.active is true
                and (
                    f.name like :filter
                    or f.city like :filter
                    or f.address like :filter
                    or f.telephone like :filter
                    or f.zipcode like :filter
                    or f.email like :filter
                    or f.description like :filter
                    or m.name like :filter
                    or r.name like :filter
                )
                $whereString
            order by $orderString
                limit :max offset :offset
            """

        def queryCount = """
            SELECT
                count(distinct f.id) as count
            FROM facility f
            LEFT JOIN municipality m on f.municipality_id = m.id
            LEFT JOIN region r on m.region_id = r.id
            LEFT JOIN facility_sport sp on sp.facility_sports_id = f.id
            $joinString
            WHERE
                f.active is true
                and (
                    f.name like :filter
                    or f.city like :filter
                    or f.address like :filter
                    or f.telephone like :filter
                    or f.zipcode like :filter
                    or f.email like :filter
                    or f.description like :filter
                    or m.name like :filter
                    or r.name like :filter
                )
                $whereString

            """
        def rows  = groovySql.rows(query, queryParameters)
        def count = groovySql.rows(queryCount, queryParameters).get(0).count
        groovySql.close()
        return [ rows:rows, count:count ]
    }

    def getFacilityMapInfo(FacilityFilterCommand cmd) {
        def facilities = findActiveFacilities(cmd)
        def facilitiesMapInfos = []

        facilities.each { Facility f ->
            facilitiesMapInfos << [ id: f.id, name: f.name, shortname: f.shortname, address: f.address, zipcode: f.zipcode, lat: f.lat, lng: f.lng, city: f.city?:"" ]
        }

        return facilitiesMapInfos
    }

    def getActiveFacility() {
        def activeUser = userService.getLoggedInUser()

        if(activeUser.facility) {
            return activeUser.facility
        }

        return null
    }

    def getEarliestOpeningHour(def facilities) {
        def earliestOpeningHour = null

        facilities.each { facility ->
            facility?.availabilities?.each { Availability av ->
                if(av.active) {
                    def hour = av.begin.getHourOfDay()

                    if(earliestOpeningHour == null) {
                        earliestOpeningHour = hour
                    }
                    else if(hour < earliestOpeningHour ) {
                        earliestOpeningHour = hour
                    }
                }
            }
        }

        if(earliestOpeningHour == null)  { earliestOpeningHour = 5 }

        return earliestOpeningHour
    }

    def getLatestClosingHour(def facilities) {
        def latestClosingHour = null

        facilities.each { facility ->
            facility?.availabilities?.each { Availability av ->
                if(av.active) {
                    def hour = av.end.getHourOfDay()

                    if(latestClosingHour == null) {
                        latestClosingHour = hour
                    }
                    else if(hour > latestClosingHour ) {
                        latestClosingHour = hour
                    }
                }
            }
        }

        if(latestClosingHour == null) { latestClosingHour = 23 }

        return latestClosingHour
    }

    def generateApiKey(def stringToHash) {
        return stringToHash.encodeAsMD5()
    }

    def getFacilityFromPublicRegistrationCode(String code) {
        def facilities = Facility.findAllByActive(true, [cache: true])
        def facility = null

        facilities.each {
            if(code.equals(it.getRegistrationCode())) {
                facility = it
            } else {
                log.debug "${code} does not equals ${it.getRegistrationCode()}"
            }
        }

        return facility
    }
    def getUsersAvailableFacilities(User user) {
        return getUsersAvailableFacilities(user, null)
    }

    def getUsersAvailableFacilities(User user, def facilities) {
        def userAvailableFacilities = []

        if(!facilities) {
            facilities = Facility.findAllByBookable(true, [cache: true])
        }

        facilities.each { Facility facility ->
            def userCourts = facility.courts?.findAll { !it.membersOnly || (it.membersOnly && user.hasMembershipIn(it.facility)) }
            if(userCourts.size() > 0) {
                userAvailableFacilities << facility
            }
        }

        return userAvailableFacilities
    }

    @Transactional
    @CacheEvict(value=["facilities_list", "facilities_listActive", "facilities_findActiveFacilities"], allEntries=true)
    Facility saveFacility(Facility facility) {
        facility.save()
    }

    @Transactional
    def updateAvailability(Facility facility, def params) {
        Availability availability = new Availability()
        def addToUser = false

        (1..7).each { int day ->
            availability = facility.availabilities?.find { it.weekday == day }

            if (!availability) {
                addToUser = true
                availability = new Availability()
                availability.weekday = day
            }

            availability.begin = getAvailabilityTime(params["fromMinute_${day}"])
            availability.end   = getAvailabilityTime(params["toMinute_${day}"])
            availability.active = params["active_${day}"]

            if (addToUser) {
                facility.addToAvailabilities(availability)
            }

            availability.save()
        }
    }

    @Transactional
    def updateSubscriptionRedeem(Facility facility, def params) {
        SubscriptionRedeem subscriptionRedeem = facility.subscriptionRedeem ?: new SubscriptionRedeem()
        def strategy = subscriptionRedeem?.strategy

        if (!params.subscriptionRedeem && facility.subscriptionRedeem) {
            subscriptionRedeem.clear()
            facility.save()
            subscriptionRedeem.delete()
        } else if (params.subscriptionRedeem) {
            if (strategy) {
                subscriptionRedeem.clearStrategy(strategy)
            }

            def redeemStrategy = getAvailableRedeemStrategies(facility).find { it.type.equals(params.strategy) }

            if (!redeemStrategy) {
                throw new IllegalArgumentException("No strategy chosen")
            }

            redeemStrategy.populate(params)

            if (!redeemStrategy.hasErrors() && redeemStrategy.save()) {
                subscriptionRedeem.strategy = redeemStrategy
                subscriptionRedeem.redeemAt = params.redeemAt
                subscriptionRedeem.facility = facility
                subscriptionRedeem.save()
                facility.subscriptionRedeem = subscriptionRedeem
                facility.save()
            } else {
                throw new IllegalArgumentException("Error in strategy")
            }
        }
    }

    public def getAvailableRedeemStrategies(Facility facility) {
        def result = []

        if (facility.hasApplicationInvoice()) {
            result << new InvoiceRowRedeemStrategy()
        }
        result << new CouponRedeemStrategy()
        if (FacilityProperty.findByFacilityAndKey(facility,
                FacilityProperty.FacilityPropertyKey.FEATURE_GIFT_CARDS.name())?.value) {
            result << new GiftCardRedeemStrategy()
        }

        return result
    }


    List<Organization> getFacilityOrganizations(Facility facility) {
        return Organization.findAllByFacility(facility)
    }

    Map<String, List<FortnoxArticle>> collectArticles (Facility facility, InvoiceRowRedeemStrategy strategy) {
        def articles = [:]
        def defaultArticles = invoiceService.getItems(facility)
        articles.put("bookingInvoiceRowArticles", getArticles(facility.bookingInvoiceRowOrganizationId, defaultArticles))
        articles.put("invoiceFeeArticles", defaultArticles)
        articles.put("invoiceFeeExternalArticles", getArticles(facility.invoiceFeeOrganizationId, defaultArticles))
        articles.put("redeemInvoiceRowArticles", getArticles(strategy?.organizationId, defaultArticles))
        return articles
    }

    private List<FortnoxArticle> getArticles(Long organizationId, List defaultArticles) {
        return organizationId ? invoiceService.getItemsForOrganization(organizationId) : defaultArticles
    }

    private LocalTime getAvailabilityTime(String minutesOfDay) {
        if (!minutesOfDay) {
            return new LocalTime("0")
        } else if (minutesOfDay == "1440") {
            return new LocalTime("23:59:59")
        } else {
            return new LocalTime("0").withField(DateTimeFieldType.minuteOfDay(), minutesOfDay as int)
        }
    }

    /**
     * Called from scheduled job with the purpose of synchronizing new and updated facilities
     * with configuration to the Asynchronous Integration Platform (AIP).
     * @return the number of sent facilities.
     * @author Magnus Lundahl
     */
    def syncFacilityConfigToIntegrationPlatform() {
        def facilities = Facility.findAll([cache: true])
        def count = 0
        facilities.each {
            integrationService.send(it)
            count++
        }
        return count
    }

    @Cacheable(value='facilities_list')
    List<Facility> list() {
        Facility.list()
    }


    @Cacheable(value='facilities_listActive')
    List<Facility> listActive() {
        List<Facility> activeFacilities = []
        grailsApplication.mainContext.facilityService.list().each {
            if (it.active) {
                activeFacilities.add(it)
            }
        }
        return activeFacilities
    }
}
