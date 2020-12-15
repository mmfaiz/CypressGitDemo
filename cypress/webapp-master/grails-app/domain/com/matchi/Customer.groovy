package com.matchi

import com.matchi.activities.Participant
import com.matchi.coupon.CustomerCoupon
import com.matchi.dynamicforms.Submission
import com.matchi.facility.FilterCustomerCommand
import com.matchi.invoice.Invoice
import com.matchi.membership.Membership
import com.matchi.membership.MembershipFamily
import com.matchi.membership.MembershipType
import com.matchi.price.PriceListCustomerCategory
import grails.plugin.asyncmail.Validator
import org.apache.commons.lang.StringUtils
import org.joda.time.LocalDate
import org.joda.time.Years

class Customer implements Comparable, Serializable {

    private static final long serialVersionUID = 12L

    static enum CustomerType {
        MALE, FEMALE, ORGANIZATION, NULL

        static list() {
            return [MALE, FEMALE, ORGANIZATION]
        }

        static listGender() {
            [MALE, FEMALE]
        }
    }

    static belongsTo = [facility: Facility]
    static hasMany = [bookings            : Booking, payments: Payment, customerCoupons: CustomerCoupon,
                      customerGroups      : CustomerGroup, subscriptions: Subscription, inviteTickets: CustomerInviteTicket,
                      updateRequestTickets: CustomerUpdateRequestTicket, disableMessagesTickets: CustomerDisableMessagesTicket,
                      formSumbissions     : Submission, courseParticipants: Participant, invoices: Invoice, memberships: Membership]
    static transients = ['lastActivity']

    Long number
    String email
    String firstname
    String lastname
    String companyname
    String contact
    String address1
    String address2
    String zipcode
    String city
    String country
    String nationality
    String telephone
    String cellphone
    String notes
    String web

    String securityNumber
    String orgNumber
    String vatNumber

    String invoiceAddress1
    String invoiceAddress2
    String invoiceCity
    String invoiceZipcode
    String invoiceContact
    String invoiceTelephone
    String invoiceEmail

    String guardianName
    String guardianEmail
    String guardianTelephone

    String guardianName2
    String guardianEmail2
    String guardianTelephone2

    User user
    CustomerType type

    Date dateCreated
    Date lastUpdated

    Boolean archived = false

    Boolean clubMessagesDisabled
    Boolean exludeFromNumberOfBookingsRule

    Integer birthyear

    Date dateOfBirth

    String club
    String accessCode

    Date deleted
    def transient lastActivity

    static constraints = {
        number(nullable: true, blank: false, min: 1l)
        email(nullable: true, email: true)
        firstname(nullable: true)
        lastname(nullable: true)
        companyname(nullable: true)
        contact(nullable: true)
        address1(nullable: true)
        address2(nullable: true)
        zipcode(nullable: true)
        city(nullable: true)
        country(nullable: true)
        nationality(nullable: true)
        telephone(nullable: true)
        cellphone(nullable: true)
        notes(nullable: true, maxSize: 2000)
        securityNumber(nullable: true)
        orgNumber(nullable: true)
        invoiceAddress1(nullable: true)
        invoiceAddress2(nullable: true)
        invoiceZipcode(nullable: true)
        invoiceCity(nullable: true)
        invoiceContact(nullable: true)
        invoiceTelephone(nullable: true)
        invoiceEmail(nullable: true, email: true)
        guardianName(nullable: true, maxSize: 255)
        guardianEmail(nullable: true, maxSize: 255, email: true)
        guardianTelephone(nullable: true, maxSize: 255)
        guardianName2(nullable: true, maxSize: 255)
        guardianEmail2(nullable: true, maxSize: 255, email: true)
        guardianTelephone2(nullable: true, maxSize: 255)
        web(nullable: true)
        user(nullable: true)
        type(nullable: true)
        archived(nullable: true)
        user(unique: ['facility'])

        clubMessagesDisabled(nullable: true)
        exludeFromNumberOfBookingsRule(nullable: true)
        birthyear(nullable: true)
        dateOfBirth(nullable: true)
        club(nullable: true)
        accessCode(nullable: true)
        deleted(nullable: true)
        vatNumber(nullable: true)
    }

    static mapping = {
        sort number: "asc"
        bookings sort: 'id', order: 'desc'
        customerCoupons sort: 'id', order: 'desc'
        inviteTickets sort: 'dateCreated', order: 'desc'
        updateRequestTickets sort: 'dateCreated', order: 'desc'
        dateOfBirth type: "date"
        facility index: "facility_cust_no_idx, facility_email_idx"
        number index: "facility_cust_no_idx"
        email index: "facility_email_idx"
    }

    static namedQueries = {
        birthyears { Facility f, Boolean membersOnly = false,
                     LocalDate membershipStartDate = null, LocalDate membershipEndDate = null ->
            projections {
                distinct("birthyear")
            }
            facility {
                eq("id", f.id)
            }
            if (membersOnly) {
                def startDate = new LocalDate()
                def endDate = startDate
                if (membershipStartDate && membershipEndDate) {
                    startDate = membershipStartDate
                    endDate = membershipEndDate
                }
                createAlias("memberships", "m")
                le("m.startDate", endDate)
                ge("m.gracePeriodEndDate", startDate)
            }
            isNotNull("birthyear")
            ne("type", CustomerType.ORGANIZATION)
            order("birthyear", "asc")
        }

        clubs { Facility f, Boolean membersOnly = false,
                LocalDate membershipStartDate = null, LocalDate membershipEndDate = null ->
            projections {
                distinct("club")
            }
            facility {
                eq("id", f.id)
            }
            if (membersOnly) {
                def startDate = new LocalDate()
                def endDate = startDate
                if (membershipStartDate && membershipEndDate) {
                    startDate = membershipStartDate
                    endDate = membershipEndDate
                }
                createAlias("memberships", "m")
                le("m.startDate", endDate)
                ge("m.gracePeriodEndDate", startDate)
            }
            isNotNull("club")
            order("club", "asc")
        }
    }

    def beforeInsert() {
        birthyear = dateOfBirthToBirthYear()
        firstname = StringUtils.capitalize(firstname)?.trim()
        lastname = StringUtils.capitalize(lastname)?.trim()
        email = StringUtils.lowerCase(email)?.trim()
    }

    def beforeUpdate() {
        birthyear = dateOfBirthToBirthYear()
        firstname = StringUtils.capitalize(firstname)?.trim()
        lastname = StringUtils.capitalize(lastname)?.trim()
        email = StringUtils.lowerCase(email)?.trim()
    }

    Integer dateOfBirthToBirthYear() {
        if (!isCompany() && dateOfBirth && (dateOfBirth[Calendar.YEAR] > 1929)) {
            return dateOfBirth[Calendar.YEAR]
        } else {
            return null
        }
    }

    boolean belongsTo(MembershipFamily family) {
        return membership?.family?.id?.equals(family.id)
    }

    boolean belongsTo(Group group) {
        return getGroups().findAll { it.id.equals(group.id) }.size() > 0
    }

    boolean hasMembership() {
        getMembership()
    }

    Membership getMembership() {
        getMembership(null)
    }

    Membership getMembership(LocalDate start) {
        getMembershipByFilter(new FilterCustomerCommand(membershipStartDate: start))
    }

    Membership getMembershipByFilter(FilterCustomerCommand cmd) {
        def start = cmd?.membershipStartDate ?: new LocalDate()
        def end = cmd?.membershipEndDate ?: start

        def m = memberships.findAll { cm ->
            cm.startDate <= end && cm.gracePeriodEndDate >= start &&
                    (!cmd?.status || cmd.status.any { cm.isInStatus(it) }) &&
                    (!cmd?.type || cmd.type.any { cm.type?.id == it || (it == 0 && !cm.type) }) &&
                    (!cmd?.members || cmd.members.any { cm.isInFamilyStatus(it) })
        }.sort {
            it.startDate
        }

        m ? m[-1] : null
    }

    boolean hasAnyMembershipAtSlotTime(Slot slot) {
        memberships.any { Membership m ->
            m.coversSlotTime(slot)
        }
    }

    boolean hasActiveMembership(LocalDate date = null) {
        getActiveMembership(date)
    }

    Membership getActiveMembership(LocalDate date = null) {
        def m = memberships.findAll {
            if (date) {
                it.isActive(date)
            } else {
                it.isActive()
            }
        }.sort {
            it.startDate
        }

        m ? m[-1] : null
    }

    Membership getCurrentNonGracedMembership() {
        def today = new LocalDate()
        return memberships.find {
            it.activated && it.startDate <= today && it.endDate >= today
        }
    }

    Membership getUnpaidPayableMembership(Integer yearlyMembershipPurchaseDaysInAdvance = 0) {
        List<Membership> m = memberships.findAll { Membership membership ->
            return membership.isUnpaidPayable(yearlyMembershipPurchaseDaysInAdvance ?: 0)
        }.sort {
            it.startDate
        }

        m ? m[-1] : null
    }

    boolean hasUpcomingMembership() {
        memberships.find { it.isUpcoming() }
    }

    Membership getUpcomingMembership() {
        memberships.findAll { it.isUpcoming() }.sort { it.startDate }[0]
    }

    boolean hasNonEndedMembership(Long exceptMembershipId) {
        def today = LocalDate.now()
        memberships.find {
            (!exceptMembershipId || it.id != exceptMembershipId) &&
                    it.gracePeriodEndDate >= today
        }
    }

    boolean hasMembershipType(MembershipType membershipType) {
        if (!membership || !membership.type) {
            return false
        }

        return membership?.type?.id == membershipType?.id
    }

    boolean canReceiveMail() {
        if ((email && email != "") || (guardianEmail && guardianEmail != "")) {
            return true
        }

        return false
    }

    def getGroups() {
        return customerGroups.collect { it.group }
    }

    def isCompany() {
        return type?.equals(CustomerType.ORGANIZATION)
    }

    void unlinkMembershipFamily() {
        memberships.each { membership ->
            def family = membership?.family
            if (family) {
                def deleteFamily = false
                if (Membership.countByFamily(family) == 1) {
                    deleteFamily = true
                } else if (membership.family.contact.id == id) {
                    family.contact = Membership.findByIdNotEqualAndFamily(
                            membership.id, family)?.customer
                    family.save()
                }
                membership.family = null
                membership.save()
                if (deleteFamily) {
                    family.delete(flush: true)
                }
            }
        }
    }

    def unlinkGroups() {
        def customerGroups = this.customerGroups
        if (customerGroups) {
            this.customerGroups = null
            customerGroups.each { CustomerGroup.unlink(this, it.group) }
            this.save()
        }
    }

    def unlinkCourseParticipants() {
        List<Participant> courseParticipants = this.courseParticipants.toList()
        if (courseParticipants) {
            courseParticipants.each { Participant participant -> participant.remove() }
            this.courseParticipants = null
            this.save()
        }
    }

    String fullName() {
        if (type.equals(CustomerType.ORGANIZATION)) {
            return this.companyname
        }

        return this.firstname + " " + this.lastname
    }

    String toString() { "${fullName()}" }

    int compareTo(Object obj) {
        if (number && obj.number) {
            return number.compareTo(obj.number)
        } else {
            return -1
        }
    }

    boolean hasInvoiceEmail() {
        return getCustomerInvoiceEmail()
    }

    String getCustomerInvoiceEmail() {
        return hasGuardianEmails() ? getCustomerGuardianEmail() : (invoiceEmail ?: email)
    }

    def getInvoiceAddress(String namePrefix = null) {
        def address = [:]
        def names = []

        names << (namePrefix ? namePrefix + " " : "") + fullName()

        if (isCompany() && contact) {
            names << contact
        }

        // Add both company name and contact info if company
        // otherwise (person) just replace name with invoice contact
        if (invoiceContact) {
            if (isCompany()) {
                names[1] = invoiceContact
            } else {
                names[0] = invoiceContact
            }
        }

        address.names = names
        address.name = fullName()

        if (invoiceAddress1) {
            address.address1 = invoiceAddress1
            address.address2 = invoiceAddress2
            address.city = invoiceCity
            address.zipcode = invoiceZipcode
        } else {
            address.address1 = address1
            address.address2 = address2
            address.city = city
            address.zipcode = zipcode
        }

        address
    }

    String getGuardianInfo() {
        ([guardianName, guardianEmail, guardianTelephone] - "" - null).join(" ")
    }

    Integer getAge() {
        (!isCompany() && dateOfBirth) ?
                Years.yearsBetween(new LocalDate(dateOfBirth), new LocalDate()).years : null
    }

    Locale getLocale() {
        new Locale(user ? user.language : facility.language)
    }

    String shortBirthYear() {
        if (birthyear && birthyear.toString().length() == 4) {
            return '-' + birthyear.toString().substring(2)
        } else {
            return ''
        }
    }

    String getPersonalNumber() {
        return getPersonalNumber(true)
    }

    String getPersonalNumber(boolean full) {
        if (dateOfBirth) {
            PersonalNumberSettings personalNumberSettings = facility.getPersonalNumberSettings()
            String dob = dateOfBirth.format(personalNumberSettings.longFormat)
            if (full) {
                return securityNumber ? "$dob-$securityNumber" : dob
            } else {
                return dob
            }
        } else if (securityNumber) {
            // TODO: remove in future (returns invalid security code that wasn't migrated properly)
            return securityNumber
        } else {
            return ""
        }
    }

    String getPersonalNumberForIdrottOnline() {
        return (this.dateOfBirth && this.securityNumber) ? getPersonalNumber() : ""
    }

    String getCustomerGuardianEmail() {
        return this.guardianEmail ?: this.guardianEmail2
    }

    boolean hasGuardianEmails() {
        return this.guardianEmail || this.guardianEmail2
    }

    def getGuardianMessageInfo() {
        def guardianInfoObject = []

        if (this.guardianEmail && this.guardianEmail != "" && Validator.isMailbox(this.guardianEmail)) guardianInfoObject << [name: this.guardianName, email: this.guardianEmail]
        if (this.guardianEmail2 && this.guardianEmail2 != "" && Validator.isMailbox(this.guardianEmail2)) guardianInfoObject << [name: this.guardianName2, email: this.guardianEmail2]

        return guardianInfoObject
    }

    def getEmailCustomerInfo() {
        return [number: this.number, name: this.fullName(), email: this.email]
    }

    boolean isEmailReceivable() {
        return !this.clubMessagesDisabled && ((this.email && this.email != "" && Validator.isMailbox(this.email) || (!this.email && this.hasGuardianEmails())))
    }

    // - Archived customers should not be active.
    // - Missing membership should not be active.
    // - Only membership status active/cancelled should be active.
    boolean hasActiveIdrottOnlineMembershipAtFacility(Facility facility) {
        !archived && getMembershipByFilter(new FilterCustomerCommand(
            membershipStartDate: new LocalDate(),
            type: facility.getHierarchicalIdrottOnlineMembershipTypes().collect{ it.id }
        ))
    }

    boolean hasActiveIdrottOnlineMembership() {
        !archived && hasMembership()
    }

    boolean isArchivedOrDeleted() {
        archived || deleted
    }

    Long getLicense() {
        number
    }

    /**
     * Checks if a customer can book requested number of slots on facility, with regards to upper limit
     * @param customer
     * @param facility
     * @param numberOfSlotsRequested
     * @return
     */
    boolean canBookMoreSlots(int numberOfSlotsRequested) {
        if (exludeFromNumberOfBookingsRule || !facility.hasBookingLimitPerCustomer()) {
            return true
        }

        int nUpComingBookings = Booking.upcomingBookings(this).count()

        return facility.getMaxBookingsPerCustomer() >= (nUpComingBookings + numberOfSlotsRequested)
    }

    int getDaysBookable() {
        if (!facility.priceListCustomerCategories) {
            return facility.bookingRuleNumDaysBookable
        }

        List<PriceListCustomerCategory> customerCategories = PriceListCustomerCategory.available(facility).listDistinct()

        List<PriceListCustomerCategory> acceptingCustomerCategories = customerCategories.findAll { PriceListCustomerCategory priceListCustomerCategory ->
            return priceListCustomerCategory.acceptConditions(this) && priceListCustomerCategory.daysBookable
        }

        if (!acceptingCustomerCategories) {
            return facility.bookingRuleNumDaysBookable
        }

        int bestCustomerRules = acceptingCustomerCategories.sort { PriceListCustomerCategory priceListCustomerCategory ->
            return priceListCustomerCategory.daysBookable
        }.last().daysBookable

        return bestCustomerRules > facility.bookingRuleNumDaysBookable ? bestCustomerRules : facility.bookingRuleNumDaysBookable
    }

    String nationality() {
        return !nationality ? country : nationality
    }

    Integer getCountOfMasterFacilityCustomers() {
        this.user ? (this?.facility?.getMasterFacilities() ?: []).sum { Facility it ->
            Customer.findByUserAndFacility(this.user, it) != null ? 1 : 0
        } as Integer : 0
    }
    Integer getCountOfMasterFacilityMemberships() {
        this.user ? (this?.facility?.getMasterFacilities() ?: []).sum { Facility it ->
            def c = Customer.findByUserAndFacility(this.user, it)
            c != null ?  c.memberships.size() : 0
        } as Integer : 0
    }

    Integer getCountOfMemberFacilityCustomers() {
        this.user ? (this?.facility?.getMemberFacilities() ?: []).sum { Facility it ->
            Customer.findByUserAndFacility(this.user, it) != null ? 1 : 0
        } as Integer : 0
    }
    Integer getCountOfMemberFacilityMemberships() {
        this.user ? (this?.facility?.getMemberFacilities() ?: []).sum { Facility it ->
            def c = Customer.findByUserAndFacility(this.user, it)
            c != null ?  c.memberships.size() : 0
        } as Integer : 0
    }
}
