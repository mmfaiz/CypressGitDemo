package com.matchi.protocol

import grails.converters.JSON
import grails.converters.XML

abstract class AbstractApiController {

    protected def renderResponse(def feed) {
        response.setCharacterEncoding("UTF-8")

        if(params.type && params.type.equals("xml")) {
            response.setContentType("text/xml")
            render new XML(feed);
        } else {
            response.setContentType("application/json")
            render new JSON(feed);
        }
    }

    protected abstract AbstractRequest.RequestType getRequestType();
}

