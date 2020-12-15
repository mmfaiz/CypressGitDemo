package com.matchi

import com.matchi.facility.FilterFacilityGroupsCommand
import com.matchi.price.CustomerGroupPriceCondition
import com.matchi.requirements.GroupRequirement
import grails.transaction.Transactional

class GroupService {
    static transactional = false

    def getGroup(def id) {
        return Group.findById(id)
    }

    def getFacilityGroups(Facility facility, FilterFacilityGroupsCommand cmd) {
        def groups = Group.withCriteria {
            eq("facility", facility)
            if(cmd.q) {
                or {
                    like("name", "%${cmd.q}%")
                    like("description", "%${cmd.q}%")
                }
            }

            order(cmd.sort, cmd.order)
        }

        return groups
    }

    @Transactional
    Group createGroup(Group group, Facility facility) {
        log.info("Creating group ${group?.name} on facility ${facility?.name}")
        group.facility = facility
        group.save()
        return group
    }

    Group createGroup(String name, Facility facility) {
        Group group = new Group(name: name)
        return createGroup(group, facility)
    }

    def addCustomerToGroup(Group group, Customer customer) {
        log.info("Adding customer ${customer.number} to group ${group.name}")
        CustomerGroup.link(customer, group)
    }

    def removeCustomerFromGroup(Group group, Customer customer) {
        log.info("Removing customer ${customer.email} from group ${group.name}")
        CustomerGroup.unlink(customer, group)
    }

    def removeFromGroupPriceConditions(Group group) {
        def customerGroupPriceConditions = CustomerGroupPriceCondition.createCriteria().list {
            groups { eq("id", group.id) }
        }
        customerGroupPriceConditions.each { it.removeFromGroups(group) }
    }

    def removefromGroupRequirements(Group group) {
        List<GroupRequirement> groupRequirements = GroupRequirement.createCriteria().list {
            groups {
                eq ("id", group.id)
            }
        }

        groupRequirements.each { it.removeFromGroups(group) }
    }

    @Transactional
    def removeGroup(Group group) {
        log.info("Removing group ${group.name}")

        removeFromGroupPriceConditions(group)
        removefromGroupRequirements(group)

        group.delete()
    }


}
