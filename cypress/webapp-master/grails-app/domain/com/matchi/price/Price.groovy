package com.matchi.price
import com.matchi.Customer
import com.matchi.Slot

class Price implements Serializable {
    PriceListConditionCategory priceCategory
    PriceListCustomerCategory customerCategory
    Long price
    String account

    static constraints = {
        account(nullable: true)
    }

    def name = {
        if(!customerCategory || !priceCategory) {
            return "Unknown"
        }
        return customerCategory.getName() + " / " + priceCategory.getName()
    }

    boolean accept(Slot slot, Customer customer) {
        if (slot?.court?.facility?.hasBookingRestrictions() && slot.bookingRestriction &&
                !slot.bookingRestriction.accept(customer, slot)) {
            return false
        }

        def priceCategoryAccepted = priceCategory.accept(slot, customer)
        def customerCategoryAccepted = customerCategory.accept(slot, customer)

        return priceCategoryAccepted && customerCategoryAccepted
    }

    boolean valid() {
        return priceCategory && customerCategory
    }

	static Price create(Long price, PriceListConditionCategory priceCategory, PriceListCustomerCategory customerCategory, String account, boolean flush = false) {
		new Price(account: account, priceCategory: priceCategory, customerCategory: customerCategory, price: price).save(flush: flush, insert: true)
	}

	static boolean remove(PriceListConditionCategory priceCategory, PriceListCustomerCategory customerCategory,
                          boolean flush = false) {
		Price instance = Price.findByCustomerCategoryAndPriceCategory(customerCategory, priceCategory)
		instance ? instance.delete(flush: flush) : false
	}

    static boolean remove(PriceListConditionCategory priceCategory, boolean flush = false) {
		def instances = Price.findAllByPriceCategory(priceCategory)
        instances.each {
            it.delete()
        }
        return true
	}

    def getPriceExVAT() {
        return price - getVATAmount()
    }

    def getVATAmount() {
        return calculateVATAmount(price, getVATAsDouble())
    }

    static Double calculateVATAmount(long priceInclVAT, double vat) {
        if(vat > 0) {
            double vatDec   = vat / 100
            double netDec   = vatDec + 1
            return (priceInclVAT - priceInclVAT / new Double(netDec)).round(2)
        }
        return 0
    }

    def getVAT() {
        return (customerCategory?customerCategory.facility.vat:0)
    }

    Double getVATAsDouble() {
        return new Double((customerCategory?customerCategory.facility.vat:0))
    }

    def isFree() {
        return valid() && price.equals(0l)
    }
}
