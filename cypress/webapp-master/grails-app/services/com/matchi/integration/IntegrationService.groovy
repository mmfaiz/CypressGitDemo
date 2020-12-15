package com.matchi.integration

import com.matchi.Customer
import com.matchi.Facility
import com.matchi.events.EventInitiator
import com.matchi.events.EventType
import com.matchi.events.OrderEventRecord
import com.matchi.marshallers.integration.AIPMarshaller
import com.matchi.orders.Order
import grails.converters.JSON
import grails.transaction.Transactional

@Transactional
class IntegrationService {
    public static final String SYNC_FACILITY_TOPIC = 'webapp.maip.sync.facility'
    public static final String EVENT_CUSTOMER_TOPIC = 'webapp.maip.event.customer'
    public static final String EVENT_ORDER_TOPIC = 'order'

    def kafkaProducerService

    def send(Facility facility) {
        def key = SYNC_FACILITY_TOPIC + "-" + facility.id
        def json = null

        JSON.use(AIPMarshaller.NAMED_CONFIG) {
            json = (facility as JSON).toString(true)
        }

        kafkaProducerService.send(SYNC_FACILITY_TOPIC, key, json)
    }

    def send(Customer customer) {
        def key = SYNC_FACILITY_TOPIC + "-" + customer.id
        def json = null

        JSON.use(AIPMarshaller.NAMED_CONFIG) {
            json = (customer as JSON).toString(true)
        }

        log.info(json)

        kafkaProducerService.send(EVENT_CUSTOMER_TOPIC, key, json)
    }

    def send(EventType eventType, EventInitiator initiator, Order order) {
        def key = EVENT_ORDER_TOPIC + "-" + order.id
        def json = null

        JSON.use(AIPMarshaller.NAMED_CONFIG) {
            json = (new OrderEventRecord(eventType, initiator, order) as JSON).toString(true)
        }

        kafkaProducerService.send(EVENT_ORDER_TOPIC, key, json)
    }
}
