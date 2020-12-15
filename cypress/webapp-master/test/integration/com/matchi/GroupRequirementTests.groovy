package com.matchi

import com.matchi.requirements.GroupRequirement

import static com.matchi.TestUtils.createCustomer
import static com.matchi.TestUtils.createFacility
import static com.matchi.TestUtils.createGroup

class GroupRequirementTests extends GroovyTestCase {

    void testCustomerInGroup() {
        Facility facility = createFacility()
        Group group = createGroup(facility)
        Customer customer = createCustomer(facility)

        CustomerGroup.link(customer, group)

        GroupRequirement groupRequirement = new GroupRequirement()
        groupRequirement.setValues([groupIds: [group.id as String], allSelectedGroups: "true"])
        customer.refresh()

        assert customer.belongsTo(group)
        assert groupRequirement.validate(customer)
    }

    void testCustomerNotInGroup() {
        Facility facility = createFacility()
        Group group = createGroup(facility)
        Customer customer = createCustomer(facility)

        GroupRequirement groupRequirement = new GroupRequirement()
        groupRequirement.setValues([groupIds: [group.id as String], allSelectedGroups: "true"])
        customer.refresh()

        assert !customer.belongsTo(group)
        assert !groupRequirement.validate(customer)
    }

    void testCustomerNotInGroupAllGroupsButPass() {
        Facility facility = createFacility()
        Group group = createGroup(facility)
        Group group2 = createGroup(facility)
        Customer customer = createCustomer(facility)

        CustomerGroup.link(customer, group)
        GroupRequirement groupRequirement = new GroupRequirement()
        groupRequirement.setValues([groupIds: [group.id as String, group2.id as String], allSelectedGroups: "false"])
        customer.refresh()

        assert customer.belongsTo(group)
        assert !customer.belongsTo(group2)
        assert groupRequirement.validate(customer)
    }

    void testCustomerNotInGroupAllGroupsNotPass() {
        Facility facility = createFacility()
        Group group = createGroup(facility)
        Group group2 = createGroup(facility)
        Customer customer = createCustomer(facility)

        CustomerGroup.link(customer, group)
        GroupRequirement groupRequirement = new GroupRequirement()
        groupRequirement.setValues([groupIds: [group.id as String, group2.id as String], allSelectedGroups: "true"])
        customer.refresh()

        assert customer.belongsTo(group)
        assert !customer.belongsTo(group2)
        assert !groupRequirement.validate(customer)
    }
}
