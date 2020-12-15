package com.matchi.membership

import com.matchi.Amount
import com.matchi.Facility
import com.matchi.FacilityProperty.FacilityPropertyKey
import com.matchi.price.Price
import org.grails.databinding.BindUsing
import org.joda.time.LocalDate
import org.springframework.context.i18n.LocaleContextHolder

import java.text.SimpleDateFormat

class MembershipType implements Serializable {

    static belongsTo = [ facility: Facility, groupedSubFacility: Facility ]

    String name
    String description

    Long price = 0L

    boolean availableOnline = true
    boolean organizationType
    boolean paidOnRenewal
    Integer renewalStartingGraceNrOfDays    // trial period added in case of failed recurring payment

    Integer validTimeAmount
    TimeUnit validTimeUnit
    Facility groupedSubFacility

    @BindUsing({ obj, source ->
        if (source["startDateYearly"]) {
            return new LocalDate(new SimpleDateFormat("dd MMMM", LocaleContextHolder.getLocale())
                    .parse(source["startDateYearly"]))
        }
    })
    LocalDate startDateYearly

    Integer purchaseDaysInAdvanceYearly

    static constraints = {
        name(blank: false, nullable: false)
        description(nullable: true, maxSize: 255)
        price(nullable: false)
        validTimeAmount(nullable: true, min: 1)
        validTimeUnit(nullable: true, validator: { val, obj ->
            val || !obj.validTimeAmount
        })
        startDateYearly(nullable: true)
        purchaseDaysInAdvanceYearly(nullable: true, min: 1, max: 365)
        renewalStartingGraceNrOfDays(nullable: true)
        groupedSubFacility(nullable: true, validator: { val, obj ->
            if (obj.facility.isMasterFacility()) {
                return val != null
            }
            return true
        })
    }

    static mapping = {
        sort "name":"asc"
    }

    static namedQueries = {
        availableForPurchase { facility ->
            or {
                eq "facility", facility
                if (facility?.masterFacilities && !facility.masterFacilities.isEmpty()) {
                    inList "facility", facility.masterFacilities
                }
            }
            eq "availableOnline", Boolean.TRUE
            isNotNull "price"
            order("price", "desc")
        }
    }

    String toString() { "$name" }

    String createOrderDescription() {
        "${facility.name} ${name}"
    }

    Amount toAmount() {
        new Amount(amount: new BigDecimal(price),
                VAT: Price.calculateVATAmount(price, new Double(facility.vat)))
    }

    boolean isRecurring() {
        paidOnRenewal && facility.isFacilityPropertyEnabled(
                FacilityPropertyKey.FEATURE_RECURRING_MEMBERSHIP)
    }
}
