package com.matchi

import com.matchi.activities.trainingplanner.Trainer
import com.matchi.coupon.CustomerCoupon
import com.matchi.devices.Device
import com.matchi.dynamicforms.Submission
import com.matchi.events.EventInitiator
import com.matchi.invoice.InvoiceRow
import com.matchi.membership.Membership
import com.matchi.orders.Order
import com.matchi.orders.OrderPayment
import com.matchi.orders.OrderRefund
import com.matchi.requests.TrainerRequest
import com.matchi.sportprofile.SportProfile
import com.matchi.watch.SlotWatch
import grails.plugin.asyncmail.Validator
import grails.util.Holders
import groovy.transform.ToString
import org.apache.commons.lang.RandomStringUtils
import org.apache.commons.lang.StringUtils
import org.joda.time.LocalDate

@ToString
class User implements Serializable, EventInitiator {

    private static final long serialVersionUID = 12L

    // "facilityUsers" - relationship between regular users (ROLE_USER) and facilities
    static hasMany = [roles        : UserRole, resetPasswordTickets: ResetPasswordTicket,
                      sportProfiles: SportProfile, availabilities: Availability,
                      favourites   : UserFavorite, formSumbissions: Submission, facilityUsers: FacilityUser]
    static belongsTo = [facility: Facility, municipality: Municipality]

   static enum Gender {
        male('Man'),
        female('Kvinna')

        String name

        Gender(String name) {
            this.name = name
        }

        static list() {
            return [male, female]
        }

        // ex: [CLAY, HARD]
        static toListFromString(def genderNames) {
            genderNames = genderNames.replaceAll(/(\[)/, '')
            genderNames = genderNames.replaceAll(/(\])/, '')

            def names = genderNames.split(",")
            return toList(names)
        }

        static toList(def genderNames) {
            return genderNames.collect {
                Gender.valueOf(it.trim())
            }
        }
    }

    String email
    String password

    String firstname
    String lastname
    String address
    String zipcode
    String city
    String telephone
    String country
    String nationality
    String description
    String activationcode
    String facebookUID
    String appleUID

    Date birthday = null
    Gender gender

    MFile profileImage
    MFile welcomeImage

    // Notifications and visibility
    boolean receiveBookingNotifications = true
    boolean receiveNewsletters = true
    boolean receiveCustomerSurveys = true
    boolean receiveFacilityNotifications = true

    boolean searchable = true
    boolean matchable

    boolean enabled = false
    boolean accountExpired = false
    boolean accountLocked = false
    boolean passwordExpired = false

    Date dateDeleted
    Date dateAgreedToTerms


    //User statistics
    Date dateCreated
    Date dateActivated
    Date dateBlocked
    Date lastLoggedIn
    Date lastUpdated

    String language = "en"  // ISO-639-1 code

    Boolean anonymouseBooking

    static constraints = {
        email(blank: false, unique: true, email: true)
        email(validator: { val ->
            return Validator.isMailbox(val)
        })
        password(nullable: true, maxSize: 255)
        firstname(nullable: false, blank: false, markup: true, maxSize: 255)
        lastname(nullable: false, blank: false, markup: true, maxSize: 255)
        address(nullable: true, markup: true, maxSize: 255)
        zipcode(nullable: true, markup: true, maxSize: 255)
        city(nullable: true, markup: true, maxSize: 255)
        telephone(nullable: true, markup: true, maxSize: 255)
        country(nullable: true, maxSize: 255)
        nationality(nullable: true)
        description(nullable: true, maxSize: 1000, markup: true)
        facility(nullable: true)
        activationcode(nullable: true)
        facebookUID(nullable: true)
        appleUID(nullable: true)
        birthday(nullable: true)
        gender(nullable: true)
        profileImage(nullable: true)
        welcomeImage(nullable: true)
        municipality(nullable: true)

        matchable(nullable: true)

        dateAgreedToTerms(nullable: true)

        dateCreated(nullable: true)
        dateActivated(nullable: true)
        dateBlocked(nullable: true)
        lastLoggedIn(nullable: true)
        lastUpdated(nullable: true)

        language(blank: false, maxSize: 2)

        anonymouseBooking(nullable: true)

        dateDeleted(nullable: true)
    }

    static mapping = {
        password column: '`password`'
        sportProfiles sort: 'sport', order: 'asc', cache: true
        facilityUsers batchSize: 5
        facility index: 'facility_offline_only_idx'
        facebookUID index: 'facebookuid_idx'
        cache true
    }

    static namedQueries = {
        allFacilityUsers { f ->
            createAlias("facilityUsers", "fu")
            eq("fu.facility", f)
        }
    }

    def beforeInsert() {
        firstname = StringUtils.capitalize(firstname)?.trim()
        lastname = StringUtils.capitalize(lastname)?.trim()
        email = StringUtils.lowerCase(email)?.trim()
    }

    def beforeUpdate() {
        firstname = StringUtils.capitalize(firstname)?.trim()
        lastname = StringUtils.capitalize(lastname)?.trim()
        email = StringUtils.lowerCase(email)?.trim()
    }

    String getPersonalNumber() {
        if (birthday) {
            def dateUtil = Holders.grailsApplication.mainContext.getBean('dateUtil')
            PersonalNumberSettings personalNumberSettings = dateUtil.getPersonalNumberSettings(country)
            return birthday.format(personalNumberSettings.longFormat)
        } else {
            return ""
        }
    }

    Set<Role> getAuthorities() {
        UserRole.findAllByUser(this).collect { it.role } as Set
    }

    boolean isInRole(String role) {
        def roles = getAuthorities()
        boolean hasRole = false
        roles.each {
            if (it.authority.equals(role)) {
                hasRole = true
            }
        }
        return hasRole
    }

    boolean isCustomerIn(Facility facility) {
        getCustomer(facility)
    }

    Customer getCustomer(Facility facility) {
        Customer.findByUserAndFacility(this, facility)
    }

    Boolean isBookableTrainer() {
        def trainerService = Holders.grailsApplication.mainContext.getBean('trainerService')
        return !trainerService.getUserBookableTrainers(this)?.isEmpty()
    }

    boolean hasActiveMembershipIn(Facility facility) {
        getCustomer(facility)?.hasActiveMembership()
    }

    boolean hasMembershipIn(Facility facility) {
        getCustomer(facility)?.hasMembership()
    }

    boolean hasCurrentRemotePayableMembershipIn(Facility facility) {
        return facility.hasEnabledRemotePaymentsFor(com.matchi.orders.Order.Article.MEMBERSHIP) && hasMembershipIn(facility) && getMembershipIn(facility).isRemotePayable()
    }

    Membership getMembershipIn(Facility facility) {
        getCustomer(facility)?.membership
    }

    boolean hasUpcomingMembershipIn(Facility facility) {
        getCustomer(facility)?.hasUpcomingMembership()
    }

    Membership getUpcomingMembershipIn(Facility facility) {
        getCustomer(facility)?.getUpcomingMembership()
    }

    boolean belongsTo(Group group) {
        return getGroups().findAll { it.id.equals(group.id) }.size() > 0
    }

    boolean hasFavourite(Facility facility) {
        return UserFavorite.findByUserAndFacility(this, facility)
    }

    def addToMatchingCustomers() {
        def customerService = Holders.grailsApplication.mainContext.getBean('customerService')
        log.debug("Add ${fullName()} to matching customers...")
        List<Facility> alreadyCustomerFacilities = Customer.findByUser(this)*.facility
        List<Customer> customers = customerService.findMatchingCustomers(alreadyCustomerFacilities, email, firstname, lastname)

        customers.each { Customer customer ->
            // No need to check if already customer since we have done that already
            customerService.linkCustomerToUser(customer, this, false)
        }
    }

    def getGroups() {
        def customers = Customer.findByUser(this)
        return customers?.customerGroups?.collect { it.group }
    }

    String fullName() {
        return this.firstname + " " + this.lastname
    }

    def completeProfileAttrs() {
        return ["birthday", "gender", "municipality"]
    }

    def completeSportProfileAttrs() {
        return ["sportProfiles", "availabilities"]
    }

    double getRoundedAverageSkillLevel() {
        return Math.ceil(getAverageSkillLevel())
    }

    double getAverageSkillLevel() {
        def totalSkillLevel = 0

        sportProfiles.each { SportProfile sp ->
            totalSkillLevel += sp.skillLevel
        }

        totalSkillLevel > 0 ? totalSkillLevel / sportProfiles.size() : 0
    }

    List hasCouponsAt() {
        Facility.createCriteria().list {
            coupons {
                customerCoupons {
                    customer {
                        eq("user", this)
                    }

                }
            }
            projections {
                distinct("name")
            }
        } ?: []
    }

    List hasMembershipIn() {
        def today = new LocalDate()
        return Facility.createCriteria().list {
            createAlias("customers", "c")
            createAlias("c.memberships", "m")
            eq("c.user", this)
            le("m.startDate", today)
            ge("m.gracePeriodEndDate", today)
            projections {
                distinct("name")
            }
        } ?: []
    }

    List playsAt() {
        return Facility.createCriteria().list {
            courts {
                slots {
                    booking {
                        customer {
                            eq "user", this
                        }
                    }
                }
            }
            projections {
                distinct('name')
            }
        } ?: []
    }

    String toString() { "$email" }

    /**
     *  Checks if there are any Orders or OrderPayments belonging to this user as issuer.
     *  Worst case complexity is O(n) where n is the number of NEW orders of the user
     */
    boolean isHardDeletable() {
        // Checking non-order-objects
        final CustomerCoupon customerCoupon = CustomerCoupon.findByCreatedBy(this)
        final Submission submission = Submission.findBySubmissionIssuer(this)
        final InvoiceRow invoiceRow = InvoiceRow.findByCreatedBy(this)
        final Membership membership = Membership.findByCreatedBy(this)
        final Order nonDeletableOrder = Order.findByIssuerAndStatusNotEqual(this, Order.Status.NEW)
        final OrderPayment nonDeleteablePayment = OrderPayment.findByIssuerAndStatusNotEqual(this, OrderPayment.Status.NEW)

        if (nonDeletableOrder || nonDeleteablePayment || customerCoupon || submission || invoiceRow || membership) return false

        final List<Order> possibleDeletableOrders = Order.findAllByIssuerAndStatus(this, Order.Status.NEW)

        return !possibleDeletableOrders || !possibleDeletableOrders.any { final Order order ->
            !order.deletable
        }
    }

    /**
     * Returns all orders that are deletable
     * @return
     */
    List<Order> getDeleteableOrders() {
        return Order.findAllByIssuer(this).findAll { Order order ->
            return order.isDeletable()
        }
    }

    void deleteOrders() {
        getDeleteableOrders().each { Order order ->
            order.deleteWithPayments(false)
        }
    }

    /**
     *  Removes connection to facility-related objects
     */
    void disconnectFromFacilities() {
        List<Customer> customers = Customer.findAllByUser(this)
        customers.each { Customer c ->
            c.user = null
            c.save()
        }

        List<Trainer> trainers = Trainer.findAllByUser(this)
        trainers.each { Trainer t ->
            t.user = null
            t.save()
        }

        this.facilityUsers?.toList().each { FacilityUser facilityUser ->
            this.removeFromFacilityUsers(facilityUser)

            facilityUser.facilityRoles?.toList().each { FacilityUserRole facilityUserRole ->
                facilityUser.removeFromFacilityRoles(facilityUserRole)
                facilityUserRole.delete()
            }

            facilityUser.delete()
        }

        this.facility = null
    }

    /**
     * Deletes relations to all non-Order-objects depending on the user, that we do not have to keep.
     */
    void deleteRelations(boolean hard = false) {
        List<SlotWatch> slotWatches = SlotWatch.findAllByUser(this)
        slotWatches.each { SlotWatch slotWatch ->
            slotWatch.delete()
        }

        List<Device> devices = Device.findAllByUser(this)
        devices.each { Device device ->
            device.delete()
        }

        List<ChangeEmailTicket> changeEmailTickets = ChangeEmailTicket.findAllByUser(this)
        changeEmailTickets.each { ChangeEmailTicket changeEmailTicket ->
            changeEmailTicket.delete()
        }

        List<PaymentInfo> paymentInfos = PaymentInfo.findAllByUser(this)
        paymentInfos.each { PaymentInfo paymentInfo ->
            paymentInfo.delete()
        }

        List<TrainerRequest> trainerRequests = TrainerRequest.findAllByRequester(this)
        trainerRequests.each { TrainerRequest trainerRequest ->
            trainerRequest.delete()
        }

        List<UserMessage> userMessagesTo = UserMessage.findAllByTo(this)
        List<UserMessage> userMessagesFrom = UserMessage.findAllByFrom(this)
        userMessagesTo.each { UserMessage userMessage -> userMessage.delete() }
        userMessagesFrom.each { UserMessage userMessage -> userMessage.delete() }

        // SportProfile belongs to both User and Sport, and needs to be removed from both
        this.sportProfiles?.toList().each { SportProfile sportProfile ->
            sportProfile.sport.removeFromSportProfiles(sportProfile)
            this.removeFromSportProfiles(sportProfile)
            sportProfile.delete()
        }

        UserRole.removeAll(this)

        this.favourites?.toList().each { UserFavorite userFavorite ->
            this.removeFromFavourites(userFavorite)
            userFavorite.delete()
        }

        this.availabilities?.toList().each { Availability availability ->
            this.removeFromAvailabilities(availability)
            availability.delete()
        }

        this.resetPasswordTickets?.toList().each { ResetPasswordTicket resetPasswordTicket ->
            this.removeFromResetPasswordTickets(resetPasswordTicket)
            resetPasswordTicket.delete()
        }
    }

    /**
     * Clear every simple property clearable
     */
    void clearProperties() {
        this.dateDeleted = new Date()

        this.password = null
        this.address = null
        this.zipcode = null
        this.city = null
        this.telephone = null
        this.country = null
        this.nationality = null
        this.description = null
        this.activationcode = null
        this.facebookUID = null
        this.birthday = null
        this.gender = null
        this.profileImage = null
        this.welcomeImage = null
        this.receiveBookingNotifications = false
        this.receiveNewsletters = false
        this.searchable = false
        this.matchable = false
        this.enabled = false
        this.accountExpired = true
        this.accountLocked = true
        this.passwordExpired = true
        this.dateActivated = null
        this.dateBlocked = null
        this.lastLoggedIn = null
        this.anonymouseBooking = null
        this.municipality = null
    }

    /**
     * Removes connection as USER (not issuer) from all Orders where not issuer, and from OrderRefunds where issuer.
     */
    void removeFromOrders() {
        Order.findAllByUserAndIssuerNotEqual(this, this).each { Order order ->
            order.user = null
            order.save()
        }

        List<OrderRefund> refunds = OrderRefund.findAllByIssuer(this)
        refunds.each { OrderRefund orderRefund ->
            orderRefund.issuer = null
            orderRefund.save()
        }
    }

    /**
     * Pseudo-random (?) scrambling function
     */
    void scramble() {
        long unixTime = new Date().getTime()

        this.firstname = (unixTime + RandomStringUtils.randomAlphanumeric(10)).encodeAsMD5()
        this.lastname = (RandomStringUtils.randomAlphanumeric(8) + unixTime).encodeAsMD5()
        this.email = 'DELETED-' + (RandomStringUtils.randomAlphanumeric(8) + unixTime).encodeAsMD5() + '@matchi.se'
    }

    // IEventInitiator
    @Override
    String getInitiatorIdentifier() {
        return id
    }
}
