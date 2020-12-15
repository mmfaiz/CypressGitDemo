package com.matchi

import com.matchi.activities.ActivityOccasion
import com.matchi.adyen.AdyenException
import com.matchi.adyen.authorization.AdyenAuthorizationBuilder
import com.matchi.adyen.authorization.AdyenAuthorizationBuilderFactory
import com.matchi.adyen.authorization.AdyenRecurringProcessingModel
import com.matchi.coupon.CustomerCoupon
import com.matchi.coupon.CustomerCouponTicket
import com.matchi.coupon.Offer
import com.matchi.invoice.InvoiceRow
import com.matchi.orders.AdyenOrderPayment
import com.matchi.orders.CouponOrderPayment
import com.matchi.orders.Order
import com.matchi.orders.OrderPayment
import com.matchi.payment.ArticleType
import com.matchi.payment.PaymentException
import com.matchi.payment.PaymentMethod
import com.matchi.payment.PaymentStatus
import com.matchi.price.Price
import grails.transaction.NotTransactional
import org.joda.time.DateTime
import org.joda.time.Interval
import org.joda.time.format.DateTimeFormat

class PaymentService {
    static transactional = true
    def grailsApplication
    def springSecurityService
    def priceListService
    def slotService
    def couponService
    def boxnetManager
    def customerService
    def invoiceService
    def couponPaymentService
    def cashService

    def registerPayment(PaymentOrder order, def paymentParameters = [:]) {

        log.debug("Registering payment with order user ${order.user}")
        customerService.getOrCreateUserCustomer(order.user, order.facility)
        Payment payment = new Payment(order)
        log.debug("Created payment with customer ${payment.customer}")

        payment.amount = String.valueOf(toPaymentPriceFormat(order.toAmount().amount))
        payment.vat = String.valueOf(toPaymentPriceFormat(order.toAmount().VAT))
        payment.status = PaymentStatus.PENDING
        payment.save()

        try {
            switch (order.method) {
                case PaymentMethod.CREDIT_CARD:
                    throw new IllegalStateException("Credit card not available here")
                    break
                case PaymentMethod.CREDIT_CARD_RECUR:
                    throw new IllegalStateException("Credit card not available here")
                    break
                case PaymentMethod.FREE:
                    payment.status = PaymentStatus.OK
                    break
                case PaymentMethod.COUPON:
                    couponPayment(order, payment)
                    break
                case PaymentMethod.GIFT_CARD:
                    couponPayment(order, payment)
                    break
                case PaymentMethod.CASH:
                    payment.status = PaymentStatus.OK
                    break
                case PaymentMethod.INVOICE:
                    invoicePayment(order, payment)
                    break
                case PaymentMethod.REGISTER:
                    payment.status = PaymentStatus.OK
                    break
            }

            if (!payment.valid()) {
                throw new PaymentException("Payment not valid", payment)
            }

        } catch (PaymentException pe) {
            log.error("Error while processing payment", pe)
            payment.status = PaymentStatus.FAILED
        }

        if (!payment.save()) {
            throw new PaymentException("Could not save payment (${payment.errors})")
        }

        return payment
    }

    def registerPaymentTransaction(Payment payment, String paidAmount, def cashRegisterTransactionId = null, InvoiceRow invoiceRow = null) {

        PaymentTransaction transaction = new PaymentTransaction()
        transaction.paidAmount = Long.parseLong(paidAmount)
        transaction.cashRegisterTransactionId = cashRegisterTransactionId
        transaction.invoiceRow = invoiceRow

        payment.addToPaymentTransactions(transaction)
        payment.status = payment.getPaymentStatus()

        if (!payment.save()) {
            throw new PaymentException("Could not save payment (${payment.errors})")
        }

        return transaction
    }

    def couponPayment(PaymentOrder paymentOrder, Payment payment) {
        payment.customerCoupon = CustomerCoupon.get(paymentOrder.customerCouponId)
        payment.status = PaymentStatus.OK

        return payment
    }

    def invoicePayment(PaymentOrder paymentOrder, Payment payment) {
        def slot = slotService.getSlot(paymentOrder.orderParameters.get("slotId"))
        def facility = slot.court.facility

        InvoiceRow row = new InvoiceRow()
        row.customer = payment.customer
        row.description = (facility?.bookingInvoiceRowDescription ? facility?.bookingInvoiceRowDescription + ": " : "") + slot?.getShortDescription()
        row.externalArticleId = facility?.bookingInvoiceRowExternalArticleId
        row.organization = invoiceService.getOrganization(facility?.bookingInvoiceRowOrganizationId)
        row.amount = 1
        row.vat = facility.vat
        row.price = payment.amountFormattedAmountOnly()
        row.createdBy = springSecurityService.getCurrentUser()

        row.save()

        log.debug("Created invoiceRow w. price: ${row.price} && vat: ${row.vat}")

        registerPaymentTransaction(payment, payment.amountFormattedAmountOnly().toString(), null, row)
        return payment
    }

    @NotTransactional
    def getFacilityPayments(Facility facility, Interval interval,
                            def methods = [PaymentMethod.list().collect { it.toString() }],
                            List courtIds = null, String timeRestrictions = null, String incomeRestriction = null) {

        def criteria = Payment.createCriteria()
        criteria.listDistinct {
            join "customer"
            join "orderParameters"

            eq("facility", facility)
            eq("status", PaymentStatus.OK)
            ge("dateDelivery", interval.start.toDate())
            le("dateDelivery", interval.end.toDate())
            or {
                inList("method", methods.collect { PaymentMethod.valueOf(it) })
            }

            isNull("dateReversed")
            isNull("dateAnnulled")

            if (courtIds || timeRestrictions || incomeRestriction) {
                createAlias("booking", "b")
                createAlias("b.slot", "s")
                createAlias("s.court", "c")
                if (courtIds) {
                    inList("c.id", courtIds)
                }
                if (timeRestrictions) {
                    sqlRestriction("1 $timeRestrictions")
                }
                if (incomeRestriction) {
                    sqlRestriction("1 $incomeRestriction")
                }
            }

            order("dateDelivery", "asc")
        }
    }

    @NotTransactional
    def getAnyPaymentInfoByUser(User user) {
        return PaymentInfo.findByUser(user, [sort: "provider", order: "asc"])
    }

    Order createBookingOrder(Slot slot, User user, String origin = Order.ORIGIN_WEB,
                             List playerCustomerIds = null, Integer unknownPlayers = null) {
        createBookingOrder(slot, Customer.findByUserAndFacility(user, slot.court.facility),
                origin, playerCustomerIds, unknownPlayers, user)
    }

    Order createBookingOrder(Slot slot, Customer customer, String origin = Order.ORIGIN_WEB,
                             List playerCustomerIds = null, Integer unknownPlayers = null, User issuer = null) {

        Order order = new Order()
        order.metadata = [slotId: slot.id]
        order.issuer = issuer ?: springSecurityService.getCurrentUser()
        order.user = customer?.user ?: issuer
        order.customer = customer
        order.dateDelivery = slot.startTime
        order.facility = slot.court.facility
        order.origin = origin
        order.description = slot.getDescription()
        order.article = Order.Article.BOOKING

        def price

        // TODO: Use PriceListService.getPriceForSlot() instead. Postponing to not affect API in current story.
        if (slot.court.facility.isFacilityPropertyEnabled(
                FacilityProperty.FacilityPropertyKey.FEATURE_CALCULATE_MULTIPLE_PLAYERS_PRICE.name())) {

            List playerCustomers = []
            if (playerCustomerIds || unknownPlayers) {
                playerCustomers = playerCustomerIds.collect {
                    customerService.getCustomer(it)
                }
                unknownPlayers?.times {
                    playerCustomers << new Customer()
                }
            }
            price = priceListService.getAvgBookingPrice(slot, playerCustomers ?: [customer])
        } else {
            price = priceListService.getBookingPrice(slot, customer ?: new Customer())
        }
        order.price = price.price
        order.vat = price.VATAmount

        order.save()
    }

    /**
     * Stale Boxnet usage
     */
    def createPaymentOrder(User user, Slot slot) {
        def customer = getPayingCustomer(user, slot)
        def customers = slot.booking.players.collect { it.customer ?: new Customer() }

        Price price = (customers && slot.court.facility.isFacilityPropertyEnabled(
                FacilityProperty.FacilityPropertyKey.FEATURE_CALCULATE_MULTIPLE_PLAYERS_PRICE.name())) ?
                priceListService.getAvgBookingPrice(slot, customers) :
                priceListService.getBookingPrice(slot, customer)

        PaymentOrder order = new PaymentOrder()
        order.articleType = ArticleType.BOOKING

        order.orderParameters = new HashMap()
        order.orderParameters.put("slotId", slot.id)

        order.facility = slot.court.facility
        order.user = user ?: springSecurityService.getCurrentUser()
        order.customer = customer?.number ? customer : null
        order.price = price.price
        order.vat = price.VATAmount
        order.priceDescription = price.name()
        order.orderDescription = createOrderDescription(order.user, slot)
        order.orderNumber = createOrderNumber(order.user, slot)
        order.dateCreated = new Date()
        order.lastUpdated = new Date()

        if (price.isFree()) {
            order.method = PaymentMethod.FREE
        }

        order.save()

        return order
    }

    /**
     *  START OF FORMER API CODE
     */

    void handleCreditCardPayment(def order, User user) throws AdyenException {
        PaymentInfo easyPayment = getAnyPaymentInfoByUser(user)

        if (easyPayment.provider.equals(PaymentInfo.PaymentProvider.ADYEN)) {
            handleAdyenPayment(order)
        }
    }

    private void handleAdyenPayment(Order order) {
        AdyenOrderPayment payment = AdyenOrderPayment.create(order, PaymentMethod.CREDIT_CARD_RECUR)
        AdyenAuthorizationBuilder authorizationBuilder = AdyenAuthorizationBuilderFactory.createStoredDetailsAuthorization(AdyenRecurringProcessingModel.SUBSCRIPTION)

        try {
            payment.authorise([:], order, authorizationBuilder)
        } catch (AdyenException ae) {
            payment.transactionId = ae.pspReference
            throw ae
        }
    }

    // TODO: method is used by mobile API only (BookingResourceController) - will be removed in future
    void handleCouponPayment(Order order, Long couponId, PaymentMethod paymentMethod, User user) {

        CouponOrderPayment couponOrderPayment = new CouponOrderPayment(order: order,
                amount: order.total(), vat: order.vat(), issuer: user, ticket: null,
                status: OrderPayment.Status.NEW, method: paymentMethod)

        def coupon = CustomerCoupon.get(couponId)

        CustomerCouponTicket ticket = couponService.consumeTicket(coupon, order)
        ticket.save()

        couponOrderPayment.ticket = ticket

        if (!couponOrderPayment.ticket) {
            couponOrderPayment.status = OrderPayment.Status.FAILED
            couponOrderPayment.errorMessage = "No tickets left on customer coupon ${coupon.id}"
            order.status = Order.Status.CANCELLED
        } else {
            order.status = Order.Status.COMPLETED
            couponOrderPayment.status = OrderPayment.Status.CAPTURED
        }

        couponOrderPayment.save(failOnError: true)
        order.addToPayments(couponOrderPayment)
        order.save()

    }

    void handleOfferPayment(Order order, Long offerId, PaymentMethod paymentMethod, User user) {

        CouponOrderPayment couponOrderPayment = new CouponOrderPayment(order: order,
                amount: order.total(), vat: order.vat(), issuer: user, ticket: null,
                status: OrderPayment.Status.NEW, method: paymentMethod)

        def coupon = couponService.getExpiresFirstCustomerCoupon(
                Customer.findByUserAndFacility(user, order.facility),
                Offer.get(offerId), order.price)
        if (coupon) {
            CustomerCouponTicket ticket = couponService.consumeTicket(coupon, order)
            if (ticket) {
                ticket.save()
                couponOrderPayment.ticket = ticket
            }
        }

        if (!couponOrderPayment.ticket) {
            couponOrderPayment.status = OrderPayment.Status.FAILED
            couponOrderPayment.errorMessage = "No tickets left on customer coupon ${coupon?.id}"
            order.status = Order.Status.CANCELLED
        } else {
            order.status = Order.Status.COMPLETED
            couponOrderPayment.status = OrderPayment.Status.CAPTURED
        }

        couponOrderPayment.save(failOnError: true)
        order.addToPayments(couponOrderPayment)
        order.save()

    }

    /**
     *  END OF FORMER API CODE
     */

    @NotTransactional
    def getPaymentsBySlots(def slots) {
        def slotPayments = []
        def total = 0L

        slots.each { Slot slot ->

            def booking = slot.booking

            if (booking?.payment) {
                total += booking?.payment?.getTotalPaid()

                log.debug("GetPaymentBySlots: ${total}, ${booking?.payment?.paymentTransactions.size()} transactions")
                slotPayments << [slot: slot, transactions: booking?.payment?.paymentTransactions]

                log.debug("GetPaymentBySlots: ${slotPayments}")
            }
        }

        return [total: total, rows: slotPayments]
    }

    /**
     * Stale Boxnet usage
     */
    def makeCashRegisterPaymentUrl(def slots, def refund) {
        def orders = []
        slots.each { Slot s ->
            PaymentOrder order = createPaymentOrder(s.booking?.customer?.user, s)
            double vatDec = s.court.facility.vat > 0 ? s.court.facility.vat / 100 : 0

            if (refund && s.booking?.payment) { // With refund
                log.debug("Requesting refund on slot ${s.id}")
                order.price = -s.booking?.payment?.totalPaid
                order.vat = vatDec > 0 ? new Double(order.price * vatDec).round(2) : 0
                order.priceDescription = "Återbetalning"
            } else if (s.booking?.payment) {  // Calculate rest of payment
                log.debug("Requesting rest of payment on slot ${s.id}")
                order.price = s.booking.payment.amountFormattedAmountOnly() - s.booking?.payment?.totalPaid
                order.vat = vatDec > 0 ? new Double(order.price * vatDec).round(2) : 0
            } else {
                log.debug("Requesting new payment on slot ${s.id}")
            }

            order.method = PaymentMethod.REGISTER
            order.dateCreated = new Date()
            order.save()

            if (refund && s.booking?.payment && s.booking?.payment?.totalPaid > 0) {
                orders << order
            } else if (!refund) {
                orders << order
            }

        }
        return boxnetManager.createRequest(orders)
    }

    @NotTransactional
    def getServiceFee(String currency) {
        return new Amount(amount: grailsApplication.config.matchi.settings.currency[currency].serviceFee, VAT: 2.5)
    }

    @NotTransactional
    def getPayingCustomer(User user, Slot slot) {
        return slot?.booking?.customer ?: (Customer.findByUserAndFacility(user, slot.court.facility) ?: new Customer())
    }

    def createOrderDescription(User user, Slot slot) {
        StringBuilder sb = new StringBuilder()
        sb.append(DateTimeFormat.forPattern("yyyy-MM-dd").print(new DateTime(slot.startTime)));
        sb.append(" ")
        sb.append(DateTimeFormat.forPattern("HH:mm").print(new DateTime(slot.startTime)));
        sb.append("-")
        sb.append(DateTimeFormat.forPattern("HH:mm").print(new DateTime(slot.endTime)));
        sb.append(" ")
        sb.append(slot.court.facility.name)
        sb.append(" ")
        sb.append(slot.court.name)

        log.info(sb.toString().encodeAsURL())
        return sb.toString()
    }

    def createOrderNumber(User user, Slot slot) {
        def index = 0
        def orderNr = user.id + "-" + slot.startTime.getTime() + "-" + slot.court.id + "-" + index

        while (Payment.findByOrderNr(orderNr) != null) {
            index++
            orderNr = user.id + "-" + slot.startTime.getTime() + "-" + slot.court.id + "-" + index
        }

        return orderNr
    }

    @NotTransactional
    def toPaymentPriceFormat(double price) {
        int result = (int) (Math.round(price * 100.0) / 100.0) * 100
        return (result == 0 ? "00" : String.valueOf(result))
    }

    @NotTransactional
    def toPaymentPriceFormat(Long price) {
        return toPaymentPriceFormat(price.doubleValue())
    }

    @NotTransactional
    def toPaymentPriceFormat(BigDecimal price) {
        return toPaymentPriceFormat(price.doubleValue())
    }

    OrderPayment makePayment(Order order, cmd) {
        if (order) {
            if (cmd.useInvoice) {
                return invoiceService.createInvoiceOrderPayment(order)
            } else if (cmd.useCoupon || cmd.useGiftCard) {
                def cc = couponService.getExpiresFirstCustomerCoupon(
                        Customer.get(cmd.customerId), Offer.get(cmd.customerCouponId), order.price)
                if (!cc) {
                    return null
                }
                return cmd.useCoupon ? couponPaymentService.createCouponOrderPayment(order, cc.id, PaymentMethod.COUPON)
                        : couponPaymentService.createCouponOrderPayment(order, cc.id, PaymentMethod.GIFT_CARD)
            } else {
                return cashService.createCashOrderPayment(order)
            }
        } else {
            return null
        }
    }

    @NotTransactional
    Long getRecordingPrice(String currency){
        Long.valueOf(grailsApplication.config.matchi.settings.currency[currency].recordingPrice)
    }
}
