package com.matchi

import com.matchi.FacilityProperty.FacilityPropertyKey
import com.matchi.activities.trainingplanner.CourseActivity
import com.matchi.facility.FilterCustomerCommand
import com.matchi.fortnox.v3.FortnoxException
import com.matchi.membership.Membership
import com.matchi.orders.Order
import com.matchi.orders.OrderPayment
import grails.transaction.NotTransactional
import grails.transaction.Transactional
import groovy.sql.GroovyRowResult
import org.apache.commons.lang3.StringUtils
import org.hibernate.criterion.CriteriaSpecification
import org.joda.time.DateTime
import org.joda.time.LocalDate
import org.joda.time.LocalDateTime

import java.sql.Timestamp

class CustomerService {

    static transactional = true
    public static final String ID = 'id'
    public static final String LAST_INVOICE_ACTIVITY = 'lastInvoiceActivity'
    public static final String LAST_ORDER_ACTIVITY = 'lastOrderActivity'
    public static final String LAST_MEMBERSHIP_ACTIVITY = 'lastMembershipActivity'
    public static final String TIME_ZONE = 'UTC'
    def userService
    def groupService
    def facilityService
    def fortnoxFacadeService
    def externalSynchronizationService
    def notificationService
    def dateUtil
    def messageSource
    def groovySql

    /**
     * Creates customer but first tries to match params against existing customer to prevent facility from having doublet of customer
     * @param CreateCustomerCommand
     * @return
     */
    def createCustomer(CreateCustomerCommand cmd) {
        log.debug("Create customer on facility: ${cmd.facilityId} w/ number ${cmd.number}")

        Facility facility = Facility.get(cmd.facilityId)

        Customer customer = findMatchingCustomer(facility, cmd.email, cmd.firstname, cmd.lastname, cmd.companyname)

        if (!customer) {
            customer = new Customer()
            copyProperties(cmd, customer)
            customer.save()
        } else {
            log.debug("Found matching customer ${customer}")
        }

        cmd.groupId?.each {
            def group = Group.findById(it)
            groupService.addCustomerToGroup(group, customer)
        }

        saveToFortnox(customer, facility)

        return customer
    }

    def updateCustomer(Customer customer, UpdateCustomerCommand cmd) {
        Facility facility = Facility.get(cmd.facilityId)
        copyProperties(cmd, customer)

        if (customer.hasErrors()) {
            return null
        }

        saveToFortnox(customer, facility)

        return customer
    }

    def updateCustomer(Customer customer, Map props) {
        Facility facility = customer.facility
        copyProperties(props, customer)

        if (customer.hasErrors()) {
            return null
        }

        saveToFortnox(customer, facility)

        return customer
    }

    def updateCustomer(Customer customer, MembershipRequestItemCommand cmd) {
        customer.address1       = cmd.address
        customer.zipcode        = cmd.zipcode
        customer.city           = cmd.city
        customer.country        = cmd.country
        customer.telephone      = cmd.telephone
        customer.type           = cmd.type
        customer.companyname    = cmd.companyname
        customer.contact        = cmd.contact
        customer.club           = cmd.club

        setPersonalNumber(customer, cmd.birthday, cmd.securitynumber, cmd.birthday + (cmd.securitynumber ? "-" + cmd.securitynumber : ""))

        if (customer.hasErrors()) {
            return null
        }

        saveToFortnox(customer, customer.facility)

        return customer
    }

    Customer getOrCreateCustomer(MembershipRequestItemCommand cmd, Facility facility) {
        getOrCreateCustomer([firstname: cmd.firstname, lastname: cmd.lastname, email: cmd.email,
                             address1: cmd.address, postal_code: cmd.zipcode, city: cmd.city, country: cmd.country,
                             telephone: cmd.telephone, type: cmd.type, companyname: cmd.companyname,
                             contact: cmd.contact, securityNumber: cmd.securitynumber,
                             dateOfBirth: dateUtil.parseDateOfBirth(facility, cmd.birthday)], facility)
    }

    def updateCustomer(Customer customer, CustomerUpdateRequestCommand cmd) {
        customer.firstname      = cmd.firstname
        customer.lastname       = cmd.lastname
        customer.email          = cmd.email
        customer.address1       = cmd.address
        customer.zipcode        = cmd.zipcode
        customer.city           = cmd.city
        customer.country        = cmd.country
        customer.telephone      = cmd.telephone
        customer.cellphone      = cmd.cellphone
        customer.type           = (Customer.CustomerType)cmd.type.toString().toUpperCase()
        setPersonalNumber(customer, cmd.birthday, cmd.securitynumber, cmd.birthday + (cmd.securitynumber ? "-" + cmd.securitynumber : ""))

        if (customer.hasErrors()) {
            return null
        }

        saveToFortnox(customer, customer.facility)

        return customer
    }

    void updateCustomersEmail(User user) {
        Customer.findAllByUser(user).each() {
            it.email = user.email
            it.save()
            saveToFortnox(it, it.facility)
        }
    }

    void clearCustomer(Customer customer) {
        Locale locale = new Locale(customer.facility.language)
        String removedFirstName = messageSource.getMessage("facility.customer.removed.firstname", null, locale)
        String removedLastName = messageSource.getMessage("facility.customer.removed.lastname", null, locale)

        customer.user = null
        customer.unlinkMembershipFamily()
        Membership.unlink(customer)
        customer.unlinkGroups()
        customer.unlinkCourseParticipants()

        customer.deleted                            = new Date()

        saveToFortnox(customer, customer.facility)

        customer.number                             = null
        customer.email                              = null
        customer.firstname                          = removedFirstName
        customer.lastname                           = removedLastName
        customer.companyname                        = null
        customer.contact                            = null
        customer.address1                           = null
        customer.address2                           = null
        customer.zipcode                            = null
        customer.city                               = null
        customer.country                            = null
        customer.telephone                          = null
        customer.cellphone                          = null
        customer.notes                              = null
        customer.web                                = null
        customer.securityNumber                     = null
        customer.orgNumber                          = null
        customer.vatNumber                          = null
        customer.invoiceAddress1                    = null
        customer.invoiceAddress2                    = null
        customer.invoiceCity                        = null
        customer.invoiceZipcode                     = null
        customer.invoiceContact                     = null
        customer.invoiceTelephone                   = null
        customer.invoiceEmail                       = null
        customer.guardianName                       = null
        customer.guardianEmail                      = null
        customer.guardianTelephone                  = null
        customer.guardianName2                      = null
        customer.guardianEmail2                     = null
        customer.guardianTelephone2                 = null
        customer.type                               = null
        customer.archived                           = true
        customer.clubMessagesDisabled               = null
        customer.exludeFromNumberOfBookingsRule     = null
        customer.birthyear                          = null
        customer.dateOfBirth                        = null
        customer.club                               = null
        customer.accessCode                         = null

        customer.save()
    }

    void linkCustomerToUser(Customer customer) {
        if (customer.email && customer.firstname && customer.lastname) {
            def user = User.withCriteria(uniqueResult: true) {
                eq("email", customer.email.trim(), [ignoreCase: true])
                eq("firstname", customer.firstname.trim(), [ignoreCase: true])
                eq("lastname", customer.lastname.trim(), [ignoreCase: true])
            }
            if (user) {
                linkCustomerToUser(customer, user)
                customer.save(flush: true)
            }
        }
    }

    /**
     * Link customer to user if user is not already a customer or if customer is of type COMPANY
     * @param customer and user
     * @return
     */
    def linkCustomerToUser(Customer customer, User user, boolean checkAlreadyCustomer = true) {
        log.info("Linking customer ${customer.id} to user ${user?.id}")
        if (!checkAlreadyCustomer || (user && !user.isCustomerIn(customer.facility))) {
            customer.user  = user
            customer.email = user.email
            customer.save(flush: true)
        }

        return user
    }

    /**
     * Returns customer based on id
     * @param customerId
     * @return
     */
    @NotTransactional
    def getCustomer(def customerId) {
        return Customer.get(customerId)
    }

    def getDistinctCustomersFromIdsList(def customerIds) {
        return Customer.createCriteria().listDistinct {
            join "user"
            inList('id', customerIds)
            order("number", "asc")
        }
    }

    def getCustomersFromIdsList(def customerIds) {
        return Customer.createCriteria().list { inList('id', customerIds) }
    }

    /**
     * Get or create users customer given facility. If customer already exists, link user to the matching customer
     * @param user
     * @param facility
     * @return Fetched or created customer
     */
    @NotTransactional
    Customer getOrCreateUserCustomer(User user, Facility facility) {
        Customer customer = findUserCustomer(user, facility)

        if (!customer) {
            log.debug("Creating customer from user ${user.email}")
            customer = new Customer()
            copyUser(user, customer, facility)
            facility.addToCustomers(customer)

        } else {
            linkCustomerToUser(customer, user)
        }

        return customer
    }

    @NotTransactional
    Customer getOrCreateCustomer(Map props, Facility facility, boolean allowUserConnection = false) {
        def customer

        if (props.dateOfBirth && props.securityNumber) {
            customer = Customer.withCriteria {
                eq("facility", facility)
                eq("dateOfBirth", props.dateOfBirth)
                eq("securityNumber", props.securityNumber)
                eq("archived", false)
                isNull("deleted")
            }[0]

            if (customer) {
                updateCustomer(customer, props)
                customer.save(flush: true)
            }
        }

        if (!customer) {
            if (props.email) {
                String propsEmail = props.email
                String propsFirstname = props.firstname
                String propsLastname = props.lastname
                customer = findMatchingCustomer(facility, propsEmail, propsFirstname, propsLastname, allowUserConnection)

                if (!customer) {
                    if (log.debugEnabled) {
                        log.debug("Creating customer from properties map ${props}")
                    }
                    customer = new Customer(props)
                    customer.facility = facility
                    customer.number = facility.nextCustomerNumber
                    customer.country = facility.country
                    customer.zipcode = props.postal_code
                    if (props.guardian) {
                        if (props.guardian.firstname || props.guardian.lastname) {
                            customer.guardianName = "${props.guardian.firstname ?: ''} ${props.guardian.lastname ?: ''}".trim()
                        }
                        customer.guardianEmail = props.guardian.email
                        customer.guardianTelephone = props.guardian.cellphone
                    }
                } else {
                    updateCustomer(customer, props)
                }

                customer.save(flush: true)
            } else {
                return null
            }
        }

        return customer
    }

    /**
     * Find customer for a user on given facility
     * @param User to find customer for
     * @param Facility where to find customer
     * @return Customer, if found
     */
    @NotTransactional
    Customer findUserCustomer(User user, Facility facility) {
        Customer customer = Customer.findByUserAndFacility(user, facility)

        if (!customer && user) {
            return findMatchingCustomer(facility, user.email, user.firstname, user.lastname)
        }

        return customer
    }

    @Transactional(readOnly = true)
    Collection<Customer> findHierarchicalUserCustomers(User user, Collection<Facility> facilities) {
        Customer customer
        Collection<Customer> customers = []

        if (!user || facilities.isEmpty()) {
            return []
        }

        facilities.each {
            customer = findUserCustomer(user, it)
            if (customer != null) {
                customers += customer
            }
        }

        return customers
    }

    @Transactional(readOnly = true)
    Collection<Customer> findHierarchicalUserCustomers(Customer customer, Boolean includeSelf = true) {
        customer?.user ?
            findHierarchicalUserCustomers(customer.user, facilityService.getAllHierarchicalFacilities(customer.facility, includeSelf))
            : (includeSelf && customer ? [customer] : [])
    }

    @Transactional(readOnly = true)
    Collection<Customer> findHierarchicalCustomersForCustomerPage(Facility facility, Customer customer, Boolean includeSelf = false) {
        Collection<Customer> customers = []
        if (customer?.facility?.isMasterFacility()) {
            customers = findHierarchicalUserCustomers(customer.user, customer.facility.getMemberFacilities())
        } else {
            customers = findHierarchicalUserCustomers(customer, includeSelf)
        }
        if (facility.id != customer.facility.id) {
            customers = customers.findAll {it-> it.facility.id == facility.id}
        }
        return customers
    }

    @Transactional(readOnly = true)
    Collection<Customer> findMasterFacilityCustomers(User user, Facility facility) {
        if (user && facility?.isMemberFacility()) {
            return facility.masterFacilities?.findResults {
                return Customer.findByUserAndFacility(user, it)
            }
        }
        return []
    }

/**
     * Find archived customers
     * @param query
     * @return
     */
    @NotTransactional
    def findArchivedCustomers(FilterCustomerCommand filter, Facility facility) {
        findCustomers(filter, facility, true)
    }

    /**
     * Find customer not in archive
     * @param query
     * @return
     */
    @NotTransactional
    def findCustomers(FilterCustomerCommand filter, Facility facility) {
        return findCustomers(filter, facility, false)
    }

    /**
     * Find customer
     * @param query
     * @return
     */
    @NotTransactional
    def findCustomers(FilterCustomerCommand filter, Facility facility, def archived, boolean fetchIdsOnly = false) {
        Locale locale = new Locale(facility.language)
        String notSpecified = messageSource.getMessage("facilityCustomer.index.clubs.noClub", null, locale)
        def sqlClubs = filter.clubs?.findAll { return !it.equals(notSpecified) }?.unique()
        def includeNonSpecifiedClub = filter.clubs?.any { return it.equals(notSpecified) }

        Date lastActivityCutoffDate
        if(filter.lastActivity) {
            DateTime lastActivityCutoffDatetime = new DateTime()
            lastActivityCutoffDate = lastActivityCutoffDatetime.minusYears(filter.lastActivity).toDate()
        }

        def startDate = new LocalDate()
        def endDate = startDate
        if (filter.membershipStartDate && filter.membershipEndDate) {
            startDate = filter.membershipStartDate
            endDate = filter.membershipEndDate
        }

        def customerRows = Customer.createCriteria().list() {
            projections { //Adds properties to make it sortable and filterable later
                property('id')
                sqlProjection("IFNULL(user_id, concat('c', {alias}.id)) as u_user", ["u_user"], [org.hibernate.type.StandardBasicTypes.STRING])
                sqlProjection("CASE WHEN ({alias}.facility_id = ${facility.id}) THEN 1 ELSE 0 END as has_default_facility", ["has_default_facility"], [org.hibernate.type.StandardBasicTypes.INTEGER])
            }
            or {
                eq("facility.id", facility.id)
                if (!filter.dontIncludeMemberFacilitysCustomer && facility.masterFacilities.size() > 0) {
                    inList "facility.id", facility.masterFacilities*.id
                }
                if (filter.localFacilities.size() > 0) {
                    inList "facility.id", filter.localFacilities
                }
            }

            eq("archived", archived)
            isNull("deleted")

            if (filter.members || filter.status || filter.type) {
                createAlias("memberships", "m", CriteriaSpecification.LEFT_JOIN)
                createAlias("m.type", "t", CriteriaSpecification.LEFT_JOIN)
                createAlias("m.family", "fml", CriteriaSpecification.LEFT_JOIN)

                if (filter.members) {
                    or {
                        if (filter.members.contains(FilterCustomerCommand.ShowMembers.MEMBERS_ONLY)) {
                            and {
                                le("m.startDate", endDate)
                                ge("m.gracePeriodEndDate", startDate)
                            }
                        }
                        if (filter.members.contains(FilterCustomerCommand.ShowMembers.NO_MEMBERS)) {
                            not {
                                inList("id", getMembersIds(facility, archived))
                            }
                        }
                        if (filter.members.contains(FilterCustomerCommand.ShowMembers.FAMILY_MEMBERS)) {
                            and {
                                le("m.startDate", endDate)
                                ge("m.gracePeriodEndDate", startDate)
                                isNotNull("fml.id")
                            }
                        }
                        if (filter.members.contains(FilterCustomerCommand.ShowMembers.FAMILY_MEMBER_CONTACTS)) {
                            and {
                                le("m.startDate", endDate)
                                ge("m.gracePeriodEndDate", startDate)
                                eqProperty("fml.contact.id", "id")
                            }
                        }
                        if (filter.members.contains(FilterCustomerCommand.ShowMembers.FAMILY_MEMBER_MEMBERS)) {
                            and {
                                le("m.startDate", endDate)
                                ge("m.gracePeriodEndDate", startDate)
                                isNotNull("fml.id")
                                neProperty("fml.contact.id", "id")
                            }
                        }
                        if (filter.members.contains(FilterCustomerCommand.ShowMembers.NO_FAMILY_MEMBERS)) {
                            and {
                                le("m.startDate", endDate)
                                ge("m.gracePeriodEndDate", startDate)
                                isNull("fml.id")
                            }
                        }
                    }
                }

                if (filter.type) {
                    le("m.startDate", endDate)
                    ge("m.gracePeriodEndDate", startDate)
                    or {
                        inList("t.id", filter.type)
                        if (filter.type.contains(0L)) {
                            isNull("t.id")
                        }
                    }
                }
            }

            if (filter.seasons.size() > 0) {
                subscriptions {
                    slots {
                        or {
                            filter.seasons.each { seasonId ->
                                Season season = Season.read(seasonId)
                                and {
                                    gt('startTime', season.startTime)
                                    lt('endTime', season.endTime)
                                }

                            }
                        }
                    }
                }
            }

            if (filter.group.size() > 0) {
                or {
                    if (filter.group.contains(0L)) {
                        isEmpty("customerGroups")
                    }
                    customerGroups(CriteriaSpecification.LEFT_JOIN) {
                        inList("group.id", filter.group)
                    }
                }
            }

            if (filter.gender.size() > 0) {
                or {
                    inList("type", filter.gender)
                    if (filter.gender.contains(Customer.CustomerType.NULL)) {
                        isNull("type")
                    }
                }
            }

            if (filter.birthyear) {
                or {
                    isNull("type")
                    ne("type", Customer.CustomerType.ORGANIZATION)
                }
                or {
                    inList("birthyear", filter.birthyear)
                    if (filter.birthyear.contains(0)) {
                        isNull("birthyear")
                    }
                }
            }

            if (filter.invoiceStatus) {
                invoices {
                    inList("status", filter.invoiceStatus)
                }
            }

            if (filter.q) {
                def q = StringUtils.replace(filter.q, "_", "\\_")
                or {
                    like("email", "%${q}%")
                    like("firstname", "%${q}%")
                    like("lastname", "%${q}%")
                    like("companyname", "%${q}%")
                    like("telephone", "%${q}%")
                    like("cellphone", "%${q}%")
                    like("contact", "%${q}%")
                    like("notes", "%${q}%")
                    like("invoiceAddress1", "%${q}%")
                    like("invoiceContact", "%${q}%")
                    sqlRestriction("{alias}.number like ?", ["%${q}%" as String])
                    sqlRestriction("concat({alias}.firstname,' ',{alias}.lastname) like ?", ["%${q}%" as String])
                }
            }

            if (filter.courses.size() > 0) {
                courseParticipants {
                    or {
                        filter.courses.each { courseId ->
                            eq('activity', CourseActivity.read(courseId))
                        }
                    }
                }
            }

            if(!sqlClubs?.isEmpty() || includeNonSpecifiedClub) {
                or {
                    if(!sqlClubs?.isEmpty()) {
                        'in'("club", sqlClubs)
                    }

                    if(includeNonSpecifiedClub) {
                        isNull("club")
                    }
                }
            }

            if(lastActivityCutoffDate) {
                le('lastUpdated', lastActivityCutoffDate)
            }

            def sort = filter.sort.tokenize(",")
            sort.each {
                order(it, filter.order)
            }
        }

        // TODO: find a way to include these extra queries inside one query above;
        //       Criteria API doesn't support "having" clause currently
        List<Long> ids = customerRows.sort{
            a,b -> b[2]-a[2]
        }.groupBy{ //Creates a map with one entry per user, sorted by has_default_facility property
            it[1]
        }.collect { // this will take customer from visited facility first and grouped facility if that doesn't exist and then property [0] = id
            it.value[0][0] as Long
        }

        if (filter.members) {
            filterLocalCustomersWithGlobalMemberships(ids, facility)
        }

        if (ids && filter.status) {
            def idsByStatus = []
            if (filter.status.contains(FilterCustomerCommand.MemberStatus.PAID)) {
                def rows = Customer.executeQuery("""
                        select c.id, o.id, o.price, (sum(p.amount) - sum(p.credited))
                        from Customer c
                        join c.memberships m
                        join m.order o
                        left join o.payments p
                        where c.id in (:cids) and m.activated = :act and o.status in (:ostat)
                            and m.startDate <= :endDate and m.gracePeriodEndDate >= :startDate
                            and (o.price = 0 or p.status in (:pstat)) and m.cancel = :cancel
                        group by c.id, o.id
                        having (sum(p.amount) - sum(p.credited)) is null
                            or (sum(p.amount) - sum(p.credited)) >= o.price""",
                    [cids: ids, act: true, endDate: endDate, startDate: startDate, cancel: false,
                     ostat: [Order.Status.CONFIRMED, Order.Status.COMPLETED],
                     pstat: [OrderPayment.Status.AUTHED, OrderPayment.Status.CAPTURED,
                             OrderPayment.Status.CREDITED]])
                rows.each {
                    idsByStatus << it[0]
                }
            }
            if (filter.status.contains(FilterCustomerCommand.MemberStatus.UNPAID)) {
                def rows = Customer.executeQuery("""
                        select c.id, o.id, o.price, o.status, (sum(p.amount) - sum(p.credited))
                        from Customer c
                        join c.memberships m
                        join m.order o
                        left join o.payments p with p.status in (:pstat)
                        where c.id in (:cids) and m.activated = :act and o.price != 0
                            and m.startDate <= :endDate and m.gracePeriodEndDate >= :startDate
                        group by c.id, o.id
                        having o.status not in (:ostat)
                            or (sum(p.amount) - sum(p.credited)) is null
                            or (sum(p.amount) - sum(p.credited)) < o.price""",
                    [cids: ids, act: true,
                     endDate: endDate, startDate: startDate,
                     ostat: [Order.Status.CONFIRMED, Order.Status.COMPLETED],
                     pstat: [OrderPayment.Status.AUTHED, OrderPayment.Status.CAPTURED,
                             OrderPayment.Status.CREDITED]])
                rows.each {
                    idsByStatus << it[0]
                }
            }
            if (filter.status.contains(FilterCustomerCommand.MemberStatus.PENDING)) {
                def rows = Customer.executeQuery("""
                        select c.id from Customer c join c.memberships m
                        where c.id in (:cids) and m.activated = :act
                            and m.startDate <= :endDate and m.gracePeriodEndDate >= :startDate""",
                    [cids: ids, act: false, endDate: endDate, startDate: startDate])
                if (rows) {
                    idsByStatus.addAll(rows)
                }
            }
            if (filter.status.contains(FilterCustomerCommand.MemberStatus.CANCEL)) {
                def rows = Customer.executeQuery("""
                        select c.id, o.id, o.price, (sum(p.amount) - sum(p.credited))
                        from Customer c
                        join c.memberships m
                        join m.order o
                        left join o.payments p
                        where c.id in (:cids) and m.activated = :act and o.status in (:ostat)
                            and m.startDate <= :endDate and m.gracePeriodEndDate >= :startDate
                            and (o.price = 0 or p.status in (:pstat)) and m.cancel = :cancel
                        group by c.id, o.id
                        having (sum(p.amount) - sum(p.credited)) is null
                            or (sum(p.amount) - sum(p.credited)) >= o.price""",
                    [cids: ids, act: true, endDate: endDate, startDate: startDate, cancel: true,
                     ostat: [Order.Status.CONFIRMED, Order.Status.COMPLETED],
                     pstat: [OrderPayment.Status.AUTHED, OrderPayment.Status.CAPTURED,
                             OrderPayment.Status.CREDITED]])
                rows.each {
                    idsByStatus << it[0]
                }
            }

            if (filter.status.contains(FilterCustomerCommand.MemberStatus.FAILED_PAYMENT)) {
                def rows = Customer.executeQuery("""
                        select c.id, o.id, o.price, o.status, (sum(p.amount) - sum(p.credited))
                        from Customer c
                        join c.memberships m
                        join m.order o
                        left join o.payments p with p.status in (:pstat)
                        where c.id in (:cids) and m.activated = :act and o.price != 0
                            and m.startDate <= :endDate and m.gracePeriodEndDate >= :startDate
                            and m.autoPayAttempts is not null
                        group by c.id, o.id
                        having o.status not in (:ostat)
                            or (sum(p.amount) - sum(p.credited)) is null
                            or (sum(p.amount) - sum(p.credited)) < o.price""",
                    [cids: ids, act: true,
                     endDate: endDate, startDate: startDate,
                     ostat: [Order.Status.CONFIRMED, Order.Status.COMPLETED],
                     pstat: [OrderPayment.Status.AUTHED, OrderPayment.Status.CAPTURED,
                             OrderPayment.Status.CREDITED]])
                rows.each {
                    idsByStatus << it[0]
                }
            }

            ids = idsByStatus.unique()
        }

        // Some special logic required if we have a last activity date
        if(lastActivityCutoffDate) {
            List<Customer> customersToCheck = Customer.createCriteria().list {
                'in'("id", ids ?: [-1L])
            }

            assignLastActivity(customersToCheck)

            ids = customersToCheck.findAll { Customer customer ->
                return customer.lastActivity <= lastActivityCutoffDate
            }*.id
        }

        if (fetchIdsOnly) {
            return ids
        }

        Customer.createCriteria().list(max: filter.allselected ? 100000 : filter.max, offset: filter.allselected ? 0 : filter.offset) {
            'in'("id", ids ?: [-1L])

            def sort = filter.sort.tokenize(",")
            sort.each {
                order(it, filter.order)
            }
        }
    }

    /**
     * Find customer that matches user on facility, email, firstname and lastname that is not archived
     * @param email
     * @param firstname
     * @param lastname
     * @return
     */
    @NotTransactional
    Customer findMatchingCustomer(Facility facility, String email, String firstname, String lastname, boolean allowUserConnection = false) {
        return findMatchingCustomer(facility, email, firstname, lastname, null, allowUserConnection)
    }
    @NotTransactional
    Customer findMatchingCustomer(Facility facility, String email, String firstname, String lastname, String companyname, boolean allowUserConnection = false) {
        log.info("Find matching customer: ${facility},${email},${firstname},${lastname},${companyname}")

        if (facility && ((email && firstname && lastname) || (email && companyname))) {
            return Customer.withCriteria {
                eq("facility", facility)
                eq("email", email?.trim(), [ignoreCase: true])

                if (firstname && lastname) {
                    eq("firstname", firstname?.trim(), [ignoreCase: true])
                    eq("lastname", lastname?.trim(), [ignoreCase: true])
                } else {
                    eq("companyname", companyname?.trim(), [ignoreCase: true])
                }

                eq("archived", false)
                isNull("deleted")

                if(!allowUserConnection) {
                    isNull("user")
                }

                maxResults(1)
            }[0]
        }
        return null
    }

    /**
     * Finds customers that match user that has not been connected to user.
     * Returns one customer per facility. Does not fetch customers from facilities provided
     * @param email
     * @param firstname
     * @param lastname
     * @param companyname
     * @return
     */
    @NotTransactional
    List<Customer> findMatchingCustomers(List<Facility> facilities, String email, String firstname, String lastname, String companyname = null) {
        log.info("Find matching customers: ${email},${firstname},${lastname},${companyname}")

        if ((email && firstname && lastname) || (email && companyname)) {
            return Customer.withCriteria {

                if(facilities?.size() > 0) {
                    not {
                        inList("facility", facilities)
                    }
                }

                eq("email", email?.trim(), [ignoreCase: true])

                if (firstname && lastname) {
                    eq("firstname", firstname?.trim(), [ignoreCase: true])
                    eq("lastname", lastname?.trim(), [ignoreCase: true])
                } else {
                    eq("companyname", companyname?.trim(), [ignoreCase: true])
                }

                eq("archived", false)

                isNull("deleted")
                isNull("user")

            // This grouping could possibly be done in the criteria, but I cannot find how
            }.groupBy { Customer customer ->
                return customer.facility.id
            }.collect { Long facilityId, List<Customer> customers ->
                return customers.first()
            }.flatten()
        }
    }

    /**
     * Saves a MATCHi <pre>Customer</pre> to Fortnox.
     * @param customer
     * @param authentication
     * @return fortnox Contact or null if operation failed
     */
    @NotTransactional
    void saveToFortnox(Customer customer, Facility facility) {
        if (!facility?.hasFortnox()) {
            return
        }

        // Following block wrapped in new transaction since MATCHi must mark
        // contact as synched even if following operations in a transaction fails.
        if(customer.id) {
            customer.save(flush: true)
            Customer.withNewTransaction {
                log.info("Saving ${customer} to fortnox")

                try {
                    fortnoxFacadeService.saveMatchiCustomerToFortnox(customer, facility)
                    if (facility.hasOrganization()) {
                        def organizations = facilityService.getFacilityOrganizations(facility)
                        organizations.each {
                            if (externalSynchronizationService.getFortnoxCustomerNumber(customer, it)) {
                                fortnoxFacadeService.saveMatchiOrganizationCustomerToFortnox(customer, it)
                            }
                        }
                    }
                } catch (com.matchi.fortnox.v3.FortnoxException ex) {
                    customer.errors.reject(FortnoxException.ERROR_CODE, ex.message)
                }
            }
        } else {
            log.warn("Unsaved customer ${customer} could not be synched to Fortnox")
        }

    }

    void disableClubMessages(Customer customer) {
        customer.clubMessagesDisabled = true
        customer.save()
    }

    @NotTransactional
    List<Customer> collectPlayers(List playerCustomerIds, Integer unknownPlayers) {
        def playerCustomers = []
        if (playerCustomerIds || unknownPlayers) {
            playerCustomers = playerCustomerIds.collect { getCustomer(it) }
            if (unknownPlayers) {
                unknownPlayers.times { playerCustomers << new Customer() }
            }
        }
        return playerCustomers
    }

    @NotTransactional
    Customer getCurrentCustomer(Facility facility) {
        def user = userService.getLoggedInUser()
        user ? Customer.findByUserAndFacility(user, facility) : null
    }

    @NotTransactional
    List getConnectedPlayersData(Customer customer, List<String> emailExcludes,
            String searchQuery) {
        def searchTokens = searchQuery.tokenize()
        Player.withCriteria {
            createAlias("booking", "b")
            createAlias("customer", "c", CriteriaSpecification.LEFT_JOIN)
            projections {
                distinct("email")
                property("c.firstname")
                property("c.lastname")
            }
            eq("b.customer", customer)
            isNotNull("email")
            if (customer.email) {
                ne("email", customer.email)
            }
            emailExcludes.each {
                ne("email", it)
            }
            or {
                ilike("email", "$searchQuery%")
                ilike("c.firstname", "$searchQuery%")
                ilike("c.lastname", "$searchQuery%")
                if (searchTokens.size() == 2) {
                    and {
                        ilike("c.firstname", "${searchTokens[0]}%")
                        ilike("c.lastname", "${searchTokens[1]}%")
                    }
                    and {
                        ilike("c.firstname", "${searchTokens[1]}%")
                        ilike("c.lastname", "${searchTokens[0]}%")
                    }
                }
            }
            maxResults(5)
        }.collect {
            def result = [id: it[0]]
            if (it[1] && it[2]) {
                result.name = "${it[1]} ${it[2]} (${it[0]})"
                result.fieldValue = "${it[1]} ${it[2]}"
            } else {
                result.name = it[0]
                result.fieldValue = it[0]
            }
            result
        }
    }

    private def copyProperties(Map props, Customer customer) {
        customer.email              = props.email ?: customer.email
        customer.lastname           = props.lastname ?: customer.lastname
        customer.address1           = props.address1 ?: customer.address1
        customer.address2           = props.address2 ?: customer.address2
        customer.zipcode            = props.zipcode ?: customer.zipcode
        customer.city               = props.city ?: customer.city
        customer.telephone          = props.telephone ?: customer.telephone
        customer.cellphone          = props.cellphone ?: customer.cellphone
        customer.club               = props.club ?: customer.club

        if (props.guardian?.firstname || props.guardian?.lastname) {
            customer.guardianName = "${props.guardian.firstname ?: ''} ${props.guardian.lastname ?: ''}".trim()
        } else {
            customer.guardianName = props.guardianName ?: customer.guardianName
        }
        customer.guardianEmail      = props.guardianEmail ?:
                props.guardian?.email ?: customer.guardianEmail
        customer.guardianTelephone  = props.guardianTelephone ?:
                props.guardian?.cellphone ?: customer.guardianTelephone
        if (props.type) {
            customer.type = props.type
        }
        if (!customer.dateOfBirth && props.dateOfBirth) {
            customer.dateOfBirth = props.dateOfBirth
            customer.birthyear = customer.dateOfBirthToBirthYear()
        }
        if (!customer.securityNumber && props.securityNumber) {
            customer.securityNumber = props.securityNumber
        }

        return customer
    }

    private def copyProperties(def cmd, Customer customer) {
        customer.facility       = Facility.get(cmd.facilityId)
        customer.number         = cmd.number
        customer.type           = cmd.type ?: null
        customer.email          = StringHelper.extendedTrim(cmd.email)
        customer.firstname      = cmd.firstname
        customer.lastname       = cmd.lastname
        customer.companyname    = cmd.companyname
        customer.contact        = cmd.contact
        customer.address1       = cmd.address1
        customer.address2       = cmd.address2
        customer.zipcode        = cmd.zipcode
        customer.city           = cmd.city
        customer.country        = cmd.country
        customer.nationality    = cmd.nationality
        customer.telephone      = cmd.telephone
        customer.cellphone      = cmd.cellphone
        customer.notes          = cmd.notes
        customer.club           = cmd.club
        customer.accessCode     = cmd.accessCode
        customer.vatNumber      = cmd.vatNumber

        setPersonalNumber(customer, cmd.personalNumber, cmd.securityNumber, cmd.orgNumber)

        customer.guardianName       = cmd.guardianName
        customer.guardianEmail      = cmd.guardianEmail
        customer.guardianTelephone  = cmd.guardianTelephone
        customer.guardianName2      = cmd.guardianName2
        customer.guardianEmail2     = cmd.guardianEmail2
        customer.guardianTelephone2 = cmd.guardianTelephone2

        customer.invoiceAddress1  = cmd.invoiceAddress1
        customer.invoiceAddress2  = cmd.invoiceAddress2
        customer.invoiceCity      = cmd.invoiceCity
        customer.invoiceZipcode   = cmd.invoiceZipcode
        customer.invoiceContact   = cmd.invoiceContact
        customer.invoiceTelephone = cmd.invoiceTelephone
        customer.invoiceEmail     = cmd.invoiceEmail
        customer.web              = cmd.web

        customer.clubMessagesDisabled = cmd.clubMessagesDisabled
        if (cmd.exludeFromNumberOfBookingsRule != null) {
            customer.exludeFromNumberOfBookingsRule = cmd.exludeFromNumberOfBookingsRule
        }

        customer.user           = customer.user ?: User.get(cmd.userId)
        return customer
    }

    private def copyUser(User user, Customer customer, Facility facility) {
        CreateCustomerCommand cmd = new CreateCustomerCommand()
        cmd.facilityId  = facility.id
        cmd.number      = facility.getNextCustomerNumber()
        cmd.type        = user.gender ? user.gender.toString().toUpperCase() : null
        cmd.email       = user.email
        cmd.firstname   = user.firstname
        cmd.lastname    = user.lastname
        cmd.address1    = user.address
        cmd.zipcode     = user.zipcode
        cmd.city        = user.municipality?.name
        cmd.country     = user.country
        cmd.nationality = user.nationality
        cmd.telephone   = user.telephone
        cmd.cellphone   = user.telephone

        cmd.securityNumber = user.birthday ? new DateTime(user.birthday).toString("yyMMdd") : null
        cmd.userId      = user.id

        log.debug("Copied user. Got customer number ${cmd.number}")

        return copyProperties(cmd, customer)
    }

    private void setPersonalNumber(Customer customer, String personalNumber, String securityNumber, String orgNumber) {
        if (customer.isCompany()) {
            customer.orgNumber = orgNumber
        }
        else {
            if (personalNumber) {
                customer.dateOfBirth = dateUtil.parseDateOfBirth(customer.facility, personalNumber)
                customer.birthyear = customer.dateOfBirthToBirthYear()
                customer.securityNumber = securityNumber
            }
            else {
                customer.dateOfBirth = null
                customer.securityNumber = null
                customer.orgNumber = null
            }
        }
    }

    /**
     * Overloading of the assignLastActivity method
     * @param customer
     */
    void assignLastActivity(Customer customer) {
        assignLastActivity([customer])
    }

    /**
     * Sets the transient property lastActivity to a list of customers
     * One database call + looping through every row gives complexity of O(n)
     * @param customers
     */
    void assignLastActivity(List<Customer> customers) {
        if(!customers || customers.size() == 0) return

        // Joining customer ids directly in query should be safe due to them being of type Long which would prevent Strings from sneaking in there
        String query = """
            select c.id as ${ID},
            (select max(i.last_updated) from invoice i where c.id=i.customer_id) as ${LAST_INVOICE_ACTIVITY},
            (select max(o.last_updated) from `order` o where c.id=o.customer_id) as ${LAST_ORDER_ACTIVITY},
            (select max(m.end_date) from membership m where c.id=m.customer_id) as ${LAST_MEMBERSHIP_ACTIVITY}
            from customer c where c.id in (${customers*.id.join(',')})
        """

        List<GroovyRowResult> result = groovySql.rows(query)
        Map<Long, Customer> customersPerId = customers.collectEntries() { Customer c -> [ (c.id): c ] }

        result.each { GroovyRowResult row ->
            Long customerId = row.get(ID) as Long
            Customer customer = customersPerId.get(customerId)
            Date lastActivity = customer.lastUpdated

            Timestamp lastInvoiceTimestamp = row.get(LAST_INVOICE_ACTIVITY, null) as Timestamp
            Timestamp lastOrderTimestamp = row.get(LAST_ORDER_ACTIVITY, null) as Timestamp
            Date lastMembershipDate = row.get(LAST_MEMBERSHIP_ACTIVITY, null) as Date

            // Invoice dates are stored as DateTime and not Date => Uses wrong timezone
            // This means that the database saves the time with UTC, compared to the rest of the DB having them in Europe/Stockholm
            if(lastInvoiceTimestamp) {
                LocalDateTime localDateTime = new LocalDateTime(lastInvoiceTimestamp.getTime())
                Date lastInvoiceDate = localDateTime.toDate(TimeZone.getTimeZone(TIME_ZONE))
                lastActivity = lastInvoiceDate > lastActivity ? lastInvoiceDate : lastActivity
            }

            if(lastOrderTimestamp) {
                Date lastOrderDate = new Date(lastOrderTimestamp.getTime())
                lastActivity = lastOrderDate > lastActivity ? lastOrderDate : lastActivity
            }

            if(lastMembershipDate) {
                lastActivity = lastMembershipDate > lastActivity ? lastMembershipDate : lastActivity
            }
            customer.lastActivity = lastActivity
        }
        groovySql.close()
    }

    Customer getCustomer(Long id, Facility facility) {
        Customer.findByIdAndFacility(id, facility)
    }

    List<Customer> listFederationsCustomers(User user) {
        def result = []

        def federationFacilities = Facility.withCriteria {
            facilityProperties {
                eq("key", FacilityPropertyKey.FEATURE_FEDERATION.name())
                or {
                    eq("value", "1")
                    eq("value", "on")
                }
            }
        }
        def customerNumberLicenseFacilities = Facility.withCriteria {
            facilityProperties {
                eq("key", FacilityPropertyKey.FEATURE_USE_CUSTOMER_NUMBER_AS_LICENSE.name())
                or {
                    eq("value", "1")
                    eq("value", "on")
                }
            }
        }
        federationFacilities.intersect(customerNumberLicenseFacilities).each { facility ->
            def customer = Customer.findByUserAndFacility(user, facility)
            if (customer) {
                result << customer
            }
        }

        result
    }

    void archiveCustomer(Customer customer) {
        customer.archived = true
        customer.user = null
        customer.unlinkMembershipFamily()
        Membership.unlink(customer)
        customer.unlinkGroups()
        customer.save(flush: true)

        saveToFortnox(customer, customer.facility)
    }

    void unArchiveCustomer(Customer customer) {
        customer.archived = false
        customer.save(flush: true)

        saveToFortnox(customer, customer.facility)
    }

    @NotTransactional
    List<Long> getMembersIds(Facility facility, Boolean archived = false) {
        def today = new LocalDate()
        def membersIds = Customer.withCriteria {
            createAlias("memberships", "m")
            projections {
                distinct("id")
            }
            eq("facility", facility)
            eq("archived", archived)
            isNull("deleted")
            le("m.startDate", today)
            ge("m.gracePeriodEndDate", today)
        }
        membersIds ?: [-1L]
    }

    def filterLocalCustomersWithGlobalMemberships(List<Long> ids, Facility facility) {
        List<Facility> globalFacilities = facility.getMasterFacilities()
        if (globalFacilities) {
            def globalMembers = Customer.createCriteria().list {
                projections {
                    distinct("id")
                    property("user")
                }
                inList "facility", globalFacilities
                inList "id", ids
            }

            if (globalMembers) {
                def users = globalMembers.collect { it[1] }

                def localCustomers = Customer.createCriteria().list {
                    projections {
                        distinct("id")
                        property("user")
                    }
                    inList "user", users
                    eq "facility", facility
                }
                if (localCustomers) {
                    def localCustomerUsers = localCustomers.collect { it[1] }
                    def globalMembersIds = globalMembers.findAll { localCustomerUsers.contains(it[1]) }.collect { it[0] }
                    def localCustomersIds = localCustomers.collect { it[0] }
                    ids.addAll(localCustomersIds)
                    ids.removeAll(globalMembersIds)
                }
            }
        }
    }
}
