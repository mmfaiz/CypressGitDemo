package com.matchi.coupon

import com.matchi.Amount
import com.matchi.Customer
import com.matchi.Facility
import com.matchi.Slot
import com.matchi.User
import com.matchi.price.Price
import com.matchi.price.PriceListCustomerCategory
import org.apache.commons.lang.RandomStringUtils
import org.joda.time.DateTime

/**
 * @author Michael Astreiko
 */
abstract class Offer implements Serializable {

    private static final long serialVersionUID = 12L

    static belongsTo = [facility: Facility]
    static hasMany = [customerCoupons: CustomerCoupon, couponConditionGroups: CouponConditionGroup, prices: CouponPrice]

    String name
    String description
    Integer nrOfDaysValid
    Date endDate
    Integer nrOfTickets

    boolean availableOnline = false
    boolean unlimited = false

    Date dateCreated
    Date lastUpdated

    boolean archived = false
    /**
     * Checks if this coupon is valid for this slot. Validity is configured with slot condition sets.
     * @param slot
     * @return true if valid otherwise false
     */
    def accept(Slot slot) {
        if (couponConditionGroups && !couponConditionGroups.isEmpty()) {
            def validGroups = couponConditionGroups.any() { it.accept(slot) }
            return validGroups
        } else {
            true // no condition groups - accept
        }
    }

    static constraints = {
        name(nullable: false, blank: false)
        description(nullable: true, maxSize: 1000)
        nrOfDaysValid(nullable: true, blank: true)
        endDate(nullable: true)
        nrOfTickets(nullable: false, blank: false)
        availableOnline(nullable: false)
        unlimited(nullable: false)
        archived(nullable: true)
    }

    static mapping = {
        couponConditionGroups sort: 'id', batchSize: 10
        table 'coupon'
        endDate type: "date"
        cache true
    }

    def createOrderDescription() {
        return "${facility.name} ${name}, ${nrOfTickets} klipp";
    }

    def createOrderNumber(User user) {
        return "${id}-${user.id}-${RandomStringUtils.random(7, false, true)}"
    }

    Amount toAmount(Customer customer) {
        long priceInclVAT = getPrice(customer).longValue()
        double vatAmount = Price.calculateVATAmount(priceInclVAT, new Double((facility.vat ?: 0)))
        return new Amount(
                amount: priceInclVAT.toBigDecimal(),
                VAT: vatAmount.toBigDecimal())
    }

    Integer getPrice(Customer customer, boolean returnNullOnMissingPrice = false) {
        def cp = CouponPrice.findAllByCoupon(this, [sort: "price", fetch: [customerCategory: "join"]]).find { p ->
            p.customerCategory.accept(this, customer)
        }

        if (cp) {
            return cp.price
        } else if (returnNullOnMissingPrice) {
            return null
        } else {
            log.info("Could not find a fallback price for coupon ${name} ${id}")
            return 0
        }
    }

    Integer numMissingPrices() {
        def categories = PriceListCustomerCategory.findAllByFacility(facility)
        categories.size() - CouponPrice.countByCouponAndCustomerCategoryInList(this, categories)
    }

    public DateTime getExpireDate() {
        return endDate ? new DateTime(endDate) :
                (nrOfDaysValid ? new DateTime().plusDays(nrOfDaysValid) : null)
    }

    String toString() { "$name" }

    abstract String getOfferTypeString()

    static namedQueries = {
        facilityCoupons { Facility facility ->
            or {
                eq "facility", facility
                if (!facility.masterFacilities?.isEmpty()) {
                    inList "facility", facility.masterFacilities
                }
            }
            eq "archived", false
        }
        availableForPurchase { facility ->
            facilityCoupons(facility)
            eq "availableOnline", Boolean.TRUE
            eq "archived", false
            or {
                isNull "endDate"
                and {
                    isNotNull "endDate"
                    ge "endDate", new Date()
                }
            }

            isNotEmpty "prices"
        }

    }
}
