package com.matchi.api

import org.springframework.validation.Errors

class InputErrorAPIException extends APIException {
    Errors errors

    InputErrorAPIException(Errors errors) {
        super(400, Code.INPUT_ERROR, errors.toString())
        this.errors = errors
        composeErrorMessage(this.errors)
    }

    private def composeErrorMessage(Errors err) {
        def buffer = new StringBuffer()
        err.fieldErrors.each {
            buffer.append it.field + " "
        }

        this.userMessage = buffer.toString()
    }

}
