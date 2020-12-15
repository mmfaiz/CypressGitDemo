package com.matchi

import com.matchi.FacilityProperty.FacilityPropertyKey
import com.matchi.activities.Participation
import com.matchi.coupon.CustomerCoupon
import com.matchi.coupon.CustomerCouponTicket
import com.matchi.play.PlayService
import com.matchi.play.Recording
import com.matchi.orders.Order
import com.matchi.orders.OrderPayment
import com.matchi.payment.PaymentFlow
import com.matchi.requests.TrainerRequest
import com.matchi.watch.ClassActivityWatch
import com.matchi.watch.SlotWatch
import com.matchi.sportprofile.SportProfile
import grails.plugin.asyncmail.Validator
import grails.validation.Validateable
import org.apache.http.HttpStatus
import org.codehaus.groovy.grails.web.pages.discovery.GrailsConventionGroovyPageLocator
import org.hibernate.criterion.CriteriaSpecification
import org.joda.time.LocalDateTime
import org.springframework.web.multipart.MultipartHttpServletRequest
import org.springframework.web.multipart.commons.CommonsMultipartFile

class UserProfileController extends GenericController {

    static allowedMethods = [updateAccountConsents: "POST"]
    static templateRootURI = "/rendering/receipt"
    static final defaultTemplateName = "receiptFacility"
    static final matchiTemplateName = "receiptMATCHi"

    GrailsConventionGroovyPageLocator groovyPageLocator
    def dateUtil
    def userService
    def paymentService
    def adyenService
    def notificationService
    def ticketService
    def bookingService
    def userActivityService
    def slotService
    def customerService
    def remotePaymentService
    def memberService
    OrderService orderService
    ActivityService activityService
    PlayService playService

    def index() {
        User user = params.id ? User.get(params.id) : getCurrentUser()

        if (!user) {
            response.sendError HttpStatus.SC_NOT_FOUND
            return
        }

        def isCurrentUser = user.id == getCurrentUser()?.id

        // If not searchable and not the same
        if (!user.isSearchable() && !isCurrentUser) {
            response.sendError HttpStatus.SC_NOT_FOUND
            return
        }

        def availabilities = []
        user?.availabilities?.each {
            if (it.active) {
                availabilities << it
            }
        }
        def userAvailableSportProfiles = getAvailableSportProfiles() - user.sportProfiles.collect { it.sport }

        [user: user, availabilities: availabilities, userAvailableSportProfiles: userAvailableSportProfiles, isCurrentUser: isCurrentUser]
    }

    def home() {
        User user = getCurrentUser()
        def activity = userActivityService.userActivityWeekly(user)

        def coupons = CustomerCoupon.createCriteria().listDistinct {
            customer {
                eq("user.id", user?.id)
            }
            coupon {
                ne("class", "promo_code")
            }
        }

        def availableCoupons = coupons.findAll { it.isValid() }
        def usedCoupons = coupons.findAll { !it.isValid() && !it.dateLocked }
        def activityWatches = ClassActivityWatch.findAllByUser(user, [sort: "fromDate", order: "asc"])
        def slotWatches = SlotWatch.findAllByUser(user, [sort: "fromDate", order: "asc"])
        def requests = TrainerRequest.findAllByRequesterAndEndGreaterThan(user, new LocalDateTime().toDate(), [sort: "start", order: "asc"])

        List<Booking> bookings = bookingService.getUserBookings(user)
        List<Participation> participations = activityService.getUserUpcomingParticipations(user)

        IReservation upcomingReservation
        List<IReservation> reservations = (bookings + participations)

        if (reservations) {
            upcomingReservation = reservations.sort { IReservation iReservation ->
                return iReservation.getDate()
            }.first()
        }

        [user               : user, bookings: bookings, activity: activity, activityWatches: activityWatches,
         availableCoupons   : availableCoupons, usedCoupons: usedCoupons, slotWatches: slotWatches,
         requests           : requests, federationsCustomers: customerService.listFederationsCustomers(user),
         memberships        : memberService.listUserMemberships(user), participations: participations,
         upcomingReservation: upcomingReservation]
    }

    def edit() {
        User user = getCurrentUser()
        List<Region> regions = Region.findAll()

        [cmd: user, regions: regions, user: user]
    }

    def bookings() {
        User user = getCurrentUser()
        List<Booking> bookings = bookingService.getUserBookings(user, true)
        List<Booking> subscriptionBookings = bookingService.getUserSubscriptionBookings(user)
        List<Participation> participations = activityService.getUserUpcomingParticipations(user)

        [user: user, bookings: bookings, subscriptionBookings: subscriptionBookings, participations: participations]
    }

    def recordings() {
        def user = getCurrentUser()
        List<Recording> recordings = playService.getUserRecordings(user)

        def limit = 10

        List<Recording> recordingsSubList = recordings.subList(
                Math.min(recordings.size(), Integer.parseInt(params.offset ?: "0").toInteger()),
                Math.min(recordings.size(), (Integer.parseInt(params.offset ?: "0") + limit).toInteger()))

        [user: user, recordings: recordingsSubList, totalCount: recordings.size(), limit: limit]
    }

    def pastBookings() {
        def user = getCurrentUser()
        def bookings = bookingService.getUserPastBookings(user, params.offset ?: 0)
        Map<Long, Recording> recordingsByBookingId = [:]
        recordingsByBookingId = playService.getRecordingsByBookingId(bookings as List<Booking>).findAll {
            playService.userCanAccessRecording(user, it.value)
        }
        [user: user, bookings: bookings, recordingsByBookingId: recordingsByBookingId]
    }

    def payments() {
        def user = getCurrentUser()

        def orders = Order.createCriteria().list(max: 10, offset: params.offset ?: 0) {
            createAlias("payments", "p", CriteriaSpecification.LEFT_JOIN)
            createAlias("booking", "booking", CriteriaSpecification.LEFT_JOIN)
            createAlias("participation", "participation", CriteriaSpecification.LEFT_JOIN)
            createAlias("memberships", "membership", CriteriaSpecification.LEFT_JOIN)
            createAlias("customerCoupon", "customerCoupon", CriteriaSpecification.LEFT_JOIN)
            createAlias("submission", "submission", CriteriaSpecification.LEFT_JOIN)
            createAlias("subscription", "subscription", CriteriaSpecification.LEFT_JOIN)
            createAlias("recordingPurchases", "recordingPurchases", CriteriaSpecification.LEFT_JOIN)
            or {
                and {
                    ne("status", Order.Status.ANNULLED)
                    ne("status", Order.Status.CANCELLED)
                    ne("status", Order.Status.NEW)
                    sqlRestriction("((amount - credited) > 0)")
                }
                and {
                    eq("status", Order.Status.ANNULLED)
                    eq("p.status", OrderPayment.Status.CAPTURED)
                }
                and {
                    eq("status", Order.Status.ANNULLED)
                    eq("p.status", OrderPayment.Status.CREDITED)
                }
                sqlRestriction("(type = 'netaxept' and {alias}.status = '${Order.Status.ANNULLED.name()}' and floor(credited) > 0 and (amount - credited) > 0)")
            }
            or {
                eq("status", Order.Status.ANNULLED)
                eq("status", Order.Status.CANCELLED)
                isNotNull("booking.id")
                isNotNull("participation.id")
                isNotNull("membership.id")
                isNotNull("customerCoupon.id")
                isNotNull("submission.id")
                isNotNull("subscription.id")
                isNotNull("recordingPurchases.id")
            }

            eq("p.issuer", user)

            sqlRestriction("type = 'adyen'")
            order("dateCreated", "desc")
        }
        /*def coupons = CustomerCoupon.createCriteria().listDistinct {
            customer {
                eq("user.id", user.id)
            }
        }

        def availableCoupons = coupons.findAll { it.isValid() }
        def usedCoupons      = coupons.findAll { !it.isValid() && !it.dateLocked }*/

        [user: user, orders: orders.unique { it.id }, recurringMemberships: memberService.listUserRecurringMembershipsToRenew(user)]
    }

    def remotePayments() {
        User user = getCurrentUser()
        Order showOrder = null
        List<Order> userOrders = remotePaymentService.getRemotePayableOrdersFor(user)
        try {
            if (params.showOrderId) {
                showOrder = orderService.getOrder(Long.parseLong(params.showOrderId))
                if (showOrder && !userOrders.contains(showOrder)) {
                    showOrder = null
                }
            }
        } catch (NumberFormatException nfe) {
            log.debug(nfe)
        }

        List<Order> orders = showOrder ? [showOrder] : userOrders

        def paginatedOrders
        def familyMemberships = [:]

        if (orders) {
            paginatedOrders = Order.createCriteria().list(max: 10, offset: params.offset ?: 0) {
                inList("id", orders*.id)
                order("dateDelivery", "asc")
            }

            paginatedOrders.each { o ->
                if (o.article == Order.Article.MEMBERSHIP) {
                    def membership = memberService.getMembership(o, user)
                    if (membership) {
                        if (membership.family) {
                            if (membership.isFamilyContact()) {
                                def members = membership.family.membersNotContact
                                familyMemberships[o.id] = [contactMembership: membership]
                                familyMemberships[o.id].nonContactMemberships = members
                                familyMemberships[o.id].familyPaymentAllowed = members.any { !it.paid }

                                // TODO: non-contact members can "pay for whole family" only
                                //       if facility "recurring memberships" feature is disabled,
                                //       since it may lead to incorrect behavior during memberships renewal.
                                //       Update renewal first and then remove this restriction
                            } else if (!membership.customer.facility.isFacilityPropertyEnabled(
                                    FacilityPropertyKey.FEATURE_RECURRING_MEMBERSHIP)) {
                                def familyPaymentAllowed = membership.family.members.count { !it.paid } > 1
                                if (familyPaymentAllowed) {
                                    familyMemberships[o.id] = [
                                            familyPaymentAllowed : familyPaymentAllowed,
                                            nonContactMemberships: [membership]]
                                    membership.family.members.each {
                                        if (it.id != membership.id) {
                                            familyMemberships[o.id].nonContactMemberships << it
                                        }
                                    }
                                }
                            }
                        } else {
                            familyMemberships[o.id] = [contactMembership: membership]
                        }
                    }
                }
            }
        }

        PaymentFlow paymentFlow = null
        if (params.long("orderId")) {
            paymentFlow = PaymentFlow.getFinished(session, params.long("orderId"))
        }

        [user             : user, orders: paginatedOrders, paymentFlow: paymentFlow,
         familyMemberships: familyMemberships]
    }

    def account() {
        User user = getCurrentUser()

        PaymentFlow paymentFlow = null
        if (params.long("orderId")) {
            paymentFlow = PaymentFlow.getFinished(session, params.long("orderId"))
        }
        [user: user, paymentInfo: paymentService.getAnyPaymentInfoByUser(user), paymentFlow: paymentFlow]
    }

    def forgetPaymentInfos() {
        User currentUser = getCurrentUser()
        adyenService.deletePaymentInfo(currentUser)
        flash.message = message(code: "userProfile.forgetPaymentInfo.success")
        redirect(action: "account")
    }

    def forgetFacebookConnect() {
        userService.forgetFacebookConnect(getCurrentUser())
        flash.message = message(code: "userProfile.forgetFacebookConnect.success")
        redirect(action: "account")
    }

    def update(UpdateUserProfileCommand cmd) {
        def user = getCurrentUser()

        if (cmd.hasErrors()) {
            render(view: "edit", model: [user: user, cmd: cmd])
            return
        }

        Boolean updateUserEmail = (cmd.email != user.email)

        if (updateUserEmail && User.findByEmail(cmd.email)) {
            flash.error = message(code: "userProfile.wrong.email")
            render(view: "edit", model: [user: user, cmd: cmd])
            return
        }

        copyProperties(user, cmd, updateUserEmail)

        user.birthday = dateUtil.composeBirthDate(cmd.birthYear, cmd.birthMonth, cmd.birthDay)
        user.lastUpdated = new Date()
        if (!user.hasErrors() && user.save()) {
            flash.message = message(code: "userProfile.update.success")
        }

        if (updateUserEmail) {
            def ticket = ticketService.createChangeEmailTicket(user, cmd.email)
            notificationService.sendChangeEmailTicketMail(user, ticket)
            flash.message = message(code: "userProfile.confirm.email")
        }

        redirect(action: "index")
    }

    def updateAccountConsents(UpdateUserAccountCommand cmd) {
        def user = getCurrentUser()

        if (cmd.hasErrors()) {
            render(status: HttpStatus.SC_BAD_REQUEST)
            return
        }

        // Newsletters and customerSurveys can be null if this method is call for existing users
        // Those values are only passed along for newly created users.
        if (cmd.newsletters)
            user.receiveNewsletters = cmd.newsletters
        if (cmd.customerSurveys)
            user.receiveCustomerSurveys = cmd.customerSurveys

        user.dateAgreedToTerms = (cmd.acceptTerms ? new Date() : null)

        if (!user.save()) {
            render(status: HttpStatus.SC_BAD_REQUEST)
            return
        }

        render(status: HttpStatus.SC_OK)
    }

    def updateAccount(UpdateUserAccountCommand cmd) {
        def user = getCurrentUser()

        if (cmd.hasErrors()) {
            render(view: "account", model: [user: user, cmd: cmd])
            return
        }

        user.receiveNewsletters = cmd.newsletters
        user.receiveCustomerSurveys = cmd.customerSurveys
        user.searchable = cmd.searchable
        user.matchable = cmd.matchable

        if (!user.hasErrors() && user.save()) {
            flash.message = message(code: "userProfile.updateAccount.success")

            redirect(action: "account")
        } else {
            render(view: "account", model: [user: user, cmd: cmd])
        }
    }

    def upload() {
        log.debug("Spara bild")

        def user = getCurrentUser()

        MultipartHttpServletRequest mpr = (MultipartHttpServletRequest) request;
        CommonsMultipartFile image = (CommonsMultipartFile) mpr.getFile("profileImage");

        try {
            userService.processProfileImage(image, user)
        } catch (IllegalStateException e) {
            flash.error = message(code: "userProfile.upload.error1")
            redirect(action: "index")
            return
        }

        if (user.profileImage && !user.profileImage.hasErrors() && user.save(failOnError: true)) {
            flash.message = message(code: "userProfile.upload.success")
        } else {
            flash.error = message(code: "userProfile.upload.error2")
        }

        redirect(action: "index")
    }

    def uploadWelcome() {
        log.debug("Spara välkomstbild")

        def user = getCurrentUser()

        MultipartHttpServletRequest mpr = (MultipartHttpServletRequest) request;
        CommonsMultipartFile image = (CommonsMultipartFile) mpr.getFile("welcomeImage");

        try {
            userService.processWelcomeImage(image, user)
        } catch (IllegalStateException e) {
            flash.error = message(code: "userProfile.uploadWelcome.error1")
            redirect(action: "index")
            return
        }

        if (user.welcomeImage && !user.welcomeImage.hasErrors() && user.save(failOnError: true)) {
            flash.message = message(code: "userProfile.uploadWelcome.success")
        } else {
            flash.error = message(code: "userProfile.uploadWelcome.error2")
        }

        redirect(action: "index")
    }

    def sportAdd() {
        def user = getCurrentUser()
        def sport = Sport.get(params.sport)

        render(view: "/templates/profile/_sportAdd", model: [user: user, sport: sport])
    }

    def sportEdit() {
        def user = getCurrentUser()

        def sport = Sport.get(params.sport)
        def profile = SportProfile.findByUserAndSport(user, sport)

        render(view: "/templates/profile/_sportEdit", model: [user: user, sport: sport, profile: profile])
    }

    def addSport() {
        def user = getCurrentUser()
        def sport = Sport.findById(params.sport)

        userService.addSportProfile(user, sport, params)
        flash.message = message(code: "userProfile.addSport.success", args: [sport.name])

        redirect(action: "index")
    }

    def removeSport() {
        def sportProfile = SportProfile.get(params.id)
        sportProfile.delete()

        flash.message = message(code: "userProfile.removeSport.success")

        redirect(action: "index")
    }

    def updateSport() {
        def user = getCurrentUser()
        def sport = Sport.findById(params.sport)
        def sp = SportProfile.findByUserAndSport(user, sport)

        userService.updateSportProfile(sp, params)
        flash.message = message(code: "userProfile.updateSport.success", args: [sport.name])

        redirect(action: "index")
    }

    def passwordForFb() {
        [user: getCurrentUser()]
    }

    def setPassword(String password) {
        if (!password) {
            flash.error = message(code: "userProfile.setPassword.error")
            return redirect(action: "passwordForFb")
        }

        def validationError = ValidationUtils.validateUserPassword(password)
        if (validationError) {
            flash.error = message(code: validationError)
            return redirect(action: "passwordForFb")
        }

        def user = getCurrentUser()
        userService.changePassword(password, user)
        redirect(controller: "loginSuccess", action: "index")
    }

    def updateAvailability() {
        def user = getCurrentUser()
        userService.updateAvailability(user, params)

        flash.message = message(code: "userProfile.updateAvailability.success")
        redirect(action: "index")
    }

    def updateMatchable() {
        def user = getCurrentUser()

        user.matchable = true
        user.save(failOnError: true)

        flash.message = message(code: "userProfile.updateMatchable.success")

        if (params.returnUrl && params.returnUrl.size() > 0) {
            redirect url: params.returnUrl
        } else {
            redirect(controller: "userProfile", action: "home")
        }
    }

    def printReceipt() {
        def order = Order.findById(params.id, [fetch: [payments: "join"]])
        def slot = slotService.getSlot(order?.metadata?.slotId)
        User user = order?.customer?.user
        User currentUser = getCurrentUser()

        if (!currentUser.isInRole("ROLE_ADMIN") && (!user || user.id != currentUser.id)) {
            flash.error = message(code: "default.accessDenied")
            return
        } else {
            String template
            String matchiLogoString
            if (!order.article.isSoldByFacility) {
                matchiLogoString = new File('web-app/images/logo-2019-slogan.png').bytes.encodeAsBase64().toString()
                template = "${templateRootURI}/${matchiTemplateName}"
            }
            else {
                template = "${templateRootURI}/${getPrintTemplate(order?.facility)}"
            }
            log.debug("PRINT TEMPLATE: ${templateRootURI}/${template}")

            def pdf = getPdfFile(template, [order: order, activity: slot?.court?.sport, user: user, isSoldByFacility: order.article.isSoldByFacility, matchiLogoString: matchiLogoString])

            render(file: pdf, contentType: 'application/pdf')
        }
    }

    def showOfferHistory(Long id) {
        def customerCoupon = CustomerCoupon.get(id)
        if (!customerCoupon || customerCoupon.customer?.user?.id != getCurrentUser().id) {
            render status: 404
            return
        }

        render template: "/templates/profile/offerHistoryPopup",
                model: [tickets: CustomerCouponTicket.findAllByCustomerCouponAndNrOfTicketsIsNotNull(
                        customerCoupon, [sort: "dateCreated"])]
    }

    private def getAvailableSportProfiles() {
        return Sport.realCoreSports.list()
    }

    private void copyProperties(User user, UpdateUserProfileCommand cmd, Boolean updateUserEmail) {
        user.firstname = cmd.firstname
        user.lastname = cmd.lastname
        if (!updateUserEmail) user.email = cmd.email
        user.telephone = cmd.telephone
        user.gender = cmd.gender ? User.Gender.valueOf(cmd.gender) as User.Gender : null
        user.municipality = Municipality.get(cmd.municipality)

        user.address = cmd.address
        user.zipcode = cmd.zipcode
        user.city = cmd.city
        user.country = cmd.country
        user.nationality = cmd.nationality
        user.language = cmd.language
        user.description = cmd.description
    }

    private String getPrintTemplate(Facility facility) {
        log.debug("Getting receipt printing template for ${facility?.name}")
        def facilityTemplate = groovyPageLocator.findViewByPath("${templateRootURI}/${facility?.shortname}/_receipt")

        if (facilityTemplate) {
            return "${facility?.shortname}/receipt"
        } else {
            log.info("Could not find receipt template for ${facility} in ${templateRootURI}/${facility?.shortname}")
        }

        return defaultTemplateName
    }
}

@Validateable(nullable = true)
class UpdateUserProfileCommand {
    Long id
    String firstname
    String lastname
    String email
    String telephone
    String gender
    int birthYear
    String birthMonth
    int birthDay

    String address
    String zipcode
    String city
    Long municipality
    String country
    String nationality
    String language
    String description

    static constraints = {
        firstname(blank: false, nullable: false, markup: true)
        lastname(blank: false, nullable: false, markup: true)
        email(blank: false, nullable: false, email: true)
        email(validator: { val ->
            return Validator.isMailbox(val)
        })
        telephone(blank: false, markup: true)
        gender(blank: false)
        birthYear(blank: false, min: 1930, max: DateUtil.getCurrentYear())
        birthMonth(blank: false)
        birthDay(blank: false, min: 1, max: 31)
        language(nullable: false, blank: false)
        description(markup: true)
        city(markup: true)
        address(markup: true)
        zipcode(markup: true)
    }


    @Override
    public String toString() {
        return "UpdateUserProfileCommand{" +
                "id=" + id +
                ", firstname='" + firstname + '\'' +
                ", lastname='" + lastname + '\'' +
                ", email='" + email + '\'' +
                ", telephone='" + telephone + '\'' +
                ", gender='" + gender + '\'' +
                ", birthYear=" + birthYear +
                ", birthMonth='" + birthMonth + '\'' +
                ", birthDay=" + birthDay +
                ", address='" + address + '\'' +
                ", zipcode='" + zipcode + '\'' +
                ", city='" + city + '\'' +
                ", municipality=" + municipality +
                ", country='" + country + '\'' +
                ", nationality='" + nationality + '\'' +
                ", language='" + language + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}

@Validateable(nullable = true)
class UpdateUserAccountCommand {
    boolean newsletters
    boolean customerSurveys
    boolean searchable
    boolean matchable
    boolean acceptTerms
}
