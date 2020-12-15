package com.matchi.coupon

import com.matchi.Booking
import com.matchi.Customer
import com.matchi.Facility
import com.matchi.IArticleItem
import com.matchi.Payment
import com.matchi.Slot
import com.matchi.User
import com.matchi.orders.Order
import org.hibernate.Hibernate
import org.joda.time.DateTime
import org.joda.time.Interval
import org.joda.time.LocalDate
import org.joda.time.Period

class CustomerCoupon implements IArticleItem {

    static belongsTo = [customer: Customer]
    static hasMany = [couponTickets: CustomerCouponTicket]
    static hasOne = [coupon: Offer]

    Payment payment
    Order order
    String note
    User createdBy
    LocalDate expireDate        // inclusive

    Date dateCreated
    Date lastUpdated
    Date dateLocked

    Integer nrOfTickets = 1

    /**
     * Checks if we can pay for a list of slots.
     * All slots must be accepted by the conditions.
     * In addition, the coupon must be unlimited or the number of tickets enough for all slots.
     * If the slots belong to an activity we should only check for the amount of tickets
     * If coupon is a gift card, the amount is already checked in previous logic.
     * @param slots
     * @return
     */
    boolean accept(List<Slot> slots, Boolean ignoreSlotsAmountRestriction = false) {
        if(!validatePeriodConditionForSlots(slots)) {
            log.info "Booking rejected because of validation period condition for slot"
            return false
        }
        slots.every{
            log.info"Slot booking ${it?.booking} is it activity ${it?.booking?.isActivity()}"
        }
        if (!slots.every { Slot slot ->
            boolean result = coupon.accept(slot)
            log.info "Coupon $coupon accept slot [$result]"
            result}) {
            log.info "Booking rejected because it's not accepted by coupon"
            return false
        }

        log.info "Number of tickets here ${nrOfTickets} and coupon is unlimited ${this.coupon.unlimited}"
        if(slots.every{it?.booking?.isActivity()} && (this.nrOfTickets>0 || this.coupon.unlimited)) {
            log.info "ticket accepted"
            return true
        }

        return this.coupon.unlimited || this.coupon.class == GiftCard ||
                ignoreSlotsAmountRestriction || this.nrOfTickets >= slots.size()
    }

    boolean accept(Slot slot) {
        return accept([slot])
    }

    static constraints = {
        coupon(nullable: false)
        payment(nullable: true)
        note(nullable: true, maxSize: 2000)
        createdBy(nullable: false)
        dateLocked(nullable: true)
        expireDate(nullable: true, blank: false)
        order(nullable: true)
    }

    static mapping = {
        sort "coupon.name"
        couponTickets sort: "id"
        order cascade: 'none'
    }

    static namedQueries = {
        facilityStats { Facility f, Date start, Date end, Boolean unlimited, String discr = "coupon" ->
            createAlias("customer", "c")
            createAlias("coupon", "cpn")
            eq("c.facility", f)
            ge("dateCreated", start)
            le("dateCreated", end)
            eq("cpn.unlimited", unlimited)
            eq("cpn.class", discr)
        }

        oldCoupon { Customer cust, Offer ofr, LocalDate expDate ->
            eq("customer", cust)
            eq("coupon", ofr)
            gt("nrOfTickets", 0)
            isNull("dateLocked")
            if (expDate) {
                eq("expireDate", expDate)
            } else {
                isNull("expireDate")
            }
        }
    }

    static CustomerCoupon link(User creator, Customer customer, Offer coupon, int nrOfTickets,
            LocalDate expireDate = null, String note = "",
            CustomerCouponTicket.Type ticketType = CustomerCouponTicket.Type.ADMIN_CREATE,
            String ticketDescription = null) {

        CustomerCoupon uc = new CustomerCoupon()
        uc.createdBy = creator
        uc.nrOfTickets = nrOfTickets
        uc.note = note
        uc.expireDate = expireDate
        customer?.addToCustomerCoupons(uc)
        coupon?.addToCustomerCoupons(uc)

        def cct = new CustomerCouponTicket(issuer: creator,
                type: ticketType, nrOfTickets: nrOfTickets)
        cct.addDescription(ticketDescription)
        uc.addToCouponTickets(cct)

        uc.save(failOnError: true)

        return uc
    }

    /**
     * Because log.info does not work from static context
     */
    def afterInsert() {
        log.info("CustomerCoupon ${this?.id} \"${this?.coupon?.name}\" for customer ${this?.customer?.id} \"${this?.customer}\": Created with ${this.nrOfTickets} tickets and expiration date ${this.expireDate}")
    }

    CustomerCouponTicket addTicket(User issuer, Integer amount = null,
            CustomerCouponTicket.Type type = CustomerCouponTicket.Type.ADMIN_CHANGE,
            String description = null, Long purchasedObjectId = null) {
        log.info("CustomerCoupon ${this?.id} \"${this?.coupon?.name}\" for customer ${this?.customer?.id} \"${this?.customer}\": Adding ${amount ?: 1} ticket(s)")

        executeUpdate('update CustomerCoupon set nrOfTickets = (nrOfTickets + :nrOfTickets) where id = :couponId', [couponId: this.id, nrOfTickets: amount ?: 1])

        def cct = new CustomerCouponTicket(customerCoupon: this, type: type,
                nrOfTickets: amount ?: 1, issuer: issuer, purchasedObjectId: purchasedObjectId)
        cct.addDescription(description)
        return cct.save(flush: true)
    }

    CustomerCouponTicket consumeTicket(Order order, User issuer,
            String ticketDescription = null, Long purchasedObjectId = null) {

        CustomerCouponTicket uct = new CustomerCouponTicket()
        uct.customerCoupon = this
        uct.type = CustomerCouponTicket.Type.getTypeByOrderArticle(order.article)
        uct.purchasedObjectId = purchasedObjectId
        uct.purchasedObjectPrice = order.price
        uct.issuer = issuer
        if (Hibernate.getClass(this.coupon) == Coupon) {
            uct.nrOfTickets = -1
            this.nrOfTickets--
        } else if (Hibernate.getClass(this.coupon) == GiftCard) {
            uct.nrOfTickets = -order.price.intValue()
            this.nrOfTickets = this.nrOfTickets - order.price.intValue()
        }
        uct.addDescription(ticketDescription)
        this.addToCouponTickets(uct)

        return uct
    }

    Integer removeTicket(User issuer, CustomerCouponTicket.Type type = CustomerCouponTicket.Type.ADMIN_CHANGE) {
        if (this.nrOfTickets > 0) {
            log.info("CustomerCoupon ${this?.id} \"${this?.coupon?.name}\" for customer ${this?.customer?.id} \"${this?.customer}\": 1 ticket removed")
            this.nrOfTickets--
            this.addToCouponTickets(new CustomerCouponTicket(type: type,
                    nrOfTickets: -1, issuer: issuer))
            this.save()
        }
        return this.nrOfTickets
    }

    String toString() { "$coupon.name" }

    boolean isExpired() {
        return expireDate && expireDate < LocalDate.now()
    }

    boolean isValid(BigDecimal price = null) {
        if (price && Hibernate.getClass(this.coupon) == GiftCard) {
            price <= nrOfTickets && !isExpired() && !dateLocked
        } else {
            (nrOfTickets > 0 || coupon.unlimited) && !isExpired() && !dateLocked
        }

    }

    /**
     * Checks if we can use this CustomerCoupon to book supplied slots with regards to period conditions
     * @param slots
     * @return
     */
    boolean validatePeriodConditionForSlots(List<Slot> slots) {
        return slots.every { Slot thisSlot ->
            List<Slot> otherSlots = slots.findAll { Slot otherSlot -> otherSlot.id != thisSlot.id }
            return validatePeriodCondition(thisSlot, otherSlots)
        }
    }

    /**
     * Checks if a slot can be booked with regards to already existing bookings within period, but also other bookings we want to make.
     * @param slot
     * @param otherSlots
     * @return
     */
    boolean validatePeriodCondition(Slot slot, List<Slot> otherSlots = []) {
        // check if slot belongs to activity,
        // for unlimited bookings we do not rely on limitations for bookings.
        if (slot?.booking?.activity && coupon.unlimited) {
            return true
        }
        if (coupon?.instanceOf(Coupon) && coupon?.conditionPeriod && coupon?.nrOfPeriods && coupon?.nrOfBookingsInPeriod) {
            Boolean validates = true
            DateTime pivot = new DateTime(slot.startTime)
            List<Interval> intervals = getSurroundingIntervals(pivot)

            // contains all dates where customer has a booking with the coupon
            List<DateTime> bookingDates = []

            // TODO: deprecated method, should be removed when coupons refactored to orders
            List<Booking> comingCouponBookings = couponTickets.findAll {
                it.purchasedObjectId && it.type == CustomerCouponTicket.Type.BOOKING
            }.collect {
                Booking.get(it.purchasedObjectId)
            }.findAll {
                it
            }
            if (!coupon.totalBookingsInPeriod) {
                // filter to contain only future/active bookings
                comingCouponBookings = comingCouponBookings.findAll {
                    new DateTime(it?.slot?.startTime).isAfter(new DateTime())
                }
            }

            bookingDates.addAll comingCouponBookings.collect() { it.slot.toInterval().start }
            bookingDates.addAll otherSlots.collect() { it.toInterval().start }

            if (!coupon.totalBookingsInPeriod) {
                bookingDates = bookingDates.findAll { new DateTime().isBefore(it) }
            }

            intervals.each { Interval interval ->
                Integer nrOfBookingsInInterval = bookingDates?.findAll { interval.contains(it) }?.size()
                if (nrOfBookingsInInterval >= coupon.nrOfBookingsInPeriod) {
                    validates = false
                }
            }

            return validates
        } else {
            //If conditionPeriod not set, always validate
            return true
        }
    }

    /**
     * Get all surrounding intervals of period length that includes the date
     * @param pivot date that must be included
     * @param period length of intervals
     * @return
     */
    List<Interval> getSurroundingIntervals(DateTime pivot) {
        DateTime pivotPeriodLastDay = getLastDayOfPeriod(pivot)
        Period period = getPeriodFromConditionPeriod()
        List<Interval> intervals = []

        (0..(coupon.nrOfPeriods - 1)).each {
            pivotPeriodLastDay = getNextPivot(pivotPeriodLastDay, it)
            Interval interval = new Interval(pivotPeriodLastDay.minus(period), pivotPeriodLastDay)
            intervals << interval
        }

        return intervals
    }

    /**
     * Get the last date of the period containing the date
     * @param DateTime date to check against
     * @param period
     * @return the last day of the period
     */
    DateTime getLastDayOfPeriod(DateTime pivot) {
        switch (coupon.conditionPeriod) {
            case Coupon.ConditionPeriod.DAILY:
                return pivot.plusDays(1).toDateMidnight().toDateTime()
                break
            case Coupon.ConditionPeriod.WEEKLY:
                return pivot.dayOfWeek().withMaximumValue().plusDays(1).toDateMidnight().toDateTime()
                break
            case Coupon.ConditionPeriod.MONTHLY:
                return pivot.dayOfMonth().withMaximumValue().plusDays(1).toDateMidnight().toDateTime()
                break
            case Coupon.ConditionPeriod.YEARLY:
                return pivot.dayOfYear().withMaximumValue().plusDays(1).toDateMidnight().toDateTime()
                break
            default:
                return
                break
        }
    }

    DateTime getNextPivot(DateTime pivot, int index) {
        switch (coupon.conditionPeriod) {
            case Coupon.ConditionPeriod.DAILY:
                return pivot.plusDays(index)
                break
            case Coupon.ConditionPeriod.WEEKLY:
                return pivot.plusWeeks(index)
                break
            case Coupon.ConditionPeriod.MONTHLY:
                return pivot.plusMonths(index)
                break
            case Coupon.ConditionPeriod.YEARLY:
                return pivot.plusYears(index)
                break
            default:
                return
                break
        }
    }


    Period getPeriodFromConditionPeriod() {
        switch (coupon.conditionPeriod) {
            case Coupon.ConditionPeriod.DAILY:
                return new Period().plusDays(coupon.nrOfPeriods)
                break
            case Coupon.ConditionPeriod.WEEKLY:
                return new Period().plusWeeks(coupon.nrOfPeriods)
                break
            case Coupon.ConditionPeriod.MONTHLY:
                return new Period().plusMonths(coupon.nrOfPeriods)
                break
            case Coupon.ConditionPeriod.YEARLY:
                return new Period().plusYears(coupon.nrOfPeriods)
                break
            default:
                return
                break
        }
    }

    @Override
    void replaceOrderAndSave(Order order) {
        this.order = order
        this.save(flush: true, failOnError: true)
    }
}
