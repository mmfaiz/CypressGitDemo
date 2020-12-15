import com.matchi.*
import com.matchi.activities.trainingplanner.Trainer
import com.matchi.devices.Device
import com.matchi.devices.Token
import com.matchi.orders.Order
import com.matchi.orders.OrderPayment
import com.matchi.orders.OrderRefund
import com.matchi.requests.TrainerRequest
import com.matchi.watch.SlotWatch
import com.matchi.sportprofile.SportProfile
import com.matchi.sportprofile.SportProfileMindset
import org.apache.commons.lang.RandomStringUtils
import org.joda.time.LocalTime
import org.junit.Test

import static com.matchi.TestUtils.*

/**
 * Created by victorlindhe on 2018-05-16.
 */
class UserDeletionIntegrationTests extends GroovyTestCase {

    def userService
    def sessionFactory

    @Test
    void testDeleteUserSimple() {
        User user = createUser()
        Long userId = user.id

        user.delete(flush: true)
        assert User.get(userId) == null
    }

    @Test
    void testDeleteUserWithRole() {
        User user = createUser()
        Long userId = user.id

        Role role = Role.findByAuthority("ROLE_USER")
        UserRole userRole = UserRole.create(user, role, true)
        user.addToRoles(userRole)
        user.save(flush: true)

        user.roles?.each { UserRole userRole1 ->
            userRole1.delete()
        }

        user.delete(flush: true)
        assert User.get(userId) == null
    }

    @Test
    void testHardDelete() {
        TestData testData = setupUser()
        User user = testData.user
        Long userId = user.id
        Facility facility = testData.facility

        // Orders we are issuer of
        int nOrders = 100
        createOrders(user, facility, true, nOrders)

        // Orders having other issuer, but our main user as the user of order
        User user2 = createUser("anotherMail@matchi.se")
        int nUserOrders = 20
        createOrders(user2, facility, false, nUserOrders)

        List<Order> userOrders = Order.findAllByUser(user2)
        assert userOrders.size() == nUserOrders

        userOrders.each { Order order ->
            order.user = user
            order.save(flush: true, failOnError: true)
        }

        List<OrderRefund> refunds = createOrderRefunds(user, userOrders)
        List<Long> refundIds = refunds*.id

        sessionFactory.getCurrentSession().flush()

        assert OrderRefund.findAllByIdInList(refundIds).size() == nUserOrders
        assert OrderRefund.findAllByIssuer(user).size() == nUserOrders

        Long roleId = user.roles.first().role.id

        assert !Order.findByUser(user2)
        assert Order.findAllByUser(user).size() == nUserOrders + nOrders
        assert Order.findAllByIssuer(user2).size() == nUserOrders

        testData.preCheck()
        assert UserRole.get(userId, roleId)

        // Let's do it!
        userService.deleteUser(user)

        // To further reproduce some of the errors from the real world
        sessionFactory.getCurrentSession().flush()

        testData.postCheck()
        checkFacilityDisconnected(user)

        assert !User.get(userId)
        assert Order.countByFacility(facility) == nUserOrders
        assert OrderPayment.countByIssuer(user2) == nUserOrders*2
        assert Order.countByFacilityAndUserIsNull(facility) == nUserOrders
        assert Order.countByIssuer(user2) == nUserOrders

        assert OrderRefund.findAllByIdInList(refundIds).size() == nUserOrders
        assert !OrderRefund.findByIssuer(user)

        assert !UserRole.get(userId, roleId)
    }

    @Test
    void testSoftDelete() {
        TestData testData = setupUser()
        User user = testData.user
        Facility facility = testData.facility

        Long userId = user.id
        String firstName = user.firstname
        String lastName = user.lastname
        String email = user.email

        int nOrders = 100
        createOrders(user, facility, false, nOrders)

        sessionFactory.getCurrentSession().flush()

        Long roleId = user.roles.first().role.id

        testData.preCheck()
        assert UserRole.get(userId, roleId)

        // Let's do it!
        userService.deleteUser(user)

        // To further reproduce some of the errors from the real world
        sessionFactory.getCurrentSession().flush()

        testData.postCheck()
        checkFacilityDisconnected(user)

        verifyScrambledUser(userId, firstName, lastName, email)

        // The orders should still be there
        assert Order.countByFacility(facility) == nOrders
        assert OrderPayment.countByIssuer(user) == nOrders*2
        assert !UserRole.get(userId, roleId)
    }

    @Test
    void testCreateOrdersHard() {
        User user = createUser()
        Facility facility = createFacility()

        int nOrders = 100

        createOrders(user, facility, true, nOrders)

        List<Order> orders = Order.findAllByFacility(facility)
        List<OrderPayment> payments = OrderPayment.findAllByIssuer(user)

        assert orders.size() == nOrders
        assert payments.size() == nOrders*2
        assert orders.every { Order order ->
            return order.status == Order.Status.NEW
        }

        assert payments.every { OrderPayment payment ->
            return payment.status == OrderPayment.Status.NEW
        }
    }

    @Test
    void testCreateOrdersSoft() {
        User user = createUser()
        Facility facility = createFacility()

        int nOrders = 100

        createOrders(user, facility, false, nOrders)

        List<Order> orders = Order.findAllByFacility(facility)
        List<OrderPayment> payments = OrderPayment.findAllByIssuer(user)

        assert orders.size() == nOrders
        assert payments.size() == nOrders*2

        assert orders*.status.unique() == Order.Status.list()
        assert payments*.status.unique() == OrderPayment.Status.list()
    }

    private TestData setupUser() {
        User user = createUser()

        user.password = RandomStringUtils.randomAlphabetic(10)
        user.address = RandomStringUtils.randomAlphabetic(10)
        user.zipcode = RandomStringUtils.randomAlphabetic(10)
        user.city = RandomStringUtils.randomAlphabetic(10)
        user.telephone = RandomStringUtils.randomNumeric(10)
        user.country = RandomStringUtils.randomAlphabetic(10)
        user.description = RandomStringUtils.randomAlphabetic(10)
        user.activationcode = RandomStringUtils.randomAlphabetic(10)
        user.facebookUID = RandomStringUtils.randomNumeric(10)
        user.birthday = new Date()
        user.gender = User.Gender.male
        user.receiveBookingNotifications = true
        user.receiveNewsletters = true
        user.searchable = true
        user.matchable = true
        user.enabled = true
        user.accountExpired = false
        user.accountLocked = false
        user.passwordExpired = false
        user.dateActivated = new Date()
        user.lastLoggedIn = new Date()
        user.municipality = createMunicipality()

        Facility facility = createFacility()

        Role role = Role.findByAuthority("ROLE_USER")
        UserRole userRole = UserRole.create(user, role, true)
        user.addToRoles(userRole)

        Role role2 = Role.findByAuthority("ROLE_ADMIN")
        UserRole userRole2 = UserRole.create(user, role2, true)
        user.addToRoles(userRole2)

        Sport sport = createSport()

        SportProfileMindset mindset = new SportProfileMindset(name: "Not exactly top 3 in Wermland", badgeColor: "matchi green")
        mindset.save(flush: true, failOnError: true)
        SportProfile sp = SportProfile.link(user, sport, 0, [mindset])
        sp.save(flush: true, failOnError: true)

        Sport sport2 = createSport()
        SportProfile sp2 = SportProfile.link(user, sport2, 23, [mindset])
        sp2.save(flush: true, failOnError: true)

        UserFavorite userFavorite = new UserFavorite(user: user, facility: facility).save(flush: true, failOnError: true)
        user.addToFavourites(userFavorite)
        UserFavorite userFavorite2 = new UserFavorite(user: user, facility: facility).save(flush: true, failOnError: true)
        user.addToFavourites(userFavorite2)

        Availability availability = new Availability(weekday: 2, begin: new LocalTime(), end: new LocalTime()).save(flush: true, failOnError: true)
        user.addToAvailabilities(availability)
        availability.save(flush: true, failOnError: true)

        Availability availability2 = new Availability(weekday: 2, begin: new LocalTime(), end: new LocalTime()).save(flush: true, failOnError: true)
        user.addToAvailabilities(availability2)
        availability2.save(flush: true, failOnError: true)

        ResetPasswordTicket resetPasswordTicket = new ResetPasswordTicket(key: "key", expires: new Date().plus(7), user: user).save(flush: true, failOnError: true)
        user.addToResetPasswordTickets(resetPasswordTicket)

        ResetPasswordTicket resetPasswordTicket2 = new ResetPasswordTicket(key: "key3333", expires: new Date().plus(7), user: user).save(flush: true, failOnError: true)
        user.addToResetPasswordTickets(resetPasswordTicket2)

        Court court = createCourt(facility)
        SlotWatch slotWatch = new SlotWatch(
                court: court,
                facility: facility,
                user: user,
                smsNotify: false,
                fromDate: new Date(),
                toDate: new Date().plus(7)
        ).save(flush: true, failOnError: true)

        Device device = new Device(user: user, deviceDescription: "hipster phone", deviceModel: "Android", deviceId: "1234-sune").save(flush: true, failOnError: true)
        Token token = device.getValidToken()
        token.save(flush: true, failOnError: true)

        ChangeEmailTicket changeEmailTicket = new ChangeEmailTicket(expires: new Date().plus(7), user: user, newEmail: "newmail@matchi.se", key: "key123").save(flush: true, failOnError: true)

        PaymentInfo paymentInfo = new PaymentInfo(provider: PaymentInfo.PaymentProvider.ADYEN, user: user).save(flush: true, failOnError: true)

        Facility facility2 = createFacility()

        Customer customer = createCustomer(facility, user.email, user.firstname, user.lastname, Customer.CustomerType.MALE, user)
        Customer customer2 = createCustomer(facility2, user.email, user.firstname, user.lastname, Customer.CustomerType.MALE, user)

        FacilityUser fu = new FacilityUser(user: user)
        fu.facility = facility
        FacilityUserRole fur = new FacilityUserRole(accessRight: FacilityUserRole.AccessRight.FACILITY_ADMIN)
        fu.addToFacilityRoles(fur)
        fu.save(flush: true, failOnError: true)
        fur.save(flush: true, failOnError: true)

        user.addToFacilityUsers(fu)

        Facility facility1 = createFacility()
        FacilityUser fu2 = new FacilityUser(user: user)
        fu2.facility = facility1
        FacilityUserRole fur2 = new FacilityUserRole(accessRight: FacilityUserRole.AccessRight.CUSTOMER)
        FacilityUserRole fur3 = new FacilityUserRole(accessRight: FacilityUserRole.AccessRight.INVOICE)
        fu2.addToFacilityRoles(fur2)
        fu2.addToFacilityRoles(fur3)
        fu2.save(flush: true, failOnError: true)
        fur2.save(flush: true, failOnError: true)
        fur3.save(flush: true, failOnError: true)

        user.addToFacilityUsers(fu2)

        Trainer trainer = new Trainer(user: user, facility: facility, firstName: user.firstname, lastName: user.lastname, sport: sport).save(flush: true, failOnError: true)

        TrainerRequest trainerRequest = new TrainerRequest(requester: user, trainer: trainer, start: new Date(), end: new Date().plus(7)).save(flush: true, failOnError: true)

        User friend = createUser('friend@matchi.se')
        UserMessage userMessage = createUserMessage(user, friend)
        UserMessage userMessage2 = createUserMessage(friend, user)

        TestData testData = new TestData()
        testData.user = user.save(flush: true, failOnError: true)
        testData.facility = facility

        testData.toBeRemoved = [
                (SlotWatch): [slotWatch.id],
                (ResetPasswordTicket): [resetPasswordTicket.id, resetPasswordTicket2.id],
                (Availability): [availability.id, availability2.id],
                (UserFavorite): [userFavorite.id, userFavorite2.id],
                (SportProfile): [sp.id, sp2.id],
                (Device): [device.id],
                (Token): [token.id],
                (ChangeEmailTicket): [changeEmailTicket.id],
                (PaymentInfo): [paymentInfo.id],
                (FacilityUser): [fu.id, fu2.id],
                (FacilityUserRole): [fur.id, fur2.id, fur3.id],
                (TrainerRequest): [trainerRequest.id],
                (UserMessage): [userMessage.id, userMessage2.id]
        ]

        testData.toPersist = [
                (Facility): [facility.id],
                (Court): [court.id],
                (Sport): [sport.id, sport2.id],
                (SportProfileMindset): [mindset.id],
                (Role): [role.id, role2.id],
                (Customer): [customer.id, customer2.id],
                (Trainer): [trainer.id],
                (User): [friend.id]
        ]

        return testData
    }

    private void checkFacilityDisconnected(User user) {
        assert user.facility == null
        assert !Customer.countByUser(user)
        assert !Trainer.countByUser(user)
    }

    private void createOrders(User user, Facility facility, boolean hardDeletable, int nOrders) {
        List<Order.Status> orderStatuses = hardDeletable ? [Order.Status.NEW] : Order.Status.list()
        List<OrderPayment.Status> paymentStatuses = hardDeletable ? [OrderPayment.Status.NEW] : OrderPayment.Status.list()

        int no = orderStatuses.size()
        int np = paymentStatuses.size()

        Order order
        OrderPayment payment

        for(int i = 0; i < nOrders; i++) {
            order = createOrder(user, facility)
            order.status = orderStatuses.get(i % no)
            payment = createAdyenOrderPayment(user, order, RandomStringUtils.randomAlphanumeric(20), paymentStatuses.get(i % np))
            order.addToPayments(payment)
            payment = createAdyenOrderPayment(user, order, RandomStringUtils.randomAlphanumeric(20), paymentStatuses.get(i % np))
            order.addToPayments(payment)
            order.save(flush: true, failOnError: true)
        }
    }

    private List<OrderRefund> createOrderRefunds(User user, List<Order> orders) {
        OrderRefund refund
        return orders?.collect { Order order ->
            refund = new OrderRefund(order: order, amount: 100, note: "Order ${order.id}", issuer: user).save(flush: true, failOnError: true)
            order.addToRefunds(refund)
            order.save(flush: true, failOnError: true)
            return refund
        }
    }

    private void verifyScrambledUser(Long userId, String firstName, String lastName, String email) {
        User user = User.get(userId)

        assert user
        assert user.firstname != firstName
        assert user.lastname != lastName
        assert user.email != email
        assert user.dateDeleted != null
        assert user.dateCreated != null
        assert user.lastUpdated != null

        assert user.password == null
        assert user.address == null
        assert user.zipcode == null
        assert user.city == null
        assert user.telephone == null
        assert user.country == null
        assert user.description == null
        assert user.activationcode == null
        assert user.facebookUID == null
        assert user.birthday == null
        assert user.gender == null
        assert user.profileImage == null
        assert user.welcomeImage == null
        assert user.receiveBookingNotifications == false
        assert user.receiveNewsletters == false
        assert user.searchable == false
        assert user.matchable == false
        assert user.enabled == false
        assert user.accountExpired == true
        assert user.accountLocked == true
        assert user.passwordExpired == true
        assert user.dateActivated == null
        assert user.dateBlocked == null
        assert user.lastLoggedIn == null
        assert user.anonymouseBooking == null
        assert user.municipality == null
    }

}

class TestData {
    User user
    Facility facility
    Map<Class, List<Long>> toBeRemoved
    Map<Class, List<Long>> toPersist

    void preCheck() {
        toBeRemoved.each { Class domain, List<Long> ids ->
            ids.each { Long id -> assert domain.get(id) }
        }

        toPersist.each { Class domain, List<Long> ids ->
            ids.each { Long id -> assert domain.get(id) }
        }
    }

    void postCheck() {
        toBeRemoved.each { Class domain, List<Long> ids ->
            ids.each { Long id -> assert !domain.get(id) }
        }

        toPersist.each { Class domain, List<Long> ids ->
            ids.each { Long id -> assert domain.get(id) }
        }
    }
}
