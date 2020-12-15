package com.matchi.integration

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.matchi.integration.events.Event
import com.matchi.integration.events.EventWrapper
import com.matchi.integration.json.DateAdapter
import com.matchi.integration.json.JodaDateAdapter
import grails.transaction.Transactional
import org.apache.kafka.clients.producer.*
import org.apache.kafka.common.KafkaException
import org.apache.kafka.common.errors.NetworkException
import org.apache.kafka.common.header.Header
import org.apache.kafka.common.header.internals.RecordHeader
import org.joda.time.DateTime

import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

@Transactional
class KafkaProducerService {
    public static final String HEADER_REQUEST_ID = "requestId"
    public static final String HEADER_KEY = "key"

    def grailsApplication
    def props
    def producer

    private boolean enabled

    Gson jsonMarshaller

    @PostConstruct
    void configure() {
        enabled = grailsApplication.config.kafka.enabled
        if (!enabled) {
            log.info("Kafka not enabled, messages will not be sent.")
            return
        }
        def bootstrapServers = grailsApplication.config.kafka.props.bootstrap.servers

        props = grailsApplication.config.kafka.props.toProperties()
        props.put(ProducerConfig.LINGER_MS_CONFIG, "100")
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, "163840")
        props.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, "20")
        props.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, "2000")
        producer = new KafkaProducer<String, String>(props)

        def builder = new GsonBuilder()
        builder.registerTypeAdapter(Date.class, new DateAdapter())
        builder.registerTypeAdapter(DateTime.class, new JodaDateAdapter())
        this.jsonMarshaller = builder.create()
        log.info("Configured Apache Kafka Producer with kafka.servers: ${bootstrapServers}")
    }

    @PreDestroy
    void destroy() {
        producer.close()
    }

    def <T extends Event> void send(EventWrapper<T> wrapper) {
        if (!enabled) {
            return
        }
        def json = jsonMarshaller.toJson(wrapper)
        send(wrapper.getTopic(), wrapper.getKey(), json)
    }

    def send(String topic, String key, String message) {
        if (!enabled) {
            return
        }
        String requestId = UUID.randomUUID().toString()

        List<Header> headers = Arrays.asList(
                new RecordHeader(HEADER_REQUEST_ID, requestId.getBytes()),
                new RecordHeader(HEADER_KEY, key.getBytes())
        )

        producer.send(new ProducerRecord<String, String>(topic, null, key, message, headers),
                { RecordMetadata metadata, Exception e ->
                    if (e != null) {
                        log.error("Unable to send message to Kafka", e)
                        if (e instanceof NetworkException) {
                            log.info("Re-creating KafkaProducer")
                            producer.flush()
                            producer.close()
                            def reconnected = false
                            while (!reconnected) {
                                try {
                                    producer = new KafkaProducer<String, String>(props)
                                    reconnected = true
                                } catch (KafkaException err) {
                                    log.error("Unable to re-connect", err)
                                }
                            }
                        }
                    } else {
                        log.info("Message sent to Kafka: topic=${topic}, key=${key}, requestId=${requestId}, partition=${metadata?.partition()}, offset=${metadata?.offset()}")
                        log.info("Kafka message: topic=${topic} message=${message}")
                    }
                } as Callback
        )
    }
}
