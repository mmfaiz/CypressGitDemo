package com.matchi.api

import com.matchi.Facility
import grails.converters.JSON

class HealthController {
    boolean shuttingDown = false
    // Example of one use case: Used by AWS Load Balancer to check if instance healthy
    // and then can start accepting request when return status code 200. Returning
    // status code 4XX or 5XX indicates a not healthy instance.
    def status() {
        log.debug("Health status")

        // Test if database connection works.
        try {
            Integer facilityCount = Facility.count()
        }
        catch (Exception exception) {
            // Render 503 status code indicating instance not healthy yet
            render(status: 503, text: 'No database connection')
            return
        }

        LinkedHashMap result = [:]
        result << [application: "OK"]
        result << [database: "OK"]
        result << [version: grailsApplication.metadata.getApplicationVersion()]
        render result as JSON
    }

    def preStop() {
        shuttingDown = true

        Thread.sleep(15000)
        render "Ok"
    }

    def readiness() {
        if (!shuttingDown) {
            render "Ok"
        } else {
            render(status: 503, text: 'No database connection')
        }
    }

    def liveness() {
        if (!shuttingDown) {
            render "Ok"
        } else {
            render(status: 503, text: 'No database connection')
        }
    }
}
