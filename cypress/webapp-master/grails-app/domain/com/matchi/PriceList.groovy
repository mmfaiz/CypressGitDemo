package com.matchi

import com.matchi.price.Price
import com.matchi.price.PriceListConditionCategory
import com.matchi.price.PriceListCustomerCategory

class PriceList implements Serializable {

    static hasMany = [priceListConditionCategories: PriceListConditionCategory]
    static belongsTo = [facility: Facility, sport: Sport]

    String name
    String description
    Date startDate
    Date dateCreated
    Date lastUpdated
    Boolean subscriptions = Boolean.FALSE
    Type type = Type.SLOT_BASED

    static constraints = {
        name(nullable: false, blank: false)
        description(nullable: true)
        startDate(nullable: false)
    }

    static mapping = {
        autoTimestamp true
        sort 'dateCreated'
        priceListConditionCategories sort: "id"
    }

    Price getBookingPrice(Customer customer, Slot slot) {
        def price
        def prices = getPrices()

        if (slot?.booking?.selectedCustomerCategory?.forceUseCategoryPrice) {
            price = prices.find {
                it.customerCategory?.id == slot.booking.selectedCustomerCategory.id &&
                        it.accept(slot, customer)
            }
        }

        if (!price) {
            prices.find { Price p ->
                if (p.accept(slot, customer)) {
                    price = p
                    return true
                }
                return false
            }
        }

        if (!price) {
            price = new Price()
            price.price = 0;
            log.info("Could not find a fallback price for price list ${name} ${id}")
        } else if (price.price != 0 && type == Type.HOUR_BASED) {
            price = new Price(price: getHourBasedPrice(price.price, slot.startTime, slot.endTime),
                    customerCategory: price.customerCategory, priceCategory: price.priceCategory,
                    account: price.account)
        }

        return price
    }

    /**
     * Retrieves all prices associated with this price list order by price
     * @return A list of prices sorted by lowest price first
     */
    List getPrices() {
        def prices = [] as List
        priceListConditionCategories.each { priceCategory ->
            prices.addAll(priceCategory.prices)
        }

        Collections.sort(prices, new Comparator<Price>() {
            int compare(Price t, Price t1) {
                return t.price.compareTo(t1.price)
            }
        })
        return prices
    }

    def numMissingPrices() {
        int numMissing = 0
        List<PriceListCustomerCategory> customerCategories = PriceListCustomerCategory.available(facility).listDistinct()

        customerCategories.each { customerCategory ->
            priceListConditionCategories.each { priceCategory ->
                def price = Price.findByCustomerCategoryAndPriceCategory(customerCategory, priceCategory)
                if (price == null) {
                    numMissing++;
                }
            }
        }
        return numMissing
    }

    protected Long getHourBasedPrice(Long basePrice, Date start, Date end) {
        def multiplier = (end.time - start.time) / DateUtil.MILLISECONDS_PER_HOUR
        (basePrice * multiplier).setScale(0, BigDecimal.ROUND_HALF_UP).toLong()
    }

    enum Type {
        SLOT_BASED, HOUR_BASED
    }
}
