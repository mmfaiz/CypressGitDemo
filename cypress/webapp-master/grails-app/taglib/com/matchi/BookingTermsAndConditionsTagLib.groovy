package com.matchi

class BookingTermsAndConditionsTagLib {

    def slotService

    def cancellationTerms = { attrs, body ->

        def slot = attrs.slot
        def facility = slot?.court?.facility

        out << message(code: "bookingTermsAndConditions.cancellationTerms.message${slot && slot.isRefundable() ? '1' : '2'}",
                args: [facility.getBookingCancellationLimit()]) << " "

        addLinkToUserAgreement()
    }

    def refundTerms = { attrs, body ->
        def policy = slotService.getSlotRefundPolicy(attrs.slot)

        out << message(code: policy?.code, args: policy?.args)
    }

    private void addLinkToUserAgreement() {
        out << message(code: "bookingTermsAndConditions.cancellationTerms.message3",
                args: [createLink(absolute: true, controller: "home", action: "useragreement", fragment: "Betalning")])
    }

}
