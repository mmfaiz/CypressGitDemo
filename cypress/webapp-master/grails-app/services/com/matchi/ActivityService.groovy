package com.matchi

import com.matchi.activities.*
import com.matchi.activities.ActivityOccasion.DELETE_REASON
import com.matchi.activities.trainingplanner.CourseActivity
import com.matchi.conditions.ActivitySlotCondition
import com.matchi.coupon.CustomerCoupon
import com.matchi.coupon.CustomerCouponTicket
import com.matchi.dynamicforms.Form
import com.matchi.dynamicforms.FormField
import com.matchi.dynamicforms.FormFieldValue
import com.matchi.dynamicforms.Submission
import com.matchi.enums.BookingGroupType
import com.matchi.events.EventInitiator
import com.matchi.events.SystemEventInitiator
import com.matchi.facility.FilterOccasionCommand
import com.matchi.idrottonline.ActivityOccasionOccurence
import com.matchi.orders.Order
import com.matchi.payment.ArticleType
import com.matchi.payment.PaymentException
import com.matchi.payment.PaymentMethod
import com.matchi.price.Price
import com.matchi.watch.ClassActivityWatch
import com.matchi.watch.ObjectWatchNotificationService
import grails.transaction.NotTransactional
import grails.transaction.Transactional
import org.apache.commons.lang.RandomStringUtils
import org.hibernate.criterion.CriteriaSpecification
import org.joda.time.DateTime
import org.joda.time.LocalDate
import org.joda.time.LocalTime

class ActivityService {
    static transactional = false
    def bookingService
    def notificationService
    def smsService
    def couponService
    def editParticipantService
    def messageSource
    def grailsApplication

    CustomerService customerService
    PaymentService paymentService
    SeasonService seasonService
    ObjectWatchNotificationService objectWatchNotificationService
    OrderStatusService orderStatusService

    /**
     * Finds all activities bound to a given facility
     * @return A list of activities
     */
    def getActivitiesByFacility(def facility, boolean archived) {
        ClassActivity.createCriteria().listDistinct {
            eq("facility", facility)
            eq("archived", archived)
            ne("deleted", true)
        }
    }

    /**
     * Finds all activities globally by search filters.
     * @return Map of activity occasions by date and activity.
     */
    Map<LocalDate, Map<ClassActivity, List<ActivityOccasion>>> searchForClassActivityOccasionsByDateAndActivity(Collection<Long> sportIds, LocalDate startDate, LocalDate endDate, String locationSearch, String querySearch, Integer level) {
        List<ActivityOccasion> occasions = searchForClassActivityOccasions(sportIds, new ArrayList<Long>(), startDate, endDate, locationSearch, querySearch, level)

        Map<LocalDate, Map<ClassActivity, List<ActivityOccasion>>> occasionsByActivityByDate = new LinkedHashMap<LocalDate, Map<ClassActivity, List<ActivityOccasion>>>()
        occasions.each { occasion ->
            if (!occasionsByActivityByDate[occasion.date]) {
                occasionsByActivityByDate[occasion.date] = new LinkedHashMap<ClassActivity, List<ActivityOccasion>>()
            }
            if (!occasionsByActivityByDate[occasion.date][occasion.activity]) {
                occasionsByActivityByDate[occasion.date][occasion.activity] = new ArrayList<ActivityOccasion>()
            }
            occasionsByActivityByDate[occasion.date][occasion.activity].push(occasion)
        }
        return occasionsByActivityByDate
    }

    List<ActivityOccasion> searchForClassActivityOccasions(Collection<Long> sportIds, Collection<Long> facilityIds, LocalDate startDate, LocalDate endDate,
                                                           String locationSearch, String querySearch, Integer level) {
        if (facilityIds && !facilityIds.isEmpty()) {
            return searchForActivityOccasionsForFacilities(sportIds, facilityIds, startDate, endDate, querySearch, level)
        } else {
            return searchForClassActivityOccasionsByLocation(sportIds, startDate, endDate, locationSearch, querySearch, level)
        }
    }

    boolean isActivityOccasionBookableForUser(ActivityOccasion occasion) {
        if (isUserParticipating(occasion)) {
            return false
        }
        if (occasion.membersOnly) {
            User user = occasion.JSONoptions?.bookingUser
            Facility facility = occasion.activity.facility
            Customer customer = customerService.findUserCustomer(user, facility)
            if (!customer || !customer.hasMembership()) {
                return false
            }
        }
        return occasion.isUpcomingOnlineOccasion() && !occasion.isFull()
    }

    boolean isUserParticipating(ActivityOccasion occasion) {
        User user = occasion.JSONoptions?.bookingUser
        Facility facility = occasion.activity.facility
        Customer customer = customerService.findUserCustomer(user, facility)
        if (!customer) {
            return false
        }

        return occasion.participations.collect { it.customer }.contains(customer)
    }
    /**
     * * Finds all activities globally by search filters.
     * @return A list of activity occasions.
     */
    private List<ActivityOccasion> searchForClassActivityOccasionsByLocation(Collection<Long> sportIds, LocalDate startDate, LocalDate endDate,
                                                                             String locationSearch, String querySearch, Integer level) {
        List<ActivityOccasion> activityOccasions = ActivityOccasion.createCriteria().listDistinct {
            createAlias("activity", "a")
            eq("a.class", "class_activity")
            eq("availableOnline", true)

            startDate = startDate ?: new LocalDate()
            if (!endDate) {
                endDate = startDate.plusDays(0)
            }

            gte("date", startDate)
            lte("date", endDate)

            createAlias("a.facility", "f")

            if (locationSearch) {
                createAlias("f.municipality", "m")
                createAlias("m.region", "r")
                //activities with courts on facilities in location. Get location from in same way as venue search page with locationSearch
                or {
                    like("f.name", "%${locationSearch}%")
                    like("f.city", "%${locationSearch}%")
                    like("f.address", "%${locationSearch}%")
                    like("f.telephone", "%${locationSearch}%")
                    like("f.zipcode", "%${locationSearch}%")
                    like("f.email", "%${locationSearch}%")
                    like("f.description", "%${locationSearch}%")
                    like("m.name", "%${locationSearch}%")
                    like("r.name", "%${locationSearch}%")
                }
            }

            eq("f.bookable", true)
            eq("f.active", true)


            if (querySearch) {
                //Get Activities including query in Title OR Description
                or {
                    like("a.description", "%${querySearch}%")
                    like("a.name", "%${querySearch}%")
                    like("a.teaser", "%${querySearch}%")
                }
            }

            if (level) {
                and {
                    lte("a.levelMin", level)
                    gte("a.levelMax", level)
                }
            }

            //Get activities belonging to a court with sportId in sportIds
            if (sportIds) {
                createAlias("bookings", "b")
                createAlias("b.slot", "s")
                createAlias("s.court", "c")
                createAlias("c.sport", "sport")

                inList("sport.id", sportIds)
            }

            //Sort on ocassion startTime
            order("date", "ASC")
            order("startTime", "ASC")
        } as List<ActivityOccasion>

        return activityOccasions.findAll { it.isUpcomingOnlineOccasion() }
    }

    private List<ActivityOccasion> searchForActivityOccasionsForFacilities(
            Collection<Long> sportIds, Collection<Long> facilityIds,
            LocalDate startDate, LocalDate endDate,
            String querySearch, Integer level) {
        List<ActivityOccasion> activityOccasions = ActivityOccasion.createCriteria().listDistinct {
            createAlias("activity", "a")
            eq("a.class", "class_activity")
            eq("availableOnline", true)

            startDate = startDate ?: new LocalDate()
            if (!endDate) {
                endDate = startDate.plusDays(0)
            }
            gte("date", startDate)
            lte("date", endDate)

            createAlias("a.facility", "f")
            if (facilityIds) {
                inList("f.id", facilityIds)
            }

            if (querySearch) {
                //Get Activities including query in Title OR Description
                or {
                    like("a.description", "%${querySearch}%")
                    like("a.name", "%${querySearch}%")
                    like("a.teaser", "%${querySearch}%")
                }
            }

            if (level) {
                and {
                    lte("a.levelMin", level)
                    gte("a.levelMax", level)
                }
            }

            //Get activities belonging to a court with sportId in sportIds
            if (sportIds) {
                createAlias("bookings", "b")
                createAlias("b.slot", "s")
                createAlias("s.court", "c")
                createAlias("c.sport", "sport")

                inList("sport.id", sportIds)
            }
        } as List<ActivityOccasion>

        return activityOccasions.findAll { it.isUpcomingOnlineOccasion() }
    }
    /**
     * Finds all active (eg not archived) activities bound to a given facility
     * @return A list of activities
     */
    def getActiveClassActivitiesByFacility(def facility) {
        def activities = ClassActivity.createCriteria().listDistinct {
            eq("facility", facility)
            ne("archived", true)
            ne("deleted", true)
        }
        return activities
    }

    /**
     * Creates an occasion on a given activity
     *  - makes the bookings of resources
     *  - saves the occasion
     * @return The created occasion
     */
    @Transactional
    def createOccasion(ClassActivity activity, LocalDate date, LocalTime startTime, LocalTime endTime,
                       def slots, def customer, def price, def maxNumParticipants, def message,
                       boolean availableOnline = false, Integer signUpDaysInAdvanceRestriction = null, Integer signUpDaysUntilRestriction = null, boolean membersOnly = false, Map comments = null,
                       Integer minNumParticipants = null, Integer cancelHoursInAdvance = null) {

        ActivityOccasion occasion = new ActivityOccasion()

        // creates booking linked to activity occasion
        Collection<Booking> bookings = createOccasionBookings(slots, customer, comments)

        // sets properties
        occasion.price = price
        occasion.activity = activity
        occasion.date = date
        occasion.startTime = startTime
        occasion.endTime = endTime
        occasion.message = message
        occasion.maxNumParticipants = maxNumParticipants
        occasion.availableOnline = availableOnline
        occasion.signUpDaysInAdvanceRestriction = signUpDaysInAdvanceRestriction
        occasion.signUpDaysUntilRestriction = signUpDaysUntilRestriction
        occasion.membersOnly = membersOnly

        // We need both
        if (minNumParticipants && cancelHoursInAdvance) {
            occasion.minNumParticipants = minNumParticipants
            occasion.setCancellationDateTime(cancelHoursInAdvance)
        }

        log.info("Bookings: " + bookings.size())
        // add booking to occasions
        bookings.each { occasion.addToBookings((Booking) it) }

        // add and save
        occasion.activity = activity
        activity.addToOccasions(occasion)
        activity.save()
    }

    /**
     * Removes an activity occasion
     * @param occasion
     */
    def removeOccasion(ActivityOccasion occasion, Closure notificationHandler = participantRemovalNotificationHandler, DELETE_REASON deleteReason) {
        def activityName = occasion.activity.name
        def bookings = occasion.bookings.collect()

        ActivityOccasion.withTransaction {
            occasion.lock()

            // remove all participations
            def participants = []
            participants.addAll(occasion.participations)
            participants.each { Participation participant ->
                removeParticipant(participant, notificationHandler)
            }
            occasion.participations.clear()

            // remove all bookings
            bookings.each { booking ->
                occasion.removeFromBookings(booking)
            }

            occasion.setDeleted(deleteReason)
            occasion.save(flush: true)
        }

        Booking.withTransaction {
            bookings.each {
                bookingService.cancelActivityOccasionBooking(it,
                        messageSource.getMessage("classActivity.cancelled.notification", [activityName] as String[], new Locale(it.facility.language)))
            }
        }
    }

    /**
     * Creates bookings to be linked to an activity occasion
     * @param slots the slots that is to be booked
     * @param user The booking user
     */
    @Transactional
    def createOccasionBookings(def slots, def customer, def comments = null) {

        // create group to collect all bookings in activity occasion

        BookingGroup group = new BookingGroup()
        group.type = BookingGroupType.ACTIVITY
        group.comment = "ACTIVITY"
        group.save()

        slots.each {
            CreateBookingCommand createBookingCommand = new CreateBookingCommand()
            createBookingCommand.customerId = customer.id
            createBookingCommand.slotId = it.id
            if (comments) {
                createBookingCommand.comments = comments.comment
                createBookingCommand.showComment = comments.showComment
            }
            def booking = bookingService.makeBooking(createBookingCommand, false)
            group.addToBookings(booking)
        }

        group.bookings
    }

    /**
     * Adds a participant to an activity occasion
     * @param occasion The occasion
     * @param user The participant
     */
    @Transactional
    def addParticipant(ActivityOccasion occasion, Customer customer, Payment payment = null, Order order = null) {
        log.info("Adding ${customer.fullName()} to occasion ${occasion.activity.name} at ${occasion.date}")
        log.info("Removing ${customer.fullName()} from activity queue if exists")

        ClassActivityWatch.findByUserAndClassActivityAndFromDate(customer.user,
                occasion.activity, occasion.date.toDateTime(occasion.startTime).toDate())?.delete()

        def existing = findParticipant(occasion, customer)

        if (payment) {
            payment.dateDelivery = occasion.getStartDateTime().toDate()
            payment.save()
        }

        if (!existing) {
            Participation participation = new Participation()
            participation.customer = customer
            participation.joined = new DateTime()
            participation.payment = payment
            participation.order = order
            occasion.addToParticipations(participation)
            occasion.save()

            notificationService.sendNewParticipation(participation)

            if (getActivityEmail(participation) && participation?.occasion?.activity?.notifyWhenSignUp) {
                notificationService.sendNewParticipationToAdmin(participation)
            }

            return participation
        } else {
            return existing
        }
    }

    /**
     * Find a participation bases on customer and occasion
     * @return A participation if found otherwise null
     */
    def findParticipant(ActivityOccasion occasion, Customer customer) {
        return Participation.findByOccasionAndCustomer(occasion, customer)
    }

    /**
     * Removes a participant to an activity occasion, refunds the customer if needed
     * @param occasion The occasion
     * @param user The participant
     */
    @Transactional
    def removeParticipant(ActivityOccasion occasion, Customer customer) {
        log.info("Removing ${customer.fullName()} from occasion ${occasion.activity.name} at ${occasion.date}")

        // find participation
        removeParticipant(Participation.findByOccasionAndCustomer(occasion, customer))
    }

    private final Closure participantRemovalNotificationHandler = { Participation participation ->
        // notify
        notificationService.sendParticipationCancelledNotification(participation)

        // notify activity leader
        if (getActivityEmail(participation) && participation?.occasion?.activity?.notifyWhenCancel) {
            notificationService.sendParticipationLeadCancelledNotification(participation)
        }
    }

    /**
     * Removes a participant to an activity occasion
     * @param occasion The occasion
     * @param user The participant
     */
    @Transactional
    def removeParticipant(Participation participation, Closure notificationHandler = participantRemovalNotificationHandler) {

        if (participation) {
            if (participation.payment) {
                throw new IllegalArgumentException("""Participation ${participation.id} still has a payment, annul or
                    reverse the payment before removing the participation""")
            }

            if (notificationHandler) {
                notificationHandler.call(participation)
            }

            // remove participant
            participation.occasion.removeFromParticipations(participation)
            participation.delete()
        }
    }

    def cancelAndRefundParticipant(ActivityOccasion occasion, User user) {
        if (!user) {
            return
        }

        Participation participation = occasion.getParticipation(user)

        if (!occasion.activity.cancelByUser) {
            return messageSource.getMessage('payment.cancel.forbidden.error', null, new Locale(user.language))
        }

        log.info("User ${user.email} cancelled participation in activity occasion ${occasion.id}")
        if (participation.payment) {
            try {
                participation.payment = null
                participation.save(failOnError: true)

            } catch (PaymentException pe) {
                //exit
                return messageSource.getMessage("activityPayment.cancel.error1", null, new Locale(user.language))
            }
        }

        def order = participation.order
        if (order) {
            def refunded = Order.withTransaction { status ->
                try {
                    if (participation.isRefundable() && order.isStillRefundable()) {

                        def amountCredit = order.total().minus(
                                getServiceFee(occasion))
                        orderStatusService.annul(order, user, "Activity cancel (fee)", amountCredit)
                    } else {
                        log.info("Participant order not refundable at this time, skipping")
                        orderStatusService.annul(order, user)
                    }
                    return true
                } catch (e) {
                    //exit
                    status.setRollbackOnly()
                    log.error "Unable to refund participation order $participation.order.id - $e.message"
                    return false
                }
            }
            if (!refunded) {
                return messageSource.getMessage("activityPayment.cancel.error1", null, new Locale(user.language))
            }
        }

        removeParticipant(participation)

        if (occasion?.participations?.size() < occasion?.maxNumParticipants) {
            objectWatchNotificationService.sendActivityNotificationsFor(occasion.id)
        }

        return true
    }

    @NotTransactional
    def getServiceFee(ActivityOccasion occasion) {
        return new Amount(amount: grailsApplication.config.matchi.settings.currency[occasion.activity.facility.currency].serviceFee, VAT: 2.5).amount
    }

    def getServiceFeeWithCurrency(ActivityOccasion occasion) {
        return getServiceFee(occasion) + " " + occasion.activity.facility.currency
    }

    /**
     * Refunds a customer payment for a participation in a activity occasion
     * @param occasion The occasion
     * @param user The user
     */
    @Transactional
    void tryAnnulParticipantPayment(Participation participation, EventInitiator eventInitiator, Boolean forceRefund = false) {
        log.info("Checking if user ${participation.customer.fullName()} is valid for refund")

        // check user has payment on activity occasion
        if (participation.payment) {
            def payment = participation.payment

            if (payment.method.equals(PaymentMethod.COUPON)) {
                couponService.refundCustomerCoupon(CustomerCoupon.get(payment.couponId),
                        CustomerCouponTicket.Type.ACTIVITY_REFUND)
            }

            payment.dateAnnulled = new Date()
            participation.payment = null
            participation.save()
        } else if (participation.order) {
            if ((forceRefund || participation.isRefundable()) && participation.order.isStillRefundable()) {
                orderStatusService.annul(participation.order, eventInitiator, "Activity cancel (no fee)", participation.order.total())
            } else {
                log.info("Participant order not refundable at this time, skipping")
                orderStatusService.annul(participation.order, eventInitiator)
            }
        } else {
            log.info("Customer (${participation.customer.fullName()}) has no payment on participation, skipping annul...")
        }

    }

    boolean isDeletable(ClassActivity activity) {
        return activity.occasions.every {it -> it.isPast() || it.isDeleted()}
    }

    def delete(ClassActivity activity) {
        if (activity.occasions.size() > 0) {
            // Only mark activity as deleted if there are past occasions
            softDelete(activity)
        } else {
            // Actually delete the activity from database if there are no occasions
            hardDelete(activity)
        }
    }

    @Transactional
    def hardDelete(ClassActivity activity) {
        def inConditions = ActivitySlotCondition.createCriteria().listDistinct {
            activities {
                eq("id", activity.id)
            }
        }

        log.debug("In conditions: ${inConditions.size()}")

        if (inConditions.size() > 0) {
            def conditions = []
            conditions.addAll(inConditions)

            conditions.each {
                it.removeFromActivities(activity)
            }
        }

        List<ActivityOccasion> occasions = []
        occasions.addAll(activity.occasions)

        occasions.each { ActivityOccasion occasion ->
            removeOccasion(occasion, {}, DELETE_REASON.MANUAL_WITH_REFUND)
        }

        ClassActivityWatch.findAllByClassActivity(activity)*.delete()

        activity.delete()
    }

    @Transactional
    def softDelete(ClassActivity activity) {
        activity.deleted = true
        activity.save()
    }

    @Transactional
    def archive(ClassActivity activity) {
        activity.archived = true
        activity.save()
    }

    def getOccasionsByActivity(def activity) {
        def occasions = ActivityOccasion.createCriteria().listDistinct {
            eq("activity", activity)
            order("date", "asc")
            order("startTime", "asc")
        }
        return occasions
    }


    def getFinishedOccasions(def occasions) {
        return occasions.findAll {
            it.isFinished()
        }
    }

    def getUpcomingOccasions(def occasions) {
        return occasions.findAll {
            it.isUpcoming()
        }
    }

    List<ClassActivity> getActiveActivitiesWithOccasions(Facility facility) {
        ClassActivity.createCriteria().listDistinct {
            createAlias("occasions", "o", CriteriaSpecification.LEFT_JOIN)
            eq("facility", facility)
            ne("archived", true)

            sqlRestriction("timestamp(o1_.date,o1_.end_time) >= now()",)

            ne("o.fieldDeleted", true)

            order("o.date", "asc")
            order("o.startTime", "asc")
        }
    }

    def getOccasionPaymentStatus(ActivityOccasion occasion) {
        def result = [:]

        occasion.participations.each { participant ->
            result.put participant, participant.payment
        }
        return result
    }

    /**
     * Searches for all activitiy occasion withing the given slots
     * @param slots Slot to search
     * @return Map<Slot, Occasion>   containing all found occasions
     */
    def getOccasionsBySlots(def slots) {

        if (!slots || slots.isEmpty()) {
            return []
        }

        def occasions = ActivityOccasion.createCriteria().listDistinct {
            createAlias("bookings", "bookings", CriteriaSpecification.LEFT_JOIN)
            createAlias("bookings.slot", "slot", CriteriaSpecification.LEFT_JOIN)

            inList("bookings.slot", slots)

            order("date", "asc")
            order("startTime", "asc")
        }

        // group result by slot
        def result = [:]
        occasions.each { occasion ->
            occasion.bookings.each { booking ->
                result.put booking.slot, occasion
            }
        }
        return result
    }

    /**
     * Get all occasions for given activities
     * @param Course activity id's
     * @return Map<Long, List<ActivityOccasion>  >
     */
    def getCourseOccasions(List<Long> activityIds) {
        def occasions = ActivityOccasion.createCriteria().listDistinct {
            createAlias("participants", "p", CriteriaSpecification.LEFT_JOIN)
            createAlias("activity", "a")

            inList("activity.id", activityIds)

            order("court", "asc")
            order("startTime", "asc")
        }

        return occasions.groupBy { it.court.id }
    }

    Map<Integer, List<ActivityOccasion>> findCourseOccasionsByHourOfDay(FilterOccasionCommand filter, Facility facility, Date defaultStartDate = null) {
        List<ActivityOccasion> occasions = findCourseOccasions(filter, facility, defaultStartDate)
        return occasions ? occasions.groupBy { it.startTime.hourOfDay } : [:]
    }

    List<ActivityOccasionOccurence> findCourseOccasionByRange(Facility facility, LocalDate startDate, LocalDate endDate) {
        if (endDate.isBefore(startDate)) return []

        List<ActivityOccasionOccurence> activityOccasionOccurences = new ArrayList<ActivityOccasionOccurence>()
        LocalDate currentDate = startDate
        while (!currentDate.isAfter(endDate)) {
            activityOccasionOccurences.addAll(findCourseOccasionBySpecificDate(facility, currentDate))
            currentDate = currentDate.plusDays(1)
        }
        activityOccasionOccurences
    }

    List<ActivityOccasionOccurence> findCourseOccasionBySpecificDate(Facility facility, LocalDate specificDate) {
        FilterOccasionCommand filter = new FilterOccasionCommand()
        Season season = seasonService.getSeasonByDate(specificDate.toDate(), facility)
        if (season) filter.seasonIds = [season.id]

        List<ActivityOccasion> occasions = findCourseOccasions(filter, facility, specificDate.toDate())
        occasions.findAll { it.day() == specificDate.getDayOfWeek() && it.activity?.startDate <= specificDate.toDate() && it.activity?.endDate >= specificDate.toDate() }.collect {
            new ActivityOccasionOccurence(specificDate, it)
        }
    }

    List<ActivityOccasion> findCourseOccasions(FilterOccasionCommand filter, Facility facility, Date defaultStartDate = null) {
        List<ActivityOccasion> occasions = ActivityOccasion.createCriteria().listDistinct {
            createAlias("activity", "a")
            eq("a.class", "course_activity")
            eq("a.facility", facility)
            if (filter.courseIds) {
                inList("a.id", filter.courseIds)
            }
            if (filter.seasonIds) {
                or {
                    Season.findAllByIdInList(filter.seasonIds).each { s ->
                        and {
                            gte("a.endDate", s.startTime)
                            lte("a.startDate", s.endTime)
                        }
                        or {
                            //the course starts before season, ends mid season
                            and {
                                and {
                                    gte('a.endDate', s.startTime)
                                    lte('a.endDate', s.endTime)
                                }
                                and {
                                    lte('a.startDate', s.startTime)
                                    lte('a.startDate', s.endTime)
                                }
                            }
                            //the course starts mid season, ends after season
                            and {
                                and {
                                    gte('a.endDate', s.startTime)
                                    gte('a.endDate', s.endTime)
                                }
                                and {
                                    gte('a.startDate', s.startTime)
                                    gte('a.startDate', s.endTime)
                                }
                            }
                            //the course starts before season, ends after season
                            and {
                                and {
                                    gte('a.endDate', s.startTime)
                                    gte('a.endDate', s.endTime)
                                }
                                and {
                                    lte('a.startDate', s.startTime)
                                    lte('a.startDate', s.endTime)
                                }
                            }
                            //the course is mid season
                            and {
                                and {
                                    gte('a.endDate', s.startTime)
                                    lte('a.endDate', s.endTime)
                                }
                                and {
                                    gte('a.startDate', s.startTime)
                                    lte('a.startDate', s.endTime)
                                }
                            }

                        }
                    }
                }
            } else if (defaultStartDate) {
                gte("a.endDate", defaultStartDate)
            }
            if (filter.trainerIds) {
                trainers {
                    inList('id', filter.trainerIds)
                }
            }

            order("startTime", "asc")
            order("court", "asc")
        }

        return occasions
    }

    def getParticipants(ActivityOccasion occasion) {
        return Participation.createCriteria().listDistinct {
            createAlias("customer", "c", CriteriaSpecification.LEFT_JOIN)
            eq("occasion", occasion)
            order("c.lastname", "asc")
        }
    }

    Amount getPriceForActivityOccasion(User user, ActivityOccasion occasion) {
        Amount amount = new Amount()
        def price = new BigDecimal(occasion.price)
        amount.amount = price

        if (price > 0) {
            amount.VAT = Price.calculateVATAmount(occasion.price, new Double(occasion.activity.facility.vat ?: 0)).round(2).toBigDecimal()
        } else {
            amount.VAT = 0
        }

        return amount
    }

    @Transactional
    def createPaymentOrder(User user, ActivityOccasion occasion) {
        PaymentOrder order = new PaymentOrder()
        order.articleType = ArticleType.ACTIVITY
        order.user = user
        order.facility = occasion.activity.facility

        order.orderParameters = new HashMap()
        order.orderParameters.put("occasionId", occasion.id.toString())
        order.orderDescription = occasion.createOrderDescription(user)
        order.orderNumber = occasion.createOrderNumber(user) + "-" + RandomStringUtils.random(5, true, false)

        order.price = occasion.price
        order.vat = Price.calculateVATAmount(occasion.price.toLong(), occasion.activity.facility.vat ?: 0)

        if (occasion.price == 0) {
            order.method = PaymentMethod.FREE
        }

        order.save(failOnError: true)
        return order
    }

    @Transactional
    Order createActivityPaymentOrder(User user, ActivityOccasion activityOccasion,
                                     String origin = Order.ORIGIN_WEB, User issuer = null) {

        Order order = new Order()
        order.issuer = issuer ?: user
        order.user = user
        order.dateDelivery = activityOccasion.getStartDateTime().toDate()
        order.facility = activityOccasion.activity.facility
        order.origin = origin
        order.description = activityOccasion.createOrderDescription(user)
        order.price = activityOccasion.price
        order.vat = Price.calculateVATAmount(activityOccasion.price.toLong(), activityOccasion.activity.facility.vat ?: 0)
        order.article = Order.Article.ACTIVITY
        order.metadata = [activityOccasionId: activityOccasion.id.toString()]

        order.save(failOnError: true)
    }

    @Transactional
    def book(Order order) {
        ActivityOccasion occasion = ActivityOccasion.get(order.metadata?.activityOccasionId)

        if (occasion) {
            addParticipant(occasion, order.customer, null, order)
        } else {
            throw new IllegalStateException("Could not find occasion on order ${order?.id}")
        }
    }

    List getCurrentAndUpcomingActivities(Class clazz, Facility facility,
                                         FormField.Type formFieldType = null) {
        clazz.createCriteria().listDistinct {
            eq("facility", facility)
            ge("endDate", new Date().clearTime())
            if (formFieldType) {
                form {
                    fields {
                        eq("type", formFieldType.name())
                    }
                }
            }
            order("name", "asc")
        }
    }

    List getCoursesWithPublishedForm(Facility facility) {
        def today = new Date().clearTime()

        CourseActivity.withCriteria {
            createAlias("form", "f")
            eq("facility", facility)
            le("f.activeFrom", today)
            ge("f.activeTo", today)
            eq("showOnline", true)
            order("listPosition", "asc")
        }
    }

    @Transactional
    void copyCourse(cmd) {
        def srcCourse = CourseActivity.get(cmd.srcCourseId)

        def newCourse = new CourseActivity(facility: srcCourse.facility, name: cmd.name,
                startDate: cmd.startDate, endDate: cmd.endDate, description: srcCourse.description,
                hintColor: srcCourse.hintColor)

        def form = new Form(name: srcCourse.form.name, description: srcCourse.form.description,
                activeFrom: cmd.activeFrom, activeTo: cmd.activeTo,
                relatedFormTemplate: srcCourse.form.relatedFormTemplate,
                facility: newCourse.facility)

        def fields = (srcCourse.form.relatedFormTemplate && !cmd.copySettings) ?
                srcCourse.form.relatedFormTemplate.templateFields : srcCourse.form.fields
        fields.each {
            def formField = new FormField(it.properties)
            formField.predefinedValues = []
            it.predefinedValues.each { pv ->
                formField.addToPredefinedValues(new FormFieldValue(pv.properties))
            }
            formField.form = form
            formField.template = null
            form.addToFields(formField)
        }

        if (cmd.copySettings) {
            form.maxSubmissions = srcCourse.form.maxSubmissions
            form.membershipRequired = srcCourse.form.membershipRequired
            form.paymentRequired = srcCourse.form.paymentRequired
            form.price = srcCourse.form.price
        }

        form.save(failOnError: true)
        newCourse.form = form

        if (cmd.copyTrainers) {
            srcCourse.trainers.each {
                newCourse.addToTrainers(it)
            }
        }

        if (cmd.copyParticipants) {
            srcCourse.participants.each {
                def submission
                if (it.submission) {
                    submission = editParticipantService.copySubmission(it.submission, newCourse)
                }

                newCourse.addToParticipants(new Participant(status: it.status,
                        customer: it.customer, submission: submission))
            }
        }

        if (cmd.copyWaitingSubmissions) {
            srcCourse.form.submissions.findAll {
                it.status == Submission.Status.WAITING
            }.each {
                editParticipantService.copySubmission(it, newCourse)
            }
        }

        if (cmd.copyOccasions) {
            srcCourse.occasions.each { oc ->

                def occasion = new ActivityOccasion(message: oc.message,
                        startTime: oc.startTime, endTime: oc.endTime, court: oc.court,
                        date: new LocalDate(newCourse.startDate).plusWeeks(1).withDayOfWeek(oc.date.dayOfWeek().get()))
                if (cmd.copyTrainers) {
                    oc.trainers.each { tr ->
                        occasion.addToTrainers(tr)
                    }
                }
                if (cmd.copyParticipants) {
                    oc.participants.each { p ->
                        if (p.activity.id == srcCourse.id) {
                            def newp = newCourse.participants.find { it.customer.id == p.customer.id }
                            if (!newp) {
                                newp = new Participant(status: p.status, customer: p.customer)
                                newCourse.addToParticipants(newp)
                            }
                            occasion.addToParticipants(newp)
                        } else {
                            occasion.addToParticipants(p)
                        }
                    }
                }
                newCourse.addToOccasions(occasion)
            }
        }

        newCourse.save(failOnError: true)
    }

    String getActivityEmail(Participation participation) {
        return ClassActivity.get(participation.occasion?.activityId)?.email
    }

    @Transactional
    void swapListPosition(CourseActivity course1, CourseActivity course2) {
        def pos1 = course1.listPosition
        course1.listPosition = course2.listPosition
        course1.save()
        course2.listPosition = pos1
        course2.save()
    }

    @NotTransactional
    Participation getUserParticipation(Long id, User user) {
        if (!user) {
            throw new IllegalArgumentException("User cannot be null")
        }

        Participation participation = Participation.get(id)

        return (participation.customer.user == user) ? participation : null
    }

    /**
     * Returns user's upcoming activity participations.
     * Defaults to looking from now and at all facilities
     * @param user
     * @param onlySpareSlotBookings
     * @param facilities
     * @return
     */
    @NotTransactional
    List<Participation> getUserUpcomingParticipations(final User user,
                                                      final LocalDate fromDate = new LocalDate(),
                                                      final LocalTime fromTime = new LocalTime(),
                                                      final List<Facility> facilities = []) {

        if (!user) {
            throw new IllegalArgumentException("User cannot be null")
        }

        final List<Long> facilityIds = facilities?.collect { it.id }

        return Participation.createCriteria().list {
            createAlias("customer", "c")
            createAlias("occasion", "o")

            if (facilityIds) {
                inList("c.facility.id", facilityIds)
            }

            or {
                gt('o.date', fromDate)
                and {
                    eq('o.date', fromDate)
                    gt('o.endTime', fromTime)
                }
            }

            eq('c.user', user)
        }.sort { Participation p ->
            return p.occasion.getStartDateTime()
        }
    }

    List<EventActivity> getOnlineEvents(Facility facility) {
        def today = new Date().clearTime()

        EventActivity.withCriteria {
            createAlias("form", "f")
            eq("facility", facility)
            le("f.activeFrom", today)
            ge("f.activeTo", today)
            eq("showOnline", true)
            order("name", "asc")
        }
    }

    /**
     * Returns occasions that had its last open application date on supplied date
     * @param date
     * @return
     */
    List<ActivityOccasion> getOccasionsToCancelByTooFewParticipants(DateTime now) {
        if (now == null) {
            throw new IllegalArgumentException("now cannot be null")
        }

        // Get cancellable ActivityOccasions that are within their cancellation span
        return ActivityOccasion.createCriteria().list {
            isNotNull('minNumParticipants')
            lte('automaticCancellationDateTime', now)
            sqlRestriction("concat({alias}.date, ' ', {alias}.start_time) > ?", [now.toString(DateUtil.DATE_AND_TIME_FORMAT)])
            sqlRestriction("(SELECT count(*) FROM participation WHERE occasion_id = {alias}.id) < min_num_participants")
            ne("fieldDeleted", true)
        }
    }
    void cancelOccasionWithFullRefundAutomatically(ActivityOccasion activityOccasion, DELETE_REASON deleteReason) {
        if (!activityOccasion.canNowBeCancelledAutomatically()) {
            throw new IllegalArgumentException("ActivityOccasion cannot be automatically cancellable")
        }

        Closure notificationHandler = { Participation participation ->
            notificationService.sendParticipationCancelledNotification(
                    participation,
                    notificationService.AUTOMATIC_ACTIVITY_CANCELLATION_PARTIPICATION_TEMPLATE
            )
            smsService.sendParticipationCancelledSMS(participation)
        }

        this.cancelOccasionWithFullRefund(activityOccasion, notificationHandler, deleteReason)
        notificationService.sendAutomaticCancellationNotificationToLeader(activityOccasion)
    }

    void cancelOccasionWithFullRefundManually(ActivityOccasion activityOccasion) {
        this.cancelOccasionWithFullRefund(activityOccasion, DELETE_REASON.MANUAL_WITH_REFUND)
    }

    private void cancelOccasionWithFullRefund(ActivityOccasion activityOccasion, Closure notificationHandler = participantRemovalNotificationHandler, DELETE_REASON deleteReason) {
        List<Order> orders = activityOccasion.participations*.order.findAll { Order order -> order != null }


        orders.each { Order order ->
            if (order.isStillRefundable()) {
                orderStatusService.annul(order, new SystemEventInitiator(), "Automatic cancellation on too few participants", order.total())
            }
        }

        // If orders do fine, remove the occasion
        removeOccasion(activityOccasion, notificationHandler, deleteReason)
    }
}
