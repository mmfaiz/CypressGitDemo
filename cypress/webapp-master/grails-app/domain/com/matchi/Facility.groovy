package com.matchi

import com.matchi.FacilityProperty.FacilityPropertyKey
import com.matchi.activities.ClassActivity
import com.matchi.activities.EventActivity
import com.matchi.activities.trainingplanner.CourseActivity
import com.matchi.api.v2.QueryFacilitiesCommand
import com.matchi.async.ScheduledTask
import com.matchi.coupon.Coupon
import com.matchi.dynamicforms.Form
import com.matchi.dynamicforms.FormTemplate
import com.matchi.enums.MembershipRequestSetting
import com.matchi.facility.Organization
import com.matchi.membership.MembershipType
import com.matchi.membership.TimeUnit
import com.matchi.orders.Order
import com.matchi.price.PriceListCustomerCategory
import com.matchi.sportprofile.SportProfile
import com.matchi.subscriptionredeem.SubscriptionRedeem
import grails.util.Holders
import groovy.time.TimeCategory
import org.apache.commons.lang3.StringUtils
import org.grails.databinding.BindUsing
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.LocalDate
import org.joda.time.ReadableInstant
import org.springframework.context.i18n.LocaleContextHolder

import java.text.SimpleDateFormat

class Facility implements Serializable {

    private static final long serialVersionUID = 12L

    public static final Integer DEFAULT_STARTING_GRACE_PERIOD = 30

    Municipality municipality

    static belongsTo = [municipality: Municipality, facilityGroupsAsMaster: FacilityGroup]
    static hasMany = [seasons           : Season, courts: Court, users: User, pricelists: PriceList, customers: Customer,
                      facilityProperties: FacilityProperty, priceListCustomerCategories: PriceListCustomerCategory, membershipTypes: MembershipType,
                      payments          : Payment, sports: Sport, coupons: Coupon, sportProfiles: SportProfile,
                      availabilities    : Availability, activities: ClassActivity, contracts: FacilityContract,
                      formTemplates     : FormTemplate, forms: Form, courses: CourseActivity, events: EventActivity,
                      facilityGroupsAsMember: FacilityGroup
    ]

    static mappedBy = [users: "facility", formTemplates : "none", facilityGroupsAsMember: "none", facilityGroupsAsMaster: "masterFacility"]
    static transients = ['giroPaymentRecords']

    SubscriptionRedeem subscriptionRedeem
    SortedSet<Sport> sports

    String name
    String shortname
    String description
    String address
    String zipcode
    String telephone
    String fax
    String city
    String country
    String email
    String website
    String apikey
    Double lat
    Double lng
    int vat
    Customer defaultBookingCustomer
    Customer relatedBookingsCustomer

    int bookingRuleNumDaysBookable

    boolean bookable
    boolean enabled
    boolean active
    boolean boxnet
    boolean invoicing = false // default

    MFile facilityOverviewImage
    MFile facilityWelcomeImage
    MFile facilityLogotypeImage

    String bookingNotificationNote

    SortedSet customers

    // Social services
    String facebook
    String twitter
    String instagram

    Integer membershipValidTimeAmount = 1
    TimeUnit membershipValidTimeUnit = TimeUnit.YEAR
    Integer membershipGraceNrOfDays = 0

    // Google Tag Manager
    String googleTagManagerContainerId

    @BindUsing({ obj, source ->
        if (source["yearlyMembershipStartDate"]) {
            return new LocalDate(new SimpleDateFormat("dd MMMM", LocaleContextHolder.getLocale())
                    .parse(source["yearlyMembershipStartDate"]))
        }
    })
    LocalDate yearlyMembershipStartDate

    Integer yearlyMembershipPurchaseDaysInAdvance

    // Invoice/banking details
    String bankgiro
    String plusgiro
    String orgnr
    String iban
    String bic

    // MLCS
    DateTime mlcsLastHeartbeat
    Integer mlcsGraceMinutesStart
    Integer mlcsGraceMinutesEnd

    // MembershipRequestSettings
    boolean recieveMembershipRequests = false
    MembershipRequestSetting membershipRequestSetting
    Integer membershipStartingGraceNrOfDays             // trial period added in case of DIRECT membershipRequestSetting
    String membershipRequestEmail
    String membershipRequestDescription

    // Settings for booking invoiceRows
    String bookingInvoiceRowExternalArticleId
    String bookingInvoiceRowDescription
    Long bookingInvoiceRowOrganizationId

    // Settings for invoice fee
    boolean useInvoiceFees = false
    String invoiceFeeArticles
    String invoiceFeeExternalArticleId
    String invoiceFeeDescription
    Long invoiceFeeAmount
    Long invoiceFeeOrganizationId

    //Define whether Facility can create dynamic Forms
    boolean isDynamicFormsEngineEnabled = false

    // setting to display who's playing at a certain time/court
    boolean showBookingHolder = true

    boolean isAllCourtsTabDefault = false

    OpeningHoursType openingHoursType = OpeningHoursType.OPENING_HOURS

    boolean whetherToSendEmailConfirmationByDefault = true

    String language = "sv"  // ISO-639-1 code
    String currency = "SEK"

    boolean requireSecurityNumber

    Boolean multisport

    static final List<Integer> POSSIBLE_VATS = [0, 6, 7, 8, 10, 21, 24, 25]

    String archived
    String salesPerson


    static constraints = {
        name nullable: false, blank: false
        shortname nullable: false, blank: false, unique: true
        description nullable: true, maxSize: 1000
        address nullable: true
        zipcode nullable: true
        city nullable: true
        telephone nullable: true
        fax nullable: true
        country nullable: false, blank: false
        currency nullable: false, blank: false
        email nullable: false, blank: false, email: true
        apikey nullable: true
        facilityOverviewImage nullable: true
        facilityWelcomeImage nullable: true
        facilityLogotypeImage nullable: true
        formTemplates nullable: true
        defaultBookingCustomer nullable: true
        relatedBookingsCustomer nullable: true
        subscriptionRedeem nullable: true
        lat nullable: false, blank: false
        lng nullable: false, blank: false
        vat nullable: false, max: 25
        bookingNotificationNote nullable: true, maxSize: 1000
        facebook nullable: true, url: true
        twitter nullable: true, url: true
        instagram nullable: true, url: true
        mlcsLastHeartbeat nullable: true
        mlcsGraceMinutesStart nullable: true
        mlcsGraceMinutesEnd nullable: true
        recieveMembershipRequests nullable: false
        membershipRequestSetting nullable: true
        membershipStartingGraceNrOfDays nullable: true, min: 0
        membershipRequestEmail nullable: true, email: true
        membershipRequestDescription nullable: true, maxSize: 2000
        bookingInvoiceRowExternalArticleId nullable: true
        bookingInvoiceRowDescription nullable: true
        bookingInvoiceRowOrganizationId nullable: true
        useInvoiceFees nullable: false
        invoiceFeeArticles nullable: true
        invoiceFeeExternalArticleId nullable: true
        invoiceFeeDescription nullable: true, maxSize: 1000
        invoiceFeeAmount nullable: true
        invoiceFeeOrganizationId nullable: true
        bankgiro nullable: true
        plusgiro nullable: true
        orgnr nullable: true
        iban nullable: true
        bic nullable: true
        invoicing nullable: false
        website nullable: true, maxSize: 255, url: true
        language blank: false, maxSize: 2
        membershipValidTimeAmount min: 1
        membershipGraceNrOfDays min: 0
        yearlyMembershipStartDate nullable: true
        yearlyMembershipPurchaseDaysInAdvance nullable: true, min: 1, max: 365
        multisport nullable: true, default: false
        googleTagManagerContainerId nullable: true
        archived nullable: true, default: null
        salesPerson nullable: true, default: null, maxSize: 255
        facilityGroupsAsMember nullable: true
        facilityGroupsAsMaster nullable: true
    }

    static mapping = {
        sort name: "asc"
        courts cache: true, sort: "listPosition"
        seasons sort: "startTime", order: "desc"
        membershipTypes sort: "name"
        priceListCustomerCategories sort: "id"
        coupons sort: "id", order: "desc"
        customers sort: "number", order: "asc", joinTable: [name: "customer", key: "facility_id"]
        availabilities batchSize: 7
        formTemplates joinTable: [name: "facility_form_templates", key: "facility_id"]
        facilityGroupsAsMember joinTable: [name: "facility_hierarchy_group_facilities", key: "facility_id"]
        facilityProperties lazy: false, cache: true
        subscriptionRedeem cache: true, fetch: 'select', include: 'all'
        municipality cache: true
        cache true
    }

    String toString() { "$name" }

    def beforeInsert() {
        this.name = StringHelper.extendedTrim(this.name)
        this.shortname = StringHelper.extendedTrim(this.shortname)
    }
    def beforeUpdate() {
        this.name = StringHelper.extendedTrim(this.name)
        this.shortname = StringHelper.extendedTrim(this.shortname)
    }

    def hasBeenArchived() {
        StringUtils.isNotEmpty(archived)
    }
    Boolean isMasterFacility() {
        this.facilityGroupsAsMaster
    }

    Boolean isMemberFacility() {
        this.facilityGroupsAsMember
    }

    List<Facility> getMasterFacilities() {
        if (isMemberFacility()) {
            return this.facilityGroupsAsMember.collect {
                it.masterFacility
            }
        }
        return []
    }

    Collection<Facility> getMemberFacilities() {
        if (isMasterFacility()) {
            return this.facilityGroupsAsMaster.facilities
        }
        return []
    }

    Collection<MembershipType> getHierarchicalIdrottOnlineMembershipTypes() {
        Collection<MembershipType> membershipTypes = []
        (getMasterFacilities())*.membershipTypes*.each {
            if (it.groupedSubFacility == this) {
                membershipTypes.add(it)
            }
        }
        return membershipTypes
    }

    Boolean hasLinkedFacilities() {
        return this.facilityGroupsAsMaster || this.facilityGroupsAsMember
    }

    def getOpeningHour(int weekDay) {
        def opening = getOpeningLocalTime(weekDay)
        if (opening) {
            return opening.getHourOfDay()
        }

        return null
    }

    def getClosingHour(int weekDay) {
        def closing = getClosingLocalTime(weekDay)
        if (closing) {
            return closing.getHourOfDay()
        }

        return null
    }

    def getOpeningLocalTime(int weekDay) {
        def av = this.availabilities?.find { it.weekday == weekDay }
        if (av && av.active) {
            return av.begin
        }

        return null
    }

    def getClosingLocalTime(int weekDay) {
        def av = this.availabilities?.find { it.weekday == weekDay }
        if (av && av.active) {
            return av.end
        }

        return null
    }

    Map getGiroPaymentRecords(File file) {
        if (this.language == 'no') {
            return BankGiroUtil.getBBSRecords(file)
        }
        return BankGiroUtil.getBgMaxRecords(file)
    }

    def getDefaultSport() {
        def defaultSportProperty = getFacilityPropertyValue(FacilityProperty.FacilityPropertyKey.FACILITY_DEFAULT_SPORT.name())
        if (defaultSportProperty != "") {
            return Sport.findById(Long.parseLong(defaultSportProperty))
        }

        def sports = getAllPublicBookableCourts().collect { it.sport }

        Sport defaultSport = null
        def maxOccurence = 0

        sports.each { Sport sport ->
            def nrOfCourts = sports.count(sport)

            if (nrOfCourts > maxOccurence) {
                maxOccurence = nrOfCourts
                defaultSport = sport
            }
        }

        return defaultSport
    }

    String getFortnoxCustomerId() {
        return getFacilityPropertyValue(FacilityProperty.FacilityPropertyKey.FORTNOX3_CUSTOMER_NUMBER.name())
    }

    def getSportsGroupedByIndoor() {
        return getSportsGroupedByIndoor(courts)
    }

    def getSportsGroupedByIndoor(def courts) {
        def sportsGrouped = courts.findAll { !it.archived && sports?.contains(it.sport) }.groupBy({ it.sport }, { it.indoor }).sort()
        def result = []
        sportsGrouped.each { r ->
            r.value.each {
                result << [sport: r.key, indoor: it.key]
            }
        }

        return result
    }

    def getAllPublicBookableCourts() {
        return courts.findAll { !it.offlineOnly && !it.archived }
    }

    /**
     * Returns true if facility only has outdoor courts
     */
    def isOnlyOutDoor() {

        def groupedByIndoor = getSportsGroupedByIndoor(getAllPublicBookableCourts())
        def numBookableIndoor = groupedByIndoor.findAll { it.indoor && !it.offlineOnly }?.size()
        def numBookableOutdoor = groupedByIndoor.findAll { !it.indoor && !it.offlineOnly }?.size()

        return numBookableOutdoor > 0 && numBookableIndoor == 0
    }

    boolean isIndoorOutdoor() {
        def groupedByIndoor = getSportsGroupedByIndoor(getAllPublicBookableCourts())
        def numBookableIndoor = groupedByIndoor.findAll { it.indoor && !it.offlineOnly }?.size()
        def numBookableOutdoor = groupedByIndoor.findAll { !it.indoor && !it.offlineOnly }?.size()
        numBookableOutdoor && numBookableIndoor
    }

    /**
     * Returns true if passed sport only is outdoor
     */
    def isSportOnlyOutDoor(Sport sport) {
        def sportCourts = courts.findAll { it.sport.equals(sport) && !it.offlineOnly && !it.archived }
        def nrOutdoor = sportCourts.findAll { !it.indoor }?.size()

        return nrOutdoor == sportCourts?.size()
    }

    def getRegistrationCode() {
        shortname.encodeAsMD5()
    }

    def getCurrentSeason() {
        def now = new Date()
        def currentSeason = null

        this.seasons.each { Season season ->
            if (season.startTime <= now && season.endTime >= now) {
                currentSeason = season
            }
        }

        return currentSeason
    }

    boolean isMembersOnly() {
        courts.find { it.membersOnly && !it.archived } &&
                !courts.find { !it.membersOnly && !it.offlineOnly && !it.archived }
    }

    def hasApplicationCashRegister() {
        return hasBoxnet()
    }

    def hasApplicationInvoice() {
        return invoicing
    }

    def hasFortnox() {
        return facilityProperties.findAll {
            it.key.equals(FacilityProperty.FacilityPropertyKey.FORTNOX3_ACCESS_TOKEN.name())
        }?.size()
    }

    def hasSMS() {
        return facilityProperties.findAll { it.key.equals(FacilityProperty.FacilityPropertyKey.FEATURE_SMS.name()) }?.size() > 0
    }

    def hasMPC() {
        return facilityProperties.findAll { it.key.equals(FacilityProperty.FacilityPropertyKey.FEATURE_MPC.name()) }?.size() > 0
    }

    List<String> getMpcNotificationMails() {
        String emailAddressesString = this.getFacilityPropertyValue(FacilityProperty.FacilityPropertyKey.MPC_NOTIFY_EMAIL_ADDRESSES.toString())
        List emailAddresses = ValidationUtils.getEmailsFromString(emailAddressesString, NotificationService.EMAIL_STRING_SEPARATOR)

        if (emailAddresses.any()) {
            return emailAddresses
        }

        return [this.email]
    }

    String getMpcNotificationPhoneNumber() {
        return getFacilityPropertyValue(FacilityProperty.FacilityPropertyKey.MPC_NOTIFY_SMS_NUMBER)
    }

    def hasTrainingPlanner() {
        return facilityProperties.findAll {
            it.key.equals(FacilityProperty.FacilityPropertyKey.FEATURE_TRAINING_PLANNER.name())
        }?.size() > 0
    }

    def hasRequirementProfiles() {
        return getFacilityPropertyValueBoolean(FacilityProperty.FacilityPropertyKey.FEATURE_REQUIREMENT_PROFILES.name())
    }

    def hasBookingRestrictions() {
        return getFacilityPropertyValueBoolean(FacilityProperty.FacilityPropertyKey.FEATURE_BOOKING_RESTRICTIONS.name())
    }

    def hasLeagueFromExcel() {
        return getFacilityPropertyValueBoolean(FacilityProperty.FacilityPropertyKey.FEATURE_LEAGUE_FROM_EXCEL.name())
    }

    def hasCameraFeature() {
        return getFacilityPropertyValueBoolean(FacilityProperty.FacilityPropertyKey.FEATURE_MATCHI_PLAY.name())
    }

    def hasBookATrainer() {
        return getFacilityPropertyValueBoolean(FacilityProperty.FacilityPropertyKey.FEATURE_TRAINERS.name()) &&
                getFacilityPropertyValueBoolean(FacilityProperty.FacilityPropertyKey.FEATURE_BOOK_A_TRAINER.name())
    }

    def hasPrivateLesson() {
        return getFacilityPropertyValueBoolean(FacilityProperty.FacilityPropertyKey.FEATURE_PRIVATE_LESSON.name())
    }

    def hasPersonalAccessCodes() {
        return facilityProperties.findAll { it.key.equals(FacilityProperty.FacilityPropertyKey.FEATURE_PERSONAL_ACCESS_CODE.name()) }?.size() > 0
    }

    def hasSubscriptionAccessCode() {
        return facilityProperties.findAll {
            it.key.equals(FacilityProperty.FacilityPropertyKey.FEATURE_SUBSCRIPTION_ACCESS_CODE.name())
        }?.size() > 0
    }

    boolean hasActivityAccessCode() {
        return isFacilityPropertyEnabled(FacilityProperty.FacilityPropertyKey.FEATURE_ACTIVITY_ACCESS_CODE.name())
    }

    boolean hasExternalArticles() {
        getFacilityPropertyValueBoolean(
                FacilityProperty.FacilityPropertyKey.FEATURE_EXTERNAL_ARTICLES.name())
    }

    boolean hasEnabledRemotePayments() {
        return getRemotePaymentArticles()
    }

    List<Order.Article> getRemotePaymentArticles() {
        List<Order.Article> remotePayables = Order.Article.remotePayables
        return remotePayables.findAll { Order.Article article ->
            return hasEnabledRemotePaymentsFor(article)
        }
    }

    boolean hasEnabledRemotePaymentsFor(Order.Article article) {
        if (!article) return false
        return getFacilityProperty("FEATURE_REMOTE_PAYMENT_" + article.name())?.value == "1"
    }

    def hasBoxnet() {
        return boxnet
    }

    def hasOrganization() {
        return facilityProperties.findAll { it.key.equals(FacilityProperty.FacilityPropertyKey.FEATURE_ORGANIZATIONS.name()) }?.size() > 0 &&
                Organization.findByFacility(this)
    }

    def getOrganization() {
        if (hasOrganization()) {
            return Organization.findByFacility(this)
        }
    }

    boolean hasIdrottOnlineMembershipSync() {
        return getFacilityPropertyValueBoolean(FacilityProperty.FacilityPropertyKey.FEATURE_IDROTT_ONLINE.name())
    }

    boolean hasIdrottOnlineActivitySync() {
        return hasIdrottOnlineMembershipSync() &&
                getFacilityPropertyValueBoolean(FacilityProperty.FacilityPropertyKey.FEATURE_IDROTT_ONLINE_ACTIVITIES.name()) &&
                getFacilityPropertyValueBoolean(FacilityProperty.FacilityPropertyKey.FEATURE_TRAINING_PLANNER.name())
    }

    String getIdrottOnlineOrganisationNumber() {
        String organisationNumber = getFacilityPropertyValue(FacilityProperty.FacilityPropertyKey.IDROTT_ONLINE_ORGANISATION_NUMBER.name())
        organisationNumber ?: orgnr
    }

    List<Sport> getIdrottOnlineMembershipSports() {
        String sportId = getFacilityPropertyValue(FacilityProperty.FacilityPropertyKey.IDROTT_ONLINE_MEMBERSHIP_SPORT.name())
        if (sportId) {
            return [Sport.findById(Long.parseLong(sportId))]
        }

        // Default behaviour.
        this.sports?.toList()
    }

    Sport getIdrottOnlineActivitiesSport() {
        String sportId = getFacilityPropertyValue(FacilityProperty.FacilityPropertyKey.IDROTT_ONLINE_ACTIVITIES_SPORT.name())
        if (sportId) {
            return Sport.findById(Long.parseLong(sportId))
        }

        // Default behaviour.
        this.getDefaultSport()
    }

    def getFortnoxAuthentication() {
        def config = Holders.config

        if (hasFortnox()) {
            def fortnoxV3Token = FacilityProperty.findByKeyAndFacility(
                    FacilityProperty.FacilityPropertyKey.FORTNOX3_ACCESS_TOKEN.name(), this)
            if (fortnoxV3Token) {
                def accessToken = config.matchi.fortnox.api.v3.override?.accessToken ?: fortnoxV3Token.value
                //Create fake Authentication for Fortnox V3 for sync process
                [db: accessToken ?: fortnoxV3Token.value]
            }
        }
    }

    def getNextCustomerNumber() {
        def max = Customer.createCriteria().get {
            projections {
                max("number")
            }
            eq("facility", this)
        }

        return max ? max + 1 : 1
    }

    boolean isBookableForUser(ReadableInstant date, User user) {
        return isBookableForLimit(date, getBookingRuleNumDaysBookableForUser(user))
    }

    boolean isBookableForLimit(ReadableInstant date, int limit) {
        if (limit > 0) {
            def bookableDateThreashold = new LocalDate().plusDays(limit + 1).toDateMidnight()

            if (!date.isBefore(bookableDateThreashold)) {
                return false
            }
        }
        return true
    }

    /**
     * Returns the amount to refund customer when cancelling a booking with
     * the given start time.
     *
     * Uses the BOOKING_LATE_REFUND_PERCENTAGE property to lookup percentage in return.
     */
    def getRefundPercentage(DateTime startTime) {

        if (isLateCancellation(startTime)) {
            def lateRefund = getFacilityPropertyValue(FacilityProperty.FacilityPropertyKey.BOOKING_LATE_REFUND_PERCENTAGE.toString())
            try {
                return Integer.parseInt(lateRefund)
            } catch (NumberFormatException nfe) {
                log.error("Facility property ${FacilityProperty.FacilityPropertyKey.BOOKING_LATE_REFUND_PERCENTAGE.toString()}" +
                        " could not be parsed as a number for facility ${name} (id: ${id})")

                return 0
            }
        } else {
            // return all
            return 100
        }

    }

    def isLateCancellation(DateTime startTime) {
        int limitHours = getBookingCancellationLimit()
        return startTime.minusHours(limitHours).isBeforeNow()
    }

    def getInvoiceFeeArticleIds() {
        if (invoiceFeeArticles == null) return []
        return invoiceFeeArticles.substring(1, invoiceFeeArticles.length() - 1).tokenize(",")
    }

    def getFacilityProperty(FacilityProperty.FacilityPropertyKey key) {
        return getFacilityProperty(key.toString())
    }

    def getFacilityProperty(String key) {
        return facilityProperties.find { it.key.equals(key) }
    }

    def getFacilityPropertyValue(String key) {
        def property = getFacilityProperty(key)

        return property ? property.value : FacilityProperty.FacilityPropertyKey.valueOf(key).defaultValue
    }

    def getFacilityPropertyValue(Enum key) {
        return getFacilityPropertyValue(key.toString())
    }

    boolean getFacilityPropertyValueBoolean(String key) {
        getFacilityPropertyValue(key)?.toBoolean()
    }

    int getBookingCancellationLimit() {
        int defaultBookingCancellationLimit = 6
        def propertyValue = getFacilityPropertyValue(FacilityProperty.FacilityPropertyKey.BOOKING_CANCELLATION_LIMIT.toString())
        if (propertyValue) {
            return Integer.parseInt(propertyValue)
        }

        return defaultBookingCancellationLimit
    }

    void removeFacilityProperty(FacilityProperty.FacilityPropertyKey key) {
        def property = getFacilityProperty(key)
        if (property) {
            removeFromFacilityProperties(property)
            property.delete()
        }
    }

    void setFacilityProperty(String key, String value) {
        def property = getFacilityProperty(key)

        if (property) {
            property.refresh()
            // FacilityProperty collection is cached so need to refresh entity from database to avoid StaleObjectException.
            property.value = value
        } else {
            addToFacilityProperties(new FacilityProperty(key: key, value: value))
        }
    }

    void setFacilityProperty(Enum key, String value) {
        setFacilityProperty(key.toString(), value)
    }

    boolean isFacilityPropertyEnabled(FacilityProperty.FacilityPropertyKey key) {
        isFacilityPropertyEnabled(key.name())
    }

    boolean isFacilityPropertyEnabled(String key) {
        def prop = getFacilityPropertyValue(key)
        prop == "1" || prop == "on"
    }

    boolean isFacilityPropertyTrue(FacilityProperty.FacilityPropertyKey key) {
        return isFacilityPropertyTrue(key.toString())
    }

    boolean isFacilityPropertyTrue(String key) {
        return getFacilityProperty(key)?.value == "1"
    }

    def setFacilityProperty(FacilityProperty.FacilityPropertyKey key, String value) {
        setFacilityProperty(key.toString(), value)
    }

    def getActiveContract(Date date) {
        return FacilityContract.activeContract(this, date).list()[0]
    }

    boolean recalculateMultiplePlayersPrice(Sport sport) {
        boolean settingEnabled = isFacilityPropertyEnabled(
                FacilityProperty.FacilityPropertyKey.FEATURE_CALCULATE_MULTIPLE_PLAYERS_PRICE.name())

        if (settingEnabled) {
            return getMultiplePlayersNumber()[sport.id.toString()] as boolean
        }

        return false
    }

    boolean hasBookingLimitPerCustomer() {
        return isFacilityPropertyEnabled(FacilityProperty.FacilityPropertyKey.FEATURE_MAXIMUM_NUMBER_OF_BOOKINGS_PER_USER.name())
    }

    boolean hasBookingLimitPerCourtGroup() {
        return isFacilityPropertyEnabled(FacilityProperty.FacilityPropertyKey.FEATURE_MAXIMUM_NUMBER_OF_BOOKINGS_PER_COURT_GROUP.name())
    }

    Integer getMaxBookingsPerCustomer() {
        if (!hasBookingLimitPerCustomer()) {
            return null
        }

        return getFacilityPropertyValue(FacilityProperty.FacilityPropertyKey.MAXIMUM_NUMBER_OF_BOOKINGS_PER_USER.name()).toInteger()
    }

    // This is just a temporary implementation before we add real support for timezone for each facility.
    // This is needed now in the IdrottOnline integration to make start/end-time for activities work.
    DateTimeZone getTimeZone() {
        DateTimeZone.forID("Europe/Stockholm") // List of IDs: http://joda-time.sourceforge.net/timezones.html
    }

    Integer getMlcsGraceMinutesStart() {
        return getFacilityPropertyValue(FacilityProperty.FacilityPropertyKey.MLCS_GRACE_MINUTES_START).toInteger()
    }

    Integer getMlcsGraceMinutesEnd() {
        return getFacilityPropertyValue(FacilityProperty.FacilityPropertyKey.MLCS_GRACE_MINUTES_END).toInteger()
    }

    String getMpcStatus() {
        return getFacilityPropertyValue(FacilityProperty.FacilityPropertyKey.MPC_STATUS).toString()
    }

    Date getMlcsLastHeartBeat() {
        DateUtil dateUtil = new DateUtil()
        String heartBeat = getFacilityPropertyValue(FacilityProperty.FacilityPropertyKey.MLCS_LAST_HEARTBEAT)

        if (!heartBeat.isEmpty()) return dateUtil.parseDateAndTime(heartBeat) else return null
    }

    Boolean mlcsOffline() {
        DateUtil dateUtil = new DateUtil()
        String heartBeat = getFacilityPropertyValue(FacilityProperty.FacilityPropertyKey.MLCS_LAST_HEARTBEAT)

        if (!heartBeat.isEmpty()) {
            use(TimeCategory) {
                return (new Date() - (dateUtil.parseDateAndTime(heartBeat) + 30.minutes) > 1.minutes)
            }
        } else {
            return null
        }
    }

    Boolean isSwedish() {
        return this.country == LocaleHelper.Country.SWEDEN.iso
    }

    Boolean isNorwegian() {
        return this.country == LocaleHelper.Country.NORWAY.iso
    }

    // Assumes that the facility admin puts in date in their local datetime format that is later stored.
    // NOTE! When changing alla datetimes to UTC in the databases this logic will be deprecated.
    DateTime getDateTimeAsUTC(DateTime dateTime) {
        dateTime.withZone(getTimeZone()).withZone(DateTimeZone.UTC)
    }

    Map getMultiplePlayersNumber() {
        String property = getFacilityPropertyValue(
                FacilityProperty.FacilityPropertyKey.MULTIPLE_PLAYERS_NUMBER)?.trim()
        if (property) {
            try {
                return Eval.me(property)
            } catch (e) {
                log.error "Unable to deserialize MULTIPLE_PLAYERS_NUMBER for facility $id", e
            }
        }
        [:]
    }

    boolean isMembershipRequiresApproval() {
        recieveMembershipRequests && membershipRequestSetting == MembershipRequestSetting.MANUAL
    }

    boolean isMembershipRequestPaymentEnabled() {
        getFacilityProperty(FacilityProperty.FacilityPropertyKey.FEATURE_MEMBERSHIP_REQUEST_PAYMENT)
    }

    boolean isMembershipStartingGracePeriodEnabled() {
        recieveMembershipRequests && membershipStartingGraceNrOfDays &&
                membershipRequestSetting == MembershipRequestSetting.DIRECT
    }

    PersonalNumberSettings getPersonalNumberSettings() {
        return Holders.applicationContext.getBean("dateUtil").getPersonalNumberSettings(country)
    }

    boolean isFamilyMembershipRequestAllowed() {
        recieveMembershipRequests && isMembershipRequestPaymentEnabled() &&
                isFacilityPropertyEnabled(FacilityProperty.FacilityPropertyKey.FEATURE_USE_FAMILY_MEMBERSHIPS)
    }

    Long getFamilyMaxPrice() {
        def maxPrice = getFacilityProperty(
                FacilityProperty.FacilityPropertyKey.FACILITY_MEMBERSHIP_FAMILY_MAX_AMOUNT)
        maxPrice?.value ? maxPrice.value.toLong() : Long.MAX_VALUE
    }

    List listSports() {
        return multisport ? Sport.list() : Sport.coreSportAndOther.list()
    }

    static def parseAll(def facilityIds) {
        if (facilityIds == null) return []
        return facilityIds.tokenize(',')
    }

    static namedQueries = {
        activeFacilities {
            eq "active", Boolean.TRUE
        }

        bookableFacilities {
            eq "bookable", Boolean.TRUE
        }

        bookableAndActiveFacilities {
            eq "active", Boolean.TRUE
            eq "bookable", Boolean.TRUE
        }

        queryBookableAndActiveFacilities { QueryFacilitiesCommand cmd ->
            eq "active", Boolean.TRUE
            eq "bookable", Boolean.TRUE

            if (cmd.country) {
                eq "country", cmd.country
            }
            if (cmd.sportIds) {
                sports {
                    inList "id", cmd.sportIds
                }
            }
            if (cmd.hasCamera?.equals(true)) {
                facilityProperties {
                    eq "key", FacilityPropertyKey.FEATURE_MATCHI_PLAY.name()
                    or {
                        eq "value", "1"
                        eq "value", "on"
                    }
                }
            }
        }
    }

    int getBookingRuleNumDaysBookableForUser(User user) {
        if (!user) {
            return bookingRuleNumDaysBookable
        }

        Customer customer = Customer.findByFacilityAndUser(this, user)

        if (!customer) {
            return bookingRuleNumDaysBookable
        }

        return customer.getDaysBookable()
    }

    boolean isAnySeasonUpdating() {
        ScheduledTask.withCriteria {
            eq('facility', this)
            eq('relatedDomainClass', Season.class.simpleName)
            eq('isTaskFinished', false)
        }
    }

    static enum OpeningHoursType {
        OPENING_HOURS,
        STAFFED_OPENING_HOURS,
        BOOKABLE_HOURS
    }

    boolean equals(o) {
        if (this.is(o)) {
            return true
        }
        if (o instanceof Facility) {
            return (this.id == ((Facility) o).id)
        }
        return false
    }
}
