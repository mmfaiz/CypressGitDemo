package com.matchi

import com.matchi.invoice.InvoiceRow
import com.matchi.orders.Order
import grails.util.Holders
import org.joda.time.DateTime
import org.joda.time.LocalTime

class Subscription implements Serializable {

    static belongsTo = [ customer: Customer, bookingGroup: BookingGroup ]
    static hasMany   = [ slots: Slot ]

    SortedSet slots
    String description
    Date dateCreated
	Date lastUpdated
    Date copiedDate = null

    //Subscription metadata
    int weekday
    LocalTime time
    int timeInterval = 1
    boolean showComment = false

    InvoiceRow invoiceRow
    Order order

    Status status = Status.ACTIVE
    Court court
    String accessCode

    boolean reminderEnabled = true

    static constraints = {
        description(nullable: true)

        weekday(nullable: false)
        time(nullable: false)
        copiedDate(nullable: true)
        timeInterval(nullable: false)
        showComment(nullable: false)
        invoiceRow(nullable: true)
        court(nullable: false)
        order(nullable: true)
        accessCode(nullable: true)
    }

    static mapping = {
        order cascade: 'none'
    }

    def createInvoiceRow(PriceList priceList, User createdBy, String description, BigDecimal discount = 0) {
        def row = new InvoiceRow()
        row.customer = customer
        row.createdBy = createdBy
        row.amount = 1
        row.price = getPrice(priceList)
        row.discount = discount
        row.description = description

        row
    }

    def createInvoiceDescription(def template = "Abonnemang (%s)") {
        def dateInformation

        if (firstSlot() && lastSlot()) {
            def startTime = new DateTime(firstSlot()?.startTime)
            def endTime = new DateTime(lastSlot()?.startTime)
            dateInformation = "${startTime.toString("yyyy-MM-dd")} - ${endTime.toString("yyyy-MM-dd")}"
        } else {
            dateInformation = ""
        }

        return String.format(template, dateInformation)
    }

    def getPriceFromOrder(){
        return Booking.createCriteria().get {
            createAlias("order", "o")
            projections {
                sum("o.price")
            }
            eq("group", bookingGroup)
        }
    }

    def getPrice() {
        def applicationContext = Holders.applicationContext
        def priceListService = applicationContext.getBean("priceListService")
        def pricelist = priceListService.getActivePriceList(
            this.firstSlot(), true) ?: priceListService.getActiveSubscriptionPriceList(
            customer.facility, slots.first().court.sport)
        return pricelist ? getPrice(pricelist) : -1
    }

    /**
     * Calculates the price on the whole subscription based on the prices in pricelist
     * @param priceList
     * @returns BigDecimal the price on the subscription
     */
    def getPrice(PriceList priceList) {
        BigDecimal total = new BigDecimal(0)
        slots.each { Slot s ->
            def price = priceList.getBookingPrice(customer, s)
            if(price?.price != null) {
                total = total.plus(price.price)
            } else {
                throw new PriceNotFoundException()
            }
        }
        total
    }

    Integer getNumberOfSlots() {
        return this.slots?.size()
    }

    Long getPricePerBooking() {
        def slot = slots.first()
        if (slot.booking?.order) {
            return slot.booking.order.price
        } else {
            def applicationContext = Holders.applicationContext
            def priceListService = applicationContext.getBean("priceListService")
            def pricelist = priceListService.getActiveSubscriptionPriceList(
                    customer.facility, slot.court.sport)
            return pricelist ? pricelist.getBookingPrice(customer, slot)?.price : null
        }
    }

    Slot firstSlot() {
        return this.slots.first()
    }

    Slot lastSlot() {
        return this.slots.last()
    }

    public static enum Status {
        ACTIVE, CANCELLED
    }
}
