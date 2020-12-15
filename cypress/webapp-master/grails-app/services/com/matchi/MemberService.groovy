package com.matchi

import com.matchi.FacilityProperty.FacilityPropertyKey
import com.matchi.adyen.AdyenException
import com.matchi.enums.MembershipRequestSetting
import com.matchi.events.SystemEventInitiator
import com.matchi.facility.FilterCustomerCommand
import com.matchi.facility.MembershipCommand
import com.matchi.invoice.Invoice
import com.matchi.membership.Membership
import com.matchi.membership.MembershipType
import com.matchi.membership.TimeUnit
import com.matchi.orders.AdyenOrderPayment
import com.matchi.orders.InvoiceOrderPayment
import com.matchi.orders.Order
import com.matchi.orders.OrderPayment
import com.matchi.price.Price
import grails.compiler.GrailsCompileStatic
import grails.transaction.Transactional
import groovy.transform.TypeCheckingMode
import groovyx.gpars.GParsPool
import org.apache.commons.lang.StringUtils
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.joda.time.LocalDate
import org.springframework.beans.factory.annotation.Value
import org.springframework.util.StopWatch

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

@GrailsCompileStatic
class MemberService {

    static transactional = false

    CashService cashService
    GrailsApplication grailsApplication
    UserService userService
    MembersFamilyService membersFamilyService
    MembershipPaymentService membershipPaymentService
    NotificationService notificationService
    PaymentService paymentService
    OrderStatusService orderStatusService
    CustomerService customerService
    FacilityService facilityService

    @Value('${matchi.membership.upcoming.purchase.daysInAdvance.monthly}')
    Integer monthlyMembershipDaysInAdvance

    @Value('${matchi.membership.upcoming.purchase.daysInAdvance.yearly}')
    Integer yearlyMembershipDaysInAdvance

    @Value('${matchi.membershipRenewal.batchSize}')
    Integer renewalBatchSize

    @Value('${matchi.membershipRenewal.poolSize}')
    Integer renewalPoolSize

    @Value('${matchi.membership.payment.failedAttemptsThreshold}')
    Integer failedPaymentAttemptsThreshold

    @Transactional
    Membership addMembership(Customer customer, MembershipType type, User issuer,
                             boolean activated, String origin, Order order = null) {
        addMembership(customer, type, null, null, null, null, issuer, activated, origin, order)
    }

    @Transactional
    Membership addMembership(Customer customer, MembershipType type, LocalDate startDate, Order order) {
        addMembership(customer, type, startDate, null, null, null, null,
            true, Order.ORIGIN_FACILITY, order)
    }

    @Transactional
    Membership addMembership(Customer customer, MembershipType type, LocalDate startDate = null,
                             LocalDate endDate = null, LocalDate gracePeriodEndDate = null,
                             User orderUser = null, User issuer = null, boolean activated = true,
                             String origin = Order.ORIGIN_FACILITY, Order order = null,
                             Integer startingGracePeriodDays = null) {

        if (!startDate) {
            startDate = new LocalDate()
        }
        if (!issuer) {
            issuer = userService.getLoggedInUser()
        }

        def membership = initNewMembership(customer, type, startDate,
            issuer, endDate, gracePeriodEndDate)
        if (!membership) {
            return null
        }

        if (!order) {
            order = membershipPaymentService.createMembershipPaymentOrder(
                orderUser ?: customer.user, type, origin, issuer, customer)
            if (!order.total()) {
                orderStatusService.complete(order, issuer)
            }
        } else if (Boolean.valueOf((String) order.metadata?.get(Order.META_ALLOW_RECURRING))
            && customer.user?.id == issuer.id) {
            membership.autoPay = true
        }

        membership.order = order
        membership.activated = activated
        membership.startingGracePeriodDays = startingGracePeriodDays

        customer.addToMemberships(membership)
        customer.save(flush: true)

        membership
    }

    Membership getMembership(Customer customer) {
        getMembership(customer, null)
    }

    Collection<Membership> getMemberships(Customer customer, Boolean searchAll = true) {
        Collection<Membership> memberships = []
        customerService.findHierarchicalUserCustomers(customer, searchAll).collect {
            if (it?.memberships)
                memberships.addAll(it.memberships)
        }
        memberships
    }

    Membership getMembership(Customer customer, LocalDate start) {
        getMembershipByFilter(customer, new FilterCustomerCommand(membershipStartDate: start))
    }

    Membership getMembershipByFilter(Customer customer, FilterCustomerCommand cmd) {
        if (!customer) {
            return null
        }
        def start = cmd?.membershipStartDate ?: new LocalDate()
        def end = cmd?.membershipEndDate ?: start

        def sortedValidMemberships = getMemberships(customer).findAll { currentMembership ->
            if (currentMembership.startDate > end || currentMembership.gracePeriodEndDate < start) {
                return false
            }
            if (cmd?.status && !cmd.status.any { currentMembership.isInStatus(it) }) {
                return false
            }
            if (cmd?.type && !cmd.type.any { currentMembership.type?.id == it || (it == 0 && !currentMembership.type) }) {
                return false
            }
            if (cmd?.members && !cmd.members.any { currentMembership.isInFamilyStatus(it) }) {
                return false
            }
            return true
        }.sort {
            it.startDate
        }

        sortedValidMemberships ? sortedValidMemberships[-1] : null
    }

    @Transactional
    void removeMembership(Membership membership, Boolean refund = null,
                          LocalDate forceEndDate = null) {
        if (refund && membership.order.isStillRefundable()) {
            orderStatusService.annul(membership.order, new SystemEventInitiator(), "Credit membership as facility admin", membership.order.total())
        }

        def family = membership.family

        if (family) {
            membersFamilyService.removeFamilyMember(membership)
        }

        def endDate = membership.endDate ?: new LocalDate().minusDays(1)
        membership.endDate = forceEndDate?.isBefore(endDate) ? forceEndDate : endDate
        membership.gracePeriodEndDate = membership.endDate
        if (membership.startDate > membership.endDate) {
            membership.startDate = membership.endDate
        }
        membership.save()

        log.info("Ended customer ${membership.customer.number} membership in ${membership.customer.facility.name}")
    }

    @Transactional
    Membership requestMembership(Customer customer, MembershipType type,
                                 Order order = null, LocalDate startDate = null) {
        if (!startDate && customer.membership) {
            removeMembership(customer.membership)
        } else if (startDate) {
            def m = customer.getMembership(startDate)
            if (m && (!m.hasGracePeriod() || startDate <= m.endDate)) {
                removeMembership(m, false, startDate.minusDays(1))
            }
        }

        def activated = order ? true : customer.facility.membershipRequestSetting?.equals(MembershipRequestSetting.DIRECT)
        Integer startingGracePeriod = !order && customer.facility.isMembershipStartingGracePeriodEnabled() ?
            customer.facility.membershipStartingGraceNrOfDays : null
        def membership = addMembership(customer, type, startDate, null, null, null, null,
            activated, Order.ORIGIN_WEB, order, startingGracePeriod)

        membership
    }

    @GrailsCompileStatic(TypeCheckingMode.SKIP)
    void handleMembershipInvoicePayment(Invoice invoice) {
        InvoiceOrderPayment.createCriteria().listDistinct {
            createAlias("orders", "o")
            createAlias("invoiceRow", "ir")
            eq("ir.invoice", invoice)
            eq("o.article", Order.Article.MEMBERSHIP)
            isNotNull("o.customer")
        }.each { orderPayment ->
            InvoiceOrderPayment.withTransaction {
                if (orderPayment.status == OrderPayment.Status.NEW
                    && invoice.status == Invoice.InvoiceStatus.PAID) {
                    orderPayment.status = OrderPayment.Status.CAPTURED
                    orderPayment.save(flush: true)
                    log.debug "OrderPayment $orderPayment.id is CAPTURED now since invoice was paid"
                    orderPayment.orders.each { order ->
                        handleMembershipPayment(order)

                        def memberships = Membership.findAllByOrder(order)
                        if (memberships) {
                            def membership = memberships.size() == 1 ? memberships[0] :
                                memberships.find { it.isFamilyContact() }
                            if (membership?.type?.recurring && membership.autoPayAttempts
                                && membership.previousMembership?.autoPay) {
                                handleSuccessRenewalPayment(membership)
                            }
                        }
                    }
                } else if (orderPayment.status != OrderPayment.Status.CREDITED &&
                    invoice.status == Invoice.InvoiceStatus.CREDITED) {
                    orderPayment.refund(orderPayment.amount)
                    orderPayment.save(flush: true)
                    log.debug "OrderPayment $orderPayment.id is CREDITED now since invoice was credited"
                    orderPayment.orders.each {
                        revertFamilyMembersPayment(it)
                    }
                } else if (invoice.status == Invoice.InvoiceStatus.CANCELLED) {
                    orderPayment.status = OrderPayment.Status.ANNULLED
                    orderPayment.save(flush: true)
                    log.debug "OrderPayment $orderPayment.id is ANNULLED now since invoice was cancelled"
                    orderPayment.orders.each {
                        revertFamilyMembersPayment(it)
                    }
                } else if (orderPayment.status == OrderPayment.Status.CAPTURED
                    && invoice.status == Invoice.InvoiceStatus.POSTED) {
                    orderPayment.status = OrderPayment.Status.NEW
                    orderPayment.save(flush: true)
                    log.debug "OrderPayment $orderPayment.id is back to NEW status since invoice was changed to posted"
                    orderPayment.orders.each {
                        revertFamilyMembersPayment(it)
                    }
                }
            }
        }
    }

    @GrailsCompileStatic(TypeCheckingMode.SKIP)
    void renewMemberships(LocalDate endDate = null) {
        if (!endDate) {
            endDate = new LocalDate()
        }

        def membershipIds = Membership.executeQuery("select m.id from Membership as m \
                where m.endDate = :end and m.cancel = :cancel and m.activated = :activated and not exists (\
                        from Membership as m2 where m2.customer = m.customer and m2.startDate > :end)",
            [end: endDate, cancel: false, activated: true])
        if (!membershipIds) {
            return
        }
        def contactMembershipIds = [].asSynchronized()
        def recurringMembershipIds = [].asSynchronized()
        def upcomingMembershipIds = new ConcurrentHashMap()

        GParsPool.withPool(renewalPoolSize) {
            def total = new AtomicInteger(0)
            def stopWatch = new StopWatch("Membership renewal batch stop watch")
            stopWatch.start()

            membershipIds.collate(renewalBatchSize).eachParallel { ids ->
                Membership.withNewSession {
                    Membership.findAllByIdInList(ids).each { currentMembership ->
                        try {
                            if (!currentMembership.isPaid()) {
                                log.debug "Unable to renew membership $currentMembership.id for customer $currentMembership.customer.id due to unpaid status"
                                return
                            }

                            log.debug "Renew membership '${currentMembership.type ?: ''}' (ID: $currentMembership.id) for customer $currentMembership.customer.id"

                            Membership.withTransaction {
                                def startDate = currentMembership.endDate.plusDays(1)
                                def customer = currentMembership.customer

                                def upcomingMembership = addMembership(customer,
                                    currentMembership.type, startDate, null, null,
                                    customer.user ?: currentMembership.order.user,
                                    customer.user ?: currentMembership.order.issuer,
                                    true, currentMembership.order.origin)

                                if (upcomingMembership) {
                                    upcomingMembershipIds[currentMembership.id] = upcomingMembership.id
                                    if (upcomingMembership.order.isFree()) {
                                        clearGracePeriod(currentMembership)
                                    } else if (upcomingMembership.type?.paidOnRenewal) {
                                        recurringMembershipIds << currentMembership.id
                                    }
                                    if (currentMembership.isFamilyContact()) {
                                        contactMembershipIds << currentMembership.id
                                    }
                                    total.incrementAndGet()
                                } else {
                                    log.error "Unable to create upcoming membership for membership with ID: $currentMembership.id"
                                }
                            }
                        } catch (e) {
                            log.error "Unable to process membership (ID: $currentMembership.id) renewal. $e.message", e
                        }
                    }
                }
            }

            stopWatch.stop()
            def time = Math.max((int) stopWatch.lastTaskTimeMillis / 1000, 1)
            def membershipPerSec = (total.get() > 0 ? (int) (total.get() / time) : 0)
            log.info("Renewed $total memberships in ${stopWatch.lastTaskTimeMillis} ms (${membershipPerSec} memberships/sec)")
        }

        if (contactMembershipIds) {
            GParsPool.withPool(renewalPoolSize) {
                log.info "Renew ${contactMembershipIds.size()} families..."
                def w = new StopWatch()
                w.start()

                contactMembershipIds.collate(renewalBatchSize).eachParallel { ids ->
                    Membership.withNewSession {
                        Membership.findAllByIdInList(ids).each { contactCurrentMembership ->
                            try {
                                Membership.withTransaction {
                                    def family = membersFamilyService.createFamily(
                                        Membership.get(upcomingMembershipIds[contactCurrentMembership.id]))
                                    contactCurrentMembership.family.membersNotContact.each { fm ->
                                        def familyUpcomingMembership = upcomingMembershipIds[fm.id] ?
                                            Membership.get(upcomingMembershipIds[fm.id]) :
                                            Membership.findByCustomerAndStartDateGreaterThan(fm.customer, fm.startDate)
                                        if (familyUpcomingMembership && !familyUpcomingMembership.family) {
                                            membersFamilyService.addFamilyMember(familyUpcomingMembership, family)
                                        }
                                    }
                                }
                            } catch (e) {
                                log.error "Unable to renew family (ID: ${contactCurrentMembership.family?.id}). $e.message", e
                            }
                        }
                    }
                }

                w.stop()
                log.info "Renewed ${contactMembershipIds.size()} families in $w.lastTaskTimeMillis ms"
            }
        }

        if (recurringMembershipIds) {
            GParsPool.withPool(renewalPoolSize) {
                log.info "Make payments for renewed memberships..."
                def total = new AtomicInteger(0)
                def w = new StopWatch()
                w.start()

                recurringMembershipIds.collate(renewalBatchSize).eachParallel { ids ->
                    Membership.withNewSession {
                        Membership.findAllByIdInList(ids).each { currentMembership ->
                            try {
                                def upcomingMembership = Membership.get(
                                    upcomingMembershipIds[currentMembership.id])
                                def customer = upcomingMembership.customer
                                def useSharedOrder = false

                                Membership.withTransaction {
                                    if (customer.facility.isFacilityPropertyEnabled(
                                        FacilityPropertyKey.FEATURE_RECURRING_MEMBERSHIP)) {
                                        if (currentMembership.autoPay && customer.user) {
                                            def order = upcomingMembership.order

                                            if (currentMembership.isFamilyContact()
                                                && Membership.countByOrder(currentMembership.order) > 1) {
                                                order = membershipPaymentService.createFamilyMembershipPaymentOrder(
                                                    upcomingMembership, customer.user, customer)
                                                useSharedOrder = true
                                            }

                                            try {
                                                paymentService.handleCreditCardPayment(order, customer.user)
                                            } catch (AdyenException ae) {
                                                handleFailedRenewalPayment(order, ae.getMessage(),
                                                    upcomingMembership, true, useSharedOrder)
                                                throw ae
                                            }

                                            if (useSharedOrder) {
                                                setSharedOrder(upcomingMembership, order)
                                            }
                                            handleSuccessRenewalPayment(upcomingMembership)
                                            total.incrementAndGet()
                                            notificationService.sendMembershipRenewReceipt(upcomingMembership)
                                        }
                                    } else {
                                        cashService.createCashOrderPayment(upcomingMembership.order)
                                        clearGracePeriod(currentMembership)
                                        total.incrementAndGet()
                                    }
                                }
                            } catch (e) {
                                if (!currentMembership.isAttached()) {
                                    currentMembership.attach()
                                }

                                log.error "Unable to make payment for upcoming customer's (ID: $currentMembership.customer.id) membership. $e.message", e
                            }
                        }
                    }
                }

                w.stop()
                log.info "Made payments for $total memberships in $w.lastTaskTimeMillis ms"
            }
        }
    }

    @GrailsCompileStatic(TypeCheckingMode.SKIP)
    void retryFailedMembershipPayments() {
        def membershipIds = Membership.withCriteria {
            projections {
                property("id")
            }
            gt("endDate", LocalDate.now())
            lt("autoPayAttempts", failedPaymentAttemptsThreshold)
        }

        if (membershipIds) {
            def facilityNotificationCustomers = new ConcurrentHashMap()

            GParsPool.withPool(renewalPoolSize) {
                membershipIds.collate(renewalBatchSize).eachParallel { ids ->
                    Membership.withNewSession {
                        Membership.findAllByIdInList(ids).each { membership ->
                            try {
                                if (membership.isPaid()) {

                                    handleSuccessRenewalPayment(membership)

                                } else if (!membership.order.isInvoiced()) {

                                    def customer = membership.customer
                                    def order = membership.order
                                    def useSharedOrder = false

                                    Membership.withTransaction {
                                        if (membership.isFamilyContact()) {
                                            def prevMembership = membership.previousMembership
                                            if (prevMembership && Membership.countByOrder(prevMembership.order) > 1) {
                                                order = membershipPaymentService.createFamilyMembershipPaymentOrder(
                                                    membership, customer.user, customer)
                                                useSharedOrder = true
                                            }
                                        }

                                        try {
                                            paymentService.handleCreditCardPayment(order, customer.user)
                                            if (useSharedOrder) {
                                                setSharedOrder(membership, order)
                                            }
                                            handleSuccessRenewalPayment(membership)
                                            notificationService.sendMembershipRenewReceipt(membership)

                                        } catch (AdyenException ae) {
                                            handleFailedRenewalPayment(order, ae.getMessage(), membership)

                                            if (membership.autoPayAttempts >= failedPaymentAttemptsThreshold) {
                                                facilityNotificationCustomers.putIfAbsent(
                                                    customer.facility.id, [].asSynchronized())
                                                facilityNotificationCustomers[customer.facility.id] <<
                                                    [id: customer.id, name: customer.fullName()]
                                            }
                                        }
                                    }
                                }
                            } catch (e) {
                                log.error "Unable to process membership (ID: $membership.id) retry payment. $e.message", e
                            }
                        }
                    }
                }
            }

            facilityNotificationCustomers.each { fid, customers ->
                notificationService.sendMembershipPaymentFailedAdminNotification(
                    Facility.get(fid), customers)
            }
        }
    }

    @Transactional
    void disableAutoRenewal(Membership membership) {
        membership.gracePeriodEndDate = membership.endDate
        membership.cancel = true
        membership.save()
    }

    @Transactional
    void enableAutoRenewal(Membership membership) {
        membership.cancel = false
        membership.save()
    }

    @Transactional
    void activateMembership(Membership membership) {
        membership.activated = true
        membership.save()
    }

    @Transactional
    void deactivateMembership(Membership membership) {
        membership.activated = false
        membership.save()
    }

    @Transactional
    void updateGracePeriod(Membership membership, Integer days) {
        updateGracePeriod(membership, membership.gracePeriodEndDate.plusDays(days))
    }

    @Transactional
    void updateGracePeriod(Membership membership, LocalDate newDate) {
        if (membership.endDate <= newDate) {
            membership.gracePeriodEndDate = newDate
            membership.save()
        }
    }

    @Transactional
    void clearGracePeriod(Membership membership) {
        if (membership && membership.gracePeriodEndDate != membership.endDate) {
            membership.gracePeriodEndDate = membership.endDate
            membership.save()
        }
    }

    List getFormMembershipTypes(Facility facility, Customer customer = null) {
        def startDate = new LocalDate()
        def upcomingStartDate
        def upcomingEndDate

        if (customer?.hasMembership()) {
            startDate = customer.memberships.max { it.endDate }.endDate.plusDays(1)
        } else if (customer?.hasUpcomingMembership()) {
            def memberships = customer.memberships.findAll { it.isUpcoming() }
            upcomingStartDate = memberships.min { it.startDate }.startDate
            upcomingEndDate = memberships.max { it.endDate }.endDate
        }

        facility.membershipTypes.collect {
            def m = Membership.newInstanceWithDates(it, facility, startDate)

            if (upcomingStartDate && upcomingEndDate) {
                if (upcomingStartDate <= m.startDate && m.startDate <= upcomingEndDate) {
                    m = Membership.newInstanceWithDates(it, facility, upcomingEndDate.plusDays(1))
                } else if (m.startDate < upcomingStartDate && upcomingStartDate <= m.endDate) {
                    m.endDate = upcomingStartDate.minusDays(1)
                    m.gracePeriodEndDate = m.endDate
                }
            }

            [id                : it.id, name: it.name, startDate: m.startDate, endDate: m.endDate,
             gracePeriodEndDate: m.gracePeriodEndDate]
        }
    }

    @GrailsCompileStatic(TypeCheckingMode.SKIP)
    Boolean isMembershipOverlapping(Customer customer, LocalDate start, LocalDate end, Long id = null) {
        Membership.withCriteria {
            projections {
                rowCount()
            }
            if (id) {
                ne("id", id)
            }
            eq("customer", customer)
            le("startDate", end)
            ge("endDate", start)
        }[0]
    }

    @Transactional
    Membership updateMembership(Membership membership, MembershipCommand cmd) {
        def resetOrder = cmd.type && !membership.type

        membership.with {
            type = cmd.type
            startDate = new LocalDate(cmd.startDate)
            endDate = new LocalDate(cmd.endDate)
            gracePeriodEndDate = new LocalDate(cmd.gracePeriodEndDate)
            startingGracePeriodDays = cmd.startingGracePeriodDays
        }

        if (membership.validate()) {
            if (resetOrder) {
                resetMembershipOrder(membership, cmd.type, userService.getLoggedInUser())
            }

            if (cmd.paid && membership.order && !membership.order.isFinalPaid()
                && !membership.order.isInvoiced()) {
                cashService.createCashOrderPayment(membership.order)
            } else if (!cmd.paid && membership.order.isPaidByCash()) {
                refundCashPayments(membership)
            }

            if (membership.cancel && !cmd.cancel) {
                enableAutoRenewal(membership)
            } else if (!membership.cancel && cmd.cancel) {
                disableAutoRenewal(membership)
            }

            return membership.save(flush: true)
        } else {
            return null
        }
    }

    @Transactional
    Membership updateMembershipFields(Membership membership, User issuer, Map data) {
        membership.with {
            if (data.startDate) {
                startDate = (LocalDate) data.startDate
            }
            if (data.endDate) {
                endDate = (LocalDate) data.endDate
            }
            if (data.gracePeriodEndDate) {
                gracePeriodEndDate = (LocalDate) data.gracePeriodEndDate
            }
            if (data.startingGracePeriodDays) {
                startingGracePeriodDays = (Integer) data.startingGracePeriodDays
            }
            if (data.typeId && data.typeId != type?.id) {
                MembershipType t = MembershipType.findById(data.typeId)
                if (!type) {
                    resetMembershipOrder(membership, t, issuer)
                }
                type = t
            }
        }

        membership.validate() ? membership.save(flush: true) : null
    }

    List<Membership> listUserMemberships(User user) {
        def ids = Membership.executeQuery("""
                select min(m.id) from Membership m join m.customer c
                where c.user.id = :userId
                    and m.startDate = (
                        select min(m2.startDate) from Membership m2 join m2.customer c2
                        where c2.user.id = :userId and c2.facility.id = c.facility.id
                            and m2.gracePeriodEndDate >= :today
                    )
                group by c.facility.id""",
            [userId: user.id, today: LocalDate.now()])

        ids ? Membership.findAllByIdInList(ids, [fetch: [customer: "join"]]) : null
    }

    Boolean isUpcomingMembershipAvailableForPurchase(Membership currentMembership) {
        if (currentMembership?.isActive() && !currentMembership.cancel && !currentMembership.autoPay
            && currentMembership.type?.price && currentMembership.type.availableOnline
            && currentMembership.type.facility.isMembershipRequestPaymentEnabled()) {

            def today = LocalDate.now()
            def validity = (currentMembership.type.validTimeAmount && currentMembership.type.validTimeUnit) ?
                currentMembership.type.validTimeUnit : currentMembership.type.facility.membershipValidTimeUnit
            if (validity == TimeUnit.YEAR) {
                if (today.plusYears(1) > currentMembership.endDate) {
                    def startDateYearly = currentMembership.type.startDateYearly ?:
                        currentMembership.type.facility.yearlyMembershipStartDate
                    if (startDateYearly) {
                        def purchaseDaysInAdvance = currentMembership.type.purchaseDaysInAdvanceYearly ?:
                            currentMembership.type.facility.yearlyMembershipPurchaseDaysInAdvance
                        def start = startDateYearly.withYear(today.getYear())
                        if (start < today) {
                            start = start.plusYears(1)
                        }
                        if (purchaseDaysInAdvance) {
                            start = start.minusDays(purchaseDaysInAdvance)
                        }
                        return today >= start
                    } else {
                        return today.plusDays(yearlyMembershipDaysInAdvance) >= currentMembership.endDate
                    }
                }
            } else if (validity == TimeUnit.MONTH) {
                return today.plusDays(monthlyMembershipDaysInAdvance) >= currentMembership.endDate
            }
        }

        false
    }

    @Transactional
    void refundCashPayments(Membership membership) {
        membership.order.payments.each {
            def op = (OrderPayment) it
            if (op.status in [OrderPayment.Status.AUTHED, OrderPayment.Status.CAPTURED] &&
                op.type == "Cash") {
                op.refund(op.amount)
                op.save()
            }
        }
    }

    @GrailsCompileStatic(TypeCheckingMode.SKIP)
    Membership getMembership(Order order, User user = null) {
        Membership.withCriteria {
            createAlias("customer", "c")
            eq("c.user", user ?: userService.loggedInUser)
            eq("order", order)
        }[0] as Membership
    }

    Collection<Membership> getActiveMemberships(User user, Facility facility) {
        Collection<Customer> customers = customerService.findHierarchicalUserCustomers(user, facilityService.getAllHierarchicalFacilities(facility))

        Collection<Membership> memberships = []
        customers.each { Customer c ->
            if (c?.membership) {
                memberships.add(c.membership)
            }
        }
        return memberships.sort { it.startDate }
    }

    @GrailsCompileStatic(TypeCheckingMode.SKIP)
    def getAvailableMembershipTypesWithDates(Map model, Facility facility, User user, Membership baseMembership) {
        def results = MembershipType.availableForPurchase(facility)?.list()?.findResults { MembershipType membershipType ->
            if (!membershipType.facility.recieveMembershipRequests) {
                return null
            }
            Facility membershipTypeFacility = membershipType.facility
            def m = Membership.newInstanceWithDates(membershipType, membershipTypeFacility, model.startDate as LocalDate)

            Customer c = Customer.findByUserAndFacility(user, membershipTypeFacility)

            boolean addType
            if (baseMembership) {
                Membership overlapping = c?.getMembership(baseMembership?.startDate)
                addType = (!overlapping || overlapping == baseMembership)
            } else {
                addType = !(c?.hasUpcomingMembership())
            }

            if (addType) {
                return [type: membershipType, startDate: m.startDate.toDate(), endDate: m.endDate.toDate()]
            }
            return null
        }
        results.removeAll([null])
        return results
    }

    Collection<Membership> getUpcomingMemberships(User user, Facility facility) {
        Collection<Customer> customers = customerService.findHierarchicalUserCustomers(user, facilityService.getAllHierarchicalFacilities(facility))

        Collection<Membership> memberships = []
        customers.each { Customer c ->
            def upcomingMemberships = c?.getUpcomingMembership()
            if (upcomingMemberships) {
                memberships.add(upcomingMemberships)
            }
        }
        memberships.sort { it.startDate }
    }

    Collection<Membership> getRemotelyPayableMemberships(User user, Facility facility) {
        Collection<Facility> facilities = facilityService.getAllHierarchicalFacilities(facility)

        facilities = facilities.findAll { it.hasEnabledRemotePaymentsFor(Order.Article.MEMBERSHIP) }

        Collection<Customer> customers = customerService.findHierarchicalUserCustomers(user, facilities)

        Collection<Membership> memberships = []
        customers.each { Customer c ->
            if (c?.membership?.isRemotePayable()) {
                memberships.add(c.membership)
            }
        }
        memberships.sort { it.startDate }
    }

    Collection<Membership> getUnpaidStartedMemberships(User user, Facility facility) {
        Collection<Customer> customers = customerService.findHierarchicalUserCustomers(user, facilityService.getAllHierarchicalFacilities(facility))

        Collection<Membership> memberships = []
        customers.each { Customer c ->
            if (c?.membership?.isUnpaidPayable(c.facility.yearlyMembershipPurchaseDaysInAdvance ?: 0)) {
                memberships.add(c.membership)
            }
        }
        memberships.sort { it.startDate }
    }

    Collection<Membership> listUserRecurringMembershipsToRenew(User user) {
        Membership.executeQuery("from Membership as m \
                where m.customer.user.id = :userId and m.endDate > :end and m.cancel = :cancel \
                and m.activated = :activated and m.autoPay = :autoPay and not exists (\
                        from Membership as m2 where m2.customer = m.customer and m2.startDate > m.startDate)",
            [userId: user.id, end: LocalDate.now(), cancel: false, activated: true, autoPay: true]).findAll {
            it.paid && it.type?.recurring
        }
    }

    private Membership initNewMembership(Customer customer, MembershipType type, LocalDate startDate,
                                         User createdBy = null, LocalDate endDate = null, LocalDate gracePeriodEndDate = null) {

        def m
        if (endDate && gracePeriodEndDate) {
            m = new Membership(type: type, startDate: startDate, endDate: endDate,
                gracePeriodEndDate: gracePeriodEndDate)
            if (isMembershipOverlapping(customer, m.startDate, m.endDate)) {
                return null
            }
        } else {
            m = Membership.newInstanceWithDates(type, customer.facility, startDate)

            def overlappedMembership = Membership.findByCustomerAndStartDateBetween(
                customer, m.startDate, m.gracePeriodEndDate,
                [sort: "startDate", order: "asc"])
            if (overlappedMembership) {
                if (m.startDate == overlappedMembership.startDate) {
                    log.error "Customer ${customer.number} already has a membership starting from $m.startDate"
                    return null
                }
                m.gracePeriodEndDate = overlappedMembership.startDate.minusDays(1)
                if (m.endDate > m.gracePeriodEndDate) {
                    m.endDate = m.gracePeriodEndDate
                }
            }
        }
        m.createdBy = createdBy

        m
    }

    private void handleMembershipPayment(Order order) {
        def membership = (Membership) Membership.findAllByOrder(order).find {
            ((Membership) it).isFamilyContact()
        }
        if (membership) {
            def mprice = membership.getPrice()
            if (order.price > mprice) {
                order.price = mprice
                order.vat = Price.calculateVATAmount(mprice,
                    new Double(membership.customer.facility.vat))
                order.save(flush: true)
            }

            if (order.isFinalPaid()) {
                orderStatusService.complete(order, userService.getCurrentUser())
                log.debug "Order $order.id is completed now"

                membership.family.membersNotContact.each { fm ->
                    if (fm.order.id != order.id && !fm.isPaid()) {
                        fm.order.price = 0
                        fm.order.vat = 0
                        orderStatusService.complete(fm.order, userService.getCurrentUser())
                    }
                }
            }
        } else if (order.isFinalPaid()) {
            orderStatusService.complete(order, userService.getCurrentUser())
            log.debug "Order $order.id is completed now"
        }
    }

    private void revertFamilyMembersPayment(Order order) {
        if (!order.isFinalPaid()) {
            def membership = (Membership) Membership.findAllByOrder(order).find {
                ((Membership) it).isFamilyContact()
            }
            if (membership) {
                membership.family.membersNotContact.each { fm ->
                    if (fm.order.id != order.id && !fm.order.price && fm.type?.price) {
                        fm.order.price = fm.type.price
                        fm.order.vat = Price.calculateVATAmount(fm.type.price,
                            new Double(fm.customer.facility.vat))
                        fm.order.save(flush: true)
                    }
                }
            }
        }
    }

    private void handleFailedRenewalPayment(Order order, String error, Membership membership,
                                            Boolean addGracePeriod = false, Boolean useSharedOrder = false) {

        order.payments.each {
            if (it instanceof AdyenOrderPayment && it.status == OrderPayment.Status.NEW) {
                it.errorMessage = StringUtils.abbreviate(
                    error, OrderPayment.ERROR_MESSAGE_MAX_SIZE)
                it.status = OrderPayment.Status.FAILED
                it.save()
            }
        }
        orderStatusService.cancel(order, new SystemEventInitiator(), true)

        if (addGracePeriod) {
            if (membership.type?.renewalStartingGraceNrOfDays) {
                membership.startingGracePeriodDays =
                    membership.type.renewalStartingGraceNrOfDays
            }
            if (useSharedOrder && order.metadata.membershipsToUpdate) {
                def membershipIdsToUpdate = order.metadata.membershipsToUpdate.toString().tokenize(",")*.toLong()
                if (membershipIdsToUpdate) {
                    membership.family.membersNotContact.each { fm ->
                        if (fm.id in membershipIdsToUpdate && fm.type?.renewalStartingGraceNrOfDays) {
                            fm.startingGracePeriodDays =
                                fm.type.renewalStartingGraceNrOfDays
                        }
                    }
                }
            }
        }

        if (!membership.autoPayAttempts) {
            membership.autoPayAttempts = 0
        }
        membership.autoPayAttempts++
        membership.save()

        if (membership.autoPayAttempts == 1
            || membership.autoPayAttempts >= failedPaymentAttemptsThreshold) {
            notificationService.sendMembershipPaymentFailedNotification(order,
                membership, membership.autoPayAttempts >= failedPaymentAttemptsThreshold)
        }
    }

    private void handleSuccessRenewalPayment(Membership membership) {
        if (membership.family) {
            membership.family.members.each { fm ->
                if (fm.isPaid()) {
                    clearGracePeriod(fm.previousMembership)
                    if (fm.startingGracePeriodDays) {
                        fm.startingGracePeriodDays = null
                        fm.save()
                    }
                }
            }
        } else {
            clearGracePeriod(membership.previousMembership)
            membership.startingGracePeriodDays = null
        }

        membership.autoPay = true
        membership.autoPayAttempts = null
        membership.save()
    }

    private void setSharedOrder(Membership membership, Order order) {
        membership.setSharedOrder(order)
        if (order.metadata.membershipsToUpdate) {
            def membershipIdsToUpdate = order.metadata.membershipsToUpdate.toString().tokenize(",")*.toLong()
            if (membershipIdsToUpdate) {
                membership.family.membersNotContact.each { fm ->
                    if (fm.id in membershipIdsToUpdate) {
                        fm.setSharedOrder(order)
                    }
                }
            }
        }
    }

    private void resetMembershipOrder(Membership membership, MembershipType type, User issuer) {
        membership.order = membershipPaymentService.createMembershipPaymentOrder(
            membership.customer.user, type, Order.ORIGIN_FACILITY,
            issuer, membership.customer)
        if (!membership.order.total()) {
            orderStatusService.complete(membership.order, issuer)
        }
    }

    void sendMembershipRequestNotification(Membership membership, Customer customer, String msg, Order order = null) {
        def activated = order ? true : customer.facility.membershipRequestSetting?.equals(MembershipRequestSetting.DIRECT)
        if (membership) {
            if (order) {
                notificationService.sendOnlineMembershipReceipt(membership)
            } else if (activated && membership.order.isFree()) {
                cashService.createCashOrderPayment(membership.order)
            }
            notificationService.sendMembershipRequestNotification(membership, customer.facility, msg)
        }
    }
}
