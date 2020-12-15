package com.matchi

import com.matchi.membership.Membership
import com.matchi.membership.MembershipFamily
import com.matchi.membership.MembershipType
import com.matchi.orders.Order
import grails.validation.Validateable
import groovyx.gpars.GParsPool
import java.util.concurrent.ConcurrentHashMap
import org.hibernate.criterion.CriteriaSpecification
import org.joda.time.DateTime
import org.springframework.beans.factory.annotation.Value
import org.springframework.util.StopWatch

class ImportCustomerService {
    static transactional = false

    def cashService
    def springSecurityService
    def grailsApplication
    def customerService
    def groupService
    def slotService
    def membersFamilyService
    def memberService
    MembershipTypeService membershipTypeService

    @Value('${matchi.customer.import.batchSize}')
    Integer importBatchSize

    @Value('${matchi.customer.import.poolSize}')
    Integer importPoolSize

    /**
     * Creates CreateCustomerCommand according to the information in List<Map> data
     * @param data is map where each element contains data for a customer (number, lastname, firstname/company-name),
     * telephone, mobile, address, zipcode, city, securityNr, type, invoiceAddress, email
     * @return customer data [ command, membership ]
     */
    def parseCustomerData(List<Map> data) {
        List<Long> customerNumbers = [].asSynchronized()
        List<ImportCustomerCommand> cmds = [].asSynchronized()

        def customersData   = []
        User currentUser = (User) springSecurityService.currentUser
        PersonalNumberSettings personalNumberSettings = currentUser?.facility.getPersonalNumberSettings()
        def facilityId = currentUser?.facility?.id
        def facilityCountry = currentUser?.facility?.country

        GParsPool.withPool(importPoolSize) {
            data.collate(importBatchSize).eachParallel { batch ->
                Customer.withNewSession {
                    def facility = Facility.get(facilityId)

                    batch.each {
                        ImportCustomerCommand cmd = new ImportCustomerCommand()
                        cmd.number        = it.number.isLong() ? Long.parseLong(it.number) : null

                        boolean duplicateNumber = customerNumbers.contains(cmd.number)

                        if(cmd.number && !duplicateNumber) {
                            customerNumbers << cmd.number
                        } else if (duplicateNumber) {
                            cmd.duplicateNumber = true
                        }

                        cmd.type          = parseCustomerType(it.type.toUpperCase())
                        cmd.customerTypeString = it.type.toUpperCase()

                        if (cmd.type.toString() == Customer.CustomerType.ORGANIZATION.toString()) {
                            cmd.companyname = it.firstname
                            cmd.orgNumber = it.securityNumber

                        } else {
                            cmd.firstname     = it.firstname
                            cmd.lastname      = it.lastname

                            if(it.securityNumber) {
                                ParsedPersonalNumber parsedPersonalNumber = new ParsedPersonalNumber(it.securityNumber, personalNumberSettings)
                                cmd.securityNumber    = parsedPersonalNumber.securityNumber
                                cmd.personalNumber    = parsedPersonalNumber.dateString
                            }
                        }

                        cmd.contact           = it.contact
                        cmd.email             = ValidationUtils.removeInvalidEmailChars(it.email)
                        cmd.telephone         = it.telephone
                        cmd.cellphone         = it.cellphone
                        cmd.address1          = it.address1
                        cmd.address2          = it.address2
                        cmd.zipcode           = it.zipcode
                        cmd.city              = it.city
                        cmd.country           = it.country ?: facilityCountry
                        cmd.web               = it.web
                        cmd.invoiceAddress1   = it.invoiceAddress1
                        cmd.invoiceAddress2   = it.invoiceAddress2
                        cmd.invoiceZipcode    = it.invoiceZipcode
                        cmd.invoiceCity       = it.invoiceCity
                        cmd.invoiceContact    = it.invoiceContact
                        cmd.invoiceTelephone  = it.invoiceTelephone
                        cmd.invoiceEmail      = ValidationUtils.removeInvalidEmailChars(it.invoiceEmail)
                        cmd.notes             = it.notes
                        cmd.guardianName      = it.guardianName
                        cmd.guardianEmail     = ValidationUtils.removeInvalidEmailChars(it.guardianEmail)
                        cmd.guardianTelephone = it.guardianTelephone
                        cmd.guardianName2      = it.guardianName2
                        cmd.guardianEmail2     = ValidationUtils.removeInvalidEmailChars(it.guardianEmail2)
                        cmd.guardianTelephone2 = it.guardianTelephone2
                        cmd.membershipTypeString = it.membershiptype
                        cmd.membershipStatusString = it.membership
                        cmd.groupNames          = it.groups?.tokenize(',')
                        cmd.familyMembershipContactNumber = it.family?.isLong() ? it.family as Long : null

                        cmd.facilityId        = facilityId
                        cmd.validate()

                        if(!cmd.hasErrors()) {
                            Customer existingCustomer = findExistingCustomer(facility, cmd)
                            if(existingCustomer) {
                                cmd.willBeUpdated = true
                                cmd.validate()
                            }
                        }

                        cmds << cmd
                    }
                }
            }
        }

        /**
         * Below we check the family id, which is the customer number of a contact person.
         * If the id does not exist or belongs to a customer in the import file that is not valid, the
         * family id will be invalid. Also, the contact must not already be a family non-contact member.
         */

        List<Customer> alreadyContacts = membersFamilyService.getAllContactsOfFacility(currentUser.facility)

        // Get current customer numbers of facility for potential contacts
        List<Long> alreadyContactsNumbers = alreadyContacts*.number
        List<Long> currentExistingCustomerNumbers = Customer.findAllByFacility(currentUser.facility)*.number
        List<Long> customerNumbersToBeAdded = []

        // Add customer numbers from valid import rows, to the list of potential contact persons
        cmds.each { ImportCustomerCommand cmd ->

            // If import row is accepted, is not already added, will not be updated, and has no connection to other contact, it is OK
            if(!cmd.hasErrors() && !cmd.willBeUpdated && !currentExistingCustomerNumbers.contains(cmd.number) && !cmd.familyMembershipContactNumber) {
                customerNumbersToBeAdded << cmd.number
            }
        }

        // Now we check the pointing ones to
        cmds.sort {it.number}.each { ImportCustomerCommand cmd ->
            if(cmd.familyMembershipContactNumber) {

                // Checking if we can connect to suggested family contact
                // Cannot point to itself
                if((!currentExistingCustomerNumbers.contains(cmd.familyMembershipContactNumber) &&
                        !customerNumbersToBeAdded.contains(cmd.familyMembershipContactNumber)) || alreadyContactsNumbers.contains(cmd.number)) {
                    cmd.pointsToInvalidFamilyContact = true
                    cmd.validate()
                }

            }

            customersData << [ cmd: cmd ]
        }

        return customersData
    }

    /**
     * Add customers to facility and to new group created for all imported
     * @param customerData map with customers data
     * @return Created customers
     */
    def importCustomers(def customerData) {
        User currentUser = (User)springSecurityService.currentUser
        Facility facility = currentUser?.facility
        Group group = new Group(name: "Importerade ${new DateTime().toString("yyyy-MM-dd HH:mm")}")
        def existing = [].asSynchronized()
        def imported = [].asSynchronized()
        def families = new ConcurrentHashMap()
        def groups = new ConcurrentHashMap()

        def stopWatch = new StopWatch("Import customers ${group.name}")

        log.info("Starting import of ${customerData?.size()} customers on ${facility.name}")
        stopWatch.start()

        if (customerData && facility) {
            groupService.createGroup(group, facility)

            def currentUserId = currentUser.id
            def facilityId = facility.id
            def groupId = group.id
            GParsPool.withPool(importPoolSize) {
                customerData.collate(importBatchSize).eachParallel { batch ->
                    Customer.withNewSession {
                        def cu = User.get(currentUserId)
                        def f = Facility.get(facilityId)
                        def g = Group.get(groupId)
                        batch.each { params ->
                            ImportCustomerCommand cmd = params.cmd
                            if (!cmd.hasErrors()) {
                                Customer customer
                                // If same customer number, we will overwrite
                                Customer existingCustomer = findExistingCustomer(f, cmd)
                                if (!existingCustomer) {
                                    customer = customerService.createCustomer(cmd)
                                } else {
                                    UpdateCustomerCommand ucmd = createUpdateCustomerCommand(cmd, f)
                                    customer = customerService.updateCustomer(existingCustomer, ucmd)
                                }
                                if (!customer.hasErrors() && customer.save()) {
                                    def addedMembership

                                    if ((cmd.membershipStatusString || cmd.membershipTypeString)
                                            && !customer.hasMembership() && !cmd.willBeUpdated) {
                                        addedMembership = addMembership(cu, customer,
                                                cmd.membershipStatusString, cmd.membershipTypeString)
                                    }



                                    if (!existingCustomer) {
                                        imported << customer.id
                                        //only new customers fall to this group. Updated customers are not
                                        groupService.addCustomerToGroup(g, customer)
                                    } else {
                                        existing << customer.id
                                    }
                                    cmd.groupNames.each { def groupName ->
                                        def gn = groupName.trim()
                                        groups.putIfAbsent(gn, [].asSynchronized())
                                        groups[gn] << customer.id
                                    }

                                    if (cmd.familyMembershipContactNumber && addedMembership && !cmd.willBeUpdated) {
                                        families.putIfAbsent(cmd.familyMembershipContactNumber, [].asSynchronized())
                                        families[cmd.familyMembershipContactNumber] << addedMembership.id
                                    }
                                }
                            } else {//!cmd.hasErrors()
                                log.debug "ERRORS >>"
                                cmd.errors.each { error ->
                                    log.debug "* ${error}"
                                }
                            }
                        }
                    }
                }
            }

            if (groups.size() > 0) {
                log.debug("Adding groups ${groups.size()}")
                addGroups(groups, facility)
            }

            if (families.size() > 0) {
                log.debug("Adding families ${families.size()}")
                addFamilies(families, facility)
            }

            // This removes temporary group if nothing imported.
            // But what if users were only updated and not imported group should still exist
            if (!imported) {
                groupService.removeGroup(group)
            }
        }
        stopWatch.stop()

        log.info("Customers import finished with ${imported.size()} new and ${existing.size()} old customers in ${stopWatch.totalTimeSeconds} sec (${(imported.size() + existing.size()) / stopWatch.totalTimeSeconds} customers/sec)")
        return [ imported: imported, existing:existing, group: group ]
    }

    private void addGroups(Map<String, List<Long>> groups, Facility facility) {
        groups.each { groupNameAndCustomers ->
            def group = Group.findByNameAndFacility(groupNameAndCustomers.key, facility)

            if(!group){
                group = groupService.createGroup(groupNameAndCustomers.key, facility)
            }

            if (group) {
                groupNameAndCustomers.value.each { Long customerId ->
                    groupService.addCustomerToGroup(group, Customer.get(customerId))
                }
            }
        }
    }

    private def addFamilies(Map<Long, List<Long>> families, Facility facility) {
        families.each { def f ->
            def familyContact = Customer.findByNumberAndFacility(f.key, facility)
            if (familyContact?.membership) {
                def family = familyContact.membership.family ?:
                        membersFamilyService.createFamily(familyContact.membership)

                f.value.each { Long membershipId ->
                    log.debug("Adding membership $membershipId to ${familyContact} family")
                    membersFamilyService.addFamilyMember(Membership.get(membershipId), family)
                }
            }
        }
    }

    private Membership addMembership(User currentUser, Customer customer,
            String status, String membershiptype) {
        def type
        if (membershiptype) {
            type = MembershipType.findByNameAndFacility(membershiptype, customer.facility)
            if(!type) {
                type = membershipTypeService.createMembershipType(membershiptype, customer.facility)
            }
        }

        def membership = memberService.addMembership(customer, type, currentUser,
                !(status in Membership.Status.listPendingStatuses()), Order.ORIGIN_FACILITY)
        if (membership && status in Membership.Status.listActiveStatuses()) {
            if (membership.order.total()) {
                cashService.createCashOrderPayment(membership.order)
            }
            if (status == Membership.Status.CANCEL.name()) {
                memberService.disableAutoRenewal(membership)
            }
        }

        membership
    }

    private Customer findExistingCustomer(Facility facility, ImportCustomerCommand cmd) {
        return Customer.findByFacilityAndNumber(facility, cmd.number)
    }

    private def createUpdateCustomerCommand(CreateCustomerCommand c, Facility facility) {
        UpdateCustomerCommand cmd = new UpdateCustomerCommand()
        cmd.facilityId     = facility.id
        cmd.number         = c.number
        cmd.type           = c.type ?: null
        cmd.email          = c.email
        cmd.firstname      = c.firstname
        cmd.lastname       = c.lastname
        cmd.companyname    = c.companyname
        cmd.contact        = c.contact
        cmd.address1       = c.address1
        cmd.address2       = c.address2
        cmd.zipcode        = c.zipcode
        cmd.city           = c.city
        cmd.country        = c.country
        cmd.telephone      = c.telephone
        cmd.cellphone      = c.cellphone
        cmd.notes          = c.notes

        cmd.securityNumber   = c.securityNumber

        cmd.invoiceAddress1  = c.invoiceAddress1
        cmd.invoiceAddress2  = c.invoiceAddress2
        cmd.invoiceCity      = c.invoiceCity
        cmd.invoiceZipcode   = c.invoiceZipcode
        cmd.invoiceContact   = c.invoiceContact
        cmd.invoiceTelephone = c.invoiceTelephone
        cmd.invoiceEmail     = c.invoiceEmail
        cmd.web              = c.web

        return cmd
    }

    private def parseCustomerType(def type) {
        switch (type) {
            case "MAN":
                return Customer.CustomerType.MALE
                break
            case "MALE":
                return Customer.CustomerType.MALE
                break
            case "M":
                return Customer.CustomerType.MALE
                break
            case "KVINNA":
                return Customer.CustomerType.FEMALE
                break
            case "FEMALE":
                return Customer.CustomerType.FEMALE
                break
            case "K":
                return Customer.CustomerType.FEMALE
                break
            case "FÖRETAG":
                return Customer.CustomerType.ORGANIZATION
                break
            case "COMPANY":
                return Customer.CustomerType.ORGANIZATION
                break
            case "F":
                return Customer.CustomerType.ORGANIZATION
                break
            default:
                return null
                break
        }
    }
}