package com.matchi

import com.matchi.conditions.*
import com.matchi.coupon.Coupon
import com.matchi.coupon.CouponConditionGroup
import com.matchi.coupon.CouponPrice
import com.matchi.coupon.GiftCard
import com.matchi.coupon.PromoCode
import com.matchi.price.PriceListCustomerCategory
import org.apache.commons.lang.RandomStringUtils
import org.joda.time.LocalTime

import static com.matchi.TestUtils.*

/**
 * @author Sergei Shushkevich
 */
class CouponServiceIntegrationTests extends GroovyTestCase {

    def couponService

    void testSave() {
        def facility = createFacility()
        def category1 = new PriceListCustomerCategory(facility: facility, name: "cat1").save(failOnError: true)
        def category2 = new PriceListCustomerCategory(facility: facility, name: "cat2").save(failOnError: true)
        def coupon = new Coupon(name: RandomStringUtils.randomAlphabetic(10), nrOfTickets: 10, facility: facility)
                .addToPrices(new CouponPrice(price: 100, customerCategory: category1))
                .addToPrices(new CouponPrice(price: 200, customerCategory: category2))

        assert couponService.save(coupon)
        assert 1 == Coupon.countByFacility(facility)
        assert 2 == CouponPrice.countByCoupon(coupon)

        coupon.prices.iterator().next().price = null

        assert couponService.save(coupon)
        assert 1 == CouponPrice.countByCoupon(coupon)
        assert 1 == coupon.prices.size()
    }

    void testCopyOffer() {
        def facility = createFacility()
        def court = createCourt(facility)
        def timeSlotCondition = new TimeSlotCondition(startTime: new LocalTime(), endTime: new LocalTime())
                .save(failOnError: true, flush: true)
        def courtSlotCondition = new CourtSlotCondition().addToCourts(court)
                .save(failOnError: true, flush: true)
        def weekdaySlotCondition = new WeekdaySlotCondition(weekdays: [2, 5])
                .save(failOnError: true, flush: true)
        def conditionSet = new SlotConditionSet()
                .addToSlotConditions(timeSlotCondition)
                .addToSlotConditions(courtSlotCondition)
                .addToSlotConditions(weekdaySlotCondition)
                .save(failOnError: true, flush: true)
        def priceListCategory = new PriceListCustomerCategory(facility: facility, name: "cat1")
                .save(failOnError: true, flush: true)
        def origCoupon = new Coupon(name: "original", nrOfPeriods: 5, nrOfBookingsInPeriod: 10,
                conditionPeriod: Coupon.ConditionPeriod.MONTHLY, totalBookingsInPeriod: true,
                facility: facility, description: "desc", nrOfDaysValid: 100,
                endDate: new Date() + 365, nrOfTickets: 15, availableOnline: true, unlimited: true)
                .addToPrices(new CouponPrice(price: 100, customerCategory: priceListCategory))
                .addToCouponConditionGroups(new CouponConditionGroup(name: "group name")
                        .addToSlotConditionSets(conditionSet))
                .save(failOnError: true, flush: true)

        def newCoupon = couponService.copyOffer(origCoupon, "new name")

        assert newCoupon
        assert 2 == Coupon.countByFacility(facility)
        assert 2 == TimeSlotCondition.count()
        assert 2 == CourtSlotCondition.count()
        assert 2 == WeekdaySlotCondition.count()
        assert newCoupon.name == "new name"
        assert newCoupon.nrOfPeriods == origCoupon.nrOfPeriods
        assert newCoupon.nrOfBookingsInPeriod == origCoupon.nrOfBookingsInPeriod
        assert newCoupon.conditionPeriod == origCoupon.conditionPeriod
        assert newCoupon.totalBookingsInPeriod == origCoupon.totalBookingsInPeriod
        assert newCoupon.facility.id == origCoupon.facility.id
        assert newCoupon.description == origCoupon.description
        assert newCoupon.nrOfDaysValid == origCoupon.nrOfDaysValid
        assert newCoupon.endDate == origCoupon.endDate
        assert newCoupon.nrOfTickets == origCoupon.nrOfTickets
        assert newCoupon.availableOnline == origCoupon.availableOnline
        assert newCoupon.unlimited == origCoupon.unlimited

        assert 1 == newCoupon.prices.size()
        def newPrice = newCoupon.prices.iterator().next()
        def origPrice = origCoupon.prices.iterator().next()
        assert newPrice.id != origPrice.id
        assert newPrice.price == origPrice.price
        assert newPrice.customerCategory.id == origPrice.customerCategory.id

        assert 1 == newCoupon.couponConditionGroups.size()
        def newConditionGroup = newCoupon.couponConditionGroups.iterator().next()
        assert newConditionGroup.name == "group name"
        assert 1 == newConditionGroup.slotConditionSets.size()
        def newConditionSet = newConditionGroup.slotConditionSets.iterator().next()
        assert 3 == newConditionSet.slotConditions.size()

        def newCondition = newConditionSet.slotConditions.find {it.instanceOf(TimeSlotCondition)}
        assert newCondition
        assert newCondition.id != timeSlotCondition.id
        assert newCondition.startTime == timeSlotCondition.startTime
        assert newCondition.endTime == timeSlotCondition.endTime

        newCondition = newConditionSet.slotConditions.find {it.instanceOf(CourtSlotCondition)}
        assert newCondition
        assert newCondition.id != courtSlotCondition.id
        assert newCondition.courts
        assert newCondition.courts.size() == courtSlotCondition.courts.size()
        assert newCondition.courts.iterator().next().id == court.id

        newCondition = newConditionSet.slotConditions.find {it.instanceOf(WeekdaySlotCondition)}
        assert newCondition
        assert newCondition.id != weekdaySlotCondition.id
        assert 2 == newCondition.weekdays.size()
        assert newCondition.weekdays.contains(2)
        assert newCondition.weekdays.contains(5)
    }

    void testFindAnyCouponById() {
        Facility facility = createFacility()
        def category = new PriceListCustomerCategory(facility: facility, name: "cat1").save(failOnError: true)
        def coupon = new Coupon(name: RandomStringUtils.randomAlphabetic(10), nrOfTickets: 10, facility: facility)
                .addToPrices(new CouponPrice(price: 100, customerCategory: category)).save(failOnError: true)

        def giftCard = new GiftCard(name: "TestGift Card", facility: facility, nrOfTickets: 42, availableOnline: true, unlimited: false)
                .save(failOnError: true)

        def promoCode = new PromoCode(code: "FOOBAR", name: "TestPromo Code", facility: facility, nrOfTickets: 42, availableOnline: true, unlimited: false)
                .save(failOnError: true)

        def maybeCoupon = couponService.findAnyCouponById(coupon.id)
        assertTrue( maybeCoupon.class.equals(Coupon) )
        assertTrue( maybeCoupon.id == coupon.id)

        def maybeGiftCard = couponService.findAnyCouponById(giftCard.id)
        assertTrue( maybeGiftCard.class.equals(GiftCard) )
        assertTrue( maybeGiftCard.id == giftCard.id)

        def maybePromocode = couponService.findAnyCouponById(promoCode.id)
        assertTrue( maybePromocode.class.equals(PromoCode) )
        assertTrue( maybePromocode.id == promoCode.id)
    }
}
