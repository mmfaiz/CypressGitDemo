package com.matchi.sms

import com.matchi.Facility
import com.matchi.FacilityProperty
import com.matchi.User
import com.matchi.activities.Activity
import com.matchi.activities.ActivityOccasion
import com.matchi.activities.Participation
import com.matchi.matex.MatexClient
import org.joda.time.LocalDate

import javax.annotation.PostConstruct

class SmsService {
    static transactional = false

    private static final String CLIENT_REF_FACILITY_PREFIX = "facility-"

    def facilityService
    def grailsApplication

    def messageSource
    MatexClient client

    def send(User to, String message) {
        send(to.telephone, message)
    }

    def send(String to, String message) {
        log.info("Sending SMS notification from MATCHi to ${to}")
        send("matchi", "MATCHi", to, message)
    }

    def send(Facility facility, String to, String message) {

        if(!facility.hasSMS()) {
            throw new IllegalArgumentException("Facility ${facility.shortname} is not allowed to send SMS")
        }

        String clientRef = "${CLIENT_REF_FACILITY_PREFIX}${facility.shortname}"
        String from = facility.getFacilityPropertyValue(FacilityProperty.FacilityPropertyKey.SMS_FROM.toString()).toString()

        if(!from) {
            throw new IllegalStateException("Facility ${facility.shortname} has no SMS from field configured, please see admin.")
        }

        send(clientRef, from, to, message)

    }

    def report(LocalDate from, LocalDate to) {
        def response = client.report(from, to)

        def result = response?.json?.findAll() {
            it.clientRef?.startsWith(CLIENT_REF_FACILITY_PREFIX)
        }.collect {

            def facility = facilityByClientRef(it.clientRef)

            if(facility) {
                [facility: facility, count: it.count ]
            }

        }

        return result
    }

    protected def send(String clientRef, String from, String to, String message) {

        if(!clientRef) {
            throw new IllegalArgumentException("No clientRef defined")
        }

        log.info("Sending SMS: ${clientRef} ${from} ${to}: ${message}")

        def status = "ok"
        def statusText = "Message sent"

        try {

            def response = this.client.send(from, to, message, clientRef)

            def success = response?.json?.messagesSent.findAll { it.sentStatus != "0" }.isEmpty()

            if(success) {
                status = "ok"
                statusText = "Meddelandet skickat"
            } else {
                status = "error"
                statusText = response?.json?.messagesSent.collect { it.errorText + " (code: ${it.sentStatus})" }
            }


        } catch(Throwable t) {
            log.error("Error while sending SMS", t)
            status = "error"
            statusText = "Internt fel, kontakta support@matchi.se"
        }

        return [from: from, status: status, text: statusText, "to": to, success: status.equals("ok")]
    }

    def facilityByClientRef(String clientRef) {
        if(clientRef.startsWith(CLIENT_REF_FACILITY_PREFIX)) {
            return Facility.findByShortname(clientRef.substring(CLIENT_REF_FACILITY_PREFIX.length(), clientRef.length()))
        }

        return null
    }

    def sendParticipationCancelledSMS(Participation participation) {
        if (!participation.occasion.activity.facility.hasSMS() || !participation.customer.cellphone) {
            return
        }

        Activity activity = participation.occasion.activity

        Locale locale = new Locale(participation.customer.user?.language ?: activity.facility.language)
        List<Object> args = [activity.name, activity.facility.name, participation.occasion.startDateTime]
        String message = messageSource.getMessage("templates.emails.html.automaticParticipationCancelledMail.text", args.toArray(), locale)

        send(activity.facility, participation.customer.cellphone, message)
    }

    @PostConstruct
    public void configure() {
        def host = grailsApplication.config?.matchi?.matex?.host
        def user = grailsApplication.config?.matchi?.matex?.user
        def password = grailsApplication.config?.matchi?.matex?.secret

        log.info("Configuring matex client at ${host} with user ${user}")
        this.client = new MatexClient(user, password, host)
    }
}
