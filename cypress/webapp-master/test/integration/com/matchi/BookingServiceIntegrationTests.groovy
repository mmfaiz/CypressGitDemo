package com.matchi

import com.matchi.enums.BookingGroupType
import com.matchi.enums.RedeemAt
import com.matchi.enums.RedeemType
import com.matchi.facility.FilterBookingsCommand
import com.matchi.invoice.InvoiceRow
import com.matchi.orders.CashOrderPayment
import com.matchi.orders.Order
import com.matchi.orders.OrderPayment
import com.matchi.price.Price
import com.matchi.price.PriceListConditionCategory
import com.matchi.schedule.TimeSpan
import com.matchi.subscriptionredeem.SlotRedeem
import com.matchi.subscriptionredeem.redeemstrategy.InvoiceRowRedeemStrategy
import org.joda.time.DateTime

import static com.matchi.TestUtils.*

/**
 * @author Sergei Shushkevich
 */
class BookingServiceIntegrationTests extends GroovyTestCase {

    def bookingService
    def redeemService
    def springSecurityService

    void testExportBookings() {
        def writer = new StringWriter()
        def customer1 = createCustomer(null, "john.doe@local.net", "John", "Doe", Customer.CustomerType.MALE)
        def slot1 = createSlot()
        createBooking(customer1, slot1)
        def customer2 = createCustomer(null, "jane.doe@local.net", "Jane", "Doe", Customer.CustomerType.FEMALE)
        def slot2 = createSlot(null, new Date() + 10, new Date() + 11)
        createBooking(customer1, slot2)
        def subscription = createSubscription(customer2)
        slot2.subscription = subscription
        slot2.save(flush: true, failOnError: true)

        bookingService.exportBookings([slot1.id, slot2.id], writer, new Locale("en"))

        assert writer.toString() == """"Customer nr.","Name","Date","Start time","End time","Price","Customer nr.","Name","Price","Credit","Comments","Players"\r\n"${customer1.number}","${customer1.fullName()}","${slot1.startTime.format('yyyy-MM-dd')}","${slot1.startTime.format('HH:mm')}","${slot1.endTime.format('HH:mm')}","0",,,,,,\r\n"${customer1.number}","${customer1.fullName()}","${slot2.startTime.format('yyyy-MM-dd')}","${slot2.startTime.format('HH:mm')}","${slot2.endTime.format('HH:mm')}","0","${customer2.number}","${customer2.fullName()}","0",,,\r\n"""
    }

    void testExportBookingCustomers() {
        def writer = new StringWriter()
        def customer1 = createCustomer(null, "john.doe@local.net", "John", "Doe", Customer.CustomerType.MALE)
        def slot1 = createSlot()
        def subscription = createSubscription(customer1)
        slot1.subscription = subscription
        slot1.save(flush: true, failOnError: true)
        def customer2 = createCustomer(null, "jane.doe@local.net", "Jane", "Doe", Customer.CustomerType.FEMALE)
        def slot2 = createSlot()
        createBooking(customer2, slot2)
        def slot3 = createSlot()
        createBooking(customer2, slot3)

        bookingService.exportBookingCustomers([slot1.id, slot2.id, slot3.id], writer)

        assert writer.toString() == "Kund nr,E-post,Typ,Namn,Adress 1,Adress 2,Post nr,Ort,Land,Tel,Mobil,Målsman,Antal bokningar i urval\r\n${customer1.number},john.doe@local.net,MALE,John Doe,,,,,,,,,0\r\n${customer2.number},jane.doe@local.net,FEMALE,Jane Doe,,,,,,,,,2\r\n"
    }

    void testHasPermissionToBooking() {
        def booking = createBooking()
        def user = createUser()
        user.facility = booking.slot.court.facility
        user.save(failOnError: true, flush: true)
        def fu = new FacilityUser(user: user)
                .addToFacilityRoles(new FacilityUserRole(accessRight: FacilityUserRole.AccessRight.FACILITY_ADMIN))
        fu.facility = user.facility
        fu.save(failOnError: true, flush: true)
        def user2 = createUser("janedoe@local.net")
        user2.facility = createFacility()
        user2.save(failOnError: true, flush: true)
        fu = new FacilityUser(user: user2)
                .addToFacilityRoles(new FacilityUserRole(accessRight: FacilityUserRole.AccessRight.FACILITY_ADMIN))
        fu.facility = user2.facility
        fu.save(failOnError: true, flush: true)

        assert bookingService.hasPermissionToBooking(user, booking)
        assert !bookingService.hasPermissionToBooking(user2, booking)
    }

    void testMakeBookings() {
        int initBookingCount = Booking.count()

        def user = createUser()
        springSecurityService.reauthenticate user.email
        def facility = createFacility()
        def customer = createCustomer(facility)
        def slot = createSlot(createCourt(facility))
        def cmd = new FacilityBookingCommand(customerId: customer.id, userId: user.id,
                slotId: slot.id, type: BookingGroupType.DEFAULT,
                paid: true, sendNotification: false)

        def update = bookingService.makeBookings(cmd)

        assert !update
        assert 1 == Booking.count() - initBookingCount
        assert 1 == Order.countByFacility(facility)
        assert 1 == CashOrderPayment.countByIssuer(user)
        def order = Order.findByFacility(facility)
        assert Order.Status.COMPLETED == order.status
        assert Order.ORIGIN_FACILITY == order.origin
        assert Order.Article.BOOKING == order.article
        assert 1 == order.payments.size()
        assert OrderPayment.Status.CAPTURED == order.payments.iterator().next().status
        def booking = Booking.last()
        assert slot.id == booking.slot.id
        assert order.id == booking.order.id
    }

    void testCancelOrders() {
        def user = createUser()
        def facility = createFacility()
        def customer = createCustomer(facility)
        def slot = createSlot(createCourt(facility))
        def booking = createBooking(customer, slot)
        customer.user = user
        customer.save(failOnError: true)
        def order = createOrder(user, facility)
        order.customer = customer
        order.save(failOnError: true)
        booking.order = order
        booking.save(failOnError: true)

        assert slot.booking

        bookingService.cancelOrders([order], user)

        assert slot.booking == null
    }

    void testRedeemWhenSubscriptionBookingMoved() {
        def user = createUser()
        def facility = createFacility()
        createSubscriptionRedeem(facility)
        def slot = createSlot(createCourt(facility))
        def customer = createCustomer(facility)
        customer.user = user
        customer.save(failOnError: true)
        def booking = createBooking(customer, slot)
        def order = createOrder(user, facility)
        order.customer = customer
        order.save(failOnError: true)
        def price = order.price
        def subscription = createSubscription(customer)
        booking.order = order
        booking.group = createBokingGroup()
        booking.group.subscription = subscription
        booking.save(failOnError: true)
        slot.subscription = subscription
        slot.save(failOnError: true)
        def freeSlot = createSlot(createCourt(facility))

        bookingService.moveBooking(slot, freeSlot, false, "Move booking message")

        bookingService.cancelBooking(booking, "Cancel booking message", true, RedeemType.FULL, user, false)

        assert -price == InvoiceRow.first()?.price
    }

    void testRedeemWhenSubscriptionBookingAndNormalBookingSwitched() {
        def user = createUser()
        def facility = createFacility()
        createSubscriptionRedeem(facility)
        def slot1 = createSlot(createCourt(facility))
        def customer = createCustomer(facility)
        customer.user = user
        customer.save(failOnError: true)
        def booking1 = createBooking(customer, slot1)
        def bookingGroup = createBokingGroup()
        def order = createOrder(user, facility)
        order.customer = customer
        order.save(failOnError: true)
        def price = order.price
        def subscription = createSubscription(customer)
        booking1.order = order
        booking1.group = bookingGroup
        booking1.group.subscription = subscription
        booking1.save(failOnError: true)
        slot1.subscription = subscription
        slot1.save(failOnError: true)
        def slot2 = createSlot(createCourt(facility))
        def booking2 = createBooking(createCustomer(facility), slot2)
        booking2.group = createBokingGroup(BookingGroupType.DEFAULT)
        booking2.save(failOnError: true)

        bookingService.moveBooking(slot1, slot2, false, "Move booking message")

        bookingService.cancelBooking(booking1, "Cancel booking message", true, RedeemType.FULL, user, false)

        assert -price == InvoiceRow.first()?.price
    }

    void testRedeemWhenCancelledSubscriptionBookingRebookedAsTheSameCustomerWithNormalBooking() {
        def user = createUser()
        springSecurityService.reauthenticate(user.email)
        def facility = createFacility()
        createSubscriptionRedeem(facility)
        def slot = createSlot(createCourt(facility))
        def customer = createCustomer(facility)
        customer.user = user
        customer.save(failOnError: true)
        def booking = createBooking(customer, slot)
        def order = createOrder(user, facility)
        order.customer = customer
        order.save(failOnError: true)
        def price = order.price
        def subscription = createSubscription(customer)
        booking.order = order
        booking.group = createBokingGroup()
        booking.group.subscription = subscription
        booking.save(failOnError: true)
        slot.subscription = subscription
        slot.save(failOnError: true)

        bookingService.cancelBooking(booking, "Cancel booking message", true, RedeemType.FULL, user, false)

        assert -price == InvoiceRow.first()?.price
        def subscriptionPriceList = createPriceList(facility, slot.court.sport, true)
        subscriptionPriceList.priceListConditionCategories = [createPriceListCategory(facility, subscriptionPriceList, 100L)]
        subscriptionPriceList.save(failOnError: true, flush: true)
        def cmd = new FacilityBookingCommand(customerId: customer.id, userId: user.id, slotId: slot.id,
                type: BookingGroupType.DEFAULT, paid: true, sendNotification: false)

        bookingService.makeBookings(cmd)

        def newBooking = Booking.last()
        price = newBooking.order.price

        bookingService.cancelBooking(newBooking, "Cancel booking message", true, RedeemType.FULL, user, false)

        assert -price == InvoiceRow.last()?.price
    }

    void testRedeemWhenCancelledSubscriptionBookingRebookedAsAnotherCustomerWithPaidNormalBookingThenRunCreditCancellations() {
        def user = createUser()
        springSecurityService.reauthenticate(user.email)
        def facility = createFacility()
        createSubscriptionRedeem(facility, RedeemAt.SLOTREBOOKED,
                createInvoiceRowRedeemStrategy(InvoiceRowRedeemStrategy.RedeemAmountType.PRICE_REDUCTION_BACK, 20L))
        def slot = createSlot(createCourt(facility))
        def customer = createCustomer(facility)
        customer.user = user
        customer.save(failOnError: true)
        def booking = createBooking(customer, slot)
        def order = createOrder(user, facility)
        def subscription = createSubscription(customer)
        booking.order = order
        booking.group = createBokingGroup()
        booking.group.subscription = subscription
        booking.save(failOnError: true)
        slot.subscription = subscription
        slot.save(failOnError: true)

        bookingService.cancelBooking(booking, "Cancel booking message", true, RedeemType.NORMAL, user, false)

        assert !InvoiceRow.count()
        assert 1 == SlotRedeem.count()
        def redeem = SlotRedeem.first()
        assert !redeem.redeemed
        assert order.price == redeem.amount

        def normalPriceList = createPriceList(facility, slot.court.sport)
        normalPriceList.priceListConditionCategories = [createPriceListCategory(facility, normalPriceList, 60L)]
        normalPriceList.save(failOnError: true, flush: true)
        def subscriptionPriceList = createPriceList(facility, slot.court.sport, true)
        subscriptionPriceList.priceListConditionCategories = [createPriceListCategory(facility, subscriptionPriceList, 100L)]
        subscriptionPriceList.save(failOnError: true, flush: true)
        def anotherUser = createUser("user@local.net")
        springSecurityService.reauthenticate(anotherUser.email)
        def anotherCustomer = createCustomer(facility)
        anotherCustomer.user = anotherUser
        anotherCustomer.save(failOnError: true)
        def cmd = new FacilityBookingCommand(customerId: anotherCustomer.id, userId: anotherUser.id, slotId: slot.id,
                type: BookingGroupType.DEFAULT, paid: true, sendNotification: false)

        bookingService.makeBookings(cmd)
        slot.booking.save(flush: true)

        Slot.metaClass.static.withNewTransaction = { Closure callable -> callable.call() }
        redeemService.redeemUnredeemedCancelations([facility])

        redeem = SlotRedeem.findById(redeem.id)
        slot = Slot.findById(slot.id)

        assert redeem.amount != slot.booking.order.price
        assert redeem.redeemed
        assert 1 == InvoiceRow.count()
        assert -(redeem.amount - 20) == InvoiceRow.first()?.price
    }

    void testSearchBooking() {
        def facility = createFacility()
        def group = createGroup(facility)
        def startTime = new DateTime()
        def endTime = new DateTime().plusHours(1)
        def slot = createSlot(createCourt(facility), startTime.toDate(), endTime.toDate())
        def timeSpan = new TimeSpan(startTime.minusHours(1), endTime)
        def filter = new FilterBookingsCommand()
        def bookingTypes = [BookingGroupType.DEFAULT, BookingGroupType.SUBSCRIPTION, BookingGroupType.ACTIVITY,
                            BookingGroupType.TRAINING, BookingGroupType.COMPETITION, BookingGroupType.NOT_AVAILABLE]

        def bookings = createBookings(facility, slot)

        assert 7 == bookingService.searchBooking(facility, timeSpan, filter).size()

        filter.bookingTypes = bookingTypes
        assert 7 == bookingService.searchBooking(facility, timeSpan, filter).size()

        filter.bookingTypes -= BookingGroupType.SUBSCRIPTION
        assert 6 == bookingService.searchBooking(facility, timeSpan, filter).size()

        filter.bookingTypes -= BookingGroupType.ACTIVITY
        assert 5 == bookingService.searchBooking(facility, timeSpan, filter).size()

        filter.bookingTypes -= BookingGroupType.TRAINING
        assert 4 == bookingService.searchBooking(facility, timeSpan, filter).size()

        filter.bookingTypes -= BookingGroupType.COMPETITION
        assert 3 == bookingService.searchBooking(facility, timeSpan, filter).size()

        filter.bookingTypes -= BookingGroupType.NOT_AVAILABLE
        assert 2 == bookingService.searchBooking(facility, timeSpan, filter).size()

        filter.bookingTypes = null
        assert 7 == bookingService.searchBooking(facility, timeSpan, filter).size()

        filter.groups = [0L]
        assert 7 == bookingService.searchBooking(facility, timeSpan, filter).size()

        filter.groups = [group.id]
        assert !bookingService.searchBooking(facility, timeSpan, filter)

        CustomerGroup.link(bookings[-1].customer, group)
        assert 1 == bookingService.searchBooking(facility, timeSpan, filter).size()

        filter.groups = [0L]
        assert 6 == bookingService.searchBooking(facility, timeSpan, filter).size()

        filter.groups = [0L, group.id]
        assert 7 == bookingService.searchBooking(facility, timeSpan, filter).size()
    }

    void testMoveSubscriptionBookingWithWithNormalBookingOnCanceledSubscriptionSlot() {
        // Arrange
        def startTime = new DateTime()
        def endTime = new DateTime().plusHours(1)
        Facility facility = createFacility()
        Court courtA = createCourt(facility)
        Court courtB = createCourt(facility)
        User userA = createUser("userA@local.net")
        User userB = createUser("userB@local.net")
        User userC = createUser("userC@local.net")
        Customer customerA = createCustomer(facility, null, null, null, null, userA)
        Customer customerB = createCustomer(facility, null, null, null, null, userB)
        Customer customerC = createCustomer(facility, null, null, null, null, userC)

        // Act
        // 1) Create two subscription slot bookings for different customers.
        // 2) Cancel the first subscription slot booking.
        // 3) Book the same slot with a normal booking (keeping the connected subscription).
        // 4) Move the normal booking slot with the canceled subscription slot.
        Slot slotA = createSubscriptionSlotBooking(courtA, startTime, endTime, customerA)
        Slot slotB = createSubscriptionSlotBooking(courtB, startTime, endTime, customerB)
        Subscription subscriptionA = slotA.subscription
        Subscription subscriptionB = slotB.subscription
        bookingService.cancelBooking(slotA.booking, "Cancel booking message", true, RedeemType.EMPTY, userA, false)
        Booking bookingC = createBooking(customerC, slotA)
        bookingC.save(failOnError: true)
        bookingService.moveBooking(slotA, slotB, false, "Move booking message")

        // Assert
        // The slots should keep their subscriptions during the move process.
        assert slotA.subscription == subscriptionA
        assert slotB.isBookedBySubscriber()
        assert slotB.subscription == subscriptionB
    }

    void testMoveCanceledSubscriptionSlotWithoutBookingWithCanceledSubscriptionSlotWithoutBooking() {
        // Arrange
        def startTime = new DateTime()
        def endTime = new DateTime().plusHours(1)
        Facility facility = createFacility()
        Court courtA = createCourt(facility)
        Court courtB = createCourt(facility)
        User userA = createUser("userA@local.net")
        User userB = createUser("userB@local.net")

        // Act
        // 1) Create two subscription slot bookings for different customers.
        // 2) Cancel both subscription slot bookings.
        // 3) Move the canceled subscription slot bookings with each other.
        Slot slotA = createSubscriptionSlotBooking(courtA, startTime, endTime, createCustomer(facility, null, null, null, null, userA))
        Slot slotB = createSubscriptionSlotBooking(courtB, startTime, endTime, createCustomer(facility, null, null, null, null, userB))
        Subscription subscriptionA = slotA.subscription
        Subscription subscriptionB = slotB.subscription
        bookingService.cancelBooking(slotA.booking, "Cancel booking message", true, RedeemType.EMPTY, userA, false)
        bookingService.cancelBooking(slotB.booking, "Cancel booking message", true, RedeemType.EMPTY, userB, false)
        bookingService.moveBooking(slotA, slotB, false, "Move booking message")

        // Assert
        // The subscriptions should switch slots so that they remain on the original datetime/court.
        assert slotA.subscription == subscriptionB
        assert slotB.subscription == subscriptionA
    }

    void testMoveBooking() {
        def user = createUser()
        def facility = createFacility()

        def slot1 = createSlot()
        slot1.save(failOnError: true)

        def order1 = createOrder(user, facility)
        order1.dateDelivery = slot1.startTime
        order1.description = slot1.getDescription()
        order1.save(failOnError: true)

        def booking1 = createBooking()
        booking1.order = order1
        booking1.save(failOnError: true)

        def slot2 = createSlot()
        slot2.startTime = (new DateTime()).plusHours(1).toDate()
        slot2.endTime = (new DateTime()).plusHours(2).toDate()
        slot2.save(failOnError: true)

        def order2 = createOrder(user, facility)
        order2.dateDelivery = slot2.startTime
        order2.description = slot2.getDescription()
        order2.save(failOnError: true)

        def booking2 = createBooking()
        booking2.order = order2
        booking2.save(failOnError: true)

        slot1.booking = booking1
        slot1.save(failOnError: true)

        slot2.booking = booking2
        slot2.save(failOnError: true)

        def originalSlot1Description = slot1.booking.order.description
        def originalSlot2Description = slot2.booking.order.description
        def originalOrder1DateDelivery = order1.dateDelivery.toString()
        def originalOrder2DateDelivery = order2.dateDelivery.toString()

        bookingService.moveBooking(slot1, slot2, false, "Move booking message")

        assert slot1.booking.order.dateDelivery.toString().equals(originalOrder2DateDelivery)
        assert slot2.booking.order.dateDelivery.toString().equals(originalOrder1DateDelivery)
        assert slot1.booking.order.description.equals(originalSlot2Description)
        assert slot2.booking.order.description.equals(originalSlot1Description)
    }

    private static Slot createSubscriptionSlotBooking(Court courtA, DateTime startTime, DateTime endTime, Customer customer) {
        Slot slot = createSlot(courtA, startTime.toDate(), endTime.toDate())

        Booking bookingA = createBooking(customer, slot)
        BookingGroup bookingGroupA = createBokingGroup()
        bookingA.group = bookingGroupA
        bookingA.save(validate: false)

        Subscription subscriptionA = createSubscription(customer, bookingGroupA, courtA)
        slot.subscription = subscriptionA
        slot.save(validate: false)
        slot
    }

    private static PriceListConditionCategory createPriceListCategory(Facility facility, PriceList priceList, Long netPrice) {
        def customerCategory = createPriceListCustomerCategory(facility)
        def category = createPriceListConditionCategory(priceList)
        Price price = createPrice(category, customerCategory, netPrice)
        category.prices = [price]
        category.conditions = []
        category.save(failOnError: true, flush: true)
        category
    }

    private List createBookings(Facility facility, Slot slot) {
        def booking1 = createBooking(createCustomer(facility), slot)
        booking1.group = createBokingGroup(BookingGroupType.DEFAULT)
        booking1.save(validate: false)

        def booking2 = createBooking(createCustomer(facility), createSlot(createCourt(facility), slot.startTime, slot.endTime))

        def subscriptionCourt = createCourt(facility)
        def subscriptionSlot = createSlot(subscriptionCourt, slot.startTime, slot.endTime)
        def subscriptionCustomer = createCustomer(facility)
        def subscriptionBooking = createBooking(subscriptionCustomer, subscriptionSlot)
        def subscriptionGroup = createBokingGroup()
        subscriptionBooking.group = subscriptionGroup
        subscriptionBooking.save(validate: false)
        subscriptionSlot.subscription = createSubscription(subscriptionCustomer, subscriptionGroup, subscriptionCourt)
        subscriptionSlot.save(validate: false)

        def activityBooking = createBooking(createCustomer(facility), createSlot(createCourt(facility), slot.startTime, slot.endTime))
        activityBooking.group = createBokingGroup(BookingGroupType.ACTIVITY)
        activityBooking.save(validate: false)

        def trainingBooking = createBooking(createCustomer(facility), createSlot(createCourt(facility), slot.startTime, slot.endTime))
        trainingBooking.group = createBokingGroup(BookingGroupType.TRAINING)
        trainingBooking.save(validate: false)

        def competitionBooking = createBooking(createCustomer(facility), createSlot(createCourt(facility), slot.startTime, slot.endTime))
        competitionBooking.group = createBokingGroup(BookingGroupType.COMPETITION)
        competitionBooking.save(validate: false)

        def notAvailableBooking = createBooking(createCustomer(facility), createSlot(createCourt(facility), slot.startTime, slot.endTime))
        notAvailableBooking.group = createBokingGroup(BookingGroupType.NOT_AVAILABLE)
        notAvailableBooking.save(validate: false)

        [booking1, booking2, subscriptionBooking, activityBooking, trainingBooking, competitionBooking, notAvailableBooking]
    }
}
