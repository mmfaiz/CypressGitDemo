package com.matchi.facility

import com.matchi.Customer
import com.matchi.GenericController
import com.matchi.membership.Membership
import com.matchi.membership.MembershipFamily
import grails.converters.JSON
import grails.validation.Validateable
import org.hibernate.criterion.CriteriaSpecification

class FacilityCustomerFamilyController extends GenericController {

    def membersFamilyService
    def customerService

    def create() {
        def member = Membership.createCriteria().get {
            createAlias("family", "f", CriteriaSpecification.LEFT_JOIN)
            createAlias("customer", "c", CriteriaSpecification.LEFT_JOIN)

            eq("id", params.long("memberId"))
        }

        if (params.long("familyId")) {
            def family = MembershipFamily.get(params.long("familyId"))

            membersFamilyService.addFamilyMember(member, family)
            flash.message = message(code: "facilityCustomerFamily.create.addedToFamily",
                    args: [member.customer.fullName()])
        } else {
            membersFamilyService.createFamily(member)
            flash.message = message(code: "facilityCustomerFamily.create.familyCreated")
        }

        redirect(controller: "facilityCustomer", action: "show", params: [id: member.customer.id])
    }

    def add(AddMembersCommand cmd) {
        def member = Membership.createCriteria().get {
            createAlias("family", "f", CriteriaSpecification.LEFT_JOIN)
            createAlias("customer", "c", CriteriaSpecification.LEFT_JOIN)

            eq("id", params.long("memberId"))
        }

        def family = member?.family

        if (member.family && cmd.addMemberId.size() > 0) {
            def membersToAdd = Membership.where { inList("id", cmd.addMemberId) }

            membersToAdd.each {
                membersFamilyService.addFamilyMember(it, family)
            }

            flash.message = message(code: "facilityCustomerFamily.add.success")
        }

        redirect(controller: "facilityCustomer", action: "show", params: [id: member.customer.id])
    }
    def edit() {
        def family = MembershipFamily.get(params.long("familyId"))
        def member = Membership.get(params.long("memberId"))
        def contact = Customer.get(params.long("contactId"))

        membersFamilyService.setFamilyContact(family, contact)

        flash.message = message(code: "facilityCustomerFamily.edit.success")

        redirect(controller: "facilityCustomer", action: "show", params: [id: member.customer.id])
    }
    def remove() {
        def family = MembershipFamily.get(params.long("familyId"))

        membersFamilyService.removeFamily(family)
        flash.message = message(code: "facilityCustomerFamily.remove.success")

        redirect(controller: "facilityCustomer", action: "show", params: [id: params.customerId])
    }
    def familyForm() {
        def member = Customer.get(params.customerId).membership

        def suggestedFamilies = membersFamilyService.getSuggestedFamilies(member)

        [ member: member, suggestedFamilies: suggestedFamilies ]
    }
    def familyAddMembersForm() {
        def member = Customer.get(params.customerId).membership

        def suggestedFamilyMembers = membersFamilyService.getSuggestedFamilyMembers(member)

        [ member: member, suggestedFamilyMembers: suggestedFamilyMembers ]
    }

    def familyEditForm() {
        [ member: Customer.get(params.customerId).membership ]
    }

    def addToFamily() {
        def memberToAdd = Membership.get(params.long("memberId"))
        def family = MembershipFamily.get(params.long("familyId"))

        if (memberToAdd) {
            membersFamilyService.addFamilyMember(memberToAdd, family)
        }

        flash.message = message(code: "facilityCustomerFamily.addToFamily.success")
        redirect(controller: "facilityCustomer", action: "show", params: [id: memberToAdd.customer.id])
    }

    def removeFromFamily() {
        log.debug("${params}")

        def memberToRemove = Membership.get(params.long("memberId"))

        if (memberToRemove) {
            membersFamilyService.removeFamilyMember(memberToRemove)
        }

        flash.message = message(code: "facilityCustomerFamily.removeFromFamily.success")
        redirect(controller: "facilityCustomer", action: "show", params: [id: params.customerId])
    }

    def familyMember() {
        render(view: "familyMemberInfo", model: [customer: customerService.getCustomer(params.long('customerId'))])
    }

    def remoteGetSuggestedFamilies() {
        def member = Membership.get(params.long("memberId"))
        def result = []
        if (!member) {
            render { suggested: 0 } as JSON
            return
        }

        def suggestedFamilies = membersFamilyService.getSuggestedFamilies(member)
        result << [ suggested: suggestedFamilies.size() ]

        render result as JSON
    }

    def remoteGetSuggestedFamilyMembers() {
        def member = Membership.get(params.long("memberId"))
        def result = []
        if (!member) {
            render { suggested: 0 } as JSON
            return
        }

        def suggestedFamilyMembers = membersFamilyService.getSuggestedFamilyMembers(member)
        result << [suggested: suggestedFamilyMembers.size()]

        render result as JSON
    }
}

@Validateable(nullable = true)
class AddMembersCommand {
    List<Long> addMemberId
}
