package com.matchi.requirements

import com.matchi.Customer
import com.matchi.Group
import com.matchi.StringHelper

class GroupRequirement extends Requirement {

    boolean allSelectedGroups = false
    static hasMany = [ groups: Group ]

    static constraints = {
        allSelectedGroups(nullable: false)
    }

    static mapping = {
        groups lazy: false
    }

    /**
     * Validates customer, by checking if in any of the groups or all of the groups depending on allSelectedGroups.
     * @param customer
     * @return
     */
    @Override
    boolean validate(Customer customer) {
        Closure closure = { Group group ->
            return customer.belongsTo(group)
        }

        return allSelectedGroups ? groups.every(closure) : groups.any(closure)
    }

    @Override
    Map getRequirementProperties() {
        return [ allSelectedGroups: allSelectedGroups, groupIds: groups.collect { it.id } ]
    }

    @Override
    void setValues(def values) {

        List<Group> selectedGroups

        if(!values.groupIds.equals(StringHelper.LIST_PLACE_HOLDER)) {
            selectedGroups = values.groupIds
                    .findAll { !it.equals(StringHelper.LIST_PLACE_HOLDER) }
                    .collect { String groupId ->
                return Group.get(Long.parseLong(groupId))
            }
        }


        this.setGroups(selectedGroups?.toSet())
        this.allSelectedGroups = Boolean.parseBoolean(values.allSelectedGroups)
    }
}
