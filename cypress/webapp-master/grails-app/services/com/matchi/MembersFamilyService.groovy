package com.matchi

import com.matchi.membership.Membership
import com.matchi.membership.MembershipFamily
import grails.transaction.NotTransactional
import org.hibernate.criterion.CriteriaSpecification
import org.joda.time.LocalDate
import org.junit.After

class MembersFamilyService {

    static transactional = true

    MembershipFamily createFamily(Membership membership) {
        log.debug("Creating family on ${membership?.customer}")
        MembershipFamily family = new MembershipFamily()
        family.contact = membership.customer
        family.addToMembers(membership)

        membership.family = family
        family.save()
        membership.save()

        return family
    }

    def addFamilyMember(Membership membership, MembershipFamily family) {
        if (!family.members.contains(membership)) {
            family.addToMembers(membership)
            family.save()
        }

        membership.family = family
        membership.save(flush: true)

        return family
    }

    def removeFamilyMember(Membership membership) {
        def family = membership.family
        family.removeFromMembers(membership)
        membership.family = null

        if (family.members.size() > 0) {
            if (family.contact == membership.customer) {
                setFamilyContact(family)
            }

            family.save()
        } else {
            removeFamily(family)
        }
    }

    def removeFamily(MembershipFamily family) {
        def members = []
        family.members.each {
            members << it
        }

        members.each { Membership member ->
            member.family = null
            family.removeFromMembers(member)

            member.save()
        }

        family.delete()
    }

    def setFamilyContact(MembershipFamily family) {
        family.contact = family.members.iterator().next()?.customer
        family.save()
    }

    def setFamilyContact(MembershipFamily family, Customer customer) {
        family.contact = customer
        family.save()
    }

    @NotTransactional
    List<MembershipFamily> getSuggestedFamilies(Membership member) {
        def customer = member.customer
        def today = new LocalDate()

        return MembershipFamily.createCriteria().listDistinct {
            createAlias("members", "m", CriteriaSpecification.LEFT_JOIN)
            createAlias("members.customer", "c", CriteriaSpecification.LEFT_JOIN)

            eq("c.facility.id", customer.facility.id)
            ne("contact.id", customer.id)
            ne("c.archived", true)
            le("m.startDate", today)
            ge("m.endDate", today)

            or {
                if (customer.telephone) { like("c.telephone", "%${customer.telephone}%") }
                if (customer.cellphone) { like("c.cellphone", "%${customer.cellphone}%") }
                if (customer.address1)  { like("c.address1", "%${customer.address1}%")   }
                if (customer.address2)  { like("c.address1", "%${customer.address2}%")   }
                if (customer.email)     { like("c.email", "%${customer.email}%")         }
            }
        }
    }

    @NotTransactional
    List<Membership> getSuggestedFamilyMembers(Membership member) {
        def customer = member.customer
        def today = new LocalDate()

        return Membership.createCriteria().listDistinct {
            createAlias("family", "f", CriteriaSpecification.LEFT_JOIN)
            createAlias("customer", "c", CriteriaSpecification.LEFT_JOIN)

            eq("c.facility.id", customer.facility.id)
            isNull("f.id")
            ne("id", member.id)
            ne("c.archived", true)
            le("startDate", today)
            ge("endDate", today)

            or {
                if (customer.telephone) { like("c.telephone", "%${customer.telephone}%") }
                if (customer.cellphone) { like("c.cellphone", "%${customer.cellphone}%") }
                if (customer.address1)  { like("c.address1", "%${customer.address1}%")   }
                if (customer.address2)  { like("c.address2", "%${customer.address2}%")   }
                if (customer.email)     { like("c.email", "%${customer.email}%")         }
            }
        }
    }

    @NotTransactional
    List<Customer> getAllContactsOfFacility(Facility facility) {
        return MembershipFamily.createCriteria().listDistinct {
            createAlias("members", "m", CriteriaSpecification.LEFT_JOIN)
            createAlias("members.customer", "c", CriteriaSpecification.LEFT_JOIN)

            eq("c.facility.id", facility.id)

            projections {
                property("contact")
            }
        }.findAll { Customer contact ->
            Membership activeMembership = contact.getActiveMembership()
            if(activeMembership) {
                return !activeMembership.isEnding()
            }

            return false
        }
    }
}
