package com.matchi

import grails.plugin.springsecurity.SpringSecurityUtils

import static com.matchi.TestUtils.*

class SecurityServiceIntegrationTests extends GroovyTestCase {

    def securityService
    def springSecurityService

    void testGetUserFacility() {
        assert !securityService.getUserFacility()

        def user = createAndAuthenticateUser()

        assert securityService.getUserFacility() == user.facility
    }

    void testHasFacilityAccessRights() {
        def user = createAndAuthenticateUser()
        def fu = new FacilityUser(user: user)
                .addToFacilityRoles(new FacilityUserRole(accessRight: FacilityUserRole.AccessRight.CUSTOMER))
                .addToFacilityRoles(new FacilityUserRole(accessRight: FacilityUserRole.AccessRight.INVOICE))
        fu.facility = user.facility
        fu.save(failOnError: true, flush: true)

        assert securityService.hasFacilityAccessRights([FacilityUserRole.AccessRight.CUSTOMER])
        assert securityService.hasFacilityAccessRights([FacilityUserRole.AccessRight.INVOICE])
        assert securityService.hasFacilityAccessRights(
                [FacilityUserRole.AccessRight.CUSTOMER, FacilityUserRole.AccessRight.INVOICE])
        assert securityService.hasFacilityAccessRights(
                [FacilityUserRole.AccessRight.CUSTOMER, FacilityUserRole.AccessRight.SCHEDULE])
        assert !securityService.hasFacilityAccessRights([FacilityUserRole.AccessRight.SCHEDULE])
    }

    void testListFacilityAccessRights() {
        def user = createAndAuthenticateUser()
        def fu = new FacilityUser(user: user)
                .addToFacilityRoles(new FacilityUserRole(accessRight: FacilityUserRole.AccessRight.CUSTOMER))
                .addToFacilityRoles(new FacilityUserRole(accessRight: FacilityUserRole.AccessRight.INVOICE))
        fu.facility = user.facility
        fu.save(failOnError: true, flush: true)

        def result = securityService.listFacilityAccessRights()

        assert result
        assert 2 == result.size()
        assert result.contains(FacilityUserRole.AccessRight.CUSTOMER)
        assert result.contains(FacilityUserRole.AccessRight.INVOICE)
    }

    void testUserHasFacilityAccessTo() {
        Facility facility = createFacility()

        // No user authenticated
        assert !securityService.hasFacilityAccessTo(null)
        assert !securityService.hasFacilityAccessTo(facility)

        User user = createAndAuthenticateUser()
        // User authenticated
        assert !securityService.hasFacilityAccessTo(null)
        assert !securityService.hasFacilityAccessTo(facility)

        // User is admin
        UserRole role = new UserRole(user: user, role: Role.findByAuthority("ROLE_ADMIN")).save(flush: true, failOnError: true)
        springSecurityService.reauthenticate user.email
        assert !securityService.hasFacilityAccessTo(null)
        assert securityService.hasFacilityAccessTo(facility)

        // User is just a user again
        role.delete(flush: true)
        springSecurityService.reauthenticate user.email
        assert !securityService.hasFacilityAccessTo(facility)

        // Adding necessary role, should not be enough for access
        role = new UserRole(user: user, role: Role.findByAuthority("ROLE_USER")).save(flush: true, failOnError: true)
        springSecurityService.reauthenticate user.email
        assert !securityService.hasFacilityAccessTo(facility)

        // Adding facility user on user's own facility, not enough
        FacilityUser fu = new FacilityUser(user: user)
        fu.addToFacilityRoles(new FacilityUserRole(accessRight: FacilityUserRole.AccessRight.CUSTOMER))
        fu.facility = user.facility
        fu.save(flush: true, failOnError: true)
        springSecurityService.reauthenticate user.email
        assert !securityService.hasFacilityAccessTo(facility)
        assert securityService.hasFacilityAccessTo(user.facility)

        // Adding facility user on the targeted facility, enough!
        FacilityUser fu2 = new FacilityUser(user: user)
        fu2.addToFacilityRoles(new FacilityUserRole(accessRight: FacilityUserRole.AccessRight.CUSTOMER))
        fu2.facility = facility
        fu2.save(flush: true, failOnError: true)
        springSecurityService.reauthenticate user.email
        assert securityService.hasFacilityAccessTo(facility)
        assert securityService.hasFacilityAccessTo(user.facility)

        // But without the role, it wont work!!
        role.delete(flush: true)
        springSecurityService.reauthenticate user.email
        assert !securityService.hasFacilityAccessTo(facility)
        assert !securityService.hasFacilityAccessTo(user.facility)
    }


    private User createAndAuthenticateUser() {
        def user = createUser()
        user.facility = createFacility()
        user.save(failOnError: true, flush: true)
        springSecurityService.reauthenticate user.email
        user
    }
}
