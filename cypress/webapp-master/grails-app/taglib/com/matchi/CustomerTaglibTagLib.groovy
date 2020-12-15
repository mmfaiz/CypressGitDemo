package com.matchi

import com.matchi.activities.Activity
import com.matchi.coupon.CustomerCoupon
import com.matchi.dynamicforms.FormField
import com.matchi.dynamicforms.Submission
import com.matchi.invoice.Invoice
import com.matchi.invoice.InvoiceRow
import com.matchi.activities.EventActivity
import com.matchi.activities.Participant
import com.matchi.activities.trainingplanner.CourseActivity
import com.matchi.membership.Membership
import org.joda.time.LocalDate

class CustomerTaglibTagLib {

    def activityService
    def customerService
    def memberService
    def bookingService
    def subscriptionService

    def customerMembership = { attrs, body ->
        def customer = (Customer)attrs.customer
        def today = new LocalDate()
        def currentMemberships = customer.memberships.findAll {
            it.startDate <= today && it.gracePeriodEndDate >= today
        }.sort {it.startDate}
        def endedMemberships = customer.memberships.findAll {it.gracePeriodEndDate < today}.sort {it.startDate}
        def upcomingMemberships = customer.memberships.findAll {it.isUpcoming()}.sort {it.startDate}

        out << render(template:"/templates/customer/customerMembership",
                model: [customer: customer, currentMemberships: currentMemberships,
                        endedMemberships: endedMemberships, upcomingMemberships: upcomingMemberships])
    }

    def customerMembershipInGroup = { attrs, body ->
        def customers = customerService.findHierarchicalUserCustomers((Customer)attrs.customer, false)

        Collection<Membership> currentMemberships = []
        Collection<Membership> endedMemberships = []
        Collection<Membership> upcomingMemberships = []

        customers.each { Customer customer ->
            def today = new LocalDate()
            currentMemberships += customer.memberships.findAll {
                it.startDate <= today && it.gracePeriodEndDate >= today
            }.sort { it.startDate }
            endedMemberships += customer.memberships.findAll { it.gracePeriodEndDate < today }.sort { it.startDate }
            upcomingMemberships += customer.memberships.findAll { it.isUpcoming() }.sort { it.startDate }
        }

            out << render(template:"/templates/customer/customerHierarchicalMembership",
                    model: [currentMemberships: currentMemberships,
                            endedMemberships: endedMemberships, upcomingMemberships: upcomingMemberships])

    }

    def customerMembershipFamily = { attrs, body ->
        def customer = (Customer)attrs.customer
        def family = customer.membership?.family

        out << render(template:"/templates/customer/customerMembershipFamily", model: [customer: customer, family: family])
    }

    def customerGroup = { attrs, body ->
        out << render(template:"/templates/customer/customerGroup", model: [customer: attrs.customer])
    }

    def customerCourseSubmission = { attrs, body ->
        Facility facility = attrs.customer.facility
        List<CourseActivity> assignableCourses
        List<Submission> submissions
        if (facility.getFacilityProperty(FacilityProperty.FacilityPropertyKey.FEATURE_TRAINING_PLANNER)) {
            submissions = Submission.findAllByCustomerAndStatus(attrs.customer, Submission.Status.WAITING)
            List<Activity> customerActivities = submissions*.form*.activity
            assignableCourses = activityService.getCurrentAndUpcomingActivities(
                    CourseActivity, facility, FormField.Type.PERSONAL_INFORMATION) - customerActivities
        }

        out << render(template:"/templates/customer/customerCourseSubmission",
                model: [customer: attrs.customer, submissions: submissions,
                        assignableCourses: assignableCourses])
    }

    def customerCourse = { attrs, body ->
        def facility = attrs.customer.facility
        def assignableCourses
        if (facility.getFacilityProperty(FacilityProperty.FacilityPropertyKey.FEATURE_TRAINING_PLANNER)) {
            def customerActivities = Participant.findAllByCustomer(
                    attrs.customer, [fetch: [activity: "join"]])*.activity
            assignableCourses = activityService.getCurrentAndUpcomingActivities(
                    CourseActivity, facility, FormField.Type.PERSONAL_INFORMATION) - customerActivities
        }

        def courseParticipants = Participant.withCriteria {
            createAlias("activity", "a")
            eq("a.class", CourseActivity.DISCRIMINATOR)
            eq("customer", attrs.customer)
            order("a.name", "asc")
        }

        out << render(template:"/templates/customer/customerCourse",
                model: [customer: attrs.customer, courseParticipants: courseParticipants,
                        assignableCourses: assignableCourses])
    }

    def customerEventActivity = { attrs, body ->
        Facility facility = attrs.customer.facility
        List<EventActivity> assignableEventActivities
        List<Submission> submissions

        submissions = Submission.withCriteria {

            eq("customer", attrs.customer)
            form {
                event {

                }
            }
        }
        assignableEventActivities = activityService.getCurrentAndUpcomingActivities(
                EventActivity, facility, FormField.Type.PERSONAL_INFORMATION) - submissions.form.event

        out << render(template:"/templates/customer/customerEventActivity",
                model: [customer: attrs.customer, submissions: submissions,
                        assignableActivities: assignableEventActivities])
    }

    def customerBooking = { attrs, body ->
        def customer = (Customer)attrs.customer
        def bookings = bookingService.getCustomerBookings(customer, true)

        out << render(template:"/templates/customer/customerBooking", model: [bookings: bookings, customer: customer])
    }

    def customerSubscription = { attrs, body ->
        Customer customer = (Customer)attrs.customer
        Season currentSeason = customer?.facility?.currentSeason
        Date date = currentSeason ? currentSeason.startTime : new Date()
        def subscriptions = subscriptionService.getCustomerSubscriptions(customer, date)

        out << render(template:"/templates/customer/customerSubscription", model: [subscriptions: subscriptions, customer: customer])
    }

    def customerInvoice = { attrs, body ->
        def customer = (Customer)attrs.customer
        def invoices = Invoice.findAllByCustomer(customer, [sort:"dateCreated", order:"desc"])
        def totalSum = invoices.sum { it.getTotalIncludingVAT() }
        def invoiceRows = getInvoiceRowsByCustomer(attrs)

        out << render(template:"/templates/customer/customerInvoice", model: [ invoices: invoices, totalSum: totalSum, customer: customer, invoiceRows: invoiceRows])
    }

    def customerInvoiceAddIcon = {attrs, body->
        if(attrs.rowsIds){
            out << g.link(action:"createInvoice", class: "pull-right", controller:"facilityInvoiceRowFlow",
                    params: [returnUrl: attrs.returnUrl,invoiceRowIds: attrs.rowsIds],'<i class="icon-plus"></i>')
        }
    }

    def customerInvoiceRow = { attrs, body ->
        def rows = getInvoiceRowsByCustomer(attrs)
        def totalSum = rows.sum { it.getTotalIncludingVAT() }
        out << render(template:"/templates/customer/customerInvoiceRow", model: [ rows: rows, totalSum: totalSum, customer: attrs.customer ])
    }
    def customerCoupon = { attrs, body ->
        def customer = attrs.customer
        def coupons = CustomerCoupon.findAllByCustomer(customer)

        def active    = coupons.findAll { !it.dateLocked && it.isValid() }
        def locked    = coupons.findAll { it.dateLocked }
        def archive   = coupons - locked - active

        out << render(template:"/templates/customer/customerCoupon", model: [ customer:customer, active:active, locked:locked, archive:archive ])
    }

    def customerCouponInGroup = { attrs, body ->

        def customers = customerService.findHierarchicalUserCustomers((Customer)attrs.customer, false)
        def coupons = []
        def active = []
        def locked = []
        def archive = []

        customers.each { Customer customer ->
            coupons += CustomerCoupon.findAllByCustomer(customer)
            active = coupons.findAll { !it.dateLocked && it.isValid() }
            locked = coupons.findAll { it.dateLocked }
            archive = coupons - locked - active
        }

        out << render(template:"/templates/customer/customerHierarchicalCoupon", model: [active:active, locked:locked, archive:archive ])
    }

    def customerCashRegisterTransactions = { attrs, body ->
        def customer = attrs.customer
        def transactions = CashRegisterTransaction.findAllByCustomer(customer)

        out << render(template:"/templates/customer/customerCashRegisterTransaction", model: [ customer:customer, transactions:transactions ])
    }

    def customerInvoiceAddress = { attrs, body ->
        def customer = attrs.customer

        def namePrefix = null
        if (Boolean.valueOf(attrs.checkAge)) {
            def age = customer.age
            if (age && age < 18) {
                namePrefix = message(code: "invoice.guardianFor")
            }
        }

        out << render(template:"/templates/customer/customerInvoiceAddress",
                model: [ address: customer.getInvoiceAddress(namePrefix) ])
    }

    def membershipStatus = { attrs, body ->
        Membership membership = attrs.membership
        if (membership) {
            out << render(template: "/templates/customer/membershipStatus",
                    model: [membership: membership])
        }
    }

    def membershipPaymentHistory = { attrs, body ->
        Membership membership = attrs.membership
        if (membership) {
            out << render(template: "/templates/customer/membershipPaymentHistory",
                    model: [membership: membership])
        }
    }

    def memberBadge = { attrs, body ->
        def customer = attrs.customer
        if (customer?.hasActiveMembership() || customer?.membership?.inStartingGracePeriod) {
            out << render(template: "/templates/customer/memberBadge")
        }
    }

    def membershipEndDate = { attrs, body ->
        def membership = attrs.membership
        if (membership) {
            out << render(template: "/templates/customer/membershipEndDate",
                    model: [membership: membership])
        }
    }

    def customerUserSportProfilesLevel = { attrs, body ->
        def user = attrs.user
        if (user) {
            out << render(template: "/templates/customer/userSportProfilesLevel",
                    model: [user: user])
        }
    }

    private List<InvoiceRow> getInvoiceRowsByCustomer(attrs) {
        InvoiceRow.withCriteria {
            eq("customer", attrs.customer)
            isNull("invoice")

            order("dateCreated", "desc")
        }
    }
}
