package com.matchi.facility
import com.matchi.GenericController
import com.matchi.membership.MembershipType
import com.matchi.price.CustomerGroupPriceCondition
import com.matchi.price.MemberTypePriceCondition
import com.matchi.price.PriceListCustomerCategory

class FacilityCustomerCategoryController extends GenericController {

    def customerCategoryService
    def groupService

    def index() {
        def facility   = getRequiredUserFacility()
        def categories = customerCategoryService.getFacilityCustomerCategories(facility)

        render(view: "index", model: [facility: facility, categories: categories])
    }

    def create() {
        def facility = getRequiredUserFacility()
        def groups   = groupService.getFacilityGroups(facility, new FilterFacilityGroupsCommand())
        def types    = MembershipType.findAllByFacility(facility)
        facility.masterFacilities.each {
            types += MembershipType.findAllByFacility(it)
        }

        render(view: "create", model: [facility: facility, groups: groups, types: types])
    }

    def edit() {
        def facility = getRequiredUserFacility()
        def customerCategory = PriceListCustomerCategory.get(params.id)
        def groups = groupService.getFacilityGroups(facility, new FilterFacilityGroupsCommand())
        def types = MembershipType.findAllByFacility(facility)
        facility.masterFacilities.each {
            types += MembershipType.findAllByFacility(it)
        }

        assertFacilityAccessTo(customerCategory)

        if(customerCategory && !customerCategory.defaultCategory)  {
            def groupCondition = customerCategory.conditions.find { it instanceof CustomerGroupPriceCondition }
            def typeCondition  = customerCategory.conditions.find { it instanceof MemberTypePriceCondition }

            render(view: "edit", model: [facility: facility, category: customerCategory, groups:groups, types:types,
                                         groupCondition: groupCondition, typeCondition:typeCondition])
        } else {
            flash.error = message(code: "facilityCustomerCategory.edit.error")
            redirect(action: "index")
        }
    }

    def save() {
        def facility = getRequiredUserFacility()
        CustomerGroupPriceCondition condition
        MemberTypePriceCondition typeCondition

        if(params.groupIds && params.list("groupIds").size() > 0) {
            condition = new CustomerGroupPriceCondition()
            condition.name = "Group"
            def groupsIds = params.list("groupIds")
            def groups = groupsIds.collect { groupService.getGroup(it) }
            groups.each { condition.addToGroups(it) }
        }

        if(params.typeIds && params.list("typeIds").size() > 0) {
            typeCondition = new MemberTypePriceCondition()
            typeCondition.name = "MemberType"
            def typeIds = params.list("typeIds")
            def types = typeIds.collect { MembershipType.get(it) }
            types.each { typeCondition.addToMembershipTypes(it) }
        }

        PriceListCustomerCategory customerCategory = new PriceListCustomerCategory(defaultCategory: false)
        customerCategory.properties["name", "onlineSelect", "forceUseCategoryPrice", "daysBookable"] = params
        customerCategory.facility = facility

        if (condition) {
            log.debug("Adding customerGroupCondition")
            customerCategory.addToConditions(condition)
        }

        if (typeCondition) {
            log.debug("Adding MemberTypeCondition")
            customerCategory.addToConditions(typeCondition)
        }

        facility.addToPriceListCustomerCategories(customerCategory)
        facility.save()

        redirect(action: "index")
    }

    def update() {
        def customerCategory = PriceListCustomerCategory.get(params.id)

        log.debug("Has conditions: ${customerCategory.conditions.size()}st")

        if(customerCategory && !customerCategory.defaultCategory) {
            assertFacilityAccessTo(customerCategory)
            def groupCondition = customerCategory.conditions.find { it instanceof CustomerGroupPriceCondition }
            def typeCondition  = customerCategory.conditions.find { it instanceof MemberTypePriceCondition }

            groupCondition?.groups?.clear()
            typeCondition?.membershipTypes?.clear()

            if(params.groupIds && params.list("groupIds").size() > 0) {
                if (!groupCondition) {
                    groupCondition = new CustomerGroupPriceCondition()
                    customerCategory.addToConditions(groupCondition)
                }

                log.debug("Adding groups to groupCondition")
                def groupsIds = params.list("groupIds")
                def groups = groupsIds.collect { groupService.getGroup(it) }
                groups.each { groupCondition.addToGroups(it) }
            } else {

                if(groupCondition) {
                    log.debug("Remove groupCondition")
                    customerCategory.removeFromConditions(groupCondition)
                    groupCondition.delete()
                }

            }

            if(params.typeIds && params.list("typeIds").size() > 0) {
                if (!typeCondition) {
                    typeCondition = new MemberTypePriceCondition()
                    customerCategory.addToConditions(typeCondition)
                }

                log.debug("Adding types to membertypecondition")
                def typesIds = params.list("typeIds")
                def types = typesIds.collect { MembershipType.get(it) }
                types.each { typeCondition.addToMembershipTypes(it) }
            } else {
                if(typeCondition) {
                    log.debug("Remove typeCondition")
                    customerCategory.removeFromConditions(typeCondition)
                    typeCondition.delete()
                }
            }

            customerCategory.properties["name", "onlineSelect", "forceUseCategoryPrice", "daysBookable"] = params
            customerCategory.save()
        } else {
            flash.error = message(code: "facilityCustomerCategory.update.error")
        }

        redirect(action: "index")
    }

    def delete() {
        def customerCategory = customerCategoryService.getCustomerCategory(params.id)

        if(customerCategory && !customerCategory.defaultCategory) {
            assertFacilityAccessTo(customerCategory)

            if(customerCategory.prices.size() > 0 && !params.confirmed) {
                def pricelist = [] as Set

                customerCategory.prices.each { price ->
                    pricelist << price.priceCategory.pricelist
                }

                render(view: "confirmDelete", model: [category:  customerCategory, pricelists: pricelist])
                return
            } else {
                customerCategoryService.removeCustomerCategory(customerCategory)
                flash.message = message(code: "facilityCustomerCategory.delete.success", args: [customerCategory.name])
                redirect(action: "index")
            }

        } else {
            flash.error = message(code: "facilityCustomerCategory.delete.error")
            redirect(action: "index")
        }
    }
}
