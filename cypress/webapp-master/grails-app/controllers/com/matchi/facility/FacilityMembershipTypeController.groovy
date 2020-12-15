package com.matchi.facility

import com.matchi.Facility
import com.matchi.GenericController
import com.matchi.membership.Membership
import com.matchi.membership.MembershipType
import com.matchi.price.MemberTypePriceCondition
import com.matchi.requirements.MemberTypeRequirement

class FacilityMembershipTypeController extends GenericController {
    def facilityService

    def index() {
        Facility facility = getUserFacility()
        List<MembershipType> types = MembershipType.findAllByFacility(getUserFacility(), params)

        List<Facility> globalFacilities = facilityService.getAllHierarchicalFacilities(facility, false)
        List<MembershipType> globalTypes = globalFacilities.collect {fac -> MembershipType.findAllByFacility(fac, params)}.flatten()

        [ types: types, globalTypes: globalTypes ]
    }

    def edit() {
        def type = MembershipType.get(params.id)
        Set<Facility> memberFacilities = type?.facility?.memberFacilities

        [ type:type, facility: type?.facility, memberFacilities: memberFacilities ]
    }

    def create() {
        Facility facility = getUserFacility()
        Set<Facility> memberFacilities = facility?.memberFacilities

        [facility: facility, memberFacilities: memberFacilities]
    }

    def save() {
        def type = new MembershipType(params)
        type.facility = getUserFacility()
        Set<Facility> memberFacilities = type?.facility?.memberFacilities


        if (!type.hasErrors() && type.save()) {
            flash.message = message(code: "facilityMembershipType.save.success", args: [type.name])
            redirect(action: "index")
            return
        }

        render (view: "create", model: [ type: type, memberFacilities: memberFacilities, facility: type?.facility ])
    }

    def update() {
        def type = MembershipType.get(params.id)
        Set<Facility> memberFacilities = type?.facility?.memberFacilities

        assertFacilityAccessTo(type)

        type.properties = params

        if (!type.hasErrors() && type.save()) {
            flash.message = message(code: "facilityMembershipType.update.success", args: [type.name])
            redirect(action: "index")
            return
        }

        render (view: "edit", model: [ type: type, memberFacilities: memberFacilities, facility: type?.facility])
    }

    def cancel() {
        def type = MembershipType.get(params.id)

        assertFacilityAccessTo(type)

        List<Membership> memberships = Membership.findAllByType(type)
        if (memberships.any{Membership m -> m.isAfterGracePeriod()}) {
            flash.error = message(code: "facilityMembershipType.delete.error")
            redirect(action: "index")
            return
        }

        MembershipType.withTransaction {
            memberships.each { m ->
                m.type = null
                m.save()
            }
            MemberTypePriceCondition.withCriteria {
                membershipTypes {
                    eq("id", type.id)
                }
            }.each { pc ->
                pc.removeFromMembershipTypes(type)
                pc.save()
            }

            MemberTypeRequirement.createCriteria().list {
                types {
                    eq("id", type.id)
                }
            }.each { req ->
                req.removeFromTypes(type)
                req.save()
            }

            type.delete()
        }

        flash.message = message(code: "facilityMembershipType.cancel.success")

        redirect(action: "index")
    }
}