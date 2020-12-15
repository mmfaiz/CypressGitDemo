package com.matchi.mpc

import com.matchi.Booking
import com.matchi.BookingException
import grails.plugins.rest.client.RestResponse
import org.springframework.http.HttpStatus

import javax.annotation.PostConstruct

class MpcService {
    static transactional = true

    def notificationService
    def grailsApplication
    MpcClient client

    def add(Booking booking) {
        log.debug("Adding codeRequest")

        CodeRequest codeRequest = new CodeRequest()
        codeRequest.booking = booking

        try {
            RestResponse response = client.add(booking)
            codeRequest.mpcId = response?.json?.id
            codeRequest.code = response?.json?.code
        } catch (Throwable throwable) {
            throw new BookingException("Could not establish connection to MPC", throwable)
        }

        if (codeRequest?.code && codeRequest?.save(failOnError: true)) {
            log.debug("Got code ${codeRequest?.code}")
            return codeRequest
        }

        return null
    }

    CodeRequest addPending(Long codeRequestId) {
        def codeRequest = CodeRequest.findById(codeRequestId,
                [lock: true, fetch: [booking: "join"]])

        try {
            RestResponse response = client.add(codeRequest.booking)
            codeRequest.mpcId = response?.json?.id
            codeRequest.code = response?.json?.code
            codeRequest.status = CodeRequest.Status.UNVERIFIED
        } catch (Throwable throwable) {
            throw new BookingException("Could not establish connection to MPC", throwable)
        }

        if (codeRequest?.code && codeRequest?.save(failOnError: true)) {
            log.debug("Got code ${codeRequest?.code}")
            return codeRequest
        } else {
            codeRequest.delete()
        }

        return null
    }

    void queue(Booking booking) {
        CodeRequest codeRequest = new CodeRequest(status: CodeRequest.Status.PENDING)
        codeRequest.booking = booking
        codeRequest.save()
    }

    void queue(List<Booking> bookings) {
        CodeRequest.withTransaction {
            bookings.each { Booking booking ->
                queue(booking)
            }
        }
    }

    def move(Booking booking) {
        log.debug("Move codeRequest")

        CodeRequest codeRequest = CodeRequest.findByBooking(booking)

        // If we have no CodeRequest, due to for example moving from non-MPC court to MPC-court, we add it
        if(!codeRequest) {
            log.info "Adding booking ${booking?.id} since no CR found"
            return add(booking)
        }

        // Don't call MPC if it is pending, not necessary
        if(codeRequest.status == CodeRequest.Status.PENDING) {
            return codeRequest
        }

        try {
            RestResponse response = client.move(booking)

            // We tried to move CR to a court without MPC settings
            if(response.statusCode == HttpStatus.UNPROCESSABLE_ENTITY) {
                codeRequest.delete()
                return null
            }

            codeRequest.status = CodeRequest.Status.UNVERIFIED
        } catch (Throwable throwable) {
            throw new BookingException("Could not establish connection to MPC", throwable)
        }

        if (codeRequest?.save(failOnError: true)) {
            log.debug("Got code ${codeRequest?.code}")
            return codeRequest
        }

        return null
    }

    def tryDelete(Booking booking) {
        try {
            def facility = booking?.customer?.facility

            if(facility?.hasMPC()) {
                CodeRequest codeRequest = CodeRequest.findByBooking(booking)
                if(codeRequest) {
                    if(codeRequest.status != CodeRequest.Status.PENDING) {
                        client.delete(booking?.id)
                    }
                    CodeRequest.executeUpdate("delete CodeRequest cr where cr.booking = ?", [booking])
                }
            }
        } catch (Throwable t) {
            log.error(t)
        }
    }

    def get(CodeRequest codeRequest) {
        try {
            RestResponse response = client.verify(codeRequest?.booking?.id)
            return response
        } catch(Throwable t) {
            log.error(t)
        }
    }

    def verify(CodeRequest codeRequest) {

        try {
            if(!Booking.get(codeRequest?.booking?.id)) {
                log.info("No booking with ${codeRequest?.booking?.id} found for CodeRequest, deleting")
                codeRequest.delete()
                return
            }

            RestResponse response = client.verify(codeRequest?.booking?.id)

            switch (response?.json?.status[0] as CodeRequest.Status) {
                case CodeRequest.Status.UNVERIFIED:
                    codeRequest.status = CodeRequest.Status.UNVERIFIED
                    break
                case CodeRequest.Status.VERIFIED:
                    codeRequest.status = CodeRequest.Status.VERIFIED
                    break
                case CodeRequest.Status.FAILED:
                    codeRequest.status = CodeRequest.Status.FAILED
                    break
                default:
                    break
            }

            codeRequest.save(failOnError: true)
        } catch (Throwable t) {
            log.error(t)
        }
    }

    void verifyMultiple(List<Long> bookingIds) {
        try {
            // Send the list of booking id's to MPC
            RestResponse response = client.verifyMultiple(bookingIds)

            // Iterate all the CodeRequests we got back from MPC, find it by its MPC id and update it.
            response?.json?.each {
                try {
                    CodeRequest codeRequest = CodeRequest.findByMpcId(it.id, [lock: true])

                    if (codeRequest != null) {
                        switch (it.status as CodeRequest.Status) {
                            case CodeRequest.Status.UNVERIFIED:
                                codeRequest.status = CodeRequest.Status.UNVERIFIED
                                break
                            case CodeRequest.Status.VERIFIED:
                                codeRequest.status = CodeRequest.Status.VERIFIED
                                break
                            case CodeRequest.Status.FAILED:
                                codeRequest.status = CodeRequest.Status.FAILED
                                break
                            default:
                                break
                        }
                        codeRequest.save(failOnError: true)
                    }
                } catch (Exception e) {
                    log.error("Unable to update CodeRequest with id " + it.id + ". Continuing with next CodeRequest.", e)
                }
            }

        }  catch (Throwable t) {
            log.error("An error occurred when trying to validate CodeRequests.", t)
        }
    }

    RestResponse listFuture(Long facilityId) {
        try {
            return client.listFuture(facilityId)
        } catch (Throwable t) {
            log.error(t)
        }
    }

    RestResponse resend(Long facilityId) {
        try {
            return client.resend(facilityId)
        } catch (Throwable t) {
            log.error(t)
        }
    }

    RestResponse reset(Long facilityId) {
        try {
            return client.reset(facilityId)
        } catch (Throwable t) {
            log.error(t)
        }
    }

    def getNode(Long facilityId) {
        try {
            return client.getNode(facilityId)?.json
        } catch (Throwable t) {
            log.error(t)
        }
    }

    def createNode(Long facilityId, Long provider) {
        try {
            return client.createNode(facilityId, provider)?.status
        } catch (Throwable t) {
            log.error(t)
        }
    }

    def updateNode(Long facilityId, Map<String, String> conf) {
        try {
            return client.updateNode(facilityId, conf)?.status
        } catch (Throwable t) {
            log.error(t)
        }
    }

    def getProviderStatus(Long providerId) {
        try {
            return client.getProviderStatus(providerId)?.json
        } catch (Throwable t) {
            log.error(t)
        }
    }

    def listProviders() {
        try {
            return client.listProviders()?.json
        } catch (Throwable t) {
            log.error(t)
        }
    }

    def listNodes() {
        try {
            return client.listNodes()?.json
        } catch (Throwable t) {
            log.error(t)
        }
    }

    @PostConstruct
    public void configure() {
        def host     = grailsApplication.config?.matchi?.mpc?.host
        def user     = grailsApplication.config?.matchi?.mpc?.username
        def password = grailsApplication.config?.matchi?.mpc?.password

        log.info("Configuring MPC client at ${host} with user ${user}")
        this.client = new MpcClient(host, user, password)
    }
}
