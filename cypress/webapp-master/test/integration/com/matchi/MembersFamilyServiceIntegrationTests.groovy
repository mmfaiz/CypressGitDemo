package com.matchi

import com.matchi.membership.Membership
import com.matchi.membership.MembershipFamily
import org.joda.time.LocalDate
import org.junit.After

import static com.matchi.TestUtils.createCustomer
import static com.matchi.TestUtils.createFacility
import static com.matchi.TestUtils.createMembership

class MembersFamilyServiceIntegrationTests extends GroovyTestCase {

    MembersFamilyService membersFamilyService

    void testGetAllContactsOfFacility() {
        Customer c = createCustomer()
        Membership m = createMembership(c)

        MembershipFamily family = membersFamilyService.createFamily(m)
        membersFamilyService.addFamilyMember(m, family)

        assert membersFamilyService.getAllContactsOfFacility(c.facility) == [c]

        Customer c2 = createCustomer(c.facility)
        Membership m2 = createMembership(c2)
        membersFamilyService.addFamilyMember(m2, family)

        assert membersFamilyService.getAllContactsOfFacility(c.facility) == [c]

        Customer c3 = createCustomer(c.facility)
        Membership m3 = createMembership(c3)

        MembershipFamily family2 = membersFamilyService.createFamily(m3)
        membersFamilyService.addFamilyMember(m3, family2)

        assert membersFamilyService.getAllContactsOfFacility(c.facility) == [c, c3]

        Facility f2 = createFacility()
        Customer c4 = createCustomer(f2)
        Membership m4 = createMembership(c4)

        MembershipFamily family3 = membersFamilyService.createFamily(m4)
        membersFamilyService.addFamilyMember(m4, family3)

        assert membersFamilyService.getAllContactsOfFacility(c.facility) == [c, c3]
        assert membersFamilyService.getAllContactsOfFacility(f2) == [c4]

        m4.startDate = new LocalDate().minusYears(1)
        m4.endDate = new LocalDate().minusDays(1)
        m4.save(flush: true, failOnError: true)

        assert membersFamilyService.getAllContactsOfFacility(c.facility) == [c, c3]
        assert !membersFamilyService.getAllContactsOfFacility(f2)
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
