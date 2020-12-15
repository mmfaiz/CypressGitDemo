package com.matchi

import com.matchi.membership.Membership
import com.matchi.membership.MembershipType
import grails.util.Holders
import grails.validation.Validateable
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.joda.time.LocalDate

@Validateable(nullable = true)
abstract class CustomerCommand implements Serializable {
    Long id
    Long facilityId
    Long number
    Customer.CustomerType type
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
    String club
    String guardianName
    String guardianEmail
    String guardianTelephone
    String guardianName2
    String guardianEmail2
    String guardianTelephone2
    String invoiceAddress1
    String invoiceAddress2
    String invoiceCity
    String invoiceZipcode
    String invoiceContact
    String invoiceTelephone
    String invoiceEmail
    String web
    String notes
    String accessCode
    String vatNumber

    String personalNumber
    String securityNumber
    String orgNumber

    Long userId

    Boolean clubMessagesDisabled
    Boolean exludeFromNumberOfBookingsRule

    List<Long> groupId

    static constraints = {
        groupId(nullable: true, validator: { groupIds, obj ->
            Facility facility = Facility.get(obj.properties['facilityId'])

            List<Long> allGroups = Group.findAllByFacility(facility).inject(new ArrayList<Long>()) { List<Long> list, group ->
                list.add(group.id)
                list
            }

            if (groupIds.any { !allGroups.contains(it) }) {
                return false
            }

            return true
        })
        email(nullable: true, email: true)
        number(nullable: false, blank: false, min: 1l, validator: { number, obj ->
            return Customer.withCriteria {
                if (obj.id) {
                    ne("id", obj.id)
                }
                eq("facility", Facility.get(obj.properties['facilityId']))
                eq("number", number)
            }.empty ? true : ['customerNumber.notUnique']
        })
        firstname(nullable: true, validator: { firstname, obj ->
            if (firstname || obj.properties['type'] == Customer.CustomerType.ORGANIZATION) {
                return true
            }
            ['updateCustomerCommand.firstname.nullable']
        })
        lastname(nullable: true, validator: { lastname, obj ->
            if (lastname || obj.properties['type'] == Customer.CustomerType.ORGANIZATION) {
                return true
            }
            ['updateCustomerCommand.lastname.nullable']
        })
        companyname(nullable: true, validator: { companyname, obj ->
            if (companyname || obj.properties['type'] != Customer.CustomerType.ORGANIZATION) {
                return true
            }
            ['updateCustomerCommand.companyname.nullable']
        })
        facilityId(nullable: false)
        guardianEmail(nullable: true, email: true)
        guardianEmail2(nullable: true, email: true)
        notes(nullable: true, maxSize: 2000)
        accessCode(nullable: true, maxSize: 255)
        clubMessagesDisabled(nullable: true)
        exludeFromNumberOfBookingsRule(nullable: true)
        vatNumber(nullable: true)

        personalNumber(nullable:true, validator: { personalNumber, obj ->
            if (!personalNumber || obj.properties['type'] == Customer.CustomerType.ORGANIZATION) {
                return true
            }

            Facility facility = Facility.get(obj.properties['facilityId'])
            PersonalNumberSettings personalNumberSettings = facility.getPersonalNumberSettings()

            if (!ValidationUtils.isDateOfBirthValid(personalNumber, false, personalNumberSettings)) {
                ["createCustomerCommand.personalNumber.invalid", personalNumberSettings.shortFormat]
            }
        })

        securityNumber(nullable: true, validator: { securityNumber, obj ->
            if (!securityNumber || obj.properties['type'] == Customer.CustomerType.ORGANIZATION) {
                return true
            }

            Facility facility = Facility.get(obj.properties['facilityId'])
            if(!facility) return false
            PersonalNumberSettings personalNumberSettings = facility.getPersonalNumberSettings()

            if (!obj.properties['personalNumber']) {
                return ["createCustomerCommand.personalNumber.invalid", personalNumberSettings.shortFormat]
            }

            if (!ValidationUtils.isPersonalNumberValid(obj.properties['personalNumber'], securityNumber, false, personalNumberSettings)) {
                ["createCustomerCommand.securityNumber.invalid", personalNumberSettings.securityNumberLength]
            }
        })

        orgNumber(nullable: true, validator: { orgNumber, obj ->
            if (!orgNumber || obj.properties['type'] != Customer.CustomerType.ORGANIZATION) {
                return true
            }

            Facility facility = Facility.get(obj.properties['facilityId'])
            PersonalNumberSettings personalNumberSettings = facility.getPersonalNumberSettings()

            if (!ValidationUtils.isOrgNumberValid(orgNumber, personalNumberSettings)) {
                ["createCustomerCommand.orgNumber.invalid", personalNumberSettings.orgFormat]
            }
        })
    }

    def propertyMissing(String name, value) {
        // do nothing (just to support "new UpdateCustomerCommand(customer.properties)")
    }
}

@Validateable(nullable = true)
class CreateCustomerCommand extends CustomerCommand {
    Boolean createMembership
    MembershipType membershipType
    LocalDate startDate
    LocalDate endDate
    LocalDate gracePeriodEndDate
    Integer startingGracePeriodDays
    Boolean membershipPaid
    Boolean membershipCancel

    static constraints = {
        importFrom CustomerCommand

        createMembership(nullable: true)
        membershipType(nullable: true, validator: { val, obj -> val || !obj.createMembership})
        startDate(nullable: true, validator: { val, obj -> val || !obj.createMembership})
        endDate(nullable: true, validator: { val, obj ->
            (val && val >= new LocalDate() && val >= obj.startDate) || !obj.createMembership
        })
        gracePeriodEndDate(nullable: true, validator: { val, obj ->
            (val && val >= obj.endDate) || !obj.createMembership
        })
        startingGracePeriodDays nullable: true, min: 1
        membershipPaid(nullable: true)
        membershipCancel(nullable: true)
    }
}

@Validateable(nullable = true)
class UpdateCustomerCommand extends CustomerCommand {
    Boolean createMembership
    MembershipType membershipType
    LocalDate startDate
    LocalDate endDate
    LocalDate gracePeriodEndDate
    Integer startingGracePeriodDays
    Boolean membershipPaid
    Boolean membershipCancel

    static constraints = {
        importFrom CustomerCommand
    }
}

@Validateable(nullable = true)
class ImportCustomerCommand extends CreateCustomerCommand {
    String membershipTypeString
    String membershipStatusString
    boolean willBeUpdated = false
    List<String> groupNames = []
    String customerTypeString
    Long familyMembershipContactNumber

    boolean pointsToInvalidFamilyContact = false
    boolean duplicateNumber = false

    static constraints = {
        importFrom CustomerCommand
        country(nullable: true, validator: { country, obj ->
            GrailsApplication grailsApplication = Holders.grailsApplication
            if(!(country in grailsApplication.config.matchi.settings.available.countries)) {
                return [ 'importCustomerCommand.country.invalid' ]
            }

            return true
        })
        facilityId(nullable: false, validator: {
            return true
        })
        membershipTypeString(nullable: true, validator: { membershipTypeString, obj ->
            if(!membershipTypeString) return true

            boolean willBeUpdated = obj.properties['willBeUpdated'] as boolean

            if(willBeUpdated) {
                return [ 'importCustomerCommand.cannotUpdateWithMemberships' ]
            }

            Facility facility = Facility.get(obj.properties['facilityId'])
            if(!facility) return false

            MembershipType membershipType = MembershipType.findByNameAndFacility(membershipTypeString, facility)
            if(!membershipType) {
                return [ 'importCustomerCommand.membershipType.missing' ]
            }

            if(!obj.properties['membershipStatusString']) {
                return [ 'importCustomerCommand.membershipStatus.missing' ]
            }

            return true
        })
        membershipStatusString(nullable: true, validator: { membershipStatusString, obj ->
            if(!membershipStatusString) return true

            boolean willBeUpdated = obj.properties['willBeUpdated'] as boolean

            if(willBeUpdated) {
                return [ 'importCustomerCommand.cannotUpdateWithMemberships' ]
            }

            if(!Membership.Status.list().collect { it.name() }.contains(membershipStatusString)) {
                return [ 'importCustomerCommand.membershipStatus.invalid' ]
            }

            if(!obj.properties['membershipTypeString']) {
                return [ 'importCustomerCommand.membershipType.missing' ]
            }

            return true
        })
        groupNames(nullable: true, validator: { groupNames, obj ->
            if (!groupNames || groupNames.size() == 0) return true
            Facility facility = Facility.get(obj.properties['facilityId'])
            if(!facility) return false

            List<String> facilityGroupNames = Group.findAllByFacility(facility).collect { Group g -> g.name.toLowerCase().trim() }

            if(groupNames.any { String groupName -> !facilityGroupNames.contains(groupName.toLowerCase().trim()) }) {
                return [ 'importCustomerCommand.membershipType.missing' ]
            }

            return true
        })
        web(nullable: true, url: true)
        customerTypeString(nullable: true, validator: { customerTypeString, obj ->
            if(!customerTypeString) return true

            List<String> okTypes = ["MAN", "MALE", "M", "KVINNA", "FEMALE", "K", "FÖRETAG", "COMPANY", "F"]

            if(!okTypes.contains(customerTypeString)) {
                return [ 'importCustomerCommand.customerType.invalid' ]
            }

        })
        familyMembershipContactNumber(nullable: true, validator: { familyMembershipContactNumber, obj ->
            if(!familyMembershipContactNumber) return true

            boolean willBeUpdated = obj.properties['willBeUpdated'] as boolean

            if(willBeUpdated) {
                return [ 'importCustomerCommand.cannotUpdateWithMemberships' ]
            }

            if(!obj.properties['membershipStatusString']) {
                return [ 'importCustomerCommand.membershipStatus.missing' ]
            }

            if(!obj.properties['membershipTypeString']) {
                return [ 'importCustomerCommand.membershipType.missing' ]
            }

            return true
        })
        number(nullable: false, blank: false, min: 1l, validator: { number, obj ->
            return true //no validation for uniqness here. validation happens at create/update commands
        })
    }
}