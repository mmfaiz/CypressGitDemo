package com.matchi.admin

import com.matchi.Customer
import com.matchi.Facility
import com.matchi.GenericController
import com.matchi.FacilityProperty

class AdminFacilityPropertiesController extends GenericController {

    def index(Long id) {
        def facility = Facility.get(id)
        [facility: facility]
    }

    def update(Long id) {
        def facility = Facility.get(id)

        FacilityProperty.FacilityPropertyKey.values().each { FacilityProperty.FacilityPropertyKey propertyKey ->
            def value = params.get(propertyKey.toString())

            if (value) {
                //validate SUBSCRIPTION_REMINDER_HOURS
                if (propertyKey == FacilityProperty.FacilityPropertyKey.SUBSCRIPTION_REMINDER_HOURS) {
                    def integerValue = params.int(propertyKey.toString())
                    if (integerValue > 99 || integerValue < 1) {
                        flash.error = g.message(code: 'facility.property.SUBSCRIPTION_REMINDER_HOURS.error')
                    } else {
                        facility.setFacilityProperty(propertyKey, value)
                    }
                } else if (propertyKey == FacilityProperty.FacilityPropertyKey.BACKHANDSMASH_CUSTOMER_ID) {
                    facility.setFacilityProperty(propertyKey, getBackhandSmashValue(value))
                } else if (propertyKey == FacilityProperty.FacilityPropertyKey.MULTIPLE_PLAYERS_NUMBER) {
                    facility.setFacilityProperty(propertyKey, value.inspect())
                } else {
                    facility.setFacilityProperty(propertyKey, value)
                }

                log.info propertyKey.toString() + ": " + params.get(propertyKey.toString())
            } else {
                if(!propertyKey.readOnly)
                    facility.removeFacilityProperty(propertyKey)
            }
        }

        if (facility.hasBookingLimitPerCustomer() && facility.hasBookingLimitPerCourtGroup()) {
            flash.error = message(code: "facility.property.MaxBookings.error")
            facility.setFacilityProperty(FacilityProperty.FacilityPropertyKey.FEATURE_MAXIMUM_NUMBER_OF_BOOKINGS_PER_USER, "0")
            facility.setFacilityProperty(FacilityProperty.FacilityPropertyKey.FEATURE_MAXIMUM_NUMBER_OF_BOOKINGS_PER_COURT_GROUP, "0")
            redirect action: "index", params: [id: params.id]
        } else {
            flash.message = message(code: "adminFacilityProperties.update.success")
            redirect action: "index", params: [id: params.id]
        }
    }

    private String getBackhandSmashValue(String value) {
        def values = []
        def errors = ""
        if (value) {
            value.replaceAll(" ", "").tokenize(",").eachWithIndex { String token, int index ->
                try {
                    if (token) {
                        if (Customer.get(Long.parseLong(token))) {
                            values << token
                        } else {
                            errors += g.message(code: 'facility.property.BACKHANDSMASH_CUSTOMER_ID.customer.error', args: [token]) + "</br>"
                        }
                    }
                } catch (NumberFormatException nfe) {
                    errors += g.message(code: 'facility.property.BACKHANDSMASH_CUSTOMER_ID.error', args: [token]) + "</br>"
                }
            }
            flash.error = errors
            getAndValidateBackhandSmashValue(values)
        } else {
            ""
        }
    }

    private String getAndValidateBackhandSmashValue(ArrayList values) {
        def backhandshamshValue = ""
        values.eachWithIndex { String id, int index ->
            if (values.size() > index + 1) {
                backhandshamshValue += id + ","
            } else {
                backhandshamshValue += id
            }
        }
        backhandshamshValue
    }

    def remove() {

    }
}
