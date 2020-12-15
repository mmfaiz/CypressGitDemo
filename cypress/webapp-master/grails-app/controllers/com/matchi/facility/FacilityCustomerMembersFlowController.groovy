package com.matchi.facility

import com.matchi.Customer
import com.matchi.Facility
import com.matchi.GenericController
import com.matchi.User
import com.matchi.membership.Membership
import com.matchi.membership.MembershipType
import com.matchi.orders.Order
import groovyx.gpars.GParsPool
import org.codehaus.groovy.grails.validation.Validateable
import org.joda.time.LocalDate
import org.springframework.beans.factory.annotation.Value

import java.util.concurrent.atomic.AtomicInteger

class FacilityCustomerMembersFlowController extends GenericController {

    static scope = "prototype"

    def cashService
    def customerService
    def memberService

    @Value('${matchi.membership.update.batchSize}')
    Integer membershipUpdateBatchSize

    @Value('${matchi.membership.update.poolSize}')
    Integer membershipUpdatePoolSize

    def activateFlow = {
        entry {
            action { FilterCustomerCommand cmd ->
                def facility = getCurrentUser().facility
                def customerIds = params.allselected ?
                    customerService.findCustomers(cmd, facility, false, true) :
                    params.list("customerId").collect { Long.parseLong(it) }
                if (!customerIds) {
                    return error()
                }

                flow.membersInfo = []
                flow.membershipIds = []
                Customer.getAll(customerIds).each {
                    def m = it?.getMembershipByFilter(cmd)
                    if (m) {
                        flow.membershipIds << m.id
                        flow.membersInfo << [customerNr  : m.customer.number,
                                             customerName: m.customer.fullName(), membershipType: m.type?.name]
                    }
                }

                if (!flow.membershipIds) {
                    return error()
                }

                flow.persistenceContext.clear()
            }
            on("error").to "cancel"
            on(Exception).to "cancel"
            on("success").to "confirm"
        }
        confirm {
            on("cancel").to "cancel"
            on("submit").to "activateMembers"
        }
        activateMembers {
            action {
                flow.membershipIds.each {
                    memberService.activateMembership(Membership.get(it))
                }
            }
            on("error").to "confirm"
            on(Exception).to "cancel"
            on("success").to "confirmation"
        }

        confirmation {
            redirect(controller: "facilityCustomerMembers", action: "index")
        }

        cancel {
            redirect(controller: "facilityCustomerMembers", action: "index")
        }
    }

    def deactivateFlow = {
        entry {
            action { FilterCustomerCommand cmd ->
                def facility = getCurrentUser().facility
                def customerIds = params.allselected ?
                    customerService.findCustomers(cmd, facility, false, true) :
                    params.list("customerId").collect { Long.parseLong(it) }
                if (!customerIds) {
                    return error()
                }

                flow.membersInfo = []
                flow.membershipIds = []
                Customer.getAll(customerIds).each {
                    def m = it?.getMembershipByFilter(cmd)
                    if (m) {
                        flow.membershipIds << m.id
                        flow.membersInfo << [customerNr  : m.customer.number,
                                             customerName: m.customer.fullName(), membershipType: m.type?.name]
                    }
                }

                if (!flow.membershipIds) {
                    return error()
                }

                flow.persistenceContext.clear()
            }
            on("error").to "cancel"
            on(Exception).to "cancel"
            on("success").to "confirm"
        }
        confirm {
            on("cancel").to "cancel"
            on("submit").to "activateMembers"
        }
        activateMembers {
            action {
                flow.membershipIds.each {
                    memberService.deactivateMembership(Membership.get(it))
                }
            }
            on("error").to "confirm"
            on(Exception).to "cancel"
            on("success").to "confirmation"
        }

        confirmation {
            redirect(controller: "facilityCustomerMembers", action: "index")
        }

        cancel {
            redirect(controller: "facilityCustomerMembers", action: "index")
        }
    }

    def cancelFlow = {
        entry {
            action { FilterCustomerCommand cmd ->
                def facility = getCurrentUser().facility
                def customerIds = params.allselected ?
                    customerService.findCustomers(cmd, facility, false, true) :
                    params.list("customerId").collect { Long.parseLong(it) }
                if (!customerIds) {
                    return error()
                }

                flow.membersInfo = []
                flow.membershipIds = []
                Customer.getAll(customerIds).each {
                    def m = it?.getMembershipByFilter(cmd)
                    if (m) {
                        flow.membershipIds << m.id
                        flow.membersInfo << [customerNr    : m.customer.number, customerName: m.customer.fullName(),
                                             membershipType: m.type?.name, membershipEndDate: m.gracePeriodEndDate.toDate()]
                    }
                }

                if (!flow.membershipIds) {
                    return error()
                }

                flow.persistenceContext.clear()
            }
            on("error").to "cancel"
            on(Exception).to "cancel"
            on("success").to "confirm"
        }
        confirm {
            on("cancel").to "cancel"
            on("submit").to "cancelMembers"
        }
        cancelMembers {
            action {
                flow.membershipIds.each {
                    memberService.disableAutoRenewal(Membership.get(it))
                }
            }
            on("error").to "confirm"
            on(Exception).to "cancel"
            on("success").to "confirmation"
        }

        confirmation {
            redirect(controller: "facilityCustomerMembers", action: "index")
        }

        cancel {
            redirect(controller: "facilityCustomerMembers", action: "index")
        }
    }

    def removeFlow = {
        entry {
            action { FilterCustomerCommand cmd ->
                def facility = getCurrentUser().facility
                def customerIds = params.allselected ?
                    customerService.findCustomers(cmd, facility, false, true) :
                    params.list("customerId").collect { Long.parseLong(it) }
                if (!customerIds) {
                    return error()
                }

                flow.membersInfo = []
                flow.membershipIds = []
                Customer.getAll(customerIds).each {
                    def m = it?.getMembershipByFilter(cmd)
                    if (m) {
                        flow.membershipIds << m.id
                        flow.membersInfo << [customerNr    : m.customer.number, customerName: m.customer.fullName(),
                                             membershipType: m.type?.name, membershipEndDate: m.gracePeriodEndDate.toDate()]
                    }
                }

                if (!flow.membershipIds) {
                    return error()
                }

                flow.persistenceContext.clear()
            }
            on("error").to "cancel"
            on(Exception).to "cancel"
            on("success").to "confirm"
        }
        confirm {
            on("cancel").to "cancel"
            on("submit").to "removeMembers"
        }
        removeMembers {
            action {
                flow.membershipIds.each {
                    memberService.removeMembership(Membership.get(it))
                }
            }
            on("error").to "confirm"
            on(Exception).to "cancel"
            on("success").to "confirmation"
        }

        confirmation {
            redirect(controller: "facilityCustomerMembers", action: "index", params: [message:
                                                                                          g.message(code: "facilityCustomerMembers.remove.success")])
        }

        cancel {
            redirect(controller: "facilityCustomerMembers", action: "index")
        }
    }

    def addMembershipFlow = {
        entry {
            action { FilterCustomerCommand cmd ->
                def facility = getCurrentUser().facility
                def customerIds = params.allselected ?
                    customerService.findCustomers(cmd, facility, false, true) :
                    params.list("customerId").collect { Long.parseLong(it) }
                if (!customerIds) {
                    return error()
                }

                flow.customerIds = customerIds
                flow.membershipTypes = memberService.getFormMembershipTypes(facility)
                facility.masterFacilities.each {
                    flow.membershipTypes += memberService.getFormMembershipTypes(it)
                }

                flow.persistenceContext.clear()
            }
            on("error").to "cancel"
            on(Exception).to "cancel"
            on("success").to "enterDetails"
        }
        enterDetails {
            on("next") { AddMembershipCommand cmd ->
                cmd.startDate = new LocalDate(params.date('startDate'))
                cmd.endDate = new LocalDate(params.date('endDate'))
                cmd.gracePeriodEndDate = new LocalDate(params.date('gracePeriodEndDate'))

                flow.cmd = cmd
                if (!flow.cmd.validate()) {
                    return error()
                }
                flow.persistenceContext.clear()
            }.to("excludeMembers")
            on("cancel").to("cancel")
        }
        excludeMembers {
            action {
                def membersCount = new AtomicInteger(0)
                def customersInfo = [].asSynchronized()

                GParsPool.withPool(membershipUpdatePoolSize) {
                    Facility facility = MembershipType.get(flow.cmd.typeId)?.facility
                    flow.customerIds.collate(membershipUpdateBatchSize).eachParallel { ids ->
                        Customer.withNewSession {
                            ids.each { id ->
                                def customer = Customer.get(id)
                                if (memberService.isMembershipOverlapping(Customer.findByUserAndFacility(customer.user, facility),
                                    flow.cmd.startDate, flow.cmd.endDate)) {
                                    membersCount.incrementAndGet()
                                } else {
                                    customersInfo << [id  : customer.id, nr: customer.number,
                                                      name: customer.fullName(), email: customer.email]
                                }
                            }
                        }
                    }
                }

                [membersCount: membersCount, customersInfo: customersInfo.sort { it.nr }]
            }
            on("error").to "cancel"
            on(Exception).to "cancel"
            on("success").to "confirm"
        }
        confirm {
            on("back").to("enterDetails")
            on("cancel").to "cancel"
            on("submit").to "saveMembership"
        }
        saveMembership {
            action {
                def issuerId = getCurrentUser().id
                GParsPool.withPool(membershipUpdatePoolSize) {
                    flow.customersInfo.collate(membershipUpdateBatchSize).eachParallel { infoList ->
                        Customer.withNewSession {
                            def issuer = User.get(issuerId)
                            def membershipType = MembershipType.get(flow.cmd.typeId)

                            infoList.each { info ->
                                Customer customer
                                if (Customer.get(info.id).user) {
                                    customer = customerService.getOrCreateUserCustomer(Customer.get(info.id).user, membershipType.facility)
                                } else {
                                    customer = Customer.get(info.id)
                                }
                                def membership = memberService.addMembership(customer,
                                    membershipType, flow.cmd.startDate, flow.cmd.endDate,
                                    flow.cmd.gracePeriodEndDate, null, issuer, true,
                                    Order.ORIGIN_FACILITY, null, flow.cmd.startingGracePeriodDays)
                                if (membership) {
                                    if (membership.order.total() && flow.cmd.paid) {
                                        cashService.createCashOrderPayment(membership.order)
                                    }
                                } else {
                                    log.error "Unable to add membership to customer (ID: $info.id)"
                                }
                            }
                        }
                    }
                }
            }
            on("error").to "confirm"
            on(Exception).to "cancel"
            on("success").to "finish"
        }
        cancel {
            redirect(controller: "facilityCustomer", action: "index")
        }
        finish {
            redirect(controller: "facilityCustomer", action: "index", params: [message:
                                                                                   g.message(code: "facilityCustomerMembers.addMembership.success")])
        }
    }

    def editMembershipsFlow = {
        entry {
            action { FilterCustomerCommand cmd ->
                def facility = getCurrentUser().facility
                def customerIds = params.allselected ?
                    customerService.findCustomers(cmd, facility, false, true) :
                    params.list("customerId").collect { Long.parseLong(it) }
                if (!customerIds) {
                    return error()
                }

                flow.membersInfo = []
                Customer.getAll(customerIds).each {
                    def m = it?.getMembershipByFilter(cmd)
                    if (m) {
                        flow.membersInfo << [customerNr                          : m.customer.number, customerName: m.customer.fullName(),
                                             customerId                          : m.customer.id, membershipId: m.id,
                                             membershipType                      : m.type?.name, membershipEndDate: m.endDate,
                                             membershipGracePeriodEndDate        : m.gracePeriodEndDate,
                                             membershipStartDate                 : m.startDate, membershipCancel: m.cancel,
                                             membershipStartingGracePeriodAllowed: !m.isPaid() && !m.customer.hasNonEndedMembership(m.id),
                                             membershipStartingGracePeriod       : m.startingGracePeriodDays,
                                             membershipTypeId                    : m.type?.id]
                    }
                }

                flow.membershipTypes = memberService.getFormMembershipTypes(facility)

                flow.persistenceContext.clear()
            }
            on("error").to "cancel"
            on(Exception).to "cancel"
            on("success").to "enterDetails"
        }
        enterDetails {
            on("next") { EditMembershipsCommand cmd ->
                cmd.startDate = new LocalDate(params.date('startDate'))
                cmd.endDate = new LocalDate(params.date('endDate'))
                cmd.gracePeriodEndDate = new LocalDate(params.date('gracePeriodEndDate'))

                flow.cmd = cmd
                if (!flow.cmd.validate()) {
                    return error()
                }

                flow.membersInfo.each { info ->
                    info.errors = []
                    info.newStartingGracePeriod = null
                    info.newStartDate = null
                    info.newEndDate = null
                    info.newGracePeriodEndDate = null

                    if (cmd.updateStartingGracePeriodDays && info.membershipStartingGracePeriodAllowed) {
                        info.newStartingGracePeriod = cmd.startingGracePeriodDays
                    }
                    if (cmd.updateStartDate) {
                        if (cmd.startDateType == EditMembershipsCommand.PeriodType.DAYS) {
                            info.newStartDate = info.membershipStartDate.plusDays(cmd.startDays)
                        } else if (info.membershipStartDate != cmd.startDate) {
                            info.newStartDate = cmd.startDate
                        }
                    }
                    if (cmd.updateEndDate) {
                        if (cmd.endDateType == EditMembershipsCommand.PeriodType.DAYS) {
                            info.newEndDate = info.membershipEndDate.plusDays(cmd.endDays)
                        } else if (info.membershipEndDate != cmd.endDate) {
                            info.newEndDate = cmd.endDate
                        }
                    }
                    if (cmd.updateGracePeriodEndDate) {
                        if (cmd.gracePeriodEndDateType == EditMembershipsCommand.PeriodType.DAYS) {
                            info.newGracePeriodEndDate = info.membershipGracePeriodEndDate.plusDays(cmd.gracePeriodEndDays)
                        } else if (info.membershipGracePeriodEndDate != cmd.gracePeriodEndDate) {
                            info.newGracePeriodEndDate = cmd.gracePeriodEndDate
                        }
                    }
                    if (cmd.updateMembershipType && info.membershipTypeId != cmd.membershipTypeId) {
                        info.newMembershipTypeId = cmd.membershipTypeId
                    }

                    if ((info.newStartDate && info.newEndDate && info.newStartDate > info.newEndDate)
                        || (info.newStartDate && !info.newEndDate && info.newStartDate > info.membershipEndDate)
                        || (!info.newStartDate && info.newEndDate && info.membershipStartDate > info.newEndDate)) {
                        info.errors << message(code: "editMembershipsCommand.endDate.lessThanStartDate")
                    }
                    if ((info.newEndDate && info.newGracePeriodEndDate && info.newEndDate > info.newGracePeriodEndDate)
                        || (info.newEndDate && !info.newGracePeriodEndDate && info.newEndDate > info.membershipGracePeriodEndDate)
                        || (!info.newEndDate && info.newGracePeriodEndDate && info.membershipEndDate > info.newGracePeriodEndDate)) {
                        info.errors << message(code: "editMembershipsCommand.gracePeriodEndDate.lessThanEndDate")
                    }
                    if (memberService.isMembershipOverlapping(Customer.get(info.customerId),
                        info.newStartDate ?: info.membershipStartDate,
                        info.newEndDate ?: info.membershipEndDate, info.membershipId)) {
                        info.errors << message(code: "facilityCustomerMembers.edit.overlaps")
                    }
                    if (info.newGracePeriodEndDate && info.membershipCancel) {
                        info.errors << message(code: "editMembershipsCommand.gracePeriodEndDate.cancel")
                    }
                }

                flow.persistenceContext.clear()
            }.to("confirm")
            on("cancel").to("cancel")
        }
        confirm {
            on("back").to("enterDetails")
            on("cancel").to "cancel"
            on("submit").to "update"
        }
        update {
            action {
                def issuerId = getCurrentUser().id
                GParsPool.withPool(membershipUpdatePoolSize) {
                    flow.membersInfo.collate(membershipUpdateBatchSize).eachParallel { infoList ->
                        Membership.withNewSession {
                            def issuer = User.get(issuerId)
                            infoList.each { info ->
                                if (!info.errors && (info.newStartDate || info.newEndDate
                                    || info.newGracePeriodEndDate || info.newStartingGracePeriod
                                    || info.newMembershipTypeId)) {
                                    def m = Membership.get(info.membershipId)
                                    if (!memberService.updateMembershipFields(m, issuer,
                                        [startDate              : info.newStartDate, endDate: info.newEndDate,
                                         gracePeriodEndDate     : info.newGracePeriodEndDate,
                                         startingGracePeriodDays: info.newStartingGracePeriod,
                                         typeId                 : info.newMembershipTypeId])) {
                                        log.error "Unable to update membership (ID: $m.id) fields: $m.errors"
                                    }
                                }
                            }
                        }
                    }
                }
            }
            on("error").to "confirm"
            on(Exception).to "cancel"
            on("success").to "finish"
        }
        cancel {
            redirect(controller: "facilityCustomerMembers", action: "index")
        }
        finish {
            redirect(controller: "facilityCustomerMembers", action: "index", params: [message:
                                                                                          g.message(code: "facilityCustomerMembers.editMemberships.success")])
        }
    }

    def setPaidFlow = {
        entry {
            action { FilterCustomerCommand cmd ->
                def facility = getCurrentUser().facility
                def customerIds = params.allselected ?
                    customerService.findCustomers(cmd, facility, false, true) :
                    params.list("customerId").collect { Long.parseLong(it) }
                if (!customerIds) {
                    return error()
                }

                flow.membersInfo = []
                flow.membershipIds = []
                flow.pendingMemberships = 0
                flow.freeMemberships = 0
                flow.paidMemberships = 0
                flow.invoicedMemberships = 0
                Customer.getAll(customerIds).each {
                    def m = it?.getMembershipByFilter(cmd)
                    if (m) {
                        if (!m.activated) {
                            flow.pendingMemberships++
                        } else if (m.order.isFree()) {
                            flow.freeMemberships++
                        } else if (m.isPaid()) {
                            flow.paidMemberships++
                        } else if (m.order.isInvoiced()) {
                            flow.invoicedMemberships++
                        } else {
                            flow.membershipIds << m.id
                            flow.membersInfo << [customerNr  : m.customer.number,
                                                 customerName: m.customer.fullName(), membershipType: m.type?.name]
                        }
                    }
                }

                flow.persistenceContext.clear()
            }
            on("error").to "cancel"
            on(Exception).to "cancel"
            on("success").to "confirm"
        }
        confirm {
            on("cancel").to "cancel"
            on("submit").to "pay"
        }
        pay {
            action {
                flow.membershipIds.each {
                    cashService.createCashOrderPayment(Membership.get(it).order)
                }
            }
            on("error").to "confirm"
            on(Exception).to "cancel"
            on("success").to "confirmation"
        }

        confirmation {
            redirect(controller: "facilityCustomerMembers", action: "index")
        }

        cancel {
            redirect(controller: "facilityCustomerMembers", action: "index")
        }
    }

    def setUnpaidFlow = {
        entry {
            action { FilterCustomerCommand cmd ->
                def facility = getCurrentUser().facility
                def customerIds = params.allselected ?
                    customerService.findCustomers(cmd, facility, false, true) :
                    params.list("customerId").collect { Long.parseLong(it) }
                if (!customerIds) {
                    return error()
                }

                flow.membersInfo = []
                flow.membershipIds = []
                flow.pendingMemberships = 0
                flow.freeMemberships = 0
                flow.unpaidMemberships = 0
                flow.nonCashPaidMemberships = 0
                Customer.getAll(customerIds).each {
                    def m = it?.getMembershipByFilter(cmd)
                    if (m) {
                        if (!m.activated) {
                            flow.pendingMemberships++
                        } else if (m.order.isFree()) {
                            flow.freeMemberships++
                        } else if (!m.isPaid()) {
                            flow.unpaidMemberships++
                        } else if (!m.order.isPaidByCash()) {
                            flow.nonCashPaidMemberships++
                        } else {
                            flow.membershipIds << m.id
                            flow.membersInfo << [customerNr  : m.customer.number,
                                                 customerName: m.customer.fullName(), membershipType: m.type?.name]
                        }
                    }
                }

                flow.persistenceContext.clear()
            }
            on("error").to "cancel"
            on(Exception).to "cancel"
            on("success").to "confirm"
        }
        confirm {
            on("cancel").to "cancel"
            on("submit").to "unpay"
        }
        unpay {
            action {
                flow.membershipIds.each {
                    memberService.refundCashPayments(Membership.get(it))
                }
            }
            on("error").to "confirm"
            on(Exception).to "cancel"
            on("success").to "confirmation"
        }

        confirmation {
            redirect(controller: "facilityCustomerMembers", action: "index")
        }

        cancel {
            redirect(controller: "facilityCustomerMembers", action: "index")
        }
    }
}

@Validateable
class AddMembershipCommand implements Serializable {

    Long typeId
    LocalDate startDate
    LocalDate endDate
    LocalDate gracePeriodEndDate
    Integer startingGracePeriodDays
    Boolean paid

    static constraints = {
        typeId nullable: false
        startDate nullable: false
        endDate nullable: false, validator: { val, obj ->
            val >= obj.startDate
        }
        gracePeriodEndDate nullable: false, validator: { val, obj ->
            val >= obj.endDate
        }
        startingGracePeriodDays nullable: true, min: 1
        paid nullable: true
    }
}

class EditMembershipsCommand implements Serializable {

    Boolean updateStartingGracePeriodDays
    Integer startingGracePeriodDays

    Boolean updateStartDate
    PeriodType startDateType
    LocalDate startDate
    Integer startDays

    Boolean updateEndDate
    PeriodType endDateType
    LocalDate endDate
    Integer endDays

    Boolean updateGracePeriodEndDate
    PeriodType gracePeriodEndDateType
    LocalDate gracePeriodEndDate
    Integer gracePeriodEndDays

    Boolean updateMembershipType
    Long membershipTypeId

    static constraints = {
        updateStartingGracePeriodDays nullable: true, validator: { val, obj ->
            val || obj.updateStartDate || obj.updateEndDate || obj.updateGracePeriodEndDate ||
                obj.updateMembershipType
        }
        startingGracePeriodDays nullable: true, validator: { val, obj ->
            val != null || !obj.updateStartingGracePeriodDays
        }

        updateStartDate nullable: true
        startDateType nullable: true, validator: { val, obj ->
            val || !obj.updateStartDate
        }
        startDate nullable: true, validator: { val, obj ->
            val || !obj.updateStartDate || obj.startDateType != PeriodType.DATE
        }
        startDays nullable: true, min: 1, validator: { val, obj ->
            val || !obj.updateStartDate || obj.startDateType != PeriodType.DAYS
        }

        updateEndDate nullable: true
        endDateType nullable: true, validator: { val, obj ->
            val || !obj.updateEndDate
        }
        endDate nullable: true, validator: { val, obj ->
            if (val && obj.updateStartDate && obj.startDateType == PeriodType.DATE && val < obj.startDate) {
                return "lessThanStartDate"
            } else if (!val && obj.updateEndDate && obj.endDateType == PeriodType.DATE) {
                return "nullable"
            } else {
                return true
            }
        }
        endDays nullable: true, min: 1, validator: { val, obj ->
            val || !obj.updateEndDate || obj.endDateType != PeriodType.DAYS
        }

        updateGracePeriodEndDate nullable: true
        gracePeriodEndDateType nullable: true, validator: { val, obj ->
            val || !obj.updateGracePeriodEndDate
        }
        gracePeriodEndDate nullable: true, validator: { val, obj ->
            if (val && val < LocalDate.now()) {
                return "min"
            } else if (val && obj.updateEndDate && obj.endDateType == PeriodType.DATE && val < obj.endDate) {
                return "lessThanEndDate"
            } else if (!val && obj.updateGracePeriodEndDate && obj.gracePeriodEndDateType == PeriodType.DATE) {
                return "nullable"
            } else {
                return true
            }
        }
        gracePeriodEndDays nullable: true, min: 1, validator: { val, obj ->
            val || !obj.updateGracePeriodEndDate || obj.gracePeriodEndDateType != PeriodType.DAYS
        }
        updateMembershipType nullable: true
        membershipTypeId nullable: true, validator: { val, obj ->
            val || !obj.updateMembershipType
        }
    }

    enum PeriodType {
        DATE, DAYS
    }
}
