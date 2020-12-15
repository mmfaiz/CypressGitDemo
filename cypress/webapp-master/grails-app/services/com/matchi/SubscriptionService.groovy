package com.matchi

import com.matchi.facility.FacilitySubscriptionFilterCommand
import com.matchi.orders.Order
import com.matchi.subscription.SubscriptionCopyInfo
import grails.transaction.NotTransactional
import groovyx.gpars.GParsPool
import org.apache.commons.lang.StringEscapeUtils as SEU
import org.hibernate.criterion.CriteriaSpecification
import org.joda.time.DateTime
import org.joda.time.LocalTime
import org.springframework.util.StopWatch

class SubscriptionService {

    static transactional = true
    def bookingAvailabilityService
    def slotService
    UserService userService
    def memberService
    def customerService
    def bookingService
    def springSecurityService
    def dateUtil
    def groovySql
    def courtRelationsBookingService
    def priceListService
    def mpcService
    def grailsApplication
    OrderStatusService orderStatusService

    def createSubscriptions(FacilityBookingCommand cmd, List playerCustomers) {
        log.debug("Processing subscriptions on ${cmd}")
        def customer = bookingService.getBookingCustomer(cmd)

        def slotIds = cmd.slotIds()
        def slots = slotService.getSlots(slotIds)

        if( slots.get(0).belongsToSubscription()) {
            Booking booking = slots.get(0)?.booking

            if (cmd.updateGroup) { // Update all subscription slots
                log.debug("Update all bookings in subscription")

                Subscription subscription = slots.get(0).subscription
                subscription.bookingGroup?.bookings?.each { Booking b ->
                    b.comments = cmd.comments
                    b.showComment = cmd.showComment
                    b.save()
                }

                if(booking) {
                    booking.save()
                }

            } else { // Update only chosen subscription slot
                log.debug("Update only chosen booking")

                if (booking) {
                    updateBooking(booking.id, cmd, playerCustomers)
                }
            }


            return true;
        }

        def interval = cmd.interval?:1
        def startingDate = new DateTime(cmd.recurrenceStart)
        def weekDays = cmd.weekDays ?: [ startingDate.dayOfWeek ]

        weekDays.each {
            slots.each { Slot slot ->
                log.debug("Creating subscription on day: ${startingDate.dayOfWeek}")
                Subscription subscription = createSubscription(cmd.comments, cmd.showComment,
                        startingDate, new DateTime(cmd.recurrenceEnd), slot, it, interval, customer)
                log.debug("Subscription created...")
                subscription.save(failOnError: true)

                createBookingsOrders(subscription)
                subscription.bookingGroup.bookings.each {
                    it.addPlayers(playerCustomers)
                    courtRelationsBookingService.tryBookRelatedCourts(it)
                }
            }
        }

        return false
    }

    private void updateBooking(Long bookingId, FacilityBookingCommand cmd, List playerCustomers = null) {
        Booking booking = Booking.get(bookingId)
        booking.comments = cmd.comments
        booking.showComment = cmd.showComment
        if (playerCustomers) {
            booking.updatePlayers(playerCustomers)
        }
        booking.save()
    }

    def createSubscription(String description, boolean showComment, DateTime dateFrom,
                           DateTime dateTo, Slot slot, def weekDay, int interval, Customer customer, String accessCode = null,
                           boolean reminderEnabled = true, User issuer = null) {

        weekDay = weekDay?: dateFrom.dayOfWeek

        log.debug("Creating subscription start ${dateFrom.toString("yyyy-MM-dd")} on ${weekDay}")

        if( dateTo.isBefore(dateFrom) ) {
            throw new IllegalArgumentException("To can not be before from!")
        } else if (!slot.court.id) {
            throw new IllegalArgumentException("A subscription needs at least one court (null)")
        }

        Subscription subscription = new Subscription()
        LocalTime time = new LocalTime(slot.startTime)

        BookingGroup bookingGroup = bookingAvailabilityService.createSubscriptionBookingGroup(subscription, dateFrom, dateTo, slot, [weekDay], interval, customer, showComment, description)

        if(!subscription.slots) {
            log.debug("No slots found for subscription")
            return null
        }

        bookingGroup.subscription = subscription
        subscription.bookingGroup = bookingGroup
        subscription.customer = customer
        subscription.description = description
        subscription.weekday = weekDay.toInteger()
        subscription.time = time
        subscription.court = slot.court
        subscription.timeInterval = interval
        subscription.showComment = showComment
        subscription.reminderEnabled = reminderEnabled
        subscription.order = createSubscriptionOrder(subscription, customer,
                Order.ORIGIN_FACILITY, issuer)
        subscription.accessCode = accessCode

        return subscription
    }

    def updateSubscription(Subscription subscription, String description, DateTime dateFrom, DateTime dateTo,
                           Slot slot, int interval, Customer customer, def showComment, String accessCode = null) {

        if(!slot) {
            throw new IllegalArgumentException("A subscription needs at least one court (null)")
        }

        BookingGroup bookingGroup = bookingAvailabilityService.updateSubscriptionBookingGroup(subscription, dateFrom, dateTo,
                slot, interval, customer, showComment, description)

        if(!bookingGroup) {
            return null
        }

        subscription.description = description
        subscription.weekday = dateFrom.getDayOfWeek()
        subscription.time = new LocalTime(slot.startTime)
        subscription.timeInterval = interval
        subscription.showComment = showComment
        subscription.court = slot.court
        subscription.accessCode = accessCode

        if (subscription.save(flush: true)) {
            createBookingsOrders(subscription)
            subscription.bookingGroup.bookings.each {
                courtRelationsBookingService.tryBookRelatedCourts(it)
            }
            return subscription
        } else {
            return null
        }
    }

    def save(Subscription subscription) {
        subscription.bookingGroup.save(failOnError: true)
        subscription.save(failOnError: true)
        createBookingsOrders(subscription)

        return subscription
    }

    @NotTransactional
    def getSubscriptions(def facility) {
        return getSubscriptions(facility, null)
    }

    @NotTransactional
    def getSubscriptions(def facility, def cmd) {
        def season = (cmd?.season?Season.get(cmd?.season):null)
        def offset = (cmd?.offset)?:0
        def max    = (cmd?.max)?:50
        def filter = "%" + ((cmd?.q)?:'') + "%"
        def sort   = (cmd?.sort?:'startTime')
        def order  = (cmd?.order?:'desc')

        def queryParameters = [
                facilityId: facility.id,
                filter: filter
        ]
        if (!cmd.allselected) {
            queryParameters.put("offset", offset)
            queryParameters.put("max", max)
        }

        def whereString = ""

        if (season) {
            def startDate = new DateTime(season.startTime)
            def endDate = new DateTime(season.endTime)

            whereString = "and sl.start_time > :startDate and sl.start_time < :endDate "

            queryParameters.put("startDate", startDate.toDate())
            queryParameters.put("endDate", endDate.toDate())
        }

        if (cmd.weekday) {
            whereString += "and s.weekday in (${cmd.weekday.join(',')}) "
        }

        if (cmd.time) {
            whereString += "and s.time = :time "
            queryParameters.put("time", cmd.time.toString('HH:mm'))
        }

        if (cmd.court) {
            whereString += "and crt.id in (${cmd.court.join(',')}) "
        }

        if (cmd.status) {
            whereString += """and s.status in ('${cmd.status*.name().join("','")}') """
        }

        if (cmd.invoiceStatus) {
            if(cmd.invoiceStatus.contains(FacilitySubscriptionFilterCommand.NOT_INVOICED_STATUS)) {
                whereString += "and ir.id is null "
            }
            if(!cmd.invoiceStatus.contains(FacilitySubscriptionFilterCommand.NOT_INVOICED_STATUS)
                    || cmd.invoiceStatus.size() > 1) {
                whereString += """and i.status in ('${cmd.invoiceStatus.collect{SEU.escapeSql(it)}.join("','")}') """
            }
        }

        def querySort = " order by ${sort} ${order}"

        def query = """
            SELECT
                s.id,
                s.weekday as weekday,
                s.time as time,
                s.copied_date as copiedDate,
                s.status as status,
                s.reminder_enabled as reminderEnabled,
                concat(c.firstname, ' ', c.lastname) as customerName,
                c.lastname as lastname,
                c.companyName as companyName,
                ir.id as invoiceRowId,
                i.id as invoiceId,
                i.status invoiceStatus,
                count(sl.id) as numSlots,
                min(sl.start_time) as startTime,
                max(sl.start_time) as endTime,
                crt.name as courtName,
                crt.id as courtId
            FROM subscription s
            INNER JOIN customer c ON c.id = s.customer_id
            LEFT  JOIN slot sl ON sl.subscription_id = s.id
            LEFT JOIN court crt on s.court_id = crt.id
            LEFT JOIN invoice_row ir on s.invoice_row_id = ir.id
            LEFT JOIN invoice i on ir.invoice_id = i.id
            WHERE
                c.facility_id = :facilityId
                and (
                    c.email like :filter
                    or concat(c.firstname, ' ', c.lastname) like :filter
                    or c.companyname like :filter
                    or c.number like :filter
                )
                $whereString
            GROUP BY s.id $querySort
            """

        if (!cmd.allselected) {
            query += " limit :max offset :offset"
        }

        def queryCount = """
            SELECT
                count(distinct s.id) as count
            FROM subscription s
            INNER JOIN customer c ON c.id = s.customer_id
            LEFT  JOIN slot sl ON sl.subscription_id = s.id
            LEFT JOIN court crt on sl.court_id = crt.id
            LEFT JOIN invoice_row ir on s.invoice_row_id = ir.id
            LEFT JOIN invoice i on ir.invoice_id = i.id
            WHERE
                c.facility_id = :facilityId
                and (
                    c.email like :filter
                    or concat(c.firstname, ' ', c.lastname) like :filter
                    or c.companyname like :filter
                    or c.number like :filter
                )
                $whereString

            """

        def rows = groovySql.rows(query, queryParameters)
        def count = groovySql.rows(queryCount, queryParameters).get(0).count

        groovySql.close()
        return [rows:rows, count:count]
    }

    def copySubscriptions(Date from, Date to, List<Subscription> subscriptions, Long userId) {

        log.debug("Copying subscriptions: ${subscriptions.size()}")

        def copiedSubscriptions = []

        // Group by weekday
        List<List<Long>> finalSubscriptionIdList =  []

        subscriptions.groupBy { Subscription subscription ->
            return subscription.weekday
        }.each { key, subList ->
            List<Long> listToAdd = subList.groupBy { Subscription sub ->
                return sub.court
            }.collect { k, subList2 ->
                finalSubscriptionIdList.add(subList2.collect { it.id })
            }
        }

        def stopWatch = new StopWatch("Copy subscriptions between ${from} and ${to}")
        stopWatch.start()


        int numberOfThreads = grailsApplication.config.matchi.threading.numberOfThreads
        GParsPool.withPool(numberOfThreads) {
            finalSubscriptionIdList.eachParallel { List<Long> subscriptionList ->
                def user = User.get(userId)
                subscriptionList.each { Long subscriptionId ->
                    Subscription.withTransaction {
                        copiedSubscriptions << copySubscription(from, to, Subscription.get(subscriptionId), user)
                    }
                }
            }
        }

        log.debug("Copied subscriptions: ${copiedSubscriptions?.size()}")
        stopWatch.stop()
        log.info(stopWatch.shortSummary())
        log.info("Copied ${copiedSubscriptions?.size()} subscriptions in ${stopWatch.totalTimeSeconds} seconds (${copiedSubscriptions?.size() / stopWatch.totalTimeSeconds} / subscription)")

        return copiedSubscriptions
    }

    @NotTransactional
    Subscription copySubscription(Date from, Date to, Subscription subscription, User user) {
        SubscriptionCopyInfo subscriptionCopyInfo = new SubscriptionCopyInfo()
        subscriptionCopyInfo.subscription = subscription

        DateTime start = new DateTime(from).withHourOfDay(subscription.time.hourOfDay).withMinuteOfHour(subscription.time.minuteOfHour)
        DateTime subscriptionStartDate = getSubscriptionSeasonStartDate(subscription, start)
        DateTime subscriptionEndDate = new DateTime(to)
        Court court = subscription.court

        def slot
        def date = subscriptionStartDate
        def dates = []
        while (date.isBefore(subscriptionEndDate)) {
            dates << date.toDate()
            date = date.plusWeeks(subscription.timeInterval)
        }
        if (dates) {
            slot = Slot.findByCourtAndStartTimeInList(court, dates, [sort: "startTime"])
        }

        def newSubscription

        if (slot) {
            newSubscription = createSubscription(subscription.description,
                    subscription.showComment, subscriptionStartDate, subscriptionEndDate,
                    slot, subscriptionStartDate.dayOfWeek, subscription.timeInterval,
                    subscription.customer, subscription.accessCode, subscription.reminderEnabled, user)

            if( newSubscription ) {
                newSubscription.save(failOnError: true)

                createBookingsOrders(newSubscription, user, false)

                newSubscription.bookingGroup.bookings.each {
                    courtRelationsBookingService.tryBookRelatedCourts(it)
                }

                subscription.copiedDate = new Date()
                subscription.save(flush: true)

                return newSubscription
            }
        }
    }

    @NotTransactional
    def getSubscriptionSeasonStartDate(SubscriptionCopyInfo subscriptionCopyInfo) {
        def newSeasonStartDay = new DateTime(subscriptionCopyInfo.toSeason.startTime).withHourOfDay(subscriptionCopyInfo.subscription.time.hourOfDay).withMinuteOfHour(subscriptionCopyInfo.subscription.time.minuteOfHour)
        def dayDiff = subscriptionCopyInfo.subscription.weekday - newSeasonStartDay.getDayOfWeek()

        if( dayDiff < 0 ) {
            dayDiff = 7 - Math.abs(dayDiff)
        }

        return newSeasonStartDay.plusDays(dayDiff)
    }

    @NotTransactional
    def getSubscriptionSeasonStartDate(Subscription subscription, DateTime subscriptionStart) {
        def dayDiff = subscription.weekday - subscriptionStart.getDayOfWeek()

        if( dayDiff < 0 ) {
            dayDiff = 7 - Math.abs(dayDiff)
        }

        return subscriptionStart.plusDays(dayDiff)
    }

    void deleteSubscription(Subscription subscription) {
        def bookingGroup = subscription.bookingGroup

        subscription.slots.collect().each { Slot slot ->
            try {

                def booking = slot.booking
                def subscriptionBooking = booking?.isSubscription()

                if(booking?.group == bookingGroup) {
                    bookingGroup.removeFromBookings(booking)
                    booking?.group = null
                }

                subscription.removeFromSlots(slot)
                slot.subscription = null

                if (booking && booking.customer.id == subscription.customer.id && subscriptionBooking) {
                    courtRelationsBookingService.tryCancelRelatedCourts(booking)
                    mpcService.tryDelete(booking)

                    slot.booking = null

                    booking.delete()
                }
                slot.save(failOnError: true)
            } catch (org.springframework.dao.DataIntegrityViolationException e) {
                log.error("Could not delete booking from subscription: ${e.localizedMessage}")
            }
        }

        subscription.bookingGroup = null
        subscription.delete()
        bookingGroup.delete(flush: true)
    }

    @NotTransactional
    def getSportIdsFromSubscriptionIds(def subscriptionIds) {
        def sportIds = Subscription.createCriteria().listDistinct {
            createAlias("slots", "s", CriteriaSpecification.LEFT_JOIN)
            createAlias("s.court", "c", CriteriaSpecification.LEFT_JOIN)
            createAlias("c.sport", "sp", CriteriaSpecification.LEFT_JOIN)

            and {
                inList("id", subscriptionIds)
            }

            projections {
                property "sp.id"
            }

        }
        return sportIds
    }

    @NotTransactional
    def getCustomerSubscriptions(Customer customer, Date minEndTime = new Date()) {
        Subscription.createCriteria().listDistinct {
            eq("customer", customer)
            slots {
                ge("startTime", minEndTime)
            }
        }
    }

    @NotTransactional
    List getSubscriptionsSummary(List<Long> ids) {
        if (!ids) {
            return []
        }

        Subscription.withCriteria {
            'in'("id", ids)
            projections {
                groupProperty("id")
                customer {
                    property("number")
                    property("firstname")
                    property("lastname")
                    property("companyname")
                }
                slots {
                    min("startTime")
                    max("startTime")
                    rowCount()
                }
            }
        }
    }

    @NotTransactional
    List getSubscriptionsToRemind(Date startTime, Date endTime, Facility facility) {
        Subscription.createCriteria().listDistinct {
            customer {
                eq('facility', facility)
            }
            slots {
                ge("startTime", startTime)
                lt("startTime", endTime)
                booking {
                    or {
                        isNull("dateReminded")
                        eq("dateReminded", false)
                    }
                }
            }
            eq("reminderEnabled", true)
        }
    }

    void createBookingsOrders(Subscription subscription, User currentUser = null, boolean flush = true) {
        def user = currentUser ?: userService.getLoggedInUser()

        subscription.bookingGroup.bookings.each { booking ->
            if (!booking.order) {
                def price = priceListService.getBookingPrice(booking.slot, subscription.customer)

                def order = new Order(issuer: user, user: user, customer: subscription.customer,
                        dateDelivery: booking.slot.startTime, facility: subscription.customer.facility,
                        origin: Order.ORIGIN_WEB, description: booking.slot.getDescription(), price: price.price,
                        vat: price.VATAmount, article: Order.Article.SUBSCRIPTION_BOOKING,
                        metadata: [slotId: booking.slot.id])

                if(flush) {
                    orderStatusService.complete(order, user)
                } else {
                    order.status = Order.Status.COMPLETED
                }

                order.save()

                booking.order = order
                booking.save()
            }
        }

        if (subscription.order?.status == Order.Status.NEW
                && subscription.bookingGroup.bookings.every {it.order?.status == Order.Status.COMPLETED}) {
            orderStatusService.complete(subscription.order, user)
        }
    }

    Order createSubscriptionOrder(Subscription subscription, Customer customer,
                                  String origin = Order.ORIGIN_FACILITY, User issuer = null) {
        def pricelist = priceListService.getActiveSubscriptionPriceList(
                customer.facility, subscription.slots.first().court.sport)
        def price = pricelist ? subscription.getPrice(pricelist) : 0

        def order = new Order()
        order.metadata = [subscriptionId: subscription.id.toString()]
        order.issuer = issuer ?: springSecurityService.getCurrentUser()
        order.user   = customer?.user
        order.customer = customer
        order.dateDelivery = subscription.slots.first().startTime
        order.facility = customer?.facility
        order.origin = origin
        order.description = subscription.createInvoiceDescription()
        order.price = price
        order.vat = price * (customer?.facility?.vat/100)
        order.article = Order.Article.SUBSCRIPTION
        order.save(failOnError: true)
    }
}
