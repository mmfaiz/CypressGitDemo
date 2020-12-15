package com.matchi.marshallers

import com.matchi.api.APIException
import com.matchi.api.Code
import grails.converters.JSON

import javax.annotation.PostConstruct

class ExceptionMarshaller {

    @PostConstruct
    void register() {
        // Standard Node marshall
        JSON.registerObjectMarshaller(Exception) { Exception exception ->
            marshallException(exception)
        }
    }

    def marshallException(Exception exception) {
        if(exception instanceof APIException) {
            [
                    status: exception.status,
                    code: exception.errorCode?.toString(),
                    message: exception.userMessage
            ]
        } else {
            [
                    status: 500,
                    code: Code.UNKNOWN_ERROR.toString(),
                    message: "Error occured during request"
            ]
        }
    }
}
