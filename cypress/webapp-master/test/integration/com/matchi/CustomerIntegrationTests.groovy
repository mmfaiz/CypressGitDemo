package com.matchi

import com.matchi.membership.Membership
import com.matchi.membership.MembershipFamily
import org.junit.After
import org.junit.Before

import static com.matchi.TestUtils.*

class CustomerIntegrationTests extends GroovyTestCase {

    void testBirthYearIsAdded() {

        def customer1 = createCustomer()
        customer1.dateOfBirth = Date.parse("yyMMdd", "020101")
        customer1.save(flush: true)
        assert customer1.birthyear == 2002

        def customer2 = createCustomer()
        customer2.dateOfBirth = null
        customer2.save(flush: true)
        assert !customer2.birthyear
    }

    void testUnlinkMembershipFamily() {
        def facility = createFacility()
        def customer1 = createCustomer(facility)
        createMembership(customer1)
        assert customer1.membership
        def customer2 = createCustomer(facility)
        createMembership(customer2)
        assert customer2.membership
        def mf = new MembershipFamily(contact: customer1).save(failOnError: true, flush: true)
        customer1.membership.family = mf
        customer1.save(failOnError: true, flush: true)
        customer2.membership.family = mf
        customer2.save(failOnError: true, flush: true)

        customer1.unlinkMembershipFamily()

        assert customer1.membership
        assert MembershipFamily.count() == 1
        mf = MembershipFamily.first()
        mf.contact.id == customer2.id
        assert Membership.countByFamily(mf) == 1
        assert Membership.findByFamily(mf).id == customer2.membership.id

        customer2.unlinkMembershipFamily()

        assert customer2.membership
        assert !MembershipFamily.count()
    }

    @After
    void tearDown() {
        List<MembershipFamily> families = MembershipFamily.all
        families.each { MembershipFamily family ->
            if (family.members) {
                List<Membership> members = family.members.toList()
                members.each { Membership member ->
                    if (member) {
                        family.removeFromMembers(member)
                        member.family = null
                    }
                }
            }
            family.delete(flush: true)
        }
    }
}
