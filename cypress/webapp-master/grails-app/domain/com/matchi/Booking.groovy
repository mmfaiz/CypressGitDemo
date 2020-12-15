package com.matchi

import com.matchi.activities.trainingplanner.Trainer
import com.matchi.coupon.CustomerCoupon
import com.matchi.coupon.CustomerCouponTicket
import com.matchi.enums.BookingGroupType
import com.matchi.mpc.CodeRequest
import com.matchi.orders.Order
import com.matchi.payment.ArticleType
import com.matchi.price.PriceListCustomerCategory
import org.hibernate.criterion.CriteriaSpecification
import org.joda.time.DateTime
import org.joda.time.Hours

class Booking implements Comparable<Booking>, Serializable, IArticleItem, IReservation {

    static belongsTo = [Slot, Customer, BookingGroup]
    static hasMany = [players: Player, trainers: Trainer]
    static hasOne = [recordingStatus: RecordingStatus]

    Slot slot
    Customer customer
    BookingGroup group
    String bookingNumber
    String comments
    String telephone
    boolean showComment = false
    boolean paid = false
    boolean online = false

    Payment payment
    Order order

    Date dateCreated
    Date lastUpdated

    Boolean dateReminded = false

    Boolean hideBookingHolder

    PriceListCustomerCategory selectedCustomerCategory

    static constraints = {
        customer nullable: false
        bookingNumber nullable: true
        comments nullable: true, maxSize: 1000
        telephone nullable: true
        showComment nullable: true
        paid nullable: false
        group nullable: true
        payment nullable: true
        online nullable: false
        order nullable: true
        dateReminded nullable: true
        hideBookingHolder nullable: true
        selectedCustomerCategory nullable: true
        recordingStatus nullable: true
    }

    static mapping = {
        trainers joinTable: [name: "booking_trainer", key: "booking_id", column: "trainer_id"]
        autoTimestamp(true)
        sort "id"
        payment cascade: "save-update"
        order cascade: "save-update"
        players sort: "id"
    }

    static namedQueries = {
        upcomingBookings { Customer c ->
            customer {
                eq("id", c.id)
            }
            slot {
                gt("startTime", new Date())
            }
            or {
                isNull("group")
                group(CriteriaSpecification.LEFT_JOIN) {
                    eq("type", BookingGroupType.DEFAULT)
                }
            }
        }
        remotePayables { List<Order> orders ->
            order {
                inList("id", orders*.id)
            }
            slot {
                gt("endTime", new Date())
            }
            or {
                isNull("group")
                group(CriteriaSpecification.LEFT_JOIN) {
                    eq("type", BookingGroupType.DEFAULT)
                }
            }
        }
    }

    /**
     * Returns orders of bookings not having ended yet
     * @param orders
     * @return
     */
    static List<Order> getRemotePayablesForOrders(List<Order> orders) {
        return remotePayables(orders).list()*.order
    }

    boolean isRemotePayable() {
        return (!group || group.type == BookingGroupType.DEFAULT) && slot.endTime > new Date()
    }

    boolean showRemotePaymentNotificationInEmail() {
        return slot.court.facility.hasEnabledRemotePaymentsFor(order?.article) && order?.isRemotePayable()
    }

    String toString() { "$bookingNumber" }

    boolean isOwner(Customer owner) {
        if (customer != null && owner != null && customer.id.equals(owner.id)) {
            return true
        }

        return false
    }

    boolean isBookedOnline() {
        return !this.order?.origin?.equals(Order.ORIGIN_FACILITY)
    }

    boolean isOwner(User owner) {
        Customer customer = Customer.findByUserAndFacility(owner, slot.court.facility)
        return isOwner(customer)
    }

    /**
     * Checks if Booking instance is fully paid by looking at order. Should be used instead of paid-flag.
     * @return
     */
    boolean isFinalPaid() {
        return order?.isFinalPaid()
    }

    /**
     * Returns access code based on the following priority:
     * 1. Personal customer access code
     * 2. Subscription access code
     * 3. MPC access code
     * 4. Facility access code
     * @return
     */
    String getAccessCode() {
        def facility = slot?.court?.facility

        // TODO Fix this ugly hack
        // https://matchi.slack.com/archives/CE81S6CNT/p1601282547049200
        // https://github.com/matchiapp/webapp/pull/2072#discussion_r496649453
        // Summary: refactor for greater flexibility in QT/MPC usage throughout application
        if (facility?.id == 27L) {
            return "1710"
        }
        if (facility?.hasPersonalAccessCodes() && this.customer.accessCode) {
            return this.customer.accessCode
        }

        if (facility?.hasSubscriptionAccessCode() && isSubscription() && this.group?.subscription?.accessCode) {
            return this.group?.subscription?.accessCode
        }

        if (facility?.hasMPC()) {
            CodeRequest codeRequest = CodeRequest.findByBooking(this)

            if (codeRequest && codeRequest.status != CodeRequest.Status.PENDING) {
                return codeRequest.code ?: FacilityAccessCode.validAccessCodeFor(slot)?.content
            }
        }

        return FacilityAccessCode.validAccessCodeFor(slot)?.content
    }

    boolean isSubscription() {
        return this.group?.isType(BookingGroupType.SUBSCRIPTION)
    }

    boolean isActivity() {
        return this.group?.isType(BookingGroupType.ACTIVITY)
    }

    boolean isNotAvailable() {
        return this.group?.isType(BookingGroupType.NOT_AVAILABLE)
    }

    CodeRequest getCodeRequest() {
        return CodeRequest.findByBooking(this)
    }

    int hashCode() {
        int result;
        result = (bookingNumber != null ? bookingNumber.hashCode() : 0);
        result = 31 * result + (id != null ? id.hashCode() : 0);
        return result;
    }

    public CustomerCoupon getCustomerCoupon() {
        def customerCouponTicket = CustomerCouponTicket.createCriteria().get {
            eq("purchasedObjectId", this.id)
            eq("type", CustomerCouponTicket.Type.BOOKING)
            uniqueResult()
        }

        return customerCouponTicket?.customerCoupon
    }

    public clearPayments() {
        def payment = this.payment
        def transactions = payment.paymentTransactions

        transactions.each {
            it.delete()
        }
        payment.delete()
        this.payment = null
    }

    Booking addPlayers(List playerCustomers) {
        if (playerCustomers) {
            playerCustomers.each {
                this.addToPlayers(it.number ? new Player(customer: it) : new Player())
            }
            this.save()
        }
        return this
    }

    Booking updatePlayers(List players) {
        def playerCustomers = players?.sort { it }?.reverse()
        def bookingPlayers = this.players?.sort { it?.customer }?.reverse()
        if (playerCustomers && bookingPlayers) {
            if (playerCustomers.size() < bookingPlayers.size()) {
                def numberPlayersToDelete = bookingPlayers.size() - playerCustomers.size()
                numberPlayersToDelete.times {
                    def lastPlayer = bookingPlayers.last()
                    this.removeFromPlayers(lastPlayer)
                    lastPlayer.delete()
                    bookingPlayers.remove(bookingPlayers.size() - 1)
                }
            }
            playerCustomers.eachWithIndex { customer, idx ->
                if (idx < bookingPlayers.size()) {
                    def bookingPlayer = bookingPlayers.get(idx)
                    if (customer.number != bookingPlayer.customer?.number) {
                        this.removeFromPlayers(bookingPlayer)
                        bookingPlayer.delete()
                        this.addToPlayers(customer.number ? new Player(customer: customer) : new Player())
                    }
                } else {
                    this.addToPlayers(customer.number ? new Player(customer: customer) : new Player())
                }
            }
        } else {
            if (bookingPlayers) {
                bookingPlayers.each {
                    this.removeFromPlayers(it)
                    it.delete()
                }
            }
        }
        return this
    }

    int compareTo(Booking other) {
        int res = 0
        if (other == null) {
            return 1
        }
        if (this) {
            res = this.slot?.startTime?.compareTo(other?.slot?.startTime)
            if (res == 0) {
                res = this.slot?.endTime?.compareTo(other?.slot?.endTime)
            }
        }


        if (res == 0 && this && other.slot?.court) {
            res = this.slot?.court?.id?.compareTo(other.slot?.court?.id)
        }
        return res;
    }

    boolean equals(o) {
        if (this.is(o)) return true;
        if (getClass() != o.class) return false;

        Booking booking = (Booking) o;

        if (this.slot?.startTime != booking.slot?.startTime) return false;
        if (this.slot?.endTime != booking.slot?.endTime) return false;
        if (this.slot?.court != null && booking.slot?.court != null && (this.slot?.court?.id != booking.slot?.court?.id)) return false

        return true;
    }

    @Override
    void replaceOrderAndSave(Order order) {
        this.order = order
        this.save(flush: true, failOnError: true)
    }

    @Override
    ArticleType getArticleType() {
        return ArticleType.BOOKING
    }

    @Override
    Date getDate() {
        return slot?.startTime
    }

    @Override
    Facility getFacility() {
        return slot?.court?.facility
    }

    boolean startsMoreThanSixHours() {
        DateTime startTime = new DateTime(slot?.startTime)
        if (startTime.afterNow) {
            Hours.hoursBetween(new DateTime(), new DateTime(slot?.startTime)).getHours() > 6
        }
    }

    boolean startsWithinSixHours() {
        DateTime startTime = new DateTime(slot?.startTime)
        if (startTime.afterNow) {
            Hours.hoursBetween(new DateTime(), new DateTime(slot?.startTime)).getHours() <= 6
        }
    }
}
