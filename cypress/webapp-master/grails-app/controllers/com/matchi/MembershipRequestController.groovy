package com.matchi

import com.matchi.membership.Membership
import com.matchi.membership.MembershipType
import grails.validation.Validateable
import org.grails.databinding.BindUsing
import org.joda.time.LocalDate

import javax.servlet.http.HttpServletResponse

class MembershipRequestController extends GenericController {

    def memberService
    def notificationService
    def customerService
    def membersFamilyService
    def facilityService

    def index() {
        def facility = Facility.findByShortname(params.name)

        def facilitiesThatReceivesMemberships = facilityService.getAllHierarchicalFacilities(facility).findAll { it.recieveMembershipRequests }

        if (facilitiesThatReceivesMemberships.isEmpty()) {
            response.sendError HttpServletResponse.SC_NOT_FOUND
            return
        }
        def user = getCurrentUser()


        def customers = customerService.findHierarchicalUserCustomers(user, facilitiesThatReceivesMemberships)

        def model = [name                             : facility.shortname,
                     user                             : user,
                     startDate                        : LocalDate.now(),
                     facilitiesThatReceivesMemberships: facilitiesThatReceivesMemberships,
                     purchasedAtFacility              : facility]

        Membership baseMembership = null

        if (!customers.isEmpty()) {
            if (params.baseMembership) {
                baseMembership = Membership.findById(params.baseMembership)
                if (!baseMembership || !customers.contains(baseMembership.customer) || !baseMembership.isRemotePayable()) {
                    response.sendError HttpServletResponse.SC_NOT_FOUND
                    return
                }

                model.cmd = initMembershipRequestCommand(baseMembership) as Serializable
                model.startDate = baseMembership.startDate
            }
        }

        model.locale = new Locale(facility?.language)

        model.typesWithDates = memberService.getAvailableMembershipTypesWithDates(model, facility, user, baseMembership)

        def facilitiesWithAvailableMembershipTypes = facilitiesThatReceivesMemberships.findAll { Facility f ->
            return model.typesWithDates.any {
                it.type.facility == f
            }
        }

        if (model.typesWithDates.isEmpty()) {
            flash.message = message(code: "membershipRequest.index.alreadyMember")
            redirect(controller: "facility", action: "show", params: [name: facility.shortname])
            return
        }

        def familyMembershipsEnabledFacilities = facilitiesWithAvailableMembershipTypes.findAll { it.familyMembershipRequestAllowed }
        model.familyMembershipsEnabledFacilities = familyMembershipsEnabledFacilities

        model.requestPayment = facility?.isMembershipRequestPaymentEnabled()

        model
    }

    def request(MembershipRequestCommand cmd) {
        Facility facility = Facility.findByShortname(cmd.sname)

        def facilitiesThatReceivesMemberships = facilityService.getAllHierarchicalFacilities(facility).findAll { it.recieveMembershipRequests }

        def user = (User) getCurrentUser()
        def customers = customerService.findHierarchicalUserCustomers(user, facilitiesThatReceivesMemberships)
        def memberships = customers.collect { it.membership }

        Map model = [cmd                              : cmd,
                     startDate                        : new LocalDate(params.startDate),
                     currentMemberships               : memberships,
                     baseMembership                   : cmd.baseMembership,
                     facilitiesThatReceivesMemberships: facilitiesThatReceivesMemberships,
                     purchasedAtFacility: cmd.purchasedAtFacility]

        model.typesWithDates = memberService.getAvailableMembershipTypesWithDates(model, facility, user, cmd.baseMembership)

        def facilitiesWithAvailableMembershipTypes = facilitiesThatReceivesMemberships.findAll { Facility f ->
            return model.typesWithDates.any {
                it.type?.facility == f
            }
        }

        def familyMembershipsEnabledFacilities = facilitiesWithAvailableMembershipTypes.findAll { it.familyMembershipRequestAllowed }
        model.familyMembershipsEnabledFacilities = familyMembershipsEnabledFacilities

        if (cmd.hasErrors()) {
            render(view: "index", model: model)
            return
        }

        cmd.items.each {
            // TODO Remove this when we've found NPE bug
            if (!it.membershipType) {
                log.error("membershipType is null for command $it")
            }
            Facility membershipFacility = it.membershipType.facility

            Boolean requireSecurityNumber = membershipFacility?.requireSecurityNumber
            Locale locale = new Locale(membershipFacility?.language)
            PersonalNumberSettings personalNumberSettings = membershipFacility?.getPersonalNumberSettings()

            boolean isCompany = it.type == Customer.CustomerType.ORGANIZATION
            personalNumberSettings.requireSecurityNumber = isCompany ? false : membershipFacility?.requireSecurityNumber

            if (requireSecurityNumber && !ValidationUtils.isPersonalNumberValid(
                it.birthday, it.securitynumber, isCompany, personalNumberSettings)) {
                it.errors.rejectValue("birthday", "membershipRequestCommand.security.number.matches.invalid",
                    [message(code: "membershipRequestCommand.securitynumber.format", locale: locale)] as String[], "")
            }

            if (!requireSecurityNumber && !ValidationUtils.isDateOfBirthValid(it.birthday as String, isCompany, personalNumberSettings)) {
                it.errors.rejectValue("birthday", "membershipRequestCommand.birthday.matches.invalid",
                    [message(code: "membershipRequestCommand.birthday.format", locale: locale)] as String[], "")
            }

            if (facility.isFacilityPropertyEnabled(FacilityProperty.FacilityPropertyKey.FEATURE_FEDERATION.name())) {
                if (it.type != Customer.CustomerType.ORGANIZATION && !it.club) {
                    it.errors.rejectValue("club", "membershipRequestCommand.club.validator.error", null, "")
                }
            }
        }

        boolean doFamilyMembership = false
        if (cmd.familyMembershipFacility && cmd.items.size() > 1) {
            cmd.items[1..-1].each {
                if (cmd.familyMembershipFacility != it.membershipType.facility) {
                    it.errors.reject("All memberships must match the familyMembershipFacility")
                }
            }
            doFamilyMembership = true
        }

        if (cmd.items.any { it.hasErrors() }) {
            cmd.errors.rejectValue("items", "membershipRequestCommand.items.validator.error", null, "")
            render(view: "index", model: model)
            return
        }

        def firstMembershipType = null
        if (cmd.items[0].membershipType) {
            firstMembershipType = cmd.items[0].membershipType
        }
        // TODO Remove this when we've found NPE bug
        if (!firstMembershipType) {
            log.error("firstMembershipType is null for command $cmd")
        }

        model.requestPayment = firstMembershipType.facility?.isMembershipRequestPaymentEnabled()

        def customer = customerService.getOrCreateUserCustomer(user, cmd.items[0].membershipType.facility)

        if (customerService.updateCustomer(customer, cmd.items[0])) {
            def familyMembers = [:]
            if (doFamilyMembership) {
                Facility membershipFacility = cmd.familyMembershipFacility
                cmd.items[1..-1].each {
                    def familyCustomer = customerService.getOrCreateCustomer(it, membershipFacility)
                    if (familyCustomer) {
                        familyMembers[familyCustomer.id] = it.membershipType
                    }
                }

                if (familyMembers.size() != (cmd.items.size() - 1)) {
                    render(view: "index", model: model)
                    return
                }
            }

            if (model.requestPayment) {
                model.membershipTypeToPay = firstMembershipType
                model.familyMembers = familyMembers
                render(view: "index", model: model)
            } else {
                def membership = memberService.requestMembership(
                    customer, firstMembershipType)
                memberService.sendMembershipRequestNotification(membership, customer, cmd.message)
                if (membership && familyMembers) {
                    def family = membersFamilyService.createFamily(membership)
                    familyMembers.each { cid, mtid ->
                        def m = memberService.requestMembership(
                            Customer.get(cid), MembershipType.get(mtid))
                        if (m) {
                            memberService.sendMembershipRequestNotification(m, Customer.get(cid), cmd.message)
                            membersFamilyService.addFamilyMember(m, family)
                        }
                    }
                }

                redirect(controller: "facility", action: "show", params: [name: cmd.sname])
            }
        } else {
            render(view: "index", model: model)
        }
    }

    private MembershipRequestCommand initMembershipRequestCommand(Membership m) {
        def cmd = new MembershipRequestCommand(
            baseMembership: m,
            familyMembershipFacility: params.boolean("applyForFamilyMembership", false) ? m.type.facility : null
        )
        cmd.items << initMembershipRequestItemCommand(m)
        if (m.isFamilyContact() && m.customer.facility.familyMembershipRequestAllowed) {
            m.family.membersNotContact.each {
                cmd.items << initMembershipRequestItemCommand(it)
            }
        }
        cmd
    }

    private MembershipRequestItemCommand initMembershipRequestItemCommand(Membership m) {
        new MembershipRequestItemCommand(membershipType: m.type,
            firstname: m.customer.firstname, lastname: m.customer.lastname,
            companyname: m.customer.companyname, email: m.customer.email,
            address: m.customer.address1, zipcode: m.customer.zipcode,
            city: m.customer.city, country: m.customer.country, club: m.customer.club,
            telephone: m.customer.telephone, contact: m.customer.contact,
            type: m.customer.type, securitynumber: m.customer.securityNumber,
            birthday: m.customer.dateOfBirth?.format(
                m.customer.facility.getPersonalNumberSettings().shortFormat))
    }
}

@Validateable(nullable = true)
class MembershipRequestCommand {

    String sname //Facility shortname
    String message
    boolean confirmation
    Membership baseMembership
    @BindUsing({ obj, source ->
        source['familyMembershipFacility'].toString().toInteger() > 0 ? Facility.get(source['familyMembershipFacility'].toString().toInteger()) : null
    })
    Facility familyMembershipFacility
    Facility purchasedAtFacility
    List<MembershipRequestItemCommand> items = [].withLazyDefault { new MembershipRequestItemCommand() }

    static constraints = {
        sname(nullable: false, blank: false)
        confirmation(validator: { confirmation ->
            confirmation ?: ['invalid.confirmation']
        })
        message(nullable: true, blank: true, markup: true)
        items(cascade: true)
    }
}

@Validateable(nullable = true)
class MembershipRequestItemCommand {

    MembershipType membershipType
    String firstname
    String lastname
    String companyname
    String email
    String address
    String zipcode
    String city
    String country
    String telephone
    String contact
    Customer.CustomerType type
    String birthday
    String securitynumber
    String club

    static constraints = {
        membershipType(validator: { val, obj ->
            val ? true : ['membershipRequestItemCommand.membershipTypeId.nullable']
        })
        firstname(nullable: true, markup: true, validator: { val, obj ->
            (val || obj.type == Customer.CustomerType.ORGANIZATION) ? true : ["membershipRequestCommand.firstname.blank"]
        })
        lastname(nullable: true, markup: true, validator: { val, obj ->
            (val || obj.type == Customer.CustomerType.ORGANIZATION) ? true : ["membershipRequestCommand.lastname.blank"]
        })
        companyname(nullable: true, markup: true, validator: { val, obj ->
            (val || obj.type != Customer.CustomerType.ORGANIZATION) ? true : ["membershipRequestCommand.companyname.validator.error"]
        })
        email(nullable: false, blank: false, email: true, markup: true)
        address(nullable: false, blank: false, markup: true)
        zipcode(nullable: false, blank: false, markup: true)
        type(nullable: false, blank: false, validator: { val, obj ->
            if (val != Customer.CustomerType.ORGANIZATION && obj.securitynumber?.length() == 4) {
                int controlNr
                try {
                    controlNr = Integer.parseInt(obj.securitynumber[2])
                } catch (NumberFormatException) {
                    return false
                }
                return LuhnValidator.validateType(controlNr, val) ? true : ['invalid.gender']
            }
            return true
        })
        city(nullable: false, blank: false, markup: true)
        country(nullable: false, blank: false, markup: true)
        telephone(nullable: false, blank: false, markup: true)
        contact(nullable: true)
        birthday(nullable: false, blank: false)
        securitynumber(nullable: true, markup: true)
    }
}