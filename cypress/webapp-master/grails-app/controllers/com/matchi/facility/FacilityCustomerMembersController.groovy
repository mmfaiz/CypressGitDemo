package com.matchi.facility

import com.matchi.*
import com.matchi.activities.Activity
import com.matchi.activities.trainingplanner.CourseActivity
import com.matchi.idrottonline.*
import com.matchi.idrottonline.commands.ActivityCommand
import com.matchi.idrottonline.commands.ParticipantCommand
import com.matchi.idrottonline.commands.PersonsCommand
import com.matchi.membership.Membership
import com.matchi.membership.MembershipType
import com.matchi.orders.Order
import grails.transaction.Transactional
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.joda.time.DateTimeConstants
import org.joda.time.LocalDate

import java.text.SimpleDateFormat

class FacilityCustomerMembersController extends GenericController {

    def cashService
    def customerService
    def memberService
    def membershipPaymentService
    def userService
    def dataSource
    GrailsApplication grailsApplication
    IdrottOnlineCommandBuilderService idrottOnlineCommandBuilderService
    IdrottOnlineService idrottOnlineService
    ActivityService activityService

    def messageSource

    public static final String MEMBERSHIP_TYPE_REQUIRED = "MEMBERSHIP_TYPE_REQUIRED"

    def index(FilterCustomerCommand filter) {
        if (!params.boolean("reset") && session[FacilityCustomerController.LIST_FILTER_KEY]?.isNotExpired()) {
            filter = session[FacilityCustomerController.LIST_FILTER_KEY]
            filter.allselected = false
            filter.members.remove(FilterCustomerCommand.ShowMembers.NO_MEMBERS)
        } else {
            session[FacilityCustomerController.LIST_FILTER_KEY] = filter
        }
        session[FacilityCustomerController.CUSTOMER_LIST_CONTROLLER_KEY] = "facilityCustomerMembers"

        log.debug("Fetching members")
        def facility = (Facility)getUserFacility()
        def facilityGroups = Group.findAllByFacility(facility, [sort: "name"]).asList()
        def birthyears = Customer.birthyears(facility, true,
                filter.membershipStartDate, filter.membershipEndDate).list()
        def seasons = Season.findAllByFacility(facility, [sort: "startTime", order: "desc"])
        def courses = CourseActivity.findAllByFacility(facility, [sort: "name"]).sort{it.isArchived()}
        def clubs = Customer.clubs(facility, true,
                filter.membershipStartDate, filter.membershipEndDate).list()
        def membershipTypes = facility.membershipTypes + facility.masterFacilities.collect {it.membershipTypes}.flatten()
        def localFacilities

        if (!filter.members) {
            filter.members = [FilterCustomerCommand.ShowMembers.MEMBERS_ONLY]
        }
        if (facility.isMasterFacility()) {
            localFacilities = facility.memberFacilities
        }


        def members = customerService.findCustomers(filter, facility)

        if(session[MEMBERSHIP_TYPE_REQUIRED]) {
            flash.error = message(code: "facilityCustomerMembers.invoice.errorMembershipTypeRequired")
            session.removeAttribute(MEMBERSHIP_TYPE_REQUIRED)
        }

        [facility: facility, members: members, filter: filter, facilityGroups: facilityGroups,
         types   : membershipTypes, birthyears: birthyears, seasons: seasons, courses: courses, clubs: clubs, localFacilities: localFacilities]
    }

    @Transactional
    def removeMembership(Long id, Boolean refund) {
        def membership = Membership.get(id)
        if (!membership) {
            render status: 404
            return
        }
        if (membership.customer?.facility?.hasLinkedFacilities()) {
            assertHierarchicalFacilityAccessTo(membership.customer)
        } else {
            assertFacilityAccessTo(membership.customer)
        }
        def customerId = membership.customer.id

        memberService.removeMembership(membership, refund, new LocalDate().minusDays(1))

        flash.message = message(code: "facilityCustomerMembers.remove.success")

        if(params.returnUrl && params.returnUrl.size() > 0) {
            redirect url: params.returnUrl
        } else {
            redirect(controller: "facilityCustomer", action: "show", id: customerId)
        }
    }

    def iOSyncStatusMembers() {
        Facility facility = getUserFacility()
        List<Customer> members = idrottOnlineService.getActiveOrTerminatedMembers(facility)
        IdrottOnlineSettings settings = new IdrottOnlineSettings(grailsApplication)
        IdrottOnlineMembershipCommand command = idrottOnlineCommandBuilderService.buildMembershipCommand(facility, settings, members)

        [validated: command.getValidPersons(), notValidated: command.getNotValidPersons(),
         facility: getUserFacility()]
    }

    def iOSyncStatusActivities() {
        Facility facility = getUserFacility()
        IdrottOnlineSettings settings = new IdrottOnlineSettings(grailsApplication)

        List<ActivityCommand> validActivities = []
        List<ActivityCommand> notValidActivities = []
        Map<Long, Activity> courseActivityLookupTable = [:]
        List<Long> isArchived = []
        if (facility?.hasIdrottOnlineActivitySync()) {
            List<ActivityOccasionOccurence> activityOccasions = findCourseOccasionByWeek(facility, params.date)
            IdrottOnlineActivitiesCommand activitiesCommand = idrottOnlineCommandBuilderService.buildActivitiesCommand(facility, settings, activityOccasions)
            validActivities = activitiesCommand.getValidActivities()
            notValidActivities = activitiesCommand.getNotValidActivities()

            activityOccasions.each { ActivityOccasionOccurence aoo ->
                courseActivityLookupTable.put(aoo.activityOccasion.id, aoo.activityOccasion.activity)
            }

            List<Long> customerIds = []

            activitiesCommand.notValidActivities.each { ActivityCommand activityCommand ->
                activityCommand.participants.each { ParticipantCommand participantCommand ->
                    participantCommand.personsCommand.each { PersonsCommand personsCommand ->
                        customerIds << personsCommand.person.customerId.toLong()
                    }
                }
            }

            if(customerIds) {
                List<Customer> customers = Customer.findAllByIdInList(customerIds.unique())
                customers.each { Customer customer ->
                    if(customer.archived) {
                        isArchived << customer.id
                    }
                }
            }
        }

        [validatedActivites: validActivities, notValidatedActivities: notValidActivities,
         facility: getUserFacility(), courseActivityLookupTable: courseActivityLookupTable, isArchived: isArchived]
    }

    def ioActivitiesSync() {
        Facility facility = getUserFacility()
        List<ActivityOccasionOccurence> activityOccasions = findCourseOccasionByWeek(facility, params.date)
        idrottOnlineService.importActivityOccasions(facility, activityOccasions, true)

        flash.message = message(code: "default.success")
        redirect(action: "iOSyncStatusActivities", params: [date: params.date])
    }

    List<ActivityOccasionOccurence> findCourseOccasionByWeek(Facility facility, String dateStr) {
        LocalDate date
        if (dateStr) {
            date = new LocalDate(new SimpleDateFormat("yyyy-MM-dd").parse(dateStr))
        } else {
            date = new LocalDate()
        }

        LocalDate monday = date.withDayOfWeek(DateTimeConstants.MONDAY)
        LocalDate sunday = date.withDayOfWeek(DateTimeConstants.SUNDAY)

        log.info("Getting activity occasions for ${facility.name} between ${monday} and ${sunday}")
        return activityService.findCourseOccasionByRange(facility, monday, sunday)
    }

    def createMembershipForm(Long customerId) {
        def customer = Customer.findById(customerId)

        if (!customer) {
            render status: 404
            return
        }

        def types = memberService.getFormMembershipTypes(customer.facility, customer)
        customer.facility.masterFacilities.each {
            types += memberService.getFormMembershipTypes(it, customer)
        }

        render(template: "createMembershipForm", model: [customer: customer,
                types: types])
    }

    @Transactional
    def saveMembership(Long customerId, MembershipCommand cmd) {
        def customer = Customer.findById(customerId)
        if (!customer) {
            render status: 404
            return
        }

        def user = customer.user
        def facility = cmd.type.facility
        if (customer.facility != facility) {
            customer = customerService.getOrCreateUserCustomer(user, facility)
        }

        if (cmd.hasErrors()) {
            flash.error = message(code: "facilityCustomerMembers.edit.failed")
            redirect(controller: "facilityCustomer", action: "show", id: customerId)
            return
        }

        def membership = memberService.addMembership(customer, cmd.type, new LocalDate(cmd.startDate),
                new LocalDate(cmd.endDate), new LocalDate(cmd.gracePeriodEndDate), null, null, true,
                Order.ORIGIN_FACILITY, null, cmd.startingGracePeriodDays)
        if (membership) {
            if (cmd.cancel) {
                memberService.disableAutoRenewal(membership)
            }
            if (membership.order.total() && cmd.paid) {
                cashService.createCashOrderPayment(membership.order)
            }
            flash.message = message(code: "facilityCustomerMembers.edit.saved")
        } else {
            flash.error = message(code: "facilityCustomerMembers.edit.overlaps")
        }

        redirect(controller: "facilityCustomer", action: "show", id: customerId)
    }

    def editMembershipForm(Long id) {
        def membership = Membership.get(id)
        if (!membership) {
            render status: 404
            return
        }
        if (membership.customer?.facility?.hasLinkedFacilities()) {
            assertHierarchicalFacilityAccessTo(membership.customer)
        } else {
            assertFacilityAccessTo(membership.customer)
        }

        render(template: "editMembershipForm", model: [membership: membership,
                types: memberService.getFormMembershipTypes(membership.customer.facility),
                customer: membership.customer])
    }

    @Transactional
    def updateMembership(Long id, MembershipCommand cmd) {
        def membership = Membership.get(id)
        if (!membership) {
            render status: 404
            return
        }
        if (membership.customer?.facility?.hasLinkedFacilities()) {
            assertHierarchicalFacilityAccessTo(membership.customer)
        } else {
            assertFacilityAccessTo(membership.customer)
        }
        if (cmd.hasErrors()) {
            flash.error = message(code: "facilityCustomerMembers.edit.failed")
            redirect(controller: "facilityCustomer", action: "show", id: membership.customer.id)
            return
        }

        if (memberService.isMembershipOverlapping(
                membership.customer, new LocalDate(cmd.startDate), new LocalDate(cmd.endDate), membership.id)) {
            flash.error = message(code: "facilityCustomerMembers.edit.overlaps")
            redirect(controller: "facilityCustomer", action: "show", id: membership.customer.id)
            return
        }

        if (memberService.updateMembership(membership, cmd)) {
            flash.message = message(code: "facilityCustomerMembers.edit.updated")
        } else {
            flash.error = message(code: "facilityCustomerMembers.edit.failed")
        }

        redirect(controller: "facilityCustomer", action: "show", id: membership.customer.id)
    }

    @Transactional
    def toggleMembershipActivation(Long id) {
        def membership = Membership.get(id)
        if (!membership) {
            render status: 404
            return
        }
        if (membership.customer?.facility?.hasLinkedFacilities()) {
            assertHierarchicalFacilityAccessTo(membership.customer)
        } else {
            assertFacilityAccessTo(membership.customer)
        }

        if (membership.activated) {
            memberService.deactivateMembership(membership)
            flash.message = message(code: "facilityCustomerMembers.membershipForm.deactivate.success")
        } else {
            memberService.activateMembership(membership)
            flash.message = message(code: "facilityCustomerMembers.membershipForm.activate.success")
        }

        redirect(controller: "facilityCustomer", action: "show", id: membership.customer.id)
    }
}

class MembershipCommand {

    MembershipType type
    Date startDate
    Date endDate
    Date gracePeriodEndDate
    Integer startingGracePeriodDays
    Boolean paid
    Boolean cancel = false

    static constraints = {
        endDate validator: { val, obj ->
            val >= obj.startDate
        }
        gracePeriodEndDate validator: { val, obj ->
            val >= obj.endDate
        }
        startingGracePeriodDays nullable: true, min: 1
        paid nullable: true
    }
}
