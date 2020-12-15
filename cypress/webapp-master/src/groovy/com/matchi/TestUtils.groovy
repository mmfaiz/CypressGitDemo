package com.matchi

import com.matchi.Camera.CameraProvider
import com.matchi.activities.Activity
import com.matchi.activities.ActivityOccasion
import com.matchi.activities.ClassActivity
import com.matchi.activities.Participant
import com.matchi.activities.Participation
import com.matchi.activities.trainingplanner.CourseActivity
import com.matchi.activities.trainingplanner.Trainer
import com.matchi.activities.trainingplanner.TrainingCourt
import com.matchi.adyen.AdyenNotification
import com.matchi.coupon.Coupon
import com.matchi.dynamicforms.Form
import com.matchi.dynamicforms.Submission
import com.matchi.enums.BookingGroupType
import com.matchi.enums.RedeemAt
import com.matchi.events.EventInitiator
import com.matchi.events.SystemEventInitiator
import com.matchi.external.ExternalSynchronizationEntity
import com.matchi.facility.Organization
import com.matchi.fortnox.v3.FortnoxInvoice
import com.matchi.i18n.Translatable
import com.matchi.invoice.Invoice
import com.matchi.membership.Membership
import com.matchi.membership.MembershipType
import com.matchi.orders.*
import com.matchi.payment.PaymentMethod
import com.matchi.price.*
import com.matchi.requirements.RequirementProfile
import com.matchi.subscriptionredeem.SubscriptionRedeem
import com.matchi.subscriptionredeem.redeemstrategy.InvoiceRowRedeemStrategy
import com.matchi.subscriptionredeem.redeemstrategy.InvoiceRowRedeemStrategy.RedeemAmountType
import com.matchi.subscriptionredeem.redeemstrategy.RedeemStrategy
import org.apache.commons.lang.RandomStringUtils
import org.joda.time.DateTime
import org.joda.time.LocalDate
import org.joda.time.LocalTime

/**
 * @author Sergei Shushkevich
 */
class TestUtils {

    static Booking createBooking(Customer customer = null, Slot slot = null) {
        new Booking(customer: customer ?: createCustomer(), slot: slot ?: createSlot())
                .save(failOnError: true, flush: true)
    }

    static Coupon createCoupon(Facility facility = null) {
        new Coupon(name: RandomStringUtils.randomAlphabetic(10),
                nrOfTickets: 10, facility: facility ?: createFacility()).save(failOnError: true)
    }

    static CourseActivity createCourse(Facility facility = createFacility(), Date startDate = new Date(),
                                       Date endDate = new Date() + 1, Form form = createForm(), int listPosition = 0, boolean showOnline = true) {
        new CourseActivity(name: RandomStringUtils.randomAlphabetic(10), facility: facility,
                startDate: startDate, endDate: endDate, form: form, listPosition: listPosition, showOnline: showOnline).save(failOnError: true, flush: true)
    }

    static ClassActivity createClassActivity(Facility facility = createFacility(), String name = RandomStringUtils.randomAlphabetic(10)) {
        new ClassActivity(archived: false, facility: facility, name: name).save(flush: true, failOnError: true)
    }

    static ActivityOccasion createActivityOccasion(Activity activity = createClassActivity()) {
        new ActivityOccasion(activity: activity, date: new LocalDate(), startTime: new LocalTime(), endTime: new LocalTime().plusHours(1)).save(failOnError: true, flush: true)
    }

    static Participant createCourseParticipant(Customer customer = createCustomer(),
                                               CourseActivity course = createCourse(), Participant.Status status = Participant.Status.ACTIVE) {
        new Participant(customer: customer, activity: course, status: status)
                .save(failOnError: true, flush: true)
    }

    static Participation createActivityOccasionParticipation(Customer customer = createCustomer(), ActivityOccasion activityOccasion = createActivityOccasion(), DateTime joined = new DateTime()) {
        new Participation(customer: customer, occasion: activityOccasion, joined: joined).save(failOnError: true, flush: true)
    }

    static Court createCourt(Facility facility = null, Sport sport = null, Integer listPosition = 1,
                             Court.Restriction restriction = Court.Restriction.NONE, Court.Surface surface = Court.Surface.HARD, boolean indoor = false) {
        new Court(name: RandomStringUtils.randomAlphabetic(10), listPosition: listPosition,
                facility: facility ?: createFacility(), sport: sport ?: createSport(), restriction: restriction,
                surface: surface, indoor: indoor)
                .save(failOnError: true, flush: true)
    }

    static BookingCancelTicket createBookingCancelTicket(Long bookingId, Long bookingCustomerId) {
        new BookingCancelTicket(bookingId: bookingId, bookingCustomerId: bookingCustomerId,
                key: RandomStringUtils.random(30, true, true), expires: new Date() + 1)
                .save(failOnError: true, flush: true)
    }

    static BookingGroup createBokingGroup(BookingGroupType type = BookingGroupType.SUBSCRIPTION) {
        new BookingGroup(type: type).save(failOnError: true, flush: true)
    }

    static Customer createCustomer(Facility facility = null, String email = null,
                                   String firstname = null, String lastname = null, Customer.CustomerType type = null, User user = null) {
        new Customer(facility: facility ?: createFacility(), number: Customer.count() + 1, email: email,
                firstname: firstname ?: "John", lastname: lastname ?: "Doe", companyname: "MATCHi", type: type, user: user)
                .save(failOnError: true, flush: true)
    }

    static Player createPlayer(Facility facility, String email, Booking booking) {
        new Player(email: email, customer: createCustomer(facility, email), booking: booking).save(failOnError: true, flush: true)
    }

    static CustomerDisableMessagesTicket createCustomerDisableMessagesTicket(Customer customer = null) {
        new CustomerDisableMessagesTicket(customer: customer ?: createCustomer(),
                key: RandomStringUtils.random(30, true, true), expires: new Date() + 1)
                .save(failOnError: true, flush: true)
    }

    static Facility createFacility(Municipality municipality = null) {
        new Facility(name: RandomStringUtils.randomAlphabetic(10),
                shortname: RandomStringUtils.randomAlphabetic(10),
                email: "${RandomStringUtils.randomAlphabetic(10)}@matchi.se", active: true, bookable: true, enabled: true,
                bookingRuleNumDaysBookable: 10, boxnet: false, lat: 57.7012, lng: 12.0261, vat: 0,
                municipality: municipality ?: createMunicipality(),
                country: "SV")
                .save(failOnError: true, flush: true)
    }

    static FacilityContract createFacilityContract(Facility facility = null) {
        new FacilityContract(name: RandomStringUtils.randomAlphabetic(10),
                fixedMonthlyFee: 1, variableMediationFee: 2, variableMediationFeePercentage: 3.0d,
                dateValidFrom: new Date(), facility: facility ?: createFacility(),
                variableCouponMediationFee: 2, variableUnlimitedCouponMediationFee: 2,
                variableGiftCardMediationFee: 3.5)
                .save(failOnError: true, flush: true)
    }

    static FacilityContractItem createFacilityContractItem(FacilityContract contract = null) {
        new FacilityContractItem(description: RandomStringUtils.randomAlphabetic(10),
                price: 99.99, contract: contract ?: createFacilityContract(), chargeMonths: [1])
                .save(failOnError: true, flush: true)
    }

    static Form createForm(Facility facility = null, Date activeFrom = new Date() - 1, Date activeTo = new Date() + 1) {
        new Form(facility: facility ?: createFacility(), name: RandomStringUtils.randomAlphabetic(10),
                activeFrom: activeFrom, activeTo: activeTo).save(failOnError: true, flush: true)
    }

    static Submission createSubmission(Customer customer = createCustomer(), Form form = createForm(), User user = createUser()) {
        new Submission(customer: customer, form: form, submissionIssuer: user).save(failOnError: true, flush: true)
    }

    static GlobalNotification createGlobalNotification() {
        new GlobalNotification(notificationText: new Translatable(translations: [en: "test"]),
                publishDate: new Date(), endDate: new Date() + 1, title: RandomStringUtils.randomAlphabetic(10),
                isForUsers: false, isForFacilityAdmins: false)
                .save(failOnError: true)
    }

    static MembershipType createMembershipType(Facility facility = null, boolean paidOnRenewal = false) {
        new MembershipType(name: RandomStringUtils.randomAlphabetic(10), price: 123,
                facility: facility ?: createFacility(), paidOnRenewal: paidOnRenewal)
                .save(failOnError: true, flush: true)
    }

    static Membership createMembership(Customer customer, LocalDate startDate = null,
                                       LocalDate endDate = null, LocalDate gracePeriodEndDate = null,
                                       MembershipType type = null, Boolean cancel = false, Boolean activated = true) {
        def today = new LocalDate()
        def issuer = createUser("${RandomStringUtils.randomAlphabetic(10)}@matchi.se")
        def order = new Order(price: 0, vat: 0, status: Order.Status.COMPLETED,
                article: Order.Article.MEMBERSHIP, user: issuer, issuer: issuer,
                description: RandomStringUtils.randomAlphabetic(10),
                facility: customer.facility, dateDelivery: new Date())
                .save(failOnError: true, flush: true)
        def m = new Membership(startDate: startDate ?: today, endDate: endDate ?: today,
                gracePeriodEndDate: gracePeriodEndDate ?: today, order: order, type: type,
                cancel: cancel, activated: activated)
        customer.addToMemberships(m).save(failOnError: true, flush: true)
        m
    }

    static Municipality createMunicipality(Region region = null) {
        new Municipality(name: "Stockholm", lat: 59.32893, lng: 18.06491, zoomlv: 11,
                region: region ?: createRegion())
                .save(failOnError: true, flush: true)
    }

    static Order createOrder(User user, Facility facility,
                             Order.Article article = Order.Article.MEMBERSHIP,
                             Map metadata = null, Date dateCreated = new Date(), Date dateDelivery = new Date()) {
        new Order(article: article, description: RandomStringUtils.randomAlphabetic(10),
                metadata: metadata, user: user, issuer: user, facility: facility, dateDelivery: dateDelivery,
                dateCreated: dateCreated, price: 100, vat: 0)
                .save(failOnError: true, flush: true)
    }

    static AdyenOrderPayment createAdyenOrderPayment(User issuer, Order order, String transactionId,
                                                     OrderPayment.Status status = OrderPayment.Status.NEW,
                                                     PaymentMethod method = PaymentMethod.CREDIT_CARD,
                                                     AdyenOrderPaymentError error = null) {
        new AdyenOrderPayment(issuer: issuer, orders: [order], status: status.toString(),
                transactionId: transactionId, method: method, error: error)
                .save(failOnError: true, flush: true)
    }

    static InvoiceOrderPayment createInvoiceOrderPayment(User issuer, Order order, String transactionId,
                                                         OrderPayment.Status status = OrderPayment.Status.NEW,
                                                         PaymentMethod method = PaymentMethod.INVOICE,
                                                         AdyenOrderPaymentError error = null) {
        new InvoiceOrderPayment(issuer: issuer, orders: [order], status: status.toString(),
                transactionId: transactionId, method: method, error: error)
                .save(failOnError: true, flush: true)
    }

    static AdyenOrderPaymentError createAdyenOrderPaymentError(Date dateCreated = new Date(), Date lastUpdated = new Date(),
                                                               String action = AdyenNotification.EventCode.CAPTURE) {
        new AdyenOrderPaymentError(action: action.toString(), reason: RandomStringUtils.random(10),
                dateCreated: dateCreated, lastUpdated: lastUpdated)
                .save(failOnError: true, flush: true)
    }

    static AdyenNotification createAdyenNotification(id = 1l, pspReference = "1", eventCode = AdyenNotification.EventCode.AUTHORISATION,
                                                     success = Boolean.TRUE, executed = Boolean.FALSE) {
        new AdyenNotification(id: id, pspReference: pspReference, eventCode: eventCode, success: success, executed: executed)
                .save(failOnError: true, flush: true)
    }

    static Region createRegion() {
        new Region(name: RandomStringUtils.randomAlphabetic(10), lat: 59.32893, lng: 18.06491, zoomlv: 9, country: "SE")
                .save(failOnError: true, flush: true)
    }

    static Season createSeason(Facility facility = createFacility(), Date startTime = new Date(),
                               Date endTime = new Date() + 1) {
        new Season(name: RandomStringUtils.randomAlphabetic(10), facility: facility,
                startTime: startTime, endTime: endTime).save(failOnError: true, flush: true)
    }

    static Slot createSlot(Court court = null, Date startTime = null, Date endTime = null, Subscription subscription = null, Long id = 1l) {
        new Slot(id: id, court: court ?: createCourt(), startTime: startTime ?: new Date(), endTime: endTime ?: new Date() + 1, subscription: subscription)
                .save(failOnError: true, flush: true)
    }

    static Sport createSport() {
        new Sport(name: RandomStringUtils.randomAlphabetic(10), position: 0).save(failOnError: true, flush: true)
    }

    static Subscription createSubscription(Customer customer = null, BookingGroup bookingGroup = null, Court court = null) {
        new Subscription(customer: customer ?: createCustomer(), bookingGroup: bookingGroup ?: createBokingGroup(),
                time: new LocalTime(), court: court ?: createCourt()).save(failOnError: true, flush: true)
    }

    static User createUser(String email = "jdoe@localhost.net") {
        new User(firstname: RandomStringUtils.randomAlphabetic(10),
                lastname: RandomStringUtils.randomAlphabetic(10),
                email: email, password: "1", enabled: true, gender: User.Gender.male)
                .save(failOnError: true, flush: true)
    }

    static UserMessage createUserMessage(User from, User to, Boolean markedAsRead = false) {
        new UserMessage(from: from, to: to, markedAsRead: markedAsRead,
                message: RandomStringUtils.randomAlphabetic(10)).save(failOnError: true, flush: true)
    }

    static TrainingCourt createTrainingCourt(Facility facility = null, Court court = null) {
        new TrainingCourt(name: RandomStringUtils.randomAlphabetic(10), facility: facility ?: createFacility(),
                court: court).save(failOnError: true, flush: true)
    }

    static InvoiceRowRedeemStrategy createInvoiceRowRedeemStrategy(RedeemAmountType amountType = null, Long amount = null) {
        new InvoiceRowRedeemStrategy(description: RandomStringUtils.randomAlphabetic(10), amount: amount ?: 1l,
                redeemAmountType: amountType ?: RedeemAmountType.PERCENTAGE_BACK).save(failOnError: true, flush: true)
    }

    static SubscriptionRedeem createSubscriptionRedeem(Facility facility, RedeemAt redeemAt = null, RedeemStrategy strategy = null) {
        new SubscriptionRedeem(facility: facility, redeemAt: redeemAt ?: RedeemAt.SLOTREBOOKED,
                strategy: strategy ?: createInvoiceRowRedeemStrategy()).save(failOnError: true, flush: true)
    }

    static PriceList createPriceList(Facility facility = null, Sport sport = null, boolean subscriptions = false,
                                     Date startDate = null, String name = null) {
        new PriceList(facility: facility ?: createFacility(), sport: sport ?: createSport(), subscriptions: subscriptions,
                startDate: startDate ?: new DateTime().minusDays(1).toDate(),
                name: name ?: RandomStringUtils.randomAlphabetic(10)).save(failOnError: true, flush: true)
    }

    static PriceListConditionCategory createPriceListConditionCategory(PriceList priceList, String name = null,
                                                                       BookingPriceCondition bookingPriceCondition = null,
                                                                       boolean defaultCategory = false) {
        new PriceListConditionCategory(pricelist: priceList, name: name ?: RandomStringUtils.randomAlphabetic(10),
                conditions: bookingPriceCondition ?: [], defaultCategory: defaultCategory)
                .save(failOnError: true, flush: true)
    }

    static PriceListCustomerCategory createPriceListCustomerCategory(Facility facility, String name = null,
                                                                     CustomerPriceCondition customerPriceCondition = null,
                                                                     boolean defaultCategory = false) {
        new PriceListCustomerCategory(facility: facility, name: name ?: RandomStringUtils.randomAlphabetic(10),
                conditions: customerPriceCondition ?: [], defaultCategory: defaultCategory)
                .save(failOnError: true, flush: true)
    }

    static Price createPrice(PriceListConditionCategory priceCategory, PriceListCustomerCategory customerCategory,
                             Long price = null) {
        new Price(price: price ?: 100L, priceCategory: priceCategory, customerCategory: customerCategory)
                .save(failOnError: true, flush: true)
    }

    static Trainer createTrainer(Facility facility = null, Sport sport = null, Customer customer = null, Boolean isBookable = false) {
        new Trainer(facility: facility ?: createFacility(), sport: sport ?: createSport(), customer: customer ?: createCustomer(),
                firstName: RandomStringUtils.randomAlphabetic(10), lastName: RandomStringUtils.randomAlphabetic(10), isBookable: isBookable)
                .save(failOnError: true, flush: true)
    }

    static Availability createAvailability(int weekDay = 1, LocalTime begin = new LocalTime(), LocalTime end = new LocalTime().plusHours(1)) {
        new Availability(weekday: weekDay, begin: begin, end: end)
                .save(failOnError: true, flush: true)
    }

    static RequirementProfile createRequirementProfile(Facility facility = createFacility(), String name = "TestRequirementProfile") {
        new RequirementProfile(facility: facility, name: name).save(failOnError: true, flush: true)
    }

    static Organization createOrganization(Facility facility, String name = null) {
        new Organization(facility: facility, name: name ?: RandomStringUtils.randomAlphabetic(10)).save(failOnError: true, flush: true)
    }

    static Group createGroup(Facility facility = null) {
        new Group(facility: facility ?: createFacility(), name: RandomStringUtils.randomAlphabetic(10)).save(failOnError: true, flush: true)
    }

    static Invoice createInvoice(Customer customer) {
        new Invoice(customer: customer, invoiceDate: new Date()).save(failOnError: true, flush: true)
    }

    static ExternalSynchronizationEntity createExternalSynchronizationEntity(Invoice invoice, String instance) {
        new ExternalSynchronizationEntity(entity: ExternalSynchronizationEntity.LocalEntity.INVOICE,
                externalSystem: ExternalSynchronizationEntity.ExternalSystem.FORTNOX, entityId: invoice.id,
                externalEntityId: (Math.random() * 100000) as Long, lastSynchronized: invoice.lastUpdated,
                instance: instance).save(failOnError: true, flush: true)
    }

    static ExternalSynchronizationEntity createExternalSynchronizationEntity(Customer customer, String instance) {
        new ExternalSynchronizationEntity(entity: ExternalSynchronizationEntity.LocalEntity.CUSTOMER,
                externalSystem: ExternalSynchronizationEntity.ExternalSystem.FORTNOX, entityId: customer.id,
                externalEntityId: (Math.random() * 100000) as Long, lastSynchronized: customer.lastUpdated,
                instance: instance).save(failOnError: true, flush: true)
    }

    static ExternalSynchronizationEntity createExternalSynchronizationEntity(FortnoxInvoice invoice, String instance) {
        new ExternalSynchronizationEntity(entity: ExternalSynchronizationEntity.LocalEntity.INVOICE,
                externalSystem: ExternalSynchronizationEntity.ExternalSystem.FORTNOX, entityId: (Math.random() * 100000) as Long,
                externalEntityId: invoice.DocumentNumber as Long, lastSynchronized: invoice.InvoiceDate,
                instance: instance).save(failOnError: true, flush: true)
    }

    static FortnoxInvoice createFortnoxInvoice() {
        new FortnoxInvoice(DocumentNumber: (Math.random() * 100000) as Integer, InvoiceDate: new Date())
    }


    static Facility createFacilityFor(Map args = null) {
        new Facility(name: args.name ?: RandomStringUtils.randomAlphabetic(10),
                shortname: RandomStringUtils.randomAlphabetic(10),
                email: "${RandomStringUtils.randomAlphabetic(10)}@matchi.se", active: true, bookable: true,
                bookingRuleNumDaysBookable: 10, boxnet: false, lat: 57.7012, lng: 12.0261, vat: 0,
                municipality: args.municipality ?: createMunicipality(),
                country: "SV")
                .save(failOnError: true, flush: true)
    }

    static Form createFormFor(Map args = null) {
        new Form(facility: args.facility ?: createFacility(),
                name: args.name ?: RandomStringUtils.randomAlphabetic(10),
                activeFrom: args.activeFrom ?: new Date() - 1,
                activeTo: args.activeTo ?: new Date() + 1).save(failOnError: true, flush: true)
    }

    static CourseActivity createCourseFor(Map args = null) {
        new CourseActivity(
                name: args.name ?: RandomStringUtils.randomAlphabetic(10),
                facility: args.facility ?: createFacility(),
                startDate: args.startDate ?: new Date(),
                endDate: args.endDate ?: new Date() + 1,
                form: args.form ?: createForm(),
                listPosition: args.listPosition ?: 0).save(failOnError: true, flush: true)
    }

    static Submission createSubmissionFor(Map args) {
        new Submission(
                customer: args.customer ?: createCustomer(),
                form: args.form ?: createForm(),
                submissionIssuer: args.user ?: createUser()).save(failOnError: true, flush: true)
    }

    static Customer createCustomerFor(Map args) {
        new Customer(
                facility: args.facility ?: createFacility(),
                number: Customer.count() + 1,
                email: args.email,
                firstname: args.firstname ?: "John",
                lastname: args.lastname ?: "Doe",
                companyname: "MATCHi", type: args.type, user: args.user)
                .save(failOnError: true, flush: true)
    }

    static Camera createCamera(Court court, Long id = 1L, String name = "Test Camera", CameraProvider cameraProvider = CameraProvider.CAAI) {
        new Camera(cameraId: id, name: name, court: court, cameraProvider: cameraProvider).save(failOnError: true, flush: true)
    }

    static EventInitiator createSystemEventInitiator() {
        new SystemEventInitiator()
    }

    static Exception npeHappens(Closure closure) {
        Exception expectedException;
        try {
            closure.call()
        } catch (NullPointerException npe) {
            expectedException =  npe
        }
        return expectedException
    }
}
