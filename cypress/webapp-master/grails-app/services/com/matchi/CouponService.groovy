package com.matchi

import com.matchi.api.Code
import com.matchi.conditions.SlotConditionSet
import com.matchi.coupon.*
import com.matchi.orders.Order
import com.matchi.orders.OrderRefund
import com.matchi.payment.PaymentException
import grails.transaction.NotTransactional
import groovy.sql.GroovyRowResult
import org.codehaus.groovy.grails.commons.GrailsDomainClass
import org.codehaus.groovy.grails.orm.hibernate.cfg.GrailsDomainBinder
import org.hibernate.Hibernate
import org.joda.time.LocalDate
import org.springframework.http.HttpStatus

import java.math.RoundingMode
import java.text.DecimalFormat

class CouponService {
    static transactional = true

    def groovySql
    def springSecurityService
    def grailsApplication
    def customerService
    def facilityService

    CustomerCouponTicket consumeTicket(CustomerCoupon customerCoupon, Order order,
            String ticketDescription = null, Long purchasedObjectId = null) {
        if (customerCoupon.isValid(order.price)) {
            def ticket = customerCoupon.consumeTicket(order,
                    springSecurityService.currentUser, ticketDescription, purchasedObjectId)
            customerCoupon.save(failOnError: true, flush: true)
            return ticket
        }
        return null
    }

    @NotTransactional
    List<PromoCode> getActivePromoCodes(Facility facility, Object sort = "name", Object order = "asc") {
        return PromoCode.createCriteria().list([sort: sort, order: order]) {
            eq("facility", facility)
            ge("endDate", new Date())
        }
    }

    @NotTransactional
    PromoCode getValidPromoCode(Facility facility, String code) {
        return PromoCode.createCriteria().get() {
            eq("facility", facility)
            eq("code", code)
            le("startDate", new Date())
            ge("endDate", new Date())
        }
    }

    @NotTransactional
    PromoCode getValidPromoCode(Long promoCodeId, Facility facility) {
        return PromoCode.createCriteria().get() {
            eq("id", promoCodeId)
            eq("facility", facility)
            le("startDate", new Date())
            ge("endDate", new Date())
        }
    }

    @NotTransactional
    def checkPromoCode(Facility facility, User user, String promoCode, List<Slot> slots = null) {
        PromoCode validPromoCode = getValidPromoCode(facility, promoCode as String)
        DecimalFormat df = new DecimalFormat()
        df.setMinimumFractionDigits(0)
        if (validPromoCode && (!slots || slots.every { Slot slot -> validPromoCode.accept(slot)})) {
            if (isPromoCodeUsed(user, validPromoCode))
                return [valid: false, errorCode: Code.ALREADY_USED_PROMO_CODE, message: "paymentController.process.errors.promoCodeAlreadyUsed"]
            else
                return [valid: true, id: validPromoCode.id, discountPercent:  validPromoCode.discountPercent,
                        discountAmount: validPromoCode.discountAmount]
        }
        else
            return [valid: false, errorCode: Code.INVALID_PROMO_CODE, message: "paymentController.process.errors.invalidPromoCode"]
    }

    @NotTransactional
    void usePromoCodeForOrders(PromoCode promoCode, List<Order> orders, int decimalPoints) {
        if (promoCode?.discountPercent) {
            orders.each { order ->
                OrderRefund refund = new OrderRefund(order: order,
                        amount: (promoCode.discountPercent.divide(100) * order.price).setScale(decimalPoints, RoundingMode.HALF_DOWN),
                        note: "PromoCode id:${promoCode.id}",
                        promoCode:promoCode.code)
                order.addToRefunds(refund)
                order.save()
            }
        }
        else if (promoCode?.discountAmount) {
            def totalPrice = orders.sum { it.price }
            def discount = promoCode.discountAmount.min(totalPrice)
            def discountProportion = discount/totalPrice
            def totalDiscounted = 0
            orders.each { order ->
                OrderRefund refund = new OrderRefund(order: order,
                        amount: (order.price * discountProportion).setScale(decimalPoints, RoundingMode.HALF_DOWN),
                        note: "PromoCode id:${promoCode.id}",
                        promoCode:promoCode.code)
                order.addToRefunds(refund)
                totalDiscounted += refund.amount
                order.save()
            }
            Double extraPenny = discount - totalDiscounted
            OrderRefund lastRefund = orders.last().refunds.first()
            lastRefund.amount = lastRefund.amount + extraPenny
            lastRefund.save()
        }
    }

    @NotTransactional
    boolean isPromoCodeUsed(User user, PromoCode promoCode) {
        return CustomerCoupon.findByCouponAndCustomer(promoCode, Customer.findByUserAndFacility(user, promoCode.facility)) != null;
    }

    @NotTransactional
    List<PromoCode> getExpiredPromoCodes(Facility facility, Object sort = "name", Object order = "asc") {
        return PromoCode.createCriteria().list([sort: sort, order: order]) {
            eq("facility", facility)
            lt("endDate", new Date())
        }
    }

    @NotTransactional
    def getValidCouponsByCustomerAndSlots(Customer customer, Collection<Slot> slots, BigDecimal price = null, type = Coupon.class, ignoreSlotsAmountRestriction = false) {
        def coupons = getValidCouponsByCustomer(customer, type, price)

        def validCoupons = coupons.findAll() { CustomerCoupon customerCoupon ->
            return customerCoupon.accept(slots, ignoreSlotsAmountRestriction)
        }

        return validCoupons
    }

    @NotTransactional
    def getValidCouponsByUserAndSlots(User user, Collection<Slot> slots,
                                      BigDecimal price = null, type = Coupon.class, Boolean ignoreSlotsAmountRestriction = false) {
        if (!slots || slots.isEmpty()) {
            return []
        }

        def facilities = slots.collect() { it.court.facility } as Set

        if (facilities.size() > 1) {
            throw new IllegalArgumentException("Can not get valid coupons from slots of different facilities")
        }

        Customer customer = ((user && facilities.first()) ? Customer.findByUserAndFacility(user, facilities.first()) : null) ?: new Customer (user: user, facility: facilities.first())

        List<CustomerCoupon> coupons = getValidCouponsByCustomerUser(customer, type, price)
        log.info "Customer coupons count: ${coupons.size()}"
        def validCoupons = coupons.findAll() { CustomerCoupon customerCoupon ->
            log.info "Customer coupon ${customerCoupon} instance of ${customerCoupon.metaClass}"
            return customerCoupon.accept(slots, ignoreSlotsAmountRestriction)
        }

        return validCoupons
    }

    @NotTransactional
    Collection<CustomerCoupon> getValidCouponsByCustomerUser(Customer customer, Class type = Coupon.class, BigDecimal price = null) {
        customer ? customerService.findHierarchicalUserCustomers(customer, true).collectMany([] as List<CustomerCoupon>) { Customer it ->
            getValidCouponsByCustomer(it, type, price)
        } : []
    }

    @NotTransactional
    private Collection<CustomerCoupon> getValidCouponsByCustomer(Customer customer, Class type = Coupon.class, BigDecimal price = null) {
        customer.customerCoupons.findAll { CustomerCoupon customerCoupon ->
            return customerCoupon.coupon.class == type && customerCoupon.isValid(price)
        }
    }

    CustomerCouponTicket refundCustomerCoupon(CustomerCouponTicket consumedTicket, Integer amount) {
        refundCustomerCoupon(consumedTicket.customerCoupon, consumedTicket.type.refundType,
                amount, consumedTicket.description, consumedTicket.purchasedObjectId)
    }

    CustomerCouponTicket refundCustomerCoupon(CustomerCoupon customerCoupon,
            CustomerCouponTicket.Type refundType, Integer amount = null,
            String description = null, Long purchasedObjectId = null) {
        log.info("Refunding ticket on customers coupon ${customerCoupon?.id}")
        customerCoupon.addTicket(springSecurityService.currentUser,
                Hibernate.getClass(customerCoupon.coupon) == GiftCard && amount ? amount : 1,
                refundType, description, purchasedObjectId)
    }

    CustomerCouponTicket refundCustomerCoupon(Booking booking, boolean forceAnnul) {
        log.info("Refunding customer when cancelling ${booking}, (${(forceAnnul ? "annulling" : "reversing")})")

        CustomerCouponTicket ticket

        if (!booking.slot.isRefundable() && !forceAnnul) {
            log.info("The booking is already confirmed, unable to refund customer, removing booking from ticket")
        } else {
            def consumedTicket = CustomerCouponTicket.findByPurchasedObjectIdAndType(
                    booking.id, CustomerCouponTicket.Type.BOOKING)

            if (consumedTicket) {
                def payment = booking.payment

                log.info("Ticket found, returning ticket")
                ticket = refundCustomerCoupon(consumedTicket,
                        ((Integer) (payment ? payment.amountFormattedAmountOnly()?.intValue() : null)))

                if (payment) {
                    if (forceAnnul) {
                        payment.dateAnnulled = new Date()
                    } else {
                        payment.dateReversed = new Date()
                    }
                }
            } else {
                log.info("No coupon found, skipping coupon refund")
            }
        }

        return ticket
    }

    def removeCustomerCoupon(CustomerCoupon customerCoupon) {
        log.info("Removing coupon (${customerCoupon.coupon.name}) from customer (${customerCoupon.customer})")
        customerCoupon.customer.removeFromCustomerCoupons(customerCoupon)
        customerCoupon.coupon.removeFromCustomerCoupons(customerCoupon)
        customerCoupon.delete(flush: true)
    }

    @NotTransactional
    def getRelatedCustomerCoupons(def slotCondition) {
        def rows = groovySql.rows("""
            select cc.id as customerCouponId
                from slot_condition sc
                left join slot_condition_set_slot_condition sss on sss.slot_condition_id = sc.id
                left join slot_condition_set scs on sss.slot_condition_set_slot_conditions_id = scs.id
                left join coupon_condition_groups_slot_conditions_sets ccc on ccc.slot_condition_set_id = scs.id
                left join coupon_condition_group ccg on ccc.coupon_condition_group_id = ccg.id
                left join coupon c on ccg.coupon_id = c.id
                left join customer_coupon cc on c.id = cc.coupon_id
                where sc.id = :id;
        """, [id: slotCondition.id])

        log.debug("Found ${rows.size()} related customer coupons....")

        groovySql.close()
        if (rows.size() > 0) {
            return CustomerCoupon.where() { id in rows?.collect { it.customerCouponId } }
        }

        return null
    }

    void updateTicketIfExists(Order order, Booking booking) {
        def payment = order.payments.find { it.type == "Coupon" }
        if (payment && payment.ticket && !payment.ticket.purchasedObjectId) {
            payment.ticket.purchasedObjectId = booking.id
            payment.ticket.addDescription(booking.slot.shortDescription)
            payment.ticket.save()
        }
    }

    Offer save(Offer coupon) {
        coupon.prices.findAll {
            it.price == null
        }.each {
            coupon.removeFromPrices(it)
            if (it.id) {
                it.delete()
            }
        }

        coupon.save()
    }

    Offer copyOffer(Offer originalOffer, String newOfferName) {
        def newOffer = Hibernate.getClass(originalOffer).newInstance()
        newOffer.properties = originalOffer.properties
        newOffer.name = newOfferName
        newOffer.customerCoupons = []
        newOffer.couponConditionGroups = []
        newOffer.prices = []

        originalOffer.couponConditionGroups.each { origGroup ->
            def ccg = new CouponConditionGroup(name: origGroup.name)
            origGroup.slotConditionSets.each { origCondSet ->
                def scs = new SlotConditionSet()
                origCondSet.slotConditions.each { origCond ->
                    def cond = Hibernate.getClass(origCond).newInstance()

                    origCond.domainClass.persistentProperties.each { p ->
                        def val = origCond."$p.name"
                        if (val) {
                            if (val instanceof Collection) {
                                val.each {
                                    cond."addTo${p.name.capitalize()}"(it)
                                }
                            } else {
                                cond."${p.name}" = val
                            }
                        }
                    }

                    scs.addToSlotConditions(cond)
                }
                ccg.addToSlotConditionSets(scs)
            }
            newOffer.addToCouponConditionGroups(ccg)
        }

        originalOffer.prices.each { origPrice ->
            newOffer.addToPrices(new CouponPrice(price: origPrice.price,
                    customerCategory: origPrice.customerCategory))
        }

        newOffer.save()
    }

    @NotTransactional
    CustomerCoupon getExpiresFirstCustomerCoupon(
            Customer customer, Offer offer, BigDecimal price) {
        if (!customer || !offer) {
            return null
        }

        Collection<CustomerCoupon> cc = []

        customerService.findHierarchicalUserCustomers(customer).each { Customer c ->
            cc.addAll(c?.customerCoupons)
        }

        cc.findAll {
            it.coupon.id == offer.id && it.isValid(price)
        }.sort {
            it.expireDate ? it.expireDate.toDate().time : Long.MAX_VALUE
        }[0]
    }

    @NotTransactional
    protected def findAnyCouponById(Long id) {
        if (!id) {
            log.debug("Input error id: ${id}")
            return null
        }
        List<GroovyRowResult> rows = groovySql.rows("""select class from coupon 
            where id=:id 
        """, [id:id])
        if (!rows || rows.size()<1) {
            log.debug("There is no coupon id:${id}")
            return null
        }
        String classFromDb = rows.get(0).get("class")
        if (!classFromDb || classFromDb.isEmpty()) {
            return null
        }
        switch(classFromDb) {
            case Coupon.MAPPING_VALUE :
                return Coupon.get(id)
            case GiftCard.MAPPING_VALUE :
                return GiftCard.get(id)
            case PromoCode.MAPPING_VALUE :
                return PromoCode.get(id)
            default:
                log.error("No domain class for id:${id}")
                return null
        }
    }

    @NotTransactional
    protected def findCouponByTypeAndId(String type, def paramId) {
        Long id;
        if (paramId instanceof String) {
            id = Long.parseLong(paramId)
        } else {
            id = paramId
        }
        if (type && !type.isEmpty()) {
            try {
                return grailsApplication.getDomainClass("com.matchi.coupon.${type}").clazz.get(id)
            } catch(Exception e) {
                log.info("Error getting `type` from params. Using fallback:", e)
                return findAnyCouponById(id)
            }
        } else {
            return findAnyCouponById(id)
        }
    }
}
