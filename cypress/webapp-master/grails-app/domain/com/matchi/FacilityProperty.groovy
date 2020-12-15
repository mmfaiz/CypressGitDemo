package com.matchi
/**
 * Key/value configuration property for facility.
 *
 * All keys are defined in FacilityPropertyKey enum.
 * Descriptions to keys are documented in message.properties
 */
class FacilityProperty implements Serializable {

    private static final long serialVersionUID = 12L

    static belongsTo = [facility: Facility]

    Date dateCreated
    Date lastUpdated

    String key
    String value

    static constraints = {
        key(nullable: false, unique: ['facility'],  validator: {
            // key must be value of FacilityPropertyKey
            if (!FacilityPropertyKey.values().collect { key -> key.toString() }.contains(it)
            ) return ['invalid.key']
        })
        value(nullable: true)
    }

    static mapping = {
        autoTimestamp true
        key(column: 'key_name')
        cache true
    }

    static enum FacilityPropertyKey {
        /* MATCHi invoicing settings */
        INVOICE_NUMBER_START (0, FacilityPropertyCategory.INVOICE),
        INVOICE_FACILITY_NAME ("FAC_NAME", FacilityPropertyCategory.INVOICE),
        INVOICE_COMPANY_HOME ("FAC_HOME", FacilityPropertyCategory.INVOICE),
        INVOICE_EMAIL ("", FacilityPropertyCategory.INVOICE),
        FEATURE_EXTERNAL_ARTICLES (false, FacilityPropertyCategory.INVOICE),
        PAYOUT_BANKGIRO ("", FacilityPropertyCategory.INVOICE),
        PAYOUT_PLUSGIRO ("", FacilityPropertyCategory.INVOICE),

        /* REMOTE PAYMENTS */
        FEATURE_REMOTE_PAYMENT_MEMBERSHIP(true, FacilityPropertyCategory.REMOTEPAYMENT),
        FEATURE_REMOTE_PAYMENT_BOOKING(false, FacilityPropertyCategory.REMOTEPAYMENT),

        /* White label pages */
        FEATURE_WHITE_LABEL(false, FacilityPropertyCategory.WHITELABEL),
        WHITE_LABEL_EXT_URL ("", FacilityPropertyCategory.WHITELABEL),

        /* Possibility to send a contract to subscriber */
        FEATURE_SUBSCRIPTION_CONTRACT (false, FacilityPropertyCategory.SUBSCRIPTION),
        SUBSCRIPTION_CONTRACT_TEXT ("", FacilityPropertyCategory.SUBSCRIPTION, true),

        /* Amount in percentage to refund when user makes late cancellation */
        BOOKING_LATE_REFUND_PERCENTAGE (0, FacilityPropertyCategory.LATECANCELLATION),

        /* Hour limit to be considered late booking */
        BOOKING_CANCELLATION_LIMIT (6, FacilityPropertyCategory.LATECANCELLATION),

        /* Subscriptions */
        FEATURE_SUBSCRIPTION_REMINDER(false, FacilityPropertyCategory.SUBSCRIPTION),
        FEATURE_SUBSCRIPTION_REMINDER_ADJUST(false, FacilityPropertyCategory.SUBSCRIPTION),
        SUBSCRIPTION_REMINDER_HOURS(24, FacilityPropertyCategory.SUBSCRIPTION),
        SUBSCRIPTION_REMINDER_TEXT("", FacilityPropertyCategory.SUBSCRIPTION, true),

        /* Fortnox 3 API */
        FORTNOX3_AUTHORIZATION_CODE("", FacilityPropertyCategory.FORTNOX),
        FORTNOX3_ACCESS_TOKEN("", FacilityPropertyCategory.FORTNOX),
        FORTNOX3_CUSTOMER_NUMBER("", FacilityPropertyCategory.FORTNOX),
        FORTNOX3_COST_CENTER("", FacilityPropertyCategory.FORTNOX),

        /* BackhandSmash */
        BACKHANDSMASH_CUSTOMER_ID("", FacilityPropertyCategory.BACKHANDSMASH),

        /* Membership */
        FEATURE_MEMBERSHIP_REQUEST_PAYMENT(true, FacilityPropertyCategory.MEMBERSHIP),
        FEATURE_USE_FAMILY_MEMBERSHIPS(false, FacilityPropertyCategory.MEMBERSHIP),
        FACILITY_MEMBERSHIP_FAMILY_MAX_AMOUNT("", FacilityPropertyCategory.MEMBERSHIP),
        FEATURE_MONTHLY_MEMBERSHIP(false, FacilityPropertyCategory.MEMBERSHIP),
        FEATURE_RECURRING_MEMBERSHIP(false, FacilityPropertyCategory.MEMBERSHIP),

        /* Dynamic forms */
        FEATURE_FACILITY_DYNAMIC_FORMS(false, FacilityPropertyCategory.DYNAMICFORMS),

        /* Training planner */
        FEATURE_TRAINERS(false, FacilityPropertyCategory.TRAININGPLANNER),
        FEATURE_TRAINING_PLANNER(false, FacilityPropertyCategory.TRAININGPLANNER),
        FEATURE_BOOK_A_TRAINER(false, FacilityPropertyCategory.TRAININGPLANNER),
        FEATURE_PRIVATE_LESSON(false, FacilityPropertyCategory.TRAININGPLANNER),

        /* Offers */
        FEATURE_GIFT_CARDS(true, FacilityPropertyCategory.OFFERS),
        FEATURE_PROMO_CODES(true, FacilityPropertyCategory.OFFERS),

        /* SMS / textmessages */
        FEATURE_SMS(true, FacilityPropertyCategory.SMS),
        SMS_FROM("", FacilityPropertyCategory.SMS),

        /* Queue function */
        FEATURE_QUEUE(true, FacilityPropertyCategory.QUEUE),

        /* MPC / Integrated passage codes */
        FEATURE_MPC(false, FacilityPropertyCategory.MPC),
        MPC_NOTIFY_EMAIL_ADDRESSES("", FacilityPropertyCategory.MPC),
        MPC_NOTIFY_SMS_NUMBER("", FacilityPropertyCategory.MPC),
        MPC_LIGHT_GRACE_MINUTES_START(0, FacilityPropertyCategory.MPC),
        MPC_LIGHT_GRACE_MINUTES_END(0, FacilityPropertyCategory.MPC),
        MPC_STATUS(MpcStatus.NOT_OK.toString(), FacilityPropertyCategory.MPC, false, true),
        FEATURE_PERSONAL_ACCESS_CODE(false, FacilityPropertyCategory.ACCESSCODE),
        FEATURE_SUBSCRIPTION_ACCESS_CODE(false, FacilityPropertyCategory.ACCESSCODE),
        FEATURE_ACTIVITY_ACCESS_CODE(false, FacilityPropertyCategory.ACCESSCODE),

        /* MLCS */
        MLCS_LAST_HEARTBEAT("", FacilityPropertyCategory.MLCS, false, true),
        MLCS_GRACE_MINUTES_START(0, FacilityPropertyCategory.MLCS),
        MLCS_GRACE_MINUTES_END(0, FacilityPropertyCategory.MLCS),

        /* Portal setting, default sport */
        FACILITY_DEFAULT_SPORT("", FacilityPropertyCategory.SPORT),

        /* Calculate new price on multiple players */
        FEATURE_CALCULATE_MULTIPLE_PLAYERS_PRICE(false, FacilityPropertyCategory.MULTIPLEPLAYERS),
        MULTIPLE_PLAYERS_NUMBER("", FacilityPropertyCategory.MULTIPLEPLAYERS),

        /* Maximum number of bookings feature */
        FEATURE_MAXIMUM_NUMBER_OF_BOOKINGS_PER_USER(false, FacilityPropertyCategory.MAXIMUMBOOKINGS),
        MAXIMUM_NUMBER_OF_BOOKINGS_PER_USER(10, FacilityPropertyCategory.MAXIMUMBOOKINGS),
        FEATURE_MAXIMUM_NUMBER_OF_BOOKINGS_PER_COURT_GROUP(false, FacilityPropertyCategory.MAXIMUMBOOKINGS),

        /* Organizations */
        FEATURE_ORGANIZATIONS(false, FacilityPropertyCategory.ORGANIZATION),

        /* IdrottOnline */
        FEATURE_IDROTT_ONLINE(false, FacilityPropertyCategory.IDROTTONLINE), // Membership
        FEATURE_IDROTT_ONLINE_ACTIVITIES(false, FacilityPropertyCategory.IDROTTONLINE), // Activities (requires FEATURE_IDROTT_ONLINE and FEATURE_TRAINING_PLANNER to work)
        IDROTT_ONLINE_ORGANISATION_NUMBER("", FacilityPropertyCategory.IDROTTONLINE), // Used when the facilites organisation number does not match the one used in IdrottOnline.
        IDROTT_ONLINE_MEMBERSHIP_SPORT("", FacilityPropertyCategory.IDROTTONLINE), // Used to override default behaviour looping all selected facility sports and syncing all of them. For example syncing padel club to squash federation.
        IDROTT_ONLINE_ACTIVITIES_SPORT("", FacilityPropertyCategory.IDROTTONLINE), // Used to override default behaviour of Facility.getDefaultSport(). For example syncing padel club to squash federation.

        /* Federation */
        FEATURE_FEDERATION(false, FacilityPropertyCategory.FEDERATION),
        FEATURE_USE_CUSTOMER_NUMBER_AS_LICENSE(false, FacilityPropertyCategory.FEDERATION),

        /* Number of upcoming activity occasions to show */
        FACILITY_UPCOMING_OCCASIONS_NUMBER(4, FacilityPropertyCategory.ACTIVITY),

        /* Requirement Profiles */
        FEATURE_REQUIREMENT_PROFILES(false, FacilityPropertyCategory.REQUIREMENTPROFILE),

        /* Booking restrictions */
        FEATURE_BOOKING_RESTRICTIONS(false, FacilityPropertyCategory.BOOKINGRESTRICTIONS),

        FEATURE_LEAGUE_FROM_EXCEL(false, FacilityPropertyCategory.TOURNAMENTS),

        FEATURE_MATCHI_PLAY(false, FacilityPropertyCategory.CAMERAS)

        private FacilityPropertyKey(String defaultValue, FacilityPropertyCategory facilityPropertyCategory = FacilityPropertyCategory.OTHER, boolean textArea = false, boolean readOnly = false) {
            this.defaultValue = defaultValue
            this.facilityPropertyType = textArea ? FacilityPropertyType.TEXTAREA : FacilityPropertyType.STRING
            this.facilityPropertyCategory = facilityPropertyCategory
            this.readOnly = readOnly
        }

        private FacilityPropertyKey(Integer defaultValue, FacilityPropertyCategory facilityPropertyCategory = FacilityPropertyCategory.OTHER) {
            this.defaultValue = defaultValue
            this.facilityPropertyType = FacilityPropertyType.INTEGER
            this.facilityPropertyCategory = facilityPropertyCategory
            this.readOnly = false
        }

        private FacilityPropertyKey(boolean defaultValue, FacilityPropertyCategory facilityPropertyCategory = FacilityPropertyCategory.OTHER) {
            this.defaultValue = defaultValue ? "1" : "0"
            this.facilityPropertyType = FacilityPropertyType.BOOLEAN
            this.facilityPropertyCategory = facilityPropertyCategory
            this.readOnly = false
        }

        private final String defaultValue
        private final FacilityPropertyType facilityPropertyType
        private final FacilityPropertyCategory facilityPropertyCategory
        private final boolean readOnly

        String getDefaultValue() { return this.defaultValue }

        FacilityPropertyCategory getCategory() { return this.facilityPropertyCategory }

        FacilityPropertyType getType() { return this.facilityPropertyType }

        boolean getReadOnly() { return this.readOnly }
    }

    static enum FacilityPropertyType {
        INTEGER,
        STRING,
        TEXTAREA,
        BOOLEAN,
        ENUM
    }

    static enum FacilityPropertyCategory {
        SUBSCRIPTION,
        MEMBERSHIP,
        OFFERS,
        LATECANCELLATION,
        MAXIMUMBOOKINGS,
        MULTIPLEPLAYERS,
        ACTIVITY,
        SPORT,
        INVOICE,
        TRAININGPLANNER,
        PAYMENT,
        REMOTEPAYMENT,
        SMS,
        QUEUE,
        DYNAMICFORMS,
        ACCESSCODE,
        ORGANIZATION,
        FEDERATION,
        MPC,
        MLCS,
        IDROTTONLINE,
        FORTNOX,
        BACKHANDSMASH,
        WHITELABEL,
        REQUIREMENTPROFILE,
        BOOKINGRESTRICTIONS,
        TOURNAMENTS,
        CAMERAS,
        OTHER
    }

    static enum MpcStatus {
        OK,
        NOT_OK
    }

    // Properties that are editable for the facility admin
    static def getPropertiesAvailableForFacility() {
        return []
    }

    List getBackhandsmashCustomers() {
        def customers = []
        if (this.key == FacilityPropertyKey.BACKHANDSMASH_CUSTOMER_ID.toString()) {
            this.value.tokenize(",").each { String token ->
                customers << Customer.get(Long.parseLong(token))
            }
        }
        customers
    }
}


