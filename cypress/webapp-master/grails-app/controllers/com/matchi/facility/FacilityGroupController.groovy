package com.matchi.facility

import com.matchi.Customer
import com.matchi.GenericController
import com.matchi.Group
import com.matchi.price.PriceListCustomerCategory
import grails.validation.Validateable

class FacilityGroupController extends GenericController {

    def groupService
    def userService
    def customerService
    def customerCategoryService

    def index(FilterFacilityGroupsCommand cmd) {
        def facility = getUserFacility()

        if (!facility) {
            throw new IllegalStateException("No facility")
        }
        def groups = groupService.getFacilityGroups(facility, cmd)

        render(view: "index", model:  [groups: groups, facility: facility])
    }

    def create() {
        def facility = getRequiredUserFacility()
        render(view: "create", model:  [facility: facility, categories: customerCategoryService.getFacilityCustomerCategories(facility, true)])
    }

    def edit() {
        def facility = getUserFacility()
        def group = groupService.getGroup(params.id)
        if(group) {
            assertFacilityAccessTo(group)
            render(view: "edit", model: [group: group, facility: facility])
        } else {
            noGroupFound()
        }
    }

    def save() {
        def facility = getUserFacility()
        def group = new Group()
        group.properties = params

        def categoryIds = params.list("categoryIds")
        def categories = categoryIds.collect { PriceListCustomerCategory.get(it) }

        group = groupService.createGroup(group, facility)

        if(!group.hasErrors() && group.save()) {

            categories.each {
                category ->
                    def condition = category.conditions.iterator().next()
                    if(condition.name != "MemberType") {
                        condition.addToGroups(group)
                    }

            }


            redirect(action: "index")
        } else {
            log.debug("HASERRORS")
            render(view: "create", model: [group: group, facility: facility])
        }
    }

    def update() {
        def facility = getUserFacility()
        def group = groupService.getGroup(params.id)

        if(group) {
            assertFacilityAccessTo(group)
            group.properties = params

            if (!group.hasErrors() && group.save()) {
                redirect(action: "index")
            } else {
                render(view: "edit", model: [ group: group, facility: facility ])
            }
        } else {
            noGroupFound()
        }
    }

    def delete() {
        def group = groupService.getGroup(params.id)
        if(group) {
            assertFacilityAccessTo(group)
            groupService.removeGroup(group)
            redirect(action: "index")
        } else {
            noGroupFound()
        }

    }

    def customers() {
        def facility = getUserFacility()
        def group = groupService.getGroup(params.id)
        if (group) {
            def customers = Customer.createCriteria().listDistinct {
                customerGroups {
                    eq("group", group)
                }
                eq("archived", false)
            }
            render(view: "customers", model: [customers: customers, group: group, facility: facility])
        } else {
            noGroupFound()
        }

    }

    def addCustomer() {
        def group = groupService.getGroup(params.id)
        def customer = customerService.getCustomer(params.long("customerId"))

        if (group && customer) {
            groupService.addCustomerToGroup(group, customer)
            flash.message = message(code: "facilityGroup.addCustomer.message1",
                    args: [customer.email, group.name])
        }

        if(params.returnUrl && params.returnUrl.size() > 0) {
            redirect url: params.returnUrl
        } else {
            redirect(action: "customers", params: [id: group.id])
        }
    }

    def addGroupForm() {
        def customer = customerService.getCustomer(params.long("customerId"))
        def facilityGroups = Group.findAllByFacility(customer.facility,[sort: "name", order: "asc"])

        def groups = facilityGroups - customer.groups

        return [ customer: customer, groups: groups]
    }

    def removeCustomer() {
        def group = groupService.getGroup(params.id)
        def customer = customerService.getCustomer(params.long("customerId"))

        if (group && customer) {
            groupService.removeCustomerFromGroup(group, customer)
            flash.message = message(code: "facilityGroup.removeCustomer.success",
                    args: [customer.email, group.name])
        }

        if(params.returnUrl && params.returnUrl.size() > 0) {
            redirect url: params.returnUrl
        } else {
            redirect(action: "customers", params: [id: group.id])
        }
    }

    protected def noGroupFound() {
        flash.error = message(code: "facilityGroup.edit.noGroupFound")
        redirect(action: "index")
    }
}

@Validateable(nullable = true)
class FilterFacilityGroupsCommand {
    String q = ""
    String order = "asc"
    String sort  = "name"
}
