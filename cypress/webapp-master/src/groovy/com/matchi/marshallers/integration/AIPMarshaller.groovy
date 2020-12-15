package com.matchi.marshallers.integration

import com.matchi.Booking
import com.matchi.Customer
import com.matchi.Facility
import com.matchi.FacilityProperty
import com.matchi.RecordingPurchase
import com.matchi.Subscription
import com.matchi.User
import com.matchi.activities.Participation
import com.matchi.coupon.CustomerCoupon
import com.matchi.dynamicforms.Submission
import com.matchi.events.EventRecord
import com.matchi.membership.Membership
import com.matchi.orders.Order
import grails.converters.JSON
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter

import javax.annotation.PostConstruct

class AIPMarshaller {
    public static final String NAMED_CONFIG = "aip"
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormat.forPattern("yyyy-MM-dd")
    @PostConstruct
    void register() {
        JSON.createNamedConfig(NAMED_CONFIG) {
            it.registerObjectMarshaller(Facility) { Facility facility ->
                marshallFacility(facility)
            }
            it.registerObjectMarshaller(Customer) { Customer customer ->
                marshallCustomer(customer)
            }
            it.registerObjectMarshaller(EventRecord) { EventRecord eventRecord ->
                marshallEventRecord(eventRecord)
            }
        }
    }

    /**
     * Facility
     * @param facility
     * @return
     */
    def marshallFacility(Facility facility) {
        [
                id: facility.id,
                name: facility.name,
                active: facility.active,
                fortnox: fortnox(facility)

        ]
    }

    def fortnox(Facility facility) {
        [
                enabled: facility.hasFortnox(),
                accessToken: facility.getFacilityPropertyValue(FacilityProperty.FacilityPropertyKey.FORTNOX3_ACCESS_TOKEN),
                authorizationCode: facility.getFacilityPropertyValue(FacilityProperty.FacilityPropertyKey.FORTNOX3_AUTHORIZATION_CODE)
        ]
    }

    /**
     * Customer
     * @param customer
     * @return
     */
    def marshallCustomer(Customer customer) {
        [
                facilityId: customer.facilityId,
                number: customer.number,
                personalNumber: customer.personalNumber,
                firstname: customer.firstname,
                lastname: customer.lastname,
                memberships: memberships(customer)
        ]
    }

    def marshallEventRecord(EventRecord eventRecord) {
        [
                eventType: eventRecord.eventType?.name(),
                initiator: eventRecord.initiator,
                timeStamp: eventRecord.timeStamp.toString(),
                data: eventRecord.getData()
        ]
    }

    def memberships(Customer customer) {
        def memberships = []

        customer.memberships
                .findAll()
                .sort {it.startDate}
                .each {

                    def membership = [
                            type: it.type.name,
                            startDate: it.startDate.toString(),
                            endDate: it.endDate.toString()
                    ]

                    memberships << membership
        }

        memberships
    }

}
