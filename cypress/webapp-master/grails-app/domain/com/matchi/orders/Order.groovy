package com.matchi.orders

import com.matchi.*
import com.matchi.activities.Participation
import com.matchi.coupon.CustomerCoupon
import com.matchi.dynamicforms.Submission
import com.matchi.invoice.Invoice.InvoiceStatus
import com.matchi.membership.Membership
import com.matchi.payment.PaymentMethod
import grails.util.Holders
import org.joda.time.DateTime

class Order implements Serializable {

    public static final String ORIGIN_FACILITY = "facility"
    public static final String ORIGIN_WEB = "web"
    public static final String ORIGIN_API = "api"
    public static final String ORIGIN_REMOTE = "remote"

    public static final int REFUND_LIMIT_DAYS = 2
    public static final String ID_SEPARATOR = ","
    public static final String PLAYER_EMAIL = "playerEmail_"
    public static final String PLAYER_CUSTOMER_ID = "playerCustomerId_"

    public static final String META_ALLOW_RECURRING = "allowRecurring"
    public static final String META_USER_MESSAGE = "userMessage"

    static belongsTo = [Booking, Participation, CustomerCoupon, Submission, Subscription]

    static hasMany = [payments: OrderPayment, refunds: OrderRefund, memberships: Membership, recordingPurchases: RecordingPurchase]

    Set<OrderPayment> payments = []
    Set refunds = []

    Booking booking
    Participation participation
    Set<Membership> memberships = []
    Set<RecordingPurchase> recordingPurchases = []
    CustomerCoupon customerCoupon
    Submission submission
    Subscription subscription
    Status status = Status.NEW
    Facility facility
    Customer customer
    User user
    User issuer
    Order.Article article
    String description
    BigDecimal price = 0
    BigDecimal vat = 0
    Date dateDelivery
    Date dateCreated
    Date lastUpdated
    String origin = ORIGIN_WEB

    Map metadata

    static constraints = {
        customer nullable: true
        user nullable: true
        issuer nullable: false
        facility nullable: true
        dateDelivery nullable: false
        origin nullable: true
        booking nullable: true
        participation nullable: true
        memberships nullable: true
        customerCoupon nullable: true
        submission nullable: true
        subscription nullable: true
    }

    PaymentMethod getFirstPaymentMethod() {
        if (isFree()) {
            return PaymentMethod.FREE
        }
        if (payments && !payments.empty) {
            return payments.first().method
        } else {
            return PaymentMethod.UNKNOWN
        }
    }

    boolean isStillRefundable() {
        boolean hasRestrictedOrderPayments = payments.any { OrderPayment p ->
            !p.allowLateRefund()
        }

        // Only lock order refund if payments are restricted for refunds
        if (!hasRestrictedOrderPayments) {
            return true
        }

        DateTime refundLimit = new DateTime().minusDays(REFUND_LIMIT_DAYS).withTimeAtStartOfDay()
        return this.dateDelivery.after(refundLimit.toDate())
    }

    void refund(def note) {
        refund(total(), note)
    }

    void refund(def amount, def note) {
        def springSecurityService = Holders.grailsApplication.mainContext.getBean('springSecurityService')
        addToRefunds(new OrderRefund(order: this, amount: amount, note: note, issuer: (springSecurityService.getCurrentUser() ?: this.issuer) as User))

        if (payments.size() > 0) {
            // TODO: Check which payment and that payment has enough amount left
            def payment = payments.first()
            payment.refund(amount)
        } else {
            log.info(LogHelper.formatOrder("No payments found on order, skipping refund", this))
        }
    }


    def total() {
        price.minus(totalRefunded())
    }

    def totalRefunded() {
        return (refunds && !refunds.isEmpty()) ? refunds?.sum(0) { it.amount } : 0
    }

    def vat() {
        if (isAnnulled()) {
            0
        } else {
            vat
        }
    }

    def isFree() {
        total() == 0
    }

    def isPartlyPaid() {
        return getTotalAmountPaid() > 0 && getTotalAmountPaid() < total()
    }

    def isFinalPaid() {
        return getTotalAmountPaid() >= total()
    }

    boolean isAnnulled() {
        return this.status == Status.ANNULLED
    }

    /**
     * Checks that Order is paid by look at OrderPayment objects so that they are not NEW, free orders are also accepted.
     * @param order
     * @return boolean saying if any payment had status NEW
     */
    boolean isProcessable() {
        if (isFree()) return true
        else if (payments == null || payments.isEmpty()) return false

        return payments.every { OrderPayment payment ->
            return payment.isProcessable()
        }
    }

    boolean areAllPayments(OrderPayment.Status status) {
        return this.payments?.toList().every { OrderPayment orderPayment ->
            return orderPayment.status.equals(status)
        }
    }

    void addPlayersToMetadata(List<Customer> players) {
        players.eachWithIndex { Customer player, int i ->
            if (player.id) {
                this.metadata[PLAYER_CUSTOMER_ID + i] = player.id.toString()
            } else {
                this.metadata[PLAYER_EMAIL + i] = player.email
            }
        }
    }

    List<Player> getPlayersFromMetadata() {
        List<Player> players = []
        this.metadata.sort().each { k, v ->
            if (k.startsWith(PLAYER_CUSTOMER_ID)) {
                def customer = Customer.findByFacilityAndId(this.facility, v.toLong())
                players.push(new Player(email: customer?.email, customer: customer))
            } else if (k.startsWith(PLAYER_EMAIL)) {
                players.push(new Player(email: v, customer: v ? Customer.findByFacilityAndEmailAndArchived(this.facility, v, false) : null))
            }
        }
        return players
    }

    def getRestAmountToPay() {
        if (!isFinalPaid()) {
            return total() - getTotalAmountPaid()
        }

        return 0
    }

    BigDecimal getTotalAmountPaid() {
        payments.findAll() { it.getStatus() in [OrderPayment.Status.CREDITED, OrderPayment.Status.CAPTURED, OrderPayment.Status.AUTHED] }
                .sum(0) { it.total() }
    }

    def hasValidPayment(def types) {
        def payments = payments.findAll() {
            it.type in types &&
                    (it.getStatus() in [OrderPayment.Status.CREDITED, OrderPayment.Status.CAPTURED, OrderPayment.Status.AUTHED])
        }

        return payments.size() > 0
    }

    OrderPayment getActivePayment() {
        def payment = payments.find() {
            (it.getStatus() in [OrderPayment.Status.CAPTURED, OrderPayment.Status.AUTHED])
        }

        return payment
    }

    def isPaidByCreditCard() {
        return (hasValidPayment(["Netaxept"]) || hasValidPayment(["Adyen"]))
    }

    def isPaidByCoupon() {
        return hasValidPayment(["Coupon"])
    }

    def isPaidByCash() {
        return hasValidPayment(["Cash"])
    }

    def isPaidByInvoice() {
        return hasValidPayment(["Invoice"])
    }

    boolean isInvoiced() {
        (!facility || facility.hasApplicationInvoice()) &&
                payments.find {
                    it.type == "Invoice" && it.invoiceRow && !(it.status in [OrderPayment.Status.FAILED, OrderPayment.Status.ANNULLED]) &&
                            (!it.invoiceRow.invoice || !(it.invoiceRow.invoice.status in [InvoiceStatus.CANCELLED, InvoiceStatus.INCORRECT]))
                }
    }

    boolean hasPayments() {
        return this.payments?.size() > 0
    }

    boolean hasRefundablePayment() {
        if (this.hasPayments()) {
            return this.payments.every { OrderPayment op ->
                return op.isRefundableTypeAndStatus()
            }
        } else {
            return false
        }
    }

    /**
     * Checks if an order is deletable.
     * This is true if the Order is NEW with no payments or just payments that are also NEW. Also there can be no article.
     * Complexity is O(n) where n is number of payments, seldom more than 1
     * @return
     */
    boolean isDeletable() {
        if (!this.status.equals(Order.Status.NEW)) return false

        if (this.retrieveArticleItem()) return false

        if (!hasPayments()) return true

        // Check if all payments are just NEW
        return this.payments.every { OrderPayment op ->
            return op.status.equals(OrderPayment.Status.NEW)
        }
    }

    /**
     * Deletes itself and related payments.
     * Only if it is deletable
     */
    void deleteWithPayments(boolean flush = false) {
        if (this.isDeletable()) {
            this.payments?.toList().each { OrderPayment op ->
                this.removeFromPayments(op)
                op.delete(flush: flush)
            }
            this.delete(flush: flush)
        }
    }

    /**
     * Returns an IArticleItem based on order article type
     * @param order
     * @return
     */
    IArticleItem retrieveArticleItem() {
        switch (this.article) {
            case Article.BOOKING:
                return Booking.findByOrder(this)
            case Article.ACTIVITY:
                return Participation.findByOrder(this)
            case Article.MEMBERSHIP:
                return Membership.findByOrder(this)
            case Article.COUPON:
                return CustomerCoupon.findByOrder(this)
            case Article.FORM_SUBMISSION:
                return Submission.findByOrder(this)
            case Article.RECORDING:
                return RecordingPurchase.findByOrder(this)
        }
    }

    static String joinOrderIds(Object orderIds) {
        if (orderIds instanceof List) {
            return ((List) orderIds).join(ID_SEPARATOR)
        }

        return orderIds as String
    }

    static List<Long> getOrderIdsFromParams(Object orderIds) {
        if (orderIds instanceof String) {
            return orderIds.split(ID_SEPARATOR).collect { it as Long }
        }

        if (orderIds instanceof List) {
            return orderIds.collect { it as Long }
        }

        return []
    }

    static List<Order> getFromParams(Object orderIds) {
        if (orderIds == null) return []

        return Order.findAllByIdInList(getOrderIdsFromParams(orderIds))
    }

    static List<Order> sortOrdersById(List<Order> orders) {
        return orders.sort { Order o1, Order o2 ->
            return o1.id.compareTo(o2.id)
        }
    }

    static Order getFirstById(List<Order> orders) {
        if (orders?.size() > 0) {
            return sortOrdersById(orders).first()
        }
        return null
    }


    def getReceiptNumber() {
        return "${id}"
    }

    void assertCustomer() {
        if (!customer && facility) {
            def customerService = Holders.grailsApplication.mainContext.getBean('customerService')
            customer = customerService.getOrCreateUserCustomer(user, facility)
            customer.save(flush: true, failOnError: true)
        }
    }

    /**
     * An order is remote payable if created by the facility, has no payment or has a failed payment
     * @return
     */
    boolean isRemotePayable() {
        boolean allPaymentsFailed = payments?.every { OrderPayment orderPayment -> orderPayment.status == OrderPayment.Status.FAILED }
        boolean allPaymentsNew = payments?.every { OrderPayment orderPayment -> orderPayment.status == OrderPayment.Status.NEW }

        return article.isRemotePayable && !isFinalPaid() && !isFree() &&
                ((status == Status.NEW && !payments) || (status == Status.CANCELLED && allPaymentsFailed) || (status == Status.CONFIRMED && allPaymentsNew))
    }

    static mapping = {
        table "`order`"
        autoTimestamp true
        payments joinTable: [name: "order_order_payments", key: 'order_id']
    }

    Order createCopyForRemotePayment() {
        Order newOrder = new Order()
        newOrder.article = article
        newOrder.origin = origin
        newOrder.price = price
        newOrder.vat = vat
        newOrder.status = Order.Status.NEW
        newOrder.facility = facility
        newOrder.customer = customer
        newOrder.issuer = issuer
        newOrder.user = user
        newOrder.description = description
        newOrder.dateDelivery = dateDelivery

        if (metadata) {
            newOrder.metadata = [:]

            metadata.each { Object key, Object val ->
                newOrder.metadata[key] = val
            }
        }


        return newOrder.save(failOnError: true, flush: true)
    }

    static enum Article {
        BOOKING(true, true), // Booking of slot
        ACTIVITY, // Participation in activity
        COUPON,
        MEMBERSHIP(true),
        FORM_SUBMISSION,
        SUBSCRIPTION_BOOKING,
        SUBSCRIPTION,
        PAYMENT_UPDATE, // Update of payment info (e.g used for saving card data)
        PRIVATE_LESSON,
        RECORDING(false, false)

        final boolean isRemotePayable
        final boolean isSoldByFacility

        Article(boolean isRemotePayable = false, isSoldByFacility = true) {
            this.isRemotePayable = isRemotePayable
            this.isSoldByFacility = isSoldByFacility
        }

        static list() {
            return [BOOKING, ACTIVITY, COUPON, MEMBERSHIP, FORM_SUBMISSION, SUBSCRIPTION_BOOKING, SUBSCRIPTION, PAYMENT_UPDATE, PRIVATE_LESSON, RECORDING]
        }

        static List<Article> getRemotePayables() {
            return this.list().findAll { Article article -> article.isRemotePayable }
        }
    }

    static enum Status {
        /* Initialized order, no confirmation by the customer. */
        NEW,

        /* Confirmed order, the product has been delivered */
        CONFIRMED,

        /* Order has not been processed (ie. failed booking) */
        CANCELLED,

        /* Payment has been received */
        COMPLETED,

        /* Order has been annulled and product returned */
        ANNULLED

        static list() {
            return [NEW, CONFIRMED, CANCELLED, COMPLETED, ANNULLED]
        }
    }
}
