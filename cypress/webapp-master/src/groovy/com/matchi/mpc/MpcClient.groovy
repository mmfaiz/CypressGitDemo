package com.matchi.mpc

import com.matchi.Booking
import com.matchi.FacilityProperty
import grails.plugins.rest.client.RestBuilder
import grails.plugins.rest.client.RestResponse
import org.joda.time.DateTime

class MpcClient {

    String username
    String password
    String host

    RestBuilder rest

    MpcClient(String host, String username, String password) {
        this.host     = host
        this.username = username
        this.password = password

        rest = new RestBuilder(connectTimeout:3000, readTimeout:30000)
    }

    RestResponse add(Booking booking) {
        def customer = booking?.customer
        def slot     = booking?.slot
        def court    = slot?.court
        def facility = court?.facility

        def startWithGrace = new DateTime(slot?.startTime).minusMinutes(facility.getMlcsGraceMinutesStart()).toString("yyyy-MM-dd'T'HH:mm:ss")
        def endWithGrace   = new DateTime(slot?.endTime).plusMinutes(facility.getMlcsGraceMinutesEnd()).toString("yyyy-MM-dd'T'HH:mm:ss")

        RestResponse response = rest.post(host + "/api/code") {
            auth username, password
            json {
                userId      = customer?.id
                userEmail   = customer?.email
                bookingId   = booking?.id
                nodeId      = facility?.id
                objectId    = court?.id
                start       = startWithGrace
                end         = endWithGrace
            }
        }

        checkErrors(response)

        return response
    }

    RestResponse move(Booking booking) {
        def slot     = booking?.slot
        def court    = slot?.court
        def facility = court?.facility

        def startWithGrace = new DateTime(slot?.startTime).minusMinutes(facility.getMlcsGraceMinutesStart()).toString("yyyy-MM-dd'T'HH:mm:ss")
        def endWithGrace   = new DateTime(slot?.endTime).plusMinutes(facility.getMlcsGraceMinutesEnd()).toString("yyyy-MM-dd'T'HH:mm:ss")

        RestResponse response = rest.post(host + "/api/code/${booking?.id}/move") {
            auth username, password
            json {
                objectId    = court?.id
                start       = startWithGrace
                end         = endWithGrace
            }
        }

        checkErrors(response)

        return response
    }

    RestResponse delete(Long bookingId) {

        RestResponse response = rest.delete(host + "/api/code/${bookingId}") {
            auth username, password
        }

        checkErrors(response)

        return response
    }

    RestResponse verify(Long bookingId) {

        RestResponse response = rest.get(host + "/api/code/${bookingId}") {
            auth username, password
        }

        checkErrors(response)

        return response
    }

    RestResponse verifyMultiple(List<Long> ids) {

        RestResponse response = rest.post(host + "/api/code/validate") {
            auth username, password
            json {
                bookingIds = ids
            }
        }

        checkErrors(response)

        return response
    }

    RestResponse listFuture(Long facilityId) {

        RestResponse response = rest.get(host + "/api/code/getForNode/${facilityId}") {
            auth username, password
        }

        checkErrors(response)

        return response
    }

    RestResponse resend(Long facilityId) {

        RestResponse response = rest.post(host + "/api/code/resend/${facilityId}") {
            auth username, password
        }

        checkErrors(response)

        return response
    }

    RestResponse reset(Long facilityId) {

        RestResponse response = rest.delete(host + "/api/node/${facilityId}") {
            auth username, password
        }

        checkErrors(response)

        return response
    }

    RestResponse getNode(Long facilityId) {

        RestResponse response = rest.get(host + "/api/node/${facilityId}") {
            auth username, password
        }

        checkErrors(response)

        return response
    }

    RestResponse createNode(Long facilityId, Long nodeProvider) {

        RestResponse response = rest.post(host + "/api/node") {
            auth username, password
            json {
                id = facilityId
                provider = nodeProvider
            }
        }

        checkErrors(response)

        return response
    }

    RestResponse updateNode(Long facilityId, Map<String, String> conf) {

        RestResponse response = rest.post(host + "/api/node/${facilityId}") {
            auth username, password
            json {
                id = facilityId
                configuration = conf
            }
        }

        checkErrors(response)

        return response
    }

    RestResponse getProviderStatus(def providerId) {

        RestResponse response = rest.get(host + "/api/health/" + providerId + "/status") {
            auth username, password
        }

        checkErrors(response)

        return response
    }

    RestResponse listProviders() {

        RestResponse response = rest.get(host + "/api/provider") {
            auth username, password
        }

        checkErrors(response)

        return response
    }

    RestResponse listNodes() {

        RestResponse response = rest.get(host + "/api/node") {
            auth username, password
        }

        checkErrors(response)

        return response
    }

    static void handleErrors(RestResponse response) {
        int status = response.getStatus()

        // If resource is not configured in MPC
        if (status == 422) {
            return
        }

        throw new MpcException("Error while communicating with MPC, status: ${response.getStatus()}")
    }

    static void checkErrors(RestResponse response) {
        int status = response.getStatus()

        if ((status < 200 || status >= 300)) {
            handleErrors(response)
        }
    }
}
