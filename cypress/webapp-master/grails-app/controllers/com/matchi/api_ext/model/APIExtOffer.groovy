package com.matchi.api_ext.model


import com.matchi.coupon.Offer
import org.joda.time.LocalDate

class APIExtOffer {
    Long id
    String type
    String name
    String description
    Boolean availableOnline
    Boolean archived
    Integer nrOfDaysValid
    LocalDate endDate
    Integer nrOfTickets
    Boolean unlimited
    List<APIExtOfferPrice> prices

    APIExtOffer(Offer offer) {
        this.id = offer.id
        this.type = offer.class.simpleName
        this.name = offer.name
        this.description = offer.description
        this.availableOnline = offer.availableOnline
        this.archived = offer.archived
        this.nrOfDaysValid = offer.nrOfDaysValid
        this.endDate = new LocalDate(offer.endDate)
        this.nrOfTickets = offer.nrOfTickets
        this.unlimited = offer.unlimited

        this.prices = new ArrayList<>()
        offer.prices?.each {price ->
            prices.add(new APIExtOfferPrice(price.customerCategory.name, price.price))
        }

    }
}
