package com.matchi.api

import grails.converters.JSON

class APIErrorHandlerController extends GenericAPIController {

    def index() {
        def exception = request.exception

        if(exception && exception?.cause instanceof APIException) {

            APIException apiException = (APIException) exception.cause
            response.setStatus(apiException.status)
            render apiException as JSON

        } else {
            response.setStatus(500)
        }

        def result = [message: "UNKNOWN"]
        render result as JSON
    }

    def notFound() {
        error(404, Code.RESOURCE_NOT_FOUND, "Resource not found")
    }
}
