package com.matchi

import com.matchi.activities.trainingplanner.Trainer
import com.matchi.coupon.CustomerCoupon
import com.matchi.coupon.Offer
import com.matchi.devices.Device
import com.matchi.devices.Token
import com.matchi.dynamicforms.Submission
import com.matchi.invoice.InvoiceRow
import com.matchi.membership.Membership
import com.matchi.orders.Order
import com.matchi.orders.OrderPayment
import com.matchi.watch.SlotWatch
import com.matchi.sportprofile.SportProfile
import com.matchi.sportprofile.SportProfileMindset
import org.joda.time.LocalDate
import org.joda.time.LocalTime
import org.junit.Test

import static com.matchi.TestUtils.*

/**
 * @author Sergei Shushkevich
 */
class UserIntegrationTests extends GroovyTestCase {

    @Test
    void testHasActiveMembershipIn() {
        Facility facility = Facility.first()
        User user = new User(firstname: "John", lastname: "Doe", email: "jdoe@local.net",
                password: "1", enabled: true).save(failOnError: true, flush: true)
        Customer customer = new Customer(facility: facility, number: Customer.count() + 1,
                user: user).save(failOnError: true, flush: true)

        assert !user.hasActiveMembershipIn(facility)

        createMembership(customer)

        assert user.hasActiveMembershipIn(facility)
    }

    @Test
    void testIsHardDeletableOrdersAndPayments() {
        User user = new User(firstname: "John2", lastname: "Doe2", email: "jdoe@local.net",
                password: "1", enabled: true).save(failOnError: true, flush: true)
        Facility facility = createFacility()

        // Nothing yet
        assert user.isHardDeletable()

        // At least one non-NEW order, then we cannot delete user
        Order order = createOrder(user, facility)
        Order order2 = createOrder(user, facility)

        order.status = Order.Status.NEW
        order2.status = Order.Status.COMPLETED

        order.save(flush: true, failOnError: true)
        order2.save(flush: true, failOnError: true)

        assert !user.isHardDeletable()

        // Both Orders are NEW and without payment, that is ok
        order2.status = Order.Status.NEW
        order2.save(flush: true, failOnError: true)
        assert user.isHardDeletable()

        // But if we add an article to it!
        order2.article = Order.Article.FORM_SUBMISSION
        Submission submission = createSubmission()
        submission.order = order2
        submission.save(flush: true, failOnError: true)
        assert !user.isHardDeletable()

        submission.delete(flush: true)
        assert user.isHardDeletable()

        // And even so if a payment has NEW payment
        OrderPayment orderPayment = createAdyenOrderPayment(user, order, "", OrderPayment.Status.NEW)
        order.addToPayments(orderPayment)
        assert user.isHardDeletable()

        // But not with a CAPTURED payment!
        orderPayment = createAdyenOrderPayment(user, order, "", OrderPayment.Status.CAPTURED)
        order.addToPayments(orderPayment)
        assert !user.isHardDeletable()

        order.removeFromPayments(orderPayment)
        orderPayment.delete()
        assert user.isHardDeletable()

        // Not that I think it might happen, but if a user has a payment on another user's order! :O
        User user2 = new User(firstname: "John2", lastname: "Doe2", email: "jdoe22@local.net",
                password: "1", enabled: true).save(failOnError: true, flush: true)
        Order order3 = createOrder(user2, facility)
        orderPayment = createAdyenOrderPayment(user, order3, "", OrderPayment.Status.CAPTURED)
        order3.addToPayments(orderPayment)

        assert !order3.isDeletable()
        assert !(user.getDeleteableOrders()*.payments).flatten().contains(orderPayment)
        assert !user.isHardDeletable()
    }

    @Test
    void testIsHardDeletableReferences() {
        User user = new User(firstname: "John2", lastname: "Doe2", email: "jdoe@local.net",
                password: "1", enabled: true).save(failOnError: true, flush: true)
        Facility facility = createFacility()

        // Nothing yet
        assert user.isHardDeletable()

        // A customer coupon
        Offer offer = createCoupon(facility)
        Customer customerOffer = createCustomer(facility, 'm@il.com')
        CustomerCoupon customerCoupon = CustomerCoupon.link(user, customerOffer, offer, 10, new LocalDate(), "note").save(flush: true, failOnError: true)
        assert !user.isHardDeletable()

        offer.removeFromCustomerCoupons(customerCoupon)
        customerOffer.removeFromCustomerCoupons(customerCoupon)
        customerCoupon.delete(flush: true)
        assert user.isHardDeletable()

        // Or a submission
        Submission submission = createSubmission(customerOffer, createForm(), user)
        assert !user.isHardDeletable()

        submission.delete(flush: true)
        assert user.isHardDeletable()

        // Or an InvoiceRow
        InvoiceRow invoiceRow = new InvoiceRow(createdBy: user, price: 0, vat: 0, customer: customerOffer).save(flush: true, failOnError: true)
        assert !user.isHardDeletable()

        invoiceRow.delete(flush: true)
        assert user.isHardDeletable()

        // Or a membership
        Membership membership = createMembership(customerOffer)
        membership.createdBy = user
        membership.save(failOnError: true, flush: true)
        assert !user.isHardDeletable()

        customerOffer.memberships.clear()
        customerOffer.save(flush: true)

        membership.delete(flush: true)
        assert user.isHardDeletable()
    }

    @Test
    void testDisconnectFromFacilities() {
        User user = new User(firstname: "John2", lastname: "Doe2", email: "jdoe@local.net",
                password: "1", enabled: true).save(failOnError: true, flush: true)
        Facility facility1 = createFacility()

        user.facility = facility1
        user.save(flush: true, failOnError: true)

        createCustomer(facility1, user.email, user.firstname, user.lastname, null, user)

        Facility facility2 = createFacility()
        Customer customer = createCustomer(facility2, user.email, user.firstname, user.lastname, null, user)
        createTrainer(facility2, null, customer, true)

        FacilityUser fu = new FacilityUser(user: user)
        fu.facility = facility1
        FacilityUserRole fur = new FacilityUserRole(accessRight: FacilityUserRole.AccessRight.CUSTOMER)
        fu.addToFacilityRoles(fur)
        fu.save(flush: true, failOnError: true)
        fur.save(flush: true, failOnError: true)

        user.addToFacilityUsers(fu)
        user.save(flush: true, failOnError: true)

        assert FacilityUser.findAllByUser(user)?.size() == 1

        user.disconnectFromFacilities()
        assert Customer.findAllByUser(user)?.size() == 0
        assert Customer.findAllByEmail(user.email)?.size() == 2
        assert Trainer.findAllByUser(user)?.size() == 0
        assert Trainer.findAllByCustomer(customer)?.size() == 1
        assert FacilityUser.findAllByUser(user)?.size() == 0
        assert user.facility == null
    }

    @Test
    void testDeleteRelations() {
        User user = new User(firstname: "John2", lastname: "Doe2", email: "jdoe@local.net",
                password: "1", enabled: true).save(failOnError: true, flush: true)
        Long userId = user.id
        Facility facility = createFacility()

        Role role = Role.findByAuthority("ROLE_USER")
        UserRole userRole = UserRole.create(user, role, true)
        user.addToRoles(userRole)

        Sport sport = createSport()

        SportProfileMindset mindset = new SportProfileMindset(name: "Not exactly top 3 in Wermland", badgeColor: "matchi green")
        mindset.save(flush: true, failOnError: true)
        SportProfile sp = SportProfile.link(user, sport, 0, null)
        sp.save(flush: true, failOnError: true)

        UserFavorite userFavorite = new UserFavorite(user: user, facility: facility).save(flush: true, failOnError: true)
        user.addToFavourites(userFavorite)

        Availability availability = new Availability(weekday: 2, begin: new LocalTime(), end: new LocalTime()).save(flush: true, failOnError: true)
        user.addToAvailabilities(availability)
        availability.save(flush: true, failOnError: true)

        ResetPasswordTicket resetPasswordTicket = new ResetPasswordTicket(key: "key", expires: new Date().plus(7), user: user).save(flush: true, failOnError: true)
        user.addToResetPasswordTickets(resetPasswordTicket)

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

        user.save(flush: true, failOnError: true)

        Long slotWatchId = slotWatch.id
        Long sportProfileId = sp.id
        Long favoriteId = userFavorite.id
        Long deviceId = device.id
        Long availabilityId = availability.id
        Long resetPasswordTicketId = resetPasswordTicket.id
        Long changeEmailTicketId = changeEmailTicket.id
        Long tokenId = token.id
        Long paymentInfoId = paymentInfo.id

        User.withTransaction {
            user.deleteRelations()
        }

        assert !SlotWatch.get(slotWatchId)
        assert !SportProfile.get(sportProfileId)
        assert !UserFavorite.get(favoriteId)
        assert !Device.get(deviceId)
        assert !Availability.get(availabilityId)
        assert !ResetPasswordTicket.get(resetPasswordTicketId)
        assert !ChangeEmailTicket.get(changeEmailTicketId)
        assert !Token.get(tokenId)
        assert !PaymentInfo.get(paymentInfoId)
    }

    @Test
    void testClearProperties() {
        String firstName = "John"
        String lastName = "Doe"
        String email = "jdoe@local.net"
        Facility facility = createFacility()

        User user = new User(
                firstname: firstName,
                lastname: lastName,
                email: email,
                password: "1",
                enabled: true,
                facebookUID: '12341231',
                address: 'Street 123',
                zipcode: '12345',
                city: 'Town',
                telephone: '112233445566',
                facility: facility,
                municipality: facility.municipality,
                birthday: new Date(),
                gender: User.Gender.male
        ).save(failOnError: true, flush: true)

        user.clearProperties()

        assert user.firstname == firstName
        assert user.lastname == lastName
        assert user.email == email
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

    @Test
    void testScramble() {
        String firstname = "John2"
        String lastname = "Doe2"
        String email = "jdoe@local.net"

        User user = new User(firstname: firstname, lastname: lastname, email: email,
                password: "1", enabled: true).save(failOnError: true, flush: true)
        user.scramble()
        user.save(flush: true, failOnError: true)

        User user2 = new User(firstname: firstname, lastname: lastname, email: email,
                password: "1", enabled: true).save(failOnError: true, flush: true)
        user2.scramble()
        user2.save(flush: true, failOnError: true)

        assert user.firstname != firstname
        assert user.lastname != lastname

        assert user2.firstname != user.firstname
        assert user2.lastname != user.lastname
        assert user2.email != user.email
    }

    @Test
    void testAddToMatchingCustomers() {
        Facility facility = createFacility()
        Customer customer = createCustomer(facility)
        customer.email = 'sune@matchi.se'
        customer.save(flush: true, failOnError: true)

        Facility facility2 = createFacility()
        Customer customer2 = createCustomer(facility2)
        customer.save(flush: true, failOnError: true)

        User user = createUser(customer.email)
        user.firstname = customer.firstname
        user.lastname = customer.lastname
        user.save(flush: true, failOnError: true)

        user.addToMatchingCustomers()

        assert Customer.findAllByIdAndUser(customer.id, user).size() == 1
        assert Customer.findAllByIdAndUser(customer2.id, user).size() == 0
    }

    void testHasMembershipIn() {
        def today = new LocalDate()
        def user = createUser()
        def facility1 = createFacility()
        def facility2 = createFacility()
        def facility3 = createFacility()
        def customer1 = createCustomer(facility1, "customer1@matchi.se", "John", "Doe", null, user)
        def customer2 = createCustomer(facility2, "customer2@matchi.se", "John", "Doe", null, user)
        def customer3 = createCustomer(facility3, "customer3@matchi.se", "John", "Doe", null, user)

        assert !user.hasMembershipIn()

        createMembership(customer1, today.plusDays(5), today.plusDays(7), today.plusDays(10))

        assert !user.hasMembershipIn()

        createMembership(customer1, today, today.plusDays(1), today.plusDays(2))

        def names = user.hasMembershipIn()

        assert names
        assert names.size() == 1
        assert names[0] == facility1.name

        createMembership(customer2, today.minusDays(10), today.minusDays(5), today.minusDays(1))
        createMembership(customer3, today.minusDays(10), today.minusDays(5), today)

        names = user.hasMembershipIn()

        assert names
        assert names.size() == 2
        assert names.contains(facility1.name)
        assert names.contains(facility3.name)
    }

    void testGetCustomer() {
        def user = createUser()
        def facility1 = createFacility()
        def facility2 = createFacility()
        def customer1 = createCustomer(facility1, null, null, null, null, user)
        createCustomer(facility2)

        assert user.getCustomer(facility1) == customer1
        assert !user.getCustomer(facility2)
    }

    void testIsCustomerIn() {
        def user = createUser()
        def facility1 = createFacility()
        def facility2 = createFacility()
        createCustomer(facility1)
        createCustomer(facility2, null, null, null, null, user)

        assert !user.isCustomerIn(facility1)
        assert user.isCustomerIn(facility2)
    }
}
