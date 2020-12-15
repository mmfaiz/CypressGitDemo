package com.matchi.payment

import com.matchi.*
import com.matchi.activities.trainingplanner.Trainer
import com.matchi.activities.trainingplanner.TrainerService
import com.matchi.adyen.AdyenException
import com.matchi.async.ScheduledTaskService
import com.matchi.coupon.CustomerCoupon
import com.matchi.coupon.PromoCode
import com.matchi.mpc.CodeRequest
import com.matchi.orders.Order
import com.matchi.price.Price
import com.matchi.price.PriceListCustomerCategory
import com.matchi.requests.TrainerRequest
import com.matchi.slots.AdjacentSlotGroup
import com.matchi.slots.SlotFilter
import grails.compiler.GrailsCompileStatic
import grails.converters.JSON
import grails.validation.Validateable
import groovy.transform.ToString
import org.apache.commons.validator.EmailValidator
import org.apache.http.HttpStatus
import org.grails.databinding.BindUsing
import org.joda.time.DateTime

import java.math.RoundingMode
import java.text.DecimalFormat

class BookingPaymentController extends GenericPaymentController {

    // We want to restrict number of slots at a time for performance and functionality
    static final Integer MAX_NUMBER_SLOTS_PER_BOOKING = 10

    BookingService bookingService
    UserService userService
    PriceListService priceListService
    CourtService courtService
    FacilityService facilityService
    TrainerService trainerService
    NotificationService notificationService
    ScheduledTaskService scheduledTaskService

    def index() {}

    /**
     * Confirmation modal where the user might change the selected slots and select payment method.
     */
    def confirm(ConfirmBookingCommand cmd) {
        Facility facility = Facility.get(cmd.facilityId)
        User user = userService.getLoggedInUser()

        if (!facility) {
            throw new IllegalArgumentException("Could not find facility with id ${cmd.facilityId}")
        }

        // Will trigger page reload if user is not logged in
        if (!user) {
            render status: HttpStatus.SC_UNAUTHORIZED
            return
        }

        /**
         * Group the selected slots by adjacency and court.
         * An adjacent slot group uses the first slot in the ordered sequence as key in the UI.
         */
        List<AdjacentSlotGroup> adjacentSlotGroups = getSlotsFromSelection(cmd, facility)
        List<Slot> slots = adjacentSlotGroups*.selectedSlots.flatten()

        // firstSlotPerSlot is a lookup table to add prices to the accurate row in the UI
        List<Slot> firstSlots = adjacentSlotGroups.collect { AdjacentSlotGroup slotGroup -> slotGroup.getFirstSlot() }
        Map<String, String> firstSlotPerSlot = slotService.createFirstSlotLookupTable(adjacentSlotGroups)

        /**
         * Make sure the slots can be booked.
         */
        if (slots.any { Slot slot -> !slot.isBookable() }) {
            render view: "error", model: [errorMessage: message(code: "payment.confirm.error1")]
            return
        } else if (slots.any { Slot slot -> slot.booking }) {
            render view: "error", model: [errorMessage: message(code: "payment.confirm.error2")]
            return
        }

        Customer customer = (user && facility) ? Customer.findByUserAndFacility(user, facility) : new Customer (user: user, facility: facility)

        // Avoid booking too much at a time
        if (slots.size() > MAX_NUMBER_SLOTS_PER_BOOKING) {
            render view: "error", model: [errorMessage: message(code: "payment.confirm.maxSlotsError", args: [MAX_NUMBER_SLOTS_PER_BOOKING])]
            return
        }

        def courtRestrictions = courtService.getCourtGroupRestrictions(customer, facility, slots)
        if (courtRestrictions && !courtRestrictions.isEmpty()) {
            render view: "error", model: [errorMessage: message(code: "payment.confirm.maxBookingsCourtError", args: [ courtRestrictions.get('courtGroup'), courtRestrictions.get('maxBookings')])]
            return
        }
        // Check if customer has reached booking limit or if user will by too many bookings
        if (!canBookMore(customer, facility, slots)) {
            render view: "error", model: [errorMessage: message(code: "payment.confirm.maxBookingsError", args: [facility.getMaxBookingsPerCustomer()])]
            return
        }

        // Price model contains info on total price, price per displayed row etc
        Map priceModel

        /**
         * Current price/booking restriction logic checks if a price is valid.
         * If not a valid price, then the price check throws an exception which we handle here and show the appropriate error message.
         */
        try {
            priceModel = buildPriceModel(adjacentSlotGroups, customer, [], null, facility)
        } catch (BookingRestrictionException bre) {
            render view: "error", model: [errorMessage: message(code: "orderPayment.confirm.bookingRestriction", args: [formatDate(date: bre.validFrom.toDate(), format: 'dd MMMM HH:mm'), bre.profiles])]
            return
        } catch (InvalidPriceException ipe) {
            render view: "error", model: [errorMessage: message(code: "orderPayment.confirm.cannotFindPrice")]
            return
        }

        Map paymentMethodsModel = getPaymentMethodsModel(slots, user, customer, facility, priceModel.totalPrice, slots.size() == 1 && hasRefererHeader())

        if (!paymentMethodsModel.methods) {
            render view: "error", model: [errorMessage: message(code: "orderPayment.confirm.cannotUseBookMany")]
            return
        }

        [
                formAction                  : "payEntryPoint",
                slots                       : slots,
                adjacentSlotGroups          : adjacentSlotGroups,
                firstSlotPerSlot            : firstSlotPerSlot,
                firstSlotIds                : firstSlots*.id,
                pricePerSlot                : priceModel.prices,
                totalPrice                  : priceModel.totalPrice,
                pricePerSlotRow             : priceModel.pricePerSlotRow,
                nPrices                     : priceModel.nPrices,
                facility                    : facility,
                user                        : user,
                customer                    : customer,
                bookTrainerModel            : getBookTrainerModel(slots, facility),
                dates                       : DateUtil.getUniqueDates(slots*.startTime),
                hasRefererHeader            : hasRefererHeader(),
                selectableCustomerCategories: PriceListCustomerCategory.findAllByFacilityAndOnlineSelectAndDeleted(facility, true, false, [sort: "name"]),
                hiddenFields                : [
                        [name: "slotIds", value: slots*.id.join(',')],
                        [name: "facilityId", value: facility.id],
                        [name: "start", value: params.start],
                        [name: "end", value: params.end]
                ],
                paymentMethodsModel         : paymentMethodsModel
        ]
    }

    def applyPromoCode(ConfirmUpdateCommand cmd) {
        Facility facility = Facility.get(params.facilityId)
        User user = getCurrentUser()
        List<Slot> slots = Slot.findAllByIdInList(cmd.slotIds)
        def promoCodeValidation = couponService.checkPromoCode(facility, user, params.promoCode, slots)
        def result
        if (promoCodeValidation.valid) {
            DecimalFormat df = new DecimalFormat()
            df.setMinimumFractionDigits(0)
            result = [status         : org.springframework.http.HttpStatus.OK.value(), id: promoCodeValidation.id,
                      message        : message(code: 'facilityPromoCodeController.process.validPromoCode') + ". " +
                              message(code: 'facilityPromoCodeController.process.discount') + " " +
                              ((promoCodeValidation.discountPercent) ? df.format(promoCodeValidation.discountPercent) + "%" : df.format(promoCodeValidation.discountAmount) + " " + facility.currency),
                      discountPercent: promoCodeValidation.discountPercent, discountAmount: promoCodeValidation.discountAmount]
        } else {
            result = [status: org.springframework.http.HttpStatus.NOT_FOUND.value(), message: message(code: promoCodeValidation.message)]
        }
        render result as JSON
    }

    /**
     * Either gets selected slots or the best suitable slot.
     * Returns the slots grouped as AdjacentSlotGroups.
     * @param confirmBookingCommand
     * @return
     */
    protected List<AdjacentSlotGroup> getSlotsFromSelection(ConfirmBookingCommand cmd, Facility facility) {
        List<AdjacentSlotGroup> slotGroups

        // Day view
        if (cmd.slotIds?.size() > 0) {
            List<Slot> slots = slotService.getSlots(cmd.slotIds)
            slotGroups = slotService.createAdjacentSlotGroupsWithSubsequentSlots(slots)

            // Week view
        } else if (cmd.start && cmd.end) {
            slotGroups = [getBestAlternativeSlotGroup(facility, new DateTime(cmd.start), new DateTime(cmd.end), cmd.sportIds, cmd.indoor)]
        }

        if (!slotGroups) {
            throw new IllegalArgumentException("Could not find any slots")
        }

        return slotGroups
    }

    /**
     * Necessary data for displaying trainers to book
     * @param slots
     * @param facility
     * @return
     */
    protected Map getBookTrainerModel(List<Slot> slots, Facility facility) {
        if (!facility.hasBookATrainer()) {
            return [:]
        }

        List<Map> availableTrainers = []
        boolean slotsAreConsecutive = slotService.areConsecutive(slots)
        boolean slotsAreSameCourt = slotService.areSameCourt(slots)
        if (slotsAreConsecutive && slotsAreSameCourt) {
            List<Slot> sortedSlots = slotService.sortByTime(slots)
            availableTrainers = trainerService.getAvailableTrainers(facility, sortedSlots.first().startTime, sortedSlots.last().endTime, sortedSlots.last().court.sport)?.collect { Trainer trainer ->
                return [id: trainer.id, name: trainer.fullName()]
            }
        }

        return [
                slotsAreConsecutive: slotsAreConsecutive,
                slotsAreSameCourt  : slotsAreSameCourt,
                availableTrainers  : availableTrainers
        ]
    }

    /**
     * Contains the model for updating the modal on slot changes
     * @param cmd
     * @return
     */
    def updateConfirmModalModel(ConfirmUpdateCommand cmd) {
        if (!springSecurityService.isLoggedIn()) {
            render status: HttpStatus.SC_UNAUTHORIZED
            return
        }

        if (cmd.hasErrors()) {
            render status: HttpStatus.SC_BAD_REQUEST
            return
        }

        List<Slot> slots = Slot.findAllByIdInList(cmd.slotIds)
        List<Slot> firstSlots = slots.findAll { Slot slot -> slot.id in cmd.firstSlotIds }
        User user = getCurrentUser()
        Facility facility = slots[0].court.facility
        Customer customer = ((user && facility) ? Customer.findByUserAndFacility(user, facility) : null) ?: new Customer(user: user, facility: facility)

        if (slots.size() > MAX_NUMBER_SLOTS_PER_BOOKING) {
            render status: HttpStatus.SC_METHOD_NOT_ALLOWED, text: message(code: "payment.confirm.maxSlotsError", args: [MAX_NUMBER_SLOTS_PER_BOOKING])
            return
        }

        def courtRestrictions = courtService.getCourtGroupRestrictions(customer, facility, slots)
        if (courtRestrictions) {
            render view: "error", model: [errorMessage: message(code: "payment.confirm.maxBookingsCourtError", args: [ courtRestrictions.get('courtGroup'), courtRestrictions.get('maxBookings')])]
            return
        }

        if (!canBookMore(customer, facility, slots)) {
            render status: HttpStatus.SC_METHOD_NOT_ALLOWED, text: message(code: "payment.confirm.maxBookingsError", args: [facility.getMaxBookingsPerCustomer()])
            return
        }

        try {
            Map model = buildPriceModel(slotService.groupByCourtAndAdjacency(slots, firstSlots), customer, cmd.playerEmails, cmd.selectedCustomerCategory, facility)
            Map paymentMethodsModel = getPaymentMethodsModel(slots, user, customer, facility, model.totalPrice, slots.size() == 1 && hasRefererHeader())

            if (!paymentMethodsModel.methods) {
                render status: HttpStatus.SC_METHOD_NOT_ALLOWED, text: message(code: "orderPayment.confirm.cannotUseBookMany")
                return
            }

            model << paymentMethodsModel
            model.bookTrainerModel = getBookTrainerModel(slots, facility)

            if (cmd.promoCodeId) {
                int decimalPoints = grailsApplication.config.matchi.settings.currency[facility.currency].decimalPoints
                PromoCode promoCode = PromoCode.findByIdAndFacility(cmd.promoCodeId, facility)
                if (promoCode?.discountPercent) {
                    model.totalPrice = ((100 - promoCode.discountPercent).divide(100) * model.totalPrice).setScale(decimalPoints, RoundingMode.HALF_UP)
                    model.pricePerSlotRow = model.pricePerSlotRow.collectEntries { entry ->
                        [entry.key, ((100 - promoCode.discountPercent).divide(100) * entry.value).setScale(decimalPoints, RoundingMode.HALF_UP)]
                    }
                } else if (promoCode?.discountAmount) {
                    def newPrice = (model.totalPrice - promoCode.discountAmount).max(0)
                    def discountProportion = newPrice / model.totalPrice
                    def totalDiscounted = 0
                    model.pricePerSlotRow = model.pricePerSlotRow.collectEntries { entry ->
                        def slotPrice = new BigDecimal(entry.value * discountProportion).setScale(decimalPoints, RoundingMode.HALF_UP)
                        totalDiscounted += (entry.value - slotPrice)
                        [entry.key, slotPrice]
                    }
                    Double extraPenny = model.totalPrice - newPrice - totalDiscounted
                    String lastSlot = model.pricePerSlotRow.keySet().last()
                    model.pricePerSlotRow.put(lastSlot, model.pricePerSlotRow[lastSlot] + extraPenny)
                    model.totalPrice = newPrice
                }
            }

            render model as JSON
        } catch (InvalidPriceException ipe) {
            render status: HttpStatus.SC_METHOD_NOT_ALLOWED, text: message(code: 'payment.confirm.bookingRestrictions')
        }
    }

    /**
     * Generates a map of information with total price, price per slot and number of slots with each price
     * @return
     */
    private Map buildPriceModel(List<AdjacentSlotGroup> slotGroups, Customer customer, List<String> playerEmails, Long selectedCustomerCategory, Facility facility) throws InvalidPriceException {
        Map<String, Long> prices = [:]
        Map<String, Long> pricePerSlotRow = [:]
        Map<String, Long> punchCardPricePerSlotRow = [:]
        def priceCalcLog = [:]

        slotGroups.each { AdjacentSlotGroup slotGroup ->
            Map<String, Long> pricesForGroup = priceListService.getPricesForSlots(
                    slotGroup.selectedSlots, customer, playerEmails,
                    selectedCustomerCategory, priceCalcLog)

            punchCardPricePerSlotRow.put(slotGroup.firstSlot.id, pricesForGroup.findAll { it.value != 0L }.size())
            Long totalPriceForGroup = pricesForGroup.values().sum()
            pricePerSlotRow.put(slotGroup.firstSlot.id, totalPriceForGroup)

            prices << pricesForGroup
        }

        Long totalPrice = prices.values().sum()

        /**
         * Extra check to avoid presenting something wrong.
         * Should not happen of course.
         */
        if (pricePerSlotRow.values().sum() != totalPrice) {
            throw new IllegalStateException("Sum ${pricePerSlotRow?.values()?.sum()} of prices per slot row does not equal total price ${totalPrice}")
        }

        return [
                prices                  : prices,
                totalPrice              : totalPrice,
                nPrices                 : prices.values().findAll { it != 0 }.size(),
                pricePerSlotRow         : pricePerSlotRow,
                punchCardPricePerSlotRow: punchCardPricePerSlotRow,
                pricePerPlayer          : priceCalcLog
        ]
    }

    /**
     * Entry point for paying. Will choose action method based on input.
     * Also performs checks and validation before sending to correct payment action method.
     */
    def payEntryPoint(MultipleBookingPaymentCommand cmd) {
        log.info "Verify user is still logged in before booking payment"
        if (!springSecurityService.isLoggedIn()) {
            render status: HttpStatus.SC_UNAUTHORIZED
            return
        }

        if (cmd.hasErrors()) {
            log.error "Unable to make bookings because of parameters errors: $cmd.errors"
            render view: "showError", model: [message: message(code: 'paymentController.process.errors.couldNotProcess')]
            return
        }

        User user = getCurrentUser()
        PaymentMethod method = cmd.method
        if (method == PaymentMethod.COUPON) {
            cmd.promoCodeId = null
        }
        List<Slot> slots = slotService.getSlots(cmd.slotIds)
        Facility facility = slots[0].court.facility
        Customer customer = Customer.findByFacilityAndUser(facility, user)

        if (!method) {
            throw new IllegalStateException("No payment method")
        }

        if (slots.size() > MAX_NUMBER_SLOTS_PER_BOOKING) {
            render view: "showError", model: [message: message(code: "payment.confirm.maxSlotsError", args: [MAX_NUMBER_SLOTS_PER_BOOKING])]
            return
        }

        def courtRestrictions = courtService.getCourtGroupRestrictions(customer, facility, slots)
        if (courtRestrictions) {
            render view: "error", model: [errorMessage: message(code: "payment.confirm.maxBookingsCourtError", args: [ courtRestrictions.get('courtGroup'), courtRestrictions.get('maxBookings')])]
            return
        }
        // Extra check to make sure the customer does not surpass the limit of max bookings. Has UI check.
        if (!canBookMore(customer, facility, slots)) {
            render view: "showError", model: [message: message(code: "payment.confirm.maxBookingsError", args: [facility.getMaxBookingsPerCustomer()])]
            return
        }

        // Make sure same facility on each slot
        if (slots.collect { Slot slot -> slot.court.facility.id }.toSet().size() > 1) {
            throw new IllegalStateException("Cannot book on more than 1 facility!")
        }

        // Make sure none of the slots has any booking. Could potentially happen that someone else books while you are in the modal
        if (slots.any { Slot slot -> slot.booking }) {
            render view: "showError", model: [message: message(code: "paymentController.process.errors.couldNotProcess")]
            return
        }

        // Make sure emails are correct. Has UI check.
        // Reason we reset the emails is to filter away empty strings.
        cmd.playerEmail = cmd.playerEmail?.findAll { it.trim() }
        if (cmd.playerEmail) {
            if (cmd.playerEmail.any { !EmailValidator.getInstance().isValid(it) }) {
                render view: "showError", model: [message: message(code: "payment.players.email.error")]
                return
            }
        }

        // Temporary special case for saved cards and 1 slot
        // To be refactored with slot extension logic
        if (cmd.method == PaymentMethod.CREDIT_CARD_RECUR && cmd.slotIds.size() == 1) {
            payRedirect(cmd, slots[0], user, facility, customer, method)
            return
        }

        // We can only use redirect if one slot
        if (cmd.method.requiresRedirect && cmd.slotIds.size() == 1) {
            payRedirect(cmd, slots[0], user, facility, customer, method)
        } else if (!cmd.method.requiresRedirect) {
            payWithoutRedirect(cmd, slots, user, method)
        } else {
            throw new IllegalStateException("Trying to use redirect for multiple slots, aborting...")
        }
    }

    /**
     * Payment logic for several bookings, handled similarly to the API logic
     */
    protected payWithoutRedirect(MultipleBookingPaymentCommand cmd, List<Slot> slots, User user, PaymentMethod method) {
        List<Booking> bookings = []
        List<Order> orders = []
        Facility facility = slots.first().court.facility

        // We don't redirect the user here. Has UI check.
        if (method.requiresRedirect) {
            throw new IllegalStateException("Wrong controller method used here")
        }

        if (facility.showBookingHolder) {
            user.anonymouseBooking = params.boolean("hideBookingHolder")
            user.save()
        }

        try {

            slots.each { Slot slot ->
                Order order = paymentService.createBookingOrder(slot, user, Order.ORIGIN_WEB)
                order.assertCustomer()

                order.price = priceListService.getPriceForSlot(slot, order.customer, cmd.playerEmail, cmd.selectedCustomerCategory).price
                order.save()

                if (cmd.playerEmail) {
                    addPlayers(cmd.playerEmail, order)
                    order.save()
                }

                if (cmd.selectedCustomerCategory) {
                    order.metadata.selectedCustomerCategory = cmd.selectedCustomerCategory as String
                    order.save()
                }

                if (user.anonymouseBooking) {
                    order.metadata.hideBookingHolder = "true"
                    order.save()
                }

                orders << order
            }

            if (cmd.promoCodeId) {
                PromoCode promoCode = couponService.getValidPromoCode(cmd.promoCodeId, facility)
                if (!promoCode || !slots.every { Slot slot -> promoCode.accept(slot) }) {
                    render view: "showError", model: [message: message(code: 'paymentController.process.errors.invalidPromoCode')]
                    return
                } else if (couponService.isPromoCodeUsed(user, promoCode)) {
                    render view: "showError", model: [message: message(code: 'paymentController.process.errors.promoCodeAlreadyUsed')]
                    return
                }
                couponService.usePromoCodeForOrders(promoCode, orders, grailsApplication.config.matchi.settings.currency[facility.currency].decimalPoints)
                new CustomerCoupon(customer: customerService.getOrCreateUserCustomer(user, facility), coupon: promoCode,
                        createdBy: user).save()
            }

            List<Order> freeOrders = []
            List<Order> nonFreeOrders = []

            orders.each { Order order ->
                BigDecimal total = order.total()

                if (total > 0) {
                    nonFreeOrders << order
                } else {
                    freeOrders << order
                }
            }

            if (nonFreeOrders.size() > 0) {
                payForOrders(nonFreeOrders, method, user, cmd.customerCouponId)
            }

            freeOrders.each { Order order ->
                order.status = Order.Status.COMPLETED
            }

            orders.each { Order order ->
                bookings << bookingService.book(order, false)
            }

            if (cmd.trainer) {
                List<Slot> sortedSlots = slotService.sortByTime(slots)
                TrainerRequest trainerRequest = TrainerRequest.create(user.id, cmd.trainer, sortedSlots.first().startTime, sortedSlots.last().endTime)

                if (trainerRequest) {
                    notificationService.sendTrainerRequestNotificationToTrainer(trainerRequest)
                }
            }

            List<Long> bookingIds = bookings*.id

            scheduledTaskService.scheduleTask(message(code: 'scheduledTask.sendEmailConfirmation.taskName'), user.id, null) { taskId ->
                bookingIds.each { Long id ->
                    Booking booking = Booking.get(id)
                    notificationService.sendNewBookingNotification(booking, null)
                    booking.players.each {
                        if (it.customer?.email != booking.customer.email && it.email != booking.customer.email) {
                            notificationService.sendNewBookingPlayerNotification(booking, it)
                        }
                    }
                }
            }

            redirect action: "receipt", params: [orderIds: Order.joinOrderIds(orders*.id)]
        }
        catch (AdyenException e) {
            def orderIds = Order.joinOrderIds(orders*.id)
            log.warn "Orders [$orderIds] failed with error: ${e?.getMessage()}"

            // ROLLBACK
            bookingService.cancelOrders(orders, user)

            render view: "showError", model: [message: e?.getMessage()]
            return
        }
        catch (Exception e) {
            def orderIds = Order.joinOrderIds(orders*.id)

            log.error "Orders [$orderIds] failed with error: ${e?.getMessage()}", e

            // ROLLBACK
            bookingService.cancelOrders(orders, user)

            render view: "showError", model: [message: message(code: 'paymentController.process.errors.couldNotProcess')]
            return
        }
    }

    /**
     * Initiates the payment process with selected paymentMethod. PaymentMethod must be redirect or use payment provider.
     */
    protected payRedirect(MultipleBookingPaymentCommand cmd, Slot slot, User user, Facility facility, Customer customer, PaymentMethod method) {
        log.debug(params)

        // We only use this action method for new cards or local payment methods. Has UI check.
        if (!method.requiresRedirect || !method.isUsingPaymentGatewayMethod()) {
            // throw new IllegalStateException("Wrong controller method used here")
            // Exception is currently not thrown due to MW-4141. Special case for CREDIT_CARD_RECUR since it might use payWithoutRedirect if > 1 slot.
            // It would require many more changes to set method.requiresRedirect = true right now, since when it is true we assume it cannot go without redirect.
        }

        // The referer header is required to create a return URL from the payment provider. Has UI check.
        if (!hasRefererHeader()) {
            render view: "showError", model: [errorMessage: message(code: "payment.confirm.refererMissing")]
            return
        }

        // We might fail retrieving a price, if for example the user selects a slot having a booking restriction. Has UI check.
        Price price
        try {
            price = priceListService.getPriceForSlot(slot, customer, cmd.playerEmail, cmd.selectedCustomerCategory)
        } catch (InvalidPriceException ipe) {
            render view: "showError", model: [message: message(code: "payment.confirm.bookingRestrictions")]
            return
        }

        Order order = new Order()
        order.metadata = [cancelUrl: createLink(controller: "bookingPayment", action: "confirm", params: params), processUrl: createLink(action: "process")]
        order.metadata << [slotId: slot.id]
        order.issuer = user
        order.user = user
        order.customer = customer
        order.dateDelivery = slot.startTime
        order.facility = facility
        order.origin = Order.ORIGIN_WEB // order coming from web client
        order.description = slot.getDescription()
        order.price = price.price
        order.vat = price.VATAmount
        order.article = Order.Article.BOOKING

        if (!order.validate()) {
            throw new IllegalStateException("Cannot save order")
        }

        order.save(flush: true)

        if (cmd.playerEmail) {
            addPlayers(cmd.playerEmail, order)
            order.save()
        }

        if (cmd.selectedCustomerCategory) {
            order.metadata.selectedCustomerCategory = cmd.selectedCustomerCategory as String
            order.save()
        }

        String finishUrl = PaymentFlow.createFinishUrl(request.getHeader(REFERER_KEY), order.id)
        startPaymentFlow(order.id, finishUrl)

        log.info(LogHelper.formatOrder("Processing payment", order))

        if (order.facility.showBookingHolder) {
            user.anonymouseBooking = params.boolean("hideBookingHolder")
            user.save()
            if (user.anonymouseBooking) {
                order.metadata.hideBookingHolder = "true"
                order.save()
            }
        }

        if (cmd.trainer) {
            order.metadata.trainer = cmd.trainer as String
            order.save()
        }

        redirect(getPaymentProviderParameters(method, order, user, cmd.promoCodeId))
    }

    private def payForOrders(List<Order> orders, PaymentMethod paymentMethod, User user, Long offerId = null) {

        log.info("Handling payments for ${orders.size()} booking orders")

        //Iterate through unique orders to prevent double payments
        orders.unique { it.id }.each { order ->
            switch (paymentMethod) {
                case PaymentMethod.COUPON:
                    paymentService.handleOfferPayment(order, offerId, paymentMethod, user)
                    break;
                case PaymentMethod.GIFT_CARD:
                    paymentService.handleOfferPayment(order, offerId, paymentMethod, user)
                    break;
                case PaymentMethod.CREDIT_CARD_RECUR:
                    paymentService.handleCreditCardPayment(order, user)

                    break;
            }

            if (!order.isFinalPaid()) {
                throw new Exception("Could not process payment on order ${order.id}")
            }
        }

    }

    /**
     * Adjust order price to added players
     * @param playerEmails
     * @param order
     * @param slot
     */
    private void addPlayers(List<String> playerEmails, Order order) {
        def customers = []
        playerEmails.each { email ->
            def c = Customer.findByFacilityAndEmailAndArchived(order.facility, email as String, false)
            customers << (c ?: new Customer(email: email))
        }
        order.addPlayersToMetadata(customers)
    }

    def receipt() {
        List<Long> orderIds = Order.getOrderIdsFromParams(params.orderIds ? params.orderIds : params.orderId)
        List<Order> orders = Order.findAllByIdInList(orderIds)

        log.info(LogHelper.formatOrder("Showing receipt!", orders.first()))

        if (!(orders?.size() > 0)) { // check user access to order
            throw new IllegalStateException("No order with id ${params.orderIds} found")
        }

        PaymentFlow paymentFlow
        if (orders.size() == 1) {
            Order order = orders.first()
            paymentFlow = PaymentFlow.popInstance(session, order.id)
        }

        List receiptOrders = []

        orders.each { Order order ->
            def slot = Slot.get(order.metadata.slotId)
            def accessCode
            if (slot?.court?.facility?.hasMPC()) {
                accessCode = CodeRequest.findByBooking(slot?.booking)?.code
            } else {
                accessCode = FacilityAccessCode.validAccessCodeFor(slot)?.content
            }

            receiptOrders << [booking: slot.booking, order: order, accessCode: accessCode]
        }

        render view: "receipt", model: [orders: receiptOrders, showPage: paymentFlow ? paymentFlow.showPage() : false]
    }

    def error(Long orderId) {
        PaymentFlow paymentFlow = PaymentFlow.popInstance(session, orderId)
        render view: "showError", model: [message: paymentFlow?.errorMessage]
    }


    @GrailsCompileStatic
    def bookingPlayers(Long facilityId, String q) {
        def emails = []
        def customer = customerService.getCurrentCustomer(facilityService.getFacility(facilityId))

        if (customer && q) {
            emails = customerService.getConnectedPlayersData(
                    customer, params.list("playerEmail"), q.trim())
        }

        render(emails as JSON)
    }

    @Override
    protected void processArticle(Order order) throws ArticleCreationException {
        Booking booking = Booking.findByOrder(order)
        log.info(LogHelper.formatOrder("Process article", order, booking))

        if (booking) {
            String alreadyProcessedError = message(code: 'paymentController.process.errors.alreadyProcessed') as String
            log.error(LogHelper.formatOrder(alreadyProcessedError, order, booking))
            throw new IllegalStateException(alreadyProcessedError)
        }

        if (!order?.issuer?.id?.equals(getCurrentUser()?.id)) {
            String authError = message(code: 'paymentController.process.errors.authError') as String
            log.error(LogHelper.formatOrder(authError, order, booking))
            throw new ArticleCreationException(authError)
        }

        log.info(LogHelper.formatOrder("Process booking for order", order, booking))
        try {
            booking = bookingService.book(order, false)
            log.info(LogHelper.formatOrder("Process booking for order successfully", order, booking))
        } catch (Throwable t) {
            log.error(LogHelper.formatOrder("Error while processing booking for order", order, booking), t)
            throw new ArticleCreationException(message(code: 'paymentController.process.errors.couldNotProcess') as String)
        }
        Long id
        User user
        try {
            id = booking.id
            user = getCurrentUser()
        } catch (Throwable t) {
            // DEBUG INFO
            log.error(LogHelper.formatOrder("Error while processing getCurrentUser for order", order, booking), t)
            throw t
        }
        try {
            scheduledTaskService.scheduleTask(message(code: 'scheduledTask.sendEmailConfirmation.taskName'), user.id, null) { taskId ->
                Booking b = Booking.get(id)
                notificationService.sendNewBookingNotification(b, null)
                b.players.each {
                    if (it.customer?.email != b.customer.email && it.email != b.customer.email) {
                        notificationService.sendNewBookingPlayerNotification(b, it)
                    }
                }
            }
        } catch (Throwable t) {
            // DEBUG INFO
            log.error(LogHelper.formatOrder("Error while processing scheduleTask for order", order, booking), t)
            throw t
        }
        try {
            if (params.promoCodeId) {
                PromoCode promoCode = couponService.getValidPromoCode(params.promoCodeId as Long, order.facility)
                new CustomerCoupon(customer: customerService.getOrCreateUserCustomer(order.user, order.facility), coupon: promoCode,
                        createdBy: order.issuer).save()
            }
        } catch (Throwable t) {
            // DEBUG INFO
            log.error(LogHelper.formatOrder("Error while processing promoCode for order", order, booking), t)
            throw t
        }

        log.info(LogHelper.formatOrder("Showing receipt", order, booking))
        redirectToFinish(PaymentFlow.State.RECEIPT, order.id, [orderId: order.id])
    }

    @Override
    protected Order.Article getArticleType() {
        return Order.Article.BOOKING
    }

    /**
     * Returns the best alternative slot together with subsequent slot, as an AdjacnetSlotGroup.
     * "Best" in this case means having the most number of subsequent slots. This makes it easier for the user
     * to find many slots to book at once. Which is a good thing.
     * @param facility
     * @param facility
     * @param facility
     * @param facility
     * @param facility
     * @return
     */
    protected AdjacentSlotGroup getBestAlternativeSlotGroup(Facility facility, DateTime from, DateTime to, List<Long> sportIds, Boolean isIndoor) {
        List<Slot> alternativeSlots = getAlternativeSlots(facility, from, to, sportIds, isIndoor)

        // We want to find the slot having the most trailing slots, so it becomes easier for the user to book as much as possible :)
        List<AdjacentSlotGroup> adjacentSlotGroups = slotService.createAdjacentSlotGroupsWithSubsequentSlots(alternativeSlots)
        return adjacentSlotGroups.sort { AdjacentSlotGroup slotGroup -> slotGroup.subsequentSlots.size() }.last()
    }

    /**
     * Returns a list of slots that can replace another slot based on time, sport, facility, indoor etc.
     * Was earlier in BookingPaymentController.
     * @param facility
     * @param start
     * @param end
     * @param sportIds
     * @param indoor
     * @return
     */
    protected List<Slot> getAlternativeSlots(Facility facility, DateTime start, DateTime end, List<Long> sportIds, Boolean indoor) {
        List<Slot> slots = []
        User user = userService.getLoggedInUser()
        if (user) {
            def surfaces = Court.Surface.list()
            def sports = Sport.findAll()

            if (sportIds?.size() > 0) {
                sports = sports.findAll { sportIds.contains(it.id) }
            }

            SlotFilter slotsFilterCommand = new SlotFilter()
            slotsFilterCommand.courts = courtService.findUsersCourts([facility], sports, surfaces, indoor, getCurrentUser())
            slotsFilterCommand.from = start
            slotsFilterCommand.to = end
            slotsFilterCommand.onlyFreeSlots = true

            slots = slotService.getSlots(slotsFilterCommand).sort()
            slots = slots.findAll { it.isBookable() }
        }

        return slots
    }

    /**
     * Checks if Customer can book more, either if the facility has a limit that will be applied to new customer
     * @param customer
     * @param facility
     * @param slots
     * @return
     */
    protected boolean canBookMore(Customer customer, Facility facility, List<Slot> slots) {
        if (customer) {
            return customer.canBookMoreSlots(slots.size())
        } else if (facility.hasBookingLimitPerCustomer()) {
            return facility.getMaxBookingsPerCustomer() >= slots.size()
        }

        return true
    }
}

@ToString(includeNames = true)
@Validateable(nullable = true)
class ConfirmBookingCommand {
    Long facilityId
    Long start
    Long end

    List<Long> sportIds

    @BindUsing({ obj, src -> src["slotIds"]?.tokenize(",") })
    List<String> slotIds

    Boolean indoor

    static constraints = {
        facilityId nullable: false
        start nullable: true
        indoor nullable: true

        sportIds nullable: true
        slotIds nullable: false

        indoor nullable: true
    }
}

@Validateable
@ToString(includeNames = true)
class MultipleBookingPaymentCommand {

    @BindUsing({ obj, src -> src["slotIds"]?.tokenize(",") })
    List<String> slotIds
    Long facilityId
    Long customerCouponId
    PaymentMethod method
    Long trainer
    Long promoCodeId

    List<String> playerEmail = []

    Long selectedCustomerCategory

    static constraints = {
        slotIds nullable: false, minSize: 1
        customerCouponId nullable: true, validator: { val, obj ->
            val || (obj.method != PaymentMethod.COUPON && obj.method != PaymentMethod.GIFT_CARD)
        }
        playerEmail nullable: true
        selectedCustomerCategory nullable: true
        trainer nullable: true
        promoCodeId nullable: true
    }
}

@Validateable(nullable = true)
@ToString(includeNames = true)
class ConfirmUpdateCommand {
    @BindUsing({ obj, src -> src["slotIds"]?.tokenize(",") })
    List<String> slotIds

    @BindUsing({ obj, src -> src["firstSlotIds"]?.tokenize(",") })
    List<String> firstSlotIds

    @BindUsing({ obj, src -> src["playerEmails"]?.tokenize(",") })
    List<String> playerEmails
    Long promoCodeId

    Long selectedCustomerCategory

    static constraints = {
        slotIds nullable: false, minSize: 1
        firstSlotIds nullable: false, minSize: 1
        playerEmails nullable: true
        promoCodeId nullable: true
        selectedCustomerCategory nullable: true
    }
}