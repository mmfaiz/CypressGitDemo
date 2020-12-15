package com.matchi.integration

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.matchi.Booking
import com.matchi.RecordingPurchase
import com.matchi.RecordingStatus
import com.matchi.events.EventType
import com.matchi.play.Recording
import grails.transaction.Transactional
import grails.validation.Validateable
import groovy.json.JsonSlurper
import groovy.transform.ToString
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.clients.consumer.KafkaConsumer

import javax.annotation.PostConstruct

@Transactional
class KafkaConsumerService {
    public static final String HEADER_REQUEST_ID = "requestId"

    def grailsApplication
    def playService
    KafkaConsumer consumer

    private boolean enabled
    Properties props

    def jsonSlurper = new JsonSlurper()
    ObjectMapper mapper = new ObjectMapper()

    @PostConstruct
    void configure() {
        enabled = grailsApplication.config.kafka.enabled
        if (!enabled) {
            log.info("Kafka not enabled, messages will not be consumed.")
            return
        }
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

        def bootstrapServers = grailsApplication.config.kafka.props.bootstrap.servers

        props = grailsApplication.config.kafka.props.toProperties()
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false")
        props.put(ConsumerConfig.CLIENT_ID_CONFIG, "webapp")
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "webapp")

        log.info("Configured Apache Kafka Consumer with kafka.servers: ${bootstrapServers}")
    }

    def startConsumer() {
        if (!this.enabled) {
            log.error("Kafka service is disabled")
            return false
        }

        Map<String, Runnable> topics = [:]
        topics.put("bookingsystem.cmd.mediastatus.0", { String s ->
            handleKafkaMediaCheckResponse(s)
        })

        topics.put("url", { String s ->
            handleKafkaUrlMessage(s)
        })

        log.info("Starting Kafka Consumer on topics ${topics.keySet()}")
        consumer = new KafkaConsumer<String, String>(props)
        consumer.subscribe(topics.keySet() as List<String>)

        consumer.withCloseable {
            log.info("Start polling Kafka Consumer on topics ${topics.keySet()}")
            //noinspection GroovyInfiniteLoopStatement
            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(5000)

                for (record in records) {
                    Runnable recordHandler = topics.get(record.topic())
                    if (recordHandler) {
                        try {
                            recordHandler(record.value())
                            consumer.commitSync()
                        } catch (Throwable throwable) {
                            log.error("Error when handling kafka records", throwable)
                        }
                    } else {
                        log.error("Topic ${record.topic()} has nothing to run")
                        consumer.commitSync()
                    }
                }
            }
        }
    }

    void storeRecordingUrl(Long bookingId, KafkaUrlResponse urlResponse) {
        RecordingPurchase.withNewTransaction { status ->
            Recording recording = playService.getRecordingFromBooking(Booking.get(bookingId))
            if (recording?.recordingPurchase) {
                recording.recordingPurchase.setArchiveUrl(urlResponse.data.newUrl)
                recording.recordingPurchase.save(flush: true, failOnError: true)

                log.info("Kafka event stored new archive url (${urlResponse.data.newUrl}) on recording with bookingId = ${recording.bookingId}")
            } else {
                log.error("Could not find recordingPurchase for bookingId ${bookingId}")
            }
        }
    }

    void handleKafkaUrlMessage(String record) {
        Map value = jsonSlurper.parseText(record) as Map

        KafkaUrlResponse urlResponse = mapper.convertValue(value as Map, KafkaUrlResponse)

        log.info("Kafka url event recieved with eventType ${urlResponse.eventType}")

        if (urlResponse.eventType == EventType.DOWNLOAD_OF_REMOTE_FILE_COMPLETED) {
            String bookingId = urlResponse.data.metadata['recording.bookingId']

            storeRecordingUrl(Long.parseLong(bookingId), urlResponse)
        }
    }

    void handleKafkaMediaCheckResponse(String record) {
        Map value = jsonSlurper.parseText(record) as Map

        KafkaMediaCheckerResponse mediaCheckResponse = mapper.convertValue(value as Map, KafkaMediaCheckerResponse)

        log.info("Kafka Media Checker event received with booking id ${mediaCheckResponse.bookingId}")

        Booking.withNewTransaction {
            Booking booking = Booking.get(mediaCheckResponse.bookingId)
            if (booking) {
                if (!booking.recordingStatus) {
                    booking.recordingStatus = new RecordingStatus()
                }
                booking.recordingStatus.status = mediaCheckResponse.status
                booking.recordingStatus.mediaUrl = mediaCheckResponse.mediaUrl
                booking.save(failOnError: true, flush: true)
                log.info("Updated booking (${booking.id}) with recording status = ${booking.recordingStatus.status}")
            }
        }
    }
}

@ToString
@Validateable(nullable = true)
class KafkaMediaCheckerResponse extends Expando {
    String bookingId
    Integer count
    RecordingStatus.Status status
    String mediaUrl

    def propertyMissing(name, value) {
        // nothing
    }
}

@ToString
@Validateable(nullable = true)
class KafkaUrlResponse extends Expando {
    EventType eventType
    String timeStamp
    KafkaUrlDataResponse data

    def propertyMissing(name, value) {
        // nothing
    }
}

class KafkaUrlDataResponse extends Expando {
    @JsonProperty("previous")
    String previousUrl
    @JsonProperty("new")
    String newUrl
    Map<String, String> metadata

    def propertyMissing(name, value) {
        // nothing
    }
}
