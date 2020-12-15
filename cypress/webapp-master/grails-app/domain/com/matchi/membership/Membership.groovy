package com.matchi.membership

import com.matchi.Customer
import com.matchi.Facility
import com.matchi.IArticleItem
import com.matchi.OrderStatusService
import com.matchi.User
import com.matchi.events.SystemEventInitiator
import com.matchi.facility.FilterCustomerCommand
import com.matchi.Slot
import com.matchi.orders.Order
import grails.util.Holders
import org.joda.time.LocalDate

class Membership implements Serializable, IArticleItem {

    static belongsTo = [ customer:Customer ]

    LocalDate startDate
    LocalDate endDate   // inclusive; i.e. endDate is included in valid time range, e.g. 1 year validity: 2018-01-01 - 2018-12-31
    LocalDate gracePeriodEndDate    // endDate + number of days of grace period; inclusive
    Integer startingGracePeriodDays // trial period that might be added to a new members

    boolean activated = true        // true by default and false in case when facility use manual approval of requested memberships
    boolean cancel = false          // indicates whether membership should be automatically renewed for the next period or not
    boolean autoPay                 // indicates whether customer decided to automatically pay for upcoming memberships
    Integer autoPayAttempts         // increased each time when auto-payment fails

    Date dateCreated
    Date lastUpdated

    MembershipType type
    MembershipFamily family

    User createdBy

    Order order

    static constraints = {
        endDate(validator: { val, obj ->
            val >= obj.startDate
        })
        gracePeriodEndDate(validator: { val, obj ->
            val >= obj.endDate
        })
        startingGracePeriodDays(nullable: true, min: 1)
        type(nullable: true)
        family(nullable: true)
        createdBy(nullable: true)
        autoPayAttempts(nullable: true)
    }

    static mapping = {
        autoTimestamp true
        sort 'dateCreated'
        endDate index: true
    }

    static Membership newInstanceWithDates(MembershipType type,
            Facility facility, LocalDate startDate) {
        def amount = type?.validTimeAmount
        def unit = type?.validTimeUnit
        if (!amount || !unit) {
            amount = facility.membershipValidTimeAmount
            unit = facility.membershipValidTimeUnit
        }
        def endDate = unit.addTime(startDate, amount).minusDays(1)

        if (unit == TimeUnit.YEAR) {
            def startDateYearly = type?.startDateYearly ?:
                    facility.yearlyMembershipStartDate
            def purchaseDaysInAdvance = type?.purchaseDaysInAdvanceYearly ?:
                    facility.yearlyMembershipPurchaseDaysInAdvance

            if (startDateYearly) {
                def start = startDateYearly.withYear(startDate.getYear())
                def startInAdvance = purchaseDaysInAdvance ?
                        start.minusDays(purchaseDaysInAdvance) : start
                if (startDate > start && startDate >= startInAdvance.plusYears(1)) {
                    start = start.plusYears(1)
                } else if (startDate < startInAdvance) {
                    start = start.minusYears(1)
                }

                if (startDate < start) {
                    startDate = start
                }
                endDate = unit.addTime(start, amount).minusDays(1)
            }
        }

        new Membership(type: type, startDate: startDate, endDate: endDate,
                gracePeriodEndDate: facility.membershipGraceNrOfDays ?
                        endDate.plusDays(facility.membershipGraceNrOfDays) : endDate)
    }

    static void unlink(Customer customer) {
        customer.memberships.collect().each { m ->
            customer.removeFromMemberships(m)
            m.delete()
            customer.save(flush: true)
        }
    }

    /**
     * Returns repayable orders of memberships that are not ended
     * @param orders
     * @return
     */
    static List<Order> getRemotePayablesForOrders(List<Order> orders) {
        LocalDate now = new LocalDate()

        return createCriteria().list {
            inList("order", orders)
            gte("endDate", now)
            eq("activated", true)
        }*.order?.findAll { Order order ->
            return order.isRemotePayable()
        }
    }

    boolean isRemotePayable() {
        LocalDate now = new LocalDate()
        return (endDate >= now) && order.isRemotePayable() && activated
    }

    def isFamilyContact() {
        return family && family.contact?.id?.equals(customer.id)
    }
    def isFamilyMember() {
        return family && !family.contact?.id?.equals(customer.id)
    }

    Long getPrice(Boolean ignoreNonActiveMembers = false) {
        if (family) {
            if (isFamilyContact() && type && type.price) {
                def price = type.price
                family.members.findAll {
                    !ignoreNonActiveMembers || (it.activated && !it.cancel)
                }.each { Membership fm ->
                    if (!fm.equals(this) && fm.type?.price) {
                        price += fm.type.price
                    }
                }
                return Math.min(price, customer.facility.familyMaxPrice)
            } else {
                return 0
            }
        }
        return type?.price ?: 0
    }

    Long getRecurringPrice() {
        if (isFamilyContact() && Membership.countByOrder(order) > 1) {
            def price = 0
            family.members.each {
                if (it.type && it.activated && !it.cancel) {
                    price += it.type.price
                }
            }
            return Math.min(price, customer.facility.familyMaxPrice)
        }

        type?.price ?: 0
    }

    boolean coversSlotTime(Slot slot) {
        LocalDate slotStartTime = new LocalDate(slot.startTime)
        return (isActive(slotStartTime) || isInStartingGracePeriod(slotStartTime))
    }

    boolean isPaid() {
        order?.status in [Order.Status.CONFIRMED, Order.Status.COMPLETED] && order.isFinalPaid()
    }

    boolean isActive(LocalDate date = new LocalDate()) {
        activated && startDate <= date && gracePeriodEndDate >= date && isPaid()
    }

    boolean isUpcoming() {
        startDate > new LocalDate()
    }

    /**
     * A membership is ending if within the grace time and is paid and activated
     * @return
     */
    boolean isEnding() {
        activated && isInGracePeriod() && isPaid()
    }

    boolean isInStatus(FilterCustomerCommand.MemberStatus memberStatus) {
        switch(memberStatus) {
            case FilterCustomerCommand.MemberStatus.PENDING:
                return !activated
            case FilterCustomerCommand.MemberStatus.PAID:
                return activated && !cancel && isPaid()
            case FilterCustomerCommand.MemberStatus.UNPAID:
                return activated && !isPaid()
            case FilterCustomerCommand.MemberStatus.CANCEL:
                return activated && cancel && isPaid()
            case FilterCustomerCommand.MemberStatus.FAILED_PAYMENT:
                return activated && autoPayAttempts && !isPaid()
        }
    }

    boolean isInFamilyStatus(FilterCustomerCommand.ShowMembers familyStatus) {
        switch(familyStatus) {
            case FilterCustomerCommand.ShowMembers.FAMILY_MEMBERS:
                return family
            case FilterCustomerCommand.ShowMembers.FAMILY_MEMBER_CONTACTS:
                return isFamilyContact()
            case FilterCustomerCommand.ShowMembers.FAMILY_MEMBER_MEMBERS:
                return isFamilyMember()
            case FilterCustomerCommand.ShowMembers.NO_FAMILY_MEMBERS:
                return !family
            case FilterCustomerCommand.ShowMembers.MEMBERS_ONLY:
                return true
        }
    }

    boolean isInGracePeriod() {
        def today = new LocalDate()
        endDate != gracePeriodEndDate &&
                endDate < today && today <= gracePeriodEndDate
    }

    boolean hasGracePeriod() {
        endDate != gracePeriodEndDate
    }

    boolean isInStartingGracePeriod(LocalDate date = new LocalDate()) {
        final boolean dateIsAfterMembershipStart = startDate <= date
        final boolean dateIsBeforeMembershipEnds = endDate >= date
        final boolean coversStartGracePeriod = startingGracePeriodDays && date <= startDate.plusDays(startingGracePeriodDays-1)

        return dateIsAfterMembershipStart && dateIsBeforeMembershipEnds && coversStartGracePeriod && activated
    }

    /**
     * A membership is "unpaid payable" if it is started or starts within "Yearly membership purchase days in advance" but just waiting for the customer's payment
     * @return
     */
    boolean isUnpaidPayable(int yearlyMembershipPurchaseDaysInAdvance) {
        LocalDate today = new LocalDate()
        return startDate.minusDays(yearlyMembershipPurchaseDaysInAdvance) <= today && endDate >= today && !isPaid() && activated
    }

    // TODO: Stale stuff to be removed after Membership PRO feature
    // TODO: it's still used by customer import/export managers (for backward compatibility)
    @Deprecated
    static enum Status {
        ACTIVE,NOT_ACTIVE,CANCEL,PENDING,CANCELLED

        static list() {
            return [ ACTIVE,NOT_ACTIVE,CANCEL,PENDING,CANCELLED ]
        }

        static List<String> listActiveStatuses() {
            [ACTIVE.name(), CANCEL.name()]
        }

        static List<String> listPendingStatuses() {
            [NOT_ACTIVE.name(), PENDING.name(), CANCELLED.name()]
        }
    }

    @Override
    void replaceOrderAndSave(Order order) {
        this.order = order
        this.save(flush: true, failOnError: true)
    }

    void setSharedOrder(Order order, Boolean flushUpdate = false) {
        if (this.order.id != order.id) {
            getOrderStatusService().annul(this.order, new SystemEventInitiator())
            this.order = order
            this.save(flush: flushUpdate)
        }
    }

    Membership getPreviousMembership() {
        Membership.findByCustomerAndStartDateLessThan(customer, startDate,
                [sort: "startDate", order: "desc"])
    }


    boolean isAfterGracePeriod(){
        LocalDate today = new LocalDate()
        gracePeriodEndDate > today
    }

    private OrderStatusService getOrderStatusService() {
        return Holders.grailsApplication.mainContext.getBean('orderStatusService')
    }
}
