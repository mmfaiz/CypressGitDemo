package com.matchi

import com.matchi.async.ScheduledTaskService
import com.matchi.orders.Order
import com.matchi.orders.OrderPayment
import com.matchi.sportprofile.SportProfile
import grails.plugin.springsecurity.SpringSecurityService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.springframework.context.MessageSource
import org.springframework.mock.web.MockMultipartFile
import org.springframework.web.multipart.MultipartFile
import com.matchi.User

import static com.matchi.TestUtils.*

@TestFor(UserService)
@Mock([ Role, User, UserRole, Facility, Municipality, Region, FacilityUser, FacilityUserRole,
        Order, OrderPayment, Sport, SportProfile, Customer, UserMessage, UserFavorite ])
class UserServiceTests {
    static DEFAULT_ENCODED_PASSWORD = "hejsanhoppsan"

    User userWithNoRoles
    Role adminRole
    Role userRole

    def mockMessageSource
    def mockScheduledTaskService

    @Before
    void setUp() {
        def mockSecurityServiceControl = mockFor(SpringSecurityService)

        mockSecurityServiceControl.demand.encodePassword(1..10) { String password ->
            return DEFAULT_ENCODED_PASSWORD
        }

        service.springSecurityService = mockSecurityServiceControl.createMock()

        adminRole = new Role(authority: "ROLE_ADMIN").save(failOnError: true)
        userRole = new Role(authority: "ROLE_USER").save(failOnError: true)
        userWithNoRoles = new User(email: 'user3@mail.com').save(validate: false)

        mockMessageSource = mockFor(MessageSource)
        service.messageSource = mockMessageSource.createMock()

        mockScheduledTaskService = mockFor(ScheduledTaskService)
        service.scheduledTaskService = mockScheduledTaskService.createMock()
    }

    @After
    void tearDown() {

    }

    @Test
    void testThatRegistrationMailIsSentOnFacebookRegistration() {
        def profile = createMockFacebookProfile()

        def notificationServiceMockControl = mockFor(NotificationService)
        notificationServiceMockControl.demand.sendRegistrationConfirmationMail(1..1) { User user ->
            assertEquals("jan@banan.com", user.email)
        }

        mockMessageSource.demand.getMessage(1) { code, args, locale ->
            return "Match user to customer"
        }

        mockScheduledTaskService.demand.scheduleTask(1) { c,d,f,c2 ->

        }

        service.notificationService = notificationServiceMockControl.createMock()

        MultipartFile file = createMockFile("image.jpg")

        service.registerUserWithFacebook(profile, file)
        notificationServiceMockControl.verify()
        mockMessageSource.verify()
        mockScheduledTaskService.verify()
    }

    @Test
    void testFacebookImageStoredToFileArchive() {
        def profile = createMockFacebookProfile()

        def fileArchiveServiceMockControl = mockFor(FileArchiveService)
        fileArchiveServiceMockControl.demand.storeFile(1..1) { def file ->
            return new MFile(textId: "TEST-ID")
        }
        service.fileArchiveService = fileArchiveServiceMockControl.createMock()

        def notificationServiceMockControl = mockFor(NotificationService)
        notificationServiceMockControl.demand.sendRegistrationConfirmationMail(1..1) { User user -> }
        service.notificationService = notificationServiceMockControl.createMock()

        MultipartFile file = createMockFile("image.jpg", "IMPORTANT_IMAGE_DATA".bytes)

        mockMessageSource.demand.getMessage(1) { code, args, locale ->
            return "Match user to customer"
        }

        mockScheduledTaskService.demand.scheduleTask(1) { c,d,f,c2 ->

        }

        User user = service.registerUserWithFacebook(profile, file)

        assertEquals("TEST-ID", user.profileImage.textId)
        fileArchiveServiceMockControl.verify()
        mockMessageSource.verify()
        mockScheduledTaskService.verify()
    }

    @Test
    void testNewFacebookUserIsRoleUser() {
        def profile = createMockFacebookProfile()

        def notificationServiceMockControl = mockFor(NotificationService)
        notificationServiceMockControl.demand.sendRegistrationConfirmationMail(1..1) { User user -> }
        service.notificationService = notificationServiceMockControl.createMock()

        MultipartFile file = createMockFile("image.jpg")

        mockMessageSource.demand.getMessage(1) { code, args, locale ->
            return "Match user to customer"
        }

        mockScheduledTaskService.demand.scheduleTask(1) { c,d,f,c2 ->

        }


        User user = service.registerUserWithFacebook(profile, file)

        assert user.isInRole("ROLE_USER")
        mockMessageSource.verify()
        mockScheduledTaskService.verify()
    }

    @Test
    void testFacebookRegistrationThrowsErrorIfUserNotValid() {
        def profile = createMockFacebookProfile(null, null, null)

        def notificationServiceMockControl = mockFor(NotificationService)
        notificationServiceMockControl.demand.sendRegistrationConfirmationMail(1..1) { User user -> }
        service.notificationService = notificationServiceMockControl.createMock()

        MultipartFile file = createMockFile("image.jpg")

        User userWithError = service.registerUserWithFacebook(profile, file)
        assert userWithError
        assert userWithError.hasErrors()
    }

    @Test
    void testFacebookRegistrationSetsPassword() {
        def profile = createMockFacebookProfile("pelle@pelle.se", "Pelle", "Persson")

        def notificationServiceMockControl = mockFor(NotificationService)
        notificationServiceMockControl.demand.sendRegistrationConfirmationMail(1..1) { User user -> }
        service.notificationService = notificationServiceMockControl.createMock()

        MultipartFile file = createMockFile("image.jpg")

        mockMessageSource.demand.getMessage(1) { code, args, locale ->
            return "Match user to customer"
        }

        mockScheduledTaskService.demand.scheduleTask(1) { c,d,f,c2 ->

        }

        User user = service.registerUserWithFacebook(profile, file)

        assert user.password != null
        mockMessageSource.verify()
        mockScheduledTaskService.verify()
    }

    @Test
    void testRegisteredUserFacebookConnectionSetsFacebookUID() {
        def profile = createMockFacebookProfile()
        MultipartFile file = createMockFile("image.jpg")

        User user = new User(email: 'user@mail.com', firstname: "user", lastname: "userlast")

        service.updateUserWithFacebook(user, profile, file)

        assert user.facebookUID != null
        assert user.facebookUID == profile.id
    }

    @Test
    void testRemoveFacilityUser() {
        def facility = createFacility()
        def user = createUser()
        def facilityUser = new FacilityUser(user: user)
        def facilityUserRole = new FacilityUserRole(accessRight: FacilityUserRole.AccessRight.FACILITY_ADMIN)
        facilityUser.facility = facility
        facilityUser.addToFacilityRoles(facilityUserRole)
        facilityUser.save(failOnError: true, flush: true)
        facilityUserRole.facilityUser = facilityUser
        facilityUserRole.save(failOnError: true, flush: true)
        user.facility = facility
        user.addToFacilityUsers(facilityUser)
        user.save(failOnError: true, flush: true)
        service.metaClass.getUserFacility = { -> facility }
        assert FacilityUser.countByFacility(facility)

        service.removeFacilityUser(user.id)

        assert !FacilityUser.countByFacility(facility)
    }

    @Test
    void testRemoveFacilityUserIfUserWithoutFacility() {
        def facility = createFacility()
        def user = createUser()
        def facilityUser = new FacilityUser(user: user)
        def facilityUserRole = new FacilityUserRole(accessRight: FacilityUserRole.AccessRight.FACILITY_ADMIN)
        facilityUser.facility = facility
        facilityUser.addToFacilityRoles(facilityUserRole)
        facilityUser.save(failOnError: true, flush: true)
        facilityUserRole.facilityUser = facilityUser
        facilityUserRole.save(failOnError: true, flush: true)
        user.addToFacilityUsers(facilityUser)
        user.save(failOnError: true, flush: true)
        service.metaClass.getUserFacility = { -> facility }
        assert FacilityUser.countByFacility(facility)

        service.removeFacilityUser(user.id)

        assert !FacilityUser.countByFacility(facility)
    }

    void testCanSendDirectMessage() {
        def adminUser = createUser("admin@matchi.se")
        UserRole.create(adminUser, adminRole)
        def regularUser = createUser("user@matchi.se")
        UserRole.create(regularUser, userRole)
        def regularUserWithCustomer = createUser("customer@matchi.se")
        UserRole.create(regularUserWithCustomer, userRole)
        def c = createCustomer()
        c.user = regularUserWithCustomer
        c.save(failOnError: true)

        assert !service.canSendDirectMessage(null, null)
        assert !service.canSendDirectMessage(adminUser, null)
        assert !service.canSendDirectMessage(null, regularUser)
        assert service.canSendDirectMessage(adminUser, regularUser)
        assert !service.canSendDirectMessage(regularUser, adminUser)
        assert service.canSendDirectMessage(regularUserWithCustomer, adminUser)

        new UserMessage(from: adminUser, to: regularUser, message: "msg").save(failOnError: true)

        assert service.canSendDirectMessage(regularUser, adminUser)
    }

    void testAddFavorite() {
        def user = createUser()
        def facility = createFacility()

        def fav = service.addFavorite(user, facility)

        assert fav
        assert fav.user == user
        assert fav.facility == facility
        assert UserFavorite.count() == 1

        def fav2 = service.addFavorite(user, facility)
        assert fav2 == fav
        assert UserFavorite.count() == 1
    }

    void testRemoveFavorite() {
        def user = createUser()
        def facility = createFacility()
        new UserFavorite(user: user, facility: facility).save(failOnError: true)

        assert service.removeFavorite(user, facility)
        assert !UserFavorite.count()
        assert !service.removeFavorite(user, facility)
    }

    MultipartFile createMockFile(def originalFileName) {
        return createMockFile(originalFileName, new byte[0]);
    }

    MultipartFile createMockFile(def originalFileName, def bytes) {
        return new MockMultipartFile(
                'imageFile',
                originalFileName,
                'image/gif',
                bytes)
    }

    org.springframework.social.facebook.api.User createMockFacebookProfile(def email, def firstName, def lastName) {
        def mockFacebookProfile = mockFor(MockFacebookProfile)

        mockFacebookProfile.demand.getEmail(1..10) { ->
            return email
        }
        mockFacebookProfile.demand.getFirstName(1..10) { ->
            return firstName
        }
        mockFacebookProfile.demand.getLastName(1..10) { ->
            return lastName
        }

        return mockFacebookProfile.createMock()
    }

    org.springframework.social.facebook.api.User createMockFacebookProfile() {
        return createMockFacebookProfile("jan@banan.com", "Jan", "Banan")
    }
}

class MockFacebookProfile extends org.springframework.social.facebook.api.User {

    MockFacebookProfile() {
        super("fbID", "", "", "", "", null)
    }

    MockFacebookProfile(String id, String username, String name, String firstName, String lastName, String gender, Locale locale) {
        super(id, name, firstName, lastName, gender, locale)
    }
}
