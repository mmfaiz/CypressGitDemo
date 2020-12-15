package com.matchi.admin

import com.matchi.Booking
import com.matchi.Customer
import com.matchi.Facility
import com.matchi.Payment
import com.matchi.Subscription
import com.matchi.User
import com.matchi.async.ScheduledTask
import com.matchi.enums.RedeemAt
import com.matchi.integration.KafkaProducerService
import com.matchi.jobs.FacilityConfigSyncJob
import com.matchi.jobs.FortnoxInvoiceJob
import com.matchi.jobs.IdrottOnlineActivitiesSyncJob
import com.matchi.jobs.IdrottOnlineMembershipSyncJob
import com.matchi.jobs.MembershipRenewJob
import com.matchi.jobs.adyen.RecurringPaymentRetryJob
import com.matchi.orders.CashOrderPayment
import com.matchi.orders.InvoiceOrderPayment
import com.matchi.orders.Order
import com.matchi.orders.OrderPayment
import com.matchi.payment.PaymentMethod
import com.matchi.subscriptionredeem.redeemstrategy.InvoiceRowRedeemStrategy
import groovyx.gpars.GParsPool
import org.apache.commons.lang.time.StopWatch
import org.hibernate.criterion.CriteriaSpecification
import org.joda.time.DateTime
import org.quartz.JobKey
import org.quartz.Scheduler
import org.quartz.Trigger
import org.quartz.impl.matchers.GroupMatcher

import java.text.SimpleDateFormat
import java.util.concurrent.atomic.AtomicInteger

class AdminJobController {

    public static final String TRIGGER_IDROTT_ONLINE_MEMBERSHIP_SYNC_JOB = "triggerIdrottOnlineMembershipSyncJob"
    public static final String TRIGGER_IDROTT_ONLINE_ACTIVITIES_SYNC_JOB = "triggerIdrottOnlineActivitiesSyncJob"
    public static final String TRIGGER_FORTNOX_INVOICE_JOB = "triggerFortnoxInvoiceJob"
    public static final String TRIGGER_FACILITY_CONFIG_SYNC_JOB = "triggerFacilityConfigSyncJob"

    Scheduler quartzScheduler
    def boxnetSyncService
    def fortnoxResyncService
    def redeemService
    def customerService
    def fileArchiveService
    def subscriptionService
    def participantMigrationService
    def userService
    def integrationService
    def groovySql
    def seasonService
    def scheduledTaskService
    KafkaProducerService kafkaProducerService

    def sessionFactory
    def propertyInstanceMap = org.codehaus.groovy.grails.plugins.DomainClassGrailsPlugin.PROPERTY_INSTANCE_MAP

    def grailsApplication

    def index() {

        def jobsInfo = []

        for (String groupName : quartzScheduler.getJobGroupNames()) {

            for (JobKey jobKey : quartzScheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName))) {

                String jobName = jobKey.getName()
                String jobGroup = jobKey.getGroup()

                //get job's trigger
                List<Trigger> triggers = (List<Trigger>) quartzScheduler.getTriggersOfJob(jobKey)

                if (triggers?.size() >= 1) {
                    jobsInfo << [name     : jobName,
                                 prev     : new DateTime(triggers.get(0).getPreviousFireTime()),
                                 next     : new DateTime(triggers.get(0).getNextFireTime()),
                                 executing: getExecutingActionFromJobName(jobName)]
                }
            }
        }

        def boxnetFacilities = Facility.list().findAll { it.hasBoxnet() }
        def fortnoxFacilities = Facility.list().findAll { it.hasFortnox() }
        def unfinishedTasks = scheduledTaskService.getUnfinishedTasks().sort { it.dateCreated }.reverse(true).collect {
            return [id: it.id, name: it.name + " (" + it.domainIdentifier + "), " + it.facility?.name + ", " + it.dateCreated]
        }

        [jobs: jobsInfo, boxnetFacilities: boxnetFacilities, fortnoxFacilities: fortnoxFacilities, unfinishedTasks: unfinishedTasks]
    }

    private String getExecutingActionFromJobName(String jobName) {
        String executingAction = null
        if (jobName == IdrottOnlineMembershipSyncJob.getCanonicalName())
            executingAction = TRIGGER_IDROTT_ONLINE_MEMBERSHIP_SYNC_JOB
        else if (jobName == IdrottOnlineActivitiesSyncJob.getCanonicalName())
            executingAction = TRIGGER_IDROTT_ONLINE_ACTIVITIES_SYNC_JOB
        else if (jobName == FortnoxInvoiceJob.getCanonicalName())
            executingAction = TRIGGER_FORTNOX_INVOICE_JOB
        else if (jobName == FacilityConfigSyncJob.getCanonicalName())
            executingAction = TRIGGER_FACILITY_CONFIG_SYNC_JOB

        executingAction
    }

    def triggerIdrottOnlineMembershipSyncJob() {
        IdrottOnlineMembershipSyncJob.triggerNow([:])
        flash.message = message(code: "default.status.success")
        redirect(action: "index")
    }

    def triggerIdrottOnlineActivitiesSyncJob() {
        IdrottOnlineActivitiesSyncJob.triggerNow()
        flash.message = message(code: "default.status.success")
        redirect(action: "index")
    }

    def triggerFortnoxInvoiceJob() {
        FortnoxInvoiceJob.triggerNow()
        flash.message = message(code: "default.status.success")
        redirect(action: "index")
    }

    def triggerFacilityConfigSyncJob() {
        FacilityConfigSyncJob.triggerNow()
        flash.message = message(code: "default.status.success")

        redirect(action: "index")
    }

    def runCashRegisterHistory() {
        def start = params.start ? new DateTime(new SimpleDateFormat("yyyy-MM-dd").parse(params.start)) : null
        def stop = params.stop ? new DateTime(new SimpleDateFormat("yyyy-MM-dd").parse(params.stop)) : null

        boxnetSyncService.syncTransactions(start, stop)

        flash.message = message(code: "adminJob.runCashRegisterHistory.message")
        redirect(action: "index")
    }

    def runRedeemSubscriptionCancellations() {
        log.debug("Running Subscription redeem job")
        def facilities = Facility.createCriteria().listDistinct {
            createAlias("subscriptionRedeem", "s", CriteriaSpecification.LEFT_JOIN)

            isNotNull("s.id")
            eq("s.redeemAt", RedeemAt.SLOTREBOOKED)
        }
        log.debug("Redeem for ${facilities.size()} facilities")

        if (facilities.size() > 0) {
            redeemService.redeemUnredeemedCancelations(facilities)
        }
        flash.message = message(code: "adminJob.runRedeemSubscriptionCancellations.message")
        redirect(action: "index")
    }

    def forceFortnoxCustomerSync() {
        def facility = Facility.get(params.long("facilityId"))
        def faciltiyCustomers = Customer.createCriteria().listDistinct {
            eq("facility", facility)
        }

        log.info("Syncing customers for ${facility.name} (${faciltiyCustomers.size()}st)")

        def watch = new StopWatch()
        watch.start()

        faciltiyCustomers.each { Customer c ->
            customerService.saveToFortnox(c, facility)
        }

        watch.stop()
        log.info "${faciltiyCustomers.size()} customers ($facility.name) have been synced with Fortnox in $watch"

        flash.message = message(code: 'adminJob.forceFortnoxCustomerSync.message',
            args: [faciltiyCustomers.size(), facility.name])
        redirect(action: "index")
    }

    def updateUploadedImages() {
        flash.message = message(code: "adminJob.updateUploadedImages.message")

        fileArchiveService.updateUploadedImages()
    }

    def resyncFortnoxInvoices() {
        log.info "Resyncing fortnox invoices..."

        def facility = Facility.get(params.long("facilityId"))
        fortnoxResyncService.startResyncInvoices([facility])

        flash.message = "Done syncing fortnox invoices for ${facility.name}"
        redirect(controller: "adminJob", action: "index")
    }

    def migratePaymentTransactions() {
        log.debug("Migrate payment transactions")

        def systemUser = User.findByEmail(grailsApplication.config.matchi.system.user.email)

        def payments = Payment.createCriteria().listDistinct {
            createAlias("paymentTransactions", "pt", CriteriaSpecification.LEFT_JOIN)
            createAlias("booking", "b", CriteriaSpecification.LEFT_JOIN)
            createAlias("b.order", "o", CriteriaSpecification.LEFT_JOIN)
            createAlias("o.payments", "op", CriteriaSpecification.LEFT_JOIN)

            eq("migrated", false)
            isNotNull("pt.id")

            order("dateCreated", "desc")

            maxResults(10000)
        }

        def size = payments.size()

        log.info("Migrating ${size} payments")
        def session = sessionFactory.getCurrentSession()

        def count = new AtomicInteger(0)

        // NOTE! This is a one time job executed for maintenance/migrations reason and therefore using hardcoded 15 as numberOfThreads.
        GParsPool.withPool(15) {
            payments.eachParallel { Payment p ->
                count.incrementAndGet()

                Order.withTransaction {
                    try {
                        tryMigratePayments(p, systemUser)
                    } catch (Exception e) {
                        log.error(e.toString())
                    }

                    if (count % 100 == 0) {
                        log.info "Migrating payments (${count}/${size})"
                        cleanUpGorm()
                    }
                }
            }
        }

        flash.message = "Done running migration of payment transactions on ${size} payments"

        // Clear outer session which tries to flush again, to avoid error
        session?.clear()
        redirect(controller: "adminJob", action: "index")
    }

    def migrateSubmissionsToRightCourse() {
        participantMigrationService.migrateSubmissionsToRightCourse()
        flash.message = "Done migrating wrong submissions!"
        redirect(controller: "adminJob", action: "index")
    }

    private void tryMigratePayments(Payment p, def systemUser) {
        Booking b = p.booking as Booking
        Order o = b?.order

        if (!o) {
            o = new Order()
            o.price = p.amountFormattedAmountOnly()
            o.vat = p.vat.toBigDecimal()
            o.dateCreated = p.dateCreated
            o.issuer = systemUser
            o.user = p?.customer?.user ?: systemUser
            o.customer = p.customer
            o.dateDelivery = p.dateDelivery ?: p.dateCreated
            o.facility = p.facility
            o.origin = Order.ORIGIN_FACILITY
            o.description = p.orderDescription
            o.article = Order.Article.BOOKING
        }

        if (b) {
            o.metadata = [slotId: b?.slotId]
            b.order = o
        }
        try {
            o?.save()
        }
        catch (Exception e) {
            o?.merge()
        }

        try {
            b?.save()
        }
        catch (Exception e) {
            b?.merge()
        }

        tryMigratePaymentTransactions(p, o, systemUser)

        p.migrated = true
        try {
            p?.save()
        }
        catch (Exception e) {
            p?.merge()
        }
    }

    private void tryMigratePaymentTransactions(Payment p, Order o, def systemUser) {

        if (p?.paymentTransactions?.size() != o?.payments?.size()) {

            switch (p.method) {
                case PaymentMethod.REGISTER:
                    p.paymentTransactions.each { pt ->
                        CashOrderPayment orderPayment = new CashOrderPayment()
                        orderPayment.issuer = o.issuer ?: systemUser
                        orderPayment.amount = pt?.paidAmount?.toBigDecimal() ?: o.total()
                        orderPayment.vat = o.vat()
                        orderPayment.referenceId = pt.cashRegisterTransactionId
                        orderPayment.status = OrderPayment.Status.CAPTURED

                        o.addToPayments(orderPayment.save(failOnError: true))
                        try {
                            o?.save()
                        }
                        catch (Exception e) {
                            o?.merge()
                        }

                        orderPayment.addToOrders(o)
                        orderPayment.save()

                        if (o.isFinalPaid()) {
                            o.status = Order.Status.COMPLETED
                            try {
                                o?.save()
                            }
                            catch (Exception e) {
                                o?.merge()
                            }
                        } else {
                            o.status = Order.Status.CONFIRMED
                            try {
                                o?.save()
                            }
                            catch (Exception e) {
                                o?.merge()
                            }
                        }
                    }
                    break
                case PaymentMethod.INVOICE:
                    p.paymentTransactions.each { pt ->
                        InvoiceOrderPayment orderPayment = new InvoiceOrderPayment()
                        orderPayment.invoiceRow = pt.invoiceRow
                        orderPayment.issuer = o.issuer
                        orderPayment.amount = pt.paidAmount
                        orderPayment.vat = o.vat
                        orderPayment.status = OrderPayment.Status.CAPTURED

                        o.addToPayments(orderPayment.save(failOnError: true))
                        try {
                            o?.save()
                        }
                        catch (Exception e) {
                            o?.merge()
                        }

                        orderPayment.addToOrders(o)
                        orderPayment.save()
                    }
                    break
                default:
                    break
            }
        }
    }

    /**
     * Stale
     */
    def updateSubscriptions() {
        def user = userService.getLoggedInUser()

        def subscriptionIds = Subscription.withCriteria {
            projections {
                property("id")
            }
            bookingGroup {
                bookings {
                    isNull("order")
                    slot {
                        gt("startTime", new Date())
                    }
                }
            }
        }.unique()

        def count = new AtomicInteger(0)

        // NOTE! This is a one time job executed for maintenance/migrations reason and therefore using hardcoded 15 as numberOfThreads.
        GParsPool.withPool(15) {
            subscriptionIds.eachParallel { id ->
                count.incrementAndGet()
                log.info "Updating subscription bookings (${count}/${subscriptionIds.size()}): $id"
                Subscription.withTransaction {
                    try {
                        subscriptionService.createBookingsOrders(Subscription.get(id), user)
                    } catch (e) {
                        log.error "Unable to create bookings orders for subscription ${id}. $e.message"
                    }
                }
            }
        }

        flash.message = message(code: "adminJob.updateSubscriptions.message")

        redirect(action: "index")
    }

    /**
     * Stale
     */
    def updateIncorrectRedeems() {
        log.debug("Updating incorrect redeems")

        def redeems = groovySql.rows("""
                select f.id as fid, ir.id as irid, o.price as pr
                from slot_redeem sr
                join invoice_row ir on sr.invoice_row_id = ir.id
                join slot s on sr.slot_id = s.id
                join subscription sbs on s.subscription_id = sbs.id
                join customer c on sbs.customer_id = c.id
                join facility f on c.facility_id = f.id
                join order_metadata om on om.metadata_idx = 'slotId' and om.metadata_elt = s.id
                join `order` o on om.metadata = o.id and o.customer_id = c.id and o.article = 'SUBSCRIPTION_BOOKING'
                where sr.amount is null and ir.invoice_id is null and -o.price != ir.price
        """)

        if (redeems) {
            redeems.groupBy {
                it.fid
            }.each { fid, vals ->
                def facility = Facility.get(fid)

                if (facility.subscriptionRedeem?.strategy?.instanceOf(InvoiceRowRedeemStrategy)) {
                    vals.each { redeem ->
                        redeemService.updateRedeemedPrice(redeem.irid, redeem.pr,
                            facility.subscriptionRedeem.strategy)
                    }
                }
            }
        }
        groovySql.close()

        flash.message = message(code: "adminJob.updateIncorrectRedeems.message")

        redirect(action: "index")
    }

    private void cleanUpGorm() {
        println "Clean up gorm"
        def session = sessionFactory.currentSession
        session.flush()
        session.clear()
        propertyInstanceMap.get().clear()
    }

    def renewMemberships() {
        MembershipRenewJob.triggerNow([endDate: params.endDate])
        flash.message = message(code: "default.status.success")
        redirect(action: "index")
    }

    def retryRecurringPayments() {
        RecurringPaymentRetryJob.triggerNow()
        flash.message = message(code: "default.status.success")
        redirect(action: "index")
    }

    def MarkTaskAsFinished() {
        ScheduledTask task = ScheduledTask.get(params.long("taskId"))

        if (task.isTaskFinished) {
            throw new IllegalArgumentException("Task is finished, can't change status")
        }

        log.info "Mark task as finished ${task.id}: ${task.name}"

        if (task.identifier == "CREATE_SEASON") {
            if (!seasonService.removeUnfinishedCreateSeasonTask(task)) {
                throw new IllegalStateException("Error deleting season")
            }
        }

        scheduledTaskService.markTaskAsFinished(task)

        redirect(controller: "adminJob", action: "index")
    }
}
