package com.matchi.subscriptionredeem

import com.matchi.Slot
import com.matchi.coupon.Offer
import com.matchi.invoice.InvoiceRow
import org.joda.time.DateTime

class SlotRedeem {

    static belongsTo = [ slot: Slot ]

    DateTime dateCreated
    DateTime lastUpdated

    InvoiceRow invoiceRow
    Offer coupon

    boolean redeemed = true
    BigDecimal amount

    static constraints = {
        invoiceRow(nullable: true)
        coupon(nullable: true)
        amount(nullable: true, validator: { val, obj ->
            val || obj.redeemed
        })
    }

    static mapping = {
        autoTimestamp(true)
    }

    /**
     * Link empy redeem (when facility has chosen not to redeem booking)
     * @param Slot to be redeemed
     * @return Created SlotRedeem
     */
    static def link(Slot slot) {
        def sr = findBySlot(slot)

        if (!sr) {
            sr = new SlotRedeem()
            sr.slot  = slot
            sr.save()
        }

        return sr
    }

    static def link(Slot slot, InvoiceRow row) {
        def sr = findBySlotAndRedeemed(slot, false)

        if (!sr) {
            sr = new SlotRedeem()
            sr.slot  = slot
            sr.redeemed = true
            sr.invoiceRow = row
            sr.save()
            return sr
        }

        if (!sr.redeemed) {
            sr.redeemed = true
            sr.invoiceRow = row
            sr.save()
        }

        return sr
    }

    static def link(Slot slot, Offer coupon) {
        def sr = findBySlotAndRedeemed(slot, false)

        if (!sr) {
            sr = new SlotRedeem()
            sr.slot  = slot
            sr.redeemed = true
            sr.coupon = coupon
            sr.save()
            return sr
        }

        if (!sr.redeemed) {
            sr.redeemed = true
            sr.coupon = coupon
            sr.save()
        }

        return sr
    }

    static SlotRedeem linkUnredeemed(Slot slot, BigDecimal amount) {
        def sr = findBySlotAndRedeemed(slot, false)

        if (sr) {
            sr.amount = amount
        } else {
            sr = new SlotRedeem(slot: slot, amount: amount, redeemed: false)
        }

        sr.save(failOnError: true)
    }
}
