package com.matchi

import au.com.bytecode.opencsv.CSVWriter
import com.matchi.activities.ActivityOccasion
import com.matchi.activities.trainingplanner.Trainer
import com.matchi.enums.BookingGroupType
import com.matchi.enums.RedeemType
import com.matchi.enums.RefundOption
import com.matchi.events.SystemEventInitiator
import com.matchi.excel.ExcelExportManager
import com.matchi.facility.FilterBookingsCommand
import com.matchi.integration.events.BookingEventType
import com.matchi.integration.events.EventType
import com.matchi.integration.events.EventWrapper
import com.matchi.integration.events.InitiatorProvider
import com.matchi.orders.CashOrderPayment
import com.matchi.orders.InvoiceOrderPayment
import com.matchi.orders.Order
import com.matchi.orders.OrderPayment
import com.matchi.payment.PaymentMethod
import com.matchi.price.PriceListCustomerCategory
import com.matchi.requests.TrainerRequest
import com.matchi.subscriptionredeem.SlotRedeem
import grails.transaction.NotTransactional
import grails.util.Environment
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.builder.ToStringBuilder
import org.apache.commons.lang3.builder.ToStringStyle
import org.hibernate.criterion.CriteriaSpecification
import org.joda.time.DateTime
import org.joda.time.LocalTime
import org.joda.time.ReadableInterval
import org.springframework.beans.factory.annotation.Value
import org.springframework.transaction.TransactionDefinition

import javax.annotation.PostConstruct
import java.text.SimpleDateFormat

class BookingService {

    public static final String EXPORT_DATE_FORMAT = "yyyy-MM-dd"
    public static final String EXPORT_TIME_FORMAT = "HH:mm"

    // Real number is 1000 but due to trailing spaces, we have some margin
    public static final int COMMENTS_MAX_INPUT_SIZE = 970

    static transactional = true

    def cashService
    def springSecurityService
    def userService
    def notificationService
    def slotService
    def paymentService
    def couponService
    def memberService
    def customerService
    def ticketService
    def redeemService
    def courtRelationsBookingService
    def priceListService
    def mpcService
    def securityService
    def objectWatchNotificationService
    def messageSource
    def kafkaProducerService
    OrderStatusService orderStatusService

    @Value('${matchi.booking.export.batchSize}')
    Integer bookingExportBatchSize

    // TODO: workaround; we need to use extra property to avoid pessimistic locks in tests due to limited support
    // (unit tests don't support "lock" in criteria; h2 database don't support "FOR UPDATE && JOIN" in integration tests)
    private boolean pessimisticLockAllowed

    @PostConstruct
    void configure() {
        pessimisticLockAllowed = Environment.current != Environment.TEST
    }

    def makeBooking(CreateBookingCommand cmd, def sendNotification = true) {

        def booking = new Booking()

        Customer customer = getBookingCustomer(cmd)

        if (customer == null) {
            throw new IllegalArgumentException("Could not find customer ${cmd.email}")
        }

        def slot = slotService.getSlot(cmd.slotId)

        if (slot == null || slot.booking) {
            throw new BookingException("Could not execute booking on slot ${slot}")
        }

        booking.customer = customer
        booking.comments = cmd.comments
        booking.showComment = cmd.showComment ?: false
        booking.telephone = cmd.telephone
        booking.slot = slot
        if (cmd.payment) {
            booking.payment = cmd.payment
        }
        booking.online = cmd.online ?: false
        booking.hideBookingHolder = cmd.hideBookingHolder
        booking.save(failOnError: true, flush: true)

        if(cmd.trainerId) {
            Trainer trainer = Trainer.get(cmd.trainerId)
            booking.addToTrainers(trainer)
            booking.save()
        }

        booking.bookingNumber = "M-" + booking.id

        slot.booking = booking
        slot.save(failOnError: true)

        if (slot.subscription && slot.subscription.customer == customer) {
            def group = slot.subscription.bookingGroup
            group.addToBookings(booking)
            group.save()
        }

        if (customer?.facility?.hasMPC() && (!cmd.bookingType?.equals(BookingGroupType.NOT_AVAILABLE) || cmd.activateMpc)) {
            mpcService.add(booking)
        }

        if (sendNotification) {
            notificationService.sendNewBookingNotification(booking, cmd.payment)
            booking.players.each { Player player ->
                if (!player.isBookingCustomer(booking.customer.email)) {
                    notificationService.sendNewBookingPlayerNotification(booking, player)
                }
            }
        }

        courtRelationsBookingService.tryBookRelatedCourts(booking)

        sendBookingEvent(booking, BookingEventType.CREATED)

        return booking
    }

    def makeBookings(List<CreateBookingCommand> cmds, def sendNotification = true) {
        def bookings = []
        cmds?.each {
            bookings << makeBooking(it, sendNotification)
        }
        return bookings
    }

    def makeBookings(FacilityBookingCommand cmd, List playerCustomerIds = null, Integer unknownPlayers = null) {
        def slotIds = cmd.slotIds()

        if (slotIds.isEmpty()) {
            throw new IllegalArgumentException("No slot ids provided to facility booking")
        }

        def bookings = []

        def existingBookings = getBookingsBySlots(slotIds);

        if (!existingBookings.isEmpty()) {
            log.info "Update of bookings: ${existingBookings.size()}"
            existingBookings.each { Booking booking ->

                def group = booking.group

                // Update all in group if chosen...
                // Subscriptions are not dealt with here
                if (cmd.updateGroup && group && !group.type.equals(BookingGroupType.SUBSCRIPTION)) {
                    group.bookings.each { Booking b ->
                        b.comments = cmd.comments
                        b.showComment = cmd.showComment
                        b.save()
                    }

                    // We should update payment status for this specific booking as we do for just one
                    booking.save()
                } else {
                    booking.comments = cmd.comments
                    booking.showComment = cmd.showComment
                    booking.updatePlayers(customerService.collectPlayers(playerCustomerIds, unknownPlayers))
                    booking.save()
                }

                if (cmd.paid) {
                    if (!booking.order) {
                        booking.order = paymentService.createBookingOrder(
                                booking.slot, booking.customer, Order.ORIGIN_FACILITY,
                                booking.players.findAll { it.customer }.collect { it.customer.id },
                                booking.players.findAll { !it.customer }.size())
                        booking.save()
                    }
                    if (!booking.order?.isFinalPaid()) {
                        paymentService.makePayment(booking.order, cmd)

                        if (cmd.useGiftCard || cmd.useCoupon) {
                            couponService.updateTicketIfExists(booking.order, booking)
                        }
                    }
                } else {
                    // Should ALWAYS be refundable since you cannot change paid status on credit card
                    // paid booking in admin booking view
                    if (booking.order?.isStillRefundable()) {
                        booking.order?.payments.each { payment ->
                            if (!payment.status.equals(OrderPayment.Status.CREDITED)) {
                                payment.refund(payment.amount)
                            }
                        }
                    }
                }
            }
            return true
        }

        def slots = slotService.getSlots(slotIds)

        def sendNotification = cmd.sendNotification
        //Do not send multiple bookingNotifications if group needs to be created and if type is of other than DEFAULT
        if ((cmd.type.equals(BookingGroupType.DEFAULT) && slots.size() > 1) || !cmd.type.equals(BookingGroupType.DEFAULT)) {
            sendNotification = false
        }

        if (!cmd.type.equals(BookingGroupType.DEFAULT) && cmd.useRecurrence) {
            //Can not include recurrence if DEFAULT booking group
            def recurringSlots = slotService.getRecurrenceSlots(new DateTime(cmd.recurrenceStart), new DateTime(cmd.recurrenceEnd), cmd.weekDays, cmd.frequency, cmd.interval, slots);
            if (recurringSlots.freeSlots.size() > 0) {
                slots = recurringSlots.freeSlots
            }
        }

        def customer = getBookingCustomer(cmd)
        cmd.customerId = customer.id

        slots.each { Slot slot ->
            if (slot.bookingRestriction) {
                slot.bookingRestriction = null
                cmd.paid = false
            }

            def order = paymentService.createBookingOrder(slot, customer,
                    Order.ORIGIN_FACILITY, playerCustomerIds, unknownPlayers)
            if (cmd.paid) {
                paymentService.makePayment(order, cmd)
            }

            order.addPlayersToMetadata(customerService.collectPlayers(playerCustomerIds, unknownPlayers))

            def booking = book(order, sendNotification, cmd)

            bookings << booking
        }

        if ((cmd.type.equals(BookingGroupType.DEFAULT) && slots.size() > 1) || !cmd.type.equals(BookingGroupType.DEFAULT)) {
            try {
                log.info("Adding booking group--------------start")
                BookingGroup bookingGroup = new BookingGroup()
                bookingGroup.addComment(cmd.comments)
                bookingGroup.type = cmd.type
                def facility = bookings[0].customer.facility.id
                bookings.each { Booking booking ->
                    log.info("Adding booking to group, facility-${facility}")
                    bookingGroup.addToBookings(booking)
                }
                log.info("Total bookings for group booking: ${bookings.size()}")

                bookingGroup.save(failOnError: true)

                if (cmd.sendNotification) {
                    notificationService.sendNewBookingGroupNotification(bookings)
                    bookings[0].players.each { Player player ->
                        if (!player.isBookingCustomer(customer.email)) {
                            notificationService.sendNewBookingGroupPlayerNotification(bookings, player)
                        }
                    }

                }
            } catch (Exception e) {
                log.error(e.message)
            }
        }

        return false
    }

    def deleteBooking(Booking booking) {
        booking.delete()
        sendBookingEvent(booking, BookingEventType.DELETED)
    }

    def cancelBooking(Booking booking, def whyMessage) {
        return cancelBooking(booking, whyMessage, false, RedeemType.NORMAL, true)
    }

    def cancelBooking(Booking booking, def whyMessage, def forceAnnul, RedeemType redeemType, def sendNotification = true) {
        cancelBooking(booking, whyMessage, forceAnnul, redeemType, springSecurityService.getCurrentUser(), sendNotification)
    }

    def cancelBooking(Booking booking, def whyMessage, def forceAnnul, RedeemType redeemType, def user,
                      def sendNotification, boolean async = false, boolean skipActivityOccasionCleanup = false, RefundOption refundOption = null) {

        booking = booking.merge()

        if (user && !hasPermissionToBooking(user, booking)) {
            throw new IllegalArgumentException("User does not have access to booking")
        }

        def slot = booking.slot
        def customer = booking?.customer
        def facility = customer?.facility

        if (facility?.subscriptionRedeem) {
            redeemService.redeem(booking, redeemType, user)
        }

        def payment = booking?.payment

        if (booking.order) {
            if ((!slot.isRefundable() && !forceAnnul) || !booking.order.isStillRefundable() || refundOption == RefundOption.NO_REFUND) {
                // annull order but make no refund
                log.info("Booking order not refundable at this time, skipping")
                orderStatusService.annul(booking.order, user)
            } else {
                if (forceAnnul && refundOption != RefundOption.CUSTOMER_PAYS_FEE) {
                    // annull order and refund the totalt amount
                    log.info("Refunding user on order ${booking.order.id} (no fee)")
                    orderStatusService.annul(booking.order, user, "Booking cancelled (no fee)", booking.order.total())
                } else {
                    // annull order and refund the totalt amount minus booking fee
                    log.info("Refunding user on order ${booking.order.id} (with fee)")
                    String refundNote = "Booking cancelled (${slot.refundPercentage()}% incl. fee)"
                    if (refundOption == RefundOption.CUSTOMER_PAYS_FEE) {
                        def amountCredit = calculateAmountToCreditForAdmin(booking)
                        if (slot.refundPercentage() == 0) {
                            refundNote = "Booking cancelled (100% incl. fee)"
                        }
                        orderStatusService.annul(booking.order, user, refundNote, amountCredit)
                    } else {
                        def amountCredit = calculateAmountToCredit(booking)
                        orderStatusService.annul(booking.order, user, refundNote, amountCredit)
                    }
                }
            }
        } else {
            couponService.refundCustomerCoupon(booking, forceAnnul)
        }

        booking = booking.save(flush: true, failOnError: true)

        // If booking belongs to a group but doesn't belong to a subscription, remove it from group
        if (!async && booking.group) {
            def group = booking.group
            group.removeFromBookings(booking)
            group.save(failOnError: true)

            if (!group.type.equals(BookingGroupType.SUBSCRIPTION) && group.bookings.size() < 1) {
                log.debug("Booking group not containing any more bookings, removing group")
                group.delete(failOnError: true)
            }
        }

        if (sendNotification) {
            notificationService.sendBookingCanceledNotification(booking, whyMessage, payment, refundOption)
            booking.players.each { Player player ->
                if (!player.isBookingCustomer(customer.email)) {
                    notificationService.sendBookingCanceledPlayerNotification(booking, player, whyMessage)
                }
            }
        }
        if (booking.trainers?.size() > 0) {
            notificationService.sendBookingCanceledTrainerNotification(booking, booking.trainers.first())
        }

        courtRelationsBookingService.tryCancelRelatedCourts(booking)
        objectWatchNotificationService.trySendNotificationsFor(slot.id)

        mpcService.tryDelete(booking)

        if (!skipActivityOccasionCleanup) {
            //Remove from occasion
            ActivityOccasion.withTransaction([propagationBehavior: TransactionDefinition.PROPAGATION_REQUIRES_NEW]) {
                def occasion = ActivityOccasion.createCriteria().get {
                    bookings {
                        eq("id", booking.id)
                    }
                    if (pessimisticLockAllowed) {
                        lock true
                    }
                }
                if (occasion) {
                    occasion.removeFromBookings(booking)
                    occasion.save(failOnError: true)
                }
            }
        }

        slot.booking = null
        booking?.delete(flush: true, failOnError: true)
        slot.save()
        sendBookingEvent(booking, BookingEventType.CANCELLED)
    }

    def cancelActivityOccasionBooking(Booking booking, def whyMessage) {
        cancelBooking(booking, whyMessage, false, RedeemType.NORMAL,
                springSecurityService.getCurrentUser(), true, true)
    }

    def cancelOrders(List<Order> orders, User user) {
        orders.each { Order order ->
            Booking booking = Booking.findByOrder(order)
            if (booking) {
                cancelBooking(booking, "", true, RedeemType.NORMAL, user, false)
            } else {
                orderStatusService.annul(order, new SystemEventInitiator(), "Transaction rollback", order.total())
            }
        }
    }

    @NotTransactional
    def calculateAmountToCredit(def booking) {

        def amount = booking.order.total()
        def refundPercentage = booking.slot.refundPercentage()

        if (refundPercentage == 100) {
            return amount.minus(paymentService.getServiceFee(
                    booking.slot.court.facility.currency).amount)
        } else {
            return amount.multiply(refundPercentage / 100)
        }
    }

    @NotTransactional
    def calculateAmountToCreditForAdmin(def booking) {

        def amount = booking.order.total()
        def refundPercentage = booking.slot.refundPercentage()

        //if we have late cancellation we get refundPercentage 0 but for admin cancellation it is ok
        //we still want to refund money minus fee
        if (refundPercentage == 100 || refundPercentage == 0) {
            return amount.minus(paymentService.getServiceFee(
                    booking.slot.court.facility.currency).amount)
        } else {
            return amount.multiply(refundPercentage / 100)
        }
    }

    def moveBooking(Slot fromSlot, Slot toSlot, def sendNotification, def message) {
        def customer = fromSlot?.booking?.customer
        def toCustomer = toSlot?.booking?.customer

        updateActivityOccasionTimeIfNecessary(fromSlot.booking, toSlot.startTime, toSlot.endTime)
        updateActivityOccasionTimeIfNecessary(toSlot.booking, fromSlot.startTime, fromSlot.endTime)

        def startTime = toSlot.startTime
        def endTime = toSlot.endTime
        def court = toSlot.court

        toSlot.startTime = fromSlot.startTime
        toSlot.endTime = fromSlot.endTime
        toSlot.court = fromSlot.court

        fromSlot.startTime = startTime
        fromSlot.endTime = endTime
        fromSlot.court = court

        courtRelationsBookingService.tryBookRelatedCourts(fromSlot)

        if (fromSlot?.booking?.order) {
            fromSlot.booking.order.dateDelivery = fromSlot.startTime
            fromSlot.booking.order.description = fromSlot.getDescription()
        }

        if (toSlot?.booking?.order) {
            toSlot.booking.order.dateDelivery = toSlot.startTime
            toSlot.booking.order.description = toSlot.getDescription()
        }

        fromSlot.save(flush: true)
        toSlot.save(flush: true)

        if (fromSlot.booking) {
            sendBookingEvent(fromSlot.booking, BookingEventType.MOVED)
        }
        if (toSlot.booking) {
            sendBookingEvent(toSlot.booking, BookingEventType.MOVED)
        }

        if (customer?.facility?.hasMPC()) {
            if (fromSlot.booking) {
                mpcService.move(fromSlot.booking)
            }
            if (toSlot.booking) {
                mpcService.move(toSlot.booking)
            }
        }

        // Move subscription between slots only when the following scenarios apply:
        // - Slot (canceled subscription) <-> Slot (canceled subscription)
        // - Slot (no subscription) <-> Slot (canceled subscription)
        // - Slot (canceled subscription) <-> Slot (no subscription)
        if ((fromSlot.isCanceledSubscriptionSlot() && toSlot.isCanceledSubscriptionSlot()) ||
                (!fromSlot.belongsToSubscription() && toSlot.isCanceledSubscriptionSlot()) ||
                (fromSlot.isCanceledSubscriptionSlot() && !toSlot.belongsToSubscription())) {

            Subscription fromSlotSubscription = fromSlot.subscription
            Subscription toSlotSubscription = toSlot.subscription
            fromSlot.subscription = toSlotSubscription
            toSlot.subscription = fromSlotSubscription
            fromSlot.save(flush: true)
            toSlot.save(flush: true)
        }

        if (sendNotification) {
            notificationService.sendMovedBookingNotification(fromSlot, toSlot, customer, message)

            if (toCustomer) {
                notificationService.sendMovedBookingNotification(toSlot, fromSlot, toCustomer, message)
            }
        }

        if (!toSlot.booking) {
            objectWatchNotificationService.trySendNotificationsFor(toSlot.id)
        }
    }

    @NotTransactional
    def getBooking(def bookingId) {
        return Booking.get(bookingId)
    }

    @NotTransactional
    def getUserBooking(def bookingId, user) {
        Booking booking = getBooking(bookingId)
        return (booking?.customer?.user == user) ? booking : null
    }

    @NotTransactional
    def getBookingCustomer(def cmd) {
        Customer customer = Customer.get(cmd.customerId)

        if (customer == null) {
            Facility facility = userService.userFacility

            CreateCustomerCommand ccc = new CreateCustomerCommand()
            ccc.facilityId = facility.id
            ccc.number = facility.getNextCustomerNumber()
            ccc.country = facility.country
            ccc.email = cmd.email?.trim()
            ccc.firstname = cmd.firstname?.trim()
            ccc.lastname = cmd.lastname?.trim()
            ccc.telephone = cmd.telephone?.trim()
            ccc.userId = cmd.userId

            customer = customerService.createCustomer(ccc)
        }

        if (!customer.user) {
            customerService.linkCustomerToUser(customer)
        }

        if (cmd.newMember && !customer.membership) {
            def membership = memberService.addMembership(customer, cmd.memberType,
                    cmd.startDate, cmd.endDate, cmd.gracePeriodEndDate, null, null, true,
                    Order.ORIGIN_FACILITY, null, cmd.startingGracePeriodDays)
            if (membership && cmd.membershipCancel) {
                memberService.disableAutoRenewal(membership)
            }
            if (membership?.order?.total() && cmd.membershipPaid) {
                cashService.createCashOrderPayment(membership.order)
            }
        }

        return customer
    }

    @NotTransactional
    def getBookingsBySlots(def slotIds) {
        def slots = slotService.getSlots(slotIds)
        def bookings = []

        slots.each {
            if (it.booking) {
                bookings << it.booking
            }
        }

        return bookings
    }

    @NotTransactional
    def getUserBookings(def user, def onlySpareSlotBookings = false, int maxPerPage = 0, int offset = 0) {
        return getUserBookings(user, onlySpareSlotBookings, maxPerPage, offset, [])
    }

    @NotTransactional
    def getUserBookings(def user, def onlySpareSlotBookings = false, int maxPerPage = 0, int offset = 0, List<Facility> facilities, showPast = false, String sortOrder = "asc") {

        maxPerPage = maxPerPage ?: -1
        def facilityIds = facilities?.collect { it.id }

        def bookings = Booking.createCriteria().list(max: maxPerPage, offset: offset) {
            createAlias('customer', 'c')
            createAlias('slot', 's')
            createAlias('slot.court', 'sc')
            createAlias('sc.facility', 'f')

            if (!facilityIds?.isEmpty()) {
                inList("f.id", facilityIds)
            }
            if (onlySpareSlotBookings) {
                createAlias('group', 'g', CriteriaSpecification.LEFT_JOIN)
                or {
                    isNull('group')
                    eq('g.type', BookingGroupType.DEFAULT)
                }
            }
            if (!showPast) {
                gt('s.startTime', new DateTime().minusHours(1).toDate())
            }
            eq('c.user', user)
            order("s.startTime", sortOrder)
        }

        return bookings
    }

    @NotTransactional
    def getUserSubscriptionBookings(def user) {
        return getUserSubscriptionBookings(user, [])
    }

    @NotTransactional
    def getUserSubscriptionBookings(def user, List<Facility> facilities) {
        def facilityIds = facilities?.collect { it.id }
        def userCustomers = Customer.findAllByUser(user)

        if (!userCustomers)
            return []

        def bookings = Booking.createCriteria().list {
            createAlias('customer', 'c', CriteriaSpecification.LEFT_JOIN)
            createAlias('slot', 's', CriteriaSpecification.LEFT_JOIN)
            createAlias('slot.court', 'sc', CriteriaSpecification.LEFT_JOIN)
            createAlias('slot.subscription', 'sub', CriteriaSpecification.LEFT_JOIN)

            if (!facilityIds?.isEmpty()) {
                inList("sc.facility.id", facilityIds)
            }
            isNotNull("sub.id")
            inList("sub.customer", userCustomers)
            gt('s.startTime', new DateTime().minusHours(1).toDate())
            eq('c.user', user)
            order("s.startTime", "asc")
        }

        return bookings
    }

    @NotTransactional
    def getUserPastBookings(User user, def offset) {
        return Booking.createCriteria().list(max: 10, offset: offset) {
            createAlias('group', 'g', CriteriaSpecification.LEFT_JOIN)
            createAlias('customer', 'c', CriteriaSpecification.LEFT_JOIN)
            createAlias('slot', 's', CriteriaSpecification.LEFT_JOIN)
            createAlias('slot.court', 'sc', CriteriaSpecification.LEFT_JOIN)
            createAlias('sc.facility', 'f', CriteriaSpecification.LEFT_JOIN)

            or {
                isNull('group')
                eq('g.type', BookingGroupType.DEFAULT)
            }

            le('s.startTime', new DateTime().toDate())
            ge('s.startTime', new DateTime().minusYears(1).toDate())
            eq('c.user', user)
            order("s.startTime", "desc")
        }
    }

    @NotTransactional
    def getCustomerBookings(def customer) {
        return getCustomerBookings(customer, false)
    }

    @NotTransactional
    def getCustomerBookings(def customer, def onlySpareSlotBookings) {
        def bookings = Booking.createCriteria().list {
            createAlias('slot', 'bookingSlot')
            createAlias('group', 'g', CriteriaSpecification.LEFT_JOIN)

            gt('bookingSlot.startTime', new DateTime().minusHours(1).toDate())
            eq('customer', customer)

            if (onlySpareSlotBookings) {
                or {
                    isNull('group')
                    eq('g.type', BookingGroupType.DEFAULT)
                }
            }

            order("bookingSlot.startTime", "asc")
        }


        return bookings
    }

    @NotTransactional
    def searchBooking(def facility, def timeSpan, FilterBookingsCommand filter) {
//def filter = null, def markpaid = null) {

        def start = timeSpan.start.toDate()
        def end = timeSpan.end.plusDays(1).toDate()

        def bookings = Booking.createCriteria().listDistinct {
            createAlias("customer", "c")
            createAlias("order", "o", CriteriaSpecification.LEFT_JOIN)
            createAlias("o.payments", "op", CriteriaSpecification.LEFT_JOIN)
            createAlias("o.refunds", "or", CriteriaSpecification.LEFT_JOIN)

            if (filter.paymentMethods.contains(FilterBookingsCommand.BookingPaymentMethod.REGULAR_COUPON)
                    || filter.paymentMethods.contains(FilterBookingsCommand.BookingPaymentMethod.UNLIMITED_COUPON)
                    || filter.paymentMethods.contains(FilterBookingsCommand.BookingPaymentMethod.GIFT_CARD)) {
                createAlias("op.ticket", "t", CriteriaSpecification.LEFT_JOIN)
                createAlias("t.customerCoupon", "cc", CriteriaSpecification.LEFT_JOIN)
                createAlias("cc.coupon", "cpn", CriteriaSpecification.LEFT_JOIN)
            }

            if (filter.paymentMethods.contains(FilterBookingsCommand.BookingPaymentMethod.CASH)
                    || filter.paymentMethods.contains(FilterBookingsCommand.BookingPaymentMethod.INVOICE)
                    || filter.paymentMethods.contains(FilterBookingsCommand.BookingPaymentMethod.FREE)
                    || filter.paymentMethods.contains(FilterBookingsCommand.BookingPaymentMethod.REGULAR_COUPON)
                    || filter.paymentMethods.contains(FilterBookingsCommand.BookingPaymentMethod.UNLIMITED_COUPON)
                    || filter.paymentMethods.contains(FilterBookingsCommand.BookingPaymentMethod.GIFT_CARD)) {
                createAlias("payment", "p", CriteriaSpecification.LEFT_JOIN)
                if (filter.paymentMethods.contains(FilterBookingsCommand.BookingPaymentMethod.REGULAR_COUPON)
                        || filter.paymentMethods.contains(FilterBookingsCommand.BookingPaymentMethod.UNLIMITED_COUPON)
                        || filter.paymentMethods.contains(FilterBookingsCommand.BookingPaymentMethod.GIFT_CARD)) {
                    createAlias("p.customerCoupon", "pcc", CriteriaSpecification.LEFT_JOIN)
                    createAlias("pcc.coupon", "pcpn", CriteriaSpecification.LEFT_JOIN)
                }
            }

            slot {
                court {
                    eq("facility", facility)
                    if (filter.courtIds) {
                        inList("id", filter.courtIds)
                    }
                }
                gt("startTime", start)
                le("startTime", end)

            }
            if (filter.q) {
                def q = StringUtils.replace(filter.q, "_", "\\_")
                or {
                    like("comments", "%${q}%")
                    like("c.email", "%${q}%")
                    like("c.companyname", "%${q}%")
                    like("c.firstname", "%${q}%")
                    like("c.lastname", "%${q}%")
                    like("c.telephone", "%${q}%")
                    like("c.cellphone", "%${q}%")
                    like("or.promoCode", "%${q}%")
                    sqlRestriction("concat(firstname,' ',lastname) like ?", ["%${q}%" as String])
                }
                and {
                    eq("c.facility", facility)
                }
            }

            if (filter.paymentMethods) {
                or {
                    if (filter.paymentMethods.contains(FilterBookingsCommand.BookingPaymentMethod.CREDIT_CARD)) {
                        isNotNull("op.transactionId")
                    }
                    if (filter.paymentMethods.contains(FilterBookingsCommand.BookingPaymentMethod.REGULAR_COUPON)) {
                        and {
                            eq("cpn.unlimited", false)
                            eq("cpn.class", "coupon")
                        }
                        and {
                            eq("pcpn.unlimited", false)
                            eq("pcpn.class", "coupon")
                        }
                    }
                    if (filter.paymentMethods.contains(FilterBookingsCommand.BookingPaymentMethod.UNLIMITED_COUPON)) {
                        and {
                            eq("cpn.unlimited", true)
                            eq("cpn.class", "coupon")
                        }
                        and {
                            eq("pcpn.unlimited", true)
                            eq("pcpn.class", "coupon")
                        }
                    }
                    if (filter.paymentMethods.contains(FilterBookingsCommand.BookingPaymentMethod.GIFT_CARD)) {
                        eq("cpn.class", "gift_card")
                        eq("pcpn.class", "gift_card")
                    }
                    if (filter.paymentMethods.contains(FilterBookingsCommand.BookingPaymentMethod.CASH)) {
                        eq("p.method", PaymentMethod.CASH)
                        eq("p.method", PaymentMethod.REGISTER)
                        eq("op.class", CashOrderPayment.DISCRIMINATOR)
                    }
                    if (filter.paymentMethods.contains(FilterBookingsCommand.BookingPaymentMethod.INVOICE)) {
                        eq("p.method", PaymentMethod.INVOICE)
                        eq("op.class", InvoiceOrderPayment.DISCRIMINATOR)
                    }
                    if (filter.paymentMethods.contains(FilterBookingsCommand.BookingPaymentMethod.FREE)) {
                        eq("p.method", PaymentMethod.FREE)
                    }
                }
            }

            if (filter.bookingTypes) {
                createAlias("slot.subscription", "sub", CriteriaSpecification.LEFT_JOIN)
                createAlias("group", "bg", CriteriaSpecification.LEFT_JOIN)
                or {
                    if (filter.bookingTypes.contains(BookingGroupType.DEFAULT)) {
                        or {
                            eq("bg.type", BookingGroupType.DEFAULT)
                            and {
                                isNull("bg.id")
                                isNull("sub.id")
                            }
                        }
                    }
                    if (filter.bookingTypes.contains(BookingGroupType.SUBSCRIPTION)) {
                        isNotNull("sub.id")
                    }

                    def bookingTypes = filter.bookingTypes.findAll { ![BookingGroupType.DEFAULT, BookingGroupType.SUBSCRIPTION].contains(it) }
                    if (!bookingTypes.isEmpty()) {
                        inList("bg.type", filter.bookingTypes?.findAll { bookingTypes })
                    }

                }
            }

            if (filter.groups) {
                or {
                    if (filter.groups.contains(0L)) {
                        isEmpty("c.customerGroups")
                    }
                    if (filter.groups.size() > 1 || !filter.groups.contains(0L)) {
                        createAlias("c.customerGroups", "cg", CriteriaSpecification.LEFT_JOIN)
                        inList("cg.group.id", filter.groups)
                    }
                }
            }

            slot {
                order('startTime', 'asc')
            }

        }

        List<Booking> paidOrUnpaidBookings
        if (filter.markpaid != null) {
            if (filter.markpaid) {
                paidOrUnpaidBookings = bookings.findAll { it.isFinalPaid() }
            } else {
                paidOrUnpaidBookings = bookings.findAll { !it.isFinalPaid() }
            }
        }

        if (filter.markpaid == null) {
            return bookings
        } else {
            return paidOrUnpaidBookings
        }

    }

    @NotTransactional
    def hasPermissionToBooking(def user, def booking) {
        log.debug("Checking booking permission for ${user}")

        if (booking.customer?.user?.id?.equals(user.id)) {
            return true
        }

        user.isInRole("ROLE_ADMIN") || (user.facility &&
                FacilityUserRole.granted(user).count() &&
                booking?.slot?.court?.facility.id.equals(user.facility.id))
    }

    def book(Order order, Boolean sendNotification = true, FacilityBookingCommand facilityBooking = null) {

        log.info("Making booking for order ${order}")

        Slot slot = slotService.getSlot(order.metadata?.slotId)

        if (!slot) {
            throw new IllegalStateException("Missing slot on order ${order.id}, ${ToStringBuilder.reflectionToString(order, ToStringStyle.MULTI_LINE_STYLE)}")
        } else {

            log.info("Making booking on ${slot} for order ${order}")

            order.assertCustomer()

            CreateBookingCommand cmd = new CreateBookingCommand()
            cmd.customerId = order.customer.id
            cmd.paid = !facilityBooking
            cmd.slotId = slot.id
            cmd.online = true
            cmd.hideBookingHolder = Boolean.valueOf(order.metadata?.hideBookingHolder)
            if (facilityBooking) {
                cmd.email = facilityBooking.email
                cmd.firstname = facilityBooking.firstname
                cmd.lastname = facilityBooking.lastname
                cmd.comments = facilityBooking.comments
                cmd.telephone = facilityBooking.telephone
                cmd.paid = facilityBooking.paid
                cmd.customerCouponId = facilityBooking.customerCouponId
                cmd.useCoupon = facilityBooking.useCoupon
                cmd.newMember = facilityBooking.newMember
                cmd.showComment = facilityBooking.showComment
                cmd.userId = facilityBooking.userId
                cmd.memberType = facilityBooking.memberType
                cmd.startDate = facilityBooking.startDate
                cmd.endDate = facilityBooking.endDate
                cmd.gracePeriodEndDate = facilityBooking.gracePeriodEndDate
                cmd.startingGracePeriodDays = facilityBooking.startingGracePeriodDays
                cmd.membershipPaid = facilityBooking.membershipPaid
                cmd.membershipCancel = facilityBooking.membershipCancel
                cmd.bookingType = facilityBooking.type
                cmd.activateMpc = facilityBooking.activateMpc
                cmd.trainerId = (facilityBooking.type.equals(BookingGroupType.PRIVATE_LESSON)) ? facilityBooking.trainerId : null
            }

            def booking = makeBooking(cmd, false)
            booking.order = order
            booking.order.getPlayersFromMetadata().each { Player player ->
                booking.addToPlayers(player)
                booking.save(flush: true)
            }

            if (order.metadata.selectedCustomerCategory) {
                booking.selectedCustomerCategory = PriceListCustomerCategory.findByIdAndFacilityAndOnlineSelect(
                        Long.valueOf(order.metadata.selectedCustomerCategory), slot.court.facility, true)
            }
            if (order.metadata.trainer) {
                TrainerRequest trainerRequest = TrainerRequest.create(order.issuerId, Long.parseLong(order.metadata?.trainer), slot.startTime, slot.endTime)

                if (trainerRequest) {
                    notificationService.sendTrainerRequestNotificationToTrainer(trainerRequest)
                }
            }

            booking.save()

            sendBookingEvent(booking, BookingEventType.UPDATED)

            couponService.updateTicketIfExists(order, booking)

            log.info("Sending booking notification mail for order ${order}")

            if (sendNotification) {
                notificationService.sendNewBookingNotification(booking, null)
                booking.players.each { Player player ->
                    if (!player.isBookingCustomer(booking.customer.email)) {
                        notificationService.sendNewBookingPlayerNotification(booking, player)
                    }
                }
            }

            log.info("Sucessfully sent booking notification for ${order}")
            return booking
        }

    }

    def sendBookingEvent(Booking booking, EventType<com.matchi.integration.events.Booking> eventType) {
        if (kafkaProducerService != null) {
            EventWrapper event = new EventWrapper(new com.matchi.integration.events.Booking(booking), eventType, InitiatorProvider.from(springSecurityService.getCurrentUser()))
            kafkaProducerService.send(event)
        }
    }

    @NotTransactional
    def findAllBookingsByInterval(Facility facility, ReadableInterval betweenInterval) {

        def bookingCriteria = Booking.createCriteria()
        def bookings = bookingCriteria.list {

            slot {
                between('startTime', betweenInterval.start.toDate(),
                        betweenInterval.end.toDate())

                court {
                    eq("facility", facility)
                }
            }

            order("id", "asc")
        }
        return bookings
    }

    @NotTransactional
    def getRecurringBookings(Booking booking) {
        def theDayAfter = new DateTime(booking.slot.startTime).plusDays(1).toDateMidnight().toDate()
        def startLocalTime = new LocalTime(booking.slot.startTime).toString("HH:mm:ss")
        def endLocalTime = new LocalTime(booking.slot.endTime).toString("HH:mm:ss")

        return Booking.createCriteria().listDistinct {
            createAlias("slot", "s")

            eq("group", booking.group)
            gt("s.startTime", theDayAfter)
            eq("s.court", booking.slot.court)

            sqlRestriction("DATE_FORMAT(start_time,'%T') = '${startLocalTime}'")
            sqlRestriction("DATE_FORMAT(end_time,'%T') = '${endLocalTime}'")
            sqlRestriction("DAYOFWEEK(start_time) = ${booking.slot.startTime[Calendar.DAY_OF_WEEK]}")
        }
    }

    void disableReminder(Booking booking) {
        booking.dateReminded = true
        booking.save()
    }

    @NotTransactional
    void exportBookings(List<String> slotIds, Writer writer, Locale locale) {
        CSVWriter csvWriter = new CSVWriter(writer, CSVWriter.DEFAULT_SEPARATOR,
                CSVWriter.DEFAULT_QUOTE_CHARACTER, CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                ExcelExportManager.CSV_LINE_BREAK)
        SimpleDateFormat dateFormat = new SimpleDateFormat(EXPORT_DATE_FORMAT)
        SimpleDateFormat timeFormat = new SimpleDateFormat(EXPORT_TIME_FORMAT)

        String invoiceNumber = messageSource.getMessage("bookingService.invoice.number", null, locale)
        String name = messageSource.getMessage("bookingService.name", null, locale)
        String date = messageSource.getMessage("bookingService.date", null, locale)
        String startTime = messageSource.getMessage("bookingService.start.time", null, locale)
        String endTime = messageSource.getMessage("bookingService.end.time", null, locale)
        String price = messageSource.getMessage("bookingService.price", null, locale)
        String creditAmount = messageSource.getMessage("bookingService.credit", null, locale)
        String comments = messageSource.getMessage("bookingService.comments", null, locale)
        String players = messageSource.getMessage("bookingService.players", null, locale)
        String unknownPlayersText = messageSource.getMessage("bookingService.unknownPlayers", null, locale)

        csvWriter.writeNext([
                invoiceNumber, name, date, startTime, endTime, price,
                invoiceNumber, name, price, creditAmount, comments, players
        ] as String[])

        Slot.withSession { session ->
            slotIds.collate(bookingExportBatchSize).each { ids ->
                Slot.findAllByIdInList(ids, [sort: "startTime"]).each { slot ->
                    if (slot.booking || slot.subscription) {
                        Customer c = slot.booking?.customer
                        Customer sc = slot.subscription?.customer

                        csvWriter.writeNext([
                                c?.number, c?.fullName(), dateFormat.format(slot.startTime),
                                timeFormat.format(slot.startTime), timeFormat.format(slot.endTime),
                                c ? priceListService.getBookingPrice(slot, c).price.toString() : null,
                                sc?.number, sc?.fullName(),
                                sc ? priceListService.getBookingPrice(slot, sc).price.toString() : null,
                                sc ? SlotRedeem.findBySlotAndInvoiceRowIsNotNull(
                                        slot, [fetch: [invoiceRow: "join"]])?.invoiceRow?.price?.abs() : null,
                                slot.booking?.comments,
                                getPlayersForExport(slot.booking, unknownPlayersText)
                        ] as String[])
                    }
                }
                session.clear()
            }
        }

        csvWriter.flush()
    }

    @NotTransactional
    void exportBookingCustomers(List slotIds, Writer writer) {
        def csvWriter = new CSVWriter(writer, CSVWriter.DEFAULT_SEPARATOR,
                CSVWriter.NO_QUOTE_CHARACTER, ExcelExportManager.CSV_LINE_BREAK)

        csvWriter.writeNext([
                "Kund nr", "E-post", "Typ", "Namn", "Adress 1", "Adress 2",
                "Post nr", "Ort", "Land", "Tel", "Mobil", "Målsman",
                "Antal bokningar i urval"
        ] as String[])

        def bookingsCount = [:]
        Slot.withCriteria {
            createAlias("booking", "b")
            createAlias("b.customer", "c")
            projections {
                groupProperty("c.id")
                count("id")
            }
            inList("id", slotIds)
        }.each {
            bookingsCount[it[0]] = it[1]
        }

        def customerIds = Slot.withCriteria {
            createAlias("subscription", "s")
            createAlias("s.customer", "c")
            projections {
                property("c.id")
            }
            inList("id", slotIds)
        }

        customerIds.addAll(bookingsCount.keySet())

        Customer.findAllByIdInList(customerIds).each { c ->
            csvWriter.writeNext([
                    c.number, c.email, c.type?.name(), c.fullName(), c.address1, c.address2,
                    c.zipcode, c.city, c.country, c.telephone, c.cellphone, c.guardianInfo,
                    bookingsCount[c.id] ?: 0
            ] as String[])
        }

        csvWriter.flush()
    }

    private void updateActivityOccasionTimeIfNecessary(Booking booking, Date start, Date end) {
        if (booking?.isActivity() && !(booking.group?.bookings?.size() > 1)) {
            def occasion = ActivityOccasion.withCriteria {
                bookings {
                    eq("id", booking.id)
                }
            }[0]
            if (occasion) {
                occasion.startTime = new LocalTime(start)
                occasion.endTime = new LocalTime(end)
                occasion.save()
            }
        }
    }

    private String getPlayersForExport(Booking booking, String unknownPlayersText) {
        def result = new StringBuilder()

        if (booking?.players) {
            def emails = new StringBuilder()
            def unknownPlayers = 0
            booking.players.each {
                if (it.customer) {
                    result << (result ? ", " : "") << it.customer.fullName()
                    if (it.customer.email) {
                        result << " <" << it.customer.email << ">"
                    }
                } else if (it.email) {
                    emails << (emails ? ", " : "") << it.email
                } else {
                    unknownPlayers++
                }
            }

            if (emails) {
                result << (result ? ", " : "") << emails
            }
            if (unknownPlayers) {
                result << (result ? ", " : "") << unknownPlayers << " " << unknownPlayersText
            }
        }

        result ? result.toString() : null
    }


    //TODO: Add functionality to just add payment to generic booking with payment...
}
