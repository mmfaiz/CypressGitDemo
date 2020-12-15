package com.matchi.matex

import grails.plugins.rest.client.RestBuilder
import grails.plugins.rest.client.RestResponse
import org.joda.time.LocalDate

class MatexClient {

    String username
    String password
    String host

    RestBuilder rest

    MatexClient(String username, String password, String host) {
        this.username = username
        this.password = password
        this.host = host

        rest = new RestBuilder(connectTimeout:3000, readTimeout:20000)
    }

    RestResponse send(String _sender, String _receiver, String _text, String _clientRef) {

        RestResponse response = rest.post(host + "/messages") {
            auth username, password
            json {
                sender = _sender
                text = _text
                receiver = _receiver
                clientRef = _clientRef
            }
        }

        checkErrors(response)

        return response
    }

    RestResponse report(LocalDate from, LocalDate to) {
        RestResponse response = rest.get(host + "/reports/${from}/${to}") {
            auth username, password
        }

        checkErrors(response)

        return response
    }

    void handleErrors(RestResponse response) {
        throw new MatexException("Error while communicating with Matex, status: ${response.getStatus()}")
    }

    void checkErrors(RestResponse response) {
        int status = response.getStatus()

        if (status < 200 || status >= 300) {
            handleErrors(response)
        }
    }



}
