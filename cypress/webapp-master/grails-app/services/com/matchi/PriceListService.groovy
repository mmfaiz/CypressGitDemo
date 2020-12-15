package com.matchi

import com.matchi.payment.BookingRestrictionException
import com.matchi.payment.InvalidPriceException
import com.matchi.price.*
import grails.compiler.GrailsCompileStatic
import grails.transaction.Transactional
import org.joda.time.DateTime

class PriceListService {

    static transactional = false
    def dateUtil
    def messageSource

    @Transactional
    def createPriceList(Facility facility, PriceList priceList) {

        priceList.facility = facility

        def standardPriceCondition = new PriceListConditionCategory(name: messageSource.getMessage("facilityPriceListCondition.form.message2",null, new Locale(facility.language)), defaultCategory: true)
        priceList.addToPriceListConditionCategories(standardPriceCondition)
        priceList.startDate = dateUtil.beginningOfDay(priceList.startDate).toDate()

        if(!priceList.save()) {
            throw new IllegalArgumentException("Could not create price list (${priceList.errors})")
        }

        return priceList
    }

    @Transactional
    def updatePriceList(PriceList priceList) {
        priceList.startDate = dateUtil.beginningOfDay(priceList.startDate).toDate()
        priceList.save()
    }

    /**
     * Return priceLists which starts after current and last one from them will be active then.
     *
     * @param priceList
     * @return List of priceLists
     */
    List getOverlappingPriceList(PriceList priceList) {

        if(!priceList.startDate) return []
        if(!priceList.facility) return []
        if(!priceList.sport) return []

        def priceLists = PriceList.createCriteria().list {
            ge("startDate", priceList.startDate)

            if(priceList.id != null) {
                notEqual("id", priceList.id)
            }
            eq("sport", priceList.sport)
            eq("facility", priceList.facility)
            eq("subscriptions", priceList.subscriptions)

            order("startDate", "desc")
        }

        return priceLists
    }

    def getByFacility(Facility facility) {
        def priceLists = PriceList.createCriteria().list {
            eq("facility", facility)
            and{
                order('subscriptions','desc')
                order("startDate", "desc")
            }
        }

        return priceLists
    }

    /**
     * The logic to get an active price list is in to get the pricelist with the latest "valid from"
     *
     * @param slot
     * @return
     */
    private PriceList getActivePriceList(Slot slot, boolean isSubscription = false) {
        PriceList.createCriteria().list {
            eq("facility", slot.court.facility)
            eq("sport", slot.court.sport)
            le("startDate", slot.startTime)
            eq("subscriptions", isSubscription)

            order('startDate', 'desc')
        }?.getAt(0)
    }

    def getActiveSubscriptionPriceList(Facility facility, Sport sport) {
        return PriceList.createCriteria().list {
            eq("facility", facility)
            eq("sport", sport)
            le("startDate", new Date())
            eq("subscriptions", Boolean.TRUE)

            order('startDate', 'desc')
        }?.getAt(0)
    }

    @Transactional
    void delete(PriceList priceList) {
        log.info("Deleting price list ${priceList.name}")
        priceList.facility.removeFromPricelists(priceList)

        List<PriceListConditionCategory> conditionCategories = priceList.priceListConditionCategories.toList()

        conditionCategories.each { PriceListConditionCategory category ->

            List<AbstractPriceCondition> categoryConditionsList = category.conditions.toList()
            categoryConditionsList.each { AbstractPriceCondition condition ->
                if (condition instanceof CourtPriceCondition) {

                    List<Court> courts = condition.courts.toList()
                    courts.each { Court court ->
                        court.removeFromCourtPriceConditions(condition)
                    }
                }

                category.removeFromConditions(condition)
                condition.delete()

            }

            List<Price> categoryPricesList = category.prices.toList()
            categoryPricesList.each { Price price ->
                category.removeFromPrices(price)
                price.delete()
            }

            category.delete()

            priceList.removeFromPriceListConditionCategories(category)

        }

        priceList.delete()
    }

    def getBookingPrices(def slots, Customer customer) {
        def result = []
        def pricesAccumulated = [:]
        def total = 0L

        slots.each { slot ->
            Price price = getBookingPrice(slot, customer)
            total += price.price
            result << price

            if(pricesAccumulated.get(price)) {
                pricesAccumulated.get(price) << [price: price, isSubscription: slot.subscription ? true : false]
            } else {
                pricesAccumulated.put(price, [[price: price, isSubscription: slot.subscription ? true : false]])
            }

        }

        return [total: total, rows: pricesAccumulated]
    }

    Price getBookingPrice(Slot slot, User user) {
        def facility = slot.court.facility
        def customer = ((user && facility) ? Customer.findByUserAndFacility(user, facility) : null) ?: new Customer(facility: facility)

        return getBookingPrice(slot, customer)
    }

    @GrailsCompileStatic
    Price getBookingPrice(Slot slot, Customer customer) {
        PriceList priceList = getActivePriceList(slot, (slot.subscription && slot.subscription.customer == customer))

        if(!priceList) {
            Price price = new Price()
            price.price = 0
            return price
        }

        return getBookingPrice(slot, customer, priceList)
    }

    @GrailsCompileStatic
    Price getBookingPrice(Slot slot, Customer customer, PriceList priceList) {
        return priceList.getBookingPrice(customer, slot)
    }

    @Transactional
    def deletePriceListConditionCategory(PriceListConditionCategory category) {
        def priceList = category.pricelist
        priceList.removeFromPriceListConditionCategories(category)
        priceList.save(failOnError: true)

        category.pricelist = null
        // Remove conditions
        clearCategoryConditions(category)
        Price.remove(category)
        category.delete(failOnError: true)
    }

    def clearCategoryConditions(PriceListConditionCategory category) {
        def conditionsToBeRemoved = []

        category.conditions.each {
            conditionsToBeRemoved << it
        }

        conditionsToBeRemoved.each { BookingPriceCondition condition ->
            condition.prepareForDelete()
        }
    }

    PriceList copyAndSave(PriceList src, PriceList dest) {
        dest.facility = src.facility
        dest.sport = src.sport
        dest.description = src.description
        dest.subscriptions = src.subscriptions
        dest.type = src.type

        src.priceListConditionCategories.each { cc ->
            def category = new PriceListConditionCategory(name: cc.name, defaultCategory: cc.defaultCategory)

            cc.conditions.each { c ->
                def condition = c.class.newInstance()
                if (c instanceof CourtPriceCondition) {
                    c.courts.each {
                        condition.addToCourts(it)
                    }
                } else {
                    condition.properties = c.properties
                }
                category.addToConditions(condition)
            }

            cc.prices.each { p ->
                category.addToPrices(new Price(price: p.price, account: p.account,
                        priceCategory: category, customerCategory: p.customerCategory))
            }

            dest.addToPriceListConditionCategories(category)
            dest.save(flush: true) // to preserve ID order
        }

        dest.save()
    }

    Map getAvgBookingPrices(List slots, List customers) {
        def pricesAccumulated = [:]
        def total = 0L

        slots.each { slot ->
            Price price = getAvgBookingPrice(slot, customers)
            total += price.price

            if(pricesAccumulated.get(price)) {
                pricesAccumulated.get(price) << [price: price, isSubscription: slot.subscription ? true : false]
            } else {
                pricesAccumulated.put(price, [[price: price, isSubscription: slot.subscription ? true : false]])
            }
        }

        return [total: total, rows: pricesAccumulated]
    }

    Price getAvgBookingPrice(Slot slot, User user) {
        def facility = slot.court.facility
        def customer = ((user && facility) ? Customer.findByUserAndFacility(user, facility) : null) ?: new Customer(facility: facility)

        getAvgBookingPrice(slot, [customer])
    }

    @GrailsCompileStatic
    Price getAvgBookingPrice(Slot slot, List<Customer> customers, Map<String, Map> priceCalcLog = null) {
        addMinimumCustomers(customers, slot)

        boolean firstFoundCustomerCategory = false
        PriceListCustomerCategory priceListCustomerCategory = null

        boolean firstFoundPriceCategory = false
        PriceListConditionCategory priceCategory = null
        def courtName = slot.court.name
        def remainingPlayers = 0
        def remainingPlayerPrice

        def price = (Long) customers.sum { Customer c ->
            c = c ?: new Customer()
            def priceInstance = getBookingPrice(slot, c)
            if (!firstFoundCustomerCategory && priceInstance?.customerCategory) {
                priceListCustomerCategory = priceInstance.customerCategory
                firstFoundCustomerCategory = true
            }

            if (!firstFoundPriceCategory && priceInstance?.priceCategory) {
                priceCategory = priceInstance.priceCategory
                firstFoundPriceCategory = true
            }

            if (priceCalcLog != null) {
                if (priceCalcLog[courtName] == null) {
                    priceCalcLog[courtName] = [players: [:], remainingPlayerPrice: 0L, slotsCount: 0L]
                }
                if (c.email) {
                    def playerPrice = (Long) priceCalcLog[courtName].players[c.email] ?: 0
                    priceCalcLog[courtName].players[c.email] = playerPrice + (priceInstance?.price ?: 0)
                } else {
                    remainingPlayers++
                    if (remainingPlayerPrice == null) {
                        remainingPlayerPrice = priceInstance?.price ?: 0
                    }
                }
            }

            priceInstance?.price
        }

        if (priceCalcLog != null) {
            priceCalcLog[courtName].remainingPlayers = remainingPlayers
            priceCalcLog[courtName].slotsCount = ((Long)priceCalcLog[courtName].slotsCount ?: 0L) + 1L
            if (remainingPlayerPrice) {
                priceCalcLog[courtName].remainingPlayerPrice = remainingPlayerPrice +
                        (Long) priceCalcLog[courtName].remainingPlayerPrice
            }
        }

        new Price(price: price / customers.size(), customerCategory: priceListCustomerCategory, priceCategory: priceCategory)
    }

    List getActivePriceLists(Facility facility) {
        def pricelists = getActivePriceListsProjections(facility)

        if (pricelists) {
            pricelists = PriceList.withCriteria {
                eq("facility", facility)
                or {
                    pricelists.each { p ->
                        and {
                            eq("startDate", p[0])
                            eq("sport", p[1])
                            eq("subscriptions", p[2])
                        }
                    }
                }
                and{
                    order('subscriptions','desc')
                    order("startDate", "desc")
                }
            }
        }

        pricelists
    }

    List getInactivePriceLists(Facility facility) {
        def pricelists = getActivePriceListsProjections(facility)

        if (pricelists) {
            pricelists = PriceList.withCriteria {
                eq("facility", facility)
                le("startDate", new Date())
                pricelists.each { p ->
                    not {
                        and {
                            eq("startDate", p[0])
                            eq("sport", p[1])
                            eq("subscriptions", p[2])
                        }
                    }
                }
                and{
                    order('subscriptions','desc')
                    order("startDate", "desc")
                }
            }
        }
    }

    List getUpcomingPriceLists(Facility facility) {
        PriceList.withCriteria {
            eq("facility", facility)
            gt("startDate", new Date())
            and{
                order('subscriptions','desc')
                order("startDate", "desc")
            }
        }
    }

    private List getActivePriceListsProjections(Facility facility) {
        PriceList.withCriteria {
            eq("facility", facility)
            le("startDate", new Date())
            projections {
                max("startDate")
                groupProperty("sport")
                groupProperty("subscriptions")
            }
        }
    }

    private void addMinimumCustomers(List customers, Slot slot) {
        Map<String, String> multiplePlayersNumber = slot.court.facility.getMultiplePlayersNumber()
        def playersNumber = (String) multiplePlayersNumber[slot.court.sport.id.toString() + "_" + slot.court.getCourtTypeAttributeByMultiplePlayersPrice()?.value] ?: multiplePlayersNumber[slot.court.sport.id.toString()]
        if (playersNumber) {
            def extraCustomersNumber = playersNumber.toInteger() - customers.size()
            if (extraCustomersNumber > 0) {
                extraCustomersNumber.times {
                    customers << new Customer()
                }
            }
        }
    }

    /**
     * Calculates the specific price for a slot depending on the supplied parameters.
     * Should be used as the general price-for-slot-method.
     * @param slot
     * @param customer
     * @param playerEmails
     * @param customerCategoryId
     * @return
     * @throws InvalidPriceException if price is invalid
     * @throws BookingRestrictionException if slot has a requirement profile not met by customer
     */
    @GrailsCompileStatic
    Price getPriceForSlot(Slot slot, Customer customer, List<String> playerEmails,
            Long customerCategoryId, Map<String, Map> priceCalcLog = null) throws InvalidPriceException, BookingRestrictionException {
        Price price
        /**
         * Special case if facility recalculates price for a sport. Then it returns the average price
         * per player. If player A should pay 100 SEK and player B (who is a member) should pay 50 SEK,
         * the price will be 75 SEK. Extra players are added with player emails.
         */

        Boolean customerAdded = false
        List<Customer> customers = []
        if (slot.court.facility.recalculateMultiplePlayersPrice(slot.court.sport)) {

            customers = playerEmails?.collect {
                def email = it.trim().toLowerCase()
                if (customer?.email == email) {
                    if (!customerAdded) {
                        customerAdded = true
                        return customer
                    }
                    return new Customer(email: email)
                }

                Customer c = Customer.findByFacilityAndEmailAndArchived(slot.court.facility, email, false)
                c ?: new Customer(email: email)
            }

            if (!customers) {
                customers = [customer ?: new Customer()]
            }

            price = withSelectedCustomerCategory(slot, customerCategoryId) {
                getAvgBookingPrice(slot, customers, priceCalcLog)
            }
        } else {
            price = withSelectedCustomerCategory(slot, customerCategoryId) {
                getBookingPrice(slot, (customer ?: new Customer()))
            }
        }

        if (slot?.court?.facility?.hasBookingRestrictions() && slot?.bookingRestriction &&
                !slot?.bookingRestriction?.accept(customers ? customers.first() : customer ?: new Customer(), slot)) {
            DateTime validFrom = new DateTime(slot.startTime).minusMinutes(slot?.bookingRestriction?.validUntilMinBeforeStart)
            throw new BookingRestrictionException(validFrom, slot.bookingRestriction.requirementProfiles.collect{ it.name }.join(", "))
        }

        // TODO: It is bad practice to use exceptions like this but the current logic requires it. Fix that.
        if(!price.valid()) {
            throw new InvalidPriceException()
        }

        return price
    }

    /**
     * Creates a slotId-to-price lookup table with prices.
     * Suitable when listing lots of prices for lots of slots.
     * @param slots
     * @param customer
     * @param playerEmails
     * @param customerCategoryId
     * @return
     * @throws InvalidPriceException
     */
    @GrailsCompileStatic
    Map<String, Long> getPricesForSlots(List<Slot> slots, Customer customer, List<String> playerEmails,
            Long customerCategoryId, Map<String, Map> priceCalcLog = null) throws InvalidPriceException {
        return slots.collectEntries { Slot slot ->
            return [(slot.id): getPriceForSlot(slot, customer, playerEmails, customerCategoryId, priceCalcLog).price]
        }
    }

    /**
     * Helper method to fetch price using a closure, to adjust for customer category price if such is selected.
     * @param slot
     * @param customerCategoryId
     * @param closure
     * @return
     */
    private Price withSelectedCustomerCategory(Slot slot, Long customerCategoryId, Closure<Price> closure) {
        if (customerCategoryId) {
            PriceListCustomerCategory category = PriceListCustomerCategory.findByIdAndFacilityAndOnlineSelect(customerCategoryId, slot.court.facility, true)
            if (category) {
                slot.booking = new Booking(selectedCustomerCategory: category)
            }
        }

        try {
            return closure.call()
        } finally {
            slot.booking?.discard()
            slot.discard()
        }
    }
}
