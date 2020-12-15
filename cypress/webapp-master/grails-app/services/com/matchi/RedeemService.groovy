package com.matchi

import com.matchi.enums.RedeemAt
import com.matchi.enums.RedeemType
import com.matchi.invoice.InvoiceRow
import com.matchi.price.Price
import com.matchi.subscriptionredeem.SlotRedeem
import com.matchi.subscriptionredeem.redeemstrategy.InvoiceRowRedeemStrategy
import org.apache.commons.lang3.StringUtils
import org.hibernate.criterion.CriteriaSpecification

class RedeemService {
    static transactional = true

    def priceListService
    def slotService
    def userService

    def redeem(Booking booking, RedeemType redeemType, User user = null) {
        def slot = booking.slot
        def facility = slot?.court?.facility
        def subscription = slot.subscription
        def redeem = null

        if (subscription && booking?.customer == subscription?.customer) { //Extra safety
            switch (redeemType) {
                case RedeemType.NORMAL:
                    if (facility.subscriptionRedeem?.redeemAt?.equals(RedeemAt.BOOKINGCANCEL)) {
                        redeem = redeemCustomer(subscription.customer, slot, false, user)
                    } else if (booking.order) {
                        try {
                            if (booking.order.price && booking.order.price > 0) {
                                redeem = SlotRedeem.linkUnredeemed(slot, booking.order.price)
                            }
                        } catch (Exception e) {
                            log.error("Unable to create unredeemed SlotRedeem record for booking $booking.id, slot $slot.id, facility: $facility.id", e)
                        }
                    } else {
                        log.error "Unable to properly redeem booking $booking.id, slot $slot.id, facility: $facility.id"
                    }
                    break
                case RedeemType.FULL:
                    redeem = redeemCustomer(subscription.customer, slot, true, user)
                    break
                case RedeemType.EMPTY:
                    redeem = redeemEmpty(booking)
                    break
                default:
                    break
            }
        }

        return redeem
    }

    def redeemEmpty(Booking booking) {
        log.debug("RedeemSubscriber none")
        return SlotRedeem.link(booking.slot)
    }

    def redeemCustomer(Customer customer, Slot slot, Boolean fullRedeem, User currentUser = null) {
        def court = slot.court
        def sport = court.sport
        def facility = court.facility
        def redeemStrategy = facility?.subscriptionRedeem?.strategy
        def extraDesc = ": " + slot.getShortDescription()

        currentUser = currentUser ?: userService.getLoggedInOrSystemUser()

        def price = new Price()

        if (facility.subscriptionRedeem?.strategy?.type?.equals("INVOICE_ROW")
                || facility.subscriptionRedeem?.strategy?.type?.equals("GIFT_CARD")) {

            def sr = SlotRedeem.findBySlotAndRedeemed(slot, false)
            if (sr) {
                price.price = sr.amount
                def createdRedeem = redeemStrategy.redeem(currentUser, customer,
                        price, extraDesc, fullRedeem)
                if (createdRedeem instanceof InvoiceRow) {
                    sr.invoiceRow = createdRedeem
                } else {
                    sr.coupon = createdRedeem
                }
                sr.redeemed = true
                return sr.save()
            }

            def order = slot.booking?.order
            if (order && order.customer == customer) {
                price.price = order.price
            } else {
                log.warn "Redeem customer $customer.id subscription booking on slot $slot.id with default price"
                PriceList priceList = priceListService.getActiveSubscriptionPriceList(facility, sport)
                if (!priceList) {
                    log.info("No price list for subscription")
                    return null
                } else {
                    price = priceList.getBookingPrice(customer, slot)
                }
            }
        }

        log.info("Redeeming subscriber:${customer.fullName()} for ${slot}")
        def createdRedeem = redeemStrategy.redeem(currentUser, customer, price, extraDesc, fullRedeem)

        if (!createdRedeem) {
            return redeemEmpty(slot.booking)
        }

        return SlotRedeem.link(slot, createdRedeem)
    }

    def redeemUnredeemedCancelations(List<Facility> facilities) {
        def slotsToRedeem = slotService.getRedeemableSlots(facilities)

        slotsToRedeem.each { Slot s ->
            Slot.withNewTransaction {
                Slot slotToRedeem = Slot.get(s.id)
                redeemCustomer(slotToRedeem?.subscription?.customer, slotToRedeem, false)
            }
        }
    }

    def getSlotRedeems(filter) {
        SlotRedeem.createCriteria().list(max: filter.max, offset: filter.offset) {
            createAlias("slot", "s")
            createAlias("s.court", "crt")
            createAlias("s.subscription", "sbs", CriteriaSpecification.LEFT_JOIN)
            createAlias("sbs.customer", "c", CriteriaSpecification.LEFT_JOIN)
            createAlias("invoiceRow", "ir", CriteriaSpecification.LEFT_JOIN)
            createAlias("coupon", "cpn", CriteriaSpecification.LEFT_JOIN)

            eq("crt.facility", userService.getLoggedInUser().facility)

            between("dateCreated", filter.start.toDateMidnight().toDateTime(),
                    filter.end.plusDays(1).toDateMidnight().toDateTime())

            if (filter.q) {
                def q = StringUtils.replace(filter.q, "_", "\\_")

                or {
                    like("c.email", "%${q}%")
                    like("c.firstname", "%${q}%")
                    like("c.lastname", "%${q}%")
                    like("c.companyname", "%${q}%")
                    like("c.telephone", "%${q}%")
                    like("c.cellphone", "%${q}%")
                    like("c.contact", "%${q}%")
                    like("c.notes", "%${q}%")
                    sqlRestriction("number like ?", ["%${q}%" as String])
                    sqlRestriction("concat(firstname,' ',lastname) like ?", ["%${q}%" as String])
                }
            }

            if (filter.redeemed != null) {
                eq("redeemed", filter.redeemed)
            }

            filter.sort.tokenize(",").each {
                order(it, filter.order)
            }
        }
    }

    void updateRedeemedPrice(Long invoiceRowId, BigDecimal price, InvoiceRowRedeemStrategy strategy) {
        def invoiceRow = InvoiceRow.get(invoiceRowId)
        def oldPrice = invoiceRow.price

        log.info "Updating price of invoice row $invoiceRowId by using normal redeem rule and order price $price"

        strategy.setNormalRedeemPrice(invoiceRow, price)
        if (invoiceRow.save()) {
            log.info "Price of invoice row $invoiceRowId was changed from $oldPrice to $invoiceRow.price"
        } else {
            log.warn "Unable to update price of invoice row $invoiceRowId because of validation errors"
        }
    }
}
