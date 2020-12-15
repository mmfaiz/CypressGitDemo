package com.matchi.api_ext


import com.matchi.api.GenericAPIController
import grails.converters.JSON

class APIExtHealthController extends GenericAPIController {

    def health() {
        def response = [
                status: "UP"
        ]

        render response as JSON
    }

}
