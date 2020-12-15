package com.matchi
import com.matchi.coupon.Coupon
import com.matchi.enums.RedeemAt
import com.matchi.subscriptionredeem.SlotRedeem
import com.matchi.subscriptionredeem.SubscriptionRedeem
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.junit.Before
import org.junit.Test

@TestFor(RedeemService)
@Mock([Customer, Facility, Court, Sport, Slot, Booking, Subscription, SlotRedeem, SubscriptionRedeem])
class RedeemServiceTests {

    def mockPriceListService
    def mockSlotService
    def mockUserService

    Facility facility
    Court court
    Sport sport
    Customer customer
    User user
    Subscription subscription
    SubscriptionRedeem subscriptionRedeem
    Coupon coupon

    @Before
    void setUp() {
        mockPriceListService = mockFor(PriceListService)
        mockSlotService      = mockFor(SlotService)
        mockUserService      = mockFor(UserService)

        service.priceListService = mockPriceListService.createMock()
        service.slotService      = mockSlotService.createMock()
        service.userService      = mockUserService.createMock()

        subscriptionRedeem = new SubscriptionRedeem(redeemAt: RedeemAt.SLOTREBOOKED)

        sport = new Sport(id: 1l, position: 0)
        court = new Court(id: 1l, sport: sport)
        facility = new Facility(id: 1l, sports: [sport], courts: [court], subscriptionRedeem: subscriptionRedeem)

        user = new User(id: 1l)
        customer = new Customer(id: 1l, facility: facility, user: user)

        subscription = new Subscription(id: 1l, customer: customer)

        coupon = new Coupon(id: 1l, facility: facility)
    }

    @Test
    void testRedeemEmpyCreatesEmpySlotRedeem() {
        Slot slot = new Slot(id: 1l)
        Booking booking = new Booking(id: 1l, slot: slot)

        SlotRedeem slotRedeem = service.redeemEmpty(booking)

        assert slotRedeem.slot == slot
        assert !slotRedeem.coupon
        assert !slotRedeem.invoiceRow
    }
}
